package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ArchiveImportService.ImportResult;
import com.github.enerccio.marginalia.domain.service.SillyTavernImportService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.SillyTavernChats;
import com.github.enerccio.marginalia.utils.UIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Runs the import of the chats of SillyTavern ({@link SillyTavernImportService}) in a progress dialog with a Stop button.
 * Stopping leaves what was imported until then, the user is told how much it was.
 */
@Configurable
public class SillyTavernImportRunner {

    /**
     * What follows the import, runs on the UI thread also when the import was stopped or failed halfway (what was stored stays,
     * the result is null after a failure).
     */
    @FunctionalInterface
    public interface Continuation {
        void accept(Manuscript result) throws Exception;
    }

    @Autowired
    private Localization loc;

    @Autowired
    private SillyTavernImportService sillyTavernImportService;

    /**
     * Stores the parts of the tree in the book.
     */
    public void run(Manuscript manuscript, SillyTavernChats.Tree tree, Continuation afterwards) {
        AtomicReference<ImportResult<Manuscript>> result = new AtomicReference<>();
        AtomicReference<Exception> failure = new AtomicReference<>();

        ProgressBarDialog progress = new ProgressBarDialog(true, true);
        progress.setTitle(loc.getValue(L.MSG_IMPORTING_SILLYTAVERN));
        progress.setTotal((long) tree.size());
        progress.setCancelLabel(L.LABEL_STOP);
        progress.create();
        progress.setAction(dialog -> {
            try {
                result.set(sillyTavernImportService.importTree(manuscript, tree, dialog::isCancelled, dialog::updateProgress));
            } catch (Exception e) {
                failure.set(e);
            }
        });
        progress.setAfterAction(() -> {
            ImportResult<Manuscript> done = result.get();
            if (failure.get() != null) {
                UIUtils.internalServerError(loc, failure.get());
            } else if (done.isComplete()) {
                Notification.success(String.format(loc.getValue(L.MSG_ARCHIVE_PARTS_ADDED), done.imported()));
            } else {
                Notification.warning(String.format(loc.getValue(L.MSG_ST_IMPORT_STOPPED), done.imported(), done.total()));
            }
            try {
                afterwards.accept(done == null ? null : done.target());
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
        progress.open();
    }
}
