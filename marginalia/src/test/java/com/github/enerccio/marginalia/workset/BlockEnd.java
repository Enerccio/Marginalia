package com.github.enerccio.marginalia.workset;

import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * What the author does after a block of the story (an act, 25 parts), as decided for the dataset (see {@code TODO.DATASET.MD}):
 * <ol>
 *   <li><b>Arc Outline, <i>Import from story</i></b> over the parts of the block (the plugin's command {@code import-arc-from-story} with {@code mapParts}, the
 *       arc and its chapters saved as <i>Finished</i>, every chapter linked to the part it begins in). The name of the arc is ignored, only the chapters are used;</li>
 *   <li><b>chapter headings in the text:</b> the first part of each chapter gets {@code # <roman numeral>. <name>} as its first line. The numbering is continuous over the whole
 *       book (the chapters of the earlier blocks count), not per arc. The heading is an edit of the author: the part is marked edited and its history gets a human version.
 *       The translations of the part move with the text (the passages are anchored and shifted by the edit);</li>
 *   <li><b>summaries</b> at the last part of each chapter (the plugin-independent command {@code regenerate-summary}, from the final text with the headings), except a very
 *       short chapter, and <b>not for the last chapter of the block</b>: it stays whole, so the next block starts with real parts in the prompt.</li>
 * </ol>
 * Everything goes through commands; the only direct call is the edit of the heading (a command for editing the text of a part with a human version does not exist).
 */
public class BlockEnd {

    private static final Gson GSON = new Gson();
    /** From this many meta summaries on, a meta summary may be merged into another one (the last resort). */
    private static final int MAX_META = 4;
    /** A chapter heading at the top of a part: {@code # IV. Name}. */
    private static final java.util.regex.Pattern HEADING = java.util.regex.Pattern.compile("\\A# [IVXLCDM]+\\. ");

    private final TranslationPass commands;
    private final ManuscriptService manuscripts;
    private final ChatMessageService messages;
    private final int minWords;
    /** How many chapters the model is asked to find in a block; null leaves it to the model (it divides where the story divides). */
    private final Integer chapterCount;
    /** The length a chapter should have, in words: the number of chapters of a block is its words divided by this (a chapter every ~1000 words is far too many). */
    private final int chapterWords;
    /**
     * The tokens the summaries of the active branch may take together in the prompt: not a fixed small cap, a share of the usable context of the book's AI (see
     * {@code workset.contextShare}). Only above it are two fresh summaries merged into a meta summary; 0 = never merge. Everything below it the prompt simply uses.
     */
    private final long summaryBudget;
    /** How many pairs of fresh summaries are merged into meta summaries at the close of a block, from the second block on (the oldest pairs first). */
    private final int metaPairsPerBlock;
    /** A summary at the end of every n-th chapter of the block (1 = each chapter, 2 = every second one); the last chapter of the block is never summarized. */
    private final int summaryEvery;

    /**
     * @param minWords     a chapter with fewer words than this gets no summary (a very short one)
     * @param chapterCount the exact number of chapters to ask for in a block, null = as the story divides
     */
    public BlockEnd(TranslationPass commands, ManuscriptService manuscripts, ChatMessageService messages, int minWords, Integer chapterCount, int chapterWords, long summaryBudget, int summaryEvery, int metaPairsPerBlock) {
        this.metaPairsPerBlock = metaPairsPerBlock;
        this.summaryEvery = summaryEvery;
        this.summaryBudget = summaryBudget;
        this.chapterCount = chapterCount;
        this.chapterWords = chapterWords;
        this.commands = commands;
        this.manuscripts = manuscripts;
        this.messages = messages;
        this.minWords = minWords;
    }

    /** Forgets the answer stored last (set by the harness); used when the model's JSON was broken, so the request is asked again instead of replayed from the cache. */
    private Runnable forgetLastAnswer = () -> {
    };

    public void forgetLastAnswerWith(Runnable forget) {
        this.forgetLastAnswer = forget;
    }

    private static final int JSON_ATTEMPTS = 4;

    /**
     * The import of the arc, asked again when the model's answer is not usable JSON (a long answer from a small model now and then has a stray brace or a missing
     * comma): the bad answer is dropped from the cache and the same request is sent once more. A refusal for the number of chapters is not retried here.
     */
    private String importArc(long bookId, Map<String, Object> args) throws Exception {
        for (int attempt = 1; ; attempt++) {
            try {
                return commands.run(manuscripts.find(bookId), "import-arc-from-story", args);
            } catch (IllegalStateException e) {
                String message = String.valueOf(e.getMessage());
                boolean broken = message.contains("Not valid JSON") || message.contains("required property");
                if (!broken || attempt >= JSON_ATTEMPTS) {
                    throw e;
                }
                System.out.println("[workset] the arc answer was not usable (" + message.substring(message.lastIndexOf(':') + 1).strip() + "), asking again (" + (attempt + 1) + "/" + JSON_ATTEMPTS + ")");
                forgetLastAnswer.run();
            }
        }
    }

    /** A chapter the model found, with the place of the part it begins in. */
    private record Found(String title, int part) {
    }

    /**
     * @param book     the book
     * @param bookId   its id, to read it again
     * @param label    names the calls of the model in the ledger ({@code label} + what it is)
     * @param from     the first part of the block (the place on the active branch, from 1)
     * @param to       the last part of the block
     * @param mark     tells the bridge what the next request is: label and kind
     * @param chain    reads the parts of the active branch, oldest first, as they are stored now
     * @param speeches counts the translated speeches of the part at a place (to prove the headings did not lose any), may be null
     * @return the report for the review (markdown)
     */
    public String run(Manuscript book, long bookId, String label, int from, int to, BiConsumer<String, String> mark, Supplier<List<ChatMessage>> chain,
                      java.util.function.IntUnaryOperator speeches) throws Exception {
        StringBuilder report = new StringBuilder();

        // ---- 1. the arc
        int before = 0;
        for (ChatMessage existing : chain.get()) {
            if (existing.getResponse() != null && HEADING.matcher(existing.getResponse()).find()) {
                before++;
            }
        }
        mark.accept(label + " arc", "outline");
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("from", from);
        args.put("to", to);
        // an exact count: given, else the words of the block over the length of a chapter (at least 2); the model alone cut 20 chapters out of 25 parts
        int blockWords = 0;
        List<ChatMessage> blockParts = chain.get();
        for (int p = from; p <= to; p++) {
            String t = blockParts.get(p - 1).getResponse();
            blockWords += t == null || t.isBlank() ? 0 : t.strip().split("\\s+").length;
        }
        int count = chapterCount != null ? chapterCount : Math.max(2, Math.round(blockWords / (float) chapterWords));
        args.put("count", count);
        args.put("state", "FINISHED");
        args.put("save", true);
        args.put("mapParts", true);
        String answer;
        String countNote = "";
        try {
            answer = importArc(bookId, args);
        } catch (IllegalStateException e) {
            // the plugin refuses an answer with another number of chapters than asked ("must have at most 4 items but found 5"): the refusal is cached like any answer, so the
            // model is asked again for the number it gave, which is a request of its own
            java.util.regex.Matcher found = java.util.regex.Pattern.compile("found (\\d+)").matcher(String.valueOf(e.getMessage()));
            if (!found.find() || Integer.parseInt(found.group(1)) == count) {
                throw e;
            }
            int given = Integer.parseInt(found.group(1));
            countNote = " The model gave " + given + " chapters for the " + count + " asked, so it was asked again for " + given + ".";
            count = given;
            args.put("count", count);
            answer = importArc(bookId, args);
        }
        JsonObject arc = GSON.fromJson(answer, JsonObject.class).getAsJsonObject("arc");
        List<Found> chapters = new ArrayList<>();
        StringBuilder dropped = new StringBuilder();
        for (JsonElement element : arc.getAsJsonArray("chapters")) {
            JsonObject row = element.getAsJsonObject();
            if (!row.has("part")) {
                throw new IllegalStateException("A chapter has no linked part (the model named a message that was not sent): " + row);
            }
            int place = row.get("part").getAsInt();
            if (!chapters.isEmpty() && place <= chapters.getLast().part()) {
                // the model gave two chapters the same beginning (or went backwards): a part has one heading, the first chapter keeps it
                dropped.append("- \"").append(row.get("title").getAsString()).append("\" (linked to part ").append(place).append(", where \"").append(chapters.getLast().title())
                        .append("\" begins) has no heading and no summary of its own\n");
                continue;
            }
            chapters.add(new Found(row.get("title").getAsString(), place));
        }
        if (chapters.isEmpty()) {
            throw new IllegalStateException("The import gave no chapters: " + answer);
        }
        report.append("**Arc Outline, import from story** (parts ").append(from).append(" to ").append(to).append(", the arc name \"").append(arc.get("title").getAsString())
                .append("\" is ignored): ").append(chapters.size()).append(" chapters (asked for ").append(count).append(", the block has about ").append(blockWords)
                .append(" words, ").append(chapterWords).append(" per chapter), ").append(before).append(" chapters in the outline before.").append(countNote).append("\n\n").append(dropped.length() == 0 ? "" : "Chapters left out because they begin where the one before does:\n\n" + dropped + "\n");

        // ---- 2. the headings
        report.append("**Headings** (continuous numbering over the book; each is an edit of the author):\n\n");
        for (int i = 0; i < chapters.size(); i++) {
            Found chapter = chapters.get(i);
            String roman = roman(before + i + 1);
            List<ChatMessage> parts = chain.get();
            ChatMessage part = parts.get(chapter.part() - 1);
            int speechesBefore = speeches == null ? -1 : speeches.applyAsInt(chapter.part());
            String text = part.getResponse() == null ? "" : part.getResponse();
            String heading = "# " + roman + ". " + chapter.title().strip();
            if (text.startsWith(heading)) {
                report.append("- part ").append(chapter.part()).append(": already has ").append(heading).append('\n');
                continue;
            }
            messages.updateResponse(manuscripts.find(bookId), messages.find(part), heading + "\n\n" + text, RevisionOrigin.AUTHOR);
            int speechesAfter = speeches == null ? -1 : speeches.applyAsInt(chapter.part());
            report.append("- part ").append(chapter.part()).append(": `").append(heading).append('`');
            if (speechesBefore >= 0) {
                report.append(" (translated speeches ").append(speechesBefore).append(" → ").append(speechesAfter).append(speechesBefore == speechesAfter ? "" : "  **LOST SOME**").append(')');
            }
            report.append('\n');
        }

        // ---- 3. the summaries
        report.append("\n**Summaries** (at the last part of each chapter; none for a chapter under ").append(minWords).append(" words, none for the last chapter of the block):\n\n");
        List<ChatMessage> parts = chain.get();
        int lastSummaryEnd = from - 1;
        if (from > 1) {
            // the last chapter of the previous block was kept whole while this block was written; it gets its own summary now, before the chapters of this block
            JsonObject listed = GSON.fromJson(commands.run(manuscripts.find(bookId), "list-summaries", Map.of()), JsonObject.class);
            int summarized = 0;
            for (JsonElement node : listed.getAsJsonArray("summaries")) {
                summarized = Math.max(summarized, node.getAsJsonObject().get("position").getAsInt());
            }
            if (summarized < from - 1) {
                int tailWords = 0;
                for (int p = summarized + 1; p <= from - 1; p++) {
                    String t = parts.get(p - 1).getResponse();
                    tailWords += t == null || t.isBlank() ? 0 : t.strip().split("\\s+").length;
                }
                String name = "the chapter kept whole from the previous block (parts " + (summarized + 1) + "-" + (from - 1) + ")";
                if (tailWords < minWords) {
                    report.append("- ").append(name).append(": ").append(tailWords).append(" words, too short for a summary\n");
                } else {
                    mark.accept(label + " summary " + (from - 1), "summary");
                    String summary = commands.run(manuscripts.find(bookId), "regenerate-summary", Map.of("part", parts.get(from - 2).getUuid()));
                    JsonObject row = GSON.fromJson(summary, JsonObject.class);
                    report.append("- ").append(name).append(": summary at part ").append(from - 1).append(", ").append(tailWords).append(" words covered").append(row != null && row.has("tokens")
                            ? ", " + row.get("tokens").getAsLong() + " tokens" : "").append(":\n\n  > ").append(row != null && row.has("text") ? row.get("text").getAsString().replace("\n", "\n  > ") : summary).append("\n\n");
                }
            }
        }
        for (int i = 0; i < chapters.size(); i++) {
            int start = chapters.get(i).part();
            int end = i + 1 < chapters.size() ? chapters.get(i + 1).part() - 1 : to;
            String name = "chapter " + roman(before + i + 1) + " (parts " + start + "-" + end + ")";
            if (i == chapters.size() - 1) {
                report.append("- ").append(name).append(": last chapter of the block, kept whole\n");
                continue;
            }
            if ((i + 1) % summaryEvery != 0) {
                report.append("- ").append(name).append(": no summary here (one at the end of every ").append(summaryEvery == 1 ? "chapter" : summaryEvery + " chapters").append(")\n");
                continue;
            }
            // the summary covers everything since the previous one, so the words are counted from there
            int words = 0;
            for (int p = lastSummaryEnd + 1; p <= end; p++) {
                String t = parts.get(p - 1).getResponse();
                words += t == null || t.isBlank() ? 0 : t.strip().split("\\s+").length;
            }
            if (words < minWords) {
                report.append("- ").append(name).append(": ").append(words).append(" words, too short for a summary\n");
                continue;
            }
            mark.accept(label + " summary " + end, "summary");
            String summary = commands.run(manuscripts.find(bookId), "regenerate-summary", Map.of("part", parts.get(end - 1).getUuid()));
            JsonObject row = GSON.fromJson(summary, JsonObject.class);
            lastSummaryEnd = end;
            report.append("- ").append(name).append(": summary at part ").append(end).append(", ").append(words).append(" words covered").append(row != null && row.has("tokens")
                    ? ", " + row.get("tokens").getAsLong() + " tokens" : "").append(":\n\n  > ").append(row != null && row.has("text") ? row.get("text").getAsString().replace("\n", "\n  > ") : summary).append("\n\n");
        }

        // meta summaries: from the second block on one pair of the oldest fresh summaries is merged (summary 1 + 2 -> meta 1), and more only if the summaries alone would
        // take more than the share of the context
        report.append(meta(book, bookId, label, mark, label.endsWith(" 1") ? 0 : metaPairsPerBlock));
        return report.toString();
    }

    /**
     * Meta summaries: when the summaries of the active branch together are more than {@code summaryBudget} tokens, the oldest two are merged into one meta summary
     * ({@code create-meta-summary}; the default meta prompt asks for about half the size), until they fit or only one is left. The numbers before and after are in the report.
     */
    public String meta(Manuscript book, long bookId, String label, BiConsumer<String, String> mark, int pairs) throws Exception {
        StringBuilder report = new StringBuilder("**Meta summaries** (").append(pairs).append(" pair(s) of the oldest fresh summaries at this block; more only above ").append(summaryBudget)
                .append(" tokens of summaries):\n\n");
        int merged = 0;
        for (int round = 0; round < 12; round++) {
            JsonObject listed = GSON.fromJson(commands.run(manuscripts.find(bookId), "list-summaries", Map.of()), JsonObject.class);
            JsonArray nodes = listed.getAsJsonArray("summaries");
            long total = 0;
            for (JsonElement node : nodes) {
                total += node.getAsJsonObject().get("tokens").getAsLong();
            }
            boolean wanted = merged < pairs || total > summaryBudget;
            report.append("- ").append(nodes.size()).append(" summaries, ").append(total).append(" tokens");
            if (!wanted) {
                report.append(merged == 0 ? ": nothing to merge at this block\n" : ": done\n");
                break;
            }
            // two FRESH summaries side by side (the list is oldest first) become a meta summary that sits on the newer one and takes the place of both; a meta summary is
            // not merged into another one, that is the last resort when there are already too many of them (a story of 100 parts never gets there)
            int pair = -1;
            for (int i = 0; i + 1 < nodes.size(); i++) {
                if ("summary".equals(nodes.get(i).getAsJsonObject().get("type").getAsString()) && "summary".equals(nodes.get(i + 1).getAsJsonObject().get("type").getAsString())) {
                    pair = i;
                    break;
                }
            }
            long metas = 0;
            for (JsonElement node : nodes) {
                metas += "meta".equals(node.getAsJsonObject().get("type").getAsString()) ? 1 : 0;
            }
            if (pair < 0) {
                if (metas < MAX_META) {
                    report.append(": no two plain summaries are side by side and there are only ").append(metas).append(" meta summaries (merging one into another is the last resort, from ")
                            .append(MAX_META).append("): nothing merged\n");
                    break;
                }
                pair = 0;
                report.append(": LAST RESORT, ").append(metas).append(" meta summaries already");
            }
            JsonObject older = nodes.get(pair).getAsJsonObject();
            JsonObject newer = nodes.get(pair + 1).getAsJsonObject();
            report.append(": merging the summaries at parts ").append(older.get("position").getAsInt()).append(" (").append(older.get("tokens").getAsLong()).append(" tokens) and ")
                    .append(newer.get("position").getAsInt()).append(" (").append(newer.get("tokens").getAsLong()).append(" tokens)\n");
            mark.accept(label + " meta " + newer.get("position").getAsInt(), "summary");
            String result = commands.run(manuscripts.find(bookId), "create-meta-summary", Map.of("from", newer.get("handle").getAsString(), "to", older.get("handle").getAsString()));
            merged++;
            JsonObject row = GSON.fromJson(result, JsonObject.class);
            report.append("  - meta summary at part ").append(newer.get("position").getAsInt()).append(row != null && row.has("tokens") ? ", " + row.get("tokens").getAsLong() + " tokens" : "")
                    .append(":\n\n    > ").append(row != null && row.has("text") ? row.get("text").getAsString().replace("\n", "\n    > ") : result).append("\n\n");
        }
        return report.toString();
    }

    private static int countChapters(JsonElement element) {
        int count = 0;
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            for (var entry : object.entrySet()) {
                if (entry.getKey().equals("chapters") && entry.getValue().isJsonArray()) {
                    count += entry.getValue().getAsJsonArray().size();
                } else {
                    count += countChapters(entry.getValue());
                }
            }
        } else if (element.isJsonArray()) {
            for (JsonElement item : (JsonArray) element) {
                count += countChapters(item);
            }
        }
        return count;
    }

    /** I, II, III... IV, IX, XL: the number of a chapter as the book writes it. */
    static String roman(int number) {
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                out.append(symbols[i]);
                number -= values[i];
            }
        }
        return out.toString();
    }

    /** Reads JSON text, a tolerant stand-in for the one line it replaces. */
    private static final class JsonParserHolder {
        static JsonElement parse(String json) {
            return com.google.gson.JsonParser.parseString(json == null || json.isBlank() ? "{}" : json);
        }
    }
}
