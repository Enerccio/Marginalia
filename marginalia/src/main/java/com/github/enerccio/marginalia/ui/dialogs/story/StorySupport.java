package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.traits.APIParameter;
import com.github.enerccio.marginalia.domain.traits.APIReturn;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.domain.traits.PublicAPI;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Small pieces the desktop and the phone story tab do the same way. Both tabs share the behaviour, so a decorator of
 * one of its methods applies to both.
 */
@Configurable
@Extendable
public class StorySupport {

    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * Stores the scroll position of the story panel on the last part of the branch, so the story opens where the
     * author left it (restored by {@code JavascriptLibrary.restoreScrollPosition}).
     */
    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "panel", type = ScrollPanel.class),
                    @APIParameter(name = "manuscript", type = Supplier.class),
                    @APIParameter(name = "frozen", type = BooleanSupplier.class)})
    public void trackScrollPosition(ScrollPanel panel, Supplier<Manuscript> manuscript, BooleanSupplier frozen) {
        panel.getElement().addEventListener("panel-scroll", event -> {
            if (frozen.getAsBoolean()) {
                return;
            }
            double pos = event.getEventData().get("event.detail.scrollTop").asDouble();
            Manuscript current = manuscript.get();
            if (current != null && current.getActiveLeaf() != null) {
                try {
                    ChatMessage leaf = chatMessageService.find(current.getActiveLeaf());
                    leaf.setScrollPosition((int) pos);
                    chatMessageService.save(leaf);
                } catch (Exception ignored) {}
            }
        }).addEventData("event.detail.scrollTop");

        JavascriptLibrary.fireDebouncedScrollEvent(panel.getElement());
    }

    /**
     * Stores the text the author edited: counts its tokens (with the tokenizer of the book's AI, a generic one when it
     * has none) and words, flags the part as edited and saves it.
     *
     * @return the saved message
     */
    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = ChatMessage.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class),
                    @APIParameter(name = "message", type = ChatMessage.class),
                    @APIParameter(name = "newResponse", type = String.class)})
    public ChatMessage saveEditedResponse(Manuscript manuscript, ChatMessage message, String newResponse) throws Exception {
        return chatMessageService.updateResponse(manuscript, message, newResponse).message();
    }

}
