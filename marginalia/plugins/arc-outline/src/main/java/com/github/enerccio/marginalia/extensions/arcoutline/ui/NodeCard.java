package com.github.enerccio.marginalia.extensions.arcoutline.ui;

import com.github.enerccio.marginalia.extensions.arcoutline.model.OutlineNode;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import org.apache.commons.lang3.StringUtils;

/**
 * An arc or a chapter as the list shows it: a card colored by the state of the node, with the title and the
 * description, a component before them (the state), one after them (the collapse button of an arc) and the buttons
 * under them.
 */
public class NodeCard extends Div {

    /**
     * @param kind     {@link ArcOutlineStyles#ARC} or {@link ArcOutlineStyles#CHAPTER}
     * @param leading  before the text, null for none
     * @param trailing after the text, null for none
     * @param expanded show the whole description and the note, not the first lines of the description
     * @param actions  the buttons under the text, none for no row of buttons
     */
    public NodeCard(OutlineNode node, String kind, Component leading, Component trailing, boolean expanded,
                    Component... actions) {
        this(node, kind, leading, trailing, expanded, expanded, actions);
    }

    /**
     * @param showNote show the note under the description (the note is private and long: the list shows it only when
     *                 the user asks for it, the preview of a generated node always)
     * @see #NodeCard(OutlineNode, String, Component, Component, boolean, Component...)
     */
    public NodeCard(OutlineNode node, String kind, Component leading, Component trailing, boolean expanded,
                    boolean showNote, Component... actions) {
        Div title = new Div(new Span(node.getTitle()));
        title.addClassName(ArcOutlineStyles.TITLE);
        Div body = new Div(title);
        body.addClassName(ArcOutlineStyles.BODY);
        if (StringUtils.isNotBlank(node.getDescription())) {
            Div description = new Div(new Span(node.getDescription()));
            description.addClassName(ArcOutlineStyles.DESCRIPTION);
            body.add(description);
        }
        if (showNote && StringUtils.isNotBlank(node.getNote())) {
            Div note = new Div(new Span(node.getNote()));
            note.addClassNames(ArcOutlineStyles.DESCRIPTION, ArcOutlineStyles.NOTE);
            body.add(note);
        }

        Div head = new Div();
        head.addClassName(ArcOutlineStyles.HEAD);
        if (leading != null) {
            head.add(leading);
        }
        head.add(body);
        if (trailing != null) {
            head.add(trailing);
        }
        add(head);

        if (actions.length > 0) {
            Div buttons = new Div(actions);
            buttons.addClassName(ArcOutlineStyles.ACTIONS);
            add(buttons);
        }

        addClassNames(ArcOutlineStyles.CARD, kind, ArcOutlineStyles.of(node.getState()));
        if (expanded) {
            addClassName(ArcOutlineStyles.EXPANDED);
        }
    }
}
