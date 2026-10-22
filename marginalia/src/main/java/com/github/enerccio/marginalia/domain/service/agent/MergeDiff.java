package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.agent.MergePlan.*;
import com.github.enerccio.marginalia.domain.service.agent.jsonmerge.JsonMerge;
import com.github.enerccio.marginalia.domain.service.agent.jsonmerge.JsonText;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The three-way comparison behind the merge of a run: the book as the run started (the base), the working copy of the agent
 * and the book as it is now, all three as the JSON snapshot of a book with its lorebooks (the entries with their uuids).
 * <p>
 * The items of the copy are matched to the items of the book by the pairs of the run (copy uuid to original uuid); an item of
 * the copy that has no pair is one the agent made. Every item is compared field by field: what the agent changed and the book
 * did not is a change to take over, what both changed to different values is a conflict, what the book changed alone is not a
 * change of the run at all. Plain text, no database: the same comparison serves the review screen and the apply.
 */
public final class MergeDiff {

    /**
     * The prefix of the key of an item the agent made, followed by its uuid in the copy.
     */
    public static final String NEW = "new:";

    public static final String ID_BOOK = "B:";
    public static final String ID_MESSAGE = "M:";
    public static final String ID_LOREBOOK = "L:";
    public static final String ID_ENTRY = "E:";

    /**
     * The settings of a book an agent can change, besides its tags and the data of the plugins ({@code attributes_*}).
     */
    public static final List<String> BOOK_FIELDS = List.of("name", "description", "language", "pov", "tense", "style", "template",
            "userPrompt", "summaryPrompt", "metaSummaryPrompt");
    public static final String TAGS = "tags";
    public static final String ATTRIBUTES = "attributes_";

    public static final List<String> MESSAGE_FIELDS = List.of("response", "instructions", "sceneSetting", "povCharacter",
            "presentCharacters", "summary");
    public static final List<String> LOREBOOK_FIELDS = List.of("name", "enabled", "tags", "subbooks");
    public static final List<String> ENTRY_FIELDS = List.of("name", "payload", "comment", "enabled", "order", "filtering",
            "filteringMode", "insertionMode", "tags", "negativeTags", "lorebook");

    private static final Set<String> SETS = Set.of("tags", "negativeTags", "subbooks");
    private static final Set<String> EDITABLE = Set.of("name", "description", "pov", "tense", "style", "template", "userPrompt",
            "summaryPrompt", "metaSummaryPrompt", "response", "instructions", "sceneSetting", "povCharacter", "presentCharacters",
            "summary", "payload", "comment", "filtering");

    private static final Pattern UUID = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final String SEPARATOR = "\u0000";
    // the part of the properties of an item that is only shown, not compared
    private static final String DISPLAY = "~";
    /**
     * The field of a part that says which part it follows. It is not one of the {@link #MESSAGE_FIELDS}: a part that is moved
     * under another is only a change of the book when the other is a part the agent inserted above it.
     */
    public static final String PARENT = "parent";
    private static final int SNIPPET = 70;

    private MergeDiff() {
    }

    /**
     * The items of one snapshot, by key, with the properties of every item as text.
     */
    static final class View {
        final Map<String, Map<String, String>> messages = new LinkedHashMap<>();
        final Map<String, Map<String, String>> lorebooks = new LinkedHashMap<>();
        final Map<String, Map<String, String>> entries = new LinkedHashMap<>();
        final Map<String, String> book = new LinkedHashMap<>();
    }

    /**
     * What the agent changed, to be taken over by the book (the review).
     *
     * @param base           the snapshot of the book when the run started
     * @param copy           the snapshot of the working copy
     * @param current        the snapshot of the book now
     * @param copyToOriginal the pairs of the run: the uuid of an item of the copy to the uuid of the item it was made from
     */
    public static MergePlan compute(JsonObject base, JsonObject copy, JsonObject current, Map<String, String> copyToOriginal) {
        Map<String, String> originalToCopy = invert(copyToOriginal);
        return compute(base, view(copy, copyToOriginal), view(current, null),
                key -> key.startsWith(NEW) ? key.substring(NEW.length()) : originalToCopy.get(key),
                key -> key.startsWith(NEW) ? null : key);
    }

