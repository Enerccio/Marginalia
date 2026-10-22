package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.WebhookFormat;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.AgentAttentionService.Notice;

/**
 * Tells a user who has the application closed that an agent of theirs needs them, by a message to the URL they put in their settings
 * ({@code UserSetting.agentWebhookUrl}): ntfy, Gotify, a Discord channel... (the {@link WebhookFormat} is what the service behind
 * the URL expects). It is the push to a phone that the open screens get from {@link AgentAttentionService}; that service calls this
 * one for a change that needs the user when no screen of the user is open (or the user asked for the message always).
 * <p>
 * The server calls a URL a user typed, so an administrator can turn the messages off for everybody ({@link #isAllowed()}) and gives
 * them to single users: the permission {@code NOTIFICATIONS} ({@link #mayNotify}), whose setting {@code notificationsAllowLocal} lets
 * the messages go to the machine of the server and to private networks ({@link #mayUseLocalAddresses}); without it only public
 * addresses are called. Administrators need neither. Only {@code http} and {@code https} are called, redirects are not followed, the answer is not read, and the URL (which may hold a
 * secret) is never written to the log.
 */
public interface AgentWebhookService {

    /**
     * Sends the message about the change to the user's webhook, if they have one, the application allows them and the change needs
     * the user. Done on a thread of its own, never throws: a service that is down is a line in the log.
     *
     * @param screenOpen whether a screen of the user is open now; the message is then sent only when the user wants it always
     */
    void notify(Notice notice, boolean screenOpen);

    /**
     * Sends a test message to the URL for the current user, for the button in the settings, and waits for the answer. A user who may
     * not get messages, or whose URL is not allowed ({@link #check(User, String)}), gets the problem without a request.
     *
     * @return null when the service accepted the message, else what went wrong
     */
    String sendTest(String url, WebhookFormat format);

    /**
     * What is wrong with the URL as a webhook, null when it can be used.
     */
    String check(String url);

    /**
     * What is wrong with the URL for this user: what {@link #check(String)} finds, then the address: unless the user may use local
     * addresses the host must resolve and all its addresses must be public (not loopback, link-local, private, shared, multicast or
     * unspecified). The name is resolved here and again when the message is sent, so a name that changes its answer in between
     * (DNS rebinding) is not caught.
     *
     * @return null when the URL can be used by the user
     */
    String check(User user, String url);

    /**
     * Whether the user may get messages at all: an administrator, or a user with the permission {@code NOTIFICATIONS}.
     *
     * @param user may be the session's proxy of the user
     */
    boolean mayNotify(User user) throws Exception;

    /**
     * Whether the messages of the user may go to addresses on the server's machine and in private networks: an administrator, or a
     * user whose permission {@code NOTIFICATIONS} is granted and has the setting.
     *
     * @param user may be the session's proxy of the user
     */
    boolean mayUseLocalAddresses(User user) throws Exception;

    /**
     * Whether the application sends messages to webhooks of users at all.
     */
    boolean isAllowed() throws Exception;

    /**
     * Turns the messages on or off for the whole application.
     *
     * @throws SecurityException when the caller is not an administrator
     */
    void setAllowed(boolean allowed) throws Exception;
}
