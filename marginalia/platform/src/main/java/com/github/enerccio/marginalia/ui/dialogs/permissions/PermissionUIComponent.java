package com.github.enerccio.marginalia.ui.dialogs.permissions;

import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.vaadin.flow.component.Component;

public interface PermissionUIComponent {

    Component getComponent();
    void model2component(Permission permission);
    void component2model(Permission permission);
    void onChange(Runnable runnable);

    /**
     * Called when the permission is granted or taken away in the dialog, and when the component is made; for settings
     * that make sense only while it is granted (sub-permissions).
     */
    default void onGrantedChanged(boolean granted) {
    }

}
