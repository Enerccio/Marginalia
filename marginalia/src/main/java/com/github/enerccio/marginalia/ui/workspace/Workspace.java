package com.github.enerccio.marginalia.ui.workspace;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.UserDialog;
import com.github.enerccio.marginalia.ui.widgets.HTabSheet;
import com.github.enerccio.marginalia.ui.workspace.parts.*;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.HashMap;
import java.util.Map;

@Configurable
@Extendable
public class Workspace {

    @Autowired
    private Localization loc;

    @Autowired
    private UserService userService;

    @Autowired
    private User currentUser;

    private HTabSheet tabs;

    private final Map<Tab, WorkspaceComponent> tabToComponent = new HashMap<>();

    private final ManuscriptPart manuscriptPart = new ManuscriptPart(this);
    private Component manuscriptPartComponent;

    private final LorebookPart lorebookPart = new LorebookPart(this);
    private Component lorebookPartComponent;

    private final UserPart userPart = new UserPart(this);
    private Component userPartComponent;

    private final ProtocolPart protocolPart = new ProtocolPart(this);
    private Component protocolPartComponent;

    private final AIPart aiPart = new AIPart(this);
    private Component aiPartComponent;

    private final ResourcesPart resourcesPart = new ResourcesPart(this);
    private Component resourcesPartComponent;

    private final TrashPart trashPart = new TrashPart(this);
    private Component trashPartComponent;

    private final AdminPart adminPart = new AdminPart(this);
    private Component adminPartComponent;

    private WorkspaceComponent activeComponent;
    private Button adminButton;
    private Button logoutButton;
    private boolean navigationLocked;
    private boolean internalEvent = false;
    private Runnable onLogout;

    public Workspace() {

    }

    public Component create() throws Exception {
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();

        tabs = new HTabSheet();
        tabs.setSizeFull();
        tabs.setTabsWidth("260px");
        tabs.setTabsMaxWidth("300px");
        vl.add(tabs);

        manuscriptPartComponent = manuscriptPart.create();
        lorebookPartComponent = lorebookPart.create();
        userPartComponent = userPart.create();
        protocolPartComponent = protocolPart.create();
        aiPartComponent = aiPart.create();
        resourcesPartComponent = resourcesPart.create();
        trashPartComponent = trashPart.create();
        adminPartComponent = adminPart.create();

        HorizontalLayout manuscriptTabHeader = new HorizontalLayout();
        manuscriptTabHeader.setAlignItems(Alignment.CENTER);
        manuscriptTabHeader.setSpacing(true);
        Span manuscriptNameSpan = new Span(loc.getValue(L.LABEL_BOOKS));
        manuscriptTabHeader.add(manuscriptNameSpan);

        Tab manuscriptTab = tabs.add(manuscriptTabHeader, manuscriptPartComponent);
        tabToComponent.put(manuscriptTab, manuscriptPart);

        HorizontalLayout lorebookTabHeader = new HorizontalLayout();
        lorebookTabHeader.setAlignItems(Alignment.CENTER);
        lorebookTabHeader.setSpacing(true);
        Span lorebookNameSpan = new Span(loc.getValue(L.LABEL_LOREBOOKS));
        lorebookTabHeader.add(lorebookNameSpan);

        Tab lorebookTab = tabs.add(lorebookTabHeader, lorebookPartComponent);
        tabToComponent.put(lorebookTab, lorebookPart);

        HorizontalLayout userTabHeader = new HorizontalLayout();
        userTabHeader.setAlignItems(Alignment.CENTER);
        userTabHeader.setSpacing(true);
        Span userNameSpan = new Span(loc.getValue(L.LABEL_SETTINGS));
        userTabHeader.add(userNameSpan);

        Tab userTab = tabs.add(userTabHeader, userPartComponent);
        tabToComponent.put(userTab, userPart);

        HorizontalLayout protocolTabHeader = new HorizontalLayout();
        protocolTabHeader.setAlignItems(Alignment.CENTER);
        protocolTabHeader.setSpacing(true);
        Span protocolNameSpan = new Span(loc.getValue(L.LABEL_PROTOCOLS));
        protocolTabHeader.add(protocolNameSpan);

        Tab protocolTab = tabs.add(protocolTabHeader, protocolPartComponent);
        tabToComponent.put(protocolTab, protocolPart);

        HorizontalLayout aiTabHeader = new HorizontalLayout();
        aiTabHeader.setAlignItems(Alignment.CENTER);
        aiTabHeader.setSpacing(true);
        Span connNameSpan = new Span(loc.getValue(L.LABEL_MODELS));
        aiTabHeader.add(connNameSpan);

        Tab aiTab = tabs.add(aiTabHeader, aiPartComponent);
        tabToComponent.put(aiTab, aiPart);

        HorizontalLayout resourcesTabHeader = new HorizontalLayout();
        resourcesTabHeader.setAlignItems(Alignment.CENTER);
        resourcesTabHeader.setSpacing(true);
        resourcesTabHeader.add(new Span(loc.getValue(L.LABEL_RESOURCES)));

        Tab resourcesTab = tabs.add(resourcesTabHeader, resourcesPartComponent);
        tabToComponent.put(resourcesTab, resourcesPart);

        HorizontalLayout trashTabHeader = new HorizontalLayout();
        trashTabHeader.setAlignItems(Alignment.CENTER);
        trashTabHeader.setSpacing(true);
        trashTabHeader.add(new Span(loc.getValue(L.LABEL_TRASH)));

        Tab trashTab = tabs.add(trashTabHeader, trashPartComponent);
        tabToComponent.put(trashTab, trashPart);

        User user = userService.find(currentUser.getId());
        tabs.setFooterComponent(createFooter(user));

        tabs.addSelectedChangeListener(e -> {
            if (internalEvent)
                return;

            if (activeComponent != null) {
                try {
                    activeComponent.onTabClosed();
                } catch (Exception ex) {
                    UIUtils.internalServerError(loc, ex);
                }
            }

            WorkspaceComponent workspaceComponent = tabToComponent.get(e.getSelectedTab());
            activeComponent = workspaceComponent;
            if (workspaceComponent != null) {
                try {
                    workspaceComponent.onTabSwitched();
                } catch (Exception ex) {
                    UIUtils.internalServerError(loc, ex);
                }
            }
        });

        activeComponent = manuscriptPart;

        return vl;
    }

