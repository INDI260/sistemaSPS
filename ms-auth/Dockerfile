# ── Etapa 1: compilar con Maven ──────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Descargar dependencias primero (capa cacheada)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Compilar
COPY src ./src
RUN mvn package -DskipTests -q

# ── Etapa 2: imagen de ejecución ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/MSAuth.war app.war
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.war"]
