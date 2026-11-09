package com.github.enerccio.marginalia.extensions.translatorcommon.model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * What the plugin keeps for a passage of the story that was translated: the English <b>source</b> the model reads in place of the translation
 * the text holds, the language, who speaks, and what is needed to tell that the author has edited the translation since. It is the data of an
 * {@code AnchoredPassage} of the kind of the plugin that made it, in its attributes under {@link #KEY} (the key is the same for every plugin: a passage has one kind); the passage's range and quote are the conlang text.
 */
public class Span {

    /** The key in the attributes of the passage. */
    // the value is the one the data had when only the Conlang Translator wrote it: a passage holds one kind of data, so every plugin can use it, and the stored data stays readable
    public static final String KEY = "com.github.enerccio.marginalia.extensions.conlangtranslator";

    private static final Gson GSON = new Gson();

    /** The language (id) the speech was translated into. */
    private String language;
    /** The English the model wrote and reads: what the passage was before it was translated. */
    private String source;
    /** Who speaks, as the model named the speaker; may be empty. */
    private String speaker = "";
    /** False for a speech the listener does not understand: the reader sees "(not understood)" behind the click. */
    private boolean understood = true;
    /** The register the model chose, empty for the neutral one. */
    private String register = "";
    /** How the translation is read (the romaji of kana or kanji text), shown above the source; empty for none. */
    private String reading = "";
    /** A hash of the translated text the plugin made, to tell that the author edited it. */
    private String textHash;
    /** The model that translated it, as the user names it, and when (epoch milliseconds). */
    private String model;
    private long time;

    public Span() {
    }

    public Span(String language, String source, String translatedText) {
        this.language = language;
        this.source = source;
        this.textHash = hash(translatedText);
    }

    /**
     * Whether the author edited the translated text since the plugin made it: the source is stale then and the reverse leaves the text as it is.
     *
     * @param currentText the text of the passage now
     */
    public boolean isEdited(String currentText) {
        return textHash != null && !textHash.equals(hash(currentText));
    }

    /**
     * Records the text the passage has now as the one the plugin made (a retranslation, or the author's edit that was accepted).
     */
    public void textIs(String translatedText) {
        this.textHash = hash(translatedText);
    }

    static String hash(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest((text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * The data stored in the attributes of a passage, or null when it has none (it is not a passage of the plugin's).
     */
    public static Span read(JsonObject attributes) {
        if (attributes == null || !attributes.has(KEY)) {
            return null;
        }
        JsonElement stored = attributes.get(KEY);
        return stored.isJsonObject() ? GSON.fromJson(stored, Span.class) : null;
    }

    public void write(JsonObject attributes) {
        attributes.add(KEY, GSON.toJsonTree(this));
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSource() {
        return source == null ? "" : source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSpeaker() {
        return speaker == null ? "" : speaker;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    public boolean isUnderstood() {
        return understood;
    }

    public void setUnderstood(boolean understood) {
        this.understood = understood;
    }

    public String getReading() {
        return reading == null ? "" : reading;
    }

    public void setReading(String reading) {
        this.reading = reading;
    }

    public String getRegister() {
        return register == null ? "" : register;
    }

    public void setRegister(String register) {
        this.register = register;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTextHash() {
        return textHash;
    }
}
