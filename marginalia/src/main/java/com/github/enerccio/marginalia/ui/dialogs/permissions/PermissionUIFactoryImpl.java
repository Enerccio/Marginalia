package com.github.enerccio.marginalia.ui.dialogs.permissions;

import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;

public class PermissionUIFactoryImpl implements PermissionUIFactory {

    @Override
    public PermissionUIComponent createComponent(PermissionType permissionType) {
        return switch (permissionType) {
            case HELPER -> new HelperPermissionComponent();
            case AGENTS -> new AgentsPermissionComponent();
            default -> null;
        };
    }
}
