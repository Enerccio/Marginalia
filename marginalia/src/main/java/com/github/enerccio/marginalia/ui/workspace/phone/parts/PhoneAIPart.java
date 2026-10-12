package com.github.enerccio.marginalia.ui.workspace.phone.parts;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.AIDialog;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceComponent;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Comparator;
import java.util.List;

/**
 * The model list for phones. Instead of the type, name and action columns of the desktop list it has a text column
 * (type and name on two lines) and a column with the buttons, so it fits a narrow screen.
 */
@Configurable
@Extendable
public class PhoneAIPart implements WorkspaceComponent {

    @Autowired
    private Localization loc;

    @Autowired
    private AIService aiService;

    private final Workspace workspace;
    private Grid<AI> grid;

    public PhoneAIPart(Workspace workspace) {
        this.workspace = workspace;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = String.class))
    public String getName() {
        return loc.getValue(L.LABEL_MODELS);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "mainLayout", type = VerticalLayout.class),
                    @APILocals(name = "addModelButton", type = Button.class)})
    @Override
    public Component create() throws Exception {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        Button addModelButton = new Button(loc.getValue(L.LABEL_ADD_MODEL), Solid.PLUS.create(), event -> {
            AIDialog dialog = new AIDialog();
            dialog.setOnSave(this::refreshGrid);
            dialog.create();
            dialog.open();
        });
        addModelButton.setThemeName("primary");
        addModelButton.setWidthFull();

        grid = new Grid<>();
        grid.setSizeFull();
        // rows grow with their content, so a long name breaks into lines instead of running under the buttons
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.addComponentColumn(this::createText).setFlexGrow(1);
        grid.addComponentColumn(this::createActions).setFlexGrow(0).setAutoWidth(true);

        mainLayout.add(addModelButton, grid);
        mainLayout.setFlexGrow(1, grid);

        refreshGrid();
        return mainLayout;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "ai", type = AI.class)},
            locals = {@APILocals(name = "textLayout", type = VerticalLayout.class)})
    private Component createText(AI ai) {
        VerticalLayout textLayout = new VerticalLayout();
        textLayout.setPadding(false);
        textLayout.setSpacing(false);
        textLayout.setWidthFull();
        textLayout.addClassName(CssStyles.MIN_WIDTH_0);

        textLayout.add(createLine(loc.getValue(L.LABEL_TYPE), loc.getValue(loc.getAIType(ai.getAiType()))));
        textLayout.add(createLine(loc.getValue(L.LABEL_NAME), ai.getName()));
        return textLayout;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Span.class),
            parameters = {@APIParameter(name = "label", type = String.class),
                    @APIParameter(name = "value", type = String.class)},
            locals = {@APILocals(name = "line", type = Span.class)})
    private Span createLine(String label, String value) {
        Span line = new Span(label + ": " + value);
        line.addClassNames(CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE);
        return line;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "ai", type = AI.class)},
            locals = {@APILocals(name = "editButton", type = Button.class),
                    @APILocals(name = "deleteButton", type = Button.class),
                    @APILocals(name = "actions", type = HorizontalLayout.class)})
    private Component createActions(AI ai) {
        Button editButton = new Button(Solid.PEN.create(), event -> {
            AIDialog dialog = new AIDialog(ai);
            dialog.setOnSave(this::refreshGrid);
            dialog.create();
            dialog.open();
        });

        Button deleteButton = new Button(Solid.TRASH.create(), event ->
                ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> {
                    try {
                        aiService.delete(ai, false);
                        refreshGrid();
                    } catch (Exception e) {
                        UIUtils.internalServerError(loc, e);
                    }
                }));
        deleteButton.setThemeName("error tertiary");

        HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
        actions.setSpacing(false);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);
        actions.addClassName(CssStyles.FLEX_SHRINK_0);
        return actions;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void refresh() throws Exception {
        refreshGrid();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "connections", type = List.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void refreshGrid() {
        if (grid == null) {
            return;
        }
        try {
            List<AI> connections = aiService.findAllForUser();
            connections.sort(Comparator.comparing(AI::getName));
            grid.setItems(connections);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
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
