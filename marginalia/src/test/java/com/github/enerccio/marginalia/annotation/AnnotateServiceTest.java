package com.github.enerccio.marginalia.annotation;

import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.AnnotateService;
import com.github.enerccio.marginalia.domain.service.AnnotateService.Mode;
import com.github.enerccio.marginalia.domain.service.AnnotateService.Proposal;
import com.github.enerccio.marginalia.domain.service.AnnotateService.Request;
import com.github.enerccio.marginalia.domain.service.AnnotationService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.service.impl.AnnotateServiceImpl;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMRequest;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import com.github.enerccio.marginalia.test.llm.MockLLMScenario;
import com.github.enerccio.marginalia.test.llm.MockLLMServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Letting a model annotate a part: what it is told, how its answer is read in each mode (and what happens with an answer that can't
 * be read), how the quotes are tied to the text, and that nothing is stored before the author accepts.
 */
class AnnotateServiceTest extends GenerationTestBase {

    private static final String TEXT = "Anna met John at the bar. They talked for hours. Then she left.";


    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private AnnotationService annotationService;

    @Autowired
    private AnnotateService annotateService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private com.github.enerccio.marginalia.domain.service.MessagePromptService messagePromptService;

    /** The reasoning of the model as the run reported it while the answer was written. */
    private final List<String> reasoningSeen = new java.util.concurrent.CopyOnWriteArrayList<>();

    private Manuscript manuscript;
    private ChatMessage before;
    private ChatMessage part;

