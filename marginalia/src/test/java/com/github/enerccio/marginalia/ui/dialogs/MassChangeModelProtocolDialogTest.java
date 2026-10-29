package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Changing the model and the protocol of many books at once: the list by name with the filter, what an empty choice
 * leaves alone, the check all box, and the books an agent works on, which nothing can be done with.
 */
class MassChangeModelProtocolDialogTest extends AgentTestBase {

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        loginAgentUser();
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private Manuscript book(String name, AI ai, Protocol protocol) throws Exception {
        Manuscript book = new Manuscript();
        book.setName(name);
        book.setAi(ai);
        book.setProtocol(protocol);
        return manuscriptService.save(book);
    }

    private MassChangeModelProtocolDialog open(Runnable onChange) {
        MassChangeModelProtocolDialog dialog = new MassChangeModelProtocolDialog(onChange);
        dialog.create();
        return dialog;
    }

    private static List<String> names(List<MassChangeModelProtocolDialog.Row> rows) {
        return rows.stream().map(MassChangeModelProtocolDialog.Row::name).toList();
    }

    private static MassChangeModelProtocolDialog.Row row(MassChangeModelProtocolDialog dialog, String name) {
        return dialog.getRows().stream().filter(row -> row.name().equals(name)).findFirst().orElseThrow();
    }

    private Manuscript reload(Manuscript book) throws Exception {
        return manuscriptService.findForUser(book.getUuid());
    }

    @Test
    void theBooksAreListedByNameWithTheirModelAndProtocol() throws Exception {
        AI ai = createAI();
        Protocol protocol = protocol();
        book("charlie", null, null);
        book("Alpha", ai, protocol);
        book("bravo", ai, null);

        MassChangeModelProtocolDialog dialog = open(null);

        assertThat(names(dialog.getRows())).containsExactly("Alpha", "bravo", "charlie");
        assertThat(row(dialog, "Alpha").model()).isEqualTo(ai.getName());
        assertThat(row(dialog, "Alpha").protocol()).isEqualTo(protocol.getName());
        assertThat(row(dialog, "bravo").protocol()).isEmpty();
        assertThat(row(dialog, "charlie").model()).isEmpty();
    }

    @Test
    void theNameFilterNarrowsTheListAndUnchecksWhatItHides() throws Exception {
        book("Alpha", null, null);
        book("Bravo", null, null);
        book("alpine", null, null);
        MassChangeModelProtocolDialog dialog = open(null);
        dialog.check(row(dialog, "Bravo"), true);

        dialog.getNameFilter().setValue("ALP");

        assertThat(names(dialog.getShown())).containsExactly("Alpha", "alpine");
        assertThat(row(dialog, "Bravo").checked()).as("a hidden book is not changed").isFalse();

        dialog.getNameFilter().setValue("");
        assertThat(names(dialog.getShown())).containsExactly("Alpha", "alpine", "Bravo");
    }

    @Test
    void anEmptyChoiceLeavesThatPartOfTheBookAlone() throws Exception {
        AI oldAi = createAI();
        Protocol oldProtocol = protocol();
        AI newAi = createAI();
        Protocol newProtocol = protocol();
        Manuscript alpha = book("Alpha", oldAi, oldProtocol);
        Manuscript bravo = book("Bravo", oldAi, oldProtocol);
        Manuscript charlie = book("Charlie", oldAi, oldProtocol);
        AtomicInteger changes = new AtomicInteger();
        MassChangeModelProtocolDialog dialog = open(changes::incrementAndGet);
        dialog.check(row(dialog, "Alpha"), true);
        dialog.check(row(dialog, "Bravo"), true);

        dialog.getPicker().setSelection(newAi, null);
        assertThat(dialog.replace()).isTrue();

        assertThat(reload(alpha).getAi().getId()).isEqualTo(newAi.getId());
        assertThat(reload(alpha).getProtocol().getId()).as("no protocol picked").isEqualTo(oldProtocol.getId());
        assertThat(reload(bravo).getAi().getId()).isEqualTo(newAi.getId());
        assertThat(reload(charlie).getAi().getId()).as("not checked").isEqualTo(oldAi.getId());
        assertThat(changes.get()).isEqualTo(1);

        dialog.getPicker().setSelection(null, newProtocol);
        assertThat(dialog.replace()).isTrue();

        assertThat(reload(alpha).getAi().getId()).as("no model picked").isEqualTo(newAi.getId());
        assertThat(reload(alpha).getProtocol().getId()).isEqualTo(newProtocol.getId());
        assertThat(reload(charlie).getProtocol().getId()).isEqualTo(oldProtocol.getId());
    }

    @Test
    void nothingIsDoneWithoutAChoiceOrWithoutCheckedBooks() throws Exception {
        AI oldAi = createAI();
        Manuscript alpha = book("Alpha", oldAi, null);
        AtomicInteger changes = new AtomicInteger();
        MassChangeModelProtocolDialog dialog = open(changes::incrementAndGet);

        dialog.check(row(dialog, "Alpha"), true);
        assertThat(dialog.replace()).as("neither the model nor the protocol is picked").isFalse();

        dialog.check(row(dialog, "Alpha"), false);
        dialog.getPicker().setSelection(createAI(), null);
        assertThat(dialog.replace()).as("no book is checked").isFalse();

        assertThat(reload(alpha).getAi().getId()).isEqualTo(oldAi.getId());
        assertThat(changes.get()).isZero();
    }

    @Test
    void theCheckAllBoxChecksTheShownBooksOnly() throws Exception {
        book("Alpha", null, null);
        book("Alpine", null, null);
        book("Bravo", null, null);
        MassChangeModelProtocolDialog dialog = open(null);
        dialog.getNameFilter().setValue("alp");

        dialog.getAllBox().setValue(true);
        // the listener reacts to the user only, as the real box does
        dialog.getShown().forEach(row -> dialog.check(row, true));

        assertThat(row(dialog, "Alpha").checked()).isTrue();
        assertThat(row(dialog, "Alpine").checked()).isTrue();
        assertThat(row(dialog, "Bravo").checked()).isFalse();
        assertThat(dialog.getAllBox().getValue()).isTrue();

        dialog.check(row(dialog, "Alpine"), false);
        assertThat(dialog.getAllBox().getValue()).as("one of the shown books is not checked").isFalse();
    }

    @Test
    void theBooksAnAgentWorksOnAreListedButCanNotBeChanged() throws Exception {
        AI oldAi = createAI();
        Manuscript free = book("Free", oldAi, null);
        Manuscript locked = book("Locked", oldAi, null);
        agentRunService.start(agentService.save(newAgent(locked)));
        MassChangeModelProtocolDialog dialog = open(null);

        assertThat(names(dialog.getRows())).as("the working copy of the run is not listed").containsExactly("Free", "Locked");
        assertThat(row(dialog, "Locked").locked()).isTrue();

        AI newAi = createAI();
        dialog.getPicker().setSelection(newAi, null);
        dialog.check(row(dialog, "Locked"), true);
        dialog.check(row(dialog, "Free"), true);
        assertThat(dialog.replace()).isTrue();

        assertThat(reload(free).getAi().getId()).isEqualTo(newAi.getId());
        assertThat(reload(locked).getAi().getId()).isEqualTo(oldAi.getId());
    }
}
