package com.github.enerccio.marginalia.extensions.nameprovider;

import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.FunctionRegistry;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * Gives the model functions to get real names (lists of forenames and surnames of countries, then names picked at
 * random from one of them). The functions are compiled on load; other extensions offer them to the model.
 */
@Configurable
public class NameProviderExtension implements MarginaliaExtension {

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    private NameLists lists;

    @Autowired
    private InferenceServices inferenceServices;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        lists = NameLists.load();
        FunctionRegistry registry = inferenceServices.getFunctionRegistry();
        // fails the load when the interface is not a valid set of functions
        int count = registry.definitionsOf(NameProviderFunctions.class).size();
        log.info("Name provider loaded with {} functions and {} categories", count, lists.categories().size());
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
    }

    /** The titles of the categories the functions can pick from. */
    public List<String> categories() {
        return lists.categories();
    }

    /** The implementation of the functions, for a caller that wraps or restricts it. */
    public NameProviderFunctionsImpl newFunctions() {
        return new NameProviderFunctionsImpl(lists);
    }

    /** The functions of one request, to be passed to the inference. */
    public FunctionSet functions() {
        return inferenceServices.getFunctionRegistry().bind(NameProviderFunctions.class, newFunctions());
    }

}
