# Use uma imagem base do OpenJDK 17
FROM openjdk:17-jdk-slim

# Define o diretório de trabalho
WORKDIR /app

# Instala Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copia os arquivos de configuração do Maven primeiro (para cache de dependências)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Baixa as dependências (isso será cacheado se o pom.xml não mudar)
RUN mvn dependency:go-offline -B

# Copia o código fonte
COPY src ./src

# Compila a aplicação
RUN mvn clean package -DskipTests

# Expõe a porta 8081 (conforme configurado no application.properties)
EXPOSE 8081

# Define variáveis de ambiente padrão (serão sobrescritas pelo Render)
ENV SPRING_DATASOURCE_URL="jdbc:oracle:thin:@localhost:1521:xe"
ENV SPRING_DATASOURCE_USERNAME="user"
ENV SPRING_DATASOURCE_PASSWORD="password"

# Comando para executar a aplicação
CMD ["java", "-jar", "target/Geosense-0.0.1-SNAPSHOT.jar"]
