package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ArchiveImportService.ImportResult;
import com.github.enerccio.marginalia.utils.SillyTavernChats;

import java.util.function.BooleanSupplier;

/**
 * Stores the chats of SillyTavern ({@link SillyTavernChats}) as the parts of a book. Like {@link ArchiveImportService}, the
 * import is not one transaction: each part is stored by itself, so it can be stopped at any time and what was imported until
 * then stays, and a failure does the same.
 */
public interface SillyTavernImportService {

    /**
     * Stores the parts of the tree and makes the active leaf of the tree the active leaf of the book (when the import is
     * stopped before it, the last part stored is). The parts are added beside the parts the book has. The text of a part is the
     * text of the message, the instructions of the turn are the instructions of the part; the reasoning and the model of the
     * message are kept, the text is not flagged as edited or as written by the model.
     *
     * @param stopped  asked before each part, true stops the import
     * @param onStored called after each part was stored
     * @throws IllegalArgumentException when the manuscript is not the user's
     */
    default ImportResult<Manuscript> importTree(Manuscript manuscript, SillyTavernChats.Tree tree, BooleanSupplier stopped,
                                                Runnable onStored) throws Exception {
        return importTree(manuscript, tree, RevisionDetail.SILLYTAVERN_IMPORT, stopped, onStored);
    }

    /**
     * Like {@link #importTree(Manuscript, SillyTavernChats.Tree, BooleanSupplier, Runnable)} for a tree made of the chats of another
     * program ({@code LmStudioChats} makes the same chats), the revisions of the parts say where the text came from.
     *
     * @param source {@link RevisionDetail#SILLYTAVERN_IMPORT} or {@link RevisionDetail#LMSTUDIO_IMPORT}
     */
    ImportResult<Manuscript> importTree(Manuscript manuscript, SillyTavernChats.Tree tree, RevisionDetail source,
                                        BooleanSupplier stopped, Runnable onStored) throws Exception;
}
