package com.github.enerccio.marginalia.workset;

import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CommandScopes;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
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

    public LabelPass(CommandService commandService, CommandScopes scopes, SettingService settings) {
        this.commands = new TranslationPass(commandService, scopes);
        this.settings = settings;
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
