package com.github.enerccio.marginalia.ui;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.AnnotationService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptStoryPart;
import com.github.enerccio.marginalia.ui.dialogs.story.AnnotationView;
import com.github.enerccio.marginalia.ui.dialogs.story.AnnotationsPanel;
import com.github.enerccio.marginalia.ui.widgets.TabDock;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.markdown.Markdown;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The story editor with annotations: the cards draw the passages, the badge counts what needs a look, the tab lists the book's
 * annotations, and an edit of a part moves the marks with its text.
 */
class StoryAnnotationsDialogTest extends MarginaliaTestBase {

    private static final String FIRST = "Anna met John at the bar. They talked for hours. Then she left.";

    @Autowired
    private AnnotationService annotationService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SettingService settingService;

    // UI.setCurrent keeps a weak reference only
    private UI ui;
    private Manuscript book;
    private ChatMessage part;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
        ChatMessage message = new ChatMessage();
        message.setResponse(FIRST);
        part = chatMessageService.createRoot(book, message);
        book.setActiveLeaf(part);
        book = manuscriptService.save(book);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    private Annotation annotate(String passage, String note) throws Exception {
        int start = FIRST.indexOf(passage);
        return annotationService.create(book, part, start, start + passage.length(), note, AnnotationSource.AUTHOR, null, null);
    }

    private ManuscriptDialog opened() throws Exception {
        ManuscriptDialog dialog = new ManuscriptDialog(book);
        dialog.create();
        // the content of a dialog is not in the UI in the test: the dock is moved there
        TabDock dock = dock(dialog);
        dock.removeFromParent();
        ui.add(dock);
        // the story part alone: opening the whole dialog needs a session of the browser (the shortcut keys are sent to it)
        storyPart(dialog).load(manuscriptService.find(book));
        return dialog;
    }

    private static ManuscriptStoryPart storyPart(ManuscriptDialog dialog) throws Exception {
        java.lang.reflect.Field story = ManuscriptDialog.class.getDeclaredField("storyPart");
        story.setAccessible(true);
        return (ManuscriptStoryPart) story.get(dialog);
    }

