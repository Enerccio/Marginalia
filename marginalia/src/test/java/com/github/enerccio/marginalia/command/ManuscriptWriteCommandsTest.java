package com.github.enerccio.marginalia.command;

import com.github.enerccio.marginalia.bound.AgenticSessionPoint;
import com.github.enerccio.marginalia.domain.collections.SummaryType;
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

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The commands that change the book, run in the session of an agent (the book is held by the {@link AgenticSessionPoint}).
 * Parts are addressed by handle (their uuid). Nobody can be asked a question there, so the scope answers the questions
 * itself.
 * <p>
 * The story: 1 "Anna met John." with two children, 2 "John went home. He slept soundly." (branch 1, active) and
 * 2 "Another John." (branch 2).
 */
class ManuscriptWriteCommandsTest extends GenerationTestBase {

    @Autowired
    private CommandService commands;

    @Autowired
    private CommandScopes commandScopes;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private TagRelationService tagRelationService;

    private AgenticRequestAttributes session;
    private Manuscript manuscript;
    private ChatMessage first, second, other;
    private boolean answer = true;

    @BeforeEach
    void setUp() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
        first = add(null, "Anna met John.");
        second = add(first, "John went home. He slept soundly.");
        other = add(first, "Another John.");
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setActiveLeaf(second);
        manuscript = manuscriptService.save(loaded);
        session = AgenticRequestAttributes.install(new AgenticSessionPoint(manuscript));
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

    /** The scope of the session, with the questions answered by the test. */
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

    private ChatMessage reload(ChatMessage message) throws Exception {
        return chatMessageService.find(message.getId());
    }

    private Manuscript book() throws Exception {
        return manuscriptService.find(manuscript.getId());
    }

    @Test
    void aPartIsEditedWhereTheTextMatchesOnce() throws Exception {
        JsonObject result = run("/edit-part " + second.getUuid() + " \"slept soundly\" \"dreamed\"");

        assertThat(reload(second).getResponse()).isEqualTo("John went home. He dreamed.");
        assertThat(reload(second).isEdited()).isTrue();
        assertThat(result.get("handle").getAsString()).isEqualTo(second.getUuid());
        assertThat(result.get("position").getAsInt()).isEqualTo(2);
        assertThat(result.get("words").getAsInt()).isEqualTo(5);
        assertThat(result.get("tokens").getAsLong()).isPositive();
    }

    @Test
    void anEditThatMatchesTwiceOrNotAtAllChangesNothing() throws Exception {
        assertThatThrownBy(() -> run("/edit-part " + first.getUuid() + " \"n\" \"m\"")).isInstanceOf(ExecutionException.class)
                .hasMessageContaining("3 times");
        assertThatThrownBy(() -> run("/edit-part " + first.getUuid() + " Zed Bob")).isInstanceOf(ExecutionException.class)
                .hasMessageContaining("not found");
        assertThatThrownBy(() -> run("/edit-part " + first.getUuid() + " \"\" Bob")).isInstanceOf(ExecutionException.class);

        assertThat(reload(first).getResponse()).isEqualTo("Anna met John.");
        assertThat(reload(first).isEdited()).isFalse();
    }

    @Test
    void aPositionIsRefusedBecauseItMoves() {
        assertThatThrownBy(() -> run("/edit-part 2 John Jack")).isInstanceOf(ExecutionException.class)
                .hasMessageContaining("handle");
        assertThatThrownBy(() -> run("/edit-part 6f1d3a52-0000-4000-8000-000000000000 John Jack")).isInstanceOf(ExecutionException.class);
    }

    @Test
    void aWholeTextIsReplacedOnlyWhenConfirmed() throws Exception {
        answer = false;
        assertThat(commands.executeLine("/replace-part-text " + first.getUuid() + " \"Brand new.\"", false, scope())
                .get(30, TimeUnit.SECONDS)).isEqualTo("Nothing was changed.");
        assertThat(reload(first).getResponse()).isEqualTo("Anna met John.");

        answer = true;
        JsonObject result = run("/replace-part-text " + first.getUuid() + " \"Brand new.\"");
        assertThat(reload(first).getResponse()).isEqualTo("Brand new.");
        assertThat(reload(first).isEdited()).isTrue();
        assertThat(result.get("words").getAsInt()).isEqualTo(2);
    }

