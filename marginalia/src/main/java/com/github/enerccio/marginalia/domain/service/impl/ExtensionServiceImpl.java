package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.tools.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExtensionServiceImpl implements ExtensionService {
    private static final Logger log = LoggerFactory.getLogger(ExtensionServiceImpl.class);

    private final Map<Pair<String, String>, List<ExtensionDecorator>> decoratorMap = new ConcurrentHashMap<>();

    @Override
    public ExtendableMethodContext createContextHolder(Object owner) {
        return new ContextImpl(owner);
    }

    @Override
    public void registerDecorator(ExtensionDecorator decorator, String className, String methodName) {
        Pair<String, String> identifier = Pair.of(className, methodName);
        decoratorMap.computeIfAbsent(identifier, _ -> new CopyOnWriteArrayList<>()).add(decorator);
    }

    @Override
    public void unregisterDecorator(ExtensionDecorator decorator) {
        for (List<ExtensionDecorator> decorators : decoratorMap.values()) {
            decorators.remove(decorator);
        }
    }

    @Override
    public void onExtendableMethodEnter(Class<?> cls, String method, Object extendableSelf) {
        List<ExtensionDecorator> decorators = getDecorators(cls, method);
        if (decorators.isEmpty()) return;

        for (ExtensionDecorator decorator : decorators) {
            try {
                decorator.onMethodEnter(extendableSelf);
            } catch (Throwable t) {
                log.error("Unhandled exception in extension '{}' while decorating {}.{}:",
                        decorator.getClass().getName(),
                        extendableSelf.getClass().getSimpleName(), method, t);
            }
        }
    }

    @Override
    public void onExtendableMethodLeave(Class<?> cls, String method, Object extendableSelf, ExtendableMethodContext context, Throwable throwing) {
        List<ExtensionDecorator> decorators = getDecorators(cls, method);
        if (decorators.isEmpty()) return;

        for (ExtensionDecorator decorator : decorators) {
            try {
                decorator.onMethodLeave(extendableSelf, context, throwing);
            } catch (ModCompatibilityException e) {
                log.warn("Mod '{}' skipped on {}.{}: {}",
                        decorator.getClass().getName(),
                        extendableSelf.getClass().getName(),
                        method,
                        e.getMessage());
            } catch (Throwable t) {
                log.error("Unhandled exception in extension '{}' while decorating {}.{}:",
                        decorator.getClass().getName(),
                        extendableSelf.getClass().getSimpleName(), method, t);
            }
        }
    }

    private List<ExtensionDecorator> getDecorators(Class<?> cls, String method) {
        Pair<String, String> key = Pair.of(cls.getName(), method);
        List<ExtensionDecorator> decorators = decoratorMap.get(key);
        return decorators != null ? decorators : Collections.emptyList();
    }

    private static class ContextImpl implements ExtendableMethodContext {
        private final Map<String, Pair<Object, Class<?>>> localVariables;
        private final Object targetInstance;

        private ContextImpl(Object targetInstance) {
            this.targetInstance = targetInstance;
            this.localVariables = new HashMap<>();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getLocalVariable(String name, Class<T> expectedType) throws Exception {
            Pair<Object, Class<?>> registeredPair = localVariables.get(name);
            if (registeredPair == null) {
                throw new ModCompatibilityException(String.format(
                        "Required component variable '%s' was not found in %s. " +
                                "The core UI code may have been refactored or renamed.",
                        name, targetInstance.getClass().getSimpleName()
                ));
            }

            Object val = registeredPair.getA();
            Class<?> registeredClass = registeredPair.getB();

            if (!expectedType.isAssignableFrom(registeredClass)) {
                throw new ModCompatibilityException(String.format(
                        "Component variable '%s' in %s is of type %s, but extension expected %s.",
                        name, targetInstance.getClass().getSimpleName(),
                        registeredClass.getName(), expectedType.getName()
                ));
            }
            return (T) val;
        }

        @Override
        public void registerLocalVariable(String name, Object value, Class<?> cls) {
            localVariables.put(name, Pair.of(value, cls));
        }

        @Override
        public boolean hasLocalVariable(String name) {
            return localVariables.containsKey(name);
        }

        @Override
        public boolean hasLocalVariable(String name, Class<?> isOfType) {
            Pair<Object, Class<?>> registeredPair = localVariables.get(name);
            return registeredPair != null && isOfType.isAssignableFrom(registeredPair.getB());
        }
    }

    private static class ModCompatibilityException extends RuntimeException {
        public ModCompatibilityException(String text) {
            super(text);
        }
    }
}