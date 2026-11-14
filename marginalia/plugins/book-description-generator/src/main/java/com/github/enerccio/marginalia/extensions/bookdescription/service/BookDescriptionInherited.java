package com.github.enerccio.marginalia.extensions.bookdescription.service;

import com.github.enerccio.marginalia.extensions.InheritedSetting;
import com.github.enerccio.marginalia.extensions.InheritedSettingRegistry;
import com.github.enerccio.marginalia.extensions.bookdescription.model.BookDescriptionSettings;

import java.util.function.Supplier;

import static com.github.enerccio.marginalia.extensions.bookdescription.BookDescriptionLocalization.*;

/**
 * The settings of the plugin that books and folders inherit (see {@code InheritedSettingRegistry}): the two prompts. Their user
 * level is the settings of the user ({@link BookDescriptionSettings}), their last level the built-in text.
 */
public final class BookDescriptionInherited {

    public static final String DESCRIPTION_PROMPT = "bookdescription.descriptionPrompt";
    public static final String NOTES_PROMPT = "bookdescription.notesPrompt";

    private BookDescriptionInherited() {
    }

    /**
     * Registers the two prompts.
     *
     * @param owner    the symbolic name of the bundle
     * @param settings the settings of the user to read the user level from; it must not write (it is asked when a setting is resolved)
     */
    public static void register(InheritedSettingRegistry registry, String owner, Supplier<BookDescriptionSettings> settings) {
        registry.register(owner, InheritedSetting.textArea(DESCRIPTION_PROMPT, () -> T.localized(BOOK_DESCRIPTION_PROMPT))
                .group(() -> T.localized(BOOK_DESCRIPTION_GENERATOR))
                .help(() -> T.localized(THE_PROMPT_THAT_WRITES_THE_NAME))
                .userValue(() -> settings.get().getDescriptionPrompt())
                .fallback(() -> T.defaults().descriptionPrompt()));
        registry.register(owner, InheritedSetting.textArea(NOTES_PROMPT, () -> T.localized(BOOK_NOTES_PROMPT))
                .group(() -> T.localized(BOOK_DESCRIPTION_GENERATOR))
                .help(() -> T.localized(THE_PROMPT_THAT_WRITES_NOTES))
                .userValue(() -> settings.get().getNotesPrompt())
                .fallback(() -> T.defaults().notesPrompt()));
    }
}
