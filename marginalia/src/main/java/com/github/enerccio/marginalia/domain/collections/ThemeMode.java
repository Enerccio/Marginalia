package com.github.enerccio.marginalia.domain.collections;

/**
 * The color scheme of the UI a user chose, see {@code UserSetting#getThemeMode()}.
 */
public enum ThemeMode {

    DARK, LIGHT;

    /**
     * The other one.
     */
    public ThemeMode toggled() {
        return this == DARK ? LIGHT : DARK;
    }

}
