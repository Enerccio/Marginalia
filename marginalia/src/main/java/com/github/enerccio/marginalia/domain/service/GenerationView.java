package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;

/**
 * A page that shows a generation of its book that it does not start itself: the story tab of the book window, for a generation
 * started by a command of its command bar ({@code /next}, {@code /swipe}, {@code /regenerate}). The command gets it from
 * {@link com.github.enerccio.marginalia.domain.service.command.CommandScope#getGenerationView()} and hands the listener it returns to the
 * generation besides its own, so the new part streams into the story as for the round button, and the round button stops it.
 */
public interface GenerationView {

    /**
     * The page (the Vaadin {@code UI}) the view is in: the generation's run belongs to it, so a closed page stops it as it stops a
     * generation the page started. Null when the view is not on a page (then it shows nothing).
     */
    Object getPage();

    /**
     * The generation is about to start: the listener that shows it, or null when the view does not show this book. Called on the
     * thread of the command, before the generation starts; the view gets ready on its page (the story is shown, the window is held as
     * for a generation it starts). When the generation does not start after all (it is refused), the caller tells the listener
     * {@link GenerationListener#onCancelled onCancelled(null)}, which lets the window go again.
     */
    GenerationListener show(Manuscript book, GenerationRequest request);

}
