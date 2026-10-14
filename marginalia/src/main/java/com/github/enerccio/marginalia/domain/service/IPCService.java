package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import org.osgi.framework.Bundle;

import java.util.Map;

/**
 * Lets extensions (and the application) offer data about books and story parts to each other, without knowing each
 * other. A provider registers a callback under its bundle and a key; whoever wants the data lists the providers and
 * asks the ones it wants.
 * <p>
 * A provider is identified by its {@code bundleIdentifier}, which should be the symbolic name of its bundle
 * ({@link Bundle#getSymbolicName()}): the application removes all providers of that identifier when the extension is
 * unloaded, a provider that used another identifier has to {@link #unregisterAll} itself in {@code onExtensionUnload}.
 * Registering a key that is registered already replaces the provider and keeps its place in the order.
 * <p>
 * The callbacks run on the thread of the caller, so the caller's session and transaction are the ones in use. A provider
 * must not assume it is called only for books of the current user's choosing - check what it needs.
 */
public interface IPCService {

    /**
     * Data about a book, as text for a prompt.
     */
    @FunctionalInterface
    interface ManuscriptPluginData {

        /**
         * @param caller           the bundle of the caller, null when the caller is the application itself
         * @param callerIdentifier a string that identifies the caller, unique to it, like the name of the class of its
         *                         extension
         * @return the data, null or blank when there is nothing to give
         */
        String getDataFor(Manuscript manuscript, Bundle caller, String callerIdentifier) throws Exception;
    }

    /**
     * Data about a story part, as text to put next to the text of the part.
     */
    @FunctionalInterface
    interface ChatMessagePluginData {

        /**
         * @param caller           the bundle of the caller, null when the caller is the application itself
         * @param callerIdentifier a string that identifies the caller, unique to it, like the name of the class of its
         *                         extension
         * @return the data, null or blank when there is nothing to give
         */
        String getDataFor(ChatMessage message, Bundle caller, String callerIdentifier) throws Exception;
    }

    /**
     * @param bundleIdentifier the symbolic name of the bundle of the provider
     * @param providerKey      the key of the provider inside its bundle, stable between versions (callers keep it)
     * @param providerName     the name shown to the user
     */
    void registerManuscriptProvider(String bundleIdentifier, String providerKey, String providerName, ManuscriptPluginData callback);

    /**
     * @see #registerManuscriptProvider
     */
    void registerChatMessageProvider(String bundleIdentifier, String providerKey, String providerName, ChatMessagePluginData callback);

    void unregisterManuscriptProvider(String bundleIdentifier, String providerKey);

    void unregisterChatMessageProvider(String bundleIdentifier, String providerKey);

    /**
     * Removes all the providers (of books and of story parts) of the bundle.
     */
    void unregisterAll(String bundleIdentifier);

    /**
     * The providers of data about books: the identifiers of the bundles that registered, each with its providers as
     * key → name. Both levels are in the order of registration. A copy, it doesn't change afterwards.
     */
    Map<String, Map<String, String>> getManuscriptProviders();

    /**
     * The providers of data about story parts, like {@link #getManuscriptProviders()}.
     */
    Map<String, Map<String, String>> getChatMessageProviders();

    /**
     * Asks a provider for its data about the book.
     *
     * @param bundleIdentifier the bundle of the provider
     * @param providerKey      the key of the provider
     * @param caller           the bundle of the caller, null for the application
     * @param callerIdentifier identifies the caller
     * @return what the provider returned; null when there is no such provider (it may be gone, extensions are unloaded
     * at any time) or it had nothing
     * @throws Exception what the provider threw
     */
    String getManuscriptData(String bundleIdentifier, String providerKey, Manuscript manuscript, Bundle caller,
                             String callerIdentifier) throws Exception;

    /**
     * Asks a provider for its data about the story part, see {@link #getManuscriptData}.
     */
    String getChatMessageData(String bundleIdentifier, String providerKey, ChatMessage message, Bundle caller,
                              String callerIdentifier) throws Exception;
}
