package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.write;

import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.utils.QuoteFinder;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.Locale;

/**
 * {@code /create-annotation part text [quote [type]]}, {@code /update-annotation annotation text [type [sendToPrompt]]},
 * {@code /resolve-annotation annotation [status]}, {@code /reattach-annotation annotation [quote]} and
 * {@code /delete-annotation annotation}: the notes on the passages of the story. A part and an annotation are given by their
 * handles, see {@link ManuscriptWriteSupport}.
 * <p>
 * An annotation is a note for the author and changes nothing of the story, so none of these commands asks (but the delete) and none
 * makes a summary outdated. Not offered to an agent yet: the working copy of an agent is not merged back with its annotations.
 */
public class AnnotationWriteCommands extends ManuscriptWriteSupport {

    static final String CREATE = "create-annotation";
    static final String UPDATE = "update-annotation";
    static final String RESOLVE = "resolve-annotation";
    static final String REATTACH = "reattach-annotation";
    static final String DELETE = "delete-annotation";

    @Command(commandName = CREATE, humanName = L.LABEL_COMMAND_CREATE_ANNOTATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, availableForAgent = false,
            description = L.DESC_COMMAND_CREATE_ANNOTATION, llmDescription = L.DESC_COMMAND_CREATE_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @CommandParameter(name = "text", type = String.class, description = L.DESC_COMMAND_ANNOTATION_TEXT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_CREATE_ANNOTATION_RETURN))
    public void create(CommandContext context) throws Exception {
        create(context, ref(context, "part"), context.get("text", String.class), null, null);
    }

    @Command(commandName = CREATE, humanName = L.LABEL_COMMAND_CREATE_ANNOTATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, availableForAgent = false,
            description = L.DESC_COMMAND_CREATE_ANNOTATION, llmDescription = L.DESC_COMMAND_CREATE_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @CommandParameter(name = "text", type = String.class, description = L.DESC_COMMAND_ANNOTATION_TEXT_PARAM),
                    @CommandParameter(name = "quote", type = String.class, optional = true, description = L.DESC_COMMAND_ANNOTATION_QUOTE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_CREATE_ANNOTATION_RETURN))
    public void createQuote(CommandContext context) throws Exception {
        create(context, ref(context, "part"), context.get("text", String.class), context.get("quote", String.class), null);
    }

    @Command(commandName = CREATE, humanName = L.LABEL_COMMAND_CREATE_ANNOTATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, availableForAgent = false,
            description = L.DESC_COMMAND_CREATE_ANNOTATION, llmDescription = L.DESC_COMMAND_CREATE_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @CommandParameter(name = "text", type = String.class, description = L.DESC_COMMAND_ANNOTATION_TEXT_PARAM),
                    @CommandParameter(name = "quote", type = String.class, optional = true, description = L.DESC_COMMAND_ANNOTATION_QUOTE_PARAM),
                    @CommandParameter(name = "type", type = String.class, optional = true, description = L.DESC_COMMAND_ANNOTATION_TYPE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_CREATE_ANNOTATION_RETURN))
    public void createType(CommandContext context) throws Exception {
        create(context, ref(context, "part"), context.get("text", String.class), context.get("quote", String.class),
                context.get("type", String.class));
    }

