package com.github.enerccio.marginalia.extensions.nameprovider;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.domain.traits.LLMParam;

import java.util.List;

/**
 * The functions the model can call to get real names: first the lists, then names picked from one of them. The names
 * differ from the ones of the name generator so that a request can have both.
 */
public interface NameProviderFunctions {

    @LLMFunction(name = "list_real_name_lists",
            description = "Lists the lists of real names (forenames and surnames of countries), all of them or those whose name contains the words of the filter. Call it first: pick_real_names accepts only these.")
    void listLists(FunctionContinuation<List<String>> done,
                   @LLMParam(required = false, description = "Words the name of the list has to contain, in any order and case, "
                           + "e.g. \"czech female\". Default: all lists") String filter);

    @LLMFunction(name = "pick_real_names",
            description = "Picks real, existing names at random from a list. Each name is a pair: the romanized name (Latin script) "
                    + "and the original one (as written in its own language, the same when it is written in Latin script).")
    void pickNames(FunctionContinuation<List<NameEntry>> done,
                   @LLMParam(description = "One of the lists from list_real_name_lists") String category,
                   @LLMParam(description = "How many names to pick, at most " + NameProviderFunctionsImpl.MAX_COUNT) int count);

}
