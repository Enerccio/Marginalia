package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.Defaults;
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
    // files joined by one merge_files
    public static final int MAX_MERGED = 100;
    // files one folder wide replace may change unless max_files says more
    public static final int MAX_REPLACED = 100;
    // how long the regular expressions of one call may run, unless told otherwise
    public static final long REGEX_MILLIS = 3_000;

    private final VirtualFileSystem fs;
    private final long regexMillis;
    private final CommandResults commandResults;

    public AgentFileToolsImpl(VirtualFileSystem fs) {
        this(fs, REGEX_MILLIS);
    }

    /**
     * @param regexMillis how long the regular expressions of one call may run
     */
    public AgentFileToolsImpl(VirtualFileSystem fs, long regexMillis) {
        this(fs, regexMillis, null);
    }

    /**
     * @param commandResults the results of the commands of the run, for list_command_results and save_command_result; null when there are none
     */
    public AgentFileToolsImpl(VirtualFileSystem fs, CommandResults commandResults) {
        this(fs, REGEX_MILLIS, commandResults);
    }

    public AgentFileToolsImpl(VirtualFileSystem fs, long regexMillis, CommandResults commandResults) {
        this.fs = fs;
        this.regexMillis = regexMillis;
        this.commandResults = commandResults;
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
    public void listFiles(FunctionContinuation<String> done, String path, boolean recursive, String glob) {
        run(done, "list_files", () -> {
            VirtualPath folder = pathOrRoot(path);
            VirtualGlob pattern = VirtualGlob.parse(glob);
            VirtualEntry entry = fs.stat(folder);
            if (entry == null) {
                throw new VirtualFileException("There is no folder " + folder);
            }
            if (!entry.directory()) {
                throw new VirtualFileException(folder + " is a file, not a folder");
            }
            List<VirtualEntry> entries = new ArrayList<>(fs.list(folder));
            String note = "";
            if (pattern != null) {
                List<VirtualEntry> walked = fs.walk(folder, MAX_SEARCHED + 1);
                if (walked.size() > MAX_SEARCHED) {
                    walked = walked.subList(0, MAX_SEARCHED);
                    note = "\n[only the first " + MAX_SEARCHED + " entries were looked at, give a folder below]";
                }
                entries = walked.stream().filter(e -> pattern.matches(folder, e.path())).toList();
                if (entries.isEmpty()) {
                    return "Nothing below " + folder + " matches " + VirtualPath.printable(glob.strip()) + note;
                }
            } else if (recursive) {
                entries = fs.walk(folder, MAX_LISTED + 1);
                if (folder.isRoot()) {
                    entries.addAll(0, fs.mountEntries());
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
                out.append("[more than ").append(MAX_LISTED).append(" entries, ")
                        .append(pattern != null ? "make the pattern narrower or list a folder below" : "list a folder below to see the rest")
                        .append("]\n");
            }
            return out.toString().stripTrailing() + note;
        });
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
            boolean lineCut = false;
            for (int i = from; i <= to; i++) {
                String line = lines[i - 1];
                if (out.length() + line.length() + 1 > MAX_READ_CHARS) {
                    if (shownTo < from) {
                        out.append(line, 0, MAX_READ_CHARS).append('\n');
                        shownTo = i;
                        lineCut = true;
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
            if (out.toString().stripTrailing().isEmpty()) {
                return "(the lines are empty)";
            }
            String result = out.toString().replaceAll("\n$", "");
            // the whole file is in front of the model: this is the moment it may copy it into a call, writing it out again at the
            // speed of its output, so it is told once more that the file itself can be passed
            return from == 1 && shownTo == total && !lineCut && text.length() >= Defaults.AGENT_FILE_HINT_FROM
                    ? result + "\n" + String.format(Defaults.AGENT_FILE_HINT, file) : result;
        });
    }

    /**
     * The text to write: the argument, or the text of the file when it is {@code @file:/path} (the same rule as for the
     * arguments of the commands, see {@link CommandFunctions#FILE_PREFIX}), or {@code @result}, so a file is copied or added to another
     * without the model writing it again.
     */
    private String textOf(String content) {
        if (content == null) {
            return "";
        }
        if (commandResults != null && CommandResults.isReference(content)) {
            try {
                return commandResults.resolve(content).text();
            } catch (IllegalArgumentException e) {
                throw new VirtualFileException("The argument content: " + e.getMessage(), e);
            }
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
    public void listCommandResults(FunctionContinuation<String> done) {
        run(done, "list_command_results", () -> {
            List<CommandResults.Entry> kept = commandResults == null ? List.of() : commandResults.list();
            if (kept.isEmpty()) {
                return "No command result is kept yet";
            }
            StringBuilder out = new StringBuilder();
            for (CommandResults.Entry entry : kept.reversed()) {
                out.append('#').append(entry.id()).append(' ').append(entry.function()).append('(').append(entry.arguments())
                        .append("): ").append(entry.text().length()).append(" characters\n");
            }
            return out.append("The newest is first, the last ").append(CommandResults.KEEP).append(" are kept.").toString();
        });
    }

    @Override
    public void saveCommandResult(FunctionContinuation<String> done, String path, Integer id, boolean append) {
        run(done, "save_command_result", () -> {
            CommandResults.Entry result = commandResults == null ? null : id == null ? commandResults.last() : commandResults.get(id);
            if (result == null) {
                throw new VirtualFileException(id == null ? "There is no result to save yet: call a command first"
                        : "There is no result #" + id + ", list_command_results shows the ones that are kept");
            }
            VirtualPath file = fs.parse(path);
            fs.writeText(file, result.text(), append);
            return (append ? "Added" : "Saved") + " the result #" + result.id() + " of " + result.function() + " ("
                    + result.text().getBytes(java.nio.charset.StandardCharsets.UTF_8).length + " bytes) "
                    + (append ? "to " : "as ") + file;
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
    public void replaceInFile(FunctionContinuation<String> done, String path, String oldText, String newText, Boolean dryRun,
                              Integer maxFiles, String glob) {
        run(done, "replace_in_file", () -> {
            if (oldText == null || oldText.isEmpty()) {
                throw new VirtualFileException("old_text is empty. To write a whole file use write_file");
            }
            VirtualPath file = fs.parse(path);
            String given = newText == null ? "" : newText;
            if (isFolder(file)) {
                return replaceBelow(file, text -> {
                    String from = oldText;
                    String to = given;
                    int found = count(text, from);
                    if (found == 0 && text.contains("\r\n") && !from.contains("\r")) {
                        from = from.replace("\n", "\r\n");
                        to = to.replace("\n", "\r\n");
                        found = count(text, from);
                    }
                    return found == 0 ? null : new Replaced(text.replace(from, to), found);
                }, dryRun, maxFiles, glob, "old_text is in no file");
            }
            String text = fs.readText(file);
            String replacement = given;
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
            if (Boolean.TRUE.equals(dryRun)) {
                return "Dry run, nothing was changed: the text would be replaced in " + file;
            }
            int at = text.indexOf(from);
            fs.writeText(file, text.substring(0, at) + replacement + text.substring(at + from.length()), false);
            return "Replaced the text in " + file;
        });
    }

    /**
     * What a replace made of one file: the new text and the number of replacements.
     */
    private record Replaced(String text, int count) {
    }

    private boolean isFolder(VirtualPath path) {
        VirtualEntry entry = fs.stat(path);
        return entry != null && entry.directory();
    }

    /**
     * The folder wide replace: plans the change of every text file below the folder (that matches the glob), refuses it when
     * it would touch more files than allowed or a file can't be written, and only then writes, all files or none. A dry run
     * (the default) writes nothing and says what would change.
     *
     * @param edit the new text of a file, null when nothing is to be replaced in it
     */
    private String replaceBelow(VirtualPath folder, Function<String, Replaced> edit, Boolean dryRun, Integer maxFiles, String glob,
                                String none) {
        VirtualGlob names = VirtualGlob.parse(glob);
        int allowed = maxFiles == null || maxFiles < 1 ? MAX_REPLACED : Math.min(maxFiles, MAX_SEARCHED);
        List<VirtualEntry> walked = fs.walk(folder, MAX_SEARCHED + 1);
        boolean capped = walked.size() > MAX_SEARCHED;
        List<VirtualEntry> files = walked.stream().limit(MAX_SEARCHED)
                .filter(e -> !e.directory() && (names == null || names.matches(folder, e.path()))).toList();
        record Change(VirtualPath file, String before, String after, int count) {
        }
        List<Change> changes = new ArrayList<>();
        int total = 0;
        for (VirtualEntry file : files) {
            if (file.size() > fs.getLimits().maxFileBytes()) {
                continue;
            }
            String text;
            try {
                text = fs.readText(file.path());
            } catch (VirtualFileException e) {
                // not text, left alone
                continue;
            }
            Replaced replaced = edit.apply(text);
            if (replaced != null && replaced.count() > 0) {
                changes.add(new Change(file.path(), text, replaced.text(), replaced.count()));
                total += replaced.count();
            }
        }
        String capNote = capped ? " [only the first " + MAX_SEARCHED + " entries were looked at, give a folder below]" : "";
        if (changes.isEmpty()) {
            throw new VirtualFileException(none + " of " + folder
                    + (names != null ? " with a name like " + VirtualPath.printable(glob.strip()) : "") + ", nothing to replace" + capNote);
        }
        if (changes.size() > allowed) {
            throw new VirtualFileException("The replacement would change " + changes.size() + " files, more than max_files "
                    + allowed + ". Nothing was changed. Narrow it with a deeper folder or glob, or raise max_files");
        }
        for (Change change : changes) {
            fs.checkWritable(change.file());
        }
        boolean dry = dryRun == null || dryRun;
        StringBuilder out = new StringBuilder(dry ? "Dry run, nothing was changed. Would replace " : "Replaced ")
                .append(total).append(total == 1 ? " occurrence in " : " occurrences in ")
                .append(changes.size()).append(changes.size() == 1 ? " file:\n" : " files:\n");
        int shown = Math.min(changes.size(), MAX_LISTED);
        for (int i = 0; i < shown; i++) {
            out.append(changes.get(i).file()).append(": ").append(changes.get(i).count())
                    .append(changes.get(i).count() == 1 ? " replacement\n" : " replacements\n");
        }
        if (changes.size() > shown) {
            out.append("and ").append(changes.size() - shown).append(" more files\n");
        }
        if (dry) {
            return out.append("Call again with dry_run false to make the change.").append(capNote).toString();
        }
        List<Change> written = new ArrayList<>();
        try {
            for (Change change : changes) {
                fs.writeText(change.file(), change.after(), false);
                written.add(change);
            }
        } catch (RuntimeException e) {
            boolean restored = true;
            for (Change change : written) {
                try {
                    fs.writeText(change.file(), change.before(), false);
                } catch (RuntimeException again) {
                    restored = false;
                    log.warn("The file {} could not be put back after a failed replace", change.file(), again);
                }
            }
            String why = e instanceof VirtualFileException ? e.getMessage() : "A file could not be written";
            if (!(e instanceof VirtualFileException)) {
                log.warn("A folder wide replace failed", e);
            }
            throw new VirtualFileException(why + (restored ? ". Nothing was changed" : ". Some files were already changed, "
                    + "check them with grep_files"));
        }
        return out.toString().stripTrailing() + capNote;
    }

    private static int count(String text, String part) {
        int count = 0;
        for (int at = text.indexOf(part); at >= 0; at = text.indexOf(part, at + part.length())) {
            count++;
        }
        return count;
    }

    @Override
    public void regexReplaceInFile(FunctionContinuation<String> done, String path, String pattern, String replacement,
                                   Boolean dryRun, Integer maxFiles, String glob) {
        run(done, "regex_replace_in_file", () -> {
            VirtualPath file = fs.parse(path);
            TextSearch search = compile(pattern, replacement, true, true);
            long deadline = DeadlineCharSequence.deadlineIn(regexMillis);
            if (isFolder(file)) {
                return replaceBelow(file, text -> {
                    TextSearch.Result result = search.apply(null, text, t -> new DeadlineCharSequence(t, deadline));
                    return result.found() ? new Replaced(result.newText(), result.hits().size()) : null;
                }, dryRun, maxFiles, glob, "The pattern matches nothing in any file");
            }
            String text = fs.readText(file);
            TextSearch.Result result = search.apply(null, text, t -> new DeadlineCharSequence(t, deadline));
            if (!result.found()) {
                throw new VirtualFileException("The pattern matches nothing in " + file);
            }
            String matches = result.hits().size() + (result.hits().size() == 1 ? " match in " : " matches in ") + file;
            if (Boolean.TRUE.equals(dryRun)) {
                return "Dry run, nothing was changed. Would replace " + matches;
            }
            fs.writeText(file, result.newText(), false);
            return "Replaced " + matches;
        });
    }

    @Override
    public void grepFiles(FunctionContinuation<String> done, String pattern, String path, boolean regex, boolean ignoreCase,
                          String glob) {
        run(done, "grep_files", () -> {
            TextSearch search = compile(pattern, null, regex, !ignoreCase);
            VirtualGlob names = VirtualGlob.parse(glob);
            VirtualPath start = pathOrRoot(path);
            VirtualEntry entry = fs.stat(start);
            if (entry == null) {
                throw new VirtualFileException("There is nothing at " + start);
            }
            List<VirtualEntry> walked = entry.directory() ? fs.walk(start, MAX_SEARCHED + 1) : List.of();
            List<VirtualEntry> files = entry.directory()
                    ? walked.stream().filter(e -> !e.directory() && (names == null || names.matches(start, e.path()))).toList()
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
            boolean capped = walked.size() > MAX_SEARCHED;
            if (matches == 0) {
                return "No match for \"" + VirtualPath.printable(pattern) + "\" in " + start
                        + (names != null && files.isEmpty() ? " (no file matches " + VirtualPath.printable(glob.strip()) + ")" : "")
                        + (capped ? " [only the first " + MAX_SEARCHED + " entries were looked at]" : "");
            }
            if (more) {
                out.append("[more than ").append(MAX_MATCHES).append(" matches, narrow the search]\n");
            } else if (files.size() > MAX_SEARCHED || capped) {
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
    public void copyFile(FunctionContinuation<String> done, String from, String to) {
        run(done, "copy_file", () -> {
            VirtualPath source = fs.parse(from);
            VirtualPath target = fs.parse(to);
            WorkspaceUsage copied = fs.copy(source, target);
            return "Copied " + source + " to " + target + " (" + copied.files() + (copied.files() == 1 ? " file, " : " files, ")
                    + copied.bytes() + " bytes)";
        });
    }

    @Override
    public void unzipFile(FunctionContinuation<String> done, String path, String target) {
        run(done, "unzip_file", () -> {
            VirtualPath archive = fs.parse(path);
            VirtualPath folder;
            if (target != null && !target.isBlank()) {
                folder = fs.parse(target);
            } else {
                String name = archive.isRoot() ? "" : archive.getName();
                if (!name.toLowerCase(java.util.Locale.ROOT).endsWith(".zip") || name.length() <= 4) {
                    throw new VirtualFileException(archive + " does not end with .zip, so give the folder to unpack into as target");
                }
                folder = archive.getParent().resolve(name.substring(0, name.length() - 4));
            }
            UnzipResult unpacked = fs.unzip(archive, folder);
            StringBuilder out = new StringBuilder("Unpacked ").append(archive).append(" into ").append(folder).append(": ")
                    .append(unpacked.files()).append(unpacked.files() == 1 ? " file (" : " files (").append(unpacked.bytes())
                    .append(" bytes), ").append(unpacked.folders()).append(unpacked.folders() == 1 ? " folder" : " folders");
            if (unpacked.skipped() > 0) {
                out.append("\nLeft out ").append(unpacked.skipped()).append(unpacked.skipped() == 1 ? " entry:" : " entries:");
                for (String skipped : unpacked.skippedNames()) {
                    out.append("\n- ").append(skipped);
                }
                if (unpacked.skipped() > unpacked.skippedNames().size()) {
                    out.append("\n- and ").append(unpacked.skipped() - unpacked.skippedNames().size()).append(" more");
                }
            }
            return out.toString();
        });
    }

    @Override
    public void mergeFiles(FunctionContinuation<String> done, List<String> paths, String target, String delimiter) {
        run(done, "merge_files", () -> {
            if (paths == null || paths.isEmpty()) {
                throw new VirtualFileException("paths is empty; name the files to join");
            }
            VirtualPath result = fs.parse(target);
            List<VirtualPath> sources = new ArrayList<>();
            for (String path : paths) {
                sources.addAll(expand(path, result));
                if (sources.size() > MAX_MERGED) {
                    throw new VirtualFileException("At most " + MAX_MERGED + " files can be joined at once; join them in "
                            + "groups and then join the results");
                }
            }
            String between = delimiter == null ? "\n" : delimiter;
            long limit = fs.getLimits().maxFileBytes();
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < sources.size(); i++) {
                if (i > 0) {
                    text.append(between);
                }
                text.append(fs.readText(sources.get(i)));
                if (text.length() > limit) {
                    throw new VirtualFileException("The joined file would be larger than the limit of " + limit
                            + " bytes; join fewer files");
                }
            }
            fs.writeText(result, text.toString(), false);
            return "Joined " + sources.size() + (sources.size() == 1 ? " file into " : " files into ") + result + " ("
                    + text.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8).length + " bytes)";
        });
    }

    /**
     * The files a path of merge_files stands for: the file itself, or for a pattern in the last name the files of the
     * folder that match it, in the order of their names, the target left out (so a merge can be repeated).
     */
    private List<VirtualPath> expand(String path, VirtualPath target) {
        if (path == null || (path.indexOf('*') < 0 && path.indexOf('?') < 0)) {
            return List.of(fs.parse(path));
        }
        int slash = path.lastIndexOf('/');
        String folderText = slash < 0 ? "/" : path.substring(0, Math.max(slash, 1));
        String name = path.substring(slash + 1);
        if (folderText.indexOf('*') >= 0 || folderText.indexOf('?') >= 0) {
            throw new VirtualFileException("A pattern can be only in the last name of a path: " + VirtualPath.printable(path));
        }
        VirtualPath folder = fs.parse(folderText);
        VirtualGlob glob = VirtualGlob.parse(name);
        VirtualEntry entry = fs.stat(folder);
        if (entry == null || !entry.directory()) {
            throw new VirtualFileException("There is no folder " + folder);
        }
        List<VirtualPath> found = fs.list(folder).stream().filter(e -> !e.directory() && !e.path().equals(target))
                .filter(e -> glob.matches(folder, e.path())).map(VirtualEntry::path).toList();
        if (found.isEmpty()) {
            throw new VirtualFileException("No file in " + folder + " matches " + VirtualPath.printable(name));
        }
        return found;
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
