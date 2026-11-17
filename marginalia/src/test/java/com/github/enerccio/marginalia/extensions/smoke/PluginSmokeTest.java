package com.github.enerccio.marginalia.extensions.smoke;

import ch.qos.logback.classic.Level;
import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.service.OsgiService.ExtensionReport;
import com.github.enerccio.marginalia.domain.service.OsgiService.LoadedExtension;
import com.github.enerccio.marginalia.domain.service.impl.OsgiServiceImpl;
import com.github.enerccio.marginalia.test.ExpectedLog;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.AopTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The smoke test of every plugin: the built JARs are installed into a real OSGi framework inside the Spring context of the tests,
 * as the application does at startup, and each must
 * <ol>
 *     <li><b>verify</b>: every decorator it registers names a method of an {@code @Extendable} class that exists, and asks only for
 *     arguments, locals, fields and return values that method has ({@code ExtensionService.verifyExtension}, the check the
 *     application runs before it starts an extension);</li>
 *     <li><b>load</b>: its {@code onExtensionLoad} passes;</li>
 *     <li><b>unload cleanly</b>: after {@code onExtensionUnload} nothing of the application points into the plugin any more
 *     (decorators, listeners, commands, providers, menu entries, settings, localization...: {@link PluginLeakFinder} walks the
 *     singletons of the context and reports every object of the plugin it still finds).</li>
 * </ol>
 * It needs the built plugins, so it is skipped unless {@code -Dmarginalia.plugins} names them: a folder of JARs, or the
 * {@code plugins} folder (then {@code <plugin>/target/<name>.jar} of each). From {@code marginalia/}, after {@code mvn install
 * -DskipTests} here, in the libraries and {@code mvn package} in the plugins:
 * <pre>
 * mvn clean test -Dtest=PluginSmokeTest -Dmarginalia.plugins=plugins
 * </pre>
 * The Desktop workflow runs it in its plugins job. {@code -Dmarginalia.plugins.only=name,name} limits it to some JARs (the start of
 * the file name).
 */
