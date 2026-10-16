package com.github.enerccio.marginalia.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The state of an element of the UI for the help assistant's model, see {@link LLMInteract}: a tree of nodes.
 *
 * @param path         logical path of the node: the names of the elements from the root, separated by {@code /}
 * @param state        Markdown text of the state of the element itself, empty when there is nothing to say
 * @param children     the elements inside it
 * @param interactions what the element offers ({@link LLMInteract#getInteraction()}): the key and what it does
 * @param problems     what is wrong with the form the element shows now ({@link LLMInteract#getLLMProblems()})
 * @param element      the live element the node describes, to apply an interaction to; null for a node that was not
 *                     taken from one. Only valid on the thread of the page, and while the UI is as it was taken.
 */
public record LLMSnapshot(String path, String state, List<LLMSnapshot> children, Map<String, String> interactions,
                          List<String> problems, LLMInteract element) {
    private static final Logger log = LoggerFactory.getLogger(LLMSnapshot.class);

    /** A deeper tree is cut (a cycle in the children of an element must not hang the request). */
    static final int MAX_DEPTH = 24;

    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    public LLMSnapshot {
        state = state == null ? "" : state.strip();
        children = children == null ? List.of() : List.copyOf(children);
        interactions = interactions == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(interactions));
        problems = problems == null ? List.of() : List.copyOf(problems);
    }

    /** A node without problems. */
    public LLMSnapshot(String path, String state, List<LLMSnapshot> children, Map<String, String> interactions,
                       LLMInteract element) {
        this(path, state, children, interactions, List.of(), element);
    }

    /** A node that offers nothing to do. */
    public LLMSnapshot(String path, String state, List<LLMSnapshot> children) {
        this(path, state, children, Map.of(), null);
    }

    /**
     * The snapshot of the element, as {@link LLMInteract#takeLLMSnapshot(boolean)} builds it by default. An element that
     * fails to describe itself is listed with an error note instead of breaking the whole tree.
     */
    public static LLMSnapshot take(LLMInteract element, boolean deep) {
        String name = safe(element::getLLMName, element.getClass().getSimpleName());
        String state = safe(element::getLLMState, "(the state could not be read)");
        List<LLMSnapshot> children = new ArrayList<>();
        int depth = DEPTH.get();
        if (depth < MAX_DEPTH) {
            DEPTH.set(depth + 1);
            try {
                List<LLMInteract> elements = safe(() -> element.getLLMChildren(deep), List.<LLMInteract>of());
                Map<String, Integer> total = new HashMap<>();
                List<LLMSnapshot> taken = new ArrayList<>();
                for (LLMInteract child : elements) {
                    LLMSnapshot snapshot = safe(() -> child.takeLLMSnapshot(deep), null);
                    if (snapshot != null) {
                        taken.add(snapshot);
                        total.merge(snapshot.path(), 1, Integer::sum);
                    }
                }
                Map<String, Integer> seen = new HashMap<>();
                for (LLMSnapshot snapshot : taken) {
                    String childName = snapshot.path();
                    LLMSnapshot node = snapshot;
                    if (total.get(childName) > 1) {
                        node = snapshot.reroot(childName, childName + "[" + seen.merge(childName, 1, Integer::sum) + "]");
                    }
                    children.add(node.under(name));
                }
            } finally {
                DEPTH.set(depth);
            }
        }
        Map<String, String> interactions = new LinkedHashMap<>();
        safe(element::getInteraction, Map.<String, LLMInteraction>of())
                .forEach((key, interaction) -> interactions.put(key, interaction.description()));
        List<String> problems = safe(element::getLLMProblems, List.<String>of());
        return new LLMSnapshot(name, state, children, interactions, problems, element);
    }

    /**
     * A snapshot of a component tree that is not an element itself (the page): a node with the given name and state
     * whose children are the nearest visible elements below the component.
     */
    public static LLMSnapshot ofComponentTree(String name, String state, Component root, boolean deep) {
        return take(new LLMInteract() {
            @Override
            public String getLLMName() {
                return name;
            }

            @Override
            public String getLLMState() {
                return state;
            }

            @Override
            public List<LLMInteract> getLLMChildren(boolean deep) {
                return nearestDescendants(root, deep);
            }
        }, deep);
    }

    /**
     * The nearest descendants of the component that are {@link LLMInteract}; the search does not go below an element
     * (it reports its own children). Invisible components (and dialogs that are not open) and what is inside them are
     * skipped, or, when {@code deep}, listed with a note that they are hidden.
     */
    public static List<LLMInteract> nearestDescendants(Component root, boolean deep) {
        List<LLMInteract> found = new ArrayList<>();
        root.getChildren().forEach(child -> collect(child, deep, false, found));
        return found;
    }

    private static void collect(Component component, boolean deep, boolean hidden, List<LLMInteract> found) {
        // a dialog that is not open shows nothing (it is removed from the page when it closes, but may still be there)
        boolean hiddenHere = hidden || isHidden(component);
        if (hiddenHere && !deep) {
            return;
        }
        if (component instanceof LLMInteract element) {
            // the search stops at an element, so this is the topmost hidden one: what is inside it is hidden with it
            found.add(hiddenHere ? hiddenElement(element) : element);
        } else {
            component.getChildren().forEach(child -> collect(child, deep, hiddenHere, found));
        }
    }

    /** Whether the component is not shown: invisible, or a dialog that is not open (it may still be in the tree). */
    static boolean isHidden(Component component) {
        return !isShown(component) || (component instanceof Dialog dialog && !dialog.isOpened());
    }

    /** Whether the component is visible; a {@code Text} (a bare text node) has no visibility and is never hidden on its own. */
    private static boolean isShown(Component component) {
        try {
            return component.isVisible();
        } catch (UnsupportedOperationException e) {
            return true;
        }
    }

    private static LLMInteract hiddenElement(LLMInteract element) {
        return new LLMInteract() {
            @Override
            public String getLLMName() {
                return element.getLLMName();
            }

            @Override
            public String getLLMState() {
                return "*Hidden: not shown at the moment.*\n\n" + element.getLLMState();
            }

            @Override
            public List<LLMInteract> getLLMChildren(boolean deep) {
                return element.getLLMChildren(deep);
            }
        };
    }

    /**
     * The state of a component that says nothing itself: its title when it is a dialog.
     */
    public static String describe(Component component) {
        if (component instanceof Dialog dialog) {
            String title = dialog.getHeaderTitle();
            return title == null || title.isBlank() ? "A dialog is open." : "A dialog is open, titled \"" + title + "\".";
        }
        return "";
    }

    /**
     * The node whose path is {@code wanted} (the root when empty), null when there is none. Case and a leading or
     * trailing slash do not matter.
     */
    public Optional<LLMSnapshot> find(String wanted) {
        String normalized = wanted == null ? "" : wanted.strip();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.isEmpty()) {
            return Optional.of(this);
        }
        return findExact(normalized);
    }

    private Optional<LLMSnapshot> findExact(String wanted) {
        if (path.equalsIgnoreCase(wanted)) {
            return Optional.of(this);
        }
        // the paths of the descendants continue this path, so only the branch that shares its prefix can match
        for (LLMSnapshot child : children) {
            Optional<LLMSnapshot> found = child.findExact(wanted);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    /**
     * The text the model gets for a path: the node and everything below it as Markdown, cut at {@code maxChars}.
     *
     * @throws IllegalArgumentException when there is no such node; the message lists the paths there are, for the
     *                                  model to choose from
     */
    public String read(String wanted, int maxChars) {
        return read(wanted, maxChars, false);
    }

    /**
     * Like {@link #read(String, int)}; {@code withInteractions} adds what each element offers (see {@link
     * #toMarkdown(boolean)}).
     */
    public String read(String wanted, int maxChars, boolean withInteractions) {
        LLMSnapshot node = find(wanted).orElseThrow(() -> new IllegalArgumentException(
                "There is no element \"" + wanted + "\" in what was read. The elements are:\n" + toOutline()
                        + "\nAn element that is not on the screen is only found with deep=true."));
        String text = node.toMarkdown(withInteractions);
        if (text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars).stripTrailing() + "\n\n[...cut. Read a path below this one for the rest:]\n" + node.toOutline();
    }

    /**
     * The tree as Markdown: a heading with the path for each node (deeper nodes get deeper headings), then its state.
     * Compact on purpose, it is read by a model.
     */
    public String toMarkdown() {
        return toMarkdown(false);
    }

    /**
     * The tree as Markdown; with {@code withInteractions} each node that offers some lists them (key and what it does)
     * under its state, for the model to apply with {@link #interact}.
     */
    public String toMarkdown(boolean withInteractions) {
        StringBuilder sb = new StringBuilder();
        render(sb, 2, withInteractions);
        return sb.toString().strip();
    }

    private void render(StringBuilder sb, int level, boolean withInteractions) {
        sb.append("#".repeat(Math.min(level, 6))).append(' ').append(path).append('\n');
        if (!state.isEmpty()) {
            sb.append(state).append('\n');
        }
        if (!problems.isEmpty()) {
            sb.append("\nProblems with the form:\n");
            problems.forEach(problem -> sb.append("- ").append(problem).append('\n'));
        }
        if (withInteractions && !interactions.isEmpty()) {
            sb.append("\nInteractions:\n");
            interactions.forEach((key, description) -> sb.append("- `").append(key).append("`: ").append(description).append('\n'));
        }
        sb.append('\n');
        children.forEach(child -> child.render(sb, level + 1, withInteractions));
    }

    /**
     * Applies interactions, in order, to the element at the path (see {@link LLMInteract#applyInteraction(List)}) and
     * returns the snapshot of the element after them. Must run on the thread of the page.
     *
     * @throws IllegalArgumentException when there is no element with the path, it offers no interactions, or one of
     *                                  the interactions is not offered; the message says what there is
     */
    public LLMSnapshot interact(String wanted, List<String> keys) {
        LLMSnapshot node = find(wanted).orElseThrow(() -> new IllegalArgumentException(
                "There is no element \"" + wanted + "\" on the screen. Elements with interactions:\n" + toInteractionOutline()));
        if (node.element == null || node.interactions.isEmpty()) {
            throw new IllegalArgumentException("The element \"" + node.path + "\" offers no interactions. Elements with interactions:\n"
                    + toInteractionOutline());
        }
        return node.element.applyInteraction(keys);
    }

    /** The paths of the nodes that offer interactions, each with their keys; for a model that asked for the wrong one. */
    public String toInteractionOutline() {
        StringBuilder sb = new StringBuilder();
        interactionOutline(sb);
        return sb.length() == 0 ? "(none)" : sb.toString().strip();
    }

    private void interactionOutline(StringBuilder sb) {
        if (!interactions.isEmpty()) {
            sb.append("- ").append(path).append(": ").append(String.join(", ", interactions.keySet())).append('\n');
        }
        children.forEach(child -> child.interactionOutline(sb));
    }

    /** The paths of this node and of everything below it, indented, for a model that asked for a path that is not there. */
    public String toOutline() {
        StringBuilder sb = new StringBuilder();
        outline(sb, 0);
        return sb.toString().strip();
    }

    private void outline(StringBuilder sb, int indent) {
        sb.append("  ".repeat(indent)).append("- ").append(path).append('\n');
        children.forEach(child -> child.outline(sb, indent + 1));
    }

    /** The node and its descendants with {@code parent/} in front of every path. */
    LLMSnapshot under(String parent) {
        return new LLMSnapshot(parent + "/" + path, state, children.stream().map(child -> child.under(parent)).toList(),
                interactions, problems, element);
    }

    /** The node and its descendants with the path prefix {@code from} replaced by {@code to}. */
    private LLMSnapshot reroot(String from, String to) {
        String rewritten = path.equals(from) || path.startsWith(from + "/") ? to + path.substring(from.length()) : path;
        return new LLMSnapshot(rewritten, state, children.stream().map(child -> child.reroot(from, to)).toList(),
                interactions, problems, element);
    }

    private static <T> T safe(Supplier<T> supplier, T fallback) {
        try {
            return supplier.get();
        } catch (RuntimeException e) {
            log.warn("An element of the UI could not be described for the help assistant", e);
            return fallback;
        }
    }
}
