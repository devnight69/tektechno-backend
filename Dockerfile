# Build stage with Gradle
FROM gradle:8.12.1-jdk21 AS build

# Set maintainer info
LABEL maintainer="kousik"

# Copy the project into the container
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

# Set JVM options for Gradle to increase heap size
ENV GRADLE_OPTS="-Xmx2g -Dorg.gradle.daemon=false"

# Build the project and verify the JAR is created
RUN gradle build --no-daemon
# Debugging step: List the files in the build/libs directory
RUN ls -l /home/gradle/src/build/libs

# Runtime stage
FROM eclipse-temurin:21-jdk-jammy

# Install netcat and any other required dependencies
RUN apt-get update && apt-get install -y netcat && rm -rf /var/lib/apt/lists/*

# Create directories for app and logs
RUN mkdir -p /app/logs

# Add a new user and set permissions
RUN useradd -ms /bin/bash tektechno

# Copy the built JAR from the build stage to the runtime stage
COPY --from=build /home/gradle/src/build/libs/payout-0.0.1-SNAPSHOT.jar /home/tektechno/payout-0.0.1-SNAPSHOT.jar

# Switch to the new user
USER tektechno

# Set working directory
WORKDIR /home/tektechno

# Run the application
CMD ["java", "-jar", "/home/tektechno/payout-0.0.1-SNAPSHOT.jar"]