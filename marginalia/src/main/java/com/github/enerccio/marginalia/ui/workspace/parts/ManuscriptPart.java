package com.github.enerccio.marginalia.ui.workspace.parts;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.TextInputDialog;
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
public class ManuscriptPart implements WorkspaceComponent {

    @Autowired
    private Localization loc;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    private final Workspace workspace;
    private Grid<Manuscript> grid;
    private VerticalLayout mainLayout;

    public ManuscriptPart(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getName() {
        return loc.getValue(L.LABEL_BOOKS);
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

        Span titleSpan = new Span(loc.getValue(L.LABEL_BOOKS));
        titleSpan.getStyle().set("font-size", "var(--lumo-font-size-l)");
        titleSpan.getStyle().set("font-weight", "bold");

        Button addBookButton = new Button(loc.getValue(L.LABEL_ADD_BOOK), event -> {
            TextInputDialog dialog = new TextInputDialog.Builder(loc.getValue(L.LABEL_NEW_BOOK), name -> {
                try {
                    Manuscript manuscript = new Manuscript();
                    manuscript.setName(name);

                    UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
                    Long defaultModelId = userSetting.getDefaultModel();
                    if (defaultModelId != null) {
                        AI ai = aiService.find(defaultModelId);
                        if (ai != null && !ai.isDeleted()) {
                            manuscript.setAi(ai);
                        }
                    }
                    Long defaultProtocolId = userSetting.getDefaultProtocol();
                    if (defaultProtocolId != null) {
                        Protocol protocol = protocolService.find(defaultProtocolId);
                        if (protocol != null && !protocol.isDeleted()) {
                            manuscript.setProtocol(protocol);
                        }
                    }

                    manuscript = manuscriptService.save(manuscript);
                    ManuscriptDialog manuscriptDialog = new ManuscriptDialog(manuscript);
                    manuscriptDialog.setOnClose(this::refreshGrid);
                    manuscriptDialog.create();
                    manuscriptDialog.open();
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            }).messageRequired().showCancel(true).build();
            dialog.open();
        });
        addBookButton.setThemeName("primary");

        headerLayout.add(titleSpan, addBookButton);
        headerLayout.setFlexGrow(1, titleSpan);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        grid = new Grid<>(Manuscript.class, false);
        grid.setSizeFull();

        grid.addColumn(Manuscript::getName)
                .setHeader(loc.getValue(L.LABEL_NAME));

        grid.addComponentColumn(manuscript -> new Button(Solid.PENCIL.create(),event -> {
            try {
                ManuscriptDialog dialog = new ManuscriptDialog(manuscript);
                dialog.setOnClose(this::refreshGrid);
                dialog.create();
                dialog.open();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        })).setHeader("").setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE);

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
            List<Manuscript> manuscripts = manuscriptService.findAllForUser();
            manuscripts.sort(Comparator.comparing(Manuscript::getModification));
            grid.setItems(manuscripts);
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