    @Command(commandName = UPDATE, humanName = L.LABEL_COMMAND_UPDATE_ANNOTATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, availableForAgent = false,
            description = L.DESC_COMMAND_UPDATE_ANNOTATION, llmDescription = L.DESC_COMMAND_UPDATE_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "annotation", type = String.class, description = L.DESC_COMMAND_ANNOTATION_PARAM),
                    @CommandParameter(name = "text", type = String.class, description = L.DESC_COMMAND_ANNOTATION_TEXT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_UPDATE_ANNOTATION_RETURN))
    public void update(CommandContext context) throws Exception {
        update(context, ref(context, "annotation"), context.get("text", String.class), null, null);
    }

    @Command(commandName = UPDATE, humanName = L.LABEL_COMMAND_UPDATE_ANNOTATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, availableForAgent = false,
            description = L.DESC_COMMAND_UPDATE_ANNOTATION, llmDescription = L.DESC_COMMAND_UPDATE_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "annotation", type = String.class, description = L.DESC_COMMAND_ANNOTATION_PARAM),
                    @CommandParameter(name = "text", type = String.class, description = L.DESC_COMMAND_ANNOTATION_TEXT_PARAM),
                    @CommandParameter(name = "type", type = String.class, optional = true, description = L.DESC_COMMAND_ANNOTATION_TYPE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_UPDATE_ANNOTATION_RETURN))
    public void updateType(CommandContext context) throws Exception {
        update(context, ref(context, "annotation"), context.get("text", String.class), context.get("type", String.class), null);
    }

    @Command(commandName = UPDATE, humanName = L.LABEL_COMMAND_UPDATE_ANNOTATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, availableForAgent = false,
            description = L.DESC_COMMAND_UPDATE_ANNOTATION, llmDescription = L.DESC_COMMAND_UPDATE_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "annotation", type = String.class, description = L.DESC_COMMAND_ANNOTATION_PARAM),
                    @CommandParameter(name = "text", type = String.class, description = L.DESC_COMMAND_ANNOTATION_TEXT_PARAM),
                    @CommandParameter(name = "type", type = String.class, optional = true, description = L.DESC_COMMAND_ANNOTATION_TYPE_PARAM),
                    @CommandParameter(name = "sendToPrompt", type = Boolean.class, optional = true, description = L.DESC_COMMAND_ANNOTATION_SEND_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_UPDATE_ANNOTATION_RETURN))
    public void updateSend(CommandContext context) throws Exception {
        update(context, ref(context, "annotation"), context.get("text", String.class), context.get("type", String.class),
                context.get("sendToPrompt", Boolean.class));
    }

    @Command(commandName = RESOLVE, humanName = L.LABEL_COMMAND_RESOLVE_ANNOTATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, availableForAgent = false,
            description = L.DESC_COMMAND_RESOLVE_ANNOTATION, llmDescription = L.DESC_COMMAND_RESOLVE_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "annotation", type = String.class, description = L.DESC_COMMAND_ANNOTATION_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_RESOLVE_ANNOTATION_RETURN))
    public void resolve(CommandContext context) throws Exception {
        resolve(context, ref(context, "annotation"), null);
    }

    @Command(commandName = RESOLVE, humanName = L.LABEL_COMMAND_RESOLVE_ANNOTATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, availableForAgent = false,
            description = L.DESC_COMMAND_RESOLVE_ANNOTATION, llmDescription = L.DESC_COMMAND_RESOLVE_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "annotation", type = String.class, description = L.DESC_COMMAND_ANNOTATION_PARAM),
                    @CommandParameter(name = "status", type = String.class, optional = true, defaultValue = "resolved", description = L.DESC_COMMAND_ANNOTATION_STATUS_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_RESOLVE_ANNOTATION_RETURN))
    public void resolveStatus(CommandContext context) throws Exception {
        resolve(context, ref(context, "annotation"), ref(context, "status"));
    }

    @Command(commandName = REATTACH, humanName = L.LABEL_COMMAND_REATTACH_ANNOTATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, availableForAgent = false,
            description = L.DESC_COMMAND_REATTACH_ANNOTATION, llmDescription = L.DESC_COMMAND_REATTACH_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "annotation", type = String.class, description = L.DESC_COMMAND_ANNOTATION_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REATTACH_ANNOTATION_RETURN))
    public void reattachWhole(CommandContext context) throws Exception {
        reattach(context, ref(context, "annotation"), null);
    }

    @Command(commandName = REATTACH, humanName = L.LABEL_COMMAND_REATTACH_ANNOTATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, availableForAgent = false,
            description = L.DESC_COMMAND_REATTACH_ANNOTATION, llmDescription = L.DESC_COMMAND_REATTACH_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "annotation", type = String.class, description = L.DESC_COMMAND_ANNOTATION_PARAM),
                    @CommandParameter(name = "quote", type = String.class, optional = true, description = L.DESC_COMMAND_ANNOTATION_QUOTE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REATTACH_ANNOTATION_RETURN))
    public void reattachQuote(CommandContext context) throws Exception {
        reattach(context, ref(context, "annotation"), context.get("quote", String.class));
    }

    @Command(commandName = DELETE, humanName = L.LABEL_COMMAND_DELETE_ANNOTATION,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE, availableForAgent = false,
            description = L.DESC_COMMAND_DELETE_ANNOTATION, llmDescription = L.DESC_COMMAND_DELETE_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "annotation", type = String.class, description = L.DESC_COMMAND_ANNOTATION_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_DELETE_ANNOTATION_RETURN))
    public void delete(CommandContext context) throws Exception {
        Book book = new Book(context);
        Annotation annotation = book.annotation(ref(context, "annotation"));
        if (!confirmed(context, loc.getValue(L.MSG_COMMAND_DELETE_ANNOTATION_CONFIRM))) {
            return;
        }
        annotationService.remove(book.manuscript, annotation);
        JsonObject result = new JsonObject();
        result.addProperty("handle", annotation.getUuid());
        result.addProperty("deleted", true);
        changed();
        answer(context, result);
    }

    private void create(CommandContext context, String handle, String text, String quote, String type) throws Exception {
        Book book = new Book(context);
        ChatMessage message = part(book, handle);
        AnnotationSource source = context.getScope().isAgent() ? AnnotationSource.AGENT : AnnotationSource.AUTHOR;
        Annotation annotation = quote == null || quote.isBlank()
                ? annotationService.create(book.manuscript, message, null, null, text, source, type, null)
                : annotationService.createFromQuote(book.manuscript, message, quote, null, null, text, source, type, null);
        JsonObject result = describe(annotation, book);
        if (annotation.isNotLocated()) {
            result.addProperty("hint", loc.getValue(L.MSG_COMMAND_ANNOTATION_NOT_LOCATED));
        }
        changed();
        answer(context, result);
    }

    private void update(CommandContext context, String handle, String text, String type, Boolean sendToPrompt) throws Exception {
        Book book = new Book(context);
        Annotation annotation = book.annotation(handle);
        Annotation updated = annotationService.update(book.manuscript, annotation, blankToNull(text), blankToNull(type), sendToPrompt);
        changed();
        answer(context, describe(updated, book));
    }

    private void resolve(CommandContext context, String handle, String status) throws Exception {
        Book book = new Book(context);
        Annotation annotation = book.annotation(handle);
        AnnotationStatus wanted = status == null || status.isBlank() ? AnnotationStatus.RESOLVED : parseStatus(status);
        Annotation updated = annotationService.setStatus(book.manuscript, annotation, wanted);
        changed();
        answer(context, describe(updated, book));
    }

    private void reattach(CommandContext context, String handle, String quote) throws Exception {
        Book book = new Book(context);
        Annotation annotation = book.annotation(handle);
        Annotation updated;
        if (quote == null || quote.isBlank()) {
            updated = annotationService.reattach(book.manuscript, annotation, null, null);
        } else {
            ChatMessage message = chatMessageService.find(annotation.getPartId());
            QuoteFinder.Match match = QuoteFinder.find(message.getResponse(), quote, annotation.getContextBefore(),
                    annotation.getContextAfter(), annotation.hasRange() ? annotation.getStart() : -1);
            if (match == null) {
                throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_QUOTE_NOT_FOUND));
            }
            updated = annotationService.reattach(book.manuscript, annotation, match.start(), match.end());
        }
        changed();
        answer(context, describe(updated, book));
    }

    /**
     * The status a command names; only the ones an author can set.
     */
    private AnnotationStatus parseStatus(String status) {
        for (AnnotationStatus value : AnnotationStatus.values()) {
            if (value != AnnotationStatus.ORPHANED && value.name().equalsIgnoreCase(status.strip())) {
                return value;
            }
        }
        throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_COMMAND_ANNOTATION_STATUS), status,
                Arrays.stream(AnnotationStatus.values()).filter(value -> value != AnnotationStatus.ORPHANED)
                        .map(value -> value.name().toLowerCase(Locale.ROOT)).toList()));
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text;
    }
}
