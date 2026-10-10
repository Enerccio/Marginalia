package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.security.AdminGuard;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.shared.Registration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.osgi.framework.*;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.wiring.FrameworkWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class OsgiServiceImpl implements OsgiService, ApplicationListener<ContextRefreshedEvent>, InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(OsgiServiceImpl.class);

    private static final long FRAMEWORK_TIMEOUT_MS = 30_000;

    @Autowired
    private Configuration configuration;

    @Autowired
    private ExtensionService extensionService;

    @Autowired
    private AdminGuard adminGuard;

    private String extensionPath;
    private Framework f;
    private BundleContext context;

    private final Map<ServiceReference<?>, MarginaliaExtension> activeExtensions = new ConcurrentHashMap<>();
    private final Map<MarginaliaExtension, Set<ComponentBinding>> componentCallbacks = new ConcurrentHashMap<>();
    private final Set<WeakReference<ExtensionObserver>> observers = new HashSet<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        extensionPath = new File(configuration.getFolder(), "extensions").getAbsolutePath();
        File testFile = new File(extensionPath);
        if (!testFile.exists()) {
            testFile.mkdirs();
        }
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        try {
            XmlWebApplicationContext applicationContext = (XmlWebApplicationContext) event.getSource();
            if (applicationContext.getDisplayName().startsWith("Root"))
                start();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void destroy() {
        onExit();
    }

    @Override
    public BundleContext getContext() {
        return context;
    }

    @Override
    public Bundle findBundleByJar(String jarName) {
        for (Bundle b : context.getBundles()) {
            if (locationFile(b).getName().equals(jarName)) {
                return b;
            }
        }
        return null;
    }

    protected void onExit() {
        try {
            if (f != null) {
                stop();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public synchronized void start() throws Exception {
        onBeforeStart();
        Map<String, String> config = new HashMap<>();

        FrameworkFactory ff = ServiceLoader.load(FrameworkFactory.class).iterator().next();

        File extdir = new File(extensionPath);
        if (!extdir.exists())
            extdir.mkdir();

        File bundleInternalStorage = new File(extdir, "org.eclipse.osgi");
        if (bundleInternalStorage.exists())
            FileUtils.deleteDirectory(bundleInternalStorage);

        config.put(Constants.FRAMEWORK_STORAGE, bundleInternalStorage.getAbsolutePath());
        config.put(Constants.FRAMEWORK_STORAGE_CLEAN, "true");
        config.put(Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT, "true");
        config.put(Constants.FRAMEWORK_BUNDLE_PARENT, Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK);
        config.put(Constants.FRAMEWORK_BOOTDELEGATION, "*");

        ClassLoader current = Thread.currentThread().getContextClassLoader();
        try {
            f = ff.newFramework(config);
            f.start();
        } finally {
            Thread.currentThread().setContextClassLoader(current);
        }
        context = f.getBundleContext();

        // every JAR on its own - a broken one must not stop the others from loading
        File[] jars = extdir.listFiles(f -> f.exists() && f.isFile() && f.getName().endsWith(".jar"));
        if (jars != null) {
            Arrays.sort(jars);
            for (File jar : jars) {
                try {
                    context.installBundle(location(jar));
                } catch (Exception e) {
                    log.error("Failed to install extension {}: {}", jar.getName(), e.getMessage(), e);
                }
            }
        }

        for (Bundle b : getBundles()) {
            try {
                startBundle(b);
            } catch (Exception e) {
                log.error("Failed to start extension {} ({}): {}", b.getSymbolicName(), locationFile(b).getName(), e.getMessage(), e);
            }
        }

        onStart();
    }

    @Override
    public synchronized void stop() throws Exception {
        onBeforeStop();
        if (f != null && f.getState() == Framework.ACTIVE) {
            for (Bundle b : getBundles()) {
                try {
                    stopBundleInternal(b);
                    b.stop();
                } catch (Exception e) {
                    log.error("Failed to stop extension {}: {}", b.getSymbolicName(), e.getMessage(), e);
                }
            }

            log.trace("stopping extension start");
            f.stop();
            FrameworkEvent event = f.waitForStop(FRAMEWORK_TIMEOUT_MS);
            if (event.getType() == FrameworkEvent.WAIT_TIMEDOUT) {
                log.warn("OSGi framework did not stop in {} ms", FRAMEWORK_TIMEOUT_MS);
            }
            log.trace("stopping extension stop");
        }
        activeExtensions.clear();
        componentCallbacks.clear();
        onStop();
    }

    @Override
    public void restart() throws Exception {
        stop();
        start();
    }

    @Override
    public synchronized Bundle installPackage(String fileName, byte[] data) throws Exception {
        adminGuard.requireAdmin();
        File extdir = new File(extensionPath);
        File target = new File(extdir, sanitizeJarName(fileName));
        // not a .jar, so a crash in the middle doesn't leave it to be loaded on the next start
        File upload = File.createTempFile("upload-", ".tmp", extdir);
        Map<File, File> backups = new LinkedHashMap<>();
        boolean targetWritten = false;

        onBeforeStop();
        onStop();
        onBeforeStart();
        try {
            FileUtils.writeByteArrayToFile(upload, data);
            String symbolicName = readSymbolicName(upload);

            // an upload of an installed bundle (same file or same symbolic name) replaces it
            for (Bundle old : getBundles()) {
                if (symbolicName.equals(old.getSymbolicName()) || location(target).equals(old.getLocation())) {
                    File oldFile = locationFile(old);
                    log.info("Replacing extension {} ({})", old.getSymbolicName(), oldFile.getName());
                    try {
                        stopBundleInternal(old);
                    } catch (Exception e) {
                        log.error("Failed to unload extension {}: {}", old.getSymbolicName(), e.getMessage(), e);
                    }
                    old.uninstall();
                    if (oldFile.exists()) {
                        File backup = new File(oldFile.getPath() + ".old");
                        Files.move(oldFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        backups.put(oldFile, backup);
                    }
                }
            }
            if (!backups.isEmpty()) {
                refreshBundles();
            }

            Files.move(upload.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            targetWritten = true;

            Bundle b = context.installBundle(location(target));
            try {
                startBundle(b);
            } catch (Exception e) {
                b.uninstall();
                throw e;
            }

            for (File backup : backups.values()) {
                Files.deleteIfExists(backup.toPath());
            }
            return b;
        } catch (Exception e) {
            if (targetWritten) {
                Files.deleteIfExists(target.toPath());
            }
            restoreBackups(backups);
            throw e;
        } finally {
            Files.deleteIfExists(upload.toPath());
            onStart();
        }
    }

    private void restoreBackups(Map<File, File> backups) {
        for (Map.Entry<File, File> entry : backups.entrySet()) {
            try {
                Files.move(entry.getValue().toPath(), entry.getKey().toPath(), StandardCopyOption.REPLACE_EXISTING);
                startBundle(context.installBundle(location(entry.getKey())));
            } catch (Exception e) {
                log.error("Failed to restore extension {}: {}", entry.getKey().getName(), e.getMessage(), e);
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public synchronized void uninstallPackage(Bundle b) throws Exception {
        adminGuard.requireAdmin();
        if (b != null && b.getBundleId() != 0) {
            onBeforeStop();
            onStop();
            onBeforeStart();
            try {
                try {
                    stopBundleInternal(b);
                } finally {
                    b.uninstall();
                    locationFile(b).delete();
                }
                refreshBundles();
            } finally {
                onStart();
            }
        }
    }

    @Override
    public List<Bundle> getBundles() {
        if (f != null && f.getState() == Framework.ACTIVE) {
            List<Bundle> bundles = new ArrayList<>();
            for (Bundle bundle : context.getBundles()) {
                // 0 is the framework itself
                if (bundle.getBundleId() != 0) {
                    bundles.add(bundle);
                }
            }
            return bundles;
        }

        return new ArrayList<>();
    }

    private void startBundle(Bundle b) throws Exception {
        b.start();
        try {
            startBundleInternal(b);
        } catch (Exception e) {
            try {
                b.stop();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            throw e;
        }
    }

    private void startBundleInternal(Bundle b) throws Exception {
        for (ServiceReference<?> sr : getServices(b, MarginaliaExtension.class)) {
            if (activeExtensions.containsKey(sr)) {
                continue;
            }
            MarginaliaExtension extension = (MarginaliaExtension) getContext().getService(sr);
            if (extension != null) {
                componentCallbacks.put(extension, ConcurrentHashMap.newKeySet());
                activeExtensions.put(sr, extension);
                try {
                    extension.onExtensionLoad(b, this, extensionService);
                } catch (RuntimeException e) {
                    // undo what the extension managed to register before it failed
                    activeExtensions.remove(sr);
                    try {
                        unloadExtension(b, sr, extension);
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                    throw e;
                }
            }
        }
    }

    private void stopBundleInternal(Bundle b) throws Exception {
        for (ServiceReference<?> sr : getServices(b, MarginaliaExtension.class)) {
            MarginaliaExtension extension = activeExtensions.remove(sr);
            if (extension != null) {
                unloadExtension(b, sr, extension);
            }
        }
    }

    private void unloadExtension(Bundle b, ServiceReference<?> sr, MarginaliaExtension extension) {
        Set<ComponentBinding> bindings = componentCallbacks.remove(extension);
        if (bindings != null) {
            for (ComponentBinding binding : bindings) {
                binding.release();
            }
        }
        try {
            extension.onExtensionUnload(b, this, extensionService);
        } finally {
            getContext().ungetService(sr);
        }
    }

    @Override
    public <T extends Component> T bindAttachableComponent(T component, Runnable callback, MarginaliaExtension extension) throws Exception {
        Set<ComponentBinding> bindings = componentCallbacks.get(extension);
        if (bindings == null) {
            throw new IllegalStateException("Extension " + extension.getClass().getName() + " is not loaded");
        }
        ComponentBinding binding = new ComponentBinding(component, callback);
        if (component.isAttached()) {
            bindings.add(binding);
        }
        binding.attachRegistration = component.addAttachListener(event -> {
            Set<ComponentBinding> current = componentCallbacks.get(extension);
            if (current != null) {
                current.add(binding);
            } else {
                // the extension was unloaded while the component was detached
                binding.release();
            }
        });
        binding.detachRegistration = component.addDetachListener(event -> {
            Set<ComponentBinding> current = componentCallbacks.get(extension);
            if (current != null) {
                current.remove(binding);
            }
        });
        return component;
    }

    private void refreshBundles() throws InterruptedException {
        CountDownLatch done = new CountDownLatch(1);
        f.adapt(FrameworkWiring.class).refreshBundles(null, event -> done.countDown());
        if (!done.await(FRAMEWORK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
            log.warn("OSGi bundle refresh did not finish in {} ms", FRAMEWORK_TIMEOUT_MS);
        }
    }

    private static String location(File jar) {
        return "file:" + jar.getPath();
    }

    private static File locationFile(Bundle b) {
        String s = b.getLocation().replaceAll("file:", "");
        s = s.replaceAll("\\\\", "/");
        return new File(s);
    }

    static String sanitizeJarName(String fileName) {
        String baseName = FilenameUtils.getBaseName(StringUtils.defaultIfBlank(fileName, "extension"));
        baseName = baseName.replaceAll("[^A-Za-z0-9._-]", "_").replaceAll("^\\.+", "");
        return StringUtils.defaultIfBlank(baseName, "extension") + ".jar";
    }

    private static String readSymbolicName(File jar) throws IOException, BundleException {
        try (JarFile jarFile = new JarFile(jar)) {
            Manifest manifest = jarFile.getManifest();
            String symbolicName = manifest != null ? manifest.getMainAttributes().getValue(Constants.BUNDLE_SYMBOLICNAME) : null;
            if (StringUtils.isBlank(symbolicName)) {
                throw new BundleException("Not an OSGi bundle: the manifest has no " + Constants.BUNDLE_SYMBOLICNAME);
            }
            return StringUtils.substringBefore(symbolicName, ";").trim();
        }
    }

    @Override
    public List<ServiceReference<?>> getServices(Bundle b, Class<?> service) throws InvalidSyntaxException {
        List<ServiceReference<?>> l = new ArrayList<>();
        String serviceName = service != null ? service.getName() : null;

        if (b != null) {
            ServiceReference<?>[] sa = context.getServiceReferences(serviceName, null);
            if (sa != null) {
                for (ServiceReference<?> s : sa) {
                    if (s.getBundle() == b) {
                        l.add(s);
                    }
                }
            }
        }

        return l;
    }

    @Override
    public List<ServiceReference<?>> getServices(Class<?> service) throws InvalidSyntaxException {
        ServiceReference<?>[] sa = context.getServiceReferences(service.getName(), null);
        List<ServiceReference<?>> ls = new ArrayList<>();

        if (sa != null) {
            ls.addAll(Arrays.asList(sa));
        }

        return ls;
    }

    @Override
    public synchronized void addSubscriber(ExtensionObserver observer) {
        WeakReference<ExtensionObserver> wes = new WeakReference<>(observer);
        observers.add(wes);
        observer.beforeServiceStarted(this);
        observer.afterServiceStarted(this);
    }

    @Override
    public synchronized void removeSubscriber(ExtensionObserver observer) {
        for (WeakReference<ExtensionObserver> wes : observers) {
            if (wes.get() != null && wes.get() == observer) {
                observers.remove(wes);
                observer.beforeServiceStopped(this);
                observer.afterServiceStopped(this);
                return;
            }
        }
    }

    @Override
    public String getExtensionsPath() {
        return extensionPath;
    }

    protected synchronized void onBeforeStart() {
        runObserverMethod(ExtensionObserver::beforeServiceStarted);
    }

    protected synchronized void onStart() {
        runObserverMethod(ExtensionObserver::afterServiceStarted);
    }

    protected synchronized void onBeforeStop() {
        runObserverMethod(ExtensionObserver::beforeServiceStopped);
    }

    protected synchronized void onStop() {
        runObserverMethod(ExtensionObserver::afterServiceStopped);
    }

    private synchronized void runObserverMethod(ObserverCallback method) {
        Set<WeakReference<ExtensionObserver>> remSet = new HashSet<>();
        for (WeakReference<ExtensionObserver> observer : observers) {
            if (observer.get() != null)
                method.run(Objects.requireNonNull(observer.get()), this);
            else
                remSet.add(observer);
        }
        observers.removeAll(remSet);
    }

    protected interface ObserverCallback {

        void run(ExtensionObserver observer, OsgiService service);

    }

    /**
     * A cleanup callback of an extension, bound to a component while the component is attached.
     */
    private static final class ComponentBinding {

        private final Component component;
        private final Runnable callback;
        private Registration attachRegistration;
        private Registration detachRegistration;

        private ComponentBinding(Component component, Runnable callback) {
            this.component = component;
            this.callback = callback;
        }

        /**
         * Runs the callback once, in the session of the component's UI, and stops tracking the component.
         */
        private void release() {
            component.getUI().ifPresentOrElse(ui -> ui.access(this::releaseNow), this::releaseNow);
        }

        private void releaseNow() {
            try {
                if (attachRegistration != null) {
                    attachRegistration.remove();
                }
                if (detachRegistration != null) {
                    detachRegistration.remove();
                }
                callback.run();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
