package com.github.enerccio.marginalia.extensions.namegenerator;

import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.google.gson.Gson;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The names of the Markov chain tables as commands, for the command bar and for agents: {@code list-name-categories}
 * and {@code generate-names}. The same as the functions {@link NameGeneratorFunctions} give a model that is asked
 * directly, here the caller writes the names into whatever it does next (a prompt, a lore entry).
 */
public class NameGeneratorCommands {

    public static final String LIST = "list-name-categories";
    public static final String GENERATE = "generate-names";

    private static final Gson GSON = new Gson();

    private static final String GENERATE_TEXT = "Generates made-up names of a category from Markov chains (at most "
            + NameGeneratorFunctionsImpl.MAX_COUNT + " at a time). Reads only.";
    private static final String GENERATE_LLM = "Generates made-up names of one category (see list-name-categories), at random each time. Use it "
            + "when you need names for characters, places or things instead of inventing them, ask for a good number (10 or more) and pick "
            + "from them; you may also change a name a little (an ending, a letter) to fit. At most " + NameGeneratorFunctionsImpl.MAX_COUNT
            + " names. Returns a JSON array. Reads only.";
    private static final String PARAM_CATEGORY = "One of the categories from list-name-categories";
    private static final String PARAM_COUNT = "How many names to generate, at most " + NameGeneratorFunctionsImpl.MAX_COUNT;
    private static final String PARAM_MIN = "Shortest name in letters, " + NameTables.FLOOR_LENGTH + " to " + NameTables.CEIL_LENGTH
            + ". Default: the usual one of the category";
    private static final String PARAM_MAX = "Longest name in letters, " + NameTables.FLOOR_LENGTH + " to " + NameTables.CEIL_LENGTH
            + ". Default: the usual one of the category";
    private static final String RESULT = "JSON array of names";

    private final NameTables tables;

    public NameGeneratorCommands(NameTables tables) {
        this.tables = tables;
    }

    @Command(commandName = LIST, humanNameText = "List name categories", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Lists the categories of names that generate-names accepts. Reads only.",
            llmDescriptionText = "Lists the categories of made-up names that generate-names accepts. Call it first. Returns a JSON array. Reads only.",
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "categories", type = String.class, descriptionText = "JSON array of the categories"))
    public void listCategories(CommandContext context) {
        context.setReturn(GSON.toJson(tables.categories()));
    }

    @Command(commandName = GENERATE, humanNameText = "Generate names", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = GENERATE_TEXT, llmDescriptionText = GENERATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "category", type = String.class, descriptionText = PARAM_CATEGORY),
                    @CommandParameter(name = "count", type = Integer.class, descriptionText = PARAM_COUNT)}),
            returns = @CommandReturn(name = "names", type = String.class, descriptionText = RESULT))
    public void generate(CommandContext context) {
        generateNames(context, null, null);
    }

    @Command(commandName = GENERATE, humanNameText = "Generate names", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = GENERATE_TEXT, llmDescriptionText = GENERATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "category", type = String.class, descriptionText = PARAM_CATEGORY),
                    @CommandParameter(name = "count", type = Integer.class, descriptionText = PARAM_COUNT),
                    @CommandParameter(name = "min_length", type = Integer.class, optional = true, descriptionText = PARAM_MIN)}),
            returns = @CommandReturn(name = "names", type = String.class, descriptionText = RESULT))
    public void generateMin(CommandContext context) {
        generateNames(context, context.get("min_length", Integer.class), null);
    }

    @Command(commandName = GENERATE, humanNameText = "Generate names", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = GENERATE_TEXT, llmDescriptionText = GENERATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "category", type = String.class, descriptionText = PARAM_CATEGORY),
                    @CommandParameter(name = "count", type = Integer.class, descriptionText = PARAM_COUNT),
                    @CommandParameter(name = "min_length", type = Integer.class, optional = true, descriptionText = PARAM_MIN),
                    @CommandParameter(name = "max_length", type = Integer.class, optional = true, descriptionText = PARAM_MAX)}),
            returns = @CommandReturn(name = "names", type = String.class, descriptionText = RESULT))
    public void generateMax(CommandContext context) {
        generateNames(context, context.get("min_length", Integer.class), context.get("max_length", Integer.class));
    }

    private void generateNames(CommandContext context, Integer minLength, Integer maxLength) {
        String category = context.get("category", String.class);
        if (category == null || !tables.has(category)) {
            throw new IllegalArgumentException("Unknown category '" + category + "'. Categories: "
                    + String.join(", ", tables.categories()));
        }
        int count = NameGeneratorFunctionsImpl.clampCount(context.get("count", Integer.class));
        context.setReturn(GSON.toJson(tables.generate(category, count, minLength, maxLength, ThreadLocalRandom.current())));
    }

}
