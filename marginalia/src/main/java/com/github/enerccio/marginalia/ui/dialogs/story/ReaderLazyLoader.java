package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.widgets.LazyLoadIndicator;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.DomEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The window over the branch of the reader (the viewer of a book): the same sliding as in the story
 * ({@link StoryLazyLoader}), but with the parts in a plain list, no cards. It starts with the first parts, or around
 * the part where the reader stopped last time (the browser remembers it, {@link JavascriptLibrary#rememberReaderPosition}).
 * Every part is an element with the attribute {@code data-message-id}.
 */
public class ReaderLazyLoader {

    private static final String FAR = "event.detail.far";

    /**
     * Same meaning as in the story: parts are dropped at the far edge only when it is this many pixels away.
     */
    private static final double DROP_DISTANCE = 1500;

    private final ScrollPanel panel;
    private final VerticalLayout content;
    private final Function<ChatMessage, Component> partFactory;
    private final LazyLoadIndicator earlierIndicator;
    private final LazyLoadIndicator laterIndicator;
    private final Map<Long, Component> parts = new LinkedHashMap<>();

    private LazyBranch window;

    /**
     * @param content     the layout in the panel that holds the parts
     * @param partFactory creates the component of a part, {@code null} when there is nothing to show of it
     */
    public ReaderLazyLoader(ScrollPanel panel, VerticalLayout content, Function<ChatMessage, Component> partFactory,
                            Localization loc) {
        this.panel = panel;
        this.content = content;
        this.partFactory = partFactory;
        this.earlierIndicator = new LazyLoadIndicator(loc.getValue(L.MSG_LOADING_EARLIER_PARTS));
        this.laterIndicator = new LazyLoadIndicator(loc.getValue(L.MSG_LOADING_LATER_PARTS));
        panel.setHeader(earlierIndicator);
        panel.setFooter(laterIndicator);
        panel.getElement().addEventListener("edge-top", event -> loadEarlier(far(event))).addEventData(FAR);
        panel.getElement().addEventListener("edge-bottom", event -> loadLater(far(event))).addEventData(FAR);
    }

    /**
     * Shows the first parts of the branch, or the ones around the part the reader stopped at and scrolls there.
     *
     * @param limit   the size of the window, {@code null} for all the parts
     * @param startAt id of the part to start at, {@code null} or a part that is not on the branch for the beginning
     */
    public void start(List<ChatMessage> branch, Integer limit, Long startAt) {
        window = LazyBranch.head(branch, limit);
        if (startAt != null) {
            window.centerOn(startAt);
        }
        content.removeAll();
        parts.clear();
        for (ChatMessage message : window.loaded()) {
            addPart(message, false);
        }
        settle(false);

        Component target = startAt != null ? parts.get(startAt) : null;
        if (target != null) {
            JavascriptLibrary.scrollIntoViewAfterLayout(target.getElement());
        }
    }

    private void addPart(ChatMessage message, boolean first) {
        Component part = partFactory.apply(message);
        if (part == null) {
            return;
        }
        part.getElement().setAttribute("data-message-id", String.valueOf(message.getId()));
        parts.put(message.getId(), part);
        if (first) {
            content.addComponentAsFirst(part);
        } else {
            content.add(part);
        }
    }

    private void loadEarlier(double far) {
        if (window == null || !window.hasEarlier()) {
            return;
        }
        List<ChatMessage> added = window.loadEarlier();
        List<ChatMessage> dropped = far >= DROP_DISTANCE ? window.trimLater() : List.of();
        removeParts(dropped);
        for (ChatMessage message : added.reversed()) {
            addPart(message, true);
        }
        settle(true);
    }

    private void loadLater(double far) {
        if (window == null || !window.hasLater()) {
            return;
        }
        List<ChatMessage> added = window.loadLater();
        List<ChatMessage> dropped = far >= DROP_DISTANCE ? window.trimEarlier() : List.of();
        removeParts(dropped);
        for (ChatMessage message : added) {
            addPart(message, false);
        }
        settle(true);
    }

    private void removeParts(List<ChatMessage> dropped) {
        for (ChatMessage message : dropped) {
            Component part = parts.remove(message.getId());
            if (part != null) {
                content.remove(part);
            }
        }
    }

    private void settle(boolean keepPosition) {
        earlierIndicator.setVisible(window.hasEarlier());
        laterIndicator.setVisible(window.hasLater());
        if (keepPosition) {
            JavascriptLibrary.restoreAnchor(panel.getElement(), content.getElement());
        }
        JavascriptLibrary.watchEdge(panel.getElement(), content.getElement(), true, window.hasEarlier());
        JavascriptLibrary.watchEdge(panel.getElement(), content.getElement(), false, window.hasLater());
    }

    private static double far(DomEvent event) {
        var far = event.getEventData().get(FAR);
        return far == null ? 0 : far.asDouble();
    }
}
