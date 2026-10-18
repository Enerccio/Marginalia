package com.github.enerccio.marginalia.command;

import com.github.enerccio.marginalia.bound.AgenticSessionPoint;
import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.test.AgenticRequestAttributes;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.utils.RegexTimeoutException;
import com.github.enerccio.marginalia.utils.TimeSafeText;
import com.google.gson.JsonArray;
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
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The commands that read the lorebooks of the book, run in the session of an agent. A lorebook is addressed by its
 * number in the tree of the book (the root is 1), an entry by its number in the lorebook, never by an id.
 * <p>
 * The lore: lorebook 1 "World" with the entries 1 Anna, 2 Dragons (tag fantasy, a comment), 3 John (switched off, tag
 * people, a regular expression as keys) and the subbook 2 "Places" with 1 Harbor. A lorebook the book does not use
 * and a deleted entry exist too and must not show.
 */
class LorebookReadCommandsTest extends GenerationTestBase {

    @Autowired
    private CommandService commands;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    private AgenticRequestAttributes session;
    private AgenticSessionPoint sessionPoint;
    private Manuscript manuscript;
    private Lorebook world, places, unused;
    private LorebookEntry anna, dragons, john, harbor;

    @BeforeEach
    void setUp() throws Exception {
        places = lorebookService.save(lorebook("Places"));
        world = lorebookService.save(lorebook("World"));
        world = lorebookService.find(world.getId());
        world.setSubbooks(new ArrayList<>(List.of(places)));
        world = lorebookService.save(world);
        unused = lorebookService.save(lorebook("Unused"));
        tagRelationService.createRelation(tag("geography"), places);

        anna = entry(world, "Anna", 10, e -> e.setPayload("Anna is a ranger from the north."));
        dragons = entry(world, "Dragons", 20, e -> {
            e.setPayload("Dragons sleep under mountains. ".repeat(200));
            e.setComment("Check the consistency with Harbor");
        });
        john = entry(world, "John", 30, e -> {
            e.setPayload("John, Anna's brother.");
            e.setEnabled(false);
            e.setFilteringMode(FilteringMode.REGEX);
            e.setFiltering("jo(h)?n");
            e.setInsertionMode(InsertionMode.BEFORE_USER_PROMPT);
        });
        harbor = entry(places, "Harbor", 10, e -> e.setPayload("A harbor town where Anna lives."));
        entry(world, "Removed", 15, e -> e.setPayload("Dragons are not here."));
        lorebookEntryService.delete(lorebookEntryService.getEntriesForLorebook(world).stream()
                .filter(e -> e.getName().equals("Removed")).findFirst().orElseThrow(), false);
        entry(unused, "Elsewhere", 10, e -> e.setPayload("Anna is mentioned here too."));

        tagRelationService.createRelation(tag("fantasy"), dragons);
        tagRelationService.createRelation(tag("people"), john);
        tagRelationService.createRelation(tag("minor"), john, true);

        manuscript = newManuscript();
        manuscript.setLorebook(world);
        manuscript = manuscriptService.save(manuscript);
        sessionPoint = new AgenticSessionPoint(manuscript);
        session = AgenticRequestAttributes.install(sessionPoint);
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

    private LorebookEntry entry(Lorebook book, String name, int order, Consumer<LorebookEntry> setup) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(book);
        entry.setName(name);
        entry.setOrder(order);
        setup.accept(entry);
        return lorebookEntryService.save(entry);
    }

    private Tag tag(String value) throws Exception {
        Tag tag = new Tag();
        tag.setValue(value);
        return tagService.save(tag);
    }

    private JsonObject run(String line) throws Exception {
        return JsonParser.parseString((String) commands.executeLine(line).get(30, TimeUnit.SECONDS)).getAsJsonObject();
    }

    private static JsonObject at(JsonArray array, int index) {
        return array.get(index).getAsJsonObject();
    }

    private static List<String> strings(JsonArray array) {
        return array.asList().stream().map(e -> e.getAsString()).toList();
    }

    // --- list-lorebooks ---

