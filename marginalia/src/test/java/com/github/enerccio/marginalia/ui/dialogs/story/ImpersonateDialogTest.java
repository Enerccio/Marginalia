package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The window that drafts the instructions of the next turn: the guidance starts as what the author typed, the plan of the caller is
 * shown, and nothing is handed over before <i>Use as Instructions</i>. (Streaming the draft is {@code ImpersonateTest}, it needs a
 * UI with a session.)
 */
class ImpersonateDialogTest extends MarginaliaTestBase {

    @Autowired
    private ManuscriptService manuscriptService;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;

    private Manuscript book;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        Manuscript m = new Manuscript();
        m.setName(uniqueName("book"));
        book = manuscriptService.save(m);
    }

    private static Stream<Component> all(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(ImpersonateDialogTest::all));
    }

    private static List<Component> components(ImpersonateDialog dialog) {
        List<Component> components = new ArrayList<>(all(dialog).toList());
        dialog.getFooter().getElement().getChildren().forEach(e -> e.getComponent().ifPresent(components::add));
        return components;
    }

    private static List<TextArea> textAreas(List<Component> components) {
        return components.stream().filter(TextArea.class::isInstance).map(TextArea.class::cast).toList();
    }

    /** The plan, the guidance and the drafted text, in this order. */
    private static TextArea plan(List<Component> components) {
        return textAreas(components).get(0);
    }

    private static TextArea guidance(List<Component> components) {
        return textAreas(components).get(1);
    }

    private static TextArea drafted(List<Component> components) {
        return textAreas(components).get(2);
    }

    private static Button useButton(List<Component> components) {
        return components.stream().filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> "primary".equals(b.getThemeName())).reduce((first, second) -> second).orElseThrow();
    }

    @Test
    void theGuidanceStartsAsTheInstructionsTheAuthorTyped() {
        ImpersonateDialog dialog = ImpersonateDialog.create(book, new TurnInput("a harbour", "Alice", "", "She confronts him"), null, text -> {
        });

        assertThat(guidance(components(dialog)).getValue()).isEqualTo("She confronts him");
    }

    @Test
    void thePlanIsShownOnlyWhenTheCallerHasOne() {
        List<Component> without = components(ImpersonateDialog.create(book, null, "  ", text -> {
        }));
        assertThat(plan(without).isVisible()).isFalse();
        assertThat(guidance(without).getValue()).isEmpty();

        List<Component> with = components(ImpersonateDialog.create(book, null, "Chapter 2: the siege", text -> {
        }));
        assertThat(plan(with).isVisible()).isTrue();
        assertThat(plan(with).getValue()).isEqualTo("Chapter 2: the siege");
        assertThat(plan(with).isReadOnly()).isTrue();
    }

    @Test
    void useIsOffUntilThereIsADraftedText() {
        List<Component> components = components(ImpersonateDialog.create(book, null, null, text -> {
        }));

        assertThat(useButton(components).isEnabled()).isFalse();

        drafted(components).setValue("Alice confronts Bob.");
        assertThat(useButton(components).isEnabled()).isTrue();

        drafted(components).setValue("  ");
        assertThat(useButton(components).isEnabled()).isFalse();
    }

    @Test
    void useHandsOverTheTextAsTheAuthorLeftItAndClosesTheWindow() {
        AtomicReference<String> used = new AtomicReference<>();
        ImpersonateDialog dialog = ImpersonateDialog.create(book, null, null, used::set);
        List<Component> components = components(dialog);
        dialog.open();

        drafted(components).setValue("  Alice confronts Bob, and he lies.\n");
        assertThat(used.get()).isNull();

        useButton(components).click();

        assertThat(used.get()).isEqualTo("Alice confronts Bob, and he lies.");
        assertThat(dialog.isOpened()).isFalse();
    }
}
