package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.service.agent.*;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

        assertThat(ok(a -> tools.listFiles(a, null, false, null))).isEqualTo("/checkpoints/  (folder)\n/notes/  (folder)\n/readme.md  (1 bytes)");
        assertThat(ok(a -> tools.listFiles(a, "/notes", false, null))).isEqualTo("/notes/plot.md  (5 bytes)");
        assertThat(ok(a -> tools.listFiles(a, "/", true, null))).isEqualTo("/checkpoints/  (folder)\n/notes/  (folder)\n/notes/plot.md  (5 bytes)\n/readme.md  (1 bytes)");
        assertThat(ok(a -> tools.listFiles(a, "/checkpoints", false, null))).isEqualTo("/checkpoints is empty");
        assertThat(error(a -> tools.listFiles(a, "/missing", false, null))).contains("no folder");
        assertThat(error(a -> tools.listFiles(a, "/readme.md", false, null))).contains("not a folder");
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

        String listing = ok(a -> tools.listFiles(a, "/", true, null));

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
        assertThat(error(a -> tools.grepFiles(a, "(a+)+\\1$", "/", true, false, null))).contains("too long");

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

        assertThat(ok(a -> tools.grepFiles(a, "dragon", null, false, false, null)))
                .isEqualTo("/notes/a.md:1: The dragon sleeps.\n/notes/a.md:2: No dragons here (really)");
        assertThat(ok(a -> tools.grepFiles(a, "dragon", "/", false, true, null)))
                .isEqualTo("/notes/deep/b.md:1: Dragon fire\n/notes/a.md:1: The dragon sleeps.\n/notes/a.md:2: No dragons here (really)");
        assertThat(ok(a -> tools.grepFiles(a, "(really)", "/notes/a.md", false, false, null))).isEqualTo("/notes/a.md:2: No dragons here (really)");
        assertThat(ok(a -> tools.grepFiles(a, "fire", "/notes/deep", false, false, null))).isEqualTo("/notes/deep/b.md:1: Dragon fire");
        assertThat(ok(a -> tools.grepFiles(a, "unicorn", null, false, false, null))).isEqualTo("No match for \"unicorn\" in /");
    }

    @Test
    void searchesFilesByRegularExpression() {
        write("/a.md", "Chapter 1\nchapter two\nChapter 12\n");

        assertThat(ok(a -> tools.grepFiles(a, "^Chapter \\d+$", null, true, false, null))).isEqualTo("/a.md:1: Chapter 1\n/a.md:3: Chapter 12");
        assertThat(ok(a -> tools.grepFiles(a, "(?i)^chapter", null, true, false, null))).hasLineCount(3);
        assertThat(error(a -> tools.grepFiles(a, "(", null, true, false, null))).contains("not valid");
        assertThat(error(a -> tools.grepFiles(a, "", null, false, false, null))).contains("empty");
        assertThat(error(a -> tools.grepFiles(a, "x", "/missing", false, false, null))).contains("nothing at /missing");
    }

    @Test
    void grepIsBoundedAndSkipsWhatIsNotText() throws IOException {
        StringBuilder many = new StringBuilder();
        for (int i = 0; i < 250; i++) {
            many.append("match ").append(i).append(i == 3 ? "x".repeat(1_000) : "").append('\n');
        }
        write("/many.md", many.toString());
        Files.write(folder.workspace().resolve("image.bin"), new byte[]{(byte) 0xff, (byte) 0xfe, 'm', 'a', 't', 'c', 'h'});

        String found = ok(a -> tools.grepFiles(a, "match", null, false, false, null));

        assertThat(found.lines().count()).isEqualTo(AgentFileToolsImpl.MAX_MATCHES + 1);
        assertThat(found).endsWith("narrow the search]").doesNotContain("image.bin");
        String cut = found.lines().filter(l -> l.contains("xxxx")).findFirst().orElseThrow();
        assertThat(cut).hasSizeLessThan(400).endsWith("...");
    }

    @Test
    void grepStopsWhenTheCallIsCancelled() {
        write("/a.md", "match\n");
        Answer answer = new Answer(true);

        tools.grepFiles(answer, "match", null, false, false, null);

        assertThat(answer.result).startsWith("No match");
    }

    @Test
    void grepDoesNotListTheCheckpoints() throws IOException {
        Files.createDirectories(folder.checkpoints());
        Files.writeString(folder.checkpoints().resolve("0001.json"), "{\"match\":1}");

        assertThat(ok(a -> tools.grepFiles(a, "match", null, false, false, null))).startsWith("No match");
        // listed only, not read: nothing to search in
        assertThat(ok(a -> tools.grepFiles(a, "match", "/checkpoints", false, false, null))).startsWith("No match");
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

        collect.accept(a -> tools.listFiles(a, "/", true, null));
        collect.accept(a -> tools.listFiles(a, "/link", false, null));
        collect.accept(a -> tools.readFile(a, "/link", 0, 0));
        collect.accept(a -> tools.readFile(a, "/../../..", 0, 0));
        collect.accept(a -> tools.readFile(a, "/notes", 0, 0));
        collect.accept(a -> tools.writeFile(a, "/link/x", "x"));
        collect.accept(a -> tools.grepFiles(a, "text", "/", false, false, null));
        collect.accept(a -> tools.grepFiles(a, "text", "/link", false, false, null));
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

    // ---------------------------------------------------------------------------------------------------------------
    // patterns of names
    // ---------------------------------------------------------------------------------------------------------------

    private void notesTree() {
        write("/readme.md", "r");
        write("/notes/plot.md", "dragon plot\n");
        write("/notes/Plot-2.MD", "second dragon\n");
        write("/notes/ideas.txt", "dragon idea\n");
        write("/notes/deep/chapter-1.md", "dragon here\n");
        write("/notes/deep/chapter-2.md", "nothing\n");
    }

    @Test
    void listsTheFilesWhoseNameMatchesAtAnyDepth() {
        notesTree();

        assertThat(ok(a -> tools.listFiles(a, null, false, "*.md")))
                .isEqualTo("/notes/deep/chapter-1.md  (12 bytes)\n/notes/deep/chapter-2.md  (8 bytes)\n"
                        + "/notes/Plot-2.MD  (14 bytes)\n/notes/plot.md  (12 bytes)\n/readme.md  (1 bytes)");
        assertThat(ok(a -> tools.listFiles(a, "/notes/deep", false, "chapter-?.md"))).hasLineCount(2);
        assertThat(ok(a -> tools.listFiles(a, "/notes", false, "*.{txt,MD}")))
                .isEqualTo("/notes/deep/chapter-1.md  (12 bytes)\n/notes/deep/chapter-2.md  (8 bytes)\n"
                        + "/notes/ideas.txt  (12 bytes)\n/notes/Plot-2.MD  (14 bytes)\n/notes/plot.md  (12 bytes)");
        assertThat(ok(a -> tools.listFiles(a, "/notes", false, "chapter-[!2]*"))).isEqualTo("/notes/deep/chapter-1.md  (12 bytes)");
    }

    @Test
    void aPatternWithASlashIsMatchedAgainstThePathBelowTheFolder() {
        notesTree();

        assertThat(ok(a -> tools.listFiles(a, "/notes", false, "deep/*.md"))).hasLineCount(2);
        assertThat(ok(a -> tools.listFiles(a, "/", false, "notes/*.md"))).isEqualTo("/notes/Plot-2.MD  (14 bytes)\n/notes/plot.md  (12 bytes)");
        assertThat(ok(a -> tools.listFiles(a, "/", false, "**/chapter-*"))).hasLineCount(2);
        assertThat(ok(a -> tools.listFiles(a, "/", false, "deep"))).isEqualTo("/notes/deep/  (folder)");
    }

    @Test
    void aPatternThatMatchesNothingSaysSo() {
        notesTree();

        assertThat(ok(a -> tools.listFiles(a, "/notes", false, "*.pdf"))).isEqualTo("Nothing below /notes matches *.pdf");
        assertThat(error(a -> tools.listFiles(a, "/notes", false, "[abc"))).contains("[ without a ]").doesNotContain("Exception");
        assertThat(error(a -> tools.listFiles(a, "/notes", false, "{a,b"))).contains("{ without a }");
        assertThat(error(a -> tools.listFiles(a, "/notes", false, "x".repeat(300)))).contains("longer");
    }

    @Test
    void grepSearchesOnlyTheFilesThatMatchThePattern() {
        notesTree();

        assertThat(ok(a -> tools.grepFiles(a, "dragon", "/", false, false, "*.md")))
                .isEqualTo("/notes/deep/chapter-1.md:1: dragon here\n/notes/Plot-2.MD:1: second dragon\n/notes/plot.md:1: dragon plot");
        assertThat(ok(a -> tools.grepFiles(a, "dragon", "/notes", false, false, "*.txt"))).isEqualTo("/notes/ideas.txt:1: dragon idea");
        assertThat(ok(a -> tools.grepFiles(a, "dragon", "/", false, false, "*.pdf")))
                .isEqualTo("No match for \"dragon\" in / (no file matches *.pdf)");
        // a single file is searched whatever the pattern
        assertThat(ok(a -> tools.grepFiles(a, "dragon", "/notes/ideas.txt", false, false, "*.md"))).isEqualTo("/notes/ideas.txt:1: dragon idea");
    }

    // ---------------------------------------------------------------------------------------------------------------
    // copy
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void copiesAFileAndLeavesTheOriginal() {
        write("/a.md", "alpha\n");

        assertThat(ok(a -> tools.copyFile(a, "/a.md", "/backup/a-copy.md"))).isEqualTo("Copied /a.md to /backup/a-copy.md (1 file, 6 bytes)");

        assertThat(fsText("/a.md")).isEqualTo("alpha\n");
        assertThat(fsText("/backup/a-copy.md")).isEqualTo("alpha\n");
    }

    @Test
    void copiesAFolderWithEverythingInIt() {
        notesTree();

        assertThat(ok(a -> tools.copyFile(a, "/notes", "/snapshots/before")))
                .isEqualTo("Copied /notes to /snapshots/before (5 files, 58 bytes)");

        assertThat(fsText("/snapshots/before/deep/chapter-1.md")).isEqualTo("dragon here\n");
        assertThat(ok(a -> tools.listFiles(a, "/notes", true, null))).hasLineCount(6);
        assertThat(ok(a -> tools.listFiles(a, "/snapshots/before", true, null))).hasLineCount(6);
    }

    @Test
    void aCopyThatCannotBeMadeChangesNothing() {
        notesTree();
        String before = ok(a -> tools.listFiles(a, "/", true, null));

        assertThat(error(a -> tools.copyFile(a, "/notes", "/notes/inside"))).contains("into itself");
        assertThat(error(a -> tools.copyFile(a, "/notes", "/notes"))).contains("the same");
        assertThat(error(a -> tools.copyFile(a, "/notes/plot.md", "/readme.md"))).contains("exists already");
        assertThat(error(a -> tools.copyFile(a, "/missing", "/other"))).contains("nothing at /missing");
        assertThat(error(a -> tools.copyFile(a, "/", "/other"))).contains("workspace itself");
        assertThat(error(a -> tools.copyFile(a, "/notes", "/checkpoints/notes"))).contains("read only");
        assertThat(error(a -> tools.copyFile(a, "/checkpoints", "/notes2"))).contains("can't be copied");

        assertThat(ok(a -> tools.listFiles(a, "/", true, null))).isEqualTo(before);
    }

    @Test
    void aCopyThatDoesNotFitTheLimitsIsNotMadeAtAll() {
        fs = VirtualFileSystem.ofRun(folder, new WorkspaceLimits(100, 100, 10), new ReentrantLock());
        tools = new AgentFileToolsImpl(fs);
        write("/d/a.md", "x".repeat(40));
        write("/d/b.md", "y".repeat(40));
        assertThat(error(a -> tools.copyFile(a, "/d", "/e"))).contains("larger than its limit");

        fs = VirtualFileSystem.ofRun(folder, new WorkspaceLimits(100, 1000, 3), new ReentrantLock());
        tools = new AgentFileToolsImpl(fs);
        write("/d/c.md", "z");
        assertThat(error(a -> tools.copyFile(a, "/d", "/e"))).contains("6 files").contains("room for 3");

        assertThat(error(a -> tools.listFiles(a, "/e", false, null))).contains("no folder");
        assertThat(ok(a -> tools.listFiles(a, "/", true, null))).doesNotContain("/e");
    }

    @Test
    void aCopyCannotGoDeeperThanAPathMay() {
        String deep = "/a/b/c/d/e/f/g/h";
        write(deep + "/note.md", "x");

        assertThat(error(a -> tools.copyFile(a, "/a", "/1/2/3/4/a"))).contains("deeper than");
        assertThat(ok(a -> tools.listFiles(a, "/", false, null))).doesNotContain("/1");
    }

    // ---------------------------------------------------------------------------------------------------------------
    // the results of the commands
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void aCommandResultIsSavedToAFileWithoutTheModelWritingItOut() {
        CommandResults results = new CommandResults();
        tools = new AgentFileToolsImpl(fs, results);
        results.add("generate_lore_entry", "prompt=Dragons", "The dragons\nsleep.");
        results.add("get_part", "part=3", "Part three.");

        assertThat(ok(a -> tools.saveCommandResult(a, "/drafts/part.md", null, false)))
                .isEqualTo("Saved the result #2 of get_part (11 bytes) as /drafts/part.md");
        assertThat(read("/drafts/part.md")).isEqualTo("Part three.");
        assertThat(ok(a -> tools.saveCommandResult(a, "/drafts/dragons.md", 1, false))).startsWith("Saved the result #1 of generate_lore_entry");
        assertThat(fsText("/drafts/dragons.md")).isEqualTo("The dragons\nsleep.");

        assertThat(ok(a -> tools.saveCommandResult(a, "/drafts/dragons.md", 2, true)))
                .isEqualTo("Added the result #2 of get_part (11 bytes) to /drafts/dragons.md");
        assertThat(fsText("/drafts/dragons.md")).isEqualTo("The dragons\nsleep.Part three.");
        assertThat(ok(a -> tools.saveCommandResult(a, "/drafts/dragons.md", 1, false)).length()).isPositive();
        assertThat(fsText("/drafts/dragons.md")).as("replaced").isEqualTo("The dragons\nsleep.");
    }

    @Test
    void aResultReferenceIsTheContentToWrite() {
        CommandResults results = new CommandResults();
        tools = new AgentFileToolsImpl(fs, results);
        results.add("a", "", "First.");
        results.add("b", "", "Second.");

        ok(a -> tools.writeFile(a, "/n.md", "@result:1"));
        ok(a -> tools.appendFile(a, "/n.md", "@result"));

        assertThat(fsText("/n.md")).isEqualTo("First.Second.");
        assertThat(error(a -> tools.writeFile(a, "/n.md", "@result:9"))).isEqualTo(
                "The argument content: There is no result #9, list_command_results shows the ones that are kept");
    }

    @Test
    void theResultsAreListedNewestFirstWithTheirCalls() {
        CommandResults results = new CommandResults();
        tools = new AgentFileToolsImpl(fs, results);
        assertThat(ok(a -> tools.listCommandResults(a))).isEqualTo("No command result is kept yet");

        results.add("generate_lore_entry", "prompt=Dragons, entry=2", "x".repeat(4321));
        results.add("get_part", "part=3", "Part three.");

        assertThat(ok(a -> tools.listCommandResults(a))).isEqualTo("""
                #2 get_part(part=3): 11 characters
                #1 generate_lore_entry(prompt=Dragons, entry=2): 4321 characters
                The newest is first, the last 10 are kept.""");
    }

    @Test
    void savingAResultFailsWithTheReason() {
        assertThat(error(a -> tools.saveCommandResult(a, "/x.md", null, false))).isEqualTo("There is no result to save yet: call a command first");

        CommandResults results = new CommandResults();
        tools = new AgentFileToolsImpl(fs, results);
        assertThat(error(a -> tools.saveCommandResult(a, "/x.md", null, false))).contains("no result to save yet");
        results.add("get_part", "", "text");
        assertThat(error(a -> tools.saveCommandResult(a, "/x.md", 7, false))).isEqualTo("There is no result #7, list_command_results shows the ones that are kept");
        assertThat(error(a -> tools.saveCommandResult(a, "/a*.md", null, false))).isNotBlank();
        assertThat(error(a -> tools.saveCommandResult(a, "/checkpoints/x.md", null, false))).contains("read only");

        fs = VirtualFileSystem.ofRun(folder, new WorkspaceLimits(3, 1_000, 10), new ReentrantLock());
        tools = new AgentFileToolsImpl(fs, results);
        assertThat(error(a -> tools.saveCommandResult(a, "/x.md", null, false))).contains("the limit of a file is 3 bytes");
        assertThat(ok(a -> tools.listFiles(a, "/", true, null))).doesNotContain("x.md");
    }

    // ---------------------------------------------------------------------------------------------------------------
    // merge
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void joinsFilesInTheOrderGivenWithADelimiter() {
        write("/one.md", "one");
        write("/two.md", "two");
        write("/three.md", "three");

        assertThat(ok(a -> tools.mergeFiles(a, List.of("/three.md", "/one.md", "/two.md"), "/out/all.md", "\n---\n")))
                .isEqualTo("Joined 3 files into /out/all.md (21 bytes)");
        assertThat(fsText("/out/all.md")).isEqualTo("three\n---\none\n---\ntwo");

        ok(a -> tools.mergeFiles(a, List.of("/one.md", "/two.md"), "/out/all.md", null));
        assertThat(fsText("/out/all.md")).isEqualTo("one\ntwo");
        ok(a -> tools.mergeFiles(a, List.of("/one.md"), "/out/all.md", ""));
        assertThat(fsText("/out/all.md")).isEqualTo("one");
    }

    @Test
    void aPatternInTheLastNameStandsForTheFilesOfAFolderInTheOrderOfTheirNames() {
        write("/ch/10.md", "ten");
        write("/ch/2.md", "two");
        write("/ch/1.md", "one");
        write("/ch/notes.txt", "no");
        write("/ch/sub/3.md", "deep");

        ok(a -> tools.mergeFiles(a, List.of("/ch/*.md"), "/book.md", "|"));

        assertThat(fsText("/book.md")).isEqualTo("one|ten|two");
    }

    @Test
    void theTargetIsLeftOutOfAPatternSoAMergeCanBeRepeated() {
        write("/ch/1.md", "one");
        write("/ch/2.md", "two");

        ok(a -> tools.mergeFiles(a, List.of("/ch/*.md"), "/ch/all.md", "+"));
        ok(a -> tools.mergeFiles(a, List.of("/ch/*.md"), "/ch/all.md", "+"));

        assertThat(fsText("/ch/all.md")).isEqualTo("one+two");
    }

    @Test
    void aMergeThatCannotBeMadeWritesNothing() {
        write("/one.md", "one");
        write("/target.md", "keep");
        write("/dir/x.md", "x");

        assertThat(error(a -> tools.mergeFiles(a, List.of("/one.md", "/missing.md"), "/target.md", null))).contains("/missing.md");
        assertThat(error(a -> tools.mergeFiles(a, List.of("/one.md", "/dir"), "/target.md", null))).contains("folder");
        assertThat(error(a -> tools.mergeFiles(a, List.of("/none/*.md"), "/target.md", null))).contains("no folder");
        assertThat(error(a -> tools.mergeFiles(a, List.of("/dir/*.pdf"), "/target.md", null))).contains("matches *.pdf");
        assertThat(error(a -> tools.mergeFiles(a, List.of("/*/x.md"), "/target.md", null))).contains("last name");
        assertThat(error(a -> tools.mergeFiles(a, List.of(), "/target.md", null))).contains("paths is empty");
        assertThat(error(a -> tools.mergeFiles(a, null, "/target.md", null))).contains("paths is empty");
        assertThat(error(a -> tools.mergeFiles(a, List.of("/one.md"), "/checkpoints/x", null))).contains("read only");

        assertThat(fsText("/target.md")).isEqualTo("keep");
    }

    @Test
    void aMergeIsLimitedLikeAnyOtherFile() {
        write("/big1.md", "a".repeat(60_000));
        write("/big2.md", "b".repeat(60_000));

        assertThat(error(a -> tools.mergeFiles(a, List.of("/big1.md", "/big2.md"), "/out.md", null))).contains("larger than the limit");
        assertThat(error(a -> tools.mergeFiles(a, java.util.Collections.nCopies(101, "/big1.md"), "/out.md", null)))
                .contains("At most 100");
        assertThat(error(a -> tools.readFile(a, "/out.md", 0, 0))).isNotBlank();
    }

    private String fsText(String path) {
        return fs.readText(com.github.enerccio.marginalia.domain.service.agent.VirtualPath.parse(path));
    }
}
