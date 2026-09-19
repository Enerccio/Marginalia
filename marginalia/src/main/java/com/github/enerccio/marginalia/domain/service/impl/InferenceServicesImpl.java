package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.domain.traits.SupportedAI;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Constructor;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class InferenceServicesImpl implements InferenceServices, InitializingBean {

    private List<Class<? extends InferenceService>> inferenceProviders;
    private final Map<AIType, Constructor<? extends InferenceService>> providerMap = new EnumMap<>(AIType.class);

    public List<Class<? extends InferenceService>> getInferenceProviders() {
        return inferenceProviders;
    }

    public void setInferenceProviders(List<Class<? extends InferenceService>> services) {
        this.inferenceProviders = services;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (inferenceProviders == null) {
            return;
        }

        for (Class<? extends InferenceService> clazz : inferenceProviders) {
            SupportedAI annotation = clazz.getAnnotation(SupportedAI.class);
            if (annotation == null) {
                throw new IllegalStateException("Class " + clazz.getName() + " missing @SupportedAI annotation");
            }

            Constructor<? extends InferenceService> constructor = clazz.getConstructor(AI.class);
            providerMap.put(annotation.value(), constructor);
        }
    }

    @Override
    public InferenceService forAI(AI ai) throws Exception {
        if (ai.getInferenceService() != null) {
            return ai.getInferenceService();
        }

        Constructor<? extends InferenceService> constructor = providerMap.get(ai.getAiType());
        if (constructor == null) {
            throw new IllegalArgumentException("Unknown provider for AI type: " + ai.getAiType());
        }

        InferenceService service = constructor.newInstance(ai);
        ai.setInferenceService(service);
        return service;
    }
}