package com.github.enerccio.marginalia.ui.shortcuts;

import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.Component;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Something the user can do with a key in the story editor. The application's own actions have no handler (the story tab
 * knows what they do), the ones of extensions bring theirs. All of them show up in the key bindings of the user settings.
 */
public final class ShortcutAction {

    private final String id;
    private final Function<Localization, String> label;
    private final String defaultKey;
    private final Consumer<Component> handler;

    /**
     * @param id         unique id, extensions prefix it with their name (for example {@code myplugin.summarize})
     * @param label      the name in the key bindings table, given the localization of the user's session
     * @param defaultKey the canonical key combination (see {@link KeyCombo}) the action has until the user changes it, null for none
     * @param handler    runs on the key, given the component of the story tab; null for the actions the application handles itself
     */
    public ShortcutAction(String id, Function<Localization, String> label, String defaultKey, Consumer<Component> handler) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id is required");
        }
        String normalized = defaultKey == null ? null : KeyCombo.normalize(defaultKey);
        if (defaultKey != null && normalized == null) {
            throw new IllegalArgumentException("not a key combination: " + defaultKey);
        }
        this.id = id;
        this.label = label;
        this.defaultKey = normalized;
        this.handler = handler;
    }

    public String getId() {
        return id;
    }

    public String getLabel(Localization loc) {
        return label.apply(loc);
    }

    /**
     * The canonical key combination, null for none.
     */
    public String getDefaultKey() {
        return defaultKey;
    }

    public Consumer<Component> getHandler() {
        return handler;
    }
}
