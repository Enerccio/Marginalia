package com.github.enerccio.marginalia.extensions.nameprovider;

import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Gives the model functions to get real names (lists of forenames and surnames of countries, then names picked at random
 * from one of them), as a tool suite and as commands. Both are registered with the tool service, which removes them when
 * the extension is unloaded; the Lore Generator, the Arc Outline and whoever else lets the user choose tools offer the
 * functions to the model.
 */
@Configurable
public class NameProviderExtension implements MarginaliaExtension {

    /** The key of the tool suite in the tool service; callers save it, don't rename it. */
    public static final String PROVIDER_KEY = "names";

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private ToolService toolService;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        NameLists lists = NameLists.load();
        FunctionRegistry registry = inferenceServices.getFunctionRegistry();
        // fails the load when the interface is not a valid set of functions
        int count = registry.definitionsOf(NameProviderFunctions.class).size();
        log.info("Name provider loaded with {} functions and {} categories", count, lists.categories().size());
        toolService.registerProvider(bundle.getSymbolicName(), PROVIDER_KEY, "Name provider",
                new NameProviderToolProvider(lists, registry));
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        toolService.unregisterAll(bundle.getSymbolicName());
    }

}
