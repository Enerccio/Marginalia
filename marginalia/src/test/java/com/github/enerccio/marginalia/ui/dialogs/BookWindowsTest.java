package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.manuscript.GenerationHold;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * One window per book: the window holds its book while it is open, another page is asked before it takes the book over.
 */
class BookWindowsTest extends MarginaliaTestBase {

    @Autowired
    private ManuscriptService manuscriptService;

    // UI.setCurrent keeps a weak reference only
    private UI here;
    private UI there;
    private Manuscript book;

    @BeforeEach
    void setUp() throws Exception {
        here = new UI();
        there = new UI();
        UI.setCurrent(there);
        login();
        Manuscript m = new Manuscript();
        m.setName(uniqueName("book"));
        book = manuscriptService.save(m);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    void theWindowHoldsItsBookWhileItIsOpen() throws Exception {
        ManuscriptDialog window = new ManuscriptDialog(manuscriptService.find(book.getId()));
        window.create();
        window.open();

        assertThat(Runs.holder(book, Runs.Operation.OPEN)).as("its own page opens it again").isNull();
        assertThat(Runs.holder(book, Runs.Operation.DELETE)).as("and is not refused by it").isNull();

        UI.setCurrent(here);
        Runs.Run hold = Runs.holder(book, Runs.Operation.OPEN);
        assertThat(hold).isNotNull();
        assertThat(hold.getSource()).isEqualTo(Runs.BOOK_WINDOW);
        assertThat(Runs.holder(book, Runs.Operation.DELETE)).as("another page does not delete it").isSameAs(hold);

        UI.setCurrent(there);
        window.close();
        UI.setCurrent(here);
        assertThat(Runs.holder(book, Runs.Operation.OPEN)).isNull();
    }

    @Test
    void theWindowHoldsEverythingWhileTheGenerationPreparesAndOnlyItsPartFromTheInferenceOn() throws Exception {
        ManuscriptDialog window = new ManuscriptDialog(manuscriptService.find(book.getId()));
        window.create();
        window.open();
        assertThat(window.getHold()).isNull();

        // the click: everything is held until the run says more
        window.freeze();
        assertThat(window.getHold()).isEqualTo(GenerationHold.WHOLE);

        Runs.Run generation = Runs.run(Runs.GENERATION, () -> "Generating").book(book).hold(book, Runs.Mode.READ)
                .stage(Runs.Stage.PREPARE).page(there).quiet().start();
        generation.setStage(Runs.Stage.INFERENCE);
        // the page's own listener needs a session; the window reads the run again when it is told
        window.unfreeze();
        window.freeze();
        assertThat(window.getHold()).isNotNull();
        assertThat(window.getHold().isWhole()).isFalse();

        generation.end();
        window.unfreeze();
        assertThat(window.getHold()).isNull();
    }

    @Test
    void aWindowThatLeavesItsPageStopsTheGenerationThePageStarted() {
        AtomicInteger stoppedThere = new AtomicInteger();
        Runs.Run generation = Runs.run(Runs.GENERATION, () -> "Generating").book(book).page(there).quiet()
                .onStop(stoppedThere::incrementAndGet).start();

        BookWindows.stopGenerationOf(book, here);
        assertThat(stoppedThere.get()).as("another page's generation").isZero();

        BookWindows.stopGenerationOf(book, there);
        assertThat(stoppedThere.get()).isEqualTo(1);
        generation.end();
    }

    @Test
    void aBookOpenInAnotherPageIsNotOpenedByTheRestoredWorkspaceAndIsOpenedWhenFree() throws Exception {
        ManuscriptDialog window = new ManuscriptDialog(manuscriptService.find(book.getId()));
        window.create();
        window.open();
        UI.setCurrent(here);
        AtomicInteger opened = new AtomicInteger();

        new BookWindows().open(book, opened::incrementAndGet, true);
        assertThat(opened.get()).isZero();

        // taking it over: the other page loses its hold at once, its window is closed in its own page
        Runs.holder(book, Runs.Operation.OPEN).stop();
        assertThat(Runs.holder(book, Runs.Operation.OPEN)).isNull();
        new BookWindows().open(book, opened::incrementAndGet, false);
        assertThat(opened.get()).isEqualTo(1);
    }
}
