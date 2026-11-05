package com.github.enerccio.marginalia.extensions.translatorcommon.ui;

import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Span;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.popover.Popover;
import org.apache.commons.lang3.StringUtils;

import static com.github.enerccio.marginalia.extensions.translatorcommon.ui.TranslatorCommonLocalization.*;

/**
 * What the author sees when a translated speech is clicked in the story: the English the model wrote and reads, who said it, and the notes
 * that tell it may not be what the translation says now (the author edited it) or that nobody in the scene understands it.
 */
public final class SourcePopover {

    private SourcePopover() {
    }

    /**
     * Opens the popover at the anchor of the mark.
     *
     * @param passage the clicked passage of the plugin's kind
     * @param anchor  the component the browser moved onto the mark
     */
    public static void open(AnchoredPassage passage, Component anchor) {
        open(passage, anchor, currentText(passage));
    }

    /**
     * Opens the popover at the anchor of the mark.
     *
     * @param currentText the translated text of the passage now, null when the reader is not the author (no warning about an edit) or it is unknown
     */
    public static void open(AnchoredPassage passage, Component anchor, String currentText) {
        Span span = Span.read(passage.getAttributes());
        if (span == null) {
            return;
        }
        Popover popover = new Popover();
        popover.setOpenOnClick(false);
        popover.addClassName(CommonStyles.SOURCE_POPOVER);
        popover.add(content(span, currentText));
        popover.setTarget(anchor);
        popover.addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                // one popover per click: the next one is made fresh, this one goes with its close
                popover.removeFromParent();
            }
        });
        popover.setOpened(true);
    }

    /**
     * The text of the passage as the part has it now, null when it can't be told.
     */
    static String currentText(AnchoredPassage passage) {
        String text;
        try {
            ChatMessage part = passage.getPart();
            text = part == null ? null : part.getResponse();
        } catch (RuntimeException e) {
            // the part is not loaded and there is no session to load it: no warning then
            return null;
        }
        if (text == null || !passage.hasRange() || passage.isOrphaned() || passage.getEnd() > text.length()) {
            return null;
        }
        return text.substring(passage.getStart(), passage.getEnd());
    }

    /**
     * The content of the popover; separate from the component so that it can be tested.
     *
     * @param currentText the translated text of the passage now, null when unknown
     */
    static Div content(Span span, String currentText) {
        Div box = new Div();
        box.addClassName(CommonStyles.SOURCE_BOX);
        if (StringUtils.isNotBlank(span.getSpeaker())) {
            Div speaker = new Div(TC.localized(SAID_BY_S).formatted(span.getSpeaker()));
            speaker.addClassName(CommonStyles.HINT);
            box.add(speaker);
        }
        if (StringUtils.isNotBlank(span.getReading())) {
            Div reading = new Div(span.getReading());
            reading.addClassName(CommonStyles.HINT);
            box.add(reading);
        }
        Div source = new Div(span.getSource());
        source.addClassName(CommonStyles.SOURCE_TEXT);
        box.add(source);
        if (!span.isUnderstood()) {
            Div note = new Div(TC.localized(NOT_UNDERSTOOD));
            note.addClassName(CommonStyles.HINT);
            box.add(note);
        }
        if (currentText != null && span.isEdited(currentText)) {
            Div note = new Div(TC.localized(EDITED_AFTER_TRANSLATION));
            note.addClassName(CommonStyles.CONFLICT);
            box.add(note);
        }
        return box;
    }
}
