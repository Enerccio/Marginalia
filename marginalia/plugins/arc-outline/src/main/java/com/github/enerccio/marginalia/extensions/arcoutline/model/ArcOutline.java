package com.github.enerccio.marginalia.extensions.arcoutline.model;

import java.util.*;

/**
 * The arcs of a book, kept in memory while the story view is open. Changes are made here and the owner stores the
 * book with {@link #toData()} after each one.
 * <p>
 * Chapters are ordered inside their arc, but moving one past the first or the last chapter of its arc takes it into
 * the neighbouring arc, so a chapter can be moved from an arc to another one.
 */
public class ArcOutline {

    private final List<Arc> arcs = new ArrayList<>();

    public ArcOutline() {
    }

    /**
     * @param data what was stored, {@code null} for an empty outline; missing and duplicate values are repaired
     */
    public ArcOutline(ArcOutlineData data) {
        if (data == null || data.getArcs() == null) {
            return;
        }
        Set<String> uuids = new HashSet<>();
        for (Arc arc : data.getArcs()) {
            if (arc == null) {
                continue;
            }
            repair(arc, uuids);
            List<Chapter> chapters = new ArrayList<>();
            if (arc.getChapters() != null) {
                for (Chapter chapter : arc.getChapters()) {
                    if (chapter != null) {
                        repair(chapter, uuids);
                        chapters.add(chapter);
                    }
                }
            }
            arc.setChapters(chapters);
            arcs.add(arc);
        }
    }

    private static void repair(OutlineNode node, Set<String> uuids) {
        if (node.getUuid() == null || !uuids.add(node.getUuid())) {
            String uuid;
            do {
                uuid = UUID.randomUUID().toString();
            } while (!uuids.add(uuid));
            node.setUuid(uuid);
        }
        if (node.getTitle() == null) {
            node.setTitle("");
        }
        if (node.getDescription() == null) {
            node.setDescription("");
        }
        if (node.getNote() == null) {
            node.setNote("");
        }
        if (node.getState() == null) {
            node.setState(OutlineState.PLANNED);
        }
    }

    /**
     * Where the story is in the outline: the arc, its chapter and the chapter after it.
     *
     * @param arc     the arc of the chapter
     * @param chapter the current chapter; {@code null} when the arc has no chapter to be at
     * @param next    the chapter after the current one (it can be in the next arc), {@code null} when there is none
     */
    public record Current(Arc arc, Chapter chapter, Chapter next) {
    }

    /**
     * The place the story is at: the first chapter marked {@link OutlineState#CURRENT}, else the first chapter that is not
     * {@link OutlineState#FINISHED}. When no chapter qualifies the first arc marked current stands alone (an arc with no chapters
     * yet), and an outline that is finished or empty has no current place.
     *
     * @return {@code null} when there is none
     */
    public Current current() {
        for (OutlineState wanted : List.of(OutlineState.CURRENT, OutlineState.PLANNED)) {
            for (Arc arc : arcs) {
                for (Chapter chapter : arc.getChapters()) {
                    if (chapter.getState() == wanted) {
                        return new Current(arc, chapter, chapterAfter(chapter));
                    }
                }
            }
        }
        return arcs.stream().filter(arc -> arc.getState() == OutlineState.CURRENT).findFirst()
                .map(arc -> new Current(arc, null, null)).orElse(null);
    }

    /**
     * The chapter after the given one in the order of the outline, across the arcs.
     */
    private Chapter chapterAfter(Chapter chapter) {
        boolean found = false;
        for (Arc arc : arcs) {
            for (Chapter candidate : arc.getChapters()) {
                if (found) {
                    return candidate;
                }
                found = candidate == chapter;
            }
        }
        return null;
    }

    public ArcOutlineData toData() {
        ArcOutlineData data = new ArcOutlineData();
        data.setArcs(new ArrayList<>(arcs));
        return data;
    }

    /**
     * The arcs in their order.
     */
    public List<Arc> arcs() {
        return Collections.unmodifiableList(arcs);
    }

    /**
     * Adds the arc after the others.
     */
    public void add(Arc arc) {
        arcs.add(arc);
    }

    /**
     * Adds the chapter at the end of the arc.
     */
    public void add(Arc arc, Chapter chapter) {
        arc.getChapters().add(chapter);
    }

    /**
     * Adds the arc so that it gets the place {@code index} (from 0); an index past the end appends it.
     */
    public void add(int index, Arc arc) {
        arcs.add(Math.clamp(index, 0, arcs.size()), arc);
    }

