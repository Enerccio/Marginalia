package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.CssStyles;
import com.vaadin.flow.component.html.Input;

/**
 * The check box of a part while the mass delete of the story is on. A plain browser check box that reports one event per click with
 * everything the story needs: the state it has after the click and whether Shift was held, so a range of parts can be ticked with
 * one click. (The Vaadin check box tells the server about the click and about the new value in separate events.)
 */
public class MassDeleteBox extends Input {

    /**
     * What the user did to the box.
     */
    @FunctionalInterface
    public interface ClickListener {
        void clicked(boolean checked, boolean shift);
    }

    public MassDeleteBox(ClickListener listener) {
        setType("checkbox");
        addClassName(CssStyles.MASS_DELETE_BOX);
        getElement().addEventListener("click", event -> {
                    boolean checked = event.getEventData().get("element.checked").asBoolean();
                    // the server must know what the browser shows, or setting the same state later would be skipped
                    getElement().setProperty("checked", checked);
                    listener.clicked(checked, event.getEventData().get("event.shiftKey").asBoolean());
                })
                .addEventData("element.checked").addEventData("event.shiftKey");
    }

    public void setChecked(boolean checked) {
        getElement().setProperty("checked", checked);
    }
}
