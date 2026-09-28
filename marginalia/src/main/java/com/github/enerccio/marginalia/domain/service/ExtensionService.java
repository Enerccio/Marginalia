package com.github.enerccio.marginalia.domain.service;

public interface ExtensionService {

    ExtendableMethodContext createContextHolder(Object owner);

    void registerDecorator(ExtensionDecorator decorator, String className, String methodName) throws Exception;
    void unregisterDecorator(ExtensionDecorator decorator);

    void onExtendableMethodEnter(Class<?> cls, String method, Object extendableSelf);
    void onExtendableMethodLeave(Class<?> cls, String method, Object extendableSelf, ExtendableMethodContext context, Throwable throwing);

    interface ExtendableMethodContext {

        void registerLocalVariable(String name, Object value, Class<?> type);
        <T> T getLocalVariable(String name, Class<T> type) throws Exception;
        boolean hasLocalVariable(String name);
        boolean hasLocalVariable(String name, Class<?> isOfType);

    }

    interface ExtensionDecorator {

        void onMethodEnter(Object instrumented) throws Exception;
        void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception;

    }



}
