package com.github.enerccio.marginalia.workset;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The author is a model reached through the {@code claude} command line tool in its non-interactive mode: one call per request, the system
 * message of the application as the system prompt (the tool's own is replaced), no tools, no session, none of the tool's settings, skills or
 * CLAUDE.md files (it runs in an empty folder). The user message goes to the standard input.
 * <p>
 * A request with a JSON schema ({@code response_format}) goes with {@code --json-schema}, so the answer has the shape the application asked for.
 */
public class ClaudeCliAuthor implements Author {

    private final String model;
    private final Path workingDir;
    private final long timeoutMinutes;

    public ClaudeCliAuthor(String model, Path workingDir, long timeoutMinutes) {
        this.model = model;
        this.workingDir = workingDir;
        this.timeoutMinutes = timeoutMinutes;
    }

    @Override
    public String id() {
        return "claude:" + model;
    }

    @Override
    public Answer answer(List<Message> messages) throws Exception {
        return answer(messages, null);
    }

    @Override
    public Answer answer(List<Message> messages, String jsonSchema) throws Exception {
        String system = messages.stream().filter(m -> m.role().equals("system")).map(Message::content).collect(Collectors.joining("\n\n"));
        List<Message> rest = messages.stream().filter(m -> !m.role().equals("system")).toList();
        String user;
        if (rest.size() == 1 && rest.get(0).role().equals("user")) {
            user = rest.get(0).content();
        } else {
            // more than one turn: the history as a transcript, the last user message at the end
            user = rest.stream().map(m -> (m.role().equals("assistant") ? "[Previous answer]\n" : "") + m.content()).collect(Collectors.joining("\n\n"));
        }
        if (user.isBlank()) {
            // a request with a system message only (the summary of a part): the tool needs an input, so the instructions are the input
            user = system;
            system = "";
        }
        Files.createDirectories(workingDir);

        List<String> command = new ArrayList<>(List.of("claude", "-p", "--model", model,
                "--tools", "", "--no-session-persistence", "--disable-slash-commands", "--setting-sources", "",
                "--output-format", "json"));
        if (jsonSchema != null && !jsonSchema.isBlank()) {
            // structured output as a server with response_format gives it: the tool answers with JSON that matches (the text is in "result")
            command.add("--json-schema");
            command.add(jsonSchema);
        }
        if (!system.isBlank()) {
            command.add("--system-prompt");
            command.add(system);
        }
        long start = System.currentTimeMillis();
        Process process = new ProcessBuilder(command).directory(workingDir.toFile()).redirectErrorStream(false).start();
        try (OutputStream in = process.getOutputStream()) {
            in.write(user.getBytes(StandardCharsets.UTF_8));
        }
        // read the standard error aside so a full pipe never stops the tool
        StringBuilder err = new StringBuilder();
        Thread errReader = new Thread(() -> {
            try {
                err.append(new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8));
            } catch (IOException ignored) {
            }
        });
        errReader.start();
        String out = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!process.waitFor(timeoutMinutes, TimeUnit.MINUTES)) {
            process.destroyForcibly();
            throw new IOException("claude did not finish in " + timeoutMinutes + " minutes");
        }
        errReader.join(2000);
        long millis = System.currentTimeMillis() - start;
        if (process.exitValue() != 0) {
            throw new IOException("claude exited with " + process.exitValue() + ": " + err + out);
        }
        JsonObject json = JsonParser.parseString(out.strip()).getAsJsonObject();
        if (json.has("is_error") && json.get("is_error").getAsBoolean()) {
            throw new IOException("claude reported an error: " + json);
        }
        String text = json.has("result") && !json.get("result").isJsonNull() ? json.get("result").getAsString() : "";
        if (text.isBlank()) {
            throw new IOException("claude gave an empty answer: " + out);
        }
        long in = 0, outTokens = 0, cached = 0;
        if (json.has("usage") && json.get("usage").isJsonObject()) {
            JsonObject usage = json.getAsJsonObject("usage");
            in = number(usage, "input_tokens") + number(usage, "cache_creation_input_tokens") + number(usage, "cache_read_input_tokens");
            outTokens = number(usage, "output_tokens");
            cached = number(usage, "cache_read_input_tokens");
        }
        double cost = json.has("total_cost_usd") && !json.get("total_cost_usd").isJsonNull() ? json.get("total_cost_usd").getAsDouble() : 0;
        return new Answer(text.strip(), "", in, outTokens, cached, cost, millis, id());
    }

    private static long number(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsLong() : 0;
    }
}
