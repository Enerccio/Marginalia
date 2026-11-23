package com.github.enerccio.marginalia.api;

import com.github.enerccio.marginalia.bound.SessionPoint;
import com.github.enerccio.marginalia.bound.ApiRequestAttributes;
import com.github.enerccio.marginalia.domain.model.impl.settings.BaseUserSetting;
import com.github.enerccio.marginalia.domain.security.model.ApiScope;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.service.ApiKeyService;
import com.github.enerccio.marginalia.domain.security.service.ApiKeyService.Principal;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.command.*;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.github.enerccio.marginalia.utils.BuildInfo;
import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * The HTTP API ({@value #PREFIX}): authenticates the bearer key ({@link ApiKeyService}), makes the request context of its user
 * ({@link ApiRequestAttributes}, no container session) and answers the call. {@code ApiServlet} only adapts the servlet request to an
 * {@link ApiRequest}, so the API is tested without HTTP.
 * <p>
 * The commands are the ones of the command bar ({@link CommandService}, those of the extensions included) and run in the scope of the
 * target the call names (the application's books, through {@link ApiTargets}). A key with {@link ApiScope#READ} runs only the commands that only read, one with
 * {@link ApiScope#WRITE} every command. A
 * command that has not ended within {@code wait} seconds goes on as a run, asked for by {@code GET /runs/<id>}. The administration
 * needs a scope for each part ({@link ApiScope#UPDATE} the update of the installation, {@link ApiScope#EXTENSIONS} the extensions; a key
 * has any combination of scopes), and the services check again that the user
 * is an administrator. The page of the API and its OpenAPI description are {@link ApiDocs}. See {@code manual/developer/api.md} for the calls.
 */
public class ApiDispatcher {
    private static final Logger log = LoggerFactory.getLogger(ApiDispatcher.class);

    public static final String PREFIX = "/api/v1";
    public static final int API_VERSION = 1;

    static final int DEFAULT_WAIT_SECONDS = 30;
    static final int MAX_WAIT_SECONDS = 300;
    static final int MAX_RUNNING_PER_USER = 16;
    static final long KEEP_FINISHED_RUN_MS = 60 * 60 * 1000L;
    static final int DEFAULT_LOG_BYTES = 64 * 1024;
    static final int MAX_LOG_BYTES = 4 * 1024 * 1024;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    @Lazy
    private CommandService commandService;

    /** What the commands are aimed at (the application's books). */
    @Autowired
    private ObjectProvider<ApiTargets> targets;

    @Autowired
    @Lazy
    private SettingService settingService;


    @Autowired
    @Lazy
    private UpdateService updateService;

    @Autowired
    @Lazy
    private Localization loc;

    /** Not in the tests (no OSGi): the extension calls then answer 503. */
    @Autowired
    private ObjectProvider<OsgiService> osgiService;

    private final Map<String, ApiRun> runs = new ConcurrentHashMap<>();

    /**
     * A command that went on after its call answered.
     */
    record ApiRun(String id, Long userId, String command, CommandRun run, long started, ApiRequestAttributes attributes) {
    }

    /**
     * Where a call is going: the segments of the path after {@value #PREFIX}.
     */
    private record Call(ApiRequest request, List<String> path, Principal principal, ApiRequestAttributes attributes) {
        String segment(int i) {
            return i < path.size() ? path.get(i) : null;
        }

        boolean is(String method, String... segments) {
            if (!request.method().equalsIgnoreCase(method) || path.size() != segments.length) {
                return false;
            }
            for (int i = 0; i < segments.length; i++) {
                if (!segments[i].equals("*") && !segments[i].equals(path.get(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    public ApiResponse handle(ApiRequest request) {
        List<String> path = split(request.path());
        if (path == null) {
            return ApiResponse.error(404, "Not found");
        }
        if ("GET".equalsIgnoreCase(request.method())) {
            // the page of the API and the description of its fixed calls need no key (ApiDocs)
            ApiResponse page = ApiDocs.page(request.path(), path);
            if (page != null) {
                return page;
            }
            if (path.equals(List.of("openapi.json")) && request.authorization() == null) {
                return ApiResponse.json(200, ApiDocs.spec(BuildInfo.current().getVersion(), null, this::text));
            }
        }
        Principal principal;
        // nobody yet: a session bean asked for meanwhile must not open a session of the container
        try (ApiRequestAttributes anonymous = new ApiRequestAttributes(new User(), targets().newSessionPoint());
             InRequestScope _ = new InRequestScope(anonymous)) {
            principal = apiKeyService.authenticate(bearer(request.authorization()));
        } catch (Exception e) {
            log.error("API: the key could not be checked", e);
            return ApiResponse.error(500, "Internal error");
        }
        if (principal == null) {
            return ApiResponse.error(401, "A valid API key is needed: Authorization: Bearer <key>")
                    .withHeader("WWW-Authenticate", "Bearer realm=\"marginalia\"");
        }

        ApiRequestAttributes attributes = new ApiRequestAttributes(principal.user(), targets().newSessionPoint());
        boolean keep = false;
        try (InRequestScope _ = new InRequestScope(attributes)) {
            Call call = new Call(request, path, principal, attributes);
            try {
                setLanguage(attributes.getSessionPoint());
                ApiResponse response = route(call);
                keep = response.status() == 202 && runs.values().stream().anyMatch(r -> r.attributes() == attributes);
                return response;
            } catch (ApiException e) {
                return ApiResponse.error(e.getStatus(), e.getMessage());
            } catch (SecurityException e) {
                return ApiResponse.error(403, StringUtils.defaultIfBlank(e.getMessage(), "Forbidden"));
            } catch (IllegalArgumentException e) {
                return ApiResponse.error(400, StringUtils.defaultIfBlank(e.getMessage(), "Bad request"));
            } catch (IllegalStateException e) {
                return ApiResponse.error(409, StringUtils.defaultIfBlank(e.getMessage(), "Conflict"));
            } catch (Exception e) {
                log.error("API: {} {} failed", request.method(), request.path(), e);
                return ApiResponse.error(500, "Internal error");
            }
        } finally {
            if (!keep) {
                attributes.close();
            }
        }
    }

    private ApiResponse route(Call call) throws Exception {
        if (call.is("GET", "version")) {
            return ApiResponse.json(200, version());
        }
        if (call.is("GET", "me")) {
            return ApiResponse.json(200, me(call.principal()));
        }
        if (call.is("GET", "openapi.json")) {
            return ApiResponse.json(200, ApiDocs.spec(BuildInfo.current().getVersion(), runnable(call.principal()), this::text));
        }
        if ("admin".equals(call.segment(0))) {
            return routeAdmin(call);
        }
        require(call, ApiScope.READ);
        if (call.is("GET", "books")) {
            return ApiResponse.json(200, books());
        }
        if (call.is("GET", "commands")) {
            return ApiResponse.json(200, commands(call.principal()));
        }
        if (call.is("POST", "commands", "*")) {
            return runCommand(call, null, call.segment(1));
        }
        if (call.is("POST", "books", "*", "commands", "*")) {
            return runCommand(call, call.segment(1), call.segment(3));
        }
        if (call.is("GET", "runs", "*")) {
            return runStatus(call, findRun(call, call.segment(1)));
        }
        if (call.is("DELETE", "runs", "*")) {
            return cancelRun(call, findRun(call, call.segment(1)));
        }
        throw new ApiException(404, "Not found");
    }

    private ApiResponse routeAdmin(Call call) throws Exception {
        if ("update".equals(call.segment(1))) {
            require(call, ApiScope.UPDATE);
        } else if ("extensions".equals(call.segment(1))) {
            require(call, ApiScope.EXTENSIONS);
        } else {
            throw new ApiException(404, "Not found");
        }
        if (call.is("GET", "admin", "update")) {
            return ApiResponse.json(200, updateStatus(updateService.getStatus()));
        }
        if (call.is("POST", "admin", "update")) {
            updateService.requestUpdate();
            log.info("API: update of the installation requested by {}", call.principal().user().getLogin());
            JsonObject json = new JsonObject();
            json.addProperty("status", "requested");
            return ApiResponse.json(202, json);
        }
        if (call.is("GET", "admin", "update", "log")) {
            return ApiResponse.text(200, updateService.readLog(logBytes(call.request())));
        }
        if (call.is("GET", "admin", "extensions")) {
            OsgiService osgi = osgi();
            JsonArray list = new JsonArray();
            for (Bundle bundle : osgi.getBundles()) {
                list.add(extension(osgi, bundle));
            }
            return ApiResponse.json(200, list);
        }
        if (call.is("PUT", "admin", "extensions", "*")) {
            return installExtension(call, call.segment(2));
        }
        if (call.is("GET", "admin", "extensions", "*")) {
            OsgiService osgi = osgi();
            return ApiResponse.json(200, extension(osgi, bundle(osgi, call.segment(2))));
        }
        if (call.is("DELETE", "admin", "extensions", "*")) {
            OsgiService osgi = osgi();
            Bundle bundle = bundle(osgi, call.segment(2));
            osgi.uninstallPackage(bundle);
            log.info("API: extension {} uninstalled by {}", call.segment(2), call.principal().user().getLogin());
            JsonObject json = new JsonObject();
            json.addProperty("status", "uninstalled");
            return ApiResponse.json(200, json);
        }
        if (call.is("POST", "admin", "extensions", "*", "stop")) {
            OsgiService osgi = osgi();
            Bundle bundle = bundle(osgi, call.segment(2));
            osgi.stopExtension(bundle);
            return ApiResponse.json(200, extension(osgi, bundle));
        }
        if (call.is("POST", "admin", "extensions", "*", "start")) {
            OsgiService osgi = osgi();
            Bundle bundle = bundle(osgi, call.segment(2));
            osgi.startExtension(bundle);
            return ApiResponse.json(200, extension(osgi, bundle));
        }
        if (call.is("GET", "admin", "extensions", "*", "log")) {
            OsgiService osgi = osgi();
            return ApiResponse.text(200, osgi.readLog(bundle(osgi, call.segment(2)), logBytes(call.request())));
        }
        throw new ApiException(404, "Not found");
    }

    // --- general ---

    static JsonObject version() {
        BuildInfo info = BuildInfo.current();
        JsonObject json = new JsonObject();
        json.addProperty("application", info.getName());
        json.addProperty("version", info.getVersion());
        json.addProperty("commit", info.getCommit());
        json.addProperty("commitFull", info.getCommitFull());
        json.addProperty("commitTime", info.getCommitTime());
        json.addProperty("buildTime", info.getBuildTime());
        json.addProperty("api", API_VERSION);
        return json;
    }

    private static JsonObject me(Principal principal) {
        JsonObject json = new JsonObject();
        json.addProperty("login", principal.user().getLogin());
        json.addProperty("fullName", principal.user().getFullName());
        json.addProperty("admin", principal.user().isAdmin());
        JsonObject key = new JsonObject();
        key.addProperty("uuid", principal.key().getUuid());
        key.addProperty("name", principal.key().getName());
        JsonArray scopes = new JsonArray();
        principal.scopes().stream().sorted().forEach(scope -> scopes.add(scope.name()));
        key.add("scopes", scopes);
        key.addProperty("expires", iso(principal.key().getExpires()));
        json.add("key", key);
        return json;
    }

    private JsonArray books() throws Exception {
        return targets().list();
    }

    private ApiTargets targets() {
        return targets.getIfAvailable(() -> ApiTargets.NONE);
    }

    // --- commands ---

    /** The commands the key may run, by name, the overloads of each together; empty for a key without READ or WRITE. */
    private List<List<CommandDefinition>> runnable(Principal principal) {
        Map<String, List<CommandDefinition>> byName = new LinkedHashMap<>();
        for (CommandDefinition definition : commandService.getCommands()) {
            byName.computeIfAbsent(definition.commandName(), _ -> new ArrayList<>()).add(definition);
        }
        return byName.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue)
                .filter(overloads -> mayRun(principal, overloads)).toList();
    }

    private JsonArray commands(Principal principal) {
        JsonArray list = new JsonArray();
        runnable(principal).forEach(overloads -> {
            CommandDefinition first = overloads.getFirst();
            JsonObject json = new JsonObject();
            json.addProperty("name", first.commandName());
            json.addProperty("title", text(first.humanName()));
            json.addProperty("description", text(first.llmDescription()) != null ? text(first.llmDescription()) : text(first.description()));
            json.addProperty("async", first.async());
            json.addProperty("readOnly", overloads.stream().allMatch(CommandDefinition::readOnly));
            json.addProperty("destructive", overloads.stream().anyMatch(d -> d.dataSeverity().isDestructive()));
            JsonArray forms = new JsonArray();
            for (CommandDefinition definition : overloads) {
                JsonObject form = new JsonObject();
                form.addProperty("anyArguments", definition.anyArguments());
                JsonArray parameters = new JsonArray();
                for (CommandParameterDefinition parameter : definition.parameters()) {
                    JsonObject p = new JsonObject();
                    p.addProperty("name", parameter.name());
                    p.addProperty("type", parameter.typeName());
                    p.addProperty("description", text(parameter.description()));
                    p.addProperty("optional", parameter.optional());
                    p.addProperty("default", StringUtils.trimToNull(parameter.defaultValue()));
                    if (!parameter.allowed().isEmpty()) {
                        JsonArray allowed = new JsonArray();
                        parameter.allowed().forEach(allowed::add);
                        p.add("allowed", allowed);
                    }
                    p.addProperty("min", StringUtils.trimToNull(parameter.min()));
                    p.addProperty("max", StringUtils.trimToNull(parameter.max()));
                    parameters.add(p);
                }
                form.add("parameters", parameters);
                if (definition.returns() != null && !definition.returns().isVoid()) {
                    JsonObject returns = new JsonObject();
                    returns.addProperty("name", definition.returns().name());
                    returns.addProperty("type", definition.returns().type().getSimpleName());
                    returns.addProperty("description", text(definition.returns().description()));
                    form.add("returns", returns);
                }
                forms.add(form);
            }
            json.add("forms", forms);
            list.add(json);
        });
        return list;
    }

    /**
     * A {@link ApiScope#WRITE} key runs every command, a {@link ApiScope#READ} key only one whose every form only reads.
     */
    private static boolean mayRun(Principal principal, List<CommandDefinition> overloads) {
        return principal.has(ApiScope.WRITE) || principal.has(ApiScope.READ) && overloads.stream().allMatch(CommandDefinition::readOnly);
    }

    private ApiResponse runCommand(Call call, String bookUuid, String commandName) throws Exception {
        List<CommandDefinition> overloads = commandService.getCommands(commandName);
        if (overloads.isEmpty()) {
            throw new ApiException(404, "No such command: " + commandName);
        }
        if (!mayRun(call.principal(), overloads)) {
            throw new ApiException(403, "The key does not have the scope " + (call.principal().has(ApiScope.READ) ? "WRITE" : "READ"));
        }
        JsonObject body = jsonBody(call.request());
        boolean confirm = body.has("confirm") && !body.get("confirm").isJsonNull() && body.get("confirm").getAsBoolean();
        int wait = DEFAULT_WAIT_SECONDS;
        if (body.has("wait") && !body.get("wait").isJsonNull()) {
            wait = Math.clamp(body.get("wait").getAsInt(), 0, MAX_WAIT_SECONDS);
        }

        BaseCommandScope scope = targets().scope(bookUuid, call.attributes().getSessionPoint(), confirm);
        Long userId = call.principal().user().getId();
        purgeRuns();
        if (runs.values().stream().filter(r -> r.userId().equals(userId) && !r.run().isDone()).count() >= MAX_RUNNING_PER_USER) {
            throw new ApiException(429, "Too many commands are running, at most " + MAX_RUNNING_PER_USER);
        }

        JsonElement arguments = body.get("arguments");
        CommandRun run;
        if (arguments != null && arguments.isJsonArray()) {
            List<Object> positional = new ArrayList<>();
            for (JsonElement element : arguments.getAsJsonArray()) {
                positional.add(value(element));
            }
            run = commandService.executePositional(commandName, positional, false, scope);
        } else if (arguments == null || arguments.isJsonNull() || arguments.isJsonObject()) {
            Map<String, Object> named = new LinkedHashMap<>();
            if (arguments != null && arguments.isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : arguments.getAsJsonObject().entrySet()) {
                    named.put(entry.getKey(), value(entry.getValue()));
                }
            }
            run = commandService.execute(commandName, named, false, scope);
        } else {
            throw new ApiException(400, "The arguments are an object (by name) or an array (by position)");
        }

        try {
            run.get(wait, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            ApiRun apiRun = new ApiRun(UUID.randomUUID().toString(), userId, commandName, run, System.currentTimeMillis(), call.attributes());
            runs.put(apiRun.id(), apiRun);
            run.whenComplete((_, _) -> apiRun.attributes().close());
            return runStatus(call, apiRun);
        } catch (ExecutionException | CancellationException e) {
            // answered below from the state of the run
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            run.cancel(true);
            throw new ApiException(503, "The call was interrupted");
        }
        return runStatus(call, new ApiRun(null, userId, commandName, run, System.currentTimeMillis(), call.attributes()));
    }

    private ApiResponse runStatus(Call call, ApiRun apiRun) {
        CommandRun run = apiRun.run();
        JsonObject json = new JsonObject();
        if (apiRun.id() != null) {
            json.addProperty("run", apiRun.id());
        }
        json.addProperty("command", apiRun.command());
        if (!run.isDone()) {
            json.addProperty("status", "running");
            json.addProperty("progress", run.getStatus());
            json.addProperty("detail", run.getSubStatus());
            return ApiResponse.json(202, json);
        }
        if (run.isCancelled()) {
            json.addProperty("status", "cancelled");
            return ApiResponse.json(409, json);
        }
        try {
            Object result = run.getNow(null);
            json.addProperty("status", "done");
            json.add("result", result == null ? null : result instanceof String s ? new JsonPrimitive(s) : ApiResponse.GSON.toJsonTree(result));
            return ApiResponse.json(200, json);
        } catch (CancellationException e) {
            json.addProperty("status", "cancelled");
            return ApiResponse.json(409, json);
        } catch (RuntimeException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            json.addProperty("status", "failed");
            json.addProperty("error", StringUtils.defaultIfBlank(cause.getMessage(), cause.getClass().getSimpleName()));
            int status = cause instanceof IllegalArgumentException ? 400
                    : cause instanceof SecurityException ? 403
                    : cause instanceof CommandException ? 422 : 500;
            if (status == 500) {
                log.error("API: command {} failed", apiRun.command(), cause);
            }
            return ApiResponse.json(status, json);
        }
    }

    private ApiResponse cancelRun(Call call, ApiRun apiRun) {
        if (!apiRun.run().isDone() && !apiRun.run().cancel(true)) {
            throw new ApiException(409, "The command can't be stopped now");
        }
        return runStatus(call, apiRun);
    }

    private ApiRun findRun(Call call, String id) {
        purgeRuns();
        ApiRun run = id == null ? null : runs.get(id);
        if (run == null || !run.userId().equals(call.principal().user().getId())) {
            throw new ApiException(404, "No such run: " + id);
        }
        return run;
    }

    private void purgeRuns() {
        long now = System.currentTimeMillis();
        runs.values().removeIf(r -> r.run().isDone() && now - r.started() > KEEP_FINISHED_RUN_MS);
    }

    /**
     * A JSON value as a command argument: text, a number, a boolean, null; an object or an array as its JSON text (commands take
     * text).
     */
    static Object value(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            }
            if (primitive.isNumber()) {
                return primitive.getAsNumber();
            }
            return primitive.getAsString();
        }
        return element.toString();
    }

    // --- administration ---

    private OsgiService osgi() {
        OsgiService osgi = osgiService.getIfAvailable();
        if (osgi == null) {
            throw new ApiException(503, "Extensions are not available");
        }
        return osgi;
    }

    private static Bundle bundle(OsgiService osgi, String jarName) {
        Bundle bundle = jarName == null ? null : osgi.findBundleByJar(jarName);
        if (bundle == null || bundle.getBundleId() == 0) {
            throw new ApiException(404, "No such extension: " + jarName);
        }
        return bundle;
    }

    private ApiResponse installExtension(Call call, String jarName) throws Exception {
        OsgiService osgi = osgi();
        if (jarName == null || !jarName.toLowerCase().endsWith(".jar")) {
            throw new ApiException(400, "The name of the extension is its JAR file, it ends with .jar");
        }
        byte[] data = call.request().body();
        if (data.length == 0) {
            throw new ApiException(400, "The body is the JAR of the extension");
        }
        try {
            Bundle bundle = osgi.installPackage(jarName, data);
            log.info("API: extension {} installed by {}", jarName, call.principal().user().getLogin());
            return ApiResponse.json(200, extension(osgi, bundle));
        } catch (ExtensionVerificationException e) {
            log.warn("API: extension {} was rejected:\n{}", e.getExtension(), e.getReport());
            JsonObject json = new JsonObject();
            json.addProperty("error", "The extension does not fit this version of the application");
            json.addProperty("extension", e.getExtension());
            json.addProperty("report", e.getReport());
            return ApiResponse.json(422, json);
        }
    }

    private static JsonObject extension(OsgiService osgi, Bundle bundle) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("jar", jarName(bundle));
        json.addProperty("symbolicName", bundle.getSymbolicName());
        json.addProperty("name", bundle.getHeaders().get("Bundle-Name"));
        json.addProperty("version", bundle.getVersion() == null ? null : bundle.getVersion().toString());
        json.addProperty("state", state(bundle.getState()));
        json.addProperty("stopped", osgi.isStopped(bundle));
        json.addProperty("loaded", osgi.getLoadedExtensions().stream().anyMatch(e -> e.bundle().getBundleId() == bundle.getBundleId()));
        OsgiService.ExtensionReport report = osgi.getVerificationReport(bundle);
        json.addProperty("valid", report == null ? null : report.valid());
        try {
            json.addProperty("sha256", osgi.getSha256(bundle));
        } catch (Exception e) {
            json.addProperty("sha256", (String) null);
        }
        return json;
    }

    private static String jarName(Bundle bundle) {
        String location = StringUtils.defaultString(bundle.getLocation()).replace("file:", "").replace('\\', '/');
        return new File(location).getName();
    }

    private static String state(int state) {
        return switch (state) {
            case Bundle.UNINSTALLED -> "UNINSTALLED";
            case Bundle.INSTALLED -> "INSTALLED";
            case Bundle.RESOLVED -> "RESOLVED";
            case Bundle.STARTING -> "STARTING";
            case Bundle.STOPPING -> "STOPPING";
            case Bundle.ACTIVE -> "ACTIVE";
            default -> String.valueOf(state);
        };
    }

    private static JsonObject updateStatus(UpdateService.UpdateStatus status) {
        JsonObject json = new JsonObject();
        json.addProperty("watching", status.watching());
        json.addProperty("running", status.running());
        json.addProperty("requested", status.requested());
        json.addProperty("updateAvailable", status.updateAvailable());
        json.addProperty("canRequest", status.canRequest());
        json.addProperty("runningVersion", status.runningVersion());
        json.addProperty("runningCommit", status.runningCommit());
        json.addProperty("branch", status.branch());
        json.addProperty("upstream", status.upstream());
        json.addProperty("remoteCommit", status.remoteCommit());
        json.addProperty("fetchFailed", status.fetchFailed());
        json.addProperty("checked", iso(status.checked()));
        json.addProperty("lastRun", iso(status.lastRun()));
        json.addProperty("lastRunOk", status.lastRunOk());
        return json;
    }

    // --- helpers ---

    private static void require(Call call, ApiScope scope) {
        if (!call.principal().has(scope)) {
            throw new ApiException(403, "The key does not have the scope " + scope.name());
        }
    }

    private void setLanguage(SessionPoint sessionPoint) throws Exception {
        BaseUserSetting setting = settingService.getUserSetting();
        sessionPoint.setLanguage(loc.resolveLanguage(setting.getLanguage()));
        sessionPoint.setPromptLanguage(loc.resolvePromptLanguage(setting.getPromptLanguage()));
    }

    private String text(CommandText text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        return StringUtils.trimToNull(text.get(loc));
    }

    private static JsonObject jsonBody(ApiRequest request) {
        if (request.body().length == 0) {
            return new JsonObject();
        }
        try {
            JsonElement element = JsonParser.parseString(new String(request.body(), StandardCharsets.UTF_8));
            if (element.isJsonNull()) {
                return new JsonObject();
            }
            if (!element.isJsonObject()) {
                throw new ApiException(400, "The body is a JSON object");
            }
            return element.getAsJsonObject();
        } catch (JsonParseException | IllegalStateException | NumberFormatException e) {
            throw new ApiException(400, "The body is not valid JSON");
        }
    }

    private static int logBytes(ApiRequest request) {
        String bytes = request.query("bytes");
        if (StringUtils.isBlank(bytes)) {
            return DEFAULT_LOG_BYTES;
        }
        try {
            return Math.clamp(Integer.parseInt(bytes.trim()), 1, MAX_LOG_BYTES);
        } catch (NumberFormatException e) {
            throw new ApiException(400, "bytes is a number");
        }
    }

    /**
     * The key of an {@code Authorization: Bearer <key>} header, null for anything else.
     */
    static String bearer(String authorization) {
        if (authorization == null) {
            return null;
        }
        String value = authorization.trim();
        if (value.length() < 7 || !value.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        return StringUtils.trimToNull(value.substring(7));
    }

    /**
     * The segments of the path after {@value #PREFIX}, null when the path is not in the API.
     */
    static List<String> split(String path) {
        if (path == null || !(path.equals(PREFIX) || path.startsWith(PREFIX + "/"))) {
            return null;
        }
        List<String> segments = new ArrayList<>();
        for (String segment : path.substring(PREFIX.length()).split("/")) {
            if (!segment.isEmpty()) {
                // a plus is a plus in a path, only the query writes a space so
                segments.add(URLDecoder.decode(segment.replace("+", "%2B"), StandardCharsets.UTF_8));
            }
        }
        return segments;
    }

    private static String iso(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(date);
    }

    /**
     * The runs that are kept, for tests.
     */
    int runCount() {
        return runs.size();
    }
}
