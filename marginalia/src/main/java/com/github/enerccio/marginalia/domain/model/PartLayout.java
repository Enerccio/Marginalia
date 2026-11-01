package com.github.enerccio.marginalia.domain.model;

import org.apache.commons.lang3.StringUtils;
import org.commonmark.node.Node;
import org.commonmark.node.SourceSpan;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;

import java.util.*;

/**
 * Where the images of a part are among its text, for the viewer and for the exports: the text is cut at the boundaries between its
 * top level blocks (paragraphs, lists, code blocks and so on are never cut), and the images are put at the cuts their
 * {@link ImageAttachment#effectiveAnchor() anchors} ask for. The cuts depend on the shape of the text, not on numbers saved with the
 * images, so they keep working when the text is edited.
 * <ul>
 *   <li>top: before the text;</li>
 *   <li>center: at the boundary nearest to the middle of the text (counted in characters);</li>
 *   <li>bottom: after the text, or before the last block when the text flows beside the image (so that at least the last block
 *   flows beside it).</li>
 * </ul>
 * An image the text flows around on both sides (a center column with wrapping) is the middle of a {@link Surrounded} band: a few blocks
 * of the text at its row (about {@value #BAND_CHARS} characters, two blocks at least) are split into the text at the left and the text
 * at the right of it. A band takes its blocks from the cut of its row, or before the end of the text for the bottom; the blocks of
 * one band are never in another, so a band that does not find two blocks left is shown as a block of the center column.
 * The images of one cut keep the order they have in the list (top before center before bottom where the cuts of the rows are the
 * same). Images that follow each other with the same anchor and wrapping form one {@link Pictures} segment.
 */
public final class PartLayout {
    private static final Parser PARSER = Parser.builder().includeSourceSpans(IncludeSourceSpans.BLOCKS).build();

    /**
     * The text of a band: it is taken block by block until it is this long.
     */
    public static final int BAND_CHARS = 400;

    private PartLayout() {
    }

    public sealed interface Segment permits Text, Pictures, Surrounded {
    }

    /**
     * @param markdown a run of whole blocks of the text
     */
    public record Text(String markdown) implements Segment {
    }

    /**
     * @param anchor the anchor of every image of the segment
     * @param wrap   true when the text flows beside the images (a left or a right anchor)
     * @param images the images, in their order
     */
    public record Pictures(ImageAnchor anchor, boolean wrap, List<ImageAttachment> images) implements Segment {
    }

    /**
     * Images in the middle of some text: the text is in two columns, with the images one above the other between them.
     *
     * @param anchor the anchor of the first image (the row is the same for all)
     * @param images the images, in their order
     * @param left   blocks of the text that are read first, at the left of the images
     * @param right  blocks of the text that follow, at the right of the images
     */
    public record Surrounded(ImageAnchor anchor, List<ImageAttachment> images, String left, String right) implements Segment {
    }

    /**
     * What goes at a place of the text: {@code pos} is the first block that follows it, {@code end} the first block after what it took.
     */
    private record Placement(int pos, int rank, int end, Segment segment) {
    }

    /**
     * @param markdown text of the part, may be blank
     * @param images   images of the part in their order, may be empty
     * @return the text and the images in the order they are shown; the text pieces together are the whole text
     */
    public static List<Segment> layout(String markdown, List<ImageAttachment> images) {
        String text = StringUtils.defaultString(markdown);
        if (images == null || images.isEmpty()) {
            return StringUtils.isBlank(text) ? List.of() : List.of(new Text(text));
        }
        int[] boundaries = boundaries(text);
        int blocks = boundaries.length - 1;
        int middle = middle(boundaries, text.length());

        // the bands first: the images the text flows around on both sides, by row
        Map<ImageAnchor.Vertical, List<ImageAttachment>> surrounding = new EnumMap<>(ImageAnchor.Vertical.class);
        for (ImageAttachment image : images) {
            if (image.surrounds()) {
                surrounding.computeIfAbsent(image.effectiveAnchor().vertical(), _ -> new ArrayList<>()).add(image);
            }
        }
        List<Placement> placements = new ArrayList<>();
        // the ranges of the blocks the bands took, by their start
        TreeMap<Integer, Integer> taken = new TreeMap<>();
        List<ImageAttachment> notPlaced = new ArrayList<>();
        int free = 0;
        for (ImageAnchor.Vertical row : ImageAnchor.Vertical.values()) {
            List<ImageAttachment> band = surrounding.get(row);
            if (band == null) {
                continue;
            }
            int start;
            int end;
            switch (row) {
                case TOP -> {
                    start = free;
                    end = forward(boundaries, start);
                }
                case CENTER -> {
                    start = Math.max(free, Math.min(middle, Math.max(0, blocks - 1)));
                    end = forward(boundaries, start);
                }
                default -> {
                    end = blocks;
                    start = Math.max(free, backward(boundaries, end));
                }
            }
            if (end - start < 2) {
                // no room for the text on both sides
                notPlaced.addAll(band);
                continue;
            }
            int split = middle(boundaries, start, end);
            placements.add(new Placement(start, 1, end, new Surrounded(band.getFirst().effectiveAnchor(), band,
                    text.substring(boundaries[start], boundaries[split]), text.substring(boundaries[split], boundaries[end]))));
            taken.put(start, end);
            free = end;
        }

        Map<Integer, List<ImageAttachment>> byCut = new TreeMap<>();
        for (ImageAttachment image : images) {
            if (image.surrounds() && !notPlaced.contains(image)) {
                continue;
            }
            int cut = switch (image.effectiveAnchor().vertical()) {
                case TOP -> 0;
                case CENTER -> middle;
                case BOTTOM -> image.floats() ? Math.max(0, blocks - 1) : blocks;
            };
            // a band has its blocks to itself, the image goes after them
            for (Map.Entry<Integer, Integer> band : taken.entrySet()) {
                if (cut > band.getKey() && cut < band.getValue()) {
                    cut = band.getValue();
                }
            }
            byCut.computeIfAbsent(cut, _ -> new ArrayList<>()).add(image);
        }
        for (Map.Entry<Integer, List<ImageAttachment>> entry : byCut.entrySet()) {
            // where the cuts of the rows meet (a part with one block, or none) the rows keep their order: top, center, bottom
            entry.getValue().sort(Comparator.comparing(image -> image.effectiveAnchor().vertical()));
            for (Segment pictures : pictures(entry.getValue())) {
                placements.add(new Placement(entry.getKey(), 0, entry.getKey(), pictures));
            }
        }
        // the images before the band that starts at their place, a stable sort keeps the order of the rest
        placements.sort(Comparator.comparingInt(Placement::pos).thenComparingInt(Placement::rank));

        List<Segment> result = new ArrayList<>();
        int previous = 0;
        for (Placement placement : placements) {
            if (placement.pos() > previous) {
                result.add(new Text(text.substring(boundaries[previous], boundaries[placement.pos()])));
            }
            result.add(placement.segment());
            previous = Math.max(previous, placement.end());
        }
        if (previous < blocks) {
            result.add(new Text(text.substring(boundaries[previous], boundaries[blocks])));
        }
        return result;
    }

