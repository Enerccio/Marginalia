package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.extensions.betareaders.BetaReadersExtension;
import com.github.enerccio.marginalia.extensions.betareaders.model.Persona;
import com.github.enerccio.marginalia.extensions.betareaders.service.NoModelException;
import com.github.enerccio.marginalia.extensions.betareaders.service.PersonaIcons;
import com.github.enerccio.marginalia.extensions.betareaders.service.PersonaWriter;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.*;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.*;

/**
 * Creates or edits a persona: its name, its description and its icon. The fields are written to the persona when it is saved;
 * a persona without an icon has a circle with the first letters of its name, which the preview shows as the name is typed.
 */
public class PersonaDialog extends Dialog {

    private static final long MAX_UPLOAD_BYTES = 10L * 1024 * 1024;

    private final Persona persona;
    private final Predicate<String> nameTaken;
    private final Runnable onSaved;

    private final TextField name = new TextField(T.localized(NAME));
    private final TextArea description = new TextArea(T.localized(DESCRIPTION));
    private final PersonaGenerator generator;
    private final Supplier<List<String>> takenNames;
    private final TextArea brief = new TextArea(T.localized(WHAT_THE_PERSONA_IS_LIKE));
    private final Button write = new Button(T.localized(WRITE_WITH_THE_MODEL), Solid.WAND_MAGIC_SPARKLES.create());
    private CancellationToken running;
    private final FloatingWindow floating;
    private final Image preview = new Image();
    private Button removeIcon;
    private String icon;

    /**
     * @param header    what the dialog says, like "New persona"
     * @param persona   the persona to edit, or a new one that the owner adds to its list in {@code onSaved}
     * @param nameTaken whether another persona of the list has this name
     * @param onSaved   called after the persona was changed
     */
    public PersonaDialog(String header, Persona persona, Predicate<String> nameTaken, Runnable onSaved) {
        this(header, persona, nameTaken, onSaved, null, List::of);
    }

