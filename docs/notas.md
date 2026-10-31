21/10

Criado projeto "catalogo-produto-api" para o microservico;

Alterado versão do maven, para 3.9.4, para corrigir problema de build.
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.14/apache-maven-3.9.14-bin.zip

Copiando .yaml do projeto;

Banco url: jdbc:postgresql://localhost:15433/oficina_produtos_db

Arquitetura do projeto: Clean Architecture, copiado modulo de produtos.
Flyway: criando script de migração para criar tabela de produtos

25/10

Possíveis controles de Saga;

1 - Ao criar um produto, criar um evento de produto criado, e enviar para o RabbitMQ, 
para que o microserviço de estoque possa consumir e criar o estoque do produto. 


OBSERVACOES:

[X] Refatorando ProdutoQueryGatewayImpl, para retornar o produto sem o estoque;
Será retornado apenas os dados do catalogo, a consulta completa será feita na aplicacao principal com alguma
estrategia de composicao de dados, para evitar acoplamento entre os microservicos.

[] Implementado RabbitMQ, para enviar eventos de produto cadastrado, e o microserviço de estoque consumir e criar o estoque do produto.
-- CriarProdutoHandler
-- ProdutoCadastradoEvent


27/10 : 

OK RABBIT : Microservicos de produtos já está salvando os dados na fila e na tabela especifica;
OK RABBIT : Projeto núcleo está lendo a fila e atualizando estoque corretamente;
OK Desvincular o dominio do produto dos Itens da OS, do estoque.
OK Quando conseguir subir o servico, ver se o estoque consegue consumir o evento e criar o estoque do produto.

