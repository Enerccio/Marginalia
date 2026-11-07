package com.github.enerccio.marginalia.notifications;

import com.github.enerccio.marginalia.domain.service.NotificationService;
import com.github.enerccio.marginalia.domain.service.NotificationService.*;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What can notify, what the user chose, and when a run that ended is worth a notification.
 */
class NotificationServiceTest extends MarginaliaTestBase {

    private static final String OWNER = "test-notifications";

    @Autowired
    private NotificationService service;

    @BeforeEach
    void user() throws Exception {
        login();
    }

    @AfterEach
    void unregister() {
        service.unregisterAll(OWNER);
    }

    private List<String> keys() {
        return service.entries().stream().map(Entry::key).toList();
    }

    @Test
    void aUserWhoChoseNothingIsToldNothing() throws Exception {
        assertThat(service.isEnabled()).isFalse();
        assertThat(service.getChosen()).isEmpty();
        assertThat(service.prepare(GENERATION, false, Outcome.FINISHED, null)).isNull();
    }

    @Test
    void everyInferenceHasASourceAndTheAutomaticOnesHaveTwoEntries() {
        assertThat(keys()).containsSubsequence(GENERATION, ANNOTATE, ANNOTATE + AUTOMATIC_SUFFIX, REDRAFT, REDRAFT + AUTOMATIC_SUFFIX,
                IMPERSONATE, SUMMARY, RESUMMARIZE, HELP_ASSISTANT);
        assertThat(keys()).doesNotContain(GENERATION + AUTOMATIC_SUFFIX, SUMMARY + AUTOMATIC_SUFFIX);
        Entry auto = service.entries().stream().filter(e -> e.key().equals(ANNOTATE + AUTOMATIC_SUFFIX)).findFirst().orElseThrow();
        Entry regular = service.entries().stream().filter(e -> e.key().equals(ANNOTATE)).findFirst().orElseThrow();
        assertThat(auto.automatic()).isTrue();
        assertThat(auto.source()).isEqualTo(ANNOTATE);
        assertThat(auto.name()).startsWith(regular.name()).isNotEqualTo(regular.name());
        assertThat(regular.automatic()).isFalse();
    }

    @Test
    void aChosenSourceNotifiesWithTheBookAndTheOutcome() throws Exception {
        service.save(true, Set.of(GENERATION));

        Notice finished = service.prepare(GENERATION, false, Outcome.FINISHED, "My Book");
        Notice failed = service.prepare(GENERATION, false, Outcome.FAILED, null);

        assertThat(finished).isNotNull();
        assertThat(finished.body()).contains("My Book").startsWith(service.entries().getFirst().name());
        assertThat(finished.tag()).isEqualTo(GENERATION);
        assertThat(failed).isNotNull();
        assertThat(failed.body()).doesNotContain("My Book").isNotEqualTo(finished.body().split("\n")[0]);
        assertThat(service.prepare(IMPERSONATE, false, Outcome.FINISHED, null)).as("not chosen").isNull();
    }

    @Test
    void theMasterSwitchOverridesTheChoice() throws Exception {
        service.save(false, Set.of(GENERATION));

        assertThat(service.getChosen()).containsExactly(GENERATION);
        assertThat(service.prepare(GENERATION, false, Outcome.FINISHED, null)).isNull();
    }

    @Test
    void theRegularEntryDoesNotNotifyForTheAutomaticRun() throws Exception {
        service.save(true, Set.of(ANNOTATE));

        assertThat(service.prepare(ANNOTATE, false, Outcome.FINISHED, null)).isNotNull();
        assertThat(service.prepare(ANNOTATE, true, Outcome.FINISHED, null)).as("only the automatic entry decides").isNull();
    }

    @Test
    void theAutomaticEntryDoesNotNotifyForTheRunTheUserOpened() throws Exception {
        service.save(true, Set.of(ANNOTATE + AUTOMATIC_SUFFIX));

        assertThat(service.prepare(ANNOTATE, true, Outcome.FINISHED, null)).isNotNull();
        assertThat(service.prepare(ANNOTATE, false, Outcome.FINISHED, null)).isNull();
    }

    @Test
    void theAutomaticRunHasItsOwnTagSoItDoesNotReplaceTheOtherNotification() throws Exception {
        service.save(true, Set.of(REDRAFT, REDRAFT + AUTOMATIC_SUFFIX));

        assertThat(service.prepare(REDRAFT, false, Outcome.FINISHED, null).tag())
                .isNotEqualTo(service.prepare(REDRAFT, true, Outcome.FINISHED, null).tag());
    }

    @Test
    void aSourceWithoutAnAutomaticRunIgnoresTheFlag() throws Exception {
        service.save(true, Set.of(SUMMARY));

        assertThat(service.prepare(SUMMARY, true, Outcome.FINISHED, null)).isNotNull();
    }

    @Test
    void anUnknownSourceNeverNotifies() throws Exception {
        service.save(true, Set.of(GENERATION));

        assertThat(service.prepare("nobody", false, Outcome.FINISHED, null)).isNull();
    }

    @Test
    void anExtensionRegistersItsOwnSourceAndItGoesWithTheExtension() throws Exception {
        service.register(OWNER, new Source("test.review", () -> "Review the book", true));

        assertThat(keys()).containsSubsequence("test.review", "test.review" + AUTOMATIC_SUFFIX);
        assertThat(service.entries()).filteredOn(e -> e.key().equals("test.review")).extracting(Entry::name).containsExactly("Review the book");
        service.save(true, Set.of("test.review"));
        assertThat(service.prepare("test.review", false, Outcome.FINISHED, null).body()).startsWith("Review the book");

        service.unregisterAll(OWNER);

        assertThat(keys()).doesNotContain("test.review");
        assertThat(service.prepare("test.review", false, Outcome.FINISHED, null)).isNull();
        assertThat(service.sources()).extracting(Source::id).doesNotContain("test.review");
    }

    @Test
    void whatTheUserChoseForAnUnloadedExtensionStaysWhenTheyChooseAgain() throws Exception {
        service.register(OWNER, new Source("test.review", () -> "Review the book"));
        service.save(true, Set.of("test.review", GENERATION));
        service.unregisterAll(OWNER);

        service.save(true, Set.of(IMPERSONATE));
        assertThat(service.getChosen()).containsExactlyInAnyOrder("test.review", IMPERSONATE);

        service.register(OWNER, new Source("test.review", () -> "Review the book"));
        assertThat(service.prepare("test.review", false, Outcome.FINISHED, null)).as("applies again when it is back").isNotNull();
    }

    @Test
    void aKeyThatIsNotAnEntryIsNotStored() throws Exception {
        service.save(true, Set.of(GENERATION, "garbage", SUMMARY + AUTOMATIC_SUFFIX));

        assertThat(service.getChosen()).containsExactly(GENERATION);
    }

    @Test
    void aNameThatFailsFallsBackToTheIdAndDoesNotBreakTheList() {
        service.register(OWNER, new Source("test.broken", () -> {
            throw new IllegalStateException("no name");
        }));

        assertThat(service.entries()).filteredOn(e -> e.key().equals("test.broken")).extracting(Entry::name).containsExactly("test.broken");
    }

    @Test
    void theChoiceBelongsToTheUser() throws Exception {
        service.save(true, Set.of(GENERATION));

        login();

        assertThat(service.isEnabled()).isFalse();
        assertThat(service.getChosen()).isEmpty();
    }

    @Test
    void theTestNoticeNeedsNoChoice() {
        Notice notice = service.test();

        assertThat(notice.title()).isNotBlank();
        assertThat(notice.body()).isNotBlank();
    }
}
