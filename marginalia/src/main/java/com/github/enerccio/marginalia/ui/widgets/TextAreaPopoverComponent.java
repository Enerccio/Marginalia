package com.github.enerccio.marginalia.ui.widgets;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.textfield.TextArea;

public class TextAreaPopoverComponent extends TextArea {

    private final Icon infoIcon;
    private final Popover popover;

    public TextAreaPopoverComponent() {
        this(null);
    }

    public TextAreaPopoverComponent(String label) {
        super(label);

        infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.getStyle().set("cursor", "pointer");
        infoIcon.getStyle().set("color", "var(--lumo-secondary-text-color)");

        popover = new Popover();
        popover.setTarget(infoIcon);

        setSuffixComponent(infoIcon);
    }

    public Icon getInfoIcon() {
        return infoIcon;
    }

    public Popover getPopover() {
        return popover;
    }

    public void setPopoverContent(Component content) {
        popover.removeAll();
        if (content != null) {
            popover.add(content);
        }
    }

    public void addPopoverContent(Component... components) {
        popover.add(components);
    }
}