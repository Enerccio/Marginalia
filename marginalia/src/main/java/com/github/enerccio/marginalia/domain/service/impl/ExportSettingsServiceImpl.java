package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.settings.ExportSettings;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.ExportSettingsService;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportTemplate;
import com.github.enerccio.marginalia.domain.service.ExporterService.Exporter;
import com.github.enerccio.marginalia.domain.service.PartStyleService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.service.TemplateService;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExportSettingsServiceImpl implements ExportSettingsService {

    @Autowired
    private SettingService settingService;

    @Autowired
    private PartStyleService partStyleService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private User currentUser;

    @Override
    @CommonTxReadOnly
    public String getChosenStyleId(String exporterId) throws Exception {
        return text(read().getStyles().get(exporterId));
    }

    @Override
    @CommonTxReadOnly
    public String getStyleId(Exporter exporter) throws Exception {
        String chosen = getChosenStyleId(exporter.getId());
        if (chosen != null) {
            PartStyle style = partStyleService.find(chosen);
            if (style != null && style.isForExport()) {
                return chosen;
            }
            // deleted since, or no longer for exports: the format has its own again
        }
        return exporter.getDefaultStyleId();
    }

    @Override
    @CommonTx
    public void setStyleId(String exporterId, String styleId) throws Exception {
        if (StringUtils.isNotBlank(styleId)) {
            PartStyle style = partStyleService.find(styleId);
            if (style == null || !style.isForExport()) {
                throw new IllegalArgumentException("No style for exports " + styleId);
            }
        }
        ExportSettings settings = write();
        if (StringUtils.isBlank(styleId)) {
            settings.getStyles().remove(exporterId);
        } else {
            settings.getStyles().add(exporterId, new JsonPrimitive(styleId));
        }
        settingService.save(settings);
    }

    @Override
    @CommonTxReadOnly
    public String getTemplate(String exporterId, String templateKey) throws Exception {
        JsonElement own = read().getTemplates().get(exporterId);
        return own == null || !own.isJsonObject() ? null : text(own.getAsJsonObject().get(templateKey));
    }

    @Override
    @CommonTxReadOnly
    public Map<String, String> getTemplates(String exporterId) throws Exception {
        Map<String, String> changed = new LinkedHashMap<>();
        JsonElement own = read().getTemplates().get(exporterId);
        if (own != null && own.isJsonObject()) {
            own.getAsJsonObject().entrySet().forEach(entry -> {
                String text = text(entry.getValue());
                if (text != null) {
                    changed.put(entry.getKey(), text);
                }
            });
        }
        return changed;
    }

    @Override
    @CommonTx
    public void setTemplate(String exporterId, ExportTemplate template, String text) throws Exception {
        if (StringUtils.isNotBlank(text)) {
            TemplateService.ValidationResult result = templateService.isValidTemplate(text, "export." + exporterId + "." + template.key(), template.dataClass());
            if (!result.isValid()) {
                throw new IllegalArgumentException(result.errorMessage());
            }
        }
        ExportSettings settings = write();
        JsonObject own = settings.getTemplates().has(exporterId) && settings.getTemplates().get(exporterId).isJsonObject()
                ? settings.getTemplates().getAsJsonObject(exporterId) : new JsonObject();
        if (StringUtils.isBlank(text)) {
            own.remove(template.key());
        } else {
            own.add(template.key(), new JsonPrimitive(text));
        }
        if (own.size() == 0) {
            settings.getTemplates().remove(exporterId);
        } else {
            settings.getTemplates().add(exporterId, own);
        }
        settingService.save(settings);
    }

    // reads never write: a user who never changed anything has no row
    private ExportSettings read() throws Exception {
        return settingService.findOrBlank(ExportSettings.class, currentUser);
    }

    private ExportSettings write() throws Exception {
        return settingService.getOrCreate(ExportSettings.class);
    }

    private static String text(JsonElement element) {
        return element == null || !element.isJsonPrimitive() || StringUtils.isBlank(element.getAsString()) ? null : element.getAsString();
    }
}
