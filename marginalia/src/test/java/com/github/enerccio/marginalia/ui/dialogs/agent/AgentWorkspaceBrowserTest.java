package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.service.AgentVirtualFileSystemService;
import com.github.enerccio.marginalia.domain.service.agent.VirtualEntry;
import com.github.enerccio.marginalia.domain.service.agent.VirtualFileSystem;
import com.github.enerccio.marginalia.domain.service.agent.VirtualPath;
import com.github.enerccio.marginalia.domain.service.agent.WorkspaceLimits;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.provider.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The files of the workspace of an agent as the user sees them: folders to enter, text to read, files to delete.
 */
class AgentWorkspaceBrowserTest extends AgentTestBase {

    @Autowired
    private AgentVirtualFileSystemService fileSystemService;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;
    private Agent agent;
    private VirtualFileSystem files;
    private final AtomicBoolean busy = new AtomicBoolean();

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        loginAgentUser();
        agent = agent();
        files = fileSystemService.openWorkspace(agent);
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private AgentWorkspaceBrowser browser() {
        AgentWorkspaceBrowser browser = new AgentWorkspaceBrowser(agent, busy::get);
        browser.create();
        return browser;
    }

    private void write(String path, String text) {
        files.writeText(VirtualPath.parse(path), text, false);
    }

    private static List<String> names(AgentWorkspaceBrowser browser) {
        return browser.getGrid().getDataProvider().fetch(new Query<>()).map(e -> e.path().getName()).toList();
    }

    private static VirtualEntry entry(AgentWorkspaceBrowser browser, String name) {
        return browser.getGrid().getDataProvider().fetch(new Query<>()).filter(e -> e.path().getName().equals(name))
                .findFirst().orElseThrow();
    }

    @Test
    void anEmptyWorkspaceSaysSo() {
        AgentWorkspaceBrowser browser = browser();

        assertThat(names(browser)).isEmpty();
        assertThat(browser.getUsageLabel().getText()).isEqualTo("The workspace is empty.");
        assertThat(browser.getUpButton().isEnabled()).isFalse();
    }

    @Test
    void listsFoldersFirstAndShowsTheUsage() {
        write("/notes.md", "my notes");
        write("/reports/continuity.md", "all fine");
        write("/a.txt", "a");

        AgentWorkspaceBrowser browser = browser();

        assertThat(names(browser)).containsExactly("reports", "a.txt", "notes.md");
        assertThat(browser.getUsageLabel().getText()).startsWith("3 files");
    }

    @Test
    void aTextFileIsShown() {
        write("/notes.md", "my notes\nsecond line");
        AgentWorkspaceBrowser browser = browser();

        browser.open(entry(browser, "notes.md"));

        assertThat(browser.getViewer().getValue()).isEqualTo("my notes\nsecond line");
    }

    @Test
    void aLongFileIsCut() {
        write("/long.txt", "x".repeat(AgentWorkspaceBrowser.VIEW_LIMIT + 500));
        AgentWorkspaceBrowser browser = browser();

        browser.open(entry(browser, "long.txt"));

        assertThat(browser.getViewer().getValue()).hasSize(AgentWorkspaceBrowser.VIEW_LIMIT);
    }

    @Test
    void foldersAreEnteredAndLeft() {
        write("/reports/continuity.md", "all fine");
        AgentWorkspaceBrowser browser = browser();

        browser.open(entry(browser, "reports"));

        assertThat(browser.getFolder().getName()).isEqualTo("reports");
        assertThat(names(browser)).containsExactly("continuity.md");
        assertThat(browser.getUpButton().isEnabled()).isTrue();

        browser.getUpButton().click();

        assertThat(browser.getFolder().isRoot()).isTrue();
        assertThat(names(browser)).containsExactly("reports");
    }

    @Test
    void aFileCanBeDeleted() {
        write("/a.txt", "a");
        write("/b.txt", "b");
        AgentWorkspaceBrowser browser = browser();

        browser.delete(entry(browser, "a.txt"));

        assertThat(names(browser)).containsExactly("b.txt");
        assertThat(files.stat(VirtualPath.parse("/a.txt"))).isNull();
    }

