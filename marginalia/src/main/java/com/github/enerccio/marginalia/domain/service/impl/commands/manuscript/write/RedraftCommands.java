package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.write;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions;
import com.github.enerccio.marginalia.domain.templates.RedraftTemplateData;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

/**
 * {@code /redraft-part part [instructions [prompt [profile [apply]]]]}: has the model write a part of the story again (see
 * {@link RedraftService}) and, unless {@code apply} is false, replaces the text of the part. The part is given by its handle,
 * see {@link ManuscriptWriteSupport}. It writes with the model of the book as the commands see it: for an agent the working
 * copy has the model of the agent or the one of the book, as the agent says ({@code Agent.useAgentModel}); {@code profile}
 * picks another model profile of the agent for one call. The command is {@code async}, slow and costs tokens.
 */
public class RedraftCommands extends ManuscriptWriteSupport {

    static final String REDRAFT_PART = "redraft-part";

    @Autowired
    private RedraftService redraftService;

    @Autowired
    private TemplateService templateService;

    @Command(commandName = REDRAFT_PART, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_REDRAFT_PART, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_REDRAFT_PART, llmDescription = L.DESC_COMMAND_REDRAFT_PART_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REDRAFT_PART_RETURN))
    public void redraft(CommandContext context) throws Exception {
        redraft(context, ref(context, "part"), null, null, null, true);
    }

    @Command(commandName = REDRAFT_PART, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_REDRAFT_PART, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_REDRAFT_PART, llmDescription = L.DESC_COMMAND_REDRAFT_PART_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_REDRAFT_INSTRUCTIONS_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REDRAFT_PART_RETURN))
    public void redraftInstructions(CommandContext context) throws Exception {
        redraft(context, ref(context, "part"), context.get("instructions", String.class), null, null, true);
    }

    @Command(commandName = REDRAFT_PART, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_REDRAFT_PART, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_REDRAFT_PART, llmDescription = L.DESC_COMMAND_REDRAFT_PART_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_REDRAFT_INSTRUCTIONS_PARAM),
                    @CommandParameter(name = "prompt", type = String.class, description = L.DESC_COMMAND_REDRAFT_PROMPT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REDRAFT_PART_RETURN))
    public void redraftPrompt(CommandContext context) throws Exception {
        redraft(context, ref(context, "part"), context.get("instructions", String.class), context.get("prompt", String.class), null, true);
    }

    @Command(commandName = REDRAFT_PART, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_REDRAFT_PART, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_REDRAFT_PART, llmDescription = L.DESC_COMMAND_REDRAFT_PART_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_REDRAFT_INSTRUCTIONS_PARAM),
                    @CommandParameter(name = "prompt", type = String.class, description = L.DESC_COMMAND_REDRAFT_PROMPT_PARAM),
                    @CommandParameter(name = "profile", type = String.class, description = L.DESC_COMMAND_GEN_PROFILE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REDRAFT_PART_RETURN))
    public void redraftProfile(CommandContext context) throws Exception {
        redraft(context, ref(context, "part"), context.get("instructions", String.class), context.get("prompt", String.class),
                ref(context, "profile"), true);
    }

    @Command(commandName = REDRAFT_PART, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_REDRAFT_PART, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_REDRAFT_PART, llmDescription = L.DESC_COMMAND_REDRAFT_PART_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_REDRAFT_INSTRUCTIONS_PARAM),
                    @CommandParameter(name = "prompt", type = String.class, description = L.DESC_COMMAND_REDRAFT_PROMPT_PARAM),
                    @CommandParameter(name = "profile", type = String.class, description = L.DESC_COMMAND_GEN_PROFILE_PARAM),
                    @CommandParameter(name = "apply", type = Boolean.class, description = L.DESC_COMMAND_REDRAFT_APPLY_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REDRAFT_PART_RETURN))
    public void redraftApply(CommandContext context) throws Exception {
        Boolean apply = context.get("apply", Boolean.class);
        redraft(context, ref(context, "part"), context.get("instructions", String.class), context.get("prompt", String.class),
                ref(context, "profile"), apply == null || apply);
    }

    private void redraft(CommandContext context, String handle, String instructions, String prompt, String profile, boolean apply)
            throws Exception {
        Book book = new Book(context);
        ChatMessage message = part(book, handle);
        if (StringUtils.isBlank(message.getResponse())) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_REDRAFT_NOT_POSSIBLE));
        }
        if (StringUtils.isNotBlank(prompt)) {
            var checked = templateService.isValidTemplate(prompt, "redraftPrompt", RedraftTemplateData.class);
            if (!checked.isValid()) {
                throw new IllegalArgumentException(checked.errorMessage());
            }
        }
        GenerationOptions options = options(context, profile);
        if (apply && !confirmed(context, String.format(loc.getValue(L.MSG_COMMAND_REDRAFT_CONFIRM), book.locate(message.getUuid(), null).position()))) {
            return;
        }

        CompletableFuture<String> done = new CompletableFuture<>();
        RedraftService.Callback callback = new RedraftService.Callback() {
            @Override
            public void onProgress(String reasoning, String text) {
                context.setSubStatus(text.length() + "");
            }

            @Override
            public void onFinished(String reasoning, String text) {
                done.complete(text);
            }

            @Override
            public void onTerminated(String reasoning, String text) {
                done.completeExceptionally(new CancellationException(loc.getValue(L.ERROR_COMMAND_GENERATION_CANCELLED)));
            }

            @Override
            public void onError(Throwable throwable) {
                done.completeExceptionally(throwable);
            }
        };
        context.setStatus(loc.getValue(L.MSG_COMMAND_REDRAFTING));
        String text;
        try (AIGate.Permit _ = placeAt(context, options.ai() != null ? options.ai() : book.manuscript.getAi())) {
            CancellationToken token;
            try {
                token = redraftService.redraft(book.manuscript, message, instructions, prompt, options, callback);
            } catch (RedraftService.ContextInsufficient e) {
                throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_COMMAND_REDRAFT_CONTEXT), e.getRequired(), e.getMax()));
            }
            if (token == null) {
                throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_REDRAFT_NOT_POSSIBLE));
            }
            text = await(context, done, token);
        }
        if (text == null) {
            return;
        }
        if (StringUtils.isBlank(text)) {
            throw new IllegalStateException(loc.getValue(L.ERROR_COMMAND_REDRAFT_NOT_POSSIBLE));
        }

        // the book as it is now: the story may have changed while the model wrote
        book = new Book(context);
        Book.Located at = book.locate(message.getUuid(), null);
        JsonObject result;
        if (apply) {
            ChatMessageService.PartEdit edit = chatMessageService.updateResponse(book.manuscript, message, text);
            result = describe(edit.message(), at.position());
            reportOutdated(result, at.path(), edit.outdated());
            result.addProperty("text", preview(text, 300));
            changed();
        } else {
            result = describe(message, at.position());
            result.addProperty("text", text);
        }
        result.addProperty("applied", apply);
        answer(context, result);
    }
}
