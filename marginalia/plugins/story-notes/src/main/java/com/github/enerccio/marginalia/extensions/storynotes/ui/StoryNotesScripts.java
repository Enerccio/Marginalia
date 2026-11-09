package com.github.enerccio.marginalia.extensions.storynotes.ui;

/**
 * The scripts this plugin runs in the browser (a plugin keeps its own, the application's are in {@code JavascriptLibrary}).
 */
public final class StoryNotesScripts {

    /**
     * Event data of a click in the preview: when it is on a link made by {@code NoteBook.linkify} (an address that starts with
     * {@code #note:}) the browser's own move to that address is stopped and the title in it is the data; for any other click
     * the data is empty. The path of the event is used because the rendered text may be in a shadow root.
     */
    public static final String LINK_CLICK = "(function () {"
            + " var a = event.composedPath().find(function (e) { return e.tagName === 'A'; });"
            + " var href = a && a.getAttribute('href');"
            + " if (href && href.indexOf('#note:') === 0) { event.preventDefault(); return href; }"
            + " return ''; })()";

    private StoryNotesScripts() {
    }
}
