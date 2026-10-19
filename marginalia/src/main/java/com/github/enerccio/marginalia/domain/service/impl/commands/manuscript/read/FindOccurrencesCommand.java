package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.ChatMessageService.MessageFulltext;
import com.github.enerccio.marginalia.domain.service.ChatMessageService.SummaryFulltext;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.utils.RegexTimeoutException;
import com.github.enerccio.marginalia.utils.TimeSafeText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * {@code /find-occurrences terms [regex [caseSensitive [summaries [allBranches [limit]]]]]}: how often and where names or words occur, counted in Java instead of in tokens; defaults: no regex, case insensitive, parts only, the active branch.
 * Changes nothing; the answer is JSON text, see {@link ManuscriptReadSupport}.
 */
public class FindOccurrencesCommand extends ManuscriptReadSupport {

    static final String NAME = "find-occurrences";

    /** The characters around a match in a snippet. */
    private static final int SNIPPET_RADIUS = 40;

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_FIND_OCCURRENCES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_FIND_OCCURRENCES, llmDescription = L.DESC_COMMAND_FIND_OCCURRENCES_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "terms", type = Object.class, description = L.DESC_COMMAND_TERMS_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_FIND_OCCURRENCES_RETURN))
    public void findOccurrences(CommandContext context) throws Exception {
        findOccurrences(context, context.get("terms", Object.class), null, null, null, null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_FIND_OCCURRENCES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_FIND_OCCURRENCES, llmDescription = L.DESC_COMMAND_FIND_OCCURRENCES_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "terms", type = Object.class, description = L.DESC_COMMAND_TERMS_PARAM),
                    @CommandParameter(name = "regex", type = Boolean.class, description = L.DESC_COMMAND_REGEX_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_FIND_OCCURRENCES_RETURN))
    public void findOccurrencesRegex(CommandContext context) throws Exception {
        findOccurrences(context, context.get("terms", Object.class), context.get("regex", Boolean.class), null, null, null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_FIND_OCCURRENCES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_FIND_OCCURRENCES, llmDescription = L.DESC_COMMAND_FIND_OCCURRENCES_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "terms", type = Object.class, description = L.DESC_COMMAND_TERMS_PARAM),
                    @CommandParameter(name = "regex", type = Boolean.class, description = L.DESC_COMMAND_REGEX_PARAM),
                    @CommandParameter(name = "caseSensitive", type = Boolean.class, description = L.DESC_COMMAND_FIND_CASE_SENSITIVE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_FIND_OCCURRENCES_RETURN))
    public void findOccurrencesCaseSensitive(CommandContext context) throws Exception {
        findOccurrences(context, context.get("terms", Object.class), context.get("regex", Boolean.class), context.get("caseSensitive", Boolean.class), null, null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_FIND_OCCURRENCES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_FIND_OCCURRENCES, llmDescription = L.DESC_COMMAND_FIND_OCCURRENCES_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "terms", type = Object.class, description = L.DESC_COMMAND_TERMS_PARAM),
                    @CommandParameter(name = "regex", type = Boolean.class, description = L.DESC_COMMAND_REGEX_PARAM),
                    @CommandParameter(name = "caseSensitive", type = Boolean.class, description = L.DESC_COMMAND_FIND_CASE_SENSITIVE_PARAM),
                    @CommandParameter(name = "summaries", type = Boolean.class, description = L.DESC_COMMAND_FIND_SUMMARIES_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_FIND_OCCURRENCES_RETURN))
    public void findOccurrencesSummaries(CommandContext context) throws Exception {
        findOccurrences(context, context.get("terms", Object.class), context.get("regex", Boolean.class), context.get("caseSensitive", Boolean.class), context.get("summaries", Boolean.class), null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_FIND_OCCURRENCES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_FIND_OCCURRENCES, llmDescription = L.DESC_COMMAND_FIND_OCCURRENCES_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "terms", type = Object.class, description = L.DESC_COMMAND_TERMS_PARAM),
                    @CommandParameter(name = "regex", type = Boolean.class, description = L.DESC_COMMAND_REGEX_PARAM),
                    @CommandParameter(name = "caseSensitive", type = Boolean.class, description = L.DESC_COMMAND_FIND_CASE_SENSITIVE_PARAM),
                    @CommandParameter(name = "summaries", type = Boolean.class, description = L.DESC_COMMAND_FIND_SUMMARIES_PARAM),
                    @CommandParameter(name = "allBranches", type = Boolean.class, description = L.DESC_COMMAND_ALL_BRANCHES_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_FIND_OCCURRENCES_RETURN))
    public void findOccurrencesAllBranches(CommandContext context) throws Exception {
        findOccurrences(context, context.get("terms", Object.class), context.get("regex", Boolean.class), context.get("caseSensitive", Boolean.class), context.get("summaries", Boolean.class), context.get("allBranches", Boolean.class), null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_FIND_OCCURRENCES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            description = L.DESC_COMMAND_FIND_OCCURRENCES, llmDescription = L.DESC_COMMAND_FIND_OCCURRENCES_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "terms", type = Object.class, description = L.DESC_COMMAND_TERMS_PARAM),
                    @CommandParameter(name = "regex", type = Boolean.class, description = L.DESC_COMMAND_REGEX_PARAM),
                    @CommandParameter(name = "caseSensitive", type = Boolean.class, description = L.DESC_COMMAND_FIND_CASE_SENSITIVE_PARAM),
                    @CommandParameter(name = "summaries", type = Boolean.class, description = L.DESC_COMMAND_FIND_SUMMARIES_PARAM),
                    @CommandParameter(name = "allBranches", type = Boolean.class, description = L.DESC_COMMAND_ALL_BRANCHES_PARAM),
                    @CommandParameter(name = "limit", type = Integer.class, description = L.DESC_COMMAND_LIMIT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_FIND_OCCURRENCES_RETURN))
    public void findOccurrencesLimit(CommandContext context) throws Exception {
        findOccurrences(context, context.get("terms", Object.class), context.get("regex", Boolean.class), context.get("caseSensitive", Boolean.class), context.get("summaries", Boolean.class), context.get("allBranches", Boolean.class), context.get("limit", Integer.class));
    }

    private void findOccurrences(CommandContext context, Object terms, Boolean regex, Boolean caseSensitive,
                                 Boolean summaries, Boolean allBranches, Integer limit) throws Exception {
        // one time for the whole search, a pattern that backtracks is stopped instead of holding the thread
        long deadline = TimeSafeText.deadlineIn(TimeSafeText.timeoutMillis());
        try {
            find(context, terms, regex, caseSensitive, summaries, allBranches, limit, deadline);
        } catch (RegexTimeoutException e) {
            throw new IllegalStateException(String.format(loc.getValue(L.ERROR_REGEX_TIMEOUT), TimeSafeText.timeoutMillis() / 1000), e);
        }
    }

    private void find(CommandContext context, Object terms, Boolean regex, Boolean caseSensitive,
                      Boolean summaries, Boolean allBranches, Integer limit, long deadline) throws Exception {
        List<String> words = new ArrayList<>();
        if (terms instanceof Collection<?> collection) {
            collection.forEach(term -> words.add(String.valueOf(term)));
        } else if (terms != null) {
            words.add(terms.toString());
        }
        words.removeIf(String::isEmpty);
        if (words.isEmpty()) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_SEARCH_TEXT_EMPTY));
        }
        boolean isRegex = Boolean.TRUE.equals(regex);
        int flags = Boolean.TRUE.equals(caseSensitive) ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        List<Pattern> patterns = new ArrayList<>();
        for (String word : words) {
            try {
                patterns.add(Pattern.compile(isRegex ? word : Pattern.quote(word), isRegex ? flags | Pattern.MULTILINE : flags));
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_INVALID_REGEX), e.getDescription()), e);
            }
        }

        Book book = new Book(context);
        boolean everywhere = Boolean.TRUE.equals(allBranches);
        // the position of a part and, when it is not on the active branch, the branch that shows it
        Map<Long, Integer> positions = new HashMap<>();
        Map<Long, Integer> branches = new HashMap<>();
        Map<Long, String> handles = new HashMap<>();
        if (everywhere) {
            StoryTree tree = book.tree();
            for (ChatMessage message : tree.byId.values()) {
                positions.put(message.getId(), tree.depth(message));
                handles.put(message.getId(), message.getUuid());
                if (!tree.activeIds.contains(message.getId())) {
                    branches.put(message.getId(), tree.branchOf(message));
                }
            }
        } else {
            List<ChatMessage> path = book.path((String) null);
            for (int i = 0; i < path.size(); i++) {
                positions.put(path.get(i).getId(), i + 1);
                handles.put(path.get(i).getId(), path.get(i).getUuid());
            }
        }

        List<MessageFulltext> texts = chatMessageService.findFulltextsContaining(book.manuscript, null);
        List<SummaryFulltext> summaryTexts = Boolean.TRUE.equals(summaries)
                ? chatMessageService.findSummaryFulltextsContaining(book.manuscript, null) : List.of();
        int max = limit(limit);

        JsonArray results = new JsonArray();
        for (int t = 0; t < words.size(); t++) {
            JsonObject row = new JsonObject();
            row.addProperty("term", words.get(t));
            JsonArray inParts = new JsonArray();
            int partsFound = 0;
            int occurrences = 0;
            for (MessageFulltext text : texts) {
                Integer position = positions.get(text.messageId());
                Match match = position == null ? null : match(patterns.get(t), text.fulltext(), deadline);
                if (match == null) {
                    continue;
                }
                partsFound++;
                occurrences += match.count();
                if (inParts.size() < max) {
                    inParts.add(hitRow(handles.get(text.messageId()), position, branches.get(text.messageId()), match));
                }
            }
            row.addProperty("parts", partsFound);
            row.addProperty("occurrences", occurrences);
            row.add("inParts", inParts);
            if (Boolean.TRUE.equals(summaries)) {
                JsonArray inSummaries = new JsonArray();
                int summariesFound = 0;
                for (SummaryFulltext text : summaryTexts) {
                    Integer position = positions.get(text.messageId());
                    Match match = position == null ? null : match(patterns.get(t), text.fulltext(), deadline);
                    if (match == null) {
                        continue;
                    }
                    summariesFound++;
                    if (inSummaries.size() < max) {
                        inSummaries.add(hitRow(handles.get(text.messageId()), position, branches.get(text.messageId()), match));
                    }
                }
                row.addProperty("summaries", summariesFound);
                row.add("inSummaries", inSummaries);
            }
            results.add(row);
        }
        JsonObject result = new JsonObject();
        result.add("terms", results);
        answer(context, result);
    }

    private record Match(int count, String snippet) {
    }

    private static Match match(Pattern pattern, String text, long deadline) {
        if (text == null) {
            return null;
        }
        Matcher matcher = TimeSafeText.matcher(pattern, text, deadline);
        int count = 0;
        String snippet = null;
        while (matcher.find()) {
            if (matcher.end() == matcher.start()) {
                continue;
            }
            if (count++ == 0) {
                int start = Math.max(0, matcher.start() - SNIPPET_RADIUS);
                int end = Math.min(text.length(), matcher.end() + SNIPPET_RADIUS);
                snippet = (start > 0 ? "…" : "") + text.substring(start, end).replaceAll("\\s+", " ").strip()
                        + (end < text.length() ? "…" : "");
            }
        }
        return count == 0 ? null : new Match(count, snippet);
    }

    private static JsonObject hitRow(String handle, int position, Integer branch, Match match) {
        JsonObject row = new JsonObject();
        row.addProperty("handle", handle);
        row.addProperty("position", position);
        if (branch != null) {
            row.addProperty("branch", branch);
        }
        row.addProperty("count", match.count());
        row.addProperty("snippet", match.snippet());
        return row;
    }
}
