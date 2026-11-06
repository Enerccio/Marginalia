package com.github.enerccio.marginalia.planner;

import com.github.enerccio.marginalia.domain.model.PlannedScene;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.PlannerService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The planner of a book: the scenes the author plans to write, stored with the book.
 */
class PlannerServiceTest extends MarginaliaTestBase {

    @Autowired
    private PlannerService plannerService;

    @Autowired
    private ManuscriptService manuscriptService;

    private Manuscript book;

    @BeforeEach
    void createBook() throws Exception {
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
    }

    private PlannedScene scene(String title) {
        return new PlannedScene(title, "a harbor at night", "Anna", "Anna, John", "Anna learns the truth.");
    }

    private List<String> titles() throws Exception {
        return plannerService.list(book).stream().map(PlannedScene::getTitle).toList();
    }

    @Test
    void aNewBookHasNoScenes() throws Exception {
        assertThat(plannerService.list(book)).isEmpty();
    }

    @Test
    void aSceneIsAddedAtTheEndAndKeepsItsDetails() throws Exception {
        plannerService.add(book, scene("The harbor"));
        PlannedScene second = plannerService.add(book, scene("The letter"));

        assertThat(titles()).containsExactly("The harbor", "The letter");
        PlannedScene stored = plannerService.list(book).get(1);
        assertThat(stored.getUuid()).isEqualTo(second.getUuid());
        assertThat(stored.getSceneSetting()).isEqualTo("a harbor at night");
        assertThat(stored.getPovCharacter()).isEqualTo("Anna");
        assertThat(stored.getPresentCharacters()).isEqualTo("Anna, John");
        assertThat(stored.getInstructions()).isEqualTo("Anna learns the truth.");
        assertThat(stored.isDone()).isFalse();
    }

    @Test
    void aSceneIsNeverAddedAsDoneAndGetsItsOwnUuid() throws Exception {
        PlannedScene given = scene("The harbor");
        given.setDone(true);
        String uuid = given.getUuid();
        PlannedScene added = plannerService.add(book, given);

        assertThat(added.isDone()).isFalse();
        assertThat(added.getUuid()).isNotEqualTo(uuid);
    }

    @Test
    void theTextsAreStrippedAndATitleIsRequired() throws Exception {
        PlannedScene padded = scene("  The harbor  ");
        padded.setInstructions("  go  ");
        plannerService.add(book, padded);
        assertThat(plannerService.list(book).get(0).getTitle()).isEqualTo("The harbor");
        assertThat(plannerService.list(book).get(0).getInstructions()).isEqualTo("go");

        assertThatThrownBy(() -> plannerService.add(book, scene("   "))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> plannerService.add(book, scene(null))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> plannerService.add(book, scene("x".repeat(PlannerService.MAX_TITLE_LENGTH + 1))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(plannerService.list(book)).hasSize(1);
    }

    @Test
    void aSceneIsMarkedDoneAndNotDone() throws Exception {
        PlannedScene added = plannerService.add(book, scene("The harbor"));
        plannerService.setDone(book, added.getUuid(), true);
        assertThat(plannerService.list(book).get(0).isDone()).isTrue();
        plannerService.setDone(book, added.getUuid(), false);
        assertThat(plannerService.list(book).get(0).isDone()).isFalse();

        assertThatThrownBy(() -> plannerService.setDone(book, "gone", true)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updatingReplacesTheDetailsButKeepsThePlace() throws Exception {
        PlannedScene first = plannerService.add(book, scene("One"));
        plannerService.add(book, scene("Two"));

        PlannedScene changed = first.copy();
        changed.setTitle("Uno");
        changed.setInstructions("new");
        changed.setDone(true);
        plannerService.update(book, changed);

        PlannedScene stored = plannerService.list(book).get(0);
        assertThat(stored.getTitle()).isEqualTo("Uno");
        assertThat(stored.getInstructions()).isEqualTo("new");
        assertThat(stored.isDone()).isTrue();
        assertThat(titles()).containsExactly("Uno", "Two");

        PlannedScene unknown = scene("x");
        assertThatThrownBy(() -> plannerService.update(book, unknown)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void scenesAreMovedAndStopAtTheEnds() throws Exception {
        PlannedScene a = plannerService.add(book, scene("A"));
        PlannedScene b = plannerService.add(book, scene("B"));
        PlannedScene c = plannerService.add(book, scene("C"));

        plannerService.move(book, c.getUuid(), -1);
        assertThat(titles()).containsExactly("A", "C", "B");
        plannerService.move(book, a.getUuid(), 5);
        assertThat(titles()).containsExactly("C", "B", "A");
        plannerService.move(book, a.getUuid(), 1);
        assertThat(titles()).containsExactly("C", "B", "A");
        plannerService.move(book, b.getUuid(), -9);
        assertThat(titles()).containsExactly("B", "C", "A");
    }

    @Test
    void aSceneIsDeletedAndAMissingOneIsIgnored() throws Exception {
        PlannedScene a = plannerService.add(book, scene("A"));
        plannerService.add(book, scene("B"));

        plannerService.delete(book, a.getUuid());
        plannerService.delete(book, "gone");
        assertThat(titles()).containsExactly("B");
    }

    @Test
    void theListIsACopyNothingChangesWhenTheCallerChangesIt() throws Exception {
        plannerService.add(book, scene("A"));
        plannerService.list(book).get(0).setTitle("changed");
        assertThat(titles()).containsExactly("A");
    }

    @Test
    void listenersAreToldOfEveryChangeUntilTheyAreRemoved() throws Exception {
        AtomicInteger told = new AtomicInteger();
        Runnable stop = plannerService.onChange(book, told::incrementAndGet);

        PlannedScene a = plannerService.add(book, scene("A"));
        plannerService.setDone(book, a.getUuid(), true);
        plannerService.delete(book, a.getUuid());
        assertThat(told.get()).isEqualTo(3);

        stop.run();
        plannerService.add(book, scene("B"));
        assertThat(told.get()).isEqualTo(3);
    }

    @Test
    void theScenesTravelWithABackupOfTheBook() throws Exception {
        plannerService.add(book, scene("A"));
        PlannedScene b = plannerService.add(book, scene("B"));
        plannerService.setDone(book, b.getUuid(), true);

        JsonObject backup = manuscriptService.createBackup(book);
        Manuscript copy = manuscriptService.cloneFromBackup(backup);

        List<PlannedScene> scenes = plannerService.list(copy);
        assertThat(scenes).extracting(PlannedScene::getTitle).containsExactly("A", "B");
        assertThat(scenes).extracting(PlannedScene::isDone).containsExactly(false, true);
    }

    @Test
    void aBookHasAtMostTheMaximumOfScenes() throws Exception {
        for (int i = 0; i < PlannerService.MAX_SCENES; i++) {
            plannerService.add(book, scene("Scene " + i));
        }
        assertThatThrownBy(() -> plannerService.add(book, scene("one too many"))).isInstanceOf(IllegalStateException.class);
    }
}
