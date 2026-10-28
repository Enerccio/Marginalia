package com.github.enerccio.marginalia.extensions.nameprovider;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The functions of {@link NameProviderFunctions} on the lists of {@link NameLists}.
 */
public class NameProviderFunctionsImpl implements NameProviderFunctions {

    /** The most names one call returns, whatever the model asks for. */
    public static final int MAX_COUNT = 100;

    private final NameLists lists;

    public NameProviderFunctionsImpl(NameLists lists) {
        this.lists = lists;
    }

    @Override
    public void listLists(FunctionContinuation<List<String>> done, String filter) {
        done.complete(lists.categories(filter));
    }

    @Override
    public void pickNames(FunctionContinuation<List<NameEntry>> done, String category, int count) {
        if (!lists.has(category)) {
            done.fail(NameLists.unknown("category", category, lists.categories(), "list_real_name_lists"));
            return;
        }
        done.complete(lists.pick(category, clampCount(count), ThreadLocalRandom.current()));
    }

    /** The count the model asked for limited to 1..{@link #MAX_COUNT}. */
    static int clampCount(int requested) {
        return Math.clamp(requested, 1, MAX_COUNT);
    }

}
