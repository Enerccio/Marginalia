package com.github.enerccio.marginalia.workset;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CommandScopes;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.command.CommandRun;
import com.github.enerccio.marginalia.domain.service.command.CommandScope;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * The last step of the author's loop for a part: the four languages of the story, translated by the two translator plugins through <b>their commands</b>
 * ({@code conlang-*}, {@code reallang-*}), the way the command bar and the agents use them. The plugins must be loaded ({@link PluginHost}); nothing of
 * them is called directly.
 * <ul>
 *   <li><b>Vrakht</b> and <b>Aethel-Vael</b> by the Conlang Translator: the languages are imported from the JSON the plugin's converters make from
 *       {@code vrakht.md} and {@code aethel-vael.md} (the plugin's own import format), so the rules and the lexicon are the author's;</li>
 *   <li><b>Japanese</b> and <b>Czech</b> by the Real Language Translator: two languages in one book, each with its own speakers.</li>
 * </ul>
 * One request per language and part. The Conlang Translator is asked for every part (as its automatic mode would), the Real Language Translator only for the
 * languages the beat of the part has (its {@code languages} codes), see {@code workset.translate.all}: a model call costs time and quota.
 */
public class TranslationPass {

    /** One of the four languages: what it is called in the library, which plugin owns it and which codes of the beats stand for it. */
    public record Lang(String name, String plugin, Set<String> codes) {

        public String command(String suffix) {
            return plugin + "-" + suffix;
        }
    }

    public static final Lang VRAKHT = new Lang("Vrakht", "conlang", Set.of("V", "v", "V+"));
    public static final Lang AETHEL_VAEL = new Lang("Aethel-Vael", "conlang", Set.of("A"));
    public static final Lang JAPANESE = new Lang("Japanese", "reallang", Set.of("J", "Jm"));
    public static final Lang CZECH = new Lang("Czech", "reallang", Set.of("C"));

    private static final Gson GSON = new Gson();

    /** A character of the speakers of a language: the name, other names, the gender and how the character speaks. */
    public record Speaker(String name, String aliases, String gender, String voice) {
    }

    /**
     * The "Names" rule of the Japanese language: the preset's text plus the fixed spelling of every name of the story, so a name is written the same way in every part
     * (the model chose again each time; the plugin has no glossary yet, see its TODO). It is for the translator only: the writer's lorebook must not hold kana, the Style
     * entry forbids the model to write any. Tomas is what the first runs wrote, kept. The Japanese characters' forms are proposals (the author decides the kanji of Mizuki).
     */
    static final String JAPANESE_NAMES = "Names of people and places keep their identity: a Japanese name is written the way a Japanese text would write it, a foreign name in katakana. "
            + "Family name first for Japanese characters unless the story orders the names otherwise. "
            + "Write the names of this story ALWAYS in exactly the form below, in every speech, never in Latin letters and never another spelling: "
            + "Tomáš = トマーシュ (Tomáš Hájek = トマーシュ・ハーイェク); Mizuki = ミズキ (Mizuki Hayashida = 林田ミズキ); "
            + "Saellen = サエレン; Aelmoria = アエルモリア; Steing = シュタイング; Paskal = パスカル; Mangel = マンゲル; Frecht = フレヒト; Kestim = ケスティム; Roid = ロイド; "
            + "Lyrshara = リュルシャラ; Vrakht = ヴラフト; Aethel-Vael = アーセル＝ヴァエル; the Dominion = ドミニオン; "
            + "Silvakhor = シルヴァコール; Kaelthal = カエルタール; the Lyrasil = リュラシル川; Fort Stein-krog = シュタイン＝クローク砦; "
            + "Zint-karn = ツィント＝カルン; Vat-veg = ヴァト＝ヴェグ; Zorg-ort = ゾルグ＝オルト; Prague = プラハ.";

    /** Who speaks each language in this story (mode "only", listed = speaks it): set on the book, and the reason to ask a translator for a part. */
    public static final Map<Lang, List<Speaker>> SPEAKERS = Map.of(
            VRAKHT, List.of(
                    new Speaker("Steing", "Steing", "male", "Life-tax assessor, thin, fifty-one. Speaks the Dominion's Vrakht like a ledger clerk; uses the Hoh tone (patronizing) even on friends."),
                    new Speaker("Paskal", "", "male", "Conscript of nineteen from the far steppe, homesick, cheerful, plain soldier's speech."),
                    new Speaker("Mangel", "Lieutenant Mangel", "female", "Officer of twenty-five, straight-backed. Gna (polite) to her commander, Bef (command) to everyone else."),
                    new Speaker("Frecht", "Commander Frecht", "male", "Commander, fifty-eight, tired and clever, speaks little and plainly."),
                    new Speaker("Kestim", "Inspector Kestim", "female", "Auditor of the capital, dry, polite, never raises her voice."),
                    new Speaker("Roid", "", "", "Surgeon of the fort."),
                    new Speaker("Saellen", "", "female", "Ferrywoman of the Lyrshara; speaks market Vrakht to the soldiers, Aethel-Vael at home. Only her speech to the Dominion is Vrakht."),
                    // the people the story does not name: the hint that they speak Vrakht (it also lets the part's text bring the note into the request)
                    new Speaker("soldier", "soldiers, sentry, guard, corporal, sergeant, conscript", "", "Soldier of the Dominion garrison: speaks plain barracks Vrakht (Bef commands, short, rough); the Hoh and Droh tones to civilians."),
                    new Speaker("clerk", "clerks, scribe, ledger-keeper", "", "Clerk of the Ledger House: speaks Vrakht, dry and exact, Gna (polite) to the assessor."),
                    new Speaker("Dominion", "Vrakht-speaking, officer, official", "", "Anyone of the Dominion (an officer, an official, an unnamed man or woman of its towns) speaks Vrakht.")),
            AETHEL_VAEL, List.of(
                    new Speaker("Saellen", "", "female", "Ferrywoman of Silvakhor landing, forty-four, broad, sings under her breath, loses patience with fools and never with the hungry."),
                    new Speaker("Aelmoria", "the reed-mother", "female", "Hedge-witch of Kaelthal, sixty-eight, small, bent, very still; speaks softly and slowly."),
                    new Speaker("Lyrshara", "villager, villagers, ferryman, boatman, merchant, merchants, child, children, mother, old woman, old man", "",
                            "Any of the river people the story does not name: speaks Aethel-Vael among themselves (soft, flowing); market Vrakht only to the Dominion.")),
            JAPANESE, List.of(
                    new Speaker("Mizuki", "Mizuki Hayashida", "female", "Nurse of twenty-seven from Sendai. Natural, everyday Japanese, a little teasing with Tomáš; polite with strangers. Mutters in Japanese when alone."),
                    new Speaker("Tomáš", "Tomáš Hájek, Tomas", "male", "Czech guide of twenty-nine who learned Japanese from novels and subtitles: stiff, bookish, over-polite at the wrong moment, sometimes boyish or anime-sounding.")),
            CZECH, List.of(
                    new Speaker("Tomáš", "Tomáš Hájek, Tomas", "male", "Czech is his private language: he speaks it ONLY when the text says he mutters, swears, thinks aloud or speaks Czech (informal, short). "
                            + "A line of his that the text does not mark is English (with Mizuki or the locals) or Japanese (with Mizuki), never Czech; a name he repeats is not translated.")));

    private final CommandService commands;
    private final CommandScopes scopes;

    public TranslationPass(CommandService commands, CommandScopes scopes) {
        this.commands = commands;
        this.scopes = scopes;
    }

    /**
     * The languages to ask for a part, in the order of the plugins given (the order of the pass), each plugin's languages in the order Vrakht, Aethel-Vael,
     * Japanese, Czech. A language is asked when somebody who speaks it is in the scene (the point of view character or one of the present ones, by name or
     * alias), so the translator also finds the Czech a model slips into a scene nobody planned it for; or when the beat has it (its codes), which covers the
     * speakers the story does not name ("a soldier"); or when the plugin is {@code ungated}.
     *
     * @param plugins  the translator plugins that are loaded, in the order to run them
     * @param codes    the language codes of the beat
     * @param present  the characters present, as the beat writes them
     * @param pov      the point of view character
     * @param ungated  the plugins that are asked for every part whatever the part says
     */
    public static List<Lang> languagesFor(List<String> plugins, List<String> codes, String present, String pov, Set<String> ungated) {
        return plugins.stream().flatMap(plugin -> List.of(VRAKHT, AETHEL_VAEL, JAPANESE, CZECH).stream().filter(l -> l.plugin().equals(plugin)))
                .filter(l -> ungated.contains(l.plugin()) || codes != null && codes.stream().anyMatch(l.codes()::contains) || speakerIsIn(l, present, pov)).toList();
    }

    /** Whether one of the speakers of the language is in the scene. */
    static boolean speakerIsIn(Lang language, String present, String pov) {
        String scene = ((present == null ? "" : present) + ", " + (pov == null ? "" : pov)).toLowerCase(java.util.Locale.ROOT);
        for (Speaker speaker : SPEAKERS.get(language)) {
            if (scene.contains(speaker.name().toLowerCase(java.util.Locale.ROOT))) {
                return true;
            }
            for (String alias : speaker.aliases().split(",")) {
                if (!alias.isBlank() && scene.contains(alias.strip().toLowerCase(java.util.Locale.ROOT))) {
                    return true;
                }
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------------------------------------------ commands

    /** Runs a command for the book and waits for its answer. */
    public String run(Manuscript book, String command, Map<String, Object> arguments) throws Exception {
        CommandScope scope = scopes.forManuscript(book);
        CommandRun run = commands.execute(command, arguments, false, scope);
        Object result;
        try {
            result = run.get(30, TimeUnit.MINUTES);
        } catch (java.util.concurrent.ExecutionException e) {
            throw new IllegalStateException(command + " " + arguments + " failed: " + e.getCause(), e.getCause());
        }
        return result == null ? "" : result.toString();
    }

    private static Map<String, Object> args(Object... pairs) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((String) pairs[i], pairs[i + 1]);
        }
        return map;
    }

    /** Whether a JSON answer holds an object with the property {@code name} equal to the value (anywhere in it). */
    static boolean hasName(String json, String name) {
        try {
            return hasName(JsonParser.parseString(json), name);
        } catch (RuntimeException e) {
            return json.contains("\"" + name + "\"");
        }
    }

    private static boolean hasName(JsonElement element, String name) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("name") && object.get("name").isJsonPrimitive() && object.get("name").getAsString().equalsIgnoreCase(name)) {
                return true;
            }
            return object.entrySet().stream().anyMatch(e -> hasName(e.getValue(), name));
        }
        if (element.isJsonArray()) {
            for (JsonElement item : element.getAsJsonArray()) {
                if (hasName(item, name)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------------------------------------------ set-up

    /**
     * Gives the library of the user the four languages and the book its language settings and speakers, for the plugins that are loaded. Safe to run again
     * (every run does): what exists is found by name and the settings are set again to the same values.
     *
     * @param skeletonDir where {@code vrakht.json} and {@code aethel_vael.json} are; made from the plugin's converters when missing
     * @param toolsDir    {@code marginalia/plugins/conlang-translator/tools}
     */
    public String setup(Manuscript book, List<String> loadedTranslators, Path skeletonDir, Path toolsDir) throws Exception {
        StringBuilder report = new StringBuilder();
        if (loadedTranslators.contains("conlang")) {
            String list = run(book, "conlang-list", Map.of());
            importIfMissing(book, list, "Vrakht", skeletonDir.resolve("vrakht.json"), toolsDir.resolve("vrakht_to_json.py"), report);
            importIfMissing(book, list, "Aethel-Vael", skeletonDir.resolve("aethel_vael.json"), toolsDir.resolve("aethel_vael_to_json.py"), report);
            run(book, "conlang-set-book-language", args("language", "Vrakht"));
            run(book, "conlang-add-book-language", args("language", "Aethel-Vael"));
            speakers(book, "conlang", "Vrakht", SPEAKERS.get(VRAKHT));
            speakers(book, "conlang", "Aethel-Vael", SPEAKERS.get(AETHEL_VAEL));
            report.append("conlang: Vrakht, Aethel-Vael set on the book\n");
        }
        if (loadedTranslators.contains("reallang")) {
            String list = run(book, "reallang-list", Map.of());
            if (!hasName(list, "Japanese")) {
                // all nine names (the overload is found by the exact set); null keeps what the preset has
                run(book, "reallang-create", args("name", "Japanese", "preset", "ja-kana-kanji", "tongue", null, "script", null, "honorifics", null, "reading", null,
                        "politeness", null, "names", null, "rules", null));
                report.append("reallang: created Japanese\n");
            }
            if (!hasName(list, "Czech")) {
                run(book, "reallang-create", args("name", "Czech", "preset", "other", "tongue", "Czech", "script", "native", "honorifics", false, "reading", false,
                        "politeness", "Colloquial spoken Czech, how a man in his thirties mutters to himself: informal (tykání), short, with the swearing the English has.",
                        "names", "Keep the names of people and places as they are.", "rules", null));
                report.append("reallang: created Czech\n");
            }
            // the fixed spelling of the names, set every run (the language may exist from an earlier run); all nine names of the overload, null keeps what is there
            run(book, "reallang-update", args("language", "Japanese", "name", null, "tongue", null, "script", null, "honorifics", null, "reading", null,
                    "politeness", null, "names", JAPANESE_NAMES, "rules", null));
            run(book, "reallang-set-book-language", args("language", "Japanese"));
            run(book, "reallang-add-book-language", args("language", "Czech"));
            run(book, "reallang-set-speakers-mode", args("mode", "only", "language", "Japanese"));
            run(book, "reallang-set-speakers-mode", args("mode", "only", "language", "Czech"));
            for (Speaker s : SPEAKERS.get(JAPANESE)) {
                setSpeaker(book, "reallang", "Japanese", s);
            }
            for (Speaker s : SPEAKERS.get(CZECH)) {
                setSpeaker(book, "reallang", "Czech", s);
            }
            report.append("reallang: Japanese, Czech set on the book\n");
        }
        return report.toString();
    }

    private void speakers(Manuscript book, String plugin, String language, List<Speaker> speakers) throws Exception {
        run(book, plugin + "-set-speakers-mode", args("mode", "only", "language", language));
        for (Speaker s : speakers) {
            setSpeaker(book, plugin, language, s);
        }
    }

    private void setSpeaker(Manuscript book, String plugin, String language, Speaker s) throws Exception {
        // the overload is found by the exact set of names: all six, empty text where there is nothing to set
        Map<String, Object> arguments = args("name", s.name(), "listed", true, "aliases", s.aliases(), "gender", s.gender(), "voice", s.voice(), "language", language);
        run(book, plugin + "-set-speaker", arguments);
    }

    private void importIfMissing(Manuscript book, String list, String name, Path json, Path converter, StringBuilder report) throws Exception {
        if (hasName(list, name)) {
            return;
        }
        if (!Files.exists(json)) {
            convert(converter, json);
        }
        run(book, "conlang-import", args("document", Files.readString(json, StandardCharsets.UTF_8)));
        report.append("conlang: imported ").append(name).append(" from ").append(json.getFileName()).append('\n');
    }

    /** Makes the language file with the plugin's own converter (python3 and nothing else). */
    private static void convert(Path converter, Path output) throws IOException, InterruptedException {
        if (!Files.exists(converter)) {
            throw new IOException("There is no " + output + " and no converter " + converter + " to make it");
        }
        Process process = new ProcessBuilder("python3", converter.toAbsolutePath().toString(), "-o", output.toAbsolutePath().toString())
                .directory(converter.getParent().toFile()).redirectErrorStream(true).start();
        String out = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0 || !Files.exists(output)) {
            throw new IOException("The converter " + converter + " failed: " + out);
        }
        System.out.println("[workset] made " + output.getFileName() + " (" + out.strip().replace('\n', ' ') + ")");
    }

    // ------------------------------------------------------------------------------------------------------------ the pass

    /**
     * Translates the speech of the part (its position on the active branch, the first is 1) into the language: one request to the model.
     *
     * @return the answer of the command, as JSON text
     */
    public String translate(Manuscript book, Lang language, int position) throws Exception {
        return run(book, language.command("translate-part"), args("part", String.valueOf(position), "language", language.name()));
    }

    /** How many translated speeches the plugin holds for the part (all its languages): to prove an edit of the text did not lose any. */
    public int speechCount(Manuscript book, String plugin, int position) throws Exception {
        JsonObject object = GSON.fromJson(run(book, plugin + "-list-passages", args("part", String.valueOf(position))), JsonObject.class);
        return object != null && object.has("speeches") ? object.getAsJsonArray("speeches").size() : 0;
    }

    /** The translated speeches of the part in this language as the plugin lists them, one line each: the English and what it became. */
    public String passages(Manuscript book, Lang language, int position) throws Exception {
        String json = run(book, language.command("list-passages"), args("part", String.valueOf(position)));
        JsonObject object = GSON.fromJson(json, JsonObject.class);
        JsonArray speeches = object != null && object.has("speeches") ? object.getAsJsonArray("speeches") : new JsonArray();
        StringBuilder out = new StringBuilder();
        for (JsonElement item : speeches) {
            JsonObject row = item.getAsJsonObject();
            // the command lists every language of the plugin for the part: only the ones of this language belong under its heading
            if (row.has("language") && !row.get("language").getAsString().equalsIgnoreCase(language.name())) {
                continue;
            }
            String translated = row.has("conlang") ? row.get("conlang").getAsString() : row.has("translated") ? row.get("translated").getAsString() : "";
            out.append("- ").append(row.has("speaker") ? row.get("speaker").getAsString() : "?").append(": \"").append(row.has("english") ? row.get("english").getAsString() : "")
                    .append("\" → ").append(translated).append(row.has("register") ? "  [" + row.get("register").getAsString() + "]" : "").append('\n');
        }
        return out.length() == 0 ? "(no translated speech)\n" : out.toString();
    }
}