    @Test
    void clearingDeletesEverything() throws Exception {
        write("/a.txt", "a");
        write("/reports/b.txt", "b");
        AgentWorkspaceBrowser browser = browser();

        browser.clear();

        assertThat(names(browser)).isEmpty();
        assertThat(fileSystemService.openWorkspace(agent).list(VirtualPath.ROOT)).isEmpty();
    }

    @Test
    void whileTheAgentWorksTheFilesAreOnlyToLookAt() throws Exception {
        write("/a.txt", "a");
        agentRunService.start(agent);
        busy.set(true);
        AgentWorkspaceBrowser browser = browser();

        assertThat(browser.getClearButton().isEnabled()).isFalse();
        browser.clear();

        assertThat(names(browser)).as("the service refused").containsExactly("a.txt");
    }

    @Test
    void aFolderIsCreatedInTheOpenFolder() {
        write("/reports/continuity.md", "all fine");
        AgentWorkspaceBrowser browser = browser();
        browser.createFolder("drafts");
        browser.open(entry(browser, "reports"));

        browser.createFolder(" chapters ");

        assertThat(names(browser)).containsExactly("chapters", "continuity.md");
        assertThat(files.stat(VirtualPath.parse("/reports/chapters")).directory()).isTrue();
        assertThat(files.stat(VirtualPath.parse("/drafts")).directory()).isTrue();
    }

    @Test
    void aFolderWithABadNameOrOneThatExistsIsRefused() {
        write("/notes.md", "my notes");
        AgentWorkspaceBrowser browser = browser();

        browser.createFolder("a/b:c");
        browser.createFolder("notes.md");

        assertThat(names(browser)).containsExactly("notes.md");
    }

    @Test
    void anUploadedTextFileLandsInTheOpenFolder() {
        write("/reports/continuity.md", "all fine");
        AgentWorkspaceBrowser browser = browser();
        browser.open(entry(browser, "reports"));

        browser.upload("plot.md", "žluťoučký kůň".getBytes(StandardCharsets.UTF_8));

        assertThat(names(browser)).containsExactly("continuity.md", "plot.md");
        assertThat(files.readText(VirtualPath.parse("/reports/plot.md"))).isEqualTo("žluťoučký kůň");
    }

    @Test
    void anUploadHasToBeTextWithAGoodNameAndNothingThatExists() {
        write("/notes.md", "my notes");
        AgentWorkspaceBrowser browser = browser();

        browser.upload("image.png", new byte[]{(byte) 0x89, 'P', 'N', 'G', 0, 1});
        browser.upload("a:b.md", "x".getBytes(StandardCharsets.UTF_8));
        browser.upload("notes.md", "replaced".getBytes(StandardCharsets.UTF_8));
        browser.upload("NOTES.md", "replaced".getBytes(StandardCharsets.UTF_8));

        assertThat(names(browser)).containsExactly("notes.md");
        assertThat(files.readText(VirtualPath.parse("/notes.md"))).isEqualTo("my notes");
    }

    @Test
    void anUploadKeepsTheLimitsOfTheAgent() {
        AgentWorkspaceBrowser browser = browser();
        WorkspaceLimits limits = fileSystemService.getLimits();

        browser.upload("big.txt", "x".repeat((int) limits.maxFileBytes() + 1).getBytes(StandardCharsets.UTF_8));
        for (int i = 0; i < limits.maxFiles() + 2; i++) {
            files.writeText(VirtualPath.parse("/f" + i + ".txt"), "x", false);
            if (files.getUsage().files() >= limits.maxFiles()) {
                break;
            }
        }
        browser.upload("one-more.txt", "x".getBytes(StandardCharsets.UTF_8));

        assertThat(files.stat(VirtualPath.parse("/big.txt"))).isNull();
        assertThat(files.stat(VirtualPath.parse("/one-more.txt"))).isNull();
        assertThat(files.getUsage().files()).isEqualTo(limits.maxFiles());
    }

    @Test
    void whileTheAgentWorksNothingCanBeUploadedOrCreated() throws Exception {
        agentRunService.start(agent);
        busy.set(true);
        AgentWorkspaceBrowser browser = browser();

        assertThat(browser.getNewFolderButton().isEnabled()).isFalse();
        assertThat(browser.getUpload().isEnabled()).isFalse();
        browser.createFolder("drafts");
        browser.upload("a.txt", "a".getBytes(StandardCharsets.UTF_8));

        assertThat(names(browser)).isEmpty();
    }
}
