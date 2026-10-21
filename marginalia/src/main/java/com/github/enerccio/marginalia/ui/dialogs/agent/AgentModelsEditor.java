package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.AgentModelProfile;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The models a sub-agent of the agent can run on: the table of the model profiles (the main one first, which is picked in the
 * dialog of the agent and is edited here only for the commands of a sub-agent on the same model), a button to add another
 * and the check box that says whether a sub-agent may run on the main model at all. Nothing is stored until
 * {@link #applyTo} puts the table into the agent.
 */
@Configurable(preConstruction = true)
@Extendable
public class AgentModelsEditor extends VerticalLayout {

    @Autowired
    private Localization loc;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    private final Checkbox selfCheckbox;
    private final Grid<AgentModelProfile> grid;
    private AgentModelProfile main = new AgentModelProfile(AgentDialog.MAIN_ROLE, null, null);
    private final List<AgentModelProfile> others = new ArrayList<>();
    private final Map<String, String> aiNames = new HashMap<>();
    private final Map<String, String> protocolNames = new HashMap<>();

    public AgentModelsEditor() {
        Span title = new Span(loc.getValue(L.LABEL_AGENT_MODELS));
        title.addClassName(CssStyles.SECTION_TITLE);
        Span help = new Span(loc.getValue(L.HELP_AGENT_MODELS));
        help.addClassName(CssStyles.HINT_TEXT);

        selfCheckbox = new Checkbox(loc.getValue(L.LABEL_AGENT_SELF_SUBAGENT), true);
        selfCheckbox.setHelperText(loc.getValue(L.HELP_AGENT_SELF_SUBAGENT));

        grid = new Grid<>(AgentModelProfile.class, false);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        grid.setAllRowsVisible(true);
        grid.addColumn(this::roleOf).setHeader(loc.getValue(L.LABEL_ROLE)).setFlexGrow(1).setResizable(true);
        grid.addColumn(this::modelOf).setHeader(loc.getValue(L.LABEL_MODEL)).setFlexGrow(2).setResizable(true);
        grid.addColumn(profile -> StringUtils.defaultString(profile.getDescription())).setHeader(loc.getValue(L.LABEL_DESCRIPTION))
                .setFlexGrow(3).setResizable(true);
        grid.addColumn(this::commandsOf).setHeader(loc.getValue(L.LABEL_AGENT_COMMANDS)).setFlexGrow(1).setResizable(true);
        grid.addComponentColumn(this::buttons).setFlexGrow(0).setWidth("170px");

        Button addButton = new Button(loc.getValue(L.LABEL_AGENT_ADD_MODEL), event -> edit(new AgentModelProfile(), false));
        addButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        setPadding(false);
        setSpacing(false);
        setWidthFull();
        add(title, help, selfCheckbox, grid, addButton);
        loadNames();
        refresh();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "ai", type = AI.class),
                    @APILocals(name = "protocol", type = Protocol.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void loadNames() {
        try {
            for (AI ai : aiService.findAllForUser()) {
                aiNames.put(ai.getUuid(), ai.getName());
            }
            for (Protocol protocol : protocolService.findAllForUser()) {
                protocolNames.put(protocol.getUuid(), protocol.getName());
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Shows the profiles of the agent (copies, the agent is not changed) and whether a sub-agent may run on the main model.
     *
     * @param profiles the profiles of the agent, the main one first; none for an agent that has no model yet
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "profiles", type = List.class),
                    @APIParameter(name = "selfAllowed", type = boolean.class)})
    public void setProfiles(List<AgentModelProfile> profiles, boolean selfAllowed) {
        main = profiles.isEmpty() ? new AgentModelProfile(AgentDialog.MAIN_ROLE, null, null) : profiles.getFirst().copy();
        others.clear();
        profiles.stream().skip(1).map(AgentModelProfile::copy).forEach(others::add);
        selfCheckbox.setValue(selfAllowed);
        refresh();
    }

    /**
     * Puts what the table says into the agent: whether a sub-agent may run on the main model, the commands of the main profile
     * and the other profiles. The main profile itself (its AI and protocol) is the dialog's; an agent that has none keeps the
     * profiles it has.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "agent", type = Agent.class)},
            locals = {@APILocals(name = "profiles", type = List.class),
                    @APILocals(name = "all", type = List.class)})
    public void applyTo(Agent agent) {
        agent.setAllowSelfSubagent(selfCheckbox.getValue() ? null : false);
        List<AgentModelProfile> profiles = new ArrayList<>(agent.getModelProfiles());
        if (profiles.isEmpty()) {
            return;
        }
        profiles.getFirst().setCommands(main.getCommands());
        List<AgentModelProfile> all = new ArrayList<>();
        all.add(profiles.getFirst());
        others.forEach(profile -> all.add(profile.copy()));
        agent.setModelProfiles(all);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "rows", type = List.class)})
    private void refresh() {
        List<AgentModelProfile> rows = new ArrayList<>();
        rows.add(main);
        rows.addAll(others);
        grid.setItems(rows);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = boolean.class),
            parameters = {@APIParameter(name = "profile", type = AgentModelProfile.class)})
    private boolean isMain(AgentModelProfile profile) {
        return profile == main;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "profile", type = AgentModelProfile.class)})
    private String roleOf(AgentModelProfile profile) {
        return isMain(profile) ? loc.getValue(L.LABEL_AGENT_MAIN_MODEL) : profile.getRole();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "profile", type = AgentModelProfile.class)})
    private String modelOf(AgentModelProfile profile) {
        if (isMain(profile)) {
            return "";
        }
        return aiNames.getOrDefault(profile.getAiUuid(), "?") + " / " + protocolNames.getOrDefault(profile.getProtocolUuid(), "?");
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "profile", type = AgentModelProfile.class)})
    private String commandsOf(AgentModelProfile profile) {
        if (!isMain(profile) && !profile.isTools()) {
            return loc.getValue(L.LABEL_AGENT_TEXT_ONLY);
        }
        if (profile.getCommands() == null) {
            return loc.getValue(L.LABEL_AGENT_COMMANDS_SAME);
        }
        return profile.getCommands().isEmpty() ? loc.getValue(L.LABEL_AGENT_COMMANDS_NONE)
                : String.format(loc.getValue(L.MSG_AGENT_COMMANDS_COUNT), profile.getCommands().size());
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = HorizontalLayout.class),
            parameters = {@APIParameter(name = "profile", type = AgentModelProfile.class)},
            locals = {@APILocals(name = "edit", type = Button.class),
                    @APILocals(name = "line", type = HorizontalLayout.class),
                    @APILocals(name = "delete", type = Button.class)})
    private HorizontalLayout buttons(AgentModelProfile profile) {
        Button edit = new Button(loc.getValue(L.LABEL_EDIT), event -> edit(profile, isMain(profile)));
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        HorizontalLayout line = new HorizontalLayout(edit);
        line.setPadding(false);
        if (!isMain(profile)) {
            Button delete = new Button(loc.getValue(L.LABEL_DELETE), event -> {
                others.remove(profile);
                refresh();
            });
            delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            line.add(delete);
        }
        return line;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "profile", type = AgentModelProfile.class),
                    @APIParameter(name = "isMainProfile", type = boolean.class)},
            locals = {@APILocals(name = "dialog", type = AgentModelDialog.class)})
    private void edit(AgentModelProfile profile, boolean isMainProfile) {
        AgentModelDialog dialog = new AgentModelDialog(profile, isMainProfile, edited -> {
            if (isMainProfile) {
                main = edited;
            } else {
                int at = others.indexOf(profile);
                if (at >= 0) {
                    others.set(at, edited);
                } else {
                    others.add(edited);
                }
            }
            refresh();
        });
        dialog.create();
        dialog.open();
    }

    // for the tests
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Grid.class))
    Grid<AgentModelProfile> getGrid() {
        return grid;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Checkbox.class))
    Checkbox getSelfCheckbox() {
        return selfCheckbox;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = List.class))
    List<AgentModelProfile> getOthers() {
        return others;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = AgentModelProfile.class))
    AgentModelProfile getMain() {
        return main;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "profile", type = AgentModelProfile.class)})
    void addModel(AgentModelProfile profile) {
        others.add(profile);
        refresh();
    }
}
