import base64
import importlib.util
import json
import os
from pathlib import Path
import subprocess
import unittest
from unittest.mock import patch

import yaml

ROOT = Path(__file__).resolve().parents[2]
spec = importlib.util.spec_from_file_location("deploy", ROOT / "scripts/deploy-k8s.py")
deploy = importlib.util.module_from_spec(spec)
spec.loader.exec_module(deploy)


class DeployTest(unittest.TestCase):
    def setUp(self):
        self.env = patch.dict(os.environ, {
            "DOCKER_IMAGE": "123.dkr.ecr.us-east-1.amazonaws.com/app:commit",
            "OPENAPI_SERVER_URL": "https://api.example.test",
            "NEW_RELIC_LICENSE_KEY": "test-license", "CATALOGO_API_KEY": "test-key",
            "DEPLOY_REVISION": "42-2", "AWS_REGION": "us-east-1",
        }, clear=True)
        self.env.start()
        self.addCleanup(self.env.stop)
        self.writes = []

    def fake_kubectl(self, *args, input=None):
        if input:
            self.writes.append(json.loads(input))
        if args[:2] == ("get", "nodes"):
            return json.dumps({"items": [{"status": {"conditions": [
                {"type": "Ready", "status": "True"}]}}]})
        if args[:2] == ("get", "secret"):
            return json.dumps({"data": {key: base64.b64encode(value.encode()).decode()
                for key, value in {"SPRING_DATASOURCE_URL": "jdbc:test",
                    "SPRING_DATASOURCE_USERNAME": "app", "SPRING_DATASOURCE_PASSWORD": "pwd",
                    "username": "rabbit", "password": "pwd", "CATALOGO_API_KEY": "test-key"}.items()}})
        return ""

    def test_no_ready_node_changes_nothing(self):
        with patch.object(deploy, "kubectl", return_value='{"items": []}'), \
                patch.object(deploy, "secret") as secret:
            with self.assertRaisesRegex(ValueError, "Nenhum no Ready"):
                deploy.main()
            secret.assert_not_called()

    def test_missing_database_secret_changes_nothing(self):
        def fake(*args, **kwargs):
            if args[:3] == ("get", "secret", deploy.DATABASE_SECRET):
                raise subprocess.CalledProcessError(1, ["kubectl"])
            return self.fake_kubectl(*args, **kwargs)
        with patch.object(deploy, "kubectl", side_effect=fake):
            with self.assertRaises(subprocess.CalledProcessError):
                deploy.main()
        self.assertEqual([], self.writes)

    def test_missing_license_fails_before_cluster_access(self):
        del os.environ["NEW_RELIC_LICENSE_KEY"]
        with patch.object(deploy, "kubectl") as kubectl:
            with self.assertRaisesRegex(ValueError, "NEW_RELIC_LICENSE_KEY"):
                deploy.main()
            kubectl.assert_not_called()

    def test_api_key_conflict_does_not_overwrite_secrets(self):
        if not deploy.CATALOG:
            self.skipTest("A chave compartilhada e gerenciada pelo catalogo")
        os.environ["CATALOGO_API_KEY"] = "different-key"
        with patch.object(deploy, "kubectl", side_effect=self.fake_kubectl):
            with self.assertRaisesRegex(ValueError, "Rotacao"):
                deploy.main()
        self.assertEqual([], self.writes)

    def test_success_keeps_database_and_broker_secrets_owned_by_infra(self):
        with patch.object(deploy, "kubectl", side_effect=self.fake_kubectl), \
                patch.object(deploy.subprocess, "check_output", return_value="jwt-test"), \
                patch("builtins.print"):
            deploy.main()
        secrets = [obj for obj in self.writes if obj["kind"] == "Secret"]
        self.assertNotIn(deploy.DATABASE_SECRET, [obj["metadata"]["name"] for obj in secrets])
        self.assertNotIn("rabbitmq-secret", [obj["metadata"]["name"] for obj in secrets])
        app_secret = next(obj for obj in secrets if obj["metadata"]["name"] == deploy.APP + "-secret")
        self.assertFalse(any(key.startswith("SPRING_DATASOURCE") for key in app_secret["data"]))
        if not deploy.CATALOG:
            self.assertEqual([deploy.APP + "-secret"], [obj["metadata"]["name"] for obj in secrets])
            self.assertIn("JWT_SECRET", app_secret["data"])

    def test_manifests_route_to_correct_port_and_use_separate_credentials(self):
        objects = {obj["kind"]: obj for obj in deploy.manifests("image:sha", "https://host", "1-2")}
        pod = objects["Deployment"]["spec"]["template"]
        container = pod["spec"]["containers"][0]
        service = objects["Service"]
        self.assertEqual(pod["metadata"]["labels"], service["spec"]["selector"])
        self.assertEqual(container["ports"][0]["containerPort"], service["spec"]["ports"][0]["targetPort"])
        self.assertEqual("image:sha", container["image"])
        self.assertEqual("1-2", pod["metadata"]["annotations"]["oficina.dinoco/deploy-revision"])
        secret_names = [item["secretRef"]["name"] for item in container["envFrom"] if "secretRef" in item]
        self.assertIn(deploy.DATABASE_SECRET, secret_names)
        self.assertIn("catalogo-integration-secret", secret_names)
        self.assertEqual("k8s,rabbit", objects["ConfigMap"]["data"]["SPRING_PROFILES_ACTIVE"])
        self.assertNotIn("SPRING_DATASOURCE_URL", objects["ConfigMap"]["data"])
        profile = yaml.safe_load((ROOT / "src/main/resources/application-k8s.yaml").read_text())
        self.assertEqual("readinessState,db", profile["management"]["endpoint"]["health"]["group"]["readiness"]["include"])
        for probe in ("startupProbe", "readinessProbe", "livenessProbe"):
            self.assertIn(probe, container)
            if deploy.CATALOG:
                self.assertIn('$CATALOGO_API_KEY', container[probe]["exec"]["command"][-1])
        self.assertNotIn('${', json.dumps(objects))

    def test_rollout_failure_propagates(self):
        def fake(*args, **kwargs):
            if args[:3] == ("rollout", "status", "deployment/" + deploy.APP + "-deployment"):
                raise subprocess.CalledProcessError(1, ["kubectl"])
            return self.fake_kubectl(*args, **kwargs)
        with patch.object(deploy, "kubectl", side_effect=fake), \
                patch.object(deploy.subprocess, "check_output", return_value="jwt-test"):
            with self.assertRaises(subprocess.CalledProcessError):
                deploy.main()


if __name__ == "__main__":
    unittest.main()
