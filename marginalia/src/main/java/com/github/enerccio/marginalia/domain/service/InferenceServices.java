package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.github.enerccio.marginalia.extensions.ExtensionAIFactory;

import java.util.List;

public interface InferenceServices {

    /**
     * The inference service of the AI, created once and cached in the AI. For an
     * {@link ExtensionAI} it is created by the
     * {@link ExtensionAIFactory} of its extension.
     *
     * @return null when there is no AI, or when the extension of an ExtensionAI is not loaded (see
     * {@link #forAIOrThrow})
     */
    InferenceService forAI(AI ai) throws Exception;

    /**
     * Like {@link #forAI}, for code that can't go on without the service.
     *
     * @return the service, null only when there is no AI
     * @throws InferenceException of the type {@link InferenceException.Type#EXTENSION_UNAVAILABLE} when the extension of
     *                            the AI is not loaded, the user is told to load it
     */
    default InferenceService forAIOrThrow(AI ai) throws Exception {
        InferenceService service = forAI(ai);
        if (service == null && ai != null && ai.getAiType() == AIType.EXTENSION) {
            String extensionId = ai instanceof ExtensionAI extensionAI ? extensionAI.getExtensionId() : "?";
            throw new InferenceException(InferenceException.Type.EXTENSION_UNAVAILABLE, null, null,
                    new IllegalStateException("Extension " + extensionId + " is not loaded"));
        }
        return service;
    }

    /**
     * Makes the provider of an extension available: AIs of the type Extension with its id can be created and used.
     * Called by the extension when it is loaded, undone with {@link #unregisterExtensionFactory} when it is unloaded.
     *
     * @throws IllegalStateException when another factory has the same id
     */
    void registerExtensionFactory(ExtensionAIFactory factory);

    void unregisterExtensionFactory(ExtensionAIFactory factory);

    /**
     * @return the factory with the id, null when the extension is not loaded
     */
    ExtensionAIFactory getExtensionFactory(String extensionId);

    /**
     * The factories of the extensions that are loaded now, ordered by name.
     */
    List<ExtensionAIFactory> getExtensionFactories();

    /**
     * Encrypts a secret (an API key, a token) with the installation key, for the settings of an
     * {@link ExtensionAI}: they are stored as they are, the AI of an
     * extension has no encrypted column. Null and empty values are returned as they are.
     */
    String protectSecret(String secret);

    /**
     * Decrypts what {@link #protectSecret} returned; null for a secret that was encrypted with another installation's key.
     */
    String revealSecret(String protectedSecret);

    /**
     * The functions the LLM can call, compiled once per interface. See {@link FunctionRegistry}.
     */
    FunctionRegistry getFunctionRegistry();

    /**
     * Lets an extension change the role the results of function calls are sent with ({@link #DEFAULT_TOOL_ROLE}), for
     * models whose chat template expects another name. Remove it again when the extension is unloaded.
     */
    void addToolRoleResolver(ToolRoleResolver resolver);

    void removeToolRoleResolver(ToolRoleResolver resolver);

    /**
     * The role to send the results of function calls to the AI's model with: the answer of the first resolver that
     * has one, otherwise {@link #DEFAULT_TOOL_ROLE}.
     */
    String resolveToolRole(AI ai);

    /** The role of the results of function calls in the OpenAI chat API. */
    String DEFAULT_TOOL_ROLE = "tool";

    interface ToolRoleResolver {

        /**
         * @return the role for the AI's model, null to leave it to the other resolvers
         */
        String toolRole(AI ai);

    }

    interface InferenceProvider {

        InferenceService create(AI ai) throws Exception;

    }

}
