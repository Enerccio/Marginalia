package com.github.enerccio.marginalia.extensions.loregenerator.ui;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import com.github.enerccio.marginalia.domain.service.tool.ToolSelection;
import com.github.enerccio.marginalia.domain.service.tool.ToolSet;
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
import com.github.enerccio.marginalia.ui.widgets.ToolSelectionButton;
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
import java.util.concurrent.Callable;

import static com.github.enerccio.marginalia.extensions.loregenerator.LoreGeneratorLocalization.T;

/**
 * Generates the content of a lore entry: a generation from the lore and a prompt, then a compaction of the result.
 * The compacted text goes to the content of the entry, the generated one to its note.
 */
@Configurable(preConstruction = true)
public class LoreGeneratorDialog extends Dialog {

    /** The whole text is one fenced code block, which the default compaction prompt asks for. */

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
    private ToolSelectionButton toolsButton;
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
    private CancellationToken running;
    private final Callable<Manuscript> book;

    /**
     * @param book the book the lorebook is edited for, null when it is edited on its own (the lorebooks of the workspace): what the
     *             model costs is counted for the book when there is one
     */
    public LoreGeneratorDialog(LoreGeneratorService service, Lorebook lorebook, LorebookEntry entry, Callable<Manuscript> book,
                               Runnable onEntryChanged) {
        this.service = service;
        this.book = book;
        this.lorebook = lorebook;
        this.entry = entry;
        this.onEntryChanged = onEntryChanged;

        setHeaderTitle(T.localized("Generate lore entry: %s").formatted(StringUtils.defaultString(entry.getName())));
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
        buildUI();
        toolsButton.setValue(data.getTools());
        String last = data.getLastGenerationPrompt();
        selectPrompt(last != null && data.getGenerationPrompts().containsKey(last)
                ? last : data.getGenerationPrompts().keySet().iterator().next());
        restoreEntryGeneration();
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
            Span warning = new Span(T.localized("This lore entry already has content. Copying the result to it replaces the content and the note."));
            warning.addClassName(CssStyles.NOTICE_WARNING);
            warning.setWidthFull();
            main.add(warning);
        }

        scopeGroup = new RadioButtonGroup<>();
        scopeGroup.setItems(LoreScope.CURRENT_LOREBOOK, LoreScope.ALL_LORE);
        scopeGroup.setItemLabelGenerator(scope -> scope == LoreScope.ALL_LORE ? T.localized("All Lore") : T.localized("Current Lorebook"));
        scopeGroup.setValue(LoreScope.CURRENT_LOREBOOK);

        limitGroup = new RadioButtonGroup<>();
        limitGroup.setItems(EntryLimit.UP_TO_POSITION, EntryLimit.ALL_ENTRIES);
        limitGroup.setItemLabelGenerator(limit -> limit == EntryLimit.ALL_ENTRIES
                ? T.localized("All entries") : T.localized("Only up to position (%d)").formatted(entry.getOrder()));
        limitGroup.setValue(EntryLimit.UP_TO_POSITION);

        promptCombo = new ComboBox<>();
        promptCombo.setPlaceholder(T.localized("Generation prompt"));
        promptCombo.setAriaLabel(T.localized("Generation prompt"));
        promptCombo.setAllowCustomValue(false);
        promptCombo.setWidth("320px");
        promptCombo.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null) {
                selectPrompt(e.getValue());
                rememberPrompt(e.getValue());
            }
        });
        saveButton = new Button(T.localized("Save"), e -> savePrompt(promptCombo.getValue()));
        saveAsButton = new Button(T.localized("Save as"), e -> openSaveAsDialog());
        deleteButton = new Button(T.localized("Delete"), e -> deletePrompt());
        deleteButton.setThemeName("error");

        // only when a tool suite is installed (names, places...)
        toolsButton = new ToolSelectionButton();
        toolsButton.addValueChangeListener(this::rememberTools);

        // one line: what lore is used, and the prompt
        HorizontalLayout header = new HorizontalLayout(scopeGroup, limitGroup, promptCombo, saveButton, saveAsButton, deleteButton, toolsButton);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);
        header.setFlexGrow(1, promptCombo);

        // the text area keeps its place in the dialog and scrolls its own text, it does not grow with the prompt
        promptArea = new TextArea(T.localized("Prompt"));
        promptArea.setWidthFull();
        promptArea.setMinHeight("120px");

        generationPane = new GenerationPane(T.localized("Run Generation"), this::runGeneration, this::stop);
        compactionPane = new GenerationPane(T.localized("Run Compaction"), this::runCompaction, this::stop);

        tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_HIDDEN);
        generationTab = tabs.add(T.localized("Generation"), generationPane);
        compactionTab = tabs.add(T.localized("Compaction"), compactionPane);

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

        exitButton = new Button(T.localized("Exit"), e -> close());
        copyButton = new Button(T.localized("Copy to Lore Entry"), e -> copyToEntry());
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
            Notification.success(T.localized("Prompt saved"));
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
                ConfirmDialog.show(T.localized("Prompt '%s' already exists, overwrite it?").formatted(newName), () -> savePrompt(newName));
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
        ConfirmDialog.show(T.localized("Delete prompt '%s'?").formatted(name), () -> {
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
        if (saved.tools() != null) {
            toolsButton.setValue(saved.tools());
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
        generation.setTools(toolsButton.getValue());
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

    // --- tools ---

    /** Saves the choice as the one the dialog opens with next time. */
    private void rememberTools(ToolSelection selection) {
        try {
            data = service.getData();
            data.setTools(selection);
            service.saveData(data);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    // --- inference ---

    private void runGeneration() {
        if (StringUtils.isBlank(promptArea.getValue())) {
            Notification.warning(T.localized("The prompt is empty"));
            return;
        }
        // the compaction of an older generation does not belong to the new one
        compactionPane.clear();
        generationPane.clear();
        try {
            // the hint about the tools is added only when the model gets them
            ToolSet tools = service.tools(toolsButton.getValue());
            String prompt = service.buildGenerationPrompt(lorebook, entry, scopeGroup.getValue(), limitGroup.getValue(),
                    promptArea.getValue(), tools);
            start(generationPane, prompt, attachmentPanel.getAttachments(), tools.functions());
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

    private Manuscript book() {
        try {
            return book == null ? null : book.call();
        } catch (Exception e) {
            return null;
        }
    }

    private void start(GenerationPane pane, String prompt, List<LLMAttachment> sent, FunctionSet functions) throws Exception {
        if (modelPicker.getAi() == null) {
            Notification.warning(T.localized("Choose a model"));
            updateControls();
            return;
        }
        InferenceService inference = inferenceServices.forAI(modelPicker.getAi());
        UI ui = UI.getCurrent();

        pane.setRunning(true);
        updateControls();
        try (InferenceSession _ = InferenceSession.open("lore-generator"); UsageContext _ = UsageContext.open(book())) {
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
        toolsButton.setEnabled(!busy);
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
            fresh.setPayload(LoreGeneratorService.unfence(compactionPane.content()));
            fresh.setComment(generationPane.content());
            // the options of this generation, the generator of the entry opens with them
            service.putEntryGeneration(fresh, currentGeneration());
            entry = lorebookEntryService.save(fresh);
            onEntryChanged.run();
            Notification.success(T.localized("Copied to the lore entry"));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

}