@EnabledIfSystemProperty(named = PluginSmokeTest.PLUGINS_PROPERTY, matches = ".+")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PluginSmokeTest extends MarginaliaTestBase {

    static final String PLUGINS_PROPERTY = "marginalia.plugins";
    static final String ONLY_PROPERTY = "marginalia.plugins.only";

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private Configuration configuration;

    @Test
    void everyPluginVerifiesLoadsAndUnloadsWithoutLeavingAnythingBehind() throws Exception {
        List<File> jars = findJars();
        assertThat(jars).as("plugin JARs in " + System.getProperty(PLUGINS_PROPERTY)).isNotEmpty();
        loginAdmin();

        File extensions = new File(configuration.getFolder(), "extensions");
        Files.createDirectories(extensions.toPath());
        for (File jar : jars) {
            Files.copy(jar.toPath(), new File(extensions, jar.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // the bean the test context leaves out (TestContextPostProcessor), made by the context so it gets its services
        OsgiServiceImpl osgi = context.getBeanFactory().createBean(OsgiServiceImpl.class);
        // a plugin class may autowire the OsgiService itself (the Conlang Translator's bridge to the Name Generator)
        context.getBeanFactory().registerSingleton("osgiService", osgi);
        Map<String, List<String>> problems = new LinkedHashMap<>();
        Map<ClassLoader, String> loaders = new IdentityHashMap<>();
        List<String> loadedJars = new ArrayList<>();
        try (ExpectedLog log = ExpectedLog.capture(OsgiServiceImpl.class)) {
            osgi.start();

            for (File jar : jars) {
                Bundle bundle = osgi.findBundleByJar(jar.getName());
                if (bundle == null) {
                    problem(problems, jar.getName(), "was not installed: " + errors(log, jar.getName()));
                    continue;
                }
                ExtensionReport report = osgi.getVerificationReport(bundle);
                if (report == null || !report.valid()) {
                    problem(problems, jar.getName(), "did not verify:\n" + (report == null ? errors(log, jar.getName()) : report.text()));
                    continue;
                }
                boolean loaded = osgi.getLoadedExtensions().stream().map(LoadedExtension::bundle).anyMatch(bundle::equals);
                if (!loaded) {
                    problem(problems, jar.getName(), "did not load (onExtensionLoad failed?): " + errors(log, bundle.getSymbolicName()));
                    continue;
                }
                loadedJars.add(jar.getName());
                // the loader that defined the extension's classes (the wiring's, and the extension's own class for certain)
                osgi.getLoadedExtensions().stream().filter(l -> l.bundle().equals(bundle))
                        .forEach(l -> loaders.put(l.extension().getClass().getClassLoader(), jar.getName()));
                BundleWiring wiring = bundle.adapt(BundleWiring.class);
                if (wiring != null && wiring.getClassLoader() != null) {
                    loaders.put(wiring.getClassLoader(), jar.getName());
                }
            }

            // the finder must see the plugins while they are loaded, else a clean result after the unload would prove nothing
            List<String> seenLoaded = findLeaks(osgi, loaders).stream().map(PluginLeakFinder.Leak::owner).distinct().toList();
            for (String jar : loadedJars) {
                if (!seenLoaded.contains(jar)) {
                    problem(problems, jar, "is loaded but the leak finder finds nothing of it in the application: the check of its unload would prove nothing");
                }
            }

            // unloaded one by one as Admin -> Extensions -> Stop does, so a failing unload names its plugin
            for (File jar : jars) {
                Bundle bundle = osgi.findBundleByJar(jar.getName());
                if (bundle == null || !loadedJars.contains(jar.getName())) {
                    continue;
                }
                try {
                    osgi.stopExtension(bundle);
                } catch (Exception | LinkageError e) {
                    problem(problems, jar.getName(), "failed to unload: " + e);
                }
            }
            for (LoadedExtension still : osgi.getLoadedExtensions()) {
                problem(problems, still.bundle().getSymbolicName(), "is still loaded after it was stopped");
            }

            for (PluginLeakFinder.Leak leak : findLeaks(osgi, loaders)) {
                problem(problems, leak.owner(), "is still referenced after unload: " + leak.path() + " -> " + leak.type());
            }
        } finally {
            osgi.stop();
        }

        assertThat(problems).as(describe(problems)).isEmpty();
    }

    private List<PluginLeakFinder.Leak> findLeaks(OsgiServiceImpl osgi, Map<ClassLoader, String> loaders) {
        PluginLeakFinder finder = new PluginLeakFinder(loaders::get);
        finder.walk("osgiService", osgi);
        ConfigurableListableBeanFactory beans = context.getBeanFactory();
        for (String name : beans.getSingletonNames()) {
            Object bean = beans.getSingleton(name);
            if (bean != null) {
                finder.walk(name, AopTestUtils.getUltimateTargetObject(bean));
            }
        }
        // a static registry reached from no bean
        finder.walkStatics(Defaults.class);
        return finder.leaks();
    }

    /** What the log says about a plugin (its JAR or symbolic name), for the report. */
    private static String errors(ExpectedLog log, String name) {
        List<String> lines = log.atLeast(Level.WARN).stream().filter(line -> line.contains(name)).toList();
        return lines.isEmpty() ? "nothing in the log" : String.join("\n", lines);
    }

    private static void problem(Map<String, List<String>> problems, String plugin, String text) {
        problems.computeIfAbsent(plugin, p -> new ArrayList<>()).add(text);
    }

    private static String describe(Map<String, List<String>> problems) {
        StringBuilder text = new StringBuilder("plugins that did not pass the smoke test:\n");
        problems.forEach((plugin, list) -> {
            text.append("\n== ").append(plugin).append('\n');
            list.forEach(line -> text.append("  - ").append(line.replace("\n", "\n    ")).append('\n'));
        });
        return text.toString();
    }

    /**
     * The JARs named by {@code marginalia.plugins}: the JARs in the folder, or, when it has none, the one JAR in the {@code target} of
     * each of its folders (the {@code original-} copies of the bundle plugin and sources/javadoc JARs left out).
     */
    static List<File> findJars() throws IOException {
        Path folder = Path.of(System.getProperty(PLUGINS_PROPERTY)).toAbsolutePath().normalize();
        List<Path> found;
        try (Stream<Path> files = Files.list(folder)) {
            found = files.filter(PluginSmokeTest::isPluginJar).sorted().toList();
        }
        if (found.isEmpty()) {
            List<Path> fromTargets = new ArrayList<>();
            try (Stream<Path> dirs = Files.list(folder)) {
                for (Path dir : dirs.filter(Files::isDirectory).sorted().toList()) {
                    Path target = dir.resolve("target");
                    if (Files.isDirectory(target)) {
                        try (Stream<Path> files = Files.list(target)) {
                            fromTargets.addAll(files.filter(PluginSmokeTest::isPluginJar).sorted().toList());
                        }
                    }
                }
            }
            found = fromTargets;
        }
        String only = System.getProperty(ONLY_PROPERTY);
        List<String> names = only == null || only.isBlank() ? List.of() : Arrays.stream(only.split(",")).map(String::strip).toList();
        return found.stream().map(Path::toFile)
                .filter(jar -> names.isEmpty() || names.stream().anyMatch(jar.getName()::startsWith))
                .toList();
    }

    private static boolean isPluginJar(Path path) {
        String name = path.getFileName().toString();
        return Files.isRegularFile(path) && name.endsWith(".jar") && !name.startsWith("original-")
                && !name.endsWith("-sources.jar") && !name.endsWith("-javadoc.jar") && !name.endsWith("-tests.jar");
    }
}
