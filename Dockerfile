# Stages: builder (the application, installed into the local Maven repository), plugins-build and plugins (all
# plugins, built against the builder's application: marginalia/plugins/docker-compose.yml builds the target plugins)
# and runtime (the image, the default target, so it must stay the last stage). The application image and the plugin
# image share the builder stage, so a server that builds both (rebuild.sh) compiles the application once: the second
# build finds it in Docker's build cache.
FROM maven:3.9-eclipse-temurin-26 AS builder

WORKDIR /build/marginalia

# Cache dependencies (the POMs inherit from the parent POM in the repository root). The platform module
# (marginalia/platform) brings the libraries; the application's own POM can't be resolved before the platform is
# installed, so what only it needs (its build plugins, the test libraries) is downloaded by the build below
COPY pom.xml /build/pom.xml
COPY marginalia/platform/pom.xml ./platform/pom.xml
COPY marginalia/pom.xml .
RUN mvn install -N -B -f /build/pom.xml \
    && mvn dependency:go-offline -B -f platform/pom.xml

# Copy source code and build the platform, then the application
COPY marginalia/platform/src ./platform/src
COPY marginalia/src ./src
# the user manual (Markdown) is packed into the WAR for the in-app help, see <resources> in marginalia/pom.xml
COPY manual/user /build/manual/user
# and its translations (translations/<language>/user), read by the help in the language of the user
COPY translations /build/translations
COPY .git /build/.git
# install, not just package: the platform JAR, the classes JAR and the parent POM go into the local Maven repository, where the plugins
# find them (plugins-build starts from this stage). Then everything the later stages don't need is deleted in the
# same step, so it never gets into the layer: the frontend tooling (node_modules, Node.js in ~/.vaadin, the npm
# cache), target/ except the WAR, and the WAR's copy in the local repository. Tens of thousands of files that the
# classic builder would otherwise write into the layer after the build, a long pause before the next step.
RUN mvn clean install -DskipTests -B -f platform/pom.xml \
    && mvn clean install -DskipTests -B \
    && rm -rf node_modules "$HOME/.vaadin" "$HOME/.npm" "$HOME/.cache" platform/target \
    && find target -mindepth 1 -maxdepth 1 ! -name '*.war' -exec rm -rf {} + \
    && find "$HOME/.m2/repository/io/github/enerccio/marginalia" -name '*.war' -delete

# Build and install the libraries the plugins share (marginalia/libs; a plugin embeds the library in its bundle), then
# every plugin (tests are skipped, as for the application; each one is a separate Maven project). Not needed by the
# runtime image: BuildKit skips these stages there, the classic builder builds them too (cached after a plugin build).
FROM builder AS plugins-build

COPY marginalia/libs ./libs
RUN for pom in libs/*/pom.xml; do \
        (cd "$(dirname "$pom")" && mvn install -DskipTests -B) || exit 1; \
    done

COPY marginalia/plugins ./plugins
RUN mkdir /jars \
    && for pom in plugins/*/pom.xml; do \
        (cd "$(dirname "$pom")" && mvn package -DskipTests -B) || exit 1; \
        cp "$(dirname "$pom")"/target/*.jar /jars/ || exit 1; \
    done

# The plugin JARs; the command copies them into the mounted folder /output, replacing the ones from an earlier build
# (a bind mount exists only when a container runs, so they can't be copied out during the build)
FROM alpine:3 AS plugins

COPY --from=plugins-build /jars /jars

CMD ["sh", "-c", "mkdir -p /output && rm -f /output/*.jar && cp /jars/*.jar /output/ && ls -l /output"]

FROM jetty:12-jdk25-eclipse-temurin AS runtime

# first, before anything that changes with the source (the WAR): this layer stays cached over normal deploys
USER root
# ffmpeg is optional: the Audiobook plugin offers the m4b export, the wav storage and the variations of voices for unnamed speakers (its rubberband
# filter) only when it finds the program. Build without it (about 200 MB
# smaller) with --build-arg WITH_FFMPEG=false
ARG WITH_FFMPEG=true
RUN if [ "$WITH_FFMPEG" = "true" ]; then \
        apt-get update && apt-get install -y --no-install-recommends ffmpeg && rm -rf /var/lib/apt/lists/*; \
    fi

USER jetty
WORKDIR $JETTY_BASE

RUN java -jar "$JETTY_HOME/start.jar" --create-startd \
    --add-modules=server,http,ee11-deploy,ee11-websocket-jakarta,ee11-webapp,ee11-jsp

COPY --from=builder /build/marginalia/target/*.war $JETTY_BASE/webapps/ROOT.war

# session cookie of its own: cookies are per host, not per port, so the plain JSESSIONID of another Java web application
# on the same host (another one on the platform, a second Marginalia) would replace this one's on every visit.
# Override it in docker-compose.yml (MARGINALIA_SESSION_COOKIE) to run two of these images on one host
ENV MARGINALIA_SESSION_COOKIE=JSESSIONID_MARGINALIA

USER root
RUN mkdir -p /var/marginalia/.marginalia && chown -R jetty:jetty /var/marginalia
# no COPY --chmod: it needs BuildKit, which the classic builder of older docker/docker-compose v1 lacks
COPY docker-entrypoint.sh /usr/local/bin/marginalia-entrypoint.sh
RUN chmod 755 /usr/local/bin/marginalia-entrypoint.sh

# starts as root to fix the owner of the data folder (a bind mount created by Docker is owned by root), then runs
# Jetty as jetty, see docker-entrypoint.sh
EXPOSE 8080

# healthy when /health answers 200 (the application is up and the database answers); the image has no curl, so bash opens
# the socket. Vaadin takes a while to start, hence the long start period
HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=3 \
    CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/8080 && printf "GET /health HTTP/1.0\r\nHost: localhost\r\n\r\n" >&3 && head -n1 <&3 | grep -q "HTTP/1\.. 200"' || exit 1

ENTRYPOINT ["/usr/local/bin/marginalia-entrypoint.sh"]
CMD ["java", "-jar", "/usr/local/jetty/start.jar"]