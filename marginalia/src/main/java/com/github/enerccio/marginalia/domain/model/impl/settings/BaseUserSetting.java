package com.github.enerccio.marginalia.domain.model.impl.settings;

import com.github.enerccio.marginalia.domain.collections.ThemeMode;
import com.github.enerccio.marginalia.domain.model.Setting;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import com.google.gson.JsonObject;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

/**
 * The settings of a user that the platform reads: the languages, the theme, the keys, where the user left the UI and
 * the default AI and protocol. An application extends it with its own settings as an entity of its own (Marginalia's
 * {@link UserSetting}) and tells {@code SettingService} which class that is ({@code getUserSetting()}). Every field is
 * an extended attribute, so moving one between this class and the application's needs no migration.
 */
@MappedSuperclass
public abstract class BaseUserSetting extends Setting {

    @ExtendedAttribute
    @Transient
    @CleanupReference(value = Policy.WEAK, target = AI.class)
    private Long defaultModel;

    @ExtendedAttribute
    @Transient
    @CleanupReference(value = Policy.WEAK, target = Protocol.class)
    private Long defaultProtocol;

    /**
     * The color scheme of the UI, {@code null} for the default one ({@link ThemeMode#DARK}).
     */
    @ExtendedAttribute
    @Transient
    private ThemeMode themeMode;

    /**
     * Code of the language of the UI, {@code null} for the default one.
     */
    @ExtendedAttribute
    @Transient
    private String language;

    /**
     * Code of the language of the built-in prompts ({@code Localization.getDefaults()}), {@code null} for English.
     * Independent of the language of the UI.
     */
    @ExtendedAttribute
    @Transient
    private String promptLanguage;

    /**
     * Where the user left the workspace (selected tab, filters, the open book, ...), written by the workspace autosave
     * as the {@code Restorable} state of the workspace and read back after the login.
     */
    @ExtendedAttribute(inject = true, injectPrefix = "workspaceState")
    @Transient
    private JsonObject workspaceState = new JsonObject();

    /**
     * The keys of the user for the keyboard shortcuts of the story editor: action id to key combination, only the ones
     * that differ from the default of the action (an empty text is no key), see {@code KeyBindings}.
     */
    @ExtendedAttribute(inject = true, injectPrefix = "keyBindings")
    @Transient
    private JsonObject keyBindings = new JsonObject();

    public Long getDefaultModel() {
        return defaultModel;
    }

    public void setDefaultModel(Long defaultModel) {
        this.defaultModel = defaultModel;
    }

    public Long getDefaultProtocol() {
        return defaultProtocol;
    }

    public void setDefaultProtocol(Long defaultProtocol) {
        this.defaultProtocol = defaultProtocol;
    }

    public ThemeMode getThemeMode() {
        return themeMode == null ? ThemeMode.DARK : themeMode;
    }

    public void setThemeMode(ThemeMode themeMode) {
        this.themeMode = themeMode;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPromptLanguage() {
        return promptLanguage;
    }

    public void setPromptLanguage(String promptLanguage) {
        this.promptLanguage = promptLanguage;
    }

    public JsonObject getWorkspaceState() {
        return workspaceState;
    }

    public void setWorkspaceState(JsonObject workspaceState) {
        this.workspaceState = workspaceState == null ? new JsonObject() : workspaceState;
    }

    public JsonObject getKeyBindings() {
        return keyBindings;
    }

    public void setKeyBindings(JsonObject keyBindings) {
        this.keyBindings = keyBindings == null ? new JsonObject() : keyBindings;
    }
}
