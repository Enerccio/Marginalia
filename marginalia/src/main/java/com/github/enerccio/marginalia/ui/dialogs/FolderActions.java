package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.FolderService.Content;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * What the user does with folders from the lists: make one, rename, delete, put books into one. The rules are the
 * service's ({@link FolderService}); this asks what the rules need to know (the name, what happens with the content) and
 * tells the user what the service refused. Shared by the book lists of the desktop and the phone.
 */
@Configurable
public class FolderActions {

    /**
     * A place to put a book: a folder, or the top level (folder null).
     */
    private record Target(Folder folder, String label) {
    }

    @Autowired
    private Localization loc;

    @Autowired
    private FolderService folderService;

    @Autowired
    private ManuscriptService manuscriptService;

    /**
     * Asks for the name and makes the folder in the parent (null is the top level).
     *
     * @param done called with the new folder
     */
    public void create(Folder parent, Consumer<Folder> done) {
        new TextInputDialog.Builder(loc.getValue(L.LABEL_NEW_FOLDER), name -> {
            try {
                done.accept(folderService.create(name, parent));
            } catch (Exception e) {
                refused(e, L.ERROR_FOLDER_NAME_TAKEN);
            }
        }).inputMessageLabel(loc.getValue(L.LABEL_FOLDER_NAME)).messageRequired().showCancel(true).build().open();
    }

    /**
     * Asks for the new name of the folder, which an agent working on a book in it doesn't prevent.
     *
     * @param done called with the renamed folder
     */
    public void rename(Folder folder, Consumer<Folder> done) {
        new TextInputDialog.Builder(loc.getValue(L.LABEL_RENAME), name -> {
            try {
                Folder renamed = folderService.rename(folder, name);
                if (renamed == null) {
                    Notification.warning(loc.getValue(L.ERROR_FOLDER_GONE));
                }
                done.accept(renamed);
            } catch (Exception e) {
                refused(e, L.ERROR_FOLDER_NAME_TAKEN);
            }
        }).inputMessageLabel(loc.getValue(L.LABEL_FOLDER_NAME)).initialValue(folder.getName()).messageRequired().showCancel(true)
                .build().open();
    }

    /**
     * Deletes the folder. An empty one after a plain confirmation; otherwise the user chooses between moving what is in it
     * to the folder above (the default) and deleting all of it, which asks a second time with the numbers.
     *
     * @param done called after the folder is deleted
     */
    public void delete(Folder folder, Runnable done) {
        try {
            Content content = folderService.describeContent(folder);
            if (content.books() == 0 && content.folders() == 0) {
                ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> deleteMoving(folder, done));
                return;
            }
            chooseDeletion(folder, content, done);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void chooseDeletion(Folder folder, Content content, Runnable done) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(loc.getValue(L.LABEL_DELETE_FOLDER));
        dialog.setWidth("460px");
        UIUtils.fitPhone(dialog, false);

        RadioButtonGroup<Boolean> choice = new RadioButtonGroup<>();
        choice.setItems(false, true);
        choice.setItemLabelGenerator(withContent -> loc.getValue(withContent ? L.LABEL_FOLDER_DELETE_CONTENT : L.LABEL_FOLDER_MOVE_CONTENT_UP));
        choice.setValue(false);

        VerticalLayout layout = new VerticalLayout(
                new Span(String.format(loc.getValue(L.MSG_DELETE_FOLDER_CONTENT), folder.getName(), content.books(), content.folders())),
                choice);
        layout.setPadding(false);
        dialog.add(layout);

        Button cancel = new Button(loc.getValue(L.LABEL_CANCEL), _ -> dialog.close());
        Button delete = new Button(loc.getValue(L.LABEL_DELETE), _ -> {
            dialog.close();
            if (choice.getValue()) {
                ConfirmDialog.show(String.format(loc.getValue(L.MSG_CONFIRM_DELETE_FOLDER_CONTENT), content.books(), content.folders()),
                        () -> deleteWithContent(folder, done));
            } else {
                deleteMoving(folder, done);
            }
        });
        delete.setThemeName("error primary");
        HorizontalLayout footer = new HorizontalLayout(cancel, delete);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        dialog.getFooter().add(footer);
        dialog.open();
    }

    private void deleteMoving(Folder folder, Runnable done) {
        try {
            folderService.delete(folder, false);
            done.run();
        } catch (IllegalStateException e) {
            Notification.warning(loc.getValue(L.ERROR_FOLDER_CLASH_ON_DELETE));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void deleteWithContent(Folder folder, Runnable done) {
        try {
            folderService.deleteWithContent(folder);
            done.run();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Asks for the folder to put the books into (the top level among the choices) and moves them. Nothing is saved: a
     * move is not a change of the book.
     *
     * @param current the folder the books are in now, left out of the choices (null: the top level is left out)
     * @param done    called after the books are moved
     */
    public void moveBooks(Collection<Manuscript> books, Folder current, Runnable done) {
        try {
            List<Target> targets = new ArrayList<>();
            if (current != null) {
                targets.add(new Target(null, loc.getValue(L.LABEL_TOP_LEVEL)));
            }
            Map<Long, String> paths = folderService.getPaths();
            List<Map.Entry<Long, String>> ordered = new ArrayList<>(paths.entrySet());
            ordered.sort(Map.Entry.comparingByValue(String.CASE_INSENSITIVE_ORDER));
            for (Map.Entry<Long, String> entry : ordered) {
                if (current == null || !entry.getKey().equals(current.getId())) {
                    targets.add(new Target(folderService.find(entry.getKey()), entry.getValue()));
                }
            }

            ListSelectDialog<Target> dialog = new ListSelectDialog<>(loc.getValue(L.LABEL_MOVE_TO_FOLDER), loc.getValue(L.LABEL_FOLDER),
                    Target::label, target -> {
                try {
                    manuscriptService.moveToFolder(books, target.folder());
                    done.run();
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            });
            dialog.create();
            dialog.setItems(targets);
            dialog.open();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void refused(Exception e, L taken) {
        if (e instanceof IllegalArgumentException) {
            Notification.warning(loc.getValue(L.ERROR_FOLDER_NAME_INVALID));
        } else if (e instanceof IllegalStateException) {
            Notification.warning(loc.getValue(taken));
        } else {
            UIUtils.internalServerError(loc, e);
        }
    }
}
