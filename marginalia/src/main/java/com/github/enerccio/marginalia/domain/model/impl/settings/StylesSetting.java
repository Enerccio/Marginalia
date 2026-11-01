package com.github.enerccio.marginalia.domain.model.impl.settings;

import com.github.enerccio.marginalia.domain.model.Setting;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import com.google.gson.JsonObject;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

/**
 * The text styles of a user (see {@code PartStyle}): the ones they made or duplicated in the settings, and which one the books use
 * when they choose none. A row of the settings table like {@link UserSetting}, one per user, created when the user first changes a
 * style; the built in styles are code, not stored here. Read and written through {@code PartStyleService}.
 */
@Entity
@DiscriminatorValue("StylesSetting")
public class StylesSetting extends Setting {

    /**
     * The styles by id, each the JSON form of a {@code PartStyle}.
     */
    @ExtendedAttribute(inject = true, injectPrefix = "styles")
    @Transient
    private JsonObject styles = new JsonObject();

    /**
     * The id of the style (built in or one of {@link #styles}) the books of the user use unless they have their own; null is the
     * plain look of the application.
     */
    @ExtendedAttribute
    @Transient
    private String defaultStyle;

    public JsonObject getStyles() {
        return styles;
    }

    public void setStyles(JsonObject styles) {
        this.styles = styles == null ? new JsonObject() : styles;
    }

    public String getDefaultStyle() {
        return defaultStyle;
    }

    public void setDefaultStyle(String defaultStyle) {
        this.defaultStyle = defaultStyle;
    }
}
