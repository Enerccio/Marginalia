package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.UIConstants;
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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    private TextField lorebookNameField;
    private Checkbox lorebookEnabledCheckbox;

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

        addLorebookButton = new Button(loc.getValue(L.LABEL_ADD_LOREBOOK), VaadinIcon.PLUS.create(), event -> createNewLorebook());
        addLorebookButton.setThemeName("primary");
        addLorebookButton.setVisible(!pinnedLorebook);

        deleteLorebookButton = new Button(loc.getValue(L.LABEL_DELETE_LOREBOOK), VaadinIcon.TRASH.create(), event -> deleteCurrentLorebook());
        deleteLorebookButton.setThemeName("error");

        addEntryButton = new Button(loc.getValue(L.LABEL_ADD_ENTRY), VaadinIcon.PLUS_CIRCLE.create(), event -> createNewEntry());
        addEntryButton.setThemeName("primary");

        refreshButton = new Button(loc.getValue(L.LABEL_REFRESH), VaadinIcon.REFRESH.create(), event -> {
            try {
                refreshEntries();
            } catch (Exception e) {
                UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
            }
        });

        controlsLayout.add(lorebookCombo, addLorebookButton, deleteLorebookButton, addEntryButton, refreshButton);
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

        lorebookHeaderLayout.add(lorebookNameField, lorebookEnabledCheckbox);
        lorebookHeaderLayout.setFlexGrow(1, lorebookNameField);

        setupGrid();

        add(controlsLayout, lorebookHeaderLayout, grid);
        setFlexGrow(1, grid);

        refresh();

        return this;
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

            Button toggleDetails = new Button(VaadinIcon.ANGLE_DOWN.create(), event -> {
                boolean isVisible = grid.isDetailsVisible(entry);
                grid.setDetailsVisible(entry, !isVisible);
                event.getSource().setIcon(!isVisible ? VaadinIcon.ANGLE_UP.create() : VaadinIcon.ANGLE_DOWN.create());
            });
            toggleDetails.setThemeName("tertiary");

            Button deleteBtn = new Button(VaadinIcon.TRASH.create(), event -> {
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

            detailsLayout.add(payloadField, commentField);
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
        addEntryButton.setEnabled(hasLorebook);
        deleteLorebookButton.setEnabled(hasLorebook);
        refreshButton.setEnabled(hasLorebook);

        if (hasLorebook) {
            lorebookNameField.setValue(StringUtils.defaultString(currentLorebook.getName()));
            lorebookEnabledCheckbox.setValue(currentLorebook.isEnabled());
            refreshEntries();
        } else {
            lorebookNameField.setValue("");
            lorebookEnabledCheckbox.setValue(false);
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
}