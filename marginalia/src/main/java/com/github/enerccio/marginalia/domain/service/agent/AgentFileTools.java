package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.domain.traits.LLMParam;

import java.util.List;

/**
 * What an agent's model can do with its files: read, write and search text in its workspace. Paths are virtual and
 * absolute ({@code /notes/plot.md}); {@code /} is the workspace, which persists between the runs of the agent, and
 * {@code /checkpoints} can only be listed. Every result is bounded, and every error says what to do about it.
 * <p>
 * The tools of an agent are these and the ones made from the commands; the files are scratch space and notes for the
 * agent, the manuscript is not among them.
 */
public interface AgentFileTools {

    @LLMFunction(name = "list_files", description = "Lists the files and folders of your workspace, the notes and files "
            + "you keep between runs. Shows each path with its size. /checkpoints lists the checkpoints of this run. "
            + "With glob it finds the files by name instead.")
    void listFiles(FunctionContinuation<String> done,
                   @LLMParam(description = "Folder to list, default /", required = false) String path,
                   @LLMParam(description = "true lists everything below the folder too, default false",
                           required = false) boolean recursive,
                   @LLMParam(description = "A name pattern: lists only what matches, at any depth below the folder. * is any "
                           + "characters, ? one character, {md,txt} one of the words, [a-c] one of the letters; the case does "
                           + "not matter. *.md matches by name, chapters/*.md by the path below the folder, **/*.md in all "
                           + "the folders", required = false) String glob);

    @LLMFunction(name = "read_file", description = "Reads a text file of your workspace. A long file is read in parts: "
            + "pass start_line and end_line, the result says which lines it shows and where to go on.")
    void readFile(FunctionContinuation<String> done,
                  @LLMParam(description = "Path of the file, for example /notes/plot.md") String path,
                  @LLMParam(name = "start_line", description = "First line to read, counted from 1, default 1",
                          required = false) int startLine,
                  @LLMParam(name = "end_line", description = "Last line to read, default the end of the file",
                          required = false) int endLine);

    @LLMFunction(name = "write_file", description = "Creates a text file in your workspace, or replaces it whole. The "
            + "folders on the way are created. To change a part of a file use replace_in_file, to add to its end use "
            + "append_file.")
    void writeFile(FunctionContinuation<String> done,
                   @LLMParam(description = "Path of the file, for example /notes/plot.md") String path,
                   @LLMParam(description = "The whole new content of the file, or @file:/path (the whole value) to copy the "
                           + "contents of another file of your workspace, or @result / @result:3 for the whole result of a "
                           + "command call") String content);

    @LLMFunction(name = "list_command_results", description = "Lists the results of your last 10 command calls (commands, not "
            + "file tools) that are kept whole: a number, the function and its arguments (shortened), the length of the "
            + "result. Use it to find a result you want to save with save_command_result.")
    void listCommandResults(FunctionContinuation<String> done);

    @LLMFunction(name = "save_command_result", description = "Saves the whole result of a command call to a file of your "
            + "workspace without you writing it out: use it right after a command that returned a long text you want to keep "
            + "or pass on, for example a generated text, so you spend no output on copying it, and the file has all of it "
            + "even when what you were shown was cut. Also to look at an older result later (read_file, grep_files). The "
            + "folders on the way are created; an existing file is replaced, or added to with append. Later pass the file as "
            + "@file:/path.")
    void saveCommandResult(FunctionContinuation<String> done,
                           @LLMParam(description = "Path of the file, for example /drafts/dragons.md") String path,
                           @LLMParam(description = "Number of the result from list_command_results, default the last one",
                                   required = false) Integer id,
                           @LLMParam(description = "true adds the result to the end of the file instead of replacing it, "
                                   + "default false", required = false) boolean append);

    @LLMFunction(name = "append_file", description = "Adds text to the end of a file of your workspace; the file is "
            + "created when it does not exist. Add the line breaks you need to the text.")
    void appendFile(FunctionContinuation<String> done,
                    @LLMParam(description = "Path of the file") String path,
                    @LLMParam(description = "The text to add, or @file:/path (the whole value) to add the contents of another "
                            + "file of your workspace, or @result / @result:3 for a result of a command call") String content);

    @LLMFunction(name = "replace_in_file", description = "Replaces a piece of text in a file of your workspace with "
            + "another. old_text has to be in the file exactly once, character for character with its spaces and line "
            + "breaks; include enough of the lines around it to make it unique. Read the file first when you are not "
            + "sure of the exact text.")
    void replaceInFile(FunctionContinuation<String> done,
                       @LLMParam(description = "Path of the file") String path,
                       @LLMParam(name = "old_text", description = "The text to replace, exactly as in the file") String oldText,
                       @LLMParam(name = "new_text", description = "The text that takes its place; empty removes the old "
                               + "text") String newText);

