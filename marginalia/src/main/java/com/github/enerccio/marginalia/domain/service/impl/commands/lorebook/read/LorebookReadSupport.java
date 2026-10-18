package com.github.enerccio.marginalia.domain.service.impl.commands.lorebook.read;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read.ManuscriptReadSupport;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

/**
 * What the commands that read the lorebooks of the open book have in common. The lorebooks of a book are its root
 * lorebook and the tree of its subbooks, and nothing else is reachable. Like the parts of the story they are addressed
 * without ids: a lorebook by its <b>number</b> (the order {@code list-lorebooks} gives, the root is 1; the numbers follow
 * the tree, so they change when a subbook is added or removed), an entry by its <b>number in the lorebook</b> (the
 * order of the entries in the lorebook, the first is 1). The options of a command are positional, as for the story.
 */
@Configurable
public abstract class LorebookReadSupport extends ManuscriptReadSupport {

    @Autowired
    protected LorebookService lorebookService;

    @Autowired
    protected LorebookEntryService lorebookEntryService;

    /**
     * The lorebooks of the book, the root first and then the subbooks level by level, each once, and the entries of
     * the ones a command asks for.
     */
    protected final class Lore {

        final List<Lorebook> books = new ArrayList<>();
        private final Map<Long, Integer> numbers = new HashMap<>();
        private final Map<Long, List<LorebookEntry>> entries = new HashMap<>();

        protected Lore(Book book) throws Exception {
            Lorebook root = book.manuscript.getLorebook() == null ? null : lorebookService.find(book.manuscript.getLorebook());
            if (root == null || root.isDeleted()) {
                throw new IllegalStateException(loc.getValue(L.ERROR_COMMAND_NO_LOREBOOK));
            }
            Queue<Lorebook> queue = new ArrayDeque<>();
            queue.add(root);
            while (!queue.isEmpty()) {
                Lorebook next = queue.remove();
                if (numbers.containsKey(next.getId())) {
                    continue;
                }
                books.add(next);
                numbers.put(next.getId(), books.size());
                queue.addAll(lorebookService.getSubbooks(next));
            }
        }

        /** The number of the lorebook, 1 for the root. */
        protected int number(Lorebook book) {
            return numbers.get(book.getId());
        }

        /** The lorebook with that number, the root when it is null. */
        protected Lorebook book(Integer number) {
            int n = number == null ? 1 : number;
            if (n < 1 || n > books.size()) {
                throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_COMMAND_NO_SUCH_LOREBOOK), n, books.size()));
            }
            return books.get(n - 1);
        }

        /** The entries of the lorebook that are not deleted, in the order of the lorebook. */
        protected List<LorebookEntry> entries(Lorebook book) throws Exception {
            List<LorebookEntry> found = entries.get(book.getId());
            if (found == null) {
                found = lorebookEntryService.getEntriesForLorebook(book);
                entries.put(book.getId(), found);
            }
            return found;
        }

        /** The entry with that number in the lorebook. */
        protected LorebookEntry entry(Lorebook book, Integer number) throws Exception {
            List<LorebookEntry> all = entries(book);
            if (number == null || number < 1 || number > all.size()) {
                throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_COMMAND_NO_SUCH_LORE_ENTRY),
                        number(book), number, all.size()));
            }
            return all.get(number - 1);
        }

        /** The numbers of the subbooks of the lorebook. */
        protected JsonArray subbooks(Lorebook book) throws Exception {
            JsonArray subs = new JsonArray();
            for (Lorebook sub : lorebookService.getSubbooks(book)) {
                Integer n = numbers.get(sub.getId());
                if (n != null) {
                    subs.add(n);
                }
            }
            return subs;
        }
    }

    protected static JsonArray names(List<Tag> tags) {
        JsonArray array = new JsonArray();
        for (Tag tag : tags) {
            array.add(tag.getValue());
        }
        return array;
    }

    /**
     * One entry as a list shows it: the settings that decide how it is used and a preview of the text.
     */
    protected JsonObject entryRow(int number, LorebookEntry entry) throws Exception {
        JsonObject row = new JsonObject();
        row.addProperty("entry", number);
        row.addProperty("name", entry.getName());
        row.addProperty("enabled", entry.isEnabled());
        row.addProperty("order", entry.getOrder());
        row.addProperty("insertion", entry.getInsertionMode().name());
        row.addProperty("filtering", entry.getFilteringMode().name());
        row.add("tags", names(tagRelationService.getTagsForObject(entry)));
        String payload = entry.getPayload();
        row.addProperty("payloadLength", payload == null ? 0 : payload.length());
        row.addProperty("preview", preview(payload, PREVIEW_LENGTH));
        return row;
    }
}
