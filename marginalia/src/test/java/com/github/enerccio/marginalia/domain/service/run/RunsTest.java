package com.github.enerccio.marginalia.domain.service.run;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The registry of runs and the check of the base delete.
 */
class RunsTest extends MarginaliaTestBase {

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    // UI.setCurrent keeps a weak reference only
    private UI page;
    private Manuscript book;
    private ChatMessage part;

    @BeforeEach
    void setUp() throws Exception {
        page = new UI();
        UI.setCurrent(page);
        login();
        Manuscript m = new Manuscript();
        m.setName(uniqueName("book"));
        book = manuscriptService.save(m);
        part = chatMessageService.find(chatMessageService.createPart(book, null, "He drew his sword.", new TurnInput(null, null, null, "Write the duel")).getId());
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    void aPartAWindowWorksOnIsNotDeletedAndShowsWhatWorksOnIt() throws Exception {
        Runs.Run run = Runs.run("test", () -> "Redrafting").book(book).hold(part, Runs.Mode.STRUCTURE).start();

        assertThat(Runs.busyWith(part.getId())).isEqualTo("Redrafting");
        assertThatThrownBy(() -> chatMessageService.delete(chatMessageService.find(part.getId()), false))
                .isInstanceOf(Runs.HeldException.class)
                .satisfies(e -> assertThat(((Runs.HeldException) e).getRun()).isSameAs(run));
        assertThatThrownBy(() -> chatMessageService.delete(chatMessageService.find(part.getId()), true))
                .isInstanceOf(Runs.HeldException.class);
        assertThat(chatMessageService.find(part.getId()).isDeleted()).isFalse();

        run.end();
        assertThat(Runs.busyWith(part.getId())).isNull();
        chatMessageService.delete(chatMessageService.find(part.getId()), false);
        assertThat(chatMessageService.find(part.getId()).isDeleted()).isTrue();
    }

    @Test
    void aRunThatOnlyReadsAPartIsStoppedWhenThePartIsDeleted() throws Exception {
        AtomicInteger stopped = new AtomicInteger();
        Runs.run("test", () -> "Extracting").book(book).hold(part, Runs.Mode.READ).onStop(stopped::incrementAndGet).start();

        assertThat(Runs.busyWith(part.getId())).as("a reading run does not mark the card").isNull();
        chatMessageService.delete(chatMessageService.find(part.getId()), false);

        assertThat(stopped.get()).isEqualTo(1);
    }

    @Test
    void aBookIsNotDeletedWhileARunWorksOnIt() throws Exception {
        Runs.Run run = Runs.run("summaries", () -> "Summarising").book(book).hold(book, Runs.Mode.READ).quiet().start();

        assertThatThrownBy(() -> manuscriptService.delete(manuscriptService.find(book.getId()), false))
                .isInstanceOf(Runs.HeldException.class);
        assertThat(chatMessageService.find(part.getId()).isDeleted()).as("its parts stay too").isFalse();

        run.end();
        manuscriptService.delete(manuscriptService.find(book.getId()), false);
        assertThat(manuscriptService.find(book.getId()).isDeleted()).isTrue();
    }

    @Test
    void theWindowOfABookKeepsOtherPagesFromDeletingItButNotItsOwnPage() throws Exception {
        UI other = new UI();
        Runs.Run window = Runs.run(Runs.BOOK_WINDOW, () -> "Open").book(book).hold(book, Runs.Mode.OPEN).page(other).quiet().start();

        assertThat(Runs.holder(book, Runs.Operation.OPEN)).isSameAs(window);
        assertThatThrownBy(() -> manuscriptService.delete(manuscriptService.find(book.getId()), false))
                .isInstanceOf(Runs.HeldException.class);

        window.end();
        Runs.run(Runs.BOOK_WINDOW, () -> "Open").book(book).hold(book, Runs.Mode.OPEN).page(page).quiet().start();
        assertThat(Runs.holder(book, Runs.Operation.OPEN)).as("the page's own window").isNull();
        manuscriptService.delete(manuscriptService.find(book.getId()), false);
        assertThat(manuscriptService.find(book.getId()).isDeleted()).isTrue();
    }

    @Test
    void twoRunsDoNotWriteOnePart() {
        Runs.Run first = Runs.run("conlang-translator", () -> "Translating").book(book).hold(part, Runs.Mode.WRITE)
                .startUnless(part, Runs.Operation.WRITE);

        assertThatThrownBy(() -> Runs.run("reallang-translator", () -> "Translating").book(book).hold(part, Runs.Mode.WRITE)
                .startUnless(part, Runs.Operation.WRITE))
                .isInstanceOf(Runs.HeldException.class);
        assertThat(Runs.ofBook(book.getId())).containsExactly(first);

        // a window that only needs the part to stay does not keep a translation away
        first.end();
        Runs.run("test", () -> "Redrafting").book(book).hold(part, Runs.Mode.STRUCTURE).start();
        assertThat(Runs.run("reallang-translator", () -> "Translating").book(book).hold(part, Runs.Mode.WRITE)
                .startUnless(part, Runs.Operation.WRITE)).isNotNull();
    }

    @Test
    void aGenerationInItsInferenceHoldsItsPartAndTheBranchAboveItOnly() throws Exception {
        ChatMessage second = chatMessageService.createPart(book, part, "They fought.", new TurnInput(null, null, null, "Go on"));
        ChatMessage written = chatMessageService.createPart(manuscriptService.find(book.getId()), second, "", new TurnInput(null, null, null, "Go on"));
        // a part of another branch, after the first one
        ChatMessage other = new ChatMessage();
        other.setResponse("He ran away.");
        ChatMessage elsewhere = chatMessageService.addPart(manuscriptService.find(book.getId()), part, other);
        Runs.Run generation = Runs.run(Runs.GENERATION, () -> "Generating").book(book).hold(book, Runs.Mode.READ)
                .stage(Runs.Stage.PREPARE).quiet().start();
        generation.hold(written, Runs.Mode.WRITE);
        generation.holdMore(java.util.List.of(part, second, written), Runs.Mode.STRUCTURE);

        // while it prepares, the generation itself moves the end of the branch
        Manuscript prepared = manuscriptService.find(book.getId());
        prepared.setActiveLeaf(second);
        manuscriptService.save(prepared);
        Manuscript back = manuscriptService.find(book.getId());
        back.setActiveLeaf(chatMessageService.find(written.getId()));
        manuscriptService.save(back);

        generation.setStage(Runs.Stage.INFERENCE);
        assertThat(generation.getWrittenPart()).isEqualTo(written.getId());
        assertThat(generation.getParts(Runs.Mode.STRUCTURE)).containsExactlyInAnyOrder(part.getId(), second.getId());

        assertThatThrownBy(() -> chatMessageService.updateResponse(manuscriptService.find(book.getId()), chatMessageService.find(written.getId()), "Mine"))
                .as("the part being written").isInstanceOf(Runs.HeldException.class);
        chatMessageService.updateResponse(manuscriptService.find(book.getId()), chatMessageService.find(second.getId()), "They fought long.");
        assertThat(chatMessageService.find(second.getId()).getResponse()).as("an earlier part is edited").isEqualTo("They fought long.");

        assertThatThrownBy(() -> chatMessageService.deleteNodeAndMigrateChildren(chatMessageService.find(second.getId()),
                manuscriptService.find(book.getId()), false)).isInstanceOf(Runs.HeldException.class);
        assertThatThrownBy(() -> chatMessageService.mergeIntoPrevious(manuscriptService.find(book.getId()), chatMessageService.find(written.getId()),
                null, null, java.util.Set.of(), java.util.Set.of())).isInstanceOf(Runs.HeldException.class);
        assertThatThrownBy(() -> chatMessageService.insertPart(manuscriptService.find(book.getId()), chatMessageService.find(part.getId()),
                "Between", new TurnInput(null, null, null, null))).isInstanceOf(Runs.HeldException.class);
        assertThatThrownBy(() -> chatMessageService.swipeAndSwitch(manuscriptService.find(book.getId()), chatMessageService.find(elsewhere.getId())))
                .as("the end of the branch stays").isInstanceOf(Runs.HeldException.class);
        Manuscript renamed = manuscriptService.find(book.getId());
        renamed.setName("Renamed");
        manuscriptService.save(renamed);

        // the other branch is free
        chatMessageService.insertPart(manuscriptService.find(book.getId()), chatMessageService.find(elsewhere.getId()), "And on.",
                new TurnInput(null, null, null, null));
        chatMessageService.delete(chatMessageService.find(elsewhere.getId()), false);
        assertThat(chatMessageService.find(elsewhere.getId()).isDeleted()).isTrue();

        generation.end();
        chatMessageService.updateResponse(manuscriptService.find(book.getId()), chatMessageService.find(written.getId()), "Mine");
    }

    @Test
    void aTranslationThatWritesAPartDoesNotKeepTheAuthorFromEditingIt() throws Exception {
        Runs.run("reallang-translator", () -> "Translating").book(book).hold(part, Runs.Mode.WRITE).startUnless(part, Runs.Operation.WRITE);

        chatMessageService.updateResponse(manuscriptService.find(book.getId()), chatMessageService.find(part.getId()), "He drew his blade.");

        assertThat(chatMessageService.find(part.getId()).getResponse()).isEqualTo("He drew his blade.");
        assertThatThrownBy(() -> chatMessageService.deleteNodeAndMigrateChildren(chatMessageService.find(part.getId()),
                manuscriptService.find(book.getId()), false)).isInstanceOf(Runs.HeldException.class);
    }

    @Test
    void anEndedRunHoldsNothingAndCanNotHoldMore() {
        Runs.Run run = Runs.run("test", () -> "Redrafting").book(book).start();
        run.end();
        run.hold(part, Runs.Mode.WRITE);

        assertThat(Runs.all()).doesNotContain(run);
        assertThat(Runs.holder(part, Runs.Operation.DELETE)).isNull();
    }

    @Test
    void aStoppedRunEndsStoppedItsNoticeIsClaimedOnceAndItsWorkCanGiveItsStopLater() {
        AtomicInteger stopped = new AtomicInteger();
        Runs.Run run = Runs.run("summaries", () -> "Summarising").book(book).notifies("resummarize", false, "Book").start();
        assertThat(run.canStop()).as("the batch makes its token after the run").isFalse();
        run.setOnStop(stopped::incrementAndGet);
        assertThat(Runs.ofUser(run.getOwnerId())).contains(run);
        assertThat(run.getBookName()).isEqualTo(book.getName());

        run.stop();
        assertThat(stopped.get()).isEqualTo(1);
        assertThat(run.isStopping()).isTrue();
        run.end(Runs.Outcome.FINISHED);

        assertThat(run.getOutcome()).as("it was asked to stop").isEqualTo(Runs.Outcome.STOPPED);
        assertThat(run.claimNotice()).isTrue();
        assertThat(run.claimNotice()).as("one page sends it").isFalse();
    }

    @Test
    void theRunBoundToAThreadIsItsCurrentRunUntilTheBindingIsClosed() throws Exception {
        Runs.Run outer = Runs.run("test", () -> "Outer").start();
        Runs.Run inner = Runs.run("test", () -> "Inner").start();
        try (Runs.Binding _ = outer.bind()) {
            try (Runs.Binding _ = inner.bind()) {
                assertThat(Runs.current()).isSameAs(inner);
                Thread other = new Thread(() -> assertThat(Runs.current()).isNull());
                other.start();
                other.join();
            }
            assertThat(Runs.current()).isSameAs(outer);
            outer.end();
            assertThat(Runs.current()).as("an ended run is nobody's").isNull();
        }
        assertThat(Runs.current()).isNull();
        inner.end();
    }
}
