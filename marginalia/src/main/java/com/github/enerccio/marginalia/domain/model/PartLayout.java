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
 *   <li>bottom: after the text, or before the last block when the text flows around the image (so that at least the last block
 *   flows around it).</li>
 * </ul>
 * The images of one cut keep the order they have in the list (top before center before bottom where the cuts of the rows are the same). Images that follow each other with the same anchor and wrapping form
 * one {@link Pictures} segment.
 */
public final class PartLayout {
    private static final Parser PARSER = Parser.builder().includeSourceSpans(IncludeSourceSpans.BLOCKS).build();

    private PartLayout() {
    }

    public sealed interface Segment permits Text, Pictures {
    }

    /**
     * @param markdown a run of whole blocks of the text
     */
    public record Text(String markdown) implements Segment {
    }

    /**
     * @param anchor the anchor of every image of the segment
     * @param wrap   true when the text flows around the images (a left or a right anchor)
     * @param images the images, in their order
     */
    public record Pictures(ImageAnchor anchor, boolean wrap, List<ImageAttachment> images) implements Segment {
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

        Map<Integer, List<ImageAttachment>> byCut = new TreeMap<>();
        for (ImageAttachment image : images) {
            int cut = switch (image.effectiveAnchor().vertical()) {
                case TOP -> 0;
                case CENTER -> middle;
                case BOTTOM -> image.floats() ? Math.max(0, blocks - 1) : blocks;
            };
            byCut.computeIfAbsent(cut, _ -> new ArrayList<>()).add(image);
        }

        List<Segment> result = new ArrayList<>();
        int previous = 0;
        for (Map.Entry<Integer, List<ImageAttachment>> entry : byCut.entrySet()) {
            int cut = entry.getKey();
            // where the cuts of the rows meet (a part with one block, or none) the rows keep their order: top, center, bottom
            entry.getValue().sort(Comparator.comparing(image -> image.effectiveAnchor().vertical()));
            if (cut > previous) {
                result.add(new Text(text.substring(boundaries[previous], boundaries[cut])));
            }
            previous = Math.max(previous, cut);
            addPictures(result, entry.getValue());
        }
        if (previous < blocks) {
            result.add(new Text(text.substring(boundaries[previous], boundaries[blocks])));
        }
        return result;
    }

    private static void addPictures(List<Segment> result, List<ImageAttachment> images) {
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
    }

    /**
     * @return the index of the boundary (see {@link #boundaries}) nearest to the middle of the text, the later one on a tie
     */
    private static int middle(int[] boundaries, int length) {
        int best = 0;
        for (int i = 0; i < boundaries.length; i++) {
            if (Math.abs(2L * boundaries[i] - length) <= Math.abs(2L * boundaries[best] - length)) {
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
