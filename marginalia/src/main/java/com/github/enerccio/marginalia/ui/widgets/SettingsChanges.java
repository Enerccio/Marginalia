package com.github.enerccio.marginalia.ui.widgets;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;

import java.util.Optional;

/**
 * Tells the page that holds a settings form that something changed which is not a value change of a field it listens to: a row added to
 * or removed from a list, a button that clears a stored secret. The settings page ({@code UserPart}) {@link #listen listens} on its layout;
 * a form calls {@link #changed} with one of its components and the nearest listening ancestor marks the page as changed (it also starts to
 * track the fields that were added since). Outside of such a page nothing happens.
 */
public final class SettingsChanges {

    private static final String KEY = SettingsChanges.class.getName() + ".listener";

    private SettingsChanges() {
    }

    /** Makes {@code root} the receiver of the changes reported by the components inside it. */
    public static void listen(Component root, Runnable onChange) {
        ComponentUtil.setData(root, KEY, onChange);
    }

    /** Reports a change made in {@code source} to the nearest listening ancestor (or {@code source} itself). */
    public static void changed(Component source) {
        Optional<Component> current = Optional.ofNullable(source);
        while (current.isPresent()) {
            Object listener = ComponentUtil.getData(current.get(), KEY);
            if (listener instanceof Runnable runnable) {
                runnable.run();
                return;
            }
            current = current.get().getParent();
        }
    }
}
