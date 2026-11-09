package com.github.enerccio.marginalia.workset;

import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Starts plugins in the harness <b>without OSGi</b>: their compiled classes are on the classpath (surefire's {@code additionalClasspathElements}, see the README),
 * the extension is created with {@code new} (the weaving injects its {@code @Autowired} fields from the Spring context of the test, as in production) and its
 * {@code onExtensionLoad} is called by hand, which is exactly what {@code OsgiServiceImpl.startBundleInternal} does once it has found the service. The extension
 * then registers its commands, its passage kinds, its text transformers and its listeners of the generation events itself; nothing is copied here.
 * <p>
 * What the extension gets instead of the framework: a {@link Bundle} that knows its symbolic name (the owner under which the extension registers its things) and an
 * {@link OsgiService} that makes the logger. Everything else of both throws, so a plugin that needs more shows up at once instead of getting a {@code null}.
 * The decorators of UI classes a plugin registers are inert here (nothing is instrumented, nothing calls them).
 */
public class PluginHost {

    /** The short names of {@code workset.plugins} and the classes they stand for. */
    public static final Map<String, String> KNOWN = new LinkedHashMap<>();

    static {
        KNOWN.put("knowledge", "com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerExtension");
        KNOWN.put("reallang", "com.github.enerccio.marginalia.extensions.reallangtranslator.ReallangTranslatorExtension");
        KNOWN.put("conlang", "com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorExtension");
        KNOWN.put("audiobook", "com.github.enerccio.marginalia.extensions.audiobook.AudiobookExtension");
        KNOWN.put("arcoutline", "com.github.enerccio.marginalia.extensions.arcoutline.ArcOutlineExtension");
    }

    private record Loaded(String name, MarginaliaExtension extension, Bundle bundle) {
    }

    private final ExtensionService extensionService;
    private final OsgiService osgi = osgiStub();
    private final List<Loaded> loaded = new ArrayList<>();

    public PluginHost(ExtensionService extensionService) {
        this.extensionService = extensionService;
    }

    /**
     * Loads the plugins, in the order given.
     *
     * @param names short names from {@link #KNOWN}
     * @throws IllegalStateException with the way to put the classes on the classpath when one is not there
     */
    public void load(List<String> names) throws Exception {
        for (String name : names) {
            String className = KNOWN.get(name);
            if (className == null) {
                throw new IllegalArgumentException("Unknown plugin '" + name + "'; known: " + KNOWN.keySet());
            }
            if (loaded.stream().anyMatch(l -> l.name.equals(name))) {
                continue;
            }
            Class<?> type;
            try {
                type = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("The classes of the plugin '" + name + "' are not on the classpath (" + className + "). Build it (mvn package in marginalia/plugins/<name>, "
                        + "after mvn install of the app and of marginalia/libs/translator-common) and pass the folders of target/classes to the run: "
                        + "-Dmaven.test.additionalClasspath=<plugin>/target/classes,<libs>/translator-common/target/classes (see workset/README.md)", e);
            }
            MarginaliaExtension extension = (MarginaliaExtension) type.getDeclaredConstructor().newInstance();
            Bundle bundle = bundleStub("marginalia.workset." + name);
            extension.onExtensionLoad(bundle, osgi, extensionService);
            loaded.add(new Loaded(name, extension, bundle));
            System.out.println("[workset] plugin loaded: " + name);
        }
    }

    public boolean isLoaded(String name) {
        return loaded.stream().anyMatch(l -> l.name.equals(name));
    }

    /** Unloads in the opposite order; a failure is printed and goes on (the JVM ends anyway). */
    public void unload() {
        for (int i = loaded.size() - 1; i >= 0; i--) {
            Loaded l = loaded.get(i);
            try {
                l.extension.onExtensionUnload(l.bundle, osgi, extensionService);
            } catch (Throwable t) {
                System.out.println("[workset] plugin " + l.name + " did not unload cleanly: " + t);
            }
        }
        loaded.clear();
    }

    // ------------------------------------------------------------------------------------------------------------ the stubs

    private static Bundle bundleStub(String symbolicName) {
        return (Bundle) Proxy.newProxyInstance(PluginHost.class.getClassLoader(), new Class<?>[]{Bundle.class}, (proxy, method, args) -> {
            Object common = objectMethod(proxy, method, args, "bundle " + symbolicName);
            if (common != NOT_AN_OBJECT_METHOD) {
                return common;
            }
            return switch (method.getName()) {
                case "getSymbolicName" -> symbolicName;
                case "getBundleId" -> 0L;
                case "getState" -> Bundle.ACTIVE;
                default -> throw new UnsupportedOperationException("The harness bundle does not implement Bundle." + method.getName()
                        + ": a plugin needs it, add it to PluginHost.bundleStub");
            };
        });
    }

    private static OsgiService osgiStub() {
        InvocationHandler handler = (proxy, method, args) -> {
            Object common = objectMethod(proxy, method, args, "osgi service of the harness");
            if (common != NOT_AN_OBJECT_METHOD) {
                return common;
            }
            if (method.getName().equals("createLogger")) {
                Bundle bundle = (Bundle) args[0];
                String name = args.length > 1 ? (String) args[1] : bundle.getSymbolicName();
                Logger logger = LoggerFactory.getLogger("extension." + name);
                return logger;
            }
            throw new UnsupportedOperationException("The harness OsgiService does not implement " + method.getName() + ": a plugin needs it, add it to PluginHost.osgiStub");
        };
        return (OsgiService) Proxy.newProxyInstance(PluginHost.class.getClassLoader(), new Class<?>[]{OsgiService.class}, handler);
    }

    private static final Object NOT_AN_OBJECT_METHOD = new Object();

    private static Object objectMethod(Object proxy, Method method, Object[] args, String description) {
        return switch (method.getName()) {
            case "toString" -> method.getParameterCount() == 0 ? description : NOT_AN_OBJECT_METHOD;
            case "hashCode" -> method.getParameterCount() == 0 ? System.identityHashCode(proxy) : NOT_AN_OBJECT_METHOD;
            case "equals" -> method.getParameterCount() == 1 ? (Object) (proxy == args[0]) : NOT_AN_OBJECT_METHOD;
            default -> NOT_AN_OBJECT_METHOD;
        };
    }
}
