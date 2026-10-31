package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ActivityTracker;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.Element;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Tells the {@link ActivityTracker} that the book is open on the screen and when the author types in it: the time of the writing
 * statistics. The story tabs of the desktop and the phone each have one for their panel. The book is open while the panel is
 * attached to the page and the page is visible (the browser says so, {@link JavascriptLibrary#watchPageVisibility}); the author is
 * editing while a part is in the editor and keys are pressed in it (only the changes of that state come from the browser,
 * {@link JavascriptLibrary#watchTyping}).
 * <p>
 * It is a helper the story tab holds and not a method of the tab, so the extension points of the tab stay as they are.
 */
public class StoryActivity {

    private final Component host;
    private final ActivityTracker tracker;
    private final Set<Element> editors = new LinkedHashSet<>();

    private Manuscript book;
    private ActivityTracker.BookPresence presence;

    /**
     * @param host the panel of the story, the events of the browser are fired on it
     */
    public StoryActivity(Component host, ActivityTracker tracker) {
        this.host = host;
        this.tracker = tracker;
        host.addAttachListener(event -> start());
        host.addDetachListener(event -> stop());
        host.getElement().addEventListener("marginalia-visibility",
                event -> {
                    if (presence != null) {
                        presence.visible(event.getEventData().get("event.detail").asBoolean());
                    }
                }).addEventData("event.detail");
        host.getElement().addEventListener("marginalia-typing",
                event -> {
                    if (presence != null) {
                        presence.editing(!editors.isEmpty() && event.getEventData().get("event.detail").asBoolean());
                    }
                }).addEventData("event.detail");
        if (host.isAttached()) {
            start();
        }
    }

    /**
     * The story tab shows this book now (the same one again changes nothing).
     */
    public void track(Manuscript manuscript) {
        boolean same = manuscript != null && book != null && Objects.equals(manuscript.getId(), book.getId());
        book = manuscript;
        if (!same) {
            stop();
            start();
        }
    }

    /**
     * A part is in the editor, the keys of the text area are watched.
     */
    public void editorOpened(Element textArea) {
        if (presence == null || !editors.add(textArea)) {
            return;
        }
        JavascriptLibrary.watchTyping(host.getElement(), textArea, (int) ActivityTracker.IDLE_CUTOFF_MILLIS);
        // the card can leave the page with the editor open
        textArea.addDetachListener(event -> editorClosed(textArea));
    }

    /**
     * The editor of the part is closed or gone.
     */
    public void editorClosed(Element textArea) {
        if (!editors.remove(textArea)) {
            return;
        }
        if (host.isAttached()) {
            JavascriptLibrary.unwatchTyping(host.getElement(), textArea);
        }
        if (editors.isEmpty() && presence != null) {
            presence.editing(false);
        }
    }

    private void start() {
        if (presence != null || book == null || !host.isAttached()) {
            return;
        }
        presence = tracker.open(book);
        JavascriptLibrary.watchPageVisibility(host.getElement());
    }

    private void stop() {
        if (presence == null) {
            return;
        }
        presence.close();
        presence = null;
        editors.clear();
        if (host.isAttached()) {
            JavascriptLibrary.unwatchPageVisibility(host.getElement());
        }
    }
}
