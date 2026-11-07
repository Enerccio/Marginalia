package com.github.enerccio.marginalia.ui.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.ActivationContext;
import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.Restorable;
import com.github.enerccio.marginalia.ui.RestorableState;
import com.github.enerccio.marginalia.ui.dialogs.*;
import com.github.enerccio.marginalia.ui.dialogs.search.LorebookSearchReplaceDialog;
import com.github.enerccio.marginalia.ui.widgets.*;
import com.github.enerccio.marginalia.utils.MarkdownArchive;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.InputStreamDownloadCallback;
import com.vaadin.flow.server.streams.InputStreamDownloadHandler;
import com.vaadin.flow.shared.Registration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link LorebookView} for phones: the same things can be done, laid out in one column. The lorebook is chosen in a
 * box, the buttons of the lorebooks (add, clone, delete, import, export, tags, description) are in a menu, its
 * settings (name, sub lorebooks, tags, scan depth, sharing) are in a section that opens, and an entry opens into a
 * form with all of its settings. Not here: the check box of root lorebooks only and the selection of many entries to
 * delete (the entry has a delete button).
 * <p>
 * It is also the view of a lorebook that is locked by an agent (an agent works on a book that uses it): it then says so
 * and changes nothing (the settings are shown as text, as before the phone could edit them), and the desktop
 * {@link LorebookView} shows this view for such a lorebook.
 */
@Configurable
@Extendable
public class PhoneLorebookView extends VerticalLayout implements Restorable, LLMInteract {

    @Autowired
    private Localization loc;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private TokenizerService tokenizerService;

    @Autowired
    private AgentRunService agentRunService;

    private Lorebook currentLorebook;
    private boolean pinnedLorebook = false;

    private ComboBox<Lorebook> lorebookCombo;
    private Button refreshButton;
    private Button searchReplaceButton;
    private Button previewActivationButton;
    private TextField entrySearchField;
    private ComboBox<String> entryTagFilter;
    private ComboBox<LorebookEntryFilter.Status> entryStatusFilter;
    private final LorebookEntryFilter entryFilter = new LorebookEntryFilter();
    private Button deleteSelectedButton;
    private MenuItem selectEntriesItem;
    private HorizontalLayout entryFilterLayout;
    private Registration selectionListener;
    private ListDataProvider<LorebookEntry> entryProvider;
    private Checkbox lorebookEnabledCheckbox;
    private Span lorebookInfoSpan;
    private Span lorebookTokensSpan;
    private Grid<LorebookEntry> grid;

    // the lorebook menu and the settings of the lorebook, hidden while an agent locks it
    private Button lorebookMenuButton;
    private ContextMenu lorebookMenu;
    private final List<MenuItem> lorebookMenuItems = new ArrayList<>();
    private MenuItem deleteLorebookItem;
    private MenuItem exportItem;
    private Button addEntryButton;
    private Button entriesMenuButton;
    private Details settingsDetails;
    private TextField lorebookNameField;
    private MultiSelectComboBox<Lorebook> subLorebooksCombo;
    private TagMultiComboBox tagMultiComboBox;
    private IntegerField scanDepthField;
    private IntegerField ownScanDepthField;
    private Checkbox lorebookSharedCheckbox;
    private Span lorebookDescriptionLine;

    private List<LorebookEntry> currentEntries = new ArrayList<>();
    private final Map<Long, Long> entryTokens = new HashMap<>();

    // uuids of the entries whose details are opened, kept over reloading the entries
    private final Set<String> openEntries = new LinkedHashSet<>();
    private Runnable onStateChange;
    // name of the agent that locks the lorebook (null when it is free), the view then changes nothing
    private String lockedBy;
    private Span lockedSpan;

    public PhoneLorebookView() {
        this(null);
    }

    public PhoneLorebookView(Lorebook lorebook) {
        this.currentLorebook = lorebook;
    }

