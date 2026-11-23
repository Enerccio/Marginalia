package com.github.enerccio.marginalia.extensions.smoke;

import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/** The leak finder of the plugin smoke test, with a class loaded by a loader of its own standing in for a plugin. */
class PluginLeakFinderTest {

    /** What a plugin registers: loaded again by {@link #pluginLoader}, so the copy belongs to the "plugin". */
    public static class PluginListener implements Runnable {
        @Override
        public void run() {
        }
    }

    /** An object of the application that keeps registrations. */
    static class Registry {
        final Map<String, List<Object>> listeners = new ConcurrentHashMap<>();
        final AtomicReference<Object> current = new AtomicReference<>();
        Object[] array = new Object[1];
        WeakReference<Object> weak;
        Nested nested = new Nested();
        static final List<Object> STATIC = new ArrayList<>();
    }

    static class Nested {
        Runnable callback;
    }

    private static ClassLoader pluginLoader() {
        URL classes = PluginLeakFinderTest.class.getProtectionDomain().getCodeSource().getLocation();
        // parent: the platform loader, so the class is defined again instead of being found in the application
        return new URLClassLoader(new URL[]{classes}, ClassLoader.getPlatformClassLoader());
    }

    private static Object pluginObject(ClassLoader loader) throws Exception {
        return loader.loadClass(PluginListener.class.getName()).getDeclaredConstructor().newInstance();
    }

    @Test
    void aPluginObjectHeldAnywhereInTheApplicationIsFoundWithItsPath() throws Exception {
        Registry.STATIC.clear();
        ClassLoader loader = pluginLoader();
        Registry registry = new Registry();
        registry.listeners.computeIfAbsent("generation", k -> new ArrayList<>()).add(pluginObject(loader));
        registry.current.set(pluginObject(loader));
        registry.array[0] = pluginObject(loader);
        registry.nested.callback = (Runnable) pluginObject(loader);
        registry.weak = new WeakReference<>(pluginObject(loader));
        Registry.STATIC.add(pluginObject(loader).getClass());

        PluginLeakFinder finder = new PluginLeakFinder(cl -> cl == loader ? "my-plugin.jar" : null);
        finder.walk("registry", registry);

        assertThat(finder.leaks()).extracting(PluginLeakFinder.Leak::path).containsExactlyInAnyOrder(
                "registry.listeners[generation][0]",
                "registry.current.get()",
                "registry.array[0]",
                "registry.nested.callback",
                "Registry.STATIC[0]");
        assertThat(finder.leaks()).allMatch(leak -> leak.owner().equals("my-plugin.jar"));
    }

    @Test
    void anApplicationThatLetGoHasNoLeaks() throws Exception {
        ClassLoader loader = pluginLoader();
        Registry registry = new Registry();
        registry.listeners.put("generation", new ArrayList<>(List.of(new Object())));
        registry.weak = new WeakReference<>(pluginObject(loader));

        PluginLeakFinder finder = new PluginLeakFinder(cl -> cl == loader ? "my-plugin.jar" : null);
        finder.walk("registry", registry);

        assertThat(finder.leaks()).isEmpty();
    }
}
