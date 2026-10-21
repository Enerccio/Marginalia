package com.github.enerccio.marginalia.ui.widgets;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.function.SerializableRunnable;
import tools.jackson.databind.node.ObjectNode;

/**
 * A context menu that is filled when the user opens it, not when it is created. Every item costs a server-side
 * listener and a rebuild of the event settings of the menu, which adds up for a story with a menu on each part. Flow
 * makes a server round-trip before it opens the menu, the items added in {@code beforeOpen} are sent before the menu
 * is shown.
 */
public class LazyContextMenu extends ContextMenu {

    private final SerializableRunnable beforeOpen;

    /**
     * @param target     the component the menu opens on
     * @param beforeOpen runs each time the menu is about to open, adds or updates the items
     */
    public LazyContextMenu(Component target, SerializableRunnable beforeOpen) {
        super(target);
        this.beforeOpen = beforeOpen;
    }

    @Override
    protected boolean onBeforeOpenMenu(ObjectNode eventDetail) {
        beforeOpen.run();
        return super.onBeforeOpenMenu(eventDetail);
    }
}
