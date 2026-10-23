package com.github.enerccio.marginalia.extensions.nameprovider;

import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.google.gson.Gson;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The real names of the lists as commands, for the command bar and for agents: {@code list-real-name-lists} and
 * {@code pick-real-names}. The same as the functions {@link NameProviderFunctions} give a model that is asked directly,
 * here the caller writes the names into whatever it does next (a prompt, a lore entry).
 */
public class NameProviderCommands {

    public static final String LIST = "list-real-name-lists";
    public static final String PICK = "pick-real-names";

    private static final Gson GSON = new Gson();

    private final NameLists lists;

    public NameProviderCommands(NameLists lists) {
        this.lists = lists;
    }

    private static final String LIST_TEXT = "Lists the lists of real names (forenames and surnames of countries) that pick-real-names accepts, "
            + "all of them or those whose name contains the words of the filter. Reads only.";
    private static final String LIST_LLM = "Lists the lists of real names (forenames and surnames of countries) that pick-real-names accepts. "
            + "Call it first; give `filter`, words the name of the list contains (in any order, not case sensitive), to list only those. "
            + "Returns a JSON array. Reads only.";

    @Command(commandName = LIST, humanNameText = "List real name lists", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = LIST_TEXT, llmDescriptionText = LIST_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "lists", type = String.class, descriptionText = "JSON array of the lists"))
    public void listLists(CommandContext context) {
        context.setReturn(GSON.toJson(lists.categories()));
    }

    @Command(commandName = LIST, humanNameText = "List real name lists", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = LIST_TEXT, llmDescriptionText = LIST_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "filter", type = String.class,
                            descriptionText = "Words the name of the lists contains, in any order, not case sensitive")}),
            returns = @CommandReturn(name = "lists", type = String.class, descriptionText = "JSON array of the lists"))
    public void listListsFiltered(CommandContext context) {
        context.setReturn(GSON.toJson(lists.categories(context.get("filter", String.class))));
    }

    @Command(commandName = PICK, humanNameText = "Pick real names", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Picks real names at random from a list (at most " + NameProviderFunctionsImpl.MAX_COUNT + " at a time). Reads only.",
            llmDescriptionText = "Picks real, existing names at random from one list (see list-real-name-lists), different each time. Each "
                    + "name is a pair: the romanized name (Latin script) and the original one (as written in its own language, the same when "
                    + "it is written in Latin script). Use it when you need names for characters instead of inventing them, ask for a good "
                    + "number (10 or more) and pick one that fits; write it as it was returned. A forename and a surname of the same country "
                    + "go together. At most " + NameProviderFunctionsImpl.MAX_COUNT + " names. Returns a JSON array of "
                    + "{romanized, original}. Reads only.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "list", type = String.class, descriptionText = "One of the lists from list-real-name-lists"),
                    @CommandParameter(name = "count", type = Integer.class,
                            descriptionText = "How many names to pick, at most " + NameProviderFunctionsImpl.MAX_COUNT)}),
            returns = @CommandReturn(name = "names", type = String.class, descriptionText = "JSON array of {romanized, original}"))
    public void pick(CommandContext context) {
        String list = context.get("list", String.class);
        if (list == null || !lists.has(list)) {
            throw new IllegalArgumentException("Unknown list '" + list + "'. Lists: " + String.join(", ", lists.categories()));
        }
        int count = NameProviderFunctionsImpl.clampCount(context.get("count", Integer.class));
        context.setReturn(GSON.toJson(lists.pick(list, count, ThreadLocalRandom.current())));
    }

}
