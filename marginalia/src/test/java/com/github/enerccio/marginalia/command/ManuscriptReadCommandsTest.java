package com.github.enerccio.marginalia.command;

import com.github.enerccio.marginalia.bound.AgenticSessionPoint;
import com.github.enerccio.marginalia.domain.collections.SummaryType;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.MessagePromptService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables;
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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The commands that read the book, run in the session of an agent: no workspace, the book is held by the
 * {@link AgenticSessionPoint}. Parts are addressed by position and branch number, never by an id.
 * <p>
 * The story: 1 "Anna met John." with two children, 2 "John went home. He slept soundly." (branch 1, active) and
 * 2 "Another John." (branch 2).
 */
class ManuscriptReadCommandsTest extends GenerationTestBase {

    @Autowired
    private CommandService commands;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private MessagePromptService messagePromptService;

    @Autowired
    private SummaryService summaryService;

    private AgenticRequestAttributes session;
    private AgenticSessionPoint sessionPoint;
    private Manuscript manuscript;
    private ChatMessage first, second, other;

    @BeforeEach
    void setUp() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
        first = add(null, "Anna met John.");
        second = add(first, "John went home. He slept soundly.");
        other = add(first, "Another John.");
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setActiveLeaf(second);
        manuscript = manuscriptService.save(loaded);

