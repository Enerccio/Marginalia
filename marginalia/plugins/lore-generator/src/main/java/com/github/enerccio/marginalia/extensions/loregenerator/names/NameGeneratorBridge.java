package com.github.enerccio.marginalia.extensions.loregenerator.names;

import com.github.enerccio.marginalia.domain.service.FunctionRegistry;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * The only class that touches the name generator extension, which is another bundle that can be loaded, unloaded and
 * installed again while the application runs. This bundle does not import its packages: a bundle that did would keep
 * the classes of the unloaded one (and fail to see the extension installed later, its classes are others) until it is
 * restarted. The extension is found among the loaded ones by the name of its class and used by reflection, its
 * functions interface is taken from its class loader.
 */
public final class NameGeneratorBridge {

    static final String EXTENSION = "com.github.enerccio.marginalia.extensions.namegenerator.NameGeneratorExtension";
    static final String FUNCTIONS = "com.github.enerccio.marginalia.extensions.namegenerator.NameGeneratorFunctions";

    private NameGeneratorBridge() {
    }

    private static MarginaliaExtension find(OsgiService osgiService) {
        for (OsgiService.LoadedExtension loaded : osgiService.getLoadedExtensions()) {
            if (loaded.extension().getClass().getName().equals(EXTENSION)) {
                return loaded.extension();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<String> categoriesOf(MarginaliaExtension extension) {
        try {
            return (List<String>) extension.getClass().getMethod("categories").invoke(extension);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("The name generator has no categories()", e);
        }
    }

    /** The categories of the loaded name generator, null when it is not loaded. */
    public static List<String> categories(OsgiService osgiService) {
        MarginaliaExtension extension = find(osgiService);
        return extension == null ? null : categoriesOf(extension);
    }

    /**
     * The functions limited to the selected categories (the ones the generator does not have are dropped), null when
     * the generator is not loaded or nothing is left.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static FunctionSet functions(OsgiService osgiService, FunctionRegistry registry, Collection<String> selected) {
        MarginaliaExtension extension = find(osgiService);
        if (extension == null) {
            return null;
        }
        List<String> categories = new ArrayList<>();
        for (String category : categoriesOf(extension)) {
            if (selected.contains(category)) {
                categories.add(category);
            }
        }
        if (categories.isEmpty()) {
            return null;
        }
        try {
            Class<?> type = extension.getClass().getClassLoader().loadClass(FUNCTIONS);
            Object delegate = extension.getClass().getMethod("newFunctions").invoke(extension);
            Object restricted = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type},
                    (proxy, method, args) -> selectedFunctions(delegate, categories, method, args));
            return registry.bind((Class) type, restricted);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("The name generator has no functions", e);
        }
    }

    /**
     * The functions of the name generator limited to the categories the user selected: the model sees only those and
     * can not generate names of the others. The rest is left to the implementation of the extension (it also keeps
     * the cap on the number of names).
     */
    @SuppressWarnings("unchecked")
    private static Object selectedFunctions(Object delegate, List<String> categories, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "listCategories" -> {
                ((FunctionContinuation<List<String>>) args[0]).complete(List.copyOf(categories));
                return null;
            }
            case "generateNames" -> {
                // the titles are matched without regard to case like the generator does
                Set<String> allowed = new HashSet<>();
                categories.forEach(c -> allowed.add(c.strip().toLowerCase(Locale.ROOT)));
                String category = (String) args[1];
                if (category == null || !allowed.contains(category.strip().toLowerCase(Locale.ROOT))) {
                    ((FunctionContinuation<List<String>>) args[0]).fail("Unknown category '" + category + "'. Categories: "
                            + String.join(", ", categories));
                    return null;
                }
            }
            default -> {
            }
        }
        try {
            return method.invoke(delegate, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

}
