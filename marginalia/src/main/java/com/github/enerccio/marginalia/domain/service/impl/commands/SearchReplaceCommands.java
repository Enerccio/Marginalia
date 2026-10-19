package com.github.enerccio.marginalia.domain.service.impl.commands;

import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.search.replace.TaskProgress;
import com.github.enerccio.marginalia.domain.service.search.replace.TextHit;

import java.util.List;

/**
 * What the two search and replace commands ({@link SearchReplaceChatMessageCommand},
 * {@link SearchReplaceLoreEntryCommand}) have in common: reporting the progress of the search to the progress window.
 */
final class SearchReplaceCommands {

    /** How often the progress window hears about the progress, in ms; it would be flooded otherwise. */
    private static final long PROGRESS_INTERVAL_MILLIS = 150;

    /** Longest search or replacement text shown whole in the question. */
    private static final int SHOWN_LENGTH = 60;

    /** The replace is one transaction, there is no point where it could stop. */
    static final TaskProgress NO_STOP = TaskProgress.NONE;

    private SearchReplaceCommands() {
    }

    /**
     * Progress of a search that is shown as "done / total" under the status and ends when the run is terminated.
     */
    static TaskProgress progress(CommandContext context) {
        return new TaskProgress() {
            private volatile long lastShown;

            @Override
            public void update(long done, long total) {
                long now = System.currentTimeMillis();
                if (total > 0 && (now - lastShown >= PROGRESS_INTERVAL_MILLIS || done >= total)) {
                    lastShown = now;
                    context.setSubStatus(done + " / " + total);
                }
            }

            @Override
            public boolean isCancelled() {
                return context.isTerminated();
            }
        };
    }

    /** How many matches a dry run shows. */
    private static final int SAMPLES = 5;

    /**
     * The first matches of a dry run, one to a line, with what they would become.
     */
    static String samples(List<TextHit> hits) {
        StringBuilder text = new StringBuilder();
        for (TextHit hit : hits.stream().limit(SAMPLES).toList()) {
            text.append("\n…").append(hit.before()).append(" [").append(shorten(hit.match())).append(" -> ")
                    .append(shorten(hit.replacement())).append("] ").append(hit.after()).append('…');
        }
        return text.toString();
    }

    /**
     * A long text cut for the question, with a line break turned into a space.
     */
    static String shorten(String text) {
        String flat = text.replace('\n', ' ');
        return flat.length() <= SHOWN_LENGTH ? flat : flat.substring(0, SHOWN_LENGTH) + "…";
    }

}
