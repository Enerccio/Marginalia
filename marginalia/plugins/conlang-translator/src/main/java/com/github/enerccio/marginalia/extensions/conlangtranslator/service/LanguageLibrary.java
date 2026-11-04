package com.github.enerccio.marginalia.extensions.conlangtranslator.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.UserDefaults;
import com.github.enerccio.marginalia.extensions.conlangtranslator.store.ConlangStore;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.Set;

/**
 * The library of languages of the user and the user level of the inherited settings, as the panel in the Settings screen and the
 * dialogs of books and folders need them. The data is in the attributes of the user's settings ({@link ConlangStore}); the Settings screen
 * stores its own entity, so the writers here put into the entity they are given and do not save.
 */
@Configurable
public class LanguageLibrary {

    @Autowired
    private SettingService settingService;
    @Autowired
    private AIService aiService;
    @Autowired
    private ProtocolService protocolService;
    @Autowired
    private ManuscriptService manuscriptService;
    @Autowired
    private LorebookService lorebookService;

    /**
     * The languages as they are stored now, each an independent object.
     */
    public List<Language> languages() throws Exception {
        return ConlangStore.readLibrary(settingService.getOrCreate(UserSetting.class).getAttributes());
    }

    /**
     * The user level of the inherited settings, as stored now.
     */
    public UserDefaults defaults() throws Exception {
        return ConlangStore.readDefaults(settingService.getOrCreate(UserSetting.class).getAttributes());
    }

    /**
     * Puts what the Settings screen holds into the user settings that it stores (see {@link ConlangStore#merge}): the languages the user
     * did not touch are the ones stored now, so the words a run added while the screen was open are not lost.
     *
     * @param shown    the languages of the screen, in its order, with its edits
     * @param loaded   the ids of the library when the screen loaded it
     * @param edited   the ids the user changed
     * @param defaults the user level of the inherited settings
     * @param into     the user settings the screen is about to store
     */
    public void put(List<Language> shown, Set<String> loaded, Set<String> edited, UserDefaults defaults, UserSetting into) throws Exception {
        JsonObject attributes = into.getAttributes();
        if (attributes == null) {
            attributes = new JsonObject();
            into.setAttributes(attributes);
        }
        List<Language> stored = ConlangStore.readLibrary(settingService.getOrCreate(UserSetting.class).getAttributes());
        ConlangStore.writeLibrary(attributes, ConlangStore.merge(stored, shown, loaded, edited));
        ConlangStore.writeDefaults(attributes, defaults);
    }

    /**
     * The languages to choose from for the inherited setting, by id.
     */
    public List<Choice> languageChoices() {
        try {
            return languages().stream().map(language -> new Choice(language.getId(), language.getName())).toList();
        } catch (Exception e) {
            throw new IllegalStateException("The languages can't be listed", e);
        }
    }

    public List<Choice> aiChoices() {
        try {
            return availableAis().stream().map(ai -> new Choice(ai.getUuid(), ai.getName())).toList();
        } catch (Exception e) {
            throw new IllegalStateException("The AIs can't be listed", e);
        }
    }

    public List<Choice> protocolChoices() {
        try {
            return availableProtocols().stream().map(protocol -> new Choice(protocol.getUuid(), protocol.getName())).toList();
        } catch (Exception e) {
            throw new IllegalStateException("The protocols can't be listed", e);
        }
    }

    /**
     * The books of the user, for the example book of a language (by uuid).
     */
    public List<Choice> bookChoices() throws Exception {
        return manuscriptService.findAllForUser().stream().filter(book -> !book.isDeleted())
                .map(book -> new Choice(book.getUuid(), book.getName())).toList();
    }

    /**
     * The lorebooks of the user, for the example lorebook of a language (by uuid).
     */
    public List<Choice> lorebookChoices() throws Exception {
        return lorebookService.findAllForUser().stream().filter(lorebook -> !lorebook.isDeleted())
                .map(lorebook -> new Choice(lorebook.getUuid(), lorebook.getName())).toList();
    }

    /**
     * The AIs of the user that are not deleted.
     */
    public List<AI> availableAis() throws Exception {
        return aiService.findAllForUser().stream().filter(ai -> !ai.isDeleted()).toList();
    }

    public List<Protocol> availableProtocols() throws Exception {
        return protocolService.findAllForUser().stream().filter(protocol -> !protocol.isDeleted()).toList();
    }

    /**
     * The defaults, for a place that must not throw a checked exception (the supplier of a setting).
     */
    public UserDefaults defaultsOrBlank() {
        try {
            return defaults();
        } catch (Exception e) {
            throw new IllegalStateException("The settings of the user can't be read", e);
        }
    }
}