    @Test
    void theOtherFieldsOfAPartAreChangedByName() throws Exception {
        run("/set-part-fields " + first.getUuid() + " \"Write a kiss\" \"A cold forest\"");
        assertThat(reload(first).getInstructions()).isEqualTo("Write a kiss");
        assertThat(reload(first).getSceneSetting()).isEqualTo("A cold forest");
        assertThat(reload(first).getPovCharacter()).isNull();
        assertThat(reload(first).getResponse()).isEqualTo("Anna met John.");

        // the turn is edited like a text, the part is not flagged as edited
        run("/edit-part " + first.getUuid() + " cold warm scene");
        assertThat(reload(first).getSceneSetting()).isEqualTo("A warm forest");
        assertThat(reload(first).isEdited()).isFalse();

        // an empty text clears a field
        run("/set-part-fields " + first.getUuid() + " \"\"");
        assertThat(reload(first).getInstructions()).isEmpty();
        assertThatThrownBy(() -> run("/set-part-fields " + first.getUuid())).isInstanceOf(ExecutionException.class);
        assertThatThrownBy(() -> run("/edit-part " + first.getUuid() + " a b nothing")).isInstanceOf(ExecutionException.class)
                .hasMessageContaining("nothing");
    }

    @Test
    void textIsAppendedAfterABlankLineOrAGivenSeparator() throws Exception {
        run("/append-to-part " + other.getUuid() + " \"More.\"");
        assertThat(reload(other).getResponse()).isEqualTo("Another John.\n\nMore.");

        run("/append-to-part " + other.getUuid() + " \"Again.\" \" \"");
        assertThat(reload(other).getResponse()).isEqualTo("Another John.\n\nMore. Again.");
    }

    @Test
    void aPartIsAddedAfterTheLastPartOrAfterAGivenOne() throws Exception {
        JsonObject added = run("/create-part \"Night fell.\"");
        ChatMessage created = chatMessageService.find(chatMessageService.find(added.get("handle").getAsString()));
        assertThat(created.getParent().getId()).isEqualTo(second.getId());
        assertThat(created.isEdited()).isFalse();
        assertThat(created.getWordCount()).isEqualTo(2);
        assertThat(added.get("position").getAsInt()).isEqualTo(3);
        assertThat(book().getActiveLeaf().getId()).isEqualTo(created.getId());

        // after another part: a new branch
        JsonObject branched = run("/create-part \"Elsewhere.\" " + first.getUuid() + " \"go there\"");
        ChatMessage elsewhere = chatMessageService.find(chatMessageService.find(branched.get("handle").getAsString()));
        assertThat(elsewhere.getParent().getId()).isEqualTo(first.getId());
        assertThat(elsewhere.getInstructions()).isEqualTo("go there");
        assertThat(branched.get("position").getAsInt()).isEqualTo(2);
        assertThat(chatMessageService.getAllMessages(book())).hasSize(5);
        assertThatThrownBy(() -> run("/create-part \" \"")).isInstanceOf(ExecutionException.class);
    }

    @Test
    void aPartInsertedAfterAPartTakesOverEverythingThatFollowedItWithoutRecreatingAnything() throws Exception {
        JsonObject result = run("/insert-part \"Night fell.\" " + first.getUuid());

        ChatMessage inserted = chatMessageService.find(chatMessageService.find(result.get("handle").getAsString()));
        assertThat(inserted.getParent().getId()).isEqualTo(first.getId());
        assertThat(inserted.isEdited()).isFalse();
        assertThat(result.get("position").getAsInt()).isEqualTo(2);
        assertThat(result.get("continuedBy").getAsInt()).as("both continuations of the part, the branches stay branches").isEqualTo(2);
        // the parts below were not made again: the same uuids, one place deeper
        assertThat(reload(second).getParent().getId()).isEqualTo(inserted.getId());
        assertThat(reload(other).getParent().getId()).isEqualTo(inserted.getId());
        assertThat(reload(second).getResponse()).isEqualTo("John went home. He slept soundly.");
        assertThat(chatMessageService.getAllMessages(book())).hasSize(4);
        assertThat(chatMessageService.getBranchFromLeaf(book().getActiveLeaf())).extracting(ChatMessage::getUuid)
                .containsExactly(first.getUuid(), inserted.getUuid(), second.getUuid());
        assertThat(book().getActiveLeaf().getId()).as("the active end stays").isEqualTo(second.getId());
        assertThat(chatMessageService.getSwipesForMessage(reload(second))).as("the swipes stay swipes").hasSize(2);
    }

