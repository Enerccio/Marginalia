package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.service.search.replace.TextSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * {@link AgentFileTools} over a {@link VirtualFileSystem}. Everything the model gets back is bounded here, and
 * everything that goes wrong is turned into a text for the model: the message of a {@link VirtualFileException} as it
 * is (it names virtual paths only), anything else as a plain "failed" with the details in the log only, so no message
 * of the Java runtime, which carries real paths, reaches the model.
 */
public final class AgentFileToolsImpl implements AgentFileTools {

    private static final Logger log = LoggerFactory.getLogger(AgentFileToolsImpl.class);

    /**
     * Longest text read_file returns, in characters.
     */
    public static final int MAX_READ_CHARS = 20_000;
    public static final int MAX_LISTED = 200;
    public static final int MAX_MATCHES = 100;
    public static final int MAX_MATCH_LINE = 300;
    // files looked at by one grep
    public static final int MAX_SEARCHED = 1_000;
    // how long the regular expressions of one call may run, unless told otherwise
    public static final long REGEX_MILLIS = 3_000;

    private final VirtualFileSystem fs;
    private final long regexMillis;

    public AgentFileToolsImpl(VirtualFileSystem fs) {
        this(fs, REGEX_MILLIS);
    }

    /**
     * @param regexMillis how long the regular expressions of one call may run
     */
    public AgentFileToolsImpl(VirtualFileSystem fs, long regexMillis) {
        this.fs = fs;
        this.regexMillis = regexMillis;
    }

    private void run(FunctionContinuation<String> done, String tool, Supplier<String> action) {
        try {
            done.complete(action.get());
        } catch (VirtualFileException e) {
            if (e.getCause() != null) {
                log.warn("The file tool {} failed: {}", tool, e.getMessage(), e.getCause());
            }
            done.fail(e.getMessage());
        } catch (RuntimeException e) {
            log.warn("The file tool {} failed", tool, e);
            done.fail("The file operation failed");
        }
    }

    @Override
    public void listFiles(FunctionContinuation<String> done, String path, boolean recursive) {
        run(done, "list_files", () -> {
            VirtualPath folder = pathOrRoot(path);
            VirtualEntry entry = fs.stat(folder);
            if (entry == null) {
                throw new VirtualFileException("There is no folder " + folder);
            }
            if (!entry.directory()) {
                throw new VirtualFileException(folder + " is a file, not a folder");
            }
            List<VirtualEntry> entries = new ArrayList<>(fs.list(folder));
            if (recursive) {
                entries = fs.walk(folder, MAX_LISTED + 1);
                if (folder.isRoot()) {
                    entries.addAll(0, fs.list(folder).stream().filter(e -> e.directory() && !isWorkspaceEntry(e)).toList());
                }
            }
            if (entries.isEmpty()) {
                return folder + " is empty";
            }
            StringBuilder out = new StringBuilder();
            int shown = Math.min(entries.size(), MAX_LISTED);
            for (int i = 0; i < shown; i++) {
                VirtualEntry e = entries.get(i);
                out.append(e.path()).append(e.directory() ? "/  (folder)" : "  (" + e.size() + " bytes)").append('\n');
            }
            if (entries.size() > shown) {
                out.append("[more than ").append(MAX_LISTED).append(" entries, list a folder below to see the rest]\n");
            }
            return out.toString().stripTrailing();
        });
    }

    private boolean isWorkspaceEntry(VirtualEntry entry) {
        return !VirtualFileSystem.CHECKPOINTS.equals(entry.path().getFirst());
    }

