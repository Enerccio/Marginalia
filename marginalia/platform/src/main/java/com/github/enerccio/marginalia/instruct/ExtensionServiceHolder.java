package com.github.enerccio.marginalia.instruct;

import com.github.enerccio.marginalia.domain.service.ExtensionService;

/**
 * Where the hooks woven into {@code @Extendable} classes find the {@link ExtensionService}: the service of the
 * running Spring context binds itself when it is created and unbinds when the context is closed. Without a bound
 * service ({@code null}) an instrumented method runs as if it had no hooks.
 */
public final class ExtensionServiceHolder {

    private static volatile ExtensionService instance;

    private ExtensionServiceHolder() {

    }

    @SuppressWarnings("unused") // called by the instrumented methods
    public static ExtensionService getInstance() {
        return instance;
    }

    public static void bind(ExtensionService service) {
        instance = service;
    }

    /**
     * Does nothing when another service was bound in the meantime (a newer Spring context).
     */
    public static void unbind(ExtensionService service) {
        if (instance == service) {
            instance = null;
        }
    }
}
