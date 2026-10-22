package com.github.enerccio.marginalia.domain.service.command;

import java.util.ArrayList;
import java.util.List;

/**
 * A command typed as text: {@code /command-name argument argument}. Arguments are separated by spaces; a space that is
 * part of an argument is written {@code \ } and a backslash {@code \\}. An argument can also be put in double quotes
 * ({@code "two words"}): inside them spaces need no escape and a quote is written {@code \"} (outside quotes {@code \"}
 * is a literal quote). A pair of quotes is an empty argument; quotes may start in the middle of an argument
 * ({@code a"b c"d} is {@code ab cd}) and one that is never closed runs to the end of the text. Any other backslash
 * stays as it is. Repeated spaces count as one.
 *
 * @param command           whether the text starts with the slash that makes it a command
 * @param name              the command name (without the slash), or the first word when there is no slash; may be empty
 * @param arguments         the arguments after the name, with the escapes resolved (text)
 * @param trailingSeparator whether the text ends with a separating space, so that the next argument is still empty
 */
public record CommandLine(boolean command, String name, List<String> arguments, boolean trailingSeparator) {

    public static CommandLine parse(String text) {
        String line = text == null ? "" : text.stripLeading();
        boolean command = line.startsWith("/");
        if (command) {
            line = line.substring(1);
        }

        List<String> words = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inWord = false;
        boolean trailing = false;
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\\' && i + 1 < line.length() && isEscapable(line.charAt(i + 1))) {
                current.append(line.charAt(++i));
                inWord = true;
                trailing = false;
            } else if (c == '"') {
                quoted = !quoted;
                inWord = true;
                trailing = false;
            } else if (c == ' ' && !quoted) {
                if (inWord) {
                    words.add(current.toString());
                    current.setLength(0);
                    inWord = false;
                }
                trailing = !words.isEmpty();
            } else {
                current.append(c);
                inWord = true;
                trailing = false;
            }
        }
        if (inWord) {
            words.add(current.toString());
        }

        String name = words.isEmpty() ? "" : words.getFirst();
        List<String> arguments = words.size() > 1 ? List.copyOf(words.subList(1, words.size())) : List.of();
        return new CommandLine(command, name, arguments, trailing);
    }

    private static boolean isEscapable(char c) {
        return c == ' ' || c == '\\' || c == '"';
    }

    /**
     * Writes an argument so that {@link #parse} reads it back as one argument.
     */
    public static String escape(String argument) {
        return argument.replace("\\", "\\\\").replace(" ", "\\ ").replace("\"", "\\\"");
    }

    /**
     * Whether the name is still being typed: there is no space after it yet.
     */
    public boolean typingName() {
        return arguments.isEmpty() && !trailingSeparator;
    }

    /**
     * The index of the argument being typed (the last one, or the next one after a space), -1 while the name is typed.
     */
    public int currentArgument() {
        if (typingName()) {
            return -1;
        }
        return trailingSeparator ? arguments.size() : arguments.size() - 1;
    }

}
