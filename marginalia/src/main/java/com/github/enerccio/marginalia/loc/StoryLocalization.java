package com.github.enerccio.marginalia.loc;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.collections.*;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookDecision;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookMatch;

import java.util.List;

/**
 * The {@link Localization} of the application: the labels of the enums of books, lorebooks, revisions and agents, and the
 * built-in prompts ({@link Defaults}) in the prompt languages. The bean {@code localization} is one, so code of the
 * application injects this type where it needs these.
 */
public interface StoryLocalization extends Localization {

    L getBackupStrategy(BackupStrategy t);
    L getFilteringMode(FilteringMode t);
    L getInsertionMode(InsertionMode t);
    L getActivationContext(ActivationContext t);
    L getActivationReason(LorebookActivationService.Reason t);
    L getLorebookMatch(LorebookMatch t);
    L getLorebookDecision(LorebookDecision t);
    L getRevisionSource(RevisionSource t);
    L getRevisionDetail(RevisionDetail t);
    L getAgentRunStatus(AgentRunStatus t);
    L getDestructiveCommandMode(DestructiveCommandMode t);

    /**
     * The built-in prompts and the texts sent to the model in the prompt language of the current session
     * ({@link #getPromptLanguage()}).
     */
    Defaults getDefaults();

    /**
     * The built-in prompts in the language, the English ones when it has none.
     */
    Defaults getDefaults(Language language);

    /**
     * The built-in prompts of every prompt language, English first: for code that has to recognize a text the
     * application wrote in any of them (a notice in the transcript of an agent run).
     */
    List<Defaults> getAllDefaults();

    /**
     * Adds the built-in prompts of a language an extension brings (see
     * {@link com.github.enerccio.marginalia.extensions.ExtensionHasLocalizedPrompts#getPromptDefaults()}).
     *
     * @throws IllegalArgumentException when the language already has prompts
     */
    void registerDefaults(Defaults texts);

    /**
     * Removes the prompts added by {@link #registerDefaults}; the ones of the application stay. A user or an agent whose
     * prompt language is gone gets English.
     */
    void unregisterDefaults(Defaults texts);
}