    /**
     * @param pinnedLorebook the lorebook is chosen by the owner of the view ({@link #setLorebook}), so the lorebook
     *                       selection is hidden, like in {@link LorebookView}
     */
    public PhoneLorebookView(boolean pinnedLorebook) {
        this(null);
        this.pinnedLorebook = pinnedLorebook;
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "controlsLayout", type = HorizontalLayout.class),
                    @APILocals(name = "filterLayout", type = HorizontalLayout.class),
                    @APILocals(name = "enabledLayout", type = HorizontalLayout.class),
                    @APILocals(name = "entriesLayout", type = HorizontalLayout.class)})
    public Component create() throws Exception {
        setSizeFull();
        setPadding(false);
        setSpacing(true);

        lorebookCombo = new ComboBox<>(loc.getValue(L.LABEL_LOREBOOK));
        lorebookCombo.setItemLabelGenerator(Lorebook::getName);
        lorebookCombo.setWidthFull();
        lorebookCombo.setVisible(!pinnedLorebook);
        lorebookCombo.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                currentLorebook = event.getValue();
                openEntries.clear();
                updateSelectedLorebook();
                fireStateChanged();
            }
        });

        refreshButton = new Button(Solid.REFRESH.create(), _ -> {
            try {
                refresh();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });

        lorebookMenuButton = new Button(Solid.ELLIPSIS_V.create());
        lorebookMenuButton.setAriaLabel(loc.getValue(L.LABEL_LOREBOOKS));
        createLorebookMenu();

        HorizontalLayout controlsLayout = new HorizontalLayout(lorebookCombo, lorebookMenuButton, refreshButton);
        controlsLayout.setWidthFull();
        controlsLayout.setAlignItems(FlexComponent.Alignment.END);
        controlsLayout.setFlexGrow(1, lorebookCombo);
        // a pinned lorebook has no box to choose from, it still has the menu (export, description) and the refresh
        controlsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        lorebookEnabledCheckbox = new Checkbox(loc.getValue(L.LABEL_ENABLED));
        lorebookEnabledCheckbox.addValueChangeListener(e -> {
            if (currentLorebook != null && e.isFromClient()) {
                try {
                    currentLorebook.setEnabled(Boolean.TRUE.equals(e.getValue()));
                    lorebookService.save(currentLorebook);
                } catch (Exception ex) {
                    UIUtils.internalServerError(loc, ex);
                }
            }
        });

        lorebookTokensSpan = createTokensSpan();

        searchReplaceButton = new Button(Solid.EXCHANGE_ALT.create(), _ -> openSearchReplaceDialog());
        searchReplaceButton.setTooltipText(loc.getValue(L.LABEL_SEARCH_AND_REPLACE));

        previewActivationButton = new Button(Solid.EYE.create(), _ -> openActivationPreview());
        previewActivationButton.setTooltipText(loc.getValue(L.LABEL_PREVIEW_ACTIVATION));

        entrySearchField = new TextField();
        entrySearchField.setPlaceholder(loc.getValue(L.LABEL_SEARCH));
        entrySearchField.setPrefixComponent(Solid.SEARCH.create());
        entrySearchField.setClearButtonVisible(true);
        entrySearchField.setWidthFull();
        entrySearchField.setValueChangeMode(ValueChangeMode.LAZY);
        entrySearchField.addValueChangeListener(_ -> applyEntrySearch());

        entryTagFilter = LorebookEntryFilter.createTagBox(loc);
        entryTagFilter.addValueChangeListener(_ -> applyEntrySearch());

        entryStatusFilter = LorebookEntryFilter.createStatusBox(loc);
        entryStatusFilter.addValueChangeListener(_ -> applyEntrySearch());
        HorizontalLayout filterLayout = new HorizontalLayout(entryTagFilter, entryStatusFilter);
        filterLayout.setWidthFull();
        filterLayout.setFlexGrow(1, entryTagFilter);
        filterLayout.setFlexGrow(1, entryStatusFilter);
        entryTagFilter.setWidth("0");
        entryStatusFilter.setWidth("0");
        entryFilterLayout = filterLayout;

        deleteSelectedButton = new Button(loc.getValue(L.LABEL_DELETE_SELECTED), Solid.TRASH.create(), _ -> deleteSelectedEntries());
        deleteSelectedButton.setThemeName("error");
        deleteSelectedButton.setWidthFull();
        deleteSelectedButton.setVisible(false);

        addEntryButton = new Button(loc.getValue(L.LABEL_ADD_ENTRY), Solid.PLUS_CIRCLE.create(), _ -> createNewEntry());
        addEntryButton.setThemeName("primary");

        entriesMenuButton = new Button(Solid.ELLIPSIS_V.create());
        entriesMenuButton.setAriaLabel(loc.getValue(L.LABEL_ENTRIES));
        createEntriesMenu();

        HorizontalLayout enabledLayout = new HorizontalLayout(lorebookEnabledCheckbox, lorebookTokensSpan, searchReplaceButton, previewActivationButton);
        enabledLayout.setWidthFull();
        enabledLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        enabledLayout.addClassName(CssStyles.FLEX_WRAP);

        HorizontalLayout entriesLayout = new HorizontalLayout(addEntryButton, entriesMenuButton);
        entriesLayout.setWidthFull();
        entriesLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        entriesLayout.setFlexGrow(1, addEntryButton);

        createSettings();

        lorebookInfoSpan = new Span();
        lorebookInfoSpan.addClassNames(CssStyles.HINT_TEXT, CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE);

        setupGrid();

        lockedSpan = new Span();
        lockedSpan.addClassNames(CssStyles.HINT_TEXT, CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE);
        lockedSpan.setVisible(false);
        add(lockedSpan, controlsLayout, enabledLayout, settingsDetails, lorebookDescriptionLine, lorebookInfoSpan, entriesLayout, entrySearchField, entryFilterLayout, deleteSelectedButton, grid);
        setFlexGrow(1, grid);

        refresh();

        return this;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    private void setupGrid() {
        grid = new Grid<>(LorebookEntry.class, false);
        grid.setSizeFull();
        // the buttons of the rows are the only way to open details, so that openEntries knows about all of them
        grid.setDetailsVisibleOnClick(false);
        // rows grow with their content, so a long name breaks into lines instead of running under the button
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.addComponentColumn(this::createEntryRow).setFlexGrow(1);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setItemDetailsRenderer(new ComponentRenderer<>(this::createEntryDetailLayout));
    }


    /**
     * The menu of the lorebook button: add, clone, delete, import, export, tags and description. A pinned view (the
     * lorebook of a book) has only the export and the description.
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "importItem", type = MenuItem.class)})
    private void createLorebookMenu() {
        lorebookMenu = new ContextMenu();
        lorebookMenu.setTarget(lorebookMenuButton);
        lorebookMenu.setOpenOnClick(true);

        if (!pinnedLorebook) {
            lorebookMenuItems.add(lorebookMenu.addItem(loc.getValue(L.LABEL_ADD_LOREBOOK), _ -> createNewLorebook()));
            lorebookMenuItems.add(lorebookMenu.addItem(loc.getValue(L.LABEL_CLONE_LOREBOOK), _ -> cloneCurrentLorebook()));
            deleteLorebookItem = lorebookMenu.addItem(loc.getValue(L.LABEL_DELETE_LOREBOOK), _ -> deleteCurrentLorebook());
            lorebookMenuItems.add(deleteLorebookItem);

            MenuItem importItem = lorebookMenu.addItem(loc.getValue(L.LABEL_IMPORT));
            importItem.getSubMenu().addItem(loc.getValue(L.LABEL_IMPORT_LOREBOOK), _ ->
                    openImportDialog(loc.getValue(L.LABEL_IMPORT_LOREBOOK), lorebookService::importLorebook));
            importItem.getSubMenu().addItem(loc.getValue(L.LABEL_IMPORT_FROM_SILLYTAVERN), _ ->
                    openImportDialog(loc.getValue(L.LABEL_IMPORT_FROM_SILLYTAVERN), lorebookService::importFromSillytavern));
            importItem.getSubMenu().addItem(loc.getValue(L.LABEL_IMPORT_FROM_ARCHIVE), _ -> openArchiveImportDialog());
            importItem.getSubMenu().addItem(loc.getValue(L.LABEL_IMPORT_FROM_OTHER_USER), _ ->
                    SharedImportDialog.forLorebooks(loc, lorebookService, this::showImported).open());
            lorebookMenuItems.add(importItem);
        }

        exportItem = lorebookMenu.addItem(loc.getValue(L.LABEL_EXPORT_LOREBOOK));
        exportItem.getSubMenu().addItem(createExportLink(loc.getValue(L.LABEL_EXPORT_LOREBOOK), lorebookService::exportLorebook));
        exportItem.getSubMenu().addItem(createExportLink(loc.getValue(L.LABEL_EXPORT_TO_SILLYTAVERN), lorebookService::exportToSillytavern));

        if (!pinnedLorebook) {
            lorebookMenuItems.add(lorebookMenu.addItem(loc.getValue(L.LABEL_TAG_LOREBOOKS), _ -> openTagDialog(false)));
        }
        lorebookMenuItems.add(lorebookMenu.addItem(loc.getValue(L.LABEL_EDIT_DESCRIPTION), _ -> editDescription()));
    }

    /**
     * The menu of the entries: mass edit and tags of many entries.
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "menu", type = ContextMenu.class)})
    private void createEntriesMenu() {
        ContextMenu menu = new ContextMenu();
        menu.setTarget(entriesMenuButton);
        menu.setOpenOnClick(true);
        menu.addItem(loc.getValue(L.LABEL_MASS_EDIT), _ -> openMassEditDialog());
        menu.addItem(loc.getValue(L.LABEL_TAG_ENTRIES), _ -> openTagDialog(true));
        selectEntriesItem = menu.addItem(loc.getValue(L.LABEL_SELECT_ENTRIES), _ -> toggleSelection());
        selectEntriesItem.setCheckable(true);
    }

    /**
     * The settings of the lorebook in a section that is closed at the start: the name, the sub lorebooks, the tags, the scan
     * depth and sharing. While an agent locks the lorebook the section is hidden and the info line says what they are.
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "content", type = VerticalLayout.class)})
    private void createSettings() {
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
                    UIUtils.internalServerError(loc, ex);
                }
            }
        });

        subLorebooksCombo = new MultiSelectComboBox<>(loc.getValue(L.LABEL_SUB_LOREBOOKS));
        subLorebooksCombo.setItemLabelGenerator(Lorebook::getName);
        subLorebooksCombo.setWidthFull();
        subLorebooksCombo.addValueChangeListener(e -> {
            if (currentLorebook != null && e.isFromClient()) {
                try {
                    currentLorebook = lorebookService.setSubbooks(currentLorebook, e.getValue());
                    updateLorebookInfo();
                } catch (LorebookCycleException ex) {
                    Notification.warning(loc.getValue(L.MSG_CYCLE_DETECTED));
                    subLorebooksCombo.setValue(e.getOldValue() != null ? e.getOldValue() : Collections.emptySet());
                } catch (Exception ex) {
                    UIUtils.internalServerError(loc, ex);
                }
            }
        });

        tagMultiComboBox = new TagMultiComboBox(loc.getValue(L.LABEL_TAGS));
        tagMultiComboBox.setWidthFull();

        scanDepthField = new IntegerField(loc.getValue(L.LABEL_SCAN_DEPTH));
        scanDepthField.setMin(0);
        scanDepthField.setMax(LorebookActivationService.MAX_SCAN_DEPTH);
        scanDepthField.setStepButtonsVisible(true);
        scanDepthField.setWidthFull();
        scanDepthField.setHelperText(loc.getValue(L.HELP_SCAN_DEPTH));
        scanDepthField.addValueChangeListener(e -> {
            if (currentLorebook != null && e.isFromClient() && e.getValue() != null) {
                try {
                    currentLorebook.setScanDepth(Math.max(0, Math.min(LorebookActivationService.MAX_SCAN_DEPTH, e.getValue())));
                    currentLorebook = lorebookService.save(currentLorebook);
                } catch (Exception ex) {
                    UIUtils.internalServerError(loc, ex);
                }
            }
        });

        // empty = the scan depth of the lorebook the book uses; only counts while this lorebook is a sub lorebook of another
        ownScanDepthField = new IntegerField(loc.getValue(L.LABEL_OWN_SCAN_DEPTH));
        ownScanDepthField.setMin(0);
        ownScanDepthField.setMax(LorebookActivationService.MAX_SCAN_DEPTH);
        ownScanDepthField.setStepButtonsVisible(true);
        ownScanDepthField.setClearButtonVisible(true);
        ownScanDepthField.setWidthFull();
        ownScanDepthField.setHelperText(loc.getValue(L.HELP_OWN_SCAN_DEPTH));
        ownScanDepthField.addValueChangeListener(e -> {
            if (currentLorebook != null && e.isFromClient()) {
                try {
                    currentLorebook.setOwnScanDepth(e.getValue() == null ? null
                            : Math.max(0, Math.min(LorebookActivationService.MAX_SCAN_DEPTH, e.getValue())));
                    currentLorebook = lorebookService.save(currentLorebook);
                } catch (Exception ex) {
                    UIUtils.internalServerError(loc, ex);
                }
            }
        });

        lorebookSharedCheckbox = new Checkbox(loc.getValue(L.LABEL_SHARE_WITH_OTHER_USERS));
        lorebookSharedCheckbox.setHelperText(loc.getValue(L.HELP_LOREBOOK_SHARED));
        lorebookSharedCheckbox.addValueChangeListener(e -> {
            if (currentLorebook != null && e.isFromClient()) {
                changeShared(Boolean.TRUE.equals(e.getValue()));
            }
        });

        VerticalLayout content = new VerticalLayout(lorebookNameField, subLorebooksCombo, tagMultiComboBox, scanDepthField, ownScanDepthField, lorebookSharedCheckbox);
        content.setPadding(false);
        content.setSpacing(false);
        content.getThemeList().add("spacing-xs");
        settingsDetails = new Details(loc.getValue(L.LABEL_SETTINGS), content);
        settingsDetails.setWidthFull();

        lorebookDescriptionLine = new Span();
        lorebookDescriptionLine.addClassNames(CssStyles.TEXT_SECONDARY, CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE);
        lorebookDescriptionLine.setVisible(false);
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = String.class),
            locals = {@APILocals(name = "name", type = String.class)})
    private String getExportFileName() {
        String name = currentLorebook != null ? StringUtils.defaultIfBlank(currentLorebook.getName(), "lorebook") : "lorebook";
        return name.replaceAll("[\\\\/:*?\"<>|]", "_") + ".json";
    }

    @FunctionalInterface
    private interface LorebookExporter {
        String export(Lorebook lorebook) throws Exception;
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = Anchor.class),
            parameters = {@APIParameter(name = "text", type = String.class),
                    @APIParameter(name = "exporter", type = PhoneLorebookView.LorebookExporter.class)},
            locals = {@APILocals(name = "link", type = Anchor.class)})
    private Anchor createExportLink(String text, LorebookExporter exporter) {
        Anchor link = new Anchor(new InputStreamDownloadHandler((InputStreamDownloadCallback) downloadEvent -> {
            try {
                byte[] data = exporter.export(currentLorebook).getBytes(StandardCharsets.UTF_8);
                return new DownloadResponse(new ByteArrayInputStream(data),
                        getExportFileName(), "application/json", data.length);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }), text);
        link.getElement().setAttribute("download", true);
        return link;
    }

    @FunctionalInterface
    private interface LorebookImporter {
        Lorebook importLorebook(String json, String name) throws Exception;
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "title", type = String.class),
                    @APIParameter(name = "importer", type = PhoneLorebookView.LorebookImporter.class)},
            locals = {@APILocals(name = "dialog", type = Dialog.class),
                    @APILocals(name = "handler", type = InMemoryUploadHandler.class),
                    @APILocals(name = "upload", type = Upload.class),
                    @APILocals(name = "cancelButton", type = Button.class),
                    @APILocals(name = "dialogLayout", type = VerticalLayout.class)})
    private void openImportDialog(String title, LorebookImporter importer) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);
        dialog.setWidth("450px");
        UIUtils.fitPhone(dialog, false);

        InMemoryUploadHandler handler = new InMemoryUploadHandler((metadata, data) -> {
            try (InputStream inputStream = new ByteArrayInputStream(data)) {
                String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                String fileName = metadata.fileName();
                if (fileName.endsWith(".json")) {
                    fileName = fileName.substring(0, fileName.length() - 5);
                }
                Lorebook importedLorebook = importer.importLorebook(jsonContent, fileName);
                dialog.close();
                showImported(importedLorebook);
            } catch (IllegalArgumentException | JsonParseException e) {
                Notification.warning(loc.getValue(L.MSG_INVALID_LOREBOOK_FILE));
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });

        Upload upload = new Upload(handler);
        upload.setAcceptedMimeTypes("application/json");
        Button cancelButton = new Button(loc.getValue(L.LABEL_CANCEL), _ -> dialog.close());
        VerticalLayout dialogLayout = new VerticalLayout(upload);
        dialogLayout.setPadding(true);

        dialog.add(dialogLayout);
        dialog.getFooter().add(cancelButton);
        dialog.open();
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class))
    private void openArchiveImportDialog() {
        ArchiveUploadDialog.show(loc.getValue(L.LABEL_IMPORT_FROM_ARCHIVE), loc.getValue(L.MSG_IMPORT_FROM_ARCHIVE),
                (archive, fileName) -> {
                    MarkdownArchive.Content content = MarkdownArchive.read(archive);
                    ArchiveEntryNamesDialog.show(content, names ->
                            new ArchiveImportRunner().importLorebook(content, fileName, names, imported -> {
                                if (imported != null) {
                                    showImported(imported);
                                } else {
                                    loadLorebooks();
                                }
                            }));
                });
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "importedLorebook", type = Lorebook.class)})
    private void showImported(Lorebook importedLorebook) {
        this.currentLorebook = importedLorebook;
        openEntries.clear();
        if (!pinnedLorebook) {
            loadLorebooks();
        } else {
            updateSelectedLorebook();
        }
        fireStateChanged();
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "shared", type = boolean.class)},
            locals = {@APILocals(name = "unshared", type = List.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void changeShared(boolean shared) {
        try {
            if (!shared) {
                currentLorebook.setShared(false);
                currentLorebook = lorebookService.save(currentLorebook);
                return;
            }
            List<Lorebook> unshared = lorebookService.findUnsharedSubbooks(currentLorebook);
            if (unshared.isEmpty()) {
                shareLorebook(false);
                return;
            }
            lorebookSharedCheckbox.setValue(false);
            ConfirmDialog.show(String.format(loc.getValue(L.MSG_SHARE_SUB_LOREBOOKS), unshared.size()),
                    () -> shareLorebook(true), () -> shareLorebook(false), true);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "withSubbooks", type = boolean.class)},
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void shareLorebook(boolean withSubbooks) {
        try {
            currentLorebook = lorebookService.share(currentLorebook, withSubbooks);
            lorebookSharedCheckbox.setValue(true);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class))
    private void editDescription() {
        if (currentLorebook == null) {
            return;
        }
        DescriptionDialog.show(loc, loc.getValue(L.LABEL_EDIT_DESCRIPTION), currentLorebook.getDescription(), text -> {
            try {
                currentLorebook.setDescription(StringUtils.trimToNull(text));
                currentLorebook = lorebookService.save(currentLorebook);
                showDescription();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "description", type = String.class)})
    private void showDescription() {
        String description = currentLorebook == null ? null : StringUtils.trimToNull(currentLorebook.getDescription());
        lorebookDescriptionLine.setText(description == null ? "" : description);
        lorebookDescriptionLine.setVisible(description != null && lockedBy == null);
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "newLorebook", type = Lorebook.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void createNewLorebook() {
        try {
            Lorebook newLorebook = new Lorebook();
            newLorebook.setName(loc.getValue(L.LABEL_NEW_LOREBOOK));
            newLorebook.setEnabled(true);
            currentLorebook = lorebookService.save(newLorebook);
            openEntries.clear();
            loadLorebooks();
            fireStateChanged();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "source", type = Lorebook.class)})
    private void cloneCurrentLorebook() {
        if (currentLorebook == null) {
            return;
        }
        Lorebook source = currentLorebook;
        CloneNameDialog.show(loc, loc.getValue(L.LABEL_CLONE_LOREBOOK), source.getName(), name -> {
            try {
                currentLorebook = lorebookService.cloneLorebook(source, name);
                openEntries.clear();
                loadLorebooks();
                fireStateChanged();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
    }

    @PublicAPI(from = "1.7.0",
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
                loadLorebooks();
                fireStateChanged();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
    }

    @PublicAPI(from = "1.7.0",
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
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)})
    private void deleteEntry(LorebookEntry entry) {
        ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> {
            try {
                lorebookEntryService.delete(entry, false);
                refreshEntries();
            } catch (Exception ex) {
                UIUtils.internalServerError(loc, ex);
            }
        });
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "entries", type = boolean.class)},
            locals = {@APILocals(name = "dialog", type = MultiTagDialog.class)})
    private void openTagDialog(boolean entries) {
        if (entries && currentLorebook == null) {
            return;
        }
        MultiTagDialog dialog = entries ? new MultiTagDialog(currentLorebook, this::refreshEntries)
                : new MultiTagDialog(MultiTagDialog.Kind.LOREBOOKS, () -> tagMultiComboBox.setForEntity(currentLorebook));
        dialog.create();
        dialog.open();
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = LorebookMassEditDialog.class)})
    private void openMassEditDialog() {
        if (currentLorebook == null) {
            return;
        }
        LorebookMassEditDialog dialog = new LorebookMassEditDialog(currentLorebook, this::refreshEntries);
        dialog.create();
        dialog.open();
    }

    /**
     * The settings of an entry as fields (not while an agent locks the lorebook): the name, the order, the tags, how and
     * where the entry is used and the note. The first three are added to the layout, the others (to go under the content)
     * are returned. See {@link #createEntryDetailLayout}.
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = List.class),
            parameters = {@APIParameter(name = "detailsLayout", type = VerticalLayout.class),
                    @APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "nameField", type = TextField.class),
                    @APILocals(name = "orderField", type = IntegerField.class),
                    @APILocals(name = "tagCombo", type = TagMultiComboBox.class),
                    @APILocals(name = "filteringModeCombo", type = ComboBox.class),
                    @APILocals(name = "filteringField", type = TextField.class),
                    @APILocals(name = "insertionModeCombo", type = ComboBox.class),
                    @APILocals(name = "negativeTagCombo", type = TagMultiComboBox.class),
                    @APILocals(name = "activationPartsField", type = IntegerField.class),
                    @APILocals(name = "activationContextCombo", type = ComboBox.class),
                    @APILocals(name = "fromMessageField", type = IntegerField.class),
                    @APILocals(name = "toMessageField", type = IntegerField.class),
                    @APILocals(name = "conditionField", type = TextArea.class),
                    @APILocals(name = "preventRecursionCheckbox", type = Checkbox.class),
                    @APILocals(name = "recursionOnlyCheckbox", type = Checkbox.class),
                    @APILocals(name = "activationSummariesCheckbox", type = Checkbox.class),
                    @APILocals(name = "commentField", type = TextArea.class),
                    @APILocals(name = "deleteButton", type = Button.class)})
    private List<Component> createEntryFields(VerticalLayout detailsLayout, LorebookEntry entry) {
        TextField nameField = new TextField(loc.getValue(L.LABEL_ENTRY_NAME));
        nameField.setWidthFull();
        nameField.setValue(StringUtils.defaultString(entry.getName()));
        nameField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                entry.setName(e.getValue());
                saveEntry(entry);
            }
        });

        IntegerField orderField = new IntegerField(loc.getValue(L.LABEL_ORDER));
        orderField.setWidthFull();
        orderField.setValue(entry.getOrder());
        orderField.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null) {
                entry.setOrder(e.getValue());
                saveEntry(entry);
            }
        });

        TagMultiComboBox tagCombo = new TagMultiComboBox(loc.getValue(L.LABEL_TAGS));
        tagCombo.setWidthFull();
        tagCombo.setForEntity(entry);

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

        IntegerField activationPartsField = new IntegerField(loc.getValue(L.LABEL_PARTS));
        activationPartsField.setWidthFull();
        activationPartsField.setMin(1);
        activationPartsField.setStepButtonsVisible(true);
        activationPartsField.setValue(entry.getActivationParts());
        activationPartsField.setVisible(entry.getActivationContext() == ActivationContext.INCLUDE_PARTS);
        activationPartsField.setHelperText(loc.getValue(L.HELP_ACTIVATION_PARTS));
        activationPartsField.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null) {
                entry.setActivationParts(e.getValue());
                saveEntry(entry);
            }
        });

        ComboBox<ActivationContext> activationContextCombo = new ComboBox<>(loc.getValue(L.LABEL_ACTIVATION_CONTEXT));
        activationContextCombo.setItems(ActivationContext.values());
        activationContextCombo.setItemLabelGenerator(context -> loc.getValue(loc.getActivationContext(context)));
        activationContextCombo.setAllowCustomValue(false);
        activationContextCombo.setValue(entry.getActivationContext());
        activationContextCombo.setWidthFull();
        activationContextCombo.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null) {
                entry.setActivationContext(e.getValue());
                saveEntry(entry);
                activationPartsField.setVisible(e.getValue() == ActivationContext.INCLUDE_PARTS);
            }
        });

        IntegerField fromMessageField = new IntegerField(loc.getValue(L.LABEL_FROM_MESSAGE));
        fromMessageField.setWidthFull();
        fromMessageField.setMin(1);
        fromMessageField.setStepButtonsVisible(true);
        fromMessageField.setValue(entry.getFromMessage());
        fromMessageField.setHelperText(loc.getValue(L.HELP_ACTIVATION_RANGE));
        fromMessageField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                entry.setFromMessage(e.getValue());
                saveEntry(entry);
            }
        });

        IntegerField toMessageField = new IntegerField(loc.getValue(L.LABEL_TO_MESSAGE));
        toMessageField.setWidthFull();
        toMessageField.setMin(1);
        toMessageField.setStepButtonsVisible(true);
        toMessageField.setValue(entry.getToMessage());
        toMessageField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                entry.setToMessage(e.getValue());
                saveEntry(entry);
            }
        });

        TextArea conditionField = LoreConditionField.create(loc, templateService, entry, () -> saveEntry(entry));

        Checkbox preventRecursionCheckbox = new Checkbox(loc.getValue(L.LABEL_PREVENT_RECURSION), entry.isPreventRecursion());
        preventRecursionCheckbox.setHelperText(loc.getValue(L.HELP_PREVENT_RECURSION));
        preventRecursionCheckbox.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                entry.setPreventRecursion(Boolean.TRUE.equals(e.getValue()));
                saveEntry(entry);
            }
        });

        Checkbox recursionOnlyCheckbox = new Checkbox(loc.getValue(L.LABEL_RECURSION_ONLY), entry.isRecursionOnly());
        recursionOnlyCheckbox.setHelperText(loc.getValue(L.HELP_RECURSION_ONLY));
        recursionOnlyCheckbox.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                entry.setRecursionOnly(Boolean.TRUE.equals(e.getValue()));
                saveEntry(entry);
            }
        });

        Checkbox activationSummariesCheckbox = new Checkbox(loc.getValue(L.LABEL_ACTIVATION_SUMMARIES), entry.isActivationSummaries());
        activationSummariesCheckbox.setHelperText(loc.getValue(L.HELP_ACTIVATION_SUMMARIES));
        activationSummariesCheckbox.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                entry.setActivationSummaries(Boolean.TRUE.equals(e.getValue()));
                saveEntry(entry);
            }
        });

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

        Button deleteButton = new Button(loc.getValue(L.LABEL_DELETE_ENTRY), Solid.TRASH.create(), _ -> deleteEntry(entry));
        deleteButton.setThemeName("error");

        detailsLayout.add(nameField, orderField, tagCombo);
        return List.of(filteringModeCombo, filteringField, insertionModeCombo, negativeTagCombo,
                activationContextCombo, activationPartsField, fromMessageField, toMessageField, conditionField, activationSummariesCheckbox, preventRecursionCheckbox,
                recursionOnlyCheckbox, commentField, deleteButton);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "enabled", type = Checkbox.class),
                    @APILocals(name = "nameSpan", type = Span.class),
                    @APILocals(name = "toggleDetails", type = Button.class),
                    @APILocals(name = "row", type = HorizontalLayout.class)})
    private Component createEntryRow(LorebookEntry entry) {
        Checkbox enabled = new Checkbox(entry.isEnabled());
        enabled.setReadOnly(lockedBy != null);
        enabled.addClassName(CssStyles.FLEX_SHRINK_0);
        enabled.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                entry.setEnabled(Boolean.TRUE.equals(e.getValue()));
                saveEntry(entry);
                updateLorebookTokens();
            }
        });

        Span nameSpan = new Span(StringUtils.defaultString(entry.getName()));
        nameSpan.addClassNames(CssStyles.TEXT_BOLD, CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE, CssStyles.MIN_WIDTH_0);

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
        toggleDetails.addClassName(CssStyles.FLEX_SHRINK_0);

        HorizontalLayout row = new HorizontalLayout(enabled, nameSpan, toggleDetails);
        row.setWidthFull();
        row.setPadding(false);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setFlexGrow(1, nameSpan);
        return row;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "detailsLayout", type = VerticalLayout.class),
                    @APILocals(name = "belowContent", type = List.class),
                    @APILocals(name = "payloadField", type = TextAreaPopoverComponent.class),
                    @APILocals(name = "payloadTokensSpan", type = Span.class)})
    public Component createEntryDetailLayout(LorebookEntry entry) {
        VerticalLayout detailsLayout = new VerticalLayout();
        detailsLayout.setWidthFull();
        detailsLayout.setPadding(false);
        detailsLayout.setSpacing(false);
        detailsLayout.getThemeList().add("spacing-xs");
        detailsLayout.addClassName(CssStyles.SUBTLE_BOX);

        List<Component> belowContent = List.of();
        if (lockedBy == null) {
            belowContent = createEntryFields(detailsLayout, entry);
        } else {
            detailsLayout.add(infoSpan(L.LABEL_ENTRY_NAME, entry.getName()));
            detailsLayout.add(infoSpan(L.LABEL_ORDER, String.valueOf(entry.getOrder())));
            detailsLayout.add(infoSpan(L.LABEL_TAGS, tagsOf(entry, false)));
            detailsLayout.add(infoSpan(L.LABEL_FILTERING_MODE,
                    entry.getFilteringMode() != null ? loc.getValue(loc.getFilteringMode(entry.getFilteringMode())) : null));
            detailsLayout.add(infoSpan(L.LABEL_FILTERING, entry.getFiltering()));
            detailsLayout.add(infoSpan(L.LABEL_INSERTION_MODE,
                    entry.getInsertionMode() != null ? loc.getValue(loc.getInsertionMode(entry.getInsertionMode())) : null));
            detailsLayout.add(infoSpan(L.LABEL_NEGATIVE_TAGS, tagsOf(entry, true)));
            if (entry.getActivationContext() == ActivationContext.INCLUDE_PARTS) {
                detailsLayout.add(infoSpan(L.LABEL_ACTIVATION_CONTEXT,
                        loc.getValue(loc.getActivationContext(entry.getActivationContext())) + " (" + entry.getActivationParts() + ")"));
            }
            if (entry.getFromMessage() != null) {
                detailsLayout.add(infoSpan(L.LABEL_FROM_MESSAGE, String.valueOf(entry.getFromMessage())));
            }
            if (entry.getToMessage() != null) {
                detailsLayout.add(infoSpan(L.LABEL_TO_MESSAGE, String.valueOf(entry.getToMessage())));
            }
            if (entry.hasActivationCondition()) {
                detailsLayout.add(infoSpan(L.LABEL_ACTIVATION_CONDITION, entry.getActivationCondition()));
            }
            if (entry.isPreventRecursion()) {
                detailsLayout.add(infoSpan(L.LABEL_PREVENT_RECURSION, "✓"));
            }
            detailsLayout.add(infoSpan(L.LABEL_NOTE, entry.getComment()));

        }

        TextAreaPopoverComponent payloadField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_CONTENT));
        payloadField.setWidthFull();
        ResizableTextArea.install(loc, payloadField, "160px");
        payloadField.setValue(StringUtils.defaultString(entry.getPayload()));
        payloadField.setReadOnly(lockedBy != null);
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

        detailsLayout.add(payloadField, payloadTokensSpan);
        belowContent.forEach(detailsLayout::add);
        return detailsLayout;
    }

    /**
     * A "label: value" line, empty when there is no value.
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Span.class),
            parameters = {@APIParameter(name = "label", type = L.class),
                    @APIParameter(name = "value", type = String.class)},
            locals = {@APILocals(name = "span", type = Span.class),
                    @APILocals(name = "labelSpan", type = Span.class),
                    @APILocals(name = "valueSpan", type = Span.class)})
    private Span infoSpan(L label, String value) {
        Span span = new Span();
        span.addClassNames(CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE);
        if (StringUtils.isBlank(value)) {
            span.setVisible(false);
            return span;
        }
        Span labelSpan = new Span(loc.getValue(label) + ": ");
        labelSpan.addClassName(CssStyles.TEXT_SEMIBOLD);
        Span valueSpan = new Span(value);
        valueSpan.addClassName(CssStyles.PRE_WRAP);
        span.add(labelSpan, valueSpan);
        return span;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class),
                    @APIParameter(name = "negative", type = boolean.class)},
            locals = {@APILocals(name = "e", type = Exception.class)})
    private String tagsOf(LorebookEntry entry, boolean negative) {
        try {
            return joinTags(tagRelationService.getTagsForObject(entry, negative));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return "";
        }
    }

    private static String joinTags(List<Tag> tags) {
        return tags.stream().map(Tag::getValue).sorted().reduce((a, b) -> a + ", " + b).orElse("");
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
            UIUtils.internalServerError(loc, ex);
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
        loadLorebooks();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "lorebooks", type = List.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void loadLorebooks() {
        if (pinnedLorebook) {
            updateSelectedLorebook();
            return;
        }
        try {
            List<Lorebook> lorebooks = lorebookService.findAllRegularForUser();
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
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Opens the search and replace in the entries of the lorebook, the entries are shown again when something was
     * replaced.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = LorebookSearchReplaceDialog.class)})
    private void openSearchReplaceDialog() {
        if (currentLorebook == null) {
            return;
        }
        LorebookSearchReplaceDialog dialog = new LorebookSearchReplaceDialog(currentLorebook, true);
        dialog.onReplaced(() -> {
            try {
                refresh();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
        dialog.open();
    }

    /**
     * Shows only the entries whose name, content, note or filter has the text of the search box in it (case does not
     * matter); all of them when the box is empty.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class))
    private void applyEntrySearch() {
        entryFilter.setText(entrySearchField.getValue());
        entryFilter.setTag(entryTagFilter.getValue());
        entryFilter.setStatus(entryStatusFilter.getValue());
        if (entryProvider != null) {
            entryProvider.setFilter(this::matchesEntrySearch);
        }
    }

    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = boolean.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)})
    private boolean matchesEntrySearch(LorebookEntry entry) {
        return entryFilter.matches(entry);
    }

    /**
     * Turns the selection of entries on or off: a check box in front of every row and a button that deletes the checked ones.
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class))
    private void toggleSelection() {
        setSelecting(selectEntriesItem.isChecked());
    }

    /**
     * Turns the check boxes in front of the entries on or off.
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "on", type = boolean.class)})
    void setSelecting(boolean on) {
        grid.deselectAll();
        grid.setSelectionMode(on ? Grid.SelectionMode.MULTI : Grid.SelectionMode.NONE);
        // a new selection model has none of the listeners of the old one, and one that selects nothing takes none
        listenToSelection(on);
        updateDeleteSelectedButton();
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "on", type = boolean.class)})
    private void listenToSelection(boolean on) {
        if (selectionListener != null) {
            selectionListener.remove();
            selectionListener = null;
        }
        if (on) {
            selectionListener = grid.addSelectionListener(_ -> updateDeleteSelectedButton());
        }
    }

    /**
     * Shows how many entries are checked on the button that deletes them, there is no button without a selection.
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class))
    private void updateDeleteSelectedButton() {
        int count = grid.getSelectedItems().size();
        deleteSelectedButton.setVisible(count > 0);
        deleteSelectedButton.setText(loc.getValue(L.LABEL_DELETE_SELECTED) + " (" + count + ")");
    }

    /**
     * Deletes the checked entries (to the trash) after the user confirms.
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "selected", type = Set.class)})
    private void deleteSelectedEntries() {
        Set<LorebookEntry> selected = new LinkedHashSet<>(grid.getSelectedItems());
        if (selected.isEmpty()) {
            return;
        }
        ConfirmDialog.show(String.format(loc.getValue(L.MSG_CONFIRM_DELETE_ENTRIES), selected.size()), () -> {
            try {
                for (LorebookEntry entry : selected) {
                    lorebookEntryService.delete(entry, false);
                }
                refreshEntries();
            } catch (Exception ex) {
                UIUtils.internalServerError(loc, ex);
            }
        });
    }

    /**
     * Opens the preview of which entries a turn with some text would use.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = LorebookActivationPreviewDialog.class)})
    private void openActivationPreview() {
        if (currentLorebook == null) {
            return;
        }
        LorebookActivationPreviewDialog dialog = new LorebookActivationPreviewDialog(currentLorebook);
        dialog.create();
        dialog.open();
    }

    /**
     * Reads whether an agent that works on a book that uses the lorebook locks it, and shows it.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "locking", type = AgentRun.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void updateLock() {
        try {
            AgentRun locking = currentLorebook == null ? null : agentRunService.findLockingRun(currentLorebook);
            lockedBy = locking == null ? null : locking.getAgent().getName();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        lockedSpan.setVisible(lockedBy != null);
        lockedSpan.setText(lockedBy == null ? "" : String.format(loc.getValue(L.MSG_AGENT_LOREBOOK_LOCKED), lockedBy));
    }


    /**
     * What can be done depends on the lorebook: nothing that changes it while an agent locks it (it can be exported and
     * refreshed), the menu without a lorebook has only what makes or imports one.
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "hasLorebook", type = boolean.class)})
    private void updateLockedControls(boolean hasLorebook) {
        boolean editable = lockedBy == null;
        lorebookMenuItems.forEach(item -> item.setVisible(editable));
        if (deleteLorebookItem != null) {
            deleteLorebookItem.setVisible(editable);
        }
        exportItem.setEnabled(hasLorebook);
        addEntryButton.setVisible(editable);
        addEntryButton.setEnabled(hasLorebook);
        entriesMenuButton.setVisible(editable);
        entriesMenuButton.setEnabled(hasLorebook);
        settingsDetails.setVisible(editable);
        settingsDetails.setEnabled(hasLorebook);
        entrySearchField.setVisible(editable);
        entryFilterLayout.setVisible(editable);
        if (!editable) {
            deleteSelectedButton.setVisible(false);
        }
        showDescription();
    }

    /**
     * Puts the lorebook into the fields of its settings: the sub lorebooks can be any other lorebook of the user.
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "available", type = List.class),
                    @APILocals(name = "subIds", type = Set.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void fillSettings() {
        if (currentLorebook == null) {
            lorebookNameField.setValue("");
            lorebookSharedCheckbox.setValue(false);
            scanDepthField.setValue(0);
            ownScanDepthField.clear();
            subLorebooksCombo.setValue(Collections.emptySet());
            tagMultiComboBox.setForEntity(null);
            showDescription();
            return;
        }
        lorebookNameField.setValue(StringUtils.defaultString(currentLorebook.getName()));
        lorebookSharedCheckbox.setValue(currentLorebook.isShared());
        scanDepthField.setValue(currentLorebook.getScanDepth());
        ownScanDepthField.setValue(currentLorebook.getOwnScanDepth());
        tagMultiComboBox.setForEntity(currentLorebook);
        try {
            List<Lorebook> available = lorebookService.findAllRegularForUser().stream()
                    .filter(l -> l.getId() != null && !l.getId().equals(currentLorebook.getId()))
                    .sorted(Comparator.comparing(Lorebook::getName))
                    .collect(Collectors.toList());
            subLorebooksCombo.setItems(available);
            Set<Long> subIds = currentLorebook.getSubbooks() == null ? Set.of() : currentLorebook.getSubbooks().stream()
                    .map(BaseEntity::getId).filter(Objects::nonNull).collect(Collectors.toSet());
            subLorebooksCombo.setValue(available.stream().filter(l -> subIds.contains(l.getId())).collect(Collectors.toSet()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        showDescription();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    private void updateSelectedLorebook() {
        boolean hasLorebook = currentLorebook != null;
        updateLock();
        updateLockedControls(hasLorebook);
        lorebookEnabledCheckbox.setEnabled(hasLorebook);
        lorebookEnabledCheckbox.setReadOnly(lockedBy != null);
        refreshButton.setEnabled(hasLorebook);
        searchReplaceButton.setEnabled(hasLorebook);
        searchReplaceButton.setVisible(lockedBy == null);
        previewActivationButton.setEnabled(hasLorebook);

        if (hasLorebook) {
            lorebookEnabledCheckbox.setValue(currentLorebook.isEnabled());
            updateLorebookInfo();
            fillSettings();
            refreshEntries();
        } else {
            fillSettings();
            lorebookEnabledCheckbox.setValue(false);
            lorebookInfoSpan.setText("");
            // the grid keeps its own record of the opened details over setItems: close them, openEntries decides what opens again
            currentEntries.forEach(old -> grid.setDetailsVisible(old, false));
            grid.setItems(new ArrayList<>());
            currentEntries = new ArrayList<>();
            entryTokens.clear();
            updateLorebookTokens();
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "info", type = StringBuilder.class),
                    @APILocals(name = "tags", type = String.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void updateLorebookInfo() {
        StringBuilder info = new StringBuilder();
        try {
            if (currentLorebook.getSubbooks() != null && !currentLorebook.getSubbooks().isEmpty()) {
                info.append(loc.getValue(L.LABEL_SUB_LOREBOOKS)).append(": ")
                        .append(currentLorebook.getSubbooks().stream().map(Lorebook::getName).sorted()
                                .reduce((a, b) -> a + ", " + b).orElse(""));
            }
            if (currentLorebook.getScanDepth() > 0) {
                if (!info.isEmpty()) {
                    info.append("\n");
                }
                info.append(loc.getValue(L.LABEL_SCAN_DEPTH)).append(": ").append(currentLorebook.getScanDepth());
            }
            String tags = joinTags(tagRelationService.getTagsForObject(currentLorebook));
            if (!tags.isEmpty()) {
                if (!info.isEmpty()) {
                    info.append("\n");
                }
                info.append(loc.getValue(L.LABEL_TAGS)).append(": ").append(tags);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        lorebookInfoSpan.setText(info.toString());
        // the settings section has these while the lorebook can be changed
        lorebookInfoSpan.setVisible(!info.isEmpty() && lockedBy != null);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "entries", type = List.class),
                    @APILocals(name = "tagNames", type = List.class),
                    @APILocals(name = "e", type = Exception.class)})
    public void refreshEntries() {
        if (currentLorebook == null || currentLorebook.getId() == null) {
            currentEntries.forEach(old -> grid.setDetailsVisible(old, false));
            grid.setItems(new ArrayList<>());
            currentEntries = new ArrayList<>();
            entryTokens.clear();
            updateLorebookTokens();
            return;
        }
        try {
            List<LorebookEntry> entries = lorebookEntryService.getEntriesForLorebook(currentLorebook.getId());
            currentEntries.forEach(old -> grid.setDetailsVisible(old, false));
            entryFilter.setTags(LorebookEntryFilter.loadTags(tagRelationService, entries));
            List<String> tagNames = LorebookEntryFilter.tagNames(tagRelationService, entries);
            entryTagFilter.setItems(tagNames);
            if (entryTagFilter.getValue() != null && !tagNames.contains(entryTagFilter.getValue())) {
                entryTagFilter.clear();
            }
            grid.deselectAll();
            entryProvider = new ListDataProvider<>(entries);
            entryProvider.setFilter(this::matchesEntrySearch);
            grid.setItems(entryProvider);
            showOpenEntries(entries);
            currentEntries = entries;
            entryTokens.clear();
            updateLorebookTokens();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
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
            UIUtils.internalServerError(loc, e);
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
     * Shows the details of the entries that were opened and still exist and hides those of the others, forgets the
     * entries that are gone.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "entries", type = List.class)},
            locals = {@APILocals(name = "entry", type = LorebookEntry.class)})
    private void showOpenEntries(List<LorebookEntry> entries) {
        openEntries.retainAll(entries.stream().map(LorebookEntry::getUuid).collect(Collectors.toSet()));
        for (LorebookEntry entry : entries) {
            grid.setDetailsVisible(entry, openEntries.contains(entry.getUuid()));
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
            UIUtils.internalServerError(loc, e);
        }
    }

}
