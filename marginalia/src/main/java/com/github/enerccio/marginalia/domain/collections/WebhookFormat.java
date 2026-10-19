package com.github.enerccio.marginalia.domain.collections;

import com.github.enerccio.marginalia.loc.L;

/**
 * What the body of the message to the webhook of a user looks like, which is what the service behind the URL expects.
 */
public enum WebhookFormat {

    /**
     * The text of the message as the body, the name of the agent as the {@code Title} header: ntfy and the services that take
     * the same.
     */
    PLAIN(L.ENUM_WEBHOOK_FORMAT_PLAIN),

    /**
     * A JSON object with the text as {@code content}: a Discord webhook (and Slack-compatible ones that accept it).
     */
    DISCORD(L.ENUM_WEBHOOK_FORMAT_DISCORD),

    /**
     * A JSON object with {@code title}, {@code message} and {@code priority}: Gotify.
     */
    GOTIFY(L.ENUM_WEBHOOK_FORMAT_GOTIFY);

    private final L label;

    WebhookFormat(L label) {
        this.label = label;
    }

    public L getLabel() {
        return label;
    }
}
