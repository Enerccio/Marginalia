package com.github.enerccio.marginalia.extensions.statetracker.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.extensions.statetracker.model.Category;
import com.github.enerccio.marginalia.extensions.statetracker.model.CategoryCatalog;
import com.github.enerccio.marginalia.extensions.statetracker.model.CategoryKind;
import com.github.enerccio.marginalia.extensions.statetracker.model.StateBook;
import com.github.enerccio.marginalia.extensions.storytracker.characters.CharacterTrackerCharacters;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.github.enerccio.marginalia.extensions.statetracker.StateTrackerLocalization.*;

/**
 * Reads and saves the state of a book (in the book's attributes) and the user's categories (in the user settings).
 */
@Configurable
public class StateService {

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SettingService settingService;

    private final Gson gson = new Gson();

    /** Views that are open, by the uuid of the book, to tell them that someone else stored the state. */
    private final Map<String, Set<Runnable>> changeListeners = new ConcurrentHashMap<>();

    /**
     * The built-in categories, in the language of the user. Their ids are fixed, so a book that copied one stays linked to it
     * after the user stores the catalog.
     */
    public static List<Category> builtInCategories() {
        Category wearing = new Category("default-wearing", T.localized(WEARING), CategoryKind.DESCRIPTION, T.localized(WEARING_INSTRUCTION));
        Category weapon = new Category("default-weapon", T.localized(WEAPON_AT_HAND), CategoryKind.SINGLE, T.localized(WEAPON_INSTRUCTION));
        weapon.setShowWhenEmpty(true);
        Category carrying = new Category("default-carrying", T.localized(CARRYING), CategoryKind.LIST, T.localized(CARRYING_INSTRUCTION));
        Category injuries = new Category("default-injuries", T.localized(INJURIES), CategoryKind.LIST, T.localized(INJURIES_INSTRUCTION));
        Category mood = new Category("default-mood", T.localized(MOOD), CategoryKind.DESCRIPTION, T.localized(MOOD_INSTRUCTION));
        mood.setStrict(false);
        return List.of(wearing, weapon, carrying, injuries, mood);
    }

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

    /** Tells the views of the book that its state was stored by someone else (a command). */
    public void changed(Manuscript manuscript) {
        Set<Runnable> listeners = changeListeners.get(manuscript.getUuid());
        if (listeners != null) {
            listeners.forEach(Runnable::run);
        }
    }

    /** The state stored in {@code manuscript} as it is held, a new empty one when there is none. */
    public StateBook read(Manuscript manuscript) {
        JsonObject attrs = manuscript.getAttributes();
        StateBook state = null;
        if (attrs != null && attrs.has(StateBook.KEY)) {
            state = gson.fromJson(attrs.get(StateBook.KEY), StateBook.class);
        }
        return state != null ? state : new StateBook();
    }

    /** The state of the book as it is stored now (a running generation holds the book it was started with). */
    public StateBook load(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        return read(current != null ? current : manuscript);
    }

    /** The characters the Character Tracker plugin keeps in the book as it is stored now, see {@link CharacterTrackerCharacters}. */
    public List<CharacterTrackerCharacters.Candidate> characterTrackerCharacters(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        return CharacterTrackerCharacters.read((current != null ? current : manuscript).getAttributes());
    }

    public void save(Manuscript manuscript, StateBook state) throws Exception {
        manuscriptService.update(manuscript, current -> {
            JsonObject attrs = current.getAttributes();
            if (attrs == null) {
                attrs = new JsonObject();
                current.setAttributes(attrs);
            }
            attrs.add(StateBook.KEY, gson.toJsonTree(state));
        });
    }

    /** The user's categories as they are stored now; the built-in ones when the user never stored any. */
    public List<Category> getUserCategories() throws Exception {
        return readUserCategories(settingService.getOrCreate(UserSetting.class));
    }

    /** The categories in {@code userSetting}; the built-in ones when it has none. */
    public List<Category> readUserCategories(UserSetting userSetting) {
        return CategoryCatalog.read(userSetting.getAttributes(), StateService::builtInCategories);
    }

    /**
     * Puts the categories into {@code userSetting} without storing it (the Settings screen stores it); {@code null} removes them,
     * the built-in ones apply again.
     */
    public void putUserCategories(UserSetting userSetting, List<Category> categories) {
        JsonObject attrs = userSetting.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            userSetting.setAttributes(attrs);
        }
        if (categories == null) {
            CategoryCatalog.reset(attrs);
        } else {
            CategoryCatalog.write(attrs, categories);
        }
    }

    /** Adds a category to the user's catalog and stores it at once (from a book: Save to my categories). */
    public void addUserCategory(Category category) throws Exception {
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        List<Category> categories = readUserCategories(userSetting);
        categories.add(category);
        putUserCategories(userSetting, categories);
        settingService.save(userSetting);
    }
}
