package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.SummaryService.SummaryBlock;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

/**
 * What the commands that read the book have in common: the services they use, the book (loaded again, with the tree
 * of its parts when a command needs the other branches) and the helpers that make the JSON answers small.
 * <p>
 * A part is addressed by its <b>handle</b>, the {@code handle} every row of an answer has (the uuid of the part; an
 * agent sees it as a short {@code h:m17}, the border of the agent translates it), or by its <b>position</b>: how deep it
 * is on a branch, the first part of the story is 1. The handle does not change when parts are added, moved or deleted,
 * the position does, so the commands that write take handles only. The branch is the active one unless a
 * <b>branch</b> is given: the handle of any part of it (the branch then goes through that part to its end), or the number
 * {@code list-branches} gives to the end of a branch (the numbers follow the creation of the ends, the oldest is 1, so
 * they change when a branch is added or deleted). Lists are paged ({@code offset}, {@code limit}), long texts are
 * returned in slices with their total length. The options of a command are positional, so to set one the ones before
 * it are given too.
 */
@Configurable
public abstract class ManuscriptReadSupport {

    /** The page size of a list when {@code limit} is left out, and the most that can be asked for. */
    protected static final int DEFAULT_LIMIT = 50;
    protected static final int MAX_LIMIT = 200;

    /** The size of a slice of a text when {@code length} is left out, and the most that can be asked for. */
    protected static final int DEFAULT_SLICE = 6000;
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
        private final CommandContext context;
        private StoryTree tree;

        public Book(CommandContext context) throws Exception {
            this.context = context;
            Manuscript fresh = context.getScope().getManuscript();
            if (fresh == null) {
                throw new IllegalStateException(loc.getValue(L.ERROR_COMMAND_NO_BOOK_OPEN));
            }
            manuscript = fresh;
        }

        /** The parts that continue the part, the oldest first. */
        public List<ChatMessage> childrenOf(ChatMessage message) throws Exception {
            return tree().children.getOrDefault(message.getId(), List.of());
        }

        /** The parts of the book that are not deleted. */
        public int partCount() throws Exception {
            return tree().byId.size();
        }

        public CommandContext context() {
            return context;
        }

        /** A part found by its handle or its position: the branch it is on, the part, and how deep it is. */
        public record Located(List<ChatMessage> path, ChatMessage message, int position) {
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
        public List<ChatMessage> path(Integer branch) throws Exception {
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
         * The parts of the branch the {@code branch} names: the handle of one of its parts or its number, the active
         * branch when it is null or blank.
         */
        public List<ChatMessage> path(String branch) throws Exception {
            if (branch == null || branch.isBlank()) {
                return path((Integer) null);
            }
            if (isHandle(branch)) {
                StoryTree t = tree();
                return t.pathTo(t.leafOf(known(branch)));
            }
            return path(number(branch));
        }

        /**
         * The part with the handle (the uuid), checked to be a part of the book.
         */
        public ChatMessage known(String handle) throws Exception {
            ChatMessage found = context.getScope().resolveMessage(handle.strip());
            ChatMessage message = tree().byId.get(found.getId());
            if (message == null) {
                throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_NO_SUCH_HANDLE));
            }
            return message;
        }

        /**
         * The part a command is aimed at: the handle of a part, or its position on the branch (see {@link #path(String)}).
         */
        public Located locate(String part, String branch) throws Exception {
            if (part != null && isHandle(part)) {
                ChatMessage message = known(part);
                List<ChatMessage> path = tree().pathTo(tree().leafOf(message));
                return new Located(path, message, path.indexOf(message) + 1);
            }
            List<ChatMessage> path = path(branch);
            int position = number(part);
            return new Located(path, part(path, position), position);
        }

        private int number(String text) {
            try {
                return Integer.parseInt(text == null ? "" : text.strip());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_COMMAND_NOT_A_REFERENCE), text), e);
            }
        }

        /**
         * @param position the position of the part on the path, the first part of the story is 1
         */
        public ChatMessage part(List<ChatMessage> path, Integer position) {
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

    private static final java.util.regex.Pattern UUID = java.util.regex.Pattern
            .compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    /** Whether the text is a handle (the uuid of an item) and not a position or a number. */
    protected static boolean isHandle(String text) {
        return text != null && UUID.matcher(text.strip()).matches();
    }

    /** What the caller gave for the parameter as text (a number from a model comes as a number), null when it is not given. */
    protected static String ref(CommandContext context, String name) {
        Object value = context.get(name);
        return value == null ? null : value.toString().strip();
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
