package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.collections.SummaryType;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * {@code /get-summary part [branch]}: the summary of a part.
 * Changes nothing; the answer is JSON text, see {@link ManuscriptReadSupport}.
 */
public class GetSummaryCommand extends ManuscriptReadSupport {

    static final String NAME = "get-summary";

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_GET_SUMMARY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_GET_SUMMARY, llmDescription = L.DESC_COMMAND_GET_SUMMARY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = Integer.class, description = L.DESC_COMMAND_PART_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_SUMMARY_RETURN))
    public void getSummary(CommandContext context) throws Exception {
        getSummary(context, context.get("part", Integer.class), null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_GET_SUMMARY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_GET_SUMMARY, llmDescription = L.DESC_COMMAND_GET_SUMMARY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = Integer.class, description = L.DESC_COMMAND_PART_PARAM),
                    @CommandParameter(name = "branch", type = Integer.class, description = L.DESC_COMMAND_BRANCH_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_SUMMARY_RETURN))
    public void getSummaryBranch(CommandContext context) throws Exception {
        getSummary(context, context.get("part", Integer.class), context.get("branch", Integer.class));
    }

    private void getSummary(CommandContext context, Integer position, Integer branch) throws Exception {
        Book book = new Book(context);
        List<ChatMessage> path = book.path(branch);
        ChatMessage message = book.part(path, position);
        JsonObject result = new JsonObject();
        result.addProperty("position", position);
        Summary summary = message.getSummary();
        result.addProperty("hasSummary", summary != null);
        if (summary != null) {
            result.addProperty("type", summary.getSummaryType() == SummaryType.META_SUMMARY ? "meta" : "summary");
            result.addProperty("tokens", summary.getSummaryTokens());
            result.addProperty("valid", Boolean.TRUE.equals(validSummaries(path).get(message.getId())));
            slice(result, summary.getSummary(), 0, MAX_SLICE);
        }
        answer(context, result);
    }
}
