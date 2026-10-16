package com.github.enerccio.marginalia.extensions.charactertracker.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.charactertracker.model.CharacterBook;
import com.github.enerccio.marginalia.extensions.charactertracker.model.CharacterRelation;
import com.github.enerccio.marginalia.extensions.charactertracker.model.RelationDirection;
import com.github.enerccio.marginalia.extensions.charactertracker.model.StoryCharacter;
import com.github.enerccio.marginalia.extensions.charactertracker.service.CharacterTrackerService;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Creates a character or edits one. The fields are applied when the character is saved; relationships (shown for an
 * existing character) are added and deleted right away.
 */
public class CharacterDialog extends Dialog {

    private static final long MAX_UPLOAD_BYTES = 10L * 1024 * 1024;

    /**
     * Which way a new relationship goes, seen from the character of the dialog.
     */
    private enum Perspective {
        OUTGOING("This character → other character"),
        INCOMING("Other character → this character"),
        BOTH_WAYS("Both ways (↔)");

        private final String label;

        Perspective(String label) {
            this.label = label;
        }
    }

    private final CharacterBook book;
    private final StoryCharacter character;
    private final boolean isNew;
    private final Runnable onChanged;

    private String icon;
    private String color;

    private TextField name;
    private TextField tagline;
    private TextArea note;
    private Image iconPreview;
    private Button removeIcon;

    private Grid<CharacterRelation> relationsGrid;
    private ComboBox<StoryCharacter> relationOther;
    private TextField relationName;
    private TextField relationTagline;

