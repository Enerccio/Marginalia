package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.WebhookFormat;
import com.github.enerccio.marginalia.domain.model.impl.settings.AppSettings;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.security.AdminGuard;
import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.security.service.PermissionService;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.domain.service.AgentAttentionService.Notice;
import com.github.enerccio.marginalia.domain.service.AgentWebhookService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Language;
import com.github.enerccio.marginalia.loc.Localization;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AgentWebhookServiceImpl implements AgentWebhookService, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(AgentWebhookServiceImpl.class);

    static final int MAX_URL_LENGTH = 2000;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    @Autowired
    @Lazy
    private SettingService settingService;

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    private Localization loc;

    @Autowired
    private AdminGuard adminGuard;

    @Autowired
    @Lazy
    private PermissionService permissionService;

    // the user of the session, for the test message; the delivery thread has the user of the notice
    @Autowired
    private User currentUser;

    // a redirect would send the message somewhere the user did not type
    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(CONNECT_TIMEOUT).build();

    // one thread, so the messages of a run arrive in the order they were made and a slow service holds up nobody else
    private final ExecutorService delivery = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "agent-webhook");
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public void notify(Notice notice, boolean screenOpen) {
        if (notice == null || !notice.attention()) {
            return;
        }
        try {
            delivery.execute(() -> {
                try {
                    deliver(notice, screenOpen);
                } catch (Exception e) {
                    log.warn("The message to the webhook of user {} could not be sent: {}", notice.userId(), e.getMessage());
                }
            });
        } catch (RuntimeException e) {
            // the server is going down
            log.debug("The message to the webhook was not queued: {}", e.getMessage());
        }
    }

    private void deliver(Notice notice, boolean screenOpen) throws Exception {
        if (!isAllowed()) {
            return;
        }
        User user = userService.find(notice.userId());
        if (user == null) {
            return;
        }
        // read only: this thread must not write while the others do
        UserSetting setting = settingService.find(UserSetting.class, user);
        String url = setting == null ? null : setting.getAgentWebhookUrl();
        if (StringUtils.isBlank(url) || !mayNotify(user) || check(user, url.strip()) != null) {
            return;
        }
        if (screenOpen && !Boolean.TRUE.equals(setting.getAgentWebhookAlways())) {
            return;
        }
        Language language = loc.resolveLanguage(setting.getLanguage());
        String problem = send(url.strip(), setting.getAgentWebhookFormat(), notice.agentName(), text(language, notice));
        if (problem != null) {
            log.warn("The message to the webhook of user {} was not accepted: {}", notice.userId(), problem);
        }
    }

    /**
     * What the change says, in the language of the user.
     */
    private String text(Language language, Notice notice) {
        String reason = notice.reason() == null ? "" : notice.reason();
        return switch (notice.status()) {
            case WAITING_APPROVAL -> String.format(loc.getValue(language, L.MSG_AGENT_NOTICE_APPROVAL), notice.agentName(), reason);
            case DONE -> String.format(loc.getValue(language, L.MSG_AGENT_NOTICE_DONE), notice.agentName());
            case FAILED -> String.format(loc.getValue(language, L.MSG_AGENT_NOTICE_FAILED), notice.agentName());
            case PAUSED -> String.format(loc.getValue(language, L.MSG_AGENT_NOTICE_PAUSED), notice.agentName(), reason);
            default -> String.format(loc.getValue(language, L.MSG_AGENT_NOTICE_IDLE), notice.agentName());
        };
    }

    @Override
    public String sendTest(String url, WebhookFormat format) {
        try {
            if (!mayNotify(currentUser)) {
                return "You may not get messages";
            }
        } catch (Exception e) {
            return "The permission could not be checked";
        }
        String problem = check(currentUser, url);
        if (problem != null) {
            return problem;
        }
        return send(url.strip(), format, "Marginalia", loc.getValue(L.MSG_AGENT_WEBHOOK_TEST_TEXT));
    }

    @Override
    public String check(String url) {
        if (StringUtils.isBlank(url)) {
            return "The URL is empty";
        }
        if (url.strip().length() > MAX_URL_LENGTH) {
            return "The URL is longer than " + MAX_URL_LENGTH + " characters";
        }
        try {
            URI uri = URI.create(url.strip());
            if (uri.getScheme() == null || !(uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https"))) {
                return "Only http and https URLs are called";
            }
            if (StringUtils.isBlank(uri.getHost())) {
                return "The URL has no host";
            }
            return null;
        } catch (IllegalArgumentException e) {
            return "The URL is not valid";
        }
    }

    @Override
    public String check(User user, String url) {
        String problem = check(url);
        if (problem != null) {
            return problem;
        }
        try {
            if (mayUseLocalAddresses(user)) {
                return null;
            }
            // one answer for a local address and for a name that does not resolve, so the answer says nothing about internal names
            String onlyPublic = "Only addresses on the internet are called: this one is on the machine of the server, in a private network or can not be found";
            InetAddress[] addresses;
            try {
                addresses = InetAddress.getAllByName(URI.create(url.strip()).getHost());
            } catch (java.net.UnknownHostException e) {
                return onlyPublic;
            }
            for (InetAddress address : addresses) {
                if (isLocal(address)) {
                    return onlyPublic;
                }
            }
            return null;
        } catch (Exception e) {
            return "The address could not be checked";
        }
    }

    /**
     * Whether the address is not one of the internet: this machine, a private or link-local network (the address of the
     * metadata service of a cloud host is one), the shared range of carriers, a unique local IPv6 address, multicast or
     * unspecified.
     */
    public static boolean isLocal(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return true;
        }
        byte[] bytes = address.getAddress();
        if (bytes.length == 4) {
            int first = bytes[0] & 0xff;
            int second = bytes[1] & 0xff;
            return first == 0 || (first == 100 && second >= 64 && second <= 127);
        }
        return (bytes[0] & 0xfe) == 0xfc;
    }

    @Override
    public boolean mayNotify(User user) throws Exception {
        User holder = holder(user);
        if (holder == null) {
            return false;
        }
        if (holder.isAdmin()) {
            return true;
        }
        Permission permission = permissionService.getPermission(holder, PermissionType.NOTIFICATIONS);
        return permission != null && permission.isGranted();
    }

    @Override
    public boolean mayUseLocalAddresses(User user) throws Exception {
        User holder = holder(user);
        if (holder == null) {
            return false;
        }
        if (holder.isAdmin()) {
            return true;
        }
        Permission permission = permissionService.getPermission(holder, PermissionType.NOTIFICATIONS);
        return permission != null && permission.isGranted() && permission.getNotificationsAllowLocal();
    }

    // the loaded user has the flag of the administrator, the session's proxy does not
    private User holder(User user) throws Exception {
        User holder = user == null || user.getId() == null ? null : userService.find(user.getId());
        return holder == null || holder.isDeleted() ? null : holder;
    }

    /**
     * Sends the message; the URL is not written to the log or to the answer.
     *
     * @return null when the service answered with a success status
     */
    private String send(String url, WebhookFormat format, String title, String message) {
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url)).timeout(REQUEST_TIMEOUT);
            switch (format == null ? WebhookFormat.PLAIN : format) {
                case PLAIN -> {
                    // a header holds Latin letters only; another title is left out and the text says it anyway
                    if (title != null && title.chars().allMatch(c -> c >= 0x20 && c < 0x7f)) {
                        request.header("Title", title);
                    }
                    request.header("Content-Type", "text/plain; charset=utf-8")
                            .POST(HttpRequest.BodyPublishers.ofString(message, StandardCharsets.UTF_8));
                }
                case DISCORD -> {
                    JsonObject body = new JsonObject();
                    body.addProperty("content", "**" + title + "**\n" + message);
                    request.header("Content-Type", "application/json; charset=utf-8")
                            .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8));
                }
                case GOTIFY -> {
                    JsonObject body = new JsonObject();
                    body.addProperty("title", title);
                    body.addProperty("message", message);
                    body.addProperty("priority", 5);
                    request.header("Content-Type", "application/json; charset=utf-8")
                            .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8));
                }
            }
            HttpResponse<Void> response = client.send(request.build(), HttpResponse.BodyHandlers.discarding());
            return response.statusCode() / 100 == 2 ? null : "The service answered with the status " + response.statusCode();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Interrupted";
        } catch (Exception e) {
            // the message of a connection error can hold the host, never the path with the secret
            return "The service could not be reached: " + e.getClass().getSimpleName();
        }
    }

    @Override
    public boolean isAllowed() throws Exception {
        AppSettings app = settingService.find(AppSettings.class, null);
        return app == null || !Boolean.TRUE.equals(app.getAgentWebhooksDisabled());
    }

    @Override
    public void setAllowed(boolean allowed) throws Exception {
        adminGuard.requireAdmin();
        AppSettings app = settingService.getOrCreateApp(AppSettings.class);
        app.setAgentWebhooksDisabled(allowed ? null : Boolean.TRUE);
        settingService.save(app);
    }

    @Override
    public void destroy() {
        delivery.shutdownNow();
    }
}
