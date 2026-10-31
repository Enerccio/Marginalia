package com.github.enerccio.marginalia.extensions;

import java.util.*;

/**
 * The settings that extensions add to the books and the folders ({@link InheritedSetting}). An extension registers them when it
 * is loaded and unregisters them in {@code onExtensionUnload} (the settings of a bundle that is unloaded are removed by its name
 * anyway). The settings dialogs of a book and of a folder are built from the settings that are registered when they open; the
 * values stay with the books and the folders while an extension is not loaded and come back with it.
 */
public class InheritedSettingRegistry {

    /**
     * The settings of one extension, as the dialogs show them: under one title.
     *
     * @param owner    who registered them
     * @param title    the title of the group
     * @param settings the settings in the order they were registered
     */
    public record Group(String owner, String title, List<InheritedSetting> settings) {
    }

    private final Map<String, InheritedSetting> settings = new LinkedHashMap<>();
    private final Map<String, String> owners = new HashMap<>();

    /**
     * Adds the setting (one with the same key is replaced).
     *
     * @param owner who registers it, the symbolic name of the bundle of an extension; see {@link #unregisterAll}
     */
    public synchronized void register(String owner, InheritedSetting setting) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(setting, "setting");
        // a replaced setting goes to the end, as a new one
        settings.remove(setting.getKey());
        settings.put(setting.getKey(), setting);
        owners.put(setting.getKey(), owner);
    }

    public synchronized void unregister(String key) {
        settings.remove(key);
        owners.remove(key);
    }

    /**
     * Removes the settings an owner registered.
     */
    public synchronized void unregisterAll(String owner) {
        new ArrayList<>(owners.entrySet()).stream().filter(entry -> Objects.equals(owner, entry.getValue()))
                .map(Map.Entry::getKey).forEach(this::unregister);
    }

    /**
     * The setting with the key, null when none is registered (the extension is not loaded).
     */
    public synchronized InheritedSetting find(String key) {
        return settings.get(key);
    }

    /**
     * The settings grouped by the extension that registered them, in the order of the first setting of each.
     */
    public synchronized List<Group> groups() {
        Map<String, List<InheritedSetting>> byOwner = new LinkedHashMap<>();
        settings.forEach((key, setting) -> byOwner.computeIfAbsent(owners.get(key), _ -> new ArrayList<>()).add(setting));
        List<Group> groups = new ArrayList<>();
        byOwner.forEach((owner, own) -> groups.add(new Group(owner, titleOf(owner, own), List.copyOf(own))));
        return groups;
    }

    private static String titleOf(String owner, List<InheritedSetting> own) {
        return own.stream().map(InheritedSetting::group).filter(Objects::nonNull).filter(title -> !title.isBlank())
                .findFirst().orElse(owner);
    }
}
