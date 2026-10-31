# Estágio 1: Build da aplicação
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compila o projeto ignorando os testes para acelerar o build
RUN mvn clean package -DskipTests

# Estágio 2: Imagem final de execução
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copia a aplicação
COPY --from=build /app/target/*.jar app.jar
# Copia o agente New Relic
COPY newrelic/newrelic.jar /app/newrelic/newrelic.jar
COPY newrelic/newrelic.yml /app/newrelic/newrelic.yml

# Define variável de ambiente, específica do agente newrelic
# Significa Standard Output (Saída Padrão).
# Em vez de gravar os logs em um arquivo físico no disco (como /var/log/newrelic.log), isso obriga o agente a imprimir os logs diretamente na tela/console do sistema.
ENV NEW_RELIC_LOG_FILE_NAME=STDOUT

EXPOSE 8081
ENTRYPOINT ["java","-javaagent:/app/newrelic/newrelic.jar","-jar","app.jar"]