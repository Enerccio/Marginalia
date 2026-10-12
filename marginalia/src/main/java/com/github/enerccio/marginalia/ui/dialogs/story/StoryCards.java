package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.vaadin.flow.component.Component;

import java.util.*;

/**
 * The cards shown by a story tab: the ones of the parts on the branch by message id, and the one that is being
 * streamed into (it is not in the map until it has an id).
 */
@Extendable
public class StoryCards<C extends Component & StoryCard> {

    private final Map<Long, C> active = new LinkedHashMap<>();
    private C streaming;

    public void clear() {
        active.clear();
    }

    public void put(C card) {
        Long id = card.getMessage().getId();
        if (id != null) {
            active.put(id, card);
        }
    }

    public C get(Long messageId) {
        return active.get(messageId);
    }

    public boolean contains(Long messageId) {
        return active.containsKey(messageId);
    }

    public int size() {
        return active.size();
    }

    public Collection<C> values() {
        return active.values();
    }

    public C last() {
        List<C> cards = active.values().stream().toList();
        return cards.getLast();
    }

    public void remove(C card) {
        active.remove(card.getMessage().getId());
    }

    public C getStreaming() {
        return streaming;
    }

    public void setStreaming(C card) {
        this.streaming = card;
    }

    /**
     * The card of the message, the streaming card for a message that has none (yet).
     */
    public C getOrStreaming(ChatMessage message) {
        if (message == null) return streaming;
        if (message.getId() != null && active.containsKey(message.getId())) {
            return active.get(message.getId());
        }
        return streaming;
    }

    /**
     * Ids of the parts whose reasoning is opened, to carry that over to the cards of a redraw.
     */
    public Set<Long> collectOpenedReasonings() {
        Set<Long> opened = new HashSet<>();
        for (C card : active.values()) {
            if (card.isReasoningOpened()) {
                opened.add(card.getMessage().getId());
            }
        }
        if (streaming != null && streaming.getMessage().getId() != null && streaming.isReasoningOpened()) {
            opened.add(streaming.getMessage().getId());
        }
        return opened;
    }

    public void markNoneLast() {
        for (C card : active.values()) {
            card.setLastMessage(false);
        }
        if (streaming != null) {
            streaming.setLastMessage(false);
        }
    }

    public void autosaveAndSwapAllToMarkdown() {
        for (C card : active.values()) {
            if (card.isEditing()) {
                card.autosaveAndSwapToMarkdown();
            }
        }
        if (streaming != null && streaming.isEditing()) {
            streaming.autosaveAndSwapToMarkdown();
        }
    }

    public void setFrozen(boolean frozen) {
        for (C card : active.values()) {
            card.setFrozen(frozen);
        }
        if (streaming != null) {
            streaming.setFrozen(frozen);
        }
    }

}
