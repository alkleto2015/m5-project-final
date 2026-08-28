# 1. Этап сбірки (Build stage)
FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /app

# Кэшування залежностей (включно Liquibase, MapStruct, Security)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходники и собираем артефакт под профилем prod
COPY src ./src
RUN mvn clean package -Pprod -DskipTests

# Етап запуска (Runtime stage)
FROM eclipse-temurin:17-jre
WORKDIR /app

# копіюємо зібраний JAR (jira-1.0.jar)
COPY --from=build /app/target/jira-1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
