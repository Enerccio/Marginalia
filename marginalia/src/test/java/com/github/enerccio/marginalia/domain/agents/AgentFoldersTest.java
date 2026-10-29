package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.AgentMergeService.MergeSelection;
import com.github.enerccio.marginalia.domain.service.EffectiveSetting.Origin;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Item;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Folders and agents: the working copy of a run is in the folder of the book, so it writes with the settings of the folders,
 * a book a run works on holds its folders in place (move, delete and a changed setting are refused), and a merge never moves
 * the book.
 */
class AgentFoldersTest extends AgentTestBase {

    @Autowired
    private FolderService folderService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private AgentMergeService mergeService;

    private Folder scifi;
    private Folder expanse;
    private Manuscript book;
    private Agent agent;

    @BeforeEach
    void setUp() throws Exception {
        loginAgentUser();
        scifi = folderService.create("Sci-fi", null);
        scifi = folderService.update(scifi, new FolderService.Details("Sci-fi", null, Language.CZECH, Map.of(PromptField.STYLE, "sparse")));
        expanse = folderService.create("Expanse", scifi);
        book = manuscript();
        manuscriptService.moveToFolder(List.of(book), expanse);
        ChatMessage first = new ChatMessage();
        first.setResponse("Once upon a time");
        chatMessageService.createRoot(manuscriptService.find(book.getId()), first);
        agent = agentService.save(newAgent(manuscriptService.find(book.getId())));
    }

    private Manuscript copyOf(AgentRun run) throws Exception {
        return manuscriptService.find(agentRunService.find(run.getId()).getWorkingCopy());
    }

    private String path(Manuscript manuscript) throws Exception {
        return folderService.getPath(manuscriptService.find(manuscript.getId()).getFolder());
    }

    private Date modification(Folder folder) throws Exception {
        return folderService.find(folder.getId()).getModification();
    }

    @Test
    void theWorkingCopyIsInTheFolderOfTheBook() throws Exception {
        AgentRun run = agentRunService.start(agent);

        assertThat(path(copyOf(run))).isEqualTo("Sci-fi/Expanse");
    }

    @Test
    void theWorkingCopyWritesWithTheSettingsOfTheFolders() throws Exception {
        Manuscript copy = copyOf(agentRunService.start(agent));

        EffectiveSetting<String> style = manuscriptService.resolve(copy, PromptField.STYLE);
        assertThat(style.value()).isEqualTo("sparse");
        assertThat(style.origin()).isEqualTo(Origin.FOLDER);
        assertThat(manuscriptService.getLanguage(copy)).isEqualTo(Language.CZECH);
    }

    @Test
    void theWorkingCopyOfABookAtTheTopLevelHasNoFolder() throws Exception {
        Manuscript loose = manuscript();
        AgentRun run = agentRunService.start(agentService.save(newAgent(loose)));

        assertThat(copyOf(run).getFolder()).isNull();
    }

    @Test
    void theWorkingCopyIsNotContentOfTheFolder() throws Exception {
        Date before = modification(expanse);
        agentRunService.start(agent);
        Manuscript copy = copyOf(agentRunService.find(agentRunService.findLockingRun(book).getId()));
        ChatMessage part = new ChatMessage();
        part.setResponse("written by the agent");
        chatMessageService.createRoot(copy, part);

        assertThat(folderService.describeContent(scifi).books()).isEqualTo(1);
        assertThat(folderService.countBooksByFolder().get(expanse.getId())).isEqualTo(1);
        assertThat(modification(expanse)).as("the work in the copy is no change of the folder").isEqualTo(before);
    }

