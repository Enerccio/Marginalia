package com.github.enerccio.marginalia.ui.workspace;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.LLMInteraction;
import com.github.enerccio.marginalia.ui.RestorableState;
import com.github.enerccio.marginalia.ui.components.InvalidExtensionsWarning;
import com.github.enerccio.marginalia.ui.dialogs.UserDialog;
import com.github.enerccio.marginalia.ui.widgets.HTabSheet;
import com.github.enerccio.marginalia.ui.widgets.LanguageSelector;
import com.github.enerccio.marginalia.ui.workspace.parts.*;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

@Configurable
@Extendable
public class DesktopWorkspace implements Workspace {

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

    // names of the parts in the stored state, so it survives reordering the tabs
    private final Map<String, WorkspaceComponent> partsByKey = new LinkedHashMap<>();
    {
        partsByKey.put("books", manuscriptPart);
        partsByKey.put("lorebooks", lorebookPart);
        partsByKey.put("settings", userPart);
        partsByKey.put("protocols", protocolPart);
        partsByKey.put("models", aiPart);
        partsByKey.put("resources", resourcesPart);
        partsByKey.put("trash", trashPart);
    }

    private final WorkspaceAutosave autosave = new WorkspaceAutosave(this);

    private WorkspaceComponent activeComponent;
    private Button adminButton;
    private Button logoutButton;
    private LanguageSelector languageSelector;
    private boolean navigationLocked;
    private Runnable onLogout;

