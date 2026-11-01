package com.github.enerccio.marginalia.domain.model.impl.settings;

import com.github.enerccio.marginalia.domain.model.Setting;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import com.google.gson.JsonObject;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

/**
 * What a user wants of their exports (see {@code Exporter}): the style each format is exported in unless the author chooses another in the
 * export dialog, and their own versions of the templates the formats render (the title page). A row of the settings table like
 * {@link StylesSetting}, one per user, created when the user first changes something; both are keyed by the id of the exporter. Read and
 * written through {@code ExportSettingsService}.
 */
@Entity
@DiscriminatorValue("ExportSettings")
public class ExportSettings extends Setting {

    /**
     * The id of the style for exports (see {@code PartStyle}) by the id of the exporter; an exporter that is not here uses its own.
     */
    @ExtendedAttribute(inject = true, injectPrefix = "styles")
    @Transient
    private JsonObject styles = new JsonObject();

    /**
     * The changed templates by the id of the exporter, each an object of the texts by the key of the template.
     */
    @ExtendedAttribute(inject = true, injectPrefix = "templates")
    @Transient
    private JsonObject templates = new JsonObject();

    public JsonObject getStyles() {
        return styles;
    }

    public void setStyles(JsonObject styles) {
        this.styles = styles == null ? new JsonObject() : styles;
    }

    public JsonObject getTemplates() {
        return templates;
    }

    public void setTemplates(JsonObject templates) {
        this.templates = templates == null ? new JsonObject() : templates;
    }
}
