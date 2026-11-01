package com.github.enerccio.marginalia.domain.model;

import com.github.enerccio.marginalia.domain.model.PartLayout.Pictures;
import com.github.enerccio.marginalia.domain.model.PartLayout.Segment;
import com.github.enerccio.marginalia.domain.model.PartLayout.Text;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Where the images of a part are among its text: the anchors, the cuts between blocks, the groups, and how it is kept in the part.
 */
class PartLayoutTest {
    private static final String TEXT = "First paragraph.\n\nSecond paragraph.\n\nThird paragraph.\n\nFourth paragraph.";

    private static ImageAttachment image(String name, ImageAnchor anchor, boolean wrap, Integer width) {
        return new ImageAttachment(name, "", anchor, wrap, width);
    }

    /**
     * A readable form of the layout: the text pieces by their first word, the pictures by their names.
     */
    private static String shape(List<Segment> segments) {
        StringBuilder shape = new StringBuilder();
        for (Segment segment : segments) {
            if (!shape.isEmpty()) {
                shape.append(" | ");
            }
            switch (segment) {
                case Text text -> {
                    String[] paragraphs = text.markdown().strip().split("\n\n");
                    shape.append(paragraphs[0].split(" ")[0]).append(paragraphs.length > 1 ? ".." + paragraphs[paragraphs.length - 1].split(" ")[0] : "");
                }
                case Pictures pictures -> shape.append(pictures.images().stream().map(ImageAttachment::resource)
                        .reduce((a, b) -> a + "," + b).orElse(""));
            }
        }
        return shape.toString();
    }

    @Test
    void anImageWithoutAnAnchorIsAfterTheText() {
        List<Segment> segments = PartLayout.layout(TEXT, List.of(new ImageAttachment("a", "caption")));

        assertThat(shape(segments)).isEqualTo("First..Fourth | a");
        assertThat(((Pictures) segments.get(1)).anchor()).isEqualTo(ImageAnchor.BOTTOM_CENTER);
        assertThat(((Pictures) segments.get(1)).wrap()).isFalse();
    }

    @Test
    void theRowIsThePlaceInTheText() {
        List<Segment> segments = PartLayout.layout(TEXT, List.of(
                image("bottom", ImageAnchor.BOTTOM_LEFT, false, null),
                image("middle", ImageAnchor.CENTER_RIGHT, false, null),
                image("top", ImageAnchor.TOP_CENTER, false, null)));

        assertThat(shape(segments)).isEqualTo("top | First..Second | middle | Third..Fourth | bottom");
    }

    @Test
    void theTextIsCutAtTheBoundaryNearestToTheMiddleOfTheText() {
        String block = " 0123456789".repeat(8);
        String text = "Alpha" + block + "\n\nBravo" + block + "\n\nGamma" + block;

        assertThat(shape(PartLayout.layout(text, List.of(image("m", ImageAnchor.CENTER_CENTER, false, null)))))
                .isEqualTo("Alpha | m | Bravo..Gamma");
    }

    @Test
    void theMiddleIsCountedInCharactersNotInBlocks() {
        // a short block and a long one: the middle of the characters is inside the long one, the boundary before it is the nearest
        String uneven = "Short.\n\n" + "Long ".repeat(40).strip() + "\n\nEnd.";

        assertThat(shape(PartLayout.layout(uneven, List.of(image("m", ImageAnchor.CENTER_CENTER, false, null)))))
                .isEqualTo("Short. | m | Long..End.");
    }

    @Test
    void aPictureTheTextFlowsAroundAtTheBottomIsBeforeTheLastBlock() {
        List<Segment> segments = PartLayout.layout(TEXT, List.of(image("f", ImageAnchor.BOTTOM_RIGHT, true, 30)));

        assertThat(shape(segments)).isEqualTo("First..Third | f | Fourth");
        assertThat(((Pictures) segments.get(1)).wrap()).isTrue();
    }

    @Test
    void theTextCannotFlowOnBothSidesOfAnImageInTheMiddle() {
        List<Segment> segments = PartLayout.layout(TEXT, List.of(image("c", ImageAnchor.BOTTOM_CENTER, true, 30)));

        assertThat(shape(segments)).isEqualTo("First..Fourth | c");
        assertThat(((Pictures) segments.get(1)).wrap()).isFalse();
    }

    @Test
    void imagesOfTheSameAnchorAreOneGroupInTheirOrder() {
        List<Segment> segments = PartLayout.layout(TEXT, List.of(
                image("a", ImageAnchor.TOP_LEFT, true, 20),
                image("b", ImageAnchor.BOTTOM_CENTER, false, null),
                image("c", ImageAnchor.TOP_LEFT, true, 20),
                image("d", ImageAnchor.TOP_RIGHT, true, 20),
                image("e", ImageAnchor.BOTTOM_CENTER, false, null)));

        assertThat(shape(segments)).isEqualTo("a,c | d | First..Fourth | b,e");
    }

