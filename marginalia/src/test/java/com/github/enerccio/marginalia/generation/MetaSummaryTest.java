package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.collections.SummaryType;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.BackupService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.SummaryNode;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.SummaryService.SummaryBlock;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationProperties;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Meta summaries: merging summaries into one, unwinding, and the summary chain generation and validation walk.
 * <p>
 * The story is m1 (root) ... m8 (leaf), a summary on mN covers mN down to the message after the previous summary.
 */
class MetaSummaryTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private BackupService backupService;

    private Manuscript manuscript;
    private List<ChatMessage> story;

    @BeforeEach
    void createStory() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
        story = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            ChatMessage message = new ChatMessage();
            message.setResponse("part " + i);
            message.setTokenCount(5);
            ChatMessage saved = i == 1 ? chatMessageService.createRoot(manuscript, message)
                    : chatMessageService.addChild(story.getLast(), message);
            story.add(saved);
        }
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setActiveLeaf(story.getLast());
        manuscript = manuscriptService.save(loaded);
    }

    private ChatMessage m(int n) throws Exception {
        return chatMessageService.find(story.get(n - 1).getId());
    }

    private Summary summaryOf(int n) throws Exception {
        ChatMessage message = m(n);
        return message.getSummary() == null ? null : summaryService.find(message.getSummary());
    }

    private AsyncResult run(StartSummary start) throws Exception {
        CompletableFuture<Summary> done = new CompletableFuture<>();
        var token = start.start(new SummaryService.AsyncCallback() {
            @Override
            public void onSummaryProgress(String reasoning, String summary) {
            }

            @Override
            public void onSummaryFinished(Summary summary) {
                done.complete(summary);
            }

            @Override
            public void onSummaryTerminated() {
                done.completeExceptionally(new IllegalStateException("terminated"));
            }

            @Override
            public void onError(Throwable throwable) {
                done.completeExceptionally(throwable);
            }
        });
        return new AsyncResult(token != null, token == null ? null : done.get(30, TimeUnit.SECONDS));
    }

    private record AsyncResult(boolean started, Summary summary) {
    }

    private interface StartSummary {
        Object start(SummaryService.AsyncCallback callback) throws Exception;
    }

    private Summary summarize(int n, String text) throws Exception {
        llm.enqueue(MockLLMResponse.text(text));
        AsyncResult result = run(callback -> summaryService.createSummary(manuscriptService.find(manuscript.getId()), m(n), callback));
        assertThat(result.started()).isTrue();
        return result.summary();
    }

    private AsyncResult meta(int from, int to, String text) throws Exception {
        llm.enqueue(MockLLMResponse.text(text));
        return run(callback -> summaryService.createMetaSummary(manuscriptService.find(manuscript.getId()), m(from), m(to), callback));
    }

    private List<SummaryBlock> blocks() throws Exception {
        return summaryService.collectBlocks(chatMessageService.getBranchFromLeaf(m(8)).reversed());
    }

    private List<String> blockTexts() throws Exception {
        return blocks().stream().map(b -> b.summary().getSummary()).toList();
    }

    @Test
    void summaryTypeDefaultsToSummary() throws Exception {
        Summary plain = summarize(2, "S2");

        assertThat(plain.getSummaryType()).isEqualTo(SummaryType.SUMMARY);
        assertThat(summaryOf(2).getSummaryType()).isEqualTo(SummaryType.SUMMARY);
        assertThat(summaryOf(2).getNextSummaryUuid()).isNull();
        assertThat(new Summary().getSummaryType()).isEqualTo(SummaryType.SUMMARY);
    }

    @Test
    void metaSummaryReplacesSummaryOfFromAndKeepsOldOne() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        summarize(6, "S6");
        String oldUuid = summaryOf(6).getUuid();

        // merges S6 and S4, S2 stays the next summary
        AsyncResult result = meta(6, 4, "META 6-4");

        assertThat(result.started()).isTrue();
        Summary meta = summaryOf(6);
        assertThat(meta.getSummary()).isEqualTo("META 6-4");
        assertThat(meta.getSummaryType()).isEqualTo(SummaryType.META_SUMMARY);
        assertThat(meta.getNextSummaryUuid()).isEqualTo(summaryOf(2).getUuid());
        assertThat(meta.getReplacedSummary()).contains("S6");
        // pointers to the replaced summary still find the meta summary
        assertThat(meta.getUuid()).isEqualTo(oldUuid);
        assertThat(summaryOf(4).getSummary()).isEqualTo("S4");

        // the LLM got the merged summaries, oldest first
        String prompt = llm.lastCompletionRequest().getPromptText();
        assertThat(prompt).contains("S4").contains("S6").doesNotContain("S2");
        assertThat(prompt.indexOf("S4")).isLessThan(prompt.indexOf("S6"));
    }

    @Test
    void metaSummaryStandsInForMergedSummaries() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        summarize(6, "S6");
        assertThat(blockTexts()).containsExactly("S6", "S4", "S2");

        meta(6, 4, "META");

        assertThat(blockTexts()).containsExactly("META", "S2");
        assertThat(blocks()).allMatch(SummaryBlock::isValid);
    }

    @Test
    void metaSummaryUpToTheRoot() throws Exception {
        summarize(2, "S2");
        summarize(5, "S5");

        meta(5, 2, "META");

        assertThat(summaryOf(5).getNextSummaryUuid()).isNull();
        assertThat(blockTexts()).containsExactly("META");
        assertThat(blocks()).allMatch(SummaryBlock::isValid);
    }

    @Test
    void changedStoryInvalidatesMetaSummary() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        summarize(6, "S6");
        meta(6, 4, "META");
        assertThat(blocks()).allMatch(SummaryBlock::isValid);

        ChatMessage edited = m(3);
        edited.setResponse("part 3 rewritten");
        chatMessageService.save(edited);

        assertThat(blocks()).extracting(b -> b.summary().getSummary() + ":" + b.isValid())
                .containsExactly("META:false", "S2:true");
    }

    @Test
    void deletedMergedSummaryInvalidatesMetaSummary() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        summarize(6, "S6");
        meta(6, 4, "META");

        summaryService.removeSummary(m(4), false);

        assertThat(blocks().getFirst().isValid()).isFalse();
    }

    @Test
    void unwindRestoresReplacedSummary() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        Summary original = summaryOf(4);
        meta(4, 2, "META");
        assertThat(summaryOf(4).getSummaryType()).isEqualTo(SummaryType.META_SUMMARY);

        summaryService.removeSummary(m(4), true);

        Summary restored = summaryOf(4);
        assertThat(restored.getSummary()).isEqualTo("S4");
        assertThat(restored.getSummaryType()).isEqualTo(SummaryType.SUMMARY);
        assertThat(restored.getUuid()).isEqualTo(original.getUuid());
        assertThat(restored.getSummaryMessageHash()).isEqualTo(original.getSummaryMessageHash());
        assertThat(blockTexts()).containsExactly("S4", "S2");
        assertThat(blocks()).allMatch(SummaryBlock::isValid);
    }

    @Test
    void deleteCompletelyDoesNotRestore() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        meta(4, 2, "META");

        summaryService.removeSummary(m(4), false);

        assertThat(summaryOf(4)).isNull();
        // what the meta summary merged is used again
        assertThat(blockTexts()).containsExactly("S2");
    }

    @Test
    void removeSummaryOfPlainSummaryIgnoresUnwind() throws Exception {
        summarize(2, "S2");

        summaryService.removeSummary(m(2), true);

        assertThat(summaryOf(2)).isNull();
    }

    @Test
    void metaSummariesNestAndUnwindOneByOne() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        summarize(6, "S6");
        summarize(8, "S8");
        meta(4, 2, "META A");
        // S8, S6 and the meta summary on 4 (which stands in for S4 and S2)
        meta(8, 4, "META B");
        assertThat(blockTexts()).containsExactly("META B");
        assertThat(summaryOf(8).getSummaryType()).isEqualTo(SummaryType.META_SUMMARY);

        summaryService.removeSummary(m(8), true);
        assertThat(blockTexts()).containsExactly("S8", "S6", "META A");

        summaryService.removeSummary(m(4), true);
        assertThat(blockTexts()).containsExactly("S8", "S6", "S4", "S2");
        assertThat(blocks()).allMatch(SummaryBlock::isValid);
    }

    @Test
    void metaSummaryOfMetaSummary() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        summarize(6, "S6");
        summarize(8, "S8");
        meta(4, 2, "META A");
        meta(8, 6, "META B");
        assertThat(blockTexts()).containsExactly("META B", "META A");

        meta(8, 4, "META C");

        assertThat(blockTexts()).containsExactly("META C");
        assertThat(blocks()).allMatch(SummaryBlock::isValid);

        summaryService.removeSummary(m(8), true);
        assertThat(blockTexts()).containsExactly("META B", "META A");
        assertThat(blocks()).allMatch(SummaryBlock::isValid);
    }

    @Test
    void metaSummaryPointerSurvivesReplacingTheNextSummary() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        summarize(6, "S6");
        summarize(8, "S8");
        // META B stops at the summary on 4
        meta(8, 6, "META B");
        assertThat(summaryOf(8).getNextSummaryUuid()).isEqualTo(summaryOf(4).getUuid());

        // the summary B points to is merged into another one
        meta(4, 2, "META A");

        assertThat(blockTexts()).containsExactly("META B", "META A");
        assertThat(blocks()).allMatch(SummaryBlock::isValid);
    }

    @Test
    void invalidRangesAreRejected() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");

        // same message, to newer than from, and a message without summary
        assertThat(meta(4, 4, "x").started()).isFalse();
        assertThat(meta(2, 4, "x").started()).isFalse();
        assertThat(meta(4, 3, "x").started()).isFalse();
        // nothing to merge
        assertThat(summaryOf(4).getSummary()).isEqualTo("S4");
    }

    @Test
    void generationUsesOnlyTheMetaSummary() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        summarize(8, "S8");
        meta(8, 4, "META");
        AtomicReference<List<String>> summaries = new AtomicReference<>();
        onEvent(Events.BEFORE_SUMMARIES, manuscript, e -> {
            @SuppressWarnings("unchecked")
            List<String> used = (List<String>) e.getProperty(GenerationProperties.SUMMARIES);
            summaries.set(List.copyOf(used));
        });

        GenerationRun run = generate(manuscript, "continue");

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(summaries.get()).containsExactly("S2", "META").doesNotContain("S4", "S8");
    }

    @Test
    void copyKeepsMetaSummary() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        meta(4, 2, "META");

        Summary copy = summaryService.copySummary(summaryOf(4));

        assertThat(copy.getSummaryType()).isEqualTo(SummaryType.META_SUMMARY);
        assertThat(copy.getReplacedSummary()).isEqualTo(summaryOf(4).getReplacedSummary());
        assertThat(copy.getNextSummaryUuid()).isEqualTo(summaryOf(4).getNextSummaryUuid());
        assertThat(copy.getUuid()).isNotEqualTo(summaryOf(4).getUuid());
    }

    @Test
    void restoredBackupKeepsMetaSummaryPointers() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        summarize(8, "S8");
        // META B stops at S2 and replaced S8, which META A (made later) does not touch
        meta(8, 4, "META B");
        assertThat(summaryOf(8).getNextSummaryUuid()).isEqualTo(summaryOf(2).getUuid());

        BackupService.ManuscriptBackup backup = backupService.takeBackup(manuscriptService.find(manuscript.getId()));
        Manuscript clone = backupService.cloneBackup(backup, uniqueName("clone"), Map.of());

        List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(manuscriptService.find(clone.getId()).getActiveLeaf());
        List<SummaryBlock> blocks = summaryService.collectBlocks(branch.reversed());
        assertThat(blocks).extracting(b -> b.summary().getSummary()).containsExactly("META B", "S2");
        assertThat(blocks).allMatch(SummaryBlock::isValid);
        // restored summaries have new uuids
        assertThat(blocks.getFirst().summary().getNextSummaryUuid()).isEqualTo(blocks.get(1).summary().getUuid());
        assertThat(blocks.get(1).summary().getUuid()).isNotEqualTo(summaryOf(2).getUuid());

        // unwinding in the clone works with the restored uuids
        summaryService.removeSummary(blocks.getFirst().head(), true);
        List<ChatMessage> after = chatMessageService.getBranchFromLeaf(manuscriptService.find(clone.getId()).getActiveLeaf());
        assertThat(summaryService.collectBlocks(after.reversed())).extracting(b -> b.summary().getSummary())
                .containsExactly("S8", "S4", "S2");
    }

    private List<SummaryNode> tree() throws Exception {
        return summaryService.collectTree(manuscriptService.find(manuscript.getId()));
    }

    private static List<String> texts(List<SummaryNode> nodes) {
        return nodes.stream().map(n -> n.getSummary().getSummary()).toList();
    }

    @Test
    void treeOfPlainSummaries() throws Exception {
        summarize(2, "S2");
        summarize(5, "S5");

        List<SummaryNode> tree = tree();

        assertThat(texts(tree)).containsExactly("S2", "S5");
        assertThat(tree).extracting(SummaryNode::getOrder).containsExactly(2, 5);
        assertThat(tree).extracting(n -> n.getMessage().getId()).containsExactly(story.get(1).getId(), story.get(4).getId());
        assertThat(tree).allMatch(n -> n.getChildren().isEmpty() && !n.isReplaced());
    }

    @Test
    void treeWithoutStory() throws Exception {
        Manuscript empty = manuscriptService.save(newManuscript());

        assertThat(summaryService.collectTree(empty)).isEmpty();
    }

    @Test
    void metaSummaryHasMergedSummariesAsChildren() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        summarize(6, "S6");
        meta(6, 4, "META");

        List<SummaryNode> tree = tree();

        assertThat(texts(tree)).containsExactly("S2", "META");
        SummaryNode meta = tree.getLast();
        assertThat(texts(meta.getChildren())).containsExactly("S4", "S6");
        SummaryNode replaced = meta.getChildren().getLast();
        assertThat(replaced.isReplaced()).isTrue();
        assertThat(replaced.getSummary().getId()).isNull();
        assertThat(replaced.getOrder()).isEqualTo(6);
        assertThat(replaced.getMessage().getId()).isEqualTo(story.get(5).getId());
        assertThat(meta.getChildren().getFirst().isReplaced()).isFalse();
        assertThat(meta.getChildren().getFirst().getSummary().getId()).isNotNull();
        assertThat(tree.getFirst().getChildren()).isEmpty();
    }

    @Test
    void nestedMetaSummariesNestInTheTree() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        summarize(6, "S6");
        summarize(8, "S8");
        meta(4, 2, "META A");
        meta(8, 6, "META B");
        meta(8, 4, "META C");

        List<SummaryNode> tree = tree();

        assertThat(texts(tree)).containsExactly("META C");
        List<SummaryNode> merged = tree.getFirst().getChildren();
        assertThat(texts(merged)).containsExactly("META A", "META B");
        assertThat(merged).extracting(SummaryNode::isReplaced).containsExactly(false, true);
        assertThat(texts(merged.get(0).getChildren())).containsExactly("S2", "S4");
        assertThat(texts(merged.get(1).getChildren())).containsExactly("S6", "S8");
        // every summary is shown once
        assertThat(merged.get(0).getChildren()).extracting(SummaryNode::isReplaced).containsExactly(false, true);
        assertThat(merged.get(1).getChildren()).extracting(SummaryNode::isReplaced).containsExactly(false, true);
    }

    @Test
    void editingSummaryTextKeepsItValid() throws Exception {
        summarize(2, "S2");
        summarize(5, "S5");

        Summary edited = summaryService.updateSummaryText(manuscriptService.find(manuscript.getId()), summaryOf(5), "S5 edited by the user");

        assertThat(summaryOf(5).getSummary()).isEqualTo("S5 edited by the user");
        assertThat(edited.getSummaryTokens()).isPositive();
        assertThat(summaryOf(5).getSummaryTokens()).isEqualTo(edited.getSummaryTokens());
        assertThat(blocks()).allMatch(SummaryBlock::isValid);
        assertThat(blockTexts()).containsExactly("S5 edited by the user", "S2");
    }
}
