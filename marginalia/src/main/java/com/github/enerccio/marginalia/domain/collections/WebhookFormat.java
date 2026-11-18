package com.github.enerccio.marginalia.domain.collections;

import com.github.enerccio.marginalia.loc.LocalizationKey;
import com.github.enerccio.marginalia.loc.M_L;

/**
 * What the body of the message to the webhook of a user looks like, which is what the service behind the URL expects.
 */
public enum WebhookFormat {

    /**
     * The text of the message as the body, the name of the agent as the {@code Title} header: ntfy and the services that take
     * the same.
     */
    PLAIN(M_L.ENUM_WEBHOOK_FORMAT_PLAIN),

    /**
     * A JSON object with the text as {@code content}: a Discord webhook (and Slack-compatible ones that accept it).
     */
    DISCORD(M_L.ENUM_WEBHOOK_FORMAT_DISCORD),

    /**
     * A JSON object with {@code title}, {@code message} and {@code priority}: Gotify.
     */
    GOTIFY(M_L.ENUM_WEBHOOK_FORMAT_GOTIFY);

    private final LocalizationKey label;

    WebhookFormat(LocalizationKey label) {
        this.label = label;
    }

    public LocalizationKey getLabel() {
        return label;
    }
}
