package com.github.enerccio.marginalia.command;

import com.github.enerccio.marginalia.bound.AgenticSessionPoint;
import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.command.CommandScope;
import com.github.enerccio.marginalia.test.AgenticRequestAttributes;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The commands that change the lorebooks of the book, run in the session of an agent. Lorebooks and entries are addressed
 * by handle (their uuid). Nobody can be asked a question there, so the scope answers the questions itself.
 * <p>
 * The lore: lorebook "World" with the entries Anna and John, and the subbook "Places" with Harbor. "Unused" is a lorebook the
 * book does not use.
 */
class LorebookWriteCommandsTest extends GenerationTestBase {

    @Autowired
    private CommandService commands;

    @Autowired
    private CommandScopes commandScopes;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TagRelationService tagRelationService;

    private AgenticRequestAttributes session;
    private Manuscript manuscript;
    private Lorebook world, places, unused;
    private LorebookEntry anna, john, harbor;
    private boolean answer = true;

    @BeforeEach
    void setUp() throws Exception {
        places = lorebookService.save(lorebook("Places"));
        world = lorebookService.save(lorebook("World"));
        world = lorebookService.find(world.getId());
        world.setSubbooks(new ArrayList<>(List.of(places)));
        world = lorebookService.save(world);
        unused = lorebookService.save(lorebook("Unused"));
        anna = entry(world, "Anna", 10, "Anna is a ranger.");
        john = entry(world, "John", 20, "John is her brother.");
        harbor = entry(places, "Harbor", 10, "A harbor town.");

        manuscript = newManuscript();
        manuscript.setLorebook(world);
        manuscript = manuscriptService.save(manuscript);
        session = AgenticRequestAttributes.install(new AgenticSessionPoint(manuscript));
    }

    @AfterEach
    void tearDown() {
        session.close();
    }

    private Lorebook lorebook(String name) {
        Lorebook lorebook = new Lorebook();
        lorebook.setName(name);
        lorebook.setSubbooks(new ArrayList<>());
        return lorebook;
    }

