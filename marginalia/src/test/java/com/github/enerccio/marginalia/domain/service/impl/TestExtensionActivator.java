package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Activator of the test bundles built by {@link OsgiServiceImplTest}. The class is loaded from the test classpath
 * (boot delegation), so every bundle shares it; the bundle's manifest headers control what it does.
 * <ul>
 *     <li>{@code X-Test-Fail-Start} - the activator throws</li>
 *     <li>{@code X-Test-Fail-Load} - {@code onExtensionLoad} throws</li>
 * </ul>
 * Loads and unloads are counted per {@code symbolicName:version}.
 */
public class TestExtensionActivator implements BundleActivator {

    public static final Map<String, Integer> LOADED = new ConcurrentHashMap<>();
    public static final Map<String, Integer> UNLOADED = new ConcurrentHashMap<>();

    public static String key(Bundle bundle) {
        return bundle.getSymbolicName() + ":" + bundle.getVersion();
    }

    @Override
    public void start(BundleContext context) {
        if (context.getBundle().getHeaders().get("X-Test-Fail-Start") != null) {
            throw new IllegalStateException("activator of " + key(context.getBundle()) + " failed");
        }
        context.registerService(MarginaliaExtension.class, new TestExtension(), null);
    }

    @Override
    public void stop(BundleContext context) {
    }

    public static class TestExtension implements MarginaliaExtension {

        @Override
        public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
            LOADED.merge(key(bundle), 1, Integer::sum);
            if (bundle.getHeaders().get("X-Test-Fail-Load") != null) {
                throw new IllegalStateException("load of " + key(bundle) + " failed");
            }
        }

        @Override
        public void onExtensionUnload(Bundle b, OsgiServiceImpl osgiService, ExtensionService extensionService) {
            UNLOADED.merge(key(b), 1, Integer::sum);
        }
    }
}
