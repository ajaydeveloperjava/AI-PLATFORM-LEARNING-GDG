# ---------- STAGE 1: Build Spring Boot App ----------
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ---------- STAGE 2: Runtime ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy Spring Boot JAR
COPY --from=build /app/target/*.jar app.jar

# Expose Spring Boot port
EXPOSE 8080

# Start Spring Boot
CMD ["java", "-jar", "app.jar"]
