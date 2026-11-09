package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.combobox.ComboBox;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.*;
import java.util.stream.Stream;

/**
 * Which entries of a lorebook the list shows: the text of the search box (name, content, note or filter), a tag of the entry
 * and its status. The three are combined (all must fit); an empty one lets everything through. The owner of the list keeps
 * one instance, feeds it with the tags of the entries when it loads them ({@link #setTags}) and uses {@link #matches} as
 * the filter of its data provider.
 */
public final class LorebookEntryFilter {

    /**
     * The status of an entry the list can be limited to.
     */
    public enum Status {
        ALL(L.LABEL_ALL),
        ENABLED(L.LABEL_ENABLED),
        DISABLED(L.LABEL_FILTER_DISABLED),
        /** Used for every turn: enabled and without a filter. */
        CONSTANT(L.LABEL_FILTER_CONSTANT);

        private final L label;

        Status(L label) {
            this.label = label;
        }

        public L label() {
            return label;
        }

        boolean accepts(LorebookEntry entry) {
            return switch (this) {
                case ALL -> true;
                case ENABLED -> entry.isEnabled();
                case DISABLED -> !entry.isEnabled();
                case CONSTANT -> entry.isEnabled() && StringUtils.isBlank(entry.getFiltering());
            };
        }
    }

    private String text;
    private String tag;
    private Status status = Status.ALL;
    // the tags of the entries by id, lower case
    private Map<Long, Set<String>> tags = Map.of();

    public void setText(String text) {
        this.text = text;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setStatus(Status status) {
        this.status = status == null ? Status.ALL : status;
    }

    /**
     * Whether the filter lets everything through.
     */
    public boolean isEmpty() {
        return StringUtils.isBlank(text) && StringUtils.isBlank(tag) && status == Status.ALL;
    }

    /**
     * The tags of the entries that are listed, by the id of the entry.
     */
    public void setTags(Map<Long, Set<String>> tags) {
        this.tags = tags == null ? Map.of() : tags;
    }

    public boolean matches(LorebookEntry entry) {
        if (!status.accepts(entry)) {
            return false;
        }
        if (StringUtils.isNotBlank(tag)
                && !tags.getOrDefault(entry.getId(), Set.of()).contains(tag.strip().toLowerCase(Locale.ROOT))) {
            return false;
        }
        if (StringUtils.isBlank(text)) {
            return true;
        }
        return Stream.of(entry.getName(), entry.getPayload(), entry.getComment(), entry.getFiltering())
                .anyMatch(value -> Strings.CI.contains(value, text.strip()));
    }

    /**
     * The tags of the entries, by the id of the entry (lower case), and the names the user sees, sorted: the ones the box lists.
     */
    public static Map<Long, Set<String>> loadTags(TagRelationService tagRelationService, List<LorebookEntry> entries) throws Exception {
        List<Long> ids = entries.stream().map(LorebookEntry::getId).filter(Objects::nonNull).toList();
        Map<Long, Set<String>> result = new HashMap<>();
        for (Map.Entry<Long, List<Tag>> row : tagRelationService.getTagsForObjects(ids, LorebookEntry.class).entrySet()) {
            Set<String> values = new HashSet<>();
            row.getValue().forEach(tag -> values.add(tag.getValue().toLowerCase(Locale.ROOT)));
            result.put(row.getKey(), values);
        }
        return result;
    }

    /**
     * The tag names to pick from: every tag that at least one entry has, sorted.
     */
    public static List<String> tagNames(TagRelationService tagRelationService, List<LorebookEntry> entries) throws Exception {
        List<Long> ids = entries.stream().map(LorebookEntry::getId).filter(Objects::nonNull).toList();
        TreeSet<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (List<Tag> row : tagRelationService.getTagsForObjects(ids, LorebookEntry.class).values()) {
            row.forEach(tag -> names.add(tag.getValue()));
        }
        return new ArrayList<>(names);
    }

    /**
     * The box that picks a tag of the entries; empty = every entry.
     */
    public static ComboBox<String> createTagBox(Localization loc) {
        ComboBox<String> box = new ComboBox<>();
        box.setPlaceholder(loc.getValue(L.LABEL_TAGS));
        box.setClearButtonVisible(true);
        box.setAllowCustomValue(false);
        return box;
    }

    /**
     * The box that picks the status of the entries.
     */
    public static ComboBox<Status> createStatusBox(Localization loc) {
        ComboBox<Status> box = new ComboBox<>();
        box.setItems(Status.values());
        box.setItemLabelGenerator(status -> loc.getValue(status.label()));
        box.setValue(Status.ALL);
        box.setAllowCustomValue(false);
        box.setPlaceholder(loc.getValue(L.LABEL_STATUS));
        return box;
    }
}
