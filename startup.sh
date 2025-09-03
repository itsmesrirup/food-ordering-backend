#!/bin/bash
# This script runs inside the Docker container on Render

# Exit immediately if a command exits with a non-zero status.
set -e

# 1. Run the Database Migration
# We use environment variables that will be set in Render.
# The 'mvnw' command is not available in the final image, so we use 'java -cp'
# to run the Flyway migration directly. This is an advanced but effective technique.
# We need to tell the app to enable Flyway for this one command.
echo "--- Starting Database Migration ---"
java -Dspring.flyway.enabled=true \
     -Dspring.datasource.url=$SPRING_DATASOURCE_URL \
     -Dspring.datasource.username=$SPRING_DATASOURCE_USERNAME \
     -Dspring.datasource.password=$SPRING_DATASOURCE_PASSWORD \
     -jar app.jar \
     --spring.flyway.locations=classpath:db/migration

# The above command will exit after migrations are done.
# If the migration fails, the 'set -e' will cause the script to stop here.
echo "--- Database Migration Finished ---"


# 2. Start the main Spring Boot Application
# The application itself will run with flyway disabled (as per application.properties)
echo "--- Starting Spring Boot Application ---"
exec java -XX:MaxRAMPercentage=80.0 -jar app.jar