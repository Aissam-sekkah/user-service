# syntax=docker/dockerfile:1

# Stage 1: Build
# Pin to a specific tag; for fully reproducible builds, pin by digest (@sha256:...).
FROM gradle:8.7-jdk21-alpine AS build
WORKDIR /app
# Copy build descriptors first so dependency resolution is cached across source-only changes.
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon > /dev/null 2>&1 || true
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user.
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the jar owned by the runtime user (read-only to it).
COPY --from=build --chown=spring:spring /app/build/libs/*.jar app.jar

USER spring:spring
EXPOSE 8080

# Liveness probe against the public actuator health endpoint (busybox wget ships with alpine).
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Container-aware heap sizing; respects cgroup memory limits.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
