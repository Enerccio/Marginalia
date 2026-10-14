package com.github.enerccio.marginalia.extensions.loregenerator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import com.github.enerccio.marginalia.extensions.loregenerator.model.LoreEntryGeneration;
import com.github.enerccio.marginalia.extensions.loregenerator.model.LoreGeneratorData;
import com.github.enerccio.marginalia.extensions.loregenerator.service.Defaults;
import com.github.enerccio.marginalia.extensions.loregenerator.service.LoreGeneratorService;
import com.github.enerccio.marginalia.extensions.loregenerator.service.LoreGeneratorService.EntryLimit;
import com.github.enerccio.marginalia.extensions.loregenerator.service.LoreGeneratorService.LoreScope;
import com.github.enerccio.marginalia.extensions.loregenerator.service.LoreGeneratorService.ModelSelection;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.ModelProtocolPicker;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates the content of a lore entry: a generation from the lore and a prompt, then a compaction of the result.
 * The compacted text goes to the content of the entry, the generated one to its note.
 */
@Configurable(preConstruction = true)
public class LoreGeneratorDialog extends Dialog {

    /** The whole text is one fenced code block, which the default compaction prompt asks for. */
    private static final Pattern FENCED = Pattern.compile("\\A\\s*```[^\\n]*\\n(.*?)\\n?```\\s*\\z", Pattern.DOTALL);

    @Autowired
    private Localization loc;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    private final LoreGeneratorService service;
    private final Lorebook lorebook;
    private final Runnable onEntryChanged;

    private LorebookEntry entry;

    private ModelProtocolPicker modelPicker;
    private RadioButtonGroup<LoreScope> scopeGroup;
    private RadioButtonGroup<EntryLimit> limitGroup;
    private ComboBox<String> promptCombo;
    private Button saveButton;
    private Button saveAsButton;
    private Button deleteButton;
    private Button nameToolsButton;
    private TextArea promptArea;
    private AttachmentPanel attachmentPanel;
    private TabSheet tabs;
    private Tab generationTab;
    private Tab compactionTab;
    private GenerationPane generationPane;
    private GenerationPane compactionPane;
    private Button exitButton;
    private Button copyButton;

    private LoreGeneratorData data;
    /** Categories of the name generator the model gets: the ones of the entry's last generation, else the user's. */
    private List<String> nameCategories;
    private CancellationToken running;

