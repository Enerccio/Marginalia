package com.github.enerccio.marginalia.ui.workspace.phone;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.domain.service.AgentService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.LLMInteraction;
import com.github.enerccio.marginalia.ui.RestorableState;
import com.github.enerccio.marginalia.ui.components.InvalidExtensionsWarning;
import com.github.enerccio.marginalia.ui.dialogs.UserDialog;
import com.github.enerccio.marginalia.ui.widgets.LanguageSelector;
import com.github.enerccio.marginalia.ui.workspace.*;
import com.github.enerccio.marginalia.ui.workspace.parts.AgentPart;
import com.github.enerccio.marginalia.ui.workspace.parts.ResourcesPart;
import com.github.enerccio.marginalia.ui.workspace.phone.parts.*;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonObject;
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

import java.util.*;

/**
 * Workspace for phones. The vertical tab sheet of {@link DesktopWorkspace} would take most of the screen, so the
 * parts are reached from a drawer (opened by the toggle in the top bar) and the active part fills the whole screen.
 * The book list, the lorebooks, the protocols, the models and the trash are the phone specific {@link PhoneManuscriptPart}, {@link PhoneLorebookPart}, {@link PhoneProtocolPart}, {@link PhoneAIPart} and {@link PhoneTrashPart}, the settings are the {@link PhoneSettingsPart}, other parts are shared with the desktop.
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

    @Autowired
    private AgentService agentService;

    private final PhoneManuscriptPart manuscriptPart = new PhoneManuscriptPart(this);
    private final PhoneLorebookPart lorebookPart = new PhoneLorebookPart(this);
    private final PhoneSettingsPart settingsPart = new PhoneSettingsPart(this);
    private final PhoneProtocolPart protocolPart = new PhoneProtocolPart(this);
    private final PhoneAIPart aiPart = new PhoneAIPart(this);
    private final ResourcesPart resourcesPart = new ResourcesPart(this);
    private final PhoneTrashPart trashPart = new PhoneTrashPart(this);
    // the tab exists only for users who may use agents
    private AgentPart agentPart;

    // names of the parts in the stored state, the same as in the desktop workspace
    private final Map<String, WorkspaceComponent> partsByKey = new LinkedHashMap<>();
    {
        partsByKey.put("books", manuscriptPart);
        partsByKey.put("lorebooks", lorebookPart);
        partsByKey.put("settings", settingsPart);
        partsByKey.put("protocols", protocolPart);
        partsByKey.put("models", aiPart);
        partsByKey.put("resources", resourcesPart);
        partsByKey.put("trash", trashPart);
    }

    private final WorkspaceAutosave autosave = new WorkspaceAutosave(this);

    private final Map<Tab, WorkspaceComponent> tabToComponent = new HashMap<>();
    private final Map<Tab, Component> tabToContent = new HashMap<>();
    private final Map<Tab, String> tabToTitle = new HashMap<>();

    private AppLayout layout;
    private DrawerToggle drawerToggle;
    private Span title;
    private Tabs tabs;
    private WorkspaceComponent activeComponent;
    private Button changePasswordButton;
    private Button logoutButton;
    private LanguageSelector languageSelector;
    private boolean navigationLocked;
    private Runnable onLogout;

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "drawer", type = VerticalLayout.class),
                    @APILocals(name = "first", type = Tab.class)})
    @Override
    public Component create() throws Exception {
        layout = new AppLayout();
        layout.addClassName(CssStyles.FULL_SIZE);

        drawerToggle = new DrawerToggle();
        title = new Span();
        title.addClassName(CssStyles.SECTION_TITLE);
        layout.addToNavbar(drawerToggle, title);
        layout.addToNavbar(true, createInvalidExtensionsWarning());

        tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.setWidthFull();

        addTab(L.LABEL_BOOKS, manuscriptPart);
        addTab(L.LABEL_LOREBOOKS, lorebookPart);
        addTab(L.LABEL_SETTINGS, settingsPart);
        addTab(L.LABEL_PROTOCOLS, protocolPart);
        addTab(L.LABEL_MODELS, aiPart);
        addAgentsTab();
        addTab(L.LABEL_RESOURCES, resourcesPart);
        addTab(L.LABEL_TRASH, trashPart);

        tabs.addSelectedChangeListener(e -> {
            Tab selected = e.getSelectedTab();
            show(tabToComponent.get(selected), tabToContent.get(selected), tabToTitle.get(selected));
            autosave();
        });

        VerticalLayout drawer = new VerticalLayout();
        drawer.setSizeFull();
        drawer.setPadding(false);
        drawer.setSpacing(false);
        drawer.add(tabs);
        drawer.setFlexGrow(1, tabs);
        drawer.add(createFooter());
        layout.addToDrawer(drawer);

        Tab first = tabs.getSelectedTab();
        show(tabToComponent.get(first), tabToContent.get(first), tabToTitle.get(first));

        return layout;
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = String.class),
            locals = {@APILocals(name = "keys", type = List.class),
                    @APILocals(name = "active", type = String.class)})
    @Override
    public String getLLMState() {
        List<String> keys = new ArrayList<>(partsByKey.keySet());
        String active = partsByKey.entrySet().stream()
                .filter(entry -> entry.getValue() == activeComponent).map(Map.Entry::getKey).findFirst().orElse("books");
        return WorkspaceLLMSupport.describe(loc, "phone", keys, active, navigationLocked);
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
        String active = partsByKey.entrySet().stream()
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
        partsByKey.values().forEach(part -> tabToComponent.forEach((tab, candidate) -> {
            if (candidate == part) {
                parts.put(part, tabToContent.get(tab));
            }
        }));
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
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "label", type = L.class),
                    @APIParameter(name = "part", type = WorkspaceComponent.class)},
            locals = {@APILocals(name = "name", type = String.class),
                    @APILocals(name = "tab", type = Tab.class)})
    private void addTab(L label, WorkspaceComponent part) throws Exception {
        String name = loc.getValue(label);
        Tab tab = new Tab(name);
        tabs.add(tab);
        tabToComponent.put(tab, part);
        tabToContent.put(tab, part.create());
        tabToTitle.put(tab, name);
    }

    /**
     * The tab of the agents, for a user who may use them: the same part as the desktop's, with a card for each agent, and the
     * count of the agents that need the user beside the name.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "tab", type = Tab.class)})
    private void addAgentsTab() throws Exception {
        if (!agentService.isAllowed()) {
            return;
        }
        agentPart = new AgentPart(this, true);
        Tab tab = new Tab(new Span(loc.getValue(L.LABEL_AGENTS)), agentPart.getAttentionBadge());
        tabs.add(tab);
        tabToComponent.put(tab, agentPart);
        tabToContent.put(tab, agentPart.create());
        tabToTitle.put(tab, loc.getValue(L.LABEL_AGENTS));
        partsByKey.put("agents", agentPart);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "part", type = WorkspaceComponent.class),
                    @APIParameter(name = "content", type = Component.class),
                    @APIParameter(name = "name", type = String.class)},
            locals = {@APILocals(name = "ex", type = Exception.class)})
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

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "footer", type = VerticalLayout.class)})
    private Component createFooter() {
        VerticalLayout footer = new VerticalLayout();
        footer.setSpacing(true);
        footer.setWidthFull();

        languageSelector = new LanguageSelector();
        languageSelector.setWidthFull();
        footer.add(languageSelector);

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

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void refresh() throws Exception {
        manuscriptPart.refresh();
        lorebookPart.refresh();
        settingsPart.refresh();
        protocolPart.refresh();
        aiPart.refresh();
        resourcesPart.refresh();
        trashPart.refresh();
        if (agentPart != null) {
            agentPart.refresh();
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "locked", type = boolean.class)},
            locals = {@APILocals(name = "tab", type = Tab.class)})
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
