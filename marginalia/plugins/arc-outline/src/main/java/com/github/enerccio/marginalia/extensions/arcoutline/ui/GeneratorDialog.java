package com.github.enerccio.marginalia.extensions.arcoutline.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.structured.PartialJson;
import com.github.enerccio.marginalia.extensions.arcoutline.model.ArcOutlineSettings;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService.ModelSelection;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.*;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * A dialog that asks the model for something in a JSON schema (an arc, the chapters of an arc). On the left the model,
 * the prompt (a template, from the settings of the user), the inputs of the subclass and the button that runs or stops
 * the model; on the right the reasoning of the model and the result, which the subclass shows while the JSON streams in.
 * When the model is done its answer is checked and only a valid one can be saved.
 * <p>
 * The subclass sets up its own fields and then calls {@link #build}.
 */
@Configurable(preConstruction = true)
public abstract class GeneratorDialog extends Dialog {

    @Autowired
    protected Localization loc;

    @Autowired
    private InferenceServices inferenceServices;

    protected final ArcOutlineService service;
    protected final Manuscript manuscript;

    private final String saveLabel;
    private final StringBuilder reasoning = new StringBuilder();
    private final StringBuilder output = new StringBuilder();

    private ModelProtocolPicker modelPicker;
    private TextAreaPopoverComponent promptArea;
    private Button nameToolsButton;
    private Button generateButton;
    private Button exitButton;
    private Button saveButton;
    private Span reasoningHeading;
    private Markdown reasoningMarkdown;
    private ScrollPanel reasoningScroll;
    private Div previewHolder;
    private Span status;

    /** Categories of the name generator the model gets. */
    private List<String> nameCategories;
    private boolean generating;
    private CancellationToken running;

    protected GeneratorDialog(ArcOutlineService service, Manuscript manuscript, String header, String saveLabel) {
        this.service = service;
        this.manuscript = manuscript;
        this.saveLabel = saveLabel;

        setHeaderTitle(header);
        setWidth("90vw");
        setHeight("90vh");
        // closing while the model runs would leave it running without anyone to read it
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                stop();
            }
        });
    }

    // --- what the subclass provides ---

    /** The template the prompt field starts with: the one of the user settings, the default one when there is none. */
    protected abstract String promptTemplate(ArcOutlineSettings settings);

    /** The template "fill default" puts in the prompt field. */
    protected abstract String defaultPromptTemplate();

    /** The variables of the template, listed under the prompt field. */
    protected abstract String variables();

    /** The inputs under the prompt, null for none. */
    protected abstract Component createInputs();

    /** Enables or disables the inputs of the subclass. */
    protected abstract void setInputsEnabled(boolean enabled);

    /**
     * The prompt to send.
     *
     * @param template  the template in the prompt field
     * @param nameTools whether the model gets the name tools (their hint is added to the prompt)
     * @throws Exception when the template is not valid
     */
    protected abstract String buildPrompt(String template, boolean nameTools) throws Exception;

    /** The JSON schema of the answer, for the generation that is about to start. */
    protected abstract JsonNode schema();

    /** Shows the empty result: nothing was generated yet. */
    protected abstract void showEmptyResult();

    /** Shows what has arrived of the answer, an object; called for every chunk of the answer. */
    protected abstract void showPartial(JsonNode partial);

    /**
     * Checks the complete answer and keeps its result for {@link #save}.
     *
     * @return {@code null} when the answer is valid, otherwise what is wrong with it
     */
    protected abstract String accept(String output);

    /** Forgets the result of the last generation (the dialog shows an empty one). */
    protected abstract void clearResult();

    protected abstract boolean hasResult();

    /**
     * Stores the result. The dialog closes afterwards.
     *
     * @param reasoning the reasoning of the model as it came, null when there was none
     * @param answer    the answer of the model as it came
     */
    protected abstract void save(String reasoning, String answer);

    /** The place the subclass shows the result in. */
    protected final Div resultHolder() {
        return previewHolder;
    }

    // --- construction ---

    /**
     * Builds the dialog. Called by the subclass when its own fields are ready.
     */
    protected final void build() {
        ArcOutlineSettings settings = loadSettings();
        nameCategories = settings.getNameCategories();
        String prompt = promptTemplate(settings);

        // less padding than the default of the dialog and tight spacing, the texts get the room
        addThemeName("no-padding");

        // starts with the model of the book, can be changed for this generation
        modelPicker = new ModelProtocolPicker();
        try {
            ModelSelection initial = service.resolveModel(manuscript);
            if (initial != null) {
                modelPicker.setSelection(initial.ai(), initial.protocol());
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }

        promptArea = new TextAreaPopoverComponent("Prompt");
        promptArea.setWidthFull();
        promptArea.setValue(prompt);
        promptArea.setMinHeight("160px");
        promptArea.setMaxHeight("40vh");
        promptArea.setHelperText("Variables: " + variables());
        promptArea.setPopoverContent(TemplateHints.create(loc, promptArea, promptArea.getPopover(), null, defaultPromptTemplate()));

        // only when the Name Generator extension is loaded
        nameToolsButton = new Button("Name tools", Solid.SIGNATURE.create(), _ -> openNameToolsDialog());
        nameToolsButton.setTooltipText("Let the model get names from the Name Generator extension");
        nameToolsButton.setVisible(service.nameCategories() != null);
        refreshNameToolsLabel();

        generateButton = new Button("Generate", Solid.WAND_MAGIC_SPARKLES.create(), _ -> {
            if (generating) {
                stop();
            } else {
                generate();
            }
        });

        HorizontalLayout buttons = new HorizontalLayout(nameToolsButton, generateButton);
        buttons.setWidthFull();
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        VerticalLayout left = new VerticalLayout(modelPicker, promptArea);
        Component inputs = createInputs();
        if (inputs != null) {
            left.add(inputs);
        }
        left.add(buttons);
        left.setPadding(true);
        left.getThemeList().add("spacing-xs");
        left.setSizeFull();
        // the inputs can be many (a text area for each chapter): the left side scrolls as a whole
        left.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_AUTO);

        reasoningHeading = new Span("Reasoning");
        reasoningHeading.addClassName(ArcOutlineStyles.GENERATION_HEADING);
        reasoningHeading.setVisible(false);
        reasoningMarkdown = new Markdown();
        reasoningScroll = new ScrollPanel(reasoningMarkdown);
        reasoningScroll.addClassName(ArcOutlineStyles.REASONING);
        reasoningScroll.setWidthFull();
        reasoningScroll.setVisible(false);
        previewHolder = new Div();
        previewHolder.setWidthFull();
        status = new Span();
        status.addClassName(CssStyles.NOTICE_WARNING);
        status.setWidthFull();
        status.setVisible(false);

        VerticalLayout right = new VerticalLayout(reasoningHeading, reasoningScroll, previewHolder, status);
        right.setPadding(true);
        right.getThemeList().add("spacing-xs");
        right.setSizeFull();
        right.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_AUTO);

        // the two halves can be resized, they start at 50%
        SplitLayout panels = new SplitLayout(left, right);
        panels.setSizeFull();
        panels.setSplitterPosition(50);
        panels.addClassName(CssStyles.MIN_HEIGHT_0);
        add(panels);

        exitButton = new Button("Exit", _ -> close());
        saveButton = new Button(saveLabel, _ -> {
            if (hasResult()) {
                save(StringUtils.defaultIfBlank(reasoning.toString(), null), output.toString());
                close();
            }
        });
        saveButton.setThemeName("primary");
        getFooter().add(exitButton, saveButton);

        showEmptyResult();
        updateControls();
    }

    private ArcOutlineSettings loadSettings() {
        try {
            return service.getSettings();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return new ArcOutlineSettings();
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
                // read again, the settings may have been changed in the meantime
                ArcOutlineSettings settings = service.getSettings();
                nameCategories = checked;
                settings.setNameCategories(nameCategories);
                service.saveSettings(settings);
                refreshNameToolsLabel();
            } catch (Exception ex) {
                UIUtils.internalServerError(loc, ex);
            }
        }).open();
    }

    // --- inference ---

    private void generate() {
        if (StringUtils.isBlank(promptArea.getValue())) {
            Notification.warning("The prompt is empty");
            return;
        }
        if (modelPicker.getAi() == null) {
            Notification.warning("Choose a model");
            return;
        }
        try {
            // the hint about the tools is added only when the model gets them
            FunctionSet nameFunctions = service.nameFunctions(nameCategories);
            String prompt = buildPrompt(promptArea.getValue(), nameFunctions != null);
            JsonNode schema = schema();
            // forget the last generation only when there is a new one to make
            reset();

            InferenceService inference = inferenceServices.forAI(modelPicker.getAi());
            UI ui = UI.getCurrent();
            generating = true;
            updateControls();
            try {
                running = inference.stream(service.toPayload(prompt), modelPicker.getProtocol(), schema, nameFunctions,
                        callback(ui));
            } catch (Exception e) {
                // nothing runs, do not leave the controls frozen
                generating = false;
                running = null;
                updateControls();
                throw e;
            }
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            // a provider that can't take a response schema or call functions
            Notification.warning(e.getMessage());
        } catch (Exception e) {
            // most likely a prompt that is not a valid template
            Notification.error("Can't generate: " + StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
        }
    }

    private InferenceService.InferenceAsyncCallback callback(UI ui) {
        return new InferenceService.InferenceAsyncCallback() {
            @Override
            public void onChunk(InferenceService.InferenceAsyncController controller, InferenceService.ChunkType chunkType, String text) {
                ui.access(() -> {
                    append(chunkType, text);
                    UIPushGuard.push(ui);
                    controller.continueInference();
                });
            }

            @Override
            public void onCompletion() {
                finish(ui, null);
            }

            @Override
            public void onCancel() {
                finish(ui, null);
            }

            @Override
            public void onError(Throwable exception) {
                finish(ui, exception);
            }

            @Override
            public boolean isDead() {
                return running != null && running.isCancelled();
            }
        };
    }

    private void append(InferenceService.ChunkType type, String text) {
        if (type == InferenceService.ChunkType.REASONING) {
            reasoning.append(text);
            reasoningMarkdown.setContent(reasoning.toString());
            reasoningScroll.setVisible(true);
            reasoningHeading.setVisible(true);
        } else {
            output.append(text);
            // what has arrived of the answer; nothing changes until there is something to read
            JsonNode partial = PartialJson.parse(output.toString());
            if (partial != null && partial.isObject()) {
                showPartial(partial);
            }
        }
    }

    private void finish(UI ui, Throwable error) {
        ui.access(() -> {
            generating = false;
            running = null;
            if (error != null) {
                UIUtils.inferenceError(loc, error);
            } else {
                String problem = StringUtils.isBlank(output) ? "The model did not answer." : accept(output.toString());
                if (problem != null) {
                    showStatus(problem);
                }
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

    // --- display ---

    /** Forgets the last generation. */
    private void reset() {
        reasoning.setLength(0);
        output.setLength(0);
        clearResult();
        reasoningMarkdown.setContent("");
        reasoningScroll.setVisible(false);
        reasoningHeading.setVisible(false);
        status.setVisible(false);
    }

    private void showStatus(String message) {
        status.setText(message);
        status.setVisible(true);
    }

    /** Freezes the controls while the model runs; the button that starts it then stops it. */
    private void updateControls() {
        modelPicker.setEnabled(!generating);
        promptArea.setEnabled(!generating);
        setInputsEnabled(!generating);
        nameToolsButton.setEnabled(!generating);
        exitButton.setEnabled(!generating);
        saveButton.setEnabled(!generating && hasResult());

        generateButton.setText(generating ? "Stop" : "Generate");
        generateButton.setIcon((generating ? Solid.STOP : Solid.WAND_MAGIC_SPARKLES).create());
        generateButton.setThemeName(generating ? "error primary" : "primary");
    }

    /** A hint in the place of the result, when there is none yet. */
    protected final Component emptyHint(String text) {
        Div empty = new Div(new Span(text));
        empty.addClassName(ArcOutlineStyles.EMPTY);
        return empty;
    }

    /** The text of a field of a JSON object, null when it is missing or not a text. */
    protected static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && value.isTextual() ? value.asText() : null;
    }
}
