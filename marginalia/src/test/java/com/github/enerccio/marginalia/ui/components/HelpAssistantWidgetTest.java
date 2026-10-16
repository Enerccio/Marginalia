package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.bound.SessionPoint;
import com.github.enerccio.marginalia.domain.collections.LimitStrategy;
import com.github.enerccio.marginalia.domain.components.AILimit;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.AILimitService;
import com.github.enerccio.marginalia.domain.service.HelpAssistantService;
import com.github.enerccio.marginalia.domain.service.HelpAssistantService.Settings;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.github.enerccio.marginalia.ui.LLMInteract;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The help assistant button: not there while the assistant is off, otherwise it opens a panel that names the AI,
 * warns when the user's limit applies and shows the conversation kept in the session.
 */
class HelpAssistantWidgetTest extends MarginaliaTestBase {

    @Autowired
    private HelpAssistantService helpService;

    @Autowired
    private AILimitService limitService;

    @Autowired
    private SessionPoint sessionPoint;

    private UI ui;
    private OpenAICompatible ai;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        // the settings belong to the installation and outlive the test
        loginAdmin();
        helpService.saveSettings(new Settings(false, null, false));
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    private void enable(boolean shared, boolean applyLimits) throws Exception {
        loginAdmin();
        ai = createAI();
        ai.setShared(shared);
        ai = (OpenAICompatible) aiService.save(ai);
        helpService.saveSettings(new Settings(true, ai.getUuid(), applyLimits));
    }

