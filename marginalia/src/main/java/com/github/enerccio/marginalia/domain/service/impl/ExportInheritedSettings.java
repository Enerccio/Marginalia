package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.service.ExportSettingsService;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportTemplate;
import com.github.enerccio.marginalia.domain.service.ExporterService.Exporter;
import com.github.enerccio.marginalia.extensions.InheritedSetting;
import com.github.enerccio.marginalia.extensions.InheritedSettingRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The templates of the exporters (the title page, the front matter...) as settings that books and folders inherit, like the prompts: the book, else the
 * nearest folder above it, else the user's own version of the template (<i>Settings → Exports</i>), else the template of the exporter. Each template is an
 * {@link InheritedSetting} of its own, so the <i>Extension Settings</i> of a book and of a folder show them (one panel per exporter), the values live in the
 * attributes of the book and the folder and travel with the backups, and {@code ManuscriptService.resolveExtension} works out what applies to a book.
 */
public final class ExportInheritedSettings {

    private static final Logger log = LoggerFactory.getLogger(ExportInheritedSettings.class);

    private ExportInheritedSettings() {
    }

    /** Who the settings of an exporter are registered for, to remove them again. */
    static String owner(Exporter exporter) {
        return "marginalia.export." + exporter.getId();
    }

    /**
     * The key of the setting of a template, stable like the ids it is made of (it is what the books and the folders keep their values under).
     */
    public static String key(String exporterId, String templateKey) {
        return "export." + exporterId + "." + templateKey;
    }

    /**
     * Registers the templates of the exporter (nothing for an exporter without templates).
     *
     * @param settings the user level of the templates; it is asked when a setting is resolved and only reads
     */
    static void register(InheritedSettingRegistry registry, Exporter exporter, ExportSettingsService settings) {
        for (ExportTemplate template : exporter.getTemplates()) {
            registry.register(owner(exporter), InheritedSetting.textArea(key(exporter.getId(), template.key()), template::label)
                    .group(() -> exporter.getName() + " (." + exporter.getFileExtension() + ")")
                    .variables(template.dataClass())
                    .userValue(() -> {
                        try {
                            return settings.getTemplate(exporter.getId(), template.key());
                        } catch (Exception e) {
                            log.warn("The template {} of {} of the user could not be read: {}", template.key(), exporter.getId(), e.getMessage(), e);
                            return null;
                        }
                    })
                    .fallback(template::defaultText));
        }
    }

    static void unregister(InheritedSettingRegistry registry, Exporter exporter) {
        registry.unregisterAll(owner(exporter));
    }
}