    @Test
    void listsTheLorebooksOfTheBookWithNumbers() throws Exception {
        JsonObject result = run("/list-lorebooks");

        assertThat(result.get("total").getAsInt()).isEqualTo(2);
        JsonArray books = result.getAsJsonArray("lorebooks");
        assertThat(at(books, 0).get("lorebook").getAsInt()).isEqualTo(1);
        assertThat(at(books, 0).get("name").getAsString()).isEqualTo("World");
        assertThat(at(books, 0).get("entries").getAsInt()).as("the deleted one is not counted").isEqualTo(3);
        assertThat(at(books, 0).get("enabledEntries").getAsInt()).isEqualTo(2);
        assertThat(at(books, 0).get("enabled").getAsBoolean()).isTrue();
        assertThat(at(books, 0).getAsJsonArray("subbooks")).hasSize(1);
        assertThat(at(books, 0).getAsJsonArray("subbooks").get(0).getAsInt()).isEqualTo(2);
        assertThat(at(books, 1).get("lorebook").getAsInt()).isEqualTo(2);
        assertThat(at(books, 1).get("name").getAsString()).isEqualTo("Places");
        assertThat(strings(at(books, 1).getAsJsonArray("tags"))).containsExactly("geography");
        assertThat(result.toString()).doesNotContain(world.getUuid()).doesNotContain("Unused");
    }

    @Test
    void aBookWithoutALorebookFails() throws Exception {
        sessionPoint.setManuscript(manuscriptService.save(withoutLorebook()));

        assertThatThrownBy(() -> run("/list-lorebooks")).isInstanceOf(ExecutionException.class)
                .hasRootCauseMessage("The open book has no lorebook.");
    }

    private Manuscript withoutLorebook() {
        return newManuscript();
    }

    // --- list-lore-entries ---

    @Test
    void listsTheEntriesOfTheRootInTheOrderOfTheLorebook() throws Exception {
        JsonObject result = run("/list-lore-entries");

        assertThat(result.get("lorebook").getAsInt()).isEqualTo(1);
        assertThat(result.get("entries").getAsInt()).isEqualTo(3);
        assertThat(result.get("matching").getAsInt()).isEqualTo(3);
        JsonArray items = result.getAsJsonArray("items");
        assertThat(items).hasSize(3);
        assertThat(items.asList().stream().map(e -> e.getAsJsonObject().get("name").getAsString()))
                .containsExactly("Anna", "Dragons", "John");
        JsonObject johnRow = at(items, 2);
        assertThat(johnRow.get("entry").getAsInt()).isEqualTo(3);
        assertThat(johnRow.get("enabled").getAsBoolean()).isFalse();
        assertThat(johnRow.get("order").getAsInt()).isEqualTo(30);
        assertThat(johnRow.get("filtering").getAsString()).isEqualTo("REGEX");
        assertThat(johnRow.get("insertion").getAsString()).isEqualTo("BEFORE_USER_PROMPT");
        assertThat(strings(johnRow.getAsJsonArray("tags"))).containsExactly("people");
        assertThat(johnRow.get("payloadLength").getAsInt()).isEqualTo("John, Anna's brother.".length());
        assertThat(johnRow.get("preview").getAsString()).isEqualTo("John, Anna's brother.");
        assertThat(at(items, 1).get("preview").getAsString()).as("a long text is cut").hasSizeLessThan(130).endsWith("…");
        assertThat(items.toString()).doesNotContain(anna.getUuid());
    }

    @Test
    void listsTheEntriesOfASubbook() throws Exception {
        JsonObject result = run("/list-lore-entries 2");

        assertThat(result.get("name").getAsString()).isEqualTo("Places");
        assertThat(at(result.getAsJsonArray("items"), 0).get("name").getAsString()).isEqualTo("Harbor");
    }

