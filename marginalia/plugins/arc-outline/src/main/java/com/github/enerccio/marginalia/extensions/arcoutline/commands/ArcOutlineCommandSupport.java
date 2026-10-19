package com.github.enerccio.marginalia.extensions.arcoutline.commands;

import com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read.ManuscriptReadSupport;
import com.github.enerccio.marginalia.extensions.arcoutline.model.*;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * What the commands of the Arc Outline have in common: the outline of the book of the scope of the command (the open
 * book for the user, the working copy for an agent), the arcs and chapters addressed by their <b>numbers</b> as the
 * outline shows them (the first arc is 1, the first chapter of an arc is 1; a number changes when something before it
 * is added, removed or moved), and the JSON the answers are made of. No uuid of the outline is given out or accepted.
 */
abstract class ArcOutlineCommandSupport extends ManuscriptReadSupport {

    protected static final String PARAM_ARC = "Number of the arc in the outline, the first arc is 1 (see get-arc-outline)";
    protected static final String PARAM_CHAPTER = "Number of the chapter in its arc, the first chapter is 1 (see get-arc-outline)";
    protected static final String PARAM_STATE = "PLANNED, CURRENT or FINISHED. Finishing an arc finishes all its chapters";

    protected final ArcOutlineService service;

    protected ArcOutlineCommandSupport(ArcOutlineService service) {
        this.service = service;
    }

    /** The outline of the book of the scope, as it is stored now. */
    protected ArcOutline outline(Book book) {
        return service.read(book.manuscript);
    }

    /** Stores the outline in the book and lets the open views of the book read it again. */
    protected void store(Book book, ArcOutline outline) throws Exception {
        service.save(book.manuscript, outline);
        service.changed(book.manuscript);
    }

    protected static Arc arc(ArcOutline outline, Integer number) {
        int count = outline.arcs().size();
        if (number == null || number < 1 || number > count) {
            throw new IllegalArgumentException(count == 0 ? "The outline has no arcs yet"
                    : "There is no arc " + number + ", the outline has " + count + (count == 1 ? " arc" : " arcs") + " (1 to " + count + ")");
        }
        return outline.arcs().get(number - 1);
    }

    protected static Chapter chapter(Arc arc, int arcNumber, Integer number) {
        int count = arc.getChapters().size();
        if (number == null || number < 1 || number > count) {
            throw new IllegalArgumentException(count == 0 ? "The arc " + arcNumber + " has no chapters yet"
                    : "There is no chapter " + number + " in the arc " + arcNumber + ", it has " + count
                    + (count == 1 ? " chapter" : " chapters") + " (1 to " + count + ")");
        }
        return arc.getChapters().get(number - 1);
    }

    /** The state called so (case and separators do not matter), the {@code fallback} for none. */
    protected static OutlineState state(String text, OutlineState fallback) {
        return option(OutlineState.class, text, fallback, "state");
    }

    protected static <E extends Enum<E>> E option(Class<E> type, String text, E fallback, String what) {
        if (StringUtils.isBlank(text)) {
            return fallback;
        }
        String wanted = text.strip().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        for (E constant : type.getEnumConstants()) {
            if (constant.name().equals(wanted)) {
                return constant;
            }
        }
        StringBuilder values = new StringBuilder();
        for (E constant : type.getEnumConstants()) {
            values.append(values.isEmpty() ? "" : ", ").append(constant.name());
        }
        throw new IllegalArgumentException("Unknown " + what + " \"" + text.strip() + "\". Use one of: " + values);
    }

    /** A title for a new node: required. */
    protected static String requiredTitle(String title) {
        if (StringUtils.isBlank(title)) {
            throw new IllegalArgumentException("The title is required");
        }
        return title.strip();
    }

    /**
     * Index (from 0) of the place a 1-based position asks for among {@code size} items that will be there after the
     * item is added or moved; no position is the end. A position past the end is the end.
     */
    protected static int index(Integer position, int size) {
        if (position == null) {
            return size;
        }
        if (position < 1) {
            throw new IllegalArgumentException("The position starts at 1");
        }
        return Math.min(position - 1, size);
    }

    // --- the answers ---

    protected static JsonObject node(int number, OutlineNode node, boolean notes) {
        JsonObject row = new JsonObject();
        row.addProperty("number", number);
        row.addProperty("title", node.getTitle());
        row.addProperty("state", node.getState().name());
        row.addProperty("description", node.getDescription());
        if (notes) {
            row.addProperty("note", node.getNote());
        } else {
            row.addProperty("hasNote", StringUtils.isNotBlank(node.getNote()));
        }
        return row;
    }

    protected static JsonObject arcRow(int number, Arc arc, boolean notes, boolean chapters) {
        JsonObject row = node(number, arc, notes);
        if (chapters) {
            JsonArray list = new JsonArray();
            int n = 1;
            for (Chapter chapter : arc.getChapters()) {
                list.add(node(n++, chapter, notes));
            }
            row.add("chapters", list);
        } else {
            row.addProperty("chapterCount", arc.getChapters().size());
        }
        return row;
    }

    /** The place the arc has in the outline now, 1 for the first. */
    protected static int numberOf(ArcOutline outline, Arc arc) {
        return outline.arcs().indexOf(arc) + 1;
    }

}
