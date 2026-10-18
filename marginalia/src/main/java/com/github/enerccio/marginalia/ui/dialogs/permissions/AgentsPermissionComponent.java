package com.github.enerccio.marginalia.ui.dialogs.permissions;

import com.github.enerccio.marginalia.domain.security.model.Permission;
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
 * The settings of the permission {@code AGENTS}: how many agents the user can have (clones included), how many of their
 * runs can work at once and how many tokens one run can spend. An empty field is the default (five agents, one run, no
 * limit of tokens).
 */
@Configurable
public class AgentsPermissionComponent implements PermissionUIComponent {

    @Autowired
    private Localization loc;

    private final List<Runnable> listeners = new ArrayList<>();
    private VerticalLayout layout;
    private IntegerField maxAgents;
    private IntegerField maxConcurrentRuns;
    private IntegerField runTokenBudget;
    private boolean loading;

    // texts need the injected localization, which is there only after the constructor
    private void build() {
        if (layout != null) {
            return;
        }
        maxAgents = field(L.LABEL_MAX_AGENTS, L.HELP_AGENTS_MAX_AGENTS);
        maxConcurrentRuns = field(L.LABEL_MAX_CONCURRENT_RUNS, L.HELP_AGENTS_MAX_CONCURRENT_RUNS);
        runTokenBudget = field(L.LABEL_RUN_TOKEN_BUDGET, L.HELP_AGENTS_RUN_TOKEN_BUDGET);
        layout = new VerticalLayout(maxAgents, maxConcurrentRuns, runTokenBudget);
        layout.setPadding(false);
    }

    private IntegerField field(L label, L help) {
        IntegerField field = new IntegerField(loc.getValue(label));
        field.setMin(0);
        field.setStepButtonsVisible(true);
        field.setHelperText(loc.getValue(help));
        field.setWidth("320px");
        field.addValueChangeListener(e -> changed());
        return field;
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
            maxAgents.setValue(permission.getAgentsMaxAgents());
            maxConcurrentRuns.setValue(permission.getAgentsMaxConcurrentRuns());
            Long budget = permission.getAgentsRunTokenBudget();
            runTokenBudget.setValue(budget == null ? null : (int) Math.min(Integer.MAX_VALUE, budget));
        } finally {
            loading = false;
        }
    }

    @Override
    public void component2model(Permission permission) {
        build();
        permission.setAgentsMaxAgents(maxAgents.getValue());
        permission.setAgentsMaxConcurrentRuns(maxConcurrentRuns.getValue());
        permission.setAgentsRunTokenBudget(runTokenBudget.getValue() == null ? null : runTokenBudget.getValue().longValue());
    }

    @Override
    public void onChange(Runnable runnable) {
        listeners.add(runnable);
    }
}
