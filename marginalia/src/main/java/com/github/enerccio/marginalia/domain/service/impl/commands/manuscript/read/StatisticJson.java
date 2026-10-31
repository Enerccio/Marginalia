package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.collections.StatisticSide;
import com.github.enerccio.marginalia.domain.components.ActivityTimes;
import com.github.enerccio.marginalia.domain.components.SizeGrowth;
import com.github.enerccio.marginalia.domain.components.StatisticCounts;
import com.google.gson.JsonObject;

import java.util.Locale;

/**
 * How the commands that report the statistics write the numbers: the counts as they are, the times in minutes (rounded down to a
 * tenth), the growth of the size in words and tokens.
 */
final class StatisticJson {

    private StatisticJson() {
    }

    static void counts(JsonObject row, StatisticCounts counts) {
        row.addProperty("tokensSent", counts.prefillTokens());
        row.addProperty("tokensGenerated", counts.generatedTokens());
        row.addProperty("requests", counts.requests());
        row.addProperty("wordsWritten", counts.wordsWritten());
        row.addProperty("wordsGenerated", counts.wordsGenerated());
        row.addProperty("wordsRemoved", counts.wordsRemoved());
        row.addProperty("partsCreated", counts.partsCreated());
        row.addProperty("partsDeleted", counts.partsDeleted());
        row.addProperty("edits", counts.edits());
    }

    static void times(JsonObject row, ActivityTimes times) {
        row.addProperty("minutesOpen", minutes(times.open()));
        row.addProperty("minutesEditing", minutes(times.edit()));
        row.addProperty("minutesGenerating", minutes(times.generate()));
        row.addProperty("minutesAgentsRunning", minutes(times.run()));
    }

    /**
     * What a reader of the numbers has to know about their quality: the day the time is recorded from, and how many of the days were
     * rebuilt from the history of the parts (an estimate, the tokens are a floor).
     */
    static void quality(JsonObject result, com.github.enerccio.marginalia.domain.components.StatisticReport report) {
        if (report.timeSince() != null) {
            result.addProperty("timeRecordedSince", report.timeSince().toString());
        }
        if (report.reconstructedDays() > 0) {
            result.addProperty("rebuiltDays", report.reconstructedDays());
        }
    }

    static void growth(JsonObject row, SizeGrowth growth) {
        if (!growth.known()) {
            row.addProperty("growthKnown", false);
            return;
        }
        row.addProperty("growthWords", growth.bookWords());
        row.addProperty("growthTokens", growth.bookTokens());
        row.addProperty("branchGrowthWords", growth.branchWords());
        row.addProperty("branchGrowthTokens", growth.branchTokens());
    }

    private static double minutes(long millis) {
        return Math.floor(millis / 6_000.0) / 10.0;
    }

    /**
     * The side a command was given: {@code all} (also when left out), {@code me} (or {@code author}) and {@code agents}.
     */
    static StatisticSide side(String text) {
        if (text == null || text.isBlank()) {
            return StatisticSide.ALL;
        }
        return switch (text.strip().toLowerCase(Locale.ROOT)) {
            case "all", "both" -> StatisticSide.ALL;
            case "me", "author" -> StatisticSide.AUTHOR;
            case "agents", "agent" -> StatisticSide.AGENTS;
            default -> throw new IllegalArgumentException("Not a side: " + text + " (all, me or agents)");
        };
    }

}