    /**
     * @param generator lets the model write the persona from a few words; null for no such button
     * @param takenNames the names of the personas of the list, which the model is told not to use
     */
    public PersonaDialog(String header, Persona persona, Predicate<String> nameTaken, Runnable onSaved, PersonaGenerator generator,
                         Supplier<List<String>> takenNames) {
        this.persona = persona;
        this.generator = generator;
        this.takenNames = takenNames;
        this.nameTaken = nameTaken;
        this.onSaved = onSaved;
        this.icon = persona.getIcon();

        setHeaderTitle(header);
        setWidth("560px");
        setMaxWidth("96vw");
        setMaxHeight("90vh");
        setCloseOnOutsideClick(false);
        setCloseOnEsc(false);
        // not modal: closing it while the model writes folds it into the corner; closed for good, it stops the model
        floating = new FloatingWindow(this, BetaReadersExtension.NOTIFY_PERSONA, () -> header, this::stopWriting);

        name.setRequiredIndicatorVisible(true);
        name.setWidthFull();
        name.setValue(StringUtils.defaultString(persona.getName()));
        name.addValueChangeListener(_ -> refreshPreview());
        DialogFocus.onOpen(this, name);

        description.setWidthFull();
        description.setMinHeight("180px");
        description.setHelperText(T.localized(HOW_THE_PERSONA_READS));
        description.setValue(StringUtils.defaultString(persona.getDescription()));

        VerticalLayout content = new VerticalLayout();
        if (generator != null) {
            content.add(buildWriter());
        }
        content.add(name, description, buildIcon());
        content.setPadding(false);
        add(content);

        Button cancel = new Button(T.localized(CANCEL), _ -> close());
        Button save = new Button(T.localized(SAVE), _ -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(cancel, save);
        DialogShortcuts.submitWith(save, name, description);
        DialogShortcuts.submitWith(write, brief);
    }

    /**
     * The wish of the user and the button that lets the model write the name and the description from it.
     */
    private VerticalLayout buildWriter() {
        brief.setWidthFull();
        brief.setMinHeight("80px");
        brief.setPlaceholder(T.localized(A_GRUMPY_EDITOR_WHO_HATES));
        brief.setHelperText(T.localized(DESCRIBE_THE_PERSONA));
        write.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        write.addClickListener(_ -> writeOrStop());
        VerticalLayout writer = new VerticalLayout(brief, write);
        writer.setPadding(false);
        return writer;
    }

    private void stopWriting() {
        if (running != null) {
            running.cancel();
        }
    }

    private void writeOrStop() {
        if (running != null) {
            running.cancel();
            return;
        }
        if (StringUtils.isBlank(brief.getValue())) {
            Notification.warning(T.localized(DESCRIBE_THE_PERSONA_FIRST));
            return;
        }
        UI ui = UI.getCurrent();
        try {
            setWriting(true);
            try (Runs.Binding _ = floating.bind()) {
            running = generator.start(brief.getValue(), takenNames.get(), outcome -> {
                if (UI.getCurrent() == ui) {
                    written(outcome);
                } else {
                    ui.access(() -> {
                        written(outcome);
                        UIPushGuard.push(ui);
                    });
                }
            });
            }
        } catch (Exception e) {
            setWriting(false);
            Notification.warning(e instanceof NoModelException ? T.localized(THERE_IS_NO_MODEL)
                    : T.localized(CANT_ASK_THE_MODEL).formatted(e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    private void written(PersonaWriter.Outcome outcome) {
        running = null;
        setWriting(false);
        if (outcome.cancelled()) {
            return;
        }
        if (outcome.error() != null || outcome.written() == null) {
            new DesktopNotifier(UI.getCurrent()).failed(BetaReadersExtension.NOTIFY_PERSONA, false, null);
        } else {
            new DesktopNotifier(UI.getCurrent()).finished(BetaReadersExtension.NOTIFY_PERSONA, false, null);
        }
        if (outcome.error() != null) {
            Notification.warning(T.localized(CANT_ASK_THE_MODEL).formatted(outcome.error().getMessage() != null
                    ? outcome.error().getMessage() : outcome.error().toString()));
        } else if (outcome.written() == null) {
            Notification.warning(T.localized(THE_MODEL_DID_NOT_ANSWER));
        } else {
            if (!outcome.written().name().isBlank()) {
                name.setValue(outcome.written().name());
            }
            if (!outcome.written().description().isBlank()) {
                description.setValue(outcome.written().description());
            }
        }
    }

    private void setWriting(boolean value) {
        floating.setRunning(value);
        write.setText(value ? T.localized(STOP) : T.localized(WRITE_WITH_THE_MODEL));
        write.setIcon(value ? Solid.STOP.create() : Solid.WAND_MAGIC_SPARKLES.create());
        brief.setReadOnly(value);
    }

    private VerticalLayout buildIcon() {
        preview.addClassName(BetaReadersStyles.ICON_LARGE);
        preview.setAlt(T.localized(ICON));

        InMemoryUploadHandler handler = new InMemoryUploadHandler((_, data) -> {
            try {
                icon = PersonaIcons.createIcon(data);
                refreshPreview();
            } catch (IllegalArgumentException e) {
                Notification.error(T.localized(THE_FILE));
            }
        });
        Upload upload = new Upload(handler);
        upload.setAcceptedMimeTypes("image/png", "image/jpeg", "image/gif");
        upload.setMaxFileSize((int) MAX_UPLOAD_BYTES);
        upload.setMaxFiles(1);
        upload.setDropAllowed(true);
        upload.setUploadButton(new Button(T.localized(UPLOAD_ICON), Solid.IMAGE.create()));
        upload.addFileRejectedListener(_ -> Notification.error(T.localized(THE_FILE_IS_NOT_AN_IMAGE)));
        upload.addSucceededListener(_ -> upload.clearFileList());

        removeIcon = new Button(T.localized(REMOVE_ICON), Solid.TRASH.create(), _ -> {
            icon = null;
            refreshPreview();
        });
        removeIcon.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout row = new HorizontalLayout(preview, upload, removeIcon);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        Span caption = new Span(T.localized(ICON_SCALED_DOWN)
                .formatted(PersonaIcons.MAX_ICON_SIZE));
        caption.addClassName(BetaReadersStyles.HINT);
        VerticalLayout layout = new VerticalLayout(caption, row);
        layout.setPadding(false);
        layout.setSpacing(false);
        refreshPreview();
        return layout;
    }

    private void refreshPreview() {
        Persona shown = new Persona(name.getValue(), "");
        // the color of the circle follows the uuid, so the preview is the one the persona keeps
        shown.setUuid(persona.getUuid());
        shown.setIcon(icon);
        preview.setSrc(PersonaIcons.source(shown));
        removeIcon.setVisible(icon != null);
    }

    private void save() {
        String trimmed = StringUtils.strip(name.getValue());
        if (StringUtils.isEmpty(trimmed)) {
            name.setInvalid(true);
            name.setErrorMessage(T.localized(THE_NAME_IS_REQUIRED));
            return;
        }
        if (nameTaken.test(trimmed)) {
            name.setInvalid(true);
            name.setErrorMessage(T.localized(ANOTHER_PERSONA_HAS_THIS_NAME));
            return;
        }
        persona.setName(trimmed);
        persona.setDescription(StringUtils.defaultString(description.getValue()));
        persona.setIcon(icon);
        onSaved.run();
        // saved: what the model still writes is not wanted
        floating.closeForGood();
    }
}
