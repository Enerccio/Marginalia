package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ArchiveImportService;
import com.github.enerccio.marginalia.domain.service.ArchiveImportService.ImportResult;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.MarkdownArchive;
import com.github.enerccio.marginalia.utils.UIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Runs the import of an archive of {@code .md} files ({@link ArchiveImportService}) in a progress dialog with a Stop button.
 * Stopping leaves what was imported until then, the user is told how much it was. Shared by the imports of the story, the new
 * book and the lorebook.
 */
@Configurable
@Extendable
public class ArchiveImportRunner {

    /**
     * What follows the import, runs on the UI thread also when the import was stopped or failed halfway (what was stored stays,
     * the result is null after a failure, as the target is not known then).
     */
    @FunctionalInterface
    public interface Continuation<T> {
        void accept(T result) throws Exception;
    }

    @Autowired
    private Localization loc;

    @Autowired
    private ArchiveImportService archiveImportService;

    /**
     * Adds the files of the archive to the end of the story of the book.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class),
                    @APIParameter(name = "content", type = MarkdownArchive.Content.class),
                    @APIParameter(name = "afterwards", type = ArchiveImportRunner.Continuation.class)})
    public void appendParts(Manuscript manuscript, MarkdownArchive.Content content, Continuation<Manuscript> afterwards) {
        run(content, (dialog) -> archiveImportService.appendParts(manuscript, content, dialog::isCancelled, dialog::updateProgress),
                L.MSG_ARCHIVE_PARTS_ADDED, afterwards);
    }

    /**
     * Makes a lorebook of the files of the archive.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "content", type = MarkdownArchive.Content.class),
                    @APIParameter(name = "name", type = String.class),
                    @APIParameter(name = "names", type = List.class),
                    @APIParameter(name = "afterwards", type = ArchiveImportRunner.Continuation.class)})
    public void importLorebook(MarkdownArchive.Content content, String name, List<String> names, Continuation<Lorebook> afterwards) {
        run(content, (dialog) -> archiveImportService.importLorebook(content, name, names, dialog::isCancelled, dialog::updateProgress),
                L.MSG_ARCHIVE_ENTRIES_ADDED, afterwards);
    }

    @FunctionalInterface
    private interface Import<T> {
        ImportResult<T> run(ProgressBarDialog dialog) throws Exception;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "content", type = MarkdownArchive.Content.class),
                    @APIParameter(name = "action", type = ArchiveImportRunner.Import.class),
                    @APIParameter(name = "doneMessage", type = L.class),
                    @APIParameter(name = "afterwards", type = ArchiveImportRunner.Continuation.class)},
            locals = {@APILocals(name = "result", type = AtomicReference.class),
                    @APILocals(name = "failure", type = AtomicReference.class),
                    @APILocals(name = "progress", type = ProgressBarDialog.class)})
    private <T> void run(MarkdownArchive.Content content, Import<T> action, L doneMessage, Continuation<T> afterwards) {
        AtomicReference<ImportResult<T>> result = new AtomicReference<>();
        AtomicReference<Exception> failure = new AtomicReference<>();

        ProgressBarDialog progress = new ProgressBarDialog(true, true);
        progress.setTitle(loc.getValue(L.MSG_IMPORTING_ARCHIVE));
        progress.setTotal((long) content.items().size());
        progress.setCancelLabel(L.LABEL_STOP);
        progress.create();
        progress.setAction(dialog -> {
            try {
                result.set(action.run(dialog));
            } catch (Exception e) {
                failure.set(e);
            }
        });
        progress.setAfterAction(() -> {
            ImportResult<T> done = result.get();
            if (failure.get() != null) {
                UIUtils.internalServerError(loc, failure.get());
            } else if (done.isComplete()) {
                Notification.success(String.format(loc.getValue(doneMessage), done.imported()));
            } else {
                Notification.warning(String.format(loc.getValue(L.MSG_ARCHIVE_IMPORT_STOPPED), done.imported(), done.total()));
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
