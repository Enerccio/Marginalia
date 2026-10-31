package com.github.enerccio.marginalia.statistics;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.collections.StatisticSide;
import com.github.enerccio.marginalia.domain.components.*;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService.Options;
import com.github.enerccio.marginalia.domain.service.ManuscriptStatisticService;
import com.github.enerccio.marginalia.domain.service.MessageRevisionService;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.domain.service.search.replace.TaskProgress;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The words, parts and edits of the writing statistics are counted where a version of a part is made
 * ({@code MessageRevisionService}): the first version of a part is a part created, a later one an edit whose change of the
 * words is written or generated when positive and removed when negative.
 */
class WordCountingTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private MessageRevisionService messageRevisionService;

    @Autowired
    private ManuscriptReplaceService replaceService;

    @Autowired
    private ManuscriptStatisticService statisticService;

    private Manuscript manuscript;

    @BeforeEach
    void createBook() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
    }

    private Manuscript book() throws Exception {
        return manuscriptService.find(manuscript.getId());
    }

    private StatisticCounts counts() throws Exception {
        return statisticService.total(StatisticFilter.of(statisticService.today(), statisticService.today(), StatisticSide.ALL)
                .forBook(manuscript.getUuid()));
    }

    private ChatMessage generate(GenerationRequest request, String text) throws Exception {
        llm.enqueue(MockLLMResponse.text(text));
        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(book(), new TurnInput(null, null, null, "go"), request, run);
        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        return chatMessageService.find(run.getMessage().getId());
    }

    private ChatMessage typed(ChatMessage parent, String text) throws Exception {
        return chatMessageService.createPart(book(), parent, text, null);
    }

    @Test
    void aTypedPartIsAPartCreatedWithItsWordsWritten() throws Exception {
        typed(null, "He drew his sword");

        assertThat(counts()).isEqualTo(new StatisticCounts(0, 0, 0, 4, 0, 0, 1, 0, 0));
    }

    @Test
    void anEditAddsTheNewWordsAndTakesAwayTheRemovedOnes() throws Exception {
        ChatMessage part = typed(null, "He drew his sword");

        chatMessageService.updateResponse(book(), part, "He drew his sword and struck");
        assertThat(counts()).isEqualTo(new StatisticCounts(0, 0, 0, 6, 0, 0, 1, 0, 1));

        chatMessageService.updateResponse(book(), part, "He struck");
        assertThat(counts()).isEqualTo(new StatisticCounts(0, 0, 0, 6, 0, 4, 1, 0, 2));
    }

    @Test
    void anEditThatKeepsTheNumberOfWordsIsAnEditOnly() throws Exception {
        ChatMessage part = typed(null, "He drew his sword");

        chatMessageService.updateResponse(book(), part, "She drew her sword");

        assertThat(counts()).isEqualTo(new StatisticCounts(0, 0, 0, 4, 0, 0, 1, 0, 1));
    }

    @Test
    void aGeneratedPartIsAPartCreatedWithItsWordsGenerated() throws Exception {
        generate(GenerationRequest.newMessage(), "The rain fell softly");

        StatisticCounts counts = counts();
        assertThat(counts.partsCreated()).isEqualTo(1);
        assertThat(counts.wordsGenerated()).isEqualTo(4);
        assertThat(counts.wordsWritten()).isZero();
        assertThat(counts.edits()).isZero();
    }

    @Test
    void aSwipeIsAnotherPartAndARegenerateAnEdit() throws Exception {
        ChatMessage first = generate(GenerationRequest.newMessage(), "The rain fell softly");

        generate(GenerationRequest.newSwipe(first), "A swipe of five words");
        StatisticCounts afterSwipe = counts();
        assertThat(afterSwipe.partsCreated()).isEqualTo(2);
        assertThat(afterSwipe.wordsGenerated()).isEqualTo(4 + 5);
        assertThat(afterSwipe.edits()).isZero();

        generate(GenerationRequest.regenerate(first), "Rain");
        StatisticCounts afterRegenerate = counts();
        assertThat(afterRegenerate.partsCreated()).isEqualTo(2);
        assertThat(afterRegenerate.edits()).isEqualTo(1);
        assertThat(afterRegenerate.wordsRemoved()).isEqualTo(3);
        assertThat(afterRegenerate.wordsGenerated()).isEqualTo(9);
    }

    @Test
    void aRedraftIsAnEditWithTheWordsGenerated() throws Exception {
        ChatMessage part = typed(null, "He drew his sword");

        chatMessageService.updateResponse(book(), part, "He drew his sword with a flourish",
                RevisionOrigin.model(RevisionSource.REDRAFT, "model · protocol", false, null));

        StatisticCounts counts = counts();
        assertThat(counts.edits()).isEqualTo(1);
        assertThat(counts.wordsGenerated()).isEqualTo(3);
        assertThat(counts.wordsWritten()).isEqualTo(4);
    }

    @Test
    void aGenerationThatGotNoTextIsNotAPart() throws Exception {
        llm.enqueue(MockLLMResponse.error(401, "wrong key"));
        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(book(), new TurnInput(null, null, null, "go"), GenerationRequest.newMessage(), run);
        run.await(TIMEOUT);

        assertThat(counts().partsCreated()).isZero();
        assertThat(counts().wordsGenerated()).isZero();
    }

    @Test
    void aBlankPartAndTheSameTextAgainCountNothing() throws Exception {
        ChatMessage blank = typed(null, "");
        assertThat(counts()).isEqualTo(StatisticCounts.ZERO);

        ChatMessage part = typed(blank, "Same words here");
        StatisticCounts created = counts();
        chatMessageService.updateResponse(book(), part, "Same words here");

        assertThat(counts()).isEqualTo(created);
    }

    @Test
    void aTextThatWasNotRecordedIsARepairNotAnEdit() throws Exception {
        ChatMessage part = typed(null, "FIRST");
        ChatMessage direct = chatMessageService.find(part.getId());
        direct.setResponse("SECOND written directly now");
        chatMessageService.save(direct);

        chatMessageService.updateResponse(book(), part, "THIRD");

        // the text of the writer that does not record is not counted, only the change that was made to it
        assertThat(counts()).isEqualTo(new StatisticCounts(0, 0, 0, 1, 0, 3, 1, 0, 1));
    }

    @Test
    void aCopyAndASavedVersionAreNotWriting() throws Exception {
        ChatMessage part = generate(GenerationRequest.newMessage(), "The rain fell softly");
        StatisticCounts before = counts();

        chatMessageService.branch(book(), chatMessageService.find(part.getId()));
        messageRevisionService.snapshot(book(), chatMessageService.find(part.getId()), "before the duel");

        assertThat(counts()).isEqualTo(before);
    }

    @Test
    void aRestoreIsAnEditAndARollBackToo() throws Exception {
        ChatMessage part = typed(null, "One two three");
        chatMessageService.updateResponse(book(), part, "One two three four five six");
        RevisionRow first = messageRevisionService.list(part, true).getLast();
        StatisticCounts before = counts();
        assertThat(before.edits()).isEqualTo(1);

        messageRevisionService.restoreOnTop(book(), chatMessageService.find(part.getId()), messageRevisionService.find(first.id()));
        StatisticCounts restored = counts();
        assertThat(restored.edits()).isEqualTo(2);
        assertThat(restored.wordsRemoved()).isEqualTo(3);

        chatMessageService.updateResponse(book(), part, "One two three four");
        messageRevisionService.rollBackTo(book(), chatMessageService.find(part.getId()), messageRevisionService.find(first.id()));
        StatisticCounts rolledBack = counts();
        assertThat(rolledBack.edits()).isEqualTo(4);
        // 3 words went with the restore, 1 with the roll back
        assertThat(rolledBack.wordsRemoved()).isEqualTo(3 + 1);
        assertThat(rolledBack.wordsWritten()).isEqualTo(3 + 3 + 1);
    }

    @Test
    void aMergeIsAnEditThatAddsNoWords() throws Exception {
        ChatMessage first = typed(null, "He drew his sword and");
        ChatMessage second = typed(first, "struck.");
        StatisticCounts before = counts();

        chatMessageService.mergeIntoPrevious(book(), second, null, PartMerge.Join.SPACE, Set.of(), Set.of());

        StatisticCounts after = counts();
        assertThat(after.edits()).isEqualTo(before.edits() + 1);
        assertThat(after.wordsWritten()).isEqualTo(before.wordsWritten());
        assertThat(after.wordsRemoved()).isEqualTo(before.wordsRemoved());
    }

    @Test
    void aMassReplaceIsAnEditOfTheChangedPartsOnly() throws Exception {
        ChatMessage john = typed(null, "John smiled");
        typed(john, "Nothing here");
        StatisticCounts before = counts();

        ManuscriptReplaceService.Plan plan = replaceService.plan(book(), new Options("John", "Jonathan Smith", false, true, false, false, false), TaskProgress.NONE);
        replaceService.apply(book(), plan, TaskProgress.NONE);

        StatisticCounts after = counts();
        assertThat(after.edits()).isEqualTo(before.edits() + 1);
        assertThat(after.wordsWritten()).isEqualTo(before.wordsWritten() + 1);
    }

    @Test
    void workThatWasCountedBeforeIsNotCountedAgain() throws Exception {
        ChatMessage part = typed(null, "He drew his sword");
        StatisticCounts before = counts();

        chatMessageService.updateResponse(book(), part, "He drew his sword and struck the beast down",
                RevisionOrigin.automatic(RevisionDetail.AGENT_MERGE, true).uncounted());

        assertThat(counts()).isEqualTo(before);
    }

    @Test
    void whatEverySourceOfAVersionAdds() {
        // the first version
        assertThat(StatisticCounts.ofVersion(RevisionSource.HUMAN, null, 5)).isEqualTo(StatisticCounts.partsCreated(1).plus(StatisticCounts.wordsWritten(5)));
        for (RevisionSource source : RevisionSource.values()) {
            if (source.isModel()) {
                assertThat(StatisticCounts.ofVersion(source, null, 5)).isEqualTo(StatisticCounts.partsCreated(1).plus(StatisticCounts.wordsGenerated(5)));
                assertThat(StatisticCounts.ofVersion(source, 2, 5)).isEqualTo(StatisticCounts.edits(1).plus(StatisticCounts.wordsGenerated(3)));
                assertThat(StatisticCounts.ofVersion(source, 7, 5)).isEqualTo(StatisticCounts.edits(1).plus(StatisticCounts.wordsRemoved(2)));
            }
        }
        assertThat(StatisticCounts.ofVersion(RevisionSource.AUTOMATIC, 7, 5)).isEqualTo(StatisticCounts.edits(1).plus(StatisticCounts.wordsRemoved(2)));
        assertThat(StatisticCounts.ofVersion(RevisionSource.PLUGIN, 2, 5)).isEqualTo(StatisticCounts.edits(1).plus(StatisticCounts.wordsWritten(3)));
        assertThat(StatisticCounts.ofVersion(RevisionSource.RESTORE, 2, 2)).isEqualTo(StatisticCounts.edits(1));
        assertThat(StatisticCounts.ofVersion(RevisionSource.DUPLICATE, null, 5)).isEqualTo(StatisticCounts.ZERO);
        assertThat(StatisticCounts.ofVersion(RevisionSource.SNAPSHOT, 5, 5)).isEqualTo(StatisticCounts.ZERO);
        assertThat(StatisticCounts.ofVersion(RevisionSource.MERGE, 5, 9)).isEqualTo(StatisticCounts.edits(1));
        assertThat(StatisticCounts.ofVersion(RevisionSource.SPLIT, 9, 5)).isEqualTo(StatisticCounts.edits(1));
    }
}
