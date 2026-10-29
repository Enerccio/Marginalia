package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Changing the language of many books at once: the list by name with the filter, the check all box, the cases where nothing
 * is done, and the books an agent works on, which nothing can be done with.
 */
class MassChangeLanguageDialogTest extends AgentTestBase {

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

    private Manuscript book(String name, Language language) throws Exception {
        Manuscript book = new Manuscript();
        book.setName(name);
        book.setLanguage(language);
        return manuscriptService.save(book);
    }

    private MassChangeLanguageDialog open(Runnable onChange) {
        MassChangeLanguageDialog dialog = new MassChangeLanguageDialog(onChange);
        dialog.create();
        return dialog;
    }

    private static List<String> names(List<MassChangeLanguageDialog.Row> rows) {
        return rows.stream().map(MassChangeLanguageDialog.Row::name).toList();
    }

    private static MassChangeLanguageDialog.Row row(MassChangeLanguageDialog dialog, String name) {
        return dialog.getRows().stream().filter(row -> row.name().equals(name)).findFirst().orElseThrow();
    }

    private Language languageOf(Manuscript book) throws Exception {
        return manuscriptService.findForUser(book.getUuid()).getLanguage();
    }

    @Test
    void theBooksAreListedByNameWithTheirLanguage() throws Exception {
        book("charlie", Language.GERMAN);
        book("Alpha", Language.ENGLISH);
        book("bravo", Language.JAPANESE);

        MassChangeLanguageDialog dialog = open(null);

        assertThat(names(dialog.getRows())).containsExactly("Alpha", "bravo", "charlie");
        assertThat(row(dialog, "bravo").language()).isEqualTo(Language.JAPANESE);
        assertThat(row(dialog, "charlie").language()).isEqualTo(Language.GERMAN);
    }

    @Test
    void theNameFilterNarrowsTheListAndUnchecksWhatItHides() throws Exception {
        book("Alpha", Language.ENGLISH);
        book("Bravo", Language.ENGLISH);
        book("alpine", Language.ENGLISH);
        MassChangeLanguageDialog dialog = open(null);
        dialog.check(row(dialog, "Bravo"), true);

        dialog.getNameFilter().setValue("ALP");

        assertThat(names(dialog.getShown())).containsExactly("Alpha", "alpine");
        assertThat(row(dialog, "Bravo").checked()).as("a hidden book is not changed").isFalse();
    }

    @Test
    void onlyTheCheckedBooksGetTheLanguage() throws Exception {
        Manuscript alpha = book("Alpha", Language.ENGLISH);
        Manuscript bravo = book("Bravo", Language.ENGLISH);
        Manuscript charlie = book("Charlie", Language.ENGLISH);
        AtomicInteger changes = new AtomicInteger();
        MassChangeLanguageDialog dialog = open(changes::incrementAndGet);
        dialog.check(row(dialog, "Alpha"), true);
        dialog.check(row(dialog, "Bravo"), true);

        dialog.getLanguageBox().setValue(Language.CZECH);
        assertThat(dialog.replace()).isTrue();

        assertThat(languageOf(alpha)).isEqualTo(Language.CZECH);
        assertThat(languageOf(bravo)).isEqualTo(Language.CZECH);
        assertThat(languageOf(charlie)).isEqualTo(Language.ENGLISH);
        assertThat(changes.get()).isEqualTo(1);
    }

    @Test
    void nothingIsDoneWithoutALanguageOrWithoutCheckedBooks() throws Exception {
        Manuscript alpha = book("Alpha", Language.GERMAN);
        AtomicInteger changes = new AtomicInteger();
        MassChangeLanguageDialog dialog = open(changes::incrementAndGet);

        dialog.check(row(dialog, "Alpha"), true);
        assertThat(dialog.replace()).as("no language is picked").isFalse();

        dialog.check(row(dialog, "Alpha"), false);
        dialog.getLanguageBox().setValue(Language.CZECH);
        assertThat(dialog.replace()).as("no book is checked").isFalse();

        assertThat(languageOf(alpha)).isEqualTo(Language.GERMAN);
        assertThat(changes.get()).isZero();
    }

    @Test
    void theCheckAllBoxFollowsTheShownBooks() throws Exception {
        book("Alpha", Language.ENGLISH);
        book("Alpine", Language.ENGLISH);
        book("Bravo", Language.ENGLISH);
        MassChangeLanguageDialog dialog = open(null);
        dialog.getNameFilter().setValue("alp");

        dialog.getShown().forEach(row -> dialog.check(row, true));

        assertThat(dialog.getAllBox().getValue()).isTrue();
        assertThat(row(dialog, "Bravo").checked()).isFalse();

        dialog.check(row(dialog, "Alpine"), false);
        assertThat(dialog.getAllBox().getValue()).isFalse();
    }

    @Test
    void theBooksAnAgentWorksOnAreListedButCanNotBeChanged() throws Exception {
        Manuscript free = book("Free", Language.ENGLISH);
        Manuscript locked = book("Locked", Language.ENGLISH);
        agentRunService.start(agentService.save(newAgent(locked)));
        MassChangeLanguageDialog dialog = open(null);

        assertThat(row(dialog, "Locked").locked()).isTrue();

        dialog.getLanguageBox().setValue(Language.CZECH);
        dialog.check(row(dialog, "Locked"), true);
        dialog.check(row(dialog, "Free"), true);
        assertThat(dialog.replace()).isTrue();

        assertThat(languageOf(free)).isEqualTo(Language.CZECH);
        assertThat(languageOf(locked)).isEqualTo(Language.ENGLISH);
    }
}