    public LoreGeneratorDialog(LoreGeneratorService service, Lorebook lorebook, LorebookEntry entry, Runnable onEntryChanged) {
        this.service = service;
        this.lorebook = lorebook;
        this.entry = entry;
        this.onEntryChanged = onEntryChanged;

        setHeaderTitle("Generate lore entry: " + StringUtils.defaultString(entry.getName()));
        setWidth("90vw");
        setHeight("90vh");
        // closing while the inference runs would leave it running without anyone to read it
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                stop();
            }
        });

        loadData();
        nameCategories = data.getNameCategories();
        buildUI();
        String last = data.getLastGenerationPrompt();
        selectPrompt(last != null && data.getGenerationPrompts().containsKey(last)
                ? last : data.getGenerationPrompts().keySet().iterator().next());
        restoreEntryGeneration();
        refreshNameToolsLabel();
        updateControls();
    }

    private void loadData() {
        try {
            data = service.getData();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            data = new LoreGeneratorData();
            data.setGenerationPrompts(new java.util.LinkedHashMap<>());
            data.getGenerationPrompts().put(LoreGeneratorService.DEFAULT_GENERATION_PROMPT, Defaults.GENERATION_PROMPT);
        }
    }

    private void buildUI() {
        // less padding than the default of the dialog and tight spacing, the texts get the room
        addThemeName("no-padding");
        VerticalLayout main = new VerticalLayout();
        main.setSizeFull();
        main.setPadding(true);
        main.getThemeList().add("spacing-xs");

        if (StringUtils.isNotBlank(entry.getPayload())) {
            Span warning = new Span("This lore entry already has content. Copying the result to it replaces the content and the note.");
            warning.addClassName(CssStyles.NOTICE_WARNING);
            warning.setWidthFull();
            main.add(warning);
        }

        scopeGroup = new RadioButtonGroup<>();
        scopeGroup.setItems(LoreScope.CURRENT_LOREBOOK, LoreScope.ALL_LORE);
        scopeGroup.setItemLabelGenerator(scope -> scope == LoreScope.ALL_LORE ? "All Lore" : "Current Lorebook");
        scopeGroup.setValue(LoreScope.CURRENT_LOREBOOK);

        limitGroup = new RadioButtonGroup<>();
        limitGroup.setItems(EntryLimit.UP_TO_POSITION, EntryLimit.ALL_ENTRIES);
        limitGroup.setItemLabelGenerator(limit -> limit == EntryLimit.ALL_ENTRIES
                ? "All entries" : "Only up to position (" + entry.getOrder() + ")");
        limitGroup.setValue(EntryLimit.UP_TO_POSITION);

        promptCombo = new ComboBox<>();
        promptCombo.setPlaceholder("Generation prompt");
        promptCombo.setAriaLabel("Generation prompt");
        promptCombo.setAllowCustomValue(false);
        promptCombo.setWidth("320px");
        promptCombo.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null) {
                selectPrompt(e.getValue());
                rememberPrompt(e.getValue());
            }
        });
        saveButton = new Button("Save", e -> savePrompt(promptCombo.getValue()));
        saveAsButton = new Button("Save as", e -> openSaveAsDialog());
        deleteButton = new Button("Delete", e -> deletePrompt());
        deleteButton.setThemeName("error");

        // only when the Name Generator extension is loaded
        nameToolsButton = new Button("Name tools", Solid.SIGNATURE.create(), e -> openNameToolsDialog());
        nameToolsButton.setTooltipText("Let the model get names from the Name Generator extension");
        nameToolsButton.setVisible(service.nameCategories() != null);
        refreshNameToolsLabel();

        // one line: what lore is used, and the prompt
        HorizontalLayout header = new HorizontalLayout(scopeGroup, limitGroup, promptCombo, saveButton, saveAsButton, deleteButton, nameToolsButton);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);
        header.setFlexGrow(1, promptCombo);

        // the text area keeps its place in the dialog and scrolls its own text, it does not grow with the prompt
        promptArea = new TextArea("Prompt");
        promptArea.setWidthFull();
        promptArea.setMinHeight("120px");

        generationPane = new GenerationPane("Run Generation", this::runGeneration, this::stop);
        compactionPane = new GenerationPane("Run Compaction", this::runCompaction, this::stop);

        tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_HIDDEN);
        generationTab = tabs.add("Generation", generationPane);
        compactionTab = tabs.add("Compaction", compactionPane);

        attachmentPanel = new AttachmentPanel(promptArea);

        VerticalLayout left = new VerticalLayout(promptArea, attachmentPanel);
        left.setPadding(false);
        left.setFlexGrow(1, promptArea);
        // size from the space left, not from the text: a long prompt scrolls inside the text area
        promptArea.setHeight("0");
        left.getThemeList().add("spacing-xs");
        left.setSizeFull();
        left.addClassName(CssStyles.MIN_HEIGHT_0);

        // the two halves can be resized, they start at 50%
        SplitLayout panels = new SplitLayout(left, tabs);
        panels.setSizeFull();
        panels.setSplitterPosition(50);
        panels.addClassName(CssStyles.MIN_HEIGHT_0);

        // starts with the default model of the user, can be changed for this generation
        modelPicker = new ModelProtocolPicker();
        try {
            ModelSelection initial = service.resolveModel();
            if (initial != null) {
                modelPicker.setSelection(initial.ai(), initial.protocol());
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }

        main.add(header, modelPicker, panels);
        main.setFlexGrow(1, panels);
        // only the texts scroll (prompt, reasoning, content), the controls stay where they are
        main.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_HIDDEN);
        add(main);

        exitButton = new Button("Exit", e -> close());
        copyButton = new Button("Copy to Lore Entry", e -> copyToEntry());
        copyButton.setThemeName("primary");
        getFooter().add(exitButton, copyButton);
    }

    // --- prompts ---

    private void selectPrompt(String name) {
        promptCombo.setItems(new ArrayList<>(data.getGenerationPrompts().keySet()));
        promptCombo.setValue(name);
        promptArea.setValue(StringUtils.defaultString(data.getGenerationPrompts().get(name)));
    }

    private void savePrompt(String name) {
        try {
            // read again, the settings may have been changed in the meantime (compaction prompt, other prompts)
            data = service.getData();
            data.getGenerationPrompts().put(name, promptArea.getValue());
            data.setLastGenerationPrompt(name);
            service.saveData(data);
            selectPromptKeepingText(name);
            Notification.success("Prompt saved");
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void selectPromptKeepingText(String name) {
        promptCombo.setItems(new ArrayList<>(data.getGenerationPrompts().keySet()));
        promptCombo.setValue(name);
    }

    private void openSaveAsDialog() {
        new SavePromptAsDialog(newName -> {
            if (data.getGenerationPrompts().containsKey(newName)) {
                ConfirmDialog.show("Prompt '" + newName + "' already exists, overwrite it?", () -> savePrompt(newName));
            } else {
                savePrompt(newName);
            }
        }).open();
    }

    private void deletePrompt() {
        String name = promptCombo.getValue();
        if (name == null) {
            return;
        }
        ConfirmDialog.show("Delete prompt '" + name + "'?", () -> {
            try {
                data = service.getData();
                data.getGenerationPrompts().remove(name);
                // getData adds the default prompt back when the last one is gone
                service.saveData(data);
                data = service.getData();
                String next = data.getGenerationPrompts().keySet().iterator().next();
                selectPrompt(next);
                rememberPrompt(next);
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
    }

    /**
     * The options the last generation copied to this entry was made with take priority over the last used prompt and the
     * saved name categories of the user.
     */
    private void restoreEntryGeneration() {
        LoreEntryGeneration saved = service.getEntryGeneration(entry);
        if (saved == null) {
            return;
        }
        if (saved.getPromptName() != null && data.getGenerationPrompts().containsKey(saved.getPromptName())) {
            promptCombo.setValue(saved.getPromptName());
        }
        if (saved.getPrompt() != null) {
            // what was sent, also when the saved prompt of that name was edited since
            promptArea.setValue(saved.getPrompt());
        }
        scopeGroup.setValue(enumOr(LoreScope.class, saved.getScope(), scopeGroup.getValue()));
        limitGroup.setValue(enumOr(EntryLimit.class, saved.getLimit(), limitGroup.getValue()));
        if (saved.getNameCategories() != null) {
            nameCategories = saved.getNameCategories();
        }
    }

    private static <E extends Enum<E>> E enumOr(Class<E> type, String name, E fallback) {
        try {
            return name == null ? fallback : Enum.valueOf(type, name);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    /** The options of the dialog as they are now, for the attributes of the entry. */
    private LoreEntryGeneration currentGeneration() {
        LoreEntryGeneration generation = new LoreEntryGeneration();
        generation.setPromptName(promptCombo.getValue());
        generation.setPrompt(promptArea.getValue());
        generation.setScope(scopeGroup.getValue().name());
        generation.setLimit(limitGroup.getValue().name());
        // only the categories that were really offered to the model
        List<String> available = service.nameCategories();
        if (nameCategories != null && available != null) {
            generation.setNameCategories(nameCategories.stream().filter(available::contains).toList());
        }
        return generation;
    }

    /** Saves the prompt as the one to select when the dialog opens next time. */
    private void rememberPrompt(String name) {
        try {
            data = service.getData();
            data.setLastGenerationPrompt(name);
            service.saveData(data);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    // --- name tools ---

    private void refreshNameToolsLabel() {
        List<String> available = service.nameCategories();
        int selected = 0;
        if (available != null && nameCategories != null) {
            selected = (int) nameCategories.stream().filter(available::contains).count();
        }
        nameToolsButton.setText(selected > 0 ? "Name tools (" + selected + ")" : "Name tools");
    }

    /** The categories of the name generator to check, with the ones saved before checked. */
    private void openNameToolsDialog() {
        List<String> available = service.nameCategories();
        if (available == null) {
            Notification.warning("The Name Generator extension is not loaded");
            nameToolsButton.setVisible(false);
            return;
        }
        new NameToolsDialog(available, nameCategories, checked -> {
            try {
                data = service.getData();
                nameCategories = checked;
                data.setNameCategories(nameCategories);
                service.saveData(data);
                refreshNameToolsLabel();
            } catch (Exception ex) {
                UIUtils.internalServerError(loc, ex);
            }
        }).open();
    }

    // --- inference ---

    private void runGeneration() {
        if (StringUtils.isBlank(promptArea.getValue())) {
            Notification.warning("The prompt is empty");
            return;
        }
        // the compaction of an older generation does not belong to the new one
        compactionPane.clear();
        generationPane.clear();
        try {
            // the hint about the tools is added only when the model gets them
            FunctionSet nameFunctions = service.nameFunctions(nameCategories);
            String prompt = service.buildGenerationPrompt(lorebook, entry, scopeGroup.getValue(), limitGroup.getValue(),
                    promptArea.getValue(), nameFunctions != null);
            start(generationPane, prompt, attachmentPanel.getAttachments(), nameFunctions);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            updateControls();
        }
    }

    private void runCompaction() {
        compactionPane.clear();
        try {
            String prompt = service.buildCompactionPrompt(lorebook, entry, scopeGroup.getValue(), limitGroup.getValue(), generationPane.content());
            // the compaction works on the generated text only
            start(compactionPane, prompt, List.of(), null);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            updateControls();
        }
    }

    private void start(GenerationPane pane, String prompt, List<LLMAttachment> sent, FunctionSet functions) throws Exception {
        if (modelPicker.getAi() == null) {
            Notification.warning("Choose a model");
            updateControls();
            return;
        }
        InferenceService inference = inferenceServices.forAI(modelPicker.getAi());
        UI ui = UI.getCurrent();

        pane.setRunning(true);
        updateControls();
        try {
            running = inference.stream(service.toPayload(prompt, new ArrayList<>(sent)), modelPicker.getProtocol(), functions, callback(ui, pane));
        } catch (Exception e) {
            // nothing runs, do not leave the controls frozen
            pane.setRunning(false);
            running = null;
            updateControls();
            if (e instanceof IllegalArgumentException || e instanceof UnsupportedOperationException) {
                // an attachment the provider can't take, or a provider that can't call functions
                Notification.warning(e.getMessage());
                return;
            }
            throw e;
        }
    }

    private InferenceService.InferenceAsyncCallback callback(UI ui, GenerationPane pane) {
        return new InferenceService.InferenceAsyncCallback() {
            @Override
            public void onChunk(InferenceService.InferenceAsyncController controller, InferenceService.ChunkType chunkType, String text) {
                ui.access(() -> {
                    pane.append(chunkType, text);
                    UIPushGuard.push(ui);
                    controller.continueInference();
                });
            }

            @Override
            public void onCompletion() {
                finish(ui, pane, null);
            }

            @Override
            public void onCancel() {
                finish(ui, pane, null);
            }

            @Override
            public void onError(Throwable exception) {
                finish(ui, pane, exception);
            }

            @Override
            public boolean isDead() {
                return running != null && running.isCancelled();
            }
        };
    }

    private void finish(UI ui, GenerationPane pane, Throwable error) {
        ui.access(() -> {
            pane.setRunning(false);
            running = null;
            if (error != null) {
                UIUtils.inferenceError(loc, error);
            }
            updateControls();
            UIPushGuard.push(ui);
        });
    }

    private void stop() {
        CancellationToken token = running;
        if (token != null) {
            token.cancel();
        }
    }

    /** Freezes the controls while an inference runs and enables the ones that have something to work with. */
    private void updateControls() {
        boolean busy = generationPane.isRunning() || compactionPane.isRunning();

        modelPicker.setEnabled(!busy);
        scopeGroup.setEnabled(!busy);
        limitGroup.setEnabled(!busy);
        promptCombo.setEnabled(!busy);
        saveButton.setEnabled(!busy);
        saveAsButton.setEnabled(!busy);
        deleteButton.setEnabled(!busy);
        nameToolsButton.setEnabled(!busy);
        promptArea.setEnabled(!busy);
        exitButton.setEnabled(!busy);
        attachmentPanel.setBusy(busy);

        generationTab.setEnabled(!busy || generationPane.isRunning());
        compactionTab.setEnabled(!busy && generationPane.hasContent() || compactionPane.isRunning());
        generationPane.setBusy(busy);
        compactionPane.setBusy(busy);
        copyButton.setEnabled(!busy && compactionPane.hasContent());
        if (!compactionTab.isEnabled() && tabs.getSelectedTab() == compactionTab) {
            tabs.setSelectedTab(generationTab);
        }
    }

    // --- result ---

    private void copyToEntry() {
        try {
            LorebookEntry fresh = lorebookEntryService.find(entry);
            fresh.setPayload(unfence(compactionPane.content()));
            fresh.setComment(generationPane.content());
            // the options of this generation, the generator of the entry opens with them
            service.putEntryGeneration(fresh, currentGeneration());
            entry = lorebookEntryService.save(fresh);
            onEntryChanged.run();
            Notification.success("Copied to the lore entry");
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * The default compaction prompt asks for the entry in a fenced code block to be copyable, the entry should not
     * contain the fence.
     */
    static String unfence(String text) {
        Matcher matcher = FENCED.matcher(text);
        return matcher.matches() ? matcher.group(1) : text.strip();
    }
}
