FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /build

# Cache dependencies
COPY marginalia/pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the application
COPY marginalia/src ./src
COPY .git ./.git
RUN cp src/main/resources/META-INF/VAADIN/config/flow-build-info.json.PRODUCTION \
       src/main/resources/META-INF/VAADIN/config/flow-build-info.json
RUN mvn clean package -DskipTests

FROM jetty:12-jre21-eclipse-temurin

USER jetty
WORKDIR $JETTY_BASE

RUN java -jar "$JETTY_HOME/start.jar" --create-startd \
    --add-modules=server,http,ee10-deploy,ee10-websocket-jakarta,ee10-webapp

COPY --from=builder /build/target/*.war $JETTY_BASE/webapps/ROOT.war

USER root
RUN mkdir -p /var/marginalia/data && chown -R jetty:jetty /var/marginalia
USER jetty

EXPOSE 8080

CMD ["java", "-jar", "/usr/local/jetty/start.jar"]