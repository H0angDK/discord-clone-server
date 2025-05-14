FROM gradle:jdk23 AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
RUN gradle --version
COPY src ./src
RUN gradle bootJar -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:23-jdk
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]