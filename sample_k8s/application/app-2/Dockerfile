ARG GRADLE_PROJECT=:application:app-2
ARG MODULE_DIR=application/app-2

FROM eclipse-temurin:21-jdk AS build
ARG GRADLE_PROJECT

WORKDIR /workspace
COPY . .
RUN ./gradlew ${GRADLE_PROJECT}:bootJar --no-daemon -x test



FROM eclipse-temurin:21-jre
ARG MODULE_DIR

WORKDIR /app
COPY --from=build /workspace/${MODULE_DIR}/build/libs/*.jar app.jar

# EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]