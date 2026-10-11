package com.github.enerccio.marginalia.extensions;

import com.github.enerccio.marginalia.loc.LocalizationBundle;

import java.util.List;

/**
 * Implemented by a {@link MarginaliaExtension} that brings languages of its own. The bundles are registered in
 * {@code Localization} right after the extension is loaded and removed when it is unloaded; a language with a bundle
 * is offered in the language selector of the workspace. A bundle for a language that already has one (the English one
 * of the application included) is refused. A user whose language is gone is shown the default language.
 */
public interface ExtensionHasLocalization {

    /**
     * The bundles of the extension. Called when the extension is loaded.
     */
    List<LocalizationBundle> getLocalizationBundles();

}
