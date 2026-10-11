package com.github.enerccio.marginalia.ui.workspace.phone.parts;

import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.PhoneTrashGrid;
import com.github.enerccio.marginalia.ui.widgets.TrashGrid;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceComponent;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * The trash tab for phones, hosting the {@link PhoneTrashGrid} (stacked filters, one content column) instead of the
 * desktop {@link TrashGrid} layout.
 */
@Configurable
@Extendable
public class PhoneTrashPart implements WorkspaceComponent {

    @Autowired
    private Localization loc;

    @Autowired
    private UserService userService;

    @Autowired
    private User currentUser;

    private final Workspace workspace;
    private TrashGrid trashGrid;

    public PhoneTrashPart(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getName() {
        return loc.getValue(L.LABEL_TRASH);
    }

    @Override
    public Component create() throws Exception {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        User user = userService.find(currentUser.getId());
        trashGrid = new PhoneTrashGrid(user != null && user.isAdmin()).create();
        // restored books, lorebooks... are back in their own tabs
        trashGrid.addRestoreListener(() -> {
            try {
                workspace.refresh();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });

        mainLayout.add(trashGrid);
        mainLayout.setFlexGrow(1, trashGrid);
        return mainLayout;
    }

    @Override
    public void refresh() throws Exception {
        if (trashGrid != null) {
            trashGrid.refresh();
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