    private LorebookEntry entry(Lorebook book, String name, int order, String payload) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(book);
        entry.setName(name);
        entry.setOrder(order);
        entry.setPayload(payload);
        return lorebookEntryService.save(entry);
    }

    private CommandScope scope() {
        CommandScope session = commandScopes.session();
        return new CommandScope() {
            @Override
            public Manuscript getManuscript() throws Exception {
                return session.getManuscript();
            }

            @Override
            public Lorebook getLorebook() throws Exception {
                return session.getLorebook();
            }

            @Override
            public AgentRun getRun() {
                return session.getRun();
            }

            @Override
            public boolean confirm(CommandContext context, String question) {
                return answer;
            }

            @Override
            public ChatMessage resolveMessage(String uuid) throws Exception {
                return session.resolveMessage(uuid);
            }

            @Override
            public Lorebook resolveLorebook(String uuid) throws Exception {
                return session.resolveLorebook(uuid);
            }

            @Override
            public LorebookEntry resolveEntry(String uuid) throws Exception {
                return session.resolveEntry(uuid);
            }
        };
    }

    private JsonObject run(String line) throws Exception {
        Object result = commands.executeLine(line, false, scope()).get(30, TimeUnit.SECONDS);
        return JsonParser.parseString((String) result).getAsJsonObject();
    }

    private LorebookEntry reload(LorebookEntry entry) throws Exception {
        return lorebookEntryService.find(entry.getId());
    }

    private List<String> tags(LorebookEntry entry, boolean negative) throws Exception {
        return tagRelationService.getTagsForObject(entry, negative).stream().map(Tag::getValue).toList();
    }

    @Test
    void aBookWithoutALorebookGetsItsRootWhenAnEntryIsCreated() throws Exception {
        manuscript.setLorebook(null);
        manuscript = manuscriptService.save(manuscript);

        JsonObject created = run("/create-lore-entry Dragons \"Dragons sleep.\"");

        Lorebook root = lorebookService.find(manuscriptService.find(manuscript.getId()).getLorebook());
        assertThat(root.getName()).isEqualTo(manuscript.getName());
        assertThat(created.get("lorebook").getAsInt()).isEqualTo(1);
        assertThat(lorebookEntryService.getEntriesForLorebook(root)).extracting(LorebookEntry::getName).containsExactly("Dragons");
    }

    @Test
    void aBookWithoutALorebookGetsItsRootWhenALorebookIsCreated() throws Exception {
        manuscript.setLorebook(null);
        manuscript = manuscriptService.save(manuscript);

        JsonObject created = run("/create-lorebook Cities");

        Lorebook root = lorebookService.find(manuscriptService.find(manuscript.getId()).getLorebook());
        assertThat(lorebookService.getSubbooks(root)).extracting(Lorebook::getName).containsExactly("Cities");
        assertThat(created.get("name").getAsString()).isEqualTo("Cities");
    }

    @Test
    void anEntryIsCreatedInTheRootOrInAGivenLorebook() throws Exception {
        JsonObject created = run("/create-lore-entry Dragons \"Dragons sleep.\"");

        LorebookEntry dragons = lorebookEntryService.find(lorebookEntryService.find(created.get("handle").getAsString()));
        assertThat(dragons.getLorebook().getId()).isEqualTo(world.getId());
        assertThat(dragons.getName()).isEqualTo("Dragons");
        assertThat(dragons.isEnabled()).isTrue();
        assertThat(created.get("lorebook").getAsInt()).isEqualTo(1);
        assertThat(created.get("entry").getAsInt()).isEqualTo(3);
        assertThat(created.getAsJsonArray("problems")).isEmpty();

        JsonObject full = run("/create-lore-entry Tavern \"A tavern.\" " + places.getUuid() + " \"note\" false 50 \"tavern|inn\" REGEX BEFORE_USER_PROMPT \"town, fantasy\"");
        LorebookEntry tavern = lorebookEntryService.find(lorebookEntryService.find(full.get("handle").getAsString()));
        assertThat(tavern.getLorebook().getId()).isEqualTo(places.getId());
        assertThat(tavern.getComment()).isEqualTo("note");
        assertThat(tavern.isEnabled()).isFalse();
        assertThat(tavern.getOrder()).isEqualTo(50);
        assertThat(tavern.getFiltering()).isEqualTo("tavern|inn");
        assertThat(tavern.getFilteringMode()).isEqualTo(FilteringMode.REGEX);
        assertThat(tavern.getInsertionMode()).isEqualTo(InsertionMode.BEFORE_USER_PROMPT);
        assertThat(tags(tavern, false)).containsExactlyInAnyOrder("town", "fantasy");
        assertThat(full.get("lorebook").getAsInt()).isEqualTo(2);
    }

    @Test
    void theProblemsOfAnEntryAreReportedButItIsSaved() throws Exception {
        JsonObject created = run("/create-lore-entry Bad \"{{#if}} broken\" " + world.getUuid() + " \"\" true 100 \"(\" REGEX");

        assertThat(created.getAsJsonArray("problems")).hasSize(2);
        assertThat(lorebookEntryService.find(created.get("handle").getAsString())).isNotNull();
        assertThatThrownBy(() -> run("/create-lore-entry Bad text " + world.getUuid() + " \"\" true 100 \"\" Weird")).isInstanceOf(ExecutionException.class)
                .hasMessageContaining("Weird");
    }

    @Test
    void aNumberOrAForeignHandleIsRefused() {
        assertThatThrownBy(() -> run("/update-lore-entry 1 name")).isInstanceOf(ExecutionException.class).hasMessageContaining("handle");
        assertThatThrownBy(() -> run("/create-lore-entry Thing text 2")).isInstanceOf(ExecutionException.class).hasMessageContaining("handle");
        // a lorebook the book does not use is not reachable
        assertThatThrownBy(() -> run("/create-lore-entry Thing text " + unused.getUuid())).isInstanceOf(ExecutionException.class);
    }

    @Test
    void onlyTheGivenFieldsOfAnEntryAreChanged() throws Exception {
        run("/update-lore-entry " + anna.getUuid() + " Annabel");
        assertThat(reload(anna).getName()).isEqualTo("Annabel");
        assertThat(reload(anna).getPayload()).isEqualTo("Anna is a ranger.");

        run("/update-lore-entry " + anna.getUuid() + " Annabel \"Annabel is a ranger.\" \"\" false 5");
        assertThat(reload(anna).getPayload()).isEqualTo("Annabel is a ranger.");
        assertThat(reload(anna).isEnabled()).isFalse();
        assertThat(reload(anna).getOrder()).isEqualTo(5);

        assertThatThrownBy(() -> run("/update-lore-entry " + anna.getUuid())).isInstanceOf(ExecutionException.class);
    }

    @Test
    void anEntryIsDeletedWhenConfirmed() throws Exception {
        answer = false;
        assertThat(commands.executeLine("/delete-lore-entry " + john.getUuid(), false, scope()).get(30, TimeUnit.SECONDS))
                .isEqualTo("Nothing was changed.");
        assertThat(reload(john).isDeleted()).isFalse();

        answer = true;
        assertThat(run("/delete-lore-entry " + john.getUuid()).get("name").getAsString()).isEqualTo("John");
        assertThat(reload(john).isDeleted()).isTrue();
        assertThat(lorebookEntryService.getEntriesForLorebook(world)).extracting(LorebookEntry::getName).containsExactly("Anna");
    }

    @Test
    void anEntryIsMovedToAnotherLorebookOfTheBook() throws Exception {
        JsonObject moved = run("/move-lore-entry " + john.getUuid() + " " + places.getUuid());

        assertThat(reload(john).getLorebook().getId()).isEqualTo(places.getId());
        assertThat(moved.get("lorebook").getAsInt()).isEqualTo(2);
        assertThatThrownBy(() -> run("/move-lore-entry " + john.getUuid() + " " + unused.getUuid())).isInstanceOf(ExecutionException.class);
        assertThat(reload(john).getLorebook().getId()).isEqualTo(places.getId());
    }

    @Test
    void theTagsOfAnEntryAreAddedAndTakenOff() throws Exception {
        run("/set-lore-entry-tags " + anna.getUuid() + " \"people, ranger\" \"\" minor");
        assertThat(tags(anna, false)).containsExactlyInAnyOrder("people", "ranger");
        assertThat(tags(anna, true)).containsExactly("minor");

        JsonObject result = run("/set-lore-entry-tags " + anna.getUuid() + " fantasy People \"\" Minor");
        assertThat(tags(anna, false)).containsExactlyInAnyOrder("ranger", "fantasy");
        assertThat(tags(anna, true)).isEmpty();
        assertThat(result.getAsJsonArray("tags")).hasSize(2);
        assertThatThrownBy(() -> run("/set-lore-entry-tags " + anna.getUuid())).isInstanceOf(ExecutionException.class);
    }

    @Test
    void aLorebookIsCreatedRenamedAndSwitchedOff() throws Exception {
        JsonObject created = run("/create-lorebook Monsters");
        Lorebook monsters = lorebookService.find(lorebookService.find(created.get("handle").getAsString()));
        assertThat(lorebookService.getSubbooks(world)).extracting(Lorebook::getName).containsExactlyInAnyOrder("Places", "Monsters");
        assertThat(created.get("lorebook").getAsInt()).isEqualTo(3);

        JsonObject nested = run("/create-lorebook Dragons " + places.getUuid());
        assertThat(lorebookService.getSubbooks(places)).extracting(Lorebook::getName).containsExactly("Dragons");
        assertThat(nested.get("lorebook").getAsInt()).isEqualTo(4);

        run("/set-lorebook " + monsters.getUuid() + " Beasts false");
        Lorebook renamed = lorebookService.find(monsters.getId());
        assertThat(renamed.getName()).isEqualTo("Beasts");
        assertThat(renamed.isEnabled()).isFalse();
    }

    @Test
    void subbooksAreAddedAndRemovedWithoutCycles() throws Exception {
        Lorebook monsters = lorebookService.find(lorebookService.find(run("/create-lorebook Monsters").get("handle").getAsString()));

        run("/add-subbook " + places.getUuid() + " " + monsters.getUuid());
        assertThat(lorebookService.getSubbooks(places)).extracting(Lorebook::getName).containsExactly("Monsters");

        assertThatThrownBy(() -> run("/add-subbook " + monsters.getUuid() + " " + world.getUuid())).isInstanceOf(ExecutionException.class);
        assertThatThrownBy(() -> run("/add-subbook " + places.getUuid() + " " + unused.getUuid())).isInstanceOf(ExecutionException.class);

        run("/remove-subbook " + places.getUuid() + " " + monsters.getUuid());
        assertThat(lorebookService.getSubbooks(places)).isEmpty();
    }

    @Test
    void aLorebookIsDeletedWithItsEntriesButNotTheRoot() throws Exception {
        assertThatThrownBy(() -> run("/delete-lorebook " + world.getUuid())).isInstanceOf(ExecutionException.class)
                .hasMessageContaining("root");

        answer = false;
        assertThat(commands.executeLine("/delete-lorebook " + places.getUuid(), false, scope()).get(30, TimeUnit.SECONDS))
                .isEqualTo("Nothing was changed.");
        answer = true;
        JsonObject deleted = run("/delete-lorebook " + places.getUuid());

        assertThat(deleted.get("entries").getAsInt()).isEqualTo(1);
        assertThat(lorebookService.find(places.getId()).isDeleted()).isTrue();
        assertThat(reload(harbor).isDeleted()).isTrue();
        assertThat(lorebookService.getSubbooks(world)).isEmpty();
    }
}
