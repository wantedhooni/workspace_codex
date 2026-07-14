# syntax=docker/dockerfile:1.7@sha256:a57df69d0ea827fb7266491f2813635de6f17269be881f696fbfdf2d83dda33e

ARG BUILD_IMAGE=eclipse-temurin:21-jdk-alpine@sha256:1ff763083f2993d57d0bf374ab10bb3e2cb873af6c13a04458ebbd3e0337dc76
ARG RUNTIME_IMAGE=eclipse-temurin:21-jre-alpine@sha256:3f08b13888f595cc49edabea7250ba69499ba25602b267da591720769400e08c

FROM ${BUILD_IMAGE} AS builder

ARG MODULE

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle gradle.properties ./
COPY gradle ./gradle
COPY api-gateway/build.gradle ./api-gateway/build.gradle
COPY user-service/build.gradle ./user-service/build.gradle
COPY account-service/build.gradle ./account-service/build.gradle
COPY contents-service/build.gradle ./contents-service/build.gradle
COPY common-metrics/build.gradle ./common-metrics/build.gradle
COPY common-security/build.gradle ./common-security/build.gradle

RUN chmod +x gradlew \
    && case "${MODULE}" in \
         api-gateway|user-service|account-service|contents-service) ;; \
         *) echo "지원하지 않는 MODULE입니다: ${MODULE}" >&2; exit 1 ;; \
       esac

RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    ./gradlew ":${MODULE}:dependencies" \
      --configuration runtimeClasspath \
      --no-daemon \
      --quiet \
      > /dev/null

COPY common-metrics/src ./common-metrics/src
COPY common-security/src ./common-security/src
COPY ${MODULE}/src ./${MODULE}/src

RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    ./gradlew ":${MODULE}:bootJar" --no-daemon --quiet \
    && find "${MODULE}/build/libs" \
         -maxdepth 1 \
         -type f \
         -name '*.jar' \
         ! -name '*-plain.jar' \
         -exec cp '{}' /tmp/application.jar ';' \
    && test -s /tmp/application.jar

FROM ${RUNTIME_IMAGE}

ARG MODULE
ARG APP_PORT=8080
ARG APP_VERSION=local

LABEL org.opencontainers.image.title="${MODULE}" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.vendor="commerce"

RUN addgroup -S -g 1000 application \
    && adduser -S -D -H -u 1000 -G application application \
    && mkdir -p /app \
    && chown -R application:application /app

WORKDIR /app

COPY --from=builder --chown=application:application /tmp/application.jar /app/application.jar

USER 1000:1000

EXPOSE ${APP_PORT}

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-Dfile.encoding=UTF-8", "-jar", "/app/application.jar"]
