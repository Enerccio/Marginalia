package com.github.enerccio.marginalia.ui;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.components.TokenizerChoice;
import com.github.enerccio.marginalia.domain.components.TokenizerKey;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.service.ExtensionTokenizer;
import com.github.enerccio.marginalia.domain.service.TokenizerService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.AIDialog;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The tokenizer of an OpenAI compatible AI in the AI dialog: chosen from the built-in ones and those of plugins, preselected for
 * the endpoint of a new AI, kept when the plugin is not loaded.
 */
class AIDialogTokenizerTest extends MarginaliaTestBase {

    @Autowired
    private TokenizerService tokenizerService;

    /** The UI of the test, held strongly (the current UI is a weak reference). */
    private UI ui;
    private String id;
    private ExtensionTokenizer tokenizer;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        id = uniqueName("tok");
        tokenizer = new ExtensionTokenizer() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getName() {
                return "Dialog tokenizer";
            }

            @Override
            public boolean accepts(TokenizerKey key) {
                return key.id().startsWith("http://special:");
            }

            @Override
            public long countTokens(TokenizerKey key, String text) {
                return 5;
            }
        };
        tokenizerService.registerTokenizer(tokenizer);
    }

    @AfterEach
    void tearDown() {
        tokenizerService.unregisterTokenizer(tokenizer);
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

    private static ComboBox<TokenizerChoice> tokenizerCombo(AIDialog dialog) {
        return field(dialog, "tokenizerCombo");
    }

    private static Button saveButton(AIDialog dialog) {
        List<Button> buttons = dialog.getFooter().getElement().getChildren()
                .flatMap(AIDialogTokenizerTest::withDescendants)
                .map(e -> e.getComponent().orElse(null)).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> "primary".equals(b.getThemeName())).toList();
        assertThat(buttons).hasSize(1);
        return buttons.get(0);
    }

    private static Stream<com.vaadin.flow.dom.Element> withDescendants(com.vaadin.flow.dom.Element element) {
        return Stream.concat(Stream.of(element), element.getChildren().flatMap(AIDialogTokenizerTest::withDescendants));
    }

    private AIDialog newOpenAiDialog(String url, String model) {
        AIDialog dialog = new AIDialog();
        dialog.create();
        ((TextField) field(dialog, "nameField")).setValue(uniqueName("dialog-ai"));
        ((IntegerField) field(dialog, "maxContextField")).setValue(4096);
        ((IntegerField) field(dialog, "maxResponseField")).setValue(256);
        ((ComboBox<AIType>) field(dialog, "typeCombo")).setValue(AIType.OPEN_AI_COMPATIBLE);
        ((TextField) field(dialog, "urlField")).setValue(url);
        ComboBox<String> models = field(dialog, "modelCombo");
        models.setItems(List.of(model));
        models.setValue(model);
        return dialog;
    }

    private OpenAICompatible saved(AIDialog dialog) throws Exception {
        String name = ((TextField) field(dialog, "nameField")).getValue();
        saveButton(dialog).click();
        AI saved = aiService.findAllForUser().stream().filter(a -> name.equals(a.getName())).findFirst().orElseThrow();
        return (OpenAICompatible) saved;
    }

    @Test
    void offersAutomaticTheBuiltInAndThePluginTokenizers() {
        AIDialog dialog = newOpenAiDialog("http://llm:8080/v1", "m");

        List<TokenizerChoice> items = tokenizerCombo(dialog).getListDataView().getItems().toList();

        assertThat(items.get(0).id()).isEmpty();
        assertThat(items).extracting(TokenizerChoice::id).contains("LLAMA_CPP_NATIVE", "ext:" + id);
        assertThat(tokenizerCombo(dialog).getValue().id()).isEmpty();
    }

    @Test
    void automaticIsSavedAsNoTokenizer() throws Exception {
        assertThat(saved(newOpenAiDialog("http://llm:8080/v1", "m")).getTokenizer()).isNull();
    }

    @Test
    void chosenTokenizerIsSaved() throws Exception {
        AIDialog dialog = newOpenAiDialog("http://llm:8080/v1", "m");
        tokenizerCombo(dialog).setValue(new TokenizerChoice("ext:" + id, "Dialog tokenizer"));

        assertThat(saved(dialog).getTokenizer()).isEqualTo("ext:" + id);
    }

    @Test
    void tokenizerThatAcceptsTheEndpointIsPreselectedForANewAi() throws Exception {
        AIDialog dialog = newOpenAiDialog("http://special:8080/v1", "m");

        assertThat(tokenizerCombo(dialog).getValue().id()).isEqualTo("ext:" + id);
        assertThat(saved(dialog).getTokenizer()).isEqualTo("ext:" + id);
    }

    @Test
    void anotherEndpointStaysOnAutomatic() {
        assertThat(tokenizerCombo(newOpenAiDialog("http://llm:8080/v1", "m")).getValue().id()).isEmpty();
    }

    @Test
    void savedAiIsEditedWithItsTokenizerAndNotChangedBySuggestions() throws Exception {
        OpenAICompatible stored = new OpenAICompatible();
        stored.setAiType(AIType.OPEN_AI_COMPATIBLE);
        stored.setName(uniqueName("stored"));
        stored.setMaxContext(4096);
        stored.setMaxCompletionTokens(256);
        stored.setUri("http://special:8080/v1");
        stored.setModel("m");
        stored.setModelName("m");
        stored.setApiKey("key");
        stored.setTokenizer("LLAMA_CPP_NATIVE");
        stored = (OpenAICompatible) aiService.save(stored);

        AIDialog dialog = new AIDialog(stored);
        dialog.create();

        assertThat(tokenizerCombo(dialog).getValue().id()).isEqualTo("LLAMA_CPP_NATIVE");
        assertThat(saved(dialog).getTokenizer()).isEqualTo("LLAMA_CPP_NATIVE");
    }

    @Test
    void tokenizerOfAPluginThatIsNotLoadedIsKept() throws Exception {
        OpenAICompatible stored = new OpenAICompatible();
        stored.setAiType(AIType.OPEN_AI_COMPATIBLE);
        stored.setName(uniqueName("orphan"));
        stored.setMaxContext(4096);
        stored.setMaxCompletionTokens(256);
        stored.setUri("http://llm:8080/v1");
        stored.setModel("m");
        stored.setModelName("m");
        stored.setApiKey("key");
        stored.setTokenizer("ext:gone");
        stored = (OpenAICompatible) aiService.save(stored);

        AIDialog dialog = new AIDialog(stored);
        dialog.create();

        assertThat(tokenizerCombo(dialog).getValue().id()).isEqualTo("ext:gone");
        assertThat(tokenizerCombo(dialog).getValue().label()).contains("gone");
        assertThat(saved(dialog).getTokenizer()).isEqualTo("ext:gone");
    }

    @Test
    void cloneKeepsTheTokenizer() throws Exception {
        OpenAICompatible stored = new OpenAICompatible();
        stored.setAiType(AIType.OPEN_AI_COMPATIBLE);
        stored.setName(uniqueName("original"));
        stored.setMaxContext(4096);
        stored.setMaxCompletionTokens(256);
        stored.setUri("http://llm:8080/v1");
        stored.setModel("m");
        stored.setModelName("m");
        stored.setApiKey("key");
        stored.setTokenizer("LITELLM_NATIVE");
        stored = (OpenAICompatible) aiService.save(stored);

        AI copy = aiService.cloneAI(stored, uniqueName("copy"));

        assertThat(((OpenAICompatible) copy).getTokenizer()).isEqualTo("LITELLM_NATIVE");
    }
}
