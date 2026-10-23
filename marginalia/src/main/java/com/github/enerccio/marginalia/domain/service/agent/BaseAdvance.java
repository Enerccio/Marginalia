package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Change;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Field;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Item;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collection;

/**
 * Brings the base snapshot of a run up to date with the changes of the user that a sync put into the working copy. The base is
 * what the review compares the copy and the book with, so a value the user changed and the copy took over has to be the base's
 * value too: otherwise a later change of the agent to it would look like a change both sides made. Nothing else of the base
 * changes: what the sync did not take over (the conflicts, the deletions) stays as it was, and the review sees it.
 * <p>
 * The items are the ones of the plan of the sync ({@link MergeDiff#computeSync}) that were applied, with the fields that were; the
 * values are read from the snapshot of the book.
 */
public final class BaseAdvance {

    private BaseAdvance() {
    }

    /**
     * @param base the base snapshot, changed in place
     * @param book the snapshot of the book the sync read
     * @param done the items that were taken over, with only the fields that were
     */
    public static void advance(JsonObject base, JsonObject book, Collection<Item> done) {
        for (Item item : done) {
            switch (item.kind()) {
                case BOOK -> book(base, book, item);
                case MESSAGE -> message(base, book, item);
                case LOREBOOK -> lorebook(base, book, item);
                case ENTRY -> entry(base, book, item);
                case ANNOTATION -> annotation(base, book, item);
            }
        }
    }

    private static String key(Item item) {
        return item.id().substring(2);
    }

    private static JsonArray array(JsonObject object, String name) {
        JsonElement element = object.get(name);
        if (element == null || !element.isJsonArray()) {
            JsonArray created = new JsonArray();
            object.add(name, created);
            return created;
        }
        return element.getAsJsonArray();
    }

    private static JsonObject find(JsonArray array, String uuid) {
        if (array == null) {
            return null;
        }
        for (JsonElement element : array) {
            if (element.isJsonObject() && uuid.equals(string(element.getAsJsonObject(), "uuid"))) {
                return element.getAsJsonObject();
            }
        }
        return null;
    }

    private static String string(JsonObject object, String name) {
        JsonElement element = object.get(name);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }

    private static JsonObject extended(JsonObject object) {
        JsonElement element = object.get("extendedContent");
        if (element == null || !element.isJsonObject()) {
            JsonObject created = new JsonObject();
            object.add("extendedContent", created);
            return created;
        }
        return element.getAsJsonObject();
    }

    /**
     * Sets a field of an object to the value it has in the other one, removes it when the other has none.
     */
    private static void copyField(JsonObject to, JsonObject from, String name) {
        JsonElement value = from == null ? null : from.get(name);
        if (value == null || value.isJsonNull()) {
            to.remove(name);
        } else {
            to.add(name, value.deepCopy());
        }
    }

    /**
     * Brings the data of one key of the {@code attributes} of a lorebook or an entry of the snapshot to the value the other has.
     */
    private static void copyAttribute(JsonObject to, JsonObject from, String field) {
        JsonElement source = from == null ? null : from.get("attributes");
        JsonElement target = to.get("attributes");
        if (target == null || !target.isJsonObject()) {
            target = new JsonObject();
            to.add("attributes", target);
        }
        copyField(target.getAsJsonObject(), source != null && source.isJsonObject() ? source.getAsJsonObject() : null,
                field.substring(MergeDiff.ATTRIBUTES.length()));
    }

    // --- the book

    private static void book(JsonObject base, JsonObject book, Item item) {
        for (Field field : item.fields()) {
            String name = field.name();
            if (name.equals("name")) {
                copyField(base, book, "name");
            } else if (name.equals(MergeDiff.TAGS)) {
                base.add(MergeDiff.TAGS, array(book, MergeDiff.TAGS).deepCopy());
            } else {
                JsonElement bookExtended = book.get("extendedContent");
                copyField(extended(base), bookExtended != null && bookExtended.isJsonObject() ? bookExtended.getAsJsonObject() : null, name);
            }
        }
    }

    // --- parts

    private static void message(JsonObject base, JsonObject book, Item item) {
        JsonArray messages = array(base, "messages");
        JsonObject from = find(array(book, "messages"), key(item));
        if (from == null) {
            return;
        }
        if (item.change() == Change.ADDED) {
            if (find(messages, key(item)) == null) {
                messages.add(from.deepCopy());
            }
            return;
        }
        JsonObject to = find(messages, key(item));
        if (to == null || item.change() != Change.CHANGED) {
            return;
        }
        for (Field field : item.fields()) {
            if (field.name().equals("summary")) {
                copyField(to, from, "summary");
            } else {
                JsonElement fromExtended = from.get("extendedContent");
                copyField(extended(to), fromExtended != null && fromExtended.isJsonObject() ? fromExtended.getAsJsonObject() : null, field.name());
            }
        }
    }

