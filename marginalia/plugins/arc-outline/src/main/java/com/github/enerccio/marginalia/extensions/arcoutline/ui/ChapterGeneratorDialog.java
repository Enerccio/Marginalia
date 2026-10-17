package com.github.enerccio.marginalia.extensions.arcoutline.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.structured.SchemaCheck;
import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputs;
import com.github.enerccio.marginalia.extensions.arcoutline.model.*;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService;
import com.github.enerccio.marginalia.extensions.arcoutline.service.Defaults;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Asks the model for the chapters of an arc. The arc is shown on the right as the outline shows it, with its chapters,
 * and the chapters the model writes are added to it as they stream in. A valid answer can be saved: the chapters go to
 * the end of the arc, each with the reasoning and the whole JSON they came from.
 * <p>
 * The number of chapters is given in words, or as an exact number; an exact number also gives a text area for each
 * chapter, to say what it should contain.
 */
public class ChapterGeneratorDialog extends GeneratorDialog {

    private static final int MAX_CHAPTERS = 40;

    private final ArcOutline outline;
    private final Arc arc;
    private final Consumer<List<Chapter>> onSave;

    private RadioButtonGroup<ChapterContext> contextGroup;
    private TextArea generalSpecification;
    private TextField countText;
    private IntegerField countNumber;
    private VerticalLayout chapterAreas;
    private final List<TextArea> chapterSpecifications = new ArrayList<>();
    /** The valid answer of the last generation, null while there is none. */
    private List<GeneratedChapter> result;

    /**
     * @param outline the outline of the book, the model is told about the arcs up to {@code arc}
     * @param arc     the arc to write the chapters for
     * @param onSave  gets the new chapters (they are not in the arc yet) when the user saves the answer
     */
    public ChapterGeneratorDialog(ArcOutlineService service, Manuscript manuscript, ArcOutline outline, Arc arc,
                                  Consumer<List<Chapter>> onSave) {
        super(service, manuscript, "Generate chapters: " + arc.getTitle(), "Save as chapters");
        this.outline = outline;
        this.arc = arc;
        this.onSave = onSave;
        build();
    }

    @Override
    protected String promptTemplate(ArcOutlineSettings settings) {
        return service.chapterGenerationPrompt(settings);
    }

    @Override
    protected String defaultPromptTemplate() {
        return Defaults.CHAPTER_GENERATION_PROMPT;
    }

    @Override
    protected String variables() {
        return "{{backgroundLore}}, {{outline}}, {{numberOfChapters}}, {{generalSpecification}}, {{chapterSpecification}}";
    }

    // --- inputs ---

