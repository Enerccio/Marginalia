package com.github.enerccio.marginalia.extensions.knowledgetracker;

import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Stub: keeps track of what each character of the book knows (and does not know) and puts it into the prompt.
 * See TODO.md for the plan.
 */
@Configurable
public class KnowledgeTrackerExtension implements MarginaliaExtension {

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        log.info("Knowledge Tracker loaded (stub, does nothing yet)");
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiService parentService, ExtensionService extensionService) {
        log.info("Knowledge Tracker unloaded");
    }
}
