package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.collections.SummaryType;
import com.github.enerccio.marginalia.domain.components.SummaryNode;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@code /list-summaries [branch]}: the summaries of a branch, which parts they cover and whether they are still valid.
 * Changes nothing; the answer is JSON text, see {@link ManuscriptReadSupport}.
 */
public class ListSummariesCommand extends ManuscriptReadSupport {

    static final String NAME = "list-summaries";

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_LIST_SUMMARIES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_LIST_SUMMARIES, llmDescription = L.DESC_COMMAND_LIST_SUMMARIES_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_LIST_SUMMARIES_RETURN))
    public void listSummaries(CommandContext context) throws Exception {
        listSummaries(context, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_LIST_SUMMARIES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_LIST_SUMMARIES, llmDescription = L.DESC_COMMAND_LIST_SUMMARIES_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "branch", type = String.class, description = L.DESC_COMMAND_BRANCH_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_LIST_SUMMARIES_RETURN))
    public void listSummariesBranch(CommandContext context) throws Exception {
        listSummaries(context, ref(context, "branch"));
    }

    private void listSummaries(CommandContext context, String branch) throws Exception {
        Book book = new Book(context);
        List<ChatMessage> path = book.path(branch);
        Map<Long, Boolean> valid = validSummaries(path);
        List<ChatMessage> newestFirst = new ArrayList<>(path);
        Collections.reverse(newestFirst);

        JsonArray blocks = new JsonArray();
        int previous = 0;
        for (SummaryNode node : summaryService.collectTree(newestFirst)) {
            JsonObject row = new JsonObject();
            row.addProperty("handle", node.getMessage().getUuid());
            row.addProperty("position", node.getOrder());
            row.addProperty("covers", (previous + 1) + "-" + node.getOrder());
            row.addProperty("type", node.getSummary().getSummaryType() == SummaryType.META_SUMMARY ? "meta" : "summary");
            row.addProperty("tokens", node.getTokens());
            row.addProperty("valid", Boolean.TRUE.equals(valid.get(node.getMessage().getId())));
            row.addProperty("replaces", node.getChildren().size());
            blocks.add(row);
            previous = node.getOrder();
        }
        JsonObject result = new JsonObject();
        result.addProperty("parts", path.size());
        result.add("summaries", blocks);
        // the parts after the last summary go into the prompt as they are
        result.addProperty("partsWithoutSummary", path.size() - previous);
        answer(context, result);
    }
}
