package com.github.enerccio.marginalia.ui.dialogs.permissions;

import com.github.enerccio.marginalia.domain.security.model.Permission;
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
 * The settings of the permission {@code HELPER} that depend on the user: whether their questions to the help assistant
 * count against their limits, and whether the assistant may move through the interface for them.
 */
@Configurable
public class HelperPermissionComponent implements PermissionUIComponent {

    @Autowired
    private Localization loc;

    private final List<Runnable> listeners = new ArrayList<>();
    private VerticalLayout layout;
    private Checkbox applyLimits;
    private Checkbox interactions;
    private boolean loading;

    // texts need the injected localization, which is there only after the constructor
    private void build() {
        if (layout != null) {
            return;
        }
        applyLimits = new Checkbox(loc.getValue(L.LABEL_APPLY_LIMITS));
        applyLimits.setHelperText(loc.getValue(L.HELP_ASSISTANT_APPLY_LIMITS));
        interactions = new Checkbox(loc.getValue(L.LABEL_HELP_INTERACTIONS));
        interactions.setHelperText(loc.getValue(L.HELP_ASSISTANT_INTERACTIONS));
        applyLimits.addValueChangeListener(e -> changed());
        interactions.addValueChangeListener(e -> changed());
        layout = new VerticalLayout(applyLimits, interactions);
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
            applyLimits.setValue(permission.getHelperApplyLimits());
            interactions.setValue(permission.getHelperInteractions());
        } finally {
            loading = false;
        }
    }

    @Override
    public void component2model(Permission permission) {
        build();
        permission.setHelperApplyLimits(applyLimits.getValue());
        permission.setHelperInteractions(interactions.getValue());
    }

    @Override
    public void onChange(Runnable runnable) {
        listeners.add(runnable);
    }
}
