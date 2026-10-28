package com.github.enerccio.marginalia.extensions.googlegenai;

import com.github.enerccio.marginalia.domain.components.TokenizerKey;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.domain.service.TokenizerResolver;
import com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer.TokenizerStrategy;
import com.github.enerccio.marginalia.extensions.ExtensionAIFactory;
import com.github.enerccio.marginalia.extensions.ExtensionAIForm;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The provider Google Gemini: its AIs are {@link ExtensionAI}s with the id {@value #EXTENSION_ID}.
 */
public class GoogleGenAiFactory implements ExtensionAIFactory {

    /** The symbolic name of the bundle. Stored in every AI of the provider, never change it. */
    public static final String EXTENSION_ID = "google-genai";

    private final InferenceServices inferenceServices;
    private final Logger log;

    // the clients (HTTP connections and threads) of the services are closed when the extension is unloaded
    private final Set<GoogleGenAiInferenceService> services = Collections.synchronizedSet(
            Collections.newSetFromMap(new WeakHashMap<>()));

    GoogleGenAiFactory(InferenceServices inferenceServices, Logger log) {
        this.inferenceServices = inferenceServices;
        this.log = log;
    }

    @Override
    public String getExtensionId() {
        return EXTENSION_ID;
    }

    @Override
    public String getName() {
        return "Google Gemini";
    }

    @Override
    public InferenceService createInferenceService(ExtensionAI ai) {
        return createService(ai);
    }

    /**
     * A service for the AI that the caller closes itself, for a throw-away AI with the values of the dialog.
     */
    GoogleGenAiInferenceService createService(ExtensionAI ai) {
        GoogleGenAiSettings settings = GoogleGenAiSettings.of(ai, inferenceServices::revealSecret);
        GoogleGenAiInferenceService service = new GoogleGenAiInferenceService(ai, settings, log, inferenceServices::currentSessionId);
        services.add(service);
        return service;
    }

    /**
     * The tokens are counted by Google: AIs with the same endpoint, model and API key share the strategy the application found
     * to work, and the strategy asks the service of the AI it is called for.
     */
    @Override
    public TokenizerResolver createTokenizerResolver() {
        TokenizerStrategy google = new TokenizerStrategy() {
            @Override
            public long countTokens(AI ai, String text) throws Exception {
                if (!(inferenceServices.forAI(ai) instanceof GoogleGenAiInferenceService service)) {
                    throw new IllegalStateException("The extension " + EXTENSION_ID + " is not loaded");
                }
                return service.countTokensOnline(text);
            }

            @Override
            public String getName() {
                return "GOOGLE_COUNT_TOKENS";
            }
        };
        return new TokenizerResolver() {
            @Override
            public TokenizerKey keyFor(AI ai) {
                GoogleGenAiSettings settings = GoogleGenAiSettings.of((ExtensionAI) ai, inferenceServices::revealSecret);
                return TokenizerKey.of(EXTENSION_ID + "#" + (settings.baseUrl().isEmpty() ? "google" : settings.baseUrl()) + "#" + settings.model(),
                        settings.apiKey());
            }

            @Override
            public List<TokenizerStrategy> chain(TokenizerKey key, AI ai) {
                return List.of(google);
            }
        };
    }

    @Override
    public ExtensionAIForm createForm(ExtensionAI ai) {
        return new GoogleGenAiForm(this, ai);
    }

    /**
     * Closes the clients of all services, the extension is unloaded: a service that is used after that fails.
     */
    void close() {
        GoogleGenAiInferenceService[] open;
        synchronized (services) {
            open = services.toArray(new GoogleGenAiInferenceService[0]);
            services.clear();
        }
        for (GoogleGenAiInferenceService service : open) {
            service.close();
        }
    }
}
