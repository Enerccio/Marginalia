package com.github.enerccio.marginalia.statistics;

import com.github.enerccio.marginalia.bound.migration.StatisticsReconstruction;
import com.github.enerccio.marginalia.bound.migration.StatisticsReconstruction.*;
import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.components.StatisticCounts;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The rules that rebuild the statistics of a book from its parts and their versions, without a database.
 */
class StatisticsReconstructionTest {

    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final LocalDate D1 = LocalDate.of(2026, 3, 2);
    private static final LocalDate D2 = D1.plusDays(1);
    private static final LocalDate D3 = D1.plusDays(2);
    private static final LocalDate TODAY = LocalDate.of(2026, 3, 10);

    private final List<PartRow> parts = new ArrayList<>();
    private final List<VersionRow> versions = new ArrayList<>();

    private static Date at(LocalDate day) {
        return Date.from(day.atTime(10, 0).atZone(ZONE).toInstant());
    }

    private PartRow part(long id, Long parent, LocalDate created, int words, boolean deleted, LocalDate modified, String model, Long prompt) {
        PartRow part = new PartRow(id, "part-" + id, parent, at(created), at(modified), deleted, words, words * 2L, prompt, model);
        parts.add(part);
        return part;
    }

    private PartRow part(long id, Long parent, LocalDate created, int words) {
        return part(id, parent, created, words, false, created, null, null);
    }

    private void version(long part, LocalDate day, RevisionSource source, int words) {
        version(part, day, source, null, false, words);
    }

    private void version(long part, LocalDate day, RevisionSource source, String detail, boolean agent, int words) {
        versions.add(new VersionRow(part, at(day), source, detail, agent, words, words * 2L));
    }

    private Result rebuild(Long leaf, boolean deletedBook) {
        return StatisticsReconstruction.rebuild(new BookRow("book", deletedBook, leaf), parts, versions, TODAY, ZONE);
    }

    private static StatisticCounts counts(Result result, LocalDate day, boolean agent) {
        return result.counts().stream().filter(c -> c.day().equals(day) && c.agent() == agent).map(CountRow::counts).findFirst()
                .orElse(StatisticCounts.ZERO);
    }

    @Test
    void aTypedPartThatWasEditedIsAPartCreatedThenAnEdit() {
        part(1, null, D1, 8);
        version(1, D1, RevisionSource.HUMAN, 10);
        version(1, D2, RevisionSource.HUMAN, 14);
        version(1, D3, RevisionSource.HUMAN, 9);

        Result result = rebuild(1L, false);

        assertThat(counts(result, D1, false)).isEqualTo(new StatisticCounts(0, 0, 0, 10, 0, 0, 1, 0, 0));
        assertThat(counts(result, D2, false)).isEqualTo(new StatisticCounts(0, 0, 0, 4, 0, 0, 0, 0, 1));
        assertThat(counts(result, D3, false)).as("shorter: removed").isEqualTo(new StatisticCounts(0, 0, 0, 0, 0, 5, 0, 0, 1));
    }

    @Test
    void aGeneratedPartCountsItsWordsAsGeneratedWithItsTokensAndPrompt() {
        part(1, null, D1, 40, false, D1, "gpt", 900L);
        version(1, D1, RevisionSource.GENERATE, "gpt · chat", false, 40);

        StatisticCounts day = counts(rebuild(1L, false), D1, false);

        assertThat(day.wordsGenerated()).isEqualTo(40);
        assertThat(day.partsCreated()).isEqualTo(1);
        assertThat(day.generatedTokens()).as("the tokens of the version").isEqualTo(80);
        assertThat(day.prefillTokens()).as("the prompt the part saved").isEqualTo(900);
        assertThat(day.requests()).isEqualTo(1);
    }

    @Test
    void thePromptOfThePartGoesToTheDayOfItsLastGeneration() {
        part(1, null, D1, 40, false, D1, "gpt", 700L);
        version(1, D1, RevisionSource.GENERATE, "gpt", false, 30);
        version(1, D2, RevisionSource.REGENERATE, "gpt", false, 40);

        Result result = rebuild(1L, false);

        assertThat(counts(result, D1, false).prefillTokens()).isZero();
        assertThat(counts(result, D2, false).prefillTokens()).isEqualTo(700);
        assertThat(counts(result, D2, false).requests()).isEqualTo(1);
    }

    @Test
    void copiesSnapshotsImportsAndRepairsCountNothing() {
        part(1, null, D1, 10);
        version(1, D1, RevisionSource.DUPLICATE, RevisionDetail.BRANCH.code(), false, 10);
        part(2, 1L, D1, 10);
        version(2, D1, RevisionSource.HUMAN, RevisionDetail.ARCHIVE_IMPORT.code(), false, 10);
        part(3, 2L, D1, 10);
        version(3, D1, RevisionSource.HUMAN, RevisionDetail.SILLYTAVERN_IMPORT.code(), false, 10);
        part(4, 3L, D1, 10);
        version(4, D1, RevisionSource.PLUGIN, null, false, 10);
        version(4, D2, RevisionSource.SNAPSHOT, null, false, 10);

        Result result = rebuild(4L, false);

        assertThat(result.counts()).isEmpty();
        assertThat(result.sizes()).as("but the book has a size").extracting(SizeRow::bookWords).containsExactly(40L);
    }

