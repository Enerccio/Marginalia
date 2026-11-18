package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.write;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.M_L;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * {@code /set-summary part text}, {@code /remove-summary part [unwind]}, {@code /revalidate-summaries [part]}: the commands that change
 * the summaries of the story. A part is given by its handle, see {@link ManuscriptWriteSupport}.
 */
public class SummaryWriteCommands extends ManuscriptWriteSupport {

    static final String SET_SUMMARY = "set-summary";
    static final String REMOVE_SUMMARY = "remove-summary";
    static final String REVALIDATE_SUMMARIES = "revalidate-summaries";

    @Command(commandName = SET_SUMMARY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class),
                    @CommandParameter(name = "text", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_SET_SUMMARY,
            description = M_L.DESC_COMMAND_SET_SUMMARY,
            llmDescription = M_L.DESC_COMMAND_SET_SUMMARY_LLM,
            parameters = {@MarginaliaParameterText(name = "part", description = M_L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @MarginaliaParameterText(name = "text", description = M_L.DESC_COMMAND_SUMMARY_TEXT_PARAM)},
            returns = M_L.DESC_COMMAND_SET_SUMMARY_RETURN)
    public void setSummary(CommandContext context) throws Exception {
        setSummary(context, ref(context, "part"), context.get("text", String.class));
    }

    @Command(commandName = REMOVE_SUMMARY,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_REMOVE_SUMMARY,
            description = M_L.DESC_COMMAND_REMOVE_SUMMARY,
            llmDescription = M_L.DESC_COMMAND_REMOVE_SUMMARY_LLM,
            parameters = @MarginaliaParameterText(name = "part", description = M_L.DESC_COMMAND_PART_HANDLE_PARAM),
            returns = M_L.DESC_COMMAND_REMOVE_SUMMARY_RETURN)
    public void removeSummary(CommandContext context) throws Exception {
        removeSummary(context, ref(context, "part"), null);
    }

    @Command(commandName = REMOVE_SUMMARY,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class),
                    @CommandParameter(name = "unwind", type = Boolean.class, optional = true, defaultValue = "false")}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_REMOVE_SUMMARY,
            description = M_L.DESC_COMMAND_REMOVE_SUMMARY,
            llmDescription = M_L.DESC_COMMAND_REMOVE_SUMMARY_LLM,
            parameters = {@MarginaliaParameterText(name = "part", description = M_L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @MarginaliaParameterText(name = "unwind", description = M_L.DESC_COMMAND_UNWIND_PARAM)},
            returns = M_L.DESC_COMMAND_REMOVE_SUMMARY_RETURN)
    public void removeSummaryUnwind(CommandContext context) throws Exception {
        removeSummary(context, ref(context, "part"), context.get("unwind", Boolean.class));
    }

    @Command(commandName = REVALIDATE_SUMMARIES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_REVALIDATE_SUMMARIES,
            description = M_L.DESC_COMMAND_REVALIDATE_SUMMARIES,
            llmDescription = M_L.DESC_COMMAND_REVALIDATE_SUMMARIES_LLM,
            returns = M_L.DESC_COMMAND_REVALIDATE_SUMMARIES_RETURN)
    public void revalidateSummaries(CommandContext context) throws Exception {
        revalidateSummaries(context, null);
    }

    @Command(commandName = REVALIDATE_SUMMARIES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_REVALIDATE_SUMMARIES,
            description = M_L.DESC_COMMAND_REVALIDATE_SUMMARIES,
            llmDescription = M_L.DESC_COMMAND_REVALIDATE_SUMMARIES_LLM,
            parameters = @MarginaliaParameterText(name = "part", description = M_L.DESC_COMMAND_REVALIDATE_PART_PARAM),
            returns = M_L.DESC_COMMAND_REVALIDATE_SUMMARIES_RETURN)
    public void revalidateSummariesPart(CommandContext context) throws Exception {
        revalidateSummaries(context, ref(context, "part"));
    }

    private void setSummary(CommandContext context, String handle, String text) throws Exception {
        Book book = new Book(context);
        ChatMessage message = part(book, handle);
        if (message.getSummary() == null) {
            throw new IllegalArgumentException(loc.getValue(M_L.ERROR_COMMAND_NO_SUMMARY));
        }
        Book.Located at = book.locate(handle, null);
        Set<Long> before = outdatedHeads(at.path());
        Summary updated = summaryService.updateSummaryText(book.manuscript, message.getSummary(), text == null ? "" : text);
        JsonObject result = new JsonObject();
        result.addProperty("handle", handle);
        result.addProperty("position", at.position());
        result.addProperty("tokens", updated.getSummaryTokens());
        reportOutdated(result, at.path(), before);
        changed();
        answer(context, result);
    }

    private void removeSummary(CommandContext context, String handle, Boolean unwind) throws Exception {
        Book book = new Book(context);
        ChatMessage message = part(book, handle);
        if (message.getSummary() == null) {
            throw new IllegalArgumentException(loc.getValue(M_L.ERROR_COMMAND_NO_SUMMARY));
        }
        Book.Located at = book.locate(handle, null);
        if (!confirmed(context, String.format(loc.getValue(M_L.MSG_COMMAND_REMOVE_SUMMARY_CONFIRM), at.position()))) {
            return;
        }
        ChatMessage saved = summaryService.removeSummary(message, Boolean.TRUE.equals(unwind));
        JsonObject result = new JsonObject();
        result.addProperty("handle", handle);
        result.addProperty("position", at.position());
        result.addProperty("hasSummary", saved != null && saved.getSummary() != null);
        changed();
        answer(context, result);
    }

    private void revalidateSummaries(CommandContext context, String handle) throws Exception {
        Book book = new Book(context);
        ChatMessage only = handle == null ? null : part(book, handle);
        List<ChatMessage> path = only == null ? book.path((String) null) : book.locate(handle, null).path();
        List<ChatMessage> newestFirst = new ArrayList<>(path);
        Collections.reverse(newestFirst);
        List<SummaryService.SummaryBlock> done = summaryService.revalidate(book.manuscript, newestFirst, only);
        JsonObject result = new JsonObject();
        result.addProperty("revalidated", done.size());
        JsonArray parts = new JsonArray();
        for (SummaryService.SummaryBlock block : done) {
            JsonObject row = new JsonObject();
            row.addProperty("handle", block.head().getUuid());
            row.addProperty("position", path.indexOf(block.head()) + 1);
            parts.add(row);
        }
        result.add("summaries", parts);
        changed();
        answer(context, result);
    }
}
