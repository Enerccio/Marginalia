package com.github.enerccio.marginalia.extensions.statetracker.service;

import com.github.enerccio.marginalia.extensions.InheritedSetting;
import com.github.enerccio.marginalia.extensions.InheritedSettingRegistry;

import static com.github.enerccio.marginalia.extensions.statetracker.StateTrackerLocalization.*;

/**
 * The settings of the plugin that books and folders inherit (see {@code InheritedSettingRegistry}): the system messages of the
 * extraction and of the tidy. Their user level is the text the user saved in the State Tracker section of the settings, their last
 * level the built-in (tested) wording in the prompt language. The clock note is not one of them: it is added after the extraction
 * message, whichever it is.
 */
public final class StateInherited {

    public static final String EXTRACTION_PROMPT = "statetracker.extractionPrompt";
    public static final String TIDY_PROMPT = "statetracker.tidyPrompt";

    private StateInherited() {
    }

    /**
     * Registers the two prompts.
     *
     * @param owner the symbolic name of the bundle
     * @param user  the user level, it must not write (it is asked when a setting is resolved)
     */
    public static void register(InheritedSettingRegistry registry, String owner, UserPrompts user) {
        registry.register(owner, InheritedSetting.textArea(EXTRACTION_PROMPT, () -> T.localized(STATE_EXTRACTION_PROMPT))
                .group(() -> T.localized(STATE_TRACKER))
                .help(() -> T.localized(THE_SYSTEM_MESSAGE_THAT_TELLS_THE_MODEL))
                .userValue(() -> user.get(EXTRACTION_PROMPT))
                .fallback(() -> T.defaults().extractionSystem()));
        registry.register(owner, InheritedSetting.textArea(TIDY_PROMPT, () -> T.localized(STATE_TIDY_PROMPT))
                .group(() -> T.localized(STATE_TRACKER))
                .help(() -> T.localized(THE_SYSTEM_MESSAGE_OF_THE_TIDY))
                .userValue(() -> user.get(TIDY_PROMPT))
                .fallback(() -> T.defaults().tidySystem()));
    }

    /** The user level of the prompts. */
    @FunctionalInterface
    public interface UserPrompts {

        /** What the user saved under {@code key}, {@code null} or blank for nothing. */
        String get(String key);
    }
}