    @Override
    protected Component createInputs() {
        contextGroup = new RadioButtonGroup<>("What the model knows of the story");
        contextGroup.setItems(ChapterContext.values());
        contextGroup.setItemLabelGenerator(c -> c.getLabel() + " - " + c.getDescription());
        contextGroup.setValue(ChapterContext.EVERYTHING);
        contextGroup.addThemeName("vertical");

        generalSpecification = new TextArea("What you want from the chapters in general");
        generalSpecification.setWidthFull();
        DialogFocus.onOpen(this, generalSpecification);
        generalSpecification.setMinHeight("100px");
        generalSpecification.setMaxHeight("25vh");
        generalSpecification.setPlaceholder("The pace, the tone, what must happen, what must not...");

        countText = new TextField("Number of chapters");
        countText.setWidthFull();
        countText.setPlaceholder("In words, like: about eight, as many as the arc needs");
        countText.setValueChangeMode(ValueChangeMode.EAGER);

        countNumber = new IntegerField("or an exact number");
        countNumber.setMin(1);
        countNumber.setMax(MAX_CHAPTERS);
        countNumber.setStepButtonsVisible(true);
        countNumber.setHelperText("Gives a field for each chapter");
        countNumber.setWidth("14rem");

        chapterAreas = new VerticalLayout();
        chapterAreas.setPadding(false);
        chapterAreas.setSpacing(false);
        chapterAreas.getThemeList().add("spacing-xs");

        // the two ways to say the number exclude each other
        countText.addValueChangeListener(e -> {
            if (e.isFromClient() && StringUtils.isNotBlank(e.getValue())) {
                countNumber.clear();
            }
        });
        countNumber.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null) {
                countText.clear();
            }
            refreshChapterAreas();
        });

        VerticalLayout inputs = new VerticalLayout(contextGroup, generalSpecification, countText, countNumber, chapterAreas);
        inputs.setPadding(false);
        inputs.getThemeList().add("spacing-xs");
        return inputs;
    }

    /**
     * One text area per chapter for the chosen number; the text of the ones that stay is kept.
     */
    private void refreshChapterAreas() {
        Integer count = countNumber.getValue();
        int wanted = count == null ? 0 : Math.clamp(count, 0, MAX_CHAPTERS);
        while (chapterSpecifications.size() > wanted) {
            TextArea removed = chapterSpecifications.removeLast();
            chapterAreas.remove(removed);
        }
        while (chapterSpecifications.size() < wanted) {
            TextArea area = new TextArea("Chapter " + (chapterSpecifications.size() + 1) + " (optional)");
            area.setWidthFull();
            area.setMinHeight("80px");
            area.setMaxHeight("20vh");
            chapterSpecifications.add(area);
            chapterAreas.add(area);
        }
    }

    @Override
    protected void setInputsEnabled(boolean enabled) {
        contextGroup.setEnabled(enabled);
        generalSpecification.setEnabled(enabled);
        countText.setEnabled(enabled);
        countNumber.setEnabled(enabled);
        chapterSpecifications.forEach(area -> area.setEnabled(enabled));
    }

    /** The exact number of chapters the author asked for, null when the model decides. */
    private Integer exactCount() {
        return countNumber.getValue();
    }

    /** The number of chapters as the prompt says it. */
    private String numberOfChapters() {
        if (exactCount() != null) {
            return "exactly " + exactCount();
        }
        return StringUtils.defaultIfBlank(countText.getValue(), "as many as the arc needs").strip();
    }

    /** What the author wants from each chapter, as a numbered list. */
    private String chapterSpecification() {
        boolean any = chapterSpecifications.stream().anyMatch(area -> StringUtils.isNotBlank(area.getValue()));
        if (!any) {
            return "(nothing in particular, decide yourself)";
        }
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < chapterSpecifications.size(); i++) {
            String specification = chapterSpecifications.get(i).getValue();
            text.append("Chapter ").append(i + 1).append(": ")
                    .append(StringUtils.isBlank(specification) ? "(not specified, decide yourself)" : specification.strip())
                    .append('\n');
        }
        return text.toString().stripTrailing();
    }

    // --- generation ---

    @Override
    protected String buildPrompt(String template, FunctionSet nameFunctions) throws Exception {
        return service.buildChapterPrompt(manuscript, outline, arc, contextGroup.getValue(), template, numberOfChapters(),
                generalSpecification.getValue(), chapterSpecification(), nameFunctions);
    }

    @Override
    protected JsonNode schema() {
        return service.chaptersSchema(exactCount());
    }

    // --- result ---

    @Override
    protected void showEmptyResult() {
        showPreview(List.of());
    }

    @Override
    protected void showPartial(JsonNode partial) {
        JsonNode chapters = partial.get("chapters");
        if (chapters == null || !chapters.isArray()) {
            return;
        }
        List<String[]> shown = new ArrayList<>();
        for (JsonNode chapter : chapters) {
            if (chapter.isObject()) {
                shown.add(new String[] {text(chapter, "name"), text(chapter, "description"), text(chapter, "note")});
            }
        }
        showPreview(shown);
    }

    /**
     * Shows the arc with the chapters it has, then the new ones (whole descriptions), as the outline shows them.
     */
    private void showPreview(List<String[]> generated) {
        resultHolder().removeAll();
        resultHolder().add(new NodeCard(arc, ArcOutlineStyles.ARC, Solid.BOOK.create(), null, false));
        for (Chapter chapter : arc.getChapters()) {
            resultHolder().add(new NodeCard(chapter, ArcOutlineStyles.CHAPTER, null, null, false));
        }
        if (generated.isEmpty()) {
            resultHolder().add(emptyHint("The chapters the model writes appear here, after the ones the arc has."));
            return;
        }
        for (String[] item : generated) {
            Chapter preview = new Chapter();
            preview.setTitle(StringUtils.defaultString(item[0], "…"));
            preview.setDescription(StringUtils.defaultString(item[1]));
            preview.setNote(StringUtils.defaultString(item[2]));
            preview.setState(OutlineState.PLANNED);
            resultHolder().add(new NodeCard(preview, ArcOutlineStyles.CHAPTER, Solid.CALENDAR.create(), null, true));
        }
    }

    private void showPreviewOf(List<GeneratedChapter> chapters) {
        showPreview(chapters.stream().map(c -> new String[] {c.name(), c.description(), c.note()}).toList());
    }

    @Override
    protected String accept(String output) {
        result = null;
        SchemaCheck check = StructuredOutputs.validate(service.chaptersSchema(exactCount()), output);
        if (!check.valid()) {
            return "The answer is not a valid list of chapters: " + String.join("; ", check.errors());
        }
        try {
            result = StructuredOutputs.parse(output, GeneratedChapters.class).chapters();
            showPreviewOf(result);
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
        return result != null && !result.isEmpty();
    }

    @Override
    protected void save(String reasoning, String answer) {
        List<Chapter> chapters = new ArrayList<>();
        for (GeneratedChapter generated : result) {
            Chapter chapter = new Chapter();
            chapter.setTitle(generated.name().trim());
            chapter.setDescription(generated.description().trim());
            chapter.setNote(generated.note().trim());
            // every chapter keeps what the model wrote for all of them, as it came
            chapter.setGenerationReasoning(reasoning);
            chapter.setGenerationResult(answer);
            chapters.add(chapter);
        }
        onSave.accept(chapters);
    }
}
