package com.github.enerccio.marginalia.command;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.command.CommandScope;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

    private Manuscript manuscript;
    private ChatMessage first, second;
    private boolean answer = true;

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
    void aRedraftProfileBelongsToAnAgent() {
        assertThatThrownBy(() -> run("/redraft-part " + first.getUuid() + " \"\" \"\" Prose")).isInstanceOf(ExecutionException.class)
                .hasMessageContaining("agents");
    }
}
