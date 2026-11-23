package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.loc.M_L;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables;
import com.github.enerccio.marginalia.domain.traits.*;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * {@code /get-template-variables scope [part [branch]]}: the template variables of the book ({@code global}) or of a part ({@code part}).
 * Changes nothing; the answer is JSON text, see {@link ManuscriptReadSupport}.
 */
public class GetTemplateVariablesCommand extends ManuscriptReadSupport {

    static final String NAME = "get-template-variables";

    @Command(commandName = NAME,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scope", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_GET_TEMPLATE_VARIABLES,
            description = M_L.DESC_COMMAND_GET_TEMPLATE_VARIABLES,
            llmDescription = M_L.DESC_COMMAND_GET_TEMPLATE_VARIABLES_LLM,
            parameters = @MarginaliaParameterText(name = "scope", description = M_L.DESC_COMMAND_SCOPE_PARAM),
            returns = M_L.DESC_COMMAND_GET_TEMPLATE_VARIABLES_RETURN)
    public void getTemplateVariables(CommandContext context) throws Exception {
        templateVariables(context, context.get("scope", String.class), null, null);
    }

    @Command(commandName = NAME,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scope", type = String.class),
                    @CommandParameter(name = "part", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_GET_TEMPLATE_VARIABLES,
            description = M_L.DESC_COMMAND_GET_TEMPLATE_VARIABLES,
            llmDescription = M_L.DESC_COMMAND_GET_TEMPLATE_VARIABLES_LLM,
            parameters = {@MarginaliaParameterText(name = "scope", description = M_L.DESC_COMMAND_SCOPE_PARAM),
                    @MarginaliaParameterText(name = "part", description = M_L.DESC_COMMAND_PART_PARAM)},
            returns = M_L.DESC_COMMAND_GET_TEMPLATE_VARIABLES_RETURN)
    public void getTemplateVariablesPart(CommandContext context) throws Exception {
        templateVariables(context, context.get("scope", String.class), ref(context, "part"), null);
    }

    @Command(commandName = NAME,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scope", type = String.class),
                    @CommandParameter(name = "part", type = String.class),
                    @CommandParameter(name = "branch", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_GET_TEMPLATE_VARIABLES,
            description = M_L.DESC_COMMAND_GET_TEMPLATE_VARIABLES,
            llmDescription = M_L.DESC_COMMAND_GET_TEMPLATE_VARIABLES_LLM,
            parameters = {@MarginaliaParameterText(name = "scope", description = M_L.DESC_COMMAND_SCOPE_PARAM),
                    @MarginaliaParameterText(name = "part", description = M_L.DESC_COMMAND_PART_PARAM),
                    @MarginaliaParameterText(name = "branch", description = M_L.DESC_COMMAND_BRANCH_PARAM)},
            returns = M_L.DESC_COMMAND_GET_TEMPLATE_VARIABLES_RETURN)
    public void getTemplateVariablesBranch(CommandContext context) throws Exception {
        templateVariables(context, context.get("scope", String.class), ref(context, "part"), ref(context, "branch"));
    }

    private void templateVariables(CommandContext context, String scope, String position, String branch) throws Exception {
        String name = scope == null ? "" : scope.strip().toLowerCase();
        Book book = new Book(context);
        TemplateVariables variables = new TemplateVariables();
        JsonObject result = new JsonObject();
        result.addProperty("scope", name);
        switch (name) {
            case "global" -> variables.loadFrom(TemplateVariables.Scope.GLOBAL, book.manuscript.getAttributes());
            case "part" -> {
                // the last part of the branch when none is given
                Book.Located at = book.locate(position == null ? String.valueOf(book.path(branch).size()) : position, branch);
                variables.loadFrom(TemplateVariables.Scope.LOCAL, at.message().getAttributes());
                result.addProperty("handle", at.message().getUuid());
                result.addProperty("position", at.position());
            }
            default -> throw new IllegalArgumentException(String.format(loc.getValue(M_L.ERROR_COMMAND_UNKNOWN_SCOPE), scope));
        }
        result.add("variables", variables.toJson("global".equals(name) ? TemplateVariables.Scope.GLOBAL
                : TemplateVariables.Scope.LOCAL));
        answer(context, result);
    }
}