    private static TabDock dock(ManuscriptDialog dialog) throws Exception {
        java.lang.reflect.Field bar = ManuscriptStoryPart.class.getDeclaredField("leftBar");
        bar.setAccessible(true);
        return (TabDock) bar.get(storyPart(dialog));
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

    private static Object field(Object owner, String name) throws Exception {
        java.lang.reflect.Field field = owner.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(owner);
    }

    /**
     * The cards of the story: the content of a tab sheet is not in the component tree before the browser selects the tab, so they
     * are read from the story part.
     */
    private static List<Object> cards(ManuscriptDialog dialog) throws Exception {
        Object cards = field(storyPart(dialog), "cards");
        return new ArrayList<>((java.util.Collection<?>) cards.getClass().getMethod("values").invoke(cards));
    }

    private static Button badge(Object card) throws Exception {
        return ((com.github.enerccio.marginalia.ui.dialogs.story.CardAnnotations) field(card, "cardAnnotations")).getBadge();
    }

    private static String story(ManuscriptDialog dialog) throws Exception {
        List<String> contents = new ArrayList<>();
        for (Object card : cards(dialog)) {
            contents.add(((Markdown) field(card, "responseMarkdown")).getContent());
        }
        return String.join("\n", contents);
    }

    @Test
    void theCardDrawsThePassagesOfTheOpenAnnotations() throws Exception {
        Annotation annotation = annotate("They talked for hours.", "Weak scene");

        String content = story(opened());

        assertThat(content).contains("<mark class=\"annotation-mark annotation-mark-author\" data-annotation=\"" + annotation.getUuid()
                + "\">They talked for hours.</mark>");
        assertThat(content.replaceAll("<[^>]*>", "")).isEqualTo(FIRST);
    }

    @Test
    void aBookWithoutAnnotationsIsDrawnAsBefore() throws Exception {
        assertThat(story(opened())).isEqualTo(FIRST);
    }

    @Test
    void theBadgeCountsWhatNeedsALook() throws Exception {
        annotate("Anna", "one");
        annotate("the bar", "two");
        ManuscriptDialog dialog = opened();

        Button badge = badge(cards(dialog).getFirst());

        assertThat(badge.isVisible()).isTrue();
        assertThat(badge.getText()).isEqualTo("2");
        assertThat(badge.getTooltip().getText()).isEqualTo("2 annotations need a look");
    }

    @Test
    void aPartWithoutAnnotationsHasNoBadge() throws Exception {
        ManuscriptDialog dialog = opened();

        assertThat((badge(cards(dialog).getFirst())).isVisible()).isFalse();
    }

    private static com.github.enerccio.marginalia.ui.dialogs.story.CardAnnotations cardAnnotations(Object card) throws Exception {
        return (com.github.enerccio.marginalia.ui.dialogs.story.CardAnnotations) field(card, "cardAnnotations");
    }

    private void marginSetting(boolean on) throws Exception {
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        setting.setAnnotationsInMargin(on);
        settingService.save(setting);
    }

    @Test
    void theNotesAreInThePopoverUnlessTheUserWantsThemInTheMargin() throws Exception {
        annotate("Anna", "one");

        Object card = cards(opened()).getFirst();

        assertThat(cardAnnotations(card).isMarginMode()).isFalse();
        assertThat(cardAnnotations(card).getMargin().isVisible()).isFalse();
        assertThat(cardAnnotations(card).getMargin().getComponentCount()).isZero();
        assertThat(((com.vaadin.flow.component.orderedlayout.VerticalLayout) field(card, "contentLayout")).getClassNames())
                .doesNotContain(CssStyles.ANNOTATION_MARGIN_LAYOUT);
    }

    @Test
    void theMarginHoldsTheNotesThatNeedALookInTheOrderOfTheirPassages() throws Exception {
        Annotation later = annotate("Then she left.", "abrupt");
        Annotation earlier = annotate("Anna", "who is she?");
        Annotation closed = annotate("the bar", "which bar?");
        annotationService.setStatus(book, closed, AnnotationStatus.RESOLVED);
        marginSetting(true);

        Object card = cards(opened()).getFirst();

        var margin = cardAnnotations(card).getMargin();
        assertThat(margin.isVisible()).isTrue();
        assertThat(all(margin, AnnotationView.class)).extracting(view -> view.getAnnotation().getId())
                .containsExactly(earlier.getId(), later.getId());
        assertThat(margin.getComponentAt(0).getElement().getAttribute("data-annotation-id")).isEqualTo(earlier.getUuid());
        assertThat(((com.vaadin.flow.component.orderedlayout.VerticalLayout) field(card, "contentLayout")).getClassNames())
                .contains(CssStyles.ANNOTATION_MARGIN_LAYOUT);
    }

    @Test
    void switchingTheMarginOnAndOffChangesEveryCardAndIsRemembered() throws Exception {
        annotate("Anna", "who is she?");
        ManuscriptDialog dialog = opened();
        Object card = cards(dialog).getFirst();
        java.lang.reflect.Method switchMargin = ManuscriptStoryPart.class.getDeclaredMethod("setAnnotationsInMargin", boolean.class);
        switchMargin.setAccessible(true);

        switchMargin.invoke(storyPart(dialog), true);

        assertThat(cardAnnotations(card).getMargin().getComponentCount()).isEqualTo(1);
        assertThat(settingService.getOrCreate(UserSetting.class).isAnnotationsInMargin()).isTrue();

        switchMargin.invoke(storyPart(dialog), false);

        assertThat(cardAnnotations(card).getMargin().isVisible()).isFalse();
        assertThat(cardAnnotations(card).getMargin().getComponentCount()).isZero();
        assertThat(settingService.getOrCreate(UserSetting.class).isAnnotationsInMargin()).isFalse();
    }

    @Test
    void theTabListsTheAnnotationsOfTheBook() throws Exception {
        Annotation annotation = annotate("They talked for hours.", "Weak scene");
        ManuscriptDialog dialog = opened();

        AnnotationsPanel panel = (AnnotationsPanel) field(storyPart(dialog), "annotationsPanel");

        assertThat(all(panel, AnnotationView.class)).extracting(view -> view.getAnnotation().getId()).containsExactly(annotation.getId());
        assertThat(all(panel, Button.class)).extracting(Button::getText).contains("Part 1");
    }

    @Test
    void theTabIsInTheDefaultLayout() throws Exception {
        TabDock dock = dock(opened());

        assertThat(dock.getLayout().toString()).contains(ManuscriptStoryPart.ANNOTATIONS_TAB);
    }

    @Test
    void anEditOfThePartMovesTheMarksWithTheText() throws Exception {
        Annotation annotation = annotate("They talked for hours.", "Weak scene");
        ManuscriptDialog dialog = opened();

        chatMessageService.storeResponse(book, part, "Prologue. " + FIRST);
        storyPart(dialog).load(manuscriptService.find(book));

        assertThat(story(dialog)).contains("data-annotation=\"" + annotation.getUuid() + "\">They talked for hours.</mark>")
                .startsWith("Prologue. Anna met John");
    }
}
