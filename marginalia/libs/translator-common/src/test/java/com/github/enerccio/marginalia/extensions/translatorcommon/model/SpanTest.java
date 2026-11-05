package com.github.enerccio.marginalia.extensions.translatorcommon.model;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The data of a translated passage: the round trip through the attributes of a passage and the detection of the author's edit.
 */
class SpanTest {

    @Test
    void theSpanSurvivesTheAttributesOfAPassage() {
        Span span = new Span("lang-1", "\"I see the enemy.\"", "\"Da ja-se vek.\"");
        span.setSpeaker("Mara");
        span.setUnderstood(false);
        span.setRegister("formal");
        span.setModel("some-model");
        span.setTime(1234L);
        JsonObject attributes = new JsonObject();
        attributes.addProperty("other", "kept");

        span.write(attributes);
        Span read = Span.read(attributes);

        assertEquals("lang-1", read.getLanguage());
        assertEquals("\"I see the enemy.\"", read.getSource());
        assertEquals("Mara", read.getSpeaker());
        assertFalse(read.isUnderstood());
        assertEquals("formal", read.getRegister());
        assertEquals("some-model", read.getModel());
        assertEquals(1234L, read.getTime());
        assertEquals(span.getTextHash(), read.getTextHash());
        assertEquals("kept", attributes.get("other").getAsString());
    }

    @Test
    void aPassageOfAnotherPluginHasNoSpan() {
        assertNull(Span.read(null));
        assertNull(Span.read(new JsonObject()));
        JsonObject broken = new JsonObject();
        broken.addProperty(Span.KEY, "not an object");
        assertNull(Span.read(broken));
    }

    @Test
    void aSpanReadWithMissingFieldsHasNoNulls() {
        JsonObject attributes = new JsonObject();
        attributes.add(Span.KEY, new JsonObject());

        Span span = Span.read(attributes);

        assertEquals("", span.getSource());
        assertEquals("", span.getSpeaker());
        assertEquals("", span.getRegister());
        assertTrue(span.isUnderstood());
        assertFalse(span.isEdited("anything"), "a span that has no hash of its text cannot tell");
    }

    @Test
    void anEditOfTheConlangIsFoundAndAcceptingItClearsIt() {
        Span span = new Span("lang-1", "Hello", "Da hal");

        assertFalse(span.isEdited("Da hal"));
        assertTrue(span.isEdited("Da hal!"));

        span.textIs("Da hal!");

        assertFalse(span.isEdited("Da hal!"));
        assertTrue(span.isEdited("Da hal"));
    }
}