    @Test
    void theWorkOfAnAgentAndItsMergeAreOnTheAgentsSide() {
        part(1, null, D1, 10);
        version(1, D1, RevisionSource.HUMAN, 10);
        part(2, 1L, D2, 20);
        version(2, D2, RevisionSource.AUTOMATIC, RevisionDetail.AGENT_MERGE.code(), false, 20);
        version(1, D2, RevisionSource.AUTOMATIC, RevisionDetail.COMMAND.code(), true, 15);

        Result result = rebuild(2L, false);

        assertThat(counts(result, D2, true)).isEqualTo(new StatisticCounts(0, 0, 0, 25, 0, 0, 1, 0, 1));
        assertThat(counts(result, D2, false)).isEqualTo(StatisticCounts.ZERO);
        assertThat(counts(result, D1, true)).isEqualTo(StatisticCounts.ZERO);
    }

    @Test
    void aDeletedPartIsAPartDeletedWithItsWordsRemovedOnTheDayOfItsLastChange() {
        part(1, null, D1, 10);
        version(1, D1, RevisionSource.HUMAN, 10);
        part(2, 1L, D1, 30, true, D3, null, null);
        version(2, D1, RevisionSource.HUMAN, 30);

        Result result = rebuild(1L, false);

        assertThat(counts(result, D3, false)).isEqualTo(new StatisticCounts(0, 0, 0, 0, 0, 30, 0, 1, 0));
        assertThat(result.sizes()).extracting(SizeRow::day, SizeRow::bookWords).containsExactly(
                org.assertj.core.groups.Tuple.tuple(D1, 40L), org.assertj.core.groups.Tuple.tuple(D3, 10L));
    }

    @Test
    void thePartsOfABookInTheTrashAreNotDeletedOneByOne() {
        part(1, null, D1, 10, true, D3, null, null);
        version(1, D1, RevisionSource.HUMAN, 10);

        Result result = rebuild(1L, true);

        assertThat(counts(result, D3, false).partsDeleted()).isZero();
        assertThat(result.sizes()).extracting(SizeRow::bookWords).containsExactly(10L);
    }

    @Test
    void aPartWithoutVersionsIsRebuiltFromThePartItself() {
        part(1, null, D1, 12, false, D1, null, null);
        part(2, 1L, D2, 20, false, D2, "gpt", null);
        part(3, 2L, D2, 0);

        Result result = rebuild(2L, false);

        assertThat(counts(result, D1, false)).isEqualTo(new StatisticCounts(0, 0, 0, 12, 0, 0, 1, 0, 0));
        assertThat(counts(result, D2, false).wordsGenerated()).isEqualTo(20);
        assertThat(counts(result, D2, false).partsCreated()).as("a part without text is not created").isEqualTo(1);
    }

    @Test
    void theDayOfTheMigrationAndTheDaysAfterItAreCountedLive() {
        part(1, null, TODAY.minusDays(1), 10);
        version(1, TODAY.minusDays(1), RevisionSource.HUMAN, 10);
        version(1, TODAY, RevisionSource.HUMAN, 50);

        Result result = rebuild(1L, false);

        assertThat(result.counts()).extracting(CountRow::day).containsExactly(TODAY.minusDays(1));
        assertThat(result.sizes()).extracting(SizeRow::day).containsExactly(TODAY.minusDays(1));
    }

    @Test
    void theSizeIsTheNewestVersionOfTheLivePartsAndABranchIsTheWayToTheLeaf() {
        part(1, null, D1, 0);
        version(1, D1, RevisionSource.HUMAN, 10);
        part(2, 1L, D1, 0);
        version(2, D1, RevisionSource.HUMAN, 20);
        part(3, 1L, D2, 0);
        version(3, D2, RevisionSource.HUMAN, 100);
        version(2, D2, RevisionSource.HUMAN, 25);

        Result result = rebuild(2L, false);

        assertThat(result.sizes()).hasSize(2);
        SizeRow first = result.sizes().getFirst();
        assertThat(first.day()).isEqualTo(D1);
        assertThat(first.bookWords()).isEqualTo(30);
        assertThat(first.bookTokens()).isEqualTo(60);
        assertThat(first.branchWords()).isEqualTo(30);
        assertThat(first.leafUuid()).isEqualTo("part-2");
        SizeRow second = result.sizes().get(1);
        assertThat(second.bookWords()).as("10 + 25 + 100").isEqualTo(135);
        assertThat(second.branchWords()).as("the other branch is not on it").isEqualTo(35);
    }

    @Test
    void aDayThatChangedNothingOfTheSizeHasNoRow() {
        part(1, null, D1, 0);
        version(1, D1, RevisionSource.HUMAN, 10);
        version(1, D2, RevisionSource.SNAPSHOT, 10);
        version(1, D3, RevisionSource.HUMAN, 12);

        assertThat(rebuild(1L, false).sizes()).extracting(SizeRow::day).containsExactly(D1, D3);
    }

    @Test
    void aBookWithNothingToRebuildFromHasNoRows() {
        part(1, null, D1, 0);

        assertThat(rebuild(1L, false).isEmpty()).isTrue();
    }

    @Test
    void theLastChangeOfARowIsTheLatestVersionOfTheDay() {
        part(1, null, D1, 0);
        versions.add(new VersionRow(1, Date.from(D1.atTime(9, 0).atZone(ZONE).toInstant()), RevisionSource.HUMAN, null, false, 5, 10));
        versions.add(new VersionRow(1, Date.from(D1.atTime(17, 30).atZone(ZONE).toInstant()), RevisionSource.HUMAN, null, false, 9, 18));

        CountRow row = rebuild(1L, false).counts().getFirst();

        assertThat(row.lastChange()).isEqualTo(Date.from(D1.atTime(17, 30).atZone(ZONE).toInstant()));
    }

}
