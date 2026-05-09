# Build stage
FROM maven:3.9-eclipse-temurin-23 AS builder
WORKDIR /app
COPY back-end/potane/pom.xml .
COPY back-end/potane/src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:23-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/potane-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
