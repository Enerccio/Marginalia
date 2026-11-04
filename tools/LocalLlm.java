import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Sends a prompt to the OpenAI-compatible AI that is set up in the Marginalia database of the dev machine and prints the answer.
 * For trying prompts on the real (local) model, as the Knowledge Tracker's were. Single file, JDK only:
 *
 * <pre>
 *   java tools/LocalLlm.java --list
 *   java tools/LocalLlm.java --models                      # what the server offers
 *   java tools/LocalLlm.java --extra '"chat_template_kwargs":{"enable_thinking":false}' ...   # raw JSON members added to the request
 *   java tools/LocalLlm.java --model ID ...                # another model of the same server, the database stays as it is
 *   java tools/LocalLlm.java --system-file sys.txt --user-file scene.txt --samples 3 > answers.txt
 *   echo "Say ok" | java tools/LocalLlm.java --reasoning
 * </pre>
 *
 * The AI (name, base URI, model id, API key) is read from {@code <home>/marginalia.sqlite} with the {@code sqlite3} command line
 * program (read only, so the application may be running), and the API key is decrypted in memory with {@code <home>/secret.key}
 * the way {@code Configuration.decrypt} does (AES/GCM, key = SHA-256 of the file). The key is never printed or written.
 * <p>
 * The model is a <b>reasoning</b> model: what it thinks counts against {@code max_tokens}. The default is 16000 here (the
 * {@code maxCompletionTokens} of the AI in the database is the application's own limit, 8000, do not copy it); a long answer wants
 * more. An empty answer with {@code finish_reason=length} means the budget was used up by the thinking, raise it.
 * <p>
 * The server has one slot: run one request at a time (the samples are sequential). A request takes 30 to 90 seconds.
 */
public class LocalLlm {

    private static final char SEP = '\u001f';

    private static final class Ai {
        final String name, uri, model, key;

        Ai(String name, String uri, String model, String key) {
            this.name = name;
            this.uri = uri;
            this.model = model;
            this.key = key;
        }

        @Override
        public String toString() {
            return name + " -> " + uri + " (" + model + ")"; // never the key
        }
    }

    public static void main(String[] args) throws Exception {
        Path home = Path.of(System.getProperty("user.home"), ".marginalia");
        String aiName = null, extra = null, modelOverride = null, system = null, user = null, schemaFile = null;
        int maxTokens = 16000, samples = 1, timeoutMinutes = 20;
        Double temperature = null;
        boolean list = false, listModels = false, showReasoning = false, raw = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--list" -> list = true;
                case "--models" -> listModels = true;
                case "--extra" -> extra = args[++i];
                case "--model" -> modelOverride = args[++i];
                case "--ai" -> aiName = args[++i];
                case "--home" -> home = Path.of(args[++i]);
                case "--max-tokens" -> maxTokens = Integer.parseInt(args[++i]);
                case "--temperature" -> temperature = Double.parseDouble(args[++i]);
                case "--system" -> system = args[++i];
                case "--system-file" -> system = Files.readString(Path.of(args[++i]));
                case "--user" -> user = args[++i];
                case "--user-file" -> user = Files.readString(Path.of(args[++i]));
                case "--json-schema" -> schemaFile = args[++i];
                case "--samples" -> samples = Integer.parseInt(args[++i]);
                case "--timeout-minutes" -> timeoutMinutes = Integer.parseInt(args[++i]);
                case "--reasoning" -> showReasoning = true;
                case "--raw" -> raw = true;
                default -> die("Unknown option " + args[i] + "\nOptions: --list --models --model ID --ai NAME --home DIR --max-tokens N --temperature T "
                        + "--system TEXT|--system-file F --user TEXT|--user-file F (default stdin) --json-schema F --extra JSON-MEMBERS --samples N "
                        + "--timeout-minutes N --reasoning (thinking to stderr) --raw (whole JSON answer)");
            }
        }

        List<Ai> ais = loadAis(home);
        if (list) {
            ais.forEach(a -> System.out.println(a));
            return;
        }
        Ai ai = pick(ais, aiName);
        if (listModels) {
            String base = ai.uri.replaceAll("/+$", "");
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create((base.endsWith("/v1") ? base : base + "/v1") + "/models"))
                    .timeout(Duration.ofSeconds(30)).GET();
            if (!ai.key.isEmpty()) {
                request.header("Authorization", "Bearer " + ai.key);
            }
            System.out.println(HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body());
            return;
        }
        if (modelOverride != null) {
            ai = new Ai(ai.name, ai.uri, modelOverride, ai.key);
        }

        if (user == null) {
            user = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
        }
        if (user.isBlank()) {
            die("No user prompt: --user, --user-file or stdin");
        }
        String schema = schemaFile == null ? null : Files.readString(Path.of(schemaFile));

        String body = requestBody(ai.model, system, user, maxTokens, temperature, schema, extra);
        String endpoint = endpoint(ai.uri);
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();

        for (int n = 1; n <= samples; n++) {
            if (samples > 1) {
                System.out.println("=== sample " + n + "/" + samples + " ===");
            }
            long started = System.nanoTime();
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(Duration.ofMinutes(timeoutMinutes))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
            if (!ai.key.isEmpty()) {
                request.header("Authorization", "Bearer " + ai.key);
            }
            HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            double seconds = (System.nanoTime() - started) / 1e9;
            if (response.statusCode() / 100 != 2) {
                die("HTTP " + response.statusCode() + " from " + endpoint + ": " + response.body());
            }
            if (raw) {
                System.out.println(response.body());
                continue;
            }
            report(response.body(), ai, seconds, maxTokens, showReasoning);
        }
    }

    // ---- the AI from the database

    private static List<Ai> loadAis(Path home) throws Exception {
        Path db = home.resolve("marginalia.sqlite");
        Path keyFile = home.resolve("secret.key");
        if (!Files.isRegularFile(db)) {
            die("No database at " + db + " (use --home)");
        }
        String sql = "select a.name, o.uri, o.model, coalesce(o.apiKey, '') from ais a join ais_openaicompat o on o.id = a.id "
                + "where a.is_deleted = 0 order by a.id";
        Process process = new ProcessBuilder("sqlite3", "-readonly", "-separator", String.valueOf(SEP), db.toString(), sql)
                .redirectErrorStream(false).start();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        process.getInputStream().transferTo(out);
        String err = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!process.waitFor(30, TimeUnit.SECONDS) || process.exitValue() != 0) {
            die("sqlite3 failed (is the sqlite3 program installed?): " + err);
        }
        SecretKeySpec secret = null;
        List<Ai> ais = new ArrayList<>();
        for (String line : out.toString(StandardCharsets.UTF_8).split("\n")) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(String.valueOf(SEP), -1);
            if (f.length < 4) {
                continue;
            }
            String key = f[3];
            if (key.startsWith("enc:")) {
                if (secret == null) {
                    secret = secretKey(keyFile);
                }
                key = decrypt(key, secret);
            }
            ais.add(new Ai(f[0], f[1], f[2], key == null ? "" : key));
        }
        if (ais.isEmpty()) {
            die("No OpenAI-compatible AI in " + db);
        }
        return ais;
    }

    private static Ai pick(List<Ai> ais, String name) {
        if (name == null) {
            return ais.get(0);
        }
        for (Ai ai : ais) {
            if (ai.name.equalsIgnoreCase(name)) {
                return ai;
            }
        }
        for (Ai ai : ais) {
            if (ai.name.toLowerCase().contains(name.toLowerCase())) {
                return ai;
            }
        }
        die("No AI named " + name + "; --list shows them");
        return null;
    }

    private static SecretKeySpec secretKey(Path keyFile) throws Exception {
        String key = Files.readString(keyFile, StandardCharsets.UTF_8).trim();
        return new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8)), "AES");
    }

    /**
     * {@code enc:<base64 of iv (12 bytes) + ciphertext + tag>}, as Configuration.encrypt writes it.
     */
    private static String decrypt(String value, SecretKeySpec secret) {
        try {
            byte[] data = Base64.getDecoder().decode(value.substring("enc:".length()));
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secret, new GCMParameterSpec(128, data, 0, 12));
            return new String(cipher.doFinal(data, 12, data.length - 12), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Cannot decrypt the API key (a secret.key of another installation?): " + e.getClass().getSimpleName());
            return null;
        }
    }

    // ---- the request

    /**
     * The base URI is kept in the AI as the application uses it (ends with /v1).
     */
    private static String endpoint(String uri) {
        String base = uri.replaceAll("/+$", "");
        return base.endsWith("/v1") ? base + "/chat/completions" : base + "/v1/chat/completions";
    }

    private static String requestBody(String model, String system, String user, int maxTokens, Double temperature, String schema,
                                      String extra) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"model\":").append(quote(model));
        sb.append(",\"stream\":false");
        sb.append(",\"max_tokens\":").append(maxTokens);
        if (temperature != null) {
            sb.append(",\"temperature\":").append(temperature);
        }
        sb.append(",\"messages\":[");
        if (system != null && !system.isBlank()) {
            sb.append("{\"role\":\"system\",\"content\":").append(quote(system)).append("},");
        }
        sb.append("{\"role\":\"user\",\"content\":").append(quote(user)).append("}]");
        if (schema != null) {
            sb.append(",\"response_format\":{\"type\":\"json_schema\",\"json_schema\":{\"name\":\"answer\",\"strict\":true,\"schema\":")
                    .append(schema.trim()).append("}}");
        }
        if (extra != null && !extra.isBlank()) {
            sb.append(",").append(extra.trim()); // raw JSON members, e.g. "chat_template_kwargs":{"enable_thinking":false}
        }
        return sb.append("}").toString();
    }

    private static String quote(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }

    // ---- the answer

    @SuppressWarnings("unchecked")
    private static void report(String json, Ai ai, double seconds, int maxTokens, boolean showReasoning) {
        Map<String, Object> root = (Map<String, Object>) new Json(json).value();
        List<Object> choices = (List<Object>) root.get("choices");
        Map<String, Object> choice = choices == null || choices.isEmpty() ? Map.of() : (Map<String, Object>) choices.get(0);
        Map<String, Object> message = choice.get("message") instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
        String content = text(message.get("content"));
        String reasoning = text(message.get("reasoning_content"));
        if (reasoning.isEmpty()) {
            reasoning = text(message.get("reasoning"));
        }
        Object finish = choice.get("finish_reason");
        Map<String, Object> usage = root.get("usage") instanceof Map<?, ?> u ? (Map<String, Object>) u : Map.of();

        if (showReasoning && !reasoning.isEmpty()) {
            System.err.println("--- reasoning ---\n" + reasoning + "\n--- /reasoning ---");
        }
        System.out.println(content);
        System.err.printf("[%s] finish=%s prompt=%s completion=%s reasoning_chars=%d max_tokens=%d %.1fs%n", ai.name, finish,
                number(usage.get("prompt_tokens")), number(usage.get("completion_tokens")), reasoning.length(), maxTokens, seconds);
        if (content.isEmpty() && "length".equals(finish)) {
            System.err.println("EMPTY ANSWER: the model used all " + maxTokens + " tokens thinking. Raise --max-tokens (it is a reasoning model).");
        } else if ("length".equals(finish)) {
            System.err.println("CUT OFF at max_tokens: raise --max-tokens.");
        }
    }

    private static String text(Object o) {
        return o instanceof String s ? s : "";
    }

    private static String number(Object o) {
        return o instanceof Double d ? String.valueOf(d.longValue()) : "?";
    }

    /**
     * Just enough JSON for a chat completion answer.
     */
    private static final class Json {
        private final String s;
        private int i;

        Json(String s) {
            this.s = s;
        }

        Object value() {
            ws();
            char c = s.charAt(i);
            if (c == '{') {
                i++;
                Map<String, Object> map = new LinkedHashMap<>();
                ws();
                if (s.charAt(i) == '}') {
                    i++;
                    return map;
                }
                while (true) {
                    ws();
                    String key = string();
                    ws();
                    i++; // :
                    map.put(key, value());
                    ws();
                    if (s.charAt(i++) == '}') {
                        return map;
                    }
                }
            }
            if (c == '[') {
                i++;
                List<Object> list = new ArrayList<>();
                ws();
                if (s.charAt(i) == ']') {
                    i++;
                    return list;
                }
                while (true) {
                    list.add(value());
                    ws();
                    if (s.charAt(i++) == ']') {
                        return list;
                    }
                }
            }
            if (c == '"') {
                return string();
            }
            if (s.startsWith("true", i)) {
                i += 4;
                return Boolean.TRUE;
            }
            if (s.startsWith("false", i)) {
                i += 5;
                return Boolean.FALSE;
            }
            if (s.startsWith("null", i)) {
                i += 4;
                return null;
            }
            int start = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) {
                i++;
            }
            return Double.valueOf(s.substring(start, i));
        }

        private String string() {
            StringBuilder sb = new StringBuilder();
            i++; // opening quote
            while (true) {
                char c = s.charAt(i++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                char e = s.charAt(i++);
                switch (e) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'u' -> {
                        sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                        i += 4;
                    }
                    default -> sb.append(e); // " \ /
                }
            }
        }

        private void ws() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
                i++;
            }
        }
    }

    private static void die(String message) {
        System.err.println(message);
        System.exit(2);
    }
}
