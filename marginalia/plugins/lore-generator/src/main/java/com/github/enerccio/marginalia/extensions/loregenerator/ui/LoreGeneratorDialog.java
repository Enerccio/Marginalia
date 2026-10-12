package com.github.enerccio.marginalia.extensions.loregenerator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import com.github.enerccio.marginalia.extensions.loregenerator.model.LoreGeneratorData;
import com.github.enerccio.marginalia.extensions.loregenerator.service.Defaults;
import com.github.enerccio.marginalia.extensions.loregenerator.service.LoreGeneratorService;
import com.github.enerccio.marginalia.extensions.loregenerator.service.LoreGeneratorService.EntryLimit;
import com.github.enerccio.marginalia.extensions.loregenerator.service.LoreGeneratorService.LoreScope;
import com.github.enerccio.marginalia.extensions.loregenerator.service.LoreGeneratorService.ModelSelection;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
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

    /**
     * Pasting files (a screenshot, a file copied in the file manager) into the prompt does not put anything in the text
     * area: the files go through the upload, like the ones chosen with its button. Pasted text is left alone.
     * {@code $0} is the text area, {@code $1} the upload.
     */
    private static final String PASTE_FILES_SCRIPT = """
            if ($0.__loreGeneratorPaste) {
              $0.removeEventListener('paste', $0.__loreGeneratorPaste, true);
            }
            $0.__loreGeneratorPaste = (event) => {
              const files = event.clipboardData ? Array.from(event.clipboardData.files) : [];
              if (files.length > 0) {
                event.preventDefault();
                $1.uploadFiles(files);
              }
            };
            $0.addEventListener('paste', $0.__loreGeneratorPaste, true);
            """;

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

    private RadioButtonGroup<LoreScope> scopeGroup;
    private RadioButtonGroup<EntryLimit> limitGroup;
    private ComboBox<String> promptCombo;
    private Button saveButton;
    private Button saveAsButton;
    private Button deleteButton;
    private TextArea promptArea;
    private Upload upload;
    private VerticalLayout attachmentList;
    private TabSheet tabs;
    private Tab generationTab;
    private Tab compactionTab;
    private Pane generationPane;
    private Pane compactionPane;
    private Button exitButton;
    private Button copyButton;

    private LoreGeneratorData data;
    private CancellationToken running;

    /** Sent with the generation, in the order they were added. */
    private final List<LLMAttachment> attachments = new ArrayList<>();

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
        buildUI();
        selectPrompt(data.getGenerationPrompts().keySet().iterator().next());
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
            }
        });
        saveButton = new Button("Save", e -> savePrompt(promptCombo.getValue()));
        saveAsButton = new Button("Save as", e -> openSaveAsDialog());
        deleteButton = new Button("Delete", e -> deletePrompt());
        deleteButton.setThemeName("error");

        // one line: what lore is used, and the prompt
        HorizontalLayout header = new HorizontalLayout(scopeGroup, limitGroup, promptCombo, saveButton, saveAsButton, deleteButton);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);
        header.setFlexGrow(1, promptCombo);

        // the text area keeps its place in the dialog and scrolls its own text, it does not grow with the prompt
        promptArea = new TextArea("Prompt");
        promptArea.setWidthFull();
        promptArea.setMinHeight("120px");

        generationPane = new Pane("Run Generation", this::runGeneration);
        compactionPane = new Pane("Run Compaction", this::runCompaction);

        tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_HIDDEN);
        generationTab = tabs.add("Generation", generationPane);
        compactionTab = tabs.add("Compaction", compactionPane);

        InMemoryUploadHandler uploadHandler = new InMemoryUploadHandler((metadata, bytes) -> {
            attachments.add(LLMAttachment.of(LLMAttachment.Kind.fromMimeType(metadata.contentType()),
                    metadata.contentType(), metadata.fileName(), bytes));
            upload.clearFileList();
            refreshAttachments();
        });
        upload = new Upload(uploadHandler);
        upload.setMaxFileSize(20 * 1024 * 1024);
        upload.setDropAllowed(true);
        // the script needs the elements of the page, which exist once the text area is attached
        promptArea.addAttachListener(e ->
                promptArea.getElement().executeJs(PASTE_FILES_SCRIPT, promptArea.getElement(), upload.getElement()));
        attachmentList = new VerticalLayout();
        attachmentList.setPadding(false);
        attachmentList.setSpacing(false);
        // many attachments scroll in their own box instead of pushing the prompt and the buttons away
        attachmentList.addClassName(CssStyles.OVERFLOW_AUTO);
        attachmentList.setMaxHeight("140px");
        attachmentList.setMinHeight("0");
        Span attachmentsLabel = new Span("Attachments (images and files sent with the generation)");
        attachmentsLabel.addClassName(CssStyles.HINT_TEXT);

        VerticalLayout left = new VerticalLayout(promptArea, attachmentsLabel, upload, attachmentList);
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

        main.add(header, panels);
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
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Save prompt as");
        TextField name = new TextField("Name");
        name.setWidthFull();
        dialog.add(name);
        Button save = new Button("Save", e -> {
            String newName = StringUtils.trimToNull(name.getValue());
            if (newName == null) {
                name.setInvalid(true);
                name.setErrorMessage("Name can't be empty");
                return;
            }
            dialog.close();
            if (data.getGenerationPrompts().containsKey(newName)) {
                ConfirmDialog.show("Prompt '" + newName + "' already exists, overwrite it?", () -> savePrompt(newName));
            } else {
                savePrompt(newName);
            }
        });
        save.setThemeName("primary");
        dialog.getFooter().add(new Button("Cancel", e -> dialog.close()), save);
        dialog.open();
        name.focus();
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
                selectPrompt(data.getGenerationPrompts().keySet().iterator().next());
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
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
            String prompt = service.buildGenerationPrompt(lorebook, entry, scopeGroup.getValue(), limitGroup.getValue(), promptArea.getValue());
            start(generationPane, prompt, attachments);
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
            start(compactionPane, prompt, List.of());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            updateControls();
        }
    }

    private void start(Pane pane, String prompt, List<LLMAttachment> sent) throws Exception {
        ModelSelection model = service.resolveModel();
        if (model == null) {
            Notification.warning("No default model is set, choose one in the settings");
            updateControls();
            return;
        }
        InferenceService inference = inferenceServices.forAI(model.ai());
        UI ui = UI.getCurrent();

        pane.running = true;
        updateControls();
        try {
            running = inference.stream(service.toPayload(prompt, new ArrayList<>(sent)), model.protocol(), callback(ui, pane));
        } catch (Exception e) {
            // nothing runs, do not leave the controls frozen
            pane.running = false;
            running = null;
            updateControls();
            if (e instanceof IllegalArgumentException) {
                // an attachment the provider can't take
                Notification.warning(e.getMessage());
                return;
            }
            throw e;
        }
    }

    private InferenceService.InferenceAsyncCallback callback(UI ui, Pane pane) {
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

    private void finish(UI ui, Pane pane, Throwable error) {
        ui.access(() -> {
            pane.running = false;
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
        boolean busy = generationPane.running || compactionPane.running;

        scopeGroup.setEnabled(!busy);
        limitGroup.setEnabled(!busy);
        promptCombo.setEnabled(!busy);
        saveButton.setEnabled(!busy);
        saveAsButton.setEnabled(!busy);
        deleteButton.setEnabled(!busy);
        promptArea.setEnabled(!busy);
        exitButton.setEnabled(!busy);
        upload.setVisible(!busy);
        refreshAttachments();

        generationTab.setEnabled(!busy || generationPane.running);
        compactionTab.setEnabled(!busy && generationPane.hasContent() || compactionPane.running);
        generationPane.setBusy(busy);
        compactionPane.setBusy(busy);
        copyButton.setEnabled(!busy && compactionPane.hasContent());
        if (!compactionTab.isEnabled() && tabs.getSelectedTab() == compactionTab) {
            tabs.setSelectedTab(generationTab);
        }
    }

    private void refreshAttachments() {
        boolean busy = generationPane != null && (generationPane.running || compactionPane.running);
        attachmentList.removeAll();
        for (LLMAttachment attachment : attachments) {
            Span label = new Span(StringUtils.defaultIfBlank(attachment.getName(), "attachment")
                    + " (" + StringUtils.defaultIfBlank(attachment.getMimeType(), "unknown type")
                    + ", " + (attachment.getData() != null ? attachment.getData().length / 1024 + " kB" : "link") + ")");
            Button delete = new Button(Solid.TRASH.create(), e -> {
                attachments.remove(attachment);
                refreshAttachments();
            });
            delete.setThemeName("error tertiary");
            delete.setTooltipText("Delete attachment");
            // the attachments of a running generation are in the request already
            delete.setEnabled(!busy);
            HorizontalLayout row = new HorizontalLayout(label, delete);
            row.setAlignItems(FlexComponent.Alignment.CENTER);
            attachmentList.add(row);
        }
    }

    // --- result ---

    private void copyToEntry() {
        try {
            LorebookEntry fresh = lorebookEntryService.find(entry);
            fresh.setPayload(unfence(compactionPane.content()));
            fresh.setComment(generationPane.content());
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

    /**
     * A tab: the reasoning, the content and the button that runs or stops the inference.
     */
    private class Pane extends VerticalLayout {

        private final Markdown reasoningMarkdown = new Markdown();
        private final Details reasoningDetails = new Details("Thinking Process");
        private final Markdown contentMarkdown = new Markdown();
        private final Button runButton;
        private final String runLabel;
        private final StringBuilder reasoning = new StringBuilder();
        private final StringBuilder content = new StringBuilder();

        boolean running;

        Pane(String runLabel, Runnable run) {
            this.runLabel = runLabel;
            setSizeFull();
            setPadding(false);

            addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_HIDDEN);

            // the reasoning has a box of its own, a long one does not push the content out of sight
            ScrollPanel reasoningScroll = new ScrollPanel(reasoningMarkdown);
            reasoningScroll.setHeight("25vh");
            reasoningDetails.add(reasoningScroll);
            reasoningDetails.setWidthFull();
            reasoningDetails.setVisible(false);
            contentMarkdown.setWidthFull();

            ScrollPanel scroll = new ScrollPanel(contentMarkdown);
            scroll.setSizeFull();
            scroll.addClassName(CssStyles.MIN_HEIGHT_0);

            runButton = new Button(runLabel, e -> {
                if (this.running) {
                    stop();
                } else {
                    run.run();
                }
            });
            runButton.setThemeName("primary");

            add(reasoningDetails, scroll, runButton);
            setFlexGrow(1, scroll);
            // at the right edge of the dialog, the same place wherever the splitter is
            setAlignSelf(FlexComponent.Alignment.END, runButton);
        }

        String content() {
            return content.toString();
        }

        boolean hasContent() {
            return StringUtils.isNotBlank(content);
        }

        void clear() {
            reasoning.setLength(0);
            content.setLength(0);
            reasoningMarkdown.setContent("");
            contentMarkdown.setContent("");
            reasoningDetails.setVisible(false);
        }

        void append(InferenceService.ChunkType type, String text) {
            if (type == InferenceService.ChunkType.REASONING) {
                reasoning.append(text);
                reasoningMarkdown.setContent(reasoning.toString());
                reasoningDetails.setVisible(true);
            } else {
                content.append(text);
                contentMarkdown.setContent(content.toString());
            }
        }

        /**
         * @param busy an inference runs (in this tab or in the other one): only the running tab can be used, to stop it
         */
        void setBusy(boolean busy) {
            runButton.setText(running ? "Stop" : runLabel);
            runButton.setThemeName(running ? "error primary" : "primary");
            runButton.setEnabled(!busy || running);
        }
    }
}
