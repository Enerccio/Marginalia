package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The dialog that edits the text of one entry from the lorebook tab of the story editor.
 */
class LorebookEntryEditDialogTest extends MarginaliaTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    // UI.setCurrent keeps a weak reference only
    private UI ui;

    @BeforeEach
    void currentUI() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private LorebookEntry entry() throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName("World");
        lorebook.setSubbooks(new ArrayList<>());
        lorebook = lorebookService.save(lorebook);
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName("Varro");
        entry.setPayload("A captain.");
        entry.setFiltering("varro");
        entry.setFilteringMode(FilteringMode.KEYWORDS_OR);
        return lorebookEntryService.save(entry);
    }

    @Test
    void theFieldsShowTheEntryAndSaveWritesThemBack() throws Exception {
        LorebookEntry entry = entry();
        AtomicInteger saved = new AtomicInteger();
        LorebookEntryEditDialog dialog = new LorebookEntryEditDialog(entry, saved::incrementAndGet).create();
        dialog.open();

        assertThat(dialog.getNameField().getValue()).isEqualTo("Varro");
        assertThat(dialog.getPayloadField().getValue()).isEqualTo("A captain.");
        assertThat(dialog.getFilteringField().getValue()).isEqualTo("varro");
        assertThat(dialog.getEnabledBox().getValue()).isTrue();

        dialog.getNameField().setValue("Captain Varro");
        dialog.getPayloadField().setValue("A captain of the watch.");
        dialog.getEnabledBox().setValue(false);
        dialog.save();

        LorebookEntry reloaded = lorebookEntryService.find(entry.getId());
        assertThat(reloaded.getName()).isEqualTo("Captain Varro");
        assertThat(reloaded.getPayload()).isEqualTo("A captain of the watch.");
        assertThat(reloaded.isEnabled()).isFalse();
        assertThat(saved).hasValue(1);
        assertThat(dialog.isOpened()).isFalse();
    }

    @Test
    void anEmptyNameIsNotSaved() throws Exception {
        LorebookEntry entry = entry();
        AtomicInteger saved = new AtomicInteger();
        LorebookEntryEditDialog dialog = new LorebookEntryEditDialog(entry, saved::incrementAndGet).create();
        dialog.open();

        dialog.getNameField().setValue("  ");
        dialog.save();

        assertThat(lorebookEntryService.find(entry.getId()).getName()).isEqualTo("Varro");
        assertThat(saved).hasValue(0);
        assertThat(dialog.getNameField().isInvalid()).isTrue();
    }
}