        sessionPoint = new AgenticSessionPoint(manuscript);
        session = AgenticRequestAttributes.install(sessionPoint);
    }

    @AfterEach
    void tearDown() {
        session.close();
    }

    private ChatMessage add(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        message.setWordCount(chatMessageService.countWords(text));
        Thread.sleep(3);  // messages are ordered by creation time
        return parent == null ? chatMessageService.createRoot(manuscript, message) : chatMessageService.addChild(parent, message);
    }

    private JsonObject run(String line) throws Exception {
        return JsonParser.parseString((String) commands.executeLine(line).get(30, TimeUnit.SECONDS)).getAsJsonObject();
    }

    @Test
    void aPatternThatBacktracksIsStopped() throws Exception {
        add(other, "a".repeat(40) + "c");
        System.setProperty(TimeSafeText.TIMEOUT_PROPERTY, "300");
        try {
            long start = System.nanoTime();
            assertThatThrownBy(() -> run("/find-occurrences \"(a+)+\\\\1b\" true false false true")).isInstanceOf(ExecutionException.class)
                    .hasRootCauseInstanceOf(RegexTimeoutException.class);
            assertThat(System.nanoTime() - start).as("stopped, not finished").isLessThan(TimeUnit.SECONDS.toNanos(10));
        } finally {
            System.clearProperty(TimeSafeText.TIMEOUT_PROPERTY);
        }
    }

    @Test
    void theBookComesFromTheSessionNotFromAWindow() throws Exception {
        assertThat(run("/get-book-info").get("name").getAsString()).isEqualTo(manuscript.getName());
        assertThat(sessionPoint.getManuscriptWindow()).isNull();
        assertThat(sessionPoint.getWorkspace()).isNull();
    }

    @Test
    void failsWithoutABook() {
        sessionPoint.setManuscript(null);

        assertThatThrownBy(() -> run("/get-book-info")).isInstanceOf(ExecutionException.class);
    }

    @Test
    void bookInfoHasTheTotals() throws Exception {
        JsonObject info = run("/get-book-info");

        assertThat(info.get("ai").getAsString()).isEqualTo(ai.getName());
        assertThat(info.getAsJsonObject("activeBranch").get("parts").getAsInt()).isEqualTo(2);
        assertThat(info.getAsJsonObject("allBranches").get("parts").getAsInt()).isEqualTo(3);
        assertThat(info.getAsJsonObject("allBranches").get("words").getAsInt()).isEqualTo(3 + 6 + 2);
        assertThat(info.getAsJsonArray("tags")).isEmpty();
    }

    @Test
    void listsThePartsOfTheActiveBranchByPosition() throws Exception {
        JsonObject result = run("/list-parts");

        assertThat(result.get("total").getAsInt()).isEqualTo(2);
        JsonArray parts = result.getAsJsonArray("parts");
        assertThat(parts.get(0).getAsJsonObject().get("position").getAsInt()).isEqualTo(1);
        assertThat(parts.get(0).getAsJsonObject().get("swipes").getAsInt()).isEqualTo(1);
        assertThat(parts.get(1).getAsJsonObject().get("position").getAsInt()).isEqualTo(2);
        assertThat(parts.get(1).getAsJsonObject().get("swipes").getAsInt()).isEqualTo(2);
        assertThat(parts.get(1).getAsJsonObject().get("preview").getAsString()).startsWith("John went home");
        assertThat(parts.get(0).getAsJsonObject().get("handle").getAsString()).isEqualTo(first.getUuid());
        assertThat(parts.get(1).getAsJsonObject().get("handle").getAsString()).isEqualTo(second.getUuid());
    }

    @Test
    void partsAndBranchesAreAddressedByHandle() throws Exception {
        JsonObject part = run("/get-part " + other.getUuid());
        assertThat(part.get("text").getAsString()).isEqualTo("Another John.");
        assertThat(part.get("position").getAsInt()).isEqualTo(2);
        assertThat(part.get("handle").getAsString()).isEqualTo(other.getUuid());

        // a branch is named by a part on it
        JsonObject list = run("/list-parts " + other.getUuid());
        assertThat(list.getAsJsonArray("parts").get(1).getAsJsonObject().get("preview").getAsString()).isEqualTo("Another John.");
        JsonObject branches = run("/list-branches");
        assertThat(branches.getAsJsonArray("branches").get(1).getAsJsonObject().get("handle").getAsString()).isEqualTo(other.getUuid());
        assertThat(branches.getAsJsonArray("branches").get(1).getAsJsonObject().get("forkHandle").getAsString()).isEqualTo(first.getUuid());

        assertThatThrownBy(() -> run("/get-part 6f1d3a52-0000-4000-8000-000000000000")).isInstanceOf(ExecutionException.class);
        assertThatThrownBy(() -> run("/get-part second")).isInstanceOf(ExecutionException.class)
                .hasMessageContaining("neither a handle");
    }

    @Test
    void listsPartsOfAnotherBranchAndPagesThem() throws Exception {
        JsonObject result = run("/list-parts 2");

        assertThat(result.getAsJsonArray("parts").get(1).getAsJsonObject().get("preview").getAsString())
                .isEqualTo("Another John.");
        JsonObject page = run("/list-parts 1 1 1");
        assertThat(page.getAsJsonArray("parts")).hasSize(1);
        assertThat(page.getAsJsonArray("parts").get(0).getAsJsonObject().get("position").getAsInt()).isEqualTo(2);
        assertThat(page.get("total").getAsInt()).isEqualTo(2);
    }

    @Test
    void anUnknownBranchOrPositionFails() {
        assertThatThrownBy(() -> run("/list-parts 7")).isInstanceOf(ExecutionException.class);
        assertThatThrownBy(() -> run("/get-part 3")).isInstanceOf(ExecutionException.class);
        assertThatThrownBy(() -> run("/get-part 0")).isInstanceOf(ExecutionException.class);
    }

    @Test
    void readsASliceOfAPart() throws Exception {
        JsonObject whole = run("/get-part 2");
        assertThat(whole.get("text").getAsString()).isEqualTo("John went home. He slept soundly.");
        assertThat(whole.get("truncated").getAsBoolean()).isFalse();

        JsonObject slice = run("/get-part 2 1 5 4");
        assertThat(slice.get("text").getAsString()).isEqualTo("went");
        assertThat(slice.get("offset").getAsInt()).isEqualTo(5);
        assertThat(slice.get("totalLength").getAsInt()).isEqualTo(33);
        assertThat(slice.get("truncated").getAsBoolean()).isTrue();
    }

    @Test
    void readsTheOtherFieldsOfAPart() throws Exception {
        ChatMessage loaded = chatMessageService.find(first.getId());
        loaded.setInstructions("Be brief");
        chatMessageService.save(loaded);

        assertThat(run("/get-part 1 1 0 100 instructions").get("text").getAsString()).isEqualTo("Be brief");
        assertThat(run("/get-part 1").getAsJsonObject("turnInput").get("instructions").getAsString()).isEqualTo("Be brief");
        assertThatThrownBy(() -> run("/get-part 1 1 0 100 nonsense")).isInstanceOf(ExecutionException.class);
    }

    @Test
    void listsTheSwipesWithTheBranchThatShowsEach() throws Exception {
        JsonArray swipes = run("/list-swipes 2").getAsJsonArray("swipes");

        assertThat(swipes).hasSize(2);
        JsonObject one = swipes.get(0).getAsJsonObject();
        JsonObject two = swipes.get(1).getAsJsonObject();
        assertThat(one.get("shown").getAsBoolean()).isTrue();
        assertThat(one.get("branch").getAsInt()).isEqualTo(1);
        assertThat(two.get("shown").getAsBoolean()).isFalse();
        assertThat(two.get("branch").getAsInt()).isEqualTo(2);
        // the branch of a swipe reads it
        assertThat(run("/get-part 2 " + two.get("branch").getAsInt()).get("text").getAsString()).isEqualTo("Another John.");
    }

    @Test
    void listsTheBranches() throws Exception {
        JsonObject result = run("/list-branches");

        assertThat(result.get("total").getAsInt()).isEqualTo(2);
        JsonObject one = result.getAsJsonArray("branches").get(0).getAsJsonObject();
        JsonObject two = result.getAsJsonArray("branches").get(1).getAsJsonObject();
        assertThat(one.get("active").getAsBoolean()).isTrue();
        assertThat(one.get("parts").getAsInt()).isEqualTo(2);
        assertThat(one.get("forkPosition").getAsInt()).isEqualTo(1);
        assertThat(two.get("active").getAsBoolean()).isFalse();
        assertThat(two.get("endPreview").getAsString()).isEqualTo("Another John.");
        assertThat(run("/list-branches 1").getAsJsonArray("branches")).hasSize(1);
    }

    @Test
    void searchesAllBranchesAndSaysWhichBranchHasTheHit() throws Exception {
        JsonObject result = run("/search-parts John");

        assertThat(result.get("found").getAsInt()).isEqualTo(3);
        JsonArray hits = result.getAsJsonArray("hits");
        assertThat(hits).hasSize(3);
        JsonObject off = null;
        for (var hit : hits) {
            if (hit.getAsJsonObject().has("branch")) {
                off = hit.getAsJsonObject();
            }
        }
        assertThat(off).isNotNull();
        assertThat(off.get("branch").getAsInt()).isEqualTo(2);
        assertThat(off.get("position").getAsInt()).isEqualTo(2);
        assertThat(off.get("snippet").getAsString()).contains("Another");
        assertThat(run("/search-parts John 1").getAsJsonArray("hits")).hasSize(1);
        assertThatThrownBy(() -> run("/search-parts \"\"")).isInstanceOf(ExecutionException.class);
    }

    @Test
    void searchesTheOlderVersionsOnlyWhenAskedTo() throws Exception {
        chatMessageService.updateResponse(manuscript, second, "Nothing to see.");

        assertThat(run("/search-parts slept").getAsJsonArray("hits")).isEmpty();
        assertThat(run("/search-parts slept 50 false").getAsJsonArray("hits")).isEmpty();

        JsonObject result = run("/search-parts slept 50 true");
        assertThat(result.get("found").getAsInt()).isEqualTo(1);
        JsonObject hit = result.getAsJsonArray("hits").get(0).getAsJsonObject();
        assertThat(hit.get("handle").getAsString()).isEqualTo(second.getUuid());
        assertThat(hit.get("snippet").getAsString()).contains("slept");
        JsonObject version = hit.getAsJsonObject("version");
        assertThat(version.get("id").getAsString()).isNotBlank();
        assertThat(version.get("time").getAsString()).isNotBlank();
        // the part was made by the test code through the service, which records it as the plugin API
        assertThat(version.get("source").getAsString()).isEqualTo("PLUGIN");
        // a part found by its text has no version
        assertThat(run("/search-parts John 50 true").getAsJsonArray("hits").get(0).getAsJsonObject().has("version")).isFalse();
    }

    @Test
    void countsOccurrencesOnTheActiveBranchOrOnAll() throws Exception {
        JsonObject active = run("/find-occurrences John").getAsJsonArray("terms").get(0).getAsJsonObject();
        assertThat(active.get("parts").getAsInt()).isEqualTo(2);
        assertThat(active.get("occurrences").getAsInt()).isEqualTo(2);

        // case insensitive by default
        assertThat(run("/find-occurrences john").getAsJsonArray("terms").get(0).getAsJsonObject().get("parts").getAsInt())
                .isEqualTo(2);
        assertThat(run("/find-occurrences john false true").getAsJsonArray("terms").get(0).getAsJsonObject().get("parts").getAsInt())
                .isZero();

        JsonObject all = run("/find-occurrences John false false false true").getAsJsonArray("terms").get(0).getAsJsonObject();
        assertThat(all.get("parts").getAsInt()).isEqualTo(3);
        JsonObject elsewhere = all.getAsJsonArray("inParts").get(2).getAsJsonObject();
        assertThat(elsewhere.get("branch").getAsInt()).isEqualTo(2);
    }

    @Test
    void takesAListOfTermsAndARegex() throws Exception {
        JsonObject result = commandsRun("find-occurrences", java.util.List.of(java.util.List.of("Anna", "slept"), true));

        JsonArray terms = result.getAsJsonArray("terms");
        assertThat(terms).hasSize(2);
        assertThat(terms.get(0).getAsJsonObject().get("parts").getAsInt()).isEqualTo(1);
        assertThat(terms.get(1).getAsJsonObject().get("parts").getAsInt()).isEqualTo(1);
        assertThatThrownBy(() -> run("/find-occurrences ( true")).isInstanceOf(ExecutionException.class);
    }

    private JsonObject commandsRun(String name, java.util.List<Object> arguments) throws Exception {
        return JsonParser.parseString((String) commands.executePositional(name, arguments).get(30, TimeUnit.SECONDS))
                .getAsJsonObject();
    }

    @Test
    void comparesTwoSwipesAndAPartWithItsOriginal() throws Exception {
        JsonObject swipes = run("/compare-parts 2 2 1 2");

        assertThat(swipes.get("identical").getAsBoolean()).isFalse();
        assertThat(swipes.get("removed").getAsInt()).isEqualTo(2);
        assertThat(swipes.get("added").getAsInt()).isEqualTo(1);
        assertThat(swipes.getAsJsonArray("changes").get(0).getAsJsonObject().get("change").getAsString()).isEqualTo("-");
        assertThat(run("/compare-parts 1 1").get("identical").getAsBoolean()).isTrue();

        assertThatThrownBy(() -> run("/compare-parts 1 0")).isInstanceOf(ExecutionException.class);
        ChatMessage loaded = chatMessageService.find(second.getId());
        loaded.setOriginalResponse("John went home. He slept badly.");
        chatMessageService.save(loaded);
        JsonObject edited = run("/compare-parts 2 0");
        assertThat(edited.get("removed").getAsInt()).isEqualTo(1);
        assertThat(edited.get("added").getAsInt()).isEqualTo(1);
    }

    @Test
    void countsTokensOfATextAndOfAPart() throws Exception {
        JsonObject text = run("/count-tokens \"three small words\"");
        assertThat(text.get("words").getAsInt()).isEqualTo(3);
        assertThat(text.get("tokens").getAsLong()).isPositive();
        assertThat(text.get("characters").getAsInt()).isEqualTo(17);

        JsonObject part = commandsRun("count-tokens", java.util.List.<Object>of("2", "2"));
        assertThat(part.get("characters").getAsInt()).isEqualTo("Another John.".length());
        // the handle of a part counts the part
        JsonObject byHandle = run("/count-tokens " + first.getUuid());
        assertThat(byHandle.get("position").getAsInt()).isEqualTo(1);
        assertThat(byHandle.get("handle").getAsString()).isEqualTo(first.getUuid());
    }

    @Test
    void metricsAndPromptOfAPart() throws Exception {
        ChatMessage loaded = chatMessageService.find(second.getId());
        loaded.setModelUsed("some-model");
        loaded.setPromptTokens(120L);
        loaded.setBuiltPromptTokens(4L);
        chatMessageService.save(loaded);
        messagePromptService.store(loaded, "the whole prompt", "lore");

        JsonObject metrics = run("/get-part-metrics 2");
        assertThat(metrics.get("generated").getAsBoolean()).isTrue();
        assertThat(metrics.get("model").getAsString()).isEqualTo("some-model");
        assertThat(metrics.get("promptTokens").getAsLong()).isEqualTo(120);

        JsonObject prompt = run("/show-prompt 2");
        assertThat(prompt.get("available").getAsBoolean()).isTrue();
        assertThat(prompt.get("text").getAsString()).isEqualTo("the whole prompt");
        assertThat(prompt.getAsJsonObject("backgroundLore").get("text").getAsString()).isEqualTo("lore");
        assertThat(run("/show-prompt 1").get("available").getAsBoolean()).isFalse();
    }

    @Test
    void summariesAreListedReadAndCheckedByPosition() throws Exception {
        assertThat(run("/list-summaries").get("partsWithoutSummary").getAsInt()).isEqualTo(2);
        assertThat(run("/get-summary 1").get("hasSummary").getAsBoolean()).isFalse();
        assertThat(run("/check-summaries").get("outdated").getAsInt()).isZero();

        Summary summary = new Summary();
        summary.setSummary("Anna and John meet.");
        summary.setSummaryTokens(5L);
        summary.setSummaryType(SummaryType.SUMMARY);
        summary.setSummaryMessageHash("not the hash of the block");
        summary = summaryService.save(summary);
        ChatMessage loaded = chatMessageService.find(first.getId());
        loaded.setSummary(summary);
        loaded.setEdited(true);
        chatMessageService.save(loaded);

        JsonObject listed = run("/list-summaries").getAsJsonArray("summaries").get(0).getAsJsonObject();
        assertThat(listed.get("position").getAsInt()).isEqualTo(1);
        assertThat(listed.get("covers").getAsString()).isEqualTo("1-1");
        assertThat(listed.get("valid").getAsBoolean()).isFalse();
        assertThat(run("/list-summaries").get("partsWithoutSummary").getAsInt()).isEqualTo(1);

        JsonObject read = run("/get-summary 1");
        assertThat(read.get("text").getAsString()).isEqualTo("Anna and John meet.");
        assertThat(read.get("valid").getAsBoolean()).isFalse();
        assertThat(run("/list-parts").getAsJsonArray("parts").get(0).getAsJsonObject().get("summary").getAsString())
                .isEqualTo("outdated");

        JsonObject check = run("/check-summaries");
        assertThat(check.get("outdated").getAsInt()).isEqualTo(1);
        JsonObject row = check.getAsJsonArray("outdatedSummaries").get(0).getAsJsonObject();
        assertThat(row.get("position").getAsInt()).isEqualTo(1);
        assertThat(row.getAsJsonArray("editedParts").get(0).getAsInt()).isEqualTo(1);
    }

    @Test
    void templateVariablesOfTheBookAndOfAPart() throws Exception {
        TemplateVariables variables = new TemplateVariables();
        variables.set(TemplateVariables.Scope.GLOBAL, "mood", "tense");
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        variables.storeTo(TemplateVariables.Scope.GLOBAL, loaded.getAttributes());
        manuscriptService.save(loaded);
        TemplateVariables local = new TemplateVariables();
        local.set(TemplateVariables.Scope.LOCAL, "place", "home");
        ChatMessage message = chatMessageService.find(second.getId());
        local.storeTo(TemplateVariables.Scope.LOCAL, message.getAttributes());
        chatMessageService.save(message);

        assertThat(run("/get-template-variables global").getAsJsonObject("variables").get("mood").getAsString())
                .isEqualTo("tense");
        JsonObject part = run("/get-template-variables part 2");
        assertThat(part.getAsJsonObject("variables").get("place").getAsString()).isEqualTo("home");
        // the last part of the branch by default
        assertThat(run("/get-template-variables part").get("position").getAsInt()).isEqualTo(2);
        assertThatThrownBy(() -> run("/get-template-variables nowhere")).isInstanceOf(ExecutionException.class);
    }

}
