package com.github.enerccio.marginalia.ui.dialogs.bookimport;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ArchiveImportService.Insertion;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ArchiveImportRunner;
import com.github.enerccio.marginalia.ui.dialogs.ChatImportFormat;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The one dialog that makes a new book of something that exists already, or adds it to a book: a zip archive of {@code .md} files,
 * the chats of SillyTavern or LM Studio, a document (Word, LibreOffice, RTF, Markdown, text, EPUB), an Obsidian vault or a Scrivener
 * project. The user picks the source, uploads the files (the accepted types, the text and the options below follow the source: an
 * archive has no options, chats a checkbox, a document the words per part and the heading level of the chapters) and names the book.
 * A file the source can't read is refused with a warning, the others stay. The callback gets what stores the parts in the book it makes.
 * <p>
 * {@link #showInto} adds the parts to a book that has a story: there is no name, the author says where the parts go ({@link InsertionPicker})
 * and the chats are not offered, as a tree of branches has no place in the middle of a story.
 */
@Configurable
public class BookImportDialog extends Dialog {

    /**
     * Stores what was read in a book, with a progress dialog, and calls the continuation when it is over (also when it was
     * stopped halfway).
     */
    @FunctionalInterface
    public interface Run {
        /**
         * @param place where the parts go in the story, the end for a book that is new
         */
        void run(Manuscript manuscript, Insertion place, ArchiveImportRunner.Continuation<Manuscript> afterwards);
    }

    /**
     * What is done when the user chose to import: makes the book of the name and runs {@code run} on it.
     */
    @FunctionalInterface
    public interface Handler {
        void accept(Run run, String bookName) throws Exception;
    }

    @Autowired
    private Localization loc;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    private final Handler handler;
    /**
     * The book the parts are added to, null when the import makes a new book.
     */
    private final Manuscript target;
    private final ArchiveImportRunner.Continuation<Manuscript> afterwards;
    private final UI ui = UI.getCurrent();

    private List<BookImportSource> sources;
    private BookImportSource source;
    private ComboBox<BookImportSource> sourceSelect;
    private Span description;
    private VerticalLayout uploadHolder;
    private VerticalLayout optionsHolder;
    private Span status;
    private TextField nameField;
    private InsertionPicker picker;
    private Button importButton;

    private BookImportDialog(Handler handler, Manuscript target, ArchiveImportRunner.Continuation<Manuscript> afterwards) {
        this.handler = handler;
        this.target = target;
        this.afterwards = afterwards;
    }

    /**
     * Opens the dialog; the callback runs when the user chose to import and the dialog closes when it does not fail.
     */
    public static void show(Handler handler) {
        BookImportDialog dialog = new BookImportDialog(handler, null, null);
        dialog.create();
        dialog.open();
    }

    /**
     * Opens the dialog to add parts to the story of a book, at the place the user picks.
     *
     * @param afterwards called on the UI thread when the import is over, also when it was stopped halfway (what was stored stays), with the book
     *                   as it is then (null after a failure)
     */
    public static void showInto(Manuscript book, ArchiveImportRunner.Continuation<Manuscript> afterwards) {
        BookImportDialog dialog = new BookImportDialog(null, Objects.requireNonNull(book, "book"), afterwards);
        dialog.create();
        dialog.open();
    }

    private void create() {
        setHeaderTitle(loc.getValue(L.LABEL_IMPORT));
        setWidth("520px");

        boolean intoBook = target != null;
        sources = Stream.of(new ArchiveSource(loc, intoBook), new ChatSource(loc, ChatImportFormat.SILLYTAVERN),
                        new ChatSource(loc, ChatImportFormat.LMSTUDIO), new DocumentSource(loc), DocumentSource.obsidian(loc),
                        DocumentSource.scrivener(loc))
                .filter(candidate -> !intoBook || candidate.insertable()).toList();
        sources.forEach(candidate -> candidate.onChange(this::refreshStatus));

        sourceSelect = new ComboBox<>(loc.getValue(L.LABEL_SOURCE));
        sourceSelect.setItems(sources);
        sourceSelect.setItemLabelGenerator(candidate -> loc.getValue(candidate.label()));
        sourceSelect.setAllowCustomValue(false);
        sourceSelect.setWidthFull();
        sourceSelect.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                select(event.getValue());
            }
        });

        description = new Span();
        uploadHolder = new VerticalLayout();
        uploadHolder.setPadding(false);
        optionsHolder = new VerticalLayout();
        optionsHolder.setPadding(false);
        status = new Span();
        nameField = new TextField(loc.getValue(L.LABEL_NAME));
        nameField.setWidthFull();

        VerticalLayout layout = new VerticalLayout(sourceSelect, description, uploadHolder, status);
        if (intoBook) {
            picker = new InsertionPicker(loc, branchOf(target));
            picker.onChange(this::refreshStatus);
            layout.add(optionsHolder, picker);
        } else {
            layout.add(nameField, optionsHolder);
        }
        layout.setPadding(true);
        add(layout);

        importButton = new Button(loc.getValue(L.LABEL_IMPORT), event -> importBook());
        importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new Button(loc.getValue(L.LABEL_CANCEL), event -> close()), importButton);
        if (!intoBook) {
            DialogShortcuts.submitWith(importButton, nameField);
            DialogFocus.onOpen(this, nameField);
        }

        sourceSelect.setValue(sources.getFirst());
    }

    // a source has its own files, text and options: what was uploaded for another one is dropped
    private void select(BookImportSource selected) {
        if (source != null && source != selected) {
            source.reset();
        }
        source = selected;
        nameField.clear();
        description.setText(loc.getValue(selected.description()));

        InMemoryUploadHandler uploadHandler = new InMemoryUploadHandler((metadata, data) -> ui.access(() -> receive(selected, data, metadata.fileName())));
        Upload upload = new Upload(uploadHandler);
        upload.setAcceptedFileTypes(selected.fileTypes());
        uploadHolder.removeAll();
        uploadHolder.add(upload);

        optionsHolder.removeAll();
        Component options = selected.options();
        if (options != null) {
            optionsHolder.add(options);
        }
        refreshStatus();
    }

    private void receive(BookImportSource uploaded, byte[] data, String fileName) {
        if (uploaded != source) {
            return;
        }
        try {
            uploaded.read(data, fileName);
        } catch (IllegalArgumentException e) {
            Notification.warning(uploaded.invalid(fileName));
            return;
        }
        if (target == null && nameField.getValue().isBlank()) {
            nameField.setValue(uploaded.suggestedName());
        }
        refreshStatus();
    }

    // the parts of the active branch, the first part first, the places the author can pick
    private List<ChatMessage> branchOf(Manuscript book) {
        try {
            return book.getActiveLeaf() == null ? List.of() : chatMessageService.getBranchFromLeaf(book.getActiveLeaf());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return List.of();
        }
    }

    private void refreshStatus() {
        status.setText(source.status());
        importButton.setEnabled(source.ready() && (picker == null || picker.insertion() != null));
    }

    private void importBook() {
        Run run;
        try {
            run = source.prepare();
        } catch (IllegalArgumentException e) {
            Notification.warning(e.getMessage());
            return;
        }
        try {
            if (target == null) {
                handler.accept(run, nameField.getValue().trim());
            } else {
                // the book as it is now: the end of the story is where its active leaf is
                run.run(manuscriptService.find(target), picker.insertion(), afterwards);
            }
            close();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
