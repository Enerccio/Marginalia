FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /build

# Cache dependencies
COPY marginalia/pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the application
COPY marginalia/src ./src
COPY .git ./.git
RUN rm src/main/resources/log4j.properties
RUN cp src/main/resources/log4j.properties.RELEASE \
       src/main/resources/log4j.properties
RUN mvn clean package -DskipTests

FROM jetty:12-jdk25-eclipse-temurin

USER jetty
WORKDIR $JETTY_BASE

RUN java -jar "$JETTY_HOME/start.jar" --create-startd \
    --add-modules=server,http,ee11-deploy,ee11-websocket-jakarta,ee11-webapp,ee11-jsp

COPY --from=builder /build/target/*.war $JETTY_BASE/webapps/ROOT.war

USER root
RUN mkdir -p /var/marginalia/data && chown -R jetty:jetty /var/marginalia
USER jetty

EXPOSE 8080

CMD ["java", "-jar", "/usr/local/jetty/start.jar"]