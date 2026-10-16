package com.github.enerccio.marginalia.extensions.arcoutline.names;

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
 * The only class that touches the name extensions, which are other bundles that can be loaded, unloaded and installed
 * again while the application runs: the Name Generator (names made up by Markov chains) and the Name Provider (real
 * names picked from lists). This bundle does not import their packages: a bundle that did would keep the classes of
 * the unloaded one (and fail to see the extension installed later, its classes are others) until it is restarted. The
 * extensions are found among the loaded ones by the name of their class and used by reflection, their functions
 * interfaces are taken from their class loaders.
 * <p>
 * The categories of both are listed together, each with the prefix of its source ({@link #GENERATOR_PREFIX},
 * {@link #PROVIDER_PREFIX}), and so they are saved. Any, one or both sources can be used in a request: the functions of
 * a source are offered to the model only when a category of it is selected.
 */
public final class NameToolsBridge {

    /** The prefix of the categories of the Name Generator. */
    public static final String GENERATOR_PREFIX = "Generator: ";
    /** The prefix of the categories of the Name Provider. */
    public static final String PROVIDER_PREFIX = "Provider: ";

    /** The name of the generating function of the Name Generator, which tells that its functions are offered. */
    public static final String GENERATOR_FUNCTION = "generate_names";
    /** The name of the picking function of the Name Provider, which tells that its functions are offered. */
    public static final String PROVIDER_FUNCTION = "pick_real_names";

    /**
     * One extension: the prefix of its categories, the classes by name, and the two methods of its functions
     * interface that the selection restricts (the one that lists the categories and the one whose second argument is a
     * category).
     */
    private record Source(String prefix, String extension, String functions, String listMethod, String pickMethod) {}

    private static final List<Source> SOURCES = List.of(
            new Source(GENERATOR_PREFIX, "com.github.enerccio.marginalia.extensions.namegenerator.NameGeneratorExtension",
                    "com.github.enerccio.marginalia.extensions.namegenerator.NameGeneratorFunctions",
                    "listCategories", "generateNames"),
            new Source(PROVIDER_PREFIX, "com.github.enerccio.marginalia.extensions.nameprovider.NameProviderExtension",
                    "com.github.enerccio.marginalia.extensions.nameprovider.NameProviderFunctions",
                    "listLists", "pickNames"));

    private NameToolsBridge() {
    }

    private static MarginaliaExtension find(OsgiService osgiService, Source source) {
        for (OsgiService.LoadedExtension loaded : osgiService.getLoadedExtensions()) {
            if (loaded.extension().getClass().getName().equals(source.extension())) {
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
            throw new IllegalStateException("The name extension " + extension.getClass().getName() + " has no categories()", e);
        }
    }

    /**
     * The categories of the loaded name extensions, each with the prefix of its source (the generator's first), null
     * when none of them is loaded.
     */
    public static List<String> categories(OsgiService osgiService) {
        List<String> all = new ArrayList<>();
        boolean loaded = false;
        for (Source source : SOURCES) {
            MarginaliaExtension extension = find(osgiService, source);
            if (extension != null) {
                loaded = true;
                for (String category : categoriesOf(extension)) {
                    all.add(source.prefix() + category);
                }
            }
        }
        return loaded ? all : null;
    }

    /**
     * The selection as it is saved now: a category without a source prefix is one of the Name Generator, the only
     * extension there was when it was saved. Null stays null.
     */
    public static List<String> normalize(Collection<String> selected) {
        if (selected == null) {
            return null;
        }
        List<String> normalized = new ArrayList<>();
        for (String category : selected) {
            normalized.add(category.startsWith(GENERATOR_PREFIX) || category.startsWith(PROVIDER_PREFIX)
                    ? category : GENERATOR_PREFIX + category);
        }
        return normalized;
    }

    /**
     * The functions limited to the selected categories (the ones the loaded extensions do not have are dropped), the
     * functions of the sources that have a selected category put together; null when no extension is loaded or nothing
     * is left.
     */
    public static FunctionSet functions(OsgiService osgiService, FunctionRegistry registry, Collection<String> selected) {
        List<String> chosen = normalize(selected);
        FunctionSet result = FunctionSet.empty();
        for (Source source : SOURCES) {
            MarginaliaExtension extension = find(osgiService, source);
            if (extension == null) {
                continue;
            }
            List<String> categories = new ArrayList<>();
            for (String category : categoriesOf(extension)) {
                if (chosen.contains(source.prefix() + category)) {
                    categories.add(category);
                }
            }
            if (!categories.isEmpty()) {
                result = result.and(restricted(registry, source, extension, categories));
            }
        }
        return result.isEmpty() ? null : result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static FunctionSet restricted(FunctionRegistry registry, Source source, MarginaliaExtension extension,
                                          List<String> categories) {
        try {
            Class<?> type = extension.getClass().getClassLoader().loadClass(source.functions());
            Object delegate = extension.getClass().getMethod("newFunctions").invoke(extension);
            Object restricted = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type},
                    (proxy, method, args) -> selectedFunctions(source, delegate, categories, method, args));
            return registry.bind((Class) type, restricted);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("The name extension " + source.extension() + " has no functions", e);
        }
    }

    /**
     * The functions of an extension limited to the categories the user selected: the model sees only those and can not
     * get names of the others. The rest is left to the implementation of the extension (it also keeps the cap on the
     * number of names).
     */
    @SuppressWarnings("unchecked")
    private static Object selectedFunctions(Source source, Object delegate, List<String> categories, Method method,
                                            Object[] args) throws Throwable {
        if (method.getName().equals(source.listMethod())) {
            ((FunctionContinuation<List<String>>) args[0]).complete(List.copyOf(categories));
            return null;
        }
        if (method.getName().equals(source.pickMethod())) {
            // the titles are matched without regard to case like the extensions do
            Set<String> allowed = new HashSet<>();
            categories.forEach(c -> allowed.add(c.strip().toLowerCase(Locale.ROOT)));
            String category = (String) args[1];
            if (category == null || !allowed.contains(category.strip().toLowerCase(Locale.ROOT))) {
                ((FunctionContinuation<?>) args[0]).fail("Unknown category '" + category + "'. Categories: "
                        + String.join(", ", categories));
                return null;
            }
        }
        try {
            return method.invoke(delegate, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

}
