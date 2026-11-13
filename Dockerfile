# ---------- Etapa 1: build (Maven + JDK) ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Etapa 2: imagem final (so o JRE + o .jar) ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --no-create-home appuser
COPY --from=build /app/target/rota-folego-api.jar app.jar
USER appuser
EXPOSE 8080
# DB_URL, DB_USER, DB_PASSWORD e CORS_ORIGINS sao definidas como variaveis de ambiente no Render
ENTRYPOINT ["java", "-jar", "app.jar"]
