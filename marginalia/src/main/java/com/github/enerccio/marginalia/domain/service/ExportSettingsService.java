package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.service.ExporterService.ExportTemplate;
import com.github.enerccio.marginalia.domain.service.ExporterService.Exporter;

import java.util.Map;

/**
 * What the current user wants of the exports of each format (see {@link Exporter}), kept in their {@code ExportSettings}: the style the
 * format is exported in, and their versions of its templates. The export dialog starts from these; nothing here changes what the author can
 * choose for one export.
 */
public interface ExportSettingsService {

    /**
     * The style the user chose for the format in the settings, as it is stored (not checked).
     *
     * @return id of the style, null when the user chose none
     */
    String getChosenStyleId(String exporterId) throws Exception;

    /**
     * The style the export dialog preselects for the format: the one the user chose, if it still exists and is a style for exports, else
     * the style of the exporter itself ({@link Exporter#getDefaultStyleId()}).
     *
     * @return id of the style, null when the exporter has none
     */
    String getStyleId(Exporter exporter) throws Exception;

    /**
     * Makes the style (one for exports, built in or the user's own) the style of the format, null returns the format to its own.
     *
     * @throws IllegalArgumentException when no such style for exports exists
     */
    void setStyleId(String exporterId, String styleId) throws Exception;

    /**
     * The template of the exporter the user changed, null when they did not.
     */
    String getTemplate(String exporterId, String templateKey) throws Exception;

    /**
     * The changed templates of the exporter by the key of the template, for {@link ExporterService.ExportOptions#templates()}; the ones
     * the user did not change are not in it.
     */
    Map<String, String> getTemplates(String exporterId) throws Exception;

    /**
     * Stores the user's version of the template; a blank text returns it to the default of the exporter.
     *
     * @throws IllegalArgumentException when the text is not a valid template
     */
    void setTemplate(String exporterId, ExportTemplate template, String text) throws Exception;

}
