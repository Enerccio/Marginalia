package com.github.enerccio.marginalia.workset;

import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CommandScopes;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The audiobook analysis of the whole book: the Audiobook plugin (started by {@link PluginHost}) reads every part and the language model of the book
 * labels who speaks each piece and how (narrator, characters, delivery, sounds), which is the script the studio shows. <b>Nothing is spoken</b>: the command
 * {@code audiobook-convert} runs with {@code what=script}, so no speech server is called. The labels and scripts are stored with the parts (the plugin's
 * stores), which is what travels in the book's export.
 * <p>
 * The plugin refuses to convert without a speech server that has a narrator voice, so {@link #setup} puts a placeholder into the user's audiobook settings
 * (an address nobody listens on, a predefined voice name). It is never contacted by this pass; it is skipped when the user already has a server.
 * <p>
 * The parts go in chunks: the command waits for its jobs, and a part is several requests to the model (the pieces are labelled in windows of 16).
 */
public class LabelPass {

    /** The key of the plugin in the attributes of the user's settings ({@code AudiobookSettings.KEY}). */
    static final String KEY = "com.github.enerccio.marginalia.extensions.audiobook";

    private final TranslationPass commands;
    private final SettingService settings;
    private final ManuscriptService manuscripts;

    public LabelPass(CommandService commandService, CommandScopes scopes, SettingService settings, ManuscriptService manuscripts) {
        this.commands = new TranslationPass(commandService, scopes);
        this.settings = settings;
        this.manuscripts = manuscripts;
    }

    /**
     * The voices of a speech server that the server has itself (its predefined ones, files like {@code Henry.wav}): the narrator, the voice of a speaker the cast does
     * not name, and a voice for each character by the name the labelling gives it.
     */
    public record Voices(String narrator, String fallback, Map<String, String> characters) {
    }

    /** The predefined voices of Chatterbox-TTS-Server given to the people of the dataset story (genders as the text has them; Roid and the unnamed speakers are a guess). */
    public static final Voices DEFAULT_VOICES = new Voices("Henry.wav", "Ryan.wav", new LinkedHashMap<>(Map.of(
            "Tomáš", "Michael.wav", "Mizuki", "Elena.wav", "Paskal", "Alexander.wav", "Steing", "Julian.wav", "Mangel", "Olivia.wav",
            "Kestim", "Gianna.wav", "Saellen", "Layla.wav", "Aelmoria", "Cora.wav", "Roid", "Thomas.wav", "Frecht", "Leonardo.wav")));

    /**
     * Points the user's audiobook settings at a real speech server: this one backend (no requests around a batch: the caller frees the card), selected, and an actor for each
     * voice. The private address check is told that the server is on the network of the machine.
     */
    public String setupSpeech(String baseUrl, String model, Voices voices) throws Exception {
        UserSetting user = settings.getOrCreate(UserSetting.class);
        JsonObject attributes = user.getAttributes() == null ? new JsonObject() : user.getAttributes();
        user.setAttributes(attributes);
        JsonObject audiobook = attributes.has(KEY) ? attributes.getAsJsonObject(KEY) : new JsonObject();
        JsonObject backend = new JsonObject();
        backend.addProperty("id", "workset-speech");
        backend.addProperty("name", "Speech server of the author");
        backend.addProperty("baseUrl", baseUrl);
        backend.addProperty("model", model);
        backend.addProperty("local", true);
        backend.addProperty("format", "mp3");
        backend.addProperty("narratorVoice", "predefined:" + voices.narrator());
        JsonArray backends = new JsonArray();
        backends.add(backend);
        audiobook.add("backends", backends);
        audiobook.addProperty("selectedBackendId", "workset-speech");
        JsonArray actors = new JsonArray();
        actors.add(actor("narrator", "Narrator", voices.narrator()));
        actors.add(actor("fallback", "Other speakers", voices.fallback()));
        voices.characters().forEach((name, file) -> actors.add(actor(name.toLowerCase(java.util.Locale.ROOT), name, file)));
        audiobook.add("actors", actors);
        attributes.add(KEY, audiobook);
        settings.save(user);
        return "speech server " + baseUrl + " (" + model + "), " + actors.size() + " actors";
    }

    private static JsonObject actor(String id, String name, String file) {
        JsonObject actor = new JsonObject();
        actor.addProperty("id", id);
        actor.addProperty("name", name);
        actor.addProperty("voice", "predefined:" + file);
        return actor;
    }

    /** The cast of the book: the narrator, the fallback and a member for each character, played by the actors of {@link #setupSpeech}. */
    public void setupCast(Manuscript book, Voices voices) throws Exception {
        JsonObject cast = new JsonObject();
        cast.addProperty("narratorActorId", "narrator");
        cast.addProperty("fallbackActorId", "fallback");
        JsonArray members = new JsonArray();
        voices.characters().keySet().forEach(name -> {
            JsonObject member = new JsonObject();
            member.addProperty("name", name);
            member.addProperty("aliases", "");
            member.addProperty("actorId", name.toLowerCase(java.util.Locale.ROOT));
            members.add(member);
        });
        cast.add("members", members);
        manuscripts.update(book, b -> {
            JsonObject attrs = b.getAttributes() == null ? new JsonObject() : b.getAttributes();
            b.setAttributes(attrs);
            JsonObject own = attrs.has("audiobook") && attrs.get("audiobook").isJsonObject() ? attrs.getAsJsonObject("audiobook") : new JsonObject();
            own.add("cast", cast);
            attrs.add("audiobook", own);
        });
    }

    /** Makes the audio of the parts {@code from..to} (the labels stored with the parts are used, the speech server speaks); the answer of the command. */
    public String convertAudio(Manuscript book, int from, int to) throws Exception {
        Map<String, Object> arguments = new LinkedHashMap<>();
        arguments.put("from", String.valueOf(from));
        arguments.put("to", String.valueOf(to));
        arguments.put("labelling", true);
        arguments.put("what", "audio");
        arguments.put("wait", true);
        return commands.run(book, "audiobook-convert", arguments);
    }

    /** Makes sure the user has a speech server (a placeholder, never called) with a narrator voice and that it is the selected one. */
    public String setup() throws Exception {
        UserSetting user = settings.getOrCreate(UserSetting.class);
        JsonObject attributes = user.getAttributes();
        if (attributes == null) {
            attributes = new JsonObject();
            user.setAttributes(attributes);
        }
        JsonObject audiobook = attributes.has(KEY) ? attributes.getAsJsonObject(KEY) : new JsonObject();
        JsonArray backends = audiobook.has("backends") ? audiobook.getAsJsonArray("backends") : new JsonArray();
        if (!backends.isEmpty() && audiobook.has("selectedBackendId") && !audiobook.get("selectedBackendId").isJsonNull()) {
            return "the user already has a speech server, left alone";
        }
        JsonObject backend = new JsonObject();
        backend.addProperty("id", "workset-no-speech");
        backend.addProperty("name", "Workset (labelling only, never called)");
        backend.addProperty("baseUrl", "http://127.0.0.1:9");
        backend.addProperty("model", "chatterbox-turbo");
        backend.addProperty("narratorVoice", "predefined:narrator.wav");
        backends.add(backend);
        audiobook.add("backends", backends);
        audiobook.addProperty("selectedBackendId", "workset-no-speech");
        attributes.add(KEY, audiobook);
        settings.save(user);
        return "placeholder speech server added (labelling only)";
    }

    /**
     * Labels the parts {@code from..to} (positions in the active branch, 1-based) with the book's model and stores the scripts. Returns the answer of the command
     * (JSON: what ended how).
     */
    public String label(Manuscript book, int from, int to) throws Exception {
        Map<String, Object> arguments = new LinkedHashMap<>();
        arguments.put("from", String.valueOf(from));
        arguments.put("to", String.valueOf(to));
        arguments.put("labelling", true);
        arguments.put("what", "script");
        arguments.put("wait", true);
        return commands.run(book, "audiobook-convert", arguments);
    }

    /** The summary of the labelled book for the review: the state of the audiobook as the plugin tells it. */
    public String status(Manuscript book) throws Exception {
        return commands.run(book, "audiobook-status", Map.of());
    }
}
