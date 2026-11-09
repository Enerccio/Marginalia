package com.github.enerccio.marginalia.extensions.storynotes.ui;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.storynotes.model.NoteBook;
import com.github.enerccio.marginalia.extensions.storynotes.model.NotePage;
import com.github.enerccio.marginalia.extensions.storynotes.service.StoryNotesService;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.github.enerccio.marginalia.extensions.storynotes.StoryNotesLocalization.*;

/**
 * Creates a note or edits one: title, tags, the Markdown text with a preview, whether the assistants see it, and the
 * pages it links to and the pages that link to it. Nothing is stored before <i>Save</i>. The note is stored with
 * {@link StoryNotesService#update}, which reads the stored pages again, so only this note is written; when it was changed
 * by someone else since the dialog opened (a command), the author is asked before it is overwritten.
 */
public class NotePageDialog extends Dialog {

    /** Where a link of the note leads: the view that owns the dialogs. */
    public interface Navigator {
        /** Opens the note with that title. */
        void open(String title);

        /** Opens a new note that starts with that title. */
        void create(String title);
    }

    private enum Result {SAVED, CONFLICT}

    private final StoryNotesService service;
    private final Manuscript manuscript;
    private final Navigator navigator;
    private final NotePage page;
    private final boolean isNew;
    /** When the stored note was last changed as the dialog opened it, to find out that someone changed it since. */
    private final long openedModified;

    private TextField title;
    private TextField tags;
    private TextArea content;
    private Checkbox aiVisible;
    private Div linksFrom;
    private Div linkedFrom;

    private final String initialTitle;
    private final String initialTags;
    private final String initialContent;
    private final boolean initialVisible;

    /**
     * @param existing    the note to edit, {@code null} to create a new one
     * @param presetTitle the title of a new note, may be null
     */
    public NotePageDialog(StoryNotesService service, Manuscript manuscript, NotePage existing, String presetTitle, Navigator navigator) {
        this.service = service;
        this.manuscript = manuscript;
        this.navigator = navigator;
        this.isNew = existing == null;
        this.page = isNew ? new NotePage() : existing.copy();
        if (isNew && presetTitle != null) {
            page.setTitle(presetTitle);
        }
        this.openedModified = page.getModified();
        this.initialTitle = page.getTitle();
        this.initialTags = String.join(", ", page.getTags());
        this.initialContent = page.getContent();
        this.initialVisible = page.isAiVisible();

        setHeaderTitle(isNew ? T.localized(NEW_NOTE) : T.localized(EDIT_NOTE));
        setWidth("860px");
        setMaxWidth("95vw");
        setMaxHeight("92vh");
        setCloseOnOutsideClick(false);
        setCloseOnEsc(false);
        addDialogCloseActionListener(_ -> requestClose());

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.add(buildFields());
        add(layout);

        Button save = new Button(T.localized(SAVE), _ -> save(false));
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button(T.localized(CANCEL), _ -> requestClose());
        getFooter().add(cancel, save);
        DialogShortcuts.submitWith(save, title, tags, content);
        refreshLinks();
    }

    private Component buildFields() {
        title = new TextField(T.localized(TITLE));
        title.setRequiredIndicatorVisible(true);
        title.setWidthFull();
        title.setMaxLength(NoteBook.MAX_TITLE);
        title.setValue(initialTitle);
        DialogFocus.onOpen(this, title);

        tags = new TextField(T.localized(TAGS));
        tags.setHelperText(T.localized(SEPARATED_BY_COMMAS));
        tags.setWidthFull();
        tags.setValue(initialTags);

        content = new TextArea(T.localized(TEXT_MARKDOWN_LINKS));
        content.setWidthFull();
        content.setMinHeight("320px");
        content.setMaxHeight("55vh");
        content.setMaxLength(NoteBook.MAX_CONTENT);
        content.setValue(initialContent);
        content.setValueChangeMode(ValueChangeMode.LAZY);
        content.setValueChangeTimeout(400);
        content.addValueChangeListener(_ -> {
            showLength();
            refreshLinks();
        });
        showLength();

        Markdown markdown = new Markdown();
        markdown.setWidthFull();
        // a click on a [[link]] of the preview opens the note (or starts it, when there is none)
        markdown.getElement().addEventListener("click", e -> followLink(e.getEventData().get(StoryNotesScripts.LINK_CLICK)))
                .addEventData(StoryNotesScripts.LINK_CLICK);
        Div previewBox = new Div(markdown);
        previewBox.addClassName(StoryNotesStyles.PREVIEW_BOX);
        previewBox.setVisible(false);

        Tab write = new Tab(T.localized(WRITE));
        Tab preview = new Tab(T.localized(PREVIEW));
        Tabs tabs = new Tabs(write, preview);
        tabs.addSelectedChangeListener(e -> {
            boolean showPreview = e.getSelectedTab() == preview;
            content.setVisible(!showPreview);
            previewBox.setVisible(showPreview);
            if (showPreview) {
                markdown.setContent(NoteBook.linkify(content.getValue()));
            }
        });

        aiVisible = new Checkbox(T.localized(VISIBLE_TO_ASSISTANTS), initialVisible);
        aiVisible.setTooltipText(T.localized(VISIBLE_TO_ASSISTANTS_TIP));

        linksFrom = new Div();
        linksFrom.addClassName(StoryNotesStyles.LINKS);
        linkedFrom = new Div();
        linkedFrom.addClassName(StoryNotesStyles.LINKS);

        VerticalLayout fields = new VerticalLayout(title, tags, tabs, content, previewBox);
        if (page.isChangedByCommand()) {
            Span flag = new Span(T.localized(CHANGED_BY_A_COMMAND));
            flag.addClassName(StoryNotesStyles.FLAG);
            fields.add(flag);
        }
        fields.add(aiVisible, linksFrom, linkedFrom);
        fields.setPadding(false);
        return fields;
    }

