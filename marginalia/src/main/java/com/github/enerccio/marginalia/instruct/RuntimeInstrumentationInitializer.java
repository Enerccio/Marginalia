package com.github.enerccio.marginalia.instruct;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.List;

/**
 * The agent that instruments the {@code @Extendable} classes the build did not weave. The packaged application is
 * woven by {@link BuildTimeWeaver} and says so with {@link #BUILD_TIME_FLAG}: the agent is not installed there at
 * all (unless a check of its classes finds some without hooks). A development run ({@code mvn jetty:run}, the IDE) has no such flag: classes compiled by the IDE or swapped
 * into the running JVM are not woven, so the agent is installed and takes the classes without the {@link Woven} mark.
 */
public class RuntimeInstrumentationInitializer implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(RuntimeInstrumentationInitializer.class);

    /**
     * Resource that only the packaged WAR has ({@code src/main/resources-release}): every {@code @Extendable} class
     * in it was woven by the build.
     */
    public static final String BUILD_TIME_FLAG = "META-INF/build-info/woven.properties";

    /**
     * System property that overrules the flag: {@code true} installs the agent in the packaged application too (an
     * extension that has {@code @Extendable} classes of its own needs it), {@code false} never installs it.
     */
    public static final String AGENT_PROPERTY = "marginalia.extendable.agent";

    @Override
    public void afterPropertiesSet() {
        String override = System.getProperty(AGENT_PROPERTY);
        boolean packaged = getClass().getClassLoader().getResource(BUILD_TIME_FLAG) != null;
        List<String> unwoven = override == null && packaged ? unwovenApplicationClasses() : List.of();
        if (!agentWanted(override, packaged, !unwoven.isEmpty())) {
            log.info("The @Extendable classes were woven at build time, the instrumentation agent is not installed");
            return;
        }
        if (!unwoven.isEmpty()) {
            log.warn("{} @Extendable classes of the packaged application are not woven ({}...), the instrumentation agent is installed for them",
                    unwoven.size(), unwoven.getFirst());
        }
        try {
            install();
        } catch (IllegalStateException e) {
            // the JVM refused the attach: only classes woven by the Maven build have hooks
            log.error("The instrumentation agent could not be installed, extensions cannot decorate @Extendable classes "
                    + "that were not woven by the Maven build (classes compiled by the IDE are not). Build with Maven "
                    + "(the process-classes phase weaves them) or start the JVM with -XX:+EnableDynamicAgentLoading "
                    + "-Djdk.attach.allowAttachSelf=true", e);
        }
    }

    /**
     * The agent is installed in a development run (no flag: classes may be compiled by the IDE or swapped in at any
     * time), and in a packaged application only when it has classes without hooks all the same - an exploded WAR
     * put together by the IDE can carry the flag over classes the IDE compiled.
     *
     * @param override value of {@link #AGENT_PROPERTY}, {@code null} when it is not set
     * @param packaged whether the application has {@link #BUILD_TIME_FLAG}
     * @param unwoven  whether an {@code @Extendable} class of the application has no hooks
     */
    static boolean agentWanted(String override, boolean packaged, boolean unwoven) {
        if (override != null) {
            return Boolean.parseBoolean(override);
        }
        return !packaged || unwoven;
    }

    /**
     * The {@code @Extendable} classes without hooks in the folder the application classes are loaded from
     * ({@code WEB-INF/classes}); none when they are not in a folder or it cannot be read.
     */
    private static List<String> unwovenApplicationClasses() {
        try {
            CodeSource source = RuntimeInstrumentationInitializer.class.getProtectionDomain().getCodeSource();
            if (source == null || source.getLocation() == null || !"file".equals(source.getLocation().getProtocol())) {
                return List.of();
            }
            Path classes = Path.of(source.getLocation().toURI());
            return Files.isDirectory(classes) ? ExtendableWeaving.unwovenClasses(classes) : List.of();
        } catch (IOException | URISyntaxException | RuntimeException e) {
            log.warn("The classes of the application could not be checked for the extension hooks", e);
            return List.of();
        }
    }

    /**
     * Attaches the agent to this JVM and registers the transformer. Call it once: a second call would register a
     * second transformer.
     *
     * @throws IllegalStateException when the JVM does not let an agent in
     */
    public static void install() {
        ByteBuddyAgent.install();

        new AgentBuilder.Default(ExtendableWeaving.byteBuddy())
                .disableClassFormatChanges() // Required for JVM retransformation of pre-loaded classes
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(AgentBuilder.DescriptionStrategy.Default.POOL_ONLY)
                .ignore(ElementMatchers.not(ElementMatchers.nameStartsWith("com.github.enerccio")))
//                .with(AgentBuilder.Listener.StreamWriting.toSystemOut())
                .type(ExtendableWeaving.unwovenExtendableTypes())
                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder
                        .visit(ExtendableWeaving.hooks()))
                .installOnByteBuddyAgent();
    }
}
