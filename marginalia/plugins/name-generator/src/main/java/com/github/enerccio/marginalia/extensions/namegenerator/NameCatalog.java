package com.github.enerccio.marginalia.extensions.namegenerator;

import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

/**
 * Everything the generator can make names of: the Markov chain tables it ships ({@link NameTables}) and the categories other extensions
 * registered ({@link NameSources}). The functions, the commands and the tool dialog use this, so a registered category is one of them
 * like any other.
 */
public final class NameCatalog {

    private final NameTables tables;
    private final NameSources sources;

    NameCatalog(NameTables tables, NameSources sources) {
        this.tables = tables;
        this.sources = sources;
    }

    NameTables tables() {
        return tables;
    }

    NameSources sources() {
        return sources;
    }

    /** The titles of the categories and groups of the tables and of the registered categories, sorted. */
    public List<String> categories() {
        // a title of the generator's own wins over one that an extension's provider has too
        return Stream.concat(tables.categories().stream(), sources.titles().stream().filter(title -> !tables.has(title))).sorted().toList();
    }

    /** The categories in whose title every word of the filter starts a word, see {@link NameTables#filter}. */
    public List<String> categories(String filter) {
        return NameTables.filter(categories(), filter);
    }

    public boolean has(String category) {
        return tables.has(category) || sources.has(category);
    }

    /**
     * Up to {@code count} different names of the category (fewer when it cannot make that many), see
     * {@link NameTables#generate(String, int, Integer, Integer, RandomGenerator)}.
     *
     * @throws IllegalArgumentException when there is no such category
     * @throws IllegalStateException    when the extension that registered the category could not make the names
     */
    public List<String> generate(String category, int count, Integer minLength, Integer maxLength, RandomGenerator random) {
        return tables.has(category) ? tables.generate(category, count, minLength, maxLength, random)
                : sources.generate(category, count, minLength, maxLength, random);
    }
}