    @Override
    public void readFile(FunctionContinuation<String> done, String path, int startLine, int endLine) {
        run(done, "read_file", () -> {
            VirtualPath file = fs.parse(path);
            String text = fs.readText(file);
            if (text.isEmpty()) {
                return "(the file is empty)";
            }
            String[] lines = text.split("\n", -1);
            int total = text.endsWith("\n") ? lines.length - 1 : lines.length;
            int from = Math.max(1, startLine);
            int to = endLine < 1 ? total : Math.min(endLine, total);
            if (from > total) {
                throw new VirtualFileException(file + " has " + total + " lines, there is no line " + from);
            }
            if (to < from) {
                throw new VirtualFileException("end_line " + endLine + " is before start_line " + from);
            }
            StringBuilder out = new StringBuilder();
            int shownTo = from - 1;
            for (int i = from; i <= to; i++) {
                String line = lines[i - 1];
                if (out.length() + line.length() + 1 > MAX_READ_CHARS) {
                    if (shownTo < from) {
                        out.append(line, 0, MAX_READ_CHARS).append('\n');
                        shownTo = i;
                    }
                    break;
                }
                out.append(line).append('\n');
                shownTo = i;
            }
            if (from > 1 || shownTo < total) {
                out.append("[lines ").append(from).append('-').append(shownTo).append(" of ").append(total);
                if (shownTo < to) {
                    out.append("; the rest is cut, go on with start_line ").append(shownTo + 1);
                } else if (shownTo < total) {
                    out.append("; go on with start_line ").append(shownTo + 1);
                }
                out.append(']');
            }
            return out.toString().stripTrailing().isEmpty() ? "(the lines are empty)" : out.toString().replaceAll("\n$", "");
        });
    }

    /**
     * The text to write: the argument, or the text of the file when it is {@code @file:/path} (the same rule as for the
     * arguments of the commands, see {@link CommandFunctions#FILE_PREFIX}), so a file is copied or added to another
     * without the model writing it again.
     */
    private String textOf(String content) {
        if (content == null) {
            return "";
        }
        if (!content.startsWith(CommandFunctions.FILE_PREFIX)) {
            return content;
        }
        try {
            return fs.readText(fs.parse(content.substring(CommandFunctions.FILE_PREFIX.length()).strip()));
        } catch (VirtualFileException e) {
            throw new VirtualFileException("The argument content: " + e.getMessage(), e);
        }
    }

    @Override
    public void writeFile(FunctionContinuation<String> done, String path, String content) {
        run(done, "write_file", () -> {
            VirtualPath file = fs.parse(path);
            String text = textOf(content);
            fs.writeText(file, text, false);
            return "Wrote " + file + " (" + text.getBytes(java.nio.charset.StandardCharsets.UTF_8).length + " bytes)";
        });
    }

    @Override
    public void appendFile(FunctionContinuation<String> done, String path, String content) {
        run(done, "append_file", () -> {
            VirtualPath file = fs.parse(path);
            String text = textOf(content);
            fs.writeText(file, text, true);
            return "Added " + text.getBytes(java.nio.charset.StandardCharsets.UTF_8).length + " bytes to " + file;
        });
    }

    @Override
    public void replaceInFile(FunctionContinuation<String> done, String path, String oldText, String newText) {
        run(done, "replace_in_file", () -> {
            if (oldText == null || oldText.isEmpty()) {
                throw new VirtualFileException("old_text is empty. To write a whole file use write_file");
            }
            VirtualPath file = fs.parse(path);
            String text = fs.readText(file);
            String replacement = newText == null ? "" : newText;
            String from = oldText;
            int count = count(text, from);
            if (count == 0 && text.contains("\r\n") && !from.contains("\r")) {
                // the model can't see the carriage returns of a file with Windows line breaks
                from = from.replace("\n", "\r\n");
                replacement = replacement.replace("\n", "\r\n");
                count = count(text, from);
            }
            if (count == 0) {
                throw new VirtualFileException("old_text is not in " + file + ". It has to match exactly, with the same "
                        + "spaces and line breaks; read the file again and copy the text");
            }
            if (count > 1) {
                throw new VirtualFileException("old_text is in " + file + " " + count + " times. Add the text around it "
                        + "so that it matches once");
            }
            int at = text.indexOf(from);
            fs.writeText(file, text.substring(0, at) + replacement + text.substring(at + from.length()), false);
            return "Replaced the text in " + file;
        });
    }

    private static int count(String text, String part) {
        int count = 0;
        for (int at = text.indexOf(part); at >= 0; at = text.indexOf(part, at + part.length())) {
            count++;
        }
        return count;
    }

