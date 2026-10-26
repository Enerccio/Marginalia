package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.collections.SummaryType;
import com.github.enerccio.marginalia.domain.components.SummaryNode;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService.Kind;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService.Options;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService.Plan;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.SummaryService.BlockTexts;
import com.github.enerccio.marginalia.domain.service.SummaryService.SummaryBlock;
import com.github.enerccio.marginalia.domain.service.search.replace.TaskProgress;
import com.github.enerccio.marginalia.domain.service.search.replace.TextSearch;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Search and replace in a book and what it does to the hashes of the summaries.
 * <pre>
 * m1 ─ m2 ─ m3[S3] ─ m4 ─ m5 ─ m6[S6]      active branch
 *                           └─ m6b[S6b]    other branch
 * </pre>
 * A summary on mN covers mN down to the message after the previous summary: S3 m1-m3, S6 m4-m6, S6b m4-m5 and m6b.
 */
class ManuscriptReplaceTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private ManuscriptReplaceService replaceService;

    private Manuscript manuscript;
    private List<ChatMessage> story;
    private ChatMessage m6b;

    @BeforeEach
    void createStory() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
        story = new ArrayList<>();
        String[] texts = {"Anna met John", "John went home", "Nothing here", "John smiled", "John left", "The end"};
        for (String text : texts) {
            story.add(add(story.isEmpty() ? null : story.getLast(), text));
        }
        m6b = add(story.get(4), "John again");

        summarize(story.get(2), "John and Anna");
        summarize(story.get(5), "John smiled and left");
        summarize(m6b, "John again");

        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setActiveLeaf(story.getLast());
        manuscript = manuscriptService.save(loaded);
    }

    private ChatMessage add(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        Thread.sleep(3);  // messages are ordered by creation time
        return parent == null ? chatMessageService.createRoot(manuscript, message) : chatMessageService.addChild(parent, message);
    }

    private void summarize(ChatMessage message, String text) throws Exception {
        Summary summary = new Summary();
        summary.setSummary(text);
        summary = summaryService.save(summary);
        message = chatMessageService.find(message.getId());
        message.setSummary(summary);
        chatMessageService.save(message);

        SummaryBlock block = summaryService.collectFirstBlock(branch(message).iterator(), BlockTexts.CURRENT);
        summary = summaryService.find(summary.getId());
        summary.setSummaryMessageHash(block.hash());
        summaryService.save(summary);
    }

    /**
     * The message and the ones above it, newest first.
     */
    private List<ChatMessage> branch(ChatMessage message) throws Exception {
        return chatMessageService.getBranchFromLeaf(chatMessageService.find(message.getId())).reversed();
    }

    private List<SummaryBlock> blocks(ChatMessage leaf) throws Exception {
        return summaryService.collectBlocks(branch(leaf));
    }

    private String response(ChatMessage message) throws Exception {
        return chatMessageService.find(message.getId()).getResponse();
    }

    private String summaryText(ChatMessage message) throws Exception {
        return summaryService.find(chatMessageService.find(message.getId()).getSummary()).getSummary();
    }

    private Manuscript book() throws Exception {
        return manuscriptService.find(manuscript.getId());
    }

    private Plan plan(String search, String replacement, boolean regex, boolean summaries, boolean allBranches) throws Exception {
        return plan(search, replacement, regex, summaries, allBranches, false);
    }

    private Plan plan(String search, String replacement, boolean regex, boolean summaries, boolean allBranches, boolean revalidate) throws Exception {
        return replaceService.plan(book(), new Options(search, replacement, regex, true, summaries, allBranches, revalidate), TaskProgress.NONE);
    }

    @Test
    void planListsTheMatchesOfTheActiveBranchInStoryOrder() throws Exception {
        Plan plan = plan("John", "Jon", false, true, false);

        assertThat(plan.items()).extracting(i -> i.kind() + ":" + i.id()).containsExactly(
                Kind.MESSAGE + ":" + story.get(0).getId(),
                Kind.MESSAGE + ":" + story.get(1).getId(),
                Kind.SUMMARY + ":" + chatMessageService.find(story.get(2).getId()).getSummary().getId(),
                Kind.MESSAGE + ":" + story.get(3).getId(),
                Kind.MESSAGE + ":" + story.get(4).getId(),
                Kind.SUMMARY + ":" + chatMessageService.find(story.get(5).getId()).getSummary().getId());
        assertThat(plan.hitCount()).isEqualTo(6);
        assertThat(plan.items().getFirst().hits().getFirst().replacement()).isEqualTo("Jon");
        // planning changes nothing
        assertThat(response(story.get(0))).isEqualTo("Anna met John");
    }

    @Test
    void replaceKeepsTheSummariesOfTheSearchedBranchValid() throws Exception {
        Plan plan = plan("John", "Jon", false, true, false);
        // the summary of the other branch is based on text that changes and the replace does not look into it
        assertThat(plan.outdatedSummaries()).isEqualTo(1);

        ManuscriptReplaceService.Result result = replaceService.apply(book(), plan, TaskProgress.NONE);

        assertThat(result.changed()).isEqualTo(6);
        assertThat(result.hits()).isEqualTo(6);
        assertThat(result.skipped()).isZero();
        assertThat(response(story.get(0))).isEqualTo("Anna met Jon");
        assertThat(response(story.get(2))).isEqualTo("Nothing here");
        assertThat(response(story.get(4))).isEqualTo("Jon left");
        assertThat(response(m6b)).isEqualTo("John again");
        assertThat(summaryText(story.get(2))).isEqualTo("Jon and Anna");
        assertThat(summaryText(story.get(5))).isEqualTo("Jon smiled and left");

        ChatMessage edited = chatMessageService.find(story.get(0).getId());
        assertThat(edited.isEdited()).isTrue();
        assertThat(edited.getWordCount()).isEqualTo(3);
        assertThat(edited.get_fulltext()).isEqualTo("Anna met Jon");

        assertThat(blocks(story.getLast())).hasSize(2).allMatch(SummaryBlock::isValid);
        List<SummaryBlock> other = blocks(m6b);
        assertThat(other).extracting(SummaryBlock::isValid).containsExactly(false, true);
    }

    @Test
    void allBranchesSearchesTheOtherBranchToo() throws Exception {
        Plan plan = plan("John", "Jon", false, true, true);
        assertThat(plan.outdatedSummaries()).isZero();
        assertThat(plan.hitCount()).isEqualTo(8);

        replaceService.apply(book(), plan, TaskProgress.NONE);

        assertThat(response(m6b)).isEqualTo("Jon again");
        assertThat(summaryText(m6b)).isEqualTo("Jon again");
        assertThat(blocks(story.getLast())).allMatch(SummaryBlock::isValid);
        assertThat(blocks(m6b)).hasSize(2).allMatch(SummaryBlock::isValid);
    }

    @Test
    void withoutSummariesTheyBecomeOutdated() throws Exception {
        Plan plan = plan("John", "Jon", false, false, false);

        assertThat(plan.items()).allMatch(item -> item.kind() == Kind.MESSAGE).hasSize(4);
        assertThat(plan.outdatedSummaries()).isEqualTo(3);

        replaceService.apply(book(), plan, TaskProgress.NONE);

        assertThat(summaryText(story.get(2))).isEqualTo("John and Anna");
        assertThat(blocks(story.getLast())).extracting(SummaryBlock::isValid).containsExactly(false, false);
    }

    @Test
    void summaryThatWasOutdatedStaysSo() throws Exception {
        ChatMessage m4 = chatMessageService.find(story.get(3).getId());
        m4.setResponse("John smiled, changed");
        chatMessageService.save(m4);

        Plan plan = plan("left", "stayed", false, true, true);
        replaceService.apply(book(), plan, TaskProgress.NONE);

        assertThat(blocks(story.getLast())).extracting(SummaryBlock::isValid).containsExactly(false, true);
    }

    @Test
    void revalidatingKeepsEverySummaryValid() throws Exception {
        // only the active branch and without the summaries: they would all become outdated
        Plan plan = plan("John", "Jon", false, false, false, true);

        assertThat(plan.outdatedSummaries()).isZero();
        replaceService.apply(book(), plan, TaskProgress.NONE);

        assertThat(summaryText(story.get(2))).isEqualTo("John and Anna");
        assertThat(blocks(story.getLast())).hasSize(2).allMatch(SummaryBlock::isValid);
        assertThat(blocks(m6b)).hasSize(2).allMatch(SummaryBlock::isValid);
    }

    @Test
    void revalidatingFixesSummariesThatTheReplaceDidNotTouch() throws Exception {
        // S3 is outdated before the replace, which only changes the part of S6
        ChatMessage m2 = chatMessageService.find(story.get(1).getId());
        m2.setResponse("John went somewhere else");
        chatMessageService.save(m2);
        assertThat(blocks(story.getLast())).extracting(SummaryBlock::isValid).containsExactly(true, false);

        Plan withoutRevalidation = plan("left", "stayed", false, true, true, false);
        assertThat(withoutRevalidation.outdatedSummaries()).isZero();
        replaceService.apply(book(), withoutRevalidation, TaskProgress.NONE);
        assertThat(blocks(story.getLast())).extracting(SummaryBlock::isValid).containsExactly(true, false);

        Plan plan = plan("stayed", "left", false, false, false, true);
        replaceService.apply(book(), plan, TaskProgress.NONE);

        assertThat(response(story.get(4))).isEqualTo("John left");
        assertThat(summaryText(story.get(2))).isEqualTo("John and Anna");
        assertThat(blocks(story.getLast())).hasSize(2).allMatch(SummaryBlock::isValid);
        assertThat(blocks(m6b)).hasSize(2).allMatch(SummaryBlock::isValid);
    }

    @Test
    void caseInsensitiveSearchFindsAllSpellings() throws Exception {
        Plan sensitive = replaceService.plan(book(), new Options("john", "Jon", false, true, true, false, false), TaskProgress.NONE);
        assertThat(sensitive.items()).isEmpty();

        Plan plan = replaceService.plan(book(), new Options("john", "Jon", false, false, true, false, false), TaskProgress.NONE);
        // four parts and two summaries of the active branch
        assertThat(plan.items()).hasSize(6);
        assertThat(plan.items().getFirst().hits().getFirst().match()).isEqualTo("John");

        replaceService.apply(book(), plan, TaskProgress.NONE);

        assertThat(response(story.get(0))).isEqualTo("Anna met Jon");
    }

    @Test
    void regexReplacementUsesGroups() throws Exception {
        Plan plan = plan("John (\\w+)", "$1 John", true, true, false);

        assertThat(plan.items().getFirst().hits().getFirst().replacement()).isEqualTo("went John");
        replaceService.apply(book(), plan, TaskProgress.NONE);

        assertThat(response(story.get(1))).isEqualTo("went John home");
        assertThat(response(story.get(3))).isEqualTo("smiled John");
    }

    @Test
    void invalidRegexIsReported() {
        assertThatThrownBy(() -> plan("(", "x", true, true, false)).isInstanceOf(TextSearch.InvalidSearchException.class);
    }

    @Test
    void nothingFoundGivesEmptyPlan() throws Exception {
        Plan plan = plan("Zebra", "Horse", false, true, true);

        assertThat(plan.items()).isEmpty();
        assertThat(plan.outdatedSummaries()).isZero();
    }

    @Test
    void textEditedAfterThePlanIsNotReplaced() throws Exception {
        Plan plan = plan("John", "Jon", false, false, false);
        ChatMessage m2 = chatMessageService.find(story.get(1).getId());
        m2.setResponse("Edited meanwhile");
        chatMessageService.save(m2);

        ManuscriptReplaceService.Result result = replaceService.apply(book(), plan, TaskProgress.NONE);

        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.changed()).isEqualTo(3);
        assertThat(response(story.get(1))).isEqualTo("Edited meanwhile");
    }

    /**
     * A meta summary on m6 that stands in for S3 and replaced S6, which it keeps inside.
     */
    private void metaSummary(String text) throws Exception {
        Summary meta = new Summary();
        meta.setSummary(text);
        meta.setSummaryType(SummaryType.META_SUMMARY);
        meta = summaryService.save(meta);
        summaryService.attachMetaSummary(meta, chatMessageService.find(story.get(5).getId()));

        SummaryBlock block = summaryService.collectFirstBlock(branch(story.get(5)).iterator(), BlockTexts.CURRENT);
        meta = summaryService.find(block.summary().getId());
        meta.setSummaryMessageHash(block.hash());
        summaryService.save(meta);
    }

    private Plan summariesPlan(String search, String replacement, boolean allBranches, boolean revalidate) throws Exception {
        return replaceService.plan(book(), ManuscriptReplaceService.Options.summariesOnly(search, replacement, false, true,
                allBranches, revalidate), TaskProgress.NONE);
    }

    /**
     * The texts of the summaries of the overview: the ones in use and, under a meta summary, those it stands in for.
     */
    private List<String> overviewTexts() throws Exception {
        List<String> texts = new ArrayList<>();
        List<SummaryNode> level = summaryService.collectTree(book());
        while (!level.isEmpty()) {
            level.forEach(node -> texts.add(node.getSummary().getSummary()));
            level = level.stream().flatMap(node -> node.getChildren().stream()).toList();
        }
        return texts;
    }

    @Test
    void summariesOnlyLeavesTheStoryAlone() throws Exception {
        Plan plan = summariesPlan("John", "Jon", false, false);

        assertThat(plan.items()).hasSize(2).allMatch(item -> item.kind() == Kind.SUMMARY);
        // the story is not searched, so the summaries that are not searched stay valid by themselves
        ManuscriptReplaceService.Result result = replaceService.apply(book(), plan, TaskProgress.NONE);

        assertThat(result.changed()).isEqualTo(2);
        assertThat(response(story.get(0))).isEqualTo("Anna met John");
        assertThat(response(story.get(3))).isEqualTo("John smiled");
        assertThat(summaryText(story.get(2))).isEqualTo("Jon and Anna");
        assertThat(summaryText(story.get(5))).isEqualTo("Jon smiled and left");
        assertThat(chatMessageService.find(story.get(0).getId()).isEdited()).isFalse();
        assertThat(blocks(story.getLast())).hasSize(2).allMatch(SummaryBlock::isValid);
    }

    @Test
    void summariesOnlyFindsNothingThatIsOnlyInTheStory() throws Exception {
        assertThat(summariesPlan("Anna met", "Anna saw", false, false).items()).isEmpty();
    }

    @Test
    void summariesOnlyKeepsTheMetaSummaryValid() throws Exception {
        metaSummary("John does it all");
        assertThat(blocks(story.getLast())).hasSize(1).allMatch(SummaryBlock::isValid);

        // S3 is part of the hash of the meta summary
        Plan plan = summariesPlan("John", "Jon", false, false);
        assertThat(plan.outdatedSummaries()).isZero();
        assertThat(plan.hitCount()).isEqualTo(3);
        replaceService.apply(book(), plan, TaskProgress.NONE);

        assertThat(overviewTexts()).containsExactlyInAnyOrder("Jon does it all", "Jon and Anna", "Jon smiled and left");
        assertThat(blocks(story.getLast())).hasSize(1).allMatch(SummaryBlock::isValid);
    }

    @Test
    void summariesOnlyChangesTheSummaryAMetaSummaryReplaced() throws Exception {
        metaSummary("The whole story");

        // only the copy inside the meta summary has this text
        Plan plan = summariesPlan("smiled and left", "stayed", false, false);
        assertThat(plan.items()).hasSize(1);
        replaceService.apply(book(), plan, TaskProgress.NONE);

        assertThat(overviewTexts()).containsExactlyInAnyOrder("The whole story", "John and Anna", "John stayed");
        assertThat(blocks(story.getLast())).allMatch(SummaryBlock::isValid);

        // unwinding brings it back with the new text
        summaryService.removeSummary(chatMessageService.find(story.get(5).getId()), true);
        assertThat(summaryText(story.get(5))).isEqualTo("John stayed");
    }

    @Test
    void summariesOnlyWarnsAboutAMetaSummaryOutsideTheSearch() throws Exception {
        // a meta summary on the other branch stands in for S3, which the search of the active branch changes
        Summary meta = new Summary();
        meta.setSummary("Other branch");
        meta.setSummaryType(SummaryType.META_SUMMARY);
        meta = summaryService.save(meta);
        summaryService.attachMetaSummary(meta, chatMessageService.find(m6b.getId()));
        SummaryBlock block = summaryService.collectFirstBlock(branch(m6b).iterator(), BlockTexts.CURRENT);
        meta = summaryService.find(block.summary().getId());
        meta.setSummaryMessageHash(block.hash());
        summaryService.save(meta);
        assertThat(blocks(m6b)).allMatch(SummaryBlock::isValid);

        Plan plan = summariesPlan("Anna", "Mary", false, false);
        assertThat(plan.outdatedSummaries()).isEqualTo(1);
        replaceService.apply(book(), plan, TaskProgress.NONE);
        assertThat(blocks(m6b)).extracting(SummaryBlock::isValid).containsExactly(false);

        // the plan with all the branches reaches it
        Plan all = summariesPlan("Mary", "Anna", true, false);
        assertThat(all.outdatedSummaries()).isZero();
        replaceService.apply(book(), all, TaskProgress.NONE);
        assertThat(blocks(m6b)).allMatch(SummaryBlock::isValid);
    }

    @Test
    void summariesOnlyRevalidatesWhatItDidNotTouch() throws Exception {
        // S3 is outdated before the replace, which changes S6 only
        ChatMessage m2 = chatMessageService.find(story.get(1).getId());
        m2.setResponse("John went somewhere else");
        chatMessageService.save(m2);
        assertThat(blocks(story.getLast())).extracting(SummaryBlock::isValid).containsExactly(true, false);

        replaceService.apply(book(), summariesPlan("smiled and left", "stayed", false, true), TaskProgress.NONE);

        assertThat(summaryText(story.get(5))).isEqualTo("John stayed");
        assertThat(blocks(story.getLast())).hasSize(2).allMatch(SummaryBlock::isValid);
        assertThat(blocks(m6b)).hasSize(2).allMatch(SummaryBlock::isValid);
    }

    @Test
    void summaryEditedAfterThePlanIsNotReplaced() throws Exception {
        metaSummary("John does it all");
        Plan plan = summariesPlan("John", "Jon", false, false);
        Summary meta = summaryService.find(chatMessageService.find(story.get(5).getId()).getSummary());
        meta.setSummary("Edited meanwhile");
        summaryService.save(meta);

        ManuscriptReplaceService.Result result = replaceService.apply(book(), plan, TaskProgress.NONE);

        assertThat(result.skipped()).isEqualTo(1);
        assertThat(overviewTexts()).contains("Edited meanwhile", "Jon and Anna");
    }

    @Test
    void searchCanBeStopped() {
        TaskProgress stopped = new TaskProgress() {
            @Override
            public void update(long done, long total) {
            }

            @Override
            public boolean isCancelled() {
                return true;
            }
        };

        assertThatThrownBy(() -> replaceService.plan(book(), new Options("John", "Jon", false, true, true, true, true), stopped))
                .isInstanceOf(CancellationException.class);
    }

    @Test
    void otherUserCannotSearchTheBook() throws Exception {
        loginAs(createUser());

        assertThatThrownBy(() -> plan("John", "Jon", false, true, true)).isInstanceOf(IllegalArgumentException.class);
    }

}
