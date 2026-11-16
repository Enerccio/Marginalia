package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.AnnotationTemplate;
import com.github.enerccio.marginalia.domain.model.WatchedPhrase;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;

import java.util.List;

/**
 * The phrase watch list: phrases and regular expressions the author does not want in the prose (clichés, tics of the model), set in the
 * user settings, on folders and on a book. They are not banned tokens: a finished part is scanned, with no model call, and every hit
 * becomes an annotation ({@link com.github.enerccio.marginalia.domain.collections.AnnotationSource#PHRASE}) made from the
 * {@link AnnotationTemplate} the entry names; the rest is what annotations do (the redraft that addresses the notes, dismiss).
 * <p>
 * The lists add up: the user settings, the folders from the top down, the book; an entry of a nearer level with the same phrase
 * ({@link WatchedPhrase#key()}) replaces the one above it, an entry that is off takes it out. Works from any thread of the user (a
 * generation scans from its own).
 */
public interface PhraseWatchService {

    /**
     * An entry of the list a book uses, and where it is set.
     *
     * @param folder the folder that sets it when the origin is {@link EffectiveSetting.Origin#FOLDER}, null otherwise
     */
    record Effective(WatchedPhrase phrase, EffectiveSetting.Origin origin, Folder folder) {
    }

    /**
     * The entries the book uses, in the order of the levels (the user's first); the ones that are off are left out.
     */
    List<Effective> resolve(Manuscript book) throws Exception;

    /**
     * What a book filed in the folder (or a folder made in it) inherits: the user settings and the folder with the ones above it, the
     * entries that are off left out. The top level (null) has the user settings only.
     */
    List<Effective> resolveInherited(Folder folder) throws Exception;

    /**
     * The annotation templates of the user settings.
     */
    List<AnnotationTemplate> getTemplates() throws Exception;

    /**
     * Sets the book's own list and whether the parts generated in it are scanned.
     *
     * @throws IllegalArgumentException when an entry can't be used (its expression is not valid)
     */
    Manuscript setForBook(Manuscript book, List<WatchedPhrase> phrases, boolean scanGenerated) throws Exception;

    /**
     * Sets the folder's own list.
     *
     * @return the saved folder, null when it is gone
     * @throws IllegalArgumentException when an entry can't be used (its expression is not valid)
     */
    Folder setForFolder(Folder folder, List<WatchedPhrase> phrases) throws Exception;

    /**
     * Annotates the hits of the book's list in the part. A hit that already has an annotation of the list for the same phrase at the
     * same place (whatever its status: one the author dismissed stays dismissed) is not annotated again.
     *
     * @return the annotations made
     */
    List<Annotation> scan(Manuscript book, ChatMessage part) throws Exception;

    /**
     * Scans every part of the active branch of the book ({@link #scan}).
     *
     * @return how many annotations were made
     */
    int scanBranch(Manuscript book) throws Exception;
}
