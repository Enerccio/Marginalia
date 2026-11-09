package com.github.enerccio.marginalia.prompt;

import com.github.enerccio.marginalia.domain.components.LoreUsage;
import com.github.enerccio.marginalia.domain.components.LoreUsageReport;
import com.github.enerccio.marginalia.domain.components.LoreUsageReport.Scope;
import com.github.enerccio.marginalia.domain.components.LoreUsageReport.Status;
import com.github.enerccio.marginalia.domain.components.PartLoreUsage;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The lore usage report ({@link LoreUsageService}): what the parts of a branch or of the whole tree used, over a range of part
 * numbers, and which entries never were used. The records are written by hand, the generation that makes them is tested with
 * the lorebook activation.
 * <pre>
 *   A(1) - B(2) - C(3) - E(4, typed by hand, the active leaf)
 *     \
 *      D(2)
 * </pre>
 */
class LoreUsageServiceTest extends MarginaliaTestBase {

    @Autowired
    private LoreUsageService loreUsageService;

    @Autowired
    private MessagePromptService messagePromptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    private Manuscript book;
    private LorebookEntry always;
    private LorebookEntry sometimes;
    private LorebookEntry deep;
    private LorebookEntry never;
    private LorebookEntry elsewhere;
    private ChatMessage a;
    private ChatMessage b;
    private ChatMessage c;
    private ChatMessage d;
    private ChatMessage e;

