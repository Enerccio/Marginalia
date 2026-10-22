package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.service.agent.*;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Zip files in the workspace and {@code unzip_file}: what is unpacked, and that an archive made to hurt is refused
 * without anything left behind.
 */
class ZipUnpackTest {

    private static final class Answer implements FunctionContinuation<String> {
        String result;
        String error;

        @Override
        public void complete(String value) {
            result = value;
        }

        @Override
        public void fail(String message) {
            error = message;
        }

        @Override
        public void fail(Throwable throwable) {
            error = throwable.getMessage();
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    }

    @TempDir
    Path home;

    private RunFolder folder;
    private VirtualFileSystem fs;
    private AgentFileTools tools;

    private void open(WorkspaceLimits limits) {
        folder = RunFolder.of(home, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        fs = VirtualFileSystem.ofRun(folder, limits, new ReentrantLock());
        tools = new AgentFileToolsImpl(fs);
    }

    private void open() {
        open(new WorkspaceLimits(100_000, 1_000_000, 100));
    }

    private String ok(Consumer<Answer> call) {
        Answer answer = new Answer();
        call.accept(answer);
        assertThat(answer.error).as("error").isNull();
        return answer.result;
    }

    private String error(Consumer<Answer> call) {
        Answer answer = new Answer();
        call.accept(answer);
        assertThat(answer.result).as("result").isNull();
        assertThat(answer.error).as("error").isNotNull();
        return answer.error;
    }

    private static byte[] zip(Map<String, byte[]> entries) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                if (!entry.getKey().endsWith("/")) {
                    zip.write(entry.getValue());
                }
                zip.closeEntry();
            }
        }
        return out.toByteArray();
    }

    private static Map<String, byte[]> entries(Object... pairs) {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            entries.put((String) pairs[i], pairs[i + 1] instanceof String s ? s.getBytes(StandardCharsets.UTF_8) : (byte[]) pairs[i + 1]);
        }
        return entries;
    }

    private void store(String path, Map<String, byte[]> entries) throws IOException {
        fs.writeArchive(fs.parse(path), zip(entries));
    }

    /**
     * Text that does not pack to nothing, as a real text does not.
     */
    private static byte[] noise(int length, int seed) {
        java.util.Random random = new java.util.Random(seed);
        StringBuilder text = new StringBuilder(length);
        while (text.length() < length) {
            text.append((char) ('a' + random.nextInt(26)));
            if (random.nextInt(6) == 0) {
                text.append(' ');
            }
        }
        return text.substring(0, length).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Text of a few words, which packs about five to one, as a real text does.
     */
    private static byte[] words(int length, int seed) {
        java.util.Random random = new java.util.Random(seed);
        String[] vocabulary = new String[60];
        for (int i = 0; i < vocabulary.length; i++) {
            vocabulary[i] = new String(noise(3 + random.nextInt(6), random.nextInt()), StandardCharsets.UTF_8).replace(" ", "x");
        }
        StringBuilder text = new StringBuilder(length);
        while (text.length() < length) {
            text.append(vocabulary[random.nextInt(vocabulary.length)]).append(' ');
        }
        return text.substring(0, length).getBytes(StandardCharsets.UTF_8);
    }

    private String names() {
        return String.join("\n", fs.walk(VirtualPath.ROOT, 1000).stream().map(e -> e.path().toString()).toList());
    }

    private void assertNothingLeftBehind() throws IOException {
        try (Stream<Path> all = Files.walk(folder.workspace())) {
            assertThat(all.map(p -> p.getFileName().toString()).filter(n -> n.startsWith(".marginalia-tmp-")).toList()).isEmpty();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // the zip file as a file of the workspace
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void aZipFileIsKeptAsItIsAndNotReadAsText() throws IOException {
        open();
        byte[] bytes = zip(entries("a.txt", "hello"));

        fs.writeArchive(fs.parse("/uploads/a.zip"), bytes);

        assertThat(fs.read(fs.parse("/uploads/a.zip"))).isEqualTo(bytes);
        assertThat(error(a -> tools.readFile(a, "/uploads/a.zip", 0, 0))).contains("is a zip file").contains("unzip_file");
    }

    @Test
    void bytesThatAreNotAZipFileAreNotKeptAsOne() {
        open();

        assertThat(Arrays.asList(catchMessage(() -> fs.writeArchive(fs.parse("/a.zip"), "plain text".getBytes(StandardCharsets.UTF_8)))))
                .first().asString().contains("is not a zip file");
    }

    private static String[] catchMessage(Runnable action) {
        try {
            action.run();
        } catch (VirtualFileException e) {
            return new String[]{e.getMessage()};
        }
        return new String[]{"nothing was thrown"};
    }

    @Test
    void aZipFileLargerThanTheLimitIsNotKept() throws IOException {
        open(new WorkspaceLimits(100, 1_000_000, 10, 100, 200));
        byte[] big = zip(entries("a.txt", "x".repeat(100) + UUID.randomUUID()));
        assertThat(big.length).isGreaterThan(100);

        // 200 bytes is the limit of a zip file here
        Map<String, byte[]> many = new LinkedHashMap<>();
        for (int i = 0; i < 20; i++) {
            many.put("f" + i + ".txt", UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        }
        byte[] larger = zip(many);

        assertThat(catchMessage(() -> fs.writeArchive(fs.parse("/big.zip"), larger))[0]).contains("the limit of a file is 200 bytes");
    }

    // ---------------------------------------------------------------------------------------------------------------
    // unzip_file
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void unpacksIntoAFolderNamedAfterTheArchive() throws IOException {
        open();
        store("/uploads/book.zip", entries("ch1.md", "One\n", "notes/plot.md", "Plot\n", "empty/", ""));

        String result = ok(a -> tools.unzipFile(a, "/uploads/book.zip", null));

        assertThat(result).isEqualTo("Unpacked /uploads/book.zip into /uploads/book: 2 files (9 bytes), 2 folders");
        assertThat(names()).contains("/uploads/book/ch1.md", "/uploads/book/notes/plot.md", "/uploads/book/empty");
        assertThat(ok(a -> tools.readFile(a, "/uploads/book/notes/plot.md", 0, 0))).isEqualTo("Plot");
        assertThat(names()).contains("/uploads/book.zip");
        assertNothingLeftBehind();
    }

    @Test
    void unpacksIntoTheFolderGiven() throws IOException {
        open();
        store("/data", entries("a.txt", "a"));

        assertThat(ok(a -> tools.unzipFile(a, "/data", "/out/here"))).startsWith("Unpacked /data into /out/here: 1 file");
        assertThat(names()).contains("/out/here/a.txt");
    }

    @Test
    void anArchiveWithoutZipInItsNeedsATarget() throws IOException {
        open();
        store("/data", entries("a.txt", "a"));

        assertThat(error(a -> tools.unzipFile(a, "/data", null))).contains("give the folder to unpack into as target");
    }

    @Test
    void leavesOutWhatTheWorkspaceDoesNotTakeAndSaysSo() throws IOException {
        open();
        store("/a.zip", entries("ok.txt", "text", "binary.bin", new byte[]{0, 1, 2, (byte) 0xff, 0}, "bad:name.txt", "x",
                "__MACOSX/._ok.txt", new byte[]{0, 5, 0}));

        String result = ok(a -> tools.unzipFile(a, "/a.zip", null));

        assertThat(result).startsWith("Unpacked /a.zip into /a: 1 file (4 bytes), 0 folders\nLeft out 3 entries:")
                .contains("- binary.bin (not a text file (UTF-8), an image or a zip file)")
                .contains("- bad:name.txt (the name is not allowed here)");
        assertThat(names()).doesNotContain("binary.bin");
    }

    @Test
    void aZipFileInsideIsStoredAndNotUnpacked() throws IOException {
        open();
        byte[] inner = zip(entries("deep.txt", "deep"));
        store("/outer.zip", entries("inner.zip", inner, "top.txt", "top"));

        ok(a -> tools.unzipFile(a, "/outer.zip", null));

        assertThat(names()).contains("/outer/inner.zip", "/outer/top.txt").doesNotContain("/outer/deep.txt");
        assertThat(fs.read(fs.parse("/outer/inner.zip"))).isEqualTo(inner);
    }

    @Test
    void anExistingTargetAndAMissingOrWrongFileAreErrors() throws IOException {
        open();
        store("/a.zip", entries("a.txt", "a"));
        ok(a -> tools.writeFile(a, "/a/keep.txt", "keep"));
        ok(a -> tools.writeFile(a, "/notes.zip", "not a zip"));

        assertThat(error(a -> tools.unzipFile(a, "/a.zip", null))).contains("/a exists already");
        assertThat(error(a -> tools.unzipFile(a, "/missing.zip", null))).contains("There is no file /missing.zip");
        assertThat(error(a -> tools.unzipFile(a, "/notes.zip", null))).contains("is not a zip file");
        assertThat(error(a -> tools.unzipFile(a, "/a", "/b"))).contains("is a folder, not a zip file");
        assertThat(ok(a -> tools.readFile(a, "/a/keep.txt", 0, 0))).isEqualTo("keep");
    }

    @Test
    void aDamagedArchiveIsAnErrorAndLeavesNothing() throws IOException {
        open();
        byte[] bytes = zip(entries("a.txt", "a".repeat(500), "b.txt", "b".repeat(500)));
        // the start is a zip, the end (with the directory of the entries) is gone
        fs.writeArchive(fs.parse("/broken.zip"), Arrays.copyOf(bytes, bytes.length / 2));

        assertThat(error(a -> tools.unzipFile(a, "/broken.zip", null))).contains("not a valid zip file or is damaged");
        assertThat(names()).isEqualTo("/broken.zip");
        assertNothingLeftBehind();
    }

    // ---------------------------------------------------------------------------------------------------------------
    // the archive is not trusted
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void anEntryThatLeadsAboveTheFolderRefusesTheWholeArchive() throws IOException {
        open();
        store("/slip.zip", entries("fine.txt", "fine", "../evil.txt", "evil"));

        assertThat(error(a -> tools.unzipFile(a, "/slip.zip", null))).contains("leads above the folder");
        assertThat(names()).isEqualTo("/slip.zip");
        assertThat(Files.exists(folder.workspace().resolve("evil.txt"))).isFalse();
        assertThat(Files.exists(folder.workspace().getParent().resolve("evil.txt"))).isFalse();
        assertNothingLeftBehind();
    }

    @Test
    void aDeeperTraversalAndAnAbsoluteNameAreRefusedToo() throws IOException {
        open();
        store("/deep.zip", entries("a/../../evil.txt", "evil"));
        store("/abs.zip", entries("/etc/evil.txt", "evil"));

        assertThat(error(a -> tools.unzipFile(a, "/deep.zip", null))).contains("leads above the folder");
        assertThat(error(a -> tools.unzipFile(a, "/abs.zip", null))).contains("absolute path");
        assertThat(names()).isEqualTo("/abs.zip\n/deep.zip");
    }

    @Test
    void aBackslashIsNotASeparatorSoTheEntryIsOnlyLeftOut() throws IOException {
        open();
        store("/win.zip", entries("ok.txt", "ok", "..\\..\\evil.txt", "evil"));

        String result = ok(a -> tools.unzipFile(a, "/win.zip", null));

        assertThat(result).contains("Left out 1 entry:").contains("(the name is not allowed here)");
        assertThat(names()).doesNotContain("evil");
    }

    @Test
    void aZipBombIsRefusedByTheBytesItUnpacksTo() throws IOException {
        open();
        // 2 MB of one byte packs to about 2 KB; the workspace here holds 1 MB
        store("/bomb.zip", entries("zeros.txt", "0".repeat(2_000_000)));

        assertThat(error(a -> tools.unzipFile(a, "/bomb.zip", null))).contains("zip bomb");
        assertThat(names()).isEqualTo("/bomb.zip");
        assertNothingLeftBehind();
    }

    @Test
    void aZipBombIsRefusedByTheRatioOfTheEntryAlsoWhenTheWorkspaceIsLarge() throws IOException {
        open(new WorkspaceLimits(10_000_000, 100_000_000, 100));
        store("/bomb.zip", entries("zeros.txt", "0".repeat(2_000_000)));

        assertThat(error(a -> tools.unzipFile(a, "/bomb.zip", null))).contains("zip bomb").contains("times");
        assertThat(names()).isEqualTo("/bomb.zip");
    }

    @Test
    void manyEntriesThatAddUpToABombAreRefusedByTheTotal() throws IOException {
        open();
        // none of them is kept (not text), so the room of the workspace is never used; they pack like a text does, and
        // together they are more than the 1 MB the whole workspace can hold
        Map<String, byte[]> many = new LinkedHashMap<>();
        for (int i = 0; i < 12; i++) {
            byte[] part = words(90_000, i);
            part[0] = 0;
            many.put("part" + i + ".bin", part);
        }
        store("/parts.zip", many);

        assertThat(error(a -> tools.unzipFile(a, "/parts.zip", null))).contains("more than the whole workspace can hold")
                .contains("zip bomb");
        assertThat(names()).isEqualTo("/parts.zip");
        assertNothingLeftBehind();
    }

    @Test
    void anArchiveWithTooManyFilesForTheWorkspaceIsRefusedWhole() throws IOException {
        open(new WorkspaceLimits(100_000, 1_000_000, 5));
        store("/many.zip", entries("1.txt", "1", "2.txt", "2", "3.txt", "3", "4.txt", "4", "5.txt", "5"));

        // the archive itself is one of the five files
        assertThat(error(a -> tools.unzipFile(a, "/many.zip", null))).contains("more files than the workspace has room for (4 more)");
        assertThat(names()).isEqualTo("/many.zip");
        assertNothingLeftBehind();
    }

    @Test
    void anArchiveWithTooManyEntriesIsRefused() throws IOException {
        open(new WorkspaceLimits(100_000, 1_000_000, 100));
        Map<String, byte[]> many = new LinkedHashMap<>();
        for (int i = 0; i <= ZipExtractorLimits.MAX_ENTRIES; i++) {
            many.put("d" + i + "/", new byte[0]);
        }
        store("/folders.zip", many);

        assertThat(error(a -> tools.unzipFile(a, "/folders.zip", null))).contains("entries, at most");
    }

    @Test
    void twoNamesThatDifferOnlyInCaseAreRefused() throws IOException {
        open();
        store("/case.zip", entries("Notes.md", "a", "notes.md", "b"));

        assertThat(error(a -> tools.unzipFile(a, "/case.zip", null))).contains("in it twice");
        assertThat(names()).isEqualTo("/case.zip");
    }

    @Test
    void aNameThatIsAFileAndAFolderIsRefused() throws IOException {
        open();
        store("/mix.zip", entries("a", "file", "a/b.txt", "inside"));

        assertThat(error(a -> tools.unzipFile(a, "/mix.zip", null))).contains("both a file and a folder");
        assertThat(names()).isEqualTo("/mix.zip");
    }

    @Test
    void aFileTooDeepOrWithAReservedNameIsLeftOut() throws IOException {
        open();
        store("/deep.zip", entries("ok.txt", "ok", "a/b/c/d/e/f/g/h/i/j/k/l/m.txt", "deep", ".marginalia-tmp-x", "t", "con.txt", "c"));

        String result = ok(a -> tools.unzipFile(a, "/deep.zip", null));

        assertThat(result).contains("1 file").contains("Left out 3 entries");
        assertThat(names()).contains("/deep/ok.txt").doesNotContain("/deep/con.txt");
    }

    @Test
    void anArchiveWithNothingToKeepIsAnError() throws IOException {
        open();
        store("/bin.zip", entries("a.bin", new byte[]{0, 1, 2}));

        assertThat(error(a -> tools.unzipFile(a, "/bin.zip", null))).startsWith("Nothing could be unpacked from /bin.zip")
                .contains("a.bin");
        assertThat(names()).isEqualTo("/bin.zip");
    }

    @Test
    void theArchiveCannotBeUnpackedOverTheReadOnlyMounts() throws IOException {
        open();
        store("/a.zip", entries("a.txt", "a"));

        assertThat(error(a -> tools.unzipFile(a, "/a.zip", "/checkpoints/x"))).contains("read only");
    }

    /**
     * The limit of entries of {@code ZipExtractor}, which is not public.
     */
    private static final class ZipExtractorLimits {
        static final int MAX_ENTRIES = 5_000;
    }
}
