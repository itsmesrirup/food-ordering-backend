# Stage 1: Build the application using Maven
# We use a specific Maven image that includes Java 21 (or your JDK version)
FROM maven:3.9-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper and pom.xml to leverage Docker layer caching
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Package the application into a JAR file
RUN ./mvnw package -DskipTests


# Stage 2: Create the final, lightweight runtime image
# We use a minimal Java Runtime Environment (JRE) image to keep the size small
FROM eclipse-temurin:21-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the JAR file from the 'build' stage into the final image
COPY --from=build /app/target/food-ordering-backend-0.0.1-SNAPSHOT.jar ./app.jar

# Copy the startup script into the container
COPY startup.sh .

# Make sure the script is executable inside the container
RUN chmod +x startup.sh

# Expose the port the application will run on
EXPOSE 8080

# The command to run when the container starts is now our script
ENTRYPOINT ["./startup.sh"]