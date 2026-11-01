package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.collections.StatisticRange;
import com.github.enerccio.marginalia.domain.components.SizeSeries;
import com.github.enerccio.marginalia.domain.components.StatisticCounts;
import com.github.enerccio.marginalia.domain.components.StatisticFilter;
import com.github.enerccio.marginalia.domain.components.StatisticReport;
import com.github.enerccio.marginalia.domain.service.StatisticReportService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.*;

/**
 * {@code /get-statistics [range] [by] [side]}: what was done to all the books of the author, by day or by book. Changes nothing;
 * the answer is JSON text, see {@link ManuscriptReadSupport}. The books that are gone are in it too, flagged.
 */
public class GetStatisticsCommand extends ManuscriptReadSupport {

    static final String NAME = "get-statistics";

    @Autowired
    private StatisticReportService reportService;

    @Command(commandName = NAME, humanName = L.LABEL_GLOBAL_STATISTICS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_GET_STATISTICS, llmDescription = L.DESC_COMMAND_GET_STATISTICS_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_STATISTICS_RETURN))
    public void getStatistics(CommandContext context) throws Exception {
        statistics(context, null, null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_GLOBAL_STATISTICS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_GET_STATISTICS, llmDescription = L.DESC_COMMAND_GET_STATISTICS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "range", type = String.class, description = L.DESC_COMMAND_STATISTICS_RANGE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_STATISTICS_RETURN))
    public void getStatisticsRange(CommandContext context) throws Exception {
        statistics(context, ref(context, "range"), null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_GLOBAL_STATISTICS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_GET_STATISTICS, llmDescription = L.DESC_COMMAND_GET_STATISTICS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "range", type = String.class, description = L.DESC_COMMAND_STATISTICS_RANGE_PARAM),
                    @CommandParameter(name = "by", type = String.class, description = L.DESC_COMMAND_STATISTICS_BY_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_STATISTICS_RETURN))
    public void getStatisticsBy(CommandContext context) throws Exception {
        statistics(context, ref(context, "range"), ref(context, "by"), null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_GLOBAL_STATISTICS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_GET_STATISTICS, llmDescription = L.DESC_COMMAND_GET_STATISTICS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "range", type = String.class, description = L.DESC_COMMAND_STATISTICS_RANGE_PARAM),
                    @CommandParameter(name = "by", type = String.class, description = L.DESC_COMMAND_STATISTICS_BY_PARAM),
                    @CommandParameter(name = "side", type = String.class, description = L.DESC_COMMAND_STATISTICS_SIDE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_STATISTICS_RETURN))
    public void getStatisticsBySide(CommandContext context) throws Exception {
        statistics(context, ref(context, "range"), ref(context, "by"), ref(context, "side"));
    }

    private void statistics(CommandContext context, String range, String by, String side) throws Exception {
        boolean byBook = by != null && !by.isBlank() && switch (by.strip().toLowerCase(Locale.ROOT)) {
            case "book", "books" -> true;
            case "day", "days" -> false;
            default -> throw new IllegalArgumentException("Not a grouping: " + by + " (day or book)");
        };
        StatisticRange.Span span = StatisticRange.parse(range, LocalDate.now());
        StatisticReport report = reportService.report(new StatisticFilter(span.from(), span.to(), StatisticJson.side(side), null));

        JsonObject result = new JsonObject();
        result.addProperty("from", report.filter().from().toString());
        result.addProperty("to", report.filter().to().toString());
        result.addProperty("side", report.filter().side().name().toLowerCase());
        JsonObject total = new JsonObject();
        StatisticJson.counts(total, report.total());
        StatisticJson.times(total, report.times());
        StatisticJson.growth(total, report.growth());
        result.add("total", total);
        StatisticJson.quality(result, report);

        if (byBook) {
            List<StatisticReport.BookLine> books = report.books().stream()
                    .sorted(Comparator.comparingLong((StatisticReport.BookLine line) -> line.book().counts().wordsAdded()).reversed())
                    .limit(MAX_LIMIT).toList();
            JsonArray rows = new JsonArray();
            for (StatisticReport.BookLine line : books) {
                JsonObject row = new JsonObject();
                row.addProperty("handle", line.book().bookUuid());
                row.addProperty("name", line.book().bookName());
                row.addProperty("folder", line.book().folderPath());
                row.addProperty("deleted", line.book().deleted());
                StatisticCounts counts = line.book().counts();
                StatisticJson.counts(row, counts);
                StatisticJson.times(row, line.times());
                StatisticJson.growth(row, line.growth());
                rows.add(row);
            }
            result.add("books", rows);
            result.addProperty("totalBooks", report.books().size());
        } else {
            Map<LocalDate, SizeSeries.Point> sizes = new HashMap<>();
            report.sizes().forEach(point -> sizes.put(point.day(), point));
            JsonArray rows = new JsonArray();
            for (StatisticReport.DayLine line : report.days()) {
                JsonObject row = new JsonObject();
                row.addProperty("day", line.day().toString());
                StatisticJson.counts(row, line.counts());
                StatisticJson.times(row, line.times());
                if (line.reconstructed()) {
                    row.addProperty("rebuilt", true);
                }
                SizeSeries.Point size = sizes.get(line.day());
                if (size != null && size.present() && size.growthKnown()) {
                    row.addProperty("growthWords", size.growthWords());
                    row.addProperty("growthTokens", size.growthTokens());
                }
                rows.add(row);
            }
            result.add("days", rows);
        }
        answer(context, result);
    }
}
