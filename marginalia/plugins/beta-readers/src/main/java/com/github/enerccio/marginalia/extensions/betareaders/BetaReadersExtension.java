package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Beta Readers: personas the user defines and a chat about the book with them. Nothing is implemented yet, see
 * {@code TODO.md}.
 */
@Configurable
public class BetaReadersExtension implements MarginaliaExtension {

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        log.info("Beta Readers loaded (stub, nothing is registered yet)");
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiService parentService, ExtensionService extensionService) {
    }
}
