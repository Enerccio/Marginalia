package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * {@code /get-part-metrics part [branch]}: tokens, timings, model and protocol of a part.
 * Changes nothing; the answer is JSON text, see {@link ManuscriptReadSupport}.
 */
public class GetPartMetricsCommand extends ManuscriptReadSupport {

    static final String NAME = "get-part-metrics";

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_GET_PART_METRICS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_GET_PART_METRICS, llmDescription = L.DESC_COMMAND_GET_PART_METRICS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = Integer.class, description = L.DESC_COMMAND_PART_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_PART_METRICS_RETURN))
    public void getPartMetrics(CommandContext context) throws Exception {
        partMetrics(context, context.get("part", Integer.class), null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_GET_PART_METRICS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_GET_PART_METRICS, llmDescription = L.DESC_COMMAND_GET_PART_METRICS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = Integer.class, description = L.DESC_COMMAND_PART_PARAM),
                    @CommandParameter(name = "branch", type = Integer.class, description = L.DESC_COMMAND_BRANCH_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_PART_METRICS_RETURN))
    public void getPartMetricsBranch(CommandContext context) throws Exception {
        partMetrics(context, context.get("part", Integer.class), context.get("branch", Integer.class));
    }

    private void partMetrics(CommandContext context, Integer position, Integer branch) throws Exception {
        Book book = new Book();
        List<ChatMessage> path = book.path(branch);
        ChatMessage message = book.part(path, position);
        JsonObject result = new JsonObject();
        result.addProperty("position", position);
        result.addProperty("generated", message.getModelUsed() != null || message.getOriginalResponse() != null);
        result.addProperty("tokens", message.getTokenCount());
        result.addProperty("words", message.getWordCount());
        result.addProperty("promptTokens", message.getPromptTokens());
        result.addProperty("reasoningTokens", message.getTokenReasoningCount());
        result.addProperty("builtPromptTokens", message.getBuiltPromptTokens());
        result.addProperty("model", message.getModelUsed());
        result.addProperty("protocol", message.getProtocolUsed());
        result.addProperty("requested", message.getRequest() == null ? null : message.getRequest().toInstant().toString());
        if (message.getRequest() != null && message.getTtft() != null) {
            result.addProperty("millisToFirstToken", message.getTtft().getTime() - message.getRequest().getTime());
        }
        if (message.getTtft() != null && message.getReasoningEnd() != null) {
            result.addProperty("millisOfReasoning", message.getReasoningEnd().getTime() - message.getTtft().getTime());
        }
        result.addProperty("edited", message.isEdited());
        answer(context, result);
    }
}
