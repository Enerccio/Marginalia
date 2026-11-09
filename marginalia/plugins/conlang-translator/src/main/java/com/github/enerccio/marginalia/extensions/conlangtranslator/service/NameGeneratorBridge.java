package com.github.enerccio.marginalia.extensions.conlangtranslator.service;

import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.NameCategory;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The one place that talks to the Name generator (a direct call, by reflection: the plugins do not see each other's classes). The plugin gives it
 * <b>one provider</b> of categories of names, registered when the plugin loads and again whenever the generator is loaded; the generator asks
 * the provider whenever it lists or generates, on the thread of the caller, and the provider answers from the library of the user whose session
 * that thread has. So nothing is registered per user or per change: a user sees their own categories, as they are saved, in the tool dialogs,
 * the commands and the agent runs, and nobody sees another's. A thread with no session gets nothing.
 */
@Configurable
public class NameGeneratorBridge implements OsgiService.ExtensionListener {

    static final String EXTENSION = "com.github.enerccio.marginalia.extensions.namegenerator.NameGeneratorExtension";
    static final String PROVIDER_KEY = "name-categories";

    @Autowired
    private OsgiService osgiService;

    private final Bundle owner;
    private final LanguageLibrary library;
    private final Logger log;

    public NameGeneratorBridge(Bundle owner, LanguageLibrary library, Logger log) {
        this.owner = owner;
        this.library = library;
        this.log = log;
    }

    /** Gives the generator the provider if it is loaded, and again whenever it is loaded later or anew. */
    public void start() {
        osgiService.addExtensionListener(this);
        register();
    }

    /** Takes back everything this plugin gave (the plugin is unloaded). */
    public void stop() {
        osgiService.removeExtensionListener(this);
        try {
            Object generator = generator();
            if (generator != null) {
                call(generator, "unregisterAll", new Class<?>[]{Bundle.class}, owner);
            }
        } catch (Exception e) {
            log.warn("The categories of names could not be taken back from the name generator: {}", e.toString());
        }
    }

    @Override
    public void extensionLoaded(Bundle bundle, MarginaliaExtension extension) {
        if (EXTENSION.equals(extension.getClass().getName())) {
            register();
        }
    }

    @Override
    public void extensionUnloaded(Bundle bundle, MarginaliaExtension extension) {
    }

    /** Whether the Name generator is loaded now. */
    public boolean available() {
        return generator() != null;
    }

    private void register() {
        try {
            Object generator = generator();
            if (generator != null) {
                Supplier<Map<String, Collection<String>>> categories = this::categoriesOfCaller;
                call(generator, "registerProvider", new Class<?>[]{Bundle.class, String.class, Supplier.class}, owner, PROVIDER_KEY, categories);
            }
        } catch (Exception e) {
            log.warn("The categories of names could not be given to the name generator: {}", e.toString());
        }
    }

    /**
     * The categories of names of the languages in the library of the user whose session this thread has, by title, with their words as they
     * are now; one without a usable word is left out.
     */
    Map<String, Collection<String>> categoriesOfCaller() {
        try {
            Map<String, Collection<String>> found = new LinkedHashMap<>();
            for (Language language : library.languages()) {
                for (NameCategory category : language.getNameCategories()) {
                    List<String> words = language.nameWords(category);
                    if (!words.isEmpty()) {
                        found.put(language.nameCategoryTitle(category), words);
                    }
                }
            }
            return found;
        } catch (Exception e) {
            throw new IllegalStateException("The languages can't be read: " + e.getMessage(), e);
        }
    }

    /**
     * Names made from the words by the generator's chain, nothing is registered.
     *
     * @throws IllegalStateException    when the Name generator is not loaded
     * @throws IllegalArgumentException when no word is usable
     */
    @SuppressWarnings("unchecked")
    public List<String> preview(Collection<String> words, int count) {
        Object generator = generator();
        if (generator == null) {
            throw new IllegalStateException("The name generator is not installed");
        }
        try {
            return (List<String>) call(generator, "previewWords", new Class<?>[]{Collection.class, Map.class, int.class}, words, null, count);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /** The loaded Name generator extension, found again at every call since it can be unloaded and installed again. */
    private Object generator() {
        return osgiService.getLoadedExtensions().stream().map(OsgiService.LoadedExtension::extension)
                .filter(extension -> EXTENSION.equals(extension.getClass().getName())).findFirst().orElse(null);
    }

    /** Calls a public method of the generator; the exception it throws is thrown as it is. */
    private static Object call(Object generator, String method, Class<?>[] types, Object... arguments) throws Exception {
        Method found = generator.getClass().getMethod(method, types);
        try {
            return found.invoke(generator, arguments);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception cause) {
                throw cause;
            }
            throw e;
        }
    }
}