    /**
     * Adds the chapter to the arc so that it gets the place {@code index} (from 0); an index past the end appends it.
     */
    public void add(Arc arc, int index, Chapter chapter) {
        arc.getChapters().add(Math.clamp(index, 0, arc.getChapters().size()), chapter);
    }

    /**
     * Moves the arc to the place {@code index} (from 0) among the arcs, counted as they are after it was taken out; an
     * index past the end puts it last.
     */
    public void moveTo(Arc arc, int index) {
        if (arcs.remove(arc)) {
            arcs.add(Math.clamp(index, 0, arcs.size()), arc);
        }
    }

    /**
     * Moves the chapter to the place {@code index} (from 0) in {@code target}, which can be another arc, counted as the
     * chapters of the arc are after it was taken out; an index past the end puts it last.
     */
    public void moveTo(Chapter chapter, Arc target, int index) {
        Arc from = arcOf(chapter);
        if (from != null) {
            from.getChapters().remove(chapter);
            target.getChapters().add(Math.clamp(index, 0, target.getChapters().size()), chapter);
        }
    }

    /**
     * Removes the arc with all its chapters.
     */
    public void delete(Arc arc) {
        arcs.remove(arc);
    }

    public void delete(Chapter chapter) {
        Arc arc = arcOf(chapter);
        if (arc != null) {
            arc.getChapters().remove(chapter);
        }
    }

    /**
     * The arc the chapter is in, {@code null} when it is in none.
     */
    public Arc arcOf(Chapter chapter) {
        for (Arc arc : arcs) {
            if (arc.getChapters().contains(chapter)) {
                return arc;
            }
        }
        return null;
    }

    public boolean canMoveUp(Arc arc) {
        return arcs.indexOf(arc) > 0;
    }

    public boolean canMoveDown(Arc arc) {
        int index = arcs.indexOf(arc);
        return index >= 0 && index < arcs.size() - 1;
    }

    /**
     * Swaps the arc with the one above it.
     */
    public void moveUp(Arc arc) {
        if (canMoveUp(arc)) {
            Collections.swap(arcs, arcs.indexOf(arc), arcs.indexOf(arc) - 1);
        }
    }

    /**
     * Swaps the arc with the one below it.
     */
    public void moveDown(Arc arc) {
        if (canMoveDown(arc)) {
            Collections.swap(arcs, arcs.indexOf(arc), arcs.indexOf(arc) + 1);
        }
    }

    /**
     * Whether there is a chapter above the chapter or an arc above its arc to take it to.
     */
    public boolean canMoveUp(Chapter chapter) {
        Arc arc = arcOf(chapter);
        return arc != null && (arc.getChapters().indexOf(chapter) > 0 || canMoveUp(arc));
    }

    public boolean canMoveDown(Chapter chapter) {
        Arc arc = arcOf(chapter);
        return arc != null && (arc.getChapters().indexOf(chapter) < arc.getChapters().size() - 1 || canMoveDown(arc));
    }

    /**
     * Moves the chapter up: above the previous chapter of its arc, or when it is the first one to the end of the
     * previous arc.
     *
     * @return the arc the chapter is in now, {@code null} when it can't move
     */
    public Arc moveUp(Chapter chapter) {
        if (!canMoveUp(chapter)) {
            return null;
        }
        Arc arc = arcOf(chapter);
        List<Chapter> chapters = arc.getChapters();
        int index = chapters.indexOf(chapter);
        if (index > 0) {
            Collections.swap(chapters, index, index - 1);
            return arc;
        }
        Arc previous = arcs.get(arcs.indexOf(arc) - 1);
        chapters.remove(chapter);
        previous.getChapters().add(chapter);
        return previous;
    }

    /**
     * Moves the chapter down: below the next chapter of its arc, or when it is the last one to the start of the next
     * arc.
     *
     * @return the arc the chapter is in now, {@code null} when it can't move
     */
    public Arc moveDown(Chapter chapter) {
        if (!canMoveDown(chapter)) {
            return null;
        }
        Arc arc = arcOf(chapter);
        List<Chapter> chapters = arc.getChapters();
        int index = chapters.indexOf(chapter);
        if (index < chapters.size() - 1) {
            Collections.swap(chapters, index, index + 1);
            return arc;
        }
        Arc next = arcs.get(arcs.indexOf(arc) + 1);
        chapters.remove(chapter);
        next.getChapters().add(0, chapter);
        return next;
    }
}
