package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.react.ReactAdapterComponent;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Markdown editor with a toolbar and a preview (the React editor {@code @uiw/react-md-editor}, set up in
 * {@code frontend/markdown-editor.tsx}): a toolbar without links, image markup, comments, help and full screen, the modes
 * edit / edit with preview / preview, and a sanitized preview. Tab moves the focus out of it.
 * <p>
 * The text comes to the server after a pause in typing and when the editor loses the focus, not with every key, so a
 * {@linkplain #addValueChangeListener listener} may save on every change. The listeners hear only of the changes the user
 * made, not of {@link #setValue}.
 * <p>
 * Ctrl+click (Cmd+click) in the text or in the preview tells the {@linkplain #addLinkClickListener link listeners} which
 * text was clicked and where; what a link is there is up to them.
 * <p>
 * Images: with an {@linkplain #setImageUploader uploader} the toolbar has an image button and images can be pasted or dropped
 * into the text; the uploader stores the image and gives the Markdown that is put in at the cursor. An image whose address is a
 * resource ({@code ![caption](resource:<uuid>)}) is shown in the preview when an {@linkplain #setImageSource image source} gives its
 * content.
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
    private final List<SerializableConsumer<LinkClick>> linkListeners = new ArrayList<>();
    private String value = "";
    private Mode mode = Mode.LIVE;
    private Map<String, String> labels = Map.of();
    private ImageUploader imageUploader;
    private Function<String, AbstractStreamResource> imageSource;
    private SerializableConsumer<String> onUploadFailed;
    // the addresses of the images the preview shows, by the uuid of the resource
    private final Map<String, StreamRegistration> imageRegistrations = new HashMap<>();

    private static final Pattern RESOURCE_IMAGE = Pattern.compile(
            "resource:([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})");

    /**
     * Stores an image the user put into the editor.
     */
    @FunctionalInterface
    public interface ImageUploader {
        /**
         * @param fileName the name of the file (a pasted image has a made-up one)
         * @param content  the bytes as the browser sent them
         * @return the Markdown to put in at the cursor
         * @throws IllegalArgumentException when the content is not an image that can be kept
         */
        String upload(String fileName, byte[] content) throws Exception;
    }

    public MarkdownEditor() {
        addClassName(CssStyles.MARKDOWN_EDITOR);
        setState("value", value);
        setState("preview", Mode.LIVE.key);
        // the state listener hears the changes of the server too: those are already in value
        addStateChangeListener("value", String.class, this::changedInBrowser);
        addStateChangeListener("preview", String.class, key -> {
            Mode picked = Mode.of(key);
            if (picked == null || picked == mode) {
                return;
            }
            mode = picked;
            new ArrayList<>(modeListeners).forEach(listener -> listener.accept(picked));
        });
    }

    private void changedInBrowser(String newValue) {
        String changed = newValue == null ? "" : newValue;
        if (Objects.equals(changed, value)) {
            return;
        }
        value = changed;
        showImages();
        new ArrayList<>(listeners).forEach(listener -> listener.accept(changed));
    }

    /**
     * A Ctrl+click on a text of the editor.
     *
     * @param text     the clicked text: the whole text when the click was in the text, the text of the clicked piece of the preview
     * @param position the position of the click in {@code text}
     * @param newTab   Shift was held too
     */
    public record LinkClick(String text, int position, boolean newTab) {
    }

    /**
     * From the browser: a Ctrl+click on a text, with the text typed since it was last sent (null when nothing waits), taken first
     * as the user's change.
     */
    @ClientCallable
    void linkClicked(String typed, String text, int position, boolean newTab) {
        if (typed != null) {
            changedInBrowser(typed);
        }
        LinkClick click = new LinkClick(text == null ? "" : text, position, newTab);
        new ArrayList<>(linkListeners).forEach(listener -> listener.accept(click));
    }

    /**
     * Told of a Ctrl+click (Cmd+click) in the text or the preview.
     */
    public Registration addLinkClickListener(SerializableConsumer<LinkClick> listener) {
        linkListeners.add(listener);
        return () -> linkListeners.remove(listener);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value == null ? "" : value;
        setState("value", this.value);
        showImages();
    }

    /**
     * Lets the user add images (the image button, paste, drop); null takes it away.
     */
    public void setImageUploader(ImageUploader uploader) {
        this.imageUploader = uploader;
        setState("images", uploader != null);
    }

    /**
     * Told the reason when an image the user added could not be kept (the message of the uploader's exception is not shown: it is
     * the caller's to word).
     */
    public void setOnUploadFailed(SerializableConsumer<String> onUploadFailed) {
        this.onUploadFailed = onUploadFailed;
    }

    /**
     * The content of the images of resources the text shows ({@code resource:<uuid>}), by uuid; null for an unknown one.
     */
    public void setImageSource(Function<String, AbstractStreamResource> imageSource) {
        this.imageSource = imageSource;
        showImages();
    }

    /**
     * From the browser: an image the user pasted, dropped or picked with the button, as base64.
     *
     * @return the Markdown to put in at the cursor, empty when the image was not kept
     */
    @ClientCallable
    String uploadImage(String fileName, String base64) {
        if (imageUploader == null || base64 == null) {
            return "";
        }
        try {
            byte[] content = Base64.getDecoder().decode(base64);
            String markup = imageUploader.upload(fileName == null ? "image" : fileName, content);
            return markup == null ? "" : markup;
        } catch (Exception e) {
            if (onUploadFailed != null) {
                onUploadFailed.accept(fileName);
            }
            return "";
        }
    }

    /**
     * Gives the browser the addresses of the images the text shows (each registered once, until the editor is detached).
     */
    private void showImages() {
        if (imageSource == null) {
            return;
        }
        VaadinSession session = VaadinSession.getCurrent();
        Map<String, String> addresses = new LinkedHashMap<>();
        Matcher matcher = RESOURCE_IMAGE.matcher(value);
        while (matcher.find()) {
            String uuid = matcher.group(1).toLowerCase();
            StreamRegistration registration = imageRegistrations.get(uuid);
            if (registration == null && session != null) {
                AbstractStreamResource resource = imageSource.apply(uuid);
                if (resource != null) {
                    registration = session.getResourceRegistry().registerResource(resource);
                    imageRegistrations.put(uuid, registration);
                }
            }
            if (registration != null) {
                addresses.put(uuid, registration.getResourceUri().toString());
            }
        }
        setState("imageUrls", addresses);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        showImages();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        imageRegistrations.values().forEach(StreamRegistration::unregister);
        imageRegistrations.clear();
        super.onDetach(detachEvent);
    }

    /** The addresses of the images the preview has (for the tests). */
    Map<String, StreamRegistration> getImageRegistrations() {
        return imageRegistrations;
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
     * The tooltips of the toolbar in the language of the page (the editor has English ones of its own), by the names of the
     * commands of the editor; a keyboard shortcut of a command is added to its tooltip in the browser.
     */
    public void localize(Localization loc) {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("bold", loc.getValue(L.LABEL_BOLD));
        labels.put("italic", loc.getValue(L.LABEL_ITALIC));
        labels.put("strikethrough", loc.getValue(L.LABEL_STRIKETHROUGH));
        labels.put("hr", loc.getValue(L.LABEL_HORIZONTAL_LINE));
        labels.put("title", loc.getValue(L.LABEL_HEADING));
        for (int level = 1; level <= 6; level++) {
            labels.put("heading" + level, String.format(loc.getValue(L.LABEL_HEADING_LEVEL), level));
        }
        labels.put("quote", loc.getValue(L.LABEL_QUOTE));
        labels.put("code", loc.getValue(L.LABEL_INLINE_CODE));
        labels.put("codeBlock", loc.getValue(L.LABEL_CODE_BLOCK));
        labels.put("table", loc.getValue(L.LABEL_TABLE));
        labels.put("unordered-list", loc.getValue(L.LABEL_BULLETED_LIST));
        labels.put("ordered-list", loc.getValue(L.LABEL_NUMBERED_LIST));
        labels.put("checked-list", loc.getValue(L.LABEL_CHECKLIST));
        labels.put("image", loc.getValue(L.LABEL_ADD_IMAGE));
        labels.put("edit", loc.getValue(L.LABEL_TEXT_ONLY));
        labels.put("live", loc.getValue(L.LABEL_TEXT_AND_PREVIEW));
        labels.put("preview", loc.getValue(L.LABEL_PREVIEW_ONLY));
        this.labels = labels;
        setState("labels", labels);
    }

    /** The tooltips given to {@link #localize}, by command name. */
    public Map<String, String> getLabels() {
        return labels;
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
