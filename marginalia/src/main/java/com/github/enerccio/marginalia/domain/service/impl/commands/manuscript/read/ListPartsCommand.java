package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * {@code /list-parts [branch [offset [limit]]]}: the parts of a branch with their position, size, swipes, summary state and a preview.
 * Changes nothing; the answer is JSON text, see {@link ManuscriptReadSupport}.
 */
public class ListPartsCommand extends ManuscriptReadSupport {

    static final String NAME = "list-parts";

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_LIST_PARTS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_LIST_PARTS, llmDescription = L.DESC_COMMAND_LIST_PARTS_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_LIST_PARTS_RETURN))
    public void listParts(CommandContext context) throws Exception {
        listParts(context, null, null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_LIST_PARTS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_LIST_PARTS, llmDescription = L.DESC_COMMAND_LIST_PARTS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "branch", type = String.class, description = L.DESC_COMMAND_BRANCH_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_LIST_PARTS_RETURN))
    public void listPartsBranch(CommandContext context) throws Exception {
        listParts(context, ref(context, "branch"), null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_LIST_PARTS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_LIST_PARTS, llmDescription = L.DESC_COMMAND_LIST_PARTS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "branch", type = String.class, description = L.DESC_COMMAND_BRANCH_PARAM),
                    @CommandParameter(name = "offset", type = Integer.class, description = L.DESC_COMMAND_OFFSET_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_LIST_PARTS_RETURN))
    public void listPartsOffset(CommandContext context) throws Exception {
        listParts(context, ref(context, "branch"), context.get("offset", Integer.class), null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_LIST_PARTS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_LIST_PARTS, llmDescription = L.DESC_COMMAND_LIST_PARTS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "branch", type = String.class, description = L.DESC_COMMAND_BRANCH_PARAM),
                    @CommandParameter(name = "offset", type = Integer.class, description = L.DESC_COMMAND_OFFSET_PARAM),
                    @CommandParameter(name = "limit", type = Integer.class, description = L.DESC_COMMAND_LIMIT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_LIST_PARTS_RETURN))
    public void listPartsLimit(CommandContext context) throws Exception {
        listParts(context, ref(context, "branch"), context.get("offset", Integer.class), context.get("limit", Integer.class));
    }

    private void listParts(CommandContext context, String branch, Integer offset, Integer limit) throws Exception {
        Book book = new Book(context);
        List<ChatMessage> path = book.path(branch);
        int from = Math.min(Math.max(offset == null ? 0 : offset, 0), path.size());
        int to = Math.min(path.size(), from + limit(limit));

        Map<Long, Boolean> validSummaries = validSummaries(path);
        JsonArray parts = new JsonArray();
        for (int i = from; i < to; i++) {
            ChatMessage message = path.get(i);
            JsonObject part = new JsonObject();
            part.addProperty("handle", message.getUuid());
            part.addProperty("position", i + 1);
            part.addProperty("swipes", chatMessageService.getSwipesForMessage(message).size());
            part.addProperty("words", message.getWordCount());
            part.addProperty("tokens", message.getTokenCount());
            part.addProperty("edited", message.isEdited());
            part.addProperty("summary", summaryState(message, validSummaries));
            part.addProperty("preview", preview(message.getResponse(), PREVIEW_LENGTH));
            parts.add(part);
        }
        JsonObject result = new JsonObject();
        result.addProperty("branch", branch == null ? "active" : branch.toString());
        result.addProperty("total", path.size());
        result.addProperty("offset", from);
        result.add("parts", parts);
        answer(context, result);
    }
}
