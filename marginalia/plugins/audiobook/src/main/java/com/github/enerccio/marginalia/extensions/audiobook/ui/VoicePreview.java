package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.audiobook.label.Delivery;
import com.github.enerccio.marginalia.extensions.audiobook.label.Tags;
import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
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
 * (for the models that voice them) and in a mood (Higgs Audio v3). The speech server is started for it when the requests before a batch do that, so
 * the first sample takes a while; a window that holds the server ({@link #holdWhileOpen}) makes the next ones quick. The button is off until the
 * sample is there.
 */
public class VoicePreview extends Composite<HorizontalLayout> {

    private final AudiobookService service;
    private final Supplier<String> voice;
    private final Supplier<String> language;
    private final Element audio = new Element("audio");
    private final Button button = new Button(T.localized(PREVIEW), Solid.VOLUME_HIGH.create());
    private final MenuBar more = new MenuBar();

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
        layout.add(button, more);
        // the choices depend on the model of the backend, read when the field is shown
        addAttachListener(e -> fillMenu());
    }

    /**
     * Keeps the speech server up for the samples while the dialog is open (see {@link AudiobookService#holdForSamples}); the requests after a
     * batch run when it is closed.
     *
     * @param warmUp start the server when the dialog opens, not with the first sample
     */
    public static void holdWhileOpen(Dialog dialog, AudiobookService service, boolean warmUp) {
        Runnable[] release = new Runnable[1];
        dialog.addAttachListener(e -> {
            if (release[0] == null) {
                release[0] = service.holdForSamples(warmUp);
            }
        });
        dialog.addDetachListener(e -> {
            if (release[0] != null) {
                release[0].run();
                release[0] = null;
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
                    audio.setAttribute("src", resource);
                    audio.callJsFunction("play");
                });
            } catch (RuntimeException e) {
                // the page is gone
            }
        });
    }
}
