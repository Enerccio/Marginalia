package com.github.enerccio.marginalia.extensions.grok;

import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * xAI Grok as an inference provider (the xAI REST API): registers the {@link GrokFactory}, so the AI
 * dialog offers the type <i>Extension</i> with the provider <i>xAI Grok</i>, and takes it back when unloaded.
 * The AIs created with it stay in the database and work again when the extension is loaded.
 */
@Configurable
public class GrokExtension implements MarginaliaExtension {

    @Autowired
    private InferenceServices inferenceServices;

    private Logger log;
    private GrokFactory factory;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        factory = new GrokFactory(inferenceServices, log);
        inferenceServices.registerExtensionFactory(factory);
        log.info("xAI Grok is available as an AI provider");
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        if (factory != null) {
            inferenceServices.unregisterExtensionFactory(factory);
            factory.close();
            factory = null;
        }
    }
}
