package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonObject;

/**
 * {@code /show-prompt part [branch [offset [length]]]}: the prompt a generated part was made from, as it was stored.
 * Changes nothing; the answer is JSON text, see {@link ManuscriptReadSupport}.
 */
public class ShowPromptCommand extends ManuscriptReadSupport {

    static final String NAME = "show-prompt";

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SHOW_PROMPT,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SHOW_PROMPT, llmDescription = L.DESC_COMMAND_SHOW_PROMPT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = Integer.class, description = L.DESC_COMMAND_PART_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SHOW_PROMPT_RETURN))
    public void showPrompt(CommandContext context) throws Exception {
        showPrompt(context, context.get("part", Integer.class), null, null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SHOW_PROMPT,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SHOW_PROMPT, llmDescription = L.DESC_COMMAND_SHOW_PROMPT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = Integer.class, description = L.DESC_COMMAND_PART_PARAM),
                    @CommandParameter(name = "branch", type = Integer.class, description = L.DESC_COMMAND_BRANCH_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SHOW_PROMPT_RETURN))
    public void showPromptBranch(CommandContext context) throws Exception {
        showPrompt(context, context.get("part", Integer.class), context.get("branch", Integer.class), null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SHOW_PROMPT,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SHOW_PROMPT, llmDescription = L.DESC_COMMAND_SHOW_PROMPT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = Integer.class, description = L.DESC_COMMAND_PART_PARAM),
                    @CommandParameter(name = "branch", type = Integer.class, description = L.DESC_COMMAND_BRANCH_PARAM),
                    @CommandParameter(name = "offset", type = Integer.class, description = L.DESC_COMMAND_OFFSET_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SHOW_PROMPT_RETURN))
    public void showPromptOffset(CommandContext context) throws Exception {
        showPrompt(context, context.get("part", Integer.class), context.get("branch", Integer.class), context.get("offset", Integer.class), null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SHOW_PROMPT,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SHOW_PROMPT, llmDescription = L.DESC_COMMAND_SHOW_PROMPT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = Integer.class, description = L.DESC_COMMAND_PART_PARAM),
                    @CommandParameter(name = "branch", type = Integer.class, description = L.DESC_COMMAND_BRANCH_PARAM),
                    @CommandParameter(name = "offset", type = Integer.class, description = L.DESC_COMMAND_OFFSET_PARAM),
                    @CommandParameter(name = "length", type = Integer.class, description = L.DESC_COMMAND_LENGTH_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SHOW_PROMPT_RETURN))
    public void showPromptLength(CommandContext context) throws Exception {
        showPrompt(context, context.get("part", Integer.class), context.get("branch", Integer.class), context.get("offset", Integer.class), context.get("length", Integer.class));
    }

    private void showPrompt(CommandContext context, Integer position, Integer branch, Integer offset, Integer length)
            throws Exception {
        Book book = new Book(context);
        ChatMessage message = book.part(book.path(branch), position);
        JsonObject result = new JsonObject();
        result.addProperty("position", position);
        result.addProperty("available", message.getBuiltPrompt() != null);
        if (message.getBuiltPrompt() != null) {
            result.addProperty("promptTokens", message.getBuiltPromptTokens());
            slice(result, message.getBuiltPrompt(), offset, length);
            if (message.getTokenBreakdown() != null) {
                result.add("tokenBreakdown", GSON.toJsonTree(message.getTokenBreakdown()));
            }
            String lore = message.getBackgroundLore();
            if (lore != null) {
                JsonObject backgroundLore = new JsonObject();
                slice(backgroundLore, lore, 0, DEFAULT_SLICE);
                result.add("backgroundLore", backgroundLore);
            }
        }
        answer(context, result);
    }
}
