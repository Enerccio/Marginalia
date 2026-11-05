package com.github.enerccio.marginalia.extensions.conlangtranslator.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.TranslationService.Outcome;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.PartRuns;
import org.slf4j.Logger;

import java.util.function.BiConsumer;

/**
 * Runs the translations of parts on worker threads, see {@link PartRuns}.
 */
public final class TranslationRuns {

    private final TranslationService service;
    private final PartRuns<Outcome> runs;

    public TranslationRuns(TranslationService service, Logger log) {
        this.service = service;
        this.runs = new PartRuns<>("conlang-translator", log);
    }

    /**
     * Whether a part is being translated.
     */
    public boolean isRunning(ChatMessage part) {
        return runs.isRunning(part);
    }

    /**
     * Translates the part on a worker thread; call it on the thread of the user's request.
     *
     * @param done called on the thread of the page (in the session of the user) when the run is over, with the outcome, or with the failure (the outcome is null then);
     *             may be null
     * @return false when the part is being translated already (nothing is started)
     */
    public boolean start(Manuscript manuscript, ChatMessage part, BiConsumer<Outcome, Throwable> done) {
        return start(manuscript, part, null, done);
    }

    /**
     * Translates the part into one language of the book on a worker thread, the rules of {@link #start(Manuscript, ChatMessage, BiConsumer)} apply.
     *
     * @param languageId the language (its id in the library), null for all of the book's
     */
    public boolean start(Manuscript manuscript, ChatMessage part, String languageId, BiConsumer<Outcome, Throwable> done) {
        return runs.start(part, () -> service.translate(manuscript, part, languageId), done);
    }

    /**
     * Translates the sentence selected in the part, see {@link TranslationService#translateSelection}; the rules of {@link #start} apply.
     */
    public boolean startSelection(Manuscript manuscript, ChatMessage part, String expected, int from, int to, BiConsumer<Outcome, Throwable> done) {
        return startSelection(manuscript, part, expected, from, to, null, done);
    }

    /**
     * The sentence is translated into one language of the book (null: the first one).
     */
    public boolean startSelection(Manuscript manuscript, ChatMessage part, String expected, int from, int to, String languageId,
                                  BiConsumer<Outcome, Throwable> done) {
        return runs.start(part, () -> service.translateSelection(manuscript, part, expected, from, to, languageId), done);
    }
}
