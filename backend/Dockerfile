# ==========================================
# DevType Backend Multi-stage Dockerfile
# docs/19-devops-deployment.md §3.1
# ==========================================

# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN ./gradlew dependencies --no-daemon || true

COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# Extract layers for efficient Docker layer caching
RUN java -Djarmode=tools -jar build/libs/*.jar extract --layers --destination extracted

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create non-root user (uid 10001)
RUN addgroup -g 10001 devtype && \
    adduser -u 10001 -G devtype -s /bin/sh -D devtype

WORKDIR /app

# Copy layered jar
COPY --from=builder --chown=devtype:devtype /workspace/extracted/dependencies/ ./
COPY --from=builder --chown=devtype:devtype /workspace/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=devtype:devtype /workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=devtype:devtype /workspace/extracted/application/ ./

USER 10001:10001

EXPOSE 8080 8081

HEALTHCHECK --interval=10s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+ExitOnOutOfMemoryError", "org.springframework.boot.loader.launch.JarLauncher"]
