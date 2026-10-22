package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.AgentCopyService;
import com.github.enerccio.marginalia.domain.service.AgentCopyService.CopyBranch;
import com.github.enerccio.marginalia.domain.service.AgentCopyService.CopyLorebook;
import com.github.enerccio.marginalia.domain.service.AgentCopyService.CopyPart;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Reading the working copy of a run: its branches, the parts of one, its lorebooks, and nothing of anyone else's.
 */
class AgentCopyServiceTest extends AgentTestBase {

    @Autowired
    private AgentCopyService copyService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    private Manuscript book;
    private AgentRun run;

    @BeforeEach
    void startRun() throws Exception {
        loginAgentUser();
        Lorebook root = new Lorebook();
        root.setName("lore");
        root.setSubbooks(new ArrayList<>());
        root = lorebookService.save(root);
        LorebookEntry dragon = new LorebookEntry();
        dragon.setLorebook(root);
        dragon.setName("dragon");
        dragon.setPayload("a red dragon");
        lorebookEntryService.save(dragon);
        lorebookService.createSubbook(root, "places");
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        manuscript.setLorebook(root);
        book = manuscriptService.save(manuscript);
        ChatMessage first = new ChatMessage();
        first.setResponse("Once upon a time");
        first = chatMessageService.createRoot(book, first);
        ChatMessage second = new ChatMessage();
        second.setResponse("there was a dragon");
        second = chatMessageService.addChild(first, second);
        ChatMessage third = new ChatMessage();
        third.setResponse("and a castle");
        third = chatMessageService.addChild(second, third);
        book.setActiveLeaf(third);
        book = manuscriptService.save(book);
        run = agentRunService.start(agentService.save(newAgent(book)));
    }

    private Manuscript copy() throws Exception {
        return agentRunService.find(run.getId()).getWorkingCopy();
    }

    private ChatMessage inCopy(String text) throws Exception {
        return chatMessageService.getAllMessages(copy()).stream().filter(m -> text.equals(m.getResponse())).findFirst().orElseThrow();
    }

    @Test
    void theStoryOfTheCopyIsReadAsItIsNow() throws Exception {
        ChatMessage other = new ChatMessage();
        other.setResponse("or a tower");
        chatMessageService.addChild(chatMessageService.find(inCopy("there was a dragon").getId()), other);

        List<CopyBranch> branches = copyService.branches(run);
        List<CopyPart> parts = copyService.branch(run, null);

        assertThat(branches).hasSize(2);
        assertThat(branches).extracting(CopyBranch::parts).containsExactly(3, 3);
        assertThat(branches).filteredOn(CopyBranch::active).hasSize(1);
        assertThat(parts).extracting(CopyPart::text).hasSize(3).startsWith("Once upon a time", "there was a dragon");
        assertThat(parts).extracting(CopyPart::position).containsExactly(1, 2, 3);
        assertThat(parts.get(2).swipes()).isEqualTo(2);
    }

    @Test
    void thePartsOfTheBranchThatShowsAPartAreGiven() throws Exception {
        ChatMessage other = new ChatMessage();
        other.setResponse("or a tower");
        other = chatMessageService.addChild(chatMessageService.find(inCopy("there was a dragon").getId()), other);

        List<CopyPart> parts = copyService.branch(run, other.getUuid());

        assertThat(parts).extracting(CopyPart::text).containsExactly("Once upon a time", "there was a dragon", "or a tower");
        assertThat(parts.getLast().swipe()).isEqualTo(2);
    }

    @Test
    void whatTheAgentWritesInTheCopyIsSeenButTheBookIsNot() throws Exception {
        ChatMessage copied = chatMessageService.find(inCopy("there was a dragon").getId());
        copied.setResponse("there was a small dragon");
        chatMessageService.save(copied);

        assertThat(copyService.branch(run, null)).extracting(CopyPart::text).contains("there was a small dragon");
        assertThat(chatMessageService.getAllMessages(manuscriptService.find(book.getId()))).extracting(ChatMessage::getResponse)
                .contains("there was a dragon");
    }

    @Test
    void theLorebooksOfTheCopyAreListedWithTheirEntries() throws Exception {
        List<CopyLorebook> books = copyService.lorebooks(run);

        assertThat(books).extracting(CopyLorebook::name).containsExactly("lore", "places");
        assertThat(books.getFirst().subbooks()).containsExactly("places");
        assertThat(books.getFirst().entries()).extracting(AgentCopyService.CopyEntry::name, AgentCopyService.CopyEntry::payload)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("dragon", "a red dragon"));
        // the copy has its own lorebooks: the uuids are not the book's
        assertThat(books.getFirst().uuid()).isNotEqualTo(manuscriptService.find(book.getId()).getLorebook().getUuid());
    }

    @Test
    void aRunWithoutACopyHasNothingToRead() throws Exception {
        run = agentRunService.stop(agentRunService.pause(agentRunService.find(run.getId())), false);
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.STOPPED_DISCARDED);

        assertThatThrownBy(() -> copyService.branches(run)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void theRunOfAnotherUserCannotBeRead() throws Exception {
        login();

        assertThatThrownBy(() -> copyService.branches(run)).isInstanceOf(IllegalArgumentException.class);
    }
}
