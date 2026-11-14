package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.ui.dialogs.manuscript.GenerationHold;
import com.vaadin.flow.component.Component;

import java.util.*;

/**
 * The cards shown by a story tab: the ones of the parts on the branch by message id, and the one that is being
 * streamed into (it is not in the map until it has an id).
 */
@Extendable
public class StoryCards<C extends Component & StoryCard> {

    private Map<Long, C> active = new LinkedHashMap<>();
    private C streaming;
    // what a generation of the book holds, null when none runs
    private GenerationHold hold;
    // how many parts of the branch are before the first card (the story loads the tail of the branch only)
    private int offset;

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class))
    public void clear() {
        active.clear();
        offset = 0;
    }

    /**
     * Number of parts of the branch that have no card because they are before the first one; the cards numbered
     * after it start counting there.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = int.class))
    public int getOffset() {
        return offset;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "offset", type = int.class)})
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Puts the cards of the earlier parts before the ones that are there, so {@link #last()} stays the last part.
     *
     * @param earlier the cards in the order of the branch
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "earlier", type = List.class)},
            locals = {@APILocals(name = "merged", type = Map.class),
                    @APILocals(name = "card", type = Component.class),
                    @APILocals(name = "id", type = Long.class)})
    public void prependAll(List<C> earlier) {
        Map<Long, C> merged = new LinkedHashMap<>();
        for (C card : earlier) {
            Long id = card.getMessage().getId();
            if (id != null) {
                merged.put(id, card);
            }
        }
        merged.putAll(active);
        active = merged;
        offset = Math.max(0, offset - earlier.size());
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "card", type = Component.class)},
            locals = {@APILocals(name = "id", type = Long.class)})
    public void put(C card) {
        Long id = card.getMessage().getId();
        if (id != null) {
            active.put(id, card);
        }
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "messageId", type = Long.class)})
    public C get(Long messageId) {
        return active.get(messageId);
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = boolean.class),
            parameters = {@APIParameter(name = "messageId", type = Long.class)})
    public boolean contains(Long messageId) {
        return active.containsKey(messageId);
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = int.class))
    public int size() {
        return active.size();
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = Collection.class))
    public Collection<C> values() {
        return active.values();
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "cards", type = List.class)})
    public C last() {
        List<C> cards = active.values().stream().toList();
        return cards.getLast();
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "card", type = Component.class)})
    public void remove(C card) {
        active.remove(card.getMessage().getId());
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = Component.class))
    public C getStreaming() {
        return streaming;
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "card", type = Component.class)})
    public void setStreaming(C card) {
        this.streaming = card;
    }

    /**
     * The card of the message, the streaming card for a message that has none (yet).
     */
    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "message", type = ChatMessage.class)})
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
    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = Set.class),
            locals = {@APILocals(name = "opened", type = Set.class),
                    @APILocals(name = "card", type = Component.class)})
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

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "card", type = Component.class)})
    public void markNoneLast() {
        for (C card : active.values()) {
            card.setLastMessage(false);
        }
        if (streaming != null) {
            streaming.setLastMessage(false);
        }
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "card", type = Component.class)})
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

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "frozen", type = boolean.class)},
            locals = {@APILocals(name = "card", type = Component.class)})
    public void setFrozen(boolean frozen) {
        for (C card : active.values()) {
            card.setFrozen(frozen);
        }
        if (streaming != null) {
            streaming.setFrozen(frozen);
        }
    }

    /**
     * A generation of the book holds the story (null: none does). While it holds the whole book every card is frozen; from the
     * inference on only the card of the part it writes is, and the others, all on the branch it continues, keep their place (their
     * text can be edited, annotated and redrafted; delete, merge and branch are off, see {@link StoryCard#isKeptInPlace()}).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "hold", type = GenerationHold.class)},
            locals = {@APILocals(name = "card", type = Component.class)})
    public void setHold(GenerationHold hold) {
        this.hold = hold;
        for (C card : active.values()) {
            applyHold(card);
        }
        if (streaming != null) {
            applyHold(streaming);
        }
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = GenerationHold.class))
    public GenerationHold getHold() {
        return hold;
    }

    /**
     * Gives the card what the generation holds of its part (a card that is added while a generation runs).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "card", type = Component.class)},
            locals = {@APILocals(name = "id", type = Long.class)})
    public void applyHold(C card) {
        if (hold == null) {
            card.setFrozen(false);
            card.setKeptInPlace(false);
            return;
        }
        Long id = card.getMessage() == null ? null : card.getMessage().getId();
        card.setFrozen(card == streaming || id == null || hold.writes(id));
        // the story shows the branch the generation continues: every part of it stays where it is
        card.setKeptInPlace(true);
    }

}
