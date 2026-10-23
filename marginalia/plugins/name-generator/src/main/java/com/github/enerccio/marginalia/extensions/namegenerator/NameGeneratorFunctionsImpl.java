package com.github.enerccio.marginalia.extensions.namegenerator;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The functions of {@link NameGeneratorFunctions} on the Markov chain tables of {@link NameTables}.
 */
public class NameGeneratorFunctionsImpl implements NameGeneratorFunctions {

    /** The most names one call returns, whatever the model asks for. */
    public static final int MAX_COUNT = 100;

    private final NameTables tables;

    public NameGeneratorFunctionsImpl(NameTables tables) {
        this.tables = tables;
    }

    @Override
    public void listCategories(FunctionContinuation<List<String>> done, String filter) {
        done.complete(tables.categories(filter));
    }

    @Override
    public void generateNames(FunctionContinuation<List<String>> done, String category, int count,
                              Integer minLength, Integer maxLength) {
        if (!tables.has(category)) {
            done.fail("Unknown category '" + category + "'. Categories: " + String.join(", ", tables.categories()));
            return;
        }
        done.complete(tables.generate(category, clampCount(count), minLength, maxLength, ThreadLocalRandom.current()));
    }

    /** The count the model asked for limited to 1..{@link #MAX_COUNT}. */
    static int clampCount(int requested) {
        return Math.clamp(requested, 1, MAX_COUNT);
    }

}
