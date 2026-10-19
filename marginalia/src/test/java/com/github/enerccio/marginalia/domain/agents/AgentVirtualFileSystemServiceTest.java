package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.AgentVirtualFileSystemService;
import com.github.enerccio.marginalia.domain.service.CleanupService;
import com.github.enerccio.marginalia.domain.service.agent.*;
import com.github.enerccio.marginalia.domain.service.function.FunctionCallException;
import com.github.enerccio.marginalia.domain.service.function.FunctionDefinition;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The files of an agent as the application hands them out: the workspace that belongs to the agent and outlives its
 * runs, who may open it, the tools of the model on top, and what happens to the folders when the agent and the run are
 * cloned, deleted and purged.
 */
class AgentVirtualFileSystemServiceTest extends AgentTestBase {

    @Autowired
    private AgentVirtualFileSystemService fileSystemService;

    @Autowired
    private CleanupService cleanupService;

    @Autowired
    private Configuration configuration;

    private User user;
    private Agent agent;
    private AgentRun run;

    @BeforeEach
    void createAgent() throws Exception {
        user = loginAgentUser();
        agent = agent();
        run = agentRunService.start(agent);
    }

    private Path home() {
        return configuration.getFolder().toPath();
    }

    private Path workspaceOf(Agent of) {
        return RunFolder.workspaceOf(home(), of.getUuid());
    }

    private Path folderOf(AgentRun of) {
        return RunFolder.runOf(home(), of.getUuid());
    }

    private String call(FunctionSet tools, String name, String arguments) throws Exception {
        return tools.call(name, arguments, Runnable::run, () -> false).get();
    }

    private void leaveFile(Path folder) throws IOException {
        Files.createDirectories(folder);
        Files.writeString(folder.resolve("left.txt"), "x");
    }

    // ---------------------------------------------------------------------------------------------------------------
    // opening
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void theWorkspaceBelongsToTheAgentAndOutlivesTheRuns() throws Exception {
        VirtualFileSystem fs = fileSystemService.open(run);
        fs.writeText(fs.parse("/memory/notes.md"), "Remember the name of the dragon", false);
        assertThat(workspaceOf(agent).resolve("memory/notes.md")).exists();

        agentRunService.transition(run, AgentRunStatus.DONE, null);
        agentRunService.transition(agentRunService.find(run.getId()), AgentRunStatus.STOPPED_DISCARDED, null);
        AgentRun second = agentRunService.start(agent);
        VirtualFileSystem next = fileSystemService.open(second);

        assertThat(second.getId()).isNotEqualTo(run.getId());
        assertThat(next.readText(next.parse("/memory/notes.md"))).isEqualTo("Remember the name of the dragon");
        assertThat(fileSystemService.openWorkspace(agent).readText(VirtualPath.parse("/memory/notes.md")))
                .isEqualTo("Remember the name of the dragon");
    }

    @Test
    void aRunSeesItsCheckpointsAndTheWorkspaceAloneDoesNot() throws Exception {
        Files.createDirectories(RunFolder.of(home(), run.getUuid(), agent.getUuid()).checkpoints());
        Files.writeString(RunFolder.of(home(), run.getUuid(), agent.getUuid()).checkpoints().resolve("0001.json"), "{}");

        assertThat(fileSystemService.open(run).list(VirtualPath.parse("/checkpoints"))).hasSize(1);
        assertThatThrownBy(() -> fileSystemService.openWorkspace(agent).list(VirtualPath.parse("/checkpoints")))
                .isInstanceOf(VirtualFileException.class);
    }

