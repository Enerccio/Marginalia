package com.github.enerccio.marginalia.loc;

import java.text.DateFormat;
import java.util.Locale;

/**
 * The platform's texts ({@link L}) and the formats of one language; a language exists when it has one of these. The
 * texts of the layers above are other {@link TextBundle}s of the same language. The application ships {@link LocalizationBundleEN}, an extension adds a
 * language by implementing {@link com.github.enerccio.marginalia.extensions.ExtensionHasLocalization}. Which bundle is
 * used is decided by {@link Localization} from the language of the user's session.
 * <p>
 * A bundle doesn't need to have every key: a text that is missing is taken from the default language. Extend
 * {@link LocalizationBundleBase} to fill the texts with {@code setValue}.
 */
public interface LocalizationBundle extends TextBundle<L> {

    Language getLanguage();

    /**
     * Name of the language in the language itself ("English", "Čeština"), shown in the language selector.
     */
    String getDisplayName();

    Locale getLocale();

    DateFormat getDateFormat();

    DateFormat getHourFormat();

    DateFormat getDateHourFormat();

    @Override
    default Class<L> getKeys() {
        return L.class;
    }

}
