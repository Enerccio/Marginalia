package com.github.enerccio.marginalia.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.tabs.Tab;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A part of the UI the help assistant's model can look at: it describes what the user currently sees as a tree of
 * elements, each with a logical path (the simple class names from the root, {@code DesktopWorkspace/ManuscriptPart}) and
 * a Markdown text of its state. The model reads the tree with the function {@code get_ui_state}.
 * <p>
 * Everything has a default, so a class that implements the interface only is listed with its name; override
 * {@link #getLLMState()} to say what is worth knowing about it (the selected book, the open tab, a filter), and
 * {@link #getLLMChildren(boolean)} when the children are not components in its own component tree (a part of the
 * workspace is not a {@link Component}, the components it creates belong to the workspace). Describe only what the user
 * can see and would say himself: no secrets, no ids, nothing long (a title of a book, not its text).
 * <p>
 * A snapshot holds only the <b>active branch</b> of the UI: what is not shown (a hidden component, the content of a
 * tab that is not selected) is not expanded. Where the UI offers alternatives, report the active one as it is and the
 * others through {@link #options(List, LLMInteract, boolean)}: they are listed as children so the model knows they exist
 * and can tell the user where to click, but have no state and no children of their own. A <b>deep</b> snapshot
 * ({@code deep = true}, the model asks for it when it needs something that is not on the screen) expands the
 * alternatives and the hidden components too; hidden ones are marked as such.
 * <p>
 * An element can also offer <b>interactions</b> ({@link #getInteraction()}): moves through the interface the model may
 * make for the user when an administrator allowed it (open a section, select a tab, open the dialog to add something),
 * applied with {@link #applyInteraction(List)}. Only moves that are harmless and can be undone with one click: never one
 * that changes, deletes, saves, sends, generates, logs out or leaves a dialog that may hold unsaved work.
 * <p>
 * Describing the state is called on the UI thread under the session lock, so it may read components freely, and must
 * be quick and must not change anything. The interactions run there too. Extensions can implement it on their own
 * components too: a component that is visible and implements it is found by the nearest {@code LLMInteract} above it.
 */
public interface LLMInteract {

    /**
     * The name of the element in the paths: the simple class name by default.
     */
    default String getLLMName() {
        Class<?> type = getClass();
        while (type.getSimpleName().isEmpty() && type.getSuperclass() != null) {
            type = type.getSuperclass();
        }
        return type.getSimpleName();
    }

    /**
     * What the user sees of this element itself, in Markdown, without its children. Empty by default, for a dialog
     * its title.
     */
    default String getLLMState() {
        return this instanceof Component component ? LLMSnapshot.describe(component) : "";
    }

    /**
     * The elements inside this one. For a component by default the nearest visible descendants that are
     * {@code LLMInteract} too (their own children are theirs to report; with {@code deep} the hidden ones as well);
     * anything that is not a component has none unless it says so.
     *
     * @param deep whether to include what is not shown: the alternatives that are not active, hidden components
     */
    default List<LLMInteract> getLLMChildren(boolean deep) {
        return this instanceof Component component ? LLMSnapshot.nearestDescendants(component, deep) : List.of();
    }

    /**
     * What is wrong with the form this element shows right now, one line each: the fields that are marked invalid, with
     * the message they show, the required ones that are empty ({@link LLMFormProblems}); the model uses it to say why a
     * form can not be saved. For a component by default the visible fields inside it that are not inside another
     * element; override to add what the form checks on its own (fields that depend on each other). Never a value of a
     * field. Empty when the form is fine, or there is none.
     */
    default List<String> getLLMProblems() {
        return this instanceof Component component ? LLMFormProblems.scan(component) : List.of();
    }

    /**
     * What the model may do to this element, by name: the keys it passes to {@link #applyInteraction(String)}. Only
     * what is possible now (a section that is open is not offered again, none while the navigation is locked), and
     * only non destructive moves, see {@link LLMInteraction}. None by default. The map is read again before every step
     * of a sequence, so an interaction may change which ones follow.
     */
    default Map<String, LLMInteraction> getInteraction() {
        return Map.of();
    }

    /**
     * Applies one interaction (a key of {@link #getInteraction()}) and returns the snapshot of the element after it.
     *
     * @throws IllegalArgumentException when the element offers no such interaction; the message lists the ones it does
     */
    default LLMSnapshot applyInteraction(String interaction) {
        return applyInteraction(List.of(interaction));
    }

    /**
     * Applies the interactions one after the other, as the model knows the sequence, and returns the snapshot of the
     * element after the last one. Each one is looked up when its turn comes, among the interactions the element offers
     * by then. The first that is not offered or fails ends the sequence with an {@link IllegalArgumentException} that
     * names what was already done; what was done stays done.
     */
    default LLMSnapshot applyInteraction(List<String> interactions) {
        List<String> done = new ArrayList<>();
        for (String key : interactions) {
            Map<String, LLMInteraction> offered = getInteraction();
            LLMInteraction interaction = LLMInteraction.lookup(offered, key);
            if (interaction == null) {
                throw new IllegalArgumentException("\"" + key + "\" is not an interaction of " + getLLMName()
                        + (done.isEmpty() ? "" : " after doing " + String.join(", ", done))
                        + ". It offers: " + (offered.isEmpty() ? "nothing" : String.join(", ", offered.keySet())) + ".");
            }
            try {
                interaction.action().run();
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("\"" + key + "\" failed: " + e.getMessage()
                        + (done.isEmpty() ? "" : " (done before: " + String.join(", ", done) + ")"), e);
            }
            done.add(key);
        }
        return takeLLMSnapshot();
    }

    /**
     * The snapshot of the active branch: this element and, recursively, its children without what is not shown.
     * The root of the returned tree has this element's name as its path, the paths of the descendants continue it
     * ({@code Name/Child/GrandChild}); children with the same name are numbered ({@code Child[1]}, {@code Child[2]}).
     */
    default LLMSnapshot takeLLMSnapshot() {
        return takeLLMSnapshot(false);
    }

    /**
     * The snapshot of this element and, recursively, of its children; with {@code deep} also of the alternatives that
     * are not active and of the hidden components. The default builds it from {@link #getLLMName()}, {@link
     * #getLLMState()} and {@link #getLLMChildren(boolean)}; an element can override it to describe itself differently.
     */
    default LLMSnapshot takeLLMSnapshot(boolean deep) {
        return LLMSnapshot.take(this, deep);
    }

    /**
     * An alternative that is not active (a tab that is not selected): listed under the name of the element, with a
     * note that it is not open and nothing else, so that the model knows it is there without reading it.
     */
    static LLMInteract inactive(LLMInteract element) {
        return new LLMInteract() {
            @Override
            public String getLLMName() {
                return element.getLLMName();
            }

            @Override
            public String getLLMState() {
                return "Not open: an alternative the user can switch to.";
            }

            @Override
            public List<LLMInteract> getLLMChildren(boolean deep) {
                return List.of();
            }
        };
    }

    /**
     * The children of an element that shows one of several alternatives: the active one as it is, in its place among
     * the others, which are {@link #inactive listed only}, or, for a deep snapshot, as they are too.
     *
     * @param all    all alternatives in the order the user sees them
     * @param active the one that is shown, null when none is
     * @param deep   whether the alternatives that are not active are expanded
     */
    static List<LLMInteract> options(List<? extends LLMInteract> all, LLMInteract active, boolean deep) {
        List<LLMInteract> options = new ArrayList<>();
        for (LLMInteract option : all) {
            options.add(deep || option == active ? option : inactive(option));
        }
        return options;
    }

    /**
     * The parts of a window or a dialog as options (see {@link #options}) when each part created a component of its
     * own: the active part is expanded with the elements inside its component (see {@link #within}), the others are
     * only listed, or, for a deep snapshot, expanded as well.
     *
     * @param parts  every part with the component it created, in the order the user sees them
     * @param active the part that is open
     */
    static List<LLMInteract> parts(Map<? extends LLMInteract, ? extends Component> parts, LLMInteract active, boolean deep) {
        List<LLMInteract> all = new ArrayList<>();
        LLMInteract activeElement = null;
        for (Map.Entry<? extends LLMInteract, ? extends Component> entry : parts.entrySet()) {
            LLMInteract element = within(entry.getKey(), entry.getValue());
            all.add(element);
            if (entry.getKey() == active) {
                activeElement = element;
            }
        }
        return options(all, activeElement, deep);
    }

    /**
     * The interactions for a row or sheet of tabs: opening each tab that is not the selected one, enabled and visible,
     * as {@code open-tab:Label}.
     *
     * @param tabs     the tabs in the order the user sees them
     * @param selected the tab that is open
     * @param select   selects a tab (as a click on it does)
     */
    static Map<String, LLMInteraction> tabs(List<Tab> tabs, Tab selected, Consumer<Tab> select) {
        Map<String, LLMInteraction> interactions = new LinkedHashMap<>();
        for (Tab tab : tabs) {
            if (tab != selected && tab.isEnabled() && tab.isVisible()) {
                interactions.put("open-tab:" + tab.getLabel(),
                        new LLMInteraction("Open the tab \"" + tab.getLabel() + "\".", () -> select.accept(tab)));
            }
        }
        return interactions;
    }

    /**
     * The interactions for buttons that open a dialog (Add book, Add model): pressing each as {@code
     * open-dialog:Label}, the label being the text of the button. A dialog only opens, nothing is created until the
     * user confirms in it. A button is offered only when it is shown and enabled, and no dialog is open (the page is
     * blocked by it; opening one on top of another would stack them).
     *
     * @param buttons the buttons, null ones are skipped
     */
    static Map<String, LLMInteraction> dialogButtons(Button... buttons) {
        Map<String, LLMInteraction> interactions = new LinkedHashMap<>();
        if (dialogIsOpen()) {
            return interactions;
        }
        for (Button button : buttons) {
            if (button != null && button.isVisible() && button.isEnabled() && !button.getText().isBlank()) {
                interactions.put("open-dialog:" + button.getText().strip(),
                        new LLMInteraction("Press the button \"" + button.getText().strip() + "\", which opens a dialog.",
                                () -> {
                                    button.click();
                                    attachPendingComponents();
                                }));
            }
        }
        return interactions;
    }

    /**
     * A dialog is put into the page only when the response is about to be sent; so that what an interaction opened is
     * there when the screen is read right after it (and is seen as open by the next interaction), the work that waits
     * for the response is done now.
     */
    private static void attachPendingComponents() {
        UI ui = UI.getCurrent();
        if (ui != null && ui.getSession() != null) {
            ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        }
    }

    /** Whether a dialog is open on the page of the current request. */
    static boolean dialogIsOpen() {
        UI ui = UI.getCurrent();
        return ui != null && ui.getChildren().anyMatch(child -> child instanceof Dialog dialog && dialog.isOpened());
    }

    /**
     * The element with the elements of the component added to its children: for a part of the workspace or a dialog,
     * which is not a component itself, while the components it created are in the tree of the window.
     */
    static LLMInteract within(LLMInteract element, Component container) {
        if (container == null) {
            return element;
        }
        return new LLMInteract() {
            @Override
            public String getLLMName() {
                return element.getLLMName();
            }

            @Override
            public String getLLMState() {
                return element.getLLMState();
            }

            @Override
            public Map<String, LLMInteraction> getInteraction() {
                return element.getInteraction();
            }

            @Override
            public List<String> getLLMProblems() {
                List<String> problems = new ArrayList<>(element.getLLMProblems());
                problems.addAll(LLMFormProblems.scan(container));
                return problems;
            }

            @Override
            public List<LLMInteract> getLLMChildren(boolean deep) {
                List<LLMInteract> children = new ArrayList<>(element.getLLMChildren(deep));
                children.addAll(LLMSnapshot.nearestDescendants(container, deep));
                return children;
            }
        };
    }
}
