package com.github.enerccio.marginalia.loc;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Base of a {@link TextBundle}: {@link #loadMessages()} fills the texts with {@link #setValue}.
 */
public abstract class TextBundleBase<K extends Enum<K> & LocalizationKey> implements TextBundle<K> {

    private final Class<K> keys;
    private final Map<K, String> messages;

    protected TextBundleBase(Class<K> keys) {
        this.keys = keys;
        this.messages = new EnumMap<>(keys);
        loadMessages();
    }

    protected abstract void loadMessages();

    protected void setValue(K key, String text) {
        messages.put(key, text);
    }

    @Override
    public Class<K> getKeys() {
        return keys;
    }

    @Override
    public String getValue(K key) {
        return messages.get(key);
    }

    @Override
    public Map<K, String> getMessages() {
        return Collections.unmodifiableMap(messages);
    }
}
