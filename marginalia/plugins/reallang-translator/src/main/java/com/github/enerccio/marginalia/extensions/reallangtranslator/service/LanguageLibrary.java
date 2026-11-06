package com.github.enerccio.marginalia.extensions.reallangtranslator.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.ExtractionPrompt;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.ExtractionPrompts;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.RealLanguage;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.UserDefaults;
import com.github.enerccio.marginalia.extensions.reallangtranslator.store.RealStore;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

/**
 * The library of languages of the user and the user level of the inherited settings, as the panel in the Settings screen and the dialogs of books
 * and folders need them. The data is in the attributes of the user's settings ({@link RealStore}); the Settings screen stores its own entity, so the
 * writers here put into the entity they are given and do not save.
 */
@Configurable
public class LanguageLibrary {

    /** One writer of the library at a time. */
    private static final Object LIBRARY = new Object();

    @Autowired
    private SettingService settingService;
    @Autowired
    private AIService aiService;
    @Autowired
    private ProtocolService protocolService;

    /**
     * The languages as they are stored now, each an independent object.
     */
    public List<RealLanguage> languages() throws Exception {
        return RealStore.readLibrary(settingService.getOrCreate(UserSetting.class).getAttributes());
    }

    /**
     * The user level of the inherited settings, as stored now.
     */
    public UserDefaults defaults() throws Exception {
        return RealStore.readDefaults(settingService.getOrCreate(UserSetting.class).getAttributes());
    }

    /**
     * Puts what the Settings screen holds into the user settings that it stores.
     *
     * @param shown    the languages of the screen, in its order, with its edits
     * @param defaults the user level of the inherited settings
     * @param into     the user settings the screen is about to store
     */
    public void put(List<RealLanguage> shown, UserDefaults defaults, UserSetting into) {
        JsonObject attributes = into.getAttributes();
        if (attributes == null) {
            attributes = new JsonObject();
            into.setAttributes(attributes);
        }
        RealStore.writeLibrary(attributes, shown);
        RealStore.writeDefaults(attributes, defaults);
    }

    /**
     * Puts what the Settings screen holds, with the user's prompts of the word extraction, into the user settings that it stores.
     *
     * @param prompts the extraction prompts of the screen, in its order, with its edits
     */
    public void put(List<RealLanguage> shown, UserDefaults defaults, List<ExtractionPrompt> prompts, UserSetting into) {
        put(shown, defaults, into);
        RealStore.writePrompts(into.getAttributes(), prompts);
    }

    // ---- the extraction prompts

    /**
     * The user's own prompts of the word extraction as stored now, each an independent object.
     */
    public List<ExtractionPrompt> userPrompts() throws Exception {
        return RealStore.readPrompts(settingService.getOrCreate(UserSetting.class).getAttributes());
    }

    /**
     * The prompts to choose from: the built-in ones, then the user's own.
     */
    public List<ExtractionPrompt> extractionPrompts() throws Exception {
        List<ExtractionPrompt> all = new ArrayList<>(ExtractionPrompts.builtIn());
        all.addAll(userPrompts());
        return all;
    }

    /**
     * The prompt with the id (a built-in one or the user's), null for none.
     */
    public ExtractionPrompt findPrompt(String id) throws Exception {
        return id == null ? null : extractionPrompts().stream().filter(prompt -> id.equals(prompt.getId())).findFirst().orElse(null);
    }

