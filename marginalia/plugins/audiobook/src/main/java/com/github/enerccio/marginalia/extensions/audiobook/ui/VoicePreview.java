package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.util.function.Supplier;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * A button that has a short sentence spoken with a voice and plays it, so a voice is heard before it is used. The speech server is started for it when the
 * requests before a batch do that, so it takes a while; the button is off until the sentence is there.
 */
public class VoicePreview extends Button {

    private final AudiobookService service;
    private final Supplier<String> voice;
    private final Supplier<String> language;
    private final Element audio = new Element("audio");

    /**
     * @param voice    the voice as text ({@code clip:<uuid>}, {@code predefined:<file>}), empty for none
     * @param language the code of the language the sentence is in, null for the language of the book
     */
    public VoicePreview(AudiobookService service, Supplier<String> voice, Supplier<String> language) {
        super(T.localized(PREVIEW), Solid.VOLUME_HIGH.create());
        this.service = service;
        this.voice = voice;
        this.language = language;
        setThemeName("tertiary small");
        getElement().appendChild(audio);
        addClickListener(e -> start());
    }

    private void start() {
        String chosen = voice.get();
        if (chosen == null || chosen.isBlank()) {
            Notification.error(T.localized(PICK_A_VOICE_FIRST));
            return;
        }
        UI ui = UI.getCurrent();
        setEnabled(false);
        setText(T.localized(MAKING_THE_SAMPLE));
        service.preview(chosen, language.get(), (bytes, error) -> {
            try {
                ui.access(() -> {
                    setEnabled(true);
                    setText(T.localized(PREVIEW));
                    if (error != null) {
                        Notification.error(T.localized(THE_VOICE_COULD_NOT_BE_HEARD).formatted(error));
                        return;
                    }
                    StreamResource resource = new StreamResource("preview-" + System.nanoTime() + ".mp3", () -> new ByteArrayInputStream(bytes));
                    resource.setContentType("audio/mpeg");
                    audio.setAttribute("src", resource);
                    audio.callJsFunction("play");
                });
            } catch (RuntimeException e) {
                // the page is gone
            }
        });
    }
}
