# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/billwise-server-1.0.0.jar app.jar
# Render sets the PORT environment variable automatically
CMD ["java", "-jar", "app.jar"]
