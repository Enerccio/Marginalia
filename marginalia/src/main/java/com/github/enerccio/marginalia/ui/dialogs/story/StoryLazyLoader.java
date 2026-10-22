package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.widgets.LazyLoadIndicator;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.DomEvent;

import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * Keeps the panel of a story tab filled with the cards of a window over the branch (the size is the setting of the
 * user): the tab opens with the last parts at the bottom of the story, scrolling to the top adds the earlier ones and
 * drops the ones at the bottom, scrolling down does the opposite. An indicator at each edge shows that there is more
 * to load, so a user who scrolls faster than the parts arrive sees that instead of the end of the story. Both the
 * desktop and the phone story tab use it.
 *
 * @param <C> the card component of the tab
 */
public class StoryLazyLoader<C extends Component & StoryCard> {

    private static final String FAR = "event.detail.far";

    /**
     * Parts are dropped at the edge opposite to the one the user scrolls to only when that edge is at least this many
     * pixels away, otherwise a window shorter than the screen would load and drop parts forever.
     */
    private static final double DROP_DISTANCE = 1500;

    private final ScrollPanel panel;
    private final StoryCards<C> cards;
    private final StoryGenerationListener.CardFactory<C> cardFactory;
    private final BooleanSupplier frozen;
    private final LazyLoadIndicator earlierIndicator;
    private final LazyLoadIndicator laterIndicator;

    private LazyBranch window;

    public StoryLazyLoader(ScrollPanel panel, StoryCards<C> cards, StoryGenerationListener.CardFactory<C> cardFactory,
                           BooleanSupplier frozen, Localization loc) {
        this.panel = panel;
        this.cards = cards;
        this.cardFactory = cardFactory;
        this.frozen = frozen;
        this.earlierIndicator = new LazyLoadIndicator(loc.getValue(L.MSG_LOADING_EARLIER_PARTS));
        this.laterIndicator = new LazyLoadIndicator(loc.getValue(L.MSG_LOADING_LATER_PARTS));
        panel.setHeader(earlierIndicator);
        panel.setFooter(laterIndicator);
        panel.getElement().addEventListener("edge-top", event -> loadEarlier(far(event))).addEventData(FAR);
        panel.getElement().addEventListener("edge-bottom", event -> loadLater(far(event))).addEventData(FAR);
    }

    /**
     * Forgets the branch, for a story that has none.
     */
    public void clear() {
        window = null;
        earlierIndicator.setVisible(false);
        laterIndicator.setVisible(false);
    }

    /**
     * Creates the cards of the last parts of the branch and scrolls to the bottom, or to the part when the story is
     * shown to find it (the window is moved to it).
     *
     * @param limit            the size of the window, {@code null} for all the parts
     * @param scrollTo         id of the part to scroll to, {@code null} for the bottom
     * @param openedReasonings ids of the parts whose reasoning stays opened
     */
    public void render(List<ChatMessage> branch, Integer limit, Long scrollTo, Set<Long> openedReasonings) {
        window = LazyBranch.tail(branch, limit);
        if (scrollTo != null) {
            window.centerOn(scrollTo);
        }
        populate(openedReasonings);

        C target = scrollTo != null ? cards.get(scrollTo) : null;
        if (target != null) {
            JavascriptLibrary.scrollToAndHighlight(target.getElement());
        } else {
            JavascriptLibrary.scrollToBottomAfterLayout(panel.getElement());
        }
    }

    /**
     * Makes sure the last part of the branch has a card, as a new part is added after it: when the user scrolled away
     * from the end, the window moves back there and the story scrolls to the bottom.
     */
    public void showTail() {
        if (window == null || window.atTail()) {
            return;
        }
        syncLoaded();
        window.moveToTail();
        populate(cards.collectOpenedReasonings());
        JavascriptLibrary.scrollToBottomAfterLayout(panel.getElement());
    }

    /**
     * Scrolls to the card of the part and flashes it, the window moves to the part first when it is not loaded
     * (the outline).
     */
    public void scrollTo(Long messageId) {
        if (window == null || window.indexOf(messageId) < 0) {
            return;
        }
        if (!window.isLoaded(messageId)) {
            syncLoaded();
            window.centerOn(messageId);
            populate(cards.collectOpenedReasonings());
        }
        C card = cards.get(messageId);
        if (card != null) {
            JavascriptLibrary.scrollToAndHighlight(card.getElement());
        }
    }

