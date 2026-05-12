# =============================================================================
# HOEM — Dockerfile (Java Spring Boot service template)
# Copy this file to each service directory and replace SERVICE_NAME
# Multi-stage build: build with Maven, run with JRE only
# =============================================================================

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml first and download dependencies (Docker cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline --no-transfer-progress

# Copy source and build
COPY src ./src
RUN mvn package -DskipTests --no-transfer-progress

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S hoem && adduser -S hoem -G hoem

# Copy JAR from build stage
COPY --from=builder /app/target/*.jar app.jar

# Set ownership
RUN chown hoem:hoem app.jar

USER hoem

# Expose service port (override per service)
EXPOSE 8080

# Run with configurable JVM options
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
