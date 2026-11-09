package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.vaadin.flow.component.Component;

/**
 * A kind of {@link AnchoredPassage anchored passage} an extension brings (the passages that are not editorial notes), registered
 * with {@link PassageKinds#register}. The core keeps the passages of a kind in line with the text (it is the anchoring of the
 * annotations: edits, a part generated again, a merge, a branch, the trash, backups) and asks the kind how to show them:
 * <ul>
 *   <li>the <b>mark</b> drawn over the passage in the text of the story ({@link #markClass}, {@link #markAttributes}), and what a
 *   click on it does ({@link #onClick});</li>
 *   <li>whether the <b>viewer</b> of a published book and the <b>exports</b> see the passages ({@link #shownInViewer},
 *   {@link #shownInExports}); they ask the kind, the core draws nothing of its own there.</li>
 * </ul>
 * A passage is never counted in the badge of the part, listed in the annotations tab or put in the margin: those lists are the
 * editorial notes for the author, and a passage has nothing to resolve. A kind that wants a list of its own makes it.
 */
public interface PassageKind {

    /**
     * @return the id of the kind, stored with every passage of it ({@link AnchoredPassage#getPassageKind()}); stable, the name of
     * the extension is a good prefix
     */
    String id();

    /**
     * @return the CSS classes of the mark over the passage (the extension's own stylesheet, or the shared
     * {@code annotation-mark} classes), null for a passage that is not marked in the text of the story
     */
    default String markClass(AnchoredPassage passage) {
        return null;
    }

    /**
     * @return more attributes of the mark, as HTML ({@code ' data-x="y"'}, a leading space, values escaped); the attribute of the
     * click ({@code data-annotation}, the uuid of the passage) is added by the core
     */
    default String markAttributes(AnchoredPassage passage) {
        return "";
    }

    /**
     * Called when the author clicks the mark of a passage of this kind in the story, on the desktop and the phone.
     *
     * @param passage the passage that was clicked
     * @param anchor  a component at the mark to open a popover or a dialog from
     */
    default void onClick(AnchoredPassage passage, Component anchor) {
    }

    /**
     * Called when a search and replace ({@link ManuscriptReplaceService}) changed the text inside a passage of this kind, after the core
     * moved the range of the passage through the change. It is not called for an edit of the author by hand (the passage can tell that
     * one by comparing the text with what it stored) nor for a replace that left the text of the passage as it was.
     * <p>
     * A kind that stores something made of the text of the passage (a hash to tell an edit, a copy) brings it up to date here, so a
     * replace is not taken for an edit.
     *
     * @param passage the passage, its range is in the new text
     * @param before  the text of the passage before the replace
     * @param after   the text of the passage now
     * @return true when the method changed the passage (its attributes); the core saves it then
     */
    default boolean textReplaced(AnchoredPassage passage, String before, String after) {
        return false;
    }

    /**
     * @return true when the viewer of a published book gets the passages of this kind
     */
    default boolean shownInViewer() {
        return false;
    }

    /**
     * @return true when the exports get the passages of this kind
     */
    default boolean shownInExports() {
        return false;
    }

}
