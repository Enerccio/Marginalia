package com.github.enerccio.marginalia.extensions.knowledgetracker.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Reads and saves the state of a book (in the book's attributes) and the separators of the user (in the user settings).
 */
@Configurable
public class KnowledgeStateService {

    /** Key of the separators in the attributes of the user settings. */
    static final String USER_SEPARATORS_KEY = KnowledgeState.KEY + ".separators";

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SettingService settingService;

    private final Gson gson = new Gson();

    /** Views that are open, by the uuid of the book, to tell them that a command changed the state. */
    private final Map<String, Set<Runnable>> changeListeners = new ConcurrentHashMap<>();

    /**
     * Calls {@code listener} when {@link #changed} says the state of the book was stored by someone else than the view.
     *
     * @return what removes the listener
     */
    public Runnable onChange(Manuscript manuscript, Runnable listener) {
        String key = manuscript.getUuid();
        changeListeners.computeIfAbsent(key, _ -> new CopyOnWriteArraySet<>()).add(listener);
        return () -> changeListeners.computeIfPresent(key, (_, set) -> {
            set.remove(listener);
            return set.isEmpty() ? null : set;
        });
    }

    /**
     * Tells the views of the book that its state was stored by someone else than the view (a command).
     */
    public void changed(Manuscript manuscript) {
        Set<Runnable> listeners = changeListeners.get(manuscript.getUuid());
        if (listeners != null) {
            listeners.forEach(Runnable::run);
        }
    }

    /**
     * The state stored in {@code manuscript} as it is held, a new empty one when there is none.
     */
    public KnowledgeState read(Manuscript manuscript) {
        JsonObject attrs = manuscript.getAttributes();
        KnowledgeState state = null;
        if (attrs != null && attrs.has(KnowledgeState.KEY)) {
            state = gson.fromJson(attrs.get(KnowledgeState.KEY), KnowledgeState.class);
        }
        return state != null ? state : new KnowledgeState();
    }

    /**
     * The state of the book as it is stored now: a running generation holds the book it was started with, the state
     * may have been edited since.
     */
    public KnowledgeState load(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        return read(current != null ? current : manuscript);
    }

    public void save(Manuscript manuscript, KnowledgeState state) throws Exception {
        manuscript = manuscriptService.find(manuscript);
        JsonObject attrs = manuscript.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            manuscript.setAttributes(attrs);
        }
        attrs.add(KnowledgeState.KEY, gson.toJsonTree(state));
        manuscriptService.save(manuscript);
    }

    /**
     * The separators of the user, empty when they have none (the built-in ones are used then).
     */
    public List<String> getUserSeparators() throws Exception {
        JsonObject attrs = settingService.getOrCreate(UserSetting.class).getAttributes();
        List<String> separators = new ArrayList<>();
        if (attrs != null && attrs.has(USER_SEPARATORS_KEY) && attrs.get(USER_SEPARATORS_KEY).isJsonArray()) {
            for (JsonElement element : attrs.getAsJsonArray(USER_SEPARATORS_KEY)) {
                if (element.isJsonPrimitive()) {
                    separators.add(element.getAsString());
                }
            }
        }
        return separators;
    }

    public void saveUserSeparators(List<String> separators) throws Exception {
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        JsonObject attrs = userSetting.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            userSetting.setAttributes(attrs);
        }
        JsonArray array = new JsonArray();
        separators.forEach(array::add);
        attrs.add(USER_SEPARATORS_KEY, array);
        settingService.save(userSetting);
    }
}
