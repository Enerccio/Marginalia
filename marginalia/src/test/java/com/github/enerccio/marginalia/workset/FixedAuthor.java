package com.github.enerccio.marginalia.workset;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Answers with a fixed text that depends only on the request: proves the whole chain (prompt, save, dates, export) without a model, and costs nothing.
 */
public class FixedAuthor implements Author {

    @Override
    public String id() {
        return "fixed";
    }

    @Override
    public Answer answer(List<Message> messages) throws Exception {
        // the annotation asks for {"findings": [...]}: one finding about a sentence the fixed text has, so the rest of the loop (accept, redraft) runs
        boolean annotate = messages.stream().anyMatch(m -> m.content().contains("{\"findings\""));
        if (annotate) {
            String json = "{\"findings\":[{\"quote\":\"The lamp burned low.\",\"note\":\"Fixed note: the closing image is a cliche.\",\"type\":\"style\"}]}";
            return new Answer(json, "", messages.stream().mapToInt(m -> m.content().length() / 4).sum(), json.length() / 4, 0, 0, 1, id());
        }
        // the translators ask for {"translations": [...]}: one entry for each quoted speech of the PART, in the language the instructions name
        boolean translate = messages.stream().anyMatch(m -> m.role().equals("system") && m.content().contains("{\"translations\":["));
        if (translate) {
            return translation(messages);
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (Message m : messages) {
            digest.update(m.content().getBytes(StandardCharsets.UTF_8));
        }
        String tag = HexFormat.of().formatHex(digest.digest()).substring(0, 8);
        String text = "This is the fixed answer " + tag + ". The river moved under the ferry, and nobody on the bank said a word. "
                + "A second paragraph follows it, so that the part has some length and the counters of words and tokens have something to count.\n\n"
                + "\"Hold the rope,\" the ferrywoman said in Vrakht, and the girl held it.\n\n"
                + "\"Where are we?\" Mizuki asked in Japanese. \"Keep your voice down,\" Tomas muttered in Czech.\n\n"
                + "\"Water is a road,\" Aelmoria said in Aethel-Vael.\n\n"
                + "\"Is it far?\" Tomas asked in English.\n\n"
                + "The lamp burned low. Somewhere above the town a bell rang twice and stopped.";
        return new Answer(text, "", messages.stream().mapToInt(m -> m.content().length() / 4).sum(), text.length() / 4, 0, 0, 1, id());
    }

    private static final Pattern LANGUAGE = Pattern.compile("\"language\": \"([^\"]+)\" when the speech is spoken in it");
    private static final Pattern QUOTE = Pattern.compile("\"([^\"\\n]+)\"");

    /** Translates every quoted speech of the PART into "the language" with a fixed sign: proves the plugin chain (command, request, answer, passages, export) without a model. */
    private Answer translation(List<Message> messages) {
        String system = messages.stream().filter(m -> m.role().equals("system")).map(Message::content).findFirst().orElse("");
        String user = messages.stream().filter(m -> m.role().equals("user")).map(Message::content).reduce((a, b) -> b).orElse("");
        Matcher language = LANGUAGE.matcher(system);
        String name = language.find() ? language.group(1) : "Unknown";
        String part = user.contains("PART:\n") ? user.substring(user.lastIndexOf("PART:\n") + 6) : user;
        StringBuilder json = new StringBuilder("{\"translations\":[");
        Matcher quote = QUOTE.matcher(part);
        boolean first = true;
        while (quote.find()) {
            String quoted = quote.group(1);
            // like a model reading the cue after the speech: "in Vrakht" claims it for Vrakht, anything else is "none" for this language
            String after = part.substring(quote.end(), Math.min(part.length(), quote.end() + 60));
            boolean mine = after.contains(" in " + name);
            String text = !mine ? "" : name.equals("Japanese") ? "テスト" : "[" + name + "] " + quoted.toLowerCase();
            json.append(first ? "" : ",").append("{\"quote\":").append(quoteJson(quoted)).append(",\"speaker\":\"the speaker\",\"language\":\"").append(mine ? name : "none")
                    .append("\",\"text\":").append(quoteJson(text)).append(",\"reading\":\"\",\"understood\":true,\"register\":\"\",\"choice\":\"fixed\"}");
            first = false;
        }
        json.append("],\"newWords\":[]}");
        String text = json.toString();
        return new Answer(text, "", messages.stream().mapToInt(m -> m.content().length() / 4).sum(), text.length() / 4, 0, 0, 1, id());
    }

    private static String quoteJson(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
