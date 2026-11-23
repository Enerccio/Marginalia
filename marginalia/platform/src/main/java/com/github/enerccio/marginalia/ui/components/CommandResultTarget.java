package com.github.enerccio.marginalia.ui.components;

/**
 * Where a {@link CommandBar} sends what a command returned. The bar does not show results itself: the component that
 * places it decides what a result means there - the story tab shows it above the bar
 * ({@link CommandResultView}), another place can insert it into a text, use it as a value or ignore it. Called on the
 * UI thread, inside the session of the user, once for every command that finished; failures and stopped commands are
 * reported by the bar.
 */
@FunctionalInterface
public interface CommandResultTarget {

    void onCommandResult(CommandResult result);

    /**
     * The user started something new (typed in the field or ran another command), so a result that is still shown is
     * out of date. Does nothing by default.
     */
    default void clearCommandResult() {
    }

}
