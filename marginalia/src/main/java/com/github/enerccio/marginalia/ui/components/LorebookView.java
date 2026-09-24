package com.github.enerccio.marginalia.ui.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.TagMultiComboBox;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Configurable
public class LorebookView extends VerticalLayout {

    @Autowired
    private Localization loc;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    private Lorebook currentLorebook;
    private boolean pinnedLorebook = false;

    private ComboBox<Lorebook> lorebookCombo;
    private Button addLorebookButton;
    private Button deleteLorebookButton;
    private Button importLorebookButton;

    private TextField lorebookNameField;
    private Checkbox lorebookEnabledCheckbox;
    private MultiSelectComboBox<Lorebook> subLorebooksCombo;

    private Button addEntryButton;
    private Button refreshButton;

    private Grid<LorebookEntry> grid;

    public LorebookView() {
        this(null);
    }

    public LorebookView(Lorebook lorebook) {
        if (lorebook != null) {
            this.currentLorebook = lorebook;
            this.pinnedLorebook = true;
        }
    }

    public Component create() throws Exception {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        HorizontalLayout controlsLayout = new HorizontalLayout();
        controlsLayout.setWidthFull();
        controlsLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        lorebookCombo = new ComboBox<>(loc.getValue(L.LABEL_LOREBOOK));
        lorebookCombo.setItemLabelGenerator(Lorebook::getName);
        lorebookCombo.setWidth("280px");
        lorebookCombo.setVisible(!pinnedLorebook);
        lorebookCombo.addValueChangeListener(event -> {
            currentLorebook = event.getValue();
            updateSelectedLorebook();
        });

        addLorebookButton = new Button(loc.getValue(L.LABEL_ADD_LOREBOOK), Solid.PLUS_CIRCLE.create(),event -> createNewLorebook());
        addLorebookButton.setThemeName("primary");
        addLorebookButton.setVisible(!pinnedLorebook);

        deleteLorebookButton = new Button(loc.getValue(L.LABEL_DELETE_LOREBOOK), Solid.TRASH.create(), event -> deleteCurrentLorebook());
        deleteLorebookButton.setThemeName("error");

        importLorebookButton = new Button(Solid.FILE_IMPORT.create());
        importLorebookButton.setVisible(!pinnedLorebook);

        ContextMenu importMenu = new ContextMenu();
        importMenu.setTarget(importLorebookButton);
        importMenu.setOpenOnClick(true);
        importMenu.addItem(loc.getValue(L.LABEL_IMPORT_FROM_SILLYTAVERN), event -> openImportSillyTavernDialog());

        addEntryButton = new Button(loc.getValue(L.LABEL_ADD_ENTRY), Solid.PLUS_CIRCLE.create(), event -> createNewEntry());
        addEntryButton.setThemeName("primary");

        refreshButton = new Button(loc.getValue(L.LABEL_REFRESH), Solid.REFRESH.create(), event -> {
            try {
                refreshEntries();
            } catch (Exception e) {
                UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
            }
        });

        controlsLayout.add(lorebookCombo, addLorebookButton, deleteLorebookButton, importLorebookButton, addEntryButton, refreshButton);
        controlsLayout.setAlignItems(Alignment.END);

        HorizontalLayout lorebookHeaderLayout = new HorizontalLayout();
        lorebookHeaderLayout.setWidthFull();
        lorebookHeaderLayout.setAlignItems(FlexComponent.Alignment.BASELINE);

        lorebookNameField = new TextField(loc.getValue(L.LABEL_NAME));
        lorebookNameField.setWidthFull();
        lorebookNameField.addValueChangeListener(e -> {
            if (currentLorebook != null && e.isFromClient()) {
                try {
                    currentLorebook.setName(e.getValue());
                    lorebookService.save(currentLorebook);
                    if (!pinnedLorebook) {
                        loadLorebooks();
                        lorebookCombo.setValue(currentLorebook);
                    }
                } catch (Exception ex) {
                    UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), ex);
                }
            }
        });

        subLorebooksCombo = new MultiSelectComboBox<>(loc.getValue(L.LABEL_SUB_LOREBOOKS));
        subLorebooksCombo.setItemLabelGenerator(Lorebook::getName);
        subLorebooksCombo.setWidthFull();
        subLorebooksCombo.addValueChangeListener(e -> {
            if (currentLorebook != null && e.isFromClient()) {
                Set<Lorebook> newSelection = e.getValue();
                if (newSelection != null && !newSelection.isEmpty()) {
                    try {
                        List<Lorebook> allUserLorebooks = lorebookService.findAllForUser();
                        Map<Long, Lorebook> lorebookMap = allUserLorebooks.stream()
                                .filter(l -> l.getId() != null)
                                .collect(Collectors.toMap(BaseEntity::getId, l -> l, (a, b) -> a));

                        for (Lorebook candidate : newSelection) {
                            if (candidate.getId() != null && (candidate.getId().equals(currentLorebook.getId())
                                    || isReachable(candidate.getId(), currentLorebook.getId(), lorebookMap, new HashSet<>()))) {
                                Notification.warning(loc.getValue(L.MSG_CYCLE_DETECTED));
                                subLorebooksCombo.setValue(e.getOldValue() != null ? e.getOldValue() : Collections.emptySet());
                                return;
                            }
                        }
                    } catch (Exception ex) {
                        UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), ex);
                        return;
                    }
                }
                try {
                    currentLorebook.setSubbooks(newSelection != null ? new ArrayList<>(newSelection) : new ArrayList<>());
                    lorebookService.save(currentLorebook);
                } catch (Exception ex) {
                    UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), ex);
                }
            }
        });

        lorebookEnabledCheckbox = new Checkbox(loc.getValue(L.LABEL_ENABLED));
        lorebookEnabledCheckbox.addValueChangeListener(e -> {
            if (currentLorebook != null && e.isFromClient()) {
                try {
                    currentLorebook.setEnabled(Boolean.TRUE.equals(e.getValue()));
                    lorebookService.save(currentLorebook);
                } catch (Exception ex) {
                    UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), ex);
                }
            }
        });

        lorebookHeaderLayout.add(lorebookNameField, subLorebooksCombo, lorebookEnabledCheckbox);
        lorebookHeaderLayout.setFlexGrow(1, lorebookNameField);
        lorebookHeaderLayout.setFlexGrow(1, subLorebooksCombo);

        setupGrid();

        add(controlsLayout, lorebookHeaderLayout, grid);
        setFlexGrow(1, grid);

        refresh();

        return this;
    }

    private void openImportSillyTavernDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(loc.getValue(L.LABEL_IMPORT_FROM_SILLYTAVERN));
        dialog.setWidth("450px");

        InMemoryUploadHandler handler = new InMemoryUploadHandler((metadata, data) -> {
            try (InputStream inputStream = new ByteArrayInputStream(data)) {
                String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                String fileName = metadata.fileName();
                if (fileName.endsWith(".json")) {
                    fileName = fileName.substring(0, fileName.length() - 5);
                }

                Lorebook importedLorebook = lorebookService.importFromSillytavern(jsonContent, fileName);
                dialog.close();

                this.currentLorebook = importedLorebook;
                if (!pinnedLorebook) {
                    loadLorebooks();
                } else {
                    updateSelectedLorebook();
                }
            } catch (Exception e) {
                UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
            }
        });

        Upload upload = new Upload(handler);
        upload.setAcceptedMimeTypes("application/json");
        Button cancelButton = new Button(loc.getValue(L.LABEL_CANCEL), event -> dialog.close());
        VerticalLayout dialogLayout = new VerticalLayout(upload);
        dialogLayout.setPadding(true);

        dialog.add(dialogLayout);
        dialog.getFooter().add(cancelButton);
        dialog.open();
    }

    private boolean isReachable(Long startId, Long targetId, Map<Long, Lorebook> lorebookMap, Set<Long> visited) {
        if (startId == null || targetId == null) {
            return false;
        }
        if (startId.equals(targetId)) {
            return true;
        }
        if (!visited.add(startId)) {
            return false;
        }
        Lorebook start = lorebookMap.get(startId);
        if (start == null || start.getSubbooks() == null) {
            return false;
        }
        for (Lorebook sub : start.getSubbooks()) {
            if (sub != null && sub.getId() != null) {
                if (isReachable(sub.getId(), targetId, lorebookMap, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setupGrid() {
        grid = new Grid<>(LorebookEntry.class, false);
        grid.setSizeFull();

        grid.addComponentColumn(entry -> {
            Checkbox cb = new Checkbox(entry.isEnabled());
            cb.addValueChangeListener(e -> {
                if (e.isFromClient()) {
                    entry.setEnabled(Boolean.TRUE.equals(e.getValue()));
                    saveEntry(entry);
                }
            });
            return cb;
        }).setHeader("").setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE);

        grid.addComponentColumn(entry -> {
            TextField tf = new TextField();
            tf.setWidthFull();
            tf.setValue(StringUtils.defaultString(entry.getName()));
            tf.addValueChangeListener(e -> {
                if (e.isFromClient()) {
                    entry.setName(e.getValue());
                    saveEntry(entry);
                }
            });
            return tf;
        }).setHeader(loc.getValue(L.LABEL_ENTRY_NAME)).setFlexGrow(2);

        grid.addComponentColumn(entry -> {
            IntegerField orderField = new IntegerField();
            orderField.setWidth("100px");
            orderField.setValue(entry.getOrder());
            orderField.addValueChangeListener(e -> {
                if (e.isFromClient() && e.getValue() != null) {
                    entry.setOrder(e.getValue());
                    saveEntry(entry);
                }
            });
            return orderField;
        }).setHeader(loc.getValue(L.LABEL_ORDER)).setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE_LARGE);

        grid.addComponentColumn(entry -> {
            TagMultiComboBox tagCombo = new TagMultiComboBox();
            tagCombo.setWidthFull();
            tagCombo.setForEntity(entry);
            return tagCombo;
        }).setHeader(loc.getValue(L.LABEL_TAGS)).setFlexGrow(2);

        grid.addComponentColumn(entry -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button toggleDetails = new Button(Solid.ANGLE_DOWN.create(), event -> {
                boolean isVisible = grid.isDetailsVisible(entry);
                grid.setDetailsVisible(entry, !isVisible);
                event.getSource().setIcon(!isVisible ? Solid.ANGLE_UP.create() : Solid.ANGLE_DOWN.create());
            });
            toggleDetails.setThemeName("tertiary");

            Button deleteBtn = new Button(Solid.TRASH.create(), event -> {
                ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> {
                    try {
                        lorebookEntryService.delete(entry, false);
                        refreshEntries();
                    } catch (Exception ex) {
                        UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), ex);
                    }
                });
            });
            deleteBtn.setThemeName("error tertiary");

            actions.add(toggleDetails, deleteBtn);
            return actions;
        }).setHeader("").setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE_HUGE);

        grid.setItemDetailsRenderer(new ComponentRenderer<>(entry -> {
            VerticalLayout detailsLayout = new VerticalLayout();
            detailsLayout.setWidthFull();
            detailsLayout.setPadding(true);
            detailsLayout.setSpacing(true);
            detailsLayout.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
            detailsLayout.getStyle().set("border-radius", "var(--lumo-border-radius-m)");

            TextArea payloadField = new TextArea(loc.getValue(L.LABEL_CONTENT));
            payloadField.setWidthFull();
            payloadField.setMinHeight("100px");
            payloadField.setValue(StringUtils.defaultString(entry.getPayload()));
            payloadField.addValueChangeListener(e -> {
                if (e.isFromClient()) {
                    entry.setPayload(e.getValue());
                    saveEntry(entry);
                }
            });

            TagMultiComboBox negativeTagCombo = new TagMultiComboBox(loc.getValue(L.LABEL_NEGATIVE_TAGS), true);
            negativeTagCombo.setWidthFull();
            negativeTagCombo.setForEntity(entry, true);

            TextArea commentField = new TextArea(loc.getValue(L.LABEL_NOTE));
            commentField.setWidthFull();
            commentField.setMinHeight("60px");
            commentField.setValue(StringUtils.defaultString(entry.getComment()));
            commentField.addValueChangeListener(e -> {
                if (e.isFromClient()) {
                    entry.setComment(e.getValue());
                    saveEntry(entry);
                }
            });

            detailsLayout.add(payloadField, negativeTagCombo, commentField);
            return detailsLayout;
        }));
    }

    public void refresh() throws Exception {
        if (!pinnedLorebook) {
            loadLorebooks();
        } else {
            updateSelectedLorebook();
        }
    }

    private void loadLorebooks() {
        try {
            List<Lorebook> lorebooks = lorebookService.findAllForUser();
            lorebooks.sort(Comparator.comparing(Lorebook::getName));
            lorebookCombo.setItems(lorebooks);
            if (currentLorebook == null && !lorebooks.isEmpty()) {
                currentLorebook = lorebooks.getFirst();
            }
            if (currentLorebook != null) {
                lorebookCombo.setValue(currentLorebook);
            }
            updateSelectedLorebook();
        } catch (Exception e) {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
        }
    }

    private void updateSelectedLorebook() {
        boolean hasLorebook = currentLorebook != null;
        lorebookNameField.setEnabled(hasLorebook);
        lorebookEnabledCheckbox.setEnabled(hasLorebook);
        subLorebooksCombo.setEnabled(hasLorebook);
        addEntryButton.setEnabled(hasLorebook);
        deleteLorebookButton.setEnabled(hasLorebook);
        refreshButton.setEnabled(hasLorebook);

        if (hasLorebook) {
            lorebookNameField.setValue(StringUtils.defaultString(currentLorebook.getName()));
            lorebookEnabledCheckbox.setValue(currentLorebook.isEnabled());

            try {
                List<Lorebook> allLorebooks = lorebookService.findAllForUser();
                List<Lorebook> availableLorebooks = allLorebooks.stream()
                        .filter(l -> l.getId() != null && !l.getId().equals(currentLorebook.getId()))
                        .sorted(Comparator.comparing(Lorebook::getName))
                        .collect(Collectors.toList());
                subLorebooksCombo.setItems(availableLorebooks);

                if (currentLorebook.getSubbooks() != null) {
                    Set<Long> subIds = currentLorebook.getSubbooks().stream()
                            .map(BaseEntity::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    Set<Lorebook> selectedSubbooks = availableLorebooks.stream()
                            .filter(l -> subIds.contains(l.getId()))
                            .collect(Collectors.toSet());
                    subLorebooksCombo.setValue(selectedSubbooks);
                } else {
                    subLorebooksCombo.setValue(Collections.emptySet());
                }
            } catch (Exception e) {
                UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
            }

            refreshEntries();
        } else {
            lorebookNameField.setValue("");
            lorebookEnabledCheckbox.setValue(false);
            subLorebooksCombo.setValue(Collections.emptySet());
            grid.setItems(new ArrayList<>());
        }
    }

    public void refreshEntries() {
        if (currentLorebook == null || currentLorebook.getId() == null) {
            grid.setItems(new ArrayList<>());
            return;
        }
        try {
            List<LorebookEntry> entries = lorebookEntryService.getEntriesForLorebook(currentLorebook.getId());
            grid.setItems(entries);
        } catch (Exception e) {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
        }
    }

    private void createNewLorebook() {
        try {
            Lorebook newLorebook = new Lorebook();
            newLorebook.setName(loc.getValue(L.LABEL_NEW_LOREBOOK));
            newLorebook.setEnabled(true);
            newLorebook = lorebookService.save(newLorebook);
            currentLorebook = newLorebook;
            loadLorebooks();
        } catch (Exception e) {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
        }
    }

    private void deleteCurrentLorebook() {
        if (currentLorebook == null) {
            return;
        }
        ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> {
            try {
                lorebookService.delete(currentLorebook, false);
                currentLorebook = null;
                if (!pinnedLorebook) {
                    loadLorebooks();
                } else {
                    updateSelectedLorebook();
                }
            } catch (Exception e) {
                UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
            }
        });
    }

    private void createNewEntry() {
        if (currentLorebook == null) {
            return;
        }
        try {
            LorebookEntry entry = new LorebookEntry();
            entry.setLorebook(currentLorebook);
            entry.setName(loc.getValue(L.LABEL_NEW_LOREBOOK));
            entry.setEnabled(true);
            entry.setOrder(100);
            lorebookEntryService.save(entry);
            refreshEntries();
        } catch (Exception e) {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
        }
    }

    private void saveEntry(LorebookEntry entry) {
        try {
            lorebookEntryService.save(entry);
        } catch (Exception e) {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
        }
    }

    public Lorebook getCurrentLorebook() {
        return currentLorebook;
    }

    public void setLorebook(Lorebook lorebook) {
        this.currentLorebook = lorebook;
        updateSelectedLorebook();
    }
}