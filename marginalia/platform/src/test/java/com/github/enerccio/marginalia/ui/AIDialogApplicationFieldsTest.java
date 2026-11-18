package com.github.enerccio.marginalia.ui;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.test.ComponentTree;
import com.github.enerccio.marginalia.test.PlatformTestBase;
import com.github.enerccio.marginalia.ui.dialogs.AIDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An application adds its own fields to the AI dialog by a subclass: they are shown in the dialog, filled from the
 * edited AI, and copied onto the AI before the one save of the dialog, here into the AI's attributes.
 */
class AIDialogApplicationFieldsTest extends PlatformTestBase {

    /** The UI of the test, held strongly (the current UI is a weak reference). */
    private UI ui;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
        ui = null;
    }

    /** The dialog of an application that keeps a note on every AI. */
    private static class NoteDialog extends AIDialog {
        final TextField note = new TextField("Note");
        int applied;

        NoteDialog(AI ai) {
            super(ai);
        }

        @Override
        protected List<Component> createApplicationFields(AI ai) {
            if (ai != null && ai.getAttributes().has("note")) {
                note.setValue(ai.getAttributes().get("note").getAsString());
            }
            return List.of(note);
        }

        @Override
        protected void applyApplicationFields(AI ai) {
            applied++;
            ai.getAttributes().addProperty("note", note.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T field(AIDialog dialog, String name) {
        try {
            Field field = AIDialog.class.getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(dialog);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static Button saveButton(AIDialog dialog) {
        return ComponentTree.all(dialog, Button.class).filter(b -> "primary".equals(b.getThemeName())).findFirst()
                .orElseThrow();
    }

    @Test
    @SuppressWarnings("unchecked")
    void newAiGetsTheApplicationFields() throws Exception {
        NoteDialog dialog = new NoteDialog(null);
        dialog.create();
        assertThat(ComponentTree.all(dialog)).contains(dialog.note);

        String name = uniqueName("dialog-ai");
        ((TextField) field(dialog, "nameField")).setValue(name);
        ((IntegerField) field(dialog, "maxContextField")).setValue(4096);
        ((IntegerField) field(dialog, "maxResponseField")).setValue(256);
        ((ComboBox<AIType>) field(dialog, "typeCombo")).setValue(AIType.OPEN_AI_COMPATIBLE);
        ((TextField) field(dialog, "urlField")).setValue(llm.baseUrl());
        ComboBox<String> models = field(dialog, "modelCombo");
        models.setItems(List.of("m"));
        models.setValue("m");
        dialog.note.setValue("for chat");
        saveButton(dialog).click();

        AI saved = aiService.findAllForUser().stream().filter(a -> name.equals(a.getName())).findFirst().orElseThrow();
        assertThat(saved.getAttributes().get("note").getAsString()).isEqualTo("for chat");
        assertThat(dialog.applied).isEqualTo(1);
    }

    @Test
    void editedAiFillsAndKeepsTheApplicationFields() throws Exception {
        AI ai = createAI();
        ai.getAttributes().addProperty("note", "before");
        ai = aiService.save(ai);

        NoteDialog dialog = new NoteDialog(aiService.findForUser(ai.getUuid()));
        dialog.create();
        assertThat(dialog.note.getValue()).isEqualTo("before");

        dialog.note.setValue("after");
        saveButton(dialog).click();

        assertThat(aiService.findForUser(ai.getUuid()).getAttributes().get("note").getAsString()).isEqualTo("after");
    }

    @Test
    void imageFlagsAreKeptBySave() throws Exception {
        AI ai = createAI();
        ai.setMultimodal(true);
        ai.setImagesInSystem(true);
        ai = aiService.save(ai);

        AI stored = aiService.findForUser(ai.getUuid());
        assertThat(stored.isMultimodal()).isTrue();
        assertThat(stored.isImagesInSystem()).isTrue();
    }
}
