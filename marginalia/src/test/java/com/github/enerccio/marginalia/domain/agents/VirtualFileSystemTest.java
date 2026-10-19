package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.service.agent.*;
import com.github.enerccio.marginalia.test.TestImages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The cage around the workspace of an agent: what the model can name, read, write and delete, and what it can never
 * reach. Attacks planted on the disk (links, other spellings) and the limits are tested here, on a real folder.
 */
class VirtualFileSystemTest {

    private static final WorkspaceLimits LIMITS = new WorkspaceLimits(1000, 5000, 10);

    @TempDir
    Path home;

    private RunFolder folder;
    private Path outside;
    private VirtualFileSystem fs;

    @BeforeEach
    void open() throws IOException {
        folder = RunFolder.of(home, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        outside = Files.createDirectories(home.resolve("outside"));
        Files.writeString(outside.resolve("secret.txt"), "secret");
        fs = VirtualFileSystem.ofRun(folder, LIMITS, new ReentrantLock());
    }

    private VirtualPath p(String path) {
        return fs.parse(path);
    }

    private void write(String path, String text) {
        fs.writeText(p(path), text, false);
    }

    private String read(String path) {
        return fs.readText(p(path));
    }

    private List<String> names(String folderPath) {
        return fs.list(p(folderPath)).stream().map(e -> e.path().toString()).toList();
    }

    // ---------------------------------------------------------------------------------------------------------------
    // the basics
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void writesAndReadsAFileAndCreatesFoldersOnTheWay() throws IOException {
        write("/notes/plot/act1.md", "The beginning\n");

        assertThat(read("/notes/plot/act1.md")).isEqualTo("The beginning\n");
        VirtualEntry entry = fs.stat(p("/notes/plot/act1.md"));
        assertThat(entry.directory()).isFalse();
        assertThat(entry.size()).isEqualTo(14);
        assertThat(fs.stat(p("/notes/plot")).directory()).isTrue();
        assertThat(fs.stat(p("/notes/missing"))).isNull();
        assertThat(fs.stat(VirtualPath.ROOT).directory()).isTrue();
        assertThat(Files.readString(folder.workspace().resolve("notes/plot/act1.md"), StandardCharsets.UTF_8))
                .isEqualTo("The beginning\n");
    }

    @Test
    void replacesAndAppends() {
        write("/a.md", "one");
        write("/a.md", "two");
        fs.writeText(p("/a.md"), " three", true);
        fs.writeText(p("/new.md"), "created by append", true);

        assertThat(read("/a.md")).isEqualTo("two three");
        assertThat(read("/new.md")).isEqualTo("created by append");
    }

    @Test
    void textIsUtf8AndUnicodeNamesWork() {
        write("/\u7269\u8a9e/\u3042.txt", "\u00e9\u7269\u8a9e \ud83d\ude00");

        assertThat(read("/\u7269\u8a9e/\u3042.txt")).isEqualTo("\u00e9\u7269\u8a9e \ud83d\ude00");
        assertThat(names("/\u7269\u8a9e")).containsExactly("/\u7269\u8a9e/\u3042.txt");
    }

    @Test
    void listsFoldersFirstThenByName() {
        write("/b.md", "");
        write("/A.md", "");
        write("/z/inner.md", "");
        write("/c/inner.md", "");

        assertThat(names("/")).containsExactly("/checkpoints", "/c", "/z", "/A.md", "/b.md");
        assertThat(fs.list(p("/c")).get(0).size()).isZero();
    }

    @Test
    void walkIsDepthFirstAndLimited() {
        write("/a/1.md", "");
        write("/a/2.md", "");
        write("/b.md", "");

        assertThat(fs.walk(VirtualPath.ROOT, 100).stream().map(e -> e.path().toString()))
                .containsExactly("/a", "/a/1.md", "/a/2.md", "/b.md");
        assertThat(fs.walk(VirtualPath.ROOT, 2)).hasSize(2);
        assertThat(fs.walk(p("/a"), 100)).hasSize(2);
    }

    @Test
    void deletesFilesAndFolders() {
        write("/a/1.md", "x");
        write("/a/b/2.md", "y");
        write("/c.md", "z");

        fs.delete(p("/c.md"), false);
        assertThat(fs.stat(p("/c.md"))).isNull();
        assertThatThrownBy(() -> fs.delete(p("/a"), false)).hasMessageContaining("not empty");
        assertThat(fs.stat(p("/a/1.md"))).isNotNull();
        fs.delete(p("/a"), true);
        assertThat(fs.stat(p("/a"))).isNull();
        assertThatThrownBy(() -> fs.delete(p("/a"), true)).hasMessageContaining("nothing");
        assertThatThrownBy(() -> fs.delete(VirtualPath.ROOT, true)).isInstanceOf(VirtualFileException.class);
        assertThat(Files.isDirectory(folder.workspace())).isTrue();
    }

    @Test
    void movesAndRenamesFilesAndFolders() {
        write("/a.md", "A");
        write("/dir/x.md", "X");
        write("/dir/sub/y.md", "Y");

        fs.move(p("/a.md"), p("/b.md"));
        fs.move(p("/b.md"), p("/new/deeper/c.md"));
        fs.move(p("/dir"), p("/renamed"));

        assertThat(fs.stat(p("/a.md"))).isNull();
        assertThat(fs.stat(p("/b.md"))).isNull();
        assertThat(read("/new/deeper/c.md")).isEqualTo("A");
        assertThat(fs.stat(p("/dir"))).isNull();
        assertThat(read("/renamed/x.md")).isEqualTo("X");
        assertThat(read("/renamed/sub/y.md")).isEqualTo("Y");
        assertThat(fs.getUsage().files()).isEqualTo(3);
    }

    @Test
    void moveRefusesWhatWouldLoseOrBreakSomething() throws IOException {
        write("/a.md", "A");
        write("/b.md", "B");
        write("/dir/x.md", "X");
        Files.createSymbolicLink(folder.workspace().resolve("link"), outside);

        assertThatThrownBy(() -> fs.move(p("/a.md"), p("/b.md"))).hasMessageContaining("exists already");
        assertThatThrownBy(() -> fs.move(p("/a.md"), p("/dir"))).hasMessageContaining("exists already");
        assertThatThrownBy(() -> fs.move(p("/a.md"), p("/a.md"))).hasMessageContaining("same");
        assertThatThrownBy(() -> fs.move(p("/dir"), p("/dir/inside"))).hasMessageContaining("into itself");
        assertThatThrownBy(() -> fs.move(p("/missing"), p("/x"))).hasMessageContaining("nothing at /missing");
        assertThatThrownBy(() -> fs.move(VirtualPath.ROOT, p("/x"))).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> fs.move(p("/a.md"), VirtualPath.ROOT)).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> fs.move(p("/a.md"), p("/B.md"))).hasMessageContaining("case");
        assertThatThrownBy(() -> fs.move(p("/a.md"), p("/checkpoints"))).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> fs.move(p("/a.md"), p("/Checkpoints"))).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> fs.move(p("/a.md"), p("/.marginalia-tmp-1"))).hasMessageContaining("reserved");
        assertThatThrownBy(() -> fs.move(p("/link"), p("/moved"))).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> fs.move(p("/a.md"), p("/link/a.md"))).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> fs.move(p("/a.md"), p("/checkpoints/a.json"))).hasMessageContaining("read only");
        assertThatThrownBy(() -> fs.move(p("/checkpoints/0001.json"), p("/a2.md"))).hasMessageContaining("read only");
        assertThat(read("/a.md")).isEqualTo("A");
        assertThat(read("/b.md")).isEqualTo("B");
        assertThat(outside.resolve("a.md")).doesNotExist();
        assertThat(Files.isSymbolicLink(folder.workspace().resolve("link"))).isTrue();
    }

    @Test
    void createsAnEmptyFolderWithTheFoldersAboveIt() throws IOException {
        fs.createFolder(p("/notes/plot/act1"));

        assertThat(fs.stat(p("/notes/plot/act1")).directory()).isTrue();
        assertThat(names("/notes/plot")).containsExactly("/notes/plot/act1");
        assertThat(fs.getUsage().files()).isZero();
        try (Stream<Path> files = Files.list(folder.workspace())) {
            assertThat(files.count()).isEqualTo(1);
        }
    }

    @Test
    void aFolderIsRefusedWhereSomethingIsAlreadyOrTheNameIsNotAllowed() {
        write("/a.md", "A");
        fs.createFolder(p("/dir"));

        assertThatThrownBy(() -> fs.createFolder(p("/a.md"))).isInstanceOf(VirtualFileException.class).hasMessageContaining("exists");
        assertThatThrownBy(() -> fs.createFolder(p("/dir"))).isInstanceOf(VirtualFileException.class).hasMessageContaining("exists");
        assertThatThrownBy(() -> fs.createFolder(p("/DIR"))).isInstanceOf(VirtualFileException.class).hasMessageContaining("case");
        assertThatThrownBy(() -> fs.createFolder(VirtualPath.ROOT)).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> fs.createFolder(p("/checkpoints"))).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> fs.createFolder(p("/dir/.marginalia-tmp-x"))).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> fs.createFolder(VirtualPath.ROOT.resolve("bad:name"))).isInstanceOf(VirtualFileException.class);
    }

    @Test
    void textIsDecodedAsTheModelWouldHaveIt() {
        VirtualPath file = p("/a.md");

        assertThat(VirtualFileSystem.decodeText("žluťoučký".getBytes(StandardCharsets.UTF_8), file)).isEqualTo("žluťoučký");
        assertThatThrownBy(() -> VirtualFileSystem.decodeText(new byte[]{(byte) 0xff, (byte) 0xfe}, file))
                .isInstanceOf(VirtualFileException.class).hasMessageContaining("not a text file");
        assertThatThrownBy(() -> VirtualFileSystem.decodeText(new byte[]{'a', 0, 'b'}, file))
                .isInstanceOf(VirtualFileException.class);
    }

    @Test
    void emptyFolderIsDeletedWithoutRecursive() {
        write("/a/b/c.md", "x");
        fs.delete(p("/a/b/c.md"), false);

        fs.delete(p("/a/b"), false);

        assertThat(fs.stat(p("/a/b"))).isNull();
    }

    @Test
    void refusesWhatIsNotThere() {
        assertThatThrownBy(() -> read("/missing.md")).isInstanceOf(VirtualFileException.class).hasMessageContaining("/missing.md");
        assertThatThrownBy(() -> fs.list(p("/missing"))).isInstanceOf(VirtualFileException.class);
        write("/file.md", "x");
        assertThatThrownBy(() -> fs.list(p("/file.md"))).hasMessageContaining("not a folder");
        assertThatThrownBy(() -> read("/file.md/inner")).hasMessageContaining("is a file");
        assertThatThrownBy(() -> write("/file.md/inner", "x")).hasMessageContaining("is a file");
        write("/dir/x.md", "x");
        assertThatThrownBy(() -> read("/dir")).hasMessageContaining("folder");
        assertThatThrownBy(() -> write("/dir", "x")).hasMessageContaining("folder");
        assertThatThrownBy(() -> write("/", "x")).isInstanceOf(VirtualFileException.class);
    }

    @Test
    void refusesBinaryContent() throws IOException {
        Files.createDirectories(folder.workspace());
        Files.write(folder.workspace().resolve("image.bin"), new byte[]{(byte) 0xff, (byte) 0xfe, 0, 1});

        assertThatThrownBy(() -> read("/image.bin")).hasMessageContaining("not a text file");
        assertThatThrownBy(() -> write("/binary.md", "a\0b")).hasMessageContaining("NUL");
    }

    // ---------------------------------------------------------------------------------------------------------------
    // out of the cage
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void dotDotCannotLeaveTheWorkspace() throws IOException {
        write("/../../../outside-attempt.md", "x");
        write("/a/../../../../../../escape.md", "y");

        assertThat(folder.workspace().resolve("outside-attempt.md")).exists();
        assertThat(folder.workspace().resolve("escape.md")).exists();
        assertThat(home.resolve("outside-attempt.md")).doesNotExist();
        // the file next to the workspace folder is not what "/../outside/secret.txt" names
        assertThatThrownBy(() -> read("/../../outside/secret.txt")).isInstanceOf(VirtualFileException.class);
    }

    @Test
    void theBaseAndTheOtherFoldersOfTheRunAreNotReachable() throws IOException {
        Files.createDirectories(folder.base());
        Files.writeString(folder.base().resolve("manuscript.json"), "{\"base\":true}");

        assertThatThrownBy(() -> read("/../base/manuscript.json")).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> read("/base/manuscript.json")).isInstanceOf(VirtualFileException.class);
        write("/../base/manuscript.json", "{\"agent\":true}");

        assertThat(Files.readString(folder.base().resolve("manuscript.json"))).isEqualTo("{\"base\":true}");
        assertThat(read("/base/manuscript.json")).isEqualTo("{\"agent\":true}");
    }

    @Test
    void aLinkToAFileOutsideIsNotFollowed() throws IOException {
        Files.createDirectories(folder.workspace());
        Files.createSymbolicLink(folder.workspace().resolve("link.txt"), outside.resolve("secret.txt"));

        assertThatThrownBy(() -> read("/link.txt")).hasMessageContaining("not a file or a folder");
        assertThatThrownBy(() -> write("/link.txt", "overwritten")).hasMessageContaining("not a file or a folder");
        assertThatThrownBy(() -> fs.writeText(p("/link.txt"), "more", true)).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> fs.stat(p("/link.txt")).toString()).isInstanceOf(VirtualFileException.class);
        assertThat(names("/")).doesNotContain("/link.txt");
        assertThat(Files.readString(outside.resolve("secret.txt"))).isEqualTo("secret");
        // deleting acts on the link only
        assertThatThrownBy(() -> fs.delete(p("/link.txt"), false)).isInstanceOf(VirtualFileException.class);
        assertThat(outside.resolve("secret.txt")).exists();
    }

    @Test
    void aLinkToAFolderOutsideIsNotFollowed() throws IOException {
        Files.createDirectories(folder.workspace());
        Files.createSymbolicLink(folder.workspace().resolve("dir"), outside);

        assertThatThrownBy(() -> read("/dir/secret.txt")).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> write("/dir/new.txt", "planted")).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> fs.list(p("/dir"))).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> fs.delete(p("/dir/secret.txt"), false)).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> fs.delete(p("/dir"), true)).isInstanceOf(VirtualFileException.class);
        assertThat(outside.resolve("new.txt")).doesNotExist();
        assertThat(outside.resolve("secret.txt")).exists();
        assertThat(fs.walk(VirtualPath.ROOT, 100)).isEmpty();
    }

    @Test
    void aLinkInTheMiddleOfAFolderTreeIsNotFollowed() throws IOException {
        write("/a/b/c.md", "x");
        Files.move(folder.workspace().resolve("a/b"), home.resolve("moved"));
        Files.createSymbolicLink(folder.workspace().resolve("a/b"), home.resolve("moved"));

        assertThatThrownBy(() -> read("/a/b/c.md")).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> write("/a/b/d.md", "y")).isInstanceOf(VirtualFileException.class);
        assertThat(home.resolve("moved/d.md")).doesNotExist();
    }

    @Test
    void aWorkspaceThatIsALinkIsRefused() throws IOException {
        Path target = Files.createDirectories(home.resolve("elsewhere"));
        Path agent = home.resolve("agent-workspaces").resolve(UUID.randomUUID().toString());
        Files.createDirectories(agent.getParent());
        Files.createSymbolicLink(agent, target);

        assertThatThrownBy(() -> VirtualFileSystem.ofWorkspace(agent, LIMITS, new ReentrantLock()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void namesThatLookLikeEscapesStayInside() throws IOException {
        write("/%2e%2e/x.md", "percent");
        write("/\uff0e\uff0e/x.md", "fullwidth");

        assertThat(folder.workspace().resolve("%2e%2e/x.md")).exists();
        assertThat(folder.workspace().resolve("\uff0e\uff0e/x.md")).exists();
        assertThat(home.resolve("x.md")).doesNotExist();
    }

    @Test
    void namesThatDifferOnlyInCaseAreOneName() throws IOException {
        write("/Notes.md", "first");

        assertThatThrownBy(() -> write("/notes.md", "second")).hasMessageContaining("Notes.md").hasMessageContaining("case");
        assertThatThrownBy(() -> read("/NOTES.MD")).isInstanceOf(VirtualFileException.class);
        write("/Folder/x.md", "x");
        assertThatThrownBy(() -> write("/folder/y.md", "y")).hasMessageContaining("Folder");
        assertThat(read("/Notes.md")).isEqualTo("first");
        try (Stream<Path> files = Files.list(folder.workspace())) {
            assertThat(files.map(f -> f.getFileName().toString()).toList()).containsExactlyInAnyOrder("Notes.md", "Folder");
        }
        // changing the content of the file under its own name is not a twin
        write("/Notes.md", "again");
        assertThat(read("/Notes.md")).isEqualTo("again");
    }

    @Test
    void theMountPointAndTheTemporaryNamesAreReserved() {
        assertThatThrownBy(() -> write("/checkpoints", "x")).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> write("/Checkpoints/x.md", "x")).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> write("/.marginalia-tmp-1", "x")).hasMessageContaining("reserved");
        write("/notes/checkpoints/x.md", "fine below the root");
        assertThat(read("/notes/checkpoints/x.md")).isEqualTo("fine below the root");
    }

    @Test
    void handMadeFilesTheModelCouldNotNameAreNotListed() throws IOException {
        Files.createDirectories(folder.workspace());
        Files.writeString(folder.workspace().resolve("bad:name"), "x");
        Files.writeString(folder.workspace().resolve(".marginalia-tmp-left-over"), "x");
        Files.writeString(folder.workspace().resolve("good.md"), "x");

        assertThat(names("/")).containsExactly("/checkpoints", "/good.md");
    }

    // ---------------------------------------------------------------------------------------------------------------
    // limits
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void aFileHasALimit() {
        write("/ok.md", "x".repeat(1000));

        assertThatThrownBy(() -> write("/big.md", "x".repeat(1001))).hasMessageContaining("limit");
        assertThat(fs.stat(p("/big.md"))).isNull();
        assertThatThrownBy(() -> fs.writeText(p("/ok.md"), "x", true)).hasMessageContaining("limit");
        assertThat(read("/ok.md")).hasSize(1000);
    }

    @Test
    void theLimitIsInBytesNotCharacters() {
        // 400 characters of three bytes
        assertThatThrownBy(() -> write("/big.md", "\u7269".repeat(400))).hasMessageContaining("limit");
        write("/ok.md", "\u7269".repeat(300));
    }

    @Test
    void theWorkspaceHasALimitOfSize() {
        for (int i = 0; i < 5; i++) {
            write("/f" + i + ".md", "x".repeat(1000));
        }

        assertThatThrownBy(() -> write("/more.md", "x")).hasMessageContaining("larger than its limit");
        // replacing a file by a smaller one, or one of the same size, is fine at the limit
        write("/f0.md", "y".repeat(1000));
        write("/f1.md", "short");
        write("/more.md", "x");
        assertThat(fs.getUsage().bytes()).isEqualTo(4000 + 5 + 1);
    }

    @Test
    void theWorkspaceHasALimitOfFiles() {
        for (int i = 0; i < 10; i++) {
            write("/f" + i + ".md", "x");
        }

        assertThatThrownBy(() -> write("/f10.md", "x")).hasMessageContaining("most files");
        write("/f3.md", "replaced");
        fs.delete(p("/f9.md"), false);
        write("/f10.md", "fits now");
        assertThat(fs.getUsage().files()).isEqualTo(10);
    }

    @Test
    void parallelWritersCannotPassTheLimits() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < 8; t++) {
                int thread = t;
                futures.add(pool.submit(() -> {
                    for (int i = 0; i < 10; i++) {
                        try {
                            fs.writeText(fs.parse("/t" + thread + "/f" + i + ".md"), "x".repeat(300), false);
                        } catch (VirtualFileException expected) {
                            // over a limit
                        }
                    }
                }));
            }
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            pool.shutdownNow();
        }

        assertThat(fs.getUsage().files()).isLessThanOrEqualTo(10);
        assertThat(fs.getUsage().bytes()).isLessThanOrEqualTo(5000);
        assertThat(fs.getUsage().files()).as("something was written").isPositive();
    }

    @Test
    void aFailedWriteLeavesTheFileAsItWasAndNoTemporaryFile() throws IOException {
        write("/a.md", "old");
        assertThatThrownBy(() -> write("/a.md", "x".repeat(2000))).isInstanceOf(VirtualFileException.class);
        write("/b.md", "new");

        assertThat(read("/a.md")).isEqualTo("old");
        try (Stream<Path> files = Files.list(folder.workspace())) {
            assertThat(files.map(f -> f.getFileName().toString()).toList()).containsExactlyInAnyOrder("a.md", "b.md");
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // the checkpoints mount, the reverse mapping
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void checkpointsCanOnlyBeListed() throws IOException {
        assertThat(fs.list(p("/checkpoints"))).isEmpty();
        Files.createDirectories(folder.checkpoints());
        Files.writeString(folder.checkpoints().resolve("0001.json"), "{}");
        Files.writeString(folder.checkpoints().resolve("0002.json"), "{}");
        Files.createDirectories(folder.checkpoints().resolve("subfolder"));

        assertThat(names("/checkpoints")).containsExactly("/checkpoints/0001.json", "/checkpoints/0002.json");
        assertThat(fs.stat(p("/checkpoints")).directory()).isTrue();
        assertThat(fs.stat(p("/checkpoints/0001.json")).size()).isEqualTo(2);
        assertThat(fs.stat(p("/checkpoints/9999.json"))).isNull();
        assertThatThrownBy(() -> read("/checkpoints/0001.json")).hasMessageContaining("listed");
        assertThatThrownBy(() -> write("/checkpoints/0003.json", "x")).hasMessageContaining("read only");
        assertThatThrownBy(() -> fs.delete(p("/checkpoints/0001.json"), false)).hasMessageContaining("read only");
        assertThatThrownBy(() -> fs.delete(p("/checkpoints"), true)).hasMessageContaining("read only");
        assertThat(folder.checkpoints().resolve("0001.json")).exists();
    }

    @Test
    void theWorkspaceAloneHasNoCheckpoints() {
        VirtualFileSystem workspace = VirtualFileSystem.ofWorkspace(folder.workspace(), LIMITS, new ReentrantLock());

        assertThat(workspace.list(VirtualPath.ROOT)).isEmpty();
        assertThatThrownBy(() -> workspace.list(workspace.parse("/checkpoints"))).isInstanceOf(VirtualFileException.class);
    }

    @Test
    void walkingTheRootDoesNotEnterTheCheckpoints() throws IOException {
        Files.createDirectories(folder.checkpoints());
        Files.writeString(folder.checkpoints().resolve("0001.json"), "{}");
        write("/a.md", "x");

        assertThat(fs.walk(VirtualPath.ROOT, 100).stream().map(e -> e.path().toString())).containsExactly("/a.md");
    }

    @Test
    void mapsRealPathsBackAndRefusesTheOthers() throws IOException {
        write("/notes/a.md", "x");
        Files.createDirectories(folder.checkpoints());

        assertThat(fs.toVirtual(folder.workspace().resolve("notes/a.md"))).isEqualTo(p("/notes/a.md"));
        assertThat(fs.toVirtual(folder.workspace())).isEqualTo(VirtualPath.ROOT);
        assertThat(fs.toVirtual(folder.workspace().resolve("notes/../notes/a.md"))).isEqualTo(p("/notes/a.md"));
        assertThat(fs.toVirtual(folder.workspace().toRealPath().resolve("notes"))).isEqualTo(p("/notes"));
        assertThat(fs.toVirtual(folder.checkpoints().resolve("0001.json"))).isEqualTo(p("/checkpoints/0001.json"));
        assertThatThrownBy(() -> fs.toVirtual(folder.base().resolve("manuscript.json"))).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> fs.toVirtual(outside.resolve("secret.txt"))).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> fs.toVirtual(folder.workspace().resolve("../x"))).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> fs.toVirtual(home)).isInstanceOf(VirtualFileException.class);
    }

    @Test
    void noMessageShowsARealPath() throws IOException {
        write("/file.md", "x");
        write("/dir/inner.md", "x");
        Files.createSymbolicLink(folder.workspace().resolve("link"), outside);
        Files.createDirectories(folder.checkpoints());
        List<Runnable> attempts = List.of(
                () -> read("/missing.md"), () -> read("/dir"), () -> read("/link"), () -> read("/link/secret.txt"),
                () -> read("/file.md/x"), () -> write("/dir", "x"), () -> write("/link", "x"), () -> write("/file.md/x", "x"),
                () -> write("/big.md", "x".repeat(5000)), () -> write("/Dir/x.md", "x"), () -> write("/FILE.MD", "x"),
                () -> fs.delete(p("/dir"), false), () -> fs.delete(p("/missing"), false),
                () -> fs.move(p("/missing"), p("/x")), () -> fs.move(p("/file.md"), p("/dir")), () -> fs.move(p("/link"), p("/x")),
                () -> fs.move(p("/file.md"), p("/link/x")), () -> fs.move(p("/dir"), p("/dir/in")), () -> fs.delete(VirtualPath.ROOT, true),
                () -> fs.list(p("/file.md")), () -> fs.list(p("/missing")), () -> fs.list(p("/link")),
                () -> read("/checkpoints/x.json"), () -> write("/checkpoints/x.json", "x"),
                () -> fs.toVirtual(outside.resolve("secret.txt")), () -> fs.toVirtual(folder.base()),
                () -> fs.parse("/a:b"), () -> fs.parse("C:\\windows"), () -> fs.parse("relative"));
        String realHome = home.toString();
        String realHomeResolved = home.toRealPath().toString();

        for (Runnable attempt : attempts) {
            assertThatThrownBy(attempt::run).isInstanceOf(VirtualFileException.class)
                    .satisfies(e -> assertThat(e.getMessage()).doesNotContain(realHome).doesNotContain(realHomeResolved)
                            .doesNotContain("agent-workspaces").doesNotContain("agent-runs").doesNotContain(folder.workspace().getFileName().toString()));
        }
    }

    @Test
    void clearsAndCopiesTheWorkspace() throws IOException {
        write("/a.md", "A");
        write("/dir/b.md", "B");
        Path otherWorkspace = RunFolder.workspaceOf(home, UUID.randomUUID().toString());
        VirtualFileSystem other = VirtualFileSystem.ofWorkspace(otherWorkspace, LIMITS, new ReentrantLock());
        Files.createSymbolicLink(folder.workspace().resolve("link"), outside);

        other.copyWorkspaceFrom(fs);

        assertThat(other.readText(other.parse("/dir/b.md"))).isEqualTo("B");
        assertThat(other.list(VirtualPath.ROOT)).hasSize(2);
        assertThat(otherWorkspace.resolve("link")).doesNotExist();
        fs.clearWorkspace();
        assertThat(fs.getUsage().files()).isZero();
        try (Stream<Path> files = Files.list(folder.workspace())) {
            assertThat(files.count()).isZero();
        }
        assertThat(outside.resolve("secret.txt")).exists();
        assertThat(other.getUsage().files()).isEqualTo(2);
    }

    @Test
    void clearingTheWorkspaceKeepsWhatIsNamedAndTheFoldersAboveIt() {
        write("/a.md", "A");
        write("/keep/x.md", "X");
        write("/keep/deep/y.md", "Y");
        write("/chapters/one.md", "1");
        write("/chapters/two.txt", "2");
        write("/other/z.txt", "Z");
        fs.createFolder(p("/empty"));

        VirtualFileSystem.Cleared cleared = fs.clearWorkspace(List.of("/keep", "chapters/*.md"));

        assertThat(fs.walk(VirtualPath.ROOT, 100)).extracting(e -> e.path().toString())
                .containsExactlyInAnyOrder("/keep", "/keep/x.md", "/keep/deep", "/keep/deep/y.md", "/chapters", "/chapters/one.md");
        assertThat(cleared.files()).isEqualTo(3);
        assertThat(cleared.folders()).isEqualTo(2);
        assertThat(cleared.kept()).extracting(VirtualPath::toString).containsExactly("/keep", "/chapters/one.md");
    }

    @Test
    void clearingTheWorkspaceDeletesNothingWhenSomethingToKeepIsMissing() {
        write("/a.md", "A");

        assertThatThrownBy(() -> fs.clearWorkspace(List.of("/a.md", "/b.md"))).isInstanceOf(VirtualFileException.class)
                .hasMessageContaining("/b.md");
        assertThatThrownBy(() -> fs.clearWorkspace(List.of("*.zip"))).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> fs.clearWorkspace(List.of("/"))).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> fs.clearWorkspace(List.of("/checkpoints"))).isInstanceOf(VirtualFileException.class);
        assertThat(read("/a.md")).isEqualTo("A");
        assertThat(fs.clearWorkspace(List.of())).extracting(VirtualFileSystem.Cleared::files).isEqualTo(1);
        assertThat(fs.getUsage().files()).isZero();
    }

    // ---------------------------------------------------------------------------------------------------------------
    // images
    // ---------------------------------------------------------------------------------------------------------------

    private VirtualFileSystem withImageLimit(long textBytes, long imageBytes) {
        return VirtualFileSystem.ofRun(folder, new WorkspaceLimits(textBytes, 100_000, 10, imageBytes), new ReentrantLock());
    }

    @Test
    void anImageMayBeLargerThanATextFileButNotThanTheImageLimit() throws IOException {
        byte[] image = TestImages.image(120, 120, "png");
        assertThat(image.length).isGreaterThan(300).isLessThan(20_000);
        fs = withImageLimit(300, 20_000);

        fs.writeImage(p("/refs/map.png"), image);

        assertThat(fs.read(p("/refs/map.png"))).isEqualTo(image);
        assertThat(fs.isImage(p("/refs/map.png"))).isTrue();
        assertThat(fs.readImage(p("/refs/map.png")).mimeType()).isEqualTo("image/png");
        assertThat(fs.getUsage().bytes()).isEqualTo(image.length);
        // text keeps its own limit
        assertThatThrownBy(() -> write("/notes.md", "x".repeat(301))).isInstanceOf(VirtualFileException.class)
                .hasMessageContaining("limit of a file");
        fs = withImageLimit(300, image.length - 1);
        assertThatThrownBy(() -> fs.writeImage(p("/refs/other.png"), image)).isInstanceOf(VirtualFileException.class)
                .hasMessageContaining("limit of a file");
    }

    @Test
    void anImageCountsAgainstTheTotalAndTheNumberOfFiles() throws IOException {
        byte[] image = TestImages.image(120, 120, "png");
        fs = VirtualFileSystem.ofRun(folder, new WorkspaceLimits(1000, image.length + 10, 2, 100_000), new ReentrantLock());

        fs.writeImage(p("/a.png"), image);

        assertThatThrownBy(() -> fs.writeImage(p("/b.png"), image)).isInstanceOf(VirtualFileException.class)
                .hasMessageContaining("larger than its limit");
        write("/a.md", "A");
        assertThatThrownBy(() -> write("/b.md", "B")).isInstanceOf(VirtualFileException.class).hasMessageContaining("most files");
    }

    @Test
    void onlyAnImageIsKeptAsAnImage() {
        assertThatThrownBy(() -> fs.writeImage(p("/refs/x.png"), "not an image at all".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(VirtualFileException.class).hasMessageContaining("not an image");
        assertThat(fs.stat(p("/refs/x.png"))).isNull();
    }

    @Test
    void textToolsSayThatAnImageIsOne() throws IOException {
        fs.writeImage(p("/pic.png"), TestImages.image(20, 20, "png"));
        write("/note.md", "text");

        assertThatThrownBy(() -> fs.readText(p("/pic.png"))).isInstanceOf(VirtualFileException.class)
                .hasMessageContaining("is an image").hasMessageContaining("view_image");
        assertThatThrownBy(() -> fs.readImage(p("/note.md"))).isInstanceOf(VirtualFileException.class)
                .hasMessageContaining("not an image");
        assertThat(fs.isImage(p("/note.md"))).isFalse();
        assertThat(fs.isImage(p("/missing.png"))).isFalse();
    }

    @Test
    void aTextFileOverTheTextLimitIsStillRefusedWhenItIsReadFromTheDisk() throws IOException {
        // put there by hand, between the limit of the text and the one of the images
        fs = withImageLimit(300, 20_000);
        Files.writeString(folder.workspace().resolve("big.md"), "x".repeat(400));

        assertThatThrownBy(() -> fs.readText(p("/big.md"))).isInstanceOf(VirtualFileException.class)
                .hasMessageContaining("larger than the limit of 300");
    }
}
