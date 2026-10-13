package com.github.enerccio.marginalia.extensions.namegenerator;

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
 * Gives the model functions to get names (categories, then names of a category from Markov chain tables). The
 * functions are compiled on load; nothing offers them to the model yet.
 */
@Configurable
public class NameGeneratorExtension implements MarginaliaExtension {

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    private NameTables tables;

    @Autowired
    private InferenceServices inferenceServices;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        tables = NameTables.load();
        FunctionRegistry registry = inferenceServices.getFunctionRegistry();
        // fails the load when the interface is not a valid set of functions
        int count = registry.definitionsOf(NameGeneratorFunctions.class).size();
        log.info("Name generator loaded with {} functions and categories {}", count, tables.categories());
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
    }

    /** The titles of the categories the functions can generate. */
    public List<String> categories() {
        return tables.categories();
    }

    /** The implementation of the functions, for a caller that wraps or restricts it. */
    public NameGeneratorFunctionsImpl newFunctions() {
        return new NameGeneratorFunctionsImpl(tables);
    }

    /** The functions of one request, to be passed to the inference. */
    public FunctionSet functions() {
        return inferenceServices.getFunctionRegistry().bind(NameGeneratorFunctions.class, newFunctions());
    }

}
