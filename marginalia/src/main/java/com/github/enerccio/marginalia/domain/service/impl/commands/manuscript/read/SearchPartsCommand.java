package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.search.FulltextHit;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * {@code /search-parts query [limit]}: the full-text search over the parts of all branches, with snippets.
 * Changes nothing; the answer is JSON text, see {@link ManuscriptReadSupport}.
 */
public class SearchPartsCommand extends ManuscriptReadSupport {

    static final String NAME = "search-parts";

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SEARCH_PARTS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SEARCH_PARTS, llmDescription = L.DESC_COMMAND_SEARCH_PARTS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "query", type = String.class, description = L.DESC_COMMAND_QUERY_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SEARCH_PARTS_RETURN))
    public void searchParts(CommandContext context) throws Exception {
        searchParts(context, context.get("query", String.class), null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SEARCH_PARTS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SEARCH_PARTS, llmDescription = L.DESC_COMMAND_SEARCH_PARTS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "query", type = String.class, description = L.DESC_COMMAND_QUERY_PARAM),
                    @CommandParameter(name = "limit", type = Integer.class, description = L.DESC_COMMAND_LIMIT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SEARCH_PARTS_RETURN))
    public void searchPartsLimit(CommandContext context) throws Exception {
        searchParts(context, context.get("query", String.class), context.get("limit", Integer.class));
    }

    private void searchParts(CommandContext context, String query, Integer limit) throws Exception {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_SEARCH_TEXT_EMPTY));
        }
        Book book = new Book(context);
        List<FulltextHit> found = chatMessageService.searchFulltext(book.manuscript, query);
        int max = limit(limit);
        StoryTree tree = book.tree();
        JsonArray hits = new JsonArray();
        for (FulltextHit hit : found) {
            ChatMessage message = tree.byId.get(hit.id());
            if (message == null) {
                continue;
            }
            if (hits.size() >= max) {
                break;
            }
            JsonObject row = new JsonObject();
            row.addProperty("handle", message.getUuid());
            row.addProperty("position", tree.depth(message));
            if (!tree.activeIds.contains(message.getId())) {
                row.addProperty("branch", tree.branchOf(message));
            }
            row.addProperty("snippet", hit.snippet());
            hits.add(row);
        }
        JsonObject result = new JsonObject();
        result.addProperty("found", found.size());
        result.add("hits", hits);
        answer(context, result);
    }
}
