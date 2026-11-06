package com.github.enerccio.marginalia.extensions.namegenerator;

import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Gives the model functions to get names (categories, then names of a category from Markov chain tables), as a tool suite
 * and as commands. Both are registered with the tool service, which removes them when the extension is unloaded; the
 * Lore Generator, the Arc Outline and whoever else lets the user choose tools offer the functions to the model.
 * <p>
 * Other extensions can add categories of their own with the public methods at the end of this class ({@link #registerWords},
 * {@link #registerGenerator}, {@link #unregister}, {@link #unregisterAll}), found among the loaded extensions by the name of this class and called by
 * reflection; they use only JDK types and {@link Bundle}, so no class of this plugin is needed on the other side. The names of the methods, their
 * parameters and the keys below are a contract, see the README.
 */
@Configurable
public class NameGeneratorExtension implements MarginaliaExtension {

    /** The key of the tool suite in the tool service; callers save it, don't rename it. */
    public static final String PROVIDER_KEY = "names";

    /** The keys of the request a generator gets (see {@link #registerGenerator}): the number of names, and the lengths when the caller gave any. */
    public static final String REQUEST_COUNT = "count";
    public static final String REQUEST_MIN_LENGTH = "minLength";
    public static final String REQUEST_MAX_LENGTH = "maxLength";

    /** The keys of the options of {@link #registerWords}. */
    public static final String OPTION_ORDER = "order";
    public static final String OPTION_MIN_LENGTH = "minLength";
    public static final String OPTION_MAX_LENGTH = "maxLength";

    /** What the extension can make names of; null while it is not loaded. */
    private volatile NameCatalog catalog;

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private ToolService toolService;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        NameTables tables = NameTables.load();
        catalog = new NameCatalog(tables, new NameSources());
        FunctionRegistry registry = inferenceServices.getFunctionRegistry();
        // fails the load when the interface is not a valid set of functions
        int count = registry.definitionsOf(NameGeneratorFunctions.class).size();
        log.info("Name generator loaded with {} functions and {} categories", count, tables.categories().size());
        toolService.registerProvider(bundle.getSymbolicName(), PROVIDER_KEY, "Name generator",
                new NameGeneratorToolProvider(catalog, registry));
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        toolService.unregisterAll(bundle.getSymbolicName());
        NameCatalog unloaded = catalog;
        catalog = null;
        if (unloaded != null) {
            unloaded.sources().clear();
        }
    }

    // ---- what other extensions call (by reflection, JDK types and Bundle only)

    /**
     * Adds a category made of <b>words</b>: the generator trains its own Markov chain on them and makes names that sound like them, so the owner
     * gives the material (the roots of a constructed language, the names of a setting) and none of the mathematics. The words are asked for again
     * at every call and the chain is trained again when they are not the ones of the last call, so the category follows the owner's data
     * without being told. The supplier is called on the thread of whoever needs the names, with the session that thread has; keep it cheap and
     * return a copy.
     * <p>
     * The category is listed, offered in the tool dialog and generated with like the ones of the generator. Registering a title the owner has
     * already replaces it; a title of the generator's own categories or of another extension is refused.
     *
     * @param owner    the bundle of the caller, its symbolic name says who owns the category; the category goes away when the bundle is not running
     * @param category the title, the owner's choice (a path such as {@code "Conlang - Vrakht"} sorts well)
     * @param words    the words to train on (names or roots; lower case is made of them, a word with {@code ^} or {@code $} is skipped)
     * @param options  may be null: {@value #OPTION_ORDER} (the length of the chain, 1 to {@value NameTables.Chain#MAX_ORDER}, default
     *                 {@value NameSources#DEFAULT_ORDER}), {@value #OPTION_MIN_LENGTH} and {@value #OPTION_MAX_LENGTH} (the usual lengths of a name,
     *                 default those of the shortest and the longest word); numbers
     * @throws IllegalArgumentException for a blank title, one that is not free, or missing arguments
     * @throws IllegalStateException    when the extension is not loaded
     */
    public void registerWords(Bundle owner, String category, Supplier<? extends Collection<String>> words, Map<String, Object> options) {
        NameCatalog current = loaded();
        current.sources().registerWords(owner, category, words, options, current.tables()::has);
    }

    /**
     * Adds a category the owner makes the names of itself (a way of making them the Markov chain does not do: compounds of roots, a table of
     * titles). The generator is only the proxy: it lists the category and calls the function when names of it are wanted.
     * <p>
     * The function gets a read only map with {@value #REQUEST_COUNT} (the number of names, 1 to {@value NameGeneratorFunctionsImpl#MAX_COUNT})
     * and, when the caller gave them, {@value #REQUEST_MIN_LENGTH} and {@value #REQUEST_MAX_LENGTH} (letters; all numbers); it returns the names,
     * and may return fewer. What it returns is stripped, made different and cut to the count. An exception it throws is reported to the caller as
     * a failed call. Same rules for the owner and the title as in {@link #registerWords}.
     *
     * @throws IllegalArgumentException for a blank title, one that is not free, or missing arguments
     * @throws IllegalStateException    when the extension is not loaded
     */
    public void registerGenerator(Bundle owner, String category, Function<Map<String, Object>, List<String>> generator) {
        NameCatalog current = loaded();
        current.sources().registerGenerator(owner, category, generator, current.tables()::has);
    }

    /**
     * Adds a <b>provider</b> of categories made of words: the generator asks it for the categories whenever they are listed or names are
     * wanted, on the thread of the caller, and makes the Markov chain of each itself. Because the provider answers for the caller (the
     * languages of the user whose session this thread has), one registration at load serves every user and nobody sees the categories of
     * another; there is nothing to register again when a user changes a category. A thread that has no session gets nothing from a provider
     * that needs one (an exception of the supplier is taken for "nothing here").
     * <p>
     * The supplier returns the title of each category and its words; a category with no words is not listed. A title of the generator's own
     * categories is shadowed by them. Registering the same key again replaces the provider.
     *
     * @param owner      the bundle of the caller; the provider goes away when it is not running
     * @param key        the owner's name of the provider
     * @param categories the categories of the caller now: title to words, a fresh map each call
     * @throws IllegalArgumentException for missing arguments
     * @throws IllegalStateException    when the extension is not loaded
     */
    public void registerProvider(Bundle owner, String key, Supplier<Map<String, Collection<String>>> categories) {
        loaded().sources().registerProvider(owner, key, categories);
    }

    /**
     * Removes a provider the owner registered.
     *
     * @return false when the owner has no provider of that key
     */
    public boolean unregisterProvider(Bundle owner, String key) {
        NameCatalog current = catalog;
        return current != null && current.sources().unregisterProvider(owner, key);
    }

    /**
     * Makes names from words with the generator's chain, without registering a category: for an extension that lets its user choose the words
     * and wants to show what they give before keeping the choice. Same options as {@link #registerWords}.
     *
     * @param count how many names, limited to 1..{@value NameGeneratorFunctionsImpl#MAX_COUNT}; fewer come back when the words are too few
     * @throws IllegalArgumentException when no word is usable
     */
    public List<String> previewWords(Collection<String> words, Map<String, Object> options, int count) {
        return NameSources.preview(words, options, count, java.util.concurrent.ThreadLocalRandom.current());
    }

    /**
     * Removes a category the owner registered.
     *
     * @return false when the owner has no category of that name
     */
    public boolean unregister(Bundle owner, String category) {
        NameCatalog current = catalog;
        return current != null && current.sources().unregister(owner, category);
    }

    /**
     * Removes every category and provider the owner registered (call it when the owner is unloaded; a bundle that is not running is dropped anyway).
     *
     * @return how many there were
     */
    public int unregisterAll(Bundle owner) {
        NameCatalog current = catalog;
        return current == null ? 0 : current.sources().unregisterAll(owner);
    }

    private NameCatalog loaded() {
        NameCatalog current = catalog;
        if (current == null) {
            throw new IllegalStateException("The name generator is not loaded");
        }
        return current;
    }

}
