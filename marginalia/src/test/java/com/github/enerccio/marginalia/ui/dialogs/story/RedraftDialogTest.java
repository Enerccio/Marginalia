package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AnnotationService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.RedraftService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
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
 * The window of the redraft of a part: nothing is stored before Apply, Apply replaces the text. (Streaming the redraft
 * is {@code RedraftTest}, it needs a UI with a session.)
 */
class RedraftDialogTest extends MarginaliaTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private AnnotationService annotationService;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;

    private Manuscript book;
    private ChatMessage part;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        Manuscript m = new Manuscript();
        m.setName(uniqueName("book"));
        book = manuscriptService.save(m);
        part = chatMessageService.find(chatMessageService.createPart(book, null, "He drew his sword.", new TurnInput(null, null, null, "Write the duel")).getId());
    }

    private static Stream<Component> all(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(RedraftDialogTest::all));
    }

    private static List<Component> components(RedraftDialog dialog) {
        List<Component> components = new ArrayList<>(all(dialog).toList());
        dialog.getFooter().getElement().getChildren().forEach(e -> e.getComponent().ifPresent(components::add));
        return components;
    }

    private static TextArea redrafted(List<Component> components) {
        // the instructions come first, the redrafted text second
        return (TextArea) components.stream().filter(TextArea.class::isInstance).skip(1).findFirst().orElseThrow();
    }

    private static Button applyButton(List<Component> components) {
        return components.stream().filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> "primary".equals(b.getThemeName())).reduce((first, second) -> second).orElseThrow();
    }

    @Test
    void applyIsOffUntilThereIsARedraftedText() {
        RedraftDialog dialog = RedraftDialog.create(book, part, saved -> {
        });
        List<Component> components = components(dialog);

        assertThat(applyButton(components).isEnabled()).isFalse();

        redrafted(components).setValue("He drew his sword slowly.");
        assertThat(applyButton(components).isEnabled()).isTrue();

        redrafted(components).setValue("  ");
        assertThat(applyButton(components).isEnabled()).isFalse();
    }

    @Test
    void applyReplacesTheTextOfThePart() throws Exception {
        AtomicReference<ChatMessage> applied = new AtomicReference<>();
        RedraftDialog dialog = RedraftDialog.create(book, part, applied::set);
        List<Component> components = components(dialog);
        dialog.open();

        redrafted(components).setValue("He drew his sword slowly, rain on the blade.");
        // nothing changed yet
        assertThat(chatMessageService.find(part.getId()).getResponse()).isEqualTo("He drew his sword.");

        applyButton(components).click();

        ChatMessage stored = chatMessageService.find(part.getId());
        assertThat(stored.getResponse()).isEqualTo("He drew his sword slowly, rain on the blade.");
        assertThat(stored.isEdited()).isTrue();
        assertThat(stored.getWordCount()).isEqualTo(9);
        assertThat(applied.get().getResponse()).isEqualTo(stored.getResponse());
        assertThat(dialog.isOpened()).isFalse();
    }

    @Test
    void theCheckboxOfTheAnnotationsIsThereOnlyForAPartThatHasOpenOnes() throws Exception {
        List<Component> without = components(RedraftDialog.create(book, part, saved -> {
        }));
        assertThat(without.stream().filter(Checkbox.class::isInstance).map(Checkbox.class::cast).findFirst().orElseThrow().isVisible()).isFalse();

        annotationService.create(book, part, 0, 2, "Who?", AnnotationSource.AUTHOR, null, null);
        Checkbox with = components(RedraftDialog.create(book, part, saved -> {
        })).stream().filter(Checkbox.class::isInstance).map(Checkbox.class::cast).findFirst().orElseThrow();

        assertThat(with.isVisible()).isTrue();
        assertThat(with.getValue()).isTrue();
    }

    @Test
    void applyResolvesTheAnnotationsTheRedraftWasAskedToDealWith() throws Exception {
        Annotation open = annotationService.create(book, part, 0, 2, "Who?", AnnotationSource.AUTHOR, null, null);
        // the redraft would have been asked to; the dialog remembers the open ones when it starts
        RedraftDialog dialog = RedraftDialog.create(book, part, saved -> {
        });
        List<Component> components = components(dialog);
        java.lang.reflect.Field addressed = RedraftDialog.class.getDeclaredField("addressed");
        addressed.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Annotation> list = (List<Annotation>) addressed.get(dialog);
        list.add(open);
        dialog.open();

        redrafted(components).setValue("Arthur drew his sword.");
        applyButton(components).click();

        assertThat(annotationService.find(open.getId()).getStatus()).isEqualTo(AnnotationStatus.RESOLVED);
    }

    @Test
    void thePassageReplacesOnlyItselfAndKeepsTheWhitespaceAroundIt() {
        RedraftService.Passage passage = new RedraftService.Passage(3, 12);

        assertThat(RedraftDialog.replacePassage("A.  Old one. B.", passage, "New text.")).isEqualTo("A.  New text. B.");
        assertThat(RedraftDialog.replacePassage("A.  Old one. B.", new RedraftService.Passage(4, 12), "\n New text.\n")).isEqualTo("A.  New text. B.");
        assertThat(RedraftDialog.replacePassage("Old one", new RedraftService.Passage(0, 7), "New")).isEqualTo("New");
    }

    @Test
    void applyForAPassageReplacesJustThePassage() throws Exception {
        // "He drew his sword." - the passage is "drew his sword"
        AtomicReference<ChatMessage> applied = new AtomicReference<>();
        RedraftDialog dialog = RedraftDialog.create(book, part, new RedraftService.Passage(3, 17), applied::set);
        List<Component> components = components(dialog);
        dialog.open();

        // the selected text, the instructions, the redrafted text
        TextArea redraftedText = (TextArea) components.stream().filter(TextArea.class::isInstance).skip(2).findFirst().orElseThrow();
        redraftedText.setValue("slowly lifted the blade");
        applyButton(components).click();

        assertThat(chatMessageService.find(part.getId()).getResponse()).isEqualTo("He slowly lifted the blade.");
        assertThat(applied.get().getResponse()).isEqualTo("He slowly lifted the blade.");
        assertThat(dialog.isOpened()).isFalse();
    }

    @Test
    void applyForAPassageIsRefusedWhenThePartChangedMeanwhile() throws Exception {
        RedraftDialog dialog = RedraftDialog.create(book, part, new RedraftService.Passage(3, 17), saved -> {
        });
        List<Component> components = components(dialog);
        dialog.open();
        chatMessageService.updateResponse(book, part, "He sheathed his sword.");

        ((TextArea) components.stream().filter(TextArea.class::isInstance).skip(2).findFirst().orElseThrow()).setValue("slowly lifted the blade");
        applyButton(components).click();

        assertThat(chatMessageService.find(part.getId()).getResponse()).isEqualTo("He sheathed his sword.");
    }

    @Test
    void onlyTheAnnotationsInThePassageOfferTheCheckbox() throws Exception {
        annotationService.create(book, part, 0, 2, "Who?", AnnotationSource.AUTHOR, null, null);
        Checkbox outside = components(RedraftDialog.create(book, part, new RedraftService.Passage(3, 17), saved -> {
        })).stream().filter(Checkbox.class::isInstance).map(Checkbox.class::cast).findFirst().orElseThrow();
        assertThat(outside.isVisible()).isFalse();

        annotationService.create(book, part, 8, 12, "Cliche", AnnotationSource.AUTHOR, null, null);
        Checkbox inside = components(RedraftDialog.create(book, part, new RedraftService.Passage(3, 17), saved -> {
        })).stream().filter(Checkbox.class::isInstance).map(Checkbox.class::cast).findFirst().orElseThrow();
        assertThat(inside.isVisible()).isTrue();
    }
}
