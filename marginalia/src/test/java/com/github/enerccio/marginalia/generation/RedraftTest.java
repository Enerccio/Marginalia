package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.RedraftService.ContextInsufficient;
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
 * Redrafting a part: what the model is told, which story is around the part and that nothing is stored.
 * <p>
 * The story is five parts with the texts {@code TEXT-1} ... {@code TEXT-5}.
 */
class RedraftTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private RedraftService redraftService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private AnnotationService annotationService;

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

    private record Redrafted(String reasoning, String text, boolean finished) {
    }

    private Redrafted redraft(int n, String instructions) throws Exception {
        CompletableFuture<Redrafted> done = new CompletableFuture<>();
        var token = redraftService.redraft(manuscriptService.find(manuscript.getId()), part(n), instructions, new RedraftService.Callback() {
            @Override
            public void onProgress(String reasoning, String text) {
            }

            @Override
            public void onFinished(String reasoning, String text) {
                done.complete(new Redrafted(reasoning, text, true));
            }

            @Override
            public void onTerminated(String reasoning, String text) {
                done.complete(new Redrafted(reasoning, text, false));
            }

            @Override
            public void onError(Throwable throwable) {
                done.completeExceptionally(throwable);
            }
        });
        assertThat(token).isNotNull();
        return done.get(30, TimeUnit.SECONDS);
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
    void promptHasThePartTheInstructionsAndTheStoryAround() throws Exception {
        ChatMessage third = part(3);
        third.setSceneSetting("the harbour");
        third.setPovCharacter("Alice");
        third.setInstructions("Alice finds the map");
        third.setBackgroundLore("Dragons exist.");
        chatMessageService.save(third);
        llm.enqueue(MockLLMResponse.text("  REDRAFTED  "));

        Redrafted result = redraft(3, "Make the harbour scene longer");

        assertThat(result.finished()).isTrue();
        assertThat(result.text()).isEqualTo("REDRAFTED");
        String prompt = prompt();
        assertThat(between(prompt, "=== PART TO REDRAFT START ===", "=== PART TO REDRAFT END ===")).contains("TEXT-3");
        assertThat(between(prompt, "=== STORY BEFORE THE PART START ===", "=== STORY BEFORE THE PART END ==="))
                .contains("TEXT-1").contains("TEXT-2").doesNotContain("TEXT-3");
        assertThat(between(prompt, "=== STORY AFTER THE PART START ===", "=== STORY AFTER THE PART END ==="))
                .contains("TEXT-4").contains("TEXT-5").doesNotContain("TEXT-3");
        assertThat(between(prompt, "=== AUTHOR'S INSTRUCTIONS START ===", "=== AUTHOR'S INSTRUCTIONS END ==="))
                .contains("Make the harbour scene longer");
        // the data the part was written with
        assertThat(prompt).contains("Location & Time of the part: the harbour").contains("Point of View Character of the part: Alice")
                .contains("Alice finds the map").contains("Dragons exist.");
        // one message, nothing is stored
        assertThat(llm.lastCompletionRequest().getMessages()).hasSize(1);
        assertThat(part(3).getResponse()).isEqualTo("TEXT-3");
        assertThat(chatMessageService.getAllMessages(manuscriptService.find(manuscript.getId()))).hasSize(5);
    }

    @Test
    void firstAndLastPartHaveNoStoryOnOneSide() throws Exception {
        redraft(1, "x");
        assertThat(prompt()).doesNotContain("STORY BEFORE THE PART START").contains("STORY AFTER THE PART START");

        redraft(5, "x");
        assertThat(prompt()).contains("STORY BEFORE THE PART START").doesNotContain("STORY AFTER THE PART START");
    }

    @Test
    void withoutInstructionsThePartIsWrittenAgain() throws Exception {
        redraft(2, "   ");

        assertThat(between(prompt(), "=== AUTHOR'S INSTRUCTIONS START ===", "=== AUTHOR'S INSTRUCTIONS END ==="))
                .contains("Write the part again");
    }

    @Test
    void validSummariesStandInForTheOlderStory() throws Exception {
        llm.enqueue(MockLLMResponse.text("SUMMARY-OF-12"));
        CompletableFuture<Object> summarized = new CompletableFuture<>();
        summaryService.createSummary(manuscriptService.find(manuscript.getId()), part(2), new SummaryService.AsyncCallback() {
            @Override
            public void onSummaryProgress(String reasoning, String summary) {
            }

            @Override
            public void onSummaryFinished(com.github.enerccio.marginalia.domain.model.impl.Summary summary) {
                summarized.complete(summary);
            }

            @Override
            public void onSummaryTerminated() {
                summarized.completeExceptionally(new IllegalStateException("terminated"));
            }

            @Override
            public void onError(Throwable throwable) {
                summarized.completeExceptionally(throwable);
            }
        });
        summarized.get(30, TimeUnit.SECONDS);

        redraft(4, "x");

        String prompt = prompt();
        assertThat(between(prompt, "=== SUMMARY OF THE STORY BEFORE THE PART START ===", "=== SUMMARY OF THE STORY BEFORE THE PART END ==="))
                .contains("SUMMARY-OF-12");
        assertThat(between(prompt, "=== STORY BEFORE THE PART START ===", "=== STORY BEFORE THE PART END ==="))
                .contains("TEXT-3").doesNotContain("TEXT-1").doesNotContain("TEXT-2");

        // the story the summary describes was changed: the summary is not told to the model, the story is
        chatMessageService.storeResponse(manuscriptService.find(manuscript.getId()), part(1), "TEXT-1 CHANGED");
        redraft(4, "x");

        prompt = prompt();
        assertThat(prompt).doesNotContain("SUMMARY-OF-12").doesNotContain("SUMMARY OF THE STORY BEFORE THE PART START");
        assertThat(between(prompt, "=== STORY BEFORE THE PART START ===", "=== STORY BEFORE THE PART END ==="))
                .contains("TEXT-1 CHANGED").contains("TEXT-2").contains("TEXT-3");
    }

    @Test
    void theNewestStoryIsKeptWhenNotAllOfItFits() throws Exception {
        // 1000 tokens of the context are reserved for the response, about 720 are the prompt without the story, so the
        // room is for three of the parts of 100 tokens
        protocol.setMaxTokens(1000 + 1100);
        protocolService.save(protocol);
        for (int i = 1; i <= 5; i++) {
            ChatMessage part = part(i);
            part.setResponse("TEXT-" + i + " " + "word ".repeat(100));
            part.setTokenCount(100);
            chatMessageService.save(part);
        }

        redraft(5, "x");

        String before = between(prompt(), "=== STORY BEFORE THE PART START ===", "=== STORY BEFORE THE PART END ===");
        assertThat(before).contains("TEXT-4").doesNotContain("TEXT-1");
    }

    @Test
    void failsWhenThePartAloneDoesNotFit() throws Exception {
        protocol.setMaxTokens(1000 + 10);
        protocolService.save(protocol);

        assertThatThrownBy(() -> redraftService.redraft(manuscriptService.find(manuscript.getId()), part(3), "x",
                new RedraftService.Callback() {
                    @Override
                    public void onProgress(String reasoning, String text) {
                    }

                    @Override
                    public void onFinished(String reasoning, String text) {
                    }

                    @Override
                    public void onTerminated(String reasoning, String text) {
                    }

                    @Override
                    public void onError(Throwable throwable) {
                    }
                })).isInstanceOf(ContextInsufficient.class);
    }

    @Test
    void refusesAPartWithoutText() throws Exception {
        ChatMessage third = part(3);
        third.setResponse("  ");
        chatMessageService.save(third);

        assertThatThrownBy(() -> redraft(3, "x")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void promptComesFromTheBookThenTheUserThenTheDefault() throws Exception {
        assertThat(manuscriptService.getRedraftPrompt(manuscript)).isEqualTo(Defaults.DEFAULT_REDRAFT_PROMPT);

        UserSetting settings = settingService.getOrCreate(UserSetting.class);
        settings.setDefaultRedraftPrompt("USER {{text}}");
        settingService.save(settings);
        assertThat(manuscriptService.getRedraftPrompt(manuscript)).isEqualTo("USER {{text}}");
        redraft(2, "x");
        assertThat(prompt()).contains("USER TEXT-2");

        Manuscript own = manuscriptService.find(manuscript.getId());
        own.setRedraftPrompt("BOOK {{text}} / {{redraftInstructions}}");
        manuscript = manuscriptService.save(own);
        assertThat(manuscriptService.getRedraftPrompt(manuscript)).isEqualTo("BOOK {{text}} / {{redraftInstructions}}");
        redraft(2, "shorter");
        assertThat(prompt()).contains("BOOK TEXT-2 / shorter");
    }

    @Test
    void theOpenAnnotationsAreGivenToTheModelWhenAsked() throws Exception {
        annotationService.create(manuscript, part(3), 0, 4, "Too short", AnnotationSource.AUTHOR, null, null);
        Annotation closed = annotationService.create(manuscript, part(3), 5, 6, "Fixed already", AnnotationSource.AUTHOR, null, null);
        annotationService.setStatus(manuscript, closed, AnnotationStatus.RESOLVED);
        annotationService.create(manuscript, part(3), null, null, "No weather", AnnotationSource.AUTHOR, null, null);
        llm.enqueue(MockLLMResponse.text("REDRAFTED"));

        CompletableFuture<String> done = new CompletableFuture<>();
        redraftService.redraft(manuscriptService.find(manuscript.getId()), part(3), "", null,
                com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions.defaults(), true, new RedraftService.Callback() {
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
        done.get(30, TimeUnit.SECONDS);

        String prompt = prompt();
        assertThat(between(prompt, "=== AUTHOR'S NOTES ON PASSAGES OF THE PART START ===", "=== AUTHOR'S NOTES ON PASSAGES OF THE PART END ==="))
                .contains("\"TEXT\": Too short").contains("(the whole part): No weather").doesNotContain("Fixed already");
        // the notes are the instructions when the author gave none
        assertThat(prompt).doesNotContain("Write the part again");
    }

    @Test
    void theAnnotationsAreNotGivenUnlessAsked() throws Exception {
        annotationService.create(manuscript, part(3), 0, 4, "Too short", AnnotationSource.AUTHOR, null, null);
        llm.enqueue(MockLLMResponse.text("REDRAFTED"));

        redraft(3, "");

        assertThat(prompt()).doesNotContain("AUTHOR'S NOTES").doesNotContain("Too short").contains("Write the part again");
    }
}
