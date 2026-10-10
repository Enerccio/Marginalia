package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.instruct.verify.ExtensionVerification;
import org.osgi.framework.Bundle;

import java.io.File;

public interface ExtensionService {

    ExtendableMethodContext createContextHolder(Object owner);

    void registerDecorator(ExtensionDecorator decorator, String className, String methodName) throws Exception;
    void unregisterDecorator(ExtensionDecorator decorator);

    /**
     * Checks that the extension fits this version of the application, without running it. The classes of the
     * extension are scanned (the classes of {@code jar} are read from the installed {@code bundle}); every
     * {@code registerDecorator} call is followed to the class and method it decorates, and the registered decorator is
     * checked for what it asks from its {@link ExtendableMethodContext} - arguments, local variables, fields and
     * methods - against the real classes of the application. Only the decorators themselves are scanned: when a
     * context is handed over to another class, nothing is checked there.
     * <p>
     * The report is written next to the JAR, named after it: {@code name.valid} or {@code name.invalid} (the one of
     * the opposite result is deleted).
     *
     * @return the result, {@link ExtensionVerification#valid()} is false when the extension asks for something that
     * does not exist
     */
    ExtensionVerification verifyExtension(Bundle bundle, File jar) throws Exception;

    void onExtendableMethodEnter(Class<?> cls, Object extendableSelf, ExtendableMethodContext context, String method);
    void onExtendableMethodLeave(Class<?> cls, Object extendableSelf, ExtendableMethodContext context, String method, Throwable throwing);

    interface ExtendableMethodContext {

        void registerLocalVariable(String name, Object value, Class<?> type);
        <T> T getLocalVariable(String name, Class<T> type) throws Exception;
        boolean hasLocalVariable(String name);
        boolean hasLocalVariable(String name, Class<?> isOfType);
        <T> T getReflectiveFieldValue(Object object, String field, Class<T> fieldReturnType);
        <T> void setReflectiveFieldValue(Object object, String field, T value, Class<T> fieldReturnType);
        MethodArgumentRefresher registerMethodArgument(String name, Object value, Class<?> type);
        <T> T getMethodArgument(String name, Class<T> type) throws Exception;
        boolean hasMethodArgument(String name);
        boolean hasMethodArgument(String name, Class<?> isOfType);
        <T> T callReflectiveMethod(Object object, String methodName, Class<?>[] argumentTypes, Object[] arguments, Class<T> returnType);
    }

    interface MethodArgumentRefresher {
        Object get();
    }

    interface ExtensionDecorator {

        void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception;
        void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception;

    }



}
