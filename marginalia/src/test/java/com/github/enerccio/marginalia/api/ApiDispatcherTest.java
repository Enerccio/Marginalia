package com.github.enerccio.marginalia.api;

import com.github.enerccio.marginalia.domain.service.command.CommandScope;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.security.model.ApiScope;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.security.service.ApiKeyService;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.Command;
import com.github.enerccio.marginalia.domain.traits.CommandDataSeverity;
import com.github.enerccio.marginalia.domain.traits.CommandParameter;
import com.github.enerccio.marginalia.domain.traits.CommandParameterSignature;
import com.github.enerccio.marginalia.domain.traits.CommandReturn;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.utils.BuildInfo;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The HTTP API without HTTP: the key, the access of the key, the commands in the scope of a book, the runs and the administration.
 */
class ApiDispatcherTest extends MarginaliaTestBase {

    @Autowired
    private ApiDispatcher dispatcher;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private CommandService commandService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private FolderService folderService;

    private final TestCommands suite = new TestCommands();

    /** Commands of the test. */
    public static class TestCommands {
        volatile CountDownLatch release = new CountDownLatch(0);

        @Command(commandName = "api-test-where", humanNameText = "Where", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                readOnly = true, signature = @CommandParameterSignature,
                returns = @CommandReturn(name = "book", type = String.class, descriptionText = "x"))
        public void where(CommandContext context) throws Exception {
            Manuscript book = CommandScope.of(context).getManuscript();
            context.setReturn(book == null ? "none" : book.getName());
        }

        @Command(commandName = "api-test-ask", humanNameText = "Ask", dataSeverity = CommandDataSeverity.DESTRUCTIVE,
                signature = @CommandParameterSignature({
                        @CommandParameter(name = "times", type = Integer.class, descriptionText = "x")}),
                returns = @CommandReturn(name = "answer", type = String.class, descriptionText = "x"))
        public void ask(CommandContext context) throws Exception {
            Integer times = context.get("times", Integer.class);
            context.setReturn(times + ":" + context.confirm("Really?"));
        }

        @Command(commandName = "api-test-slow", humanNameText = "Slow", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                readOnly = true, async = true, signature = @CommandParameterSignature,
                returns = @CommandReturn(name = "done", type = String.class, descriptionText = "x"))
        public void slow(CommandContext context) throws Exception {
            release.await(30, TimeUnit.SECONDS);
            context.setReturn("slept");
        }

        @Command(commandName = "api-test-fail", humanNameText = "Fail", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                readOnly = true, signature = @CommandParameterSignature,
                returns = @CommandReturn(name = "x", type = String.class, descriptionText = "x"))
        public void fail(CommandContext context) {
            throw new IllegalStateException("broken on purpose");
        }
    }

    @BeforeEach
    void setUp() {
        commandService.registerCommands(suite);
    }

    @AfterEach
    void tearDown() {
        suite.release.countDown();
        commandService.unregisterCommands(suite);
    }

    private String key(User user, ApiScope... scopes) throws Exception {
        loginAs(user);
        String token = apiKeyService.create(uniqueName("key"), Set.of(scopes), null).token();
        // the API does not need the session of the test, it makes its own
        loginAs(new User());
        return token;
    }

    private User apiUser() throws Exception {
        User user = createUser();
        setPermission(user, PermissionType.API, true);
        return user;
    }

    private ApiResponse call(String token, String method, String path, String body) {
        return dispatcher.handle(new ApiRequest(method, ApiDispatcher.PREFIX + path, Map.of(),
                token == null ? null : "Bearer " + token, body == null ? null : body.getBytes(StandardCharsets.UTF_8)));
    }

    private static JsonObject json(ApiResponse response) {
        return JsonParser.parseString(response.bodyText()).getAsJsonObject();
    }

