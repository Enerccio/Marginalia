package com.github.enerccio.marginalia.workset;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CommandScopes;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The knowledge step of the author's loop: the Knowledge Tracker (plugin, started by {@link PluginHost}) reads each finished part and the next prompt carries
 * what the characters know and do not know. Driven through the plugin's <b>commands</b> ({@code add-tracked-character}, {@code add-knowledge},
 * {@code knowledge-process}); the only thing written directly is the settings of the book, which the plugin has no command for (they are part of its state in
 * the book's attributes, where its own settings tab stores them).
 * <p>
 * <b>Start state by hand, the rest by the model.</b> {@code knowledge_seed.json} has the items of part 1 ({@code role: start}: what the characters know when the
 * story begins, the author enters them) and the items the story should lead to ({@code role: expected}). Only the start items are entered; the expected ones are
 * never inserted, they are shown in the review next to what the model found, for the author to compare.
 * <p>
 * The extraction runs after the redraft and before the translation (the order of the author's loop), once per part, with the book's model, and its changes are
 * applied at once and logged ({@code askBeforeApplying} off). {@code autoProcess} stays off: the plugin would otherwise do the same inside the next generation,
 * where the call could not be told apart from the part's own.
 */
public class KnowledgePass {

    private static final Gson GSON = new Gson();
    /** The key of the plugin's state in the attributes of the book ({@code KnowledgeState.KEY}). */
    static final String KEY = "com.github.enerccio.marginalia.extensions.knowledgetracker";

    /** A tracked character: the name and the other names the story calls it by. */
    record Tracked(String name, String aliases) {
    }

    /** The cast the tracker follows (the lorebook's people; what a character does not know is the point of the tracker in this story). */
    static final List<Tracked> CAST = List.of(
            new Tracked("Tomáš", "Tomáš Hájek, Tomas"),
            new Tracked("Mizuki", "Mizuki Hayashida"),
            new Tracked("Saellen", ""),
            new Tracked("Aelmoria", "the reed-mother"),
            new Tracked("Steing", "the assessor"),
            new Tracked("Mangel", "Lieutenant Mangel"),
            new Tracked("Paskal", ""));

    private final TranslationPass commands;
    private final ManuscriptService manuscripts;
    private JsonArray seedItems = new JsonArray();

    public KnowledgePass(CommandService commandService, CommandScopes scopes, ManuscriptService manuscripts) {
        this.commands = new TranslationPass(commandService, scopes);
        this.manuscripts = manuscripts;
    }

    /**
     * The tracked characters, the start items and the settings of the book. Safe to run every time: a character that is tracked and an item the character has
     * are left alone.
     */
    public String setup(Manuscript book, Path seedFile) throws Exception {
        JsonObject seed = GSON.fromJson(Files.readString(seedFile, StandardCharsets.UTF_8), JsonObject.class);
        seedItems = seed.getAsJsonArray("items");
        StringBuilder report = new StringBuilder();

        // the settings first: the block in the prompt, the model reads each part, its changes are applied and logged
        manuscripts.update(manuscripts.find(book), b -> {
            JsonObject attributes = b.getAttributes();
            JsonObject state = attributes.has(KEY) ? attributes.getAsJsonObject(KEY).deepCopy() : new JsonObject();
            JsonObject settings = state.has("settings") ? state.getAsJsonObject("settings") : new JsonObject();
            settings.addProperty("inPrompt", true);
            settings.addProperty("closedWorld", true);
            settings.addProperty("autoProcess", false);
            settings.addProperty("extractWithModel", true);
            settings.addProperty("askBeforeApplying", false);
            settings.addProperty("partsPerRun", 1);
            state.add("settings", settings);
            attributes.add(KEY, state);
        });

        String tracked = commands.run(book, "list-tracked-characters", Map.of());
        for (Tracked t : CAST) {
            if (!TranslationPass.hasName(tracked, t.name())) {
                Map<String, Object> args = new LinkedHashMap<>();
                args.put("name", t.name());
                if (!t.aliases().isEmpty()) {
                    args.put("aliases", t.aliases());
                }
                commands.run(book, "add-tracked-character", args);
                report.append("tracked ").append(t.name()).append('\n');
            }
        }
        int entered = 0;
        for (JsonElement element : seedItems) {
            JsonObject item = element.getAsJsonObject();
            if (!"start".equals(item.get("role").getAsString())) {
                continue; // what the story should lead to is checked, never inserted
            }
            String character = item.get("character").getAsString();
            String text = item.get("text").getAsString();
            String existing = commands.run(book, "list-knowledge", Map.of("character", character));
            if (existing.contains(text)) {
                continue;
            }
            commands.run(book, "add-knowledge", Map.of("character", character, "text", text,
                    "kind", item.get("kind").getAsString().equals("DOES_NOT_KNOW") ? "does-not-know" : "knows"));
            entered++;
        }
        report.append("start items entered by hand: ").append(entered).append('\n');
        remember(book);
        return report.toString();
    }

    /**
     * Reads the next unread part (the one just finished) with the model and applies what it finds.
     *
     * @return the answer of the command, JSON text
     */
    public String process(Manuscript book) throws Exception {
        return commands.run(book, "knowledge-process", Map.of());
    }

    /** One item as the plugin lists it: what is written about whom, and whether it still holds. */
    private record Item(String kind, String text, boolean active, String origin) {
        String key() {
            return kind + "|" + text;
        }
    }

    /** What the plugin held after the previous part (or the start state), per character, to tell what a part changed. */
    private Map<String, List<Item>> previous = new LinkedHashMap<>();

    private Map<String, List<Item>> read(Manuscript book) throws Exception {
        Map<String, List<Item>> now = new LinkedHashMap<>();
        for (Tracked t : CAST) {
            JsonObject row = GSON.fromJson(commands.run(book, "list-knowledge", Map.of("character", t.name())), JsonObject.class);
            JsonArray items = row != null && row.has("items") ? row.getAsJsonArray("items") : new JsonArray();
            List<Item> list = new ArrayList<>();
            for (JsonElement element : items) {
                JsonObject item = element.getAsJsonObject();
                list.add(new Item(item.get("kind").getAsString(), item.get("text").getAsString(), "active".equals(item.get("state").getAsString()),
                        item.has("origin") ? item.get("origin").getAsString() : ""));
            }
            now.put(t.name(), list);
            JsonArray met = row != null && row.has("met") ? row.getAsJsonArray("met") : new JsonArray();
            met.forEach(m -> list.add(new Item("met", m.getAsString(), true, "")));
        }
        return now;
    }

    /** Remembers what the plugin holds now as the point the next part is compared with. */
    public void remember(Manuscript book) throws Exception {
        previous = read(book);
    }

    /**
     * What the part changed (items that are new, items that stopped holding, people met) and everything the plugin holds now, per character, for the review.
     * The comparison is with the state after the previous part, or the start state for the first.
     */
    public String state(Manuscript book) throws Exception {
        Map<String, List<Item>> now = read(book);
        StringBuilder changes = new StringBuilder();
        StringBuilder all = new StringBuilder();
        for (var entry : now.entrySet()) {
            String who = entry.getKey();
            Map<String, Item> before = new LinkedHashMap<>();
            previous.getOrDefault(who, List.of()).forEach(i -> before.put(i.key(), i));
            for (Item item : entry.getValue()) {
                Item old = before.get(item.key());
                if (old == null) {
                    changes.append("+ ").append(who).append(item.kind().equals("met") ? " has met " : item.kind().equals("knows") ? " knows: " : " does not know: ")
                            .append(item.text()).append(item.active() ? "" : "  (already old)").append(item.origin().isEmpty() ? "" : "  [" + item.origin() + "]").append('\n');
                } else if (old.active() && !item.active()) {
                    changes.append("- ").append(who).append(item.kind().equals("knows") ? " knew: " : " did not know: ").append(item.text()).append("  (no longer holds)\n");
                }
            }
            all.append("**").append(who).append("**\n");
            for (String kind : List.of("knows", "does-not-know", "met")) {
                List<String> texts = entry.getValue().stream().filter(i -> i.kind().equals(kind) && i.active()).map(Item::text).toList();
                if (!texts.isEmpty()) {
                    all.append("  - ").append(kind.equals("knows") ? "knows" : kind.equals("met") ? "has met" : "does not know").append(": ").append(String.join("; ", texts)).append('\n');
                }
            }
            if (entry.getValue().stream().noneMatch(Item::active)) {
                all.append("  - (nothing yet)\n");
            }
            long old = entry.getValue().stream().filter(i -> !i.active()).count();
            if (old > 0) {
                all.append("  - ").append(old).append(" old item(s) kept in the history\n");
            }
        }
        previous = now;
        return "Changed by this part:\n\n" + (changes.length() == 0 ? "(nothing)\n" : changes) + "\nTracked now (what holds):\n\n" + all;
    }

    /** The items the seed expects the model to find at this part, for the review (never inserted). */
    public String expectedAt(int part) {
        List<String> lines = new ArrayList<>();
        for (JsonElement element : seedItems) {
            JsonObject item = element.getAsJsonObject();
            if ("expected".equals(item.get("role").getAsString()) && item.get("part").getAsInt() == part) {
                lines.add("- " + item.get("character").getAsString() + " " + (item.get("kind").getAsString().equals("DOES_NOT_KNOW") ? "does not know" : "knows")
                        + ": " + item.get("text").getAsString());
            }
        }
        return lines.isEmpty() ? "(nothing expected at this part)" : String.join("\n", lines);
    }
}
