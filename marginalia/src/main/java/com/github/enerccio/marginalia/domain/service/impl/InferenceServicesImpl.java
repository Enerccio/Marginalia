package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.github.enerccio.marginalia.domain.service.FunctionRegistry;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.domain.traits.SupportedAI;
import com.github.enerccio.marginalia.extensions.ExtensionAIFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InferenceServicesImpl implements InferenceServices, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(InferenceServicesImpl.class);

    @Autowired
    private Configuration configuration;

    private final Map<String, ExtensionAIFactory> extensionFactories = new ConcurrentHashMap<>();

    private List<Class<? extends InferenceService>> inferenceProviders;
    private final Map<AIType, Constructor<? extends InferenceService>> providerMap = new EnumMap<>(AIType.class);
    private final FunctionRegistry functionRegistry = new FunctionRegistryImpl();
    private final List<ToolRoleResolver> toolRoleResolvers = new CopyOnWriteArrayList<>();

    public List<Class<? extends InferenceService>> getInferenceProviders() {
        return inferenceProviders;
    }

    public void setInferenceProviders(List<Class<? extends InferenceService>> services) {
        this.inferenceProviders = services;
    }

    @Override
    public FunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }

    @Override
    public void addToolRoleResolver(ToolRoleResolver resolver) {
        toolRoleResolvers.add(resolver);
    }

    @Override
    public void removeToolRoleResolver(ToolRoleResolver resolver) {
        toolRoleResolvers.remove(resolver);
    }

    @Override
    public String resolveToolRole(AI ai) {
        for (ToolRoleResolver resolver : toolRoleResolvers) {
            String role = resolver.toolRole(ai);
            if (role != null && !role.isBlank()) {
                return role;
            }
        }
        return DEFAULT_TOOL_ROLE;
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
        if (ai == null)
            return null;

        if (ai.getAiType() == AIType.EXTENSION) {
            return forExtensionAI((ExtensionAI) ai);
        }

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

    /**
     * The service of an AI of an extension comes from the factory of the extension. The cached one is used only while
     * the factory that created it is still the one registered: when the extension was unloaded the AI has no service
     * (null), when it was loaded again the service is created anew.
     */
    private InferenceService forExtensionAI(ExtensionAI ai) throws Exception {
        ExtensionAIFactory factory = extensionFactories.get(ai.getExtensionId());
        if (factory == null) {
            ai.setInferenceService(null);
            ai.setInferenceServiceFactory(null);
            return null;
        }
        if (ai.getInferenceService() != null && ai.getInferenceServiceFactory() == factory) {
            return ai.getInferenceService();
        }

        InferenceService service = factory.createInferenceService(ai);
        if (service == null) {
            throw new IllegalStateException("Extension " + ai.getExtensionId() + " created no inference service");
        }
        ai.setInferenceService(service);
        ai.setInferenceServiceFactory(factory);
        return service;
    }

    @Override
    public void registerExtensionFactory(ExtensionAIFactory factory) {
        String id = factory.getExtensionId();
        if (StringUtils.isBlank(id)) {
            throw new IllegalArgumentException("The extension id of " + factory.getClass().getName() + " is blank");
        }
        if (extensionFactories.putIfAbsent(id, factory) != null) {
            throw new IllegalStateException("There is an AI provider of an extension with the id " + id + " already");
        }
        log.info("Registered the AI provider of the extension {}", id);
    }

    @Override
    public void unregisterExtensionFactory(ExtensionAIFactory factory) {
        if (extensionFactories.remove(factory.getExtensionId(), factory)) {
            log.info("Unregistered the AI provider of the extension {}", factory.getExtensionId());
        }
    }

    @Override
    public ExtensionAIFactory getExtensionFactory(String extensionId) {
        return extensionId == null ? null : extensionFactories.get(extensionId);
    }

    @Override
    public List<ExtensionAIFactory> getExtensionFactories() {
        return extensionFactories.values().stream()
                .sorted(Comparator.comparing(ExtensionAIFactory::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public String protectSecret(String secret) {
        return configuration.encrypt(secret);
    }

    @Override
    public String revealSecret(String protectedSecret) {
        return configuration.decrypt(protectedSecret);
    }
}