    @Test
    void withoutAValidKeyNothingIsAnswered() throws Exception {
        assertThat(call(null, "GET", "/version", null).status()).isEqualTo(401);
        ApiResponse wrong = call("mrg_aaaaaaaaaaaa_nope", "GET", "/version", null);
        assertThat(wrong.status()).isEqualTo(401);
        assertThat(wrong.headers()).containsKey("WWW-Authenticate");
        assertThat(dispatcher.handle(new ApiRequest("GET", "/api/v1/version", Map.of(), "Basic abc", null)).status()).isEqualTo(401);
        assertThat(dispatcher.handle(new ApiRequest("GET", "/other", Map.of(), null, null)).status()).isEqualTo(404);
    }

    @Test
    void theVersionTellsTheCommitTheApplicationWasBuiltFrom() throws Exception {
        String token = key(apiUser(), ApiScope.READ);
        ApiResponse response = call(token, "GET", "/version", null);

        assertThat(response.status()).isEqualTo(200);
        JsonObject json = json(response);
        assertThat(json.get("version").getAsString()).isEqualTo(BuildInfo.current().getVersion());
        assertThat(json.get("api").getAsInt()).isEqualTo(ApiDispatcher.API_VERSION);
        assertThat(json.has("commitFull")).isTrue();
    }

    @Test
    void meIsTheUserOfTheKey() throws Exception {
        User user = apiUser();
        String token = key(user, ApiScope.WRITE);
        JsonObject json = json(call(token, "GET", "/me", null));

        assertThat(json.get("login").getAsString()).isEqualTo(user.getLogin());
        assertThat(json.getAsJsonObject("key").getAsJsonArray("scopes").asList()).extracting(e -> e.getAsString())
                .containsExactly("READ", "WRITE");
        assertThat(RequestContextHolder.getRequestAttributes()).as("the scope of the test is back")
                .isNotInstanceOf(com.github.enerccio.marginalia.bound.ApiRequestAttributes.class);
    }

    @Test
    void theBooksAreTheBooksOfTheUser() throws Exception {
        User user = apiUser();
        loginAs(user);
        Folder folder = new Folder();
        folder.setName(uniqueName("folder"));
        folder = folderService.save(folder);
        Manuscript mine = new Manuscript();
        mine.setName(uniqueName("mine"));
        mine.setFolder(folder);
        mine = manuscriptService.save(mine);
        User other = apiUser();
        loginAs(other);
        Manuscript theirs = new Manuscript();
        theirs.setName(uniqueName("theirs"));
        theirs = manuscriptService.save(theirs);

        String token = key(user, ApiScope.READ);
        JsonArray books = JsonParser.parseString(call(token, "GET", "/books", null).bodyText()).getAsJsonArray();
        String mineUuid = mine.getUuid();
        String theirsUuid = theirs.getUuid();
        String folderUuid = folder.getUuid();
        assertThat(books.asList()).anyMatch(b -> b.getAsJsonObject().get("uuid").getAsString().equals(mineUuid)
                && b.getAsJsonObject().get("folder").getAsString().equals(folderUuid));
        assertThat(books.asList()).noneMatch(b -> b.getAsJsonObject().get("uuid").getAsString().equals(theirsUuid));

        // a command can't be aimed at the book of another user
        assertThat(call(token, "POST", "/books/" + theirsUuid + "/commands/api-test-where", "{}").status()).isEqualTo(404);
        JsonObject result = json(call(token, "POST", "/books/" + mineUuid + "/commands/api-test-where", "{}"));
        assertThat(result.get("status").getAsString()).isEqualTo("done");
        assertThat(result.get("result").getAsString()).isEqualTo(mine.getName());
        assertThat(json(call(token, "POST", "/commands/api-test-where", null)).get("result").getAsString()).isEqualTo("none");
    }

    @Test
    void aReadKeySeesAndRunsOnlyTheCommandsThatRead() throws Exception {
        String token = key(apiUser(), ApiScope.READ);
        JsonArray commands = JsonParser.parseString(call(token, "GET", "/commands", null).bodyText()).getAsJsonArray();
        assertThat(commands.asList()).anyMatch(c -> c.getAsJsonObject().get("name").getAsString().equals("api-test-where"));
        assertThat(commands.asList()).noneMatch(c -> c.getAsJsonObject().get("name").getAsString().equals("api-test-ask"));
        assertThat(call(token, "POST", "/commands/api-test-ask", "{\"arguments\":{\"times\":2}}").status()).isEqualTo(403);
    }

