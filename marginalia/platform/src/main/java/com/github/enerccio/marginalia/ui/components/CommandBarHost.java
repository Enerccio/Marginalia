package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.domain.service.command.BaseCommandScope;
import com.github.enerccio.marginalia.domain.service.run.Runs;

/**
 * What a {@link CommandBar} is in (in the application: the story of a book): the scope its commands run in, and the hold a command
 * that shows its progress there takes until its work starts ({@code progressInStory}).
 */
public interface CommandBarHost {

    /**
     * The scope of a command typed into the bar.
     */
    BaseCommandScope scope();

    /**
     * The hold of what the bar shows, from the start of a command that shows its progress there to the start of that work; null when
     * there is nothing to hold, or it is held already (the command is then refused as before, with its name).
     */
    Runs.Run holdForGeneration(String humanName);
}