    private static List<Segment> pictures(List<ImageAttachment> images) {
        List<Segment> result = new ArrayList<>();
        List<ImageAttachment> group = new ArrayList<>();
        for (ImageAttachment image : images) {
            if (!group.isEmpty() && (group.getFirst().effectiveAnchor() != image.effectiveAnchor()
                    || group.getFirst().floats() != image.floats())) {
                result.add(new Pictures(group.getFirst().effectiveAnchor(), group.getFirst().floats(), group));
                group = new ArrayList<>();
            }
            group.add(image);
        }
        if (!group.isEmpty()) {
            result.add(new Pictures(group.getFirst().effectiveAnchor(), group.getFirst().floats(), group));
        }
        return result;
    }

    /**
     * @return the end of a band that starts at the block: whole blocks until {@link #BAND_CHARS} characters and two blocks, or the end
     */
    private static int forward(int[] boundaries, int start) {
        int blocks = boundaries.length - 1;
        int end = start;
        int chars = 0;
        while (end < blocks && (chars < BAND_CHARS || end - start < 2)) {
            chars += boundaries[end + 1] - boundaries[end];
            end++;
        }
        return end;
    }

    /**
     * @return the start of a band that ends before the block: whole blocks back until {@link #BAND_CHARS} characters and two blocks, or
     * the start of the text
     */
    private static int backward(int[] boundaries, int end) {
        int start = end;
        int chars = 0;
        while (start > 0 && (chars < BAND_CHARS || end - start < 2)) {
            chars += boundaries[start] - boundaries[start - 1];
            start--;
        }
        return start;
    }

    /**
     * @return the index of the boundary (see {@link #boundaries}) nearest to the middle of the text, the later one on a tie
     */
    private static int middle(int[] boundaries, int length) {
        return middle(boundaries, 0, boundaries.length - 1, length / 2.0);
    }

    /**
     * @return the boundary between the first and the last of the blocks from start to end that is nearest to the middle of them
     */
    private static int middle(int[] boundaries, int start, int end) {
        return Math.max(start + 1, Math.min(end - 1, middle(boundaries, start, end, (boundaries[start] + boundaries[end]) / 2.0)));
    }

    private static int middle(int[] boundaries, int from, int to, double target) {
        int best = from;
        for (int i = from; i <= to; i++) {
            if (Math.abs(boundaries[i] - target) <= Math.abs(boundaries[best] - target)) {
                best = i;
            }
        }
        return best;
    }

    /**
     * @return the offsets in the text where its top level blocks start, and the length of the text at the end; the first offset is 0
     * (blank lines before the first block belong to it), so {@code n} blocks give {@code n + 1} offsets; one offset (0) when the text
     * has no block
     */
    private static int[] boundaries(String text) {
        if (StringUtils.isBlank(text)) {
            return new int[]{0};
        }
        int[] lineStarts = lineStarts(text);
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);
        Node document = PARSER.parse(text);
        boolean first = true;
        for (Node block = document.getFirstChild(); block != null; block = block.getNext()) {
            List<SourceSpan> spans = block.getSourceSpans();
            if (first) {
                first = false;
                continue;
            }
            if (!spans.isEmpty() && spans.getFirst().getLineIndex() < lineStarts.length) {
                int offset = lineStarts[spans.getFirst().getLineIndex()];
                if (offset > offsets.getLast()) {
                    offsets.add(offset);
                }
            }
        }
        offsets.add(text.length());
        int[] result = new int[offsets.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = offsets.get(i);
        }
        return result;
    }

    private static int[] lineStarts(String text) {
        List<Integer> starts = new ArrayList<>();
        starts.add(0);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                i++;
                starts.add(i + 1);
            } else if (c == '\n' || c == '\r') {
                starts.add(i + 1);
            }
        }
        int[] result = new int[starts.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = starts.get(i);
        }
        return result;
    }
}
