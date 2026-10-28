package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The button of the new turn form that lets the model draft the instructions, and putting the draft into them.
 */
class NewTurnFormTest extends MarginaliaTestBase {

    @Autowired
    private Localization loc;

    private static Stream<Component> all(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(NewTurnFormTest::all));
    }

    private Button impersonateButton(VerticalLayout content) {
        String label = loc.getValue(L.LABEL_IMPERSONATE);
        return all(content).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> label.equals(b.getText())).findFirst().orElseThrow();
    }

    private TextArea instructionsField(VerticalLayout content) {
        String label = loc.getValue(L.LABEL_INSTRUCTIONS);
        return all(content).filter(TextArea.class::isInstance).map(TextArea.class::cast)
                .filter(t -> label.equals(t.getLabel())).findFirst().orElseThrow();
    }

    @Test
    void theButtonIsThereOnlyWhenSomethingHandlesIt() {
        NewTurnForm form = new NewTurnForm(loc);
        VerticalLayout content = form.createContent(() -> {
        });
        assertThat(impersonateButton(content).isVisible()).isFalse();

        form.setOnImpersonate(() -> {
        });
        assertThat(impersonateButton(content).isVisible()).isTrue();

        form.setOnImpersonate(null);
        assertThat(impersonateButton(content).isVisible()).isFalse();
    }

    @Test
    void aHandlerSetBeforeTheFormIsBuiltShowsTheButton() {
        NewTurnForm form = new NewTurnForm(loc);
        form.setOnImpersonate(() -> {
        });

        assertThat(impersonateButton(form.createContent(() -> {
        })).isVisible()).isTrue();
    }

    @Test
    void clickingTheButtonRunsTheHandler() {
        NewTurnForm form = new NewTurnForm(loc);
        AtomicInteger clicks = new AtomicInteger();
        form.setOnImpersonate(clicks::incrementAndGet);
        VerticalLayout content = form.createContent(() -> {
        });

        impersonateButton(content).click();

        assertThat(clicks.get()).isEqualTo(1);
    }

    @Test
    void theDraftGoesIntoTheInstructionsAsIfTheUserTypedIt() {
        NewTurnForm form = new NewTurnForm(loc);
        AtomicInteger changes = new AtomicInteger();
        form.setOnChange(changes::incrementAndGet);
        VerticalLayout content = form.createContent(() -> {
        });

        form.setInstructions("Alice confronts Bob.");

        assertThat(instructionsField(content).getValue()).isEqualTo("Alice confronts Bob.");
        assertThat(form.toTurnInput().instructions()).isEqualTo("Alice confronts Bob.");
        // typed values are not replaced by the last part when the form opens again
        assertThat(form.storeState().get("edited").getAsBoolean()).isTrue();
        assertThat(changes.get()).isEqualTo(1);
    }

    @Test
    void theDraftWaitsForAFormThatIsNotBuiltYet() {
        NewTurnForm form = new NewTurnForm(loc);

        form.setInstructions("Alice confronts Bob.");

        assertThat(form.toTurnInput().instructions()).isEqualTo("Alice confronts Bob.");
    }
}
