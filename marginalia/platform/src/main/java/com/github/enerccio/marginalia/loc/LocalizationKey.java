package com.github.enerccio.marginalia.loc;

/**
 * A key of a UI text. Each layer has its own enum of keys with its own {@link TextBundle}s, one per language: the
 * platform {@link L} (its bundles are the {@link LocalizationBundle}s, which also give the language), the application
 * {@code M_L}. {@link Localization#getValue(LocalizationKey)} looks the key up in the bundles of its enum, so code that
 * shows texts of several layers keeps them as this type.
 * <p>
 * Only enums implement it: the methods are the ones every enum has.
 */
public interface LocalizationKey {

    String name();

    /**
     * The enum of the key, the one whose bundles have its texts.
     */
    Class<?> getDeclaringClass();
}
