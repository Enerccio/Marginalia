package com.github.enerccio.marginalia.extensions.nameprovider;

import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * Gives the model functions to get real names (lists of forenames and surnames of countries, then names picked at
 * random from one of them). The functions are compiled on load and the commands registered; other extensions offer them to the model.
 */
@Configurable
public class NameProviderExtension implements MarginaliaExtension {

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    private NameLists lists;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private CommandService commandService;

    /** Kept to unregister it, the service takes the object. */
    private NameProviderCommands commands;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        lists = NameLists.load();
        FunctionRegistry registry = inferenceServices.getFunctionRegistry();
        // fails the load when the interface is not a valid set of functions
        int count = registry.definitionsOf(NameProviderFunctions.class).size();
        log.info("Name provider loaded with {} functions and {} categories", count, lists.categories().size());
        commands = new NameProviderCommands(lists);
        commandService.registerCommands(commands);
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        if (commands != null) {
            commandService.unregisterCommands(commands);
            commands = null;
        }
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
