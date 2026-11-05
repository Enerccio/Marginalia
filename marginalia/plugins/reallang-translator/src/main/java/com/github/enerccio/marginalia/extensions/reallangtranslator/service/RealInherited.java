package com.github.enerccio.marginalia.extensions.reallangtranslator.service;

import com.github.enerccio.marginalia.extensions.InheritedSetting;
import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.InheritedSettingRegistry;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.UserDefaults;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.ReaderMode;

import java.util.List;
import java.util.function.Supplier;

import static com.github.enerccio.marginalia.extensions.reallangtranslator.ReallangTranslatorLocalization.*;

/**
 * What a book or a folder chooses for the plugin (see {@code InheritedSettingRegistry}): the language, whether a part is translated by itself, the
 * model and the protocol of the request, and what the readers of a published book see. The user level is {@link UserDefaults}, the panel of the
 * plugin in the Settings screen.
 */
public final class RealInherited {

    public static final String LANGUAGE = "reallangtranslator.language";
    public static final String AUTO = "reallangtranslator.auto";
    public static final String AI = "reallangtranslator.ai";
    public static final String PROTOCOL = "reallangtranslator.protocol";
    public static final String READER = "reallangtranslator.reader";

    /** The ids of the two choices of {@link #AUTO}. */
    public static final String YES = "yes";
    public static final String NO = "no";

    private RealInherited() {
    }

    /**
     * @param defaults  the user level, read when a setting is resolved; it must not write
     * @param languages the languages of the library, the id of a language is what is stored
     * @param ais       the AIs to choose from, the uuid of an AI is what is stored
     * @param protocols the protocols to choose from, the uuid of a protocol is what is stored
     */
    public static void register(InheritedSettingRegistry registry, String owner, Supplier<UserDefaults> defaults,
                                Supplier<List<Choice>> languages, Supplier<List<Choice>> ais, Supplier<List<Choice>> protocols) {
        registry.register(owner, InheritedSetting.choice(LANGUAGE, () -> T.localized(LANGUAGE_OF_THE_SPEECH), languages)
                .group(() -> T.localized(REAL_LANGUAGE_TRANSLATOR))
                .help(() -> T.localized(THE_SPEECH_IS_TRANSLATED_INTO))
                .userValue(() -> defaults.get().getLanguageId()));
        registry.register(owner, InheritedSetting.choice(AUTO, () -> T.localized(TRANSLATE_BY_ITSELF), () -> List.of(
                        new Choice(YES, T.localized(YES_TEXT)), new Choice(NO, T.localized(NO_TEXT))))
                .group(() -> T.localized(REAL_LANGUAGE_TRANSLATOR))
                .help(() -> T.localized(A_NEW_PART_IS_TRANSLATED))
                .userValue(() -> defaults.get().isAuto() ? YES : NO)
                .fallback(() -> NO));
        registry.register(owner, InheritedSetting.choice(AI, () -> T.localized(MODEL_OF_THE_TRANSLATION), ais)
                .group(() -> T.localized(REAL_LANGUAGE_TRANSLATOR))
                .help(() -> T.localized(EMPTY_THE_MODEL_OF_THE_BOOK))
                .userValue(() -> defaults.get().getAiUuid()));
        registry.register(owner, InheritedSetting.choice(PROTOCOL, () -> T.localized(PROTOCOL_OF_THE_TRANSLATION), protocols)
                .group(() -> T.localized(REAL_LANGUAGE_TRANSLATOR))
                .help(() -> T.localized(EMPTY_THE_PROTOCOL_OF_THE_BOOK))
                .userValue(() -> defaults.get().getProtocolUuid()));
        // no user level and no fallback but the default mode: the reader of a published book is not the author, whose defaults must not
        // decide what the reader sees
        registry.register(owner, InheritedSetting.choice(READER, () -> T.localized(READERS_SEE), () -> List.of(
                        new Choice(ReaderMode.CLICK.id(), T.localized(THE_TRANSLATION_WITH_THE_ENGLISH_ON_CLICK)),
                        new Choice(ReaderMode.TRANSLATED.id(), T.localized(THE_TRANSLATION_ONLY)),
                        new Choice(ReaderMode.SOURCE.id(), T.localized(ENGLISH_ONLY))))
                .group(() -> T.localized(REAL_LANGUAGE_TRANSLATOR))
                .help(() -> T.localized(WHAT_THE_READERS_OF_THE_PUBLISHED_BOOK_SEE))
                .fallback(() -> ReaderMode.CLICK.id()));
    }
}
