package com.github.enerccio.marginalia.api;

import com.github.enerccio.marginalia.domain.security.model.ApiScope;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.security.service.ApiKeyService;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The page of the API and its OpenAPI description: what needs no key, what a key adds, and that the description lists exactly the calls
 * the dispatcher answers.
 */
class ApiDocsTest extends MarginaliaTestBase {

    @Autowired
    private ApiDispatcher dispatcher;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private CommandService commandService;

    private final ApiDispatcherTest.TestCommands suite = new ApiDispatcherTest.TestCommands();

    @BeforeEach
    void setUp() {
        commandService.registerCommands(suite);
    }

    @AfterEach
    void tearDown() {
        commandService.unregisterCommands(suite);
    }

    private String key(ApiScope... scopes) throws Exception {
        // the administration scopes are only for an administrator
        User user = Set.of(scopes).stream().anyMatch(ApiScope::isAdministration) ? createUser(uniqueName("admin"), "password", true) : createUser();
        setPermission(user, PermissionType.API, true);
        loginAs(user);
        String token = apiKeyService.create(uniqueName("key"), Set.of(scopes), null).token();
        loginAs(new User());
        return token;
    }

    private ApiResponse get(String token, String path) {
        return dispatcher.handle(new ApiRequest("GET", path, Map.of(), token == null ? null : "Bearer " + token, null));
    }

    private JsonObject paths(ApiResponse spec) {
        assertThat(spec.status()).isEqualTo(200);
        return JsonParser.parseString(spec.bodyText()).getAsJsonObject().getAsJsonObject("paths");
    }

    @Test
    void thePageAndItsFilesNeedNoKey() {
        ApiResponse page = get(null, "/api/v1/");
        assertThat(page.status()).isEqualTo(200);
        assertThat(page.contentType()).startsWith("text/html");
        assertThat(page.bodyText()).contains("docs/swagger-ui-bundle.js", "docs/api-docs.js");

        ApiResponse withoutSlash = get(null, "/api/v1");
        assertThat(withoutSlash.status()).isEqualTo(302);
        assertThat(withoutSlash.headers()).containsEntry("Location", "v1/");

        assertThat(ApiDocs.swaggerVersion()).as("the webjar is on the classpath").isNotNull();
        ApiResponse bundle = get(null, "/api/v1/docs/swagger-ui-bundle.js");
        assertThat(bundle.status()).isEqualTo(200);
        assertThat(bundle.contentType()).startsWith("text/javascript");
        assertThat(bundle.body().length).isGreaterThan(100_000);
        assertThat(get(null, "/api/v1/docs/api-docs.js").status()).isEqualTo(200);
        assertThat(get(null, "/api/v1/docs/swagger-ui.css").status()).isEqualTo(200);
    }

    @Test
    void onlyTheFilesOfThePageAreServed() {
        assertThat(get(null, "/api/v1/docs/index.html").status()).isEqualTo(404);
        assertThat(get(null, "/api/v1/docs/swagger-initializer.js").status()).as("a file of the webjar the page does not use").isEqualTo(404);
        assertThat(get(null, "/api/v1/docs/%2e%2e%2fpom.xml").status()).isEqualTo(404);
        assertThat(get(null, "/api/v1/docs/a/b").status()).as("anything else needs a key").isEqualTo(401);
    }

    @Test
    void withoutAKeyTheDescriptionHasOnlyTheFixedCalls() {
        JsonObject paths = paths(get(null, "/api/v1/openapi.json"));
        assertThat(paths.keySet()).contains("/version", "/books/{book}/commands/{command}", "/admin/extensions/{jar}");
        assertThat(paths.keySet()).noneMatch(p -> p.startsWith("/books/{book}/commands/api-test"));
    }

    @Test
    void aKeyAddsTheCommandsItMayRun() throws Exception {
        JsonObject read = paths(get(key(ApiScope.READ), "/api/v1/openapi.json"));
        assertThat(read.keySet()).contains("/books/{book}/commands/api-test-where").doesNotContain("/books/{book}/commands/api-test-ask");

        JsonObject write = paths(get(key(ApiScope.READ, ApiScope.WRITE), "/api/v1/openapi.json"));
        JsonObject ask = write.getAsJsonObject("/books/{book}/commands/api-test-ask").getAsJsonObject("post");
        JsonObject arguments = ask.getAsJsonObject("requestBody").getAsJsonObject("content").getAsJsonObject("application/json")
                .getAsJsonObject("schema").getAsJsonObject("properties").getAsJsonObject("arguments");
        assertThat(arguments.getAsJsonObject("properties").getAsJsonObject("times").get("type").getAsString()).isEqualTo("integer");
        assertThat(arguments.getAsJsonArray("required").asList()).extracting(e -> e.getAsString()).containsExactly("times");
        assertThat(ask.get("description").getAsString()).contains("WRITE", "Changes or removes data");

        JsonObject update = paths(get(key(ApiScope.UPDATE), "/api/v1/openapi.json"));
        assertThat(update.keySet()).as("no commands without READ or WRITE").noneMatch(p -> p.startsWith("/books/{book}/commands/api-test"));
    }

    @Test
    void aWrongKeyIsRefusedAlsoForTheDescription() {
        assertThat(get("mrg_aaaaaaaaaaaa_nope", "/api/v1/openapi.json").status()).isEqualTo(401);
    }

    @Test
    void theDescriptionListsExactlyTheCallsOfTheDispatcher() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/github/enerccio/marginalia/api/ApiDispatcher.java"));
        Matcher m = Pattern.compile("call\\.is\\(\"(\\w+)\"((?:,\\s*\"[^\"]*\")*)\\)").matcher(source);
        Set<String> answered = new TreeSet<>();
        while (m.find()) {
            String segments = Pattern.compile("\"([^\"]*)\"").matcher(m.group(2)).results().map(r -> r.group(1))
                    .collect(Collectors.joining("/"));
            answered.add(m.group(1) + " /" + segments);
        }
        Set<String> documented = ApiDocs.ROUTES.stream().map(r -> r.method() + " " + r.path().replaceAll("\\{\\w+}", "*"))
                .collect(Collectors.toCollection(TreeSet::new));
        assertThat(answered).as("the calls found in ApiDispatcher").hasSizeGreaterThan(10);
        assertThat(documented).isEqualTo(answered);
    }
}
