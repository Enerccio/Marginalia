package com.github.enerccio.marginalia.domain.service.impl;

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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class LorebookServiceImpl extends ExtendableServiceImpl<Lorebook, LorebookRepository> implements LorebookService {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

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
            subbooks.add(find(l));
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

        String bookName = name;
        if (StringUtils.isBlank(bookName)) {
            bookName = "Imported Lorebook";
        } else if (bookName.toLowerCase().endsWith(".json")) {
            bookName = bookName.substring(0, bookName.length() - 5);
        }

        Lorebook lorebook = new Lorebook();
        lorebook.setName(bookName);
        lorebook.setEnabled(true);
        lorebook = save(lorebook);

        JsonObject rootObj = gson.fromJson(json, JsonObject.class);
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
            LorebookEntry lorebookEntry = new LorebookEntry();
            lorebookEntry.setLorebook(lorebook);

            // Map comment -> name
            String comment = entryObj.has("comment") && !entryObj.get("comment").isJsonNull()
                    ? entryObj.get("comment").getAsString() : "";
            lorebookEntry.setName(StringUtils.defaultIfBlank(comment, "Entry"));
            lorebookEntry.setComment(comment);

            // Map content -> content (payload)
            String content = entryObj.has("content") && !entryObj.get("content").isJsonNull()
                    ? entryObj.get("content").getAsString() : "";
            lorebookEntry.setPayload(content);

            // Map order -> order
            int order = entryObj.has("order") && !entryObj.get("order").isJsonNull()
                    ? entryObj.get("order").getAsInt() : 100;
            lorebookEntry.setOrder(order);

            // Map disable -> enabled
            boolean disable = entryObj.has("disable") && !entryObj.get("disable").isJsonNull()
                    && entryObj.get("disable").getAsBoolean();
            lorebookEntry.setEnabled(!disable);

            lorebookEntry = lorebookEntryService.save(lorebookEntry);

            // Process trigger keys as positive/include tags
            List<String> positiveTags = new ArrayList<>();
            positiveTags.addAll(extractStrings(entryObj, "key"));
            positiveTags.addAll(extractStrings(entryObj, "keysecondary"));

            for (String tagStr : positiveTags) {
                Tag tag = getOrCreateTag(tagStr);
                if (tag != null) {
                    tagRelationService.createRelation(tag, lorebookEntry, false);
                }
            }

            // Process characterFilter as positive or negative tags depending on isExclude
            if (entryObj.has("characterFilter") && entryObj.get("characterFilter").isJsonObject()) {
                JsonObject filterObj = entryObj.getAsJsonObject("characterFilter");
                boolean isExclude = filterObj.has("isExclude") && !filterObj.get("isExclude").isJsonNull()
                        && filterObj.get("isExclude").getAsBoolean();

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
        if (StringUtils.isBlank(value)) {
            return null;
        }
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
}