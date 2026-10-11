package com.github.enerccio.marginalia.extensions.chineselocalization;

import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.ExtensionHasLocalization;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.loc.LocalizationBundle;
import org.osgi.framework.Bundle;

import java.util.List;

/**
 * Adds Simplified Chinese to the languages of the UI. There is nothing to do on load or unload: Marginalia registers
 * the bundles returned by {@link #getLocalizationBundles()} after the extension is loaded and removes them when it is
 * unloaded.
 */
public class ChineseLocalizationExtension implements MarginaliaExtension, ExtensionHasLocalization {

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
    }

    @Override
    public List<LocalizationBundle> getLocalizationBundles() {
        return List.of(new LocalizationBundleZH());
    }

}