    /**
     * What the user changed in the book since the run started, to be taken over by the working copy (the sync on resume): the
     * same comparison with the roles swapped. The items of the plan have the book as their source and the copy as their
     * target; what the agent changed too is a conflict.
     *
     * @param book           the snapshot of the book now
     * @param copy           the snapshot of the working copy
     * @param copyToOriginal the pairs of the run
     */
    public static MergePlan computeSync(JsonObject base, JsonObject book, JsonObject copy, Map<String, String> copyToOriginal) {
        Map<String, String> originalToCopy = invert(copyToOriginal);
        return compute(base, view(book, null), view(copy, copyToOriginal), key -> key, originalToCopy::get);
    }

    private static Map<String, String> invert(Map<String, String> copyToOriginal) {
        Map<String, String> originalToCopy = new HashMap<>();
        copyToOriginal.forEach((copyUuid, original) -> originalToCopy.putIfAbsent(original, copyUuid));
        return originalToCopy;
    }

    /**
     * What changed between two states of the same book, for a look at the difference (not for a merge): the items of {@code later}
     * that are not in {@code earlier} are added, the ones it lacks are deleted. The same item is found by its key: the uuid of
     * the original in the pairs, or what the pairs say for an item without an original; a key that is not in the pairs is the uuid
     * itself. So two states of copies that were made apart (before and after a restore) still match.
     *
     * @param earlierKeys the uuid of an item of {@code earlier} to its key
     * @param laterKeys   the same for {@code later}
     */
    public static MergePlan between(JsonObject earlier, Map<String, String> earlierKeys, JsonObject later, Map<String, String> laterKeys) {
        View before = view(earlier, earlierKeys);
        return compute(before, view(later, laterKeys), before, key -> key, key -> key);
    }

    private static MergePlan compute(JsonObject base, View changed, View other, Function<String, String> source,
                                     Function<String, String> target) {
        return compute(view(base, null), changed, other, source, target);
    }

    private static MergePlan compute(View baseView, View changed, View other, Function<String, String> source,
                                     Function<String, String> target) {
        boolean bookChanged = !baseView.messages.equals(other.messages) || !baseView.lorebooks.equals(other.lorebooks)
                || !baseView.entries.equals(other.entries) || !baseView.book.equals(other.book);

        Map<String, String> names = new HashMap<>();
        for (View v : List.of(baseView, other, changed)) {
            v.lorebooks.forEach((key, props) -> names.put(key, props.get("name")));
        }

        Context context = new Context(baseView, changed, other, source, target, names);
        List<Item> items = new ArrayList<>(bookItems(context));
        items.addAll(lorebookItems(context));
        items.addAll(entryItems(context));
        items.addAll(messageItems(context));
        return new MergePlan(items, names, bookChanged);
    }

    /**
     * The three views, and where the uuid of an item is in the source (the side that changed) and in the target (the side that
     * takes the change over). A key is the uuid in the book, or {@code new:} and the uuid in the copy for an item the agent made.
     */
    private record Context(View base, View changed, View other, Function<String, String> source, Function<String, String> target,
                           Map<String, String> names) {

        String sourceUuid(String key) {
            return source.apply(key);
        }

        String targetUuid(String key) {
            return target.apply(key);
        }
    }

    // --- reading a snapshot

    static View view(JsonObject snapshot, Map<String, String> toOriginal) {
        View view = new View();
        if (snapshot == null) {
            return view;
        }
        readBook(snapshot, view, toOriginal);
        readLorebooks(snapshot, view, toOriginal);
        readMessages(snapshot, view, toOriginal);
        return view;
    }

    private static String key(String uuid, Map<String, String> toOriginal) {
        if (uuid == null || uuid.isEmpty()) {
            return "";
        }
        return toOriginal == null ? uuid : toOriginal.getOrDefault(uuid, NEW + uuid);
    }

