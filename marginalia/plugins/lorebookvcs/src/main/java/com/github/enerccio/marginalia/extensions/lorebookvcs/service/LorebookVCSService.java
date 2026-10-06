package com.github.enerccio.marginalia.extensions.lorebookvcs.service;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.extensions.lorebookvcs.model.LoreEntryRevision;
import com.github.enerccio.marginalia.extensions.lorebookvcs.model.LoreEntryVCSData;
import com.github.enerccio.marginalia.extensions.lorebookvcs.model.LorebookVCSData;
import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

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
                return gson.fromJson(attrs.get(LorebookVCSData.KEY), LorebookVCSData.class);
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
        if (StringUtils.isBlank(value)) return null;
        String trimmed = value.trim();
        List<Tag> matches = tagService.searchTagsForUser(trimmed, 0, 10);
        Tag tag = matches.stream()
                .filter(t -> StringUtils.equalsIgnoreCase(t.getValue(), trimmed))
                .findFirst()
                .orElse(null);
        if (tag == null) {
            tag = new Tag();
            tag.setValue(trimmed);
            tag = tagService.save(tag);
        }
        return tag;
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

            rev.setPayload(extractSTValueString(data, "content", fallbackEntry.getPayload()));
            rev.setComment(extractSTValueString(data, "comment", fallbackEntry.getComment()));
            rev.setName(fallbackEntry.getName());

            if (data.has("disable") && !data.get("disable").isJsonNull()) {
                rev.setEnabled(!data.get("disable").getAsBoolean());
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

            rev.setFilteringMode(fallbackEntry.getFilteringMode());
            rev.setFiltering(fallbackEntry.getFiltering());
            rev.setInsertionMode(fallbackEntry.getInsertionMode());

            List<String> posTags = new ArrayList<>();
            posTags.addAll(extractSTStringList(data, "key"));
            posTags.addAll(extractSTStringList(data, "keysecondary"));

            boolean isExclude = data.has("character_exclusion") && data.get("character_exclusion").getAsBoolean();
            List<String> charFilter = extractSTStringList(data, "characterFilter");
            List<String> negTags = new ArrayList<>();

            if (isExclude) {
                negTags.addAll(charFilter);
            } else {
                posTags.addAll(charFilter);
            }

            rev.setPositiveTags(posTags);
            rev.setNegativeTags(negTags);
        }

        return rev;
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