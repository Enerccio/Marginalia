package com.github.enerccio.marginalia.utils.docimport;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 * Decoding of the plain text files ({@link MarkdownReader}, {@link PlainTextReader}), which come in any encoding: a byte
 * order mark decides, then UTF-8 when the bytes are valid UTF-8, and Windows-1252 (what a Windows editor writes) otherwise.
 */
final class TextFiles {

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    private TextFiles() {
    }

    /**
     * @return the text with the line breaks as {@code \n} and without the byte order mark
     */
    static String decode(byte[] data) {
        String text;
        if (data.length >= 2 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xFE) {
            text = new String(data, 2, data.length - 2, StandardCharsets.UTF_16LE);
        } else if (data.length >= 2 && (data[0] & 0xFF) == 0xFE && (data[1] & 0xFF) == 0xFF) {
            text = new String(data, 2, data.length - 2, StandardCharsets.UTF_16BE);
        } else {
            int offset = data.length >= 3 && (data[0] & 0xFF) == 0xEF && (data[1] & 0xFF) == 0xBB && (data[2] & 0xFF) == 0xBF ? 3 : 0;
            try {
                text = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(data, offset, data.length - offset)).toString();
            } catch (CharacterCodingException e) {
                text = new String(data, offset, data.length - offset, WINDOWS_1252);
            }
        }
        if (text.indexOf('\u0000') >= 0) {
            throw new IllegalArgumentException("Not a text file");
        }
        return text.replace("\r\n", "\n").replace('\r', '\n');
    }
}
