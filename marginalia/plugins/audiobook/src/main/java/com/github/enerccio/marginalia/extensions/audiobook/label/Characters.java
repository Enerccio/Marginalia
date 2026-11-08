package com.github.enerccio.marginalia.extensions.audiobook.label;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** The names of the characters the book already tells (the point of view and the present characters of its parts), for the instruction of the labelling. */
public final class Characters {

    /** More names only crowd the instruction. */
    public static final int MAX = 12;

    private Characters() {
    }

    /** @param fields the texts with names, comma, semicolon or line separated (null and blank ones are skipped); the order of the first mention is kept */
    public static List<String> of(List<String> fields) {
        Set<String> names = new LinkedHashSet<>();
        for (String field : fields) {
            if (field == null) {
                continue;
            }
            for (String name : field.split("[,;\\n]")) {
                String clean = name.strip();
                if (!clean.isEmpty() && clean.length() <= 60) {
                    names.add(clean);
                }
            }
        }
        List<String> list = new ArrayList<>(names);
        return list.size() > MAX ? list.subList(0, MAX) : list;
    }
}
