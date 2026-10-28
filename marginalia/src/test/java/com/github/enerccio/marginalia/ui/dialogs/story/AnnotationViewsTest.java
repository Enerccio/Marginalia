package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AnnotationService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The views of the annotations: one annotation with its buttons, the list of the book, and the dialog that asks for a note.
 */
class AnnotationViewsTest extends MarginaliaTestBase {

    private static final String FIRST = "Anna met John at the bar. They talked for hours.";
    private static final String SECOND = "The next morning John woke up late.";

    @Autowired
    private AnnotationService annotationService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    // UI.setCurrent keeps a weak reference only
    private UI ui;
    private Manuscript book;
    private ChatMessage first;
    private ChatMessage second;
    private StoryAnnotations annotations;
    private final List<Annotation> attached = new ArrayList<>();
    private final List<Annotation> visited = new ArrayList<>();

    private final AnnotationView.Host host = new AnnotationView.Host() {
        @Override
        public boolean canAttach(Annotation annotation) {
            return true;
        }

        @Override
        public void attachToSelection(Annotation annotation) {
            attached.add(annotation);
        }

        @Override
        public boolean canGoToPart(Annotation annotation) {
            return true;
        }

        @Override
        public void goToPart(Annotation annotation) {
            visited.add(annotation);
        }
    };

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
        first = part(null, FIRST);
        second = part(first, SECOND);
        annotations = new StoryAnnotations(() -> book, ids -> {
        });
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    private ChatMessage part(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        ChatMessage saved = parent == null ? chatMessageService.createRoot(book, message) : chatMessageService.addChild(parent, message);
        Thread.sleep(2);
        return saved;
    }

    private Annotation annotate(ChatMessage part, String passage, String note) throws Exception {
        int start = part.getResponse().indexOf(passage);
        return annotationService.create(book, part, start, start + passage.length(), note, AnnotationSource.AUTHOR, null, null);
    }

    private static List<Component> descendants(Component root) {
        List<Component> all = new ArrayList<>();
        root.getChildren().forEach(child -> {
            all.add(child);
            all.addAll(descendants(child));
        });
        return all;
    }

    private static <T extends Component> List<T> all(Component root, Class<T> type) {
        return descendants(root).stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
    }

    private static Button button(Component root, String tooltip) {
        return all(root, Button.class).stream()
                .filter(b -> b.getTooltip() != null && tooltip.equals(b.getTooltip().getText()))
                .findFirst().orElseThrow(() -> new AssertionError("no button with the tooltip " + tooltip));
    }

    private static boolean hasButton(Component root, String tooltip) {
        return all(root, Button.class).stream().anyMatch(b -> b.getTooltip() != null && tooltip.equals(b.getTooltip().getText()));
    }

    private static Button textButton(Component root, String text) {
        return all(root, Button.class).stream().filter(b -> text.equals(b.getText())).findFirst()
                .orElseThrow(() -> new AssertionError("no button " + text));
    }

    private static String statusOf(Component root) {
        return all(root, Span.class).stream().filter(s -> s.getClassNames().contains(CssStyles.ANNOTATION_STATUS))
                .map(Span::getText).findFirst().orElse(null);
    }

    private Annotation stored(Annotation annotation) throws Exception {
        return annotationService.find(annotation.getId());
    }

    // ------------------------------------------------------------------------------------------------ the view

    @Test
    void theViewShowsTheStateTheQuoteAndTheNote() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");
        annotations.invalidate();

        AnnotationView view = new AnnotationView(annotations.byUuid(annotation.getUuid()), annotations, host);

