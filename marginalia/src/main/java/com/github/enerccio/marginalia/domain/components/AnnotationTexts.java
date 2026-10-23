package com.github.enerccio.marginalia.domain.components;

import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * The annotations of a part as text for a prompt: one line for each, the passage in quotes and the note.
 */
public final class AnnotationTexts {

    private AnnotationTexts() {
    }

    /**
     * @return the annotations that are open (the ones that lost their passage and the closed ones are left out), one line each;
     * empty when there are none
     */
    public static String openAnnotations(List<Annotation> annotations) {
        StringBuilder text = new StringBuilder();
        for (Annotation annotation : annotations) {
            if (annotation.getStatus() != AnnotationStatus.OPEN || StringUtils.isBlank(annotation.getText())) {
                continue;
            }
            if (!text.isEmpty()) {
                text.append('\n');
            }
            text.append("- ");
            if (annotation.hasRange() && StringUtils.isNotBlank(annotation.getQuote())) {
                text.append('"').append(oneLine(annotation.getQuote())).append("\": ");
            } else {
                text.append("(the whole part): ");
            }
            text.append(oneLine(annotation.getText()));
        }
        return text.toString();
    }

    /**
     * The notes of the author that are to be given to the model with the story, for one part: the open annotations marked to be sent, one line
     * each. A note about the whole part names the part by its first words.
     *
     * @return empty when the part has none
     */
    public static String promptNotes(List<Annotation> annotations, ChatMessage part) {
        StringBuilder text = new StringBuilder();
        for (Annotation annotation : annotations) {
            if (annotation.getStatus() != AnnotationStatus.OPEN || !annotation.isSendToPrompt() || StringUtils.isBlank(annotation.getText())) {
                continue;
            }
            if (!text.isEmpty()) {
                text.append('\n');
            }
            text.append("- ");
            if (annotation.hasRange() && StringUtils.isNotBlank(annotation.getQuote())) {
                text.append('"').append(oneLine(annotation.getQuote())).append("\": ");
            } else {
                text.append("(the part beginning \"").append(StringUtils.abbreviate(oneLine(StringUtils.defaultString(part.getResponse())), 60))
                        .append("\"): ");
            }
            text.append(oneLine(annotation.getText()));
        }
        return text.toString();
    }

    private static String oneLine(String text) {
        return text.replaceAll("\\s+", " ").strip();
    }

}
