package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.AgentRun;

import java.util.List;

/**
 * Lets the user read the working copy of the book of a run, which is hidden: the branches of its story, the parts of one of them
 * and its lorebooks, as they are now (also while the agent works on them). Nothing here changes the copy, and nothing opens it
 * as a book of the user: it has no window, so no backup of it is made (see {@code StoryBackup}) and no plugin sees it open.
 * <p>
 * Works for the runs of the current user that have a copy: not for a run that ended or was discarded.
 */
public interface AgentCopyService {

    /**
     * The ends of the branches of the story, the oldest first.
     *
     * @throws IllegalArgumentException when the run is not a run of the user
     * @throws IllegalStateException    when the run has no copy
     */
    List<CopyBranch> branches(AgentRun run) throws Exception;

    /**
     * The parts of the branch that shows the part, the first part of the story first: when the part is on the branch the copy is
     * set to, that one, otherwise the newest one below the part.
     *
     * @param partUuid the uuid of a part of the copy; null or unknown for the branch the copy is set to
     * @throws IllegalArgumentException when the run is not a run of the user
     * @throws IllegalStateException    when the run has no copy
     */
    List<CopyPart> branch(AgentRun run, String partUuid) throws Exception;

    /**
     * The lorebooks of the copy with their entries: the root lorebook and its subbooks level by level, each once.
     *
     * @throws IllegalArgumentException when the run is not a run of the user
     * @throws IllegalStateException    when the run has no copy
     */
    List<CopyLorebook> lorebooks(AgentRun run) throws Exception;

    /**
     * @param number  the number of the branch, from 1
     * @param uuid    the last part of the branch
     * @param preview the start of the text of the last part
     * @param parts   how many parts the branch has
     * @param active  whether it is the branch the copy is set to
     */
    record CopyBranch(int number, String uuid, String preview, int parts, boolean active) {
    }

    /**
     * @param position the place in the branch, from 1
     * @param swipe    which of the parts that follow the same part this is, from 1, with {@code swipes} of them
     */
    record CopyPart(String uuid, int position, int swipe, int swipes, String text, String instructions, String sceneSetting,
                    String povCharacter, String presentCharacters, String summary, boolean edited, int words) {
    }

    /**
     * @param subbooks the names of its subbooks
     */
    record CopyLorebook(String uuid, String name, boolean enabled, List<String> subbooks, List<CopyEntry> entries) {
    }

    record CopyEntry(String uuid, String name, boolean enabled, int order, String payload, String comment, List<String> tags,
                     List<String> negativeTags, String filtering, String filteringMode, String insertionMode) {
    }
}