    private LorebookEntry entry(Lorebook lorebook, String name) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        return lorebookEntryService.save(entry);
    }

    private ChatMessage part(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return parent == null ? chatMessageService.createRoot(book, message) : chatMessageService.addChild(parent, message);
    }

    private void record(ChatMessage part, Object... usedAndTokensAndDepth) throws Exception {
        LoreUsage usage = new LoreUsage();
        for (int i = 0; i < usedAndTokensAndDepth.length; i += 3) {
            usage.add(((LorebookEntry) usedAndTokensAndDepth[i]).getUuid(), (Integer) usedAndTokensAndDepth[i + 1], (Integer) usedAndTokensAndDepth[i + 2]);
        }
        messagePromptService.store(part, "prompt", "lore", usage);
    }

    @BeforeEach
    void createStory() throws Exception {
        login();
        Lorebook world = new Lorebook();
        world.setName("world");
        world = lorebookService.save(world);
        Lorebook other = new Lorebook();
        other.setName("other");
        other = lorebookService.save(other);
        always = entry(world, "always");
        sometimes = entry(world, "sometimes");
        deep = entry(world, "deep");
        never = entry(world, "never");
        elsewhere = entry(other, "elsewhere");

        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        manuscript.setLorebook(world);
        book = manuscriptService.save(manuscript);
        a = part(null, "A");
        b = part(a, "B");
        c = part(b, "C");
        d = part(a, "D");
        e = part(c, "E");
        book = manuscriptService.update(book, m -> m.setActiveLeaf(e));

        record(a, always, 10, 0, sometimes, 20, 0);
        record(b, always, 10, 0, elsewhere, 5, 0);
        record(c, always, 10, 0, deep, 30, 1);
        record(d, always, 10, 0);
    }

    private LoreUsageReport.Row row(LoreUsageReport report, LorebookEntry entry) {
        return report.rows().stream().filter(r -> r.entry().equals(entry.getUuid())).findFirst().orElseThrow();
    }

    @Test
    void theActiveBranchCountsItsPartsAndTheShareIsOfTheRecordedOnes() throws Exception {
        LoreUsageReport report = loreUsageService.forManuscript(book, Scope.ACTIVE_BRANCH, null, null);

        assertThat(report.parts()).isEqualTo(4);
        assertThat(report.recordedParts()).isEqualTo(3);
        assertThat(report.hasLorebook()).isTrue();
        LoreUsageReport.Row row = row(report, always);
        assertThat(row.used()).isEqualTo(3);
        assertThat(row.status()).isEqualTo(Status.ALWAYS);
        assertThat(row.tokens()).isEqualTo(30);
        assertThat(row.averageTokens()).isEqualTo(10);
        assertThat(row.firstPart()).isEqualTo(1);
        assertThat(row.lastPart()).isEqualTo(3);
        assertThat(row.inLorebook()).isTrue();
        assertThat(row.lorebook()).isEqualTo("world");

        assertThat(row(report, sometimes).status()).isEqualTo(Status.SOMETIMES);
        assertThat(row(report, deep).maxDepth()).isEqualTo(1);
        LoreUsageReport.Row unused = row(report, never);
        assertThat(unused.status()).isEqualTo(Status.NEVER);
        assertThat(unused.used()).isZero();
        assertThat(unused.firstPart()).isZero();
        // the part D is of another branch
        assertThat(report.rows().stream().mapToInt(LoreUsageReport.Row::used).sum()).isEqualTo(3 + 1 + 1 + 1);
    }

    @Test
    void theRowsAreTheMostUsedFirst() throws Exception {
        LoreUsageReport report = loreUsageService.forManuscript(book, Scope.ACTIVE_BRANCH, null, null);

        assertThat(report.rows()).extracting(LoreUsageReport.Row::name).first().isEqualTo("always");
        assertThat(report.rows()).extracting(LoreUsageReport.Row::name).last().isEqualTo("never");
    }

    @Test
    void allBranchesCountTheOtherBranchesToo() throws Exception {
        LoreUsageReport report = loreUsageService.forManuscript(book, Scope.ALL_BRANCHES, null, null);

        assertThat(report.parts()).isEqualTo(5);
        assertThat(report.recordedParts()).isEqualTo(4);
        assertThat(row(report, always).used()).isEqualTo(4);
        assertThat(row(report, always).status()).isEqualTo(Status.ALWAYS);
        // D is part number 2 (the second part of its branch)
        assertThat(row(report, always).lastPart()).isEqualTo(3);
    }

    @Test
    void theRangeIsOfPartNumbersAndIncludesBothEnds() throws Exception {
        LoreUsageReport report = loreUsageService.forManuscript(book, Scope.ACTIVE_BRANCH, 2, 3);

        assertThat(report.from()).isEqualTo(2);
        assertThat(report.to()).isEqualTo(3);
        assertThat(report.parts()).isEqualTo(2);
        assertThat(report.recordedParts()).isEqualTo(2);
        assertThat(row(report, sometimes).status()).isEqualTo(Status.NEVER);
        assertThat(row(report, deep).status()).isEqualTo(Status.SOMETIMES);
        assertThat(row(report, always).status()).isEqualTo(Status.ALWAYS);

        // in the whole tree the numbers are the depth, so 2..2 holds B and D
        LoreUsageReport second = loreUsageService.forManuscript(book, Scope.ALL_BRANCHES, 2, 2);
        assertThat(second.parts()).isEqualTo(2);
        assertThat(row(second, always).used()).isEqualTo(2);
        assertThat(row(second, deep).used()).isZero();
    }

    @Test
    void aRangeWithoutRecordsHasNothingToShare() throws Exception {
        LoreUsageReport report = loreUsageService.forManuscript(book, Scope.ACTIVE_BRANCH, 4, null);

        assertThat(report.parts()).isEqualTo(1);
        assertThat(report.recordedParts()).isZero();
        assertThat(report.rows()).allMatch(row -> row.used() == 0);
    }

    @Test
    void anEntryThatIsNotInTheLorebookAnyMoreIsListedWithItsName() throws Exception {
        LoreUsageReport report = loreUsageService.forManuscript(book, Scope.ACTIVE_BRANCH, null, null);

        LoreUsageReport.Row row = row(report, elsewhere);
        assertThat(row.inLorebook()).isFalse();
        assertThat(row.name()).isEqualTo("elsewhere");
        assertThat(row.lorebook()).isEqualTo("other");
        assertThat(row.used()).isEqualTo(1);
    }

    @Test
    void anEntryThatCannotBeFoundHasNoName() throws Exception {
        LoreUsage usage = new LoreUsage();
        usage.add("not-an-entry", 7, 0);
        messagePromptService.store(c, "prompt", "lore", usage);

        LoreUsageReport report = loreUsageService.forManuscript(book, Scope.ACTIVE_BRANCH, null, null);

        LoreUsageReport.Row row = report.rows().stream().filter(r -> r.entry().equals("not-an-entry")).findFirst().orElseThrow();
        assertThat(row.name()).isNull();
        assertThat(row.inLorebook()).isFalse();
        assertThat(row.tokens()).isEqualTo(7);
    }

    @Test
    void aBookWithoutALorebookStillReportsWhatWasUsed() throws Exception {
        book = manuscriptService.update(book, m -> m.setLorebook(null));

        LoreUsageReport report = loreUsageService.forManuscript(book, Scope.ACTIVE_BRANCH, null, null);

        assertThat(report.hasLorebook()).isFalse();
        assertThat(row(report, always).inLorebook()).isFalse();
        assertThat(row(report, always).used()).isEqualTo(3);
    }

    @Test
    void aPartListsItsEntriesWithNamesTokensAndDepth() throws Exception {
        PartLoreUsage usage = loreUsageService.forPart(c);

        assertThat(usage.recorded()).isTrue();
        assertThat(usage.entries()).extracting(PartLoreUsage.Used::name, PartLoreUsage.Used::lorebook, PartLoreUsage.Used::tokens, PartLoreUsage.Used::depth)
                .containsExactly(org.assertj.core.api.Assertions.tuple("always", "world", 10L, 0),
                        org.assertj.core.api.Assertions.tuple("deep", "world", 30L, 1));
        assertThat(usage.totalTokens()).isEqualTo(40);
    }

    @Test
    void aPartWithoutARecordIsNotRecordedAndAnEmptyRecordIsRecordedWithNothing() throws Exception {
        assertThat(loreUsageService.forPart(e).recorded()).isFalse();
        assertThat(loreUsageService.forPart(e).entries()).isEmpty();

        messagePromptService.store(e, "prompt", "", new LoreUsage());
        PartLoreUsage usage = loreUsageService.forPart(e);
        assertThat(usage.recorded()).isTrue();
        assertThat(usage.entries()).isEmpty();
    }

    @Test
    void anotherUserSeesNothingOfTheBook() throws Exception {
        loginAs(createUser());

        assertThat(loreUsageService.forPart(c).recorded()).isFalse();
        assertThatThrownBy(() -> loreUsageService.forManuscript(book, Scope.ACTIVE_BRANCH, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theNumbersOfAPartAreItsDepthWhateverTheOrderOfCreation() throws Exception {
        // a second branch made later: its parts still number from the root
        ChatMessage f = part(d, "F");
        record(f, sometimes, 3, 0);

        LoreUsageReport report = loreUsageService.forManuscript(book, Scope.ALL_BRANCHES, 3, 3);

        assertThat(report.parts()).isEqualTo(2);
        assertThat(row(report, sometimes).used()).isEqualTo(1);
        assertThat(row(report, sometimes).firstPart()).isEqualTo(3);
    }
}
