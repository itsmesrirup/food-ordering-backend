# Stage 1: Build the application AND run migrations
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven files first for layer caching
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Run Flyway migration during the build process.
# This requires the database credentials to be passed as build arguments.
# We will set these arguments on Render.
ARG SPRING_DATASOURCE_URL
ARG SPRING_DATASOURCE_USERNAME
ARG SPRING_DATASOURCE_PASSWORD
RUN ./mvnw flyway:migrate

# Package the application into a JAR file
RUN ./mvnw package -DskipTests


# Stage 2: Create the final, lightweight runtime image
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the JAR file from the 'build' stage
COPY --from=build /app/target/food-ordering-backend-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 8080

# The startup command is now simple again, as migrations are already done.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=80.0", "-jar", "app.jar"]