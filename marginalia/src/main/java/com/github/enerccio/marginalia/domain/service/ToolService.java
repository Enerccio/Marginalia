package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.service.tool.*;

import java.util.List;

/**
 * Lets extensions (and the application) offer suites of tools to whoever lets the model call functions, without knowing
 * each other: a suite registers a {@link ToolProvider} under its bundle, a key and a human name, and a caller (the Lore
 * Generator, the Arc Outline...) shows them to the user with the {@code ToolSelectionButton}, keeps the user's
 * {@link ToolSelection} and turns it into the functions of a request with {@link #resolve}. Nothing is imported between
 * extensions, so one can be unloaded and installed again while another keeps running.
 * <p>
 * A provider is identified by its {@code bundleIdentifier}, which should be the symbolic name of its bundle
 * ({@code Bundle.getSymbolicName()}): the application removes all providers of that identifier (and the commands they
 * registered) when the extension is unloaded. Registering a key that is registered already replaces the provider and
 * keeps its place in the order. The key is part of what callers save, so don't rename it.
 */
public interface ToolService {

    /**
     * @param bundleIdentifier the symbolic name of the bundle of the provider
     * @param providerKey      the key of the provider inside its bundle, stable between versions (callers keep it)
     * @param providerName     the human name shown to the user ("Name provider")
     * @param provider         the suite; if it has {@link ToolProvider#commands()} they are registered with the command service
     */
    void registerProvider(String bundleIdentifier, String providerKey, String providerName, ToolProvider provider);

    /** Removes the provider and the commands it registered. */
    void unregisterProvider(String bundleIdentifier, String providerKey);

    /** Removes all the providers of the bundle and the commands they registered. */
    void unregisterAll(String bundleIdentifier);

    /**
     * The registered providers in the order of registration, with their items as they are now. A copy. Ask again when
     * you show them to the user: extensions come and go at any time.
     */
    List<ToolProviderInfo> getProviders();

    /**
     * The functions and the hints for the providers the selection turns on. A provider that is not registered (any more)
     * or whose checked items are all gone is left out, one that fails is logged and left out. Run on the thread of the
     * caller, so its session is the one providers see.
     *
     * @throws IllegalStateException when two providers have a function of the same name
     */
    ToolSet resolve(ToolSelection selection, ToolRequest request);

}
