package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AnnotationService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.QuoteFinder;
import com.github.enerccio.marginalia.utils.TextMarkup;
import com.github.enerccio.marginalia.utils.UIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The annotations of the book a story tab shows: loaded once for the whole book (one query) and kept until something changes
 * them, the marks the cards put into the text of their parts, and the changes the author makes (the views of an annotation, the
 * list of the book and the dialog that makes one all go through here, so the cards are drawn again and the list is refreshed
 * whoever changed it). The desktop and the phone story tab both use it.
 */
@Configurable(preConstruction = true)
public class StoryAnnotations {

    /**
     * The attribute of a mark that tells which annotations it is a part of (the uuid of the annotation).
     */
    public static final String MARK_ATTRIBUTE = "data-annotation";

    /**
     * The attribute of a note in the margin that tells which annotation it is (the uuid).
     */
    public static final String NOTE_ATTRIBUTE = "data-annotation-id";

    @Autowired
    private Localization loc;

    @Autowired
    private AnnotationService annotationService;

    private final Supplier<Manuscript> book;
    private final Consumer<Collection<Long>> redraw;
    private final List<Runnable> listeners = new ArrayList<>();

    private Map<Long, List<Annotation>> byPart;

    /**
     * @param book   the book as it is now
     * @param redraw draws the cards of these parts again (the marks in their text changed)
     */
    public StoryAnnotations(Supplier<Manuscript> book, Consumer<Collection<Long>> redraw) {
        this.book = book;
        this.redraw = redraw;
    }

    /**
     * Called whenever an annotation was changed through this object, or the annotations were read again.
     */
    public void addChangeListener(Runnable listener) {
        listeners.add(listener);
    }

    /**
     * Forgets what was loaded: the story is shown again, or the annotations may have been changed by something else (a command,
     * an extension).
     */
    public void invalidate() {
        byPart = null;
    }

    private Map<Long, List<Annotation>> loaded() {
        if (byPart == null) {
            byPart = new HashMap<>();
            Manuscript manuscript = book.get();
            if (manuscript != null) {
                try {
                    for (Annotation annotation : annotationService.listForManuscript(manuscript)) {
                        byPart.computeIfAbsent(annotation.getPartId(), id -> new ArrayList<>()).add(annotation);
                    }
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            }
        }
        return byPart;
    }

    /**
     * All the annotations of the part, in the order of their passages.
     */
    public List<Annotation> of(Long partId) {
        return partId == null ? List.of() : loaded().getOrDefault(partId, List.of());
    }

    /**
     * The annotations of the part that are in the text and in the list of the part: not closed.
     */
    public int openCount(Long partId) {
        return (int) of(partId).stream().filter(annotation -> annotation.getStatus() != AnnotationStatus.RESOLVED
                && annotation.getStatus() != AnnotationStatus.DISMISSED).count();
    }

    /**
     * Every annotation of the book, the parts in the order of the book.
     */
    public List<Annotation> all() {
        List<Annotation> all = new ArrayList<>();
        loaded().values().forEach(all::addAll);
        return all;
    }

    /**
     * The annotation with the uuid, null when the book has none.
     */
    public Annotation byUuid(String uuid) {
        return all().stream().filter(annotation -> annotation.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    /**
     * The marks of the open annotations of the part that have a passage in its text: the range, the class that tells where the note
     * comes from and the uuid of the annotation, which the click on the mark reports.
     *
     * @param text the text the card shows; an annotation whose passage is not in it (the text is being generated again) has no mark
     */
    public List<TextMarkup.Mark> marks(ChatMessage message, String text) {
        List<TextMarkup.Mark> marks = new ArrayList<>();
        if (message == null || text == null) {
            return marks;
        }
        for (Annotation annotation : of(message.getId())) {
            if (!annotation.getStatus().isMarked() || !annotation.hasRange() || annotation.getEnd() > text.length()) {
                continue;
            }
            marks.add(new TextMarkup.Mark(annotation.getStart(), annotation.getEnd(),
                    CssStyles.ANNOTATION_MARK + " " + markClass(annotation.getSource()),
                    " " + MARK_ATTRIBUTE + "=\"" + annotation.getUuid() + "\""));
        }
        return marks;
    }

    private static String markClass(AnnotationSource source) {
        return switch (source) {
            case AI -> CssStyles.ANNOTATION_MARK_AI;
            case AGENT -> CssStyles.ANNOTATION_MARK_AGENT;
            case PLUGIN -> CssStyles.ANNOTATION_MARK_PLUGIN;
            default -> CssStyles.ANNOTATION_MARK_AUTHOR;
        };
    }

    // ---- changes

    /**
     * Annotates the passage, or the whole part when the range is null.
     *
     * @return the annotation; null when it could not be made (the author was told)
     */
    public Annotation create(ChatMessage part, Integer start, Integer end, String text, String type, boolean sendToPrompt) {
        try {
            Annotation annotation = annotationService.create(book.get(), part, start, end, text, AnnotationSource.AUTHOR, type, null);
            if (sendToPrompt) {
                annotation = annotationService.update(book.get(), annotation, null, null, true);
            }
            changed(part.getId());
            return annotation;
        } catch (IllegalArgumentException e) {
            com.github.enerccio.marginalia.ui.widgets.Notification.error(e.getMessage());
            return null;
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return null;
        }
    }

    public void update(Annotation annotation, String text, String type, Boolean sendToPrompt) {
        run(annotation, () -> annotationService.update(book.get(), annotation, text, type, sendToPrompt));
    }

    public void setStatus(Annotation annotation, AnnotationStatus status) {
        run(annotation, () -> annotationService.setStatus(book.get(), annotation, status));
    }

    /**
     * Attaches the annotation to another passage of its part (the whole part when the range is null).
     */
    public void reattach(Annotation annotation, Integer start, Integer end) {
        run(annotation, () -> annotationService.reattach(book.get(), annotation, start, end));
    }

    public void remove(Annotation annotation) {
        run(annotation, () -> {
            annotationService.remove(book.get(), annotation);
            return annotation;
        });
    }

    /**
     * Where the selected text of a part is in its source: the places the markdown shown to the author and the source differ, the
     * words are found again by their surroundings.
     *
     * @param before the text shown before the selection, or null
     * @param after  the text shown after it, or null
     * @return null when the text is not in the part
     */
    public static int[] locate(String source, String selected, String before, String after) {
        QuoteFinder.Match match = QuoteFinder.find(source, selected, before, after, -1);
        return match == null ? null : new int[]{match.start(), match.end()};
    }

    @FunctionalInterface
    private interface Change {
        Annotation apply() throws Exception;
    }

    private void run(Annotation annotation, Change change) {
        try {
            change.apply();
            changed(annotation.getPartId());
        } catch (IllegalArgumentException e) {
            com.github.enerccio.marginalia.ui.widgets.Notification.error(e.getMessage());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void changed(Long partId) {
        invalidate();
        if (partId != null) {
            redraw.accept(List.of(partId));
        }
        new ArrayList<>(listeners).forEach(Runnable::run);
    }

}