    // --- annotations

    /**
     * The part of the snapshot whose annotations hold the one with the uuid.
     */
    private static JsonObject partOfAnnotation(JsonArray messages, String uuid) {
        for (JsonElement element : messages) {
            if (element.isJsonObject() && find(arrayOf(element.getAsJsonObject(), "annotations"), uuid) != null) {
                return element.getAsJsonObject();
            }
        }
        return null;
    }

    private static JsonArray arrayOf(JsonObject object, String name) {
        JsonElement element = object.get(name);
        return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
    }

    private static void annotation(JsonObject base, JsonObject book, Item item) {
        JsonArray messages = array(base, "messages");
        JsonObject fromPart = partOfAnnotation(array(book, "messages"), key(item));
        JsonObject from = fromPart == null ? null : find(arrayOf(fromPart, "annotations"), key(item));
        if (from == null) {
            return;
        }
        JsonObject inBase = partOfAnnotation(messages, key(item));
        if (item.change() == Change.ADDED) {
            JsonObject target = inBase != null ? null : find(messages, string(fromPart, "uuid"));
            if (target != null) {
                array(target, "annotations").add(from.deepCopy());
            }
            return;
        }
        JsonObject to = inBase == null ? null : find(arrayOf(inBase, "annotations"), key(item));
        if (to == null || item.change() != Change.CHANGED) {
            return;
        }
        for (Field field : item.fields()) {
            if (field.name().equals("status")) {
                copyField(to, from, "status");
            } else {
                JsonElement fromExtended = from.get("extendedContent");
                copyField(extended(to), fromExtended != null && fromExtended.isJsonObject() ? fromExtended.getAsJsonObject() : null, field.name());
            }
        }
    }

    // --- lorebooks and entries

    private static void lorebook(JsonObject base, JsonObject book, Item item) {
        JsonArray books = array(base, "lorebooks");
        JsonObject from = find(array(book, "lorebooks"), key(item));
        if (from == null) {
            return;
        }
        if (item.change() == Change.ADDED) {
            if (find(books, key(item)) == null) {
                // its entries come as items of their own
                JsonObject added = from.deepCopy();
                added.add("entries", new JsonArray());
                books.add(added);
            }
            return;
        }
        JsonObject to = find(books, key(item));
        if (to == null || item.change() != Change.CHANGED) {
            return;
        }
        for (Field field : item.fields()) {
            if (field.name().startsWith(MergeDiff.ATTRIBUTES)) {
                copyAttribute(to, from, field.name());
            } else {
                copyField(to, from, field.name());
            }
        }
    }

    private static JsonObject lorebookOfEntry(JsonArray books, String entryUuid) {
        if (books == null) {
            return null;
        }
        for (JsonElement element : books) {
            if (element.isJsonObject() && find(array(element.getAsJsonObject(), "entries"), entryUuid) != null) {
                return element.getAsJsonObject();
            }
        }
        return null;
    }

    private static void entry(JsonObject base, JsonObject book, Item item) {
        JsonArray books = array(base, "lorebooks");
        JsonArray bookBooks = array(book, "lorebooks");
        JsonObject fromBook = lorebookOfEntry(bookBooks, key(item));
        JsonObject from = fromBook == null ? null : find(array(fromBook, "entries"), key(item));
        if (from == null) {
            return;
        }
        JsonObject inBase = lorebookOfEntry(books, key(item));
        if (item.change() == Change.ADDED) {
            JsonObject target = inBase != null ? inBase : find(books, string(fromBook, "uuid"));
            if (inBase == null && target != null) {
                array(target, "entries").add(from.deepCopy());
            }
            return;
        }
        if (inBase == null || item.change() != Change.CHANGED) {
            return;
        }
        JsonObject to = find(array(inBase, "entries"), key(item));
        for (Field field : item.fields()) {
            if (field.name().equals("lorebook")) {
                JsonObject target = find(books, string(fromBook, "uuid"));
                if (target != null && target != inBase) {
                    array(inBase, "entries").remove(to);
                    array(target, "entries").add(to);
                }
            } else if (field.name().startsWith(MergeDiff.ATTRIBUTES)) {
                copyAttribute(to, from, field.name());
            } else {
                copyField(to, from, field.name());
            }
        }
    }
}
