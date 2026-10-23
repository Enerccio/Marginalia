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
import com.github.enerccio.marginalia.utils.TextMarkup;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The annotations a story tab keeps: read once for the book, the marks they put into the text of a part, and the changes the
 * author makes through it.
 */
class StoryAnnotationsTest extends MarginaliaTestBase {

    private static final String TEXT = "Anna met John at the bar. They talked for hours. Then she left.";

    @Autowired
    private AnnotationService annotationService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    // UI.setCurrent keeps a weak reference only
    private UI ui;
    private final List<Collection<Long>> redrawn = new ArrayList<>();
    private int changes;
    private Manuscript book;
    private ChatMessage part;
    private StoryAnnotations annotations;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
        ChatMessage message = new ChatMessage();
        message.setResponse(TEXT);
        part = chatMessageService.createRoot(book, message);
        annotations = new StoryAnnotations(() -> book, redrawn::add);
        annotations.addChangeListener(() -> changes++);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    private int start(String passage) {
        return TEXT.indexOf(passage);
    }

    private Annotation make(String passage, String note, AnnotationSource source) throws Exception {
        return annotationService.create(book, part, start(passage), start(passage) + passage.length(), note, source, null, null);
    }

    @Test
    void theOpenAnnotationsWithAPassageAreMarkedWithTheirUuid() throws Exception {
        Annotation open = make("They talked for hours.", "Weak scene", AnnotationSource.AUTHOR);
        Annotation resolved = make("Then she left.", "Abrupt", AnnotationSource.AI);
        annotationService.setStatus(book, resolved, AnnotationStatus.RESOLVED);
        annotationService.create(book, part, null, null, "Whole part", AnnotationSource.AUTHOR, null, null);

        List<TextMarkup.Mark> marks = annotations.marks(part, TEXT);

        assertThat(marks).hasSize(1);
        TextMarkup.Mark mark = marks.getFirst();
        assertThat(mark.start()).isEqualTo(start("They talked"));
        assertThat(mark.end()).isEqualTo(start("They talked") + "They talked for hours.".length());
        assertThat(mark.cssClass()).contains(CssStyles.ANNOTATION_MARK).contains(CssStyles.ANNOTATION_MARK_AUTHOR);
        assertThat(mark.attributes()).contains(StoryAnnotations.MARK_ATTRIBUTE + "=\"" + open.getUuid() + "\"");
    }

    @Test
    void theMarkTellsWhoWroteTheNote() throws Exception {
        make("Anna", "By a model", AnnotationSource.AI);
        make("John", "By an agent", AnnotationSource.AGENT);
        make("bar", "By an extension", AnnotationSource.PLUGIN);

        assertThat(annotations.marks(part, TEXT)).extracting(TextMarkup.Mark::cssClass)
                .anyMatch(c -> c.contains(CssStyles.ANNOTATION_MARK_AI))
                .anyMatch(c -> c.contains(CssStyles.ANNOTATION_MARK_AGENT))
                .anyMatch(c -> c.contains(CssStyles.ANNOTATION_MARK_PLUGIN));
    }

    @Test
    void aPassageThatIsNotInTheTextHasNoMark() throws Exception {
        make("Then she left.", "Abrupt", AnnotationSource.AUTHOR);

        // the text of the card is shorter than the passage: it is being generated again
        assertThat(annotations.marks(part, "Anna")).isEmpty();
        assertThat(annotations.marks(null, TEXT)).isEmpty();
        assertThat(annotations.marks(part, null)).isEmpty();
    }

    @Test
    void theMarksPutIntoTheTextLeaveTheTextAsItIs() throws Exception {
        make("They talked for hours.", "Weak scene", AnnotationSource.AUTHOR);
        make("talked for", "Which hours?", AnnotationSource.AUTHOR);

        String marked = TextMarkup.apply(TEXT, annotations.marks(part, TEXT));

        assertThat(marked.replaceAll("<[^>]*>", "")).isEqualTo(TEXT);
        assertThat(marked).contains("data-annotation");
    }

    @Test
    void openCountIsWhatNeedsALook() throws Exception {
        make("Anna", "one", AnnotationSource.AUTHOR);
        Annotation closed = make("John", "two", AnnotationSource.AUTHOR);
        annotationService.setStatus(book, closed, AnnotationStatus.DISMISSED);
        Annotation lost = make("bar", "three", AnnotationSource.AUTHOR);
        chatMessageService.storeResponse(book, part, TEXT.replace(" at the bar", ""));

        annotations.invalidate();

        assertThat(annotations.of(part.getId())).hasSize(3);
        assertThat(annotations.openCount(part.getId())).isEqualTo(2);
        assertThat(annotations.openCount(null)).isZero();
        assertThat(annotationService.find(lost.getId()).getStatus()).isEqualTo(AnnotationStatus.ORPHANED);
    }

    @Test
    void theAnnotationsAreReadAgainOnlyAfterTheyChanged() throws Exception {
        make("Anna", "one", AnnotationSource.AUTHOR);
        assertThat(annotations.of(part.getId())).hasSize(1);

        // made behind its back (a command, an extension)
        make("John", "two", AnnotationSource.AUTHOR);
        assertThat(annotations.of(part.getId())).hasSize(1);

        annotations.invalidate();
        assertThat(annotations.of(part.getId())).hasSize(2);
    }

    @Test
    void changesGoThroughTheServiceRedrawTheCardAndTellTheListeners() throws Exception {
        int s = start("They talked for hours.");
        Annotation made = annotations.create(part, s, s + 22, "Weak scene", "pacing", true);

        assertThat(made).isNotNull();
        assertThat(annotationService.find(made.getId()).isSendToPrompt()).isTrue();
        assertThat(annotationService.find(made.getId()).getType()).isEqualTo("pacing");
        assertThat(redrawn.getLast()).containsExactly(part.getId());
        assertThat(changes).isEqualTo(1);
        assertThat(annotations.of(part.getId())).hasSize(1);

        annotations.setStatus(made, AnnotationStatus.RESOLVED);
        assertThat(annotationService.find(made.getId()).getStatus()).isEqualTo(AnnotationStatus.RESOLVED);
        annotations.update(made, "Fixed", null, false);
        assertThat(annotationService.find(made.getId()).getText()).isEqualTo("Fixed");
        annotations.reattach(made, start("Anna"), start("Anna") + 4);
        assertThat(annotationService.find(made.getId()).getStart()).isEqualTo(start("Anna"));
        annotations.remove(made);
        assertThat(annotationService.find(made.getId()).isDeleted()).isTrue();
        assertThat(annotations.of(part.getId())).isEmpty();
        assertThat(changes).isEqualTo(5);
    }

    @Test
    void aBadPassageIsRefusedWithoutAnError() {
        Annotation made = annotations.create(part, 5, 9999, "x", null, false);

        assertThat(made).isNull();
        assertThat(changes).isZero();
    }

    @Test
    void theSelectionOfTheBrowserIsFoundInTheSource() {
        String source = "Anna met **John** at the *bar*. They talked.";

        int[] range = StoryAnnotations.locate(source, "John at the bar", "Anna met ", ". They talked.");

        assertThat(range).isNotNull();
        assertThat(source.substring(range[0], range[1])).contains("John").contains("bar");
        assertThat(StoryAnnotations.locate(source, "A dragon landed on the church roof", null, null)).isNull();
        assertThat(StoryAnnotations.locate(source, "talked", "They ", ".")).containsExactly(source.indexOf("talked"), source.indexOf("talked") + 6);
    }
}
