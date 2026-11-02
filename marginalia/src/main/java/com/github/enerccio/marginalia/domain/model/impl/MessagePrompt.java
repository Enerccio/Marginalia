package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.components.LoreUsage;
import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.persistence.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * What was sent to the model to write a {@link ChatMessage part}: the built prompt (the whole payload, a request weighs
 * hundreds of kilobytes) and the lore that was in it. It is only read when somebody asks for it (the prompt dialog, a
 * summary, a review), so it lives apart from the part: the parts of a branch are all loaded together, and a story
 * view keeps them for as long as it is open. At most one per part, see {@code MessagePromptService}. The texts are
 * stored deflated; the accessors give and take the plain text. The small things about the prompt (the lore entries it
 * used, {@link #getLoreUsage()}) are extended attributes, so a report of them reads that and not the texts.
 */
@Entity
@Table(name = "message_prompts")
public class MessagePrompt extends ExtendableEntity {

    /**
     * The keys the prompt and the lore had in the extended content of the part, before they were stored apart from it:
     * they are still the keys in a backup of a book ({@code ManuscriptService#createBackup}).
     */
    public static final String PROMPT_KEY = "builtPrompt";
    public static final String LORE_KEY = "backgroundLore";
    /**
     * The key of {@link #getLoreUsage()} in the extended content of a part in a backup of a book, next to the two above.
     */
    public static final String LORE_USAGE_KEY = "loreUsage";

    // a soft deleted part takes its prompt with it; nullable: the cleanup nulls the reference before it purges the part
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    @CleanupReference(Policy.OWNED_BY)
    private ChatMessage message;

    // deflated: the prompt repeats the story of the branch and the lore, and compresses several times over
    @Column(length = 268435456)
    private byte[] builtPrompt;

    @Column(length = 268435456)
    private byte[] backgroundLore;

    // JSON of LoreUsage, extended attributes are plain strings; null when the lore was not recorded
    @ExtendedAttribute
    @Transient
    private String loreUsage;

    public ChatMessage getMessage() {
        return message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }

    public String getBuiltPrompt() {
        return inflate(builtPrompt);
    }

    public void setBuiltPrompt(String builtPrompt) {
        this.builtPrompt = deflate(builtPrompt);
    }

    public String getBackgroundLore() {
        return inflate(backgroundLore);
    }

    public void setBackgroundLore(String backgroundLore) {
        this.backgroundLore = deflate(backgroundLore);
    }

    /**
     * @return the lore entries the generation used, null when that was not recorded (a prompt made before it, a book
     * without a lorebook)
     */
    public LoreUsage getLoreUsage() {
        return LoreUsage.fromJson(loreUsage);
    }

    public void setLoreUsage(LoreUsage usage) {
        this.loreUsage = usage == null ? null : usage.toJson();
    }

    /**
     * @return {@link #getLoreUsage()} as it is stored (json), for the code that moves it without reading it
     */
    public String getLoreUsageJson() {
        return loreUsage;
    }

    /**
     * @param storedExtendedContent the bytes of the column {@code extendedContent} of a row, as they are stored
     * @return the lore usage in it, null when it has none
     */
    public static LoreUsage loreUsageOf(byte[] storedExtendedContent) {
        byte[] plain = plain(storedExtendedContent);
        if (plain == null || plain.length == 0) {
            return null;
        }
        try {
            JsonObject root = JsonParser.parseString(new String(plain, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonElement stored = root.get("loreUsage");
            return stored == null || !stored.isJsonPrimitive() ? null : LoreUsage.fromJson(stored.getAsString());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static byte[] deflate(String text) {
        if (text == null) {
            return null;
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DeflaterOutputStream out = new DeflaterOutputStream(bytes)) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    private static String inflate(byte[] stored) {
        if (stored == null) {
            return null;
        }
        try (InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(stored))) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
