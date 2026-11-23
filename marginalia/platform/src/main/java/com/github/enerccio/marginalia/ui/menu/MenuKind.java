package com.github.enerccio.marginalia.ui.menu;

/**
 * The menus of the story editor that are filled from the {@link StoryMenuRegistry}.
 */
public enum MenuKind {
    /** The settings (cogs) menu of the story: acts on the story editor. */
    TOOLS,
    /** The menu of a part (the hamburger button of its card): acts on the story editor and the card. */
    CARD,
    /** The menu that opens with a right click on the text of a part: acts on the story editor and the card. */
    TEXT
}
