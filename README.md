# 🚗 Catalogo de Produtos para Oficina Mecânica Dinoco

Microserviço de catalogo de produtos.

## Execução local - rabbitmq

Inicie uma única instância do broker para os dois projetos:

```sh
docker compose -f infra-local/compose.rabbitmq.yaml up -d
```

Painel: http://localhost:15672 — usuário oficina, senha oficina-local, SOMENTE desenvolvimento.


Iniciar o container - Subir API e banco

	docker compose up --build -d 
