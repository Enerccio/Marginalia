package com.github.enerccio.marginalia.extensions.arcoutline.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.structured.SchemaCheck;
import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputs;
import com.github.enerccio.marginalia.extensions.arcoutline.model.*;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService.StoryMessage;
import com.github.enerccio.marginalia.extensions.arcoutline.service.Defaults;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Asks the model to turn a part of the written story into an arc with chapters. The part is a range of the messages of
 * the active branch of the book; a counter shows how many tokens the prompt with that range has, so the author sees
 * what is sent. The arc and its chapters are shown on the right as the outline shows them and fill in while the JSON
 * streams in. A valid answer can be saved: the arc goes to the end of the outline, with the reasoning and the whole JSON
 * it came from.
 */
public class StoryImportDialog extends GeneratorDialog {

    private static final int MAX_CHAPTERS = 40;
    /** How much of a message is shown to tell which one it is. */
    private static final int PREVIEW_LENGTH = 90;

    private final ArcOutline outline;
    /** The messages with text of the active branch, as the branch was when the dialog opened. */
    private final List<StoryMessage> messages;
    private final Consumer<Arc> onSave;

    private IntegerField fromField;
    private IntegerField toField;
    private IntegerField chaptersField;
    private Select<OutlineState> stateSelect;
    private Span tokenInfo;
    /** The valid answer of the last generation, null while there is none. */
    private GeneratedStoryArc result;

    /**
     * @param outline  the outline of the book, the model is told about its arcs
     * @param messages the messages of the active branch (see {@link ArcOutlineService#activeBranch}), not empty
     * @param onSave   gets the new arc with its chapters (it is not in the outline yet) when the user saves the answer
     */
    public StoryImportDialog(ArcOutlineService service, Manuscript manuscript, ArcOutline outline,
                             List<StoryMessage> messages, Consumer<Arc> onSave) {
        super(service, manuscript, "Import an arc from the story", "Save as arc");
        this.outline = outline;
        this.messages = messages;
        this.onSave = onSave;
        build();
        updateTokenInfo();
    }

    @Override
    protected String promptTemplate(ArcOutlineSettings settings) {
        return service.importPrompt(settings);
    }

    @Override
    protected String defaultPromptTemplate() {
        return Defaults.IMPORT_PROMPT;
    }

    @Override
    protected String variables() {
        return "{{backgroundLore}}, {{existingArcs}}, {{story}}, {{numberOfChapters}}";
    }

    // the names are in the story
    @Override
    protected boolean supportsNameTools() {
        return false;
    }

    @Override
    protected void promptOrModelChanged() {
        updateTokenInfo();
    }

    // --- inputs ---

    @Override
    protected Component createInputs() {
        int last = messages.size();

        fromField = rangeField("From message", 1, last);
        toField = rangeField("To message", last, last);
        fromField.addValueChangeListener(_ -> rangeChanged());
        toField.addValueChangeListener(_ -> rangeChanged());
        HorizontalLayout range = new HorizontalLayout(fromField, toField);
        range.setWidthFull();
        range.setPadding(false);
        range.getThemeList().add("spacing-xs");

        Span rangeHint = new Span("The messages with text of the active branch, " + last + " in all, are numbered from the first one.");
        rangeHint.addClassNames(CssStyles.TEXT_XS, CssStyles.TEXT_SECONDARY);

        chaptersField = new IntegerField("Number of chapters (optional)");
        chaptersField.setMin(1);
        chaptersField.setMax(MAX_CHAPTERS);
        chaptersField.setStepButtonsVisible(true);
        chaptersField.setHelperText("Empty: the model divides the story where it divides naturally");
        chaptersField.setWidthFull();
        chaptersField.addValueChangeListener(_ -> updateTokenInfo());

        stateSelect = new Select<>();
        stateSelect.setLabel("State of the imported arc and its chapters");
        stateSelect.setItems(OutlineState.values());
        stateSelect.setItemLabelGenerator(OutlineState::getLabel);
        // the story is written already
        stateSelect.setValue(OutlineState.FINISHED);
        stateSelect.setWidthFull();

        tokenInfo = new Span();
        tokenInfo.setWidthFull();
        tokenInfo.addClassNames(CssStyles.TEXT_S);

        VerticalLayout inputs = new VerticalLayout(range, rangeHint, chaptersField, stateSelect, tokenInfo);
        inputs.setPadding(false);
        inputs.getThemeList().add("spacing-xs");
        return inputs;
    }

