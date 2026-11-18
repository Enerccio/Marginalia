package com.github.enerccio.marginalia.api;

import com.github.enerccio.marginalia.domain.security.model.ApiScope;
import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.domain.service.command.CommandParameterDefinition;
import com.github.enerccio.marginalia.domain.service.command.CommandText;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The description of the API for people: an OpenAPI 3.1 document ({@code GET /api/v1/openapi.json}) and the page that shows it with
 * Swagger UI ({@code GET /api/v1/}, its files under {@code /api/v1/docs/}). The page, its files and the document of the fixed calls need
 * no key; with a key the document also has a call for every command the key may run, with the arguments of its forms. The page keeps the
 * key in memory only and sends it with the calls it makes (<i>Try it out</i> runs them for real).
 * <p>
 * {@link #ROUTES} lists the calls of {@link ApiDispatcher}; {@code ApiDocsTest} fails when the dispatcher answers a call that is not
 * listed here or the other way round.
 */
final class ApiDocs {

    /** What a call takes as its body. */
    enum Body {NONE, COMMAND, JAR}

    /** What a call answers with 200. */
    enum Answer {OBJECT, ARRAY, TEXT, RUN}

    /**
     * A call of the API.
     *
     * @param scope   the scope the key needs, null for any key
     * @param changes whether it changes something on the server (the page says so)
     * @param query   the query parameters, name to description
     */
    record Route(String method, String path, ApiScope scope, String tag, String summary, String description, boolean changes, Body body,
                 Answer answer, Map<String, String> query) {
    }

    static final String TAG_GENERAL = "General";
    static final String TAG_COMMANDS = "Books and commands";
    static final String TAG_RUNS = "Runs";
    static final String TAG_UPDATE = "Update of the installation";
    static final String TAG_EXTENSIONS = "Extensions";
    static final String TAG_EACH_COMMAND = "Each command on a book";

    private static final Map<String, String> BYTES = Map.of("bytes", "How many bytes of the end of the log (default 65536, at most 4 MB)");

    static final List<Route> ROUTES = List.of(
            new Route("GET", "/version", null, TAG_GENERAL, "The version and commit the server runs", null, false, Body.NONE, Answer.OBJECT, Map.of()),
            new Route("GET", "/me", null, TAG_GENERAL, "The user and the key", "The scopes are the effective ones.", false, Body.NONE, Answer.OBJECT,
                    Map.of()),
            new Route("GET", "/openapi.json", null, TAG_GENERAL, "This description of the API",
                    "Without a key only the fixed calls; with a key also a call for every command the key may run.", false, Body.NONE,
                    Answer.OBJECT, Map.of()),
            new Route("GET", "/books", ApiScope.READ, TAG_COMMANDS, "The books of the user", null, false, Body.NONE, Answer.ARRAY, Map.of()),
            new Route("GET", "/commands", ApiScope.READ, TAG_COMMANDS, "The commands the key may run",
                    "With their forms (overloads), parameters and what they return.", false, Body.NONE, Answer.ARRAY, Map.of()),
            new Route("POST", "/commands/{command}", ApiScope.READ, TAG_COMMANDS, "Run a command that needs no book",
                    "A key with READ runs only the commands that only read, WRITE every command.", true, Body.COMMAND, Answer.RUN, Map.of()),
            new Route("POST", "/books/{book}/commands/{command}", ApiScope.READ, TAG_COMMANDS, "Run a command on a book",
                    "The book is the cage of the command, as for an agent. With a key, every command also has a call of its own under \""
                            + TAG_EACH_COMMAND + "\".", true, Body.COMMAND, Answer.RUN, Map.of()),
            new Route("GET", "/runs/{run}", ApiScope.READ, TAG_RUNS, "The state of a run", null, false, Body.NONE, Answer.RUN, Map.of()),
            new Route("DELETE", "/runs/{run}", ApiScope.READ, TAG_RUNS, "Stop a run", "409 when it can't be stopped now.", true, Body.NONE,
                    Answer.RUN, Map.of()),
            new Route("GET", "/admin/update", ApiScope.UPDATE, TAG_UPDATE, "The state of the update watcher", null, false, Body.NONE,
                    Answer.OBJECT, Map.of()),
            new Route("POST", "/admin/update", ApiScope.UPDATE, TAG_UPDATE, "Update and restart the installation",
                    "Asks the update watcher to update; the server restarts. 409 without a watcher or while an update runs.", true, Body.NONE,
                    Answer.OBJECT, Map.of()),
            new Route("GET", "/admin/update/log", ApiScope.UPDATE, TAG_UPDATE, "The end of the output of the last update", null, false,
                    Body.NONE, Answer.TEXT, BYTES),
            new Route("GET", "/admin/extensions", ApiScope.EXTENSIONS, TAG_EXTENSIONS, "The installed extensions", null, false, Body.NONE,
                    Answer.ARRAY, Map.of()),
            new Route("GET", "/admin/extensions/{jar}", ApiScope.EXTENSIONS, TAG_EXTENSIONS, "One extension", null, false, Body.NONE,
                    Answer.OBJECT, Map.of()),
            new Route("PUT", "/admin/extensions/{jar}", ApiScope.EXTENSIONS, TAG_EXTENSIONS, "Install or replace an extension",
                    "The body is the JAR. It replaces an extension with the same symbolic name or file; 422 with the report when it does not"
                            + " fit this version of the application (the old one is restored).", true, Body.JAR, Answer.OBJECT, Map.of()),
            new Route("DELETE", "/admin/extensions/{jar}", ApiScope.EXTENSIONS, TAG_EXTENSIONS, "Uninstall an extension",
                    "The JAR is deleted.", true, Body.NONE, Answer.OBJECT, Map.of()),
            new Route("POST", "/admin/extensions/{jar}/stop", ApiScope.EXTENSIONS, TAG_EXTENSIONS, "Stop an extension",
                    "It stays installed and stays stopped after a restart.", true, Body.NONE, Answer.OBJECT, Map.of()),
            new Route("POST", "/admin/extensions/{jar}/start", ApiScope.EXTENSIONS, TAG_EXTENSIONS, "Start a stopped extension", null, true,
                    Body.NONE, Answer.OBJECT, Map.of()),
            new Route("GET", "/admin/extensions/{jar}/log", ApiScope.EXTENSIONS, TAG_EXTENSIONS, "The log of an extension", null, false,
                    Body.NONE, Answer.TEXT, BYTES));

    private static final Map<String, String> PATH_PARAMETERS = Map.of(
            "command", "The name of the command (GET /commands)",
            "book", "The uuid of a book of the user (GET /books)",
            "run", "The id of the run the command call answered with",
            "jar", "The file name of the extension, ending with .jar");

    private static final String WARNING = "**Try it out runs the calls for real** on this server, as the user of the key: commands change "
            + "books, and the administration calls update the installation or install extensions.";

    private ApiDocs() {
    }

    // --- the document ---

    /**
     * The OpenAPI document.
     *
     * @param version  the version of the application
     * @param commands the commands the key may run, the overloads of each together; null without a key
     * @param text     a text of a command in the language of the user, null for none
     */
    static JsonObject spec(String version, List<List<CommandDefinition>> commands, Function<CommandText, String> text) {
        JsonObject spec = new JsonObject();
        spec.addProperty("openapi", "3.1.0");
        JsonObject info = new JsonObject();
        info.addProperty("title", "Marginalia API");
        info.addProperty("version", version + " (API " + ApiDispatcher.API_VERSION + ")");
        info.addProperty("description", "JSON over HTTP with a bearer key made in Settings → API keys. " + WARNING
                + (commands == null ? "\n\nGive a key above to see a call for every command it may run." : ""));
        spec.add("info", info);
        JsonObject server = new JsonObject();
        server.addProperty("url", ApiDispatcher.PREFIX);
        spec.add("servers", array(server));
        JsonObject bearer = new JsonObject();
        bearer.add("bearer", new JsonArray());
        spec.add("security", array(bearer));
        spec.add("tags", tags(commands != null));

        JsonObject paths = new JsonObject();
        for (Route route : ROUTES) {
            JsonObject item = paths.has(route.path()) ? paths.getAsJsonObject(route.path()) : new JsonObject();
            item.add(route.method().toLowerCase(), operation(route));
            paths.add(route.path(), item);
        }
        if (commands != null) {
            for (List<CommandDefinition> overloads : commands) {
                JsonObject item = new JsonObject();
                item.add("post", commandOperation(overloads, text));
                paths.add("/books/{book}/commands/" + overloads.getFirst().commandName(), item);
            }
        }
        spec.add("paths", paths);
        spec.add("components", components());
        return spec;
    }

    private static JsonArray tags(boolean withCommands) {
        JsonArray tags = new JsonArray();
        for (String name : List.of(TAG_GENERAL, TAG_COMMANDS, TAG_RUNS, TAG_UPDATE, TAG_EXTENSIONS)) {
            JsonObject tag = new JsonObject();
            tag.addProperty("name", name);
            tags.add(tag);
        }
        if (withCommands) {
            JsonObject tag = new JsonObject();
            tag.addProperty("name", TAG_EACH_COMMAND);
            tag.addProperty("description", "The commands this key may run, each with the arguments of its forms.");
            tags.add(tag);
        }
        return tags;
    }

    private static JsonObject operation(Route route) {
        JsonObject op = new JsonObject();
        op.add("tags", array(new JsonPrimitive(route.tag())));
        op.addProperty("summary", route.summary());
        op.addProperty("operationId", route.method().toLowerCase() + route.path().replaceAll("[^A-Za-z0-9]+", "-").replaceAll("-$", ""));
        List<String> notes = new ArrayList<>();
        notes.add("Scope: " + (route.scope() == null ? "any key" : route.scope().name() + (route.scope() == ApiScope.READ
                && route.body() == Body.COMMAND ? " (WRITE for a command that changes data)" : "")) + ".");
        if (route.description() != null) {
            notes.add(route.description());
        }
        if (route.body() == Body.COMMAND) {
            notes.add("**May change data**, depending on the command.");
        } else if (route.changes()) {
            notes.add("**Changes the server.**");
        }
        op.addProperty("description", String.join("\n\n", notes));
        JsonArray parameters = new JsonArray();
        Matcher m = Pattern.compile("\\{(\\w+)}").matcher(route.path());
        while (m.find()) {
            parameters.add(parameter(m.group(1), "path", PATH_PARAMETERS.get(m.group(1)), true, stringSchema()));
        }
        route.query().forEach((name, description) -> {
            JsonObject schema = new JsonObject();
            schema.addProperty("type", "integer");
            parameters.add(parameter(name, "query", description, false, schema));
        });
        if (!parameters.isEmpty()) {
            op.add("parameters", parameters);
        }
        switch (route.body()) {
            case COMMAND -> op.add("requestBody", body("application/json", ref("CommandCall"), false));
            case JAR -> {
                JsonObject binary = new JsonObject();
                binary.addProperty("type", "string");
                binary.addProperty("contentMediaType", "application/java-archive");
                op.add("requestBody", body("application/java-archive", binary, true));
            }
            case NONE -> {
            }
        }
        op.add("responses", responses(route.answer(), null));
        return op;
    }

    private static JsonObject commandOperation(List<CommandDefinition> overloads, Function<CommandText, String> text) {
        CommandDefinition first = overloads.getFirst();
        boolean readOnly = overloads.stream().allMatch(CommandDefinition::readOnly);
        boolean destructive = overloads.stream().anyMatch(d -> d.dataSeverity().isDestructive());
        JsonObject op = new JsonObject();
        op.add("tags", array(new JsonPrimitive(TAG_EACH_COMMAND)));
        String title = text.apply(first.humanName());
        op.addProperty("summary", title == null || title.equals(first.commandName()) ? first.commandName() : first.commandName() + " — " + title);
        op.addProperty("operationId", "command-" + first.commandName());
        List<String> notes = new ArrayList<>();
        String description = text.apply(first.llmDescription()) != null ? text.apply(first.llmDescription()) : text.apply(first.description());
        if (description != null) {
            notes.add(description);
        }
        notes.add("Scope: " + (readOnly ? "READ (it only reads)." : "WRITE.") + (destructive ? " **Changes or removes data that exists.**" : "")
                + (first.async() ? " Runs in the background: wait for it or follow its run." : ""));
        notes.add("Run without a book: POST /commands/" + first.commandName() + " with the same body.");
        op.addProperty("description", String.join("\n\n", notes));
        op.add("parameters", array(parameter("book", "path", PATH_PARAMETERS.get("book"), true, stringSchema())));

        JsonArray forms = new JsonArray();
        for (CommandDefinition form : overloads) {
            forms.add(arguments(form, text));
        }
        JsonObject call = new JsonObject();
        call.addProperty("type", "object");
        JsonObject properties = new JsonObject();
        if (forms.size() == 1) {
            properties.add("arguments", forms.get(0));
        } else {
            JsonObject oneOf = new JsonObject();
            oneOf.add("oneOf", forms);
            oneOf.addProperty("description", "One of the forms of the command.");
            properties.add("arguments", oneOf);
        }
        properties.add("confirm", ref("CommandCall", "confirm"));
        properties.add("wait", ref("CommandCall", "wait"));
        call.add("properties", properties);
        op.add("requestBody", body("application/json", call, false));
        String returns = overloads.stream().filter(d -> d.returns() != null && !d.returns().isVoid())
                .map(d -> d.returns().name() + " (" + d.returns().type().getSimpleName() + ")" + (text.apply(d.returns().description()) == null ? ""
                        : ": " + text.apply(d.returns().description()))).distinct().reduce((a, b) -> a + "; " + b).orElse(null);
        op.add("responses", responses(Answer.RUN, returns));
        return op;
    }

    /** The arguments of one form of a command, by name. */
    private static JsonObject arguments(CommandDefinition form, Function<CommandText, String> text) {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        if (form.anyArguments()) {
            schema.addProperty("description", "Any arguments, by name.");
            schema.addProperty("additionalProperties", true);
            return schema;
        }
        JsonObject properties = new JsonObject();
        JsonArray required = new JsonArray();
        for (CommandParameterDefinition parameter : form.parameters()) {
            JsonObject property = typeSchema(parameter.type());
            String description = text.apply(parameter.description());
            if (description != null) {
                property.addProperty("description", description);
            }
            if (!parameter.allowed().isEmpty()) {
                JsonArray allowed = new JsonArray();
                parameter.allowed().forEach(allowed::add);
                property.add("enum", allowed);
            }
            number(parameter.min()).ifPresent(min -> property.addProperty("minimum", min));
            number(parameter.max()).ifPresent(max -> property.addProperty("maximum", max));
            String defaultValue = StringUtils.trimToNull(parameter.defaultValue());
            if (defaultValue != null) {
                property.add("default", defaultOf(property, defaultValue));
            }
            properties.add(parameter.name(), property);
            if (!parameter.optional()) {
                required.add(parameter.name());
            }
        }
        schema.add("properties", properties);
        if (!required.isEmpty()) {
            schema.add("required", required);
        }
        schema.addProperty("additionalProperties", false);
        return schema;
    }

    /** The JSON type of a parameter of a command (they get text, numbers and booleans; anything else as its text). */
    static JsonObject typeSchema(Class<?> type) {
        JsonObject schema = new JsonObject();
        if (type == Object.class) {
            return schema;
        }
        String json;
        if (Set.of(Integer.class, int.class, Long.class, long.class, Short.class, short.class).contains(type)) {
            json = "integer";
        } else if (Set.of(Double.class, double.class, Float.class, float.class, Number.class, BigDecimal.class).contains(type)) {
            json = "number";
        } else if (type == Boolean.class || type == boolean.class) {
            json = "boolean";
        } else {
            json = "string";
            if (type.isEnum()) {
                JsonArray values = new JsonArray();
                for (Object constant : type.getEnumConstants()) {
                    values.add(((Enum<?>) constant).name());
                }
                schema.add("enum", values);
            }
        }
        schema.addProperty("type", json);
        return schema;
    }

    private static java.util.Optional<BigDecimal> number(String text) {
        try {
            return StringUtils.isBlank(text) ? java.util.Optional.empty() : java.util.Optional.of(new BigDecimal(text.trim()));
        } catch (NumberFormatException e) {
            return java.util.Optional.empty();
        }
    }

    private static JsonElement defaultOf(JsonObject schema, String value) {
        String type = schema.has("type") ? schema.get("type").getAsString() : "";
        return switch (type) {
            case "integer", "number" -> number(value).<JsonElement>map(JsonPrimitive::new).orElse(new JsonPrimitive(value));
            case "boolean" -> value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")
                    ? new JsonPrimitive(Boolean.parseBoolean(value)) : new JsonPrimitive(value);
            default -> new JsonPrimitive(value);
        };
    }

    private static JsonObject responses(Answer answer, String returns) {
        JsonObject responses = new JsonObject();
        JsonObject ok = new JsonObject();
        switch (answer) {
            case OBJECT -> {
                ok.addProperty("description", "OK");
                JsonObject schema = new JsonObject();
                schema.addProperty("type", "object");
                ok.add("content", content("application/json", schema));
            }
            case ARRAY -> {
                ok.addProperty("description", "OK");
                JsonObject schema = new JsonObject();
                schema.addProperty("type", "array");
                schema.add("items", new JsonObject());
                ok.add("content", content("application/json", schema));
            }
            case TEXT -> {
                ok.addProperty("description", "OK, plain text");
                ok.add("content", content("text/plain", stringSchema()));
            }
            case RUN -> {
                ok.addProperty("description", "Done: status \"done\" with the result" + (returns == null ? "" : ", " + returns));
                ok.add("content", content("application/json", ref("Run")));
                JsonObject running = new JsonObject();
                running.addProperty("description", "Still running: status \"running\" and the id of the run to ask for");
                running.add("content", content("application/json", ref("Run")));
                responses.add("202", running);
            }
        }
        responses.add("200", ok);
        for (String status : List.of("400", "401", "403", "404", "409", "422")) {
            JsonObject error = new JsonObject();
            error.addProperty("$ref", "#/components/responses/Error");
            responses.add(status, error);
        }
        return responses;
    }

    private static JsonObject components() {
        JsonObject components = new JsonObject();
        JsonObject schemes = new JsonObject();
        JsonObject bearer = new JsonObject();
        bearer.addProperty("type", "http");
        bearer.addProperty("scheme", "bearer");
        bearer.addProperty("description", "A key made in Settings → API keys (mrg_...).");
        schemes.add("bearer", bearer);
        components.add("securitySchemes", schemes);

        JsonObject schemas = new JsonObject();
        schemas.add("Error", object(Map.of("error", stringSchema())));

        JsonObject arguments = new JsonObject();
        arguments.addProperty("description", "By name (an object) or by position (an array). A number, a text, a boolean or null is passed as it"
                + " is, an object or an array as its JSON text.");
        JsonObject byName = new JsonObject();
        byName.addProperty("type", "object");
        JsonObject byPosition = new JsonObject();
        byPosition.addProperty("type", "array");
        arguments.add("oneOf", array(byName, byPosition));
        JsonObject confirm = new JsonObject();
        confirm.addProperty("type", "boolean");
        confirm.addProperty("default", false);
        confirm.addProperty("description", "The answer to every question the command asks (nobody can be asked).");
        JsonObject wait = new JsonObject();
        wait.addProperty("type", "integer");
        wait.addProperty("minimum", 0);
        wait.addProperty("maximum", ApiDispatcher.MAX_WAIT_SECONDS);
        wait.addProperty("default", ApiDispatcher.DEFAULT_WAIT_SECONDS);
        wait.addProperty("description", "How many seconds the call waits for the command before it answers with a run.");
        Map<String, JsonObject> call = new LinkedHashMap<>();
        call.put("arguments", arguments);
        call.put("confirm", confirm);
        call.put("wait", wait);
        schemas.add("CommandCall", object(call));

        JsonObject status = stringSchema();
        status.add("enum", array(new JsonPrimitive("running"), new JsonPrimitive("done"), new JsonPrimitive("failed"),
                new JsonPrimitive("cancelled")));
        Map<String, JsonObject> run = new LinkedHashMap<>();
        run.put("run", stringSchema());
        run.put("command", stringSchema());
        run.put("status", status);
        run.put("progress", stringSchema());
        run.put("detail", stringSchema());
        run.put("result", new JsonObject());
        run.put("error", stringSchema());
        schemas.add("Run", object(run));
        components.add("schemas", schemas);

        JsonObject responses = new JsonObject();
        JsonObject error = new JsonObject();
        error.addProperty("description", "An error: {\"error\": \"...\"}");
        error.add("content", content("application/json", ref("Error")));
        responses.add("Error", error);
        components.add("responses", responses);
        return components;
    }

    // --- the page and its files ---

    /** The files of Swagger UI the page uses (the others of the webjar are not served). */
    private static final Set<String> SWAGGER_FILES = Set.of("swagger-ui.css", "swagger-ui-bundle.js", "favicon-32x32.png", "favicon-16x16.png");
    /** The files of the page itself, in {@code api-docs/} of the classpath. */
    private static final Set<String> OWN_FILES = Set.of("api-docs.js", "api-docs.css");

    private static volatile String swaggerVersion;

    /**
     * The page or one of its files for a {@code GET}, null when the call is not one of them.
     *
     * @param rawPath  the path of the call, with its trailing slash
     * @param segments the segments after the prefix
     */
    static ApiResponse page(String rawPath, List<String> segments) {
        if (segments.isEmpty()) {
            if (!rawPath.endsWith("/")) {
                // the files are found next to the page
                return new ApiResponse(302, "text/plain; charset=utf-8", new byte[0], Map.of("Location", "v1/"));
            }
            return file("api-docs/index.html", "text/html; charset=utf-8", false)
                    .withHeader("Content-Security-Policy", "frame-ancestors 'none'; object-src 'none'; base-uri 'self'");
        }
        if (segments.size() != 2 || !"docs".equals(segments.getFirst())) {
            return null;
        }
        String name = segments.get(1);
        if (OWN_FILES.contains(name)) {
            return file("api-docs/" + name, typeOf(name), false);
        }
        if (SWAGGER_FILES.contains(name)) {
            String version = swaggerVersion();
            return version == null ? ApiResponse.error(404, "Swagger UI is not installed")
                    : file("META-INF/resources/webjars/swagger-ui/" + version + "/" + name, typeOf(name), true);
        }
        return ApiResponse.error(404, "Not found");
    }

    private static ApiResponse file(String resource, String type, boolean cache) {
        try (InputStream in = ApiDocs.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                return ApiResponse.error(404, "Not found");
            }
            ApiResponse response = new ApiResponse(200, type, in.readAllBytes(), Map.of("X-Content-Type-Options", "nosniff"));
            // Swagger UI changes only with the application; the page and its script are small and follow the application at once
            return cache ? response.withHeader("Cache-Control", "public, max-age=86400") : response;
        } catch (IOException e) {
            return ApiResponse.error(500, "Internal error");
        }
    }

    private static String typeOf(String name) {
        if (name.endsWith(".js")) {
            return "text/javascript; charset=utf-8";
        }
        if (name.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        return name.endsWith(".png") ? "image/png" : "application/octet-stream";
    }

    /** The version of the Swagger UI webjar on the classpath, null without it. */
    static String swaggerVersion() {
        String version = swaggerVersion;
        if (version == null) {
            try (InputStream in = ApiDocs.class.getClassLoader().getResourceAsStream("META-INF/maven/org.webjars/swagger-ui/pom.properties")) {
                if (in != null) {
                    Properties properties = new Properties();
                    properties.load(in);
                    version = properties.getProperty("version");
                    swaggerVersion = version;
                }
            } catch (IOException e) {
                return null;
            }
        }
        return version;
    }

    // --- helpers ---

    private static JsonObject parameter(String name, String in, String description, boolean required, JsonObject schema) {
        JsonObject parameter = new JsonObject();
        parameter.addProperty("name", name);
        parameter.addProperty("in", in);
        if (description != null) {
            parameter.addProperty("description", description);
        }
        parameter.addProperty("required", required);
        parameter.add("schema", schema);
        return parameter;
    }

    private static JsonObject body(String type, JsonObject schema, boolean required) {
        JsonObject body = new JsonObject();
        body.addProperty("required", required);
        body.add("content", content(type, schema));
        return body;
    }

    private static JsonObject content(String type, JsonObject schema) {
        JsonObject media = new JsonObject();
        media.add("schema", schema);
        JsonObject content = new JsonObject();
        content.add(type, media);
        return content;
    }

    private static JsonObject object(Map<String, JsonObject> properties) {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        JsonObject props = new JsonObject();
        properties.forEach(props::add);
        schema.add("properties", props);
        return schema;
    }

    private static JsonObject stringSchema() {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "string");
        return schema;
    }

    private static JsonObject ref(String schema) {
        JsonObject ref = new JsonObject();
        ref.addProperty("$ref", "#/components/schemas/" + schema);
        return ref;
    }

    private static JsonObject ref(String schema, String property) {
        JsonObject ref = new JsonObject();
        ref.addProperty("$ref", "#/components/schemas/" + schema + "/properties/" + property);
        return ref;
    }

    private static JsonArray array(JsonElement... elements) {
        JsonArray array = new JsonArray();
        for (JsonElement element : elements) {
            array.add(element);
        }
        return array;
    }
}
