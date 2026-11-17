package com.github.enerccio.marginalia.extensions.smoke;

import java.lang.ref.Reference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Finds what still points into an unloaded plugin: walks the objects of the application (the singletons of the Spring context, the
 * static fields of their classes) through their fields, collections, maps, arrays, {@link AtomicReference}s and {@link Optional}s and
 * reports every object, class or lambda that was loaded by one of the given class loaders, with the path it was found on. A plugin
 * that is unloaded but still registered somewhere (a decorator, a listener, a command, a provider) shows up here, and so does the
 * class loader it keeps alive.
 * <p>
 * Only the classes of the application ({@code com.github.enerccio.marginalia}) are read field by field; of the JDK only the
 * containers above are opened, other libraries (Hibernate, Vaadin, Spring) are not entered. Weak and soft references do not hold
 * anything and are skipped.
 */
public final class PluginLeakFinder {

    private static final String APPLICATION_PACKAGE = "com.github.enerccio.marginalia.";
    private static final int MAX_DEPTH = 14;

    /** A reference into a plugin: where it was found and which plugin it belongs to. */
    public record Leak(String owner, String path, String type) {
        @Override
        public String toString() {
            return owner + ": " + path + " -> " + type;
        }
    }

    private final Function<ClassLoader, String> pluginOf;
    private final Set<Object> visited = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Class<?>> staticsDone = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<String, Leak> leaks = new LinkedHashMap<>();

    /**
     * @param pluginOf the name of the plugin a class loader belongs to, null for a loader of the application
     */
    public PluginLeakFinder(Function<ClassLoader, String> pluginOf) {
        this.pluginOf = pluginOf;
    }

    /** Walks one root; call it for each, then read {@link #leaks()}. */
    public void walk(String name, Object root) {
        visit(root, new ArrayList<>(List.of(name)), 0);
    }

    /** Walks the static fields of a class (a registry that is a static field). */
    public void walkStatics(Class<?> type) {
        statics(type, 0);
    }

    /** One entry per place, in the order they were found. */
    public List<Leak> leaks() {
        return List.copyOf(leaks.values());
    }

    private void visit(Object value, List<String> path, int depth) {
        if (value == null || depth > MAX_DEPTH) {
            return;
        }
        if (value instanceof Class<?> type) {
            check(type, path, "class " + type.getName());
            return;
        }
        Class<?> type = value.getClass();
        if (type.isPrimitive() || value instanceof String || value instanceof Number || value instanceof Boolean
                || value instanceof Character || value instanceof Enum<?> || value instanceof Reference<?>) {
            return;
        }
        if (check(type, path, type.getName())) {
            return;
        }
        if (!visited.add(value)) {
            return;
        }
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : copy(map.entrySet())) {
                String key = String.valueOf(entry.getKey());
                visit(entry.getKey(), with(path, "{key " + shorten(key) + "}"), depth + 1);
                visit(entry.getValue(), with(path, "[" + shorten(key) + "]"), depth + 1);
            }
            return;
        }
        if (value instanceof Collection<?> collection) {
            int i = 0;
            for (Object element : copy(collection)) {
                visit(element, with(path, "[" + i++ + "]"), depth + 1);
            }
            return;
        }
        if (type.isArray()) {
            if (!type.getComponentType().isPrimitive()) {
                int length = Array.getLength(value);
                for (int i = 0; i < length; i++) {
                    visit(Array.get(value, i), with(path, "[" + i + "]"), depth + 1);
                }
            }
            return;
        }
        if (value instanceof AtomicReference<?> reference) {
            visit(reference.get(), with(path, ".get()"), depth + 1);
            return;
        }
        if (value instanceof Optional<?> optional) {
            visit(optional.orElse(null), with(path, ".get()"), depth + 1);
            return;
        }
        if (!type.getName().startsWith(APPLICATION_PACKAGE)) {
            return;
        }
        statics(type, depth);
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            if (!c.getName().startsWith(APPLICATION_PACKAGE)) {
                break;
            }
            for (Field field : c.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive() || !field.trySetAccessible()) {
                    continue;
                }
                try {
                    visit(field.get(value), with(path, "." + field.getName()), depth + 1);
                } catch (IllegalAccessException ignored) {
                    // a field that can't be read can't be checked
                }
            }
        }
    }

    private void statics(Class<?> type, int depth) {
        for (Class<?> c = type; c != null && c.getName().startsWith(APPLICATION_PACKAGE); c = c.getSuperclass()) {
            if (!staticsDone.add(c)) {
                continue;
            }
            for (Field field : c.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive() || !field.trySetAccessible()) {
                    continue;
                }
                try {
                    visit(field.get(null), new ArrayList<>(List.of(c.getSimpleName() + "." + field.getName())), depth + 1);
                } catch (IllegalAccessException | RuntimeException | LinkageError ignored) {
                    // a static that can't be read can't be checked
                }
            }
        }
    }

    /** @return whether the type is of a plugin (reported, not entered) */
    private boolean check(Class<?> type, List<String> path, String description) {
        Class<?> base = type;
        while (base.isArray()) {
            base = base.getComponentType();
        }
        String plugin = pluginOf.apply(base.getClassLoader());
        if (plugin == null) {
            return false;
        }
        String where = String.join("", path);
        leaks.putIfAbsent(plugin + "|" + where, new Leak(plugin, where, description));
        return true;
    }

    private static List<String> with(List<String> path, String step) {
        List<String> next = new ArrayList<>(path.size() + 1);
        next.addAll(path);
        next.add(step);
        return next;
    }

    private static String shorten(String text) {
        return text.length() > 60 ? text.substring(0, 57) + "..." : text;
    }

    private static <T> List<T> copy(Collection<T> collection) {
        // a registry of another thread may change while it is read; what is in it now is enough
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                return new ArrayList<>(collection);
            } catch (RuntimeException e) {
                // ConcurrentModificationException: read again
            }
        }
        return List.of();
    }
}
