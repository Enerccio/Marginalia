package com.github.enerccio.marginalia.extensions.sidequery.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.sidequery.model.SideQueryData;
import com.github.enerccio.marginalia.extensions.sidequery.model.SideQuerySession;
import com.github.enerccio.marginalia.extensions.sidequery.service.SideQueryService;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;

import java.util.HashMap;
import java.util.Map;

public class SideQueryView extends VerticalLayout {

    private SideQueryService sideQueryService;

    private final Manuscript manuscript;
    private SideQueryData data;

    private HorizontalLayout tabHeader;
    private Tabs tabs;
    private Button addTabBtn;
    private VerticalLayout contentHolder;

    private boolean generating = false;

    private final Map<Tab, SideQuerySession> tabSessionMap = new HashMap<>();

    public SideQueryView(Manuscript manuscript, SideQueryService sideQueryService) {
        this.manuscript = manuscript;
        this.sideQueryService = sideQueryService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        loadData();
        buildUI();
    }

    private void loadData() {
        if (manuscript != null) {
            data = sideQueryService.getSideQueryData(manuscript);
        } else {
            data = new SideQueryData();
        }
    }

    private void saveData() {
        if (manuscript != null && data != null) {
            try {
                sideQueryService.saveSideQueryData(manuscript, data);
            } catch (Exception e) {
                UIUtils.internalServerError(null, e);
            }
        }
    }

    private void buildUI() {
        tabHeader = new HorizontalLayout();
        tabHeader.setWidthFull();
        tabHeader.setAlignItems(Alignment.CENTER);
        tabHeader.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        tabs = new Tabs();
        tabs.setWidthFull();
        tabs.addSelectedChangeListener(e -> {
            if (generating) {
                return;
            }
            Tab selected = e.getSelectedTab();
            if (selected != null && tabSessionMap.containsKey(selected)) {
                SideQuerySession session = tabSessionMap.get(selected);
                data.setActiveTab(data.getSessions().indexOf(session));
                saveData();
                showSessionContent(session);
            }
        });

        addTabBtn = new Button(Solid.PLUS.create(), e -> addNewTab());
        addTabBtn.setThemeName("tertiary icon small");

        tabHeader.add(tabs, addTabBtn);

        contentHolder = new VerticalLayout();
        contentHolder.setSizeFull();
        contentHolder.setPadding(false);
        contentHolder.setSpacing(false);

        add(tabHeader, contentHolder);
        setFlexGrow(1, contentHolder);

        refreshTabs();
    }

    private void refreshTabs() {
        tabs.removeAll();
        tabSessionMap.clear();

        for (int i = 0; i < data.getSessions().size(); i++) {
            SideQuerySession session = data.getSessions().get(i);

            HorizontalLayout tabContent = new HorizontalLayout();
            tabContent.setAlignItems(Alignment.CENTER);
            tabContent.setSpacing(true);

            Span titleSpan = new Span(session.getName());

            Button editBtn = new Button(Solid.PEN.create(), e -> renameTab(session));
            editBtn.setThemeName("tertiary icon small");

            Button closeBtn = new Button(Solid.TIMES.create(), e -> closeTab(session));
            closeBtn.setThemeName("tertiary icon small");
            closeBtn.setVisible(data.getSessions().size() > 1);

            tabContent.add(titleSpan, editBtn, closeBtn);

            Tab tab = new Tab(tabContent);
            tabSessionMap.put(tab, session);
            tabs.add(tab);
        }

        if (data.getActiveTab() >= 0 && data.getActiveTab() < data.getSessions().size()) {
            tabs.setSelectedIndex(data.getActiveTab());
            showSessionContent(data.getSessions().get(data.getActiveTab()));
        }
    }

    private void showSessionContent(SideQuerySession session) {
        contentHolder.removeAll();
        SideQueryTabContent tabContent = new SideQueryTabContent(
                sideQueryService,
                manuscript,
                session,
                this::saveData,
                this::setGenerating
        );
        contentHolder.add(tabContent);
    }

    public void setGenerating(boolean generating) {
        this.generating = generating;
        tabHeader.setEnabled(!generating);
    }

    private void addNewTab() {
        if (generating) return;
        SideQuerySession newSession = new SideQuerySession();
        newSession.setName("Tab " + (data.getSessions().size() + 1));
        data.getSessions().add(newSession);
        data.setActiveTab(data.getSessions().size() - 1);
        saveData();
        refreshTabs();
    }

    private void renameTab(SideQuerySession session) {
        if (generating) return;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Rename Tab");

        TextField nameField = new TextField("Tab Name");
        nameField.setWidthFull();
        nameField.setValue(session.getName());

        Button saveBtn = new Button("Save", e -> {
            String val = nameField.getValue() != null ? nameField.getValue().trim() : "";
            if (!val.isEmpty()) {
                session.setName(val);
                session.setManuallyRenamed(true);
                saveData();
                refreshTabs();
            }
            dialog.close();
        });
        saveBtn.setThemeName("primary");

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        dialog.add(new VerticalLayout(nameField));
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void closeTab(SideQuerySession session) {
        if (generating) return;
        if (data.getSessions().size() <= 1) {
            Notification.warning("Cannot close the last tab");
            return;
        }

        int index = data.getSessions().indexOf(session);
        data.getSessions().remove(session);

        if (data.getActiveTab() >= data.getSessions().size()) {
            data.setActiveTab(data.getSessions().size() - 1);
        }

        saveData();
        refreshTabs();
    }
}