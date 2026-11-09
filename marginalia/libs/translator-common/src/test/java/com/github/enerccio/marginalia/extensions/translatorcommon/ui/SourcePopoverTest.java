package com.github.enerccio.marginalia.extensions.translatorcommon.ui;

import com.github.enerccio.marginalia.extensions.translatorcommon.model.Span;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the popover over a translated speech says: the source, the speaker, and the two warnings.
 */
class SourcePopoverTest {

    private static List<String> texts(Div box) {
        return box.getChildren().map(Component::getElement).map(element -> element.getText()).toList();
    }

    @Test
    void theSourceIsShownWithTheSpeaker() {
        Span span = new Span("lang-1", "\"I see the enemy.\"", "\"Da ja-se vek.\"");
        span.setSpeaker("Mara");

        Div box = SourcePopover.content(span, "\"Da ja-se vek.\"");

        assertEquals(List.of("Said by Mara", "\"I see the enemy.\""), texts(box));
    }

    @Test
    void aSpeechNobodyUnderstandsSaysSo() {
        Span span = new Span("lang-1", "\"Go.\"", "\"Ke.\"");
        span.setUnderstood(false);

        Div box = SourcePopover.content(span, "\"Ke.\"");

        assertEquals(List.of("\"Go.\"", "(not understood by the listener)"), texts(box));
    }

    @Test
    void anEditedTextWarnsThatTheSourceMayBeStale() {
        Span span = new Span("lang-1", "\"Go.\"", "\"Ke.\"");

        Div box = SourcePopover.content(span, "\"Ke za.\"");

        assertEquals(2, texts(box).size());
        assertTrue(box.getChildren().anyMatch(child -> child.hasClassName(CommonStyles.CONFLICT)));
    }

    @Test
    void anUnknownCurrentTextRaisesNoWarning() {
        Span span = new Span("lang-1", "\"Go.\"", "\"Ke.\"");

        assertEquals(List.of("\"Go.\""), texts(SourcePopover.content(span, null)));
    }

    @Test
    void aReadingIsShownAboveTheSource() {
        Span span = new Span("ja", "\"I see the enemy.\"", "\"敵が見える。\"");
        span.setReading("teki ga mieru.");

        assertEquals(List.of("teki ga mieru.", "\"I see the enemy.\""), texts(SourcePopover.content(span, null)));
    }

    @Test
    void theAuthorsPopoverHasAButtonThatEditsTheSource() {
        Span span = new Span("lang-1", "\"Go.\"", "\"Ke.\"");
        int[] clicked = {0};

        Div box = SourcePopover.content(span, "\"Ke.\"", () -> clicked[0]++);

        com.vaadin.flow.component.button.Button button = box.getChildren()
                .filter(com.vaadin.flow.component.button.Button.class::isInstance)
                .map(com.vaadin.flow.component.button.Button.class::cast).findFirst().orElseThrow();
        assertEquals("Edit the source", button.getText());
        button.click();
        assertEquals(1, clicked[0]);
    }

    @Test
    void theReadersPopoverHasNoButton() {
        Span span = new Span("lang-1", "\"Go.\"", "\"Ke.\"");

        assertTrue(SourcePopover.content(span, null).getChildren().noneMatch(com.vaadin.flow.component.button.Button.class::isInstance));
    }
}
