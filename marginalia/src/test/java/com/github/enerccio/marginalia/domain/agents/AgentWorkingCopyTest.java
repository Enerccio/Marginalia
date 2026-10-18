package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static com.github.enerccio.marginalia.domain.collections.AgentRunStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The working copy of a run: every run gets a copy of the book and of its lorebooks of its own, hidden from the lists
 * of the user, and it is deleted for good when the run ends.
 */
class AgentWorkingCopyTest extends AgentTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    private Manuscript book;
    private Lorebook root;
    private Lorebook sub;
    private ChatMessage first;
    private ChatMessage second;
    private ChatMessage swipe;
    private Agent agent;

    @BeforeEach
    void createBook() throws Exception {
        loginAgentUser();
        sub = lorebook("sub");
        entry(sub, "sub entry");
        root = lorebook("root");
        entry(root, "first entry");
        entry(root, "second entry");
        root.setSubbooks(new ArrayList<>(List.of(sub)));
        root = lorebookService.save(root);

        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        manuscript.setLorebook(root);
        book = manuscriptService.save(manuscript);

        first = message(null, "Once upon a time");
        second = message(first, "there was a dragon");
        swipe = message(first, "there was a knight");
        Summary summary = new Summary();
        summary.setSummary("a dragon");
        summary = summaryService.save(summary);
        ChatMessage stored = chatMessageService.find(second.getId());
        stored.setSummary(summary);
        chatMessageService.save(stored);
        book = manuscriptService.find(book.getId());
        book.setActiveLeaf(chatMessageService.find(second.getId()));
        book = manuscriptService.save(book);
        Tag tag = new Tag();
        tag.setValue(uniqueName("tag"));
        tagRelationService.createRelation(tagService.save(tag), book);

        agent = agentService.save(newAgent(book));
    }

    private ChatMessage message(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return parent == null ? chatMessageService.createRoot(book, message) : chatMessageService.addChild(parent, message);
    }

    private Lorebook lorebook(String name) throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName(uniqueName(name));
        lorebook.setSubbooks(new ArrayList<>());
        return lorebookService.save(lorebook);
    }

    private LorebookEntry entry(Lorebook lorebook, String name) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        entry.setPayload(name + " payload");
        return lorebookEntryService.save(entry);
    }

    private AgentRun reload(AgentRun run) throws Exception {
        return agentRunService.find(run.getId());
    }

    private Manuscript copyOf(AgentRun run) throws Exception {
        return reload(run).getWorkingCopy();
    }

    @Test
    void copyHasTheContentOfTheBookButIsAnotherBook() throws Exception {
        Manuscript copy = copyOf(agentRunService.start(agent));

        assertThat(copy.isAgentic()).isTrue();
        assertThat(copy.getId()).isNotEqualTo(book.getId());
        assertThat(copy.getUuid()).isNotEqualTo(book.getUuid());
        assertThat(copy.getName()).isEqualTo(book.getName());
        assertThat(copy.isPublished()).isFalse();
        assertThat(chatMessageService.getAllMessages(copy)).extracting(ChatMessage::getResponse)
                .containsExactlyInAnyOrder("Once upon a time", "there was a dragon", "there was a knight");
        assertThat(copy.getActiveLeaf().getResponse()).isEqualTo("there was a dragon");
        assertThat(copy.getActiveLeaf().getId()).isNotEqualTo(second.getId());
        assertThat(copy.getActiveLeaf().getSummary()).isNotNull();
        assertThat(copy.getActiveLeaf().getSummary().getSummary()).isEqualTo("a dragon");
        assertThat(tagRelationService.getTagsForObject(copy)).hasSize(1);
    }

    @Test
    void copyHasLorebooksOfItsOwn() throws Exception {
        Manuscript copy = copyOf(agentRunService.start(agent));

        Lorebook copiedRoot = lorebookService.find(copy.getLorebook());
        assertThat(copiedRoot.isAgentic()).isTrue();
        assertThat(copiedRoot.getId()).isNotEqualTo(root.getId());
        assertThat(copiedRoot.getName()).isEqualTo(root.getName());
        assertThat(lorebookEntryService.getEntriesForLorebook(copiedRoot)).extracting(LorebookEntry::getName)
                .containsExactlyInAnyOrder("first entry", "second entry");
        List<Lorebook> subbooks = lorebookService.getSubbooks(copiedRoot);
        assertThat(subbooks).hasSize(1);
        assertThat(subbooks.getFirst().isAgentic()).isTrue();
        assertThat(subbooks.getFirst().getId()).isNotEqualTo(sub.getId());
        assertThat(lorebookEntryService.getEntriesForLorebook(subbooks.getFirst())).extracting(LorebookEntry::getName)
                .containsExactly("sub entry");

        // the lorebooks of the user are what they were
        Lorebook original = lorebookService.find(root.getId());
        assertThat(original.isAgentic()).isFalse();
        assertThat(manuscriptService.find(book.getId()).getLorebook().getId()).isEqualTo(root.getId());
        assertThat(lorebookEntryService.getEntriesForLorebook(original)).hasSize(2);
        assertThat(lorebookService.getSubbooks(original)).extracting(Lorebook::getId).containsExactly(sub.getId());
    }

    @Test
    void copiesAreHiddenFromTheListsOfTheUser() throws Exception {
        Manuscript copy = copyOf(agentRunService.start(agent));
        Lorebook copiedRoot = lorebookService.find(copy.getLorebook());

        assertThat(manuscriptService.findAllRegularForUser()).extracting(Manuscript::getId)
                .contains(book.getId()).doesNotContain(copy.getId());
        assertThat(manuscriptService.findAllAgenticForUser()).extracting(Manuscript::getId)
                .contains(copy.getId()).doesNotContain(book.getId());
        assertThat(manuscriptService.searchRegularManuscripts(null)).contains(book.getId()).doesNotContain(copy.getId());
        assertThat(manuscriptService.searchAgenticManuscripts(null)).contains(copy.getId()).doesNotContain(book.getId());
        assertThat(lorebookService.findAllRegularForUser()).extracting(Lorebook::getId)
                .contains(root.getId(), sub.getId()).doesNotContain(copiedRoot.getId());
        assertThat(lorebookService.findAllAgenticForUser()).extracting(Lorebook::getId)
                .contains(copiedRoot.getId()).doesNotContain(root.getId(), sub.getId());
    }

    @Test
    void everyRunMakesACopyOfItsOwn() throws Exception {
        AgentRun first = agentRunService.start(agent);
        Long firstCopy = copyOf(first).getId();
        agentRunService.transition(first, FAILED, "failed");

        AgentRun second = agentRunService.start(agent);

        assertThat(copyOf(second).getId()).isNotEqualTo(firstCopy);
        assertThat(chatMessageService.getAllMessages(copyOf(second))).hasSize(3);
    }

    private void assertCopyIsGone(AgentRun run, Manuscript copy, Lorebook lorebookCopy, ChatMessage copiedMessage) throws Exception {
        assertThat(reload(run).getWorkingCopy()).as("the run lets go of the copy").isNull();
        assertThat(manuscriptService.find(copy.getId())).as("the copy is deleted for good").isNull();
        assertThat(lorebookService.find(lorebookCopy.getId())).isNull();
        assertThat(chatMessageService.find(copiedMessage.getId())).isNull();
        assertThat(lorebookService.findAllAgenticForUser()).isEmpty();
        assertThat(manuscriptService.findAllAgenticForUser()).isEmpty();
        // and the book of the user is whole
        assertThat(manuscriptService.find(book.getId()).isDeleted()).isFalse();
        assertThat(chatMessageService.getAllMessages(book)).hasSize(3);
        assertThat(lorebookEntryService.getEntriesForLorebook(root)).hasSize(2);
        assertThat(lorebookService.getSubbooks(root)).hasSize(1);
    }

    private void assertRunEndsWithoutCopy(AgentRunStatus end, java.util.function.Function<AgentRun, AgentRun> ending) throws Exception {
        AgentRun run = agentRunService.start(agent);
        Manuscript copy = copyOf(run);
        Lorebook lorebookCopy = lorebookService.find(copy.getLorebook());
        ChatMessage copiedMessage = chatMessageService.getAllMessages(copy).getFirst();

        AgentRun ended = ending.apply(run);

        assertThat(ended.getStatus()).isEqualTo(end);
        assertThat(ended.getWorkingCopy()).isNull();
        assertCopyIsGone(run, copy, lorebookCopy, copiedMessage);
    }

    @Test
    void failedRunDeletesItsCopy() throws Exception {
        assertRunEndsWithoutCopy(FAILED, run -> {
            try {
                return agentRunService.transition(run, FAILED, "failed");
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @Test
    void doneRunDeletesItsCopy() throws Exception {
        assertRunEndsWithoutCopy(DONE, run -> {
            try {
                return agentRunService.transition(run, DONE, "report");
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @Test
    void discardedRunDeletesItsCopy() throws Exception {
        assertRunEndsWithoutCopy(STOPPED_DISCARDED, run -> {
            try {
                return agentRunService.stop(agentRunService.pause(run), false);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @Test
    void deletedRunDeletesItsCopy() throws Exception {
        AgentRun run = agentRunService.start(agent);
        Manuscript copy = copyOf(run);
        Lorebook lorebookCopy = lorebookService.find(copy.getLorebook());
        ChatMessage copiedMessage = chatMessageService.getAllMessages(copy).getFirst();

        agentRunService.delete(reload(run), false);

        assertCopyIsGone(run, copy, lorebookCopy, copiedMessage);
    }

    @Test
    void pausedAndKeptRunsKeepTheirCopyAndResumeOnIt() throws Exception {
        AgentRun run = agentRunService.start(agent);
        Long copyId = copyOf(run).getId();

        AgentRun paused = agentRunService.pause(run);
        assertThat(paused.getStatus()).isEqualTo(PAUSED);
        assertThat(paused.getWorkingCopy().getId()).isEqualTo(copyId);

        AgentRun resumed = agentRunService.resume(paused);
        assertThat(resumed.getStatus()).isEqualTo(RUNNING);
        assertThat(resumed.getWorkingCopy().getId()).isEqualTo(copyId);

        AgentRun kept = agentRunService.stop(agentRunService.pause(resumed), true);
        assertThat(kept.getStatus()).isEqualTo(STOPPED_KEPT);
        assertThat(kept.getWorkingCopy().getId()).isEqualTo(copyId);
        assertThat(manuscriptService.find(copyId)).isNotNull();
    }

    @Test
    void resumeMakesACopyForARunThatHasNone() throws Exception {
        AgentRun run = new AgentRun();
        run.setAgent(agent);
        run.setStatus(PAUSED);
        run = agentRunService.save(run);
        assertThat(reload(run).getWorkingCopy()).isNull();

        AgentRun resumed = agentRunService.resume(reload(run));

        assertThat(resumed.getWorkingCopy()).isNotNull();
        assertThat(resumed.getWorkingCopy().isAgentic()).isTrue();
        assertThat(resumed.getWorkingCopy().getId()).isNotEqualTo(book.getId());
    }

    @Test
    void booksOfTheUserAreNeverDeletedAsCopies() throws Exception {
        assertThatThrownBy(() -> manuscriptService.deleteAgenticCopy(book)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> lorebookService.deleteAgenticCopy(root)).isInstanceOf(IllegalArgumentException.class);

        assertThat(manuscriptService.find(book.getId())).isNotNull();
        assertThat(lorebookService.find(root.getId())).isNotNull();
        assertThat(lorebookEntryService.getEntriesForLorebook(root)).hasSize(2);
    }

    @Test
    void copyOfACopyIsRefused() throws Exception {
        Manuscript copy = copyOf(agentRunService.start(agent));

        assertThatThrownBy(() -> manuscriptService.createAgenticCopy(copy)).isInstanceOf(IllegalArgumentException.class);
    }
}
