package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceServices;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class InferenceServicesImpl implements InferenceServices {

    private List<InferenceProvider> inferenceProviders;
    private ConcurrentHashMap<AIType, InferenceProvider> providerCache = new ConcurrentHashMap<>();

    public List<InferenceProvider> getInferenceProviders() {
        return inferenceProviders;
    }

    public void setInferenceProviders(List<InferenceProvider> services) {
        this.inferenceProviders = services;
    }

    @Override
    public InferenceService forAI(AI ai) throws Exception {
        if (ai.getInferenceService() != null) {
            return ai.getInferenceService();
        }

        InferenceProvider provider = providerCache.getOrDefault(ai.getAiType(), null);
        if (provider == null) {
            AI test = new AI();
            test.setAiType(ai.getAiType());
            for (InferenceProvider p : inferenceProviders) {
                InferenceService service = p.create(test);
                if (service.getType() == test.getAiType()) {
                    provider = p;
                    providerCache.put(ai.getAiType(), p);
                    break;
                }
            }
        }

        if (provider == null)
            throw new IllegalArgumentException("Unknown provider: " + ai.getAiType());

        InferenceService service = provider.create(ai);
        ai.setInferenceService(service);
        return service;
    }

}
