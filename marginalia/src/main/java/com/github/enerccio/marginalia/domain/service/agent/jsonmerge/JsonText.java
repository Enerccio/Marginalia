package com.github.enerccio.marginalia.domain.service.agent.jsonmerge;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.TreeMap;

/**
 * The JSON texts of the merge: the compact form with sorted keys that is compared and stored (two texts of the same data are
 * the same string), the pretty printed form of Gson that the user reads and edits, and the strict reading of what the user wrote.
 * A blank text is no value at all, the data is not there.
 */
public final class JsonText {

    private static final Gson COMPACT = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();

    private JsonText() {
    }

    /**
     * The text the user is shown: Gson's pretty printing, empty for no value.
     */
    public static String pretty(JsonElement element) {
        return element == null ? "" : PRETTY.toJson(element);
    }

    /**
     * The text that is compared and stored: no white space, the keys of every object in order, empty for no value.
     */
    public static String compact(JsonElement element) {
        return element == null ? "" : COMPACT.toJson(sorted(element));
    }

    /**
     * Pretty printed text of a compact one; a text that is not JSON is returned as it is.
     */
    public static String pretty(String text) {
        try {
            return pretty(parse(text));
        } catch (InvalidJsonException e) {
            return text == null ? "" : text;
        }
    }

    /**
     * The same data with the keys of every object in order.
     */
    public static JsonElement sorted(JsonElement element) {
        if (element == null || element.isJsonPrimitive() || element.isJsonNull()) {
            return element;
        }
        if (element.isJsonArray()) {
            JsonArray array = new JsonArray();
            for (JsonElement item : element.getAsJsonArray()) {
                array.add(sorted(item));
            }
            return array;
        }
        JsonObject object = new JsonObject();
        for (Map.Entry<String, JsonElement> member : new TreeMap<>(element.getAsJsonObject().asMap()).entrySet()) {
            object.add(member.getKey(), sorted(member.getValue()));
        }
        return object;
    }

    /**
     * Reads one JSON value, strictly: a text that is not JSON (an unquoted word, a comma too many, something after the value) is
     * refused, as the parser of Gson would take a lot of them. Blank text is no value, {@code null}.
     *
     * @throws InvalidJsonException the text is not JSON, its message says where
     */
    public static JsonElement parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try (JsonReader reader = new JsonReader(new StringReader(text))) {
            reader.setLenient(false);
            JsonElement element = new Gson().getAdapter(JsonElement.class).read(reader);
            if (reader.peek() != JsonToken.END_DOCUMENT) {
                throw new InvalidJsonException("There is more after the value");
            }
            return element;
        } catch (InvalidJsonException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw new InvalidJsonException(e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }

    /**
     * Whether the text is JSON (or blank).
     */
    public static boolean isValid(String text) {
        try {
            parse(text);
            return true;
        } catch (InvalidJsonException e) {
            return false;
        }
    }

    /**
     * Text that is not JSON.
     */
    public static final class InvalidJsonException extends IllegalArgumentException {
        public InvalidJsonException(String message) {
            super(message);
        }
    }
}
