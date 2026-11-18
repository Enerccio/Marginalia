package com.github.enerccio.marginalia.domain.service.impl.commands.lorebook.read;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.loc.M_L;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Locale;

/**
 * {@code /list-lore-entries [lorebook [filter [offset [limit]]]]}: the entries of a lorebook (the root when none is
 * given) with their settings and a preview, the ones whose name or tag contains the filter. The numbers are the
 * position in the whole lorebook, so they stay the same with a filter. Changes nothing; the answer is JSON text, see
 * {@link LorebookReadSupport}.
 */
public class ListLoreEntriesCommand extends LorebookReadSupport {

    static final String NAME = "list-lore-entries";

    @Command(commandName = NAME,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_LIST_LORE_ENTRIES,
            description = M_L.DESC_COMMAND_LIST_LORE_ENTRIES,
            llmDescription = M_L.DESC_COMMAND_LIST_LORE_ENTRIES_LLM,
            returns = M_L.DESC_COMMAND_LIST_LORE_ENTRIES_RETURN)
    public void listLoreEntries(CommandContext context) throws Exception {
        listLoreEntries(context, null, null, null, null);
    }

    @Command(commandName = NAME,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "lorebook", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_LIST_LORE_ENTRIES,
            description = M_L.DESC_COMMAND_LIST_LORE_ENTRIES,
            llmDescription = M_L.DESC_COMMAND_LIST_LORE_ENTRIES_LLM,
            parameters = @MarginaliaParameterText(name = "lorebook", description = M_L.DESC_COMMAND_LOREBOOK_PARAM),
            returns = M_L.DESC_COMMAND_LIST_LORE_ENTRIES_RETURN)
    public void listLoreEntriesLorebook(CommandContext context) throws Exception {
        listLoreEntries(context, ref(context, "lorebook"), null, null, null);
    }

    @Command(commandName = NAME,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "lorebook", type = String.class),
                    @CommandParameter(name = "filter", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_LIST_LORE_ENTRIES,
            description = M_L.DESC_COMMAND_LIST_LORE_ENTRIES,
            llmDescription = M_L.DESC_COMMAND_LIST_LORE_ENTRIES_LLM,
            parameters = {@MarginaliaParameterText(name = "lorebook", description = M_L.DESC_COMMAND_LOREBOOK_PARAM),
                    @MarginaliaParameterText(name = "filter", description = M_L.DESC_COMMAND_LORE_FILTER_PARAM)},
            returns = M_L.DESC_COMMAND_LIST_LORE_ENTRIES_RETURN)
    public void listLoreEntriesFilter(CommandContext context) throws Exception {
        listLoreEntries(context, ref(context, "lorebook"), context.get("filter", String.class), null, null);
    }

    @Command(commandName = NAME,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "lorebook", type = String.class),
                    @CommandParameter(name = "filter", type = String.class),
                    @CommandParameter(name = "offset", type = Integer.class, optional = true, defaultValue = "0", min = "0")}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_LIST_LORE_ENTRIES,
            description = M_L.DESC_COMMAND_LIST_LORE_ENTRIES,
            llmDescription = M_L.DESC_COMMAND_LIST_LORE_ENTRIES_LLM,
            parameters = {@MarginaliaParameterText(name = "lorebook", description = M_L.DESC_COMMAND_LOREBOOK_PARAM),
                    @MarginaliaParameterText(name = "filter", description = M_L.DESC_COMMAND_LORE_FILTER_PARAM),
                    @MarginaliaParameterText(name = "offset", description = M_L.DESC_COMMAND_OFFSET_PARAM)},
            returns = M_L.DESC_COMMAND_LIST_LORE_ENTRIES_RETURN)
    public void listLoreEntriesOffset(CommandContext context) throws Exception {
        listLoreEntries(context, ref(context, "lorebook"), context.get("filter", String.class), context.get("offset", Integer.class), null);
    }

    @Command(commandName = NAME,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "lorebook", type = String.class),
                    @CommandParameter(name = "filter", type = String.class),
                    @CommandParameter(name = "offset", type = Integer.class, optional = true, defaultValue = "0", min = "0"),
                    @CommandParameter(name = "limit", type = Integer.class, optional = true, defaultValue = "50", min = "1", max = "200")}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_LIST_LORE_ENTRIES,
            description = M_L.DESC_COMMAND_LIST_LORE_ENTRIES,
            llmDescription = M_L.DESC_COMMAND_LIST_LORE_ENTRIES_LLM,
            parameters = {@MarginaliaParameterText(name = "lorebook", description = M_L.DESC_COMMAND_LOREBOOK_PARAM),
                    @MarginaliaParameterText(name = "filter", description = M_L.DESC_COMMAND_LORE_FILTER_PARAM),
                    @MarginaliaParameterText(name = "offset", description = M_L.DESC_COMMAND_OFFSET_PARAM),
                    @MarginaliaParameterText(name = "limit", description = M_L.DESC_COMMAND_LIMIT_PARAM)},
            returns = M_L.DESC_COMMAND_LIST_LORE_ENTRIES_RETURN)
    public void listLoreEntriesLimit(CommandContext context) throws Exception {
        listLoreEntries(context, ref(context, "lorebook"), context.get("filter", String.class), context.get("offset", Integer.class), context.get("limit", Integer.class));
    }

    private void listLoreEntries(CommandContext context, String lorebook, String filter, Integer offset, Integer limit) throws Exception {
        Lore lore = new Lore(new Book(context));
        Lorebook book = lore.book(lorebook);
        List<LorebookEntry> all = lore.entries(book);
        String needle = filter == null || filter.isBlank() ? null : filter.strip().toLowerCase(Locale.ROOT);

        JsonArray rows = new JsonArray();
        int matching = 0;
        int from = Math.max(offset == null ? 0 : offset, 0);
        int max = limit(limit);
        for (int i = 0; i < all.size(); i++) {
            LorebookEntry entry = all.get(i);
            if (needle != null && !matches(entry, needle)) {
                continue;
            }
            if (matching++ >= from && rows.size() < max) {
                rows.add(entryRow(i + 1, entry));
            }
        }
        JsonObject result = new JsonObject();
        result.addProperty("lorebook", lore.number(book));
        result.addProperty("handle", book.getUuid());
        result.addProperty("name", book.getName());
        result.addProperty("entries", all.size());
        result.addProperty("matching", matching);
        result.addProperty("offset", Math.min(from, matching));
        result.add("items", rows);
        answer(context, result);
    }

    private boolean matches(LorebookEntry entry, String needle) throws Exception {
        if (entry.getName() != null && entry.getName().toLowerCase(Locale.ROOT).contains(needle)) {
            return true;
        }
        for (Tag tag : tagRelationService.getTagsForObject(entry)) {
            if (tag.getValue() != null && tag.getValue().toLowerCase(Locale.ROOT).contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