    public DesktopWorkspace() {

    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "vl", type = VerticalLayout.class),
                    @APILocals(name = "manuscriptTabHeader", type = HorizontalLayout.class),
                    @APILocals(name = "manuscriptNameSpan", type = Span.class),
                    @APILocals(name = "manuscriptTab", type = Tab.class),
                    @APILocals(name = "lorebookTabHeader", type = HorizontalLayout.class),
                    @APILocals(name = "lorebookNameSpan", type = Span.class),
                    @APILocals(name = "lorebookTab", type = Tab.class),
                    @APILocals(name = "userTabHeader", type = HorizontalLayout.class),
                    @APILocals(name = "userNameSpan", type = Span.class),
                    @APILocals(name = "userTab", type = Tab.class),
                    @APILocals(name = "protocolTabHeader", type = HorizontalLayout.class),
                    @APILocals(name = "protocolNameSpan", type = Span.class),
                    @APILocals(name = "protocolTab", type = Tab.class),
                    @APILocals(name = "aiTabHeader", type = HorizontalLayout.class),
                    @APILocals(name = "connNameSpan", type = Span.class),
                    @APILocals(name = "aiTab", type = Tab.class),
                    @APILocals(name = "resourcesTabHeader", type = HorizontalLayout.class),
                    @APILocals(name = "resourcesTab", type = Tab.class),
                    @APILocals(name = "trashTabHeader", type = HorizontalLayout.class),
                    @APILocals(name = "trashTab", type = Tab.class),
                    @APILocals(name = "user", type = User.class)})
    @Override
    public Component create() throws Exception {
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();

        tabs = new HTabSheet();
        tabs.setSizeFull();
        tabs.setTabsWidth("260px");
        tabs.setTabsMaxWidth("300px");
        vl.add(createInvalidExtensionsWarning(), tabs);

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
            autosave();
        });

        activeComponent = manuscriptPart;

        return vl;
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = String.class),
            locals = {@APILocals(name = "keys", type = List.class),
                    @APILocals(name = "active", type = String.class)})
    @Override
    public String getLLMState() {
        List<String> keys = new ArrayList<>(partsByKey.keySet());
        String active = activeComponent == adminPart ? "admin" : partsByKey.entrySet().stream()
                .filter(entry -> entry.getValue() == activeComponent).map(Map.Entry::getKey).findFirst().orElse("books");
        if (adminButton != null) {
            keys.add("admin");
        }
        return WorkspaceLLMSupport.describe(loc, "desktop", keys, active, navigationLocked);
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = Map.class),
            locals = {@APILocals(name = "sections", type = Map.class),
                    @APILocals(name = "active", type = String.class)})
    @Override
    public Map<String, LLMInteraction> getInteraction() {
        Map<String, Runnable> sections = new LinkedHashMap<>();
        partsByKey.forEach((key, part) -> tabToComponent.forEach((tab, candidate) -> {
            if (candidate == part) {
                sections.put(key, () -> tabs.setSelectedTab(tab));
            }
        }));
        if (adminButton != null) {
            sections.put("admin", this::openAdmin);
        }
        String active = activeComponent == adminPart ? "admin" : partsByKey.entrySet().stream()
                .filter(entry -> entry.getValue() == activeComponent).map(Map.Entry::getKey).findFirst().orElse("books");
        return WorkspaceLLMSupport.openSections(loc, sections, active, navigationLocked);
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = List.class),
            parameters = {@APIParameter(name = "deep", type = boolean.class)},
            locals = {@APILocals(name = "parts", type = Map.class)})
    @Override
    public List<LLMInteract> getLLMChildren(boolean deep) {
        Map<WorkspaceComponent, Component> parts = new LinkedHashMap<>();
        parts.put(manuscriptPart, manuscriptPartComponent);
        parts.put(lorebookPart, lorebookPartComponent);
        parts.put(userPart, userPartComponent);
        parts.put(protocolPart, protocolPartComponent);
        parts.put(aiPart, aiPartComponent);
        parts.put(resourcesPart, resourcesPartComponent);
        parts.put(trashPart, trashPartComponent);
        if (adminButton != null) {
            parts.put(adminPart, adminPartComponent);
        }
        return LLMInteract.parts(parts, activeComponent, deep);
    }

    /**
     * The red bar about invalid extensions, hidden while there are none. Extensions that become invalid later show
     * when the user logs in again (the workspace is built once per login).
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "user", type = User.class)})
    private Component createInvalidExtensionsWarning() throws Exception {
        User user = userService.find(currentUser.getId());
        return new InvalidExtensionsWarning(user != null && user.isAdmin()).create();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "user", type = User.class)},
            locals = {@APILocals(name = "footer", type = VerticalLayout.class),
                    @APILocals(name = "changePasswordButton", type = Button.class)})
    private Component createFooter(User user) {
        VerticalLayout footer = new VerticalLayout();
        footer.setPadding(false);
        footer.setSpacing(true);
        footer.setWidthFull();

        if (user != null && user.isAdmin()) {
            footer.add(createAdminFooter());
        }

        languageSelector = new LanguageSelector();
        languageSelector.setWidthFull();
        footer.add(languageSelector);

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

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class))
    private Component createAdminFooter() {
        adminButton = new Button(loc.getValue(L.LABEL_ADMIN), Solid.USER_TIE.create());
        adminButton.setWidthFull();
        adminButton.addClickListener(e -> openAdmin());
        return adminButton;
    }

    /**
     * Shows the Admin page in place of the open section, what the Admin button does.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "ex", type = Exception.class)})
    private void openAdmin() {
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
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "locked", type = boolean.class)},
            locals = {@APILocals(name = "selectedTab", type = Tab.class),
                    @APILocals(name = "tab", type = Tab.class)})
    @Override
    public void setNavigationLocked(boolean locked) {
        navigationLocked = locked;
        Tab selectedTab = tabs.getSelectedTab();
        for (Tab tab : tabToComponent.keySet()) {
            tab.setEnabled(!locked || tab.equals(selectedTab));
        }
        if (adminButton != null) {
            adminButton.setEnabled(!locked);
        }
        if (languageSelector != null) {
            languageSelector.setEnabled(!locked);
        }
        if (logoutButton != null) {
            logoutButton.setEnabled(!locked);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = boolean.class))
    @Override
    public boolean isNavigationLocked() {
        return navigationLocked;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = ManuscriptSection.class))
    @Override
    public ManuscriptSection getManuscriptSection() {
        return manuscriptPart;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Runnable.class))
    @Override
    public Runnable getOnLogout() {
        return onLogout;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "onLogout", type = Runnable.class)})
    @Override
    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void refresh() throws Exception {
        manuscriptPart.refresh();
        lorebookPart.refresh();
        userPart.refresh();
        protocolPart.refresh();
        aiPart.refresh();
        resourcesPart.refresh();
        trashPart.refresh();
        // the admin part (backups, cleanup) refuses anybody but administrators; only they get the button
        if (adminButton != null) {
            adminPart.refresh();
        }
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void autosave() {
        autosave.save();
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void restoreAutosaved() {
        autosave.restore();
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = JsonObject.class),
            locals = {@APILocals(name = "state", type = JsonObject.class),
                    @APILocals(name = "parts", type = JsonObject.class),
                    @APILocals(name = "selected", type = WorkspaceComponent.class)})
    @Override
    public JsonObject storeState() {
        JsonObject state = new JsonObject();
        JsonObject parts = new JsonObject();
        WorkspaceComponent selected = tabs == null ? null : tabToComponent.get(tabs.getSelectedTab());
        partsByKey.forEach((key, part) -> {
            RestorableState.put(parts, key, part.storeState());
            if (part == selected) {
                state.addProperty("tab", key);
            }
        });
        RestorableState.put(state, "parts", parts);
        return state;
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "self", type = JsonObject.class)},
            locals = {@APILocals(name = "parts", type = JsonObject.class),
                    @APILocals(name = "part", type = WorkspaceComponent.class)})
    @Override
    public void restoreState(JsonObject self) {
        JsonObject parts = RestorableState.object(self, "parts");
        partsByKey.forEach((key, part) -> {
            JsonObject partState = RestorableState.object(parts, key);
            if (partState != null) {
                part.restoreState(partState);
            }
        });

        WorkspaceComponent part = partsByKey.get(RestorableState.string(self, "tab"));
        if (part != null) {
            tabToComponent.forEach((tab, component) -> {
                if (component == part && !tab.equals(tabs.getSelectedTab())) {
                    tabs.setSelectedTab(tab);
                }
            });
        }
    }

}
