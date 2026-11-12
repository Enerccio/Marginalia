package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.CssStyles;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.react.ReactAdapterComponent;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A Markdown editor with a toolbar and a preview (the React editor {@code @uiw/react-md-editor}, set up in
 * {@code frontend/markdown-editor.tsx}): a toolbar without links, image markup, comments, help and full screen, the modes
 * edit / edit with preview / preview, and a sanitized preview. Tab moves the focus out of it.
 * <p>
 * The text comes to the server after a pause in typing and when the editor loses the focus, not with every key, so a
 * {@linkplain #addValueChangeListener listener} may save on every change. The listeners hear only of the changes the user
 * made, not of {@link #setValue}.
 */
@Tag("marginalia-markdown-editor")
@JsModule("./markdown-editor.tsx")
@NpmPackage(value = "@uiw/react-md-editor", version = "4.1.2")
@NpmPackage(value = "rehype-sanitize", version = "6.0.0")
public class MarkdownEditor extends ReactAdapterComponent implements HasSize, HasStyle {

    /**
     * What the editor shows: the text, the text with the preview next to it, or the preview.
     */
    public enum Mode {
        EDIT("edit"), LIVE("live"), PREVIEW("preview");

        private final String key;
        Mode(String key) {
            this.key = key;
        }

        /** The mode of a key the browser sent, {@code null} for an unknown one. */
        public static Mode of(String key) {
            for (Mode mode : values()) {
                if (mode.key.equals(key)) {
                    return mode;
                }
            }
            return null;
        }
    }

    private final List<SerializableConsumer<String>> listeners = new ArrayList<>();
    private final List<SerializableConsumer<Mode>> modeListeners = new ArrayList<>();
    private String value = "";
    private Mode mode = Mode.LIVE;

    public MarkdownEditor() {
        addClassName(CssStyles.MARKDOWN_EDITOR);
        setState("value", value);
        setState("preview", Mode.LIVE.key);
        // the state listener hears the changes of the server too: those are already in value
        addStateChangeListener("value", String.class, newValue -> {
            String changed = newValue == null ? "" : newValue;
            if (Objects.equals(changed, value)) {
                return;
            }
            value = changed;
            new ArrayList<>(listeners).forEach(listener -> listener.accept(changed));
        });
        addStateChangeListener("preview", String.class, key -> {
            Mode picked = Mode.of(key);
            if (picked == null || picked == mode) {
                return;
            }
            mode = picked;
            new ArrayList<>(modeListeners).forEach(listener -> listener.accept(picked));
        });
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value == null ? "" : value;
        setState("value", this.value);
    }

    public void setPlaceholder(String placeholder) {
        setState("placeholder", placeholder);
    }

    public void setReadOnly(boolean readOnly) {
        setState("readOnly", readOnly);
    }

    public void setMode(Mode mode) {
        this.mode = mode == null ? Mode.LIVE : mode;
        setState("preview", this.mode.key);
    }

    public Mode getMode() {
        return mode;
    }

    /**
     * Told of the mode after the user picked another one with the buttons of the editor (not of {@link #setMode}).
     */
    public Registration addModeChangeListener(SerializableConsumer<Mode> listener) {
        modeListeners.add(listener);
        return () -> modeListeners.remove(listener);
    }

    /**
     * Told of the text after the user changed it.
     */
    public Registration addValueChangeListener(SerializableConsumer<String> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }
}
