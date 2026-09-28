package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NonNull;
import org.osgi.framework.*;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OsgiServiceImpl implements OsgiService, ApplicationListener<ContextRefreshedEvent>, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(OsgiServiceImpl.class);

    @Autowired
    private Configuration configuration;

    @Autowired
    private ExtensionService extensionService;

    private String extensionPath;
    private Framework f;
    private BundleContext context;

    private final Map<ServiceReference<?>, MarginaliaExtension> activeExtensions = new ConcurrentHashMap<>();
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
    public BundleContext getContext() {
        return context;
    }

    @Override
    public Bundle findBundleByJar(String jarName) {
        for (Bundle b : context.getBundles()) {
            String s = b.getLocation().replaceAll("file:", "");
            s = s.replaceAll("\\\\", "/");
            File f = new File(s);
            if (f.getName().equals(jarName)) {
                return b;
            }
        }
        return null;
    }

    @SuppressWarnings("BusyWait")
    protected void onExit() {
        try {
            if (f != null) {
                stop();
                while (f.getState() != Framework.RESOLVED)
                    Thread.sleep(100);
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

        config.put(Constants.FRAMEWORK_STORAGE, extensionPath);
        config.put(Constants.FRAMEWORK_STORAGE_CLEAN, "true");
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

        for (Bundle bundle : new ArrayList<>(getBundles())) {
            if (isOurBundle(bundle)) {
                bundle.uninstall();
            }
        }

        if (extdir.exists()) {
            File[] jars = extdir.listFiles(f -> f.exists() && f.isFile() && f.getName().endsWith(".jar"));
            if (jars != null)
                for (File f : jars) {
                    context.installBundle("file:" + f.getPath());
                }
        }

        for (Bundle b : context.getBundles()) {
            b.start();
            startBundleInternal(b);
        }

        onStart();
    }

    private boolean isOurBundle(Bundle bundle) throws InvalidSyntaxException {
        List<ServiceReference<?>> serviceReferences = getServices(bundle, MarginaliaExtension.class);
        return serviceReferences != null && !serviceReferences.isEmpty();
    }

    @Override
    public synchronized void stop() throws Exception {
        onBeforeStop();
        if (f != null && f.getState() == Framework.ACTIVE) {
            for (Bundle b : context.getBundles()) {
                stopBundleInternal(b);
                b.stop();
            }

            log.trace("stopping extension start");
            f.stop();
            log.trace("stopping extension stop");
        }
        onStop();
    }

    @Override
    public void restart() throws Exception {
        stop();
        start();
    }

    @Override
    public void installPackage(File f) throws Exception {
        onBeforeStop();
        onStop();
        onBeforeStart();

        Bundle b = context.installBundle("file:" + f.getPath());

        b.start();

        startBundleInternal(b);
        onStart();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void uninstallPackage(Bundle b) throws Exception {
        if (b != null) {
            onBeforeStop();
            onStop();
            onBeforeStart();

            stopBundleInternal(b);

            b.uninstall();

            String s = b.getLocation().replaceAll("file:", "");
            s = s.replaceAll("\\\\", "/");
            File f = new File(s);

            f.delete();

            onStart();
        }
    }

    @Override
    public List<Bundle> getBundles() {
        try {
            if (f != null && f.getState() == Framework.ACTIVE) {
                Bundle[] bundles = context.getBundles();
                List<Bundle> ourBundles = new ArrayList<>();
                for (Bundle bundle : bundles) {
                    List<ServiceReference<?>> services = getServices(bundle, MarginaliaExtension.class);
                    if (!services.isEmpty()) {
                        ourBundles.add(bundle);
                    }
                }
                return ourBundles;
            }

            return new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void startBundleInternal(Bundle b) throws Exception {
        for (ServiceReference<?> sr : getServices(b, MarginaliaExtension.class)) {
            MarginaliaExtension extension = (MarginaliaExtension) getContext().getService(sr);
            if (extension != null) {
                activeExtensions.put(sr, extension);
                extension.onExtensionLoad(b, this, extensionService);
            }
        }
    }

    private void stopBundleInternal(Bundle b) throws Exception {
        for (ServiceReference<?> sr : getServices(b, MarginaliaExtension.class)) {
            MarginaliaExtension extension = activeExtensions.remove(sr);
            if (extension != null) {
                try {
                    extension.onExtensionUnload(b, this, extensionService);
                } finally {
                    getContext().ungetService(sr);
                }
            }
        }
    }

    @Override
    public List<ServiceReference<?>> getServices(Bundle b, Class<?> service) throws InvalidSyntaxException {
        List<ServiceReference<?>> l = new ArrayList<>();
        String serviceName = service != null ? service.getName() : null;

        context.getBundles();

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
            ls.addAll(Arrays.asList(context.getServiceReferences(service.getName(), null)));
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
}
