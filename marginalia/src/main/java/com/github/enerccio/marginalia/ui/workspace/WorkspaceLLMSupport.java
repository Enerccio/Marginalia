package com.github.enerccio.marginalia.ui.workspace;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.LLMInteraction;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * What the two workspaces tell the help assistant about themselves, see {@link LLMInteract}: the tabs by the names the
 * user reads (the keys of the parts are the same in both layouts) and the active one.
 */
public final class WorkspaceLLMSupport {

    private static final Map<String, L> LABELS = new LinkedHashMap<>();
    static {
        LABELS.put("books", L.LABEL_BOOKS);
        LABELS.put("lorebooks", L.LABEL_LOREBOOKS);
        LABELS.put("settings", L.LABEL_SETTINGS);
        LABELS.put("protocols", L.LABEL_PROTOCOLS);
        LABELS.put("models", L.LABEL_MODELS);
        LABELS.put("resources", L.LABEL_RESOURCES);
        LABELS.put("trash", L.LABEL_TRASH);
        LABELS.put("admin", L.LABEL_ADMIN);
    }

    private WorkspaceLLMSupport() {
    }

    /**
     * @param layout    {@code desktop} or {@code phone}
     * @param tabKeys   keys of the tabs the user can open, in order
     * @param activeKey key of the one that is open
     * @param locked    whether the navigation is locked (the open tab has unsaved changes)
     */
    public static String describe(Localization loc, String layout, List<String> tabKeys, String activeKey, boolean locked) {
        StringBuilder sb = new StringBuilder("The ").append(layout).append(" layout of the main window. Its sections: ")
                .append(tabKeys.stream().map(key -> label(loc, key)).collect(Collectors.joining(", ")))
                .append(". The open section is **").append(label(loc, activeKey)).append("**.");
        if (locked) {
            sb.append(" The other sections can not be opened until the unsaved changes of this one are saved or discarded.");
        }
        return sb.toString();
    }

    /**
     * The interactions of a workspace: opening each section but the open one, as {@code open-section:Label}. None while
     * the navigation is locked (a click on them does nothing then, either).
     *
     * @param sections  how to open each section, by key, in the order the user sees them
     * @param activeKey key of the one that is open
     * @param locked    whether the navigation is locked
     */
    public static Map<String, LLMInteraction> openSections(Localization loc, Map<String, Runnable> sections, String activeKey,
                                                           boolean locked) {
        Map<String, LLMInteraction> interactions = new LinkedHashMap<>();
        if (locked) {
            return interactions;
        }
        sections.forEach((key, open) -> {
            if (!key.equals(activeKey)) {
                String label = label(loc, key);
                interactions.put("open-section:" + label, new LLMInteraction("Open the section \"" + label + "\".", open));
            }
        });
        return interactions;
    }

    private static String label(Localization loc, String key) {
        L label = LABELS.get(key);
        return label == null ? String.valueOf(key) : loc.getValue(label);
    }
}
