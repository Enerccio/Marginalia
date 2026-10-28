package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
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
 * stored deflated; the accessors give and take the plain text.
 */
@Entity
@Table(name = "message_prompts")
public class MessagePrompt extends OwnedEntity {

    /**
     * The keys the prompt and the lore had in the extended content of the part, before they were stored apart from it:
     * they are still the keys in a backup of a book ({@code ManuscriptService#createBackup}).
     */
    public static final String PROMPT_KEY = "builtPrompt";
    public static final String LORE_KEY = "backgroundLore";

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