    @Test
    void aWriteKeyRunsEveryCommandAndAnswersTheQuestionsWithConfirm() throws Exception {
        String token = key(apiUser(), ApiScope.WRITE);
        JsonArray commands = JsonParser.parseString(call(token, "GET", "/commands", null).bodyText()).getAsJsonArray();
        JsonObject ask = commands.asList().stream().map(c -> c.getAsJsonObject())
                .filter(c -> c.get("name").getAsString().equals("api-test-ask")).findFirst().orElseThrow();
        assertThat(ask.get("destructive").getAsBoolean()).isTrue();
        assertThat(ask.getAsJsonArray("forms").get(0).getAsJsonObject().getAsJsonArray("parameters").get(0).getAsJsonObject()
                .get("type").getAsString()).isEqualTo("Integer");

        assertThat(json(call(token, "POST", "/commands/api-test-ask", "{\"arguments\":{\"times\":2}}")).get("result").getAsString())
                .isEqualTo("2:false");
        assertThat(json(call(token, "POST", "/commands/api-test-ask", "{\"arguments\":{\"times\":3},\"confirm\":true}"))
                .get("result").getAsString()).isEqualTo("3:true");
        assertThat(json(call(token, "POST", "/commands/api-test-ask", "{\"arguments\":[4],\"confirm\":true}"))
                .get("result").getAsString()).as("by position").isEqualTo("4:true");
    }

    @Test
    void badCallsAreTold() throws Exception {
        String token = key(apiUser(), ApiScope.WRITE);
        assertThat(call(token, "POST", "/commands/no-such-command", "{}").status()).isEqualTo(404);
        assertThat(call(token, "POST", "/commands/api-test-ask", "not json").status()).isEqualTo(400);
        assertThat(call(token, "POST", "/commands/api-test-ask", "{\"arguments\":{\"wrong\":1}}").status()).isEqualTo(400);
        ApiResponse failed = call(token, "POST", "/commands/api-test-fail", "{}");
        assertThat(failed.status()).isIn(422, 500);
        assertThat(json(failed).get("status").getAsString()).isEqualTo("failed");
        assertThat(call(token, "GET", "/nothing-here", null).status()).isEqualTo(404);
    }

    @Test
    void aLongCommandGoesOnAsARunOfItsUser() throws Exception {
        String token = key(apiUser(), ApiScope.READ);
        suite.release = new CountDownLatch(1);

        ApiResponse started = call(token, "POST", "/commands/api-test-slow", "{\"wait\":0}");
        assertThat(started.status()).isEqualTo(202);
        String run = json(started).get("run").getAsString();
        assertThat(json(call(token, "GET", "/runs/" + run, null)).get("status").getAsString()).isEqualTo("running");

        String other = key(apiUser(), ApiScope.READ);
        assertThat(call(other, "GET", "/runs/" + run, null).status()).as("not the run of another user").isEqualTo(404);

        suite.release.countDown();
        JsonObject done = null;
        for (int i = 0; i < 100; i++) {
            done = json(call(token, "GET", "/runs/" + run, null));
            if (!done.get("status").getAsString().equals("running")) {
                break;
            }
            Thread.sleep(50);
        }
        assertThat(done.get("status").getAsString()).isEqualTo("done");
        assertThat(done.get("result").getAsString()).isEqualTo("slept");
    }

    @Test
    void aRunCanBeCancelled() throws Exception {
        String token = key(apiUser(), ApiScope.READ);
        suite.release = new CountDownLatch(1);

        String run = json(call(token, "POST", "/commands/api-test-slow", "{\"wait\":0}")).get("run").getAsString();
        ApiResponse cancelled = call(token, "DELETE", "/runs/" + run, null);
        assertThat(json(cancelled).get("status").getAsString()).isEqualTo("cancelled");
    }

