package com.github.enerccio.marginalia.extensions.reallangtranslator.service;

import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.PartResult;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.RangePart;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.Summary;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeRuns;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

/**
 * Runs the word extraction over a range of parts on a worker thread of the user's session, one part after the other, see {@link RangeRuns}. In the
 * summary {@code speeches} is the number of terms the model listed and {@code newWords} the number that were added to the glossary.
 */
public final class ExtractionRuns {

    private final RangeRuns runs;

    public ExtractionRuns(ExtractionService service, Logger log) {
        // the mode of the range run is about translating; this work has one action and ignores it. It only reads the parts.
        this.runs = new RangeRuns((book, _, part, languageId) -> work(service, book, part, languageId), "reallang-translator-extraction", log,
                null, Runs.Mode.READ);
    }

    private static PartResult work(ExtractionService service, Manuscript book, ChatMessage part, String languageId) throws Exception {
        ExtractionService.Outcome outcome = service.extract(book, part, languageId);
        return switch (outcome.status()) {
            case DONE -> PartResult.done(outcome.found(), outcome.added(), 0);
            case EMPTY -> PartResult.nothing();
            case NO_LANGUAGE, NO_MODEL, NO_PROMPT -> PartResult.abort(outcome.note());
            case BAD_ANSWER -> PartResult.failed(outcome.note());
        };
    }

    /**
     * Whether an extraction run of the book is on its way.
     */
    public boolean isRunning(Manuscript book) {
        return runs.isRunning(book);
    }

    /**
     * Starts the run; call it on the thread of the user's request.
     *
     * @param languageId the language of the book (its id), null for the first one
     * @param parts      the parts to read, in order
     * @param progress   told on the thread of the page with the number of parts done, before each part
     * @param done       told on the thread of the page when the run is over
     * @return the handle to stop the run, null when a run of the book is on its way already
     */
    public RangeRuns.Handle start(Manuscript book, String languageId, List<RangePart> parts, IntConsumer progress, BiConsumer<Summary, Throwable> done) {
        return runs.start(book, RangeLoop.Mode.TRANSLATE_NEW, languageId, parts, progress, done);
    }
}
