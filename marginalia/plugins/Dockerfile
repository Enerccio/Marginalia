# Build environment for the plugins: builds and installs the application (the plugins compile against its classes
# JAR), then builds every plugin. Build context is the repository root, see docker-compose.yml.
#
# The finished JARs are copied to /output when the container runs, so mount a folder there
# (docker compose run --rm plugins).
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /build/marginalia

# Cache dependencies
COPY marginalia/pom.xml .
RUN mvn dependency:go-offline -B

# Build and install the application into the local Maven repository
COPY marginalia/src ./src
COPY .git /build/.git
RUN mvn install -DskipTests -B

# Build the plugins; each one is a separate Maven project
COPY marginalia/plugins ./plugins
RUN mkdir /jars \
    && for pom in plugins/*/pom.xml; do \
        (cd "$(dirname "$pom")" && mvn package -B) || exit 1; \
        cp "$(dirname "$pom")"/target/*.jar /jars/ || exit 1; \
    done

FROM alpine:3

COPY --from=builder /jars /jars

# copies the plugin JARs into the mounted folder, replacing the ones from an earlier build
CMD ["sh", "-c", "mkdir -p /output && rm -f /output/*.jar && cp /jars/*.jar /output/ && ls -l /output"]
