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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

/**
 * Asks the model for a new arc. The arc it answers with is shown as an arc of the outline while it streams in; a valid
 * answer can be saved as an arc at the end of the outline, together with the reasoning and the JSON it came from.
 */
public class ArcGeneratorDialog extends GeneratorDialog {

    private final ArcOutline outline;
    private final Consumer<Arc> onSave;

    private TextArea specificationArea;
    /** The valid answer of the last generation, null while there is none. */
    private GeneratedArc result;

    /**
     * @param outline the outline of the book, the model is told about its arcs
     * @param onSave  gets the new arc (it is not in the outline yet) when the user saves the answer
     */
    public ArcGeneratorDialog(ArcOutlineService service, Manuscript manuscript, ArcOutline outline, Consumer<Arc> onSave) {
        super(service, manuscript, "Generate an arc", "Save as arc");
        this.outline = outline;
        this.onSave = onSave;
        build();
    }

    @Override
    protected String promptTemplate(ArcOutlineSettings settings) {
        return service.generationPrompt(settings);
    }

    @Override
    protected String defaultPromptTemplate() {
        return Defaults.GENERATION_PROMPT;
    }

    @Override
    protected String variables() {
        return "{{backgroundLore}}, {{existingArcs}}, {{specification}}";
    }

    @Override
    protected Component createInputs() {
        specificationArea = new TextArea("Specification");
        specificationArea.setWidthFull();
        specificationArea.setMinHeight("140px");
        specificationArea.setMaxHeight("30vh");
        specificationArea.setPlaceholder("What you want from the arc: its theme, the characters, where it should lead...");
        return specificationArea;
    }

    @Override
    protected void setInputsEnabled(boolean enabled) {
        specificationArea.setEnabled(enabled);
    }

    @Override
    protected String buildPrompt(String template, FunctionSet nameFunctions) throws Exception {
        return service.buildPrompt(manuscript, outline, template, specificationArea.getValue(), nameFunctions);
    }

    @Override
    protected JsonNode schema() {
        return service.schema();
    }

    @Override
    protected void showEmptyResult() {
        resultHolder().removeAll();
        resultHolder().add(emptyHint("The arc the model writes appears here."));
    }

    @Override
    protected void showPartial(JsonNode partial) {
        showPreview(text(partial, "name"), text(partial, "description"), text(partial, "note"));
    }

    /**
     * Shows the arc as the outline shows it, without its chapters and buttons.
     */
    private void showPreview(String title, String description, String note) {
        Arc preview = new Arc();
        preview.setTitle(StringUtils.defaultString(title, "…"));
        preview.setDescription(StringUtils.defaultString(description));
        preview.setNote(StringUtils.defaultString(note));
        preview.setState(OutlineState.PLANNED);
        resultHolder().removeAll();
        resultHolder().add(new NodeCard(preview, ArcOutlineStyles.ARC, Solid.CALENDAR.create(), null, true));
    }

    @Override
    protected String accept(String output) {
        result = null;
        SchemaCheck check = StructuredOutputs.validate(service.schema(), output);
        if (!check.valid()) {
            return "The answer is not a valid arc: " + String.join("; ", check.errors());
        }
        try {
            result = StructuredOutputs.parse(output, GeneratedArc.class);
            showPreview(result.name(), result.description(), result.note());
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
        return result != null;
    }

    @Override
    protected void save(String reasoning, String answer) {
        Arc arc = new Arc();
        arc.setTitle(result.name().trim());
        arc.setDescription(result.description().trim());
        arc.setNote(result.note().trim());
        // what the model wrote, as it came
        arc.setGenerationReasoning(reasoning);
        arc.setGenerationResult(answer);
        onSave.accept(arc);
    }
}
