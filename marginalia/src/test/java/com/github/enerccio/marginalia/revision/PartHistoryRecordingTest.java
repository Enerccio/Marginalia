package com.github.enerccio.marginalia.revision;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.components.PartMerge;
import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.components.RevisionRow;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessageRevision;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService.Options;
import com.github.enerccio.marginalia.domain.service.MessageRevisionService;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.domain.service.search.replace.TaskProgress;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Where the versions of a part are recorded: generation (a new part, a swipe, a regenerated part, text that arrived before an
 * error), the author's edit, a redraft with a reasoning of its own, the writers that are not the author, a merge, a branch,
 * a mass replace, and the text a writer that does not record leaves behind.
 */
class PartHistoryRecordingTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private MessageRevisionService messageRevisionService;

    @Autowired
    private ManuscriptReplaceService replaceService;

    private Manuscript manuscript;

    @BeforeEach
    void createBook() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
    }

    private ChatMessage generate(GenerationRequest request, MockLLMResponse response) throws Exception {
        llm.enqueue(response);
        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(manuscriptService.find(manuscript.getId()),
                new TurnInput(null, null, null, "go"), request, run);
        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        return chatMessageService.find(run.getMessage().getId());
    }

    private ChatMessage generate(GenerationRequest request, String text) throws Exception {
        return generate(request, MockLLMResponse.text(text));
    }

    /**
     * The versions of the part, oldest first.
     */
    private List<MessageRevision> history(ChatMessage part) throws Exception {
        List<MessageRevision> versions = new ArrayList<>();
        for (RevisionRow row : messageRevisionService.list(part, true).reversed()) {
            versions.add(messageRevisionService.find(row.id()));
        }
        return versions;
    }

    private List<RevisionSource> sources(ChatMessage part) throws Exception {
        return messageRevisionService.list(part, true).reversed().stream().map(RevisionRow::source).toList();
    }

    private Manuscript book() throws Exception {
        return manuscriptService.find(manuscript.getId());
    }

    @Test
    void aGeneratedPartStartsItsHistoryWithTheTextAndTheReasoning() throws Exception {
        ChatMessage part = generate(GenerationRequest.newMessage(), MockLLMResponse.text("FIRST").withReasoning("Thinking about it."));

        List<MessageRevision> versions = history(part);
        assertThat(versions).hasSize(1);
        assertThat(versions.getFirst().getSource()).isEqualTo(RevisionSource.GENERATE);
        assertThat(versions.getFirst().getResponse()).isEqualTo("FIRST");
        assertThat(versions.getFirst().getResponseReasoning()).isEqualTo("Thinking about it.");
        assertThat(versions.getFirst().getSourceDetail()).isEqualTo(part.getModelUsed() + " · " + part.getProtocolUsed());
        assertThat(versions.getFirst().isAgent()).isFalse();
    }

    @Test
    void aSwipeHasItsOwnHistoryAndTheSwipedPartKeepsItsOwn() throws Exception {
        ChatMessage first = generate(GenerationRequest.newMessage(), "FIRST");
        ChatMessage swiped = generate(GenerationRequest.newSwipe(first), "FIRST-B");

        assertThat(history(first)).extracting(MessageRevision::getResponse).containsExactly("FIRST");
        assertThat(history(swiped)).extracting(MessageRevision::getResponse).containsExactly("FIRST-B");
        assertThat(sources(swiped)).containsExactly(RevisionSource.GENERATE);
    }

    @Test
    void aRegeneratedPartKeepsTheTextItHadAndAddsTheNewOne() throws Exception {
        ChatMessage part = generate(GenerationRequest.newMessage(), MockLLMResponse.text("FIRST").withReasoning("First thoughts."));

        generate(GenerationRequest.regenerate(part), MockLLMResponse.text("SECOND").withReasoning("Second thoughts."));

        List<MessageRevision> versions = history(part);
        assertThat(versions).extracting(MessageRevision::getResponse).containsExactly("FIRST", "SECOND");
        assertThat(versions).extracting(MessageRevision::getSource).containsExactly(RevisionSource.GENERATE, RevisionSource.REGENERATE);
        assertThat(versions).extracting(MessageRevision::getResponseReasoning).containsExactly("First thoughts.", "Second thoughts.");
        assertThat(chatMessageService.find(part.getId()).getResponse()).isEqualTo("SECOND");
    }

    @Test
    void aFailedRegenerateAddsNoVersion() throws Exception {
        ChatMessage part = generate(GenerationRequest.newMessage(), "FIRST");
        llm.enqueue(MockLLMResponse.error(401, "wrong key"));
        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(book(), new TurnInput(null, null, null, "go"), GenerationRequest.regenerate(part), run);
        run.await(TIMEOUT);

        assertThat(chatMessageService.find(part.getId()).getResponse()).isEqualTo("FIRST");
        assertThat(history(part)).extracting(MessageRevision::getResponse).containsExactly("FIRST");
    }

    @Test
    void textThatArrivedBeforeAnErrorIsAVersion() throws Exception {
        generate(GenerationRequest.newMessage(), "FIRST");
        llm.enqueue(MockLLMResponse.chunks("partial ", "text ", "never sent").disconnectAfter(2));
        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(book(), new TurnInput(null, null, null, "go"), GenerationRequest.newMessage(), run);
        run.await(TIMEOUT);

        ChatMessage leaf = chatMessageService.find(book().getActiveLeaf());
        assertThat(leaf.getResponse()).startsWith("partial");
        assertThat(history(leaf)).extracting(MessageRevision::getResponse).containsExactly(leaf.getResponse());
        assertThat(sources(leaf)).containsExactly(RevisionSource.GENERATE);
    }

    @Test
    void theAuthorsEditIsAHumanVersion() throws Exception {
        ChatMessage part = generate(GenerationRequest.newMessage(), "FIRST");

        chatMessageService.updateResponse(book(), part, "FIRST, edited.");

        assertThat(sources(part)).containsExactly(RevisionSource.GENERATE, RevisionSource.HUMAN);
        MessageRevision edited = history(part).getLast();
        assertThat(edited.getResponse()).isEqualTo("FIRST, edited.");
        assertThat(edited.getResponseReasoning()).isNull();
        assertThat(edited.isAgent()).isFalse();
        assertThat(edited.getSourceDetail()).isNull();
    }

    @Test
    void aRedraftKeepsTheReasoningItCameWithNotTheOneOfThePart() throws Exception {
        ChatMessage part = generate(GenerationRequest.newMessage(), MockLLMResponse.text("FIRST").withReasoning("Reasoning of the generation."));

        chatMessageService.updateResponse(book(), part, "FIRST, redrafted.",
                RevisionOrigin.model(RevisionSource.REDRAFT, "model · protocol", false, "Reasoning of the redraft."));

        MessageRevision redrafted = history(part).getLast();
        assertThat(redrafted.getSource()).isEqualTo(RevisionSource.REDRAFT);
        assertThat(redrafted.getResponseReasoning()).isEqualTo("Reasoning of the redraft.");
        assertThat(redrafted.getSourceDetail()).isEqualTo("model · protocol");
        // the part keeps what it had
        assertThat(chatMessageService.find(part.getId()).getResponseReasoning()).isEqualTo("Reasoning of the generation.");
        assertThat(history(part).getFirst().getResponseReasoning()).isEqualTo("Reasoning of the generation.");
    }

    @Test
    void aWriterThatIsNotTheAuthorSaysSoAndAnAgentIsFlagged() throws Exception {
        ChatMessage part = chatMessageService.createPart(book(), null, "FIRST", null);

        chatMessageService.updateResponse(book(), part, "SECOND", RevisionOrigin.automatic(RevisionDetail.COMMAND, true));
        chatMessageService.storeResponse(book(), part, "THIRD", new RevisionOrigin(RevisionSource.PLUGIN, "reviewer", false, null));

        List<MessageRevision> versions = history(part);
        assertThat(versions).extracting(MessageRevision::getSource)
                .containsExactly(RevisionSource.HUMAN, RevisionSource.AUTOMATIC, RevisionSource.PLUGIN);
        assertThat(versions).extracting(MessageRevision::isAgent).containsExactly(false, true, false);
        assertThat(versions).extracting(MessageRevision::getSourceDetail).containsExactly(null, RevisionDetail.COMMAND.code(), "reviewer");
    }

    @Test
    void aCreatedPartStartsItsHistory() throws Exception {
        ChatMessage typed = chatMessageService.createPart(book(), null, "FIRST", null);
        ChatMessage imported = chatMessageService.createPart(book(), typed, "SECOND", null, RevisionOrigin.human(RevisionDetail.ARCHIVE_IMPORT, false));
        ChatMessage inserted = chatMessageService.insertPart(book(), typed, "BETWEEN", null, RevisionOrigin.automatic(RevisionDetail.COMMAND, true));

        assertThat(history(typed)).extracting(MessageRevision::getSource).containsExactly(RevisionSource.HUMAN);
        assertThat(history(imported).getFirst().getSourceDetail()).isEqualTo(RevisionDetail.ARCHIVE_IMPORT.code());
        assertThat(history(inserted).getFirst().isAgent()).isTrue();
        assertThat(history(inserted).getFirst().getResponse()).isEqualTo("BETWEEN");
    }

    @Test
    void aTextWrittenByAWriterThatDoesNotRecordIsKeptBeforeTheNextChange() throws Exception {
        ChatMessage part = chatMessageService.createPart(book(), null, "FIRST", null);
        // a writer that saves the part itself
        ChatMessage direct = chatMessageService.find(part.getId());
        direct.setResponse("SECOND, written directly.");
        chatMessageService.save(direct);

        chatMessageService.updateResponse(book(), part, "THIRD");

        assertThat(history(part)).extracting(MessageRevision::getResponse).containsExactly("FIRST", "SECOND, written directly.", "THIRD");
        assertThat(sources(part)).containsExactly(RevisionSource.HUMAN, RevisionSource.PLUGIN, RevisionSource.HUMAN);
    }

    @Test
    void aMergeIsAVersionOfTheSurvivingPart() throws Exception {
        ChatMessage first = chatMessageService.createPart(book(), null, "He drew his sword and", null);
        ChatMessage second = chatMessageService.createPart(book(), first, "struck.", null);

        chatMessageService.mergeIntoPrevious(book(), second, null, PartMerge.Join.SPACE, Set.of(), Set.of());

        assertThat(history(first)).extracting(MessageRevision::getResponse).containsExactly("He drew his sword and", "He drew his sword and struck.");
        assertThat(sources(first)).containsExactly(RevisionSource.HUMAN, RevisionSource.MERGE);
    }

    @Test
    void aBranchStartsAHistoryOfItsOwn() throws Exception {
        ChatMessage part = generate(GenerationRequest.newMessage(), MockLLMResponse.text("FIRST").withReasoning("Thinking."));

        ChatMessage branch = chatMessageService.branch(book(), chatMessageService.find(part.getId()));

        List<MessageRevision> versions = history(branch);
        assertThat(versions).hasSize(1);
        assertThat(versions.getFirst().getSource()).isEqualTo(RevisionSource.DUPLICATE);
        assertThat(versions.getFirst().getSourceDetail()).isEqualTo(RevisionDetail.BRANCH.code());
        assertThat(versions.getFirst().getResponse()).isEqualTo("FIRST");
        assertThat(versions.getFirst().getResponseReasoning()).isEqualTo("Thinking.");
        assertThat(history(part)).hasSize(1);
    }

    @Test
    void aMassReplaceIsAnAutomaticVersionOfTheChangedPartsOnly() throws Exception {
        ChatMessage john = chatMessageService.createPart(book(), null, "John smiled", null);
        ChatMessage other = chatMessageService.createPart(book(), john, "Nothing here", null);

        ManuscriptReplaceService.Plan plan = replaceService.plan(book(), new Options("John", "Jon", false, true, false, false, false), TaskProgress.NONE);
        replaceService.apply(book(), plan, TaskProgress.NONE);

        assertThat(history(john)).extracting(MessageRevision::getResponse).containsExactly("John smiled", "Jon smiled");
        assertThat(sources(john)).containsExactly(RevisionSource.HUMAN, RevisionSource.AUTOMATIC);
        assertThat(history(john).getLast().getSourceDetail()).isEqualTo(RevisionDetail.REPLACE.code());
        assertThat(history(other)).hasSize(1);
    }
}
