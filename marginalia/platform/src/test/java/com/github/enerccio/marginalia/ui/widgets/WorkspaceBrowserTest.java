package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.domain.service.agent.VirtualEntry;
import com.github.enerccio.marginalia.domain.service.agent.VirtualFileSystem;
import com.github.enerccio.marginalia.domain.service.agent.VirtualPath;
import com.github.enerccio.marginalia.domain.service.agent.WorkspaceLimits;
import com.github.enerccio.marginalia.test.PlatformTestBase;
import com.github.enerccio.marginalia.test.TestImages;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.provider.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The files of a workspace as the user sees them: folders to enter, text to read, files to delete, uploads. The
 * workspace is a folder of the test over {@link WorkspaceBrowser.Source#of}, with small limits; Marginalia's agent
 * workspace behind it is tested by {@code AgentWorkspaceSourceTest}.
 */
class WorkspaceBrowserTest extends PlatformTestBase {

    private static final WorkspaceLimits LIMITS = new WorkspaceLimits(10_000, 100_000, 20);

    @TempDir
    Path home;

    // UI.setCurrent keeps only a weak reference, see GpuLockSelectTest
    private UI ui;
    private VirtualFileSystem files;
    private final AtomicBoolean busy = new AtomicBoolean();

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        files = VirtualFileSystem.ofWorkspace(home.resolve("workspace"), LIMITS, new ReentrantLock());
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private WorkspaceBrowser browser() {
        WorkspaceBrowser browser = new WorkspaceBrowser(WorkspaceBrowser.Source.of(files), busy::get);
        browser.create();
        return browser;
    }

    @Test
    void aPhoneShowsTheSizeUnderTheNameAndTheToolbarAsIcons() {
        write("/notes.txt", "hello");
        WorkspaceBrowser browser = new WorkspaceBrowser(WorkspaceBrowser.Source.of(files), busy::get);
        browser.setPhone(true);
        browser.create();

        assertThat(browser.getGrid().getColumns()).as("name and buttons, the size is under the name").hasSize(2);
        assertThat(com.github.enerccio.marginalia.test.ComponentTree.all(browser.nameCell(entry(browser, "notes.txt")),
                com.vaadin.flow.component.html.Span.class).map(s -> s.getElement().getText()).toList()).contains("notes.txt", "5 bytes");
        assertThat(com.github.enerccio.marginalia.test.ComponentTree.all(browser, com.vaadin.flow.component.button.Button.class)
                .filter(b -> b.getAriaLabel().filter("Up"::equals).isPresent()).findFirst().orElseThrow().getText()).isEmpty();
    }

    private void write(String path, String text) {
        files.writeText(VirtualPath.parse(path), text, false);
    }

    private static List<String> names(WorkspaceBrowser browser) {
        return browser.getGrid().getDataProvider().fetch(new Query<>()).map(e -> e.path().getName()).toList();
    }

    private static VirtualEntry entry(WorkspaceBrowser browser, String name) {
        return browser.getGrid().getDataProvider().fetch(new Query<>()).filter(e -> e.path().getName().equals(name))
                .findFirst().orElseThrow();
    }

    @Test
    void anEmptyWorkspaceSaysSo() {
        WorkspaceBrowser browser = browser();

        assertThat(names(browser)).isEmpty();
        assertThat(browser.getUsageLabel().getText()).isEqualTo("The workspace is empty.");
        assertThat(browser.getUpButton().isEnabled()).isFalse();
    }

    @Test
    void listsFoldersFirstAndShowsTheUsage() {
        write("/notes.md", "my notes");
        write("/reports/continuity.md", "all fine");
        write("/a.txt", "a");

        WorkspaceBrowser browser = browser();

        assertThat(names(browser)).containsExactly("reports", "a.txt", "notes.md");
        assertThat(browser.getUsageLabel().getText()).startsWith("3 files");
    }

    @Test
    void aTextFileIsShown() {
        write("/notes.md", "my notes\nsecond line");
        WorkspaceBrowser browser = browser();

        browser.open(entry(browser, "notes.md"));

        assertThat(browser.getViewer().getValue()).isEqualTo("my notes\nsecond line");
    }

    @Test
    void aLongFileIsCut() {
        write("/long.txt", "x".repeat(WorkspaceBrowser.VIEW_LIMIT + 500));
        WorkspaceBrowser browser = browser();

        browser.open(entry(browser, "long.txt"));

        assertThat(browser.getViewer().getValue()).hasSize(WorkspaceBrowser.VIEW_LIMIT);
    }

    @Test
    void foldersAreEnteredAndLeft() {
        write("/reports/continuity.md", "all fine");
        WorkspaceBrowser browser = browser();

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
        WorkspaceBrowser browser = browser();

        browser.delete(entry(browser, "a.txt"));

        assertThat(names(browser)).containsExactly("b.txt");
        assertThat(files.stat(VirtualPath.parse("/a.txt"))).isNull();
    }

    @Test
    void clearingDeletesEverything() throws Exception {
        write("/a.txt", "a");
        write("/reports/b.txt", "b");
        WorkspaceBrowser browser = browser();

        browser.clear();

        assertThat(names(browser)).isEmpty();
        assertThat(files.list(VirtualPath.ROOT)).isEmpty();
    }

    @Test
    void aFolderIsCreatedInTheOpenFolder() {
        write("/reports/continuity.md", "all fine");
        WorkspaceBrowser browser = browser();
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
        WorkspaceBrowser browser = browser();

        browser.createFolder("a/b:c");
        browser.createFolder("notes.md");

        assertThat(names(browser)).containsExactly("notes.md");
    }

    @Test
    void anUploadedTextFileLandsInTheOpenFolder() {
        write("/reports/continuity.md", "all fine");
        WorkspaceBrowser browser = browser();
        browser.open(entry(browser, "reports"));

        browser.upload("plot.md", "žluťoučký kůň".getBytes(StandardCharsets.UTF_8));

        assertThat(names(browser)).containsExactly("continuity.md", "plot.md");
        assertThat(files.readText(VirtualPath.parse("/reports/plot.md"))).isEqualTo("žluťoučký kůň");
    }

    @Test
    void anUploadHasToBeTextWithAGoodNameAndNothingThatExists() {
        write("/notes.md", "my notes");
        WorkspaceBrowser browser = browser();

        browser.upload("image.png", new byte[]{(byte) 0x89, 'P', 'N', 'G', 0, 1});
        browser.upload("a:b.md", "x".getBytes(StandardCharsets.UTF_8));
        browser.upload("notes.md", "replaced".getBytes(StandardCharsets.UTF_8));
        browser.upload("NOTES.md", "replaced".getBytes(StandardCharsets.UTF_8));

        assertThat(names(browser)).containsExactly("notes.md");
        assertThat(files.readText(VirtualPath.parse("/notes.md"))).isEqualTo("my notes");
    }

    @Test
    void anUploadedZipFileIsKeptAsItIs() throws Exception {
        WorkspaceBrowser browser = browser();
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zip = new java.util.zip.ZipOutputStream(out)) {
            zip.putNextEntry(new java.util.zip.ZipEntry("a.md"));
            zip.write("text".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        byte[] archive = out.toByteArray();

        browser.upload("pack.zip", archive);
        browser.upload("pack.zip", archive);

        assertThat(names(browser)).containsExactly("pack.zip");
        assertThat(files.read(VirtualPath.parse("/pack.zip"))).isEqualTo(archive);
        assertThat(files.unzip(VirtualPath.parse("/pack.zip"), VirtualPath.parse("/pack")).files()).isEqualTo(1);
    }

    @Test
    void anUploadedImageLandsInTheOpenFolder() throws Exception {
        WorkspaceBrowser browser = browser();
        browser.createFolder("refs");
        browser.open(entry(browser, "refs"));
        byte[] picture = TestImages.image(80, 60, "png");

        browser.upload("map.png", picture);

        assertThat(names(browser)).containsExactly("map.png");
        assertThat(files.read(VirtualPath.parse("/refs/map.png"))).isEqualTo(picture);
        assertThat(files.readImage(VirtualPath.parse("/refs/map.png")).width()).isEqualTo(80);
    }

    @Test
    void somethingThatOnlyLooksLikeAnImageIsNotUploaded() {
        WorkspaceBrowser browser = browser();
        byte[] fake = java.util.Arrays.copyOf(new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A}, 64);

        browser.upload("fake.png", fake);
        browser.upload("tool.bin", new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13});

        assertThat(names(browser)).isEmpty();
    }

    @Test
    void anImageKeepsTheLimitsToo() throws Exception {
        write("/notes.md", "x");
        WorkspaceBrowser browser = browser();
        byte[] picture = TestImages.image(40, 40, "png");

        // the workspace is full of the other files: an image counts like any file
        WorkspaceLimits limits = LIMITS;
        for (int i = 0; i < limits.maxFiles() - 1; i++) {
            files.writeText(VirtualPath.parse("/f" + i + ".txt"), "x", false);
        }
        browser.upload("one-more.png", picture);

        assertThat(files.stat(VirtualPath.parse("/one-more.png"))).isNull();
        assertThat(files.getUsage().files()).isEqualTo(limits.maxFiles());
    }

    @Test
    void anUploadKeepsTheLimits() {
        WorkspaceBrowser browser = browser();
        WorkspaceLimits limits = LIMITS;

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
    void whileSomethingWorksOnTheFilesTheyAreOnlyToLookAt() throws Exception {
        busy.set(true);
        WorkspaceBrowser browser = browser();

        assertThat(browser.getNewFolderButton().isEnabled()).isFalse();
        assertThat(browser.getUpload().isEnabled()).isFalse();
        browser.createFolder("drafts");
        browser.upload("a.txt", "a".getBytes(StandardCharsets.UTF_8));

        assertThat(names(browser)).isEmpty();
        assertThat(browser.getClearButton().isEnabled()).isFalse();
    }
}
