package com.github.enerccio.marginalia.ui.dialogs.permissions;

import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.service.EffectiveHistoryLimit;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

/**
 * The setting of the permission {@code VERSION_HISTORY}: the most versions of a part the history of the user's books keeps. An
 * empty field is no limit from the administrator; a permission added here starts with the default, so that adding it does not lift
 * the limit the user has without it. The field can be edited only while the permission is granted.
 */
@Configurable
public class VersionHistoryPermissionComponent implements PermissionUIComponent {

    @Autowired
    private Localization loc;

    private final List<Runnable> listeners = new ArrayList<>();
    private VerticalLayout layout;
    private IntegerField maxVersions;
    private boolean loading;

    // texts need the injected localization, which is there only after the constructor
    private void build() {
        if (layout != null) {
            return;
        }
        maxVersions = new IntegerField(loc.getValue(L.LABEL_HISTORY_MAX_VERSIONS));
        maxVersions.setMin(0);
        maxVersions.setStepButtonsVisible(true);
        maxVersions.setClearButtonVisible(true);
        maxVersions.setHelperText(loc.getValue(L.HELP_VERSION_HISTORY_PERMISSION));
        maxVersions.setWidth("320px");
        maxVersions.addValueChangeListener(e -> changed());
        layout = new VerticalLayout(maxVersions);
        layout.setPadding(false);
    }

    private void changed() {
        // not an edit while the model is put into the component
        if (!loading) {
            listeners.forEach(Runnable::run);
        }
    }

    @Override
    public Component getComponent() {
        build();
        return layout;
    }

    @Override
    public void model2component(Permission permission) {
        build();
        loading = true;
        try {
            boolean added = permission.getId() == null && permission.getHistoryMaxVersions() == null;
            maxVersions.setValue(added ? Integer.valueOf(EffectiveHistoryLimit.DEFAULT_MAX_VERSIONS) : permission.getHistoryMaxVersions());
            maxVersions.setEnabled(permission.isGranted());
        } finally {
            loading = false;
        }
    }

    @Override
    public void component2model(Permission permission) {
        build();
        permission.setHistoryMaxVersions(maxVersions.getValue());
    }

    @Override
    public void onGrantedChanged(boolean granted) {
        build();
        maxVersions.setEnabled(granted);
    }

    @Override
    public void onChange(Runnable runnable) {
        listeners.add(runnable);
    }
}
