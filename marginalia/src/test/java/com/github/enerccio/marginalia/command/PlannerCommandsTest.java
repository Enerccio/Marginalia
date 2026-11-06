package com.github.enerccio.marginalia.command;

import com.github.enerccio.marginalia.domain.model.PlannedScene;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CommandScopes;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.PlannerService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The commands of the planner of a book: scenes are addressed by their number in the list.
 */
class PlannerCommandsTest extends MarginaliaTestBase {

    @Autowired
    private CommandService commands;

    @Autowired
    private CommandScopes commandScopes;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private PlannerService plannerService;

    private Manuscript book;

    @BeforeEach
    void createBook() throws Exception {
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
    }

    private JsonObject run(String line) throws Exception {
        Object result = commands.executeLine(line, false, commandScopes.forManuscript(book)).get(30, TimeUnit.SECONDS);
        return JsonParser.parseString((String) result).getAsJsonObject();
    }

    private List<String> titles() throws Exception {
        return plannerService.list(book).stream().map(PlannedScene::getTitle).toList();
    }

    @Test
    void aSceneIsAddedWithItsDetailsAndListed() throws Exception {
        JsonObject added = run("/add-planned-scene \"The harbor\" \"Anna finds the letter.\" \"A harbor at night\" Anna \"Anna, John\"");

        assertThat(added.get("number").getAsInt()).isEqualTo(1);
        PlannedScene stored = plannerService.list(book).get(0);
        assertThat(stored.getTitle()).isEqualTo("The harbor");
        assertThat(stored.getInstructions()).isEqualTo("Anna finds the letter.");
        assertThat(stored.getSceneSetting()).isEqualTo("A harbor at night");
        assertThat(stored.getPovCharacter()).isEqualTo("Anna");
        assertThat(stored.getPresentCharacters()).isEqualTo("Anna, John");

        run("/add-planned-scene Second");
        JsonObject list = run("/get-planned-scenes");
        assertThat(list.get("total").getAsInt()).isEqualTo(2);
        assertThat(list.get("done").getAsInt()).isZero();
        assertThat(list.getAsJsonArray("scenes").get(1).getAsJsonObject().get("title").getAsString()).isEqualTo("Second");
        assertThat(list.getAsJsonArray("scenes").get(1).getAsJsonObject().get("number").getAsInt()).isEqualTo(2);
    }

    @Test
    void updatingChangesOnlyWhatIsGiven() throws Exception {
        run("/add-planned-scene One \"old instructions\" \"old setting\"");

        run("/update-planned-scene 1 \"Uno\"");
        PlannedScene stored = plannerService.list(book).get(0);
        assertThat(stored.getTitle()).isEqualTo("Uno");
        assertThat(stored.getInstructions()).isEqualTo("old instructions");
        assertThat(stored.getSceneSetting()).isEqualTo("old setting");

        run("/update-planned-scene 1 Uno \"new instructions\"");
        assertThat(plannerService.list(book).get(0).getInstructions()).isEqualTo("new instructions");
        assertThat(plannerService.list(book).get(0).getSceneSetting()).isEqualTo("old setting");
    }

    @Test
    void aSceneIsMarkedDoneAndMovedByNumber() throws Exception {
        run("/add-planned-scene A");
        run("/add-planned-scene B");
        run("/add-planned-scene C");

        assertThat(run("/set-planned-scene-done 2").get("done").getAsBoolean()).isTrue();
        assertThat(plannerService.list(book).get(1).isDone()).isTrue();
        run("/set-planned-scene-done 2 false");
        assertThat(plannerService.list(book).get(1).isDone()).isFalse();

        run("/move-planned-scene 3 1");
        assertThat(titles()).containsExactly("C", "A", "B");
        run("/move-planned-scene 1 99");
        assertThat(titles()).containsExactly("A", "B", "C");
    }

    @Test
    void aSceneIsDeletedByNumber() throws Exception {
        run("/add-planned-scene A");
        run("/add-planned-scene B");

        assertThat(run("/delete-planned-scene 1").get("deleted").getAsString()).isEqualTo("A");
        assertThat(titles()).containsExactly("B");
    }

    @Test
    void aSceneThatIsNotThereIsAnError() throws Exception {
        run("/add-planned-scene A");

        assertThatThrownBy(() -> run("/set-planned-scene-done 5")).isInstanceOf(ExecutionException.class)
                .hasMessageContaining("no scene 5");
        assertThatThrownBy(() -> run("/delete-planned-scene 0")).isInstanceOf(ExecutionException.class);
        assertThat(titles()).containsExactly("A");
    }

    @Test
    void aSceneNeedsATitle() throws Exception {
        assertThatThrownBy(() -> run("/add-planned-scene \" \"")).isInstanceOf(ExecutionException.class);
        assertThat(plannerService.list(book)).isEmpty();
    }
}
