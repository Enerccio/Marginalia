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
        CompletableFuture<Outcome> done = new CompletableFuture<>();
        var token = annotateService.annotate(manuscriptService.find(manuscript.getId()), chatMessageService.find(part.getId()), request,
                new AnnotateService.Callback() {
                    @Override
                    public void onProgress(String reasoning, String text) {
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
        Outcome outcome = run(request);
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
        assertThat(llm.lastCompletionRequest().isStream()).isFalse();
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
        assertThat(llm.lastCompletionRequest().isStream()).isFalse();
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
