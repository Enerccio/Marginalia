package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.FolderService.Choice;
import com.github.enerccio.marginalia.domain.service.FolderService.Outcome;
import com.github.enerccio.marginalia.domain.service.FolderService.Resolution;
import com.github.enerccio.marginalia.domain.service.FolderService.Transfer;
import com.github.enerccio.marginalia.domain.service.ManuscriptLockedException;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.dialogs.agent.AgentLockCheck;
import com.github.enerccio.marginalia.ui.shortcuts.KeyBindings;
import com.github.enerccio.marginalia.ui.shortcuts.ShortcutActions;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Two panes side by side, each showing a folder with its folders and books, like a two-pane file manager. This is the
 * structure and the transfer: loading the panes, moving about in them (the keyboard included) and copying or moving what is
 * marked (or under the cursor) in the active pane into the folder of the other one, and making, renaming and deleting a folder or a book (the one under
 * the cursor), and the mass changes (model and protocol, language, tags) and the advanced search, which work on the marked books or on the
 * folder of the active pane. The other actions are added to it in turn.
 * <p>
 * Keys (in a pane): Left / Right (and Tab, a key the user can change) change the pane, Up / Down move the cursor from row to row (the grid does that; Shift marks the
 * entry it leaves), Enter opens the folder under the cursor like a double click, Backspace goes up one level, Space / Insert mark
 * the entry under the cursor. The buttons of the row below (copy, move, new folder, rename, delete and the mass changes) have keys of their own that the user sets in the key bindings of the settings
 * ({@link ShortcutActions}); the buttons show them. The keys are caught in the browser ({@link JavascriptLibrary#fireOrganiseKeys}) so the grid does not
 * use them to move between its cells.
 */
@Configurable
public class OrganiseDialog extends Dialog {

    private enum Kind {
        /** The first entry of a pane that is in a folder: the folder above. */
        UP,
        FOLDER,
        BOOK
    }

    /**
     * One line of a pane: the folder above (UP, its folder is where it leads, null is the top level), a folder or a book. An
     * entry is the same as itself only: the folder of the entry above can be a proxy outside of a session that can't be hashed.
     */
    private record Entry(Kind kind, Folder folder, Manuscript book) {
        @Override
        public boolean equals(Object other) {
            return this == other;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        String name() {
            return switch (kind) {
                case UP -> "..";
                case FOLDER -> folder.getName();
                case BOOK -> book.getName();
            };
        }
    }

    @Autowired
    private Localization loc;

    @Autowired
    private FolderService folderService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SettingService settingService;

    /**
     * A button of the row under the panes and the key of the user for it.
     */
    private record Action(String id, L label, Runnable run, boolean button) {
    }

    private final List<Action> actions = new ArrayList<>();
    private KeyBindings keyBindings = KeyBindings.of(null);

    private final Folder leftStart;
    private final Folder rightStart;
    private Pane left;
    private Pane right;
    private Pane active;
    // shows a folder (null is the top level) in the list of books, null: the button only closes the window
    private Consumer<Folder> onShowInList;
    private Consumer<Folder> onTarget;
    // the last question the window asked (a clash of folder names, the go-on of a copy), for the tests
    private Dialog lastQuestion;

    /**
     * @param left  the folder the left pane starts in, null is the top level
     * @param right the folder the right pane starts in
     */
    public OrganiseDialog(Folder left, Folder right) {
        this.leftStart = left;
        this.rightStart = right;
    }

    /**
     * @param onShowInList called with the folder of the active pane (null is the top level) after the window is closed, when the
     *                     user asks to see it in the list of books
     */
    public void setOnShowInList(Consumer<Folder> onShowInList) {
        this.onShowInList = onShowInList;
    }

    /**
     * @param onTarget called with the folder of the right pane (null is the top level) when the window is closed, so that the next one
     *                 can start there
     */
    public void setOnTarget(Consumer<Folder> onTarget) {
        this.onTarget = onTarget;
    }

    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_ORGANISE));
        setWidth("1100px");
        setMaxWidth("96%");
        setHeight("80%");
        setModality(ModalityMode.STRICT);
        setCloseOnOutsideClick(false);
        UIUtils.fitPhone(this, true);

        try {
            keyBindings = KeyBindings.of(settingService.getOrCreate(UserSetting.class));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        actions.add(new Action(ShortcutActions.ORGANISE_COPY, L.LABEL_COPY, () -> transfer(true), true));
        actions.add(new Action(ShortcutActions.ORGANISE_MOVE, L.LABEL_MOVE, () -> transfer(false), true));
        actions.add(new Action(ShortcutActions.ORGANISE_NEW_FOLDER, L.LABEL_NEW_FOLDER, this::newFolder, true));
        actions.add(new Action(ShortcutActions.ORGANISE_RENAME, L.LABEL_RENAME, this::rename, true));
        actions.add(new Action(ShortcutActions.ORGANISE_DELETE, L.LABEL_DELETE, this::delete, true));
        actions.add(new Action(ShortcutActions.ORGANISE_FOLDER_SETTINGS, L.LABEL_FOLDER_SETTINGS, this::folderSettings, true));
        actions.add(new Action(ShortcutActions.ORGANISE_MODEL_PROTOCOL, L.LABEL_MASS_CHANGE_MODEL_PROTOCOL, this::massChangeModelProtocol, true));
        actions.add(new Action(ShortcutActions.ORGANISE_LANGUAGE, L.LABEL_MASS_CHANGE_LANGUAGE, this::massChangeLanguage, true));
        actions.add(new Action(ShortcutActions.ORGANISE_TAG_BOOKS, L.LABEL_TAG_BOOKS, this::tagBooks, true));
        actions.add(new Action(ShortcutActions.ORGANISE_SEARCH, L.LABEL_ADVANCED_SEARCH, this::search, true));
        actions.add(new Action(ShortcutActions.ORGANISE_SHOW_IN_LIST, L.LABEL_SHOW_FOLDER_IN_LIST, this::showInList, true));
        // a key only, like Left / Right
        actions.add(new Action(ShortcutActions.ORGANISE_SWITCH_PANE, L.LABEL_SWITCH_PANE, () -> active.key("ArrowRight"), false));

        left = new Pane(leftStart);
        right = new Pane(rightStart);

        Span hint = new Span(loc.getValue(L.HELP_ORGANISE));
        hint.addClassName(CssStyles.HINT_TEXT);

        HorizontalLayout panes = new HorizontalLayout(left.layout, right.layout);
        panes.setSizeFull();
        panes.setPadding(false);
        panes.addClassName(CssStyles.MIN_HEIGHT_0);
        panes.expand(left.layout, right.layout);

        VerticalLayout content = new VerticalLayout(hint, panes);
        content.setSizeFull();
        content.setPadding(false);
        content.addClassName(CssStyles.MIN_HEIGHT_0);
        content.expand(panes);
        add(content);

        HorizontalLayout footer = new HorizontalLayout();
        for (Action action : actions.stream().filter(Action::button).toList()) {
            footer.add(new Button(buttonLabel(action), _ -> action.run().run()));
        }
        footer.add(new Button(loc.getValue(L.LABEL_CLOSE), _ -> close()));
        footer.setWidthFull();
        footer.setWrap(true);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(footer);

        left.load();
        right.load();
        activate(left);
        addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                JavascriptLibrary.focusOrganiseGrid(left.grid.getElement(), left.cursorIndex());
            } else if (onTarget != null) {
                onTarget.accept(right.folder);
            }
        });
    }

    /** The name of the button and the key of the user for it. */
    private String buttonLabel(Action action) {
        String key = keyBindings.get(action.id());
        return loc.getValue(action.label()) + (key == null ? "" : " (" + key + ")");
    }

    /** The keys of the actions as the browser script wants them: key combination to action id. */
    private String actionKeys() {
        JsonObject keys = new JsonObject();
        for (Action action : actions) {
            String key = keyBindings.get(action.id());
            if (key != null) {
                keys.addProperty(key, action.id());
            }
        }
        return keys.toString();
    }

    // for the tests of the dialog

    Dialog lastQuestion() {
        return lastQuestion;
    }

    List<String> buttons() {
        return actions.stream().filter(Action::button).map(this::buttonLabel).toList();
    }

    List<String> names(boolean leftPane) {
        return (leftPane ? left : right).grid.getListDataView().getItems().map(Entry::name).toList();
    }

    String path(boolean leftPane) {
        return (leftPane ? left : right).path.getText();
    }

    void open(boolean leftPane, String name) {
        Pane pane = leftPane ? left : right;
        pane.open(pane.grid.getListDataView().getItems().filter(entry -> entry.name().equals(name)).findFirst().orElseThrow());
    }

    void up(boolean leftPane) {
        (leftPane ? left : right).up();
    }

    void press(String key) {
        active.key(key);
    }

    boolean leftActive() {
        return active == left;
    }

    /** Puts the cursor of the pane on the entry, as the grid does when the user moves to the row. */
    void cursorOn(boolean leftPane, String name) {
        Pane pane = leftPane ? left : right;
        pane.moveCursor(pane.grid.getListDataView().getItems().filter(entry -> entry.name().equals(name)).findFirst().orElseThrow());
        activate(pane);
    }

    /** The name of the entry under the cursor of the pane, null when there is none. */
    String cursor(boolean leftPane) {
        Entry cursor = (leftPane ? left : right).cursor;
        return cursor == null ? null : cursor.name();
    }

    /**
     * Copies or moves from the active pane to the other without asking, the clashes are decided by the name of the folder.
     */
    void transferNow(boolean copy, boolean onlyCurrentBranch, Map<String, Choice> choicesByName) {
        Pane from = active;
        Pane to = other(from);
        List<Entry> chosen = from.selection();
        Map<Long, Choice> choices = new HashMap<>();
        for (Entry entry : chosen) {
            if (entry.kind() == Kind.FOLDER && choicesByName.containsKey(entry.name())) {
                choices.put(entry.folder().getId(), choicesByName.get(entry.name()));
            }
        }
        performNow(new Transfer(chosen.stream().filter(entry -> entry.kind() == Kind.BOOK).map(Entry::book).toList(),
                chosen.stream().filter(entry -> entry.kind() == Kind.FOLDER).map(Entry::folder).toList(),
                to.folder, copy, onlyCurrentBranch, choices));
    }

    /** The folders of the pane that a copy or move of the marked entries to the other pane finds a name taken in. */
    List<String> clashes(boolean copy) throws Exception {
        Pane from = active;
        return folderService.findClashes(from.selection().stream().filter(entry -> entry.kind() == Kind.FOLDER).map(Entry::folder).toList(),
                other(from).folder, copy).stream().map(Folder::getName).toList();
    }

    /** What follows a folder that was made or renamed: both panes are loaded again, the cursor of the active one is on the folder. */
    void folderChanged(Folder folder) {
        changed(Kind.FOLDER, folder.getId());
    }

    Set<String> marked(boolean leftPane) {
        return (leftPane ? left : right).marked();
    }

    /**
     * Copies or moves what is marked in the active pane (the entry under the cursor when nothing is) into the folder of the
     * other pane: the folders that find their name taken ask what to do, a copy asks to go on (and whether to take only the
     * current branch of the books), then the panes are loaded again.
     */
    private void transfer(boolean copy) {
        Pane from = active;
        Pane to = other(from);
        List<Entry> chosen = from.selection();
        if (chosen.isEmpty()) {
            Notification.warning(loc.getValue(L.MSG_ORGANISE_NOTHING_SELECTED));
            return;
        }
        if (sameFolder(from.folder, to.folder)) {
            Notification.warning(loc.getValue(L.MSG_ORGANISE_SAME_FOLDER));
            return;
        }
        try {
            List<Manuscript> books = chosen.stream().filter(entry -> entry.kind() == Kind.BOOK).map(Entry::book).toList();
            List<Folder> folders = chosen.stream().filter(entry -> entry.kind() == Kind.FOLDER).map(Entry::folder).toList();
            for (Manuscript book : books) {
                if (new AgentLockCheck().refuses(book)) {
                    return;
                }
            }
            List<Folder> clashes = folderService.findClashes(folders, to.folder, copy);
            resolveClashes(new ArrayList<>(clashes), to.folder, new HashMap<>(), null, choices -> {
                if (!copy) {
                    performNow(new Transfer(books, folders, to.folder, false, false, choices));
                    return;
                }
                try {
                    long steps = copySteps(books, folders, choices);
                    confirmCopy(books.size() + folders.size(), to.folder, hasBooks(books, folders),
                            onlyCurrentBranch -> performCopy(new Transfer(books, folders, to.folder, true, onlyCurrentBranch, choices), steps));
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            });
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private static boolean sameFolder(Folder a, Folder b) {
        return a == null ? b == null : b != null && a.getId().equals(b.getId());
    }

    private boolean hasBooks(List<Manuscript> books, List<Folder> folders) throws Exception {
        if (!books.isEmpty()) {
            return true;
        }
        for (Folder folder : folders) {
            if (folderService.describeContent(folder).books() > 0) {
                return true;
            }
        }
        return false;
    }

    private String pathOf(Folder folder) throws Exception {
        return folder == null ? loc.getValue(L.LABEL_TOP_LEVEL) : folderService.getPath(folder);
    }

    /**
     * Asks about the folders one after another; "apply to all" decides the rest. A rename of all gives the others a free name
     * of their own ("name (2)").
     */
    private void resolveClashes(List<Folder> pending, Folder target, Map<Long, Choice> choices, FolderClashDialog.Decision forAll,
                                Consumer<Map<Long, Choice>> done) throws Exception {
        if (pending.isEmpty()) {
            done.accept(choices);
            return;
        }
        Folder folder = pending.getFirst();
        List<Folder> rest = new ArrayList<>(pending.subList(1, pending.size()));
        String free = freeName(folder.getName(), target, choices);
        if (forAll != null) {
            Choice choice = forAll.choice();
            choices.put(folder.getId(), choice.resolution() == Resolution.RENAME ? new Choice(Resolution.RENAME, free) : choice);
            resolveClashes(rest, target, choices, forAll, done);
            return;
        }
        FolderClashDialog question = new FolderClashDialog(folder.getName(), pathOf(target), free, decision -> {
            try {
                choices.put(folder.getId(), decision.choice());
                resolveClashes(rest, target, choices, decision.forAll() ? decision : null, done);
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }, () -> {
        });
        lastQuestion = question;
        question.create();
    }

    /**
     * A name for the folder that nothing in the target has and no other rename of the transfer uses.
     */
    private String freeName(String name, Folder target, Map<Long, Choice> choices) throws Exception {
        Set<String> taken = new HashSet<>();
        for (Folder inside : folderService.findChildren(target)) {
            taken.add(inside.getName().toLowerCase());
        }
        for (Choice choice : choices.values()) {
            if (choice.resolution() == Resolution.RENAME) {
                taken.add(choice.name().toLowerCase());
            }
        }
        for (int number = 2; ; number++) {
            String candidate = name + " (" + number + ")";
            if (!taken.contains(candidate.toLowerCase())) {
                return candidate;
            }
        }
    }

    /**
     * The last question of a copy: go on, and (when books are copied) only the current branch of them, which is the default.
     */
    private void confirmCopy(int count, Folder target, boolean withBooks, Consumer<Boolean> go) throws Exception {
        Dialog dialog = new Dialog();
        lastQuestion = dialog;
        dialog.setHeaderTitle(loc.getValue(L.LABEL_COPY));
        dialog.setWidth("460px");
        UIUtils.fitPhone(dialog, false);

        Checkbox onlyCurrentBranch = new Checkbox(loc.getValue(L.LABEL_ONLY_CURRENT_BRANCH), true);
        onlyCurrentBranch.setVisible(withBooks);
        VerticalLayout layout = new VerticalLayout(new Span(String.format(loc.getValue(L.MSG_ORGANISE_COPY_TO), count, pathOf(target))),
                onlyCurrentBranch);
        layout.setPadding(false);
        dialog.add(layout);

        Button cancel = new Button(loc.getValue(L.LABEL_CANCEL), _ -> dialog.close());
        Button copy = new Button(loc.getValue(L.LABEL_COPY), _ -> {
            dialog.close();
            go.accept(onlyCurrentBranch.getValue());
        });
        copy.setThemeName("primary");
        HorizontalLayout footer = new HorizontalLayout(cancel, copy);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        dialog.getFooter().add(footer);
        dialog.open();
    }

    /**
     * How many steps a copy takes (what the progress counts): every book and every folder that is made, the folders left out by
     * the choices not counted.
     */
    private long copySteps(List<Manuscript> books, List<Folder> folders, Map<Long, Choice> choices) throws Exception {
        long steps = books.size();
        for (Folder folder : folders) {
            Choice choice = choices.get(folder.getId());
            if (choice == null || choice.resolution() != Resolution.SKIP) {
                FolderService.Content content = folderService.describeContent(folder);
                steps += 1 + content.folders() + content.books();
            }
        }
        return Math.max(1, steps);
    }

    /**
     * Copies in the background with a progress bar that can't be stopped (a stop would leave the copy half done and the user
     * with no way to tell which), then tells what happened and loads both panes again.
     */
    private void performCopy(Transfer transfer, long steps) {
        AtomicReference<Outcome> outcome = new AtomicReference<>();
        AtomicReference<Exception> failure = new AtomicReference<>();

        ProgressBarDialog progress = new ProgressBarDialog(true);
        progress.setTitle(loc.getValue(L.LABEL_COPY));
        progress.setTotal(steps);
        progress.create();
        progress.setAction(dialog -> {
            try {
                outcome.set(folderService.transfer(transfer, dialog::updateProgress));
            } catch (Exception e) {
                failure.set(e);
            }
        });
        progress.setAfterAction(() -> report(outcome.get(), failure.get()));
        progress.open();
    }

    /**
     * Does the transfer now (a move is quick), tells what happened and loads both panes again.
     */
    private void performNow(Transfer transfer) {
        try {
            report(folderService.transfer(transfer), null);
        } catch (Exception e) {
            report(null, e);
        }
    }

    private void report(Outcome outcome, Exception failure) {
        if (failure == null) {
            Notification.success(String.format(loc.getValue(L.MSG_ORGANISE_DONE), outcome.books(), outcome.folders(), outcome.skipped()));
        } else if (failure instanceof ManuscriptLockedException) {
            UIUtils.internalServerError(loc, failure);
        } else if (failure instanceof IllegalStateException) {
            Notification.warning(loc.getValue(L.ERROR_FOLDER_TRANSFER_REFUSED));
        } else {
            UIUtils.internalServerError(loc, failure);
        }
        left.reload();
        right.reload();
        JavascriptLibrary.focusOrganiseGrid(active.grid.getElement(), active.cursorIndex());
    }

    /**
     * Makes a folder in the folder of the active pane.
     */
    private void newFolder() {
        new FolderActions().create(active.folder, created -> changed(Kind.FOLDER, created.getId()));
    }

    /**
     * Renames the folder or the book under the cursor of the active pane.
     */
    private void rename() {
        Entry entry = underCursor();
        if (entry == null) {
            return;
        }
        if (entry.kind() == Kind.FOLDER) {
            new FolderActions().rename(entry.folder(), renamed -> changed(Kind.FOLDER, renamed == null ? null : renamed.getId()));
            return;
        }
        Manuscript book = entry.book();
        try {
            if (new AgentLockCheck().refuses(book)) {
                return;
            }
            new TextInputDialog.Builder(loc.getValue(L.LABEL_RENAME), name -> renameBook(book, name))
                    .inputMessageLabel(loc.getValue(L.LABEL_NAME)).initialValue(book.getName()).messageRequired().showCancel(true).build().open();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Opens the settings of the folder under the cursor of the active pane (a book has its own window).
     */
    private void folderSettings() {
        Entry entry = underCursor();
        if (entry != null && entry.kind() == Kind.FOLDER) {
            new FolderDialog(entry.folder(), saved -> changed(Kind.FOLDER, saved.getId())).show();
        }
    }

    void renameBook(Manuscript book, String name) {
        try {
            Manuscript renamed = manuscriptService.update(book, stored -> stored.setName(name.strip()));
            changed(Kind.BOOK, renamed.getId());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Deletes the folder (it asks what happens with what is in it) or the book under the cursor of the active pane.
     */
    private void delete() {
        Entry entry = underCursor();
        if (entry == null) {
            return;
        }
        if (entry.kind() == Kind.FOLDER) {
            new FolderActions().delete(entry.folder(), () -> changed(null, null));
            return;
        }
        Manuscript book = entry.book();
        try {
            if (!new AgentLockCheck().refuses(book)) {
                ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> deleteBook(book));
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    void deleteBook(Manuscript book) {
        try {
            manuscriptService.delete(book, false);
            changed(null, null);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * The entry under the cursor of the active pane for an action on one entry; none (and a warning) when the pane is empty or
     * the cursor is on the folder above.
     */
    private Entry underCursor() {
        Entry entry = active.cursor;
        if (entry == null || entry.kind() == Kind.UP) {
            Notification.warning(loc.getValue(L.MSG_ORGANISE_NOTHING_SELECTED));
            return null;
        }
        return entry;
    }

    /**
     * Loads both panes again after a folder or a book was made, renamed or deleted (the other pane may show the same folder) and
     * puts the cursor of the active one on the entry (null: it stays on its row number).
     *
     * @param kind the kind of the entry to put the cursor on
     * @param id   its id
     */
    private void changed(Kind kind, Long id) {
        left.reload();
        right.reload();
        if (id != null) {
            active.moveCursorWhere(entry -> entry.kind() == kind
                    && id.equals(kind == Kind.FOLDER ? entry.folder().getId() : entry.book().getId()));
        }
        JavascriptLibrary.focusOrganiseGrid(active.grid.getElement(), active.cursorIndex());
    }

    /**
     * The books marked in the active pane, or none: the cursor row doesn't count, it would always narrow a mass change to one
     * book.
     */
    private List<Manuscript> markedBooks() {
        return active.selectionOnlyMarked().stream().filter(entry -> entry.kind() == Kind.BOOK).map(Entry::book).toList();
    }

    private void massChangeModelProtocol() {
        List<Manuscript> books = markedBooks();
        MassChangeModelProtocolDialog dialog = books.isEmpty() ? new MassChangeModelProtocolDialog(null, active.folder)
                : new MassChangeModelProtocolDialog(null, books);
        dialog.create();
        dialog.open();
    }

    private void massChangeLanguage() {
        List<Manuscript> books = markedBooks();
        MassChangeLanguageDialog dialog = books.isEmpty() ? new MassChangeLanguageDialog(null, active.folder)
                : new MassChangeLanguageDialog(null, books);
        dialog.create();
        dialog.open();
    }

    private void tagBooks() {
        List<Manuscript> books = markedBooks();
        MultiTagDialog dialog = books.isEmpty() ? new MultiTagDialog(MultiTagDialog.Kind.BOOKS, () -> {
        }, active.folder) : new MultiTagDialog(() -> {
        }, books);
        dialog.create();
        dialog.open();
    }

    /**
     * Closes the window and shows the folder of the active pane in the list of books.
     */
    private void showInList() {
        Folder shown = active.folder;
        close();
        if (onShowInList != null) {
            onShowInList.accept(shown);
        }
    }

    /**
     * Opens the advanced search in the folder of the active pane; a result is shown in that pane.
     */
    private void search() {
        Pane pane = active;
        ManuscriptSearchDialog[] dialog = new ManuscriptSearchDialog[1];
        dialog[0] = new ManuscriptSearchDialog(pane.folder, null, book -> {
            dialog[0].close();
            showInFolder(pane, book);
        });
        dialog[0].open();
    }

    /**
     * Takes the pane to the folder of the book and marks the book there.
     */
    private void showInFolder(Pane pane, Manuscript book) {
        activate(pane);
        pane.show(book);
    }

    // for the tests of the dialog

    void showInFolder(boolean leftPane, Manuscript book) {
        showInFolder(leftPane ? left : right, book);
    }

    private Pane other(Pane pane) {
        return pane == left ? right : left;
    }

    private void activate(Pane pane) {
        Pane before = active;
        active = pane;
        if (before != pane) {
            // the cursor row is shown in the active pane only
            for (Pane changed : new Pane[]{before, pane}) {
                if (changed != null && changed.cursor != null) {
                    changed.grid.getListDataView().refreshItem(changed.cursor);
                }
            }
        }
        left.layout.removeClassName(CssStyles.ORGANISE_PANE_ACTIVE);
        right.layout.removeClassName(CssStyles.ORGANISE_PANE_ACTIVE);
        pane.layout.addClassName(CssStyles.ORGANISE_PANE_ACTIVE);
    }

    /**
     * One side of the dialog: the folder it shows, the entries of it and the cursor.
     */
    private final class Pane {
        private Folder folder;
        private final Span path = new Span();
        private final Grid<Entry> grid = new Grid<>();
        private final VerticalLayout layout;
        // the entry under the cursor, the last one the grid reported focus on
        private Entry cursor;

        Pane(Folder folder) {
            this.folder = folder;

            grid.setSizeFull();
            grid.addThemeVariants(GridVariant.LUMO_COMPACT);
            grid.setSelectionMode(Grid.SelectionMode.MULTI);
            grid.addComponentColumn(entry -> name(entry)).setHeader(loc.getValue(L.LABEL_NAME)).setFlexGrow(3);
            grid.addColumn(entry -> entry.kind() == Kind.FOLDER ? count(entry.folder()) : "")
                    .setHeader(loc.getValue(L.LABEL_BOOKS)).setFlexGrow(0).setWidth("100px");
            // only the pane the keyboard is in shows its cursor row
            grid.setPartNameGenerator(entry -> entry == cursor && active == this ? CssStyles.ORGANISE_CURSOR_ROW : null);
            grid.addCellFocusListener(event -> {
                moveCursor(event.getItem().orElse(null));
                activate(this);
            });
            grid.addItemDoubleClickListener(event -> open(event.getItem()));

            // movement: the keys are caught on the grid, so they work in the pane that has the focus
            JavascriptLibrary.fireOrganiseKeys(grid.getElement(), actionKeys());
            grid.getElement().addEventListener("organise-key", event -> key(event.getEventData().get("event.detail").asString()))
                    .addEventData("event.detail");

            path.addClassName(CssStyles.TEXT_BOLD);
            layout = new VerticalLayout(path, grid);
            layout.setPadding(false);
            layout.setSpacing(false);
            layout.addClassNames(CssStyles.ORGANISE_PANE, CssStyles.MIN_HEIGHT_0);
            layout.expand(grid);
        }

        /**
         * What a key of {@link JavascriptLibrary#fireOrganiseKeys} does.
         */
        void key(String key) {
            switch (key) {
                case "ArrowLeft", "ArrowRight" -> goTo(other(this));
                case "Enter" -> open(cursor);
                case "Backspace" -> up();
                case "Insert", "Space" -> mark();
                // the grid moves to the next row after this, the entry it leaves stays marked
                case "Shift+ArrowUp", "Shift+ArrowDown" -> {
                    if (cursor != null && cursor.kind() != Kind.UP) {
                        grid.select(cursor);
                    }
                }
                // the id of an action of the row of buttons
                default -> actions.stream().filter(action -> action.id().equals(key)).findFirst().ifPresent(action -> action.run().run());
            }
        }

        /**
         * Puts the cursor on the entry (null: it is not on a row, for example in the header) and shows it on the whole row.
         */
        private void moveCursor(Entry entry) {
            Entry before = cursor;
            cursor = entry;
            for (Entry changed : new Entry[]{before, entry}) {
                if (changed != null) {
                    grid.getListDataView().refreshItem(changed);
                }
            }
        }

        private com.vaadin.flow.component.Component name(Entry entry) {
            VaadinIcon icon = switch (entry.kind()) {
                case UP -> VaadinIcon.LEVEL_UP;
                case FOLDER -> VaadinIcon.FOLDER;
                case BOOK -> VaadinIcon.BOOK;
            };
            HorizontalLayout name = new HorizontalLayout(icon.create(), new Span(entry.name()));
            name.setPadding(false);
            name.setAlignItems(FlexComponent.Alignment.CENTER);
            return name;
        }

        private String count(Folder inside) {
            Long books = counts.get(inside.getId());
            return NumberFormat.getIntegerInstance().format(books == null ? 0 : books);
        }

        private Map<Long, Long> counts = Map.of();

        /**
         * Reads the folder again. A folder that is gone is the top level.
         */
        void load() {
            try {
                if (folder != null) {
                    Folder stored = folderService.find(folder.getId());
                    folder = stored == null || stored.isDeleted() ? null : stored;
                }
                counts = folderService.countBooksByFolder();
                List<Entry> entries = new ArrayList<>();
                if (folder != null) {
                    entries.add(new Entry(Kind.UP, folder.getParent(), null));
                }
                for (Folder inside : folderService.findChildren(folder)) {
                    entries.add(new Entry(Kind.FOLDER, inside, null));
                }
                List<Manuscript> books = new ArrayList<>(manuscriptService.findRegularInFolder(folder, false));
                books.sort(Comparator.comparing(book -> StringUtils.defaultString(book.getName()), String.CASE_INSENSITIVE_ORDER));
                for (Manuscript book : books) {
                    entries.add(new Entry(Kind.BOOK, null, book));
                }
                grid.setItems(entries);
                grid.deselectAll();
                // the grid focuses the first row when it gets the focus
                cursor = entries.isEmpty() ? null : entries.getFirst();
                path.setText(folder == null ? loc.getValue(L.LABEL_TOP_LEVEL) : folderService.getPath(folder));
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }

        /**
         * Goes into the folder of the entry, or up for the entry above; a book is not opened.
         */
        void open(Entry entry) {
            if (entry == null || entry.kind() == Kind.BOOK) {
                return;
            }
            Folder left = entry.kind() == Kind.UP ? folder : null;
            folder = entry.folder();
            load();
            focusRow(left);
        }

        /**
         * Puts the cursor and the focus on the folder (the one we came from), or on the first row.
         */
        private void focusRow(Folder child) {
            List<Entry> entries = grid.getListDataView().getItems().toList();
            int index = 0;
            for (int i = 0; child != null && i < entries.size(); i++) {
                Entry entry = entries.get(i);
                if (entry.kind() == Kind.FOLDER && entry.folder().getId().equals(child.getId())) {
                    index = i;
                    break;
                }
            }
            if (!entries.isEmpty()) {
                moveCursor(entries.get(index));
            }
            JavascriptLibrary.focusOrganiseGrid(grid.getElement(), index);
        }

        void up() {
            try {
                Folder stored = folder == null ? null : folderService.find(folder.getId());
                if (stored != null) {
                    Folder left = stored;
                    folder = stored.getParent();
                    load();
                    focusRow(left);
                }
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }

        /**
         * Marks or unmarks the entry under the cursor, the folder above can't be marked.
         */
        void mark() {
            if (cursor == null || cursor.kind() == Kind.UP) {
                return;
            }
            if (grid.getSelectedItems().contains(cursor)) {
                grid.deselect(cursor);
            } else {
                grid.select(cursor);
            }
        }

        /** The marked entries only. */
        List<Entry> selectionOnlyMarked() {
            return grid.getListDataView().getItems().filter(entry -> grid.getSelectedItems().contains(entry)).toList();
        }

        /**
         * Goes to the folder of the book, marks it and puts the cursor and the focus on it.
         */
        void show(Manuscript book) {
            folder = book.getFolder();
            load();
            List<Entry> entries = grid.getListDataView().getItems().toList();
            for (Entry entry : entries) {
                if (entry.kind() == Kind.BOOK && entry.book().getId().equals(book.getId())) {
                    grid.select(entry);
                    moveCursor(entry);
                    break;
                }
            }
            JavascriptLibrary.focusOrganiseGrid(grid.getElement(), cursorIndex());
        }

        /**
         * What a copy or a move works on: the marked entries, or the one under the cursor when none is marked; the entry above
         * is never one of them.
         */
        List<Entry> selection() {
            List<Entry> marked = grid.getListDataView().getItems().filter(entry -> grid.getSelectedItems().contains(entry)).toList();
            if (!marked.isEmpty()) {
                return marked;
            }
            return cursor == null || cursor.kind() == Kind.UP ? List.of() : List.of(cursor);
        }

        /**
         * Reads the folder again and keeps the cursor on its row number (or the last row when there are fewer now).
         */
        void reload() {
            int index = cursorIndex();
            load();
            List<Entry> entries = grid.getListDataView().getItems().toList();
            if (!entries.isEmpty()) {
                moveCursor(entries.get(Math.min(index, entries.size() - 1)));
            }
        }

        /** Puts the cursor on the first entry that matches, if there is one. */
        void moveCursorWhere(java.util.function.Predicate<Entry> match) {
            grid.getListDataView().getItems().filter(match).findFirst().ifPresent(this::moveCursor);
        }

        /** The row the cursor is on, the first one when it is on none. */
        int cursorIndex() {
            return Math.max(0, grid.getListDataView().getItems().toList().indexOf(cursor));
        }

        private void goTo(Pane pane) {
            activate(pane);
            JavascriptLibrary.focusOrganiseGrid(pane.grid.getElement(), pane.cursorIndex());
        }

        // for the tests of the dialog
        Set<String> marked() {
            return grid.getSelectedItems().stream().map(Entry::name).collect(java.util.stream.Collectors.toSet());
        }
    }
}