    @Test
    void theFilterMatchesTheNameAndTheTagsAndKeepsTheNumbers() throws Exception {
        JsonObject byName = run("/list-lore-entries 1 drag");
        assertThat(byName.get("matching").getAsInt()).isEqualTo(1);
        assertThat(at(byName.getAsJsonArray("items"), 0).get("entry").getAsInt()).as("number in the whole lorebook").isEqualTo(2);

        JsonObject byTag = run("/list-lore-entries 1 PEOPLE");
        assertThat(byTag.getAsJsonArray("items")).hasSize(1);
        assertThat(at(byTag.getAsJsonArray("items"), 0).get("name").getAsString()).isEqualTo("John");

        assertThat(run("/list-lore-entries 1 nothing-like-this").getAsJsonArray("items")).isEmpty();
    }

    @Test
    void pagesTheEntries() throws Exception {
        JsonObject page = run("/list-lore-entries 1 \"\" 1 1");

        assertThat(page.get("matching").getAsInt()).isEqualTo(3);
        assertThat(page.get("offset").getAsInt()).isEqualTo(1);
        assertThat(page.getAsJsonArray("items")).hasSize(1);
        assertThat(at(page.getAsJsonArray("items"), 0).get("name").getAsString()).isEqualTo("Dragons");
    }

    @Test
    void aLorebookTheBookDoesNotUseIsNotReachable() {
        assertThatThrownBy(() -> run("/list-lore-entries 3")).isInstanceOf(ExecutionException.class)
                .hasRootCauseMessage("There is no lorebook 3, the book has 2 lorebooks (see list-lorebooks).");
        assertThatThrownBy(() -> run("/list-lore-entries 0")).isInstanceOf(ExecutionException.class);
    }

    // --- get-lore-entry ---

    @Test
    void getsAnEntryWithItsSettings() throws Exception {
        JsonObject result = run("/get-lore-entry 3");

        assertThat(result.get("lorebook").getAsInt()).isEqualTo(1);
        assertThat(result.get("entry").getAsInt()).isEqualTo(3);
        assertThat(result.get("name").getAsString()).isEqualTo("John");
        assertThat(result.get("enabled").getAsBoolean()).isFalse();
        assertThat(result.get("filtering").getAsString()).isEqualTo("REGEX");
        assertThat(result.get("keys").getAsString()).isEqualTo("jo(h)?n");
        assertThat(strings(result.getAsJsonArray("tags"))).containsExactly("people");
        assertThat(strings(result.getAsJsonArray("negativeTags"))).containsExactly("minor");
        assertThat(result.getAsJsonObject("payload").get("text").getAsString()).isEqualTo("John, Anna's brother.");
        assertThat(result.getAsJsonObject("payload").get("truncated").getAsBoolean()).isFalse();
        assertThat(result.toString()).doesNotContain(john.getUuid());
    }

    @Test
    void getsAnEntryOfASubbookAndItsComment() throws Exception {
        assertThat(run("/get-lore-entry 1 2").get("name").getAsString()).isEqualTo("Harbor");
        assertThat(run("/get-lore-entry 2").getAsJsonObject("comment").get("text").getAsString())
                .isEqualTo("Check the consistency with Harbor");
    }

    @Test
    void aLongTextComesInSlices() throws Exception {
        int total = dragons.getPayload().length();

        JsonObject first = run("/get-lore-entry 2").getAsJsonObject("payload");
        assertThat(first.get("totalLength").getAsInt()).isEqualTo(total);
        assertThat(first.get("length").getAsInt()).isEqualTo(4000);
        assertThat(first.get("truncated").getAsBoolean()).isTrue();

        JsonObject second = run("/get-lore-entry 2 1 4000 100").getAsJsonObject("payload");
        assertThat(second.get("offset").getAsInt()).isEqualTo(4000);
        assertThat(second.get("length").getAsInt()).isEqualTo(100);
        assertThat(second.get("text").getAsString()).isEqualTo(dragons.getPayload().substring(4000, 4100));
    }

    @Test
    void anEntryThatIsNotThereFails() {
        assertThatThrownBy(() -> run("/get-lore-entry 4")).isInstanceOf(ExecutionException.class)
                .hasRootCauseMessage("Lorebook 1 has no entry 4, it has 3 entries.");
        assertThatThrownBy(() -> run("/get-lore-entry 0")).isInstanceOf(ExecutionException.class);
        assertThatThrownBy(() -> run("/get-lore-entry 2 2")).isInstanceOf(ExecutionException.class)
                .hasRootCauseMessage("Lorebook 2 has no entry 2, it has 1 entries.");
    }

