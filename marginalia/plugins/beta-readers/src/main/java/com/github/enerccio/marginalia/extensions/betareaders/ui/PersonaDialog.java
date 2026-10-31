package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.betareaders.model.Persona;
import com.github.enerccio.marginalia.extensions.betareaders.service.PersonaIcons;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.Notification;
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

import java.util.function.Predicate;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.T;

/**
 * Creates or edits a persona: its name, its description and its icon. The fields are written to the persona when it is saved;
 * a persona without an icon has a circle with the first letters of its name, which the preview shows as the name is typed.
 */
public class PersonaDialog extends Dialog {

    private static final long MAX_UPLOAD_BYTES = 10L * 1024 * 1024;

    private final Persona persona;
    private final Predicate<String> nameTaken;
    private final Runnable onSaved;

    private final TextField name = new TextField(T.localized("Name"));
    private final TextArea description = new TextArea(T.localized("Description"));
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
        this.persona = persona;
        this.nameTaken = nameTaken;
        this.onSaved = onSaved;
        this.icon = persona.getIcon();

        setHeaderTitle(header);
        setWidth("560px");
        setMaxWidth("96vw");
        setMaxHeight("90vh");
        setCloseOnOutsideClick(false);

        name.setRequiredIndicatorVisible(true);
        name.setWidthFull();
        name.setValue(StringUtils.defaultString(persona.getName()));
        name.addValueChangeListener(_ -> refreshPreview());
        DialogFocus.onOpen(this, name);

        description.setWidthFull();
        description.setMinHeight("180px");
        description.setHelperText(T.localized("How the persona reads and what it cares about; it is what the model plays"));
        description.setValue(StringUtils.defaultString(persona.getDescription()));

        VerticalLayout content = new VerticalLayout(name, description, buildIcon());
        content.setPadding(false);
        add(content);

        Button cancel = new Button(T.localized("Cancel"), _ -> close());
        Button save = new Button(T.localized("Save"), _ -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(cancel, save);
    }

    private VerticalLayout buildIcon() {
        preview.addClassName(BetaReadersStyles.ICON_LARGE);
        preview.setAlt(T.localized("Icon"));

        InMemoryUploadHandler handler = new InMemoryUploadHandler((_, data) -> {
            try {
                icon = PersonaIcons.createIcon(data);
                refreshPreview();
            } catch (IllegalArgumentException e) {
                Notification.error(T.localized("The file is not an image that can be read"));
            }
        });
        Upload upload = new Upload(handler);
        upload.setAcceptedMimeTypes("image/png", "image/jpeg", "image/gif");
        upload.setMaxFileSize((int) MAX_UPLOAD_BYTES);
        upload.setMaxFiles(1);
        upload.setDropAllowed(true);
        upload.setUploadButton(new Button(T.localized("Upload icon"), Solid.IMAGE.create()));
        upload.addFileRejectedListener(_ -> Notification.error(T.localized("The file is not an image or it is too big")));
        upload.addSucceededListener(_ -> upload.clearFileList());

        removeIcon = new Button(T.localized("Remove icon"), Solid.TRASH.create(), _ -> {
            icon = null;
            refreshPreview();
        });
        removeIcon.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout row = new HorizontalLayout(preview, upload, removeIcon);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        Span caption = new Span(T.localized("Icon (scaled down to at most %1$d x %1$d pixels; without one, a circle with the first letters of the name is shown)")
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
            name.setErrorMessage(T.localized("The name is required"));
            return;
        }
        if (nameTaken.test(trimmed)) {
            name.setInvalid(true);
            name.setErrorMessage(T.localized("Another persona has this name"));
            return;
        }
        persona.setName(trimmed);
        persona.setDescription(StringUtils.defaultString(description.getValue()));
        persona.setIcon(icon);
        onSaved.run();
        close();
    }
}
