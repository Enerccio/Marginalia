package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.collections.StatisticRange;
import com.github.enerccio.marginalia.domain.components.SizeSeries;
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
import java.util.Map;

/**
 * {@code /get-book-statistics [range] [side]}: what was done to the open book day by day. Changes nothing; the answer is JSON
 * text, see {@link ManuscriptReadSupport}.
 */
public class GetBookStatisticsCommand extends ManuscriptReadSupport {

    static final String NAME = "get-book-statistics";

    @Autowired
    private StatisticReportService reportService;

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_GET_BOOK_STATISTICS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_GET_BOOK_STATISTICS, llmDescription = L.DESC_COMMAND_GET_BOOK_STATISTICS_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_BOOK_STATISTICS_RETURN))
    public void getBookStatistics(CommandContext context) throws Exception {
        statistics(context, null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_GET_BOOK_STATISTICS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_GET_BOOK_STATISTICS, llmDescription = L.DESC_COMMAND_GET_BOOK_STATISTICS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "range", type = String.class, description = L.DESC_COMMAND_STATISTICS_RANGE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_BOOK_STATISTICS_RETURN))
    public void getBookStatisticsRange(CommandContext context) throws Exception {
        statistics(context, ref(context, "range"), null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_GET_BOOK_STATISTICS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_GET_BOOK_STATISTICS, llmDescription = L.DESC_COMMAND_GET_BOOK_STATISTICS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "range", type = String.class, description = L.DESC_COMMAND_STATISTICS_RANGE_PARAM),
                    @CommandParameter(name = "side", type = String.class, description = L.DESC_COMMAND_STATISTICS_SIDE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_BOOK_STATISTICS_RETURN))
    public void getBookStatisticsSide(CommandContext context) throws Exception {
        statistics(context, ref(context, "range"), ref(context, "side"));
    }

    private void statistics(CommandContext context, String range, String side) throws Exception {
        Book book = new Book(context);
        StatisticRange.Span span = StatisticRange.parse(range, LocalDate.now());
        StatisticReport report = reportService.report(new StatisticFilter(span.from(), span.to(), StatisticJson.side(side),
                book.manuscript.getUuid()));

        JsonObject result = new JsonObject();
        result.addProperty("name", book.manuscript.getName());
        result.addProperty("from", report.filter().from().toString());
        result.addProperty("to", report.filter().to().toString());
        result.addProperty("side", report.filter().side().name().toLowerCase());
        JsonObject total = new JsonObject();
        StatisticJson.counts(total, report.total());
        StatisticJson.times(total, report.times());
        StatisticJson.growth(total, report.growth());
        result.add("total", total);
        StatisticJson.quality(result, report);

        Map<LocalDate, SizeSeries.Point> sizes = new java.util.HashMap<>();
        report.sizes().forEach(point -> sizes.put(point.day(), point));
        JsonArray days = new JsonArray();
        for (StatisticReport.DayLine line : report.days()) {
            JsonObject row = new JsonObject();
            row.addProperty("day", line.day().toString());
            StatisticJson.counts(row, line.counts());
            StatisticJson.times(row, line.times());
            if (line.reconstructed()) {
                row.addProperty("rebuilt", true);
            }
            SizeSeries.Point size = sizes.get(line.day());
            if (size != null && size.present()) {
                row.addProperty("sizeWords", size.bookWords());
                row.addProperty("sizeTokens", size.bookTokens());
                if (size.growthKnown()) {
                    row.addProperty("growthWords", size.growthWords());
                    row.addProperty("growthTokens", size.growthTokens());
                }
            }
            days.add(row);
        }
        result.add("days", days);
        answer(context, result);
    }
}