    private static Stream<Component> descendants(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(HelpAssistantWidgetTest::descendants));
    }

    private static Component panel(HelpAssistantWidget widget) {
        return widget.getChildren().filter(c -> c.getElement().getClassList().contains(CssStyles.HELP_PANEL)).findFirst().orElseThrow();
    }

    private static List<String> texts(Component root) {
        return descendants(root).filter(Span.class::isInstance).map(c -> ((Span) c).getText()).toList();
    }

    private static boolean visible(Component root, String cssClass) {
        return descendants(root).anyMatch(c -> c.getElement().getClassList().contains(cssClass) && c.isVisible());
    }

    @Test
    void isNotShownWhileTheAssistantIsOff() throws Exception {
        login();

        assertThat(new HelpAssistantWidget().isVisible()).isFalse();
    }

    @Test
    void opensWithTheNameOfTheAiAndNoWarningForAnUnmeteredModel() throws Exception {
        enable(false, true);
        loginAs(createUser());
        sessionPoint.getHelpChat().clear();
        HelpAssistantWidget widget = new HelpAssistantWidget();
        ui.add(widget);

        assertThat(widget.isVisible()).isTrue();
        assertThat(panel(widget).isVisible()).as("closed until the button is clicked").isFalse();
        ((Button) widget.getChildren().findFirst().orElseThrow()).click();

        assertThat(panel(widget).isVisible()).isTrue();
        assertThat(texts(panel(widget))).anyMatch(t -> t.contains(ai.getName()));
        assertThat(visible(panel(widget), CssStyles.HELP_WARNING)).isFalse();
    }

    @Test
    void warnsWhenTheLimitOfTheUserApplies() throws Exception {
        enable(true, true);
        User reader = createUser();
        AILimit limit = new AILimit();
        limit.setStrategy(LimitStrategy.UNLIMITED);
        limitService.setLimit(ai, reader, limit);
        loginAs(reader);
        HelpAssistantWidget widget = new HelpAssistantWidget();
        ui.add(widget);

        ((Button) widget.getChildren().findFirst().orElseThrow()).click();

        assertThat(visible(panel(widget), CssStyles.HELP_WARNING)).isTrue();
    }

    @Test
    void showsTheConversationOfTheSession() throws Exception {
        enable(false, false);
        loginAs(createUser());
        sessionPoint.getHelpChat().clear();
        sessionPoint.getHelpChat().add(LLMChatMessage.of(LLMRole.USER, "How do I back up?"));
        sessionPoint.getHelpChat().add(LLMChatMessage.of(LLMRole.ASSISTANT, "Open **Admin**."));
        HelpAssistantWidget widget = new HelpAssistantWidget();
        ui.add(widget);

        ((Button) widget.getChildren().findFirst().orElseThrow()).click();

        assertThat(descendants(panel(widget)).filter(c -> c.getElement().getClassList().contains(CssStyles.HELP_MESSAGE)).count())
                .isEqualTo(2);
        assertThat(descendants(panel(widget)).anyMatch(c -> c.getElement().getClassList().contains(CssStyles.HELP_MESSAGE_USER)
                && "How do I back up?".equals(c.getElement().getText()))).isTrue();
        sessionPoint.getHelpChat().clear();
    }

    /** A part of the page the model can see. */
    static class Editor extends Div implements LLMInteract {
        @Override
        public String getLLMState() {
            return "The user edits **Moby Dick**.";
        }
    }

    static class SomeDialog extends Dialog implements LLMInteract {
    }

    /** Two views, one shown at a time; the other can be opened. */
    static class Switcher extends Div implements LLMInteract {
        private String open = "first";

        @Override
        public String getLLMState() {
            return "The " + open + " view is open.";
        }

        @Override
        public java.util.Map<String, com.github.enerccio.marginalia.ui.LLMInteraction> getInteraction() {
            String other = open.equals("first") ? "second" : "first";
            return java.util.Map.of("open-view:" + other, new com.github.enerccio.marginalia.ui.LLMInteraction(
                    "Open the " + other + " view.", () -> open = other));
        }
    }

    @Test
    void theInteractionsAreListedOnlyWhenTheyAreAsked() {
        ui.add(new Switcher());

        assertThat(HelpAssistantWidget.readUiStateNow(ui, "", false, false)).doesNotContain("open-view");
        assertThat(HelpAssistantWidget.readUiStateNow(ui, "", false, true)).contains("`open-view:second`: Open the second view.");
    }

    @Test
    void anInteractionIsAppliedAndThePageAfterItReturned() {
        Switcher switcher = new Switcher();
        ui.add(switcher);

        String after = HelpAssistantWidget.interactNow(ui, "UI/Switcher", List.of("open-view:second"));

        assertThat(after).contains("The second view is open.").contains("`open-view:first`").doesNotContain("`open-view:second`");
    }

    @Test
    void aSequenceOfInteractionsOnThePageIsAppliedInOrder() {
        Switcher switcher = new Switcher();
        ui.add(switcher);

        String after = HelpAssistantWidget.interactNow(ui, "UI/Switcher", List.of("open-view:second", "open-view:first", "open-view:second"));

        assertThat(after).contains("The second view is open.");
    }

    @Test
    void anInteractionThatIsNotThereIsRefusedWithWhatIs() {
        ui.add(new Switcher());

        assertThatThrownBy(() -> HelpAssistantWidget.interactNow(ui, "UI/Switcher", List.of("open-view:first")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("It offers: open-view:second");
        assertThatThrownBy(() -> HelpAssistantWidget.interactNow(ui, "UI/Nope", List.of("x")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("UI/Switcher: open-view:second");
    }

    @Test
    void theScreenIsReadAsMarkdownFromThePage() {
        Editor editor = new Editor();
        ui.add(new Div(editor));

        String state = HelpAssistantWidget.readUiStateNow(ui, "", false);

        assertThat(state).startsWith("## UI").contains("### UI/Editor").contains("The user edits **Moby Dick**.");
        assertThat(HelpAssistantWidget.readUiStateNow(ui, "UI/Editor", false)).startsWith("## UI/Editor")
                .doesNotContain("## UI\n");
    }

    @Test
    void aPathThatIsNotThereListsTheOnesThatAre() {
        ui.add(new Editor());

        assertThatThrownBy(() -> HelpAssistantWidget.readUiStateNow(ui, "UI/Nope", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UI/Nope").hasMessageContaining("- UI/Editor").hasMessageContaining("deep=true");
    }

    @Test
    void anOpenDialogIsPartOfTheScreen() {
        // an opened dialog is attached to the page when the response is sent, here it is added by hand
        SomeDialog dialog = new SomeDialog();
        dialog.setHeaderTitle("Edit model");
        ui.add(dialog);
        dialog.open();

        String state = HelpAssistantWidget.readUiStateNow(ui, "", false);

        assertThat(state).contains("UI/SomeDialog").contains("titled \"Edit model\"");

        dialog.close();
        assertThat(HelpAssistantWidget.readUiStateNow(ui, "", false)).doesNotContain("SomeDialog");
        assertThat(HelpAssistantWidget.readUiStateNow(ui, "", true)).contains("UI/SomeDialog").contains("*Hidden");
    }

    @Test
    void aLongStateIsCutWithTheOutlineOfWhatWasLeftOut() {
        Div page = new Div();
        for (int i = 0; i < 400; i++) {
            Editor editor = new Editor();
            page.add(editor);
        }
        ui.add(page);

        String state = HelpAssistantWidget.readUiStateNow(ui, "", false);

        assertThat(state.length()).isLessThan(30000);
        assertThat(state).contains("[...cut").contains("- UI/Editor[400]");
    }

    @Test
    void theProblemsTheModelReadsAreThoseOfTheSessionAndTheBrowserNewestFirst() {
        var inSession = new com.github.enerccio.marginalia.bound.UiProblem("s", System.currentTimeMillis() - 2_000,
                com.github.enerccio.marginalia.bound.UiProblem.Kind.VALIDATION, "Validation failed, can't save", "");
        String browser = "[{\"id\":\"b\",\"time\":" + (System.currentTimeMillis() - 90_000)
                + ",\"kind\":\"EXCEPTION\",\"message\":\"Internal error\",\"detail\":\"X: y\"},"
                + "{\"id\":\"s\",\"time\":1,\"kind\":\"ERROR\",\"message\":\"the same, older copy\",\"detail\":\"\"}]";

        String text = HelpAssistantWidget.problemsText(List.of(inSession), browser, 0);

        assertThat(text).startsWith("1. **VALIDATION**").contains("2. **EXCEPTION**").contains("Detail: X: y")
                .doesNotContain("older copy");
        assertThat(HelpAssistantWidget.problemsText(List.of(inSession), browser, 1)).doesNotContain("EXCEPTION");
        assertThat(HelpAssistantWidget.problemsText(List.of(), "not json", 3)).contains("Nothing went wrong");
    }
}
