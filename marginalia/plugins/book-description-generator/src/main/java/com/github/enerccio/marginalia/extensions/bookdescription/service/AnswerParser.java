package com.github.enerccio.marginalia.extensions.bookdescription.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.enerccio.marginalia.domain.service.structured.PartialJson;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the name and the description of the book out of an answer of the model. The format is judged from the text, not from
 * what was asked for: a model that answers in Markdown when it was asked for JSON (or the other way round) is still read.
 * <ul>
 *   <li>JSON: an object with {@code name} and {@code description}, also after a sentence of introduction or in a fenced block.</li>
 *   <li>Markdown: the first line is the name (a heading, bold, or {@code Name: ...}), the rest is the description, without a
 *       {@code Description:} label.</li>
 * </ul>
 */
public final class AnswerParser {

    /** What was read. */
    public record Parsed(String name, String description) {
    }

    private static final Pattern THINK = Pattern.compile("^\\s*<(think|thinking)>.*?</\\1>", Pattern.DOTALL);
    private static final Pattern THINK_OPEN = Pattern.compile("^\\s*<(?:think|thinking)>", Pattern.DOTALL);
    private static final Pattern FENCED = Pattern.compile("\\A\\s*```[^\\n]*\\n(.*?)\\n?```\\s*\\z", Pattern.DOTALL);
    private static final Pattern JSON_KEYS = Pattern.compile("\\{\\s*\"(?:name|description)\"");
    private static final Pattern NAME_LABEL = Pattern.compile("^(?:book\\s+)?(?:name|title)[*_]*\\s*[:：][*_]*\\s*(.*)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DESCRIPTION_LABEL = Pattern.compile("^(?:book\\s+)?(?:description|blurb|synopsis)[*_]*\\s*(?:[:：][*_]*\\s*(.*))?$", Pattern.CASE_INSENSITIVE);

    private AnswerParser() {
    }

    /**
     * @return what the answer says; null when it has no name or no description
     */
    public static Parsed parse(String answer) {
        if (answer == null) {
            return null;
        }
        String text = withoutReasoning(answer).strip();
        Matcher fenced = FENCED.matcher(text);
        if (fenced.matches()) {
            text = fenced.group(1).strip();
        }
        Parsed json = readJson(text);
        return json != null ? json : readMarkdown(text);
    }

    /** The text without a reasoning at its start (between think tags); nothing when it is cut in the middle of one. */
    public static String withoutReasoning(String text) {
        Matcher think = THINK.matcher(text);
        if (think.find()) {
            return text.substring(think.end());
        }
        // cut in the middle of the thinking: nothing was answered
        return THINK_OPEN.matcher(text).find() ? "" : text;
    }

    // --- JSON ---

    private static Parsed readJson(String text) {
        int start = text.startsWith("{") ? 0 : -1;
        if (start < 0) {
            Matcher keys = JSON_KEYS.matcher(text);
            if (keys.find()) {
                start = keys.start();
            }
        }
        if (start < 0) {
            return null;
        }
        JsonNode root = PartialJson.parse(text.substring(start));
        if (root == null || !root.isObject()) {
            return null;
        }
        return of(root.path("name").asText(""), root.path("description").asText(""));
    }

    // --- Markdown ---

    private static Parsed readMarkdown(String text) {
        List<String> lines = new ArrayList<>(text.lines().toList());
        while (!lines.isEmpty() && lines.getFirst().isBlank()) {
            lines.removeFirst();
        }
        if (lines.isEmpty()) {
            return null;
        }
        String first = lead(lines.removeFirst());
        Matcher label = NAME_LABEL.matcher(first);
        String name = clean(label.matches() ? label.group(1) : first);

        while (!lines.isEmpty() && lines.getFirst().isBlank()) {
            lines.removeFirst();
        }
        if (!lines.isEmpty()) {
            Matcher description = DESCRIPTION_LABEL.matcher(lead(lines.getFirst()));
            // a label alone on its line, or with the first line of the description after it
            if (description.matches()) {
                String rest = description.group(1) == null ? "" : description.group(1).strip();
                lines.removeFirst();
                if (!rest.isEmpty()) {
                    lines.addFirst(rest);
                }
            }
        }
        return of(name, String.join("\n", lines));
    }

    /** A line without the heading marks and the emphasis it starts with. */
    private static String lead(String line) {
        return line.strip().replaceFirst("^[#>\\s*_]+", "").strip();
    }

    /** A name without its emphasis, heading marks and quotes. */
    private static String clean(String line) {
        String cleaned = line.strip();
        cleaned = cleaned.replaceFirst("^[#>\\s]+", "");
        cleaned = cleaned.replaceAll("[*_`]+", "");
        cleaned = cleaned.replaceAll("#+\\s*$", "");
        return cleaned.strip();
    }

    private static Parsed of(String name, String description) {
        String n = unquote(name.strip());
        String d = description.strip();
        return n.isEmpty() || d.isEmpty() ? null : new Parsed(n, d);
    }

    private static String unquote(String text) {
        if (text.length() >= 2) {
            char first = text.charAt(0);
            char last = text.charAt(text.length() - 1);
            if ((first == '"' && last == '"') || (first == '“' && last == '”') || (first == '„' && last == '“') || (first == '「' && last == '」')) {
                return text.substring(1, text.length() - 1).strip();
            }
        }
        return text;
    }
}
