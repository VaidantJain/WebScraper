# Use OpenJDK 21 as the base image
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container (replace with your actual JAR name)
COPY target/webScraper-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8084
EXPOSE 8084

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
