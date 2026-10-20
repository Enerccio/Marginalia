package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.write;

import com.github.enerccio.marginalia.domain.collections.SummaryType;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.AIGate;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonObject;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

/**
 * {@code /regenerate-summary part}, {@code /create-meta-summary from to}: the commands that have the model make the summaries of the
 * story. They are {@code async}, slow and cost tokens. The parts are given by their handles, see {@link ManuscriptWriteSupport}.
 */
public class SummaryGenerationCommands extends ManuscriptWriteSupport {

    static final String REGENERATE_SUMMARY = "regenerate-summary";
    static final String CREATE_META_SUMMARY = "create-meta-summary";

    @Command(commandName = REGENERATE_SUMMARY, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_REGENERATE_SUMMARY, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_REGENERATE_SUMMARY, llmDescription = L.DESC_COMMAND_REGENERATE_SUMMARY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REGENERATE_SUMMARY_RETURN))
    public void regenerateSummary(CommandContext context) throws Exception {
        regenerateSummary(context, ref(context, "part"));
    }

    @Command(commandName = CREATE_META_SUMMARY, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_CREATE_META_SUMMARY, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_CREATE_META_SUMMARY, llmDescription = L.DESC_COMMAND_CREATE_META_SUMMARY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "from", type = String.class, description = L.DESC_COMMAND_META_FROM_PARAM),
                    @CommandParameter(name = "to", type = String.class, description = L.DESC_COMMAND_META_TO_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_CREATE_META_SUMMARY_RETURN))
    public void createMetaSummary(CommandContext context) throws Exception {
        createMetaSummary(context, ref(context, "from"), ref(context, "to"));
    }

    private void regenerateSummary(CommandContext context, String handle) throws Exception {
        Book book = new Book(context);
        ChatMessage message = part(book, handle);
        summarize(context, book, message, null);
    }

    private void createMetaSummary(CommandContext context, String from, String to) throws Exception {
        Book book = new Book(context);
        summarize(context, book, part(book, from), part(book, to));
    }

    private void summarize(CommandContext context, Book book, ChatMessage head, ChatMessage oldest) throws Exception {
        CompletableFuture<Summary> done = new CompletableFuture<>();
        SummaryService.AsyncCallback callback = new SummaryService.AsyncCallback() {
            @Override
            public void onSummaryProgress(String reasoning, String summary) {
                context.setSubStatus(summary.length() + "");
            }

            @Override
            public void onSummaryFinished(Summary summary) {
                done.complete(summary);
            }

            @Override
            public void onSummaryTerminated() {
                done.completeExceptionally(new CancellationException(loc.getValue(L.ERROR_COMMAND_GENERATION_CANCELLED)));
            }

            @Override
            public void onError(Throwable throwable) {
                done.completeExceptionally(throwable);
            }
        };
        context.setStatus(loc.getValue(L.MSG_COMMAND_SUMMARIZING));
        Summary summary;
        try (AIGate.Permit _ = placeAt(context, book.manuscript.getAi())) {
            CancellationToken token = oldest == null ? summaryService.createSummary(book.manuscript, head, callback)
                    : summaryService.createMetaSummary(book.manuscript, head, oldest, callback);
            if (token == null) {
                throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_SUMMARY_NOT_POSSIBLE));
            }
            summary = await(context, done, token);
        }
        if (summary == null) {
            return;
        }
        Book.Located at = new Book(context).locate(head.getUuid(), null);
        JsonObject result = new JsonObject();
        result.addProperty("handle", head.getUuid());
        result.addProperty("position", at.position());
        result.addProperty("type", summary.getSummaryType() == SummaryType.META_SUMMARY ? "meta" : "summary");
        result.addProperty("tokens", summary.getSummaryTokens());
        result.addProperty("text", preview(summary.getSummary(), 300));
        changed();
        answer(context, result);
    }
}
