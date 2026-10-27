package com.github.enerccio.marginalia.domain.model;

import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import com.google.gson.JsonObject;
import jakarta.persistence.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@MappedSuperclass
@EntityListeners({ ExtendableEntityListener.class })
public class ExtendableEntity extends OwnedEntity {

    // smaller content is not worth the header of the stream
    private static final int COMPRESS_FROM = 256;
    private static final byte GZIP_0 = 0x1f;
    private static final byte GZIP_1 = (byte) 0x8b;

    @Column(length = 268435456)
    private byte[] extendedContent;

    @Lob
    private String _fulltext;

    @Transient
    @ExtendedAttribute(inject = true, injectPrefix = "attributes")
    private JsonObject attributes = new JsonObject();

    /**
     * @return the extended content as plain text (JSON); it is stored gzipped, see {@link #setExtendedContent}
     */
    public byte[] getExtendedContent() {
        return plain(extendedContent);
    }

    /**
     * Stores the content gzipped when it is not small: the content is mostly text, and an entity keeps it as long as it is
     * held, next to the fields and the attributes made from it. What was stored before, plain, is still read (a gzip stream
     * begins with bytes that no JSON or text does) and is compressed when the entity is saved again.
     */
    public void setExtendedContent(byte[] extendedContent) {
        this.extendedContent = compress(extendedContent);
    }

    /**
     * @return whether the content is stored plain though it is big enough to be compressed (a row from before the compression)
     */
    public boolean needsCompression() {
        return extendedContent != null && extendedContent.length >= COMPRESS_FROM && !isGzip(extendedContent);
    }

    private static boolean isGzip(byte[] stored) {
        return stored.length >= 2 && stored[0] == GZIP_0 && stored[1] == GZIP_1;
    }

    /**
     * @param stored the bytes of the column {@code extendedContent}, as they are stored
     * @return the plain content
     */
    public static byte[] plain(byte[] stored) {
        if (stored == null || !isGzip(stored)) {
            return stored;
        }
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(stored))) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @param plain a plain content
     * @return what {@link #setExtendedContent} stores of it, for the code that writes the column itself (a bulk update)
     */
    public static byte[] compress(byte[] plain) {
        byte[] content = plain;
        if (content == null || content.length < COMPRESS_FROM) {
            return content;
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(content.length / 4 + 32);
        try (GZIPOutputStream out = new GZIPOutputStream(bytes)) {
            out.write(content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    public JsonObject getAttributes() {
        return attributes;
    }

    public void setAttributes(JsonObject attributes) {
        this.attributes = attributes;
    }

    public String get_fulltext() {
        return _fulltext;
    }

    public void set_fulltext(String _fulltext) {
        this._fulltext = _fulltext;
    }
}
