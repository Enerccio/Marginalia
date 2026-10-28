package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.write;

import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.AIGate;
import com.github.enerccio.marginalia.domain.service.AnnotateService;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.command.CommandScope;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.utils.QuoteFinder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

/**
 * {@code /create-annotation part text [quote [type]]}, {@code /update-annotation annotation text [type [sendToPrompt]]},
 * {@code /resolve-annotation annotation [status]}, {@code /reattach-annotation annotation [quote]},
 * {@code /delete-annotation annotation} and {@code /generate-annotation part [profile]}: the notes on the passages of the story.
 * A part and an annotation are given by their handles, see {@link ManuscriptWriteSupport}.
 * <p>
 * {@code generate-annotation} has the model review the part like the <i>Annotate with AI</i> dialog does (see {@link AnnotateService})
 * and, as there is nobody to pick the proposals, adds all of them as notes. It reviews with the model of the scope like the Lore Generator
 * ({@link CommandScope#getModel}: for an agent its own model when {@code Agent.useAgentModel} is on, else the model of the book; the
 * model of the book at the command bar); {@code profile} picks another model profile of the agent for one call. The command is {@code async}, slow and costs tokens.
 * <p>
 * An annotation is a note for the author and changes nothing of the story, so none of these commands asks (but the delete) and none
 * makes a summary outdated. An agent writes them in its working copy; the review takes them over like the other changes of the run
 * ({@code MergeDiff}).
 */
public class AnnotationWriteCommands extends ManuscriptWriteSupport {

    static final String CREATE = "create-annotation";
    static final String UPDATE = "update-annotation";
    static final String RESOLVE = "resolve-annotation";
    static final String REATTACH = "reattach-annotation";
    static final String DELETE = "delete-annotation";
    static final String GENERATE = "generate-annotation";

    @Autowired
    private AnnotateService annotateService;

