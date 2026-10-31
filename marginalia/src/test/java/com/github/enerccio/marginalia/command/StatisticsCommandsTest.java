package com.github.enerccio.marginalia.command;

import com.github.enerccio.marginalia.bound.AgenticSessionPoint;
import com.github.enerccio.marginalia.domain.components.StatisticCounts;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.ManuscriptStatisticService;
import com.github.enerccio.marginalia.test.AgenticRequestAttributes;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@code get-book-statistics} and {@code get-statistics}, run in the session of an agent: no workspace, the book is held by the
 * {@link AgenticSessionPoint}.
 */
class StatisticsCommandsTest extends GenerationTestBase {

    @Autowired
    private CommandService commands;

    @Autowired
    private ManuscriptStatisticService statisticService;

    private AgenticRequestAttributes session;
    private Manuscript manuscript;
    private Manuscript other;

    @BeforeEach
    void setUp() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
        other = manuscriptService.save(newManuscript());
        statisticService.count(manuscript, StatisticCounts.wordsWritten(120).plus(StatisticCounts.partsCreated(3)));
        statisticService.count(manuscript, true, StatisticCounts.wordsGenerated(500));
        statisticService.count(other, StatisticCounts.wordsWritten(30));
        session = AgenticRequestAttributes.install(new AgenticSessionPoint(manuscript));
    }

    @AfterEach
    void tearDown() {
        session.close();
    }

    private JsonObject run(String line) throws Exception {
        return JsonParser.parseString((String) commands.executeLine(line).get(30, TimeUnit.SECONDS)).getAsJsonObject();
    }

    @Test
    void aBookReportsItsDaysAndTotals() throws Exception {
        JsonObject result = run("/get-book-statistics today");

        assertThat(result.get("name").getAsString()).isEqualTo(manuscript.getName());
        assertThat(result.get("from").getAsString()).isEqualTo(result.get("to").getAsString());
        assertThat(result.get("side").getAsString()).isEqualTo("all");
        JsonObject total = result.getAsJsonObject("total");
        assertThat(total.get("wordsWritten").getAsLong()).isEqualTo(120);
        assertThat(total.get("wordsGenerated").getAsLong()).isEqualTo(500);
        assertThat(total.get("partsCreated").getAsLong()).isEqualTo(3);
        assertThat(total.has("minutesOpen")).isTrue();
        assertThat(result.getAsJsonArray("days")).hasSize(1);
        assertThat(result.getAsJsonArray("days").get(0).getAsJsonObject().get("wordsWritten").getAsLong()).isEqualTo(120);
    }

    @Test
    void theRangeIsTheLastWeekWhenLeftOut() throws Exception {
        JsonObject result = run("/get-book-statistics");

        assertThat(java.time.LocalDate.parse(result.get("to").getAsString()).toEpochDay()
                - java.time.LocalDate.parse(result.get("from").getAsString()).toEpochDay()).isEqualTo(6);
        assertThat(result.getAsJsonObject("total").get("wordsWritten").getAsLong()).isEqualTo(120);
    }

    @Test
    void theSideTellsWhoseWorkIsReported() throws Exception {
        assertThat(run("/get-book-statistics today me").getAsJsonObject("total").get("wordsGenerated").getAsLong()).isZero();
        assertThat(run("/get-book-statistics today agents").getAsJsonObject("total").get("wordsGenerated").getAsLong()).isEqualTo(500);
        assertThat(run("/get-book-statistics today agents").getAsJsonObject("total").get("wordsWritten").getAsLong()).isZero();
    }

    @Test
    void anotherRangeHasNothing() throws Exception {
        JsonObject result = run("/get-book-statistics 2020-01-01..2020-01-31");

        assertThat(result.getAsJsonArray("days")).isEmpty();
        assertThat(result.getAsJsonObject("total").get("wordsWritten").getAsLong()).isZero();
    }

    @Test
    void aWrongRangeOrSideIsRefused() {
        assertThatThrownBy(() -> run("/get-book-statistics whenever")).isInstanceOf(ExecutionException.class);
        assertThatThrownBy(() -> run("/get-book-statistics today everybody")).isInstanceOf(ExecutionException.class);
    }

    @Test
    void theBooksAreReportedTogetherByDay() throws Exception {
        JsonObject result = run("/get-statistics today");

        assertThat(result.getAsJsonObject("total").get("wordsWritten").getAsLong()).isEqualTo(150);
        assertThat(result.getAsJsonArray("days")).hasSize(1);
        assertThat(result.has("books")).isFalse();
    }

    @Test
    void theBooksCanBeListedWithTheMostWordsFirst() throws Exception {
        JsonObject result = run("/get-statistics today book");

        assertThat(result.getAsJsonArray("books")).hasSize(2);
        JsonObject first = result.getAsJsonArray("books").get(0).getAsJsonObject();
        assertThat(first.get("handle").getAsString()).isEqualTo(manuscript.getUuid());
        assertThat(first.get("name").getAsString()).isEqualTo(manuscript.getName());
        assertThat(first.get("deleted").getAsBoolean()).isFalse();
        assertThat(first.get("wordsGenerated").getAsLong()).isEqualTo(500);
        assertThat(result.getAsJsonArray("books").get(1).getAsJsonObject().get("handle").getAsString()).isEqualTo(other.getUuid());
        assertThat(result.get("totalBooks").getAsInt()).isEqualTo(2);
        assertThat(result.has("days")).isFalse();
    }

    @Test
    void theSideAppliesToAllTheBooksToo() throws Exception {
        JsonObject result = run("/get-statistics today book agents");

        assertThat(result.getAsJsonArray("books")).hasSize(1);
        assertThat(result.getAsJsonObject("total").get("wordsWritten").getAsLong()).isZero();
    }

    @Test
    void aWrongGroupingIsRefused() {
        assertThatThrownBy(() -> run("/get-statistics today week")).isInstanceOf(ExecutionException.class);
    }

}
