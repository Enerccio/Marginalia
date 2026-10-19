package com.github.enerccio.marginalia.domain.service.impl.commands.lorebook.read;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.Command;
import com.github.enerccio.marginalia.domain.traits.CommandDataSeverity;
import com.github.enerccio.marginalia.domain.traits.CommandParameterSignature;
import com.github.enerccio.marginalia.domain.traits.CommandReturn;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * {@code /list-lorebooks}: the root lorebook of the open book and the tree of its subbooks, with their numbers.
 * Changes nothing; the answer is JSON text, see {@link LorebookReadSupport}.
 */
public class ListLorebooksCommand extends LorebookReadSupport {

    static final String NAME = "list-lorebooks";

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_LIST_LOREBOOKS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_LIST_LOREBOOKS, llmDescription = L.DESC_COMMAND_LIST_LOREBOOKS_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_LIST_LOREBOOKS_RETURN))
    public void listLorebooks(CommandContext context) throws Exception {
        Lore lore = new Lore(new Book(context));
        JsonArray books = new JsonArray();
        for (Lorebook book : lore.books) {
            JsonObject row = new JsonObject();
            row.addProperty("lorebook", lore.number(book));
            row.addProperty("handle", book.getUuid());
            row.addProperty("name", book.getName());
            row.addProperty("enabled", book.isEnabled());
            int enabled = 0;
            for (LorebookEntry entry : lore.entries(book)) {
                if (entry.isEnabled()) {
                    enabled++;
                }
            }
            row.addProperty("entries", lore.entries(book).size());
            row.addProperty("enabledEntries", enabled);
            row.add("tags", names(tagRelationService.getTagsForObject(book)));
            row.add("subbooks", lore.subbooks(book));
            books.add(row);
        }
        JsonObject result = new JsonObject();
        result.addProperty("total", lore.books.size());
        result.add("lorebooks", books);
        answer(context, result);
    }
}
