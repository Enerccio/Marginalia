package com.github.enerccio.marginalia.extensions;

import com.github.enerccio.marginalia.loc.LocalizationBundle;
import com.github.enerccio.marginalia.loc.M_L;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.TextBundle;

import java.util.List;

/**
 * Implemented by a {@link MarginaliaExtension} that brings languages of its own. The bundles are registered in
 * {@code Localization} right after the extension is loaded and removed when it is unloaded; a language with a bundle
 * is offered in the language selector of the workspace. A bundle for a language that already has one (the English one
 * of the application included) is refused. A user whose language is gone is shown the default language. Prompts in
 * other languages are {@link ExtensionHasLocalizedPrompts}.
 * <p>
 * A {@link LocalizationBundle} has the platform's texts ({@code L}) and the formats; the texts of the layers above it
 * (the application's {@code M_L}) are {@link #getTextBundles()}, so a language for the whole application brings both.
 * A key a bundle lacks is shown in the default language.
 */
public interface ExtensionHasLocalization {

    /**
     * The bundles of the extension. Called when the extension is loaded.
     */
    List<LocalizationBundle> getLocalizationBundles();

    /**
     * The texts of the enums of keys above the platform (the application's {@code M_L}) for the languages of the
     * extension, registered after {@link #getLocalizationBundles()}. None by default.
     */
    default List<TextBundle<?>> getTextBundles() {
        return List.of();
    }

}
