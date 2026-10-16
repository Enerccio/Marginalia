package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.GenerationListener;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequestType;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.github.enerccio.marginalia.utils.UiProblems;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;

/**
 * Shows a running generation in a story tab: creates and fills the card of the new part as the chunks arrive, and
 * redraws the story when the generation ends. The tab reacts to the end through {@link Callbacks}.
 *
 * @param <C> the card component of the tab
 */
@Extendable
public class StoryGenerationListener<C extends Component & StoryCard> implements GenerationListener {

    /**
     * Creates the card of a part: the message, whether it is the last of the branch, its number in the story.
     */
    @FunctionalInterface
    public interface CardFactory<C> {
        C create(ChatMessage message, boolean isLast, int orderId);
    }

    /**
     * The state of the tab the generation changes.
     */
    public interface Callbacks {

        /**
         * The generation is over (finished or cancelled), no token to cancel any more.
         */
        void generationEnded();

        /**
         * A part was generated: time for the backup of the book.
         */
        void partGenerated();

        /**
         * The turn was discarded, the tab keeps what the user typed instead of re-syncing (and clearing) the form.
         */
        void turnDiscarded();

        /**
         * Reloads the manuscript, cleanup may have removed the new part and changed the active one.
         */
        void reloadManuscript() throws Exception;

        /**
         * Redraws the story.
         */
        void renderStory();
    }

    private final UI ui;
    private final GenerationRequest request;
    private final StoryCards<C> cards;
    private final ScrollPanel panel;
    private final CardFactory<C> cardFactory;
    private final StoryHost host;
    private final ChatMessageService chatMessageService;
    private final Localization loc;
    private final Callbacks callbacks;

