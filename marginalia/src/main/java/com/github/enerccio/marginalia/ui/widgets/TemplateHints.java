package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.domain.service.TemplateService;
import com.github.enerccio.marginalia.domain.templates.TemplateData;
import com.github.enerccio.marginalia.domain.templates.macros.Macros;
import com.github.enerccio.marginalia.domain.traits.LocalizedTemplateDescription;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Content of template hint popovers: "fill default" button, template variables and supported macros.
 */
public final class TemplateHints {

    /**
     * Properties of the shared template context, available in every template (see TemplateContext.property()).
     */
    private static final Map<String, L> CONTEXT_VARIABLES = new LinkedHashMap<>();

    static {
        CONTEXT_VARIABLES.put("povCharacter", L.DESC_TEMPLATE_POV_CHARACTER);
        CONTEXT_VARIABLES.put("sceneSetting", L.DESC_TEMPLATE_SCENE_SETTING);
        CONTEXT_VARIABLES.put("presentCharacters", L.DESC_TEMPLATE_PRESENT_CHARACTERS);
        CONTEXT_VARIABLES.put("instructions", L.DESC_TEMPLATE_INSTRUCTIONS);
        CONTEXT_VARIABLES.put("narrativePov", L.DESC_TEMPLATE_NARRATIVE_POV);
        CONTEXT_VARIABLES.put("narrativeTense", L.DESC_TEMPLATE_NARRATIVE_TENSE);
        CONTEXT_VARIABLES.put("style", L.DESC_TEMPLATE_STYLE);
        CONTEXT_VARIABLES.put("manuscriptName", L.DESC_TEMPLATE_MANUSCRIPT_NAME);
        CONTEXT_VARIABLES.put("manuscriptDescription", L.DESC_TEMPLATE_MANUSCRIPT_DESCRIPTION);
    }

    private TemplateHints() {
    }

    public static Component create(Localization loc, HasValue<?, String> field, Popover popover,
                                   Class<? extends TemplateData> clazz, String defaultValue) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.setWidth("380px");

        if (StringUtils.isNotBlank(defaultValue)) {
            Button fillDefaultButton = new Button(loc.getValue(L.LABEL_FILL_DEFAULT), event -> {
                field.setValue(defaultValue);
                if (popover != null) {
                    popover.close();
                }
            });
            fillDefaultButton.setThemeName("primary small");
            fillDefaultButton.setWidthFull();
            layout.add(fillDefaultButton);
        }

        if (clazz != null) {
            Span header = new Span(loc.getValue(L.LABEL_AVAILABLE_VARIABLES));
            header.getStyle().set("font-weight", "bold");
            header.getStyle().set("font-size", "var(--lumo-font-size-m)");
            layout.add(header);

            Set<String> declared = new HashSet<>();
            for (Field f : clazz.getDeclaredFields()) {
                LocalizedTemplateDescription descAnnot = f.getAnnotation(LocalizedTemplateDescription.class);
                if (descAnnot == null) {
                    String getterName = "get" + StringUtils.capitalize(f.getName());
                    try {
                        Method method = clazz.getMethod(getterName);
                        descAnnot = method.getAnnotation(LocalizedTemplateDescription.class);
                    } catch (Exception ignored) {
                    }
                }

                String descriptionText = descAnnot != null ? loc.getValue(descAnnot.loc()) : "";
                layout.add(createItem("{{" + f.getName() + "}}", descriptionText));
                declared.add(f.getName());
            }

            for (Map.Entry<String, L> variable : CONTEXT_VARIABLES.entrySet()) {
                if (!declared.contains(variable.getKey())) {
                    layout.add(createItem("{{" + variable.getKey() + "}}", loc.getValue(variable.getValue())));
                }
            }

            layout.add(createMacroList(loc));
        }

        return layout;
    }

    /**
     * Shows names the template uses but that aren't defined (they render as "Error") as helper text of the field. It's
     * only a warning, the template can still be saved. Null or result without warnings clears it.
     */
    public static void showWarnings(Localization loc, HasHelper field, TemplateService.ValidationResult result) {
        if (result == null || !result.hasWarnings()) {
            field.setHelperText(null);
            return;
        }
        field.setHelperText(loc.getValue(L.MSG_TEMPLATE_UNKNOWN_NAMES) + String.join(", ", result.unknownNames()));
    }

    /**
     * Collapsible list of supported SillyTavern compatible macros.
     */
    public static Component createMacroList(Localization loc) {
        VerticalLayout macros = new VerticalLayout();
        macros.setPadding(false);
        macros.setSpacing(true);
        for (Macros.MacroHint hint : Macros.hints()) {
            macros.add(createItem(hint.signature(), loc.getValue(hint.description())));
        }

        Details details = new Details(loc.getValue(L.LABEL_AVAILABLE_MACROS), macros);
        details.setWidthFull();
        details.setOpened(false);
        return details;
    }

    private static Component createItem(String signature, String description) {
        VerticalLayout itemLayout = new VerticalLayout();
        itemLayout.setPadding(false);
        itemLayout.setSpacing(false);

        Span varSpan = new Span(signature);
        varSpan.getStyle().set("font-family", "monospace");
        varSpan.getStyle().set("font-weight", "bold");
        varSpan.getStyle().set("color", "var(--lumo-primary-color)");

        Span descSpan = new Span(description);
        descSpan.getStyle().set("font-size", "var(--lumo-font-size-s)");
        descSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");

        itemLayout.add(varSpan, descSpan);
        return itemLayout;
    }
}