    private IntegerField rangeField(String label, int value, int last) {
        IntegerField field = new IntegerField(label);
        field.setMin(1);
        field.setMax(last);
        field.setStepButtonsVisible(true);
        field.setValue(value);
        field.setValueChangeMode(ValueChangeMode.LAZY);
        field.setWidthFull();
        field.setHelperText(preview(value));
        return field;
    }

    /** The beginning of the message, in one line. */
    private String preview(Integer number) {
        if (number == null || number < 1 || number > messages.size()) {
            return "No such message";
        }
        String text = StringUtils.normalizeSpace(messages.get(number - 1).text());
        return StringUtils.abbreviate(text, PREVIEW_LENGTH);
    }

    private void rangeChanged() {
        fromField.setHelperText(preview(fromField.getValue()));
        toField.setHelperText(preview(toField.getValue()));
        updateTokenInfo();
    }

    @Override
    protected void setInputsEnabled(boolean enabled) {
        fromField.setEnabled(enabled);
        toField.setEnabled(enabled);
        chaptersField.setEnabled(enabled);
        stateSelect.setEnabled(enabled);
    }

    /**
     * The messages of the chosen range.
     *
     * @throws IllegalArgumentException when the range is not valid
     */
    private List<StoryMessage> selected() {
        int from = fromField.getValue() != null ? fromField.getValue() : 1;
        int to = toField.getValue() != null ? toField.getValue() : messages.size();
        if (from < 1 || to > messages.size()) {
            throw new IllegalArgumentException("The messages are numbered 1 to " + messages.size());
        }
        if (from > to) {
            throw new IllegalArgumentException("The first message is after the last one");
        }
        return messages.subList(from - 1, to);
    }

    /** The exact number of chapters the author asked for, null when the model decides. */
    private Integer exactCount() {
        return chaptersField.getValue();
    }

    // --- the token counter ---

