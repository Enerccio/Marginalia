package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.collections.WebhookFormat;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.AgentAttentionService;
import com.github.enerccio.marginalia.domain.service.AgentAttentionService.Notice;
import com.github.enerccio.marginalia.domain.service.AgentWebhookService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.service.impl.AgentWebhookServiceImpl;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The message to the webhook of a user whose agent needs them while the application is closed.
 */
class AgentWebhookTest extends AgentTestBase {

    /** What the fake service received. */
    private record Received(String method, String title, String contentType, String body) {
    }

    @Autowired
    private AgentWebhookService webhooks;

    @Autowired
    private AgentAttentionService attention;

    @Autowired
    private SettingService settingService;

    private HttpServer server;
    private final List<Received> received = new CopyOnWriteArrayList<>();
    private final AtomicInteger status = new AtomicInteger(200);
    private User user;
    private Agent agent;

    @BeforeEach
    void setUp() throws Exception {
        user = loginAgentUser();
        // the fake service is on 127.0.0.1
        grantNotifications(true, true);
        agent = agent();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/hook", exchange -> {
            received.add(new Received(exchange.getRequestMethod(), exchange.getRequestHeaders().getFirst("Title"),
                    exchange.getRequestHeaders().getFirst("Content-Type"),
                    new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)));
            if (status.get() / 100 == 3) {
                exchange.getResponseHeaders().add("Location", "http://127.0.0.1:1/elsewhere");
            }
            exchange.sendResponseHeaders(status.get(), -1);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.stop(0);
        loginAdmin();
        webhooks.setAllowed(true);
    }

    private void grantNotifications(boolean granted, boolean local) {
        updatePermission(user, PermissionType.NOTIFICATIONS, permission -> {
            permission.setGranted(granted);
            permission.setNotificationsAllowLocal(local);
        });
    }

    private String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/hook";
    }

    private void configure(WebhookFormat format, boolean always) throws Exception {
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        setting.setAgentWebhookUrl(url());
        setting.setAgentWebhookFormat(format);
        setting.setAgentWebhookAlways(always ? Boolean.TRUE : null);
        settingService.save(setting);
    }

    private Notice waits() {
        return new Notice(user.getId(), "run", agent.getUuid(), agent.getName(), AgentRunStatus.IDLE, null, true);
    }

    private Received next() throws Exception {
        long end = System.currentTimeMillis() + 5_000;
        while (received.isEmpty() && System.currentTimeMillis() < end) {
            TimeUnit.MILLISECONDS.sleep(20);
        }
        assertThat(received).as("a message arrived").isNotEmpty();
        return received.getFirst();
    }

    private void expectNothing() throws Exception {
        // the delivery has its own thread: a message that was going to come would have by now
        TimeUnit.MILLISECONDS.sleep(500);
        assertThat(received).isEmpty();
    }

    @Test
    void aPlainMessageHasTheTextAsTheBodyAndTheAgentAsTheTitle() throws Exception {
        configure(WebhookFormat.PLAIN, false);

        webhooks.notify(waits(), false);

        Received message = next();
        assertThat(message.method()).isEqualTo("POST");
        assertThat(message.title()).isEqualTo(agent.getName());
        assertThat(message.contentType()).startsWith("text/plain");
        assertThat(message.body()).isEqualTo("The agent \"" + agent.getName() + "\" waits for you.");
    }

    @Test
    void aDiscordMessageIsAJsonObjectWithContent() throws Exception {
        configure(WebhookFormat.DISCORD, false);

        webhooks.notify(waits(), false);

        JsonObject body = JsonParser.parseString(next().body()).getAsJsonObject();
        assertThat(body.get("content").getAsString()).contains(agent.getName()).contains("waits for you");
    }

    @Test
    void aGotifyMessageHasTitleMessageAndPriority() throws Exception {
        configure(WebhookFormat.GOTIFY, false);

        webhooks.notify(waits(), false);

        JsonObject body = JsonParser.parseString(next().body()).getAsJsonObject();
        assertThat(body.get("title").getAsString()).isEqualTo(agent.getName());
        assertThat(body.get("message").getAsString()).contains("waits for you");
        assertThat(body.get("priority").getAsInt()).isPositive();
    }

    @Test
    void aTitleWithLettersAHeaderCannotHoldIsLeftOutOfTheHeader() throws Exception {
        configure(WebhookFormat.PLAIN, false);
        Notice notice = new Notice(user.getId(), "run", agent.getUuid(), "Pisatelň 世界", AgentRunStatus.DONE, null, true);

        webhooks.notify(notice, false);

        Received message = next();
        assertThat(message.title()).isNull();
        assertThat(message.body()).contains("Pisatelň 世界").contains("finished");
    }

    @Test
    void aScreenThatIsOpenIsEnoughUnlessTheUserWantsTheMessageAlways() throws Exception {
        configure(WebhookFormat.PLAIN, false);
        webhooks.notify(waits(), true);
        expectNothing();

        configure(WebhookFormat.PLAIN, true);
        webhooks.notify(waits(), true);
        next();
    }

    @Test
    void aChangeThatNeedsNobodyIsNotSent() throws Exception {
        configure(WebhookFormat.PLAIN, true);

        webhooks.notify(new Notice(user.getId(), "run", agent.getUuid(), agent.getName(), AgentRunStatus.RUNNING, null, false), false);

        expectNothing();
    }

    @Test
    void aUserWithoutAnAddressGetsNothing() throws Exception {
        webhooks.notify(waits(), false);

        expectNothing();
    }

    @Test
    void theAdministratorCanTurnTheMessagesOff() throws Exception {
        configure(WebhookFormat.PLAIN, false);
        loginAdmin();
        webhooks.setAllowed(false);
        assertThat(webhooks.isAllowed()).isFalse();

        webhooks.notify(waits(), false);

        expectNothing();
        loginAs(user);
        assertThatThrownBy(() -> webhooks.setAllowed(true)).isInstanceOf(SecurityException.class);
    }

    @Test
    void onlyHttpAddressesAreCalled() {
        assertThat(webhooks.check(url())).isNull();
        assertThat(webhooks.check("https://ntfy.sh/topic")).isNull();
        assertThat(webhooks.check("")).isNotNull();
        assertThat(webhooks.check("ftp://example.org/x")).contains("http");
        assertThat(webhooks.check("file:///etc/passwd")).contains("http");
        assertThat(webhooks.check("http:///nohost")).isNotNull();
        assertThat(webhooks.check("not a url")).isNotNull();
        assertThat(webhooks.check("http://example.org/" + "a".repeat(2100))).contains("longer");
    }

    @Test
    void aUserWithoutThePermissionGetsNothingAndCannotTest() throws Exception {
        configure(WebhookFormat.PLAIN, false);
        grantNotifications(false, true);

        webhooks.notify(waits(), false);

        expectNothing();
        assertThat(webhooks.mayNotify(user)).isFalse();
        assertThat(webhooks.sendTest(url(), WebhookFormat.PLAIN)).contains("may not");
        assertThat(received).isEmpty();
    }

    @Test
    void anAddressOnTheMachineNeedsTheSubPermission() throws Exception {
        configure(WebhookFormat.PLAIN, false);
        grantNotifications(true, false);
        assertThat(webhooks.mayNotify(user)).isTrue();
        assertThat(webhooks.mayUseLocalAddresses(user)).isFalse();

        webhooks.notify(waits(), false);

        expectNothing();
        assertThat(webhooks.check(user, url())).contains("Only addresses on the internet");
        assertThat(webhooks.sendTest(url(), WebhookFormat.PLAIN)).contains("Only addresses on the internet");
        assertThat(received).as("no request was made").isEmpty();
    }

    @Test
    void theSubPermissionCountsOnlyWhileThePermissionIsGranted() throws Exception {
        grantNotifications(false, true);

        assertThat(webhooks.mayUseLocalAddresses(user)).isFalse();
    }

    @Test
    void anAdministratorNeedsNeitherPermission() throws Exception {
        loginAdmin();

        assertThat(webhooks.mayNotify(currentUser)).isTrue();
        assertThat(webhooks.mayUseLocalAddresses(currentUser)).isTrue();
        assertThat(webhooks.check(currentUser, url())).isNull();
        assertThat(webhooks.sendTest(url(), WebhookFormat.PLAIN)).isNull();
    }

    @Test
    void localMeansEverythingThatIsNotTheInternet() throws Exception {
        for (String local : new String[]{"127.0.0.1", "127.1.2.3", "10.0.0.5", "172.16.4.1", "192.168.1.20", "169.254.169.254",
                "100.64.0.1", "0.0.0.0", "0.1.2.3", "224.0.0.1", "::1", "fe80::1", "fd12:3456::1", "fc00::1", "::", "::ffff:127.0.0.1",
                "::ffff:10.1.1.1"}) {
            assertThat(AgentWebhookServiceImpl.isLocal(InetAddress.getByName(local))).as(local).isTrue();
        }
        for (String internet : new String[]{"8.8.8.8", "1.1.1.1", "93.184.216.34", "172.32.0.1", "100.128.0.1", "2606:4700:4700::1111"}) {
            assertThat(AgentWebhookServiceImpl.isLocal(InetAddress.getByName(internet))).as(internet).isFalse();
        }
    }

    @Test
    void literalAddressesOfThePublicInternetPassAndLocalOnesDoNot() throws Exception {
        grantNotifications(true, false);

        assertThat(webhooks.check(user, "https://8.8.8.8/topic")).isNull();
        assertThat(webhooks.check(user, "http://localhost:8080/x")).isNotNull();
        assertThat(webhooks.check(user, "http://[::1]/x")).isNotNull();
        assertThat(webhooks.check(user, "http://169.254.169.254/latest/meta-data")).isNotNull();
        assertThat(webhooks.check(user, "http://192.168.0.10/x")).isNotNull();
        assertThat(webhooks.check(user, "ftp://8.8.8.8/x")).contains("http");
    }

    @Test
    void aTestMessageSaysWhetherTheServiceAcceptedIt() throws Exception {
        assertThat(webhooks.sendTest(url(), WebhookFormat.PLAIN)).isNull();
        assertThat(next().body()).contains("test message");

        status.set(500);
        assertThat(webhooks.sendTest(url(), WebhookFormat.PLAIN)).contains("500");
        assertThat(webhooks.sendTest("gopher://x", WebhookFormat.PLAIN)).isNotNull();
    }

    @Test
    void aRedirectIsNotFollowedAndTheAddressIsNotInTheProblem() throws Exception {
        status.set(302);

        String problem = webhooks.sendTest(url(), WebhookFormat.PLAIN);

        assertThat(problem).contains("302").doesNotContain("127.0.0.1");
        assertThat(received).hasSize(1);
    }

    @Test
    void anAddressNobodyListensAtIsAProblemNotAnException() throws Exception {
        String dead = url();
        server.stop(0);

        String problem = webhooks.sendTest(dead, WebhookFormat.PLAIN);

        assertThat(problem).isNotNull().doesNotContain("/hook");
    }

    @Test
    void aRunThatWaitsForTheUserSendsTheMessageWhenNoScreenIsOpen() throws Exception {
        configure(WebhookFormat.PLAIN, false);
        AgentRun run = agentRunService.start(agent);

        agentRunService.transition(run, AgentRunStatus.IDLE, "Which name?");

        assertThat(next().body()).contains("waits for you");
    }

    @Test
    void aRunThatWaitsForTheUserSendsNothingWhileAScreenIsOpen() throws Exception {
        configure(WebhookFormat.PLAIN, false);
        Runnable close = attention.subscribe(user.getId(), notice -> { });
        try {
            AgentRun run = agentRunService.start(agent);

            agentRunService.transition(run, AgentRunStatus.IDLE, "Which name?");

            expectNothing();
        } finally {
            close.run();
        }
    }
}
