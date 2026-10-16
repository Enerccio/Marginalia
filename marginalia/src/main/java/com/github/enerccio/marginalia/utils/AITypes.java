package com.github.enerccio.marginalia.utils;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.extensions.ExtensionAIFactory;
import com.github.enerccio.marginalia.loc.Localization;

/**
 * The type of an AI as the user sees it in the lists.
 */
public final class AITypes {

    private AITypes() {
    }

    /**
     * The localized type of the AI; for an AI of an extension the name of its provider, or the type with the id of the
     * extension when it is not loaded.
     */
    public static String label(Localization loc, InferenceServices inferenceServices, AI ai) {
        String type = loc.getValue(loc.getAIType(ai.getAiType()));
        if (ai instanceof ExtensionAI extensionAI) {
            ExtensionAIFactory factory = inferenceServices.getExtensionFactory(extensionAI.getExtensionId());
            return factory != null ? factory.getName() : type + ": " + extensionAI.getExtensionId();
        }
        return type;
    }

}
