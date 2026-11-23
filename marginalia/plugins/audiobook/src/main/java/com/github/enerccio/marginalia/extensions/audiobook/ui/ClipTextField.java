package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * The words spoken in a voice clip: typed by the user, or recognised by the speech recognition of the settings (Recognize). Saved with its own button
 * at once (the text belongs to the clip, wherever it is used). Hidden while no clip is shown.
 */
public class ClipTextField extends VerticalLayout {

    private final AudiobookService service;
    private final TextArea text = new TextArea(T.localized(TEXT_OF_THE_CLIP));
    private final Button recognize = new Button(T.localized(RECOGNIZE), Solid.MICROPHONE.create(), e -> recognize());
    private final Button save = new Button(T.localized(SAVE), e -> save());
    private String clipUuid;

    public ClipTextField(AudiobookService service) {
        this.service = service;
        setPadding(false);
        setSpacing(false);
        text.setWidthFull();
        text.setHelperText(T.localized(TEXT_OF_THE_CLIP_HELP));
        text.setMaxHeight("10em");
        recognize.setThemeName("tertiary small");
        save.setThemeName("tertiary small");
        HorizontalLayout buttons = new HorizontalLayout(recognize, save);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        add(text, buttons);
        setVisible(false);
    }

    /** Shows the text of the clip ({@code clip:<uuid>} or the bare uuid); hidden for anything that is not a clip. */
    public void show(String voice) {
        String uuid = voice == null ? null : voice.startsWith("clip:") ? voice.substring("clip:".length()) : voice.contains(":") ? null : voice;
        clipUuid = StringUtils.trimToNull(uuid);
        setVisible(clipUuid != null);
        text.setValue(clipUuid == null ? "" : StringUtils.defaultString(service.clipText(clipUuid)));
        recognize.setEnabled(clipUuid != null && service.getSettings().recognizer() != null);
        recognize.setTooltipText(service.getSettings().recognizer() == null ? T.localized(NO_SPEECH_RECOGNITION) : null);
    }

    /** The text area, for the keyboard shortcut of a dialog. */
    public TextArea textArea() {
        return text;
    }

    /** Stores the text when it changed (a dialog calls it when it saves). */
    public void saveIfChanged() {
        if (clipUuid != null && !StringUtils.equals(StringUtils.trimToNull(text.getValue()), service.clipText(clipUuid))) {
            service.saveClipText(clipUuid, text.getValue());
        }
    }

    private void save() {
        if (clipUuid != null) {
            service.saveClipText(clipUuid, text.getValue());
            Notification.success(T.localized(THE_TEXT_OF_THE_CLIP_IS_SAVED));
        }
    }

    private void recognize() {
        if (clipUuid == null) {
            return;
        }
        String uuid = clipUuid;
        UI ui = UI.getCurrent();
        recognize.setEnabled(false);
        recognize.setText(T.localized(RECOGNIZING));
        service.recognizeClip(uuid, (recognized, error) -> ui.access(() -> {
            recognize.setEnabled(true);
            recognize.setText(T.localized(RECOGNIZE));
            if (error != null) {
                Notification.error(T.localized(RECOGNITION_FAILED).formatted(error));
            } else if (uuid.equals(clipUuid)) {
                // shown for the user to correct; stored with Save (or the dialog's save)
                text.setValue(StringUtils.defaultString(recognized));
            }
            UIPushGuard.push(ui);
        }));
    }
}
