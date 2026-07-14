ARG GRADLE_PROJECT=:shared-server:api-gateway
ARG MODULE_DIR=shared-server/api-gateway

FROM eclipse-temurin:21-jdk AS build
ARG GRADLE_PROJECT

WORKDIR /workspace
COPY . .
RUN ls -al
RUN ./gradlew ${GRADLE_PROJECT}:bootJar --no-daemon -x test



FROM eclipse-temurin:21-jre
ARG MODULE_DIR
WORKDIR /app

COPY --from=build /workspace/${MODULE_DIR}/build/libs/*.jar app.jar

# EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]