    /**
     * The prompt the reference names: its id, else its name (the case does not matter), among the built-in and the user's.
     *
     * @throws IllegalArgumentException when there is none, or the name is not unique
     */
    public ExtractionPrompt resolvePrompt(String reference) throws Exception {
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("Give the prompt: its name or its id, see reallang-extraction-prompts");
        }
        String wanted = reference.strip();
        List<ExtractionPrompt> all = extractionPrompts();
        for (ExtractionPrompt prompt : all) {
            if (prompt.getId().equals(wanted)) {
                return prompt;
            }
        }
        List<ExtractionPrompt> named = all.stream().filter(prompt -> prompt.getName().equalsIgnoreCase(wanted)).toList();
        if (named.size() == 1) {
            return named.getFirst();
        }
        throw new IllegalArgumentException(named.isEmpty() ? "There is no prompt " + wanted + ". Prompts: " + String.join("; ", all.stream().map(ExtractionPrompt::getName).toList())
                : "More than one prompt is called " + wanted + ", give its id");
    }

    /**
     * Stores a prompt of the user (replaces the one with its id, else adds it), saved at once. A built-in prompt can't be stored: copy it first.
     *
     * @throws IllegalArgumentException when the prompt is a built-in one, or has no name or no text
     */
    public void storePrompt(ExtractionPrompt prompt) throws Exception {
        if (ExtractionPrompts.isBuiltIn(prompt.getId())) {
            throw new IllegalArgumentException("A built-in prompt can't be changed, make a copy of it");
        }
        if (prompt.getName().isEmpty() || prompt.getText().isEmpty()) {
            throw new IllegalArgumentException("The prompt needs a name and a text");
        }
        synchronized (LIBRARY) {
            UserSetting user = settingService.getOrCreate(UserSetting.class);
            JsonObject attributes = user.getAttributes();
            if (attributes == null) {
                attributes = new JsonObject();
                user.setAttributes(attributes);
            }
            List<ExtractionPrompt> prompts = RealStore.readPrompts(attributes);
            boolean replaced = false;
            for (int i = 0; i < prompts.size(); i++) {
                if (prompts.get(i).getId().equals(prompt.getId())) {
                    prompts.set(i, prompt);
                    replaced = true;
                }
            }
            if (!replaced) {
                prompts.add(prompt);
            }
            RealStore.writePrompts(attributes, prompts);
            settingService.save(user);
        }
    }

    /**
     * Takes a prompt of the user out of the library, saved at once.
     *
     * @return whether the library had it
     */
    public boolean removePrompt(String id) throws Exception {
        synchronized (LIBRARY) {
            UserSetting user = settingService.getOrCreate(UserSetting.class);
            if (user.getAttributes() == null) {
                return false;
            }
            List<ExtractionPrompt> prompts = RealStore.readPrompts(user.getAttributes());
            if (!prompts.removeIf(prompt -> prompt.getId().equals(id))) {
                return false;
            }
            RealStore.writePrompts(user.getAttributes(), prompts);
            settingService.save(user);
            return true;
        }
    }

    // ---- what the commands need: the library written and saved at once

    /**
     * The language the reference names: its id, else its name (the case does not matter).
     *
     * @throws IllegalArgumentException when there is none, or the name is not unique
     */
    public RealLanguage resolve(String reference) throws Exception {
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("Give the language: its name or its id, see reallang-list");
        }
        String wanted = reference.strip();
        List<RealLanguage> all = languages();
        for (RealLanguage language : all) {
            if (language.getId().equals(wanted)) {
                return language;
            }
        }
        List<RealLanguage> named = all.stream().filter(language -> language.getName().strip().equalsIgnoreCase(wanted)).toList();
        if (named.size() == 1) {
            return named.getFirst();
        }
        throw new IllegalArgumentException(named.isEmpty() ? "There is no language " + wanted + ". Languages: "
                + (all.isEmpty() ? "none" : String.join(", ", all.stream().map(RealLanguage::getName).toList()))
                : "More than one language is called " + wanted + ", give its id");
    }

    /**
     * Stores the language (replaces the one with its id, else adds it) in the library, saved at once. The library is written one at a time.
     */
    public void store(RealLanguage language) throws Exception {
        synchronized (LIBRARY) {
            UserSetting user = settingService.getOrCreate(UserSetting.class);
            JsonObject attributes = user.getAttributes();
            if (attributes == null) {
                attributes = new JsonObject();
                user.setAttributes(attributes);
            }
            RealStore.save(attributes, language);
            settingService.save(user);
        }
    }

    /**
     * Takes the language out of the library, saved at once.
     *
     * @return whether the library had it
     */
    public boolean remove(String id) throws Exception {
        synchronized (LIBRARY) {
            UserSetting user = settingService.getOrCreate(UserSetting.class);
            if (user.getAttributes() == null || !RealStore.delete(user.getAttributes(), id)) {
                return false;
            }
            settingService.save(user);
            return true;
        }
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
