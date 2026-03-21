# syntax=docker/dockerfile:1

# ── Build stage ────────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

COPY schemas/ schemas/
COPY league-service/pom.xml league-service/pom.xml
COPY league-service/src league-service/src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -f league-service/pom.xml package \
        -DskipTests \
        -Dschema.registry.skip=true \
        -q

# ── Runtime stage ──────────────────────────────────────────────────────────────
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.24

ENV LANGUAGE='en_US:en'

COPY --chown=185 --from=build /workspace/league-service/target/quarkus-app/lib/      /deployments/lib/
COPY --chown=185 --from=build /workspace/league-service/target/quarkus-app/*.jar     /deployments/
COPY --chown=185 --from=build /workspace/league-service/target/quarkus-app/app/      /deployments/app/
COPY --chown=185 --from=build /workspace/league-service/target/quarkus-app/quarkus/  /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT ["/opt/jboss/container/java/run/run-java.sh"]