    /**
     * Brings the match of a search to the screen: the window moves to the part when it is not loaded, the card is drawn
     * again when it was (the mark of the current match changed) and the story scrolls to the mark with the CSS class.
     */
    public void showMatch(Long messageId, String currentCssClass) {
        if (window == null || window.indexOf(messageId) < 0) {
            return;
        }
        if (!window.isLoaded(messageId)) {
            syncLoaded();
            window.centerOn(messageId);
            populate(cards.collectOpenedReasonings());
        }
        C card = cards.get(messageId);
        if (card != null) {
            card.renderResponse();
            JavascriptLibrary.scrollToSearchMatch(card.getElement(), currentCssClass);
        }
    }

    /**
     * Draws the response of the cards of these parts again, the ones that have no card are skipped.
     */
    public void renderResponses(Collection<Long> messageIds) {
        for (Long id : messageIds) {
            C card = cards.get(id);
            if (card != null) {
                card.renderResponse();
            }
        }
    }

    /**
     * Creates the cards of the window in place of the ones that are there.
     */
    private void populate(Set<Long> openedReasonings) {
        panel.removeAll();
        cards.clear();
        cards.setOffset(window.offset());

        List<ChatMessage> loaded = window.loaded();
        for (int i = 0; i < loaded.size(); i++) {
            C card = createCard(loaded.get(i), window.offset() + i, openedReasonings);
            cards.put(card);
            panel.add(card);
        }
        settle(false);
    }

    private C createCard(ChatMessage message, int index, Set<Long> openedReasonings) {
        C card = cardFactory.create(message, index == window.total() - 1, index + 1);
        card.setFrozen(frozen.getAsBoolean());
        card.setReasoningOpened(openedReasonings.contains(message.getId()));
        return card;
    }

    /**
     * Adds the earlier parts above the first card and, when the bottom is far enough, drops the last ones; the part the
     * user is reading stays in place.
     */
    private void loadEarlier(double far) {
        if (window == null || !window.hasEarlier()) {
            return;
        }
        List<ChatMessage> added = window.loadEarlier();
        List<ChatMessage> dropped = canDrop(far) ? window.trimLater() : List.of();
        removeCards(dropped);

        List<C> created = new ArrayList<>(added.size());
        for (int i = 0; i < added.size(); i++) {
            created.add(createCard(added.get(i), window.offset() + i, Set.of()));
        }
        for (C card : created.reversed()) {
            panel.addComponentAsFirst(card);
        }
        cards.prependAll(created);
        cards.setOffset(window.offset());
        settle(true);
    }

    /**
     * Adds the later parts below the last card and, when the top is far enough, drops the first ones.
     */
    private void loadLater(double far) {
        if (window == null || !window.hasLater()) {
            return;
        }
        int start = window.offset() + window.loaded().size();
        List<ChatMessage> added = window.loadLater();
        List<ChatMessage> dropped = canDrop(far) ? window.trimEarlier() : List.of();
        removeCards(dropped);

        for (int i = 0; i < added.size(); i++) {
            C card = createCard(added.get(i), start + i, Set.of());
            cards.put(card);
            panel.add(card);
        }
        cards.setOffset(window.offset());
        settle(true);
    }

    /**
     * Parts are never dropped while a part is generated, its card is the last one and the branch does not have it.
     */
    private boolean canDrop(double far) {
        return far >= DROP_DISTANCE && !frozen.getAsBoolean();
    }

    /**
     * Removes the cards of the parts the window dropped. A part the user is editing is saved first, and the instance of
     * the card goes back to the window, so the card made of the part later shows the text as it is now.
     */
    private void removeCards(List<ChatMessage> dropped) {
        for (ChatMessage message : dropped) {
            C card = cards.get(message.getId());
            if (card == null) {
                continue;
            }
            if (card.isEditing()) {
                card.autosaveAndSwapToMarkdown();
            }
            window.refresh(card.getMessage());
            cards.remove(card);
            panel.remove(card);
        }
    }

    private void syncLoaded() {
        for (C card : new HashSet<>(cards.values())) {
            window.refresh(card.getMessage());
        }
    }

    /**
     * Shows the indicators of the edges that have more, arms the edges and, after parts were added or dropped around
     * the part the user reads, puts it back in place.
     */
    private void settle(boolean keepPosition) {
        earlierIndicator.setVisible(window.hasEarlier());
        laterIndicator.setVisible(window.hasLater());
        if (keepPosition) {
            JavascriptLibrary.restoreAnchor(panel.getElement(), panel.getContent().getElement());
        }
        JavascriptLibrary.watchEdge(panel.getElement(), panel.getContent().getElement(), true, window.hasEarlier());
        JavascriptLibrary.watchEdge(panel.getElement(), panel.getContent().getElement(), false, window.hasLater());
    }

    private static double far(DomEvent event) {
        var far = event.getEventData().get(FAR);
        return far == null ? 0 : far.asDouble();
    }
}