    @Test
    void imagesOfTheSameCutThatDifferAreSeparateGroups() {
        List<Segment> segments = PartLayout.layout(TEXT, List.of(
                image("a", ImageAnchor.TOP_LEFT, false, null),
                image("b", ImageAnchor.TOP_LEFT, true, 20)));

        assertThat(segments.subList(0, 2)).allMatch(Pictures.class::isInstance);
        assertThat(shape(segments)).isEqualTo("a | b | First..Fourth");
    }

    @Test
    void blocksAreNeverCut() {
        String text = "Intro.\n\n- one\n- two\n- three\n\n```\ncode\n\nmore code\n```\n\nOutro.";
        List<Segment> segments = PartLayout.layout(text, List.of(image("m", ImageAnchor.CENTER_LEFT, false, null)));

        // the pieces together are the text, in order, and each piece starts at a block
        String joined = segments.stream().filter(Text.class::isInstance).map(s -> ((Text) s).markdown()).reduce("", String::concat);
        assertThat(joined).isEqualTo(text);
        for (Segment segment : segments) {
            if (segment instanceof Text piece) {
                assertThat(piece.markdown().strip()).doesNotStartWith("more code").doesNotStartWith("- two");
            }
        }
    }

    @Test
    void aPartWithOnlyImagesHasNoText() {
        List<Segment> segments = PartLayout.layout("  ", List.of(
                image("a", ImageAnchor.BOTTOM_CENTER, false, null),
                image("b", ImageAnchor.TOP_CENTER, false, null)));

        assertThat(shape(segments)).isEqualTo("b | a");
    }

    @Test
    void aPartWithOneBlockHasItsImagesBeforeOrAfter() {
        assertThat(shape(PartLayout.layout("Only.", List.of(
                image("t", ImageAnchor.TOP_LEFT, true, 20),
                image("m", ImageAnchor.CENTER_CENTER, false, null),
                image("b", ImageAnchor.BOTTOM_LEFT, true, 20))))).isEqualTo("t | b | Only. | m");
    }

    @Test
    void withoutImagesTheTextIsOnePiece() {
        assertThat(shape(PartLayout.layout(TEXT, List.of()))).isEqualTo("First..Fourth");
        assertThat(PartLayout.layout(null, List.of())).isEmpty();
        assertThat(PartLayout.layout("", null)).isEmpty();
    }

    @Test
    void theWidthIsKeptInTheLimits() {
        assertThat(new ImageAttachment("a", "", null, false, 5).width()).isEqualTo(ImageAttachment.MIN_WIDTH);
        assertThat(new ImageAttachment("a", "", null, false, 500).width()).isEqualTo(ImageAttachment.MAX_WIDTH);
        assertThat(new ImageAttachment("a", "", null, false, null).width()).isNull();
        // text needs room beside the image
        assertThat(image("a", ImageAnchor.TOP_LEFT, true, 90).effectiveWidth()).isEqualTo(ImageAttachment.MAX_WRAPPED_WIDTH);
        assertThat(image("a", ImageAnchor.TOP_CENTER, true, 90).effectiveWidth()).isEqualTo(90);
    }

    @Test
    void thePlaceIsKeptInThePart() {
        ChatMessage message = new ChatMessage();
        message.setImages(List.of(image("a", ImageAnchor.CENTER_LEFT, true, 35).withCaption("map")));

        ImageAttachment read = message.getImages().getFirst();
        assertThat(read.anchor()).isEqualTo(ImageAnchor.CENTER_LEFT);
        assertThat(read.wrap()).isTrue();
        assertThat(read.width()).isEqualTo(35);
        assertThat(read.caption()).isEqualTo("map");
    }

    @Test
    void imagesSavedBeforeThePlaceExistedAreAfterTheTextInTheirSize() throws Exception {
        ChatMessage message = new ChatMessage();
        var field = ChatMessage.class.getDeclaredField("imageAttachments");
        field.setAccessible(true);
        field.set(message, "[{\"resource\":\"uuid-1\",\"caption\":\"old\"}]");

        ImageAttachment read = message.getImages().getFirst();
        assertThat(read.resource()).isEqualTo("uuid-1");
        assertThat(read.anchor()).isNull();
        assertThat(read.effectiveAnchor()).isEqualTo(ImageAnchor.BOTTOM_CENTER);
        assertThat(read.wrap()).isFalse();
        assertThat(read.width()).isNull();
    }
}
