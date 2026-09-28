package com.github.enerccio.marginalia.domain.service;

public interface ExtensionService {

    ExtendableMethodContext createContextHolder(Object owner);

    void registerDecorator(ExtensionDecorator decorator, String className, String methodName) throws Exception;
    void unregisterDecorator(ExtensionDecorator decorator);

    void onExtendableMethodEnter(Class<?> cls, Object extendableSelf, String method);
    void onExtendableMethodLeave(Class<?> cls, Object extendableSelf, ExtendableMethodContext context, String method, Throwable throwing);

    interface ExtendableMethodContext {

        void registerLocalVariable(String name, Object value, Class<?> type);
        <T> T getLocalVariable(String name, Class<T> type) throws Exception;
        boolean hasLocalVariable(String name);
        boolean hasLocalVariable(String name, Class<?> isOfType);
        <T> T getReflectiveFieldValue(Object object, String field, Class<T> fieldReturnType);
        <T> void setReflectiveFieldValue(Object object, String field, T value, Class<T> fieldReturnType);

    }

    interface ExtensionDecorator {

        void onMethodEnter(Object instrumented) throws Exception;
        void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception;

    }



}
