package com.github.enerccio.marginalia.extensions;

/**
 * A bean that keeps what extensions registered with it, by owner (the symbolic name of the bundle). When an extension is
 * unloaded, {@code OsgiService} calls {@link #unregisterAll} of every such bean after {@code onExtensionUnload}, so what
 * the extension forgot to remove goes with it. The platform does not need to know the registries of the application.
 */
public interface ExtensionRegistrations {

    /**
     * Removes everything the owner registered.
     *
     * @param owner the symbolic name of the bundle of an extension
     */
    void unregisterAll(String owner);
}
