# Build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy Gradle wrapper and build files first (these change less frequently)
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle ./
COPY settings.gradle* ./

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies first (this layer will be cached unless build.gradle changes)
RUN ./gradlew dependencies --no-daemon

# Copy source code (this layer will be invalidated when source changes)
COPY src ./src

# Build the application
RUN ./gradlew clean bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

ENV JAVA_OPTS="-XX:+UseZGC -Dspring.threads.virtual.enabled=true"

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-docker}"]
