package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.audiobook.audio.AudioKind;
import com.github.enerccio.marginalia.extensions.audiobook.label.Delivery;
import com.github.enerccio.marginalia.extensions.audiobook.label.Tags;
import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.audiobook.script.VoiceRef;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.function.Supplier;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * A button that has a sample spoken with a voice and plays it, so a voice is heard before it is used, and a menu of other samples: with sounds
 * (for the models that voice them) and in a mood (Higgs Audio v3). The first sample takes a while when the GPU holds something else (the GPU lock
 * loads the speech server first); the next ones are quick. The button is off until the sample is there. For a voice that is a reference clip, a
 * second button plays the clip itself as it is stored (no synthesis).
 */
public class VoicePreview extends Composite<HorizontalLayout> {

    private final AudiobookService service;
    private final Supplier<String> voice;
    private final Supplier<String> language;
    private final Element audio = new Element("audio");
    private final Button button = new Button(T.localized(PREVIEW), Solid.VOLUME_HIGH.create());
    private final MenuBar more = new MenuBar();
    private final Element clipAudio = new Element("audio");
    private final Button listen = new Button(Solid.FILE_AUDIO.create());

    /**
     * @param voice    the voice as text ({@code clip:<uuid>}, {@code predefined:<file>}), empty for none
     * @param language the code of the language the sentence is in, null for the language of the book
     */
    public VoicePreview(AudiobookService service, Supplier<String> voice, Supplier<String> language) {
        this.service = service;
        this.voice = voice;
        this.language = language;
        button.setThemeName("tertiary small");
        button.getElement().appendChild(audio);
        button.addClickListener(e -> start(false, null));
        more.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE, MenuBarVariant.LUMO_SMALL, MenuBarVariant.LUMO_ICON);
        HorizontalLayout layout = getContent();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        // the reference clip as it is stored: no synthesis, so it plays at once
        listen.setThemeName("tertiary small icon");
        listen.setTooltipText(T.localized(LISTEN_TO_THE_REFERENCE_CLIP));
        listen.setAriaLabel(T.localized(LISTEN_TO_THE_REFERENCE_CLIP));
        listen.getElement().appendChild(clipAudio);
        listen.addClickListener(e -> playClip());
        layout.add(button, more, listen);
        // the choices depend on the model of the backend, read when the field is shown
        addAttachListener(e -> {
            fillMenu();
            refresh();
        });
    }

    /** Shows the button of the reference clip only for a voice that is a clip; called when the voice changes. */
    public void refresh() {
        listen.setVisible(clipOf(voice.get()) != null);
    }

    /** The resource of the clip a voice speaks with (a shifted voice: its stored copy, else the clip it is made from); null for no clip. */
    private String clipOf(String chosen) {
        VoiceRef ref = chosen == null || chosen.isBlank() ? null : VoiceRef.parse(chosen);
        if (ref == null || ref.kind() != VoiceRef.Kind.CLIP) {
            return null;
        }
        if (ref.shift() != null) {
            String shifted = service.getSettings().shiftedClip(ref.id(), ref.shift().spec());
            if (shifted != null) {
                return shifted;
            }
        }
        return ref.id();
    }

    private void playClip() {
        String uuid = clipOf(voice.get());
        if (uuid == null) {
            Notification.warning(T.localized(THE_VOICE_IS_NOT_A_CLIP));
            return;
        }
        byte[] bytes = service.clipBytes(uuid);
        if (bytes == null) {
            Notification.error(T.localized(THE_VOICE_COULD_NOT_BE_HEARD).formatted("?"));
            return;
        }
        AudioKind kind = AudioKind.of(bytes);
        StreamResource resource = new StreamResource("clip-" + System.nanoTime() + "." + kind.extension(), () -> new ByteArrayInputStream(bytes));
        resource.setContentType(kind.mime());
        audio.callJsFunction("pause");
        clipAudio.setAttribute("src", resource);
        clipAudio.callJsFunction("play");
    }

    /**
     * Loads the speech server on its GPU when the dialog opens (see {@link AudiobookService#warmUp}), so the first sample does not wait for it.
     */
    public static void warmUpOnOpen(Dialog dialog, AudiobookService service) {
        boolean[] done = new boolean[1];
        dialog.addAttachListener(e -> {
            if (!done[0]) {
                done[0] = true;
                service.warmUp();
            }
        });
    }

    private void fillMenu() {
        more.removeAll();
        String model = service.sampleModel(language.get());
        boolean sounds = model != null && !Tags.forModel(model).isEmpty();
        boolean moods = BackendConfig.MODEL_HIGGS.equals(model);
        more.setVisible(sounds || moods);
        if (!sounds && !moods) {
            return;
        }
        MenuItem root = more.addItem(Solid.CHEVRON_DOWN.create());
        root.setAriaLabel(T.localized(OTHER_SAMPLES));
        more.setTooltipText(root, T.localized(OTHER_SAMPLES));
        SubMenu menu = root.getSubMenu();
        menu.addItem(T.localized(READ_PLAINLY), e -> start(false, null));
        if (sounds) {
            menu.addItem(T.localized(WITH_SOUNDS), e -> start(true, null));
        }
        if (moods) {
            Arrays.stream(Delivery.values()).filter(d -> d != Delivery.NEUTRAL)
                    .forEach(d -> menu.addItem(T.localized(MOOD_NAME).formatted(d.label()), e -> start(false, d)));
        }
    }

    private void start(boolean sounds, Delivery mood) {
        String chosen = voice.get();
        if (chosen == null || chosen.isBlank()) {
            Notification.error(T.localized(PICK_A_VOICE_FIRST));
            return;
        }
        UI ui = UI.getCurrent();
        button.setEnabled(false);
        more.setEnabled(false);
        button.setText(T.localized(MAKING_THE_SAMPLE));
        service.preview(chosen, language.get(), sounds, mood, (bytes, error) -> {
            try {
                ui.access(() -> {
                    button.setEnabled(true);
                    more.setEnabled(true);
                    button.setText(T.localized(PREVIEW));
                    if (error != null) {
                        Notification.error(T.localized(THE_VOICE_COULD_NOT_BE_HEARD).formatted(error));
                        return;
                    }
                    StreamResource resource = new StreamResource("preview-" + System.nanoTime() + ".mp3", () -> new ByteArrayInputStream(bytes));
                    resource.setContentType("audio/mpeg");
                    clipAudio.callJsFunction("pause");
                    audio.setAttribute("src", resource);
                    audio.callJsFunction("play");
                });
            } catch (RuntimeException e) {
                // the page is gone
            }
        });
    }
}
