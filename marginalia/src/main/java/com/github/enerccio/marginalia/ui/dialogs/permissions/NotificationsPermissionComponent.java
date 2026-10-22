package com.github.enerccio.marginalia.ui.dialogs.permissions;

import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.traits.APIParameter;
import com.github.enerccio.marginalia.domain.traits.APIReturn;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.domain.traits.PublicAPI;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

/**
 * The setting of the permission {@code NOTIFICATIONS}: whether the messages may go to addresses on the server's machine
 * and in private networks. It is a sub-permission, the box can be ticked only while the permission itself is granted.
 */
@Configurable
@Extendable
public class NotificationsPermissionComponent implements PermissionUIComponent {

    @Autowired
    private Localization loc;

    private final List<Runnable> listeners = new ArrayList<>();
    private VerticalLayout layout;
    private Checkbox allowLocal;
    private boolean loading;

    // texts need the injected localization, which is there only after the constructor
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    private void build() {
        if (layout != null) {
            return;
        }
        allowLocal = new Checkbox(loc.getValue(L.LABEL_NOTIFICATIONS_ALLOW_LOCAL));
        allowLocal.setHelperText(loc.getValue(L.HELP_NOTIFICATIONS_ALLOW_LOCAL));
        allowLocal.addValueChangeListener(e -> changed());
        layout = new VerticalLayout(allowLocal);
        layout.setPadding(false);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    private void changed() {
        // not an edit while the model is put into the component
        if (!loading) {
            listeners.forEach(Runnable::run);
        }
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Component.class))
    @Override
    public Component getComponent() {
        build();
        return layout;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "permission", type = Permission.class)})
    @Override
    public void model2component(Permission permission) {
        build();
        loading = true;
        try {
            allowLocal.setValue(permission.getNotificationsAllowLocal());
            allowLocal.setEnabled(permission.isGranted());
        } finally {
            loading = false;
        }
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "permission", type = Permission.class)})
    @Override
    public void component2model(Permission permission) {
        build();
        // what is not allowed any more is not kept ticked behind the scenes
        permission.setNotificationsAllowLocal(allowLocal.getValue() && permission.isGranted());
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "granted", type = boolean.class)})
    @Override
    public void onGrantedChanged(boolean granted) {
        build();
        allowLocal.setEnabled(granted);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "runnable", type = Runnable.class)})
    @Override
    public void onChange(Runnable runnable) {
        listeners.add(runnable);
    }
}
