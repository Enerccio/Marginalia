package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.security.AdminGuard;
import com.github.enerccio.marginalia.domain.service.ExtensionVerificationException;
import com.github.enerccio.marginalia.domain.service.OsgiService.ExtensionReport;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.instruct.fixture.verify.VerifyFixtures;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

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
    private long applicationTimestamp;

    @BeforeEach
    void setUp() throws Exception {
        TestExtensionActivator.LOADED.clear();
        TestExtensionActivator.UNLOADED.clear();

        Configuration configuration = new Configuration();
        configuration.setFolder(home);
        configuration.afterPropertiesSet();

        applicationTimestamp = 0;
        service = new OsgiServiceImpl() {
            @Override
            protected long applicationTimestamp() {
                return applicationTimestamp;
            }
        };
        ReflectionTestUtils.setField(service, "configuration", configuration);
        ReflectionTestUtils.setField(service, "extensionService", new ExtensionServiceImpl());
        // the administrator check is covered with the Spring context, here the current user is not available
        ReflectionTestUtils.setField(service, "adminGuard", new AdminGuard() {
            @Override
            public void requireAdmin() {
            }
        });
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
        return bundleWith(symbolicName, version, List.of(), headers);
    }

    /**
     * A bundle with a decorator registration for a class that doesn't exist: it fails the verification.
     */
    private static byte[] invalidBundle(String symbolicName, String version) throws IOException {
        return bundleWith(symbolicName, version, List.of("VerifyFixtures$BrokenTargets.class"));
    }

    private static byte[] bundleWith(String symbolicName, String version, List<String> classes, String... headers) throws IOException {
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
        try (JarOutputStream jar = new JarOutputStream(out, manifest)) {
            // the manifest is all a test bundle needs, classes are only read by the verification
            for (String name : classes) {
                String path = VerifyFixtures.class.getPackageName().replace('.', '/') + "/" + name;
                jar.putNextEntry(new ZipEntry(path));
                try (var in = VerifyFixtures.class.getClassLoader().getResourceAsStream(path)) {
                    jar.write(in.readAllBytes());
                }
                jar.closeEntry();
            }
        }
        return out.toByteArray();
    }

    private void writeJar(String name, byte[] data) throws IOException {
        Files.write(new File(extensions, name).toPath(), data);
    }

    private List<String> jarNames() {
        String[] names = extensions.list((dir, name) -> name.endsWith(".jar"));
        Arrays.sort(names);
        return Arrays.asList(names);
    }

    /**
     * Verification reports next to the JARs.
     */
    private List<String> reportNames() {
        String[] names = extensions.list((dir, name) -> name.endsWith(".valid") || name.endsWith(".invalid"));
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
    void installAndUninstallNeedAnAdministrator() throws Exception {
        writeJar("a.jar", bundle("test.a", "1.0.0"));
        service.start();
        Bundle installed = bundleNamed("test.a");
        ReflectionTestUtils.setField(service, "adminGuard", new AdminGuard() {
            @Override
            public void requireAdmin() {
                throw new SecurityException("denied");
            }
        });

        assertThatThrownBy(() -> service.installPackage("b.jar", bundle("test.b", "1.0.0"))).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> service.uninstallPackage(installed)).isInstanceOf(SecurityException.class);

        assertThat(jarNames()).containsExactly("a.jar");
        assertThat(installed.getState()).isEqualTo(Bundle.ACTIVE);
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

    @Test
    void verifiedExtensionsAreStartedAndLeaveAReport() throws Exception {
        writeJar("a.jar", bundle("test.a", "1.0.0"));

        service.start();

        assertThat(bundleNamed("test.a").getState()).isEqualTo(Bundle.ACTIVE);
        assertThat(reportNames()).containsExactly("a.valid");
        ExtensionReport report = service.getVerificationReport(bundleNamed("test.a"));
        assertThat(report.valid()).isTrue();
        assertThat(report.text()).contains("Extension: a.jar").contains("Result: VALID");
    }

    @Test
    void invalidExtensionIsInstalledButNotStarted() throws Exception {
        writeJar("good.jar", bundle("test.good", "1.0.0"));
        writeJar("bad.jar", invalidBundle("test.bad", "1.0.0"));

        service.start();

        assertThat(bundleNamed("test.good").getState()).isEqualTo(Bundle.ACTIVE);
        assertThat(bundleNamed("test.bad").getState()).isNotEqualTo(Bundle.ACTIVE);
        assertThat(TestExtensionActivator.LOADED).containsOnlyKeys("test.good:1.0.0");
        assertThat(reportNames()).containsExactly("bad.invalid", "good.valid");
        assertThat(service.getVerificationReport(bundleNamed("test.bad")).valid()).isFalse();
        assertThat(log.errors()).anyMatch(m -> m.contains("bad.jar") && m.contains("does not exist in this version"));
    }

    @Test
    void invalidUploadIsRejectedWithTheReportAndKeepsTheInstalledVersion() throws Exception {
        writeJar("a.jar", bundle("test.a", "1.0.0"));
        service.start();

        assertThatThrownBy(() -> service.installPackage("a.jar", invalidBundle("test.a", "2.0.0")))
                .isInstanceOf(ExtensionVerificationException.class)
                .hasMessageContaining("Result: INVALID")
                .hasMessageContaining("NoSuchClass does not exist");

        assertThat(jarNames()).containsExactly("a.jar");
        assertThat(bundleNamed("test.a").getVersion().toString()).isEqualTo("1.0.0");
        assertThat(bundleNamed("test.a").getState()).isEqualTo(Bundle.ACTIVE);
        assertThat(reportNames()).containsExactly("a.valid");
        assertThat(service.getVerificationReport(bundleNamed("test.a")).valid()).isTrue();
    }

    @Test
    void reportIsReusedWhileItIsNewerThanTheExtensionAndTheApplication() throws Exception {
        writeJar("a.jar", bundle("test.a", "1.0.0"));
        service.start();
        File report = new File(extensions, "a.valid");
        Files.writeString(report.toPath(), "kept", StandardCharsets.UTF_8);
        service.restart();

        assertThat(report).hasContent("kept");
        assertThat(bundleNamed("test.a").getState()).isEqualTo(Bundle.ACTIVE);

        // a report of an invalid extension is final as well: the extension is not even tried
        Files.delete(report.toPath());
        File invalid = new File(extensions, "a.invalid");
        Files.writeString(invalid.toPath(), "Result: INVALID\nnot compatible", StandardCharsets.UTF_8);
        service.restart();

        assertThat(bundleNamed("test.a").getState()).isNotEqualTo(Bundle.ACTIVE);
        assertThat(invalid).exists();
        assertThat(log.errors()).anyMatch(m -> m.contains("not compatible"));
    }

    @Test
    void reportOlderThanTheExtensionIsVerifiedAgain() throws Exception {
        writeJar("a.jar", bundle("test.a", "1.0.0"));
        File report = new File(extensions, "a.invalid");
        Files.writeString(report.toPath(), "Result: INVALID\nstale", StandardCharsets.UTF_8);
        Files.setLastModifiedTime(report.toPath(), java.nio.file.attribute.FileTime.fromMillis(System.currentTimeMillis() - 60_000));

        service.start();

        assertThat(bundleNamed("test.a").getState()).isEqualTo(Bundle.ACTIVE);
        assertThat(reportNames()).containsExactly("a.valid");
    }

    @Test
    void reportOlderThanTheApplicationIsVerifiedAgain() throws Exception {
        writeJar("a.jar", invalidBundle("test.a", "1.0.0"));
        service.start();
        assertThat(reportNames()).containsExactly("a.invalid");
        File report = new File(extensions, "a.invalid");
        String before = Files.readString(report.toPath());

        // a new version of the application: the extension is checked against it again
        applicationTimestamp = System.currentTimeMillis() + 60_000;
        service.restart();

        assertThat(reportNames()).containsExactly("a.invalid");
        assertThat(Files.readString(report.toPath())).isNotEqualTo(before).contains("Result: INVALID");

        // the application fits the extension again
        writeJar("a.jar", bundle("test.a", "1.0.0"));
        applicationTimestamp = System.currentTimeMillis() + 120_000;
        service.restart();

        assertThat(reportNames()).containsExactly("a.valid");
        assertThat(bundleNamed("test.a").getState()).isEqualTo(Bundle.ACTIVE);
    }

    @Test
    void unloadingAnExtensionDeletesItsReport() throws Exception {
        writeJar("a.jar", bundle("test.a", "1.0.0"));
        writeJar("bad.jar", invalidBundle("test.bad", "1.0.0"));
        service.start();
        assertThat(reportNames()).containsExactly("a.valid", "bad.invalid");

        service.uninstallPackage(bundleNamed("test.bad"));
        assertThat(reportNames()).containsExactly("a.valid");

        service.uninstallPackage(bundleNamed("test.a"));
        assertThat(reportNames()).isEmpty();
        assertThat(jarNames()).isEmpty();
    }

    @Test
    void replacingAnExtensionVerifiesTheNewVersion() throws Exception {
        writeJar("a.jar", bundle("test.a", "1.0.0"));
        service.start();
        File report = new File(extensions, "a.valid");
        Files.writeString(report.toPath(), "old report", StandardCharsets.UTF_8);

        service.installPackage("a.jar", bundle("test.a", "1.0.1"));

        assertThat(reportNames()).containsExactly("a.valid");
        assertThat(report).content().startsWith("Extension: a.jar");
    }
}
