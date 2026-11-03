package com.github.enerccio.marginalia.domain.service.impl.generation;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.CommandScopes;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.domain.templates.CommandMacroRunner;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Runs the commands of the {@code {{command::name::argument}}} macros of one generation, lazily: a command runs when the
 * template reaches the macro, not before, and only a command that is {@link CommandDefinition#availableInMacros() made
 * for macros} (which is read only) can be run at all.
 * <p>
 * The answer is kept, as a cache, in the attributes of the part the generation continues from (the one before the part
 * being generated), under {@link #ATTRIBUTE_KEY} and the name and the arguments of the call. The estimation render and the
 * real render of a generation, the lorebook entries, a second generation from the same part (after a failed one) read it
 * instead of running the command again. A regenerate and a swipe ask for a fresh answer: they ignore what is kept, and
 * when the first command of such a generation answers the old answers are dropped. A command that fails or runs out of
 * time renders empty and is not kept, so it is tried again.
 * <p>
 * The macro runs in the worker thread of the generation, which waits for an {@code async} command to answer (at most
 * {@link #TIMEOUT_SECONDS} seconds).
 */
public class CommandMacros implements CommandMacroRunner {
    private static final Logger log = LoggerFactory.getLogger(CommandMacros.class);

    /**
     * Key in the attributes of the part under which the answers are kept: an object with the call as the key.
     */
    public static final String ATTRIBUTE_KEY = "commandMacroCache";

    /**
     * How long the generation waits for a command.
     */
    public static final long TIMEOUT_SECONDS = 120;

    /**
     * What the macro renders for a command that does not exist or is not made for macros - a typo in the template.
     */
    static final String UNKNOWN_COMMAND = "Error";

    private static final char SEPARATOR = '\u0001';

    private final CommandService commandService;
    private final CommandScopes commandScopes;
    private final ChatMessageService chatMessageService;
    private final Manuscript manuscript;
    private final ChatMessage anchor;
    private final boolean refresh;

    /**
     * The answers of this generation, so the same call is not run twice even when nothing can be kept (no part yet, the
     * save failed) or when it is a refresh.
     */
    private final Map<String, String> answers = new HashMap<>();

    /**
     * What was kept by an earlier generation, null when nothing or when this one refreshes.
     */
    private final JsonObject kept;

    private boolean dropped;

    /**
     * @param anchor  the part the generation continues from, {@code null} when the book has none (nothing is kept)
     * @param refresh true for a regenerate and a swipe
     */
    public CommandMacros(CommandService commandService, CommandScopes commandScopes, ChatMessageService chatMessageService,
                         Manuscript manuscript, ChatMessage anchor, boolean refresh) {
        this.commandService = commandService;
        this.commandScopes = commandScopes;
        this.chatMessageService = chatMessageService;
        this.manuscript = manuscript;
        this.anchor = anchor;
        this.refresh = refresh;
        this.kept = refresh ? null : keptOf(anchor);
    }

    private static JsonObject keptOf(ChatMessage anchor) {
        JsonObject attributes = anchor == null ? null : anchor.getAttributes();
        JsonElement element = attributes == null ? null : attributes.get(ATTRIBUTE_KEY);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    @Override
    public synchronized String run(String commandName, List<String> arguments) {
        String key = commandName + SEPARATOR + String.join(String.valueOf(SEPARATOR), arguments);
        String answer = answers.get(key);
        if (answer != null) {
            return answer;
        }
        if (!allowed(commandName)) {
            log.warn("The template runs {}, which is not a command made for macros", commandName);
            return UNKNOWN_COMMAND;
        }
        if (kept != null && kept.has(key) && kept.get(key).isJsonPrimitive()) {
            answer = kept.get(key).getAsString();
            answers.put(key, answer);
            return answer;
        }

        try {
            answer = execute(commandName, arguments);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        } catch (Exception e) {
            log.warn("The command {} of a macro failed: {}", commandName, e.getMessage(), e);
            return "";
        }
        answers.put(key, answer);
        keep(key, answer);
        return answer;
    }

    private boolean allowed(String commandName) {
        List<CommandDefinition> definitions = commandService.getCommands(commandName);
        return !definitions.isEmpty() && definitions.stream().allMatch(CommandDefinition::availableInMacros);
    }

    private String execute(String commandName, List<String> arguments) throws Exception {
        var run = commandService.executePositional(commandName, new ArrayList<>(arguments), false, commandScopes.forManuscript(manuscript));
        try {
            return text(run.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        } catch (TimeoutException e) {
            run.cancel(true);
            throw e;
        }
    }

    private static String text(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof JsonElement element) {
            return element.isJsonNull() ? "" : element.isJsonPrimitive() ? element.getAsString() : element.toString();
        }
        return value.toString();
    }

    /**
     * Writes the answer to the part; the generation goes on without it when that fails.
     */
    private void keep(String key, String answer) {
        if (anchor == null || anchor.getId() == null) {
            return;
        }
        try {
            ChatMessage part = chatMessageService.find(anchor.getId());
            if (part == null) {
                return;
            }
            JsonObject attributes = part.getAttributes();
            if (attributes == null) {
                attributes = new JsonObject();
                part.setAttributes(attributes);
            }
            JsonObject cache = attributes.has(ATTRIBUTE_KEY) && attributes.get(ATTRIBUTE_KEY).isJsonObject()
                    ? attributes.getAsJsonObject(ATTRIBUTE_KEY) : new JsonObject();
            if (refresh && !dropped) {
                // the first fresh answer replaces everything the earlier generations kept
                cache = new JsonObject();
                dropped = true;
            }
            cache.addProperty(key, answer);
            attributes.add(ATTRIBUTE_KEY, cache);
            chatMessageService.save(part);
        } catch (Exception e) {
            log.warn("Can't keep the answer of a command macro: {}", e.getMessage(), e);
        }
    }
}
