package com.github.enerccio.marginalia.command;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.AnnotationService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.GenerationListener;
import com.github.enerccio.marginalia.domain.service.GenerationView;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.command.CommandException;
import com.github.enerccio.marginalia.domain.service.command.CommandRun;
import com.github.enerccio.marginalia.domain.service.command.CommandScope;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequestType;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The commands that have the model write: {@code next}, {@code swipe}, {@code regenerate} and the two summaries. They run
 * the real generation against the mock server, for the book of a fixed scope.
 * <p>
 * The story: 1 "Anna met John." with the child 2 "John went home." (the active end).
 */
class GenerationCommandsTest extends GenerationTestBase {

    @Autowired
    private CommandService commands;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private AnnotationService annotationService;

    private Manuscript manuscript;
    private ChatMessage first, second;
    private boolean answer = true;
    // the story the commands are typed into, null for none
    private GenerationView view;

    @BeforeEach
    void setUp() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
        first = add(null, "Anna met John.");
        second = add(first, "John went home.");
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setActiveLeaf(second);
        manuscript = manuscriptService.save(loaded);
    }

    private ChatMessage add(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        message.setWordCount(chatMessageService.countWords(text));
        Thread.sleep(3);
        return parent == null ? chatMessageService.createRoot(manuscript, message) : chatMessageService.addChild(parent, message);
    }

    private CommandScope scope() {
        return new CommandScope() {
            @Override
            public Manuscript getManuscript() throws Exception {
                return manuscriptService.find(manuscript.getId());
            }

            @Override
            public Lorebook getLorebook() {
                return null;
            }

            @Override
            public AgentRun getRun() {
                return null;
            }

            @Override
            public boolean confirm(CommandContext context, String question) {
                return answer;
            }

            @Override
            public GenerationView getGenerationView() {
                return view;
            }

            @Override
            public ChatMessage resolveMessage(String uuid) throws Exception {
                ChatMessage message = chatMessageService.find(chatMessageService.find(uuid));
                if (message == null || message.isDeleted() || !message.getParentScript().getId().equals(manuscript.getId())) {
                    throw new IllegalArgumentException("The part does not exist");
                }
                return message;
            }

            @Override
            public Lorebook resolveLorebook(String uuid) {
                throw new IllegalArgumentException("The lorebook does not exist");
            }

            @Override
            public LorebookEntry resolveEntry(String uuid) {
                throw new IllegalArgumentException("The lorebook entry does not exist");
            }
        };
    }

    private Object run(String line) throws Exception {
        return commands.executeLine(line, false, scope()).get(60, TimeUnit.SECONDS);
    }

    private JsonObject json(String line) throws Exception {
        return JsonParser.parseString((String) run(line)).getAsJsonObject();
    }

    private ChatMessage part(JsonObject result) throws Exception {
        return chatMessageService.find(chatMessageService.find(result.get("handle").getAsString()));
    }

    private Manuscript book() throws Exception {
        return manuscriptService.find(manuscript.getId());
    }

    @Test
    void theNextPartIsWrittenAfterTheEndOfTheActiveBranch() throws Exception {
        llm.fallback(com.github.enerccio.marginalia.test.llm.MockLLMResponse.text("Night fell."));

        JsonObject result = json("/next \"A forest\" Anna \"\" \"Let night fall\"");

        ChatMessage created = part(result);
        assertThat(created.getResponse()).isEqualTo("Night fell.");
        assertThat(created.getParent().getId()).isEqualTo(second.getId());
        assertThat(created.getSceneSetting()).isEqualTo("A forest");
        assertThat(created.getPovCharacter()).isEqualTo("Anna");
        assertThat(created.getInstructions()).isEqualTo("Let night fall");
        assertThat(result.get("position").getAsInt()).isEqualTo(3);
        assertThat(result.get("model").getAsString()).isEqualTo(ai.getName());
        assertThat(book().getActiveLeaf().getId()).isEqualTo(created.getId());
    }

    @Test
    void theNextPartCanContinueAfterAnotherPart() throws Exception {
        JsonObject result = json("/next \"\" \"\" \"\" \"\" " + first.getUuid());

        ChatMessage created = part(result);
        assertThat(created.getParent().getId()).isEqualTo(first.getId());
        assertThat(result.get("position").getAsInt()).isEqualTo(2);
        assertThat(book().getActiveLeaf().getId()).isEqualTo(created.getId());
    }

    @Test
    void aSwipeIsAnotherVersionOfTheLastPart() throws Exception {
        llm.fallback(com.github.enerccio.marginalia.test.llm.MockLLMResponse.text("John took a walk."));
        run("/set-part-fields " + second.getUuid() + " \"Go for a walk\"");

        JsonObject result = json("/swipe");

        ChatMessage created = part(result);
        assertThat(created.getId()).isNotEqualTo(second.getId());
        assertThat(created.getParent().getId()).isEqualTo(first.getId());
        assertThat(created.getResponse()).isEqualTo("John took a walk.");
        assertThat(created.getInstructions()).as("the turn of the part it is another version of").isEqualTo("Go for a walk");
        assertThat(chatMessageService.find(second.getId()).getResponse()).isEqualTo("John went home.");
        assertThat(book().getActiveLeaf().getId()).isEqualTo(created.getId());
    }

    @Test
    void onlyTheLastPartOfABranchIsWrittenAgain() {
        assertThatThrownBy(() -> run("/swipe " + first.getUuid())).isInstanceOf(ExecutionException.class)
                .hasMessageContaining("last part");
        assertThatThrownBy(() -> run("/regenerate " + first.getUuid())).isInstanceOf(ExecutionException.class);
    }

    @Test
    void aPartIsWrittenAgainInPlaceWhenConfirmed() throws Exception {
        llm.fallback(com.github.enerccio.marginalia.test.llm.MockLLMResponse.text("John stayed out."));
        answer = false;
        assertThat(run("/regenerate")).isEqualTo("Nothing was changed.");
        assertThat(chatMessageService.find(second.getId()).getResponse()).isEqualTo("John went home.");

        answer = true;
        JsonObject result = json("/regenerate " + second.getUuid());

        assertThat(result.get("handle").getAsString()).isEqualTo(second.getUuid());
        assertThat(chatMessageService.find(second.getId()).getResponse()).isEqualTo("John stayed out.");
    }

    @Test
    void aModelProfileBelongsToAnAgent() {
        assertThatThrownBy(() -> run("/next \"\" \"\" \"\" \"\" \"\" Prose")).isInstanceOf(ExecutionException.class)
                .hasMessageContaining("agents");
    }

    @Test
    void aSummaryIsWrittenForAPartAndAMetaSummaryForARange() throws Exception {
        llm.fallback(com.github.enerccio.marginalia.test.llm.MockLLMResponse.text("Anna and John."));

        JsonObject summary = json("/regenerate-summary " + first.getUuid());

        assertThat(summary.get("type").getAsString()).isEqualTo("summary");
        assertThat(summary.get("text").getAsString()).isEqualTo("Anna and John.");
        Summary stored = summaryService.find(chatMessageService.find(first.getId()).getSummary());
        assertThat(stored.getSummary()).isEqualTo("Anna and John.");

        run("/regenerate-summary " + second.getUuid());
        llm.fallback(com.github.enerccio.marginalia.test.llm.MockLLMResponse.text("The whole story."));
        JsonObject meta = json("/create-meta-summary " + second.getUuid() + " " + first.getUuid());

        assertThat(meta.get("type").getAsString()).isEqualTo("meta");
        assertThat(meta.get("text").getAsString()).isEqualTo("The whole story.");
        assertThat(summaryService.find(chatMessageService.find(second.getId()).getSummary()).getSummary()).isEqualTo("The whole story.");
    }

    @Test
    void anOlderPartIsRedraftedAndReplaced() throws Exception {
        llm.fallback(com.github.enerccio.marginalia.test.llm.MockLLMResponse.text("Anna met John in the rain."));

        JsonObject result = json("/redraft-part " + first.getUuid() + " \"Add rain\"");

        assertThat(result.get("applied").getAsBoolean()).isTrue();
        assertThat(chatMessageService.find(first.getId()).getResponse()).isEqualTo("Anna met John in the rain.");
        // the part after it stays, the story around the part was told to the model
        assertThat(chatMessageService.find(second.getId()).getResponse()).isEqualTo("John went home.");
        String prompt = llm.lastCompletionRequest().getPromptText();
        assertThat(prompt).contains("Add rain").contains("Anna met John.").contains("John went home.");
    }

    @Test
    void aRedraftWithApplyFalseOnlyReturnsTheText() throws Exception {
        llm.fallback(com.github.enerccio.marginalia.test.llm.MockLLMResponse.text("A longer meeting."));

        JsonObject result = json("/redraft-part " + first.getUuid() + " \"Longer\" \"\" \"\" false");

        assertThat(result.get("applied").getAsBoolean()).isFalse();
        assertThat(result.get("text").getAsString()).isEqualTo("A longer meeting.");
        assertThat(chatMessageService.find(first.getId()).getResponse()).isEqualTo("Anna met John.");
    }

    @Test
    void aRedraftCanUseAnotherPromptThanTheBook() throws Exception {
        llm.fallback(com.github.enerccio.marginalia.test.llm.MockLLMResponse.text("New."));

        run("/redraft-part " + first.getUuid() + " \"x\" \"REWRITE {{text}} BECAUSE {{redraftInstructions}}\"");

        assertThat(llm.lastCompletionRequest().getPromptText()).contains("REWRITE Anna met John. BECAUSE x");
        assertThatThrownBy(() -> run("/redraft-part " + first.getUuid() + " \"x\" \"{{#unclosed}}\"")).isInstanceOf(ExecutionException.class);
    }

    @Test
    void theModelAnnotatesAnOlderPartAndTheNotesAreAdded() throws Exception {
        llm.fallback(com.github.enerccio.marginalia.test.llm.MockLLMResponse.text(
                "{\"findings\": [{\"quote\": \"Anna met John.\", \"note\": \"Flat opening\", \"type\": \"style\"},"
                        + " {\"quote\": \"not in the part\", \"note\": \"Contradicts the lore\"}]}"));

        JsonObject result = json("/generate-annotation " + first.getUuid());

        assertThat(result.get("part").getAsString()).isEqualTo(first.getUuid());
        assertThat(result.get("created").getAsInt()).isEqualTo(2);
        assertThat(result.getAsJsonArray("annotations")).hasSize(2);
        var notes = annotationService.listForPart(book(), chatMessageService.find(first.getId()));
        assertThat(notes).extracting(Annotation::getText).containsExactlyInAnyOrder("Flat opening", "Contradicts the lore");
        assertThat(notes).allSatisfy(note -> assertThat(note.getSource()).isEqualTo(com.github.enerccio.marginalia.domain.collections.AnnotationSource.AI));
        // the story is untouched, the model was told the part and the story after it
        assertThat(chatMessageService.find(first.getId()).getResponse()).isEqualTo("Anna met John.");
        assertThat(llm.lastCompletionRequest().getPromptText()).contains("Anna met John.").contains("John went home.");
    }

    @Test
    void aPartWithoutFindingsGetsNoNotes() throws Exception {
        llm.fallback(com.github.enerccio.marginalia.test.llm.MockLLMResponse.text("{\"findings\": []}"));

        JsonObject result = json("/generate-annotation " + first.getUuid());

        assertThat(result.get("created").getAsInt()).isZero();
        assertThat(annotationService.listForPart(book(), chatMessageService.find(first.getId()))).isEmpty();
    }

    @Test
    void anAnnotationProfileBelongsToAnAgent() {
        assertThatThrownBy(() -> run("/generate-annotation " + first.getUuid() + " Prose")).isInstanceOf(ExecutionException.class)
                .hasMessageContaining("agents");
    }

    @Test
    void aRedraftProfileBelongsToAnAgent() {
        assertThatThrownBy(() -> run("/redraft-part " + first.getUuid() + " \"\" \"\" Prose")).isInstanceOf(ExecutionException.class)
                .hasMessageContaining("agents");
    }

    @Test
    void impersonateReturnsTheDraftedInstructionsAndChangesNothing() throws Exception {
        llm.fallback(com.github.enerccio.marginalia.test.llm.MockLLMResponse.text("Anna asks John about the letter."));

        Object result = run("/impersonate \"She asks about the letter\"");

        assertThat(result).isEqualTo("Anna asks John about the letter.");
        String prompt = llm.lastCompletionRequest().getPromptText();
        assertThat(prompt).contains("She asks about the letter").contains("Anna met John.").contains("John went home.");
        assertThat(chatMessageService.getAllMessages(book())).hasSize(2);
        assertThat(book().getActiveLeaf().getId()).isEqualTo(second.getId());
    }

    @Test
    void impersonateTakesThePlanOfTheStoryAndAnotherPrompt() throws Exception {
        llm.fallback(com.github.enerccio.marginalia.test.llm.MockLLMResponse.text("Drafted."));

        run("/impersonate \"x\" \"Chapter 2\" \"DRAFT {{impersonateInstructions}} FOR {{outline}}\"");

        assertThat(llm.lastCompletionRequest().getPromptText()).contains("DRAFT x FOR Chapter 2");
        assertThatThrownBy(() -> run("/impersonate \"x\" \"\" \"{{#unclosed}}\"")).isInstanceOf(ExecutionException.class);
    }

    @Test
    void impersonateIsNotForAgents() {
        assertThat(commands.getCommands()).filteredOn(definition -> definition.commandName().equals("impersonate"))
                .isNotEmpty().noneMatch(definition -> definition.availableForAgent());
    }

    /**
     * A story that shows the generations of the commands: it records what its listener is told, and the page its generation belongs
     * to.
     */
    private static final class Story implements GenerationView {
        final Object page = new Object();
        final List<String> events = new CopyOnWriteArrayList<>();
        final List<Object> pagesOfTheRun = new CopyOnWriteArrayList<>();
        volatile GenerationRequestType shownType;

        @Override
        public Object getPage() {
            return page;
        }

        @Override
        public GenerationListener show(Manuscript book, GenerationRequest request) {
            shownType = request.getRequestType();
            return new GenerationListener() {
                @Override
                public void onNodeCreated(ChatMessage message) {
                    events.add("created");
                    Runs.Run generation = Runs.generationOf(book.getId());
                    pagesOfTheRun.add(generation == null ? "none" : generation.getPage());
                }

                @Override
                public void onReasoningChunk(String chunk, ChatMessage message) {
                }

                @Override
                public void onResponseChunk(String chunk, ChatMessage message) {
                    events.add("chunk");
                }

                @Override
                public void onMetricsUpdated(ChatMessage message) {
                }

                @Override
                public void onComplete(ChatMessage message) {
                    events.add("complete");
                }

                @Override
                public void onCancelled(ChatMessage partialMessage) {
                    events.add(partialMessage == null ? "cancelled:nothing" : "cancelled");
                }

                @Override
                public void onError(Throwable cause) {
                    events.add("error");
                }

                @Override
                public void onSimpleError(String error) {
                    events.add("error");
                }

                @Override
                public void askQuestion(String question, Runnable yes, Runnable no) {
                    yes.run();
                }
            };
        }
    }

    @Test
    void theStoryOfTheCommandBarShowsTheGenerationOnItsPage() throws Exception {
        llm.fallback(MockLLMResponse.chunks("Night ", "fell."));
        Story story = new Story();
        view = story;

        JsonObject result = json("/next");

        assertThat(part(result).getResponse()).isEqualTo("Night fell.");
        assertThat(story.shownType).isEqualTo(GenerationRequestType.NEW_MESSAGE);
        assertThat(story.events).startsWith("created").contains("chunk").endsWith("complete");
        assertThat(story.pagesOfTheRun).as("a closed page stops it, what follows the part opens there").containsExactly(story.page);
    }

    @Test
    void theRoundButtonStopsTheGenerationOfACommandAndTheCommandEndsStopped() throws Exception {
        CountDownLatch gate = new CountDownLatch(1);
        llm.fallback(MockLLMResponse.chunks("Night ", "fell.").waitFor(gate));
        Story story = new Story();
        view = story;

        CommandRun command = commands.executeLine("/next", false, scope());
        Runs.Run generation = null;
        for (int i = 0; i < 600 && generation == null; i++) {
            generation = Runs.generationOf(manuscript.getId());
            if (generation == null) {
                Thread.sleep(50);
            }
        }
        assertThat(generation).as("the generation of the command runs").isNotNull();
        // what the round button does for a generation the tab did not start
        generation.stop();
        gate.countDown();

        assertThatThrownBy(() -> command.get(60, TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class)
                .cause().isInstanceOf(CommandException.class).cause().isInstanceOf(CancellationException.class);
        assertThat(story.events).anyMatch(event -> event.startsWith("cancelled"));
    }

    @Test
    void aRefusedGenerationLetsTheStoryGo() throws Exception {
        Story story = new Story();
        view = story;
        Runs.Run other = Runs.run(Runs.GENERATION, () -> "another generation").book(book()).start();
        try {
            assertThatThrownBy(() -> run("/next")).isInstanceOf(ExecutionException.class);
        } finally {
            other.end();
        }
        assertThat(story.events).containsExactly("cancelled:nothing");
    }

    @Test
    void onlyTheGenerationsOfTheStoryShowTheirProgressInIt() {
        assertThat(commands.getCommands()).filteredOn(definition -> definition.progressInStory())
                .extracting(definition -> definition.commandName()).containsOnly("next", "swipe", "regenerate");
    }
}
