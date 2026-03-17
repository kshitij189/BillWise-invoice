# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/billwise-server-1.0.0.jar app.jar
# Render sets the PORT environment variable automatically
CMD ["java", "-jar", "app.jar"]
