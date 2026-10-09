package com.github.enerccio.marginalia.extensions.lorebookvcs.service;

import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.domain.service.impl.SillyTavernEntryConverter;
import com.github.enerccio.marginalia.extensions.lorebookvcs.model.LoreEntryRevision;
import com.github.enerccio.marginalia.extensions.lorebookvcs.model.LoreEntryVCSData;
import com.github.enerccio.marginalia.extensions.lorebookvcs.model.LorebookVCSData;
import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.function.Consumer;

@Configurable
public class LorebookVCSService {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private TagService tagService;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public LorebookVCSData getVCSData(Lorebook lorebook) {
        if (lorebook == null || lorebook.getId() == null) return new LorebookVCSData();
        try {
            Lorebook fresh = lorebookService.find(lorebook);
            if (fresh != null) {
                lorebook = fresh;
            }
        } catch (Exception ignored) {}

        JsonObject attrs = lorebook.getAttributes();
        if (attrs != null && attrs.has(LorebookVCSData.KEY)) {
            try {
                LorebookVCSData stored = gson.fromJson(attrs.get(LorebookVCSData.KEY), LorebookVCSData.class);
                if (stored != null) {
                    if (stored.getEntries() == null) {
                        stored.setEntries(new HashMap<>());
                    }
                    return stored;
                }
            } catch (Exception ignored) {}
        }
        LorebookVCSData data = new LorebookVCSData();
        if (lorebook.getUuid() != null) {
            data.setLorebookUuid(lorebook.getUuid());
        }
        return data;
    }

    public void saveVCSData(Lorebook lorebook, LorebookVCSData data) throws Exception {
        if (lorebook == null || lorebook.getId() == null) return;
        Lorebook fresh = lorebookService.find(lorebook);
        if (fresh == null) return;

        JsonObject attrs = fresh.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            fresh.setAttributes(attrs);
        }
        attrs.add(LorebookVCSData.KEY, gson.toJsonTree(data));
        Lorebook saved = lorebookService.save(fresh);

