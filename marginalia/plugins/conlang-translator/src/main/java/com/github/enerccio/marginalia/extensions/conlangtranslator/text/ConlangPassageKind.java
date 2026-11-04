package com.github.enerccio.marginalia.extensions.conlangtranslator.text;

import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.domain.service.PassageKind;
import com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangStyles;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Span;

/**
 * The kind of the passages of the translated speech: marked in the story with a dotted line. The click that shows the English source
 * is the display step; the viewer and the exports are told to ask the plugin.
 */
public final class ConlangPassageKind implements PassageKind {

    @Override
    public String id() {
        return Span.KIND;
    }

    @Override
    public String markClass(AnchoredPassage passage) {
        return ConlangStyles.PASSAGE;
    }

    @Override
    public boolean shownInViewer() {
        return true;
    }

    @Override
    public boolean shownInExports() {
        return true;
    }
}
