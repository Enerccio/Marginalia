package com.github.enerccio.marginalia.extensions.conlangtranslator.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * What an action over a range of the parts of the story does, one part after the other: which parts the range holds for an action, the
 * loop with the stop between parts, and what it adds up to. No thread and no session here: {@link RangeRuns} gives it both.
 */
public final class RangeLoop {

    private RangeLoop() {
    }

    /** What is done to the parts of the range. */
    public enum Mode {
        /** Translate the parts that have no translated speech. */
        TRANSLATE_NEW,
        /** Translate the parts that have translated speech again, from their English. */
        RETRANSLATE,
        /** Put the English back into the parts that have translated speech. */
        REVERT
    }

    /**
     * A part of the story as the range sees it.
     *
     * @param number     its place among the parts that have text, counted from 1 (what the export dialog calls the message)
     * @param translated whether it has a translated speech
     */
    public record RangePart(int number, ChatMessage part, boolean translated) {
    }

    /** How a part went. */
    public enum Kind {
        /** The part was changed. */
        DONE,
        /** There was nothing to change in the part. */
        NOTHING,
        /** The part failed; the run goes on with the next. */
        FAILED,
        /** The whole run cannot go on (no language, no model): the same would happen to every part. */
        ABORT
    }

    /**
     * @param speeches speeches written (translate) or put back (revert)
     * @param newWords words added to the lexicon
     * @param kept     speeches the author edited, which a revert leaves as they are
     * @param note     a short text for the failure or the abort
     */
    public record PartResult(Kind kind, int speeches, int newWords, int kept, String note) {

        public static PartResult done(int speeches, int newWords, int kept) {
            return new PartResult(Kind.DONE, speeches, newWords, kept, "");
        }

        public static PartResult nothing() {
            return new PartResult(Kind.NOTHING, 0, 0, 0, "");
        }

        public static PartResult failed(String note) {
            return new PartResult(Kind.FAILED, 0, 0, 0, note);
        }

        public static PartResult abort(String note) {
            return new PartResult(Kind.ABORT, 0, 0, 0, note);
        }
    }

    /**
     * What the run came to.
     *
     * @param planned      parts the run was to do
     * @param processed    parts it did something to or looked at before it ended
     * @param changed      parts that were changed
     * @param speeches     speeches written or put back
     * @param newWords     new words in the lexicon
     * @param kept         edited speeches left as they are
     * @param failed       parts that failed
     * @param firstFailure the note of the first failure, empty when none
     * @param stopped      the user stopped it
     * @param aborted      it could not go on, {@code firstFailure} says why
     */
    public record Summary(int planned, int processed, int changed, int speeches, int newWords, int kept, int failed, String firstFailure,
                          boolean stopped, boolean aborted) {
    }

    /**
     * The parts of the range the mode works on.
     *
     * @param all  the parts of the branch that have text, in order
     * @param from the first part number of the range
     * @param to   the last part number of the range
     */
    public static List<RangePart> select(Mode mode, List<RangePart> all, int from, int to) {
        List<RangePart> selected = new ArrayList<>();
        for (RangePart part : all) {
            if (part.number() < from || part.number() > to) {
                continue;
            }
            if (mode == Mode.TRANSLATE_NEW ? !part.translated() : part.translated()) {
                selected.add(part);
            }
        }
        return selected;
    }

    /** Does the work of one part. */
    public interface PartAction {
        PartResult apply(ChatMessage part) throws Exception;
    }

    /**
     * Runs the action on the parts one after the other. The stop is looked at before each part (a request to a model that is on its way can't be
     * called back), a part that throws is a failure and the run goes on, an {@link Kind#ABORT} ends it.
     *
     * @param progress told before each part with the number of the parts done so far; may be null
     */
    public static Summary run(List<RangePart> parts, PartAction action, AtomicBoolean stop, Consumer<Integer> progress) {
        int processed = 0;
        int changed = 0;
        int speeches = 0;
        int words = 0;
        int kept = 0;
        int failed = 0;
        String firstFailure = "";
        boolean stopped = false;
        boolean aborted = false;
        for (RangePart item : parts) {
            if (stop.get()) {
                stopped = true;
                break;
            }
            if (progress != null) {
                progress.accept(processed);
            }
            PartResult result;
            try {
                result = action.apply(item.part());
            } catch (Exception e) {
                result = PartResult.failed(String.valueOf(e.getMessage()));
            }
            processed++;
            switch (result.kind()) {
                case DONE -> {
                    changed++;
                    speeches += result.speeches();
                    words += result.newWords();
                    kept += result.kept();
                }
                case NOTHING -> {
                }
                case FAILED -> {
                    failed++;
                    if (firstFailure.isEmpty()) {
                        firstFailure = result.note();
                    }
                }
                case ABORT -> {
                    aborted = true;
                    firstFailure = result.note();
                }
            }
            if (aborted) {
                break;
            }
        }
        return new Summary(parts.size(), processed, changed, speeches, words, kept, failed, firstFailure, stopped, aborted);
    }
}
