package com.github.enerccio.marginalia.extensions.betareaders.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputs;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaSpoken;
import com.github.enerccio.marginalia.extensions.betareaders.model.Reply;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The JSON schema of a reply, made for one request: the speakers allowed are the personas present (and the narrator), the number of
 * messages is limited.
 */
public final class BetaSchemas {

    private BetaSchemas() {
    }

    /**
     * @param speakers     the names of the personas present; {@link BetaSpoken#NARRATOR} is added
     * @param maxMessages  the most messages of a turn; 0 or less for no limit
     */
    public static ObjectNode replySchema(List<String> speakers, int maxMessages) {
        ObjectNode schema = StructuredOutputs.schemaOf(Reply.class);
        ObjectNode properties = (ObjectNode) schema.path("properties");

        if (properties.path("messages") instanceof ObjectNode messages) {
            flattenItems(messages);
            if (maxMessages > 0) {
                messages.put("maxItems", maxMessages);
            }
            if (messages.path("items").path("properties").path("speaker") instanceof ObjectNode speaker) {
                ArrayNode names = speaker.putArray("enum");
                allowed(speakers).forEach(names::add);
            }
            if (messages.path("items").path("properties").path("parts") instanceof ObjectNode parts) {
                flattenItems(parts);
                // the generator names the constants of the enum, the model writes (and Jackson reads) the lower case names
                if (parts.path("items").path("properties").path("type") instanceof ObjectNode type) {
                    ArrayNode types = type.putArray("enum");
                    types.add("action").add("speech");
                }
            }
        }
        return schema;
    }

    /**
     * The names a speaker may have: the personas present and the narrator, each once.
     */
    public static List<String> allowed(List<String> speakers) {
        Set<String> names = new LinkedHashSet<>(speakers);
        names.add(BetaSpoken.NARRATOR);
        return new ArrayList<>(names);
    }

    /**
     * The constraints of a list field are also put on its items as an allOf, which some providers can't take (response formats are
     * turned into grammars): the schema of the item alone is kept.
     */
    private static void flattenItems(ObjectNode list) {
        JsonNode allOf = list.path("items").path("allOf");
        if (allOf.isArray() && !allOf.isEmpty()) {
            list.set("items", allOf.get(0));
        }
    }

    private static boolean contains(ArrayNode array, String value) {
        for (JsonNode node : array) {
            if (value.equals(node.asText())) {
                return true;
            }
        }
        return false;
    }
}