    /**
     * @param existing  the character to edit, {@code null} to create a new one
     * @param onChanged called after the book changed (a character saved, a relationship added or deleted) so the
     *                  owner stores it and refreshes what it shows
     */
    public CharacterDialog(CharacterBook book, StoryCharacter existing, Runnable onChanged) {
        this.book = book;
        this.onChanged = onChanged;
        this.isNew = existing == null;
        this.character = isNew ? new StoryCharacter() : existing;
        this.icon = character.getIcon();
        this.color = CharacterTrackerService.safeColor(character.getColor());

        setHeaderTitle(isNew ? "New character" : "Edit character");
        setWidth("640px");
        setMaxWidth("95vw");
        setMaxHeight("90vh");
        setCloseOnOutsideClick(false);
        setCloseOnEsc(false);
        addDialogCloseActionListener(_ -> requestClose());

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.add(buildFields());
        if (isNew) {
            content.add(new Span("Save the character to add its relationships."));
        } else {
            content.add(buildRelations());
        }
        add(content);

        Button save = new Button("Save", _ -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", _ -> requestClose());
        getFooter().add(cancel, save);
    }

    private Component buildFields() {
        name = new TextField("Name");
        name.setRequiredIndicatorVisible(true);
        name.setWidthFull();
        name.setValue(StringUtils.defaultString(character.getName()));

        tagline = new TextField("Tagline");
        tagline.setWidthFull();
        tagline.setValue(StringUtils.defaultString(character.getTagline()));

        note = new TextArea("Note");
        note.setWidthFull();
        note.setMinHeight("120px");
        note.setValue(StringUtils.defaultString(character.getNote()));

        VerticalLayout fields = new VerticalLayout(name, tagline, note, buildIcon(), buildColor());
        fields.setPadding(false);
        return fields;
    }

    private Component buildIcon() {
        iconPreview = new Image();
        iconPreview.setAlt("Icon");
        iconPreview.addClassName(CharacterTrackerStyles.ICON);

        InMemoryUploadHandler handler = new InMemoryUploadHandler((_, data) -> {
            try {
                icon = CharacterTrackerService.createIcon(data);
                refreshIcon();
            } catch (IllegalArgumentException e) {
                Notification.error("The file is not an image that can be read");
            }
        });
        Upload upload = new Upload(handler);
        upload.setAcceptedMimeTypes("image/png", "image/jpeg", "image/gif");
        upload.setMaxFileSize((int) MAX_UPLOAD_BYTES);
        upload.setMaxFiles(1);
        upload.setDropAllowed(true);
        upload.setUploadButton(new Button("Upload icon", Solid.IMAGE.create()));
        upload.addFileRejectedListener(_ -> Notification.error("The file is not an image or it is too big"));
        upload.addSucceededListener(_ -> upload.clearFileList());

        removeIcon = new Button("Remove icon", Solid.TRASH.create(), _ -> {
            icon = null;
            refreshIcon();
        });
        removeIcon.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Span caption = new Span("Icon (scaled down to at most " + CharacterTrackerService.MAX_ICON_SIZE + " x "
                + CharacterTrackerService.MAX_ICON_SIZE + " pixels)");
        VerticalLayout layout = new VerticalLayout(caption, new HorizontalLayout(upload, removeIcon), iconPreview);
        layout.setPadding(false);
        layout.setSpacing(false);
        refreshIcon();
        return layout;
    }

    private void refreshIcon() {
        String source = CharacterTrackerService.iconSource(icon);
        iconPreview.setVisible(source != null);
        removeIcon.setVisible(source != null);
        if (source != null) {
            iconPreview.setSrc(source);
        }
    }

    private Component buildColor() {
        Input input = new Input();
        input.setType("color");
        input.setValue(color);
        input.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        input.addClassName(CharacterTrackerStyles.COLOR_INPUT);
        input.addValueChangeListener(e -> color = CharacterTrackerService.safeColor(e.getValue()));

        HorizontalLayout layout = new HorizontalLayout(new Span("Color"), input);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        return layout;
    }

    private Component buildRelations() {
        relationsGrid = new Grid<>();
        relationsGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER);
        relationsGrid.setAllRowsVisible(true);
        relationsGrid.addColumn(this::describeRelation).setHeader("Character").setFlexGrow(2);
        relationsGrid.addColumn(CharacterRelation::getName).setHeader("Name").setFlexGrow(2);
        relationsGrid.addColumn(CharacterRelation::getTagline).setHeader("Tagline").setFlexGrow(2);
        relationsGrid.addComponentColumn(relation -> {
            Button delete = new Button(Solid.TRASH.create(), _ -> deleteRelation(relation));
            delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            delete.setAriaLabel("Delete relationship");
            return delete;
        }).setFlexGrow(0).setWidth("4rem");
        refreshRelations();

        VerticalLayout layout = new VerticalLayout(new H4("Relationships"), relationsGrid, buildNewRelation());
        layout.setPadding(false);
        layout.addClassName(CharacterTrackerStyles.DIALOG_SECTION);
        return layout;
    }

    private Component buildNewRelation() {
        List<StoryCharacter> others = book.sortedCharacters().stream()
                .filter(c -> !c.getUuid().equals(character.getUuid())).toList();

        relationOther = new ComboBox<>("Search character");
        relationOther.setItems((c, filter) -> StringUtils.defaultString(c.getName()).toLowerCase(Locale.ROOT)
                .contains(StringUtils.defaultString(filter).toLowerCase(Locale.ROOT)), others);
        relationOther.setItemLabelGenerator(StoryCharacter::getName);
        relationOther.setClearButtonVisible(true);
        relationOther.setWidthFull();

        Select<Perspective> perspective = new Select<>();
        perspective.setLabel("Direction");
        perspective.setItems(Perspective.values());
        perspective.setItemLabelGenerator(p -> p.label);
        perspective.setValue(Perspective.OUTGOING);
        perspective.setWidthFull();

        relationName = new TextField("Name");
        relationName.setWidthFull();
        relationTagline = new TextField("Tagline");
        relationTagline.setWidthFull();

        Button add = new Button("Add relationship", Solid.PLUS.create(), _ -> {
            StoryCharacter target = relationOther.getValue();
            if (target == null || StringUtils.isBlank(relationName.getValue())) {
                Notification.error("Choose a character and enter the name of the relationship");
                return;
            }
            addRelation(target, perspective.getValue(), relationName.getValue().trim(),
                    StringUtils.trimToEmpty(relationTagline.getValue()));
            relationOther.clear();
            relationName.clear();
            relationTagline.clear();
        });
        add.setEnabled(!others.isEmpty());
        if (others.isEmpty()) {
            relationOther.setHelperText("There is no other character yet");
        }

        VerticalLayout layout = new VerticalLayout(add, relationOther, perspective, relationName, relationTagline);
        layout.setPadding(false);
        return layout;
    }

