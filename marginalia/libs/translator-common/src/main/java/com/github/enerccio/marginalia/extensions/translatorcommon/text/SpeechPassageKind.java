package com.github.enerccio.marginalia.extensions.translatorcommon.text;

import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.domain.service.PassageKind;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.SourcePopover;
import com.vaadin.flow.component.Component;

/**
 * The kind of the passages of the translated speech of a plugin: marked in the story, the click shows the English source, and the viewer and
 * the exports are told to ask the plugin. Each plugin registers its own, with its own id and mark.
 */
public final class SpeechPassageKind implements PassageKind {

    private final String id;
    private final String markClass;

    /**
     * @param id        the id of the kind (unique among the plugins)
     * @param markClass the CSS class of the mark over a translated speech
     */
    public SpeechPassageKind(String id, String markClass) {
        this.id = id;
        this.markClass = markClass;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String markClass(AnchoredPassage passage) {
        return markClass;
    }

    @Override
    public void onClick(AnchoredPassage passage, Component anchor) {
        SourcePopover.open(passage, anchor);
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
