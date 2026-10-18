package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.service.agent.*;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the model gets back from the file tools: bounded results, and errors it can act on.
 */
class AgentFileToolsTest {

    /**
     * The answer of a tool, which answers at once.
     */
    private static final class Answer implements FunctionContinuation<String> {
        private final boolean cancelled;
        String result;
        String error;

        Answer(boolean cancelled) {
            this.cancelled = cancelled;
        }

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
            return cancelled;
        }
    }

    @TempDir
    Path home;

    private RunFolder folder;
    private VirtualFileSystem fs;
    private AgentFileTools tools;

    @BeforeEach
    void open() {
        folder = RunFolder.of(home, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        fs = VirtualFileSystem.ofRun(folder, new WorkspaceLimits(100_000, 1_000_000, 100), new ReentrantLock());
        tools = new AgentFileToolsImpl(fs);
    }

    private String ok(Consumer<Answer> call) {
        Answer answer = new Answer(false);
        call.accept(answer);
        assertThat(answer.error).as("error").isNull();
        assertThat(answer.result).as("result").isNotNull();
        return answer.result;
    }

    private String error(Consumer<Answer> call) {
        Answer answer = new Answer(false);
        call.accept(answer);
        assertThat(answer.result).as("result").isNull();
        assertThat(answer.error).as("error").isNotNull();
        return answer.error;
    }

    private void write(String path, String content) {
        ok(a -> tools.writeFile(a, path, content));
    }

    private String read(String path) {
        return ok(a -> tools.readFile(a, path, 0, 0));
    }

    private static String lines(int count) {
        StringBuilder text = new StringBuilder();
        for (int i = 1; i <= count; i++) {
            text.append("line ").append(i).append('\n');
        }
        return text.toString();
    }

    // ---------------------------------------------------------------------------------------------------------------
    // write, read
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void writesAndReadsAFile() {
        assertThat(ok(a -> tools.writeFile(a, "/notes/plot.md", "A story\nin two lines\n"))).isEqualTo("Wrote /notes/plot.md (21 bytes)");

        assertThat(read("/notes/plot.md")).isEqualTo("A story\nin two lines");
        assertThat(ok(a -> tools.appendFile(a, "/notes/plot.md", "and a third\n"))).isEqualTo("Added 12 bytes to /notes/plot.md");
        assertThat(read("/notes/plot.md")).isEqualTo("A story\nin two lines\nand a third");
    }

    @Test
    void readsPartsOfAFileByLine() {
        write("/long.md", lines(50));

        assertThat(ok(a -> tools.readFile(a, "/long.md", 10, 12))).isEqualTo("line 10\nline 11\nline 12\n[lines 10-12 of 50; go on with start_line 13]");
        assertThat(ok(a -> tools.readFile(a, "/long.md", 49, 0))).isEqualTo("line 49\nline 50\n[lines 49-50 of 50]");
        assertThat(ok(a -> tools.readFile(a, "/long.md", 0, 2))).isEqualTo("line 1\nline 2\n[lines 1-2 of 50; go on with start_line 3]");
        assertThat(ok(a -> tools.readFile(a, "/long.md", 50, 99))).isEqualTo("line 50\n[lines 50-50 of 50]");
        assertThat(error(a -> tools.readFile(a, "/long.md", 51, 0))).contains("50 lines");
        assertThat(error(a -> tools.readFile(a, "/long.md", 10, 5))).contains("before");
    }

    @Test
    void aLongFileIsCutAtALineAndTheRestIsOneCallAway() {
        write("/long.md", lines(5_000));

        String first = read("/long.md");

        assertThat(first.length()).isLessThan(AgentFileToolsImpl.MAX_READ_CHARS + 200);
        assertThat(first).contains("line 1\nline 2\n").contains("the rest is cut, go on with start_line ");
        int next = Integer.parseInt(first.substring(first.lastIndexOf("start_line ") + 11, first.length() - 1));
        assertThat(first).contains("line " + (next - 1) + "\n").doesNotContain("line " + next + "\n");
        assertThat(ok(a -> tools.readFile(a, "/long.md", next, next))).startsWith("line " + next + "\n");
    }

    @Test
    void aSingleHugeLineIsCut() {
        write("/one.md", "x".repeat(60_000));

        assertThat(read("/one.md").length()).isLessThan(AgentFileToolsImpl.MAX_READ_CHARS + 200);
    }

    @Test
    void emptyFilesAreSaid() {
        write("/empty.md", "");

        assertThat(read("/empty.md")).isEqualTo("(the file is empty)");
    }

    @Test
    void errorsTellWhatToDo() {
        assertThat(error(a -> tools.readFile(a, "/missing.md", 0, 0))).contains("/missing.md");
        assertThat(error(a -> tools.readFile(a, "missing.md", 0, 0))).contains("absolute").contains("/notes/plot.md");
        assertThat(error(a -> tools.readFile(a, "", 0, 0))).contains("absolute");
        assertThat(error(a -> tools.writeFile(a, "/CON", "x"))).contains("reserved");
        assertThat(error(a -> tools.writeFile(a, "/big.md", "x".repeat(100_001)))).contains("limit");
    }

    @Test
    void aMissingContentIsAnEmptyFile() {
        ok(a -> tools.writeFile(a, "/none.md", null));

        assertThat(read("/none.md")).isEqualTo("(the file is empty)");
    }

    // ---------------------------------------------------------------------------------------------------------------
    // list
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void listsTheWorkspace() {
        write("/notes/plot.md", "12345");
        write("/readme.md", "x");

        assertThat(ok(a -> tools.listFiles(a, null, false))).isEqualTo("/checkpoints/  (folder)\n/notes/  (folder)\n/readme.md  (1 bytes)");
        assertThat(ok(a -> tools.listFiles(a, "/notes", false))).isEqualTo("/notes/plot.md  (5 bytes)");
        assertThat(ok(a -> tools.listFiles(a, "/", true))).isEqualTo("/checkpoints/  (folder)\n/notes/  (folder)\n/notes/plot.md  (5 bytes)\n/readme.md  (1 bytes)");
        assertThat(ok(a -> tools.listFiles(a, "/checkpoints", false))).isEqualTo("/checkpoints is empty");
        assertThat(error(a -> tools.listFiles(a, "/missing", false))).contains("no folder");
        assertThat(error(a -> tools.listFiles(a, "/readme.md", false))).contains("not a folder");
    }

    @Test
    void aLongListingIsCut() {
        for (int i = 0; i < 60; i++) {
            write("/f" + i + ".md", "x");
        }
        fs = VirtualFileSystem.ofRun(folder, new WorkspaceLimits(100_000, 1_000_000, 1000), new ReentrantLock());
        tools = new AgentFileToolsImpl(fs);
        for (int i = 60; i < 260; i++) {
            write("/f" + i + ".md", "x");
        }

        String listing = ok(a -> tools.listFiles(a, "/", true));

        assertThat(listing.lines().count()).isEqualTo(AgentFileToolsImpl.MAX_LISTED + 1);
        assertThat(listing).endsWith("list a folder below to see the rest]");
    }

    // ---------------------------------------------------------------------------------------------------------------
    // replace
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void replacesTextThatIsThereOnce() {
        write("/a.md", "The hero left.\nThe villain stayed.\n");

        assertThat(ok(a -> tools.replaceInFile(a, "/a.md", "villain stayed", "villain fled"))).isEqualTo("Replaced the text in /a.md");

        assertThat(read("/a.md")).isEqualTo("The hero left.\nThe villain fled.");
    }

    @Test
    void replacesAcrossLinesAndRemovesWithEmptyNewText() {
        write("/a.md", "one\ntwo\nthree\n");

        ok(a -> tools.replaceInFile(a, "/a.md", "one\ntwo\n", ""));

        assertThat(read("/a.md")).isEqualTo("three");
    }

    @Test
    void replaceNeedsExactlyOneMatch() {
        write("/a.md", "cat dog cat\n");

        assertThat(error(a -> tools.replaceInFile(a, "/a.md", "cat", "bird"))).contains("2 times").contains("matches once");
        assertThat(error(a -> tools.replaceInFile(a, "/a.md", "cow", "bird"))).contains("not in /a.md").contains("exactly");
        assertThat(error(a -> tools.replaceInFile(a, "/a.md", "", "bird"))).contains("empty");
        assertThat(error(a -> tools.replaceInFile(a, "/a.md", null, "bird"))).contains("empty");
        assertThat(read("/a.md")).isEqualTo("cat dog cat");
    }

    @Test
    void replaceInADollarAndBackslashIsLiteral() {
        write("/a.md", "price: X\n");

        ok(a -> tools.replaceInFile(a, "/a.md", "X", "$5 \\ $1"));

        assertThat(read("/a.md")).isEqualTo("price: $5 \\ $1");
    }

    @Test
    void replaceFindsTextOfAFileWithWindowsLineBreaks() throws IOException {
        Files.createDirectories(folder.workspace());
        Files.writeString(folder.workspace().resolve("win.md"), "one\r\ntwo\r\nthree\r\n");

        ok(a -> tools.replaceInFile(a, "/win.md", "one\ntwo", "1\n2"));

        assertThat(Files.readString(folder.workspace().resolve("win.md"))).isEqualTo("1\r\n2\r\nthree\r\n");
    }

    @Test
    void replaceOfAMissingFileFails() {
        assertThat(error(a -> tools.replaceInFile(a, "/missing.md", "a", "b"))).contains("/missing.md");
    }

    // ---------------------------------------------------------------------------------------------------------------
    // regular expressions
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void replacesByRegularExpression() {
        write("/a.md", "Chapter 1\nChapter 22\nIntro\n");

        assertThat(ok(a -> tools.regexReplaceInFile(a, "/a.md", "Chapter (\\d+)", "Part $1"))).isEqualTo("Replaced 2 matches in /a.md");
        assertThat(read("/a.md")).isEqualTo("Part 1\nPart 22\nIntro");
        assertThat(ok(a -> tools.regexReplaceInFile(a, "/a.md", "^Intro$", "Start"))).isEqualTo("Replaced 1 match in /a.md");
        assertThat(read("/a.md")).endsWith("Start");
        ok(a -> tools.regexReplaceInFile(a, "/a.md", "Part ", ""));
        assertThat(read("/a.md")).isEqualTo("1\n22\nStart");
    }

    @Test
    void regularExpressionErrorsAreForTheModel() {
        write("/a.md", "text\n");

        assertThat(error(a -> tools.regexReplaceInFile(a, "/a.md", "(unclosed", "x"))).contains("not valid");
        assertThat(error(a -> tools.regexReplaceInFile(a, "/a.md", "text", "$2"))).contains("replacement is not valid");
        assertThat(error(a -> tools.regexReplaceInFile(a, "/a.md", "nothing", "x"))).contains("matches nothing");
        assertThat(error(a -> tools.regexReplaceInFile(a, "/a.md", "", "x"))).contains("empty");
        assertThat(read("/a.md")).isEqualTo("text");
    }

    @Test
    void aRegularExpressionThatBacktracksForeverIsStopped() {
        // the back reference defeats the optimizations of the regular expression engine
        write("/a.md", "a".repeat(40) + "b");
        tools = new AgentFileToolsImpl(fs, 200);
        long start = System.nanoTime();

        assertThat(error(a -> tools.regexReplaceInFile(a, "/a.md", "(a+)+\\1$", "x"))).contains("too long");
        assertThat(error(a -> tools.grepFiles(a, "(a+)+\\1$", "/", true, false))).contains("too long");

        assertThat((System.nanoTime() - start) / 1_000_000).as("milliseconds").isLessThan(3_000);
        assertThat(read("/a.md")).hasSize(41);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // grep
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void searchesFilesForTextAsItIs() {
        write("/notes/a.md", "The dragon sleeps.\nNo dragons here (really)\n");
        write("/notes/deep/b.md", "Dragon fire\n");
        write("/c.md", "nothing\n");

        assertThat(ok(a -> tools.grepFiles(a, "dragon", null, false, false)))
                .isEqualTo("/notes/a.md:1: The dragon sleeps.\n/notes/a.md:2: No dragons here (really)");
        assertThat(ok(a -> tools.grepFiles(a, "dragon", "/", false, true)))
                .isEqualTo("/notes/deep/b.md:1: Dragon fire\n/notes/a.md:1: The dragon sleeps.\n/notes/a.md:2: No dragons here (really)");
        assertThat(ok(a -> tools.grepFiles(a, "(really)", "/notes/a.md", false, false))).isEqualTo("/notes/a.md:2: No dragons here (really)");
        assertThat(ok(a -> tools.grepFiles(a, "fire", "/notes/deep", false, false))).isEqualTo("/notes/deep/b.md:1: Dragon fire");
        assertThat(ok(a -> tools.grepFiles(a, "unicorn", null, false, false))).isEqualTo("No match for \"unicorn\" in /");
    }

    @Test
    void searchesFilesByRegularExpression() {
        write("/a.md", "Chapter 1\nchapter two\nChapter 12\n");

        assertThat(ok(a -> tools.grepFiles(a, "^Chapter \\d+$", null, true, false))).isEqualTo("/a.md:1: Chapter 1\n/a.md:3: Chapter 12");
        assertThat(ok(a -> tools.grepFiles(a, "(?i)^chapter", null, true, false))).hasLineCount(3);
        assertThat(error(a -> tools.grepFiles(a, "(", null, true, false))).contains("not valid");
        assertThat(error(a -> tools.grepFiles(a, "", null, false, false))).contains("empty");
        assertThat(error(a -> tools.grepFiles(a, "x", "/missing", false, false))).contains("nothing at /missing");
    }

    @Test
    void grepIsBoundedAndSkipsWhatIsNotText() throws IOException {
        StringBuilder many = new StringBuilder();
        for (int i = 0; i < 250; i++) {
            many.append("match ").append(i).append(i == 3 ? "x".repeat(1_000) : "").append('\n');
        }
        write("/many.md", many.toString());
        Files.write(folder.workspace().resolve("image.bin"), new byte[]{(byte) 0xff, (byte) 0xfe, 'm', 'a', 't', 'c', 'h'});

        String found = ok(a -> tools.grepFiles(a, "match", null, false, false));

        assertThat(found.lines().count()).isEqualTo(AgentFileToolsImpl.MAX_MATCHES + 1);
        assertThat(found).endsWith("narrow the search]").doesNotContain("image.bin");
        String cut = found.lines().filter(l -> l.contains("xxxx")).findFirst().orElseThrow();
        assertThat(cut).hasSizeLessThan(400).endsWith("...");
    }

    @Test
    void grepStopsWhenTheCallIsCancelled() {
        write("/a.md", "match\n");
        Answer answer = new Answer(true);

        tools.grepFiles(answer, "match", null, false, false);

        assertThat(answer.result).startsWith("No match");
    }

    @Test
    void grepDoesNotListTheCheckpoints() throws IOException {
        Files.createDirectories(folder.checkpoints());
        Files.writeString(folder.checkpoints().resolve("0001.json"), "{\"match\":1}");

        assertThat(ok(a -> tools.grepFiles(a, "match", null, false, false))).startsWith("No match");
        // listed only, not read: nothing to search in
        assertThat(ok(a -> tools.grepFiles(a, "match", "/checkpoints", false, false))).startsWith("No match");
    }

    // ---------------------------------------------------------------------------------------------------------------
    // delete
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void movesFilesAndFolders() {
        write("/a.md", "A");
        write("/dir/x.md", "X");
        write("/taken.md", "T");

        assertThat(ok(a -> tools.moveFile(a, "/a.md", "/notes/b.md"))).isEqualTo("Moved /a.md to /notes/b.md");
        assertThat(ok(a -> tools.moveFile(a, "/dir", "/notes/dir"))).isEqualTo("Moved /dir to /notes/dir");

        assertThat(read("/notes/b.md")).isEqualTo("A");
        assertThat(read("/notes/dir/x.md")).isEqualTo("X");
        assertThat(error(a -> tools.moveFile(a, "/notes/b.md", "/taken.md"))).contains("exists already");
        assertThat(error(a -> tools.moveFile(a, "/missing.md", "/x.md"))).contains("nothing at /missing.md");
        assertThat(error(a -> tools.moveFile(a, "/notes", "/notes/in"))).contains("into itself");
        assertThat(error(a -> tools.moveFile(a, "notes", "/x"))).contains("absolute");
        assertThat(error(a -> tools.moveFile(a, "/notes/b.md", ""))).contains("absolute");
        assertThat(read("/taken.md")).isEqualTo("T");
    }

    @Test
    void deletesFilesAndFolders() {
        write("/a/1.md", "x");
        write("/b.md", "x");

        assertThat(ok(a -> tools.deleteFile(a, "/b.md", false))).isEqualTo("Deleted /b.md");
        assertThat(error(a -> tools.deleteFile(a, "/a", false))).contains("not empty").contains("recursive");
        assertThat(ok(a -> tools.deleteFile(a, "/a", true))).isEqualTo("Deleted /a");
        assertThat(error(a -> tools.deleteFile(a, "/a", true))).contains("nothing at /a");
        assertThat(error(a -> tools.deleteFile(a, "/", true))).contains("workspace");
        assertThat(error(a -> tools.deleteFile(a, "/checkpoints/0001.json", false))).contains("read only");
    }

    // ---------------------------------------------------------------------------------------------------------------
    // what the model never gets
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void noResultOrErrorHasARealPath() throws IOException {
        write("/notes/a.md", "text\n");
        Files.createSymbolicLink(folder.workspace().resolve("link"), home);
        String[] real = {home.toString(), home.toRealPath().toString(), folder.workspace().getFileName().toString()};
        java.util.List<String> texts = new java.util.ArrayList<>();
        Consumer<Consumer<Answer>> collect = call -> {
            Answer answer = new Answer(false);
            call.accept(answer);
            texts.add(String.valueOf(answer.result) + String.valueOf(answer.error));
        };

        collect.accept(a -> tools.listFiles(a, "/", true));
        collect.accept(a -> tools.listFiles(a, "/link", false));
        collect.accept(a -> tools.readFile(a, "/link", 0, 0));
        collect.accept(a -> tools.readFile(a, "/../../..", 0, 0));
        collect.accept(a -> tools.readFile(a, "/notes", 0, 0));
        collect.accept(a -> tools.writeFile(a, "/link/x", "x"));
        collect.accept(a -> tools.grepFiles(a, "text", "/", false, false));
        collect.accept(a -> tools.grepFiles(a, "text", "/link", false, false));
        collect.accept(a -> tools.replaceInFile(a, "/link", "a", "b"));
        collect.accept(a -> tools.regexReplaceInFile(a, "/link", "a", "b"));
        collect.accept(a -> tools.deleteFile(a, "/link", true));
        collect.accept(a -> tools.moveFile(a, "/link", "/x"));
        collect.accept(a -> tools.moveFile(a, "/notes/a.md", "/link/a.md"));

        assertThat(texts).hasSize(13);
        for (String text : texts) {
            for (String path : real) {
                assertThat(text).doesNotContain(path);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // a file as the content
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void writeFileCopiesAnotherFileWhenTheContentIsAReference() {
        write("/parts/one.md", "the first part\n");

        String result = ok(a -> tools.writeFile(a, "/book.md", "@file:/parts/one.md"));

        assertThat(result).contains("/book.md");
        assertThat(read("/book.md")).contains("the first part");
        assertThat(read("/parts/one.md")).contains("the first part");
    }

    @Test
    void appendFileAddsAnotherFileToTheEnd() {
        write("/book.md", "start\n");
        write("/parts/one.md", "one\n");
        write("/parts/two.md", "two\n");

        ok(a -> tools.appendFile(a, "/book.md", "@file:/parts/one.md"));
        ok(a -> tools.appendFile(a, "/book.md", "@file:/parts/two.md"));

        assertThat(fsText("/book.md")).isEqualTo("start\none\ntwo\n");
    }

    @Test
    void aReferenceToAFileThatIsNotThereIsAnErrorAndNothingIsWritten() {
        String message = error(a -> tools.writeFile(a, "/book.md", "@file:/nope.md"));

        assertThat(message).startsWith("The argument content: ").contains("/nope.md");
        assertThat(error(a -> tools.readFile(a, "/book.md", 0, 0))).isNotBlank();
    }

    @Test
    void aTextThatOnlyContainsTheReferenceIsWrittenAsItIs() {
        write("/parts/one.md", "one\n");

        write("/note.md", "see @file:/parts/one.md");

        assertThat(fsText("/note.md")).isEqualTo("see @file:/parts/one.md");
    }

    @Test
    void theContentIsReadBeforeTheFileIsReplacedSoAFileCanBeRewrittenFromItself() {
        write("/a.md", "again ");

        ok(a -> tools.appendFile(a, "/a.md", "@file:/a.md"));
        assertThat(fsText("/a.md")).isEqualTo("again again ");

        ok(a -> tools.writeFile(a, "/a.md", "@file:/a.md"));
        assertThat(fsText("/a.md")).isEqualTo("again again ");
    }

    private String fsText(String path) {
        return fs.readText(com.github.enerccio.marginalia.domain.service.agent.VirtualPath.parse(path));
    }
}