    @Command(commandName = GENERATE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_GENERATE_ANNOTATION, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_GENERATE_ANNOTATION, llmDescription = L.DESC_COMMAND_GENERATE_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GENERATE_ANNOTATION_RETURN))
    public void generate(CommandContext context) throws Exception {
        generate(context, ref(context, "part"), null);
    }

    @Command(commandName = GENERATE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_GENERATE_ANNOTATION, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_GENERATE_ANNOTATION, llmDescription = L.DESC_COMMAND_GENERATE_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @CommandParameter(name = "profile", type = String.class, description = L.DESC_COMMAND_GEN_PROFILE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GENERATE_ANNOTATION_RETURN))
    public void generateProfile(CommandContext context) throws Exception {
        generate(context, ref(context, "part"), ref(context, "profile"));
    }

    @Command(commandName = CREATE, humanName = L.LABEL_COMMAND_CREATE_ANNOTATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_CREATE_ANNOTATION, llmDescription = L.DESC_COMMAND_CREATE_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @CommandParameter(name = "text", type = String.class, description = L.DESC_COMMAND_ANNOTATION_TEXT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_CREATE_ANNOTATION_RETURN))
    public void create(CommandContext context) throws Exception {
        create(context, ref(context, "part"), context.get("text", String.class), null, null);
    }

    @Command(commandName = CREATE, humanName = L.LABEL_COMMAND_CREATE_ANNOTATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
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
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
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
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_UPDATE_ANNOTATION, llmDescription = L.DESC_COMMAND_UPDATE_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "annotation", type = String.class, description = L.DESC_COMMAND_ANNOTATION_PARAM),
                    @CommandParameter(name = "text", type = String.class, description = L.DESC_COMMAND_ANNOTATION_TEXT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_UPDATE_ANNOTATION_RETURN))
    public void update(CommandContext context) throws Exception {
        update(context, ref(context, "annotation"), context.get("text", String.class), null, null);
    }

    @Command(commandName = UPDATE, humanName = L.LABEL_COMMAND_UPDATE_ANNOTATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
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
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
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
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_RESOLVE_ANNOTATION, llmDescription = L.DESC_COMMAND_RESOLVE_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "annotation", type = String.class, description = L.DESC_COMMAND_ANNOTATION_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_RESOLVE_ANNOTATION_RETURN))
    public void resolve(CommandContext context) throws Exception {
        resolve(context, ref(context, "annotation"), null);
    }

    @Command(commandName = RESOLVE, humanName = L.LABEL_COMMAND_RESOLVE_ANNOTATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_RESOLVE_ANNOTATION, llmDescription = L.DESC_COMMAND_RESOLVE_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "annotation", type = String.class, description = L.DESC_COMMAND_ANNOTATION_PARAM),
                    @CommandParameter(name = "status", type = String.class, optional = true, defaultValue = "resolved", description = L.DESC_COMMAND_ANNOTATION_STATUS_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_RESOLVE_ANNOTATION_RETURN))
    public void resolveStatus(CommandContext context) throws Exception {
        resolve(context, ref(context, "annotation"), ref(context, "status"));
    }

    @Command(commandName = REATTACH, humanName = L.LABEL_COMMAND_REATTACH_ANNOTATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_REATTACH_ANNOTATION, llmDescription = L.DESC_COMMAND_REATTACH_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "annotation", type = String.class, description = L.DESC_COMMAND_ANNOTATION_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REATTACH_ANNOTATION_RETURN))
    public void reattachWhole(CommandContext context) throws Exception {
        reattach(context, ref(context, "annotation"), null);
    }

    @Command(commandName = REATTACH, humanName = L.LABEL_COMMAND_REATTACH_ANNOTATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_REATTACH_ANNOTATION, llmDescription = L.DESC_COMMAND_REATTACH_ANNOTATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "annotation", type = String.class, description = L.DESC_COMMAND_ANNOTATION_PARAM),
                    @CommandParameter(name = "quote", type = String.class, optional = true, description = L.DESC_COMMAND_ANNOTATION_QUOTE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REATTACH_ANNOTATION_RETURN))
    public void reattachQuote(CommandContext context) throws Exception {
        reattach(context, ref(context, "annotation"), context.get("quote", String.class));
    }

    @Command(commandName = DELETE, humanName = L.LABEL_COMMAND_DELETE_ANNOTATION,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
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

    private void generate(CommandContext context, String handle, String profile) throws Exception {
        Book book = new Book(context);
        ChatMessage message = part(book, handle);
        if (message.getResponse() == null || message.getResponse().isBlank()) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_ANNOTATE_NOT_POSSIBLE));
        }
        GenerationOptions options = options(context, profile);
        if (options.ai() == null) {
            // as the Lore Generator does: the model the scope works with (an agent's own model when its definition says so, else the
            // model of the book), and without a choice of the scope (the command bar) the model of the book
            CommandScope.ModelChoice scoped = context.getScope().getModel();
            if (scoped != null && scoped.ai() != null) {
                options = options.withModel(scoped.ai(), scoped.protocol());
            }
        }
        AI ai = options.ai() != null ? options.ai() : book.manuscript.getAi();

        CompletableFuture<List<AnnotateService.Proposal>> done = new CompletableFuture<>();
        AnnotateService.Callback callback = new AnnotateService.Callback() {
            @Override
            public void onProgress(String reasoning, String text) {
                context.setSubStatus(text.length() + "");
            }

            @Override
            public void onFinished(List<AnnotateService.Proposal> proposals, String raw) {
                done.complete(proposals);
            }

            @Override
            public void onUnreadable(String raw, String reason) {
                done.completeExceptionally(new IllegalStateException(String.format(loc.getValue(L.ERROR_COMMAND_ANNOTATE_UNREADABLE), reason)));
            }

            @Override
            public void onTerminated(String raw) {
                done.completeExceptionally(new CancellationException(loc.getValue(L.ERROR_COMMAND_GENERATION_CANCELLED)));
            }

            @Override
            public void onError(Throwable throwable) {
                done.completeExceptionally(throwable);
            }
        };
        context.setStatus(loc.getValue(L.MSG_COMMAND_ANNOTATING));
        List<AnnotateService.Proposal> proposals;
        try (AIGate.Permit _ = placeAt(context, ai)) {
            CancellationToken token;
            try {
                token = annotateService.annotate(book.manuscript, message,
                        new AnnotateService.Request(null, null, options, AnnotateService.Mode.AUTO, null, null), callback);
            } catch (AnnotateService.ContextInsufficient e) {
                throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_COMMAND_ANNOTATE_CONTEXT), e.getRequired(), e.getMax()));
            }
            if (token == null) {
                throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_ANNOTATE_NOT_POSSIBLE));
            }
            proposals = await(context, done, token);
        }
        if (proposals == null) {
            return;
        }

        // the book as it is now: the story may have changed while the model reviewed
        book = new Book(context);
        ChatMessage current = part(book, handle);
        List<Annotation> made = annotateService.accept(book.manuscript, current, proposals, ai == null ? null : ai.getName());
        JsonArray notes = new JsonArray();
        for (Annotation annotation : made) {
            notes.add(describe(annotation, book));
        }
        JsonObject result = new JsonObject();
        result.addProperty("part", current.getUuid());
        result.addProperty("created", made.size());
        result.add("annotations", notes);
        if (!made.isEmpty()) {
            changed();
        }
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