    /**
     * The text with the uuids of the copy replaced by the uuids of the book, what the copy says about its items is then said
     * in the words of the book.
     */
    public static String translate(String text, Map<String, String> toOriginal) {
        if (text == null || toOriginal == null || text.length() < 36) {
            return text;
        }
        Matcher matcher = UUID.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(toOriginal.getOrDefault(matcher.group(), matcher.group())));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String text(JsonObject object, String name) {
        JsonElement element = object == null ? null : object.get(name);
        if (element == null || element.isJsonNull()) {
            return "";
        }
        return element.isJsonPrimitive() ? element.getAsString() : element.toString();
    }

    /**
     * The data kept in an attribute as the compact text that is compared, with the uuids of the copy replaced by the uuids of the
     * book; the text of what is not JSON.
     */
    private static String json(JsonElement value, Map<String, String> toOriginal) {
        String text = translate(value.toString(), toOriginal);
        try {
            return JsonText.compact(JsonText.parse(text));
        } catch (JsonText.InvalidJsonException e) {
            return text;
        }
    }

    /**
     * The fields for the data extensions keep in the {@code attributes} of a lorebook or an entry, one for each key.
     */
    private static void readAttributes(JsonObject marshalled, Map<String, String> props, Map<String, String> toOriginal) {
        JsonObject attributes = object(marshalled, "attributes");
        if (attributes != null) {
            attributes.keySet().stream().sorted().forEach(key -> props.put(ATTRIBUTES + key, json(attributes.get(key), toOriginal)));
        }
    }

    private static JsonObject object(JsonObject object, String name) {
        JsonElement element = object == null ? null : object.get(name);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private static String set(JsonArray array) {
        Set<String> values = new TreeSet<>();
        if (array != null) {
            for (JsonElement element : array) {
                if (element.isJsonPrimitive()) {
                    values.add(element.getAsString());
                }
            }
        }
        return String.join("\n", values);
    }

    private static JsonArray array(JsonObject object, String name) {
        JsonElement element = object == null ? null : object.get(name);
        return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
    }

    private static void readBook(JsonObject snapshot, View view, Map<String, String> toOriginal) {
        view.book.put("name", text(snapshot, "name"));
        JsonObject extended = object(snapshot, "extendedContent");
        for (String field : BOOK_FIELDS) {
            if (!field.equals("name")) {
                view.book.put(field, text(extended, field));
            }
        }
        if (extended != null) {
            for (String field : extended.keySet()) {
                if (field.startsWith(ATTRIBUTES)) {
                    // the JSON of the value, a plugin keeps whatever it likes there
                    view.book.put(field, json(extended.get(field), toOriginal));
                }
            }
        }
        view.book.put(TAGS, set(array(snapshot, TAGS)));
    }

    private static void readLorebooks(JsonObject snapshot, View view, Map<String, String> toOriginal) {
        JsonArray books = array(snapshot, "lorebooks");
        if (books == null) {
            return;
        }
        for (JsonElement element : books) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject bookObject = element.getAsJsonObject();
            String bookKey = key(text(bookObject, "uuid"), toOriginal);
            if (bookKey.isEmpty()) {
                continue;
            }
            Map<String, String> props = new LinkedHashMap<>();
            props.put("name", text(bookObject, "name"));
            props.put("enabled", text(bookObject, "enabled"));
            props.put("tags", set(array(bookObject, "tags")));
            Set<String> subbooks = new TreeSet<>();
            JsonArray refs = array(bookObject, "subbooks");
            if (refs != null) {
                for (JsonElement ref : refs) {
                    subbooks.add(key(ref.getAsString(), toOriginal));
                }
            }
            props.put("subbooks", String.join("\n", subbooks));
            readAttributes(bookObject, props, toOriginal);
            view.lorebooks.put(bookKey, props);

            JsonArray entries = array(bookObject, "entries");
            if (entries == null) {
                continue;
            }
            for (JsonElement entryElement : entries) {
                if (!entryElement.isJsonObject()) {
                    continue;
                }
                JsonObject entry = entryElement.getAsJsonObject();
                String entryKey = key(text(entry, "uuid"), toOriginal);
                if (entryKey.isEmpty()) {
                    continue;
                }
                Map<String, String> entryProps = new LinkedHashMap<>();
                entryProps.put("lorebook", bookKey);
                for (String field : List.of("name", "payload", "comment", "enabled", "order", "filtering", "filteringMode", "insertionMode")) {
                    entryProps.put(field, text(entry, field));
                }
                entryProps.put("tags", set(array(entry, "tags")));
                entryProps.put("negativeTags", set(array(entry, "negativeTags")));
                readAttributes(entry, entryProps, toOriginal);
                view.entries.put(entryKey, entryProps);
            }
        }
    }

