package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.write;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.AIGate;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.SummaryService.SummaryBlock;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read.ManuscriptReadSupport;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.utils.CommandUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.concurrent.*;

/**
 * What the commands that change the book have in common. They work on the same book as the commands that read it (the
 * book that is open for the user, the working copy for an agent) and they address a part by its <b>handle</b> only: the
 * position of a part moves when parts are added or deleted, so a position that was right when it was read may be
 * another part by the time it is written. The handle is the {@code handle} of the rows of the lists.
 * <p>
 * An edit of the story can make the summaries outdated (a summary carries the hash of the story it describes): the
 * answer lists them, so the author (or the agent) can say they are still right ({@code revalidate-summaries}) or make them
 * again.
 */
@Configurable
public abstract class ManuscriptWriteSupport extends ManuscriptReadSupport {

    @Autowired
    protected TagService tagService;

    @Autowired
    protected AIGate aiGate;

    /** The fields of a part that can be changed as text, with the name they have in the commands. */
    protected static final List<String> FIELDS = List.of("response", "instructions", "scene", "pov", "present");

    /**
     * The part with that handle. A position is refused, see the class description.
     */
    protected ChatMessage part(Book book, String handle) throws Exception {
        if (!isHandle(handle)) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_HANDLE_REQUIRED));
        }
        return book.known(handle);
    }

    protected String fieldName(String field) {
        String name = field == null || field.isBlank() ? "response" : field.strip().toLowerCase();
        if (!FIELDS.contains(name)) {
            throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_COMMAND_UNKNOWN_FIELD), field,
                    String.join(", ", FIELDS)));
        }
        return name;
    }

    protected static String read(ChatMessage message, String field) {
        String text = switch (field) {
            case "instructions" -> message.getInstructions();
            case "scene" -> message.getSceneSetting();
            case "pov" -> message.getPovCharacter();
            case "present" -> message.getPresentCharacters();
            default -> message.getResponse();
        };
        return text == null ? "" : text;
    }

    protected static void write(ChatMessage message, String field, String text) {
        switch (field) {
            case "instructions" -> message.setInstructions(text);
            case "scene" -> message.setSceneSetting(text);
            case "pov" -> message.setPovCharacter(text);
            case "present" -> message.setPresentCharacters(text);
            default -> message.setResponse(text);
        }
    }

    /**
     * The heads of the summaries of the branch that no longer describe the story, to tell what a change made outdated.
     */
    protected Set<Long> outdatedHeads(List<ChatMessage> path) throws Exception {
        List<ChatMessage> newestFirst = new ArrayList<>(path);
        Collections.reverse(newestFirst);
        Set<Long> heads = new HashSet<>();
        for (SummaryBlock block : summaryService.collectBlocks(newestFirst)) {
            if (!block.isValid()) {
                heads.add(block.head().getId());
            }
        }
        return heads;
    }

    /**
     * Puts the summaries a change made outdated into the answer, with a hint what to do about them: the ones of the path
     * that are outdated now and were not before.
     *
     * @param before the heads that were outdated before the change, see {@link #outdatedHeads}
     */
    protected void reportOutdated(JsonObject result, List<ChatMessage> path, Set<Long> before) throws Exception {
        List<ChatMessage> newestFirst = new ArrayList<>(path);
        Collections.reverse(newestFirst);
        List<SummaryBlock> blocks = new ArrayList<>();
        for (SummaryBlock block : summaryService.collectBlocks(newestFirst)) {
            if (!block.isValid() && !before.contains(block.head().getId())) {
                blocks.add(block);
            }
        }
        reportOutdated(result, path, blocks.reversed());
    }

    /**
     * Puts the given summaries (oldest first) into the answer as outdated.
     */
    protected void reportOutdated(JsonObject result, List<ChatMessage> path, List<SummaryBlock> outdated) {
        if (outdated.isEmpty()) {
            return;
        }
        JsonArray rows = new JsonArray();
        for (SummaryBlock block : outdated) {
            JsonObject row = new JsonObject();
            row.addProperty("handle", block.head().getUuid());
            int position = path.indexOf(block.head());
            if (position >= 0) {
                row.addProperty("position", position + 1);
            }
            rows.add(row);
        }
        result.add("outdatedSummaries", rows);
        result.addProperty("hint", String.format(loc.getValue(L.MSG_COMMAND_OUTDATED_SUMMARIES_HINT), outdated.size()));
    }

    /**
     * The part as the answer of a command tells about it.
     */
    protected JsonObject describe(ChatMessage message, int position) {
        JsonObject result = new JsonObject();
        result.addProperty("handle", message.getUuid());
        result.addProperty("position", position);
        result.addProperty("words", message.getWordCount());
        result.addProperty("tokens", message.getTokenCount());
        result.addProperty("edited", message.isEdited());
        return result;
    }

    /**
     * The values of a list given as one text, separated by commas, semicolons or line breaks.
     */
    protected static List<String> list(String text) {
        List<String> values = new ArrayList<>();
        if (text != null) {
            for (String value : text.split("[,;\\n]")) {
                if (!value.isBlank()) {
                    values.add(value.strip());
                }
            }
        }
        return values;
    }

    /**
     * Waits for the answer of something the command started, and stops it when the command is stopped.
     *
     * @param token cancels the work, may be null
     * @return the answer; null when the command was stopped (nobody wants an answer then)
     */
    protected <T> T await(CommandContext context, CompletableFuture<T> future, CancellationToken token) throws Exception {
        while (true) {
            try {
                return future.get(100, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                if (context.isTerminated()) {
                    if (token != null) {
                        token.cancel();
                    }
                    return null;
                }
            } catch (ExecutionException e) {
                if (e.getCause() instanceof CancellationException && context.isTerminated()) {
                    return null;
                }
                Throwable cause = e.getCause();
                throw cause instanceof Exception exception ? exception : e;
            }
        }
    }

    /**
     * A place at the AI for the request a command of an agent makes with it (the cap of the AI, see {@link AIGate}); to be
     * closed when the request is over. The user at the command bar and an AI that is not known get none to wait for.
     *
     * @throws CancellationException when the command was stopped while it waited
     */
    protected AIGate.Permit placeAt(CommandContext context, AI ai) throws Exception {
        if (ai == null || context.getScope().getRun() == null) {
            return () -> {
            };
        }
        CompletableFuture<AIGate.Permit> place = aiGate.acquire(ai);
        if (!place.isDone()) {
            context.setStatus(loc.getValue(L.MSG_COMMAND_WAITING_FOR_MODEL));
        }
        AIGate.Permit permit = await(context, place, null);
        if (permit == null) {
            place.cancel(true);
            throw new CancellationException(loc.getValue(L.ERROR_COMMAND_GENERATION_CANCELLED));
        }
        return permit;
    }

    /**
     * Shows the book as it is now to the user whose command changed it; an agent has no window.
     */
    protected static void changed() {
        CommandUtils.reloadManuscriptWindow();
    }

    /**
     * Asks whoever called the command to confirm; false (and the answer that nothing was changed) when they do not.
     */
    protected boolean confirmed(CommandContext context, String question) throws InterruptedException {
        if (context.confirm(question)) {
            return true;
        }
        if (!context.isTerminated()) {
            context.setReturn(loc.getValue(L.MSG_COMMAND_NOTHING_CHANGED));
        }
        return false;
    }
}
