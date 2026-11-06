package com.github.enerccio.marginalia.extensions.translatorcommon.text;

import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Span;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpeechPassageKindTest {

    private final SpeechPassageKind kind = new SpeechPassageKind("test.speech", "mark");

    private AnchoredPassage passageOf(String translated) {
        AnchoredPassage passage = new AnchoredPassage();
        new Span("vrakht", "Hello", translated).write(passage.getAttributes());
        return passage;
    }

    @Test
    void aReplaceInsideTheSpeechIsNotAnEdit() {
        AnchoredPassage passage = passageOf("Da drak");

        boolean changed = kind.textReplaced(passage, "Da drak", "Da drakh");

        assertTrue(changed);
        Span span = Span.read(passage.getAttributes());
        assertFalse(span.isEdited("Da drakh"));
        assertEquals("Hello", span.getSource());
    }

    @Test
    void aSpeechTheAuthorEditedBeforeStaysEdited() {
        AnchoredPassage passage = passageOf("Da drak");

        boolean changed = kind.textReplaced(passage, "Da drak, my own", "Da drakh, my own");

        assertFalse(changed);
        assertTrue(Span.read(passage.getAttributes()).isEdited("Da drakh, my own"));
    }

    @Test
    void aPassageWithoutTheDataIsLeftAlone() {
        assertFalse(kind.textReplaced(new AnchoredPassage(), "a", "b"));
    }
}
