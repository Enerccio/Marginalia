package com.github.enerccio.marginalia.ui.workspace;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.HTabSheet;
import com.github.enerccio.marginalia.ui.workspace.parts.AIPart;
import com.github.enerccio.marginalia.ui.workspace.parts.AdminPart;
import com.github.enerccio.marginalia.ui.workspace.parts.ProtocolPart;
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
public class Workspace {

    @Autowired
    private Localization loc;

    @Autowired
    private UserService userService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private User currentUser;

    private HTabSheet tabs;

    private final Map<Tab, WorkspaceComponent> tabToComponent = new HashMap<>();

    private final ProtocolPart protocolPart = new ProtocolPart(this);
    private Component protocolPartComponent;

    private final AIPart aiPart = new AIPart(this);
    private Component aiPartComponent;

    private final AdminPart adminPart = new AdminPart(this);
    private Component adminPartComponent;

    private WorkspaceComponent activeComponent;
    private boolean internalEvent = false;

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

        protocolPartComponent = protocolPart.create();
        aiPartComponent = aiPart.create();
        adminPartComponent = adminPart.create();

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

        User user = userService.find(currentUser.getId());
        if (user != null && user.isAdmin()) {
            tabs.setFooterComponent(createAdminFooter());
        }

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

        return vl;
    }

    private Component createAdminFooter() {
        Button adminButton = new Button(loc.getValue(L.LABEL_ADMIN), Solid.USER_TIE.create());
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

    public void refresh() throws Exception {
        internalEvent = true;
        try {

        } finally {
            internalEvent = false;
        }

        protocolPart.refresh();
        aiPart.refresh();
        adminPart.refresh();
    }

}