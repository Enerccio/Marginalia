package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.write;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonObject;

/**
 * {@code /edit-part part old new [field]}, {@code /replace-part-text part text [field]}, {@code /append-to-part part text [separator]},
 * {@code /set-part-fields part [instructions [scene [pov [present]]]]}: the commands that change the text of a part of the story and
 * what it was made from. A part is given by its handle, see {@link ManuscriptWriteSupport}; the options are positional, so to set one
 * the ones before it are given too.
 */
public class PartEditCommands extends ManuscriptWriteSupport {

    static final String EDIT_PART = "edit-part";
    static final String REPLACE_PART_TEXT = "replace-part-text";
    static final String APPEND_TO_PART = "append-to-part";
    static final String SET_PART_FIELDS = "set-part-fields";

    @Command(commandName = EDIT_PART, humanName = L.LABEL_COMMAND_EDIT_PART,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_EDIT_PART, llmDescription = L.DESC_COMMAND_EDIT_PART_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @CommandParameter(name = "old", type = String.class, description = L.DESC_COMMAND_OLD_PARAM),
                    @CommandParameter(name = "new", type = String.class, description = L.DESC_COMMAND_NEW_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_EDIT_PART_RETURN))
    public void editPart(CommandContext context) throws Exception {
        editPart(context, ref(context, "part"), context.get("old", String.class), context.get("new", String.class), null);
    }

