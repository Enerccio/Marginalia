package com.github.enerccio.marginalia.loc;

import java.util.Map;

/**
 * The texts of one enum of keys ({@link LocalizationKey}) in one language. The platform's are the
 * {@link LocalizationBundle}s; a layer above the platform adds the bundles of its own enum with
 * {@link Localization#registerTexts}, and an extension with
 * {@link com.github.enerccio.marginalia.extensions.ExtensionHasLocalization#getTextBundles()}. A text a bundle doesn't
 * have is taken from the bundle of the default language. Extend {@link TextBundleBase} to fill it with {@code setValue}.
 *
 * @param <K> the enum of the keys
 */
public interface TextBundle<K extends Enum<K> & LocalizationKey> {

    Language getLanguage();

    /**
     * The enum of the keys.
     */
    Class<K> getKeys();

    /**
     * The text of the key, {@code null} when the bundle doesn't have it.
     */
    String getValue(K key);

    /**
     * All texts of the bundle.
     */
    Map<K, String> getMessages();
}
