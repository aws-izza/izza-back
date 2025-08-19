# 간단한 Spring Boot Dockerfile
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Gradle wrapper 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# 실행 권한 부여
RUN chmod +x gradlew

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN ./gradlew bootJar -x test

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
CMD ["java", "-jar", "build/libs/izza-back-0.0.1-SNAPSHOT.jar"]