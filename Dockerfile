FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /build

# Cache dependencies
COPY marginalia/pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the application
COPY marginalia/src ./src
COPY .git ./.git
RUN mvn clean package -DskipTests

FROM jetty:12-jdk25-eclipse-temurin

USER jetty
WORKDIR $JETTY_BASE

RUN java -jar "$JETTY_HOME/start.jar" --create-startd \
    --add-modules=server,http,ee11-deploy,ee11-websocket-jakarta,ee11-webapp,ee11-jsp

COPY --from=builder /build/target/*.war $JETTY_BASE/webapps/ROOT.war

USER root
RUN mkdir -p /var/marginalia/.marginalia && chown -R jetty:jetty /var/marginalia
# no COPY --chmod: it needs BuildKit, which the classic builder of older docker/docker-compose v1 lacks
COPY docker-entrypoint.sh /usr/local/bin/marginalia-entrypoint.sh
RUN chmod 755 /usr/local/bin/marginalia-entrypoint.sh

# starts as root to fix the owner of the data folder (a bind mount created by Docker is owned by root), then runs
# Jetty as jetty, see docker-entrypoint.sh
EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/marginalia-entrypoint.sh"]
CMD ["java", "-jar", "/usr/local/jetty/start.jar"]