    @Test
    void aRunningBookHoldsItsFoldersInPlace() throws Exception {
        agentRunService.start(agent);
        Folder other = folderService.create("Other", null);

        assertThatThrownBy(() -> folderService.move(folderService.find(scifi.getId()), other)).isInstanceOf(FolderBookLockedException.class);
        assertThatThrownBy(() -> folderService.move(folderService.find(expanse.getId()), null)).isInstanceOf(FolderBookLockedException.class);
        assertThatThrownBy(() -> folderService.delete(folderService.find(scifi.getId()), false)).isInstanceOf(FolderBookLockedException.class);
        assertThatThrownBy(() -> folderService.deleteWithContent(folderService.find(scifi.getId()))).isInstanceOf(FolderBookLockedException.class);
        assertThatThrownBy(() -> folderService.update(folderService.find(scifi.getId()),
                new FolderService.Details("Sci-fi", null, Language.CZECH, Map.of(PromptField.STYLE, "lush"))))
                .isInstanceOf(FolderBookLockedException.class).hasMessageContaining(manuscriptService.find(book.getId()).getName());
        assertThatThrownBy(() -> folderService.update(folderService.find(scifi.getId()),
                new FolderService.Details("Sci-fi", null, Language.GERMAN, Map.of(PromptField.STYLE, "sparse"))))
                .isInstanceOf(FolderBookLockedException.class);
        assertThatThrownBy(() -> manuscriptService.moveToFolder(List.of(manuscriptService.find(book.getId())), other))
                .isInstanceOf(ManuscriptLockedException.class);

        // the name and the description are not what the book inherits
        assertThat(folderService.rename(folderService.find(scifi.getId()), "Science fiction").getName()).isEqualTo("Science fiction");
        assertThat(path(book)).isEqualTo("Science fiction/Expanse");
        // an unrelated folder is free
        assertThat(folderService.move(other, null)).isNotNull();
    }

    @Test
    void aPausedRunLetsTheFoldersGo() throws Exception {
        AgentRun run = agentRunService.start(agent);
        agentRunService.pause(agentRunService.find(run.getId()));
        Folder other = folderService.create("Other", null);

        manuscriptService.moveToFolder(List.of(manuscriptService.find(book.getId())), other);

        assertThat(path(book)).isEqualTo("Other");
    }

    @Test
    void theCopyFollowsTheBookWhenItWasMovedWhilePaused() throws Exception {
        AgentRun run = agentRunService.start(agent);
        agentRunService.pause(agentRunService.find(run.getId()));
        Folder other = folderService.create("Other", null);
        manuscriptService.moveToFolder(List.of(manuscriptService.find(book.getId())), other);

        agentRunService.resume(agentRunService.find(run.getId()));

        assertThat(path(copyOf(run))).isEqualTo("Other");
        assertThat(manuscriptService.resolve(copyOf(run), PromptField.STYLE).origin()).isEqualTo(Origin.DEFAULT);
    }

    @Test
    void aMergeNeverMovesTheBook() throws Exception {
        AgentRun run = agentRunService.start(agent);
        Manuscript copy = copyOf(run);
        ChatMessage added = new ChatMessage();
        added.setResponse("the agent continues");
        ChatMessage root = chatMessageService.getAllMessages(copy).getFirst();
        chatMessageService.addChild(chatMessageService.find(root.getId()), added);

        agentRunService.transition(agentRunService.find(run.getId()), AgentRunStatus.DONE, "report");
        // the user moves the book while the result waits to be taken over
        Folder other = folderService.create("Other", null);
        manuscriptService.moveToFolder(List.of(manuscriptService.find(book.getId())), other);
        MergePlan plan = mergeService.plan(agentRunService.find(run.getId()));
        assertThat(plan.items()).isNotEmpty();
        mergeService.apply(agentRunService.find(run.getId()),
                new MergeSelection(plan.items().stream().map(Item::id).collect(Collectors.toSet()), Map.of(), false));

        assertThat(path(book)).isEqualTo("Other");
        assertThat(chatMessageService.getAllMessages(manuscriptService.find(book.getId())))
                .extracting(ChatMessage::getResponse).contains("the agent continues");
    }

    @Test
    void aWorkingCopyReplacedFromASnapshotStaysInTheFolder() throws Exception {
        AgentRun run = agentRunService.start(agent);
        JsonObject snapshot = manuscriptService.snapshotAgenticCopy(copyOf(run));
        ManuscriptService.AgenticCopy fresh = manuscriptService.createAgenticCopyFromSnapshot(snapshot);
        assertThat(fresh.manuscript().getFolder()).isNull();

        agentRunService.replaceWorkingCopy(agentRunService.find(run.getId()), fresh.manuscript(), List.of());

        assertThat(path(copyOf(run))).isEqualTo("Sci-fi/Expanse");
    }
}