    @Test
    void aPartInsertedBeforeAPartComesAfterTheOneAboveIt() throws Exception {
        JsonObject result = run("/insert-part \"Prologue.\" " + first.getUuid() + " true");

        ChatMessage inserted = chatMessageService.find(chatMessageService.find(result.get("handle").getAsString()));
        assertThat(inserted.getParent()).as("before the first part of the story: a new first part").isNull();
        assertThat(inserted.getParentScript().getId()).isEqualTo(manuscript.getId());
        assertThat(reload(first).getParent().getId()).isEqualTo(inserted.getId());
        assertThat(result.get("position").getAsInt()).isEqualTo(1);
        assertThat(chatMessageService.getBranchFromLeaf(book().getActiveLeaf())).extracting(ChatMessage::getUuid)
                .containsExactly(inserted.getUuid(), first.getUuid(), second.getUuid());

        // before a part with a part above it: between the two
        JsonObject between = run("/insert-part \"Meanwhile.\" " + second.getUuid() + " true");
        ChatMessage meanwhile = chatMessageService.find(chatMessageService.find(between.get("handle").getAsString()));
        assertThat(meanwhile.getParent().getId()).isEqualTo(first.getId());
        assertThat(reload(second).getParent().getId()).isEqualTo(meanwhile.getId());
        assertThat(reload(other).getParent().getId()).as("the swipes of the part move together").isEqualTo(meanwhile.getId());
    }

    @Test
    void aPartInsertedAfterTheEndOfTheActiveBranchIsTheNewEnd() throws Exception {
        JsonObject result = run("/insert-part \"Night fell.\" " + second.getUuid());

        ChatMessage inserted = chatMessageService.find(chatMessageService.find(result.get("handle").getAsString()));
        assertThat(inserted.getParent().getId()).isEqualTo(second.getId());
        assertThat(book().getActiveLeaf().getId()).isEqualTo(inserted.getId());
        assertThat(result.get("continuedBy").getAsInt()).isZero();
        assertThat(result.get("position").getAsInt()).isEqualTo(3);
    }

    @Test
    void anInsertedPartMakesTheSummariesAboveItOutdatedAndSaysSo() throws Exception {
        // the summary of the second part covers the first too
        Summary summary = new Summary();
        summary.setSummary("Anna met John, who went home.");
        summary.setSummaryTokens(5L);
        summary.setSummaryType(SummaryType.SUMMARY);
        summary = summaryService.save(summary);
        ChatMessage loaded = chatMessageService.find(second.getId());
        loaded.setSummary(summary);
        chatMessageService.save(loaded);
        run("/revalidate-summaries");
        assertThat(run("/check-summaries").get("outdated").getAsInt()).isZero();

        JsonObject result = run("/insert-part \"Night fell.\" " + first.getUuid());

        assertThat(result.getAsJsonArray("outdatedSummaries").get(0).getAsJsonObject().get("handle").getAsString()).isEqualTo(second.getUuid());
    }

    @Test
    void aPartIsNotInsertedWithoutTextOrWithoutAKnownPart() {
        assertThatThrownBy(() -> run("/insert-part \" \" " + first.getUuid())).isInstanceOf(ExecutionException.class);
        assertThatThrownBy(() -> run("/insert-part \"Text.\" 2")).isInstanceOf(ExecutionException.class);
    }

    @Test
    void theFirstPartOfAnEmptyBookIsMadeWithoutAParent() throws Exception {
        Manuscript empty = manuscriptService.save(newManuscript());
        session.close();
        session = AgenticRequestAttributes.install(new AgenticSessionPoint(empty));

        JsonObject added = run("/create-part \"Once upon a time.\"");

        assertThat(added.get("position").getAsInt()).isEqualTo(1);
        assertThat(manuscriptService.find(empty.getId()).getActiveLeaf().getUuid()).isEqualTo(added.get("handle").getAsString());
    }

