package com.github.enerccio.marginalia.inference;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.enerccio.marginalia.domain.service.structured.PartialJson;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PartialJson}: what has arrived of a streamed JSON answer so far.
 */
class PartialJsonTest {

    @Test
    void unfinishedStringIsTakenAsItIs() {
        JsonNode node = PartialJson.parse("{\"name\": \"The Fal");
        assertThat(node.get("name").asText()).isEqualTo("The Fal");
    }

    @Test
    void keyWithoutValueIsLeftOut() {
        assertThat(PartialJson.parse("{\"name\": \"Siege\", \"descr").has("description")).isFalse();
        assertThat(PartialJson.parse("{\"name\": \"Siege\", \"description\":").get("name").asText()).isEqualTo("Siege");
    }

    @Test
    void escapesAreDecoded() {
        JsonNode node = PartialJson.parse("{\"description\": \"one\\ntwo \\\"q\\\"\"}");
        assertThat(node.get("description").asText()).isEqualTo("one\ntwo \"q\"");
    }

    @Test
    void completeJsonIsReadAsIs() {
        JsonNode node = PartialJson.parse("{\"name\":\"A\",\"description\":\"B\"}");
        assertThat(node.get("name").asText()).isEqualTo("A");
        assertThat(node.get("description").asText()).isEqualTo("B");
    }

    @Test
    void markdownBlockIsIgnoredEvenWhenNotClosed() {
        assertThat(PartialJson.parse("```json\n{\"name\": \"A").get("name").asText()).isEqualTo("A");
        assertThat(PartialJson.parse("```json\n{\"name\": \"A\"}\n```").get("name").asText()).isEqualTo("A");
        assertThat(PartialJson.parse("```json")).isNull();
        // the closing fence is only half there
        assertThat(PartialJson.parse("```json\n{\"name\": \"A\"}\n``").get("name").asText()).isEqualTo("A");
        assertThat(PartialJson.parse("```json\n{\"name\": \"A\"}\n`").get("name").asText()).isEqualTo("A");
    }

    @Test
    void nothingToShowYetIsNull() {
        assertThat(PartialJson.parse(null)).isNull();
        assertThat(PartialJson.parse("")).isNull();
        assertThat(PartialJson.parse("   ")).isNull();
        assertThat(PartialJson.parse("Sure, here is")).isNull();
    }

    @Test
    void cutInsideAnEscapeDoesNotThrow() {
        // a unicode escape that has not fully arrived
        PartialJson.parse("{\"name\": \"A\\u00");
        PartialJson.parse("{\"name\": \"A\\");
    }

    @Test
    void arrayIsSupported() {
        JsonNode node = PartialJson.parse("[{\"name\": \"A\"}, {\"name\": \"B");
        assertThat(node.isArray()).isTrue();
        assertThat(node.get(1).get("name").asText()).isEqualTo("B");
    }
}