    @Override
    public void regexReplaceInFile(FunctionContinuation<String> done, String path, String pattern, String replacement) {
        run(done, "regex_replace_in_file", () -> {
            VirtualPath file = fs.parse(path);
            TextSearch search = compile(pattern, replacement, true, true);
            String text = fs.readText(file);
            long deadline = DeadlineCharSequence.deadlineIn(regexMillis);
            TextSearch.Result result = search.apply(null, text, t -> new DeadlineCharSequence(t, deadline));
            if (!result.found()) {
                throw new VirtualFileException("The pattern matches nothing in " + file);
            }
            fs.writeText(file, result.newText(), false);
            return "Replaced " + result.hits().size() + (result.hits().size() == 1 ? " match in " : " matches in ") + file;
        });
    }

    @Override
    public void grepFiles(FunctionContinuation<String> done, String pattern, String path, boolean regex, boolean ignoreCase) {
        run(done, "grep_files", () -> {
            TextSearch search = compile(pattern, null, regex, !ignoreCase);
            VirtualPath start = pathOrRoot(path);
            VirtualEntry entry = fs.stat(start);
            if (entry == null) {
                throw new VirtualFileException("There is nothing at " + start);
            }
            List<VirtualEntry> files = entry.directory()
                    ? fs.walk(start, MAX_SEARCHED + 1).stream().filter(e -> !e.directory()).toList()
                    : List.of(entry);
            long deadline = DeadlineCharSequence.deadlineIn(regexMillis);
            Function<String, CharSequence> view = t -> new DeadlineCharSequence(t, deadline);
            StringBuilder out = new StringBuilder();
            int matches = 0;
            boolean more = false;
            search:
            for (VirtualEntry file : files) {
                if (done.isCancelled()) {
                    break;
                }
                if (file.size() > fs.getLimits().maxFileBytes()) {
                    continue;
                }
                String text;
                try {
                    text = fs.readText(file.path());
                } catch (VirtualFileException e) {
                    // not text, not searched
                    continue;
                }
                String[] lines = text.split("\n", -1);
                for (int i = 0; i < lines.length; i++) {
                    if (search.matches(lines[i], view)) {
                        if (matches == MAX_MATCHES) {
                            more = true;
                            break search;
                        }
                        matches++;
                        String line = lines[i].stripTrailing();
                        out.append(file.path()).append(':').append(i + 1).append(": ")
                                .append(line.length() > MAX_MATCH_LINE ? line.substring(0, MAX_MATCH_LINE) + "..." : line)
                                .append('\n');
                    }
                }
            }
            if (matches == 0) {
                return "No match for \"" + VirtualPath.printable(pattern) + "\" in " + start;
            }
            if (more) {
                out.append("[more than ").append(MAX_MATCHES).append(" matches, narrow the search]\n");
            } else if (files.size() > MAX_SEARCHED) {
                out.append("[only the first ").append(MAX_SEARCHED).append(" files were searched]\n");
            }
            return out.toString().stripTrailing();
        });
    }

    @Override
    public void moveFile(FunctionContinuation<String> done, String from, String to) {
        run(done, "move_file", () -> {
            VirtualPath source = fs.parse(from);
            VirtualPath target = fs.parse(to);
            fs.move(source, target);
            return "Moved " + source + " to " + target;
        });
    }

    @Override
    public void deleteFile(FunctionContinuation<String> done, String path, boolean recursive) {
        run(done, "delete_file", () -> {
            VirtualPath target = fs.parse(path);
            fs.delete(target, recursive);
            return "Deleted " + target;
        });
    }

    private VirtualPath pathOrRoot(String path) {
        return path == null || path.isBlank() ? VirtualPath.ROOT : fs.parse(path);
    }

    private static TextSearch compile(String pattern, String replacement, boolean regex, boolean caseSensitive) {
        if (pattern == null || pattern.isEmpty()) {
            throw new VirtualFileException("The pattern is empty");
        }
        try {
            return TextSearch.of(pattern, replacement, regex, caseSensitive);
        } catch (TextSearch.InvalidSearchException e) {
            throw new VirtualFileException(e.getKind() == TextSearch.InvalidSearchException.Kind.SEARCH
                    ? "The regular expression is not valid: " + e.getMessage()
                    : "The replacement is not valid: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new VirtualFileException("The pattern is not valid");
        }
    }

}
