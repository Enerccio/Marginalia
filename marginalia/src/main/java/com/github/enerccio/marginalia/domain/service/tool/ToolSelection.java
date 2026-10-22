package com.github.enerccio.marginalia.domain.service.tool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * What the user allowed the model to use, per provider (by {@link #ref}). The owner of the choice (an extension's
 * settings, a saved generation) keeps it as JSON; a plain object for that. A provider that has items is chosen by having
 * items checked, one that is only on or off by {@link Entry#isEnabled}. The user's own hint replaces the default one of
 * the provider.
 */
public class ToolSelection {

    private Map<String, Entry> providers = new LinkedHashMap<>();

    /** The choice for one provider. */
    public static class Entry {

        private boolean enabled;
        private List<String> items = new ArrayList<>();
        private String hint;

        /** Whether a provider that has no items is on. */
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /** The ids of the checked items. */
        public List<String> getItems() {
            return items == null ? List.of() : items;
        }

        public void setItems(List<String> items) {
            this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
        }

        /** The hint the user wrote for this provider, null or blank for the default one. */
        public String getHint() {
            return hint;
        }

        public void setHint(String hint) {
            this.hint = hint == null || hint.isBlank() ? null : hint;
        }

        boolean isBlank() {
            return !enabled && getItems().isEmpty() && hint == null;
        }
    }

    /** The reference of the provider registered under the bundle and the key. */
    public static String ref(String bundleIdentifier, String providerKey) {
        return bundleIdentifier + "/" + providerKey;
    }

    public Map<String, Entry> getProviders() {
        return providers == null ? Map.of() : providers;
    }

    public void setProviders(Map<String, Entry> providers) {
        this.providers = providers == null ? new LinkedHashMap<>() : new LinkedHashMap<>(providers);
    }

    /** The choice for the provider, null when the user made none. */
    public Entry get(String ref) {
        return getProviders().get(ref);
    }

    /** The choice for the provider, created when there is none. */
    public Entry entry(String ref) {
        if (providers == null) {
            providers = new LinkedHashMap<>();
        }
        return providers.computeIfAbsent(ref, _ -> new Entry());
    }

    /** Checks the items of a provider, for a selection made from a list saved before this one existed. */
    public ToolSelection withItems(String ref, Collection<String> items) {
        entry(ref).setItems(items == null ? List.of() : List.copyOf(items));
        return this;
    }

    /**
     * Whether nothing is chosen (a hint written for a provider that is not on does not count).
     */
    public boolean isEmpty() {
        return getProviders().values().stream().noneMatch(e -> e.isEnabled() || !e.getItems().isEmpty());
    }

    /**
     * How many things are on among the providers that exist now: the checked items that are still offered, and the
     * providers that are only on or off and are on.
     */
    public int count(Collection<ToolProviderInfo> available) {
        int count = 0;
        for (ToolProviderInfo provider : available) {
            Entry entry = get(provider.ref());
            if (entry == null) {
                continue;
            }
            if (provider.selectable()) {
                Set<String> ids = provider.items().stream().map(ToolItem::id).collect(Collectors.toSet());
                count += (int) entry.getItems().stream().filter(ids::contains).count();
            } else if (entry.isEnabled()) {
                count++;
            }
        }
        return count;
    }

    /** A copy to edit without changing this one. */
    public ToolSelection copy() {
        ToolSelection copy = new ToolSelection();
        getProviders().forEach((ref, entry) -> {
            Entry e = copy.entry(ref);
            e.setEnabled(entry.isEnabled());
            e.setItems(entry.getItems());
            e.setHint(entry.getHint());
        });
        return copy;
    }

    /** Drops the entries that choose nothing and have no hint. */
    public ToolSelection compact() {
        if (providers != null) {
            providers.values().removeIf(Entry::isBlank);
        }
        return this;
    }

}