    private static void readMessages(JsonObject snapshot, View view, Map<String, String> toOriginal) {
        JsonArray messages = array(snapshot, "messages");
        if (messages == null) {
            return;
        }
        for (JsonElement element : messages) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject message = element.getAsJsonObject();
            String messageKey = key(text(message, "uuid"), toOriginal);
            if (messageKey.isEmpty()) {
                continue;
            }
            Map<String, String> props = new LinkedHashMap<>();
            props.put(PARENT, key(text(message, "parentUuid"), toOriginal));
            JsonObject extended = object(message, "extendedContent");
            for (String field : MESSAGE_FIELDS) {
                if (!field.equals("summary")) {
                    props.put(field, text(extended, field));
                }
            }
            if (extended != null) {
                // what extensions (and the template variables of the generation) keep in the attributes of the part, one field per key
                extended.keySet().stream().filter(field -> field.startsWith(ATTRIBUTES)).sorted()
                        .forEach(field -> props.put(field, json(extended.get(field), toOriginal)));
            }
            JsonObject summary = object(message, "summary");
            if (summary != null) {
                JsonObject content = object(summary, "extendedContent");
                props.put("summary", String.join(SEPARATOR, text(content, "summary"), text(content, "summaryType"),
                        text(content, "summaryMessageHash"), translate(text(content, "nextSummaryUuid"), toOriginal),
                        translate(text(content, "replacedSummary"), toOriginal)));
                props.put(DISPLAY + "summary", text(content, "summary"));
            }
            view.messages.put(messageKey, props);
        }
        // what a part follows is shown as the part (its place and its start), not as a key
        for (Map<String, String> props : view.messages.values()) {
            String parent = props.get("parent");
            props.put(DISPLAY + "parent", parent == null || !view.messages.containsKey(parent) ? "" : messageLabel(view, parent));
        }
    }

    // --- comparing

    private static String norm(String value) {
        return value == null ? "" : value;
    }

    private static Type typeOf(String name) {
        if (SETS.contains(name)) {
            return Type.SET;
        }
        if (name.startsWith(ATTRIBUTES)) {
            return Type.JSON;
        }
        return name.equals("lorebook") || name.equals(PARENT) ? Type.REF : Type.TEXT;
    }

    private static String shown(Map<String, String> props, String name) {
        if (props == null) {
            return "";
        }
        String display = props.get(DISPLAY + name);
        return norm(display != null ? display : props.get(name));
    }

    private static boolean editable(String name) {
        return EDITABLE.contains(name) || name.startsWith(ATTRIBUTES);
    }

    /**
     * The field the agent changed, null when it did not or the book already has its value.
     */
    private static Field compare(String name, Map<String, String> base, Map<String, String> copy, Map<String, String> current) {
        String b = norm(base == null ? null : base.get(name));
        String c = norm(copy.get(name));
        String o = norm(current == null ? null : current.get(name));
        if (c.equals(b) || c.equals(o)) {
            return null;
        }
        Type type = typeOf(name);
        // JSON is in conflict where both changed the same place, not where both changed the data
        boolean conflict = type == Type.JSON ? JsonMerge.conflicts(b, c, o) : type != Type.SET && !o.equals(b);
        return new Field(name, type, shown(base, name), shown(copy, name), shown(current, name), conflict, editable(name));
    }

    private static List<Field> compareAll(List<String> names, Map<String, String> base, Map<String, String> copy,
                                          Map<String, String> current) {
        List<Field> fields = new ArrayList<>();
        for (String name : names) {
            Field field = compare(name, base, copy, current);
            if (field != null) {
                fields.add(field);
            }
        }
        return fields;
    }

    /**
     * The fields of an item that has only one side, the agent's (made by it) or the base's (deleted by it).
     */
    private static List<Field> oneSided(List<String> names, Map<String, String> props, boolean agent, Map<String, String> current) {
        List<Field> fields = new ArrayList<>();
        for (String name : names) {
            String value = shown(props, name);
            if (value.isEmpty() && !name.equals("response") && !name.equals("name")) {
                continue;
            }
            fields.add(agent ? new Field(name, typeOf(name), "", value, "", false, editable(name))
                    : new Field(name, typeOf(name), value, "", shown(current, name), false, false));
        }
        return fields;
    }

    private static boolean same(List<String> names, Map<String, String> a, Map<String, String> b) {
        for (String name : names) {
            if (!Objects.equals(norm(a.get(name)), norm(b == null ? null : b.get(name)))) {
                return false;
            }
        }
        return true;
    }

    private static int depth(Map<String, Map<String, String>> messages, String key) {
        int depth = 0;
        String current = key;
        Set<String> seen = new LinkedHashSet<>();
        while (current != null && !current.isEmpty() && messages.containsKey(current) && seen.add(current)) {
            depth++;
            current = messages.get(current).get("parent");
        }
        return depth;
    }

    private static String snippet(Map<String, String> props) {
        String text = norm(props.get("response")).strip().replaceAll("\\s+", " ");
        if (text.isEmpty()) {
            text = norm(props.get("instructions")).strip().replaceAll("\\s+", " ");
        }
        return text.length() > SNIPPET ? text.substring(0, SNIPPET).stripTrailing() + "…" : text;
    }

    // --- the settings of the book

    private static List<Item> bookItems(Context context) {
        Set<String> names = new LinkedHashSet<>(BOOK_FIELDS);
        names.add(TAGS);
        for (View view : List.of(context.base, context.changed, context.other)) {
            view.book.keySet().stream().filter(name -> name.startsWith(ATTRIBUTES)).sorted().forEach(names::add);
        }
        List<Item> items = new ArrayList<>();
        for (String name : names) {
            Field field = compare(name, context.base.book, context.changed.book, context.other.book);
            if (field != null) {
                items.add(new Item(ID_BOOK + name, Kind.BOOK, Change.CHANGED, null, null, name, null, List.of(field), field.conflict(), null, List.of()));
            }
        }
        return items;
    }

    // --- lorebooks and their entries

    private static List<Item> lorebookItems(Context context) {
        List<Item> changed = new ArrayList<>();
        List<Item> added = new ArrayList<>();
        List<Item> deleted = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> e : context.changed.lorebooks.entrySet()) {
            String key = e.getKey();
            Map<String, String> props = e.getValue();
            Map<String, String> base = context.base.lorebooks.get(key);
            Map<String, String> current = context.other.lorebooks.get(key);
            if (base == null) {
                if (current == null) {
                    added.add(new Item(ID_LOREBOOK + key, Kind.LOREBOOK, Change.ADDED, context.sourceUuid(key), null, props.get("name"),
                            null, oneSided(lorebookFields(props), props, true, null), false, null, List.of()));
                }
                continue;
            }
            List<Field> fields = compareAll(lorebookFields(base, props, current), base, props, current);
            if (!fields.isEmpty()) {
                changed.add(new Item(ID_LOREBOOK + key, Kind.LOREBOOK, Change.CHANGED, context.sourceUuid(key), context.targetUuid(key), props.get("name"), null,
                        fields, fields.stream().anyMatch(Field::conflict), current == null ? MergePlan.PROBLEM_GONE : null, List.of()));
            }
        }
        for (Map.Entry<String, Map<String, String>> e : context.base.lorebooks.entrySet()) {
            String key = e.getKey();
            Map<String, String> current = context.other.lorebooks.get(key);
            if (context.changed.lorebooks.containsKey(key) || current == null) {
                continue;
            }
            boolean conflict = !same(lorebookFields(e.getValue(), current), e.getValue(), current) || !entriesOf(context.base, key).equals(entriesOf(context.other, key));
            deleted.add(new Item(ID_LOREBOOK + key, Kind.LOREBOOK, Change.DELETED, null, context.targetUuid(key), e.getValue().get("name"), null,
                    oneSided(lorebookFields(e.getValue()), e.getValue(), false, current), conflict, null, List.of()));
        }
        List<Item> items = new ArrayList<>(added);
        items.addAll(changed);
        items.addAll(deleted);
        return items;
    }

    private static Set<String> entriesOf(View view, String lorebook) {
        Set<String> keys = new TreeSet<>();
        view.entries.forEach((key, props) -> {
            if (lorebook.equals(props.get("lorebook"))) {
                keys.add(key);
            }
        });
        return keys;
    }

    private static List<Item> entryItems(Context context) {
        List<Item> changed = new ArrayList<>();
        List<Item> added = new ArrayList<>();
        List<Item> deleted = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> e : context.changed.entries.entrySet()) {
            String key = e.getKey();
            Map<String, String> props = e.getValue();
            Map<String, String> base = context.base.entries.get(key);
            Map<String, String> current = context.other.entries.get(key);
            if (base == null) {
                if (current == null) {
                    String lorebook = props.get("lorebook");
                    boolean newBook = isNew(context, lorebook);
                    added.add(new Item(ID_ENTRY + key, Kind.ENTRY, Change.ADDED, context.sourceUuid(key), null, entryLabel(context, props), newBook ? ID_LOREBOOK + lorebook : null,
                            oneSided(entryFields(props), props, true, null), false,
                            newBook || context.other.lorebooks.containsKey(lorebook) ? null : MergePlan.PROBLEM_NEEDS_LOREBOOK, List.of()));
                }
                continue;
            }
            List<Field> fields = compareAll(entryFields(base, props, current), base, props, current);
            if (!fields.isEmpty()) {
                changed.add(new Item(ID_ENTRY + key, Kind.ENTRY, Change.CHANGED, context.sourceUuid(key), context.targetUuid(key), entryLabel(context, props), null,
                        fields, fields.stream().anyMatch(Field::conflict), current == null ? MergePlan.PROBLEM_GONE : null, List.of()));
            }
        }
        for (Map.Entry<String, Map<String, String>> e : context.base.entries.entrySet()) {
            String key = e.getKey();
            Map<String, String> current = context.other.entries.get(key);
            if (context.changed.entries.containsKey(key) || current == null) {
                continue;
            }
            deleted.add(new Item(ID_ENTRY + key, Kind.ENTRY, Change.DELETED, null, context.targetUuid(key), entryLabel(context, e.getValue()), null,
                    oneSided(entryFields(e.getValue()), e.getValue(), false, current), !same(entryFields(e.getValue(), current), e.getValue(), current), null, List.of()));
        }
        List<Item> items = new ArrayList<>(added);
        items.addAll(changed);
        items.addAll(deleted);
        return items;
    }

    /**
     * Whether the lorebook is new on the changed side: neither the base nor the other side has it.
     */
    private static boolean isNew(Context context, String lorebook) {
        return context.changed.lorebooks.containsKey(lorebook) && !context.base.lorebooks.containsKey(lorebook)
                && !context.other.lorebooks.containsKey(lorebook);
    }

    private static boolean isNewPart(Context context, String part) {
        return context.changed.messages.containsKey(part) && !context.base.messages.containsKey(part)
                && !context.other.messages.containsKey(part);
    }

    private static String entryLabel(Context context, Map<String, String> props) {
        return norm(context.names.get(props.get("lorebook"))) + " / " + norm(props.get("name"));
    }

    // --- parts

    private static List<Item> messageItems(Context context) {
        List<Item> changed = new ArrayList<>();
        List<Map.Entry<String, Map<String, String>>> added = new ArrayList<>();
        List<Item> deleted = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> e : context.changed.messages.entrySet()) {
            String key = e.getKey();
            Map<String, String> props = e.getValue();
            Map<String, String> base = context.base.messages.get(key);
            Map<String, String> current = context.other.messages.get(key);
            if (base == null) {
                if (current == null) {
                    added.add(e);
                }
                continue;
            }
            List<Field> fields = compareAll(messageFields(base, props, current), base, props, current);
            // a part inserted above it: the part follows the new part now, the rest of its branch with it
            String parent = props.get(PARENT);
            boolean insertedAbove = parent != null && !parent.equals(base.get(PARENT)) && isNewPart(context, parent);
            if (insertedAbove) {
                Field moved = compare(PARENT, base, props, current);
                if (moved != null) {
                    fields = new ArrayList<>(fields);
                    fields.add(moved);
                }
            }
            if (!fields.isEmpty()) {
                changed.add(new Item(ID_MESSAGE + key, Kind.MESSAGE, Change.CHANGED, context.sourceUuid(key), context.targetUuid(key),
                        messageLabel(context.changed, key), insertedAbove ? ID_MESSAGE + parent : null, fields, fields.stream().anyMatch(Field::conflict),
                        current == null ? MergePlan.PROBLEM_GONE : null, List.of()));
            }
        }
        added.sort(Comparator.comparingInt(e -> depth(context.changed.messages, e.getKey())));
        List<Item> items = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> e : added) {
            String key = e.getKey();
            String parent = e.getValue().get("parent");
            boolean newParent = isNewPart(context, parent);
            items.add(new Item(ID_MESSAGE + key, Kind.MESSAGE, Change.ADDED, context.sourceUuid(key), null, messageLabel(context.changed, key),
                    newParent ? ID_MESSAGE + parent : null, oneSided(messageFields(e.getValue()), e.getValue(), true, null), false,
                    parent.isEmpty() || newParent || context.other.messages.containsKey(parent) ? null : MergePlan.PROBLEM_NEEDS_PARENT, List.of()));
        }
        items.addAll(changed);
        for (Map.Entry<String, Map<String, String>> e : context.base.messages.entrySet()) {
            String key = e.getKey();
            Map<String, String> current = context.other.messages.get(key);
            if (context.changed.messages.containsKey(key) || current == null) {
                continue;
            }
            deleted.add(new Item(ID_MESSAGE + key, Kind.MESSAGE, Change.DELETED, null, context.targetUuid(key), messageLabel(context.base, key), null,
                    oneSided(messageFields(e.getValue()), e.getValue(), false, current), !same(messageFields(e.getValue(), current), e.getValue(), current),
                    null, List.of()));
        }
        items.addAll(deleted);
        return items;
    }

    /**
     * The fields of a part: its texts, and a field for each key of the data in its attributes that any of the given versions has.
     */
    @SafeVarargs
    private static List<String> messageFields(Map<String, String>... versions) {
        return withAttributes(MESSAGE_FIELDS, versions);
    }

    @SafeVarargs
    private static List<String> lorebookFields(Map<String, String>... versions) {
        return withAttributes(LOREBOOK_FIELDS, versions);
    }

    @SafeVarargs
    private static List<String> entryFields(Map<String, String>... versions) {
        return withAttributes(ENTRY_FIELDS, versions);
    }

    @SafeVarargs
    private static List<String> withAttributes(List<String> fixed, Map<String, String>... versions) {
        Set<String> attributes = new TreeSet<>();
        for (Map<String, String> version : versions) {
            if (version != null) {
                version.keySet().stream().filter(name -> name.startsWith(ATTRIBUTES)).forEach(attributes::add);
            }
        }
        List<String> fields = new ArrayList<>(fixed);
        fields.addAll(attributes);
        return fields;
    }

    private static String messageLabel(View view, String key) {
        return depth(view.messages, key) + ": " + snippet(view.messages.get(key));
    }

    /**
     * The keys of the items of the plan that the given one needs to be taken over first, itself last.
     */
    public static List<String> chain(MergePlan plan, Item item) {
        Map<String, Item> byId = new HashMap<>();
        plan.items().forEach(i -> byId.put(i.id(), i));
        List<String> chain = new ArrayList<>();
        Item current = item;
        while (current != null && !chain.contains(current.id())) {
            chain.add(0, current.id());
            current = current.dependsOn() == null ? null : byId.get(current.dependsOn());
        }
        return chain;
    }

    /**
     * The ids of the items that need the given one (the parts after a new part, the entries of a new lorebook), however deep.
     */
    public static Collection<String> dependents(MergePlan plan, String id) {
        Set<String> found = new LinkedHashSet<>();
        boolean grew = true;
        while (grew) {
            grew = false;
            for (Item item : plan.items()) {
                if (item.dependsOn() != null && (item.dependsOn().equals(id) || found.contains(item.dependsOn())) && found.add(item.id())) {
                    grew = true;
                }
            }
        }
        return found;
    }
}
