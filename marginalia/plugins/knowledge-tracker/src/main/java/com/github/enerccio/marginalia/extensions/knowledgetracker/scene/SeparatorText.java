package com.github.enerccio.marginalia.extensions.knowledgetracker.scene;

import java.util.ArrayList;
import java.util.List;

/**
 * The separators of the present characters field as the text the user edits: one per line, a new line shown as
 * {@code \n}.
 */
public final class SeparatorText {

    private static final String NEWLINE_TOKEN = "\\n";

    private SeparatorText() {
    }

    public static String format(List<String> separators) {
        List<String> lines = new ArrayList<>();
        for (String separator : separators) {
            if (separator == null) {
                continue;
            }
            if (separator.isBlank()) {
                if (separator.indexOf('\n') >= 0) {
                    lines.add(NEWLINE_TOKEN);
                }
            } else {
                lines.add(separator.strip());
            }
        }
        return String.join("\n", lines);
    }

    public static List<String> parse(String text) {
        List<String> separators = new ArrayList<>();
        if (text == null) {
            return separators;
        }
        for (String line : text.replace("\r\n", "\n").replace('\r', '\n').split("\n")) {
            String stripped = line.strip();
            if (stripped.equals(NEWLINE_TOKEN)) {
                separators.add("\n");
            } else if (!stripped.isEmpty()) {
                separators.add(stripped);
            }
        }
        return separators;
    }
}
