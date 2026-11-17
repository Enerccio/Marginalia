package com.github.enerccio.marginalia.ui;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.AIDialog;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxBase;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The model box of an OpenAI compatible AI in the AI dialog: Refresh lists the models of the server, a typed name joins that list
 * instead of replacing it, and a model the server does not list is kept.
 */
class AIDialogModelListTest extends MarginaliaTestBase {

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

    @SuppressWarnings("unchecked")
    private AIDialog newOpenAiDialog() {
        AIDialog dialog = new AIDialog();
        dialog.create();
        ((TextField) field(dialog, "nameField")).setValue(uniqueName("dialog-ai"));
        ((IntegerField) field(dialog, "maxContextField")).setValue(4096);
        ((IntegerField) field(dialog, "maxResponseField")).setValue(256);
        ((ComboBox<AIType>) field(dialog, "typeCombo")).setValue(AIType.OPEN_AI_COMPATIBLE);
        ((TextField) field(dialog, "urlField")).setValue(llm.baseUrl());
        return dialog;
    }

    private static ComboBox<String> models(AIDialog dialog) {
        return field(dialog, "modelCombo");
    }

    /** What the Refresh button next to the box does. */
    private static void refresh(AIDialog dialog) {
        try {
            Method fetch = AIDialog.class.getDeclaredMethod("fetchModels");
            fetch.setAccessible(true);
            fetch.invoke(dialog);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void aTypedModelJoinsTheListOfTheServer() {
        llm.models("gemma-4-12b-it", "gemma-4-31b-it-MTP-MM");
        AIDialog dialog = newOpenAiDialog();
        refresh(dialog);
        assertThat(models(dialog).getListDataView().getItems().toList()).containsExactly("gemma-4-12b-it", "gemma-4-31b-it-MTP-MM");

        ComponentUtil.fireEvent(models(dialog), new ComboBoxBase.CustomValueSetEvent<>(models(dialog), true, "typed-model"));

        assertThat(models(dialog).getValue()).isEqualTo("typed-model");
        assertThat(models(dialog).getListDataView().getItems().toList())
                .containsExactly("typed-model", "gemma-4-12b-it", "gemma-4-31b-it-MTP-MM");
    }

    @Test
    void aModelTheServerDoesNotListIsKeptFirst() {
        llm.models("gemma-4-31b-it-MTP-MM");
        AIDialog dialog = newOpenAiDialog();
        models(dialog).setItems("gemma-4-31b-it-MM-MTP");
        models(dialog).setValue("gemma-4-31b-it-MM-MTP");

        refresh(dialog);

        assertThat(models(dialog).getValue()).isEqualTo("gemma-4-31b-it-MM-MTP");
        assertThat(models(dialog).getListDataView().getItems().toList()).containsExactly("gemma-4-31b-it-MM-MTP", "gemma-4-31b-it-MTP-MM");
    }
}
