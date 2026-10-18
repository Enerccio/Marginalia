package com.github.enerccio.marginalia.ui.dialogs.permissions;

import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.vaadin.flow.component.Component;

public interface PermissionUIComponent {

    Component getComponent();
    void model2component(Permission permission);
    void component2model(Permission permission);
    void onChange(Runnable runnable);

}