    public StoryGenerationListener(UI ui, GenerationRequest request, StoryCards<C> cards, ScrollPanel panel,
                                   CardFactory<C> cardFactory, StoryHost host, ChatMessageService chatMessageService,
                                   Localization loc, Callbacks callbacks) {
        this.ui = ui;
        this.request = request;
        this.cards = cards;
        this.panel = panel;
        this.cardFactory = cardFactory;
        this.host = host;
        this.chatMessageService = chatMessageService;
        this.loc = loc;
        this.callbacks = callbacks;
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class))
    private void scrollToBottom() {
        JavascriptLibrary.scrollToBottomAfterLayout(panel.getElement());
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class))
    private void scrollToBottomIfAtBottom() {
        JavascriptLibrary.scrollToBottomIfAtBottom(panel.getElement());
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "message", type = ChatMessage.class),
                    @APIParameter(name = "orderId", type = int.class)},
            locals = {@APILocals(name = "card", type = Component.class)})
    private C addStreamingCard(ChatMessage message, int orderId) {
        C card = cardFactory.create(message, true, orderId);
        card.setFrozen(true);
        cards.put(card);
        cards.setStreaming(card);
        panel.add(card);
        return card;
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "message", type = ChatMessage.class)})
    @Override
    public void onNodeCreated(ChatMessage message) {
        ui.access(() -> {
            cards.markNoneLast();
            int newOrderId = cards.size() + 1;

            if (request.getRequestType() == GenerationRequestType.NEW_MESSAGE) {
                addStreamingCard(message, newOrderId);
            } else if (request.getRequestType() == GenerationRequestType.REGENERATE) {
                C card = cards.getOrStreaming(message);
                card.restartStreaming(message);
                cards.setStreaming(card);
            } else {
                C last = cards.last();
                cards.remove(last);
                panel.remove(last);
                addStreamingCard(message, newOrderId);
            }
            scrollToBottom();
            UIPushGuard.push(ui);
        });
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "chunk", type = String.class),
                    @APIParameter(name = "message", type = ChatMessage.class)})
    @Override
    public void onReasoningChunk(String chunk, ChatMessage message) {
        ui.access(() -> {
            C card = cards.getOrStreaming(message);
            if (card != null) {
                card.updateReasoning(message.getResponseReasoning());
                scrollToBottomIfAtBottom();
            }
            UIPushGuard.push(ui);
        });
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "chunk", type = String.class),
                    @APIParameter(name = "message", type = ChatMessage.class)})
    @Override
    public void onResponseChunk(String chunk, ChatMessage message) {
        ui.access(() -> {
            C card = cards.getOrStreaming(message);
            if (card != null) {
                // the card may hold another instance of the part, its diff view needs the text as generated so far
                card.getMessage().setOriginalResponse(message.getOriginalResponse());
                card.updateResponse(message.getResponse());
                scrollToBottomIfAtBottom();
            }
            UIPushGuard.push(ui);
        });
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "message", type = ChatMessage.class)})
    @Override
    public void onMetricsUpdated(ChatMessage message) {
        ui.access(() -> {
            C card = cards.getOrStreaming(message);
            if (card != null) {
                card.updateMetrics(message);
            }
            UIPushGuard.push(ui);
        });
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "message", type = ChatMessage.class)})
    @Override
    public void onComplete(ChatMessage message) {
        ui.access(() -> {
            callbacks.generationEnded();
            cards.setStreaming(null);
            host.unfreeze();

            callbacks.partGenerated();

            JavascriptLibrary.getScrollTop(panel.getElement())
                    .then(Integer.class, scrollTop -> {
                        saveScrollPosition(message, scrollTop);
                        callbacks.renderStory();
                        UIPushGuard.push(ui);
                    });
        });
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "partialMessage", type = ChatMessage.class)})
    @Override
    public void onCancelled(ChatMessage partialMessage) {
        ui.access(() -> {
            cards.markNoneLast();
            C streaming = cards.getStreaming();
            if (partialMessage == null) {
                callbacks.turnDiscarded();
                if (streaming != null)
                    panel.remove(streaming);
            } else {
                if (streaming != null && !partialMessage.getId().equals(streaming.getMessage().getId())) {
                    panel.remove(streaming);
                    C card = cardFactory.create(partialMessage, true, cards.size() + 1);
                    card.setFrozen(true);
                    cards.put(card);
                    cards.setStreaming(null);
                    panel.add(card);
                }
            }

            callbacks.generationEnded();
            cards.setStreaming(null);
            host.unfreeze();

            JavascriptLibrary.getScrollTop(panel.getElement())
                    .then(Integer.class, scrollTop -> {
                        saveScrollPosition(partialMessage, scrollTop);
                        try {
                            callbacks.reloadManuscript();
                        } catch (Exception e) {
                            UIUtils.internalServerError(loc, e);
                        }
                        callbacks.renderStory();
                        UIPushGuard.push(ui);
                    });
            UIPushGuard.push(ui);
        });
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "message", type = ChatMessage.class),
                    @APIParameter(name = "scrollTop", type = Integer.class)})
    private void saveScrollPosition(ChatMessage message, Integer scrollTop) {
        if (scrollTop != null && message != null) {
            message.setScrollPosition(scrollTop);
            try {
                chatMessageService.save(message);
            } catch (Exception ignored) {
            }
        }
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "cause", type = Throwable.class)})
    @Override
    public void onError(Throwable cause) {
        ui.access(() -> {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), cause);
            UIPushGuard.push(ui);
        });
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "error", type = String.class)})
    @Override
    public void onSimpleError(String error) {
        ui.access(() -> {
            Notification.error(error);
            UIPushGuard.push(ui);
        });
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "description", type = String.class)})
    @Override
    public void onInferenceError(String description) {
        ui.access(() -> {
            UiProblems.inference(description, null);
            Notification.error(description);
            UIPushGuard.push(ui);
        });
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "warning", type = String.class)})
    @Override
    public void onWarning(String warning) {
        ui.access(() -> {
            Notification.warning(warning);
            UIPushGuard.push(ui);
        });
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "question", type = String.class),
                    @APIParameter(name = "yes", type = Runnable.class),
                    @APIParameter(name = "no", type = Runnable.class)})
    @Override
    public void askQuestion(String question, Runnable yes, Runnable no) {
        ui.access(() -> {
            ConfirmDialog.show(question, yes, no);
            UIPushGuard.push(ui);
        });
    }

}
