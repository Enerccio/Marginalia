package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;

import java.util.ArrayList;
import java.util.List;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * Picks a voice: one of the reference clips of the user (an audio resource, a button adds one) or the file name of a voice of the speech server
 * typed in (<code>Henry.wav</code>). The value is the text form of a voice, {@code clip:<uuid>} or {@code predefined:<file>}, empty for none.
 */
public class VoiceField extends CustomField<String> {

    private final VoiceChoices choices;
    private final ComboBox<String> box = new ComboBox<>();
    private final Runnable reload = this::reloadItems;
    private boolean updating;
    private java.util.function.Supplier<String> language = () -> null;

    public VoiceField(String label, VoiceChoices choices) {
        this.choices = choices;
        setLabel(label);

        box.setAllowCustomValue(true);
        box.setClearButtonVisible(true);
        box.setWidth("22em");
        box.setPlaceholder(T.localized(A_CLIP_OR_THE_FILE_OF_A_VOICE_OF_THE_SERVER));
        box.setItemLabelGenerator(choices::label);
        box.addCustomValueSetListener(e -> {
            String typed = e.getDetail().strip();
            if (!typed.isEmpty()) {
                box.setValue(typed.startsWith("clip:") || typed.startsWith("predefined:") ? typed : "predefined:" + typed);
            }
        });
        box.addValueChangeListener(e -> {
            if (!updating) {
                updateValue();
            }
        });

        InMemoryUploadHandler handler = new InMemoryUploadHandler((metadata, data) -> {
            try {
                AudiobookService.Clip clip = choices.upload(metadata.fileName(), data, metadata.contentType());
                setValue("clip:" + clip.uuid());
                VoicesDialog.warnAbout(clip.name(), choices.service().measureClip(clip.uuid()));
            } catch (RuntimeException e) {
                AudiobookLog.get().warn("The clip could not be added: {}", e.getMessage(), e);
                Notification.error(T.localized(THE_CLIP_COULD_NOT_BE_ADDED).formatted(e.getMessage()));
            }
        });
        Upload upload = new Upload(handler);
        upload.setAcceptedFileTypes("audio/wav", "audio/x-wav", "audio/mpeg", ".wav", ".mp3");
        upload.setDropAllowed(false);
        upload.setMaxFiles(1);
        upload.setUploadButton(new Button(T.localized(ADD_A_CLIP), Solid.UPLOAD.create()));

        VoicePreview preview = new VoicePreview(choices.service(), this::getValue, () -> language.get());
        HorizontalLayout row = new HorizontalLayout(box, upload, preview);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setPadding(false);
        add(row);
        reloadItems();
    }

    /** The language the sample sentence is in (a voice for a language): the code, null for the language of the book. */
    public void setLanguageSupplier(java.util.function.Supplier<String> language) {
        this.language = language;
    }

    @Override
    protected void onAttach(com.vaadin.flow.component.AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        choices.addListener(reload);
    }

    @Override
    protected void onDetach(com.vaadin.flow.component.DetachEvent detachEvent) {
        choices.removeListener(reload);
        super.onDetach(detachEvent);
    }

    private void reloadItems() {
        String current = box.getValue();
        List<String> items = new ArrayList<>();
        choices.clips().forEach(c -> items.add("clip:" + c.uuid()));
        if (current != null && !current.isBlank() && !items.contains(current)) {
            items.add(current);
        }
        updating = true;
        try {
            box.setItems(items);
            box.setValue(current);
        } finally {
            updating = false;
        }
    }

    @Override
    protected String generateModelValue() {
        return box.getValue() == null ? "" : box.getValue();
    }

    @Override
    protected void setPresentationValue(String value) {
        String voice = value == null ? "" : value;
        updating = true;
        try {
            List<String> items = new ArrayList<>();
            choices.clips().forEach(c -> items.add("clip:" + c.uuid()));
            if (!voice.isBlank() && !items.contains(voice)) {
                items.add(voice);
            }
            box.setItems(items);
            box.setValue(voice.isBlank() ? null : voice);
        } finally {
            updating = false;
        }
    }
}