    private void addRelation(StoryCharacter otherCharacter, Perspective perspective, String name, String tagline) {
        CharacterRelation relation = new CharacterRelation();
        relation.setName(name);
        relation.setTagline(tagline);
        switch (perspective) {
            case INCOMING -> {
                relation.setDirection(RelationDirection.SOURCE_TARGET);
                relation.setSourceUuid(otherCharacter.getUuid());
                relation.setTargetUuid(character.getUuid());
            }
            case BOTH_WAYS -> {
                relation.setDirection(RelationDirection.UNIDIRECTIONAL);
                relation.setSourceUuid(character.getUuid());
                relation.setTargetUuid(otherCharacter.getUuid());
            }
            default -> {
                relation.setDirection(RelationDirection.SOURCE_TARGET);
                relation.setSourceUuid(character.getUuid());
                relation.setTargetUuid(otherCharacter.getUuid());
            }
        }
        book.put(relation);
        onChanged.run();
        refreshRelations();
    }

    private void deleteRelation(CharacterRelation relation) {
        book.delete(relation);
        onChanged.run();
        refreshRelations();
    }

    private void refreshRelations() {
        relationsGrid.setItems(book.relationsOf(character));
    }

    /**
     * The other character with an arrow: → the opened character targets it, ← it targets the opened
     * character, ↔ unidirectional.
     */
    private String describeRelation(CharacterRelation relation) {
        StoryCharacter other = book.get(relation.otherEnd(character.getUuid()));
        String otherName = other != null ? other.getName() : "?";
        if (relation.getDirection() == RelationDirection.UNIDIRECTIONAL) {
            return "↔ " + otherName;
        }
        return (character.getUuid().equals(relation.getSourceUuid()) ? "→ " : "← ") + otherName;
    }

    /**
     * True when a field differs from the stored character or a relationship was filled in but not added yet.
     * Relationships already added or deleted are applied right away and are not counted.
     */
    private boolean hasUnsavedChanges() {
        if (!name.getValue().equals(StringUtils.defaultString(character.getName()))
                || !tagline.getValue().equals(StringUtils.defaultString(character.getTagline()))
                || !note.getValue().equals(StringUtils.defaultString(character.getNote()))
                || !Objects.equals(icon, character.getIcon())
                || !color.equals(CharacterTrackerService.safeColor(character.getColor()))) {
            return true;
        }
        return relationOther != null && (relationOther.getValue() != null
                || StringUtils.isNotEmpty(relationName.getValue())
                || StringUtils.isNotEmpty(relationTagline.getValue()));
    }

    private void requestClose() {
        if (hasUnsavedChanges()) {
            ConfirmDialog.show("There are unsaved changes. Discard them?", this::close);
        } else {
            close();
        }
    }

    private void save() {
        if (StringUtils.isBlank(name.getValue())) {
            name.setInvalid(true);
            name.setErrorMessage("The name is required");
            return;
        }
        character.setName(name.getValue().trim());
        character.setTagline(StringUtils.trimToEmpty(tagline.getValue()));
        character.setNote(StringUtils.defaultString(note.getValue()));
        character.setIcon(icon);
        character.setColor(color);
        book.put(character);
        onChanged.run();
        close();
    }
}
