package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.write;

import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.ImpersonateService;
import com.github.enerccio.marginalia.domain.service.TemplateService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.templates.ImpersonateTemplateData;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

/**
 * {@code /impersonate [instructions [outline [prompt]]]}: has the model draft the instructions of the next turn in the place of the
 * author (see {@link ImpersonateService}) and returns the text; it changes nothing. It is for the user at the command bar: an agent
 * writes the story with its own instructions, so the command is not {@link Command#availableForAgent() available for an agent}. The
 * command is {@code async}, slow and costs tokens.
 */
public class ImpersonateCommands extends ManuscriptWriteSupport {

    static final String IMPERSONATE = "impersonate";

    @Autowired
    private ImpersonateService impersonateService;

    @Autowired
    private TemplateService templateService;

    @Command(commandName = IMPERSONATE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_IMPERSONATE, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, availableForAgent = false, readOnly = true,
            description = L.DESC_COMMAND_IMPERSONATE,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_IMPERSONATE_RETURN))
    public void impersonate(CommandContext context) throws Exception {
        impersonate(context, null, null, null);
    }

    @Command(commandName = IMPERSONATE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_IMPERSONATE, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, availableForAgent = false, readOnly = true,
            description = L.DESC_COMMAND_IMPERSONATE,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_IMPERSONATE_INSTRUCTIONS_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_IMPERSONATE_RETURN))
    public void impersonateInstructions(CommandContext context) throws Exception {
        impersonate(context, context.get("instructions", String.class), null, null);
    }

    @Command(commandName = IMPERSONATE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_IMPERSONATE, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, availableForAgent = false, readOnly = true,
            description = L.DESC_COMMAND_IMPERSONATE,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_IMPERSONATE_INSTRUCTIONS_PARAM),
                    @CommandParameter(name = "outline", type = String.class, description = L.DESC_COMMAND_IMPERSONATE_OUTLINE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_IMPERSONATE_RETURN))
    public void impersonateOutline(CommandContext context) throws Exception {
        impersonate(context, context.get("instructions", String.class), context.get("outline", String.class), null);
    }

    @Command(commandName = IMPERSONATE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_IMPERSONATE, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, availableForAgent = false, readOnly = true,
            description = L.DESC_COMMAND_IMPERSONATE,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_IMPERSONATE_INSTRUCTIONS_PARAM),
                    @CommandParameter(name = "outline", type = String.class, description = L.DESC_COMMAND_IMPERSONATE_OUTLINE_PARAM),
                    @CommandParameter(name = "prompt", type = String.class, description = L.DESC_COMMAND_IMPERSONATE_PROMPT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_IMPERSONATE_RETURN))
    public void impersonatePrompt(CommandContext context) throws Exception {
        impersonate(context, context.get("instructions", String.class), context.get("outline", String.class), context.get("prompt", String.class));
    }

    private void impersonate(CommandContext context, String instructions, String outline, String prompt) throws Exception {
        Book book = new Book(context);
        if (StringUtils.isNotBlank(prompt)) {
            var checked = templateService.isValidTemplate(prompt, "impersonatePrompt", ImpersonateTemplateData.class);
            if (!checked.isValid()) {
                throw new IllegalArgumentException(checked.errorMessage());
            }
        }

        CompletableFuture<String> done = new CompletableFuture<>();
        ImpersonateService.Callback callback = new ImpersonateService.Callback() {
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
        context.setStatus(loc.getValue(L.MSG_COMMAND_IMPERSONATING));
        ImpersonateService.Request request = ImpersonateService.Request.of(instructions).withOutline(outline).withPrompt(prompt);
        CancellationToken token;
        try {
            token = impersonateService.impersonate(book.manuscript, request, callback);
        } catch (ImpersonateService.ContextInsufficient e) {
            throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_COMMAND_IMPERSONATE_CONTEXT), e.getRequired(), e.getMax()));
        }
        if (token == null) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_IMPERSONATE_NOT_POSSIBLE));
        }
        String text = await(context, done, token);
        if (text == null) {
            return;
        }
        if (StringUtils.isBlank(text)) {
            throw new IllegalStateException(loc.getValue(L.ERROR_COMMAND_IMPERSONATE_NOT_POSSIBLE));
        }
        context.setReturn(text);
    }
}
