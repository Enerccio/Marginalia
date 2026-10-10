package com.github.enerccio.marginalia.extensions.authorsnote.ui;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.authorsnote.model.AuthorsNoteData;
import com.github.enerccio.marginalia.extensions.authorsnote.service.AuthorsNoteService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Author's note of a book, edited in the story sidebar. Every change is saved to the book right away, the next
 * generation uses it.
 */
@Configurable(preConstruction = true)
public class AuthorsNoteView extends VerticalLayout {

    @Autowired
    private Localization loc;

    private final AuthorsNoteService authorsNoteService;
    private final Manuscript manuscript;
    private final AuthorsNoteData data;

    private TextArea note;

    public AuthorsNoteView(Manuscript manuscript, AuthorsNoteService authorsNoteService) {
        this.manuscript = manuscript;
        this.authorsNoteService = authorsNoteService;
        this.data = loadData();

        setSizeFull();
        buildUI();
    }

    private AuthorsNoteData loadData() {
        try {
            // the story view keeps the book it was opened with, the note may have been saved since
            return authorsNoteService.loadCurrent(manuscript);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return authorsNoteService.getAuthorsNote(manuscript);
        }
    }

    private void buildUI() {
        Checkbox enabled = new Checkbox("Insert into prompt", data.isEnabled());
        enabled.addValueChangeListener(e -> {
            data.setEnabled(e.getValue());
            saveData();
            updateTokenEstimate();
        });

        IntegerField depth = new IntegerField("Insertion depth");
        depth.setHelperText("Messages after the note: 0 = after the instructions, 1 = right before them");
        depth.setMin(0);
        depth.setStepButtonsVisible(true);
        depth.setValue(data.getDepth());
        depth.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue() >= 0) {
                data.setDepth(e.getValue());
                saveData();
            }
        });

        Select<LLMRole> role = new Select<>();
        role.setLabel("Role");
        role.setItems(LLMRole.values());
        role.setItemLabelGenerator(r -> StringUtils.capitalize(r.name().toLowerCase()));
        role.setValue(data.getRole());
        role.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                data.setRole(e.getValue());
                saveData();
            }
        });

        HorizontalLayout placement = new HorizontalLayout(depth, role);
        placement.setWidthFull();
        placement.setAlignItems(FlexComponent.Alignment.BASELINE);
        placement.setFlexGrow(1, depth, role);

        note = new TextArea("Author's Note");
        note.setWidthFull();
        ResizableTextArea.install(loc, note, "160px");
        note.setValue(StringUtils.defaultString(data.getNote()));
        note.setValueChangeMode(ValueChangeMode.LAZY);
        note.setValueChangeTimeout(1000);
        note.addValueChangeListener(e -> {
            data.setNote(e.getValue());
            saveData();
            updateTokenEstimate();
        });
        updateTokenEstimate();

        TextArea privateNote = new TextArea("Author's Note (private)");
        privateNote.setHelperText("For you only, never sent to the model");
        privateNote.setWidthFull();
        ResizableTextArea.install(loc, privateNote, "100px");
        privateNote.setValue(StringUtils.defaultString(data.getPrivateNote()));
        privateNote.setValueChangeMode(ValueChangeMode.LAZY);
        privateNote.setValueChangeTimeout(1000);
        privateNote.addValueChangeListener(e -> {
            data.setPrivateNote(e.getValue());
            saveData();
        });

        add(enabled, placement, note, privateNote);
        setFlexGrow(1, note, privateNote);
    }

    /**
     * Shows the approximate size of the note; it is reserved from the context of every generation, so the story gets
     * that much less room.
     */
    private void updateTokenEstimate() {
        String text = "Inserted into the prompt of every generation";
        try {
            long tokens = authorsNoteService.estimateTokens(manuscript, data.getNote());
            text = data.isEnabled()
                    ? "~" + tokens + " tokens, taken from the room for the story"
                    : "~" + tokens + " tokens, not inserted";
        } catch (Exception ignored) {
            // no estimate (e.g. the book's model is gone) - the note still works
        }
        note.setHelperText(text);
    }

    private void saveData() {
        try {
            authorsNoteService.saveAuthorsNote(manuscript, data);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
