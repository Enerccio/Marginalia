package com.github.enerccio.marginalia.ui;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.extensions.ExtensionAIFactory;
import com.github.enerccio.marginalia.extensions.ExtensionAIForm;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.AIDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The AI dialog with the type Extension: the providers of the loaded extensions are offered, the form of the chosen
 * one is shown and applied to the saved AI, and an AI of an extension that is not loaded can still be edited.
 */
class AIDialogExtensionTest extends MarginaliaTestBase {

    /** The UI of the test, held strongly (the current UI is a weak reference). */
    private UI ui;
    private String extensionId;
    private ProbeFactory factory;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        extensionId = uniqueName("ext");
        factory = new ProbeFactory(extensionId);
    }

    @AfterEach
    void tearDown() {
        inferenceServices.unregisterExtensionFactory(factory);
        UI.setCurrent(null);
        ui = null;
    }

    /** A private field of the dialog: the content of the tab sheet is not reachable through the children. */
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

    private static List<Component> typeSettings(AIDialog dialog) {
        FormLayout layout = field(dialog, "dynamicFormLayout");
        return layout.getChildren().toList();
    }

    private static Button saveButton(AIDialog dialog) {
        List<Button> buttons = dialog.getFooter().getElement().getChildren()
                .flatMap(AIDialogExtensionTest::withDescendants)
                .map(e -> e.getComponent().orElse(null)).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> "primary".equals(b.getThemeName())).toList();
        assertThat(buttons).hasSize(1);
        return buttons.get(0);
    }

    private static Stream<com.vaadin.flow.dom.Element> withDescendants(com.vaadin.flow.dom.Element element) {
        return Stream.concat(Stream.of(element), element.getChildren().flatMap(AIDialogExtensionTest::withDescendants));
    }

    private static ComboBox<AIType> typeCombo(AIDialog dialog) {
        return field(dialog, "typeCombo");
    }

    private static ComboBox<ExtensionAIFactory> providerCombo(AIDialog dialog) {
        return field(dialog, "providerCombo");
    }

    private static TextField nameField(AIDialog dialog) {
        return field(dialog, "nameField");
    }

    /** What the dialog needs besides the type to save. */
    private static void fillGeneralSettings(AIDialog dialog, String name) {
        nameField(dialog).setValue(name);
        ((IntegerField) field(dialog, "maxContextField")).setValue(4096);
        ((IntegerField) field(dialog, "maxResponseField")).setValue(256);
    }

    private AIDialog newDialog() {
        AIDialog dialog = new AIDialog();
        dialog.create();
        return dialog;
    }

    private ExtensionAI savedExtensionAI(String name) throws Exception {
        ExtensionAI ai = new ExtensionAI();
        ai.setAiType(AIType.EXTENSION);
        ai.setExtensionId(extensionId);
        ai.setName(name);
        ai.setMaxContext(4096);
        ai.setMaxCompletionTokens(256);
        ai.getSettings().addProperty("model", "stored");
        return (ExtensionAI) aiService.save(ai);
    }

    @Test
    void typeExtensionIsOfferedOnlyWhenSomeExtensionProvidesAnAI() {
        AIDialog without = newDialog();
        assertThat(typeCombo(without).getListDataView().getItems()).doesNotContain(AIType.EXTENSION);

        inferenceServices.registerExtensionFactory(factory);
        AIDialog with = newDialog();
        assertThat(typeCombo(with).getListDataView().getItems()).contains(AIType.EXTENSION);
    }

    @Test
    void theOnlyProviderIsChosenAndItsFormShown() {
        inferenceServices.registerExtensionFactory(factory);
        AIDialog dialog = newDialog();

        typeCombo(dialog).setValue(AIType.EXTENSION);

        assertThat(providerCombo(dialog).getValue()).isSameAs(factory);
        assertThat(factory.forms).hasSize(1);
        assertThat(typeSettings(dialog)).contains(factory.forms.get(0).field);
    }

    @Test
    void savedAiBelongsToTheExtensionAndGetsTheValuesOfTheForm() throws Exception {
        inferenceServices.registerExtensionFactory(factory);
        AIDialog dialog = newDialog();
        String name = uniqueName("dialog-ai");
        fillGeneralSettings(dialog, name);
        typeCombo(dialog).setValue(AIType.EXTENSION);
        factory.forms.get(0).field.setValue("typed model");

        saveButton(dialog).click();

        AI saved = aiService.findAllForUser().stream().filter(a -> name.equals(a.getName())).findFirst().orElseThrow();
        assertThat(saved).isInstanceOf(ExtensionAI.class);
        ExtensionAI ai = (ExtensionAI) saved;
        assertThat(ai.getAiType()).isEqualTo(AIType.EXTENSION);
        assertThat(ai.getExtensionId()).isEqualTo(extensionId);
        assertThat(ai.getSettings().get("model").getAsString()).isEqualTo("typed model");
    }

    @Test
    void invalidFormDoesNotSave() throws Exception {
        inferenceServices.registerExtensionFactory(factory);
        AIDialog dialog = newDialog();
        String name = uniqueName("dialog-ai");
        fillGeneralSettings(dialog, name);
        typeCombo(dialog).setValue(AIType.EXTENSION);
        factory.forms.get(0).valid = false;

        saveButton(dialog).click();

        assertThat(aiService.findAllForUser()).extracting(AI::getName).doesNotContain(name);
    }

    @Test
    void editShowsTheStoredSettingsAndFixesTheProvider() throws Exception {
        inferenceServices.registerExtensionFactory(factory);
        ExtensionAI stored = savedExtensionAI(uniqueName("stored"));

        AIDialog dialog = new AIDialog(stored);
        dialog.create();

        assertThat(providerCombo(dialog).getValue()).isSameAs(factory);
        assertThat(providerCombo(dialog).isReadOnly()).isTrue();
        assertThat(typeCombo(dialog).isReadOnly()).isTrue();
        assertThat(factory.forms.get(0).field.getValue()).isEqualTo("stored");
    }

    @Test
    void testConnectionUsesTheValuesInTheForm() throws Exception {
        inferenceServices.registerExtensionFactory(factory);
        AIDialog dialog = newDialog();
        fillGeneralSettings(dialog, uniqueName("dialog-ai"));
        typeCombo(dialog).setValue(AIType.EXTENSION);
        factory.forms.get(0).field.setValue("unsaved model");

        // the tab sheet disables the content of the tab that isn't shown: the user opens the tab first
        ((FormLayout) field(dialog, "dynamicFormLayout")).setEnabled(true);
        ((Button) field(dialog, "testConnectionButton")).click();

        assertThat(factory.testedModels).containsExactly("unsaved model");
    }

    @Test
    void aiOfAnUnloadedExtensionIsEditedWithoutItsSettings() throws Exception {
        ExtensionAI stored = savedExtensionAI(uniqueName("orphan"));
        // the extension is not registered: it was unloaded

        AIDialog dialog = new AIDialog(stored);
        dialog.create();

        List<Span> notices = typeSettings(dialog).stream().filter(Span.class::isInstance).map(Span.class::cast)
                .filter(span -> span.hasClassName(CssStyles.NOTICE_WARNING)).toList();
        assertThat(notices).hasSize(1);
        assertThat(notices.get(0).getText()).contains(extensionId);
        assertThat(factory.forms).isEmpty();

        nameField(dialog).setValue("Renamed orphan");
        saveButton(dialog).click();

        ExtensionAI loaded = (ExtensionAI) aiService.find(stored);
        assertThat(loaded.getName()).isEqualTo("Renamed orphan");
        assertThat(loaded.getExtensionId()).isEqualTo(extensionId);
        assertThat(loaded.getSettings().get("model").getAsString()).isEqualTo("stored");
    }

    private static class ProbeForm implements ExtensionAIForm {
        final TextField field = new TextField("Model");
        boolean valid = true;

        ProbeForm(ExtensionAI ai) {
            field.setValue(ai.getSettings().has("model") ? ai.getSettings().get("model").getAsString() : "");
        }

        @Override
        public Component getComponent() {
            return field;
        }

        @Override
        public boolean validate() {
            return valid;
        }

        @Override
        public void apply(ExtensionAI ai) {
            ai.getSettings().addProperty("model", field.getValue());
        }
    }

    private static class ProbeFactory implements ExtensionAIFactory {
        private final String id;
        final List<ProbeForm> forms = new ArrayList<>();
        final List<String> testedModels = new ArrayList<>();
        final AtomicInteger created = new AtomicInteger();

        ProbeFactory(String id) {
            this.id = id;
        }

        @Override
        public String getExtensionId() {
            return id;
        }

        @Override
        public String getName() {
            return "Probe provider";
        }

        @Override
        public InferenceService createInferenceService(ExtensionAI ai) {
            created.incrementAndGet();
            String model = ai.getSettings().get("model").getAsString();
            return new InferenceService() {
                @Override
                public List<String> getModels() {
                    return List.of();
                }

                @Override
                public List<String> testConnection() {
                    testedModels.add(model);
                    return List.of("a", "b");
                }

                @Override
                public long countTokens(String text) {
                    return 0;
                }

                @Override
                public long countTokensApprox(String text) {
                    return 0;
                }

                @Override
                public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, InferenceAsyncCallback callback) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public ExtensionAIForm createForm(ExtensionAI ai) {
            ProbeForm form = new ProbeForm(ai);
            forms.add(form);
            return form;
        }
    }
}