    @BeforeEach
    void createStory() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
        ChatMessage first = new ChatMessage();
        first.setResponse("Anna sailed to the harbour.");
        first.setTokenCount(6);
        before = chatMessageService.createRoot(manuscript, first);
        ChatMessage second = new ChatMessage();
        second.setResponse(TEXT);
        second.setTokenCount(15);
        part = chatMessageService.addChild(before, second);
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setActiveLeaf(part);
        manuscript = manuscriptService.save(loaded);
    }

    private sealed interface Outcome permits Done, Unreadable, Stopped, Failed {
    }

    private record Done(List<Proposal> proposals, String raw) implements Outcome {
    }

    private record Unreadable(String raw, String reason) implements Outcome {
    }

    private record Stopped(String raw) implements Outcome {
    }

    private record Failed(Throwable error) implements Outcome {
    }

    private Outcome run(Request request) throws Exception {
        return run(manuscript, part, request);
    }

    private Outcome run(Manuscript book, ChatMessage target, Request request) throws Exception {
        CompletableFuture<Outcome> done = new CompletableFuture<>();
        var token = annotateService.annotate(manuscriptService.find(book.getId()), chatMessageService.find(target.getId()), request,
                new AnnotateService.Callback() {
                    @Override
                    public void onProgress(String reasoning, String text) {
                        if (!reasoning.isBlank()) {
                            reasoningSeen.add(reasoning);
                        }
                    }

                    @Override
                    public void onFinished(List<Proposal> proposals, String raw) {
                        done.complete(new Done(proposals, raw));
                    }

                    @Override
                    public void onUnreadable(String raw, String reason) {
                        done.complete(new Unreadable(raw, reason));
                    }

                    @Override
                    public void onTerminated(String raw) {
                        done.complete(new Stopped(raw));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        done.complete(new Failed(throwable));
                    }
                });
        assertThat(token).isNotNull();
        return done.get(20, TimeUnit.SECONDS);
    }

    private Done done(Request request) throws Exception {
        return done(manuscript, part, request);
    }

    private Done done(Manuscript book, ChatMessage target, Request request) throws Exception {
        Outcome outcome = run(book, target, request);
        assertThat(outcome).isInstanceOf(Done.class);
        return (Done) outcome;
    }

    private static Request request(String instructions, Mode mode) {
        return new Request(instructions, null, GenerationOptions.defaults(), mode, null, null);
    }

    private static final String FINDINGS = """
            {"findings": [
              {"quote": "They talked for hours.", "note": "Weak scene", "type": "pacing"},
              {"quote": "Then she left.", "note": "Abrupt"}
            ]}""";

    // ------------------------------------------------------------------------------------------------ structured

    @Test
    void structuredAnswerBecomesProposalsTiedToThePassages() throws Exception {
        llm.enqueue(MockLLMResponse.text(FINDINGS));

        Done result = done(request("flag weak scenes", Mode.STRUCTURED));

        assertThat(result.proposals()).hasSize(2);
        Proposal first = result.proposals().getFirst();
        assertThat(first.quote()).isEqualTo("They talked for hours.");
        assertThat(first.note()).isEqualTo("Weak scene");
        assertThat(first.type()).isEqualTo("pacing");
        assertThat(first.located()).isTrue();
        assertThat(TEXT.substring(first.start(), first.end())).isEqualTo("They talked for hours.");
        assertThat(result.proposals().get(1).type()).isNull();
    }

    @Test
    void theModelGetsThePartTheStoryAroundItTheInstructionsAndTheFormat() throws Exception {
        llm.enqueue(MockLLMResponse.text("{\"findings\": []}"));

        done(request("flag weak scenes", Mode.STRUCTURED));

        String prompt = llm.lastCompletionRequest().getPromptText();
        assertThat(prompt).contains(TEXT).contains("Anna sailed to the harbour.").contains("flag weak scenes")
                .contains("OUTPUT FORMAT").contains("\"findings\"");
        assertThat(llm.lastCompletionRequest().isStream()).isTrue();
    }

    @Test
    void withoutInstructionsThePromptHasItsOwnTask() throws Exception {
        llm.enqueue(MockLLMResponse.text("{\"findings\": []}"));

        done(request("", Mode.STRUCTURED));

        assertThat(llm.lastCompletionRequest().getPromptText()).contains("continuity errors");
    }

    @Test
    void aQuoteThatIsNotInThePartIsAProposalAboutTheWholePart() throws Exception {
        llm.enqueue(MockLLMResponse.text("{\"findings\": [{\"quote\": \"A dragon landed on the church\", \"note\": \"Contradicts the lore\"}]}"));

        Done result = done(request(null, Mode.STRUCTURED));

        assertThat(result.proposals()).hasSize(1);
        assertThat(result.proposals().getFirst().located()).isFalse();
        assertThat(result.proposals().getFirst().note()).isEqualTo("Contradicts the lore");
    }

    @Test
    void aLooseQuoteIsStillFound() throws Exception {
        llm.enqueue(MockLLMResponse.text("{\"findings\": [{\"quote\": \"they talked for HOURS\", \"note\": \"Weak\"}]}"));

        Done result = done(request(null, Mode.STRUCTURED));

        Proposal proposal = result.proposals().getFirst();
        assertThat(proposal.located()).isTrue();
        assertThat(TEXT.substring(proposal.start(), proposal.end())).isEqualToIgnoringCase("They talked for hours");
    }

    @Test
    void anAnswerThatCanNotBeReadIsAskedForOnceMore() throws Exception {
        llm.enqueue(MockLLMResponse.text("Sure! Here are my thoughts about the part."), MockLLMResponse.text(FINDINGS));

        Done result = done(request(null, Mode.STRUCTURED));

        assertThat(result.proposals()).hasSize(2);
        List<MockLLMRequest> requests = llm.getCompletionRequests();
        assertThat(requests).hasSize(2);
        assertThat(requests.get(1).getPromptText()).contains("could not be read").contains("Sure! Here are my thoughts");
    }

    @Test
    void anAnswerThatCanNotBeReadTwiceIsGivenAsItIs() throws Exception {
        llm.enqueue(MockLLMResponse.text("First, no JSON."), MockLLMResponse.text("Second, no JSON either."));

        Outcome outcome = run(request(null, Mode.STRUCTURED));

        assertThat(outcome).isInstanceOf(Unreadable.class);
        assertThat(((Unreadable) outcome).raw()).isEqualTo("Second, no JSON either.");
        assertThat(((Unreadable) outcome).reason()).isNotBlank();
        assertThat(llm.getCompletionRequests()).hasSize(2);
    }

    @Test
    void autoAsksForTheSchemaToo() throws Exception {
        llm.enqueue(MockLLMResponse.text(FINDINGS));

        Done result = done(request(null, Mode.AUTO));

        assertThat(result.proposals()).hasSize(2);
        assertThat(llm.lastCompletionRequest().isStream()).isTrue();
    }

    // ------------------------------------------------------------------------------------------------ delimited

    @Test
    void delimitedAnswerIsReadLeniently() throws Exception {
        llm.enqueue(MockLLMResponse.chunks("> They talked for hours.\n[pacing] Weak scene,\nnothing happens.\n---\n",
                "> \"Then she left.\"\nAbrupt\n\n> Anna met John at the bar.\nWhich bar?"));

        Done result = done(request(null, Mode.DELIMITED));

        assertThat(result.proposals()).hasSize(3);
        Proposal first = result.proposals().getFirst();
        assertThat(first.quote()).isEqualTo("They talked for hours.");
        assertThat(first.type()).isEqualTo("pacing");
        assertThat(first.note()).isEqualTo("Weak scene,\nnothing happens.");
        assertThat(result.proposals().get(1).quote()).isEqualTo("Then she left.");
        assertThat(result.proposals().get(1).located()).isTrue();
        assertThat(result.proposals().get(2).note()).isEqualTo("Which bar?");
        assertThat(llm.lastCompletionRequest().isStream()).isTrue();
        assertThat(llm.lastCompletionRequest().getPromptText()).contains("separated by a line with three dashes");
    }

    @Test
    void delimitedAnswerOfNothingHasNoProposals() throws Exception {
        llm.enqueue(MockLLMResponse.text("NONE"));

        Done result = done(request(null, Mode.DELIMITED));

        assertThat(result.proposals()).isEmpty();
        assertThat(result.raw()).isEqualTo("NONE");
    }

    @Test
    void aBlockWithoutAQuoteIsANoteAboutTheWholePart() {
        List<AnnotateServiceImpl.Finding> found = AnnotateServiceImpl.parseBlocks("The pacing of the part drags.\n---\n> Then she left.\nAbrupt");

        assertThat(found).hasSize(2);
        assertThat(found.getFirst().quote()).isEmpty();
        assertThat(found.getFirst().note()).isEqualTo("The pacing of the part drags.");
        assertThat(found.get(1).quote()).isEqualTo("Then she left.");
    }

    @Test
    void aQuoteOfSeveralLinesIsOnePassage() {
        List<AnnotateServiceImpl.Finding> found = AnnotateServiceImpl.parseBlocks("> They talked\n> for hours.\nWeak");

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().quote()).isEqualTo("They talked for hours.");
        assertThat(AnnotateServiceImpl.parseBlocks("")).isEmpty();
        assertThat(AnnotateServiceImpl.parseBlocks("No problems.")).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------ two stages

    @Test
    void theSecondModelTurnsTheCritiqueIntoNotes() throws Exception {
        MockLLMScenario other = MockLLMServer.shared().scenario();
        try {
            OpenAICompatible extractor = createAI(other);
            llm.enqueue(MockLLMResponse.chunks("The scene where \"They talked for hours.\" ", "is weak, nothing happens."));
            other.enqueue(MockLLMResponse.text("{\"findings\": [{\"quote\": \"They talked for hours.\", \"note\": \"Weak scene\"}]}"));

            Done result = done(new Request("", null, GenerationOptions.defaults(), Mode.TWO_STAGE, extractor, null));

            assertThat(result.proposals()).hasSize(1);
            assertThat(result.proposals().getFirst().note()).isEqualTo("Weak scene");
            assertThat(result.proposals().getFirst().located()).isTrue();
            assertThat(result.raw()).contains("is weak");
            // the first model wrote the critique as text, the second one got the part and the critique
            assertThat(llm.getCompletionRequests()).hasSize(1);
            assertThat(llm.lastCompletionRequest().isStream()).isTrue();
            assertThat(llm.lastCompletionRequest().getPromptText()).contains("Write your review as free text");
            assertThat(other.getCompletionRequests()).hasSize(1);
            assertThat(other.lastCompletionRequest().getPromptText()).contains(TEXT).contains("is weak, nothing happens.");
        } finally {
            other.close();
        }
    }

    @Test
    void withoutASecondModelTheFirstOneExtracts() throws Exception {
        llm.enqueue(MockLLMResponse.text("The scene \"They talked for hours.\" is weak."),
                MockLLMResponse.text("{\"findings\": [{\"quote\": \"They talked for hours.\", \"note\": \"Weak\"}]}"));

        Done result = done(request(null, Mode.TWO_STAGE));

        assertThat(result.proposals()).hasSize(1);
        assertThat(llm.getCompletionRequests()).hasSize(2);
    }

    // ------------------------------------------------------------------------------------------------ the prompt

    @Test
    void theOpenAnnotationsAreToldToTheModelSoItDoesNotRepeatThem() throws Exception {
        int start = TEXT.indexOf("Then she left.");
        annotationService.create(manuscript, part, start, start + 14, "Abrupt ending", AnnotationSource.AUTHOR, null, null);
        Annotation closed = annotationService.create(manuscript, part, 0, 4, "Name?", AnnotationSource.AUTHOR, null, null);
        annotationService.setStatus(manuscript, closed, AnnotationStatus.RESOLVED);
        llm.enqueue(MockLLMResponse.text("{\"findings\": []}"));

        done(request(null, Mode.STRUCTURED));

        String prompt = llm.lastCompletionRequest().getPromptText();
        assertThat(prompt).contains("EXISTING NOTES").contains("\"Then she left.\": Abrupt ending");
        assertThat(prompt).doesNotContain("Name?");
    }

    @Test
    void thePromptOfTheBookComesBeforeThePromptOfTheUser() throws Exception {
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        setting.setDefaultAnnotatePrompt("USER PROMPT {{text}} {{outputFormat}}");
        settingService.save(setting);
        llm.enqueue(MockLLMResponse.text("{\"findings\": []}"), MockLLMResponse.text("{\"findings\": []}"));

        try {
            done(request(null, Mode.STRUCTURED));
            assertThat(llm.lastCompletionRequest().getPromptText()).contains("USER PROMPT " + TEXT);

            Manuscript loaded = manuscriptService.find(manuscript.getId());
            loaded.setAnnotatePrompt("BOOK PROMPT {{text}} {{outputFormat}}");
            manuscript = manuscriptService.save(loaded);
            done(request(null, Mode.STRUCTURED));
            assertThat(llm.lastCompletionRequest().getPromptText()).contains("BOOK PROMPT " + TEXT).doesNotContain("USER PROMPT");
        } finally {
            setting.setDefaultAnnotatePrompt(null);
            settingService.save(setting);
        }
    }

    @Test
    void aPromptCanBeGivenForOneRun() throws Exception {
        llm.enqueue(MockLLMResponse.text("{\"findings\": []}"));

        done(new Request("x", "ONE RUN {{annotateInstructions}}", GenerationOptions.defaults(), Mode.STRUCTURED, null, null));

        assertThat(llm.lastCompletionRequest().getPromptText()).contains("ONE RUN x");
    }

    // ------------------------------------------------------------------------------------------------ while the model writes

    @Test
    void theReasoningOfTheModelIsReportedWhileItWritesTheStructuredAnswer() throws Exception {
        llm.enqueue(MockLLMResponse.text(FINDINGS).withReasoning("Let me read ", "the part again."));

        Done result = done(request("flag weak scenes", Mode.STRUCTURED));

        assertThat(result.proposals()).hasSize(2);
        assertThat(reasoningSeen).isNotEmpty();
        assertThat(reasoningSeen.getLast()).isEqualTo("Let me read the part again.");
    }

    @Test
    void stopEndsTheRunAtOnceAndWhatTheModelAnswersLaterIsDropped() throws Exception {
        java.util.concurrent.CountDownLatch gate = new java.util.concurrent.CountDownLatch(1);
        llm.enqueue(MockLLMResponse.text(FINDINGS).waitFor(gate));
        List<String> events = new java.util.concurrent.CopyOnWriteArrayList<>();
        CompletableFuture<Void> stopped = new CompletableFuture<>();
        var token = annotateService.annotate(manuscriptService.find(manuscript.getId()), chatMessageService.find(part.getId()),
                request("flag weak scenes", Mode.STRUCTURED), new AnnotateService.Callback() {
                    @Override
                    public void onProgress(String reasoning, String text) {
                        events.add("progress");
                    }

                    @Override
                    public void onFinished(List<Proposal> proposals, String raw) {
                        events.add("finished");
                    }

                    @Override
                    public void onUnreadable(String raw, String reason) {
                        events.add("unreadable");
                    }

                    @Override
                    public void onTerminated(String raw) {
                        events.add("terminated");
                        stopped.complete(null);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        events.add("error");
                    }
                });
        assertThat(token).isNotNull();
        llm.awaitCompletionRequests(1, java.time.Duration.ofSeconds(10));

        // the model has not written a word yet: the request is out, the author presses Stop
        token.cancel();

        stopped.get(5, TimeUnit.SECONDS);
        gate.countDown();
        Thread.sleep(1000);
        assertThat(events).containsExactly("terminated");
    }

    // ------------------------------------------------------------------------------------------------ what a server can reuse

    private List<MockLLMRequest.Message> sent() {
        return llm.lastCompletionRequest().getMessages();
    }

    private static List<String> roles(List<MockLLMRequest.Message> messages) {
        return messages.stream().map(MockLLMRequest.Message::role).toList();
    }

    @Test
    void theStoryBeforeThePartIsSentAsTheEarlierMessagesOfTheConversation() throws Exception {
        llm.enqueue(MockLLMResponse.text("{\"findings\": []}"));

        done(request("flag weak scenes", Mode.STRUCTURED));

        var messages = sent();
        assertThat(roles(messages)).containsExactly("system", "user", "assistant", "user");
        assertThat(messages.get(0).content()).contains("follows as the earlier messages of this conversation")
                .doesNotContain(TEXT).doesNotContain("Anna sailed to the harbour.").doesNotContain("\u0001");
        assertThat(messages.get(1).content()).isEqualTo("[ The story so far. ]");
        assertThat(messages.get(2).content()).isEqualTo("Anna sailed to the harbour.");
        assertThat(messages.get(3).content()).startsWith("=== PART TO REVIEW START ===").contains(TEXT).contains("flag weak scenes")
                .contains("OUTPUT FORMAT").doesNotContain("Anna sailed to the harbour.").doesNotContain("\u0001");
    }

    @Test
    void theBookHasASystemPromptAndAPromptForTheLastMessage() throws Exception {
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setAnnotateSystemPrompt("SYSTEM OF THE BOOK {{narrativePov}} / {{previousStory}}");
        loaded.setAnnotatePrompt("LAST OF THE BOOK {{text}}");
        manuscript = manuscriptService.save(loaded);
        llm.enqueue(MockLLMResponse.text("{\"findings\": []}"));

        done(request("x", Mode.STRUCTURED));

        var messages = sent();
        assertThat(roles(messages)).containsExactly("system", "user", "assistant", "user");
        assertThat(messages.get(0).content()).startsWith("SYSTEM OF THE BOOK Third-Person Limited / The story before the part follows");
        assertThat(messages.get(2).content()).isEqualTo("Anna sailed to the harbour.");
        assertThat(messages.get(3).content()).isEqualTo("LAST OF THE BOOK " + TEXT);
    }

    @Test
    void thePromptOfOneRunReplacesTheLastMessageOnly() throws Exception {
        llm.enqueue(MockLLMResponse.text("{\"findings\": []}"));

        done(new Request("x", "ONE RUN {{annotateInstructions}}", GenerationOptions.defaults(), Mode.STRUCTURED, null, null));

        var messages = sent();
        assertThat(roles(messages)).containsExactly("system", "user", "assistant", "user");
        assertThat(messages.get(0).content()).contains("OBJECTIVE");
        assertThat(messages.getLast().content()).isEqualTo("ONE RUN x");
    }

    @Test
    void theSystemPromptOfTheUserIsUsedWhenTheBookHasNone() throws Exception {
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        setting.setDefaultAnnotateSystemPrompt("SYSTEM OF THE USER");
        settingService.save(setting);
        llm.enqueue(MockLLMResponse.text("{\"findings\": []}"));

        done(request("x", Mode.STRUCTURED));

        assertThat(sent().getFirst().content()).startsWith("SYSTEM OF THE USER");
    }

    /** A book of twenty parts of 50 tokens, the active leaf is the last one. */
    private Manuscript longBook(List<ChatMessage> parts) throws Exception {
        Manuscript book = manuscriptService.save(newManuscript());
        ChatMessage last = null;
        for (int i = 1; i <= 20; i++) {
            ChatMessage message = new ChatMessage();
            message.setResponse("STORY-PART-" + i + " and some more words of the story");
            message.setTokenCount(50);
            last = last == null ? chatMessageService.createRoot(book, message) : chatMessageService.addChild(last, message);
            parts.add(last);
        }
        Manuscript loaded = manuscriptService.find(book.getId());
        loaded.setActiveLeaf(last);
        return manuscriptService.save(loaded);
    }

    private void budget(int tokens) throws Exception {
        protocol.setMaxTokens(tokens);
        protocol.setReplyTokens(100);
        protocolService.save(protocol);
    }

    private int storyPartsSent(Manuscript book, ChatMessage target, int budget) throws Exception {
        budget(budget);
        llm.enqueue(MockLLMResponse.text("{\"findings\": []}"));
        done(book, target, request("flag weak scenes", Mode.STRUCTURED));
        return (int) sent().stream().filter(m -> m.role().equals("assistant")).count();
    }

    @Test
    void theChainStaysTheSameWhenThePartIsRedrafted() throws Exception {
        List<ChatMessage> parts = new ArrayList<>();
        Manuscript book = longBook(parts);
        ChatMessage target = parts.get(14);
        // the smallest budget that takes at least eight parts of the story before the part, and the middle of its step
        int low = 1500;
        int high = 12_000;
        while (low < high) {
            int middle = (low + high) / 2;
            if (storyPartsSent(book, target, middle) >= 8) {
                high = middle;
            } else {
                low = middle + 1;
            }
        }
        int sentParts = storyPartsSent(book, target, low + 128);
        assertThat(sentParts).isBetween(8, 13);              // the budget cuts the story, it is not all there
        List<MockLLMRequest.Message> first = sent();

        // a redraft makes the part much longer, what is left of the budget for the story is smaller by that
        ChatMessage redrafted = chatMessageService.find(target.getId());
        redrafted.setResponse(redrafted.getResponse() + " and a redraft that is much longer".repeat(12));
        chatMessageService.save(redrafted);
        llm.enqueue(MockLLMResponse.text("{\"findings\": []}"));
        done(book, target, request("flag weak scenes", Mode.STRUCTURED));
        List<MockLLMRequest.Message> second = sent();

        // the same story, in the same messages: all but the last message, which holds the part
        assertThat(second).hasSameSizeAs(first);
        assertThat(second.subList(0, second.size() - 1)).isEqualTo(first.subList(0, first.size() - 1));
        assertThat(second.getLast().content()).isNotEqualTo(first.getLast().content()).contains("a redraft that is much longer");
    }

    @Test
    void theSystemMessageDoesNotDependOnThePartThatIsAnnotated() throws Exception {
        List<ChatMessage> parts = new ArrayList<>();
        Manuscript book = longBook(parts);
        budget(32_000);
        // each part keeps the lore it was written with, and that differs from part to part
        messagePromptService.store(parts.get(9), null, "LORE-OF-TEN");
        messagePromptService.store(parts.get(10), null, "LORE-OF-ELEVEN");

        llm.enqueue(MockLLMResponse.text("{\"findings\": []}"));
        done(book, parts.get(9), request("flag weak scenes", Mode.STRUCTURED));
        List<MockLLMRequest.Message> first = sent();
        llm.enqueue(MockLLMResponse.text("{\"findings\": []}"));
        done(book, parts.get(10), request("flag weak scenes", Mode.STRUCTURED));
        List<MockLLMRequest.Message> second = sent();

        // the lore is in the last message: a server that kept the first request goes on from the same system message
        assertThat(second.getFirst()).isEqualTo(first.getFirst());
        assertThat(first.getFirst().content()).doesNotContain("LORE-OF");
        assertThat(first.getLast().content()).contains("LORE-OF-TEN").doesNotContain("LORE-OF-ELEVEN");
        assertThat(second.getLast().content()).contains("LORE-OF-ELEVEN").doesNotContain("LORE-OF-TEN");
    }

    @Test
    void theNextPartAddsMessagesAfterTheOnesOfTheLastRequest() throws Exception {
        List<ChatMessage> parts = new ArrayList<>();
        Manuscript book = longBook(parts);
        budget(32_000);

        llm.enqueue(MockLLMResponse.text("{\"findings\": []}"));
        done(book, parts.get(9), request("flag weak scenes", Mode.STRUCTURED));
        List<MockLLMRequest.Message> first = sent();
        llm.enqueue(MockLLMResponse.text("{\"findings\": []}"));
        done(book, parts.get(10), request("flag weak scenes", Mode.STRUCTURED));
        List<MockLLMRequest.Message> second = sent();

        // what a server kept of the first request is all there, in the same order, up to the last message
        int kept = first.size() - 1;
        assertThat(second.size()).isEqualTo(first.size() + 2);
        assertThat(second.subList(0, kept)).isEqualTo(first.subList(0, kept));
        assertThat(second.get(kept).role()).isEqualTo("user");
        assertThat(second.get(kept).content()).isEqualTo("[ The story goes on. ]");
        assertThat(second.get(kept + 1).role()).isEqualTo("assistant");
        assertThat(second.get(kept + 1).content()).startsWith("STORY-PART-10 ");
        assertThat(second.getLast().role()).isEqualTo("user");
    }

    // ------------------------------------------------------------------------------------------------ nothing is stored

    @Test
    void nothingIsStoredUntilTheAuthorAccepts() throws Exception {
        llm.enqueue(MockLLMResponse.text(FINDINGS));

        done(request(null, Mode.STRUCTURED));

        assertThat(annotationService.listForManuscript(manuscript)).isEmpty();
    }

    @Test
    void acceptMakesAnnotationsOfTheModelAndNotesAboutTheWholePartOfWhatIsNotFound() throws Exception {
        Proposal found = new Proposal("They talked for hours.", "Weak scene", "pacing", 26, 48);
        Proposal lost = new Proposal("A dragon landed", "Contradicts the lore", null, null, null);

        List<Annotation> made = annotateService.accept(manuscript, chatMessageService.find(part.getId()), List.of(found, lost), "mock-model");

        assertThat(made).hasSize(2);
        Annotation first = annotationService.find(made.getFirst().getId());
        assertThat(first.getSource()).isEqualTo(AnnotationSource.AI);
        assertThat(first.getLabel()).isEqualTo("mock-model");
        assertThat(first.getType()).isEqualTo("pacing");
        assertThat(TEXT.substring(first.getStart(), first.getEnd())).isEqualTo("They talked for hours.");
        Annotation second = annotationService.find(made.get(1).getId());
        assertThat(second.hasRange()).isFalse();
        assertThat(second.isNotLocated()).isTrue();
        assertThat(second.getText()).isEqualTo("Contradicts the lore");
    }

    // ------------------------------------------------------------------------------------------------ the model

    @Test
    void theDefaultModelOfTheUserIsUsedWhenTheyHaveOne() throws Exception {
        assertThat(annotateService.defaultOptions().ai()).isNull();
        OpenAICompatible own = createAI();
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        setting.setDefaultAnnotateModel(own.getId());
        settingService.save(setting);

        try {
            assertThat(annotateService.defaultOptions().ai().getId()).isEqualTo(own.getId());
            assertThat(annotateService.defaultOptions().protocol()).isNull();
        } finally {
            setting.setDefaultAnnotateModel(null);
            settingService.save(setting);
        }
    }

    @Test
    void aBookWithoutAModelCanNotBeAnnotated() throws Exception {
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setAi(null);
        manuscript = manuscriptService.save(loaded);

        var token = annotateService.annotate(manuscriptService.find(manuscript.getId()), chatMessageService.find(part.getId()),
                Request.of("x"), new AnnotateService.Callback() {
                    public void onProgress(String reasoning, String text) {
                    }

                    public void onFinished(List<Proposal> proposals, String raw) {
                    }

                    public void onUnreadable(String raw, String reason) {
                    }

                    public void onTerminated(String raw) {
                    }

                    public void onError(Throwable throwable) {
                    }
                });

        assertThat(token).isNull();
    }
}