    @Test
    void aPartIsBranchedAndTheCopyBecomesTheActiveEnd() throws Exception {
        JsonObject result = run("/branch-part " + second.getUuid());

        assertThat(result.get("copiedFrom").getAsString()).isEqualTo(second.getUuid());
        assertThat(result.get("handle").getAsString()).isNotEqualTo(second.getUuid());
        ChatMessage copy = chatMessageService.find(chatMessageService.find(result.get("handle").getAsString()));
        assertThat(copy.getResponse()).isEqualTo(second.getResponse());
        assertThat(copy.getParent().getId()).isEqualTo(first.getId());
        assertThat(book().getActiveLeaf().getId()).isEqualTo(copy.getId());
        assertThat(chatMessageService.getSwipesForMessage(second)).hasSize(3);
    }

    @Test
    void aPartIsDeletedWhenConfirmedAndTheActiveEndMovesUp() throws Exception {
        answer = false;
        assertThat(commands.executeLine("/delete-part " + second.getUuid(), false, scope()).get(30, TimeUnit.SECONDS))
                .isEqualTo("Nothing was changed.");
        assertThat(reload(second).isDeleted()).isFalse();

        answer = true;
        JsonObject result = run("/delete-part " + second.getUuid());

        assertThat(reload(second).isDeleted()).isTrue();
        assertThat(result.get("followingParts").getAsInt()).isZero();
        assertThat(book().getActiveLeaf().getId()).isEqualTo(first.getId());
        assertThat(result.get("activePart").getAsString()).isEqualTo(first.getUuid());
    }

    @Test
    void theChildrenOfADeletedPartContinueItsParent() throws Exception {
        Manuscript empty = manuscriptService.save(newManuscript());
        ChatMessage root = chatMessageService.createRoot(empty, text("Root."));
        ChatMessage middle = chatMessageService.addChild(root, text("Middle."));
        ChatMessage end = chatMessageService.addChild(middle, text("End."));
        empty = manuscriptService.find(empty.getId());
        empty.setActiveLeaf(end);
        empty = manuscriptService.save(empty);
        session.close();
        session = AgenticRequestAttributes.install(new AgenticSessionPoint(empty));

        JsonObject result = run("/delete-part " + middle.getUuid());

        assertThat(result.get("followingParts").getAsInt()).isEqualTo(1);
        assertThat(reload(end).getParent().getId()).isEqualTo(root.getId());
        assertThat(manuscriptService.find(empty.getId()).getActiveLeaf().getId()).isEqualTo(end.getId());
    }

    private ChatMessage text(String text) {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return message;
    }

    @Test
    void anotherBranchBecomesTheActiveOne() throws Exception {
        JsonObject result = run("/switch-branch " + other.getUuid());

        assertThat(book().getActiveLeaf().getId()).isEqualTo(other.getId());
        assertThat(result.get("branchEnd").getAsString()).isEqualTo(other.getUuid());
        assertThat(result.get("parts").getAsInt()).isEqualTo(2);
    }

