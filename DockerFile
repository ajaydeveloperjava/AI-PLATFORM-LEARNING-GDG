# ---------- STAGE 1: Build Spring Boot App ----------
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests


# ---------- STAGE 2: Runtime ----------
FROM ubuntu:22.04

WORKDIR /app

# Install Java & required tools
RUN apt-get update && apt-get install -y \
    curl \
    openjdk-17-jdk \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Install Ollama (CPU)
RUN curl -fsSL https://ollama.com/install.sh | sh

# Copy Spring Boot JAR
COPY --from=build /app/target/*.jar app.jar

# Pull a SMALL model (IMPORTANT for Render)
RUN ollama pull qwen:0.5b

# Expose Spring Boot port
EXPOSE 8080

# Start Ollama + Spring Boot
CMD ollama serve & java -jar app.jar