    @LLMFunction(name = "regex_replace_in_file", description = "Replaces every match of a regular expression (Java "
            + "syntax) in a file of your workspace. The replacement can use groups of the pattern as $1, $2; write \\$ "
            + "for a dollar sign. Fails when nothing matches.")
    void regexReplaceInFile(FunctionContinuation<String> done,
                            @LLMParam(description = "Path of the file") String path,
                            @LLMParam(description = "The regular expression; ^ and $ match at every line") String pattern,
                            @LLMParam(description = "The replacement; empty removes the matches") String replacement);

    @LLMFunction(name = "grep_files", description = "Searches the files of your workspace for a text or a regular "
            + "expression and lists the matching lines as path:line: text. Searches one file, or all the files below a "
            + "folder.")
    void grepFiles(FunctionContinuation<String> done,
                   @LLMParam(description = "What to look for; a regular expression when regex is true, otherwise the "
                           + "text as it is") String pattern,
                   @LLMParam(description = "File or folder to search, default / (everything)", required = false) String path,
                   @LLMParam(description = "true reads the pattern as a regular expression (Java syntax), default false",
                           required = false) boolean regex,
                   @LLMParam(name = "ignore_case", description = "true ignores the case of the letters, default false",
                           required = false) boolean ignoreCase,
                   @LLMParam(description = "Search only the files whose name matches this pattern, for example *.md or "
                           + "chapters/*.md; the same patterns as in list_files. Used when path is a folder",
                           required = false) String glob);

    @LLMFunction(name = "move_file", description = "Moves or renames a file or a folder of your workspace. The target "
            + "must not exist yet; the folders above it are created.")
    void moveFile(FunctionContinuation<String> done,
                  @LLMParam(description = "Path of the file or folder to move") String from,
                  @LLMParam(description = "The new path, for example /notes/old/plot.md") String to);

    @LLMFunction(name = "copy_file", description = "Copies a file or a folder of your workspace, with everything in it. "
            + "The target must not exist yet; the folders above it are created. The copy counts against the limits of the "
            + "workspace. To keep a version of your notes before you change them, copy the folder.")
    void copyFile(FunctionContinuation<String> done,
                  @LLMParam(description = "Path of the file or folder to copy") String from,
                  @LLMParam(description = "The path of the copy, for example /backup/notes") String to);

    @LLMFunction(name = "unzip_file", description = "Unpacks a zip file of your workspace (for example one the author "
            + "uploaded) into a new folder. The folders inside are made; the files that are kept are text files, images "
            + "and zip files (a zip file inside is stored, not unpacked), the others are left out and listed in the "
            + "result. All or nothing: the archive is refused when it has more files or bytes than the workspace has room "
            + "for, a name that leads out of the folder, or is a zip bomb. Afterwards list_files shows what is in the "
            + "folder.")
    void unzipFile(FunctionContinuation<String> done,
                   @LLMParam(description = "Path of the zip file, for example /uploads/notes.zip") String path,
                   @LLMParam(description = "The folder to unpack into; it must not exist yet. Default: the path of the zip "
                           + "file without .zip, so /uploads/notes.zip is unpacked into /uploads/notes",
                           required = false) String target);

    @LLMFunction(name = "merge_files", description = "Joins several files of your workspace into one file, without the "
            + "text going through you. The target is created, or replaced when it exists. Use it to put notes together "
            + "into one document; to add one file to another use append_file with @file:/path.")
    void mergeFiles(FunctionContinuation<String> done,
                    @LLMParam(description = "The files to join, in this order. The last name of a path can be a pattern with "
                            + "* or ?, for example /chapters/*.md: it stands for the files of that folder in the order of "
                            + "their names (the target itself is left out)") List<String> paths,
                    @LLMParam(description = "Path of the file to make") String target,
                    @LLMParam(description = "The text put between the files, exactly as written, default one line break",
                            required = false) String delimiter);

    @LLMFunction(name = "delete_file", description = "Deletes a file or a folder of your workspace. A folder that is not "
            + "empty is deleted only with recursive true, with everything in it. This cannot be undone.")
    void deleteFile(FunctionContinuation<String> done,
                    @LLMParam(description = "Path of the file or folder") String path,
                    @LLMParam(description = "true deletes a folder with everything in it, default false",
                            required = false) boolean recursive);

}