    // --- search-lore ---

    @Test
    void searchesAllTheLorebooksOfTheBook() throws Exception {
        JsonObject result = run("/search-lore anna");

        // Anna (name and text), John (text), Harbor (text); the entry of the unused lorebook is not found
        assertThat(result.get("found").getAsInt()).isEqualTo(3);
        JsonArray hits = result.getAsJsonArray("hits");
        assertThat(hits).hasSize(3);
        JsonObject first = at(hits, 0);
        assertThat(first.get("lorebook").getAsInt()).isEqualTo(1);
        assertThat(first.get("entry").getAsInt()).isEqualTo(1);
        assertThat(strings(first.getAsJsonArray("matchedIn"))).containsExactly("name", "text");
        assertThat(first.get("snippet").getAsString()).containsIgnoringCase("anna");
        JsonObject last = at(hits, 2);
        assertThat(last.get("lorebook").getAsInt()).isEqualTo(2);
        assertThat(last.get("name").getAsString()).isEqualTo("Harbor");
        assertThat(result.toString()).doesNotContain("Elsewhere").doesNotContain("Removed");
    }

    @Test
    void searchesTheCommentToo() throws Exception {
        JsonObject result = run("/search-lore consistency");

        assertThat(result.get("found").getAsInt()).isEqualTo(1);
        assertThat(strings(at(result.getAsJsonArray("hits"), 0).getAsJsonArray("matchedIn"))).containsExactly("comment");
    }

    @Test
    void theQueryIsPlainTextUnlessItIsARegularExpression() throws Exception {
        assertThat(run("/search-lore \"jo(h)?n\"").get("found").getAsInt()).as("as text").isZero();
        JsonObject result = run("/search-lore \"jo(h)?n\" true");

        assertThat(result.get("found").getAsInt()).isEqualTo(1);
        assertThat(at(result.getAsJsonArray("hits"), 0).get("name").getAsString()).isEqualTo("John");
    }

    @Test
    void limitsTheHitsButCountsAll() throws Exception {
        JsonObject result = run("/search-lore anna false 1");

        assertThat(result.get("found").getAsInt()).isEqualTo(3);
        assertThat(result.getAsJsonArray("hits")).hasSize(1);
    }

    @Test
    void anEmptyQueryOrABadExpressionFails() {
        assertThatThrownBy(() -> run("/search-lore \" \"")).isInstanceOf(ExecutionException.class);
        assertThatThrownBy(() -> run("/search-lore \"(\" true")).isInstanceOf(ExecutionException.class)
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aPatternThatBacktracksIsStopped() throws Exception {
        entry(world, "Trap", 40, e -> e.setPayload("a".repeat(40) + "c"));
        System.setProperty(TimeSafeText.TIMEOUT_PROPERTY, "300");
        try {
            long start = System.nanoTime();
            assertThatThrownBy(() -> run("/search-lore \"(a+)+\\\\1b\" true")).isInstanceOf(ExecutionException.class)
                    .hasRootCauseInstanceOf(RegexTimeoutException.class);
            assertThat(System.nanoTime() - start).as("stopped, not finished").isLessThan(TimeUnit.SECONDS.toNanos(10));
        } finally {
            System.clearProperty(TimeSafeText.TIMEOUT_PROPERTY);
        }
    }

    @Test
    void theCommandsOnlyRead() throws Exception {
        long before = lorebookEntryService.getEntriesForLorebook(world).size();
        var modified = lorebookEntryService.find(anna.getId()).getModification();

        run("/list-lorebooks");
        run("/list-lore-entries");
        run("/get-lore-entry 1");
        run("/search-lore anna");

        assertThat(lorebookEntryService.getEntriesForLorebook(world)).hasSize((int) before);
        assertThat(lorebookEntryService.find(anna.getId()).getModification()).isEqualTo(modified);
    }
}
