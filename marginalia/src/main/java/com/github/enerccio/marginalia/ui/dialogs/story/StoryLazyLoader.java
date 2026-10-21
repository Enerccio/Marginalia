package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.vaadin.flow.component.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * Fills the panel of a story tab with the cards of the last parts of the branch only (the limit is the setting of the
 * user) and adds the earlier ones when the user scrolls to the top. The tab always opens at the bottom of the story.
 * Both the desktop and the phone story tab use it.
 *
 * @param <C> the card component of the tab
 */
public class StoryLazyLoader<C extends Component & StoryCard> {

    private final ScrollPanel panel;
    private final StoryCards<C> cards;
    private final StoryGenerationListener.CardFactory<C> cardFactory;
    private final BooleanSupplier frozen;

    private LazyBranch window;

    public StoryLazyLoader(ScrollPanel panel, StoryCards<C> cards, StoryGenerationListener.CardFactory<C> cardFactory,
                           BooleanSupplier frozen) {
        this.panel = panel;
        this.cards = cards;
        this.cardFactory = cardFactory;
        this.frozen = frozen;
        panel.getElement().addEventListener("edge-top", event -> loadEarlier());
    }

    /**
     * Forgets the branch, for a story that has none.
     */
    public void clear() {
        window = null;
    }

    /**
     * Creates the cards of the last parts of the branch and scrolls to the bottom, or to the part when the story is
     * shown to find it (the cards before it are created too).
     *
     * @param limit            how many parts to load at a time, {@code null} for all
     * @param scrollTo         id of the part to scroll to, {@code null} for the bottom
     * @param openedReasonings ids of the parts whose reasoning stays opened
     */
    public void render(List<ChatMessage> branch, Integer limit, Long scrollTo, Set<Long> openedReasonings) {
        window = LazyBranch.tail(branch, limit);
        if (scrollTo != null) {
            window.loadEarlierThrough(scrollTo);
        }
        int total = window.total();
        int offset = window.offset();
        cards.setOffset(offset);

        List<ChatMessage> loaded = window.loaded();
        for (int i = 0; i < loaded.size(); i++) {
            ChatMessage message = loaded.get(i);
            int index = offset + i;
            C card = cardFactory.create(message, index == total - 1, index + 1);
            card.setFrozen(frozen.getAsBoolean());
            card.setReasoningOpened(openedReasonings.contains(message.getId()));
            cards.put(card);
            panel.add(card);
        }

        C target = scrollTo != null ? cards.get(scrollTo) : null;
        if (target != null) {
            JavascriptLibrary.scrollToAndHighlight(target.getElement());
        } else {
            JavascriptLibrary.scrollToBottomAfterLayout(panel.getElement());
        }
        JavascriptLibrary.watchEdge(panel.getElement(), true, window.hasEarlier());
    }

    /**
     * Makes sure the part has a card, creating the cards of the parts before it when they are not loaded yet.
     *
     * @return the card of the part, {@code null} when it is not on the branch
     */
    public C reveal(Long messageId) {
        if (window != null && !cards.contains(messageId)) {
            prepend(window.loadEarlierThrough(messageId), false);
        }
        return cards.get(messageId);
    }

    /**
     * Adds the next chunk of earlier parts above the first card and keeps the part the user is reading in place.
     */
    private void loadEarlier() {
        if (window == null || !window.hasEarlier()) {
            return;
        }
        prepend(window.loadEarlier(), true);
    }

    private void prepend(List<ChatMessage> earlier, boolean keepPosition) {
        if (earlier.isEmpty()) {
            return;
        }
        int offset = window.offset();
        List<C> created = new ArrayList<>(earlier.size());
        for (int i = 0; i < earlier.size(); i++) {
            C card = cardFactory.create(earlier.get(i), false, offset + i + 1);
            card.setFrozen(frozen.getAsBoolean());
            created.add(card);
        }

        if (keepPosition) {
            JavascriptLibrary.rememberDistanceFromBottom(panel.getElement());
        }
        for (C card : created.reversed()) {
            panel.addComponentAsFirst(card);
        }
        cards.prependAll(created);
        if (keepPosition) {
            JavascriptLibrary.restoreDistanceFromBottom(panel.getElement());
        }
        JavascriptLibrary.watchEdge(panel.getElement(), true, window.hasEarlier());
    }
}
