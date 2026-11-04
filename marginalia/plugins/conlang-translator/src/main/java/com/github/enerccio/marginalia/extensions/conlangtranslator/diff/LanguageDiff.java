package com.github.enerccio.marginalia.extensions.conlangtranslator.diff;

import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.Items.Access;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.LexiconEntry;

import java.util.*;

/**
 * Compares two languages and writes the picked differences into a third (the language as it is now): the one engine behind an import that meets
 * an existing language, the comparison of a language with its copy, the proposals of the designer and the review of an agent's work.
 * <p>
 * {@link #diff(Language, Language, Language)} is a <b>three-way</b> merge: {@code base} is what both languages came from, {@code incoming}
 * the language whose changes are offered, {@code current} the language they would be written into. A field that only the incoming language
 * changed is a change; one that both changed to the same value is nothing; one that both changed differently is a <b>conflict</b>, which keeps our
 * value unless the user takes theirs. Things are the same by their <b>id</b> (a copy keeps the ids), and a word that is new in the incoming
 * language but is already here under another id (the same form and a shared meaning) is a conflict, found by the word itself.
 * Without a base ({@link #compare}), the base is the current language: every difference is a change of the incoming one.
 * <p>
 * The change set is never applied by the diff: {@link #apply} writes what the user picked, and skips a change whose fields are not what the
 * set saw (the language was edited after the review opened).
 */
public final class LanguageDiff {

    private LanguageDiff() {
    }

    /**
     * What applying a change set did.
     *
     * @param applied the changes that were written
     * @param stale   the picked changes that were skipped because the language is not what the set saw (or the thing is not there any more)
     * @param unmet   the picked changes that were skipped because what they depend on was not picked
     */
    public record Result(List<Change> applied, List<Change> stale, List<Change> unmet) {
    }

    /**
     * The changes that would make {@code current} like {@code incoming}, with no base: every difference is the incoming language's.
     */
    public static ChangeSet compare(Language current, Language incoming) {
        ChangeSet set = diff(current, incoming, current);
        set.setTwoWay(true);
        return set;
    }

    /**
     * The three-way difference; none of the languages is changed.
     */
    public static ChangeSet diff(Language base, Language incoming, Language current) {
        ChangeSet set = new ChangeSet(incoming, current);
        for (Target target : Target.values()) {
            diffTarget(Items.of(target), base, incoming, current, set);
        }
        findDependencies(set, incoming, current);
        return set;
    }

    // ---- making the set

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void diffTarget(Access access, Language base, Language incoming, Language current, ChangeSet set) {
        List<Object> baseItems = access.items(base);
        List<Object> incomingItems = access.items(incoming);
        List<Object> currentItems = access.items(current);
        Map<String, Object> baseById = byId(access, baseItems);
        Map<String, Object> currentById = byId(access, currentItems);
        Set<String> ids = new LinkedHashSet<>();
        incomingItems.forEach(item -> ids.add(access.id(item)));
        baseItems.forEach(item -> ids.add(access.id(item)));
        Map<String, Object> incomingById = byId(access, incomingItems);

        for (String id : ids) {
            Object in = incomingById.get(id);
            Object was = baseById.get(id);
            Object here = currentById.get(id);
            if (in != null) {
                if (was == null) {
                    if (here == null) {
                        addChange(access, in, incomingItems, currentItems, set);
                        continue;
                    }
                    // added on both sides under one id: no base, so what differs is theirs
                    was = here;
                }
                if (here == null) {
                    if (!sameContent(access, was, in)) {
                        Change change = new Change(access.target, Change.Kind.CHANGE, id, access.label(in));
                        change.setPayload(access.copy(in));
                        change.setAnchor(anchorOf(access, incomingItems, id));
                        change.setConflict(Conflict.DELETED_HERE_CHANGED_THERE);
                        set.add(change);
                    }
                    continue;
                }
                changeFields(access, was, in, here, set);
            } else if (was != null && here != null) {
                Change change = new Change(access.target, Change.Kind.DELETE, id, access.label(here));
                if (!sameContent(access, was, here)) {
                    change.setConflict(Conflict.CHANGED_HERE_DELETED_THERE);
                }
                set.add(change);
            }
        }
        if (access.ordered()) {
            moves(access, baseItems, incomingItems, currentItems, set);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addChange(Access access, Object in, List<Object> incomingItems, List<Object> currentItems, ChangeSet set) {
        Change change = new Change(access.target, Change.Kind.ADD, access.id(in), access.label(in));
        change.setPayload(access.copy(in));
        change.setAnchor(anchorOf(access, incomingItems, access.id(in)));
        Map<String, String> text = access.text(in);
        text.forEach((field, value) -> {
            if (!value.isEmpty()) {
                change.fields().add(new FieldChange(field, null, value, "", value, false, false));
            }
        });
        ((Map<String, List<String>>) access.lists(in)).forEach((field, values) -> {
            if (!values.isEmpty()) {
                String joined = String.join("\n", values);
                change.fields().add(new FieldChange(field, null, joined, "", joined, true, false));
            }
        });
        if (access.target == Target.WORD && duplicateOfWord((LexiconEntry) in, currentItems)) {
            change.setConflict(Conflict.SAME_WORD_EXISTS);
        }
        set.add(change);
    }

    /** The same form and a shared meaning, under another id. */
    private static boolean duplicateOfWord(LexiconEntry word, List<?> currentItems) {
        for (Object other : currentItems) {
            LexiconEntry entry = (LexiconEntry) other;
            if (!entry.getId().equals(word.getId()) && entry.isForm(word.getWord(), false) && word.getSenses().stream()
                    .anyMatch(sense -> entry.hasGloss(sense.getGloss()))) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void changeFields(Access access, Object was, Object in, Object here, ChangeSet set) {
        Change change = new Change(access.target, Change.Kind.CHANGE, access.id(here), access.label(here));
        Map<String, String> baseText = access.text(was);
        Map<String, String> inText = access.text(in);
        Map<String, String> hereText = access.text(here);
        for (String field : inText.keySet()) {
            String b = baseText.getOrDefault(field, "");
            String t = inText.get(field);
            String c = hereText.getOrDefault(field, "");
            if (t.equals(b) || t.equals(c)) {
                continue;
            }
            boolean conflict = !c.equals(b);
            change.fields().add(new FieldChange(field, b, t, c, conflict ? c : t, false, conflict));
        }
        Map<String, List<String>> baseLists = access.lists(was);
        Map<String, List<String>> inLists = access.lists(in);
        Map<String, List<String>> hereLists = access.lists(here);
        for (String field : inLists.keySet()) {
            List<String> b = baseLists.getOrDefault(field, List.of());
            List<String> t = inLists.get(field);
            List<String> c = hereLists.getOrDefault(field, List.of());
            ListMerge merged = mergeList(b, t, c, element -> access.elementKey(field, element));
            if (merged.values.equals(c) && !merged.conflict) {
                continue;
            }
            change.fields().add(new FieldChange(field, String.join("\n", b), String.join("\n", t), String.join("\n", c),
                    String.join("\n", merged.values), true, merged.conflict));
        }
        if (!change.fields().isEmpty()) {
            set.add(change);
        }
    }

    private record ListMerge(List<String> values, boolean conflict) {
    }

    /** The elements of a list field merged as a set: what the incoming list added or removed is added or removed here. */
    private static ListMerge mergeList(List<String> base, List<String> incoming, List<String> current, java.util.function.UnaryOperator<String> key) {
        Map<String, String> b = keyed(base, key);
        Map<String, String> t = keyed(incoming, key);
        Map<String, String> result = keyed(current, key);
        Map<String, String> c = keyed(current, key);
        boolean conflict = false;
        for (Map.Entry<String, String> element : t.entrySet()) {
            String name = element.getKey();
            if (!b.containsKey(name)) {
                if (!c.containsKey(name)) {
                    result.put(name, element.getValue());
                } else if (!c.get(name).equals(element.getValue())) {
                    conflict = true;
                }
            } else if (!b.get(name).equals(element.getValue())) {
                if (!c.containsKey(name)) {
                    conflict = true;
                } else if (c.get(name).equals(b.get(name))) {
                    result.put(name, element.getValue());
                } else if (!c.get(name).equals(element.getValue())) {
                    conflict = true;
                }
            }
        }
        for (Map.Entry<String, String> element : b.entrySet()) {
            String name = element.getKey();
            if (!t.containsKey(name) && c.containsKey(name)) {
                if (c.get(name).equals(element.getValue())) {
                    result.remove(name);
                } else {
                    conflict = true;
                }
            }
        }
        return new ListMerge(new ArrayList<>(result.values()), conflict);
    }

    private static Map<String, String> keyed(List<String> elements, java.util.function.UnaryOperator<String> key) {
        Map<String, String> map = new LinkedHashMap<>();
        elements.forEach(element -> map.putIfAbsent(key.apply(element), element));
        return map;
    }

    /** The order of the things the languages share: a thing that is not in the longest common run of two orders was moved. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void moves(Access access, List<Object> baseItems, List<Object> incomingItems, List<Object> currentItems, ChangeSet set) {
        List<String> baseIds = ids(access, baseItems);
        List<String> incomingIds = ids(access, incomingItems);
        List<String> currentIds = ids(access, currentItems);
        List<String> common = baseIds.stream().filter(incomingIds::contains).toList();
        List<String> baseOrder = baseIds.stream().filter(common::contains).toList();
        List<String> incomingOrder = incomingIds.stream().filter(common::contains).toList();
        Set<String> movedThere = new LinkedHashSet<>(incomingOrder);
        movedThere.removeAll(longestCommon(baseOrder, incomingOrder));
        if (movedThere.isEmpty()) {
            return;
        }
        List<String> hereOrder = currentIds.stream().filter(common::contains).toList();
        Set<String> movedHere = new LinkedHashSet<>(hereOrder);
        movedHere.removeAll(longestCommon(baseOrder, hereOrder));
        Map<String, Object> currentById = byId(access, currentItems);
        for (String id : movedThere) {
            Object here = currentById.get(id);
            if (here == null) {
                continue;
            }
            Change change = new Change(access.target, Change.Kind.MOVE, id, access.label(here));
            change.setAnchor(anchorOf(access, incomingItems, id));
            if (movedHere.contains(id)) {
                change.setConflict(Conflict.BOTH_MOVED);
            }
            set.add(change);
        }
    }

    /** The elements of the longest common subsequence (the elements that stayed where they were). */
    private static Set<String> longestCommon(List<String> a, List<String> b) {
        int[][] length = new int[a.size() + 1][b.size() + 1];
        for (int i = a.size() - 1; i >= 0; i--) {
            for (int j = b.size() - 1; j >= 0; j--) {
                length[i][j] = a.get(i).equals(b.get(j)) ? length[i + 1][j + 1] + 1 : Math.max(length[i + 1][j], length[i][j + 1]);
            }
        }
        Set<String> kept = new LinkedHashSet<>();
        int i = 0;
        int j = 0;
        while (i < a.size() && j < b.size()) {
            if (a.get(i).equals(b.get(j))) {
                kept.add(a.get(i));
                i++;
                j++;
            } else if (length[i + 1][j] >= length[i][j + 1]) {
                i++;
            } else {
                j++;
            }
        }
        return kept;
    }

    /** A new word that uses a new category or register depends on it. */
    private static void findDependencies(ChangeSet set, Language incoming, Language current) {
        for (Change change : set.changes()) {
            if (change.target() != Target.WORD || change.kind() == Change.Kind.DELETE || change.kind() == Change.Kind.MOVE) {
                continue;
            }
            for (FieldChange field : change.fields()) {
                if (field.value().isEmpty()) {
                    continue;
                }
                if ("category".equals(field.field()) && current.findCategory(field.value()) == null && incoming.findCategory(field.value()) != null) {
                    change.dependsOn().add(Target.CATEGORY + ":" + field.value());
                }
                if ("register".equals(field.field()) && current.findRegister(field.value()) == null && incoming.findRegister(field.value()) != null) {
                    change.dependsOn().add(Target.REGISTER + ":" + field.value());
                }
            }
        }
    }

    // ---- applying

    /**
     * Writes the picked changes into {@code current}. Adds, changes and moves go first (in the order of {@link Target}, so a category exists before the
     * words that use it), deletes last. A change is skipped, and reported, when the thing is not as the set saw it, or when what it depends on is not
     * picked. A conflicting field is written with the value it holds ({@link FieldChange#value()}), which is our own unless the user took theirs.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Result apply(Language current, ChangeSet set) {
        List<Change> applied = new ArrayList<>();
        List<Change> stale = new ArrayList<>();
        List<Change> unmet = new ArrayList<>();
        List<Change> picked = set.changes().stream().filter(Change::isPicked).toList();
        Set<String> pickedThings = new HashSet<>();
        picked.forEach(change -> pickedThings.add(change.thing()));
        for (boolean deletes : new boolean[]{false, true}) {
            for (Target target : Target.values()) {
                Access access = Items.of(target);
                for (Change change : picked) {
                    if (change.target() != target || (change.kind() == Change.Kind.DELETE) != deletes) {
                        continue;
                    }
                    if (!pickedThings.containsAll(change.dependsOn().stream().filter(thing -> !existsIn(current, thing)).toList())) {
                        unmet.add(change);
                    } else if (applyOne(access, current, change)) {
                        applied.add(change);
                    } else {
                        stale.add(change);
                    }
                }
            }
        }
        return new Result(applied, stale, unmet);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean existsIn(Language language, String thing) {
        int cut = thing.indexOf(':');
        Access access = Items.of(Target.valueOf(thing.substring(0, cut)));
        String id = thing.substring(cut + 1);
        return byId(access, access.items(language)).containsKey(id);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean applyOne(Access access, Language current, Change change) {
        List<Object> items = access.items(current);
        Object here = byId(access, items).get(change.id());
        switch (change.kind()) {
            case ADD -> {
                if (here != null) {
                    return false;
                }
                access.insert(current, access.copy(change.payload()), change.anchor());
                writeFields(access, byId(access, access.items(current)).get(change.id()), change, false);
                return true;
            }
            case DELETE -> {
                if (here == null) {
                    return false;
                }
                access.remove(current, here);
                return true;
            }
            case MOVE -> {
                if (here == null) {
                    return false;
                }
                items.remove(here);
                access.insert(current, here, change.anchor());
                return true;
            }
            default -> {
                if (here == null) {
                    if (change.conflict() == Conflict.DELETED_HERE_CHANGED_THERE && change.payload() != null) {
                        access.insert(current, access.copy(change.payload()), change.anchor());
                        return true;
                    }
                    return false;
                }
                if (!isAsSeen(access, here, change)) {
                    return false;
                }
                writeFields(access, here, change, true);
                return true;
            }
        }
    }

    /** Whether every field of the change is still what the set saw. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean isAsSeen(Access access, Object here, Change change) {
        Map<String, String> text = access.text(here);
        Map<String, List<String>> lists = access.lists(here);
        for (FieldChange field : change.fields()) {
            String live = field.isList() ? String.join("\n", lists.getOrDefault(field.field(), List.of())) : text.getOrDefault(field.field(), "");
            if (!live.equals(field.now())) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void writeFields(Access access, Object item, Change change, boolean onlyChanging) {
        for (FieldChange field : change.fields()) {
            if (onlyChanging && !field.changes()) {
                continue;
            }
            if (field.isList()) {
                access.setList(item, field.field(), field.value().isEmpty() ? List.of() : Arrays.asList(field.value().split("\n", -1)));
            } else {
                access.setText(item, field.field(), field.value());
            }
        }
    }

    // ---- helpers

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, Object> byId(Access access, List<Object> items) {
        Map<String, Object> map = new LinkedHashMap<>();
        items.forEach(item -> map.putIfAbsent(access.id(item), item));
        return map;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<String> ids(Access access, List<Object> items) {
        return items.stream().map(item -> access.id(item)).map(String.class::cast).toList();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String anchorOf(Access access, List<Object> items, String id) {
        String previous = null;
        for (Object item : items) {
            if (access.id(item).equals(id)) {
                return previous;
            }
            previous = (String) access.id(item);
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean sameContent(Access access, Object a, Object b) {
        return access.text(a).equals(access.text(b)) && access.lists(a).equals(access.lists(b));
    }
}