    @Test
    void aSummaryIsSetRevalidatedAndRemoved() throws Exception {
        assertThatThrownBy(() -> run("/set-summary " + first.getUuid() + " text")).isInstanceOf(ExecutionException.class)
                .hasMessageContaining("no summary");

        Summary summary = new Summary();
        summary.setSummary("Anna and John meet.");
        summary.setSummaryTokens(5L);
        summary.setSummaryType(SummaryType.SUMMARY);
        summary.setSummaryMessageHash("not the hash of the block");
        summary = summaryService.save(summary);
        ChatMessage loaded = chatMessageService.find(first.getId());
        loaded.setSummary(summary);
        chatMessageService.save(loaded);

        JsonObject set = run("/set-summary " + first.getUuid() + " \"Anna meets John in the forest.\"");
        assertThat(summaryService.find(chatMessageService.find(first.getId()).getSummary()).getSummary())
                .isEqualTo("Anna meets John in the forest.");
        assertThat(set.get("tokens").getAsLong()).isPositive();

        assertThat(run("/check-summaries").get("outdated").getAsInt()).isEqualTo(1);
        JsonObject revalidated = run("/revalidate-summaries");
        assertThat(revalidated.get("revalidated").getAsInt()).isEqualTo(1);
        assertThat(revalidated.getAsJsonArray("summaries").get(0).getAsJsonObject().get("handle").getAsString()).isEqualTo(first.getUuid());
        assertThat(run("/check-summaries").get("outdated").getAsInt()).isZero();

        // an edit makes it outdated again, and the answer says so
        JsonObject edit = run("/edit-part " + first.getUuid() + " Anna Eve");
        assertThat(edit.getAsJsonArray("outdatedSummaries").get(0).getAsJsonObject().get("handle").getAsString()).isEqualTo(first.getUuid());
        assertThat(edit.get("hint").getAsString()).contains("revalidate-summaries");
        assertThat(run("/revalidate-summaries " + first.getUuid()).get("revalidated").getAsInt()).isEqualTo(1);

        answer = false;
        commands.executeLine("/remove-summary " + first.getUuid(), false, scope()).get(30, TimeUnit.SECONDS);
        assertThat(chatMessageService.find(first.getId()).getSummary()).isNotNull();
        answer = true;
        assertThat(run("/remove-summary " + first.getUuid()).get("hasSummary").getAsBoolean()).isFalse();
        assertThat(chatMessageService.find(first.getId()).getSummary()).isNull();
    }

    @Test
    void theInfoOfTheBookIsChangedOnlyWhereGiven() throws Exception {
        JsonObject result = run("/set-book-info \"A new name\" \"About two people\" \"First person\" Past \"Dry\" cs");

        Manuscript changed = book();
        assertThat(changed.getName()).isEqualTo("A new name");
        assertThat(changed.getDescription()).isEqualTo("About two people");
        assertThat(changed.getPov()).isEqualTo("First person");
        assertThat(changed.getTense()).isEqualTo("Past");
        assertThat(changed.getStyle()).isEqualTo("Dry");
        assertThat(changed.getLanguage().getCode()).isEqualTo("cs");
        assertThat(result.get("language").getAsString()).isEqualTo("cs");

        run("/set-book-info \"Another name\"");
        assertThat(book().getPov()).isEqualTo("First person");
        assertThatThrownBy(() -> run("/set-book-info \"\"")).isInstanceOf(ExecutionException.class);
        assertThatThrownBy(() -> run("/set-book-info x y z z z Klingon")).isInstanceOf(ExecutionException.class)
                .hasMessageContaining("Klingon");
        assertThat(book().getName()).isEqualTo("Another name");
    }

    @Test
    void thePromptsAreChangedWhenConfirmedAndCheckedFirst() throws Exception {
        answer = false;
        commands.executeLine("/set-book-prompts \"{{backgroundLore}} - new\"", false, scope()).get(30, TimeUnit.SECONDS);
        assertThat(book().getTemplate()).isEqualTo("{{backgroundLore}}");

        answer = true;
        JsonObject result = run("/set-book-prompts \"{{backgroundLore}} - new\" \"{{instructions}} please\"");
        assertThat(book().getTemplate()).isEqualTo("{{backgroundLore}} - new");
        assertThat(book().getUserPrompt()).isEqualTo("{{instructions}} please");
        assertThat(result.getAsJsonArray("changed")).hasSize(2);

        assertThatThrownBy(() -> run("/set-book-prompts \"{{#if}} broken\"")).isInstanceOf(ExecutionException.class);
        assertThat(book().getTemplate()).isEqualTo("{{backgroundLore}} - new");
    }

    @Test
    void tagsAreAddedAndTakenOff() throws Exception {
        JsonObject result = run("/set-book-tags \"fantasy, dark\"");
        assertThat(tagValues(result)).containsExactlyInAnyOrder("fantasy", "dark");

        result = run("/set-book-tags \"Fantasy,gloomy\" dark");
        assertThat(tagValues(result)).containsExactlyInAnyOrder("fantasy", "gloomy");
        assertThat(tagRelationService.getTagsForObject(book())).hasSize(2);
    }

    private static List<String> tagValues(JsonObject result) {
        return result.getAsJsonArray("tags").asList().stream().map(e -> e.getAsString()).toList();
    }
}
