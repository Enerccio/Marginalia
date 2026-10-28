package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService.Reason;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LorebookEntryVerdict;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextArea;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * The preview of the activation of a lorebook: the entries a text would use and why, with the scan depth and the tags
 * of a book.
 */
class LorebookActivationPreviewDialogTest extends AgentTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService entryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private Localization loc;

    // UI.setCurrent keeps a weak reference only
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

    private static Stream<Component> all(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(LorebookActivationPreviewDialogTest::all));
    }

    private Lorebook book(int scanDepth) throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName("World");
        lorebook.setScanDepth(scanDepth);
        lorebook.setSubbooks(new ArrayList<>());
        lorebook = lorebookService.save(lorebook);
        entry(lorebook, "Varro", "Varro serves the Duke of Ostra.", "varro", 1);
        entry(lorebook, "Ostra", "A harbor city.", "ostra", 2);
        entry(lorebook, "Always", "Always here.", null, 3);
        return lorebook;
    }

    private void entry(Lorebook lorebook, String name, String payload, String filter, int order) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        entry.setPayload(payload);
        entry.setFiltering(filter);
        entry.setOrder(order);
        entryService.save(entry);
    }

    @SuppressWarnings("unchecked")
    private static Grid<LorebookEntryVerdict> grid(LorebookActivationPreviewDialog dialog) {
        return (Grid<LorebookEntryVerdict>) all(dialog).filter(Grid.class::isInstance).findFirst().orElseThrow();
    }

    private static List<LorebookEntryVerdict> verdicts(LorebookActivationPreviewDialog dialog) {
        return grid(dialog).getGenericDataView().getItems().toList();
    }

    private static TextArea text(LorebookActivationPreviewDialog dialog) {
        return all(dialog).filter(TextArea.class::isInstance).map(TextArea.class::cast).findFirst().orElseThrow();
    }

    @Test
    void theTextDecidesWhichEntriesAreUsedAndWhy() throws Exception {
        LorebookActivationPreviewDialog dialog = new LorebookActivationPreviewDialog(book(0));
        dialog.create();
        assertThat(verdicts(dialog)).extracting(v -> v.entry().getName(), v -> v.verdict().reason())
                .containsExactly(tuple("Varro", Reason.FILTER_NOT_MATCHED), tuple("Ostra", Reason.FILTER_NOT_MATCHED),
                        tuple("Always", Reason.CONSTANT));

        text(dialog).setValue("Varro walks in.");

        assertThat(verdicts(dialog)).extracting(v -> v.entry().getName(), v -> v.verdict().reason())
                .containsExactly(tuple("Varro", Reason.FILTER_MATCHED), tuple("Ostra", Reason.FILTER_NOT_MATCHED),
                        tuple("Always", Reason.CONSTANT));
    }

    @Test
    void theScanDepthOfTheLorebookBringsInEntriesAndTheTableSaysSo() throws Exception {
        LorebookActivationPreviewDialog dialog = new LorebookActivationPreviewDialog(book(1));
        dialog.create();

        text(dialog).setValue("Varro walks in.");

        assertThat(verdicts(dialog)).extracting(v -> v.entry().getName(), v -> v.verdict().reason(),
                        LorebookEntryVerdict::depth)
                .containsExactly(tuple("Varro", Reason.FILTER_MATCHED, 0), tuple("Ostra", Reason.LORE_MATCHED, 1),
                        tuple("Always", Reason.CONSTANT, 0));
        assertThat(all(dialog).filter(Span.class::isInstance).map(Span.class::cast).map(Span::getText))
                .anyMatch(text -> text.startsWith("3 of 3 entries"));
    }

    @Test
    void theTagsOfTheChosenBookCount() throws Exception {
        Lorebook lorebook = book(0);
        LorebookEntry winter = new LorebookEntry();
        winter.setLorebook(lorebook);
        winter.setName("Winter");
        winter.setPayload("Snow.");
        winter = entryService.save(winter);
        tagRelationService.createRelation(tagService.getOrCreateForUser("winter"), winter);
        Manuscript tagged = manuscript();
        tagRelationService.createRelation(tagService.getOrCreateForUser("winter"), tagged);

        LorebookActivationPreviewDialog dialog = new LorebookActivationPreviewDialog(lorebook);
        dialog.create();
        assertThat(verdicts(dialog)).filteredOn(v -> v.entry().getName().equals("Winter"))
                .extracting(v -> v.verdict().reason()).containsExactly(Reason.MISSING_TAG);

        @SuppressWarnings("unchecked")
        ComboBox<Manuscript> books = (ComboBox<Manuscript>) all(dialog).filter(ComboBox.class::isInstance).findFirst().orElseThrow();
        books.setValue(tagged);

        assertThat(verdicts(dialog)).filteredOn(v -> v.entry().getName().equals("Winter"))
                .extracting(v -> v.verdict().reason()).containsExactly(Reason.CONSTANT);
        assertThat(loc.getValue(L.LABEL_TAKE_TAGS_OF_BOOK)).isNotBlank();
    }
}
