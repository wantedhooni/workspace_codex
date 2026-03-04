FROM eclipse-temurin:21-jdk-jammy AS build

ARG APP_MODULE

WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle build.gradle gradle.properties ./
COPY common common
COPY services services
COPY config-repo config-repo

RUN chmod +x gradlew
RUN ./gradlew :services:${APP_MODULE}:bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy

ARG APP_MODULE
ARG APP_PORT

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/services/${APP_MODULE}/build/libs/*.jar /app/app.jar

EXPOSE ${APP_PORT}

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-} -jar /app/app.jar"]