    private void showLength() {
        content.setHelperText(T.localized(CHARACTERS_COUNT).formatted(content.getValue().length()));
    }

    /**
     * Shows the notes the text links to (a link to a note that does not exist offers to create it) and the notes that link
     * to this one. Read from the stored notes, the text is the one in the field.
     */
    private void refreshLinks() {
        NoteBook notes;
        try {
            notes = service.load(manuscript);
        } catch (Exception e) {
            return;
        }
        linksFrom.removeAll();
        List<String> links = NoteBook.linksIn(content.getValue());
        if (!links.isEmpty()) {
            linksFrom.add(label(T.localized(LINKS_FROM_THIS_NOTE)));
            for (String link : links) {
                NotePage target = notes.byTitle(link);
                if (target != null) {
                    linksFrom.add(linkButton(target.getTitle(), () -> navigator.open(target.getTitle()), false));
                } else {
                    linksFrom.add(linkButton(T.localized(CREATE_THE_NOTE).formatted(link), () -> navigator.create(link), true));
                }
            }
        }
        linkedFrom.removeAll();
        if (!isNew) {
            List<NotePage> backlinks = notes.backlinks(page);
            if (!backlinks.isEmpty()) {
                linkedFrom.add(label(T.localized(LINKED_FROM)));
                backlinks.forEach(p -> linkedFrom.add(linkButton(p.getTitle(), () -> navigator.open(p.getTitle()), false)));
            }
        }
    }

    /** The click on a link of the preview: {@code data} is the address of the link, empty for another click. */
    private void followLink(Object data) {
        String title = NoteBook.titleOfHref(data == null ? null : data.toString().replace("\"", ""));
        if (title == null) {
            return;
        }
        try {
            if (service.load(manuscript).byTitle(title) != null) {
                navigator.open(title);
            } else {
                navigator.create(title);
            }
        } catch (Exception e) {
            Notification.error(e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }

    private static Span label(String text) {
        Span label = new Span(text);
        label.addClassName(StoryNotesStyles.LINKS_LABEL);
        return label;
    }

    private static Button linkButton(String text, Runnable action, boolean missing) {
        Button button = new Button(text, _ -> action.run());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        if (missing) {
            button.addClassName(StoryNotesStyles.MISSING);
        }
        return button;
    }

    private boolean hasUnsavedChanges() {
        return !StringUtils.defaultString(title.getValue()).equals(initialTitle)
                || !StringUtils.defaultString(tags.getValue()).equals(initialTags)
                || !StringUtils.defaultString(content.getValue()).equals(initialContent)
                || aiVisible.getValue() != initialVisible;
    }

    private void requestClose() {
        if (hasUnsavedChanges()) {
            ConfirmDialog.show(T.localized(THERE_ARE_UNSAVED_CHANGES_DISCARD), this::close);
        } else {
            close();
        }
    }

    private void save(boolean overwrite) {
        String cleanTitle;
        List<String> cleanTags;
        String cleanContent;
        try {
            if (StringUtils.isBlank(title.getValue())) {
                throw new IllegalArgumentException(T.localized(THE_TITLE_IS_REQUIRED));
            }
            cleanTitle = NoteBook.cleanTitle(title.getValue());
            cleanTags = NoteBook.parseTags(tags.getValue());
            cleanTags = NoteBook.cleanTags(cleanTags);
            cleanContent = NoteBook.cleanContent(content.getValue());
        } catch (IllegalArgumentException e) {
            title.setInvalid(true);
            title.setErrorMessage(e.getMessage());
            return;
        }
        final String newTitle = cleanTitle;
        final List<String> newTags = cleanTags;
        final String newContent = cleanContent;
        final boolean visible = aiVisible.getValue();
        try {
            Result result = service.update(manuscript, notes -> {
                if (notes.titleTaken(newTitle, page.getUuid())) {
                    throw new IllegalArgumentException(T.localized(ANOTHER_NOTE_HAS_THIS_TITLE));
                }
                NotePage stored = notes.get(page.getUuid());
                if (!overwrite && stored != null && stored.getModified() != openedModified) {
                    return Result.CONFLICT;
                }
                // a note deleted in the meantime is stored again
                NotePage target = stored != null ? stored : page.copy();
                notes.rename(target, newTitle);
                target.setContent(newContent);
                target.setTags(newTags);
                target.setAiVisible(visible);
                target.setChangedByCommand(false);
                target.touch();
                notes.put(target);
                return Result.SAVED;
            });
            if (result == Result.CONFLICT) {
                ConfirmDialog.show(T.localized(CHANGED_WHILE_EDITING_OVERWRITE), () -> save(true));
                return;
            }
            close();
        } catch (IllegalArgumentException e) {
            title.setInvalid(true);
            title.setErrorMessage(e.getMessage());
        } catch (Exception e) {
            Notification.error(e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }
}
