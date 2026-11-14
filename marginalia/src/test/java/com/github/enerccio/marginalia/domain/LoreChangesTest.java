package com.github.enerccio.marginalia.domain;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LoreChanges;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.shared.Registration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The open pages hear of the entries written elsewhere: once per transaction, after the commit, with the page that wrote them.
 */
class LoreChangesTest extends MarginaliaTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private final List<LoreChanges.Change> changes = new CopyOnWriteArrayList<>();
    private Registration listener;
    private Lorebook lorebook;

    @BeforeEach
    void listen() throws Exception {
        login();
        lorebook = new Lorebook();
        lorebook.setName("World");
        lorebook.setSubbooks(new ArrayList<>());
        lorebook = lorebookService.save(lorebook);
        listener = LoreChanges.addListener(changes::add);
    }

    @AfterEach
    void stop() {
        listener.remove();
        UI.setCurrent(null);
    }

    private LorebookEntry entry(String name) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        entry.setPayload("About " + name + ".");
        return lorebookEntryService.save(entry);
    }

    @Test
    void aSaveAndADeleteAreToldWithTheLorebookAndTheEntry() throws Exception {
        LorebookEntry varro = entry("Varro");
        assertThat(changes).hasSize(1);
        assertThat(changes.getFirst().lorebookIds()).containsExactly(lorebook.getId());
        assertThat(changes.getFirst().entryUuids()).containsExactly(varro.getUuid());
        assertThat(changes.getFirst().source()).as("no page: a worker").isNull();

        lorebookEntryService.delete(lorebookEntryService.find(varro.getId()), false);

        assertThat(changes).hasSize(2);
        assertThat(changes.get(1).entryUuids()).containsExactly(varro.getUuid());
    }

    @Test
    void theWritesOfOneTransactionAreOneChangeAfterTheCommit() throws Exception {
        List<Integer> heardInside = new ArrayList<>();
        List<String> uuids = new ArrayList<>();
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            try {
                uuids.add(entry("Varro").getUuid());
                uuids.add(entry("Lia").getUuid());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            heardInside.add(changes.size());
        });

        assertThat(heardInside).containsExactly(0);
        assertThat(changes).hasSize(1);
        assertThat(changes.getFirst().entryUuids()).containsExactlyInAnyOrderElementsOf(uuids);
    }

    @Test
    void aRolledBackTransactionTellsNothing() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.executeWithoutResult(status -> {
            try {
                entry("Varro");
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            status.setRollbackOnly();
        });

        assertThat(changes).isEmpty();
    }

    @Test
    void theChangeKnowsThePageThatMadeIt() throws Exception {
        UI ui = new UI();
        UI.setCurrent(ui);

        entry("Varro");

        assertThat(changes.getFirst().source()).isSameAs(ui);
    }
}
