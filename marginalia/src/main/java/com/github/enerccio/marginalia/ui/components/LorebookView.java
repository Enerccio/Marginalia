package com.github.enerccio.marginalia.ui.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.TemplateService;
import com.github.enerccio.marginalia.domain.service.TokenizerService;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.Restorable;
import com.github.enerccio.marginalia.ui.RestorableState;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.*;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.InputStreamDownloadCallback;
import com.vaadin.flow.server.streams.InputStreamDownloadHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Configurable
@Extendable
public class LorebookView extends VerticalLayout implements Restorable {

    @Autowired
    private Localization loc;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private TokenizerService tokenizerService;

    private Lorebook currentLorebook;
    private boolean pinnedLorebook = false;

    private ComboBox<Lorebook> lorebookCombo;
    private Button addLorebookButton;
    private Button deleteLorebookButton;
    private Button importLorebookButton;
    private Anchor exportLorebookAnchor;
    private Button exportLorebookButton;

    private TextField lorebookNameField;
    private Checkbox lorebookEnabledCheckbox;
    private MultiSelectComboBox<Lorebook> subLorebooksCombo;
    private TagMultiComboBox tagMultiComboBox;

    private Button addEntryButton;
    private Button refreshButton;

    private Grid<LorebookEntry> grid;
    private Span lorebookTokensSpan;

    private List<LorebookEntry> currentEntries = new ArrayList<>();
    private final Map<Long, Long> entryTokens = new HashMap<>();

    // uuids of the entries whose details are opened, kept over reloading the entries
    private final Set<String> openEntries = new LinkedHashSet<>();
    private Runnable onStateChange;

    public LorebookView() {
        this(null);
    }

    public LorebookView(boolean pinnedLorebook) {
        this(null, pinnedLorebook);
    }

    public LorebookView(Lorebook lorebook) {
        this(lorebook, lorebook != null);
    }

