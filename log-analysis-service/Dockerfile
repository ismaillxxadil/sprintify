# Use JDK 21 image
FROM eclipse-temurin:21-jdk-jammy

# Set working directory inside the container
WORKDIR /app

# Copy the jar file from the target folder
COPY target/*.jar app.jar

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]