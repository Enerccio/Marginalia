package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.domain.traits.LLMParam;

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
            + "you keep between runs. Shows each path with its size. /checkpoints lists the checkpoints of this run.")
    void listFiles(FunctionContinuation<String> done,
                   @LLMParam(description = "Folder to list, default /", required = false) String path,
                   @LLMParam(description = "true lists everything below the folder too, default false",
                           required = false) boolean recursive);

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
                           + "contents of another file of your workspace") String content);

    @LLMFunction(name = "append_file", description = "Adds text to the end of a file of your workspace; the file is "
            + "created when it does not exist. Add the line breaks you need to the text.")
    void appendFile(FunctionContinuation<String> done,
                    @LLMParam(description = "Path of the file") String path,
                    @LLMParam(description = "The text to add, or @file:/path (the whole value) to add the contents of another "
                            + "file of your workspace") String content);

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
                           required = false) boolean ignoreCase);

    @LLMFunction(name = "move_file", description = "Moves or renames a file or a folder of your workspace. The target "
            + "must not exist yet; the folders above it are created.")
    void moveFile(FunctionContinuation<String> done,
                  @LLMParam(description = "Path of the file or folder to move") String from,
                  @LLMParam(description = "The new path, for example /notes/old/plot.md") String to);

    @LLMFunction(name = "delete_file", description = "Deletes a file or a folder of your workspace. A folder that is not "
            + "empty is deleted only with recursive true, with everything in it. This cannot be undone.")
    void deleteFile(FunctionContinuation<String> done,
                    @LLMParam(description = "Path of the file or folder") String path,
                    @LLMParam(description = "true deletes a folder with everything in it, default false",
                            required = false) boolean recursive);

}
