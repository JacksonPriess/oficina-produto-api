# Catálogo de Produtos — Oficina Mecânica Dinoco

Microsserviço da **Oficina Mecânica Dinoco** responsável pelo cadastro, pela consulta, pela atualização e pela desativação dos produtos usados nas ordens de serviço. O núcleo consulta o catálogo por HTTP para obter os dados e o preço de venda do produto.

Ao cadastrar um produto, a API grava o produto e o evento `ProdutoCadastrado` no PostgreSQL na mesma transação. A tabela de eventos pendentes, chamada *outbox*, permite publicar no RabbitMQ em segundo plano. O núcleo consome o evento para inicializar o saldo de estoque.

## Tecnologias

- Java 21, Maven Wrapper e Spring Boot 4.0.6.
- Spring Web MVC para a API HTTP, Spring Security para a chave de API e Spring Boot Actuator para as verificações de saúde.
- PostgreSQL 16, Spring Data JPA e Hibernate para persistência. O Flyway aplica as migrações do banco.
- RabbitMQ e Spring AMQP para mensageria.
- Springdoc OpenAPI para documentação da API. JUnit 5, REST Assured e Testcontainers para testes.
- Docker e Docker Compose para execução local. O [guia de deploy no Kubernetes](docs/DEPLOY-K8S.md) descreve a publicação no ambiente AWS.

## Execução local

Instale o Docker com o Compose, inicie o Docker e execute os comandos na raiz do repositório.

### 1. Inicie o RabbitMQ compartilhado

O catálogo e o núcleo usam a mesma instância do RabbitMQ. Se o ambiente já tiver o broker disponível como `rabbitmq` na rede `dinoco-local`, siga para o próximo passo.

Se você usa o arquivo compartilhado de infraestrutura local, execute o comando apontando para o caminho onde ele está salvo.

```bash
docker compose -f infra-local/compose.rabbitmq.yaml up -d
```

Caso precise iniciar o broker diretamente, crie a rede e o contêiner com os comandos abaixo. Se a rede já existir, pule a primeira linha.

```bash
docker network create dinoco-local
docker run -d --name dinoco-rabbitmq --hostname dinoco-rabbitmq --network dinoco-local --network-alias rabbitmq -p 127.0.0.1:5672:5672 -p 127.0.0.1:15672:15672 -e RABBITMQ_DEFAULT_USER=oficina -e RABBITMQ_DEFAULT_PASS=oficina-local -v dinoco_rabbitmq_data:/var/lib/rabbitmq rabbitmq:4.1-management
```

O painel fica em [localhost:15672](http://localhost:15672). As credenciais `oficina` e `oficina-local` são para desenvolvimento local.

### 2. Inicie a API e o PostgreSQL

```bash
docker compose up --build -d
docker compose ps
docker compose logs -f api
```

| Recurso | Endereço ou configuração local |
| --- | --- |
| API | `http://localhost:8081` |
| PostgreSQL | `localhost:15434`, banco `oficina_produtos_db` |
| RabbitMQ | `rabbitmq:5672` pela rede `dinoco-local` |
| Chave da API | `CATALOGO_API_KEY`, com padrão local `chave-secreta-api-catalogo` |

Para executar a API pelo IntelliJ, inicie apenas o banco com `docker compose up -d postgres-produtos`, mantenha o RabbitMQ iniciado com a porta `5672` publicada e execute a aplicação com os perfis Spring `dev,rabbit`. Para parar os serviços deste Compose, use `docker compose down`.

## Rotas da API

Envie `X-API-Key` com o valor de `CATALOGO_API_KEY` nas requisições. A chave também é exigida nas rotas de documentação OpenAPI e do Actuator.

| Método | Rota | Operação |
| --- | --- | --- |
| POST | `/api/produtos` | Cadastrar |
| GET | `/api/produtos/{id}` | Consultar por ID |
| GET | `/api/produtos?busca=oleo` | Consultar por termo |
| PUT | `/api/produtos/{id}` | Atualizar |
| DELETE | `/api/produtos/{id}` | Desativar |

Para testar a API local pelo terminal, consulte os produtos com a chave de desenvolvimento.

```bash
curl -H "X-API-Key: chave-secreta-api-catalogo" http://localhost:8081/api/produtos
```

### Cobertura de Testes — Melhoria Futura

Foi criado no `pom.xml` o perfil Maven `cobertura-80`, que permite validar uma cobertura mínima de **80% das linhas de código**, utilizando o JaCoCo.

O perfil pode ser executado manualmente com o comando:

```bash
./mvnw clean verify -Pcobertura-80