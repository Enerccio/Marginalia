package com.github.enerccio.marginalia.extensions.betareaders;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.enerccio.marginalia.domain.service.structured.SchemaCheck;
import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputs;
import com.github.enerccio.marginalia.extensions.betareaders.model.Reply;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaSchemas;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The schema of a reply is made for a request: the speakers are the personas present and the narrator, the number of messages is
 * limited, and the reasoning is there only for a model that does not reason on its own.
 */
class BetaSchemasTest {

    private static final String ANSWER = """
            {"reasoning": "Athena has the most to say.", "messages": [
              {"speaker": "Athena", "parts": [{"type": "action", "text": "nods"}, {"type": "speech", "text": "Good chapter."}]},
              {"speaker": "Narrator", "parts": [{"type": "speech", "text": "Vesper snorts."}]}]}""";

    private static List<String> speakers(JsonNode schema) {
        List<String> names = new ArrayList<>();
        schema.path("properties").path("messages").path("items").path("properties").path("speaker").path("enum")
                .forEach(node -> names.add(node.asText()));
        return names;
    }

    @Test
    void theSpeakersArePersonasPresentAndTheNarrator() {
        JsonNode schema = BetaSchemas.replySchema(List.of("Athena", "Vesper"), 0, true);

        assertEquals(List.of("Athena", "Vesper", "Narrator"), speakers(schema));
    }

    @Test
    void theNarratorIsNeverTwiceAndAPersonaNamedLikeItDoesNotAddOne() {
        assertEquals(List.of("Athena", "Narrator"), speakers(BetaSchemas.replySchema(List.of("Athena", "Narrator"), 0, true)));
    }

    @Test
    void theNumberOfMessagesIsLimited() {
        JsonNode limited = BetaSchemas.replySchema(List.of("Athena"), 3, true);
        JsonNode unlimited = BetaSchemas.replySchema(List.of("Athena"), 0, true);

        assertEquals(3, limited.path("properties").path("messages").path("maxItems").asInt());
        assertTrue(unlimited.path("properties").path("messages").path("maxItems").isMissingNode());
    }

    @Test
    void theReasoningIsFirstAndRequiredOnlyWithoutNativeReasoning() {
        JsonNode with = BetaSchemas.replySchema(List.of("Athena"), 0, true);
        JsonNode without = BetaSchemas.replySchema(List.of("Athena"), 0, false);

        assertEquals("reasoning", with.path("required").get(0).asText());
        assertFalse(with.path("properties").path("reasoning").isMissingNode());
        assertTrue(without.path("properties").path("reasoning").isMissingNode());
        assertEquals(1, without.path("required").size());
        assertEquals("messages", without.path("required").get(0).asText());
    }

    @Test
    void theItemsAreNotWrappedInAnAllOf() {
        JsonNode schema = BetaSchemas.replySchema(List.of("Athena"), 3, true);

        JsonNode messages = schema.path("properties").path("messages");
        assertTrue(messages.path("items").path("allOf").isMissingNode());
        assertTrue(messages.path("items").path("properties").path("parts").path("items").path("allOf").isMissingNode());
    }

    @Test
    void thePartTypesAreTheLowerCaseNames() {
        JsonNode schema = BetaSchemas.replySchema(List.of("Athena"), 0, true);

        JsonNode type = schema.path("properties").path("messages").path("items").path("properties").path("parts")
                .path("items").path("properties").path("type");
        assertEquals("action", type.path("enum").get(0).asText());
        assertEquals("speech", type.path("enum").get(1).asText());
    }

    @Test
    void anAnswerInTheSchemaIsValidAndReadsIntoTheReply() throws Exception {
        JsonNode schema = BetaSchemas.replySchema(List.of("Athena", "Vesper"), 4, true);

        SchemaCheck check = StructuredOutputs.validate(schema, ANSWER);
        Reply reply = StructuredOutputs.parse(ANSWER, Reply.class);

        assertTrue(check.valid(), () -> String.valueOf(check));
        assertEquals("Athena has the most to say.", reply.reasoning());
        assertEquals(2, reply.messages().size());
        assertEquals(Reply.Type.ACTION, reply.messages().getFirst().parts().getFirst().type());
        assertEquals("Good chapter.", reply.messages().getFirst().parts().get(1).text());
    }

    @Test
    void aSpeakerThatIsNotPresentIsNotValid() {
        JsonNode schema = BetaSchemas.replySchema(List.of("Vesper"), 4, true);

        assertFalse(StructuredOutputs.validate(schema, ANSWER).valid());
    }

    @Test
    void tooManyMessagesAreNotValid() {
        JsonNode schema = BetaSchemas.replySchema(List.of("Athena"), 1, true);

        assertFalse(StructuredOutputs.validate(schema, ANSWER).valid());
    }

    @Test
    void theReasoningIsNotAllowedWhenTheSchemaHasNone() {
        JsonNode schema = BetaSchemas.replySchema(List.of("Athena"), 4, false);

        assertFalse(StructuredOutputs.validate(schema, ANSWER).valid());
    }
}
