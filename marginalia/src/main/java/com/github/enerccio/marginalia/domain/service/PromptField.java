package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;

import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;

/**
 * The text settings a book has, which a folder can set for the books in it too. A book uses its own, else the nearest
 * folder above it that sets one, else the one of the user settings, else the built-in default, see
 * {@link ManuscriptService#resolve}. A blank text is not set.
 */
public enum PromptField {
    MASTER_TEMPLATE(Manuscript::getTemplate, Folder::getTemplate, UserSetting::getMasterTemplate, Defaults.DEFAULT_MASTER_TEMPLATE),
    POV(Manuscript::getPov, Folder::getPov, UserSetting::getDefaultPov, Defaults.DEFAULT_POV),
    TENSE(Manuscript::getTense, Folder::getTense, UserSetting::getDefaultTense, Defaults.DEFAULT_TENSE),
    STYLE(Manuscript::getStyle, Folder::getStyle, UserSetting::getDefaultStyle, Defaults.DEFAULT_STYLE),
    USER_PROMPT(Manuscript::getUserPrompt, Folder::getUserPrompt, UserSetting::getDefaultUserPrompt,
            Defaults.DEFAULT_USER_PROMPT),
    SUMMARY_PROMPT(Manuscript::getSummaryPrompt, Folder::getSummaryPrompt, UserSetting::getDefaultSummaryPrompt,
            Defaults.DEFAULT_SUMMARY_PROMPT),
    META_SUMMARY_PROMPT(Manuscript::getMetaSummaryPrompt, Folder::getMetaSummaryPrompt,
            UserSetting::getDefaultMetaSummaryPrompt, Defaults.DEFAULT_META_SUMMARY),
    REDRAFT_SYSTEM_PROMPT(Manuscript::getRedraftSystemPrompt, Folder::getRedraftSystemPrompt,
            UserSetting::getDefaultRedraftSystemPrompt, Defaults.DEFAULT_REDRAFT_SYSTEM_PROMPT),
    REDRAFT_PROMPT(Manuscript::getRedraftPrompt, Folder::getRedraftPrompt, UserSetting::getDefaultRedraftPrompt,
            Defaults.DEFAULT_REDRAFT_PROMPT),
    ANNOTATE_SYSTEM_PROMPT(Manuscript::getAnnotateSystemPrompt, Folder::getAnnotateSystemPrompt,
            UserSetting::getDefaultAnnotateSystemPrompt, Defaults.DEFAULT_ANNOTATE_SYSTEM_PROMPT),
    ANNOTATE_PROMPT(Manuscript::getAnnotatePrompt, Folder::getAnnotatePrompt, UserSetting::getDefaultAnnotatePrompt,
            Defaults.DEFAULT_ANNOTATE_PROMPT),
    IMPERSONATE_PROMPT(Manuscript::getImpersonatePrompt, Folder::getImpersonatePrompt,
            UserSetting::getDefaultImpersonatePrompt, Defaults.DEFAULT_IMPERSONATE_PROMPT);

    private final Function<Manuscript, String> book;
    private final Function<Folder, String> folder;
    private final Function<UserSetting, String> user;
    private final String fallback;

    PromptField(Function<Manuscript, String> book, Function<Folder, String> folder, Function<UserSetting, String> user,
                String fallback) {
        this.book = book;
        this.folder = folder;
        this.user = user;
        this.fallback = fallback;
    }

    /**
     * What the book sets itself, null or blank when it does not.
     */
    public String ofBook(Manuscript manuscript) {
        return book.apply(manuscript);
    }

    /**
     * What the folder sets itself, null or blank when it does not.
     */
    public String ofFolder(Folder folder) {
        return this.folder.apply(folder);
    }

    /**
     * What the user settings set, null or blank when they do not.
     */
    public String ofUser(UserSetting settings) {
        return user.apply(settings);
    }

    /**
     * Sets what the book sets itself; a blank text clears it (the book inherits).
     */
    public void setOfBook(Manuscript manuscript, String value) {
        String text = StringUtils.defaultIfBlank(value, null);
        switch (this) {
            case MASTER_TEMPLATE -> manuscript.setTemplate(text);
            case POV -> manuscript.setPov(text);
            case TENSE -> manuscript.setTense(text);
            case STYLE -> manuscript.setStyle(text);
            case USER_PROMPT -> manuscript.setUserPrompt(text);
            case SUMMARY_PROMPT -> manuscript.setSummaryPrompt(text);
            case META_SUMMARY_PROMPT -> manuscript.setMetaSummaryPrompt(text);
            case REDRAFT_SYSTEM_PROMPT -> manuscript.setRedraftSystemPrompt(text);
            case REDRAFT_PROMPT -> manuscript.setRedraftPrompt(text);
            case ANNOTATE_SYSTEM_PROMPT -> manuscript.setAnnotateSystemPrompt(text);
            case ANNOTATE_PROMPT -> manuscript.setAnnotatePrompt(text);
            case IMPERSONATE_PROMPT -> manuscript.setImpersonatePrompt(text);
        }
    }

    /**
     * Sets what the folder sets for its books; a blank text clears it (the folder inherits).
     */
    public void setOfFolder(Folder folder, String value) {
        String text = StringUtils.defaultIfBlank(value, null);
        switch (this) {
            case MASTER_TEMPLATE -> folder.setTemplate(text);
            case POV -> folder.setPov(text);
            case TENSE -> folder.setTense(text);
            case STYLE -> folder.setStyle(text);
            case USER_PROMPT -> folder.setUserPrompt(text);
            case SUMMARY_PROMPT -> folder.setSummaryPrompt(text);
            case META_SUMMARY_PROMPT -> folder.setMetaSummaryPrompt(text);
            case REDRAFT_SYSTEM_PROMPT -> folder.setRedraftSystemPrompt(text);
            case REDRAFT_PROMPT -> folder.setRedraftPrompt(text);
            case ANNOTATE_SYSTEM_PROMPT -> folder.setAnnotateSystemPrompt(text);
            case ANNOTATE_PROMPT -> folder.setAnnotatePrompt(text);
            case IMPERSONATE_PROMPT -> folder.setImpersonatePrompt(text);
        }
    }

    public String fallback() {
        return fallback;
    }
}