    @Test
    void eachPartOfTheAdministrationNeedsItsOwnScope() throws Exception {
        String write = key(apiUser(), ApiScope.WRITE);
        assertThat(call(write, "GET", "/admin/update", null).status()).isEqualTo(403);
        assertThat(call(write, "GET", "/admin/extensions", null).status()).isEqualTo(403);

        User admin = createUser(uniqueName("admin"), DEFAULT_PASSWORD, true);
        String update = key(admin, ApiScope.UPDATE);
        ApiResponse status = call(update, "GET", "/admin/update", null);
        assertThat(status.status()).isEqualTo(200);
        assertThat(json(status).has("watching")).isTrue();
        // no watcher on the host of the test
        assertThat(call(update, "POST", "/admin/update", null).status()).isEqualTo(409);
        assertThat(call(update, "GET", "/admin/extensions", null).status()).as("not the extensions").isEqualTo(403);
        assertThat(call(update, "GET", "/books", null).status()).as("no commands either").isEqualTo(403);
        assertThat(call(update, "POST", "/commands/api-test-where", "{}").status()).isEqualTo(403);
        assertThat(call(update, "GET", "/version", null).status()).as("every key reads the version").isEqualTo(200);

        String extensions = key(admin, ApiScope.EXTENSIONS);
        assertThat(call(extensions, "GET", "/admin/update", null).status()).isEqualTo(403);
        // the tests run without OSGi: past the scope, no extensions here
        assertThat(call(extensions, "GET", "/admin/extensions", null).status()).isEqualTo(503);
        assertThat(call(extensions, "PUT", "/admin/extensions/x.jar", "jar").status()).isEqualTo(503);
        assertThat(call(extensions, "GET", "/admin/other", null).status()).isEqualTo(404);
    }

    @Test
    void scopesCombine() throws Exception {
        User admin = createUser(uniqueName("admin"), DEFAULT_PASSWORD, true);
        String deploy = key(admin, ApiScope.READ, ApiScope.UPDATE, ApiScope.EXTENSIONS);
        assertThat(call(deploy, "GET", "/admin/update", null).status()).isEqualTo(200);
        assertThat(call(deploy, "GET", "/admin/extensions", null).status()).isEqualTo(503);
        assertThat(call(deploy, "POST", "/commands/api-test-where", "{}").status()).isEqualTo(200);
        assertThat(call(deploy, "POST", "/commands/api-test-ask", "{\"arguments\":{\"times\":1}}").status())
                .as("read, not write").isEqualTo(403);
    }

    @Test
    void theAdministrationScopesOfADemotedAdministratorAreGone() throws Exception {
        User admin = createUser(uniqueName("admin"), DEFAULT_PASSWORD, true);
        String token = key(admin, ApiScope.WRITE, ApiScope.UPDATE);
        setPermission(admin, PermissionType.API, true);
        loginAs(admin);
        User stored = userService.find(admin.getId());
        stored.setAdmin(false);
        userService.save(stored);

        assertThat(call(token, "GET", "/admin/update", null).status()).isEqualTo(403);
        assertThat(json(call(token, "GET", "/me", null)).getAsJsonObject("key").getAsJsonArray("scopes").asList())
                .extracting(e -> e.getAsString()).containsExactly("READ", "WRITE");
    }

    @Test
    void pathsAndHeadersAreParsed() {
        assertThat(ApiDispatcher.split("/api/v1/admin/extensions/a%20b.jar")).containsExactly("admin", "extensions", "a b.jar");
        assertThat(ApiDispatcher.split("/api/v1/admin/extensions/a+b.jar")).containsExactly("admin", "extensions", "a+b.jar");
        assertThat(ApiDispatcher.split("/api/v10")).isNull();
        assertThat(ApiDispatcher.bearer("bearer  abc ")).isEqualTo("abc");
        assertThat(ApiDispatcher.bearer("Basic abc")).isNull();
    }
}
