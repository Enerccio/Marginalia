package com.github.enerccio.marginalia.domain.service.impl.commands.lorebook.read;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.utils.RegexTimeoutException;
import com.github.enerccio.marginalia.utils.TimeSafeText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * {@code /search-lore query [regex [limit]]}: the entries of all the lorebooks of the open book whose name, text or
 * comment contain the query (case does not matter), with a snippet. Changes nothing; the answer is JSON text, see
 * {@link LorebookReadSupport}.
 */
public class SearchLoreCommand extends LorebookReadSupport {

    static final String NAME = "search-lore";

    /** The characters around a match in a snippet. */
    private static final int SNIPPET_RADIUS = 40;

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SEARCH_LORE,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_SEARCH_LORE, llmDescription = L.DESC_COMMAND_SEARCH_LORE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "query", type = String.class, description = L.DESC_COMMAND_LORE_QUERY_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SEARCH_LORE_RETURN))
    public void searchLore(CommandContext context) throws Exception {
        searchLore(context, context.get("query", String.class), null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SEARCH_LORE,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_SEARCH_LORE, llmDescription = L.DESC_COMMAND_SEARCH_LORE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "query", type = String.class, description = L.DESC_COMMAND_LORE_QUERY_PARAM),
                    @CommandParameter(name = "regex", type = Boolean.class, description = L.DESC_COMMAND_REGEX_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SEARCH_LORE_RETURN))
    public void searchLoreRegex(CommandContext context) throws Exception {
        searchLore(context, context.get("query", String.class), context.get("regex", Boolean.class), null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SEARCH_LORE,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_SEARCH_LORE, llmDescription = L.DESC_COMMAND_SEARCH_LORE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "query", type = String.class, description = L.DESC_COMMAND_LORE_QUERY_PARAM),
                    @CommandParameter(name = "regex", type = Boolean.class, description = L.DESC_COMMAND_REGEX_PARAM),
                    @CommandParameter(name = "limit", type = Integer.class, description = L.DESC_COMMAND_LIMIT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SEARCH_LORE_RETURN))
    public void searchLoreLimit(CommandContext context) throws Exception {
        searchLore(context, context.get("query", String.class), context.get("regex", Boolean.class), context.get("limit", Integer.class));
    }

    private void searchLore(CommandContext context, String query, Boolean regex, Integer limit) throws Exception {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_SEARCH_TEXT_EMPTY));
        }
        boolean isRegex = Boolean.TRUE.equals(regex);
        Pattern pattern;
        try {
            int flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
            pattern = Pattern.compile(isRegex ? query : Pattern.quote(query), isRegex ? flags | Pattern.MULTILINE : flags);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_INVALID_REGEX), e.getDescription()), e);
        }

        Lore lore = new Lore(new Book(context));
        // one time for the whole search, a pattern that backtracks is stopped instead of holding the thread
        long deadline = TimeSafeText.deadlineIn(TimeSafeText.timeoutMillis());
        try {
            answer(context, search(lore, pattern, limit(limit), deadline));
        } catch (RegexTimeoutException e) {
            throw new IllegalStateException(String.format(loc.getValue(L.ERROR_REGEX_TIMEOUT), TimeSafeText.timeoutMillis() / 1000), e);
        }
    }

    private JsonObject search(Lore lore, Pattern pattern, int max, long deadline) throws Exception {
        int found = 0;
        JsonArray hits = new JsonArray();
        for (Lorebook book : lore.books) {
            int number = 0;
            for (LorebookEntry entry : lore.entries(book)) {
                number++;
                String[] texts = {entry.getName(), entry.getPayload(), entry.getComment()};
                String[] fields = {"name", "text", "comment"};
                JsonArray matchedIn = new JsonArray();
                String snippet = null;
                for (int f = 0; f < texts.length; f++) {
                    String around = snippet(pattern, texts[f], deadline);
                    if (around != null) {
                        matchedIn.add(fields[f]);
                        if (snippet == null) {
                            snippet = around;
                        }
                    }
                }
                if (matchedIn.isEmpty()) {
                    continue;
                }
                found++;
                if (hits.size() < max) {
                    JsonObject row = new JsonObject();
                    row.addProperty("lorebook", lore.number(book));
                    row.addProperty("entry", number);
                    row.addProperty("handle", entry.getUuid());
                    row.addProperty("name", entry.getName());
                    row.add("matchedIn", matchedIn);
                    row.addProperty("snippet", snippet);
                    hits.add(row);
                }
            }
        }
        JsonObject result = new JsonObject();
        result.addProperty("found", found);
        result.add("hits", hits);
        return result;
    }

    /** The text around the first match, null when there is none. */
    private static String snippet(Pattern pattern, String text, long deadline) {
        if (text == null) {
            return null;
        }
        Matcher matcher = TimeSafeText.matcher(pattern, text, deadline);
        while (matcher.find()) {
            if (matcher.end() == matcher.start()) {
                continue;
            }
            int start = Math.max(0, matcher.start() - SNIPPET_RADIUS);
            int end = Math.min(text.length(), matcher.end() + SNIPPET_RADIUS);
            return (start > 0 ? "…" : "") + text.substring(start, end).replaceAll("\\s+", " ").strip()
                    + (end < text.length() ? "…" : "");
        }
        return null;
    }
}