    @Test
    void nobodyOpensTheFilesOfAnotherUser() throws Exception {
        VirtualFileSystem fs = fileSystemService.open(run);
        fs.writeText(fs.parse("/secret.md"), "mine", false);

        loginAgentUser();

        assertThatThrownBy(() -> fileSystemService.open(run)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> fileSystemService.openWorkspace(agent)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> fileSystemService.getUsage(agent)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> fileSystemService.clearWorkspace(agent)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> fileSystemService.open(null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(workspaceOf(agent).resolve("secret.md")).exists();
    }

    @Test
    void twoAgentsHaveTwoWorkspaces() throws Exception {
        Agent other = agent();
        VirtualFileSystem mine = fileSystemService.openWorkspace(agent);
        VirtualFileSystem theirs = fileSystemService.openWorkspace(other);

        mine.writeText(mine.parse("/a.md"), "mine", false);

        assertThat(theirs.list(VirtualPath.ROOT)).isEmpty();
        assertThat(workspaceOf(agent)).isNotEqualTo(workspaceOf(other));
    }

    @Test
    void theLimitsAreTheLimitsOfTheFileSystem() throws Exception {
        assertThat(fileSystemService.open(run).getLimits()).isEqualTo(fileSystemService.getLimits());
        assertThat(fileSystemService.getUsage(agent)).isEqualTo(new WorkspaceUsage(0, 0));
    }

    // ---------------------------------------------------------------------------------------------------------------
    // the tools
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void theModelWorksOnTheFilesThroughTheFunctionSet() throws Exception {
        FunctionSet tools = fileSystemService.getFileTools(fileSystemService.open(run));

        assertThat(tools.getDefinitions().stream().map(FunctionDefinition::getName)).containsExactlyInAnyOrder("list_files", "read_file",
                "write_file", "append_file", "replace_in_file", "regex_replace_in_file", "grep_files", "move_file", "copy_file", "merge_files",
                "delete_file", "list_command_results", "save_command_result");
        assertThat(call(tools, "write_file", "{\"path\":\"/notes/plot.md\",\"content\":\"The hero left.\\n\"}"))
                .isEqualTo("Wrote /notes/plot.md (15 bytes)");
        call(tools, "replace_in_file", "{\"path\":\"/notes/plot.md\",\"old_text\":\"left\",\"new_text\":\"stayed\"}");

        assertThat(call(tools, "read_file", "{\"path\":\"/notes/plot.md\"}")).isEqualTo("The hero stayed.");
        assertThat(call(tools, "read_file", "{\"path\":\"/notes/plot.md\",\"start_line\":1,\"end_line\":1}")).startsWith("The hero stayed.");
        assertThat(call(tools, "grep_files", "{\"pattern\":\"hero\"}")).isEqualTo("/notes/plot.md:1: The hero stayed.");
        assertThat(call(tools, "list_files", "{\"path\":\"/notes\"}")).isEqualTo("/notes/plot.md  (17 bytes)");
        assertThat(call(tools, "list_files", "{\"glob\":\"*.md\"}")).isEqualTo("/notes/plot.md  (17 bytes)");
        assertThat(call(tools, "copy_file", "{\"from\":\"/notes\",\"to\":\"/copy\"}")).startsWith("Copied /notes to /copy");
        assertThat(call(tools, "merge_files", "{\"paths\":[\"/notes/plot.md\",\"/copy/*.md\"],\"target\":\"/all.md\",\"delimiter\":\"|\"}"))
                .startsWith("Joined 2 files into /all.md");
        assertThat(call(tools, "read_file", "{\"path\":\"/all.md\"}")).isEqualTo("The hero stayed.\n|The hero stayed.");
        assertThat(call(tools, "list_command_results", "{}")).isEqualTo("No command result is kept yet");
        assertThatThrownBy(() -> call(tools, "save_command_result", "{\"path\":\"/x.md\"}"))
                .hasMessageContaining("There is no result to save yet: call a command first");
        assertThat(call(tools, "move_file", "{\"from\":\"/notes/plot.md\",\"to\":\"/old/plot.md\"}")).isEqualTo("Moved /notes/plot.md to /old/plot.md");
        assertThat(call(tools, "delete_file", "{\"path\":\"/notes\",\"recursive\":true}")).isEqualTo("Deleted /notes");
        assertThat(workspaceOf(agent).resolve("old/plot.md")).exists();
        assertThat(workspaceOf(agent).resolve("notes")).doesNotExist();
    }

    @Test
    void aToolErrorIsAnErrorForTheModel() throws Exception {
        FunctionSet tools = fileSystemService.getFileTools(fileSystemService.open(run));

        assertThatThrownBy(() -> call(tools, "read_file", "{\"path\":\"/missing.md\"}"))
                .isInstanceOf(ExecutionException.class).hasCauseInstanceOf(FunctionCallException.class)
                .hasMessageContaining("There is no file /missing.md");
        assertThatThrownBy(() -> call(tools, "read_file", "{\"path\":\"/../../../etc/passwd\"}"))
                .hasMessageContaining("There is no file /etc/passwd");
        assertThatThrownBy(() -> call(tools, "read_file", "{}")).isInstanceOf(ExecutionException.class);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // usage, clear, clone
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void countsWhatTheWorkspaceHolds() throws Exception {
        VirtualFileSystem fs = fileSystemService.open(run);
        fs.writeText(fs.parse("/a.md"), "12345", false);
        fs.writeText(fs.parse("/d/b.md"), "123", false);

        assertThat(fileSystemService.getUsage(agent)).isEqualTo(new WorkspaceUsage(2, 8));
    }

    @Test
    void clearingNeedsTheAgentToBeStill() throws Exception {
        VirtualFileSystem fs = fileSystemService.open(run);
        fs.writeText(fs.parse("/stale.md"), "old", false);

        assertThatThrownBy(() -> fileSystemService.clearWorkspace(agent)).isInstanceOf(IllegalStateException.class);
        assertThat(fs.stat(fs.parse("/stale.md"))).isNotNull();

        agentRunService.transition(run, AgentRunStatus.IDLE, null);
        agentRunService.transition(agentRunService.find(run.getId()), AgentRunStatus.PAUSED, null);
        fileSystemService.clearWorkspace(agent);

        assertThat(fileSystemService.getUsage(agent)).isEqualTo(new WorkspaceUsage(0, 0));
        assertThat(workspaceOf(agent)).isDirectory();
    }

    @Test
    void clearingWithoutARunWorks() throws Exception {
        Agent idle = agent();
        VirtualFileSystem fs = fileSystemService.openWorkspace(idle);
        fs.writeText(fs.parse("/stale.md"), "old", false);

        fileSystemService.clearWorkspace(idle);

        assertThat(fs.list(VirtualPath.ROOT)).isEmpty();
    }

    @Test
    void cloningAnAgentCopiesItsWorkspace() throws Exception {
        VirtualFileSystem fs = fileSystemService.open(run);
        fs.writeText(fs.parse("/memory/notes.md"), "The dragon is called Ember", false);
        Manuscript target = manuscript();

        Agent copy = agentService.cloneAgent(agent, "Copy", target);

        VirtualFileSystem copied = fileSystemService.openWorkspace(copy);
        assertThat(copied.readText(copied.parse("/memory/notes.md"))).isEqualTo("The dragon is called Ember");
        copied.writeText(copied.parse("/memory/notes.md"), "changed in the copy", false);
        assertThat(fs.readText(fs.parse("/memory/notes.md"))).as("the copies are separate").isEqualTo("The dragon is called Ember");
    }

    @Test
    void cloningAnAgentThatNeverUsedItsWorkspaceMakesNoFolder() throws Exception {
        Agent fresh = agent();

        Agent copy = agentService.cloneAgent(fresh, "Copy", manuscript());

        assertThat(workspaceOf(fresh)).doesNotExist();
        assertThat(workspaceOf(copy)).doesNotExist();
    }

    @Test
    void aWorkspaceIsNotCopiedOntoItself() throws Exception {
        assertThatThrownBy(() -> fileSystemService.copyWorkspace(agent, agent)).isInstanceOf(IllegalArgumentException.class);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // the end of the folders
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void aSoftDeletedAgentKeepsItsWorkspace() throws Exception {
        leaveFile(workspaceOf(agent));
        leaveFile(folderOf(run));

        agentService.delete(agentService.find(agent.getId()), false);

        assertThat(workspaceOf(agent).resolve("left.txt")).exists();
        assertThat(folderOf(run).resolve("left.txt")).exists();
    }

    @Test
    void anAgentDeletedForGoodTakesItsFolders() throws Exception {
        leaveFile(workspaceOf(agent));
        leaveFile(folderOf(run));

        agentService.delete(agentService.find(agent.getId()), true);

        assertThat(workspaceOf(agent)).doesNotExist();
        assertThat(folderOf(run)).doesNotExist();
    }

    @Test
    void aRunDeletedForGoodTakesItsFolderOnly() throws Exception {
        leaveFile(workspaceOf(agent));
        leaveFile(folderOf(run));

        agentRunService.delete(agentRunService.find(run.getId()), true);

        assertThat(folderOf(run)).doesNotExist();
        assertThat(workspaceOf(agent).resolve("left.txt")).exists();
    }

    @Test
    void cleanupPurgesTheFoldersWithTheirObjects() throws Exception {
        leaveFile(workspaceOf(agent));
        leaveFile(folderOf(run));
        updatePermission(user, PermissionType.AGENTS, permission -> permission.setAgentsMaxConcurrentRuns(5));
        Agent kept = agent();
        AgentRun keptRun = agentRunService.start(kept);
        leaveFile(workspaceOf(kept));
        leaveFile(folderOf(keptRun));
        // the book of a run that works is locked: the user pauses the run first
        agentRunService.pause(agentRunService.find(run.getId()));
        manuscriptService.delete(manuscriptService.find(agent.getManuscript().getId()), false);

        loginAdmin();
        cleanupService.purge();

        assertThat(agentService.find(agent.getId())).isNull();
        assertThat(workspaceOf(agent)).doesNotExist();
        assertThat(folderOf(run)).doesNotExist();
        assertThat(workspaceOf(kept).resolve("left.txt")).as("an agent that stays keeps its workspace").exists();
        assertThat(folderOf(keptRun).resolve("left.txt")).exists();
    }

    @Test
    void cleanupOfAloneRunKeepsTheWorkspace() throws Exception {
        leaveFile(workspaceOf(agent));
        leaveFile(folderOf(run));
        agentRunService.delete(agentRunService.find(run.getId()), false);

        loginAdmin();
        cleanupService.purge();

        assertThat(agentRunService.find(run.getId())).isNull();
        assertThat(folderOf(run)).doesNotExist();
        assertThat(workspaceOf(agent).resolve("left.txt")).exists();
    }

    @Test
    void aRunListedInTheTrashStillHasItsFolder() throws Exception {
        leaveFile(folderOf(run));

        agentRunService.delete(agentRunService.find(run.getId()), false);

        assertThat(folderOf(run).resolve("left.txt")).as("it can be restored").exists();
    }
}
