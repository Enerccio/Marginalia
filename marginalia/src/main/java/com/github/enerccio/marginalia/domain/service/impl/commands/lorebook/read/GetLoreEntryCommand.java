package com.github.enerccio.marginalia.domain.service.impl.commands.lorebook.read;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonObject;

/**
 * {@code /get-lore-entry entry [lorebook [offset [length]]]}: one entry of a lorebook (the root when none is given) with
 * all its settings, its comment and a slice of its text with the total length. Changes nothing; the answer is JSON text,
 * see {@link LorebookReadSupport}.
 */
public class GetLoreEntryCommand extends LorebookReadSupport {

    static final String NAME = "get-lore-entry";

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_GET_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_GET_LORE_ENTRY, llmDescription = L.DESC_COMMAND_GET_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = Integer.class, description = L.DESC_COMMAND_LORE_ENTRY_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_LORE_ENTRY_RETURN))
    public void getLoreEntry(CommandContext context) throws Exception {
        getLoreEntry(context, context.get("entry", Integer.class), null, null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_GET_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_GET_LORE_ENTRY, llmDescription = L.DESC_COMMAND_GET_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = Integer.class, description = L.DESC_COMMAND_LORE_ENTRY_PARAM),
                    @CommandParameter(name = "lorebook", type = Integer.class, description = L.DESC_COMMAND_LOREBOOK_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_LORE_ENTRY_RETURN))
    public void getLoreEntryLorebook(CommandContext context) throws Exception {
        getLoreEntry(context, context.get("entry", Integer.class), context.get("lorebook", Integer.class), null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_GET_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_GET_LORE_ENTRY, llmDescription = L.DESC_COMMAND_GET_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = Integer.class, description = L.DESC_COMMAND_LORE_ENTRY_PARAM),
                    @CommandParameter(name = "lorebook", type = Integer.class, description = L.DESC_COMMAND_LOREBOOK_PARAM),
                    @CommandParameter(name = "offset", type = Integer.class, description = L.DESC_COMMAND_OFFSET_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_LORE_ENTRY_RETURN))
    public void getLoreEntryOffset(CommandContext context) throws Exception {
        getLoreEntry(context, context.get("entry", Integer.class), context.get("lorebook", Integer.class), context.get("offset", Integer.class), null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_GET_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_GET_LORE_ENTRY, llmDescription = L.DESC_COMMAND_GET_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = Integer.class, description = L.DESC_COMMAND_LORE_ENTRY_PARAM),
                    @CommandParameter(name = "lorebook", type = Integer.class, description = L.DESC_COMMAND_LOREBOOK_PARAM),
                    @CommandParameter(name = "offset", type = Integer.class, description = L.DESC_COMMAND_OFFSET_PARAM),
                    @CommandParameter(name = "length", type = Integer.class, description = L.DESC_COMMAND_LENGTH_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_LORE_ENTRY_RETURN))
    public void getLoreEntryLength(CommandContext context) throws Exception {
        getLoreEntry(context, context.get("entry", Integer.class), context.get("lorebook", Integer.class), context.get("offset", Integer.class), context.get("length", Integer.class));
    }

    private void getLoreEntry(CommandContext context, Integer entryNumber, Integer lorebook, Integer offset, Integer length) throws Exception {
        Lore lore = new Lore(new Book());
        Lorebook book = lore.book(lorebook);
        LorebookEntry entry = lore.entry(book, entryNumber);

        JsonObject result = new JsonObject();
        result.addProperty("lorebook", lore.number(book));
        result.addProperty("entry", entryNumber);
        result.addProperty("name", entry.getName());
        result.addProperty("enabled", entry.isEnabled());
        result.addProperty("order", entry.getOrder());
        result.addProperty("insertion", entry.getInsertionMode().name());
        result.addProperty("filtering", entry.getFilteringMode().name());
        result.addProperty("keys", entry.getFiltering());
        result.add("tags", names(tagRelationService.getTagsForObject(entry)));
        result.add("negativeTags", names(tagRelationService.getTagsForObject(entry, true)));
        JsonObject comment = new JsonObject();
        slice(comment, entry.getComment(), null, null);
        result.add("comment", comment);
        JsonObject payload = new JsonObject();
        slice(payload, entry.getPayload(), offset, length);
        result.add("payload", payload);
        answer(context, result);
    }
}
