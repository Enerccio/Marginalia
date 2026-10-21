package com.github.enerccio.marginalia.ui.dialogs.permissions;

import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.traits.*;
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
 * runs can work at once, how many requests to the model one run can have in the air (the agent and its sub-agents), how many tokens one run can spend, and the quotas: how many working copies of books their runs
 * keep and what a workspace of an agent may hold (largest file, size, number of files). An empty field is the default
 * (five agents, one run, one request at a time, no limit of tokens or of copies, the limits of the application for the files).
 */
@Configurable
@Extendable
public class AgentsPermissionComponent implements PermissionUIComponent {

    @Autowired
    private Localization loc;

    private final List<Runnable> listeners = new ArrayList<>();
    private VerticalLayout layout;
    private IntegerField maxAgents;
    private IntegerField maxConcurrentRuns;
    private IntegerField runTokenBudget;
    private IntegerField maxCopies;
    private IntegerField maxFileKb;
    private IntegerField maxWorkspaceMb;
    private IntegerField maxWorkspaceFiles;
    private IntegerField maxCheckpointMb;
    private IntegerField maxConcurrency;
    private boolean loading;

    // texts need the injected localization, which is there only after the constructor
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    private void build() {
        if (layout != null) {
            return;
        }
        maxAgents = field(L.LABEL_MAX_AGENTS, L.HELP_AGENTS_MAX_AGENTS);
        maxConcurrentRuns = field(L.LABEL_MAX_CONCURRENT_RUNS, L.HELP_AGENTS_MAX_CONCURRENT_RUNS);
        runTokenBudget = field(L.LABEL_RUN_TOKEN_BUDGET, L.HELP_AGENTS_RUN_TOKEN_BUDGET);
        maxCopies = field(L.LABEL_AGENTS_MAX_COPIES, L.HELP_AGENTS_MAX_COPIES);
        maxFileKb = field(L.LABEL_AGENTS_MAX_FILE_KB, L.HELP_AGENTS_MAX_FILE_KB);
        maxWorkspaceMb = field(L.LABEL_AGENTS_MAX_WORKSPACE_MB, L.HELP_AGENTS_MAX_WORKSPACE_MB);
        maxWorkspaceFiles = field(L.LABEL_AGENTS_MAX_WORKSPACE_FILES, L.HELP_AGENTS_MAX_WORKSPACE_FILES);
        maxCheckpointMb = field(L.LABEL_AGENTS_MAX_CHECKPOINT_MB, L.HELP_AGENTS_MAX_CHECKPOINT_MB);
        maxConcurrency = field(L.LABEL_AGENTS_MAX_CONCURRENCY, L.HELP_AGENTS_MAX_CONCURRENCY);
        layout = new VerticalLayout(maxAgents, maxConcurrentRuns, maxConcurrency, runTokenBudget, maxCopies, maxFileKb, maxWorkspaceMb,
                maxWorkspaceFiles, maxCheckpointMb);
        layout.setPadding(false);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = IntegerField.class),
            parameters = {@APIParameter(name = "label", type = L.class),
                    @APIParameter(name = "help", type = L.class)},
            locals = {@APILocals(name = "field", type = IntegerField.class)})
    private IntegerField field(L label, L help) {
        IntegerField field = new IntegerField(loc.getValue(label));
        field.setMin(0);
        field.setStepButtonsVisible(true);
        field.setHelperText(loc.getValue(help));
        field.setWidth("320px");
        field.addValueChangeListener(e -> changed());
        return field;
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
            parameters = {@APIParameter(name = "permission", type = Permission.class)},
            locals = {@APILocals(name = "budget", type = Long.class)})
    @Override
    public void model2component(Permission permission) {
        build();
        loading = true;
        try {
            maxAgents.setValue(permission.getAgentsMaxAgents());
            maxConcurrentRuns.setValue(permission.getAgentsMaxConcurrentRuns());
            maxConcurrency.setValue(permission.getAgentsMaxConcurrency());
            Long budget = permission.getAgentsRunTokenBudget();
            runTokenBudget.setValue(budget == null ? null : (int) Math.min(Integer.MAX_VALUE, budget));
            maxCopies.setValue(permission.getAgentsMaxCopies());
            maxFileKb.setValue(permission.getAgentsMaxFileKb());
            maxWorkspaceMb.setValue(permission.getAgentsMaxWorkspaceMb());
            maxWorkspaceFiles.setValue(permission.getAgentsMaxWorkspaceFiles());
            maxCheckpointMb.setValue(permission.getAgentsMaxCheckpointMb());
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
        permission.setAgentsMaxAgents(maxAgents.getValue());
        permission.setAgentsMaxConcurrentRuns(maxConcurrentRuns.getValue());
        permission.setAgentsMaxConcurrency(maxConcurrency.getValue());
        permission.setAgentsRunTokenBudget(runTokenBudget.getValue() == null ? null : runTokenBudget.getValue().longValue());
        permission.setAgentsMaxCopies(maxCopies.getValue());
        permission.setAgentsMaxFileKb(maxFileKb.getValue());
        permission.setAgentsMaxWorkspaceMb(maxWorkspaceMb.getValue());
        permission.setAgentsMaxWorkspaceFiles(maxWorkspaceFiles.getValue());
        permission.setAgentsMaxCheckpointMb(maxCheckpointMb.getValue());
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "runnable", type = Runnable.class)})
    @Override
    public void onChange(Runnable runnable) {
        listeners.add(runnable);
    }
}
