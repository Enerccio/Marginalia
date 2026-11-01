package com.github.enerccio.marginalia.ui.menu;

import java.util.ArrayList;
import java.util.List;

/**
 * The path of a menu group: names separated by {@code /} ({@code "Whatever/Is"} is the group Is inside the group Whatever). A
 * slash that is a part of a name is written {@code \/}.
 */
public final class MenuPath {

    private MenuPath() {
    }

    /**
     * The names of the path, outermost first; none for a blank path.
     */
    public static List<String> parse(String path) {
        List<String> names = new ArrayList<>();
        if (path == null || path.isBlank()) {
            return names;
        }
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '\\' && i + 1 < path.length() && path.charAt(i + 1) == '/') {
                name.append('/');
                i++;
            } else if (c == '/') {
                add(names, name);
            } else {
                name.append(c);
            }
        }
        add(names, name);
        return names;
    }

    private static void add(List<String> names, StringBuilder name) {
        String text = name.toString().strip();
        name.setLength(0);
        if (!text.isEmpty()) {
            names.add(text);
        }
    }

    /**
     * The path of the names (the inverse of {@link #parse}); the identity of a group.
     */
    public static String join(List<String> names) {
        StringBuilder path = new StringBuilder();
        for (String name : names) {
            if (path.length() > 0) {
                path.append('/');
            }
            path.append(name.replace("/", "\\/"));
        }
        return path.toString();
    }

    /**
     * The path of the first {@code count} names.
     */
    public static String join(List<String> names, int count) {
        return join(names.subList(0, count));
    }
}
