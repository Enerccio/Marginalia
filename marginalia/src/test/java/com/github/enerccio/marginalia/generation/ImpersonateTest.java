package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ImpersonateService;
import com.github.enerccio.marginalia.domain.service.ImpersonateService.ContextInsufficient;
import com.github.enerccio.marginalia.domain.service.MessagePromptService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Drafting the instructions of the next turn: what the model is told and that nothing is stored.
 * <p>
 * The story is five parts with the texts {@code TEXT-1} ... {@code TEXT-5}.
 */
class ImpersonateTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private MessagePromptService messagePromptService;

    @Autowired
    private ImpersonateService impersonateService;

    @Autowired
    private SettingService settingService;

    private Manuscript manuscript;
    private List<ChatMessage> story;

    @BeforeEach
    void createStory() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
        story = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ChatMessage message = new ChatMessage();
            message.setResponse("TEXT-" + i);
            message.setTokenCount(5);
            ChatMessage saved = i == 1 ? chatMessageService.createRoot(manuscript, message)
                    : chatMessageService.addChild(story.getLast(), message);
            story.add(saved);
        }
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setActiveLeaf(story.getLast());
        manuscript = manuscriptService.save(loaded);
    }

    private ChatMessage part(int n) throws Exception {
        return chatMessageService.find(story.get(n - 1).getId());
    }

    private record Drafted(String reasoning, String text, boolean finished) {
    }

    private Drafted impersonate(ImpersonateService.Request request) throws Exception {
        CompletableFuture<Drafted> done = new CompletableFuture<>();
        var token = impersonateService.impersonate(manuscriptService.find(manuscript.getId()), request, new ImpersonateService.Callback() {
            @Override
            public void onProgress(String reasoning, String text) {
            }

            @Override
            public void onFinished(String reasoning, String text) {
                done.complete(new Drafted(reasoning, text, true));
            }

            @Override
            public void onTerminated(String reasoning, String text) {
                done.complete(new Drafted(reasoning, text, false));
            }

            @Override
            public void onError(Throwable throwable) {
                done.completeExceptionally(throwable);
            }
        });
        assertThat(token).isNotNull();
        return done.get(30, TimeUnit.SECONDS);
    }

    private Drafted impersonate(String guidance) throws Exception {
        return impersonate(ImpersonateService.Request.of(guidance));
    }

    private String prompt() {
        return llm.lastCompletionRequest().getPromptText();
    }

    private static String between(String text, String from, String to) {
        int start = text.indexOf(from);
        assertThat(start).as(from).isGreaterThanOrEqualTo(0);
        int end = text.indexOf(to, start);
        assertThat(end).as(to).isGreaterThan(start);
        return text.substring(start + from.length(), end);
    }

    @Test
    void promptHasTheStoryTheGuidanceAndTheTurnDetailsOfTheLastPart() throws Exception {
        ChatMessage last = part(5);
        last.setSceneSetting("the harbour");
        last.setPovCharacter("Alice");
        last.setPresentCharacters("Alice, Bob");
        chatMessageService.save(last);
        messagePromptService.store(last, null, "Dragons exist.");
        llm.enqueue(MockLLMResponse.text("  DRAFTED  "));

        Drafted result = impersonate("She confronts him about the letter");

        assertThat(result.finished()).isTrue();
        assertThat(result.text()).isEqualTo("DRAFTED");
        String prompt = prompt();
        assertThat(between(prompt, "=== STORY SO FAR START ===", "=== STORY SO FAR END ==="))
                .contains("TEXT-1").contains("TEXT-3").contains("TEXT-5");
        assertThat(between(prompt, "=== AUTHOR'S GUIDANCE START ===", "=== AUTHOR'S GUIDANCE END ==="))
                .contains("She confronts him about the letter");
        assertThat(prompt).contains("Location & Time of the next part: the harbour").contains("Point of View Character of the next part: Alice")
                .contains("Characters present in the next part: Alice, Bob").contains("Dragons exist.")
                .doesNotContain("PLAN OF THE STORY START");
        // one message, nothing is stored
        assertThat(llm.lastCompletionRequest().getMessages()).hasSize(1);
        assertThat(chatMessageService.getAllMessages(manuscriptService.find(manuscript.getId()))).hasSize(5);
        assertThat(manuscriptService.find(manuscript.getId()).getActiveLeaf().getId()).isEqualTo(story.getLast().getId());
    }

    @Test
    void theTurnDetailsOfTheRequestReplaceTheOnesOfTheLastPart() throws Exception {
        ChatMessage last = part(5);
        last.setSceneSetting("the harbour");
        last.setPovCharacter("Alice");
        chatMessageService.save(last);

        impersonate(ImpersonateService.Request.of("x").withTurn("the castle", "", "Carol"));

        String prompt = prompt();
        assertThat(prompt).contains("Location & Time of the next part: the castle").contains("Characters present in the next part: Carol")
                .doesNotContain("the harbour").doesNotContain("Point of View Character");
    }

    @Test
    void withoutGuidanceTheStoryContinuesWhereItLeads() throws Exception {
        impersonate("   ");

        assertThat(between(prompt(), "=== AUTHOR'S GUIDANCE START ===", "=== AUTHOR'S GUIDANCE END ==="))
                .contains("continue the story where it leads");
    }

    @Test
    void thePlanOfTheCallerIsGivenToTheModel() throws Exception {
        impersonate(ImpersonateService.Request.of("x").withOutline("Chapter 3: the siege\nThe city falls."));

        assertThat(between(prompt(), "=== PLAN OF THE STORY START ===", "=== PLAN OF THE STORY END ==="))
                .contains("Chapter 3: the siege").contains("The city falls.");
    }

    @Test
    void theLastInstructionsOfTheAuthorAreTheExamples() throws Exception {
        for (int i = 1; i <= 5; i++) {
            ChatMessage part = part(i);
            part.setInstructions(i == 3 ? "  " : "INSTR-" + i);
            chatMessageService.save(part);
        }

        impersonate("x");

        // the three newest that are not blank, oldest first
        String examples = between(prompt(), "=== EXAMPLES OF THE AUTHOR'S INSTRUCTIONS START ===", "=== EXAMPLES OF THE AUTHOR'S INSTRUCTIONS END ===");
        assertThat(examples).contains("INSTR-2").contains("INSTR-4").contains("INSTR-5").doesNotContain("INSTR-1");
        assertThat(examples.indexOf("INSTR-2")).isLessThan(examples.indexOf("INSTR-4"));
        assertThat(examples.indexOf("INSTR-4")).isLessThan(examples.indexOf("INSTR-5"));
    }

    @Test
    void noExamplesWhenTheAuthorGaveNoInstructions() throws Exception {
        impersonate("x");

        assertThat(prompt()).doesNotContain("EXAMPLES OF THE AUTHOR'S INSTRUCTIONS");
    }

    @Test
    void aBookWithoutPartsCanBeContinued() throws Exception {
        Manuscript empty = manuscriptService.save(newManuscript());
        llm.enqueue(MockLLMResponse.text("OPENING"));

        CompletableFuture<String> done = new CompletableFuture<>();
        impersonateService.impersonate(manuscriptService.find(empty.getId()),
                ImpersonateService.Request.of("The story opens in a storm").withOutline("Chapter 1"), new ImpersonateService.Callback() {
                    public void onProgress(String reasoning, String text) {
                    }

                    public void onFinished(String reasoning, String text) {
                        done.complete(text);
                    }

                    public void onTerminated(String reasoning, String text) {
                        done.complete(text);
                    }

                    public void onError(Throwable throwable) {
                        done.completeExceptionally(throwable);
                    }
                });

        assertThat(done.get(30, TimeUnit.SECONDS)).isEqualTo("OPENING");
        assertThat(prompt()).doesNotContain("STORY SO FAR START").contains("The story opens in a storm").contains("Chapter 1");
    }

    @Test
    void failsWhenThePromptAloneDoesNotFit() throws Exception {
        protocol.setMaxTokens(1000 + 10);
        protocolService.save(protocol);

        assertThatThrownBy(() -> impersonate("x")).isInstanceOf(ContextInsufficient.class);
    }

    @Test
    void promptComesFromTheBookThenTheUserThenTheDefault() throws Exception {
        assertThat(manuscriptService.getImpersonatePrompt(manuscript)).isEqualTo(Defaults.DEFAULT_IMPERSONATE_PROMPT);

        UserSetting settings = settingService.getOrCreate(UserSetting.class);
        settings.setDefaultImpersonatePrompt("USER {{previousStory}}");
        settingService.save(settings);
        assertThat(manuscriptService.getImpersonatePrompt(manuscript)).isEqualTo("USER {{previousStory}}");
        impersonate("x");
        assertThat(prompt()).contains("USER TEXT-1");

        Manuscript own = manuscriptService.find(manuscript.getId());
        own.setImpersonatePrompt("BOOK {{impersonateInstructions}}");
        manuscript = manuscriptService.save(own);
        assertThat(manuscriptService.getImpersonatePrompt(manuscript)).isEqualTo("BOOK {{impersonateInstructions}}");
        impersonate("shorter");
        assertThat(prompt()).contains("BOOK shorter");

        // a prompt of the request wins over both
        impersonate(ImpersonateService.Request.of("x").withPrompt("REQUEST {{outline}}").withOutline("plan"));
        assertThat(prompt()).contains("REQUEST plan");
    }
}
