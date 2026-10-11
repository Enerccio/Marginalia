package com.github.enerccio.marginalia.ui.workspace.phone;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.UserDialog;
import com.github.enerccio.marginalia.ui.workspace.DesktopWorkspace;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceComponent;
import com.github.enerccio.marginalia.ui.workspace.parts.*;
import com.github.enerccio.marginalia.ui.workspace.phone.parts.PhoneManuscriptPart;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.HashMap;
import java.util.Map;

/**
 * Workspace for phones. The vertical tab sheet of {@link DesktopWorkspace} would take most of the screen, so the
 * parts are reached from a drawer (opened by the toggle in the top bar) and the active part fills the whole screen.
 * The book list is the phone specific {@link PhoneManuscriptPart}, other parts are shared with the desktop.
 */
@Configurable
@Extendable
public class PhoneWorkspace implements Workspace {

    @Autowired
    private Localization loc;

    @Autowired
    private UserService userService;

    @Autowired
    private User currentUser;

    private final PhoneManuscriptPart manuscriptPart = new PhoneManuscriptPart(this);
    private final LorebookPart lorebookPart = new LorebookPart(this);
    private final UserPart userPart = new UserPart(this);
    private final ProtocolPart protocolPart = new ProtocolPart(this);
    private final AIPart aiPart = new AIPart(this);
    private final ResourcesPart resourcesPart = new ResourcesPart(this);
    private final TrashPart trashPart = new TrashPart(this);
    private final AdminPart adminPart = new AdminPart(this);

    private final Map<Tab, WorkspaceComponent> tabToComponent = new HashMap<>();
    private final Map<Tab, Component> tabToContent = new HashMap<>();
    private final Map<Tab, String> tabToTitle = new HashMap<>();

    private AppLayout layout;
    private DrawerToggle drawerToggle;
    private Span title;
    private Tabs tabs;
    private Component adminPartComponent;
    private WorkspaceComponent activeComponent;
    private Button adminButton;
    private Button changePasswordButton;
    private Button logoutButton;
    private boolean navigationLocked;
    private Runnable onLogout;

    @Override
    public Component create() throws Exception {
        layout = new AppLayout();
        layout.addClassName(CssStyles.FULL_SIZE);

        drawerToggle = new DrawerToggle();
        title = new Span();
        title.addClassName(CssStyles.SECTION_TITLE);
        layout.addToNavbar(drawerToggle, title);

        tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.setWidthFull();

        addTab(L.LABEL_BOOKS, manuscriptPart);
        addTab(L.LABEL_LOREBOOKS, lorebookPart);
        addTab(L.LABEL_SETTINGS, userPart);
        addTab(L.LABEL_PROTOCOLS, protocolPart);
        addTab(L.LABEL_MODELS, aiPart);
        addTab(L.LABEL_RESOURCES, resourcesPart);
        addTab(L.LABEL_TRASH, trashPart);

        adminPartComponent = adminPart.create();

        tabs.addSelectedChangeListener(e -> {
            Tab selected = e.getSelectedTab();
            if (selected == null) {
                // the admin part is shown, it has no tab
                return;
            }
            show(tabToComponent.get(selected), tabToContent.get(selected), tabToTitle.get(selected));
        });

        User user = userService.find(currentUser.getId());

        VerticalLayout drawer = new VerticalLayout();
        drawer.setSizeFull();
        drawer.setPadding(false);
        drawer.setSpacing(false);
        drawer.add(tabs);
        drawer.setFlexGrow(1, tabs);
        drawer.add(createFooter(user));
        layout.addToDrawer(drawer);

        Tab first = tabs.getSelectedTab();
        show(tabToComponent.get(first), tabToContent.get(first), tabToTitle.get(first));

        return layout;
    }

    private void addTab(L label, WorkspaceComponent part) throws Exception {
        String name = loc.getValue(label);
        Tab tab = new Tab(name);
        tabs.add(tab);
        tabToComponent.put(tab, part);
        tabToContent.put(tab, part.create());
        tabToTitle.put(tab, name);
    }

    private void show(WorkspaceComponent part, Component content, String name) {
        if (activeComponent != null) {
            try {
                activeComponent.onTabClosed();
            } catch (Exception ex) {
                UIUtils.internalServerError(loc, ex);
            }
        }

        layout.setContent(content);
        title.setText(name);
        layout.setDrawerOpened(false);
        activeComponent = part;
        try {
            part.onTabSwitched();
        } catch (Exception ex) {
            UIUtils.internalServerError(loc, ex);
        }
    }

    private Component createFooter(User user) {
        VerticalLayout footer = new VerticalLayout();
        footer.setSpacing(true);
        footer.setWidthFull();

        if (user != null && user.isAdmin()) {
            adminButton = new Button(loc.getValue(L.LABEL_ADMIN), Solid.USER_TIE.create());
            adminButton.setWidthFull();
            adminButton.addClickListener(e -> {
                tabs.setSelectedTab(null);
                show(adminPart, adminPartComponent, loc.getValue(L.LABEL_ADMIN));
            });
            footer.add(adminButton);
        }

        changePasswordButton = new Button(loc.getValue(L.LABEL_CHANGE_PASSWORD), Solid.KEY.create());
        changePasswordButton.setWidthFull();
        changePasswordButton.addClickListener(e -> {
            try {
                UserDialog dialog = new UserDialog(userService.find(currentUser.getId()), true);
                dialog.setSelfEdit(true);
                dialog.create();
                dialog.open();
            } catch (Exception ex) {
                UIUtils.internalServerError(loc, ex);
            }
        });
        footer.add(changePasswordButton);

        if (onLogout != null) {
            logoutButton = new Button(loc.getValue(L.LABEL_LOGOUT), Solid.SIGN_OUT_ALT.create());
            logoutButton.setWidthFull();
            logoutButton.addClickListener(e -> onLogout.run());
            footer.add(logoutButton);
        }

        return footer;
    }

    @Override
    public void refresh() throws Exception {
        manuscriptPart.refresh();
        lorebookPart.refresh();
        userPart.refresh();
        protocolPart.refresh();
        aiPart.refresh();
        resourcesPart.refresh();
        trashPart.refresh();
        adminPart.refresh();
    }

    @Override
    public void setNavigationLocked(boolean locked) {
        navigationLocked = locked;
        drawerToggle.setEnabled(!locked);
        if (locked) {
            layout.setDrawerOpened(false);
        }
        for (Tab tab : tabToComponent.keySet()) {
            tab.setEnabled(!locked);
        }
        if (adminButton != null) {
            adminButton.setEnabled(!locked);
        }
        if (logoutButton != null) {
            logoutButton.setEnabled(!locked);
        }
    }

    @Override
    public boolean isNavigationLocked() {
        return navigationLocked;
    }

    @Override
    public Runnable getOnLogout() {
        return onLogout;
    }

    @Override
    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }

}
