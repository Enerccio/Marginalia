package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.IPCService;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;

import java.util.LinkedHashMap;
import java.util.Map;

public class IPCServiceImpl implements IPCService {

    private record Provider<T>(String name, T callback) {
    }

    private final Registry<ManuscriptPluginData> manuscripts = new Registry<>();
    private final Registry<ChatMessagePluginData> messages = new Registry<>();

    @Override
    public void registerManuscriptProvider(String bundleIdentifier, String providerKey, String providerName, ManuscriptPluginData callback) {
        manuscripts.register(bundleIdentifier, providerKey, providerName, callback);
    }

    @Override
    public void registerChatMessageProvider(String bundleIdentifier, String providerKey, String providerName, ChatMessagePluginData callback) {
        messages.register(bundleIdentifier, providerKey, providerName, callback);
    }

    @Override
    public void unregisterManuscriptProvider(String bundleIdentifier, String providerKey) {
        manuscripts.unregister(bundleIdentifier, providerKey);
    }

    @Override
    public void unregisterChatMessageProvider(String bundleIdentifier, String providerKey) {
        messages.unregister(bundleIdentifier, providerKey);
    }

    @Override
    public void unregisterAll(String bundleIdentifier) {
        manuscripts.unregisterAll(bundleIdentifier);
        messages.unregisterAll(bundleIdentifier);
    }

    @Override
    public Map<String, Map<String, String>> getManuscriptProviders() {
        return manuscripts.names();
    }

    @Override
    public Map<String, Map<String, String>> getChatMessageProviders() {
        return messages.names();
    }

    @Override
    public String getManuscriptData(String bundleIdentifier, String providerKey, Manuscript manuscript, Bundle caller,
                                    String callerIdentifier) throws Exception {
        ManuscriptPluginData callback = manuscripts.find(bundleIdentifier, providerKey);
        // called outside of the lock, a provider may take long or call the service itself
        return callback == null ? null : callback.getDataFor(manuscript, caller, callerIdentifier);
    }

    @Override
    public String getChatMessageData(String bundleIdentifier, String providerKey, ChatMessage message, Bundle caller,
                                     String callerIdentifier) throws Exception {
        ChatMessagePluginData callback = messages.find(bundleIdentifier, providerKey);
        return callback == null ? null : callback.getDataFor(message, caller, callerIdentifier);
    }

    /**
     * The providers of one kind: bundle → key → provider, both levels in the order of registration.
     */
    private static final class Registry<T> {

        private final Map<String, Map<String, Provider<T>>> providers = new LinkedHashMap<>();

        synchronized void register(String bundleIdentifier, String providerKey, String providerName, T callback) {
            if (StringUtils.isBlank(bundleIdentifier) || StringUtils.isBlank(providerKey)) {
                throw new IllegalArgumentException("The bundle identifier and the provider key are required");
            }
            if (callback == null) {
                throw new IllegalArgumentException("The provider callback is required");
            }
            // putting an existing key again keeps its place
            providers.computeIfAbsent(bundleIdentifier, _ -> new LinkedHashMap<>())
                    .put(providerKey, new Provider<>(StringUtils.defaultIfBlank(providerName, providerKey), callback));
        }

        synchronized void unregister(String bundleIdentifier, String providerKey) {
            Map<String, Provider<T>> bundle = providers.get(bundleIdentifier);
            if (bundle != null) {
                bundle.remove(providerKey);
                if (bundle.isEmpty()) {
                    providers.remove(bundleIdentifier);
                }
            }
        }

        synchronized void unregisterAll(String bundleIdentifier) {
            providers.remove(bundleIdentifier);
        }

        synchronized T find(String bundleIdentifier, String providerKey) {
            Map<String, Provider<T>> bundle = providers.get(bundleIdentifier);
            Provider<T> provider = bundle == null ? null : bundle.get(providerKey);
            return provider == null ? null : provider.callback();
        }

        synchronized Map<String, Map<String, String>> names() {
            Map<String, Map<String, String>> result = new LinkedHashMap<>();
            providers.forEach((bundle, bundleProviders) -> {
                Map<String, String> names = new LinkedHashMap<>();
                bundleProviders.forEach((key, provider) -> names.put(key, provider.name()));
                result.put(bundle, java.util.Collections.unmodifiableMap(names));
            });
            return java.util.Collections.unmodifiableMap(result);
        }
    }
}
