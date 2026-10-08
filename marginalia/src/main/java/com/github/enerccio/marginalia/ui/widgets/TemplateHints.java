package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.domain.templates.TemplateData;
import com.github.enerccio.marginalia.domain.templates.macros.Macros;
import com.github.enerccio.marginalia.domain.traits.LocalizedTemplateDescription;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Content of template hint popovers: "fill default" button, template variables and supported macros.
 */
public final class TemplateHints {

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
            }

            layout.add(createMacroList(loc));
        }

        return layout;
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
