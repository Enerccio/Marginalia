package com.github.enerccio.marginalia.instruct;

import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.impl.ExtensionServiceImpl;
import com.github.enerccio.marginalia.instruct.fixture.InstrumentedSubject;
import com.github.enerccio.marginalia.instruct.fixture.agent.AgentConfigurableSubject;
import com.github.enerccio.marginalia.instruct.fixture.agent.AgentEarlySubject;
import com.github.enerccio.marginalia.instruct.fixture.agent.AgentPlainSubject;
import com.github.enerccio.marginalia.instruct.fixture.agent.AgentWovenSubject;
import com.github.enerccio.marginalia.instruct.fixture.verify.VerifyTarget;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.pool.TypePool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * {@link BuildTimeWeaver}, the build step that puts the hooks into the {@code @Extendable} classes: here on a copy of
 * the fixture classes, and (in a Maven run) that the classes of the application went through it.
 */
class BuildTimeWeaverTest {

    private static final String FIXTURES = "com/github/enerccio/marginalia/instruct/fixture";

    @TempDir
    Path classes;

    private ExtensionServiceImpl extensionService;
    private ExtensionService previousService;

    @BeforeEach
    void setUp() throws Exception {
        Path root = Path.of(InstrumentedSubject.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        try (Stream<Path> files = Files.walk(root.resolve(FIXTURES))) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                Path copy = classes.resolve(root.relativize(file).toString());
                Files.createDirectories(copy.getParent());
                Files.copy(file, copy);
            }
        }
        extensionService = new ExtensionServiceImpl();
        previousService = ExtendableMethodVisitorTest.setHolder(extensionService);
    }

    @AfterEach
    void tearDown() throws Exception {
        ExtendableMethodVisitorTest.setHolder(previousService);
    }

    private Path file(Class<?> type) {
        return classes.resolve(type.getName().replace('.', '/') + ".class");
    }

    private BuildTimeWeaver.Result weave() throws IOException {
        return BuildTimeWeaver.weave(classes, getClass().getClassLoader());
    }

    /**
     * Loads the fixture classes from the woven folder, everything else from the class path of the test.
     */
    private Instrumenter.IsolatedLoader wovenLoader() throws IOException {
        Instrumenter.IsolatedLoader loader = new Instrumenter.IsolatedLoader(getClass().getClassLoader());
        try (Stream<Path> files = Files.walk(classes)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".class")).toList()) {
                String name = classes.relativize(file).toString().replace(file.getFileSystem().getSeparator(), "/");
                loader.add(name.substring(0, name.length() - ".class".length()).replace('/', '.'), Files.readAllBytes(file));
            }
        }
        return loader;
    }

    @Test
    void extendableClassesAreWovenAndMarked() throws Exception {
        byte[] plain = Files.readAllBytes(file(AgentPlainSubject.class));
        byte[] marked = Files.readAllBytes(file(AgentWovenSubject.class));

        BuildTimeWeaver.Result result = weave();

        // the agent subjects (early, late, configurable) and the targets of the verifier (the class, its nested
        // class, the child)
        assertThat(result.woven()).isEqualTo(6);
        assertThat(result.alreadyWoven()).isEqualTo(1);
        Instrumenter.IsolatedLoader loader = wovenLoader();
        assertThat(loader.loadClass(AgentEarlySubject.class.getName()).isAnnotationPresent(Woven.class)).isTrue();
        assertThat(loader.loadClass(VerifyTarget.Card.class.getName()).isAnnotationPresent(Woven.class)).isTrue();
        // not @Extendable (the visitor tests instrument it themselves)
        assertThat(loader.loadClass(InstrumentedSubject.class.getName()).isAnnotationPresent(Woven.class)).isFalse();
        assertThat(loader.loadClass(AgentPlainSubject.class.getName()).isAnnotationPresent(Woven.class)).isFalse();
        assertThat(Files.readAllBytes(file(AgentPlainSubject.class))).isEqualTo(plain);
        assertThat(Files.readAllBytes(file(AgentWovenSubject.class))).isEqualTo(marked);
        try (Stream<Path> files = Files.walk(classes)) {
            assertThat(files.filter(f -> f.toString().endsWith(".weaving"))).isEmpty();
        }
    }

    @Test
    void wovenClassesVerify() throws Exception {
        weave();
        Instrumenter.IsolatedLoader loader = wovenLoader();

        for (Class<?> type : List.of(VerifyTarget.class, VerifyTarget.Card.class, AgentEarlySubject.class, AgentConfigurableSubject.class)) {
            StringWriter errors = new StringWriter();
            CheckClassAdapter.verify(new ClassReader(Files.readAllBytes(file(type))), loader, false, new PrintWriter(errors));
            assertThat(errors.toString()).isEmpty();
            // initializing runs the bytecode verifier of the JVM
            assertThat(Class.forName(type.getName(), true, loader).getClassLoader()).isSameAs(loader);
        }
    }

    @Test
    void wovenMethodsCallTheDecorators() throws Exception {
        weave();
        List<String> seen = new ArrayList<>();
        extensionService.registerDecorator(new ExtensionDecorator() {
            @Override
            public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {
                seen.add("enter " + context.getMethodArgument("name", String.class));
            }

            @Override
            public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                seen.add("leave " + context.getLocalVariable("greeting", StringBuilder.class));
                context.setCurrentReturn(context.getOriginalReturn(String.class).toUpperCase());
            }
        }, AgentEarlySubject.class.getName(), "greet");
        List<String> thrown = new ArrayList<>();
        extensionService.registerDecorator(new ExtensionDecorator() {
            @Override
            public void onMethodEnter(Object instrumented, ExtendableMethodContext context) {
            }

            @Override
            public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) {
                thrown.add(throwing == null ? "none" : throwing.getClass().getSimpleName());
            }
        }, AgentEarlySubject.class.getName(), "divide");

        Class<?> type = wovenLoader().loadClass(AgentEarlySubject.class.getName());
        Object subject = type.getConstructor().newInstance();

        assertThat(type.getMethod("greet", String.class).invoke(subject, "world")).isEqualTo("HELLO WORLD");
        assertThat(seen).containsExactly("enter world", "leave hello world");
        assertThat(type.getMethod("divide", int.class, int.class).invoke(subject, 6, 3)).isEqualTo(2);
        assertThatThrownBy(() -> type.getMethod("divide", int.class, int.class).invoke(subject, 1, 0))
                .hasCauseInstanceOf(ArithmeticException.class);
        assertThat(thrown).containsExactly("none", "ArithmeticException");
    }

    @Test
    void withoutAServiceWovenMethodsRunAsBefore() throws Exception {
        weave();
        ExtendableMethodVisitorTest.setHolder(null);

        Class<?> type = wovenLoader().loadClass(AgentEarlySubject.class.getName());

        assertThat(type.getMethod("greet", String.class).invoke(type.getConstructor().newInstance(), "x")).isEqualTo("hello x");
    }

    @Test
    void secondRunLeavesTheWovenClassesAlone() throws Exception {
        BuildTimeWeaver.Result first = weave();
        byte[] woven = Files.readAllBytes(file(AgentEarlySubject.class));

        BuildTimeWeaver.Result second = weave();

        assertThat(second.woven()).isZero();
        assertThat(second.alreadyWoven()).isEqualTo(first.woven() + first.alreadyWoven());
        assertThat(Files.readAllBytes(file(AgentEarlySubject.class))).isEqualTo(woven);
    }

    @Test
    void classCompiledAgainIsWovenAgain() throws Exception {
        weave();
        // what the compiler does to one class of a woven folder
        Files.write(file(AgentEarlySubject.class), Instrumenter.originalBytes(AgentEarlySubject.class));

        BuildTimeWeaver.Result result = weave();

        assertThat(result.woven()).isEqualTo(1);
        assertThat(wovenLoader().loadClass(AgentEarlySubject.class.getName()).isAnnotationPresent(Woven.class)).isTrue();
    }

    @Test
    void mainFailsOnAFolderWithoutExtendableClasses(@TempDir Path empty) throws Exception {
        Path plain = empty.resolve(AgentPlainSubject.class.getName().replace('.', '/') + ".class");
        Files.createDirectories(plain.getParent());
        Files.copy(file(AgentPlainSubject.class), plain);

        assertThatThrownBy(() -> BuildTimeWeaver.main(new String[]{empty.toString()}))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("No @Extendable class");
        assertThatThrownBy(() -> BuildTimeWeaver.main(new String[0])).isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * The Maven build weaves {@code target/classes} before the tests ({@code weave-extendable} in the pom, which sets
     * the property). A run of the IDE on classes it compiled itself has no such step and skips this.
     */
    @Test
    void theBuildWeavesEveryExtendableClassOfTheApplication() {
        assumeTrue(Boolean.getBoolean("marginalia.test.woven"), "not a Maven run");
        TypePool pool = PublicAPISignatures.pool();

        List<TypeDescription> extendable = PublicAPISignatures.extendableTypes(pool);

        assertThat(extendable).isNotEmpty();
        assertThat(extendable.stream().filter(type -> !BuildTimeWeaver.isWoven(type)).map(TypeDescription::getName))
                .as("@Extendable classes without the hooks").isEmpty();
    }

    @Test
    void agentIsLeftOutOnlyWhereTheBuildWoveEverything() {
        // development run: classes can come unwoven at any time
        assertThat(RuntimeInstrumentationInitializer.agentWanted(null, false, false)).isTrue();
        // the packaged application
        assertThat(RuntimeInstrumentationInitializer.agentWanted(null, true, false)).isFalse();
        // the flag over classes the IDE compiled
        assertThat(RuntimeInstrumentationInitializer.agentWanted(null, true, true)).isTrue();
        assertThat(RuntimeInstrumentationInitializer.agentWanted("true", true, false)).isTrue();
        assertThat(RuntimeInstrumentationInitializer.agentWanted("false", false, true)).isFalse();
    }

    @Test
    void unwovenClassesOfAFolderAreFound() throws Exception {
        assertThat(ExtendableWeaving.unwovenClasses(classes)).containsExactly(
                AgentConfigurableSubject.class.getName(), AgentEarlySubject.class.getName(),
                "com.github.enerccio.marginalia.instruct.fixture.agent.AgentLateSubject",
                "com.github.enerccio.marginalia.instruct.fixture.verify.VerifyChild",
                VerifyTarget.class.getName(), VerifyTarget.Card.class.getName());

        weave();

        assertThat(ExtendableWeaving.unwovenClasses(classes)).isEmpty();
    }

    @Test
    void serviceOfTheRunningContextIsBoundAndUnbound() throws Exception {
        ExtensionServiceImpl first = new ExtensionServiceImpl();
        ExtensionServiceImpl second = new ExtensionServiceImpl();

        first.afterPropertiesSet();
        assertThat(ExtensionServiceHolder.getInstance()).isSameAs(first);
        second.afterPropertiesSet();
        // the older context is closed after the newer one started
        first.destroy();
        assertThat(ExtensionServiceHolder.getInstance()).isSameAs(second);
        second.destroy();
        assertThat(ExtensionServiceHolder.getInstance()).isNull();
    }
}
