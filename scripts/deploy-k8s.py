"""Deploy executado pelo workflow, com credenciais recebidas pelo ambiente."""
import base64
import json
import os
from pathlib import Path
import subprocess
import sys
from urllib.parse import urlparse

import yaml

ROOT = Path(__file__).resolve().parents[1]
APP = "catalogo-produto-api"
DATABASE_SECRET = "catalogo-produto-database-secret"
CATALOG = APP == "catalogo-produto-api"


def kubectl(*args, input=None):
    return subprocess.check_output(
        ["kubectl", "-n", os.environ.get("K8S_NAMESPACE", "default"), *args],
        input=input, text=True)


def required(name):
    value = os.environ.get(name, "")
    if not value.strip():
        raise ValueError(f"Configure {name} antes do deploy.")
    return value


def read_secret(name, keys):
    raw = kubectl("get", "secret", name, "-o", "json")
    data = json.loads(raw).get("data", {})
    if any(not data.get(key) for key in keys):
        raise ValueError(f"Secret {name} incompleto: esperado {', '.join(keys)}.")
    return data


def apply(resource):
    # JSON via stdin: valores secretos nao aparecem nos argumentos do processo.
    kubectl("apply", "-f", "-", input=json.dumps(resource))


def secret(name, data):
    apply({"apiVersion": "v1", "kind": "Secret",
           "metadata": {"name": name}, "type": "Opaque",
           "data": {key: base64.b64encode(value.encode()).decode()
                    for key, value in data.items()}})


def manifests(image, public_url, revision):
    documents = []
    for filename in ("configmap.yaml", "service.yaml", "deployment.yaml", "hpa.yaml"):
        obj = yaml.safe_load((ROOT / "k8s" / filename).read_text())
        if obj["kind"] == "ConfigMap":
            obj["data"]["OPENAPI_SERVER_URL"] = public_url
        if obj["kind"] == "Deployment":
            obj["spec"]["template"]["spec"]["containers"][0]["image"] = image
            # Uma nova execucao reaplica ConfigMaps/Secrets ao criar novos Pods.
            obj["spec"]["template"]["metadata"].setdefault("annotations", {})[
                "oficina.dinoco/deploy-revision"] = revision
        documents.append(obj)
    return documents


def main():
    image = required("DOCKER_IMAGE")
    public_url = required("OPENAPI_SERVER_URL")
    if urlparse(public_url).scheme not in ("http", "https") or not urlparse(public_url).netloc:
        raise ValueError("OPENAPI_SERVER_URL deve ser uma URL HTTP(S) completa.")
    license_key = required("NEW_RELIC_LICENSE_KEY")
    api_key = required("CATALOGO_API_KEY") if CATALOG else None
    revision = required("DEPLOY_REVISION")
    # Falhar antes de alterar qualquer recurso quando nao houver no pronto.
    nodes = json.loads(kubectl("get", "nodes", "-o", "json"))["items"]
    if not any(not node.get("spec", {}).get("unschedulable", False)
               and any(c["type"] == "Ready" and c["status"] == "True"
                       for c in node.get("status", {}).get("conditions", [])) for node in nodes):
        raise ValueError("Nenhum no Ready disponivel. Recupere o no antes do deploy.")
    read_secret(DATABASE_SECRET, ["SPRING_DATASOURCE_URL", "SPRING_DATASOURCE_USERNAME",
                                  "SPRING_DATASOURCE_PASSWORD"])
    read_secret("rabbitmq-secret", ["username", "password"])
    kubectl("rollout", "status", "statefulset/rabbitmq", "--timeout=180s")
    app_secrets = {"NEW_RELIC_LICENSE_KEY": license_key}
    if CATALOG:
        # A chave compartilhada e criada pelo catalogo; o nucleo apenas a consome.
        # Nao trocar silenciosamente: Pods existentes mantem o valor anterior.
        existing = kubectl("get", "secret", "catalogo-integration-secret",
                           "--ignore-not-found", "-o", "json")
        if existing.strip():
            stored = json.loads(existing).get("data", {}).get("CATALOGO_API_KEY", "")
            if base64.b64decode(stored).decode() != api_key:
                raise ValueError("CATALOGO_API_KEY diverge do Secret existente. Rotacao deve ser coordenada.")
    else:
        read_secret("catalogo-integration-secret", ["CATALOGO_API_KEY"])
        kubectl("rollout", "status", "deployment/catalogo-produto-api-deployment", "--timeout=180s")
        # Mesmo Secret AWS usado pelo Lambda; nao criar uma segunda chave JWT.
        jwt = subprocess.check_output([
            "aws", "secretsmanager", "get-secret-value", "--region", required("AWS_REGION"),
            "--secret-id", "oficina-auth-jwt-secret", "--query", "SecretString", "--output", "text"
        ], text=True).strip()
        if not jwt or jwt == "None":
            raise ValueError("Secret oficina-auth-jwt-secret vazio.")
        app_secrets["JWT_SECRET"] = jwt
    if CATALOG:
        secret("catalogo-integration-secret", {"CATALOGO_API_KEY": api_key})
    secret(f"{APP}-secret", app_secrets)
    for resource in manifests(image, public_url, revision):
        # Recursos nao secretos mantem o gerenciador padrao usado no projeto.
        kubectl("apply", "-f", "-", input=json.dumps(resource))
    kubectl("rollout", "status", f"deployment/{APP}-deployment", "--timeout=900s")
    print(kubectl("get", "pods,svc,hpa", "-l", f"app={APP}"))
    print(f"Deploy de {APP} concluido.")


if __name__ == "__main__":
    try:
        main()
    except ValueError as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)
    except subprocess.CalledProcessError:
        # Nao imprimir stdout capturado: pode conter dados de Secrets.
        print("Deploy falhou. Consulte o erro do comando e os eventos do Kubernetes.", file=sys.stderr)
        for args in (("get", "pods", "-l", f"app={APP}", "-o", "wide"),
                     ("get", "events", "--sort-by=.metadata.creationTimestamp")):
            try:
                print(kubectl(*args), file=sys.stderr)
            except subprocess.CalledProcessError:
                pass
        sys.exit(1)
