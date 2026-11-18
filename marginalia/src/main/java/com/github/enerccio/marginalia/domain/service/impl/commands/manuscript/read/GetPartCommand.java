package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.M_L;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * {@code /get-part part [branch [offset [length [field]]]]}: a slice of the text of a part (or of one of its other fields) and what it was made from.
 * Changes nothing; the answer is JSON text, see {@link ManuscriptReadSupport}.
 */
public class GetPartCommand extends ManuscriptReadSupport {

    static final String NAME = "get-part";

    private static final List<String> FIELDS = List.of("response", "instructions", "scene", "pov", "present", "reasoning");

    @Command(commandName = NAME,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_GET_PART,
            description = M_L.DESC_COMMAND_GET_PART,
            llmDescription = M_L.DESC_COMMAND_GET_PART_LLM,
            parameters = @MarginaliaParameterText(name = "part", description = M_L.DESC_COMMAND_PART_PARAM),
            returns = M_L.DESC_COMMAND_GET_PART_RETURN)
    public void getPart(CommandContext context) throws Exception {
        getPart(context, ref(context, "part"), null, null, null, null);
    }

    @Command(commandName = NAME,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class),
                    @CommandParameter(name = "branch", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_GET_PART,
            description = M_L.DESC_COMMAND_GET_PART,
            llmDescription = M_L.DESC_COMMAND_GET_PART_LLM,
            parameters = {@MarginaliaParameterText(name = "part", description = M_L.DESC_COMMAND_PART_PARAM),
                    @MarginaliaParameterText(name = "branch", description = M_L.DESC_COMMAND_BRANCH_PARAM)},
            returns = M_L.DESC_COMMAND_GET_PART_RETURN)
    public void getPartBranch(CommandContext context) throws Exception {
        getPart(context, ref(context, "part"), ref(context, "branch"), null, null, null);
    }

    @Command(commandName = NAME,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class),
                    @CommandParameter(name = "branch", type = String.class),
                    @CommandParameter(name = "offset", type = Integer.class, optional = true, defaultValue = "0", min = "0")}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_GET_PART,
            description = M_L.DESC_COMMAND_GET_PART,
            llmDescription = M_L.DESC_COMMAND_GET_PART_LLM,
            parameters = {@MarginaliaParameterText(name = "part", description = M_L.DESC_COMMAND_PART_PARAM),
                    @MarginaliaParameterText(name = "branch", description = M_L.DESC_COMMAND_BRANCH_PARAM),
                    @MarginaliaParameterText(name = "offset", description = M_L.DESC_COMMAND_OFFSET_PARAM)},
            returns = M_L.DESC_COMMAND_GET_PART_RETURN)
    public void getPartOffset(CommandContext context) throws Exception {
        getPart(context, ref(context, "part"), ref(context, "branch"), context.get("offset", Integer.class), null, null);
    }

    @Command(commandName = NAME,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class),
                    @CommandParameter(name = "branch", type = String.class),
                    @CommandParameter(name = "offset", type = Integer.class, optional = true, defaultValue = "0", min = "0"),
                    @CommandParameter(name = "length", type = Integer.class, optional = true, defaultValue = "6000", min = "1", max = "20000")}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_GET_PART,
            description = M_L.DESC_COMMAND_GET_PART,
            llmDescription = M_L.DESC_COMMAND_GET_PART_LLM,
            parameters = {@MarginaliaParameterText(name = "part", description = M_L.DESC_COMMAND_PART_PARAM),
                    @MarginaliaParameterText(name = "branch", description = M_L.DESC_COMMAND_BRANCH_PARAM),
                    @MarginaliaParameterText(name = "offset", description = M_L.DESC_COMMAND_OFFSET_PARAM),
                    @MarginaliaParameterText(name = "length", description = M_L.DESC_COMMAND_LENGTH_PARAM)},
            returns = M_L.DESC_COMMAND_GET_PART_RETURN)
    public void getPartLength(CommandContext context) throws Exception {
        getPart(context, ref(context, "part"), ref(context, "branch"), context.get("offset", Integer.class), context.get("length", Integer.class), null);
    }

    @Command(commandName = NAME,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class),
                    @CommandParameter(name = "branch", type = String.class),
                    @CommandParameter(name = "offset", type = Integer.class, optional = true, defaultValue = "0", min = "0"),
                    @CommandParameter(name = "length", type = Integer.class, optional = true, defaultValue = "6000", min = "1", max = "20000"),
                    @CommandParameter(name = "field", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_GET_PART,
            description = M_L.DESC_COMMAND_GET_PART,
            llmDescription = M_L.DESC_COMMAND_GET_PART_LLM,
            parameters = {@MarginaliaParameterText(name = "part", description = M_L.DESC_COMMAND_PART_PARAM),
                    @MarginaliaParameterText(name = "branch", description = M_L.DESC_COMMAND_BRANCH_PARAM),
                    @MarginaliaParameterText(name = "offset", description = M_L.DESC_COMMAND_OFFSET_PARAM),
                    @MarginaliaParameterText(name = "length", description = M_L.DESC_COMMAND_LENGTH_PARAM),
                    @MarginaliaParameterText(name = "field", description = M_L.DESC_COMMAND_FIELD_PARAM)},
            returns = M_L.DESC_COMMAND_GET_PART_RETURN)
    public void getPartField(CommandContext context) throws Exception {
        getPart(context, ref(context, "part"), ref(context, "branch"), context.get("offset", Integer.class), context.get("length", Integer.class), context.get("field", String.class));
    }

    private void getPart(CommandContext context, String position, String branch, Integer offset, Integer length,
                         String field) throws Exception {
        Book book = new Book(context);
        Book.Located at = book.locate(position, branch);
        List<ChatMessage> path = at.path();
        ChatMessage message = at.message();
        String name = field == null || field.isBlank() ? "response" : field.strip().toLowerCase();
        String text = switch (name) {
            case "response" -> message.getResponse();
            case "instructions" -> message.getInstructions();
            case "scene" -> message.getSceneSetting();
            case "pov" -> message.getPovCharacter();
            case "present" -> message.getPresentCharacters();
            case "reasoning" -> message.getResponseReasoning();
            default -> throw new IllegalArgumentException(String.format(loc.getValue(M_L.ERROR_COMMAND_UNKNOWN_FIELD), field,
                    String.join(", ", FIELDS)));
        };

        JsonObject result = new JsonObject();
        result.addProperty("handle", message.getUuid());
        result.addProperty("position", at.position());
        result.addProperty("parts", path.size());
        result.addProperty("field", name);
        slice(result, text, offset, length);
        result.addProperty("words", message.getWordCount());
        result.addProperty("tokens", message.getTokenCount());
        result.addProperty("hasOriginalResponse", message.getOriginalResponse() != null);
        result.addProperty("model", message.getModelUsed());
        result.addProperty("protocol", message.getProtocolUsed());
        if ("response".equals(name)) {
            // what a new generation of the part would reuse
            JsonObject input = new JsonObject();
            input.addProperty("instructions", message.getInstructions());
            input.addProperty("scene", message.getSceneSetting());
            input.addProperty("pov", message.getPovCharacter());
            input.addProperty("present", message.getPresentCharacters());
            result.add("turnInput", input);
        }
        answer(context, result);
    }
}