    private Component createFooter(User user) {
        VerticalLayout footer = new VerticalLayout();
        footer.setPadding(false);
        footer.setSpacing(true);
        footer.setWidthFull();

        if (user != null && user.isAdmin()) {
            footer.add(createAdminFooter());
        }

        Button changePasswordButton = new Button(loc.getValue(L.LABEL_CHANGE_PASSWORD), Solid.KEY.create());
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

    private Component createAdminFooter() {
        adminButton = new Button(loc.getValue(L.LABEL_ADMIN), Solid.USER_TIE.create());
        adminButton.setWidthFull();
        adminButton.addClickListener(e -> {
            if (activeComponent != null) {
                try {
                    activeComponent.onTabClosed();
                } catch (Exception ex) {
                    UIUtils.internalServerError(loc, ex);
                }
            }
            tabs.setCustomContent(adminPartComponent);
            activeComponent = adminPart;
            try {
                adminPart.onTabSwitched();
            } catch (Exception ex) {
                UIUtils.internalServerError(loc, ex);
            }
        });
        return adminButton;
    }

    /**
     * Locks the navigation (other tabs, admin and logout) while the active tab has unsaved changes.
     */
    public void setNavigationLocked(boolean locked) {
        navigationLocked = locked;
        Tab selectedTab = tabs.getSelectedTab();
        for (Tab tab : tabToComponent.keySet()) {
            tab.setEnabled(!locked || tab.equals(selectedTab));
        }
        if (adminButton != null) {
            adminButton.setEnabled(!locked);
        }
        if (logoutButton != null) {
            logoutButton.setEnabled(!locked);
        }
    }

    public boolean isNavigationLocked() {
        return navigationLocked;
    }

    public Runnable getOnLogout() {
        return onLogout;
    }

    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }

    public void refresh() throws Exception {
        internalEvent = true;
        try {

        } finally {
            internalEvent = false;
        }

        manuscriptPart.refresh();
        lorebookPart.refresh();
        userPart.refresh();
        protocolPart.refresh();
        aiPart.refresh();
        resourcesPart.refresh();
        trashPart.refresh();
        adminPart.refresh();
    }

}