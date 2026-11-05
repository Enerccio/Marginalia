package com.github.enerccio.marginalia.extensions.translatorcommon.service;

import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.domain.service.PassageKind;
import com.github.enerccio.marginalia.domain.service.PassageKinds;
import com.github.enerccio.marginalia.domain.service.PassageService;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Span;

import java.util.ArrayList;
import java.util.List;

/**
 * The translated passages of a part whichever plugin made them. Every plugin that uses this library registers a passage kind of its own and keeps the
 * same {@link Span} data in its passages, so a passage is a translated speech when it has that data; the page of a book that holds the speech of two
 * plugins is drawn from all of them at once (a plugin that drew only its own would draw the text again from the stored text and lose the marks of the
 * other).
 */
public final class TranslatedPassages {

    private TranslatedPassages() {
    }

    /**
     * @param partId the part
     * @return the passages of the part of every registered kind that carry the data of a translated speech
     */
    public static List<AnchoredPassage> of(PassageService passages, PassageKinds kinds, Long partId) throws Exception {
        List<AnchoredPassage> found = new ArrayList<>();
        for (PassageKind kind : kinds.all()) {
            for (AnchoredPassage passage : passages.listForParts(List.of(partId), kind.id())) {
                if (Span.read(passage.getAttributes()) != null) {
                    found.add(passage);
                }
            }
        }
        return found;
    }

    /**
     * The CSS class of the mark over the passage: the one its kind gives, else {@code fallback}.
     */
    public static String markClass(PassageKinds kinds, AnchoredPassage passage, String fallback) {
        PassageKind kind = kinds.get(passage.getPassageKind());
        String mark = kind == null ? null : kind.markClass(passage);
        return mark == null ? fallback : mark;
    }
}
