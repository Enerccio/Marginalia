package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.audiobook.audio.AudioKind;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * The controls of the audiobook in the viewer: play / pause, stop, the speed. The page is given the address of the audio of every part that
 * has some (each a stream resource of this page, read for the user who asks, like the images of the parts); {@code audiobook-player.js} does the
 * playing in the browser: it fetches the audio of a part into memory (so that it plays also where the browser would not play a stream without
 * range requests), plays the parts in order, marks the one that is heard, loads the next one while it plays and answers the media keys of the device.
 */
public class AudioPlayer extends Div {

    private final Component scroller;
    private final Component content;
    private final String title;
    private final Map<Long, String> audio;
    private final AudiobookService service;
    private final List<StreamRegistration> registrations = new ArrayList<>();

    private final Button play = button(Solid.HEADPHONES, LISTEN_TO_THE_AUDIOBOOK, AudiobookStyles.PLAYER_PLAY);
    private final Button pause = button(Solid.PAUSE, PAUSE_LISTENING, AudiobookStyles.PLAYER_PAUSE);
    private final Button stop = button(Solid.STOP, STOP, AudiobookStyles.PLAYER_STOP);
    private final Element speed = new Element("input").setAttribute("type", "range").setAttribute("min", "0.75")
            .setAttribute("max", "2").setAttribute("step", "0.05").setAttribute("title", T.localized(SPEED));
    private final Button offer = button(Solid.VOLUME_HIGH, READ_THIS_PART_WITH_THE_BROWSER_VOICE, AudiobookStyles.PLAYER_OFFER);
    private final Span status = new Span();

    /**
     * @param scroller the scrolling panel of the reader
     * @param content  the layout whose children are the parts (the elements with {@code data-message-id})
     * @param title    the title of the book, for the media controls of the device
     * @param audio    the resource of the audio of each part that has some, by the id of the part ({@code PlayableParts.of})
     */
    public AudioPlayer(Component scroller, Component content, String title, Map<Long, String> audio, AudiobookService service) {
        this.scroller = scroller;
        this.content = content;
        this.title = title == null ? "" : title;
        this.audio = audio;
        this.service = service;
        addClassName(AudiobookStyles.PLAYER);
        status.addClassName(AudiobookStyles.PLAYER_STATUS);
        speed.getClassList().add(AudiobookStyles.PLAYER_SPEED);
        getElement().appendChild(speed);
        add(play, pause, stop, status, offer);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        JsonObject urls = new JsonObject();
        UI ui = attachEvent.getUI();
        for (Map.Entry<Long, String> entry : audio.entrySet()) {
            String resource = entry.getValue();
            // the audio of a part is mp3 or wav, depending on the storage of the installation when it was made
            AudioKind kind = service.audioKindOf(resource);
            StreamResource stream = new StreamResource("part-" + entry.getKey() + "." + kind.extension(), () -> {
                byte[] bytes = service.audioBytes(resource);
                return new ByteArrayInputStream(bytes == null ? new byte[0] : bytes);
            });
            stream.setContentType(kind.mime());
            StreamRegistration registration = ui.getSession().getResourceRegistry().registerResource(stream);
            registrations.add(registration);
            urls.addProperty(String.valueOf(entry.getKey()), registration.getResourceUri().toString());
        }
        getElement().executeJs(AudiobookStyles.playerScript(), getElement(), scroller.getElement(), content.getElement(), play.getElement(),
                pause.getElement(), stop.getElement(), speed, status.getElement(), urls.toString(), title,
                T.localized(THIS_PART_HAS_NO_AUDIO), T.localized(THE_AUDIO_COULD_NOT_BE_LOADED), offer.getElement());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        // leaving the book silences it, the audio belongs to the page
        detachEvent.getUI().getPage().executeJs("if (document.getElementById('audiobook-audio')) { document.getElementById('audiobook-audio').pause(); }");
        registrations.forEach(StreamRegistration::unregister);
        registrations.clear();
    }

    private static Button button(Solid icon, String label, String cssClass) {
        Button button = new Button(icon.create());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        button.setTooltipText(T.localized(label));
        button.getElement().setAttribute("aria-label", T.localized(label));
        button.addClassName(cssClass);
        return button;
    }
}
