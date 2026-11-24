package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.extensions.audiobook.audio.AudioKind;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * Try a voice with a text of your own: it is spoken as it is typed by the backend in use (no labelling, no sound cues, the tags of the model reach
 * it), with a seed of your choice, and played when it is ready.
 */
public class VoiceTry extends VerticalLayout {

    private final AudiobookService service;
    private final TextArea text = new TextArea(T.localized(TEXT_TO_SPEAK));
    private final IntegerField seed = new IntegerField(T.localized(SEED));
    private final Button speak = new Button(T.localized(SPEAK), Solid.VOLUME_HIGH.create(), e -> speak());
    private final Element audio = new Element("audio");
    private final com.vaadin.flow.component.html.Div tags = new com.vaadin.flow.component.html.Div();
    private String voice;

    public VoiceTry(AudiobookService service) {
        this.service = service;
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        Span help = new Span(T.localized(TRY_THE_VOICE_HELP));
        help.addClassName(CssStyles.HINT_TEXT);
        text.setWidthFull();
        text.setMinRows(2);
        seed.setPlaceholder("42");
        seed.setWidth("8em");
        seed.setHelperText(T.localized(SEED_HELP));
        speak.setThemeName("small");
        audio.setAttribute("controls", true);
        audio.getClassList().add(AudiobookStyles.CONVERT_AUDIO);
        audio.setVisible(false);
        HorizontalLayout row = new HorizontalLayout(seed, speak);
        row.setAlignItems(FlexComponent.Alignment.BASELINE);
        Button info = new Button(Solid.CIRCLE_INFO.create(), e -> toggleTags());
        info.setThemeName("tertiary small");
        info.setTooltipText(T.localized(TAGS_OF_THE_MODEL));
        HorizontalLayout title = new HorizontalLayout(new H4(T.localized(TRY_THE_VOICE)), info);
        title.setAlignItems(FlexComponent.Alignment.CENTER);
        tags.setVisible(false);
        tags.setWidthFull();
        add(title, help, tags, text, row);
        getElement().appendChild(audio);
        DialogShortcuts.submitWith(speak, text, seed);
    }

    /** The voice the text is spoken with ({@code clip:<uuid>} or a voice of the server); null: none. */
    public void show(String voice) {
        this.voice = voice;
        audio.setVisible(false);
        audio.removeAttribute("src");
        speak.setEnabled(voice != null);
    }

    /** Shows (or hides) the tags the model of the backend in use reads, each a button that puts it at the end of the text. */
    private void toggleTags() {
        if (tags.isVisible()) {
            tags.setVisible(false);
            return;
        }
        tags.removeAll();
        com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig backend = service.selectedBackend();
        java.util.List<String> known = backend == null ? java.util.List.of()
                : com.github.enerccio.marginalia.extensions.audiobook.label.Tags.syntaxFor(backend.getModel());
        Span title = new Span(backend == null ? T.localized(NO_BACKEND_YET)
                : known.isEmpty() ? T.localized(THE_MODEL_READS_NO_TAGS).formatted(backend.getName())
                : T.localized(THE_TAGS_OF).formatted(backend.getName()));
        title.addClassName(CssStyles.HINT_TEXT);
        HorizontalLayout chips = new HorizontalLayout();
        chips.setWrap(true);
        chips.setSpacing(false);
        for (String tag : known) {
            Button chip = new Button(tag, e -> {
                String current = text.getValue();
                text.setValue(current.isEmpty() || current.endsWith(" ") ? current + tag : current + " " + tag);
                text.focus();
            });
            chip.setThemeName("tertiary small");
            chips.add(chip);
        }
        tags.add(title, chips);
        tags.setVisible(true);
    }

    private void speak() {
        if (voice == null) {
            return;
        }
        if (text.getValue().isBlank()) {
            Notification.warning(T.localized(TYPE_A_TEXT));
            return;
        }
        UI ui = UI.getCurrent();
        String spoken = voice;
        speak.setEnabled(false);
        speak.setText(T.localized(SPEAKING));
        service.speakTest(voice, text.getValue(), seed.getValue(), (bytes, error) -> ui.access(() -> {
            speak.setEnabled(true);
            speak.setText(T.localized(SPEAK));
            if (error != null || bytes == null) {
                Notification.error(T.localized(THE_VOICE_COULD_NOT_SPEAK).formatted(error));
            } else if (spoken.equals(voice)) {
                AudioKind kind = AudioKind.of(bytes);
                StreamResource resource = new StreamResource("try-" + System.nanoTime() + "." + kind.extension(), () -> new ByteArrayInputStream(bytes));
                resource.setContentType(kind.mime());
                audio.setAttribute("src", resource);
                audio.setVisible(true);
                audio.callJsFunction("play");
            }
            UIPushGuard.push(ui);
        }));
    }
}
