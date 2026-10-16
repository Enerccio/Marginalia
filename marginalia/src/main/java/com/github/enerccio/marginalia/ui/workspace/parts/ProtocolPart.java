package com.github.enerccio.marginalia.ui.workspace.parts;

import java.util.Map;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceLLMSupport;
import com.github.enerccio.marginalia.ui.LLMInteraction;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.domain.traits.APILocals;
import com.github.enerccio.marginalia.domain.traits.APIReturn;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.domain.traits.PublicAPI;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.ProtocolDialog;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceComponent;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Comparator;
import java.util.List;

@Configurable
@Extendable
public class ProtocolPart implements WorkspaceComponent {

    @Autowired
    private Localization loc;

    @Autowired
    private ProtocolService protocolService;

    private final Workspace workspace;
    private Grid<Protocol> grid;
    private Button addProtocolButton;
    private VerticalLayout mainLayout;

    public ProtocolPart(Workspace workspace) {
        this.workspace = workspace;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = String.class))
    public String getName() {
        return loc.getValue(L.LABEL_PROTOCOLS);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "headerLayout", type = HorizontalLayout.class),
                    @APILocals(name = "titleSpan", type = Span.class),
                    @APILocals(name = "addProtocolButton", type = Button.class)})
    @Override
    public Component create() throws Exception {
        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Span titleSpan = new Span(loc.getValue(L.LABEL_PROTOCOLS));
        titleSpan.addClassName(CssStyles.SECTION_TITLE);

        Button addProtocolButton = new Button(loc.getValue(L.LABEL_ADD_PROTOCOL), event -> {
            ProtocolDialog dialog = new ProtocolDialog();
            dialog.setOnSave(this::refreshGrid);
            dialog.create();
            dialog.open();
        });
        addProtocolButton.setThemeName("primary");
        this.addProtocolButton = addProtocolButton;

        headerLayout.add(titleSpan, addProtocolButton);
        headerLayout.setFlexGrow(1, titleSpan);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        grid = new Grid<>(Protocol.class, false);
        grid.setSizeFull();

        grid.addColumn(protocol -> loc.getValue(loc.getProtocolType(protocol.getProtocolType())))
                .setHeader(loc.getValue(L.LABEL_TYPE))
                .setFlexGrow(1);

        grid.addColumn(Protocol::getName)
                .setHeader(loc.getValue(L.LABEL_NAME))
                .setFlexGrow(1);

        grid.addComponentColumn(protocol -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button editButton = new Button(Solid.PEN.create(), event -> {
                ProtocolDialog dialog = new ProtocolDialog(protocol);
                dialog.setOnSave(this::refreshGrid);
                dialog.create();
                dialog.open();
            });

            Button deleteButton = new Button(Solid.TRASH.create(), event ->
                    ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> {
                        try {
                            protocolService.delete(protocol, false);
                            refreshGrid();
                        } catch (Exception e) {
                            UIUtils.internalServerError(loc, e);
                        }
                    }));
            deleteButton.setThemeName("error tertiary");

            actions.add(editButton, deleteButton);
            return actions;
        }).setHeader("").setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE_HUGE);

        mainLayout.add(headerLayout, grid);
        mainLayout.setFlexGrow(1, grid);

        refreshGrid();

        return mainLayout;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void refresh() throws Exception {
        refreshGrid();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "protocols", type = List.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void refreshGrid() {
        if (grid == null) {
            return;
        }
        try {
            List<Protocol> protocols = protocolService.findAllForUser();
            protocols.sort(Comparator.comparing(Protocol::getName));
            grid.setItems(protocols);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = String.class))
    @Override
    public String getLLMState() {
        return WorkspaceLLMSupport.describeTable("protocols", grid, addProtocolButton);
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = Map.class))
    @Override
    public Map<String, LLMInteraction> getInteraction() {
        return LLMInteract.dialogButtons(addProtocolButton);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void onTabSwitched() throws Exception {
        refresh();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void onTabClosed() throws Exception {

    }
}