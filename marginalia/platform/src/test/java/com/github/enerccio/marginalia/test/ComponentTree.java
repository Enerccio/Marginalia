package com.github.enerccio.marginalia.test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;

import java.util.stream.Stream;

/**
 * Finds components of a built view without a browser.
 */
public final class ComponentTree {

    private ComponentTree() {
    }

    /**
     * The component and everything below it, the footer of a dialog included (it is not among its children).
     */
    public static Stream<Component> all(Component root) {
        Stream<Component> children = root.getChildren();
        if (root instanceof Dialog dialog) {
            children = Stream.concat(children, dialog.getFooter().getElement().getChildren()
                    .map(e -> e.getComponent().orElseThrow()));
        }
        return Stream.concat(Stream.of(root), children.flatMap(ComponentTree::all));
    }

    public static <T extends Component> Stream<T> all(Component root, Class<T> type) {
        return all(root).filter(type::isInstance).map(type::cast);
    }

    /**
     * The button with the text (the text, not the tooltip).
     */
    public static Button button(Component root, String text) {
        return all(root, Button.class).filter(b -> text.equals(b.getText())).findFirst()
                .orElseThrow(() -> new AssertionError("No button " + text));
    }
}
