package com.github.enerccio.marginalia.domain.service.structured;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.funnysaltyfish.partialjsonparser.PartialJsonParser;

/**
 * Reads the JSON of a streamed answer while it is still being written, to show what has arrived so far. Unfinished
 * strings are taken as they are, a key without its value (or with an unfinished one that is not a string) is left out.
 * Nothing is validated, use {@link StructuredOutputs#validate} once the answer is complete.
 */
public final class PartialJson {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PartialJson() {
    }

    /**
     * What has arrived of the JSON so far (a markdown block around it is ignored).
     *
     * @return an object or an array; {@code null} when the text is empty, is not JSON, or has not got far enough yet
     * to tell
     */
    public static JsonNode parse(String text) {
        String json = StructuredOutputs.cleanJson(stripOpenFence(text));
        if (json.isEmpty()) {
            return null;
        }
        try {
            Object parsed = PartialJsonParser.INSTANCE.parse(json);
            if (!(parsed instanceof java.util.Map<?, ?>) && !(parsed instanceof java.util.List<?>)) {
                return null;
            }
            return MAPPER.valueToTree(parsed);
        } catch (Exception e) {
            // the parser reports a text that is cut in the middle of a token (an escape, a number) as an error:
            // the caller keeps what it showed before and tries again with the next chunk
            return null;
        }
    }

    /**
     * {@link StructuredOutputs#cleanJson} removes a markdown block only when it is closed, the streamed text has
     * the opening fence long before the closing one.
     */
    private static String stripOpenFence(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.stripLeading();
        if (!trimmed.startsWith("```")) {
            return text;
        }
        int newline = trimmed.indexOf('\n');
        if (newline < 0) {
            return "";
        }
        String body = trimmed.substring(newline + 1);
        int close = body.lastIndexOf("```");
        if (close >= 0) {
            return body.substring(0, close);
        }
        // the closing fence may be half there: one or two backticks after the JSON
        int end = body.length();
        while (end > 0 && body.charAt(end - 1) == '`' && body.length() - end < 2) {
            end--;
        }
        return body.substring(0, end);
    }
}
