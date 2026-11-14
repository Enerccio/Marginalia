package com.github.enerccio.marginalia.planner;

import com.github.enerccio.marginalia.domain.model.PlannedScene;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
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

    @Autowired
    private ChatMessageService chatMessageService;

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

    // --- insert between scenes ---

    @Test
    void aSceneIsInsertedAfterTheSceneGiven() throws Exception {
        PlannedScene a = plannerService.add(book, scene("A"));
        PlannedScene c = plannerService.add(book, scene("C"));

        PlannedScene b = plannerService.insert(book, a.getUuid(), scene("B"));
        plannerService.insert(book, c.getUuid(), scene("D"));

        assertThat(titles()).containsExactly("A", "B", "C", "D");
        assertThat(b.getUuid()).isNotIn(a.getUuid(), c.getUuid());
        assertThat(plannerService.list(book).get(1).isDone()).isFalse();
    }

    @Test
    void insertNeedsTheSceneItGoesAfterAndATitle() throws Exception {
        PlannedScene a = plannerService.add(book, scene("A"));

        assertThatThrownBy(() -> plannerService.insert(book, "no-such-scene", scene("B"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> plannerService.insert(book, a.getUuid(), scene(" "))).isInstanceOf(IllegalArgumentException.class);
        assertThat(titles()).containsExactly("A");
    }

    // --- merge ---

    @Test
    void twoScenesBecomeOneInThePlaceOfTheFirst() throws Exception {
        PlannedScene a = plannerService.add(book, scene("A"));
        PlannedScene b = plannerService.add(book, scene("B"));
        PlannedScene c = plannerService.add(book, scene("C"));
        AtomicInteger told = new AtomicInteger();
        plannerService.onChange(book, told::incrementAndGet);

        PlannedScene merged = plannerService.merge(book, a.getUuid(), b.getUuid(),
                new PlannedScene(" A and B ", "the harbor, then the ship", "Anna", "Anna, John, Mara", "Both things happen."));

        assertThat(titles()).containsExactly("A and B", "C");
        PlannedScene stored = plannerService.list(book).get(0);
        assertThat(stored.getUuid()).isEqualTo(a.getUuid()).isEqualTo(merged.getUuid());
        assertThat(stored.getSceneSetting()).isEqualTo("the harbor, then the ship");
        assertThat(stored.getPresentCharacters()).isEqualTo("Anna, John, Mara");
        assertThat(stored.getInstructions()).isEqualTo("Both things happen.");
        assertThat(plannerService.list(book).get(1).getUuid()).isEqualTo(c.getUuid());
        assertThat(told.get()).isEqualTo(1);
    }

    @Test
    void theMergedSceneIsDoneOnlyWhenBothWere() throws Exception {
        PlannedScene a = plannerService.add(book, scene("A"));
        PlannedScene b = plannerService.add(book, scene("B"));
        PlannedScene c = plannerService.add(book, scene("C"));
        plannerService.setDone(book, a.getUuid(), true);
        plannerService.setDone(book, b.getUuid(), true);

        assertThat(plannerService.merge(book, a.getUuid(), b.getUuid(), scene("AB")).isDone()).isTrue();
        assertThat(plannerService.merge(book, a.getUuid(), c.getUuid(), scene("ABC")).isDone()).isFalse();
        assertThat(titles()).containsExactly("ABC");
    }

    @Test
    void mergeNeedsTwoScenesOfTheBookAndATitle() throws Exception {
        PlannedScene a = plannerService.add(book, scene("A"));
        PlannedScene b = plannerService.add(book, scene("B"));

        assertThatThrownBy(() -> plannerService.merge(book, a.getUuid(), a.getUuid(), scene("AA"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> plannerService.merge(book, a.getUuid(), "no-such-scene", scene("AB"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> plannerService.merge(book, a.getUuid(), b.getUuid(), scene(""))).isInstanceOf(IllegalArgumentException.class);
        assertThat(titles()).containsExactly("A", "B");
    }

    // --- import from the branch ---

    private ChatMessage part(ChatMessage parent, String instructions, String setting, String pov, String present) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse("the text of the part");
        message.setInstructions(instructions);
        message.setSceneSetting(setting);
        message.setPovCharacter(pov);
        message.setPresentCharacters(present);
        Thread.sleep(3);
        return parent == null ? chatMessageService.createRoot(book, message) : chatMessageService.addChild(parent, message);
    }

    @Test
    void thePartsOfTheBranchBecomeScenesAtTheEnd() throws Exception {
        plannerService.add(book, scene("Planned before"));
        ChatMessage root = part(null, null, null, null, null);
        ChatMessage first = part(root, "Anna finds the letter.\nShe reads it twice.", "a harbor at night", "Anna", "Anna, John");
        ChatMessage other = part(first, "The other branch.", null, null, null);
        ChatMessage second = part(first, " ", "the ship", null, null);
        assertThat(other.getId()).isNotNull();
        book = manuscriptService.find(book);
        book.setActiveLeaf(second);
        book = manuscriptService.save(book);

        int added = plannerService.importBranch(book);

        // the first part has no turn details, the other branch is not the one the book is on
        assertThat(added).isEqualTo(2);
        assertThat(titles()).containsExactly("Planned before", "Anna finds the letter.", "the ship");
        PlannedScene scene = plannerService.list(book).get(1);
        assertThat(scene.getInstructions()).isEqualTo("Anna finds the letter.\nShe reads it twice.");
        assertThat(scene.getSceneSetting()).isEqualTo("a harbor at night");
        assertThat(scene.getPovCharacter()).isEqualTo("Anna");
        assertThat(scene.getPresentCharacters()).isEqualTo("Anna, John");
        assertThat(scene.isDone()).isFalse();
    }

    @Test
    void aLongFirstLineIsCutForTheTitle() throws Exception {
        ChatMessage root = part(null, "x".repeat(300), null, null, null);
        book = manuscriptService.find(book);
        book.setActiveLeaf(root);
        book = manuscriptService.save(book);

        plannerService.importBranch(book);

        assertThat(titles().get(0)).hasSize(PlannerService.IMPORTED_TITLE_LENGTH).endsWith("…");
        assertThat(plannerService.list(book).get(0).getInstructions()).hasSize(300);
    }

    @Test
    void aBookWithoutPartsImportsNothing() throws Exception {
        AtomicInteger told = new AtomicInteger();
        plannerService.onChange(book, told::incrementAndGet);

        assertThat(plannerService.importBranch(book)).isZero();
        assertThat(titles()).isEmpty();
        assertThat(told.get()).isZero();
    }

    @Test
    void nothingIsImportedWhenTheScenesDoNotFit() throws Exception {
        for (int i = 0; i < PlannerService.MAX_SCENES - 1; i++) {
            plannerService.add(book, scene("Scene " + i));
        }
        ChatMessage root = part(null, "one", null, null, null);
        ChatMessage leaf = part(root, "two", null, null, null);
        book = manuscriptService.find(book);
        book.setActiveLeaf(leaf);
        book = manuscriptService.save(book);

        assertThatThrownBy(() -> plannerService.importBranch(book)).isInstanceOf(IllegalStateException.class);
        assertThat(plannerService.list(book)).hasSize(PlannerService.MAX_SCENES - 1);
    }
}
