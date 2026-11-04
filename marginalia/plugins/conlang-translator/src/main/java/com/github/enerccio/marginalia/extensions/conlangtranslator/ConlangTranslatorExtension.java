package com.github.enerccio.marginalia.extensions.conlangtranslator;

import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Translates the direct speech of the characters who speak a constructed language into that language and lets the reader
 * reveal the original. A stub: nothing is hooked yet, see TODO.md for the plan.
 */
@Configurable
public class ConlangTranslatorExtension implements MarginaliaExtension {

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        log.info("Conlang Translator loaded (stub, nothing is hooked yet)");
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiService parentService, ExtensionService extensionService) {
        log.info("Conlang Translator unloaded");
    }
}