    public LorebookView(Lorebook lorebook, boolean pinnedLorebook) {
        if (lorebook != null) {
            this.currentLorebook = lorebook;
        }
        this.pinnedLorebook = pinnedLorebook;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "controlsLayout", type = HorizontalLayout.class),
                    @APILocals(name = "importMenu", type = ContextMenu.class),
                    @APILocals(name = "lorebookHeaderLayout", type = HorizontalLayout.class)})
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
            if (event.isFromClient()) {
                currentLorebook = event.getValue();
                openEntries.clear();
                updateSelectedLorebook();
                fireStateChanged();
            }
        });

        addLorebookButton = new Button(loc.getValue(L.LABEL_ADD_LOREBOOK), Solid.PLUS_CIRCLE.create(),event -> createNewLorebook());
        addLorebookButton.setThemeName("primary");
        addLorebookButton.setVisible(!pinnedLorebook);

        deleteLorebookButton = new Button(loc.getValue(L.LABEL_DELETE_LOREBOOK), Solid.TRASH.create(), event -> deleteCurrentLorebook());
        deleteLorebookButton.setThemeName("error");
        deleteLorebookButton.setVisible(!pinnedLorebook);

        importLorebookButton = new Button(Solid.FILE_IMPORT.create());
        importLorebookButton.setVisible(!pinnedLorebook);

        ContextMenu importMenu = new ContextMenu();
        importMenu.setTarget(importLorebookButton);
        importMenu.setOpenOnClick(true);
        importMenu.addItem(loc.getValue(L.LABEL_IMPORT_LOREBOOK), event ->
                openImportDialog(loc.getValue(L.LABEL_IMPORT_LOREBOOK), lorebookService::importLorebook));
        importMenu.addItem(loc.getValue(L.LABEL_IMPORT_FROM_SILLYTAVERN), event ->
                openImportDialog(loc.getValue(L.LABEL_IMPORT_FROM_SILLYTAVERN), lorebookService::importFromSillytavern));

        exportLorebookAnchor = new Anchor(new InputStreamDownloadHandler((InputStreamDownloadCallback) downloadEvent -> {
            try {
                byte[] data = lorebookService.exportLorebook(currentLorebook).getBytes(StandardCharsets.UTF_8);
                return new DownloadResponse(new ByteArrayInputStream(data),
                        getExportFileName(), "application/json", data.length);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }), "");
        exportLorebookAnchor.getElement().setAttribute("download", true);
        exportLorebookButton = new Button(Solid.FILE_EXPORT.create());
        exportLorebookButton.setTooltipText(loc.getValue(L.LABEL_EXPORT_LOREBOOK));
        exportLorebookAnchor.add(exportLorebookButton);

        addEntryButton = new Button(loc.getValue(L.LABEL_ADD_ENTRY), Solid.PLUS_CIRCLE.create(), event -> createNewEntry());
        addEntryButton.setThemeName("primary");

        refreshButton = new Button(loc.getValue(L.LABEL_REFRESH), Solid.REFRESH.create(), event -> {
            try {
                refreshEntries();
            } catch (Exception e) {
                UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
            }
        });

        controlsLayout.add(lorebookCombo, addLorebookButton, deleteLorebookButton, importLorebookButton, exportLorebookAnchor, addEntryButton, refreshButton);
        controlsLayout.setAlignItems(Alignment.END);

        HorizontalLayout lorebookHeaderLayout = new HorizontalLayout();
        lorebookHeaderLayout.setWidthFull();
        lorebookHeaderLayout.setAlignItems(FlexComponent.Alignment.BASELINE);

        lorebookNameField = new TextField(loc.getValue(L.LABEL_NAME));
        lorebookNameField.setWidthFull();
        lorebookNameField.addValueChangeListener(e -> {
            if (currentLorebook != null && e.isFromClient()) {
                try {
                    if (StringUtils.isBlank(e.getValue())) {
                        lorebookNameField.setInvalid(true);
                        lorebookNameField.setErrorMessage(loc.getValue(L.ERROR_NAME_CANT_BE_EMPTY));
                    } else {
                        lorebookNameField.setInvalid(false);
                        lorebookNameField.setErrorMessage("");
                        currentLorebook.setName(e.getValue());
                        lorebookService.save(currentLorebook);
                        if (!pinnedLorebook) {
                            loadLorebooks();
                            lorebookCombo.setValue(currentLorebook);
                        }
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

        tagMultiComboBox = new TagMultiComboBox(loc.getValue(L.LABEL_TAGS));
        tagMultiComboBox.setWidthFull();

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

        lorebookHeaderLayout.add(lorebookNameField, subLorebooksCombo, tagMultiComboBox, lorebookEnabledCheckbox);
        lorebookHeaderLayout.setFlexGrow(1, lorebookNameField);
        lorebookHeaderLayout.setFlexGrow(1, subLorebooksCombo);
        lorebookHeaderLayout.setFlexGrow(1, tagMultiComboBox);

        lorebookTokensSpan = createTokensSpan();

        setupGrid();

        add(controlsLayout, lorebookHeaderLayout, lorebookTokensSpan, grid);
        setFlexGrow(1, grid);

        refresh();

        return this;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = String.class),
            locals = {@APILocals(name = "name", type = String.class)})
    private String getExportFileName() {
        String name = currentLorebook != null ? StringUtils.defaultIfBlank(currentLorebook.getName(), "lorebook") : "lorebook";
        return name.replaceAll("[\\\\/:*?\"<>|]", "_") + ".json";
    }

    @FunctionalInterface
    private interface LorebookImporter {
        Lorebook importLorebook(String json, String name) throws Exception;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "title", type = String.class),
                    @APIParameter(name = "importer", type = LorebookView.LorebookImporter.class)},
            locals = {@APILocals(name = "dialog", type = Dialog.class),
                    @APILocals(name = "handler", type = InMemoryUploadHandler.class),
                    @APILocals(name = "upload", type = Upload.class),
                    @APILocals(name = "cancelButton", type = Button.class),
                    @APILocals(name = "dialogLayout", type = VerticalLayout.class)})
    private void openImportDialog(String title, LorebookImporter importer) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);
        dialog.setWidth("450px");

        InMemoryUploadHandler handler = new InMemoryUploadHandler((metadata, data) -> {
            try (InputStream inputStream = new ByteArrayInputStream(data)) {
                String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                String fileName = metadata.fileName();
                if (fileName.endsWith(".json")) {
                    fileName = fileName.substring(0, fileName.length() - 5);
                }

                Lorebook importedLorebook = importer.importLorebook(jsonContent, fileName);
                dialog.close();

                this.currentLorebook = importedLorebook;
                if (!pinnedLorebook) {
                    loadLorebooks();
                } else {
                    updateSelectedLorebook();
                }
            } catch (IllegalArgumentException | JsonParseException e) {
                Notification.warning(loc.getValue(L.MSG_INVALID_LOREBOOK_FILE));
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

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = boolean.class),
            parameters = {@APIParameter(name = "startId", type = Long.class),
                    @APIParameter(name = "targetId", type = Long.class),
                    @APIParameter(name = "lorebookMap", type = Map.class),
                    @APIParameter(name = "visited", type = Set.class)},
            locals = {@APILocals(name = "start", type = Lorebook.class),
                    @APILocals(name = "sub", type = Lorebook.class)})
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

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    private void setupGrid() {
        grid = new Grid<>(LorebookEntry.class, false);
        grid.setSizeFull();

        grid.addComponentColumn(entry -> {
            Checkbox cb = new Checkbox(entry.isEnabled());
            cb.addValueChangeListener(e -> {
                if (e.isFromClient()) {
                    entry.setEnabled(Boolean.TRUE.equals(e.getValue()));
                    saveEntry(entry);
                    updateLorebookTokens();
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

            Button toggleDetails = new Button((grid.isDetailsVisible(entry) ? Solid.ANGLE_UP : Solid.ANGLE_DOWN).create(), event -> {
                boolean isVisible = grid.isDetailsVisible(entry);
                grid.setDetailsVisible(entry, !isVisible);
                event.getSource().setIcon(!isVisible ? Solid.ANGLE_UP.create() : Solid.ANGLE_DOWN.create());
                if (isVisible) {
                    openEntries.remove(entry.getUuid());
                } else {
                    openEntries.add(entry.getUuid());
                }
                fireStateChanged();
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

        grid.setItemDetailsRenderer(new ComponentRenderer<>(this::createEntryDetailLayout));
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "detailsLayout", type = VerticalLayout.class),
                    @APILocals(name = "payloadField", type = TextAreaPopoverComponent.class),
                    @APILocals(name = "payloadTokensSpan", type = Span.class),
                    @APILocals(name = "settingsForm", type = FormLayout.class),
                    @APILocals(name = "filteringModeCombo", type = ComboBox.class),
                    @APILocals(name = "filteringField", type = TextField.class),
                    @APILocals(name = "insertionModeCombo", type = ComboBox.class),
                    @APILocals(name = "negativeTagCombo", type = TagMultiComboBox.class),
                    @APILocals(name = "commentField", type = TextArea.class)})
    public Component createEntryDetailLayout(LorebookEntry entry) {
        VerticalLayout detailsLayout = new VerticalLayout();
        detailsLayout.setWidthFull();
        detailsLayout.setPadding(false);
        detailsLayout.setSpacing(false);
        detailsLayout.getThemeList().add("spacing-xs");
        detailsLayout.addClassName(CssStyles.SUBTLE_BOX);

        TextAreaPopoverComponent payloadField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_CONTENT));
        payloadField.setWidthFull();
        ResizableTextArea.install(loc, payloadField, "160px");
        payloadField.setValue(StringUtils.defaultString(entry.getPayload()));
        payloadField.setPopoverContent(TemplateHints.create(loc, payloadField, payloadField.getPopover(), LorebookTemplateData.class, null));
        validatePayload(payloadField);

        Span payloadTokensSpan = createTokensSpan();
        payloadTokensSpan.setText(formatTokens(tokensOf(entry)));

        payloadField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                // invalid templates are still saved (user may be in the middle of editing), generation uses them as plain text
                validatePayload(payloadField);
                entry.setPayload(e.getValue());
                saveEntry(entry);
                payloadTokensSpan.setText(formatTokens(recountEntry(entry)));
                updateLorebookTokens();
            }
        });

        FormLayout settingsForm = new FormLayout();
        settingsForm.setResponsiveSteps(new ResponsiveStep("400px", 2), new ResponsiveStep("800px", 4));
        settingsForm.setWidthFull();

        ComboBox<FilteringMode> filteringModeCombo = new ComboBox<>(loc.getValue(L.LABEL_FILTERING_MODE));
        filteringModeCombo.setItems(FilteringMode.values());
        filteringModeCombo.setItemLabelGenerator(mode -> loc.getValue(loc.getFilteringMode(mode)));
        filteringModeCombo.setValue(entry.getFilteringMode());
        filteringModeCombo.setWidthFull();
        filteringModeCombo.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                entry.setFilteringMode(e.getValue());
                saveEntry(entry);
            }
        });

        TextField filteringField = new TextField(loc.getValue(L.LABEL_FILTERING));
        filteringField.setWidthFull();
        filteringField.setValue(StringUtils.defaultString(entry.getFiltering()));
        filteringField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                entry.setFiltering(e.getValue());
                saveEntry(entry);
            }
        });

        ComboBox<InsertionMode> insertionModeCombo = new ComboBox<>(loc.getValue(L.LABEL_INSERTION_MODE));
        insertionModeCombo.setItems(InsertionMode.values());
        insertionModeCombo.setItemLabelGenerator(mode -> loc.getValue(loc.getInsertionMode(mode)));
        insertionModeCombo.setValue(entry.getInsertionMode());
        insertionModeCombo.setWidthFull();
        insertionModeCombo.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                entry.setInsertionMode(e.getValue());
                saveEntry(entry);
            }
        });

        TagMultiComboBox negativeTagCombo = new TagMultiComboBox(loc.getValue(L.LABEL_NEGATIVE_TAGS), true);
        negativeTagCombo.setWidthFull();
        negativeTagCombo.setForEntity(entry, true);

        TextArea commentField = new TextArea(loc.getValue(L.LABEL_NOTE));
        commentField.setWidthFull();
        ResizableTextArea.install(loc, commentField, "100px");
        commentField.setValue(StringUtils.defaultString(entry.getComment()));
        commentField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                entry.setComment(e.getValue());
                saveEntry(entry);
            }
        });

        settingsForm.add(filteringModeCombo, filteringField, insertionModeCombo, negativeTagCombo);

        detailsLayout.add(payloadField, payloadTokensSpan, settingsForm, commentField);
        return detailsLayout;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Span.class),
            locals = {@APILocals(name = "span", type = Span.class)})
    private Span createTokensSpan() {
        Span span = new Span();
        span.addClassName(CssStyles.HINT_TEXT);
        return span;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "tokens", type = long.class)})
    private String formatTokens(long tokens) {
        return "~" + tokens + " " + loc.getValue(L.LABEL_TOKENS);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = long.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "cached", type = Long.class)})
    private long tokensOf(LorebookEntry entry) {
        Long cached = entry.getId() != null ? entryTokens.get(entry.getId()) : null;
        return cached != null ? cached : recountEntry(entry);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = long.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "ex", type = Exception.class)})
    private long recountEntry(LorebookEntry entry) {
        long tokens = 0;
        try {
            tokens = tokenizerService.countTokensApprox(StringUtils.defaultString(entry.getPayload()));
        } catch (Exception ex) {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), ex);
        }
        if (entry.getId() != null) {
            entryTokens.put(entry.getId(), tokens);
        }
        return tokens;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "entry", type = LorebookEntry.class)})
    private void updateLorebookTokens() {
        if (currentLorebook == null) {
            lorebookTokensSpan.setText("");
            return;
        }
        long total = 0;
        for (LorebookEntry entry : currentEntries) {
            if (entry.isEnabled()) {
                total += tokensOf(entry);
            }
        }
        lorebookTokensSpan.setText(formatTokens(total));
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "payloadField", type = TextArea.class)},
            locals = {@APILocals(name = "payload", type = String.class),
                    @APILocals(name = "result", type = TemplateService.ValidationResult.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void validatePayload(TextArea payloadField) {
        String payload = payloadField.getValue();
        if (StringUtils.isBlank(payload)) {
            payloadField.setInvalid(false);
            TemplateHints.showWarnings(loc, payloadField, null);
            return;
        }
        try {
            TemplateService.ValidationResult result = templateService.isValidTemplate(payload, "lorebookEntry", LorebookTemplateData.class);
            payloadField.setErrorMessage(result.isValid() ? null : loc.getValue(L.MSG_INVALID_TEMPLATE) + result.errorMessage());
            payloadField.setInvalid(!result.isValid());
            TemplateHints.showWarnings(loc, payloadField, result);
        } catch (Exception e) {
            payloadField.setErrorMessage(loc.getValue(L.MSG_INVALID_TEMPLATE) + e.getMessage());
            payloadField.setInvalid(true);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    public void refresh() throws Exception {
        if (!pinnedLorebook) {
            loadLorebooks();
        } else {
            updateSelectedLorebook();
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "lorebooks", type = List.class),
                    @APILocals(name = "e", type = Exception.class)})
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

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "allLorebooks", type = List.class),
                    @APILocals(name = "availableLorebooks", type = List.class),
                    @APILocals(name = "subIds", type = Set.class),
                    @APILocals(name = "selectedSubbooks", type = Set.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void updateSelectedLorebook() {
        boolean hasLorebook = currentLorebook != null;
        lorebookNameField.setEnabled(hasLorebook);
        lorebookEnabledCheckbox.setEnabled(hasLorebook);
        subLorebooksCombo.setEnabled(hasLorebook);
        addEntryButton.setEnabled(hasLorebook);
        deleteLorebookButton.setEnabled(hasLorebook);
        exportLorebookButton.setEnabled(hasLorebook);
        exportLorebookAnchor.setEnabled(hasLorebook);
        refreshButton.setEnabled(hasLorebook);
        tagMultiComboBox.setEnabled(hasLorebook);
        tagMultiComboBox.setForEntity(currentLorebook);

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
            currentEntries = new ArrayList<>();
            entryTokens.clear();
            updateLorebookTokens();
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "entries", type = List.class),
                    @APILocals(name = "e", type = Exception.class)})
    public void refreshEntries() {
        if (currentLorebook == null || currentLorebook.getId() == null) {
            grid.setItems(new ArrayList<>());
            currentEntries = new ArrayList<>();
            entryTokens.clear();
            updateLorebookTokens();
            return;
        }
        try {
            List<LorebookEntry> entries = lorebookEntryService.getEntriesForLorebook(currentLorebook.getId());
            grid.setItems(entries);
            showOpenEntries(entries);
            currentEntries = entries;
            entryTokens.clear();
            updateLorebookTokens();
        } catch (Exception e) {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "newLorebook", type = Lorebook.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void createNewLorebook() {
        try {
            Lorebook newLorebook = new Lorebook();
            newLorebook.setName(loc.getValue(L.LABEL_NEW_LOREBOOK));
            newLorebook.setEnabled(true);
            newLorebook = lorebookService.save(newLorebook);
            currentLorebook = newLorebook;
            openEntries.clear();
            loadLorebooks();
            fireStateChanged();
        } catch (Exception e) {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    private void deleteCurrentLorebook() {
        if (currentLorebook == null) {
            return;
        }
        ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> {
            try {
                lorebookService.delete(currentLorebook, false);
                currentLorebook = null;
                openEntries.clear();
                if (!pinnedLorebook) {
                    loadLorebooks();
                } else {
                    updateSelectedLorebook();
                }
                fireStateChanged();
            } catch (Exception e) {
                UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
            }
        });
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "entry", type = LorebookEntry.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void createNewEntry() {
        if (currentLorebook == null) {
            return;
        }
        try {
            LorebookEntry entry = new LorebookEntry();
            entry.setLorebook(currentLorebook);
            entry.setName(loc.getValue(L.LABEL_NEW_LOREBOOK_ENTRY));
            entry.setEnabled(true);
            entry.setOrder(100);
            lorebookEntryService.save(entry);
            refreshEntries();
        } catch (Exception e) {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void saveEntry(LorebookEntry entry) {
        try {
            lorebookEntryService.save(entry);
        } catch (Exception e) {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Lorebook.class))
    public Lorebook getCurrentLorebook() {
        return currentLorebook;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "lorebook", type = Lorebook.class)})
    public void setLorebook(Lorebook lorebook) {
        if (!Objects.equals(currentLorebook, lorebook)) {
            openEntries.clear();
        }
        this.currentLorebook = lorebook;
        updateSelectedLorebook();
    }

    /**
     * Opens the details of the entries that were opened and still exist, forgets the others.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "entries", type = List.class)},
            locals = {@APILocals(name = "entry", type = LorebookEntry.class)})
    private void showOpenEntries(List<LorebookEntry> entries) {
        openEntries.retainAll(entries.stream().map(LorebookEntry::getUuid).collect(Collectors.toSet()));
        for (LorebookEntry entry : entries) {
            if (openEntries.contains(entry.getUuid())) {
                grid.setDetailsVisible(entry, true);
            }
        }
    }

    /**
     * Called after the user changed what {@link #storeState()} stores (the lorebook, an opened entry), the owner
     * passes it on to the workspace autosave.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "onStateChange", type = Runnable.class)})
    public void setOnStateChange(Runnable onStateChange) {
        this.onStateChange = onStateChange;
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class))
    private void fireStateChanged() {
        if (onStateChange != null) {
            onStateChange.run();
        }
    }

    /**
     * The lorebook (unless it is pinned: its owner chooses it) and the entries whose details are opened.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = JsonObject.class),
            locals = {@APILocals(name = "state", type = JsonObject.class)})
    @Override
    public JsonObject storeState() {
        JsonObject state = new JsonObject();
        if (!pinnedLorebook && currentLorebook != null && currentLorebook.getUuid() != null) {
            state.addProperty("lorebook", currentLorebook.getUuid());
        }
        if (!openEntries.isEmpty()) {
            state.add("entries", RestorableState.array(openEntries));
        }
        return state;
    }

    /**
     * Selects the stored lorebook when it still exists (otherwise the lorebook stays as it is) and opens the stored
     * entries that are still in it.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "self", type = JsonObject.class)},
            locals = {@APILocals(name = "lorebookUuid", type = String.class),
                    @APILocals(name = "stored", type = Lorebook.class),
                    @APILocals(name = "e", type = Exception.class)})
    @Override
    public void restoreState(JsonObject self) {
        if (grid == null) {
            return;
        }
        try {
            String lorebookUuid = RestorableState.string(self, "lorebook");
            if (!pinnedLorebook && lorebookUuid != null) {
                Lorebook stored = lorebookService.findForUser(lorebookUuid);
                if (stored != null) {
                    currentLorebook = stored;
                }
            }
            openEntries.clear();
            openEntries.addAll(RestorableState.strings(self, "entries"));
            refresh();
        } catch (Exception e) {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
        }
    }
}
