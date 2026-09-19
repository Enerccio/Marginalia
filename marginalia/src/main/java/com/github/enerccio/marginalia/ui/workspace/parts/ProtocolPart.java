package com.github.enerccio.marginalia.ui.workspace.parts;

import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.main.dialogs.ProtocolDialog;
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

import java.util.ArrayList;
import java.util.List;

@Configurable
public class ProtocolPart implements WorkspaceComponent {

    @Autowired
    private Localization loc;

    @Autowired
    private ProtocolService protocolService;

    private final Workspace workspace;
    private Grid<Protocol> grid;
    private VerticalLayout mainLayout;

    public ProtocolPart() {
        this(null);
    }

    public ProtocolPart(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getName() {
        return loc.getValue(L.LABEL_PROTOCOLS);
    }

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
        titleSpan.getStyle().set("font-size", "var(--lumo-font-size-l)");
        titleSpan.getStyle().set("font-weight", "bold");

        Button addProtocolButton = new Button(loc.getValue(L.LABEL_ADD_PROTOCOL), event -> {
            ProtocolDialog dialog = new ProtocolDialog();
            dialog.setOnSave(this::refreshGrid);
            dialog.create();
            dialog.open();
        });
        addProtocolButton.setThemeName("primary");

        headerLayout.add(titleSpan, addProtocolButton);
        headerLayout.setFlexGrow(1, titleSpan);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        grid = new Grid<>(Protocol.class, false);
        grid.setSizeFull();

        grid.addColumn(protocol -> loc.getValue(loc.getProtocolType(protocol.getProtocolType())))
                .setHeader(loc.getValue(L.LABEL_TYPE))
                .setAutoWidth(true);

        grid.addColumn(Protocol::getName)
                .setHeader(loc.getValue(L.LABEL_NAME))
                .setAutoWidth(true);

        grid.addComponentColumn(protocol -> new Button(loc.getValue(L.LABEL_EDIT), event -> {
            ProtocolDialog dialog = new ProtocolDialog(protocol);
            dialog.setOnSave(this::refreshGrid);
            dialog.create();
            dialog.open();
        })).setHeader("").setAutoWidth(true);

        mainLayout.add(headerLayout, grid);
        mainLayout.setFlexGrow(1, grid);

        refreshGrid();

        return mainLayout;
    }

    @Override
    public void refresh() throws Exception {
        refreshGrid();
    }

    private void refreshGrid() {
        if (grid == null) {
            return;
        }
        try {
            List<Long> ids = protocolService.findAllIds();
            List<Protocol> protocols = new ArrayList<>();
            for (Long id : ids) {
                Protocol protocol = protocolService.find(id);
                if (protocol != null) {
                    protocols.add(protocol);
                }
            }
            grid.setItems(protocols);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @Override
    public void onTabSwitched() throws Exception {
        refresh();
    }

    @Override
    public void onTabClosed() throws Exception {

    }
}