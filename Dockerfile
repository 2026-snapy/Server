# syntax=docker/dockerfile:1.7

# ---------- Build stage ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Gradle wrapper & 설정 먼저 복사 (레이어 캐시 활용)
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies || true

# 소스 복사 후 빌드
COPY src src
RUN ./gradlew --no-daemon clean bootJar -x test

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre AS runtime

ENV TZ=Asia/Seoul \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

RUN groupadd --system spring && useradd --system --gid spring spring

WORKDIR /app
COPY --from=build /workspace/build/libs/messi-of-coding-0.0.1-SNAPSHOT.jar app.jar
RUN chown -R spring:spring /app
USER spring

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