    @Command(commandName = EDIT_PART, humanName = L.LABEL_COMMAND_EDIT_PART,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_EDIT_PART, llmDescription = L.DESC_COMMAND_EDIT_PART_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @CommandParameter(name = "old", type = String.class, description = L.DESC_COMMAND_OLD_PARAM),
                    @CommandParameter(name = "new", type = String.class, description = L.DESC_COMMAND_NEW_PARAM),
                    @CommandParameter(name = "field", type = String.class, description = L.DESC_COMMAND_WRITE_FIELD_PARAM, allowed = {"response", "instructions", "scene", "pov", "present"})}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_EDIT_PART_RETURN))
    public void editPartField(CommandContext context) throws Exception {
        editPart(context, ref(context, "part"), context.get("old", String.class), context.get("new", String.class), ref(context, "field"));
    }

    @Command(commandName = REPLACE_PART_TEXT, humanName = L.LABEL_COMMAND_REPLACE_PART_TEXT,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_REPLACE_PART_TEXT, llmDescription = L.DESC_COMMAND_REPLACE_PART_TEXT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @CommandParameter(name = "text", type = String.class, description = L.DESC_COMMAND_NEW_TEXT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REPLACE_PART_TEXT_RETURN))
    public void replacePartText(CommandContext context) throws Exception {
        replacePartText(context, ref(context, "part"), context.get("text", String.class), null);
    }

    @Command(commandName = REPLACE_PART_TEXT, humanName = L.LABEL_COMMAND_REPLACE_PART_TEXT,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_REPLACE_PART_TEXT, llmDescription = L.DESC_COMMAND_REPLACE_PART_TEXT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @CommandParameter(name = "text", type = String.class, description = L.DESC_COMMAND_NEW_TEXT_PARAM),
                    @CommandParameter(name = "field", type = String.class, description = L.DESC_COMMAND_WRITE_FIELD_PARAM, allowed = {"response", "instructions", "scene", "pov", "present"})}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REPLACE_PART_TEXT_RETURN))
    public void replacePartTextField(CommandContext context) throws Exception {
        replacePartText(context, ref(context, "part"), context.get("text", String.class), ref(context, "field"));
    }

    @Command(commandName = APPEND_TO_PART, humanName = L.LABEL_COMMAND_APPEND_TO_PART,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_APPEND_TO_PART, llmDescription = L.DESC_COMMAND_APPEND_TO_PART_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @CommandParameter(name = "text", type = String.class, description = L.DESC_COMMAND_NEW_TEXT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_APPEND_TO_PART_RETURN))
    public void appendToPart(CommandContext context) throws Exception {
        appendToPart(context, ref(context, "part"), context.get("text", String.class), null);
    }

    @Command(commandName = APPEND_TO_PART, humanName = L.LABEL_COMMAND_APPEND_TO_PART,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_APPEND_TO_PART, llmDescription = L.DESC_COMMAND_APPEND_TO_PART_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @CommandParameter(name = "text", type = String.class, description = L.DESC_COMMAND_NEW_TEXT_PARAM),
                    @CommandParameter(name = "separator", type = String.class, description = L.DESC_COMMAND_SEPARATOR_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_APPEND_TO_PART_RETURN))
    public void appendToPartSeparator(CommandContext context) throws Exception {
        appendToPart(context, ref(context, "part"), context.get("text", String.class), context.get("separator", String.class));
    }

    @Command(commandName = SET_PART_FIELDS, humanName = L.LABEL_COMMAND_SET_PART_FIELDS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_PART_FIELDS, llmDescription = L.DESC_COMMAND_SET_PART_FIELDS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_TURN_INSTRUCTIONS_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_PART_FIELDS_RETURN))
    public void setPartFields(CommandContext context) throws Exception {
        setPartFields(context, ref(context, "part"), context.get("instructions", String.class), null, null, null);
    }

    @Command(commandName = SET_PART_FIELDS, humanName = L.LABEL_COMMAND_SET_PART_FIELDS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_PART_FIELDS, llmDescription = L.DESC_COMMAND_SET_PART_FIELDS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_TURN_INSTRUCTIONS_PARAM),
                    @CommandParameter(name = "scene", type = String.class, description = L.DESC_COMMAND_TURN_SCENE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_PART_FIELDS_RETURN))
    public void setPartFieldsScene(CommandContext context) throws Exception {
        setPartFields(context, ref(context, "part"), context.get("instructions", String.class), context.get("scene", String.class), null, null);
    }

    @Command(commandName = SET_PART_FIELDS, humanName = L.LABEL_COMMAND_SET_PART_FIELDS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_PART_FIELDS, llmDescription = L.DESC_COMMAND_SET_PART_FIELDS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_TURN_INSTRUCTIONS_PARAM),
                    @CommandParameter(name = "scene", type = String.class, description = L.DESC_COMMAND_TURN_SCENE_PARAM),
                    @CommandParameter(name = "pov", type = String.class, description = L.DESC_COMMAND_TURN_POV_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_PART_FIELDS_RETURN))
    public void setPartFieldsPov(CommandContext context) throws Exception {
        setPartFields(context, ref(context, "part"), context.get("instructions", String.class), context.get("scene", String.class), context.get("pov", String.class), null);
    }

    @Command(commandName = SET_PART_FIELDS, humanName = L.LABEL_COMMAND_SET_PART_FIELDS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_PART_FIELDS, llmDescription = L.DESC_COMMAND_SET_PART_FIELDS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_PART_HANDLE_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_TURN_INSTRUCTIONS_PARAM),
                    @CommandParameter(name = "scene", type = String.class, description = L.DESC_COMMAND_TURN_SCENE_PARAM),
                    @CommandParameter(name = "pov", type = String.class, description = L.DESC_COMMAND_TURN_POV_PARAM),
                    @CommandParameter(name = "present", type = String.class, description = L.DESC_COMMAND_TURN_PRESENT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_PART_FIELDS_RETURN))
    public void setPartFieldsPresent(CommandContext context) throws Exception {
        setPartFields(context, ref(context, "part"), context.get("instructions", String.class), context.get("scene", String.class), context.get("pov", String.class), context.get("present", String.class));
    }

    private void editPart(CommandContext context, String handle, String old, String replacement, String field) throws Exception {
        Book book = new Book(context);
        ChatMessage message = part(book, handle);
        String name = fieldName(field);
        if (old == null || old.isEmpty()) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_EDIT_OLD_EMPTY));
        }
        String current = read(message, name);
        int first = current.indexOf(old);
        if (first < 0) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_EDIT_NOT_FOUND));
        }
        int found = 0;
        for (int at = first; at >= 0; at = current.indexOf(old, at + old.length())) {
            found++;
        }
        if (found > 1) {
            throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_COMMAND_EDIT_AMBIGUOUS), found));
        }
        store(context, book, message, name,
                current.substring(0, first) + (replacement == null ? "" : replacement) + current.substring(first + old.length()));
    }

    private void replacePartText(CommandContext context, String handle, String text, String field) throws Exception {
        Book book = new Book(context);
        ChatMessage message = part(book, handle);
        String name = fieldName(field);
        if (!confirmed(context, String.format(loc.getValue(L.MSG_COMMAND_REPLACE_PART_CONFIRM), book.locate(handle, null).position()))) {
            return;
        }
        store(context, book, message, name, text == null ? "" : text);
    }

    private void appendToPart(CommandContext context, String handle, String text, String separator) throws Exception {
        Book book = new Book(context);
        ChatMessage message = part(book, handle);
        String current = read(message, "response");
        String between = separator != null ? separator : current.isBlank() ? "" : "\n\n";
        store(context, book, message, "response", current + between + (text == null ? "" : text));
    }

    private void setPartFields(CommandContext context, String handle, String instructions, String scene, String pov,
                               String present) throws Exception {
        Book book = new Book(context);
        ChatMessage message = part(book, handle);
        if (instructions == null && scene == null && pov == null && present == null) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_NOTHING_TO_CHANGE));
        }
        if (instructions != null) {
            message.setInstructions(instructions);
        }
        if (scene != null) {
            message.setSceneSetting(scene);
        }
        if (pov != null) {
            message.setPovCharacter(pov);
        }
        if (present != null) {
            message.setPresentCharacters(present);
        }
        ChatMessage saved = chatMessageService.save(message);
        JsonObject result = describe(saved, book.locate(handle, null).position());
        result.add("turnInput", turnInput(saved));
        changed();
        answer(context, result);
    }

    private static JsonObject turnInput(ChatMessage message) {
        JsonObject input = new JsonObject();
        input.addProperty("instructions", message.getInstructions());
        input.addProperty("scene", message.getSceneSetting());
        input.addProperty("pov", message.getPovCharacter());
        input.addProperty("present", message.getPresentCharacters());
        return input;
    }

    /**
     * Stores the new text of a field of the part and answers with the part and the summaries it made outdated.
     */
    private void store(CommandContext context, Book book, ChatMessage message, String field, String text) throws Exception {
        Book.Located at = book.locate(message.getUuid(), null);
        JsonObject result;
        if ("response".equals(field)) {
            ChatMessageService.PartEdit edit = chatMessageService.updateResponse(book.manuscript, message, text,
                    RevisionOrigin.automatic(RevisionDetail.COMMAND, true));
            result = describe(edit.message(), at.position());
            reportOutdated(result, at.path(), edit.outdated());
        } else {
            write(message, field, text);
            result = describe(chatMessageService.save(message), at.position());
        }
        result.addProperty("field", field);
        changed();
        answer(context, result);
    }
}
