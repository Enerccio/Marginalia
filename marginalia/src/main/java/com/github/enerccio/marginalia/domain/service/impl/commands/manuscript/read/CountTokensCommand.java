package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer.JavaTokkitStrategy;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonObject;

/**
 * {@code /count-tokens text} (the text can be the handle of a part), {@code /count-tokens part branch}: tokens (with the tokenizer of the book's model), words and characters of a text or of a part.
 * Changes nothing; the answer is JSON text, see {@link ManuscriptReadSupport}.
 */
public class CountTokensCommand extends ManuscriptReadSupport {

    static final String NAME = "count-tokens";

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_COUNT_TOKENS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_COUNT_TOKENS, llmDescription = L.DESC_COMMAND_COUNT_TOKENS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "text", type = String.class, description = L.DESC_COMMAND_TEXT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_COUNT_TOKENS_RETURN))
    public void countText(CommandContext context) throws Exception {
        countText(context, context.get("text", String.class));
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_COUNT_TOKENS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_COUNT_TOKENS, llmDescription = L.DESC_COMMAND_COUNT_TOKENS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_PARAM),
                    @CommandParameter(name = "branch", type = String.class, description = L.DESC_COMMAND_BRANCH_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_COUNT_TOKENS_RETURN))
    public void countPartBranch(CommandContext context) throws Exception {
        countPart(context, ref(context, "part"), ref(context, "branch"));
    }

    private void countText(CommandContext context, String text) throws Exception {
        Book book = new Book(context);
        if (isHandle(text)) {
            // the handle of a part counts the part
            countPart(context, text, null);
            return;
        }
        countTokens(context, book.manuscript, text, null);
    }

    private void countPart(CommandContext context, String position, String branch) throws Exception {
        Book book = new Book(context);
        Book.Located at = book.locate(position, branch);
        countTokens(context, book.manuscript, at.message().getResponse(), at);
    }

    private void countTokens(CommandContext context, Manuscript manuscript, String text, Book.Located at) throws Exception {
        String content = text == null ? "" : text;
        AI ai = manuscript.getAi();
        InferenceService inferenceService = ai == null ? null : inferenceServices.forAI(ai);
        JsonObject result = new JsonObject();
        if (at != null) {
            result.addProperty("handle", at.message().getUuid());
            result.addProperty("position", at.position());
        }
        result.addProperty("tokens", inferenceService == null
                ? new JavaTokkitStrategy().countTokens(null, content) : inferenceService.countTokens(content));
        result.addProperty("tokenizer", inferenceService == null ? "generic" : ai.getName());
        result.addProperty("words", chatMessageService.countWords(content));
        result.addProperty("characters", content.length());
        answer(context, result);
    }
}