    /**
     * Shows how big the story of the range and the whole prompt are, and how much the model takes, so the author knows
     * what a range costs before the model starts.
     */
    private void updateTokenInfo() {
        // not built yet
        if (tokenInfo == null) {
            return;
        }
        tokenInfo.removeClassName(CssStyles.NOTICE_WARNING);
        List<StoryMessage> range;
        try {
            range = selected();
        } catch (IllegalArgumentException e) {
            tokenInfo.setText(e.getMessage() + ".");
            tokenInfo.addClassName(CssStyles.NOTICE_WARNING);
            return;
        }

        NumberFormat format = NumberFormat.getIntegerInstance();
        long story = range.stream().mapToLong(StoryMessage::tokens).sum();
        StringBuilder text = new StringBuilder("Story: ").append(range.size())
                .append(range.size() == 1 ? " message, about " : " messages, about ")
                .append(format.format(story)).append(" tokens.");
        try {
            long prompt = service.countTokens(buildPrompt(promptValue(), false));
            text.append(" Prompt sent to the model: about ").append(format.format(prompt)).append(" tokens");
            int available = TokenLimits.promptTokens(selectedAi(), selectedProtocol());
            if (available > 0) {
                text.append(" of ").append(format.format(available)).append(" the model takes");
                if (prompt > available) {
                    text.append(" - it does not fit, choose fewer messages");
                    tokenInfo.addClassName(CssStyles.NOTICE_WARNING);
                }
            }
            text.append('.');
        } catch (Exception e) {
            // most likely a prompt that is not a valid template
            text.append(" The prompt can't be built: ").append(StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
            tokenInfo.addClassName(CssStyles.NOTICE_WARNING);
        }
        tokenInfo.setText(text.toString());
    }

    // --- generation ---

    @Override
    protected String buildPrompt(String template, boolean nameTools) throws Exception {
        return service.buildImportPrompt(manuscript, outline, template, selected(), exactCount());
    }

    @Override
    protected JsonNode schema() {
        return service.importSchema(exactCount());
    }

    // --- result ---

    @Override
    protected void showEmptyResult() {
        showPreview(null, null, null, List.of());
    }

    @Override
    protected void showPartial(JsonNode partial) {
        List<String[]> chapters = new ArrayList<>();
        JsonNode list = partial.get("chapters");
        if (list != null && list.isArray()) {
            for (JsonNode chapter : list) {
                if (chapter.isObject()) {
                    chapters.add(new String[] {text(chapter, "name"), text(chapter, "description"), text(chapter, "note")});
                }
            }
        }
        showPreview(text(partial, "name"), text(partial, "description"), text(partial, "note"), chapters);
    }

    /**
     * Shows the arc and its chapters as the outline shows them, with the whole description and note, without buttons.
     */
    private void showPreview(String title, String description, String note, List<String[]> chapters) {
        resultHolder().removeAll();
        if (title == null && description == null && note == null && chapters.isEmpty()) {
            resultHolder().add(emptyHint("The arc the model writes appears here, with its chapters."));
            return;
        }
        Arc arc = new Arc();
        arc.setTitle(StringUtils.defaultString(title, "…"));
        arc.setDescription(StringUtils.defaultString(description));
        arc.setNote(StringUtils.defaultString(note));
        resultHolder().add(new NodeCard(arc, ArcOutlineStyles.ARC, Solid.CALENDAR.create(), null, true));
        for (String[] item : chapters) {
            Chapter chapter = new Chapter();
            chapter.setTitle(StringUtils.defaultString(item[0], "…"));
            chapter.setDescription(StringUtils.defaultString(item[1]));
            chapter.setNote(StringUtils.defaultString(item[2]));
            resultHolder().add(new NodeCard(chapter, ArcOutlineStyles.CHAPTER, Solid.CALENDAR.create(), null, true));
        }
    }

    @Override
    protected String accept(String output) {
        result = null;
        SchemaCheck check = StructuredOutputs.validate(service.importSchema(exactCount()), output);
        if (!check.valid()) {
            return "The answer is not a valid arc with chapters: " + String.join("; ", check.errors());
        }
        try {
            result = StructuredOutputs.parse(output, GeneratedStoryArc.class);
            showPreview(result.name(), result.description(), result.note(),
                    result.chapters().stream().map(c -> new String[] {c.name(), c.description(), c.note()}).toList());
            return null;
        } catch (Exception e) {
            return "The answer can't be read: " + e.getMessage();
        }
    }

    @Override
    protected void clearResult() {
        result = null;
        showEmptyResult();
    }

    @Override
    protected boolean hasResult() {
        return result != null && !result.chapters().isEmpty();
    }

    @Override
    protected void save(String reasoning, String answer) {
        OutlineState state = stateSelect.getValue() != null ? stateSelect.getValue() : OutlineState.FINISHED;

        Arc arc = new Arc();
        arc.setTitle(result.name().trim());
        arc.setDescription(result.description().trim());
        arc.setNote(result.note().trim());
        // the arc and each of its chapters keep what the model wrote for all of them, as it came
        arc.setGenerationReasoning(reasoning);
        arc.setGenerationResult(answer);
        for (GeneratedChapter generated : result.chapters()) {
            Chapter chapter = new Chapter();
            chapter.setTitle(generated.name().trim());
            chapter.setDescription(generated.description().trim());
            chapter.setNote(generated.note().trim());
            chapter.setGenerationReasoning(reasoning);
            chapter.setGenerationResult(answer);
            chapter.setState(state);
            arc.getChapters().add(chapter);
        }
        // after the chapters, a finished arc finishes them
        arc.setState(state);
        onSave.accept(arc);
    }
}