        // Keep in-memory lorebook reference synchronized across UI renders
        if (lorebook.getAttributes() == null) {
            lorebook.setAttributes(new JsonObject());
        }
        lorebook.getAttributes().add(LorebookVCSData.KEY, gson.toJsonTree(data));
        lorebook.setExtendedContent(saved.getExtendedContent());
    }

    /**
     * Re-reads the stored history, lets {@code change} modify the history of one entry and saves it. Only that entry's
     * part is touched, so several open entries don't overwrite each other's revisions.
     *
     * @return the entry's history as saved
     */
    public LoreEntryVCSData updateEntryData(Lorebook lorebook, String entryUuid, Consumer<LoreEntryVCSData> change) throws Exception {
        LorebookVCSData data = getVCSData(lorebook);
        LoreEntryVCSData entryData = data.getOrCreateEntry(entryUuid);
        change.accept(entryData);
        saveVCSData(lorebook, data);
        return entryData;
    }

    public boolean hasHistory(Lorebook lorebook) {
        return !getVCSData(lorebook).getEntries().isEmpty();
    }

    /**
     * Maps an imported history onto the entries of {@code lorebook}. Entries are matched by uuid first, then - for
     * histories exported from another copy of the lorebook, whose entries have different uuids - by the name and order
     * of the entry's current revision, then by the name alone when it is unique.
     *
     * @return number of imported entry histories that match no entry and were dropped
     */
    public int remapEntries(LorebookVCSData data, Lorebook lorebook) throws Exception {
        List<LorebookEntry> entries = lorebookEntryService.getEntriesForLorebook(lorebook);
        Map<String, LorebookEntry> byUuid = new HashMap<>();
        for (LorebookEntry entry : entries) {
            byUuid.put(entry.getUuid(), entry);
        }

        Map<String, LoreEntryVCSData> imported = data.getEntries() != null ? data.getEntries() : new HashMap<>();
        Map<String, LoreEntryVCSData> mapped = new HashMap<>();
        Set<String> used = new HashSet<>();
        List<LoreEntryVCSData> unmatched = new ArrayList<>();

        for (Map.Entry<String, LoreEntryVCSData> e : imported.entrySet()) {
            if (e.getValue() != null && byUuid.containsKey(e.getKey())) {
                e.getValue().setEntryUuid(e.getKey());
                mapped.put(e.getKey(), e.getValue());
                used.add(e.getKey());
            } else if (e.getValue() != null) {
                unmatched.add(e.getValue());
            }
        }

        int dropped = 0;
        for (LoreEntryVCSData entryData : unmatched) {
            LoreEntryRevision current = entryData.getCurrent();
            if (current == null && !entryData.getRevisions().isEmpty()) {
                current = entryData.getRevisions().getLast();
            }
            LorebookEntry match = null;
            if (current != null) {
                LoreEntryRevision rev = current;
                List<LorebookEntry> free = entries.stream().filter(en -> !used.contains(en.getUuid())).toList();
                match = free.stream()
                        .filter(en -> Objects.equals(StringUtils.defaultString(en.getName()), rev.getName()) && en.getOrder() == rev.getOrder())
                        .findFirst().orElse(null);
                if (match == null) {
                    List<LorebookEntry> sameName = free.stream()
                            .filter(en -> Objects.equals(StringUtils.defaultString(en.getName()), rev.getName()))
                            .toList();
                    if (sameName.size() == 1) {
                        match = sameName.getFirst();
                    }
                }
            }
            if (match == null) {
                dropped++;
                continue;
            }
            entryData.setEntryUuid(match.getUuid());
            mapped.put(match.getUuid(), entryData);
            used.add(match.getUuid());
        }

        data.setEntries(mapped);
        data.setLorebookUuid(lorebook.getUuid());
        return dropped;
    }

    public void applyRevisionToEntry(LorebookEntry entry, LoreEntryRevision revision) throws Exception {
        revision.applyTo(entry);
        lorebookEntryService.save(entry);

        // Sync positive tags
        List<Tag> currentPositive = tagRelationService.getTagsForObject(entry, false);
        for (Tag t : currentPositive) {
            tagRelationService.removeRelation(t, entry, false);
        }
        for (String tagName : revision.getPositiveTags()) {
            Tag t = getOrCreateTag(tagName);
            if (t != null) tagRelationService.createRelation(t, entry, false);
        }

        // Sync negative tags
        List<Tag> currentNegative = tagRelationService.getTagsForObject(entry, true);
        for (Tag t : currentNegative) {
            tagRelationService.removeRelation(t, entry, true);
        }
        for (String tagName : revision.getNegativeTags()) {
            Tag t = getOrCreateTag(tagName);
            if (t != null) tagRelationService.createRelation(t, entry, true);
        }
    }

    public LoreEntryRevision createSnapshot(LorebookEntry entry) throws Exception {
        List<String> posTags = tagRelationService.getTagsForObject(entry, false)
                .stream().map(Tag::getValue).toList();
        List<String> negTags = tagRelationService.getTagsForObject(entry, true)
                .stream().map(Tag::getValue).toList();
        return LoreEntryRevision.snapshotOf(entry, posTags, negTags);
    }

    private Tag getOrCreateTag(String value) throws Exception {
        return tagService.getOrCreateForUser(value);
    }

    public String exportVCSJson(Lorebook lorebook) {
        LorebookVCSData vcsData = getVCSData(lorebook);
        return gson.toJson(vcsData);
    }

    public LorebookVCSData parseVCSJson(String json) {
        return gson.fromJson(json, LorebookVCSData.class);
    }

    public LorebookVCSData parseSillyTavernVCSJson(String json, Lorebook lorebook) throws Exception {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        LorebookVCSData vcsData = new LorebookVCSData();
        vcsData.setLorebookUuid(lorebook.getUuid());

        if (root.has("entries") && root.get("entries").isJsonArray()) {
            JsonArray entriesArray = root.getAsJsonArray("entries");
            List<LorebookEntry> existingEntries = lorebookEntryService.getEntriesForLorebook(lorebook);

            for (int i = 0; i < entriesArray.size(); i++) {
                JsonObject stEntry = entriesArray.get(i).getAsJsonObject();
                String stUid = extractString(stEntry, "uid");

                LorebookEntry matchedEntry = null;
                try {
                    int uidIndex = Integer.parseInt(stUid);
                    if (uidIndex >= 0 && uidIndex < existingEntries.size()) {
                        matchedEntry = existingEntries.get(uidIndex);
                    }
                } catch (Exception ignored) {}

                if (matchedEntry == null && i < existingEntries.size()) {
                    matchedEntry = existingEntries.get(i);
                }

                if (matchedEntry == null) continue;

                LoreEntryVCSData entryVcs = new LoreEntryVCSData();
                entryVcs.setEntryUuid(matchedEntry.getUuid());

                if (stEntry.has("currentRevision") && !stEntry.get("currentRevision").isJsonNull()) {
                    entryVcs.setCurrentRevision(stEntry.get("currentRevision").getAsInt());
                }

                if (stEntry.has("revisions") && stEntry.get("revisions").isJsonArray()) {
                    JsonArray revsArray = stEntry.getAsJsonArray("revisions");
                    for (JsonElement revElem : revsArray) {
                        JsonObject stRev = revElem.getAsJsonObject();
                        LoreEntryRevision rev = parseSTRevision(stRev, matchedEntry);
                        entryVcs.getRevisions().add(rev);
                    }
                }

                if (!entryVcs.getRevisions().isEmpty()) {
                    vcsData.getEntries().put(matchedEntry.getUuid(), entryVcs);
                }
            }
        }
        return vcsData;
    }

    private LoreEntryRevision parseSTRevision(JsonObject stRev, LorebookEntry fallbackEntry) {
        LoreEntryRevision rev = new LoreEntryRevision();
        if (stRev.has("revisionId") && !stRev.get("revisionId").isJsonNull()) {
            rev.setRevisionId(stRev.get("revisionId").getAsString());
        }
        if (stRev.has("lastModified") && !stRev.get("lastModified").isJsonNull()) {
            rev.setLastModified(stRev.get("lastModified").getAsLong());
        }

        if (stRev.has("data") && stRev.get("data").isJsonObject()) {
            JsonObject data = stRev.getAsJsonObject("data");
            JsonObject flat = flattenSTData(data);

            rev.setPayload(extractSTValueString(data, "content", fallbackEntry.getPayload()));
            rev.setComment(extractSTValueString(data, "comment", fallbackEntry.getComment()));
            rev.setName(fallbackEntry.getName());

            if (flat.has("disable") && !flat.get("disable").isJsonNull()) {
                rev.setEnabled(SillyTavernEntryConverter.enabled(flat));
            } else {
                rev.setEnabled(fallbackEntry.isEnabled());
            }

            if (data.has("order")) {
                try {
                    rev.setOrder(Integer.parseInt(extractSTValueString(data, "order", String.valueOf(fallbackEntry.getOrder()))));
                } catch (Exception e) {
                    rev.setOrder(fallbackEntry.getOrder());
                }
            } else {
                rev.setOrder(fallbackEntry.getOrder());
            }

            // trigger keys become the entry's filter, as in the SillyTavern lorebook import
            if (flat.has("key") || flat.has("constant")) {
                List<String> primaryKeys = extractSTStringList(data, "key");
                List<String> secondaryKeys = extractSTStringList(data, "keysecondary");
                SillyTavernEntryConverter.Filter filter = SillyTavernEntryConverter.filter(flat, primaryKeys, secondaryKeys);
                if (filter != null) {
                    rev.setFiltering(filter.filtering());
                    rev.setFilteringMode(filter.mode());
                } else {
                    rev.setFiltering("");
                    rev.setFilteringMode(FilteringMode.TEXT);
                }
            } else {
                rev.setFiltering(fallbackEntry.getFiltering());
                rev.setFilteringMode(fallbackEntry.getFilteringMode());
            }
            rev.setInsertionMode(flat.has("position") ? SillyTavernEntryConverter.insertionMode(flat) : fallbackEntry.getInsertionMode());

            // character filter restricts the entry to certain characters/tags - the closest concept are tags
            List<String> posTags = new ArrayList<>();
            List<String> negTags = new ArrayList<>();
            JsonElement charFilterElem = flat.get("characterFilter");
            if (charFilterElem != null && charFilterElem.isJsonObject()) {
                JsonObject charFilter = charFilterElem.getAsJsonObject();
                boolean isExclude = charFilter.has("isExclude") && !charFilter.get("isExclude").isJsonNull()
                        && charFilter.get("isExclude").getAsBoolean();
                List<String> filterTags = new ArrayList<>();
                filterTags.addAll(extractSTStringList(charFilter, "names"));
                filterTags.addAll(extractSTStringList(charFilter, "tags"));
                (isExclude ? negTags : posTags).addAll(filterTags);
            } else {
                boolean isExclude = flat.has("character_exclusion") && !flat.get("character_exclusion").isJsonNull()
                        && flat.get("character_exclusion").getAsBoolean();
                (isExclude ? negTags : posTags).addAll(extractSTStringList(data, "characterFilter"));
            }

            rev.setPositiveTags(posTags);
            rev.setNegativeTags(negTags);
        }

        return rev;
    }

    /**
     * Revision data wraps the values as {@code {"value": ...}}, the SillyTavern entry converter expects them plain.
     */
    private JsonObject flattenSTData(JsonObject data) {
        JsonObject flat = new JsonObject();
        for (Map.Entry<String, JsonElement> e : data.entrySet()) {
            JsonElement value = e.getValue();
            if (value != null && value.isJsonObject() && value.getAsJsonObject().has("value")) {
                value = value.getAsJsonObject().get("value");
            }
            flat.add(e.getKey(), value);
        }
        return flat;
    }

    private String extractSTValueString(JsonObject data, String fieldName, String fallback) {
        if (!data.has(fieldName) || data.get(fieldName).isJsonNull()) return fallback;
        JsonElement elem = data.get(fieldName);
        if (elem.isJsonObject()) {
            JsonObject obj = elem.getAsJsonObject();
            if (obj.has("value") && !obj.get("value").isJsonNull()) {
                return obj.get("value").getAsString();
            }
        } else if (elem.isJsonPrimitive()) {
            return elem.getAsString();
        }
        return fallback;
    }

    private List<String> extractSTStringList(JsonObject data, String fieldName) {
        List<String> result = new ArrayList<>();
        if (!data.has(fieldName) || data.get(fieldName).isJsonNull()) return result;
        JsonElement elem = data.get(fieldName);
        if (elem.isJsonObject()) {
            JsonObject obj = elem.getAsJsonObject();
            if (obj.has("value")) {
                elem = obj.get("value");
            }
        }

        if (elem.isJsonArray()) {
            for (JsonElement item : elem.getAsJsonArray()) {
                if (item != null && !item.isJsonNull() && item.isJsonPrimitive()) {
                    result.add(item.getAsString().trim());
                }
            }
        } else if (elem.isJsonPrimitive()) {
            String str = elem.getAsString();
            if (StringUtils.isNotBlank(str)) {
                for (String s : str.split(",")) {
                    if (StringUtils.isNotBlank(s)) {
                        result.add(s.trim());
                    }
                }
            }
        }
        return result;
    }

    private String extractString(JsonObject obj, String prop) {
        if (obj.has(prop) && !obj.get(prop).isJsonNull()) {
            return obj.get(prop).getAsString();
        }
        return "";
    }
}