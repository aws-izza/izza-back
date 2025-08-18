# 1. Build stage
FROM gradle:8.7-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# 2. Runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# build/libs 안에 jar 파일 생성됨
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]