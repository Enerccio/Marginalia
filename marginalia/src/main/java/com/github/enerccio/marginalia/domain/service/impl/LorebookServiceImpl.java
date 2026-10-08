package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.repository.LorebookRepository;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class LorebookServiceImpl extends ExtendableServiceImpl<Lorebook, LorebookRepository> implements LorebookService {
    private static final Logger log = LoggerFactory.getLogger(LorebookServiceImpl.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    public static final String EXPORT_FORMAT = "marginalia-lorebook";
    public static final int EXPORT_VERSION = 2;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private TagService tagService;

    @Override
    @CommonTxReadOnly
    public List<Lorebook> getSubbooks(Lorebook lorebook) throws Exception {
        Lorebook refresh = find(lorebook);
        List<Lorebook> subbooks = new ArrayList<>();
        for (Lorebook l : refresh.getSubbooks()) {
            Lorebook subbook = find(l);
            if (subbook != null && !subbook.isDeleted()) {
                subbooks.add(subbook);
            }
        }
        return subbooks;
    }

    @Override
    @CommonTxReadOnly
    public Lorebook fillEntries(Lorebook book) throws Exception {
        book.setCachedEntries(lorebookEntryService.getEntriesForLorebook(book));
        List<Tag> bookTags = tagRelationService.getTagsForObject(book);
        for (LorebookEntry entry : book.getCachedEntries()) {
            List<Tag> entryTags = tagRelationService.getTagsForObject(entry);
            List<Tag> entryNegativeTags = tagRelationService.getTagsForObject(entry, true);
            Set<String> positiveTags = new HashSet<>();
            for (Tag tag : bookTags) {
                positiveTags.add(tag.getValue());
            }
            for (Tag tag : entryTags) {
                positiveTags.add(tag.getValue());
            }
            entry.setCachedTags(positiveTags.stream().toList());
            entry.setCachedNegativeTags(entryNegativeTags.stream().map(Tag::getValue).toList());
        }
        return book;
    }

    @Override
    @CommonTx
    public Lorebook importFromSillytavern(String json, String name) throws Exception {
        if (StringUtils.isBlank(json)) {
            throw new IllegalArgumentException("JSON content cannot be empty");
        }
        JsonObject rootObj = gson.fromJson(json, JsonObject.class);
        if (rootObj == null) {
            throw new IllegalArgumentException("JSON content cannot be empty");
        }

        // world info name stored in the file wins over the uploaded file name
        String bookName = SillyTavernEntryConverter.string(rootObj, "name");
        if (StringUtils.isBlank(bookName)) {
            bookName = name;
        }
        if (StringUtils.isBlank(bookName)) {
            bookName = "Imported Lorebook";
        } else if (bookName.toLowerCase().endsWith(".json")) {
            bookName = bookName.substring(0, bookName.length() - 5);
        }
        bookName = bookName.trim();

        Lorebook lorebook = new Lorebook();
        lorebook.setName(bookName);
        lorebook.setEnabled(true);
        lorebook = save(lorebook);

        if (!rootObj.has("entries")) {
            return lorebook;
        }

        JsonElement entriesElement = rootObj.get("entries");
        List<JsonObject> entryObjects = new ArrayList<>();

        if (entriesElement.isJsonObject()) {
            JsonObject entriesObj = entriesElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : entriesObj.entrySet()) {
                if (entry.getValue() != null && entry.getValue().isJsonObject()) {
                    entryObjects.add(entry.getValue().getAsJsonObject());
                }
            }
        } else if (entriesElement.isJsonArray()) {
            for (JsonElement item : entriesElement.getAsJsonArray()) {
                if (item != null && item.isJsonObject()) {
                    entryObjects.add(item.getAsJsonObject());
                }
            }
        }

        // Sort by uid
        entryObjects.sort(Comparator.comparingInt(this::extractUid));

        for (JsonObject entryObj : entryObjects) {
            List<String> primaryKeys = extractStrings(entryObj, "key");
            List<String> secondaryKeys = extractStrings(entryObj, "keysecondary");

            LorebookEntry lorebookEntry = new LorebookEntry();
            lorebookEntry.setLorebook(lorebook);
            lorebookEntry.setName(SillyTavernEntryConverter.name(entryObj, primaryKeys));
            lorebookEntry.setComment(SillyTavernEntryConverter.unsupportedSettings(entryObj));
            lorebookEntry.setPayload(StringUtils.defaultString(SillyTavernEntryConverter.string(entryObj, "content")));
            lorebookEntry.setOrder(SillyTavernEntryConverter.order(entryObj));
            lorebookEntry.setEnabled(SillyTavernEntryConverter.enabled(entryObj));
            lorebookEntry.setInsertionMode(SillyTavernEntryConverter.insertionMode(entryObj));

            // trigger keys are matched against the prompt text, like SillyTavern scans the chat
            SillyTavernEntryConverter.Filter filter = SillyTavernEntryConverter.filter(entryObj, primaryKeys, secondaryKeys);
            if (filter != null) {
                lorebookEntry.setFiltering(filter.filtering());
                lorebookEntry.setFilteringMode(filter.mode());
            }

            lorebookEntry = lorebookEntryService.save(lorebookEntry);

            // character filter restricts the entry to certain characters/tags - the closest concept are book tags
            if (entryObj.has("characterFilter") && entryObj.get("characterFilter").isJsonObject()) {
                JsonObject filterObj = entryObj.getAsJsonObject("characterFilter");
                boolean isExclude = getBoolean(filterObj, "isExclude", false);

                List<String> filterTags = new ArrayList<>();
                filterTags.addAll(extractStrings(filterObj, "names"));
                filterTags.addAll(extractStrings(filterObj, "tags"));

                for (String tagStr : filterTags) {
                    Tag tag = getOrCreateTag(tagStr);
                    if (tag != null) {
                        tagRelationService.createRelation(tag, lorebookEntry, isExclude);
                    }
                }
            }
        }

        return lorebook;
    }

    @Override
    @CommonTxReadOnly
    public String exportLorebook(Lorebook lorebook) throws Exception {
        Lorebook book = find(lorebook);
        if (book == null) {
            throw new IllegalArgumentException("Lorebook not found");
        }

        JsonObject root = new JsonObject();
        root.addProperty("format", EXPORT_FORMAT);
        root.addProperty("version", EXPORT_VERSION);
        root.addProperty("root", book.getUuid());
        root.add("lorebooks", marshalLorebooks(book));
        return gson.toJson(root);
    }

    @Override
    @CommonTx
    public Lorebook importLorebook(String json, String name) throws Exception {
        if (StringUtils.isBlank(json)) {
            throw new IllegalArgumentException("JSON content cannot be empty");
        }

        JsonObject rootObj = gson.fromJson(json, JsonObject.class);
        if (rootObj == null || !EXPORT_FORMAT.equals(getString(rootObj, "format"))) {
            throw new IllegalArgumentException("Not a " + EXPORT_FORMAT + " file");
        }

        if (!rootObj.has("lorebooks")) {
            // version 1, single lorebook without subbooks
            Lorebook lorebook = new Lorebook();
            lorebook.setName(StringUtils.defaultIfBlank(getString(rootObj, "name"), StringUtils.defaultIfBlank(name, "Imported Lorebook")));
            lorebook.setEnabled(getBoolean(rootObj, "enabled", true));
            lorebook = save(lorebook);
            unmarshalContent(lorebook, rootObj);
            return lorebook;
        }

        // explicit file import always creates new lorebooks
        JsonArray lorebooks = rootObj.getAsJsonArray("lorebooks");
        Map<String, LorebookDecision> decisions = new HashMap<>();
        for (JsonElement element : lorebooks) {
            String uuid = element.isJsonObject() ? getString(element.getAsJsonObject(), "uuid") : null;
            if (uuid != null) {
                decisions.put(uuid, LorebookDecision.CREATE);
            }
        }
        Map<String, Lorebook> imported = importLorebooks(lorebooks, decisions);
        Lorebook root = imported.get(getString(rootObj, "root"));
        if (root == null) {
            throw new IllegalArgumentException("Root lorebook missing in " + EXPORT_FORMAT + " file");
        }
        return root;
    }

    @Override
    @CommonTxReadOnly
    public JsonArray marshalLorebooks(Lorebook root) throws Exception {
        JsonArray array = new JsonArray();
        Set<Long> visited = new HashSet<>();
        Deque<Lorebook> queue = new ArrayDeque<>();
        Lorebook rootBook = find(root);
        if (rootBook != null) {
            queue.add(rootBook);
        }
        while (!queue.isEmpty()) {
            Lorebook book = queue.remove();
            if (!visited.add(book.getId())) {
                continue;
            }
            List<Lorebook> subbooks = getSubbooks(book);
            JsonObject bookObj = new JsonObject();
            bookObj.addProperty("uuid", book.getUuid());
            bookObj.addProperty("name", book.getName());
            bookObj.addProperty("enabled", book.isEnabled());
            bookObj.add("tags", toJsonArray(tagRelationService.getTagsForObject(book)));
            JsonArray subbookRefs = new JsonArray();
            for (Lorebook subbook : subbooks) {
                subbookRefs.add(subbook.getUuid());
                queue.add(subbook);
            }
            bookObj.add("subbooks", subbookRefs);
            JsonArray entries = new JsonArray();
            for (LorebookEntry entry : lorebookEntryService.getEntriesForLorebook(book)) {
                entries.add(marshalEntry(entry));
            }
            bookObj.add("entries", entries);
            array.add(bookObj);
        }
        return array;
    }

    @Override
    @CommonTxReadOnly
    public List<LorebookImportCandidate> analyzeImport(JsonArray lorebooks, String rootUuid) throws Exception {
        List<LorebookImportCandidate> candidates = new ArrayList<>();
        if (lorebooks == null) {
            return candidates;
        }
        List<Lorebook> userLorebooks = findAllForUser();
        for (JsonElement element : lorebooks) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject bookObj = element.getAsJsonObject();
            LorebookImportCandidate candidate = new LorebookImportCandidate();
            candidate.setUuid(getString(bookObj, "uuid"));
            candidate.setName(getString(bookObj, "name"));
            candidate.setRoot(candidate.getUuid() != null && candidate.getUuid().equals(rootUuid));
            candidate.setEntryCount(bookObj.has("entries") && bookObj.get("entries").isJsonArray() ? bookObj.getAsJsonArray("entries").size() : 0);

            Lorebook byUuid = findForUser(candidate.getUuid());
            Lorebook byName = userLorebooks.stream()
                    .filter(l -> StringUtils.equalsIgnoreCase(StringUtils.trim(l.getName()), StringUtils.trim(candidate.getName())))
                    .findFirst().orElse(null);
            if (byUuid != null) {
                candidate.setMatch(LorebookMatch.EXISTING);
                candidate.setMatchedLorebook(byUuid);
                candidate.setDecision(LorebookDecision.LINK);
            } else if (byName != null) {
                candidate.setMatch(LorebookMatch.SAME_NAME);
                candidate.setMatchedLorebook(byName);
                candidate.setDecision(LorebookDecision.LINK);
            } else {
                candidate.setMatch(LorebookMatch.NOT_FOUND);
                candidate.setDecision(LorebookDecision.CREATE);
            }
            candidates.add(candidate);
        }
        return candidates;
    }

    @Override
    @CommonTx
    public Map<String, Lorebook> importLorebooks(JsonArray lorebooks, Map<String, LorebookDecision> decisions) throws Exception {
        Map<String, Lorebook> resolved = new HashMap<>();
        if (lorebooks == null) {
            return resolved;
        }
        Map<String, LorebookImportCandidate> candidates = new HashMap<>();
        for (LorebookImportCandidate candidate : analyzeImport(lorebooks, null)) {
            candidates.put(candidate.getUuid(), candidate);
        }

        Map<String, JsonObject> processed = new LinkedHashMap<>();
        for (JsonElement element : lorebooks) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject bookObj = element.getAsJsonObject();
            String uuid = getString(bookObj, "uuid");
            LorebookImportCandidate candidate = candidates.get(uuid);
            if (uuid == null || candidate == null || resolved.containsKey(uuid)) {
                continue;
            }
            LorebookDecision decision = decisions != null ? decisions.get(uuid) : null;
            if (candidate.getMatch() == LorebookMatch.EXISTING && decision != LorebookDecision.CREATE) {
                decision = LorebookDecision.LINK;
            }
            if (decision == null || decision == LorebookDecision.SKIP) {
                continue;
            }
            if (decision == LorebookDecision.LINK && candidate.getMatchedLorebook() != null) {
                resolved.put(uuid, candidate.getMatchedLorebook());
                processed.put(uuid, bookObj);
                continue;
            }

            Lorebook lorebook = new Lorebook();
            lorebook.setName(StringUtils.defaultIfBlank(getString(bookObj, "name"), "Imported Lorebook"));
            lorebook.setEnabled(getBoolean(bookObj, "enabled", true));
            // keep original uuid when free, so later restores link to this copy
            if (find(uuid) == null) {
                lorebook.setUuid(uuid);
            }
            lorebook = save(lorebook);
            unmarshalContent(lorebook, bookObj);
            resolved.put(uuid, lorebook);
            processed.put(uuid, bookObj);
        }

        // rebuild the backup's subbook structure between the resolved lorebooks; linked lorebooks get the missing
        // subbooks added and keep the ones they already have
        for (Map.Entry<String, JsonObject> entry : processed.entrySet()) {
            Lorebook lorebook = find(resolved.get(entry.getKey()));
            List<Lorebook> subbooks = lorebook.getSubbooks() == null ? new ArrayList<>() : new ArrayList<>(lorebook.getSubbooks());
            boolean changed = false;
            for (String subbookUuid : extractStrings(entry.getValue(), "subbooks")) {
                Lorebook subbook = resolved.get(subbookUuid);
                if (subbook != null && !subbook.getId().equals(lorebook.getId())
                        && subbooks.stream().noneMatch(l -> l.getId().equals(subbook.getId()))) {
                    subbooks.add(subbook);
                    changed = true;
                }
            }
            if (changed) {
                lorebook.setSubbooks(subbooks);
                resolved.put(entry.getKey(), save(lorebook));
            }
        }
        return resolved;
    }

    private JsonObject marshalEntry(LorebookEntry entry) throws Exception {
        JsonObject entryObj = new JsonObject();
        entryObj.addProperty("name", entry.getName());
        entryObj.addProperty("payload", entry.getPayload());
        entryObj.addProperty("comment", entry.getComment());
        entryObj.addProperty("enabled", entry.isEnabled());
        entryObj.addProperty("order", entry.getOrder());
        entryObj.addProperty("filtering", entry.getFiltering());
        entryObj.addProperty("filteringMode", entry.getFilteringMode() != null ? entry.getFilteringMode().name() : null);
        entryObj.addProperty("insertionMode", entry.getInsertionMode() != null ? entry.getInsertionMode().name() : null);
        entryObj.add("tags", toJsonArray(tagRelationService.getTagsForObject(entry)));
        entryObj.add("negativeTags", toJsonArray(tagRelationService.getTagsForObject(entry, true)));
        return entryObj;
    }

    /**
     * Imports tags and entries of marshalled lorebook into already saved lorebook.
     */
    private void unmarshalContent(Lorebook lorebook, JsonObject bookObj) throws Exception {
        for (String tagStr : extractStrings(bookObj, "tags")) {
            Tag tag = getOrCreateTag(tagStr);
            if (tag != null) {
                tagRelationService.createRelation(tag, lorebook, false);
            }
        }

        if (!bookObj.has("entries") || !bookObj.get("entries").isJsonArray()) {
            return;
        }
        for (JsonElement item : bookObj.getAsJsonArray("entries")) {
            if (item == null || !item.isJsonObject()) {
                continue;
            }
            JsonObject entryObj = item.getAsJsonObject();

            LorebookEntry entry = new LorebookEntry();
            entry.setLorebook(lorebook);
            entry.setName(StringUtils.defaultIfBlank(getString(entryObj, "name"), "Entry"));
            entry.setPayload(getString(entryObj, "payload"));
            entry.setComment(getString(entryObj, "comment"));
            entry.setEnabled(getBoolean(entryObj, "enabled", true));
            entry.setFiltering(getString(entryObj, "filtering"));

            if (entryObj.has("order") && !entryObj.get("order").isJsonNull()) {
                entry.setOrder(entryObj.get("order").getAsInt());
            }
            FilteringMode filteringMode = getEnum(entryObj, "filteringMode", FilteringMode.class);
            if (filteringMode != null) {
                entry.setFilteringMode(filteringMode);
            }
            InsertionMode insertionMode = getEnum(entryObj, "insertionMode", InsertionMode.class);
            if (insertionMode != null) {
                entry.setInsertionMode(insertionMode);
            }

            entry = lorebookEntryService.save(entry);

            for (String tagStr : extractStrings(entryObj, "tags")) {
                Tag tag = getOrCreateTag(tagStr);
                if (tag != null) {
                    tagRelationService.createRelation(tag, entry, false);
                }
            }
            for (String tagStr : extractStrings(entryObj, "negativeTags")) {
                Tag tag = getOrCreateTag(tagStr);
                if (tag != null) {
                    tagRelationService.createRelation(tag, entry, true);
                }
            }
        }
    }

    private JsonArray toJsonArray(List<Tag> tags) {
        JsonArray array = new JsonArray();
        for (Tag tag : tags) {
            array.add(tag.getValue());
        }
        return array;
    }

    private String getString(JsonObject parent, String fieldName) {
        if (parent == null || !parent.has(fieldName) || parent.get(fieldName).isJsonNull()) {
            return null;
        }
        return parent.get(fieldName).getAsString();
    }

    /**
     * Enum value of a field, {@code null} when missing or unknown (e.g. a mode added in a newer version), so the
     * entry keeps its default instead of failing the whole import.
     */
    private <E extends Enum<E>> E getEnum(JsonObject parent, String fieldName, Class<E> type) {
        String value = getString(parent, fieldName);
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.trim());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown {} '{}' in imported lorebook entry, using default", fieldName, value);
            return null;
        }
    }

    private boolean getBoolean(JsonObject parent, String fieldName, boolean defaultValue) {
        if (parent == null || !parent.has(fieldName) || parent.get(fieldName).isJsonNull()) {
            return defaultValue;
        }
        return parent.get(fieldName).getAsBoolean();
    }

    private int extractUid(JsonObject entryObj) {
        if (entryObj != null && entryObj.has("uid") && !entryObj.get("uid").isJsonNull()) {
            try {
                return entryObj.get("uid").getAsInt();
            } catch (Exception ignored) {
            }
        }
        return 0;
    }

    private List<String> extractStrings(JsonObject parent, String fieldName) {
        List<String> list = new ArrayList<>();
        if (parent == null || !parent.has(fieldName) || parent.get(fieldName).isJsonNull()) {
            return list;
        }
        JsonElement elem = parent.get(fieldName);
        if (elem.isJsonArray()) {
            for (JsonElement item : elem.getAsJsonArray()) {
                if (item != null && !item.isJsonNull() && item.isJsonPrimitive()) {
                    String str = item.getAsString();
                    if (StringUtils.isNotBlank(str)) {
                        list.add(str.trim());
                    }
                }
            }
        } else if (elem.isJsonPrimitive()) {
            String str = elem.getAsString();
            if (StringUtils.isNotBlank(str)) {
                for (String part : str.split(",")) {
                    if (StringUtils.isNotBlank(part)) {
                        list.add(part.trim());
                    }
                }
            }
        }
        return list;
    }

    private Tag getOrCreateTag(String value) throws Exception {
        return tagService.getOrCreateForUser(value);
    }
}