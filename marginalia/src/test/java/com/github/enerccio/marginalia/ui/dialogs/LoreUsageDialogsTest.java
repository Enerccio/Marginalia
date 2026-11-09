package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.components.LoreUsage;
import com.github.enerccio.marginalia.domain.components.LoreUsageReport;
import com.github.enerccio.marginalia.domain.components.LoreUsageReport.Scope;
import com.github.enerccio.marginalia.domain.components.PartLoreUsage;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The two windows of the lore usage: the entries of one part, and the report of the book with its scope, range and filter.
 */
class LoreUsageDialogsTest extends MarginaliaTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private MessagePromptService messagePromptService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private Localization loc;

    // UI.setCurrent keeps a weak reference only
    private UI ui;
    private Manuscript book;
    private LorebookEntry used;
    private LorebookEntry unused;
    private ChatMessage a;
    private ChatMessage b;
    private ChatMessage c;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        Lorebook lorebook = new Lorebook();
        lorebook.setName("world");
        lorebook = lorebookService.save(lorebook);
        used = entry(lorebook, "used");
        unused = entry(lorebook, "unused");
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        manuscript.setLorebook(lorebook);
        book = manuscriptService.save(manuscript);
        a = part(null, "A");
        b = part(a, "B");
        c = part(a, "C");
        book = manuscriptService.update(book, m -> m.setActiveLeaf(b));
        LoreUsage usage = new LoreUsage();
        usage.add(used.getUuid(), 12, 0);
        messagePromptService.store(a, "prompt", "lore", usage);
        messagePromptService.store(c, "prompt", "lore", usage);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
        ui = null;
    }

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

    private static Stream<Component> all(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(LoreUsageDialogsTest::all));
    }

    private static List<String> texts(Component dialog) {
        return all(dialog).filter(Div.class::isInstance).map(Div.class::cast).map(Div::getText).toList();
    }

    @SuppressWarnings("unchecked")
    private static <T> Grid<T> grid(Component dialog) {
        return (Grid<T>) all(dialog).filter(Grid.class::isInstance).findFirst().orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private Select<Scope> scopeSelect(Component dialog) {
        return (Select<Scope>) all(dialog).filter(Select.class::isInstance).map(Select.class::cast)
                .filter(select -> loc.getValue(L.LABEL_PARTS).equals(select.getLabel())).findFirst().orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private Select<Object> showSelect(Component dialog) {
        return (Select<Object>) all(dialog).filter(Select.class::isInstance).map(Select.class::cast)
                .filter(select -> loc.getValue(L.LABEL_JSON_SHOW).equals(select.getLabel())).findFirst().orElseThrow();
    }

    private List<LoreUsageReport.Row> rows(Component dialog) {
        Grid<LoreUsageReport.Row> grid = grid(dialog);
        return grid.getGenericDataView().getItems().toList();
    }

    private BookLoreUsageDialog openBook() throws Exception {
        BookLoreUsageDialog dialog = new BookLoreUsageDialog(book);
        dialog.create();
        return dialog;
    }

    @Test
    void thePartDialogListsTheEntriesTheGenerationUsed() throws Exception {
        LoreUsageDialog dialog = new LoreUsageDialog(a);
        dialog.create();

        Grid<PartLoreUsage.Used> grid = grid(dialog);
        assertThat(grid.getGenericDataView().getItems().toList()).extracting(PartLoreUsage.Used::name).containsExactly("used");
        assertThat(texts(dialog)).contains(String.format(loc.getValue(L.MSG_LORE_USAGE_PART_SUMMARY), 1, "12"));
    }

    @Test
    void aPartWithoutARecordSaysSo() throws Exception {
        LoreUsageDialog dialog = new LoreUsageDialog(b);
        dialog.create();

        assertThat(texts(dialog)).contains(loc.getValue(L.MSG_LORE_USAGE_PART_NOT_RECORDED));
        assertThat(all(dialog).anyMatch(Grid.class::isInstance)).isFalse();
    }

    @Test
    void aRecordOfNothingSaysThatTheLorebookUsedNothing() throws Exception {
        messagePromptService.store(b, "prompt", "", new LoreUsage());
        LoreUsageDialog dialog = new LoreUsageDialog(b);
        dialog.create();

        assertThat(texts(dialog)).contains(loc.getValue(L.MSG_LORE_USAGE_PART_NOTHING));
    }

    @Test
    void theBookDialogStartsWithTheActiveBranch() throws Exception {
        BookLoreUsageDialog dialog = openBook();

        // A (recorded) and B (not) are the branch
        assertThat(texts(dialog)).anyMatch(text -> text.startsWith(String.format(loc.getValue(L.MSG_LORE_USAGE_SUMMARY), 2, 1))
                && text.contains(String.format(loc.getValue(L.MSG_LORE_USAGE_UNRECORDED), 1)));
        assertThat(rows(dialog)).extracting(LoreUsageReport.Row::name).containsExactly("used", "unused");
        assertThat(rows(dialog).getFirst().used()).isEqualTo(1);
    }

    @Test
    void switchingToAllBranchesCountsTheOtherBranchesAndTheRangeLimitsThem() throws Exception {
        BookLoreUsageDialog dialog = openBook();

        scopeSelect(dialog).setValue(Scope.ALL_BRANCHES);
        assertThat(rows(dialog).getFirst().used()).isEqualTo(2);

        IntegerField from = all(dialog).filter(IntegerField.class::isInstance).map(IntegerField.class::cast)
                .filter(field -> loc.getValue(L.LABEL_ANNOTATE_FROM_PART).equals(field.getLabel())).findFirst().orElseThrow();
        from.setValue(2);
        // C is the only recorded part from number 2, A is not in the range
        assertThat(rows(dialog).getFirst().used()).isEqualTo(1);
    }

    @Test
    void theFilterShowsOnlyTheEntriesOfAStatus() throws Exception {
        BookLoreUsageDialog dialog = openBook();

        Select<Object> show = showSelect(dialog);
        Object never = show.getListDataView().getItems().filter(item -> item.toString().equals("NEVER")).findFirst().orElseThrow();
        show.setValue(never);

        assertThat(rows(dialog)).extracting(LoreUsageReport.Row::name).containsExactly("unused");
        assertThat(unused.getName()).isEqualTo("unused");
    }

    @Test
    void whenNoPartHasARecordTheDialogSaysThereIsNothingToCount() throws Exception {
        messagePromptService.store(a, null, null, null);
        messagePromptService.store(c, null, null, null);

        BookLoreUsageDialog dialog = openBook();

        assertThat(texts(dialog)).contains(loc.getValue(L.MSG_LORE_USAGE_NO_RECORDS));
        assertThat(rows(dialog)).isEmpty();
    }
}
