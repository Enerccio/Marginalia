package com.github.enerccio.marginalia.extensions.namegenerator;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.domain.traits.LLMParam;

import java.util.List;

/**
 * The functions the model can call to get names: first the categories, then the names of one of them.
 */
public interface NameGeneratorFunctions {

    @LLMFunction(name = "list_name_categories", linkedTool = "generate_names",
            description = "Lists the categories of names that can be generated, all of them or those whose name contains the words of the filter (there are over a thousand, so filter). Call it first: generate_names accepts only these.")
    void listCategories(FunctionContinuation<List<String>> done,
                        @LLMParam(required = false, description = "Words the name of the category has to contain, in any order and "
                                + "case, e.g. \"chad male\" finds \"Africa - Central Africa - Chad - Male\". Default: all categories") String filter);

    @LLMFunction(name = "generate_names", linkedTool = "list_name_categories",
            description = "Generates names of a category. Use it when you need names for characters, places or things instead of inventing them.")
    void generateNames(FunctionContinuation<List<String>> done,
                       @LLMParam(description = "One of the categories from list_name_categories") String category,
                       @LLMParam(description = "How many names to generate, at most " + NameGeneratorFunctionsImpl.MAX_COUNT) int count,
                       @LLMParam(name = "min_length", required = false,
                               description = "Shortest name in letters, " + NameTables.FLOOR_LENGTH + " to " + NameTables.CEIL_LENGTH
                                       + ". Default: the usual one of the category") Integer minLength,
                       @LLMParam(name = "max_length", required = false,
                               description = "Longest name in letters, " + NameTables.FLOOR_LENGTH + " to " + NameTables.CEIL_LENGTH
                                       + ". Default: the usual one of the category") Integer maxLength);

}
