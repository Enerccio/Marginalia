package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The metadata lines of a part (ids, model, protocol, tokens, words), kept up to date while the part streams.
 */
@Extendable
public class MessageMetrics {

    private static final String NOT_AVAILABLE = "N/A";

    private final Localization loc;
    private final int orderId;

    private final Span header;
    private final Span idSpan;
    private final Span modelSpan;
    private final Span protocolSpan;
    private final Span tokensSpan;
    private final Span wordsSpan;

    /**
     * @param showProtocol whether the protocol line is shown (the phone card has room for it, the desktop one not)
     */
    public MessageMetrics(Localization loc, ChatMessage message, int orderId, boolean showProtocol) {
        this.loc = loc;
        this.orderId = orderId;

        header = new Span(loc.getValue(L.LABEL_NODE_METADATA));
        header.addClassNames(CssStyles.TEXT_SEMIBOLD, CssStyles.TEXT_XS);

        idSpan = new Span(idText(message));
        idSpan.addClassNames(CssStyles.TEXT_SEMIBOLD, CssStyles.TEXT_XS);

        modelSpan = new Span(modelText(message));
        modelSpan.addClassName(CssStyles.TEXT_XS);

        protocolSpan = showProtocol ? new Span(protocolText(message)) : null;
        if (protocolSpan != null) {
            protocolSpan.addClassName(CssStyles.TEXT_XS);
        }

        tokensSpan = new Span(tokensText(message));
        tokensSpan.addClassName(CssStyles.TEXT_XS);

        wordsSpan = new Span(wordsText(message));
        wordsSpan.addClassName(CssStyles.TEXT_XS);
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = Span.class))
    public Span getHeader() {
        return header;
    }

    /**
     * The lines under the header, in display order.
     */
    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = Component[].class),
            locals = {@APILocals(name = "lines", type = List.class)})
    public Component[] getLines() {
        List<Component> lines = new ArrayList<>();
        lines.add(idSpan);
        lines.add(modelSpan);
        if (protocolSpan != null) {
            lines.add(protocolSpan);
        }
        lines.add(tokensSpan);
        lines.add(wordsSpan);
        return lines.toArray(new Component[0]);
    }

    /**
     * @param card the message of the card, the id is of this one
     * @param msg  the message with the new values
     */
    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "card", type = ChatMessage.class),
                    @APIParameter(name = "msg", type = ChatMessage.class)})
    public void update(ChatMessage card, ChatMessage msg) {
        idSpan.setText(idText(card));
        modelSpan.setText(modelText(msg));
        if (protocolSpan != null) {
            protocolSpan.setText(protocolText(msg));
        }
        tokensSpan.setText(tokensText(msg));
        wordsSpan.setText(wordsText(msg));
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "message", type = ChatMessage.class)})
    private String idText(ChatMessage message) {
        return String.format(loc.getValue(L.LABEL_MESSAGE_IDS), orderId, (message.getId() != null ? message.getId() : NOT_AVAILABLE));
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "message", type = ChatMessage.class)})
    private String modelText(ChatMessage message) {
        return loc.getValue(L.LABEL_MODEL) + ": " + StringUtils.defaultIfBlank(message.getModelUsed(), NOT_AVAILABLE);
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "message", type = ChatMessage.class)})
    private String protocolText(ChatMessage message) {
        return loc.getValue(L.LABEL_PROTOCOLS) + ": " + StringUtils.defaultIfBlank(message.getProtocolUsed(), NOT_AVAILABLE);
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "message", type = ChatMessage.class)})
    private String tokensText(ChatMessage message) {
        return loc.getValue(L.LABEL_TOKENS) + ": " + message.getTokenCount() + " (" + message.getPromptTokens() + ")";
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "message", type = ChatMessage.class)})
    private String wordsText(ChatMessage message) {
        return loc.getValue(L.LABEL_WORDS) + ": " + message.getWordCount();
    }

}
