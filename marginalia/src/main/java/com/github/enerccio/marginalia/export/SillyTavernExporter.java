package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import com.github.enerccio.marginalia.domain.service.ExporterService.Exporter;
import com.github.enerccio.marginalia.ui.dialogs.ProgressBarDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * The story as a chat of SillyTavern (a JSON Lines file that its "Import chat" reads): the instructions the author gave are the messages of
 * the user and the text the model wrote are the messages of the character, which is named after the book. A message the author wrote
 * without instructions (or edited) is just the message of the character.
 */
public class SillyTavernExporter implements Exporter {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    @Override
    public String getName() {
        return "SillyTavern chat";
    }

    @Override
    public String getId() {
        return "sillytavern";
    }

    @Override
    public String getFileExtension() {
        return "jsonl";
    }

    @Override
    public String getMimeType() {
        return "application/x-ndjson; charset=UTF-8";
    }

    @Override
    public void exportTo(ExportOptions options, List<ChatMessage> chatMessages, ProgressBarDialog progress, Path target) throws Exception {
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(target))) {
            write(options, chatMessages, progress, out);
        }
    }

    private void write(ExportOptions options, List<ChatMessage> chatMessages, ProgressBarDialog progress, OutputStream out) throws IOException {
        String user = StringUtils.defaultIfBlank(StringUtils.trimToNull(options.author()), "User");
        String character = StringUtils.defaultIfBlank(StringUtils.trimToNull(options.bookName()), "Story");

        JsonObject header = new JsonObject();
        header.addProperty("user_name", user);
        header.addProperty("character_name", character);
        header.addProperty("create_date", Instant.now().toString());
        header.add("chat_metadata", new JsonObject());
        line(out, header);

        for (ChatMessage message : chatMessages) {
            String response = StringUtils.trimToNull(message.getResponse());
            if (response == null) {
                progress.updateProgress();
                continue;
            }
            String date = (message.getRequest() == null ? new Date() : message.getRequest()).toInstant().toString();
            String instructions = StringUtils.trimToNull(message.getInstructions());
            if (instructions != null) {
                line(out, entry(user, true, date, instructions));
            }
            JsonObject answer = entry(character, false, date, response);
            String reasoning = StringUtils.trimToNull(message.getResponseReasoning());
            if (reasoning != null) {
                answer.getAsJsonObject("extra").addProperty("reasoning", reasoning);
            }
            line(out, answer);
            progress.updateProgress();
        }
    }

    private static JsonObject entry(String name, boolean isUser, String date, String text) {
        JsonObject entry = new JsonObject();
        entry.addProperty("name", name);
        entry.addProperty("is_user", isUser);
        entry.addProperty("is_system", false);
        entry.addProperty("send_date", date);
        entry.addProperty("mes", text);
        entry.add("extra", new JsonObject());
        return entry;
    }

    private static void line(OutputStream out, JsonObject json) throws IOException {
        // Gson writes a line break in a string as \n, so the object stays on one line
        out.write(GSON.toJson(json).getBytes(StandardCharsets.UTF_8));
        out.write('\n');
    }
}
