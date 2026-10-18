package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.SummaryService.SummaryBlock;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.CommandUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

/**
 * What the commands that read the book have in common: the services they use, the book (loaded again, with the tree
 * of its parts when a command needs the other branches) and the helpers that make the JSON answers small.
 * <p>
 * A part is addressed only by its <b>position</b>: how deep it is on a branch, the first part of the story is 1. The
 * branch is the active one unless a <b>branch number</b> is given: the number {@code list-branches} gives to the end
 * of a branch (the numbers follow the creation of the ends, the oldest is 1, so they change when a branch is added or
 * deleted). Neither the positions nor the branch numbers are stored anywhere, no id of any entity is given out or
 * accepted. Lists are paged ({@code offset}, {@code limit}), long texts are returned in slices with their total
 * length. The options of a command are positional, so to set one the ones before it are given too.
 */
@Configurable
public abstract class ManuscriptReadSupport {

    /** The page size of a list when {@code limit} is left out, and the most that can be asked for. */
    protected static final int DEFAULT_LIMIT = 50;
    protected static final int MAX_LIMIT = 200;

    /** The size of a slice of a text when {@code length} is left out, and the most that can be asked for. */
    protected static final int DEFAULT_SLICE = 4000;
    protected static final int MAX_SLICE = 20000;

    /** How much of a part the lists show. */
    protected static final int PREVIEW_LENGTH = 120;

    protected static final Gson GSON = new Gson();

    @Autowired
    protected ManuscriptService manuscriptService;

    @Autowired
    protected ChatMessageService chatMessageService;

    @Autowired
    protected SummaryService summaryService;

    @Autowired
    protected TagRelationService tagRelationService;

    @Autowired
    protected InferenceServices inferenceServices;

    @Autowired
    protected Localization loc;

    /**
     * The book the command reads, loaded again (the copy of the window can be older than the stored book), with the
     * tree of its parts loaded when a command needs the other branches.
     */
    protected final class Book {

        public final Manuscript manuscript;
        private StoryTree tree;

        public Book() throws Exception {
            Manuscript open = CommandUtils.getCurrentManuscript();
            Manuscript fresh = open == null ? null : manuscriptService.findForUser(open.getUuid());
            if (fresh == null) {
                throw new IllegalStateException(loc.getValue(L.ERROR_COMMAND_NO_BOOK_OPEN));
            }
            manuscript = fresh;
        }

        StoryTree tree() throws Exception {
            if (tree == null) {
                tree = new StoryTree(chatMessageService.getAllMessages(manuscript), manuscript.getActiveLeaf());
            }
            return tree;
        }

        /**
         * The parts of the branch, the first part of the story first. The active branch when {@code branch} is null.
         */
        List<ChatMessage> path(Integer branch) throws Exception {
            if (branch == null) {
                return chatMessageService.getBranchFromLeaf(manuscript.getActiveLeaf());
            }
            StoryTree t = tree();
            if (branch < 1 || branch > t.leaves.size()) {
                throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_COMMAND_NO_SUCH_BRANCH), branch,
                        t.leaves.size()));
            }
            return t.pathTo(t.leaves.get(branch - 1));
        }

        /**
         * @param position the position of the part on the path, the first part of the story is 1
         */
        ChatMessage part(List<ChatMessage> path, Integer position) {
            if (path.isEmpty()) {
                throw new IllegalStateException(loc.getValue(L.ERROR_COMMAND_NO_STORY));
            }
            if (position == null || position < 1 || position > path.size()) {
                throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_COMMAND_NO_SUCH_PART), position,
                        path.size()));
            }
            return path.get(position - 1);
        }

    }


    // ---------------------------------------------------------------------------------------------------------------
    // helpers

    /** The summary of a part on the path: whether it still describes the story, by the id of the part. */
    Map<Long, Boolean> validSummaries(List<ChatMessage> path) throws Exception {
        List<ChatMessage> newestFirst = new ArrayList<>(path);
        Collections.reverse(newestFirst);
        Map<Long, Boolean> valid = new LinkedHashMap<>();
        for (SummaryBlock block : summaryService.collectBlocks(newestFirst)) {
            valid.put(block.head().getId(), block.isValid());
        }
        return valid;
    }

    static String summaryState(ChatMessage message, Map<Long, Boolean> valid) {
        if (message.getSummary() == null) {
            return "none";
        }
        return Boolean.FALSE.equals(valid.get(message.getId())) ? "outdated" : "valid";
    }

    protected static int limit(Integer limit) {
        return limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
    }

    /**
     * Puts a slice of the text and where it is in the text into the answer.
     */
    protected static void slice(JsonObject result, String text, Integer offset, Integer length) {
        String content = text == null ? "" : text;
        int start = Math.min(Math.max(offset == null ? 0 : offset, 0), content.length());
        int end = Math.min(content.length(), start + Math.min(Math.max(length == null ? DEFAULT_SLICE : length, 1), MAX_SLICE));
        // do not cut a surrogate pair
        if (start > 0 && start < content.length() && Character.isLowSurrogate(content.charAt(start))) {
            start--;
        }
        if (end < content.length() && Character.isLowSurrogate(content.charAt(end))) {
            end++;
        }
        result.addProperty("text", content.substring(start, end));
        result.addProperty("offset", start);
        result.addProperty("length", end - start);
        result.addProperty("totalLength", content.length());
        result.addProperty("truncated", end < content.length());
    }

    protected static String preview(String text, int length) {
        if (text == null) {
            return "";
        }
        String line = text.replaceAll("\\s+", " ").strip();
        if (line.length() <= length) {
            return line;
        }
        int end = length;
        if (Character.isHighSurrogate(line.charAt(end - 1))) {
            end--;
        }
        return line.substring(0, end) + "…";
    }

    protected static void answer(CommandContext context, JsonObject result) {
        context.setReturn(GSON.toJson(result));
    }
}
