package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.components.RevisionClearing;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.MessageRevisionService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * What the settings menu of the story does with the version history of the whole book (the desktop and the phone story tab): clear
 * it, or empty its trash. The history of a part is {@link PartHistoryDialog}.
 */
@Configurable(preConstruction = true)
public class BookHistoryActions {

    @Autowired
    private Localization loc;

    @Autowired
    private MessageRevisionService messageRevisionService;

    private final Manuscript manuscript;

    public BookHistoryActions(Manuscript manuscript) {
        this.manuscript = manuscript;
    }

    /**
     * Asks, then clears the history of every part of the book: only the current version of each stays.
     */
    public void clear() {
        if (manuscript == null) {
            return;
        }
        new ClearHistoryDialog(loc.getValue(L.LABEL_CLEAR_BOOK_HISTORY), new ClearHistoryDialog.Clearing() {
            @Override
            public RevisionClearing preview(boolean keepSnapshots) throws Exception {
                return messageRevisionService.previewClear(manuscript, keepSnapshots);
            }

            @Override
            public RevisionClearing clear(boolean keepSnapshots) throws Exception {
                return messageRevisionService.clear(manuscript, keepSnapshots);
            }
        }, () -> {
        }).open();
    }

    /**
     * Asks, then deletes for good the versions in the trash of the history of every part of the book.
     */
    public void emptyTrash() {
        if (manuscript != null) {
            ConfirmDialog.show(loc.getValue(L.MSG_HISTORY_CONFIRM_EMPTY_TRASH), this::doEmptyTrash);
        }
    }

    private void doEmptyTrash() {
        try {
            int removed = messageRevisionService.emptyTrash(manuscript);
            Notification.success(String.format(loc.getValue(removed == 0 ? L.MSG_HISTORY_NO_TRASH : L.MSG_HISTORY_TRASH_EMPTIED), removed));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