        assertThat(statusOf(view)).isEqualTo("Open");
        assertThat(all(view, Span.class)).extracting(Span::getText).contains("Author", "They talked for hours.");
        assertThat(all(view, com.vaadin.flow.component.html.Div.class)).extracting(com.vaadin.flow.component.html.Div::getText)
                .contains("Weak scene");
    }

    @Test
    void resolveDismissAndReopenChangeTheStatus() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");
        annotations.invalidate();
        AnnotationView view = new AnnotationView(annotations.byUuid(annotation.getUuid()), annotations, host);

        button(view, "Resolve").click();
        assertThat(stored(annotation).getStatus()).isEqualTo(AnnotationStatus.RESOLVED);
        assertThat(statusOf(view)).isEqualTo("Resolved");
        assertThat(hasButton(view, "Resolve")).isFalse();

        button(view, "Reopen").click();
        assertThat(stored(annotation).getStatus()).isEqualTo(AnnotationStatus.OPEN);
        assertThat(statusOf(view)).isEqualTo("Open");

        button(view, "Dismiss").click();
        assertThat(stored(annotation).getStatus()).isEqualTo(AnnotationStatus.DISMISSED);
        assertThat(statusOf(view)).isEqualTo("Dismissed");
    }

    @Test
    void theNoteCanBeEdited() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");
        annotationService.update(book, annotation, null, "pacing", null);
        annotations.invalidate();
        AnnotationView view = new AnnotationView(annotations.byUuid(annotation.getUuid()), annotations, host);

        button(view, "Edit").click();
        TextArea text = all(view, TextArea.class).getFirst();
        TextField type = all(view, TextField.class).getFirst();
        assertThat(text.getValue()).isEqualTo("Weak scene");
        assertThat(type.getValue()).isEqualTo("pacing");
        text.setValue("Very weak scene");
        type.setValue("");
        textButton(view, "Save").click();

        Annotation saved = stored(annotation);
        assertThat(saved.getText()).isEqualTo("Very weak scene");
        assertThat(saved.getType()).isNull();
        assertThat(all(view, TextArea.class)).isEmpty();
    }

    @Test
    void anEmptyNoteIsNotSavedAndCancelLeavesTheNote() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");
        annotations.invalidate();
        AnnotationView view = new AnnotationView(annotations.byUuid(annotation.getUuid()), annotations, host);

        button(view, "Edit").click();
        all(view, TextArea.class).getFirst().setValue("  ");
        textButton(view, "Save").click();
        assertThat(stored(annotation).getText()).isEqualTo("Weak scene");
        assertThat(all(view, TextArea.class).getFirst().isInvalid()).isTrue();

        textButton(view, "Cancel").click();
        assertThat(all(view, TextArea.class)).isEmpty();
    }

    @Test
    void sendToPromptIsAFlagOnTheAnnotation() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");
        annotations.invalidate();
        AnnotationView view = new AnnotationView(annotations.byUuid(annotation.getUuid()), annotations, host);

        Checkbox send = all(view, Checkbox.class).getFirst();
        assertThat(send.getValue()).isFalse();
        send.setValue(true);
        // the value of a checkbox set on the server is not a change of the client: the view only follows the client
        assertThat(stored(annotation).isSendToPrompt()).isFalse();
    }

    @Test
    void anOrphanedNoteSaysSoAndCanBeAttachedToTheSelection() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");
        chatMessageService.storeResponse(book, first, "Anna met John at the bar.");
        annotations.invalidate();

        AnnotationView view = new AnnotationView(annotations.byUuid(annotation.getUuid()), annotations, host);

        assertThat(statusOf(view)).isEqualTo("Orphaned");
        assertThat(all(view, Span.class)).extracting(Span::getText).anyMatch(t -> t.contains("is gone from the text"));
        textButton(view, "Attach to selection").click();
        assertThat(attached).extracting(Annotation::getId).containsExactly(annotation.getId());
    }

    @Test
    void aNoteWithAPassageHasNoAttachButton() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");
        annotations.invalidate();

        AnnotationView view = new AnnotationView(annotations.byUuid(annotation.getUuid()), annotations, host);

        assertThat(all(view, Button.class)).extracting(Button::getText).doesNotContain("Attach to selection");
        textButton(view, "Show in story").click();
        assertThat(visited).hasSize(1);
    }

    @Test
    void deleteAsksFirst() throws Exception {
        Annotation annotation = annotate(first, "They talked for hours.", "Weak scene");
        annotations.invalidate();
        AnnotationView view = new AnnotationView(annotations.byUuid(annotation.getUuid()), annotations, host);

        button(view, "Delete").click();

        // the question is a dialog, nothing is deleted before it is answered
        assertThat(stored(annotation).isDeleted()).isFalse();
    }

    // ------------------------------------------------------------------------------------------------ the list

    @Test
    void theListGroupsTheAnnotationsByPartInTheOrderOfTheStory() throws Exception {
        annotate(second, "John woke up", "late");
        Annotation later = annotate(first, "They talked for hours.", "Weak scene");
        Annotation early = annotate(first, "Anna", "Name?");
        Annotation closed = annotate(first, "the bar", "Which bar?");
        annotationService.setStatus(book, closed, AnnotationStatus.RESOLVED);

        AnnotationsPanel panel = new AnnotationsPanel(annotations, host);
        panel.refresh(Map.of(first.getId(), 1, second.getId(), 2));

        List<AnnotationView> views = all(panel, AnnotationView.class);
        assertThat(views).extracting(view -> view.getAnnotation().getId())
                .containsExactly(early.getId(), later.getId(), annotations.of(second.getId()).getFirst().getId());
        assertThat(all(panel, Button.class)).extracting(Button::getText).contains("Part 1", "Part 2");
        assertThat(panel.openCount()).isEqualTo(3);
        assertThat(all(panel, Span.class)).extracting(Span::getText).contains("3 need a look, 3 shown");

        textButton(panel, "Part 2").click();
        assertThat(visited).hasSize(1);
        assertThat(visited.getFirst().getPartId()).isEqualTo(second.getId());
    }

    @Test
    void theFilterShowsTheClosedOnesAndAll() throws Exception {
        annotate(first, "Anna", "open");
        Annotation closed = annotate(first, "the bar", "closed");
        annotationService.setStatus(book, closed, AnnotationStatus.DISMISSED);
        AnnotationsPanel panel = new AnnotationsPanel(annotations, host);
        panel.refresh(Map.of(first.getId(), 1));
        @SuppressWarnings("unchecked")
        com.vaadin.flow.component.combobox.ComboBox<Object> filter = all(panel, com.vaadin.flow.component.combobox.ComboBox.class).getFirst();

        assertThat(all(panel, AnnotationView.class)).hasSize(1);

        filter.setValue(filter.getListDataView().getItems().toList().get(1));
        assertThat(all(panel, AnnotationView.class)).extracting(view -> view.getAnnotation().getText()).containsExactly("closed");

        filter.setValue(filter.getListDataView().getItems().toList().get(2));
        assertThat(all(panel, AnnotationView.class)).hasSize(2);
    }

    @Test
    void theListIsRedrawnWhenAnAnnotationChanges() throws Exception {
        Annotation annotation = annotate(first, "Anna", "open");
        AnnotationsPanel panel = new AnnotationsPanel(annotations, host);
        panel.refresh(Map.of(first.getId(), 1));
        assertThat(all(panel, AnnotationView.class)).hasSize(1);

        annotations.setStatus(annotations.of(first.getId()).getFirst(), AnnotationStatus.RESOLVED);

        assertThat(all(panel, AnnotationView.class)).isEmpty();
        assertThat(all(panel, Span.class)).extracting(Span::getText).contains("0 need a look, 0 shown");
        assertThat(stored(annotation).getStatus()).isEqualTo(AnnotationStatus.RESOLVED);
    }

    @Test
    void partsOfOtherBranchesAreListedButNotShownInTheStory() throws Exception {
        annotate(second, "John woke up", "other branch");
        AnnotationsPanel panel = new AnnotationsPanel(annotations, host);

        panel.refresh(Map.of(first.getId(), 1));

        assertThat(all(panel, Span.class)).extracting(Span::getText).contains("Part of another branch");
        assertThat(all(panel, Button.class)).extracting(Button::getText).noneMatch(t -> t.startsWith("Part "));
    }

    @Test
    void anEmptyBookSaysHowToStart() {
        AnnotationsPanel panel = new AnnotationsPanel(annotations, host);
        panel.refresh(Map.of());

        assertThat(all(panel, Span.class)).extracting(Span::getText).anyMatch(t -> t.startsWith("No annotations here"));
    }

    // ------------------------------------------------------------------------------------------------ the dialog

    @Test
    void theDialogHandsOverWhatWasEntered() {
        List<String> got = new ArrayList<>();
        AnnotationDialog dialog = AnnotationDialog.create("They talked for hours.",
                (text, type, send) -> got.add(text + "|" + type + "|" + send));

        dialog.noteField().setValue("Weak scene");
        dialog.typeField().setValue("pacing");
        dialog.sendField().setValue(true);
        dialog.saveForTest();

        assertThat(got).containsExactly("Weak scene|pacing|true");
    }

    @Test
    void theDialogNeedsANote() {
        List<String> got = new ArrayList<>();
        AnnotationDialog dialog = AnnotationDialog.create(null, (text, type, send) -> got.add(text));

        dialog.noteField().setValue(" ");
        dialog.saveForTest();

        assertThat(got).isEmpty();
        assertThat(dialog.noteField().isInvalid()).isTrue();
        // no type is no type
        dialog.noteField().setValue("Rewrite");
        dialog.typeField().setValue("  ");
        dialog.saveForTest();
        assertThat(got).containsExactly("Rewrite");
    }
}
