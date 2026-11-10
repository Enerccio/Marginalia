package com.github.enerccio.marginalia.ui.dialogs.bookimport;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ArchiveImportService;
import com.github.enerccio.marginalia.domain.service.ArchiveImportService.ImportResult;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ArchiveImportRunner;
import com.github.enerccio.marginalia.ui.dialogs.ProgressBarDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Runs the storing of the parts cut from a document ({@link ArchiveImportService#appendParts}) in a progress dialog with a
 * Stop button. Stopping leaves what was imported until then, the user is told how much it was.
 */
@Configurable
final class DocumentImportRunner {

    @Autowired
    private Localization loc;

    @Autowired
    private ArchiveImportService archiveImportService;

    void run(Manuscript manuscript, List<String> parts, RevisionDetail source, ArchiveImportRunner.Continuation<Manuscript> afterwards) {
        AtomicReference<ImportResult<Manuscript>> result = new AtomicReference<>();
        AtomicReference<Exception> failure = new AtomicReference<>();

        ProgressBarDialog progress = new ProgressBarDialog(true, true);
        progress.setTitle(loc.getValue(L.MSG_IMPORTING_DOCUMENT));
        progress.setTotal((long) parts.size());
        progress.setCancelLabel(L.LABEL_STOP);
        progress.create();
        progress.setAction(dialog -> {
            try {
                result.set(archiveImportService.appendParts(manuscript, parts, source, dialog::isCancelled, dialog::updateProgress));
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
