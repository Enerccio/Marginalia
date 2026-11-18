package com.github.enerccio.marginalia.ui.dialogs.permissions;

import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;

public interface PermissionUIFactory {

    PermissionUIComponent createComponent(PermissionType permissionType);

}
