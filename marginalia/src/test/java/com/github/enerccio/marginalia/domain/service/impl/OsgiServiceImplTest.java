package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.test.ExpectedLog;
import com.vaadin.flow.component.html.Div;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Loading, replacing and unloading extensions on a real OSGi framework, with test bundles built on the fly (see
 * {@link TestExtensionActivator}).
 */
class OsgiServiceImplTest {

    @TempDir
    File home;

    private File extensions;
    private OsgiServiceImpl service;
    private ExpectedLog log;

    @BeforeEach
    void setUp() throws Exception {
        TestExtensionActivator.LOADED.clear();
        TestExtensionActivator.UNLOADED.clear();

        Configuration configuration = new Configuration();
        configuration.setFolder(home);
        configuration.afterPropertiesSet();

        service = new OsgiServiceImpl();
        ReflectionTestUtils.setField(service, "configuration", configuration);
        ReflectionTestUtils.setField(service, "extensionService", Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{ExtensionService.class}, (proxy, method, args) -> null));
        service.afterPropertiesSet();
        extensions = new File(service.getExtensionsPath());
        log = ExpectedLog.capture(OsgiServiceImpl.class);
    }

    @AfterEach
    void tearDown() {
        service.destroy();
        log.close();
    }

    private static byte[] bundle(String symbolicName, String version, String... headers) throws IOException {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.putValue("Bundle-ManifestVersion", "2");
        attributes.putValue("Bundle-SymbolicName", symbolicName);
        attributes.putValue("Bundle-Version", version);
        attributes.putValue("Bundle-Activator", TestExtensionActivator.class.getName());
        for (String header : headers) {
            attributes.putValue(header, "true");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JarOutputStream ignored = new JarOutputStream(out, manifest)) {
            // the manifest is all a test bundle needs
        }
        return out.toByteArray();
    }

    private void writeJar(String name, byte[] data) throws IOException {
        Files.write(new File(extensions, name).toPath(), data);
    }

    private List<String> jarNames() {
        String[] names = extensions.list((dir, name) -> !name.equals("org.eclipse.osgi"));
        Arrays.sort(names);
        return Arrays.asList(names);
    }

    private Bundle bundleNamed(String symbolicName) {
        return service.getBundles().stream().filter(b -> symbolicName.equals(b.getSymbolicName())).findFirst().orElseThrow();
    }

    private MarginaliaExtension extensionOf(Bundle bundle) throws Exception {
        ServiceReference<?> reference = service.getServices(bundle, MarginaliaExtension.class).getFirst();
        return (MarginaliaExtension) service.getContext().getService(reference);
    }

    @Test
    void brokenJarsDontStopOtherExtensionsFromLoading() throws Exception {
        writeJar("a.jar", bundle("test.a", "1.0.0"));
        writeJar("a-copy.jar", bundle("test.a", "1.0.0"));
        writeJar("b.jar", "not a jar".getBytes(StandardCharsets.UTF_8));
        writeJar("c.jar", bundle("test.c", "1.0.0", "X-Test-Fail-Start"));
        writeJar("d.jar", bundle("test.d", "1.0.0", "X-Test-Fail-Load"));
        writeJar("e.jar", bundle("test.e", "1.0.0"));

        service.start();

        assertThat(TestExtensionActivator.LOADED).containsEntry("test.a:1.0.0", 1).containsEntry("test.e:1.0.0", 1);
        assertThat(bundleNamed("test.a").getState()).isEqualTo(Bundle.ACTIVE);
        assertThat(bundleNamed("test.e").getState()).isEqualTo(Bundle.ACTIVE);
        // bundles that failed are listed, not active
        assertThat(bundleNamed("test.c").getState()).isNotEqualTo(Bundle.ACTIVE);
        assertThat(bundleNamed("test.d").getState()).isNotEqualTo(Bundle.ACTIVE);
        // a failed load is undone
        assertThat(TestExtensionActivator.UNLOADED).containsEntry("test.d:1.0.0", 1);
        assertThat(log.errors()).anyMatch(m -> m.contains("b.jar")).anyMatch(m -> m.contains("a-copy.jar") || m.contains("a.jar"))
                .anyMatch(m -> m.contains("test.c")).anyMatch(m -> m.contains("test.d"));
    }

    @Test
    void failedExtensionCanBeUnloaded() throws Exception {
        writeJar("c.jar", bundle("test.c", "1.0.0", "X-Test-Fail-Start"));
        service.start();

        service.uninstallPackage(bundleNamed("test.c"));

        assertThat(service.getBundles()).isEmpty();
        assertThat(jarNames()).isEmpty();
    }

    @Test
    void uploadOfInstalledBundleReplacesIt() throws Exception {
        writeJar("a.jar", bundle("test.a", "1.0.0"));
        service.start();

        // same file name
        Bundle updated = service.installPackage("a.jar", bundle("test.a", "1.0.1"));

        assertThat(updated.getState()).isEqualTo(Bundle.ACTIVE);
        assertThat(service.getBundles()).extracting(b -> b.getVersion().toString()).containsExactly("1.0.1");
        assertThat(TestExtensionActivator.LOADED).containsEntry("test.a:1.0.0", 1).containsEntry("test.a:1.0.1", 1);
        assertThat(TestExtensionActivator.UNLOADED).containsEntry("test.a:1.0.0", 1);

        // different file name, same symbolic name
        service.installPackage("a-2.jar", bundle("test.a", "1.0.2"));

        assertThat(service.getBundles()).extracting(b -> b.getVersion().toString()).containsExactly("1.0.2");
        assertThat(TestExtensionActivator.UNLOADED).containsEntry("test.a:1.0.1", 1);
        assertThat(jarNames()).containsExactly("a-2.jar");
    }

    @Test
    void failedUploadIsDeletedAndKeepsTheInstalledVersion() throws Exception {
        writeJar("a.jar", bundle("test.a", "1.0.0"));
        service.start();

        assertThatThrownBy(() -> service.installPackage("a-new.jar", bundle("test.a", "2.0.0", "X-Test-Fail-Load")))
                .hasMessageContaining("failed");

        assertThat(jarNames()).containsExactly("a.jar");
        Bundle restored = bundleNamed("test.a");
        assertThat(restored.getVersion().toString()).isEqualTo("1.0.0");
        assertThat(restored.getState()).isEqualTo(Bundle.ACTIVE);
        assertThat(TestExtensionActivator.LOADED).containsEntry("test.a:1.0.0", 2);
    }

    @Test
    void uploadThatIsNotABundleLeavesNothingBehind() throws Exception {
        service.start();

        assertThatThrownBy(() -> service.installPackage("x.jar", "not a jar".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(IOException.class);
        assertThatThrownBy(() -> service.installPackage("y.jar", bundle("", "1.0.0")))
                .hasMessageContaining("Not an OSGi bundle");

        assertThat(jarNames()).isEmpty();
        assertThat(service.getBundles()).isEmpty();
    }

    @Test
    void uploadedFileNameIsSanitized() throws Exception {
        assertThat(OsgiServiceImpl.sanitizeJarName("../../evil name.jar")).isEqualTo("evil_name.jar");
        assertThat(OsgiServiceImpl.sanitizeJarName("..\\x.jar")).isEqualTo("x.jar");
        assertThat(OsgiServiceImpl.sanitizeJarName(".hidden.jar")).isEqualTo("hidden.jar");
        assertThat(OsgiServiceImpl.sanitizeJarName(null)).isEqualTo("extension.jar");

        service.start();
        service.installPackage("../outside.jar", bundle("test.a", "1.0.0"));

        assertThat(jarNames()).containsExactly("outside.jar");
        assertThat(new File(home, "outside.jar")).doesNotExist();
    }

    @Test
    void stopUnloadsExtensionsAndStopsTheFramework() throws Exception {
        writeJar("a.jar", bundle("test.a", "1.0.0"));
        service.start();
        Framework framework = (Framework) ReflectionTestUtils.getField(service, "f");

        service.destroy();

        assertThat(TestExtensionActivator.UNLOADED).containsEntry("test.a:1.0.0", 1);
        assertThat(framework.getState()).isNotEqualTo(Bundle.ACTIVE);
        assertThat(service.getBundles()).isEmpty();
    }

    @Test
    void componentCallbacksAreDroppedWithTheExtension() throws Exception {
        writeJar("a.jar", bundle("test.a", "1.0.0"));
        service.start();
        Bundle bundle = bundleNamed("test.a");
        MarginaliaExtension extension = extensionOf(bundle);
        Map<Object, ?> callbacks = (Map<Object, ?>) ReflectionTestUtils.getField(service, "componentCallbacks");

        service.bindAttachableComponent(new Div(), () -> {
        }, extension);
        assertThat(callbacks).containsKey(extension);

        service.uninstallPackage(bundle);

        assertThat(callbacks).doesNotContainKey(extension);
        assertThatThrownBy(() -> service.bindAttachableComponent(new Div(), () -> {
        }, extension)).isInstanceOf(IllegalStateException.class).hasMessageContaining("not loaded");
    }
}
