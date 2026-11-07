package com.github.enerccio.marginalia.extensions.knowledgetracker.service;

import com.github.enerccio.marginalia.extensions.InheritedSetting;
import com.github.enerccio.marginalia.extensions.InheritedSettingRegistry;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ExtractionPrompt;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.TidyPrompt;

import static com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerLocalization.*;

/**
 * The settings of the plugin that books and folders inherit (see {@code InheritedSettingRegistry}): the system messages of the
 * extraction and of the tidy pass. Their user level is the text the user saved in the Knowledge tab, their last level the built-in
 * (tested) wording.
 */
public final class KnowledgeInherited {

    public static final String EXTRACTION_PROMPT = "knowledgetracker.extractionPrompt";
    public static final String TIDY_PROMPT = "knowledgetracker.tidyPrompt";

    private KnowledgeInherited() {
    }

    /**
     * Registers the two prompts.
     *
     * @param owner the symbolic name of the bundle
     * @param user  the user level, it must not write (it is asked when a setting is resolved)
     */
    public static void register(InheritedSettingRegistry registry, String owner, UserPrompts user) {
        registry.register(owner, InheritedSetting.textArea(EXTRACTION_PROMPT, () -> T.localized(KNOWLEDGE_EXTRACTION_PROMPT))
                .group(() -> T.localized(KNOWLEDGE_TRACKER))
                .help(() -> T.localized(THE_SYSTEM_MESSAGE_THAT_TELLS_THE_MODEL))
                .userValue(() -> user.get(EXTRACTION_PROMPT))
                .fallback(() -> ExtractionPrompt.SYSTEM));
        registry.register(owner, InheritedSetting.textArea(TIDY_PROMPT, () -> T.localized(KNOWLEDGE_TIDY_PROMPT))
                .group(() -> T.localized(KNOWLEDGE_TRACKER))
                .help(() -> T.localized(THE_SYSTEM_MESSAGE_OF_THE_TIDY_PASS))
                .userValue(() -> user.get(TIDY_PROMPT))
                .fallback(() -> TidyPrompt.SYSTEM));
    }

    /** The user level of the prompts. */
    @FunctionalInterface
    public interface UserPrompts {

        /** What the user saved under {@code key}, {@code null} or blank for nothing. */
        String get(String key);
    }
}
