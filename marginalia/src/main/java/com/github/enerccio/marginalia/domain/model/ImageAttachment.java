package com.github.enerccio.marginalia.domain.model;

/**
 * Image attached to a message. The image is not part of the message text (so it never reaches the model and does not
 * change summaries), it is shown with the message and embedded in the exports. Where it is shown is a property of the
 * attachment (see {@link PartLayout}); the editor of the part ignores it and lists the images.
 *
 * @param resource uuid of the {@link com.github.enerccio.marginalia.domain.model.impl.Resource} holding the image
 * @param caption  text shown with the image, may be empty
 * @param anchor   where the image is, null for {@link #DEFAULT_ANCHOR} (after the text, centered)
 * @param wrap     true to let the text flow around the image: beside it for a left or right anchor, on both sides of it (in two
 *                 columns) for a center one
 * @param width    width of the image in percent of the width of the text, null for the size of the image (never wider than the
 *                 text); {@link #MIN_WIDTH} to {@link #MAX_WIDTH}, at most {@link #MAX_WRAPPED_WIDTH} when the text flows beside it and
 *                 {@link #MAX_SURROUNDED_WIDTH} when it is in the middle of the text
 */
public record ImageAttachment(String resource, String caption, ImageAnchor anchor, boolean wrap, Integer width) {

    public static final ImageAnchor DEFAULT_ANCHOR = ImageAnchor.BOTTOM_CENTER;
    public static final int MIN_WIDTH = 10;
    public static final int MAX_WIDTH = 100;
    public static final int MAX_WRAPPED_WIDTH = 60;
    public static final int MAX_SURROUNDED_WIDTH = 40;
    /**
     * The width of an image in the middle of the text without a width of its own.
     */
    public static final int SURROUNDED_WIDTH = 30;

    /**
     * An image after the text, in the size it has.
     */
    public ImageAttachment(String resource, String caption) {
        this(resource, caption, null, false, null);
    }

    public ImageAttachment {
        if (width != null) {
            width = Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, width));
        }
    }

    /**
     * @return the anchor, {@link #DEFAULT_ANCHOR} when none is set
     */
    public ImageAnchor effectiveAnchor() {
        return anchor == null ? DEFAULT_ANCHOR : anchor;
    }

    /**
     * @return true when the text flows beside the image: wrapping was asked for and the image is at the left or at the right
     */
    public boolean floats() {
        return wrap && effectiveAnchor().horizontal() != ImageAnchor.Horizontal.CENTER;
    }

    /**
     * @return true when the text is on both sides of the image: wrapping was asked for and the image is in the middle column
     */
    public boolean surrounds() {
        return wrap && effectiveAnchor().horizontal() == ImageAnchor.Horizontal.CENTER;
    }

    /**
     * @return the largest width in percent of the text the image can have: the text needs room beside it
     */
    public int maxWidth() {
        return surrounds() ? MAX_SURROUNDED_WIDTH : floats() ? MAX_WRAPPED_WIDTH : MAX_WIDTH;
    }

    /**
     * @return width in percent of the text, null for the size of the image; an image the text flows around is limited by
     * {@link #maxWidth()}, wider would leave no room for the text, and one in the middle of the text without a width has
     * {@link #SURROUNDED_WIDTH}
     */
    public Integer effectiveWidth() {
        if (width == null) {
            return surrounds() ? Integer.valueOf(SURROUNDED_WIDTH) : null;
        }
        return Integer.valueOf(Math.min(width, maxWidth()));
    }

    public ImageAttachment withCaption(String caption) {
        return new ImageAttachment(resource, caption, anchor, wrap, width);
    }

    public ImageAttachment withResource(String resource) {
        return new ImageAttachment(resource, caption, anchor, wrap, width);
    }

    public ImageAttachment withLayout(ImageAnchor anchor, boolean wrap, Integer width) {
        return new ImageAttachment(resource, caption, anchor, wrap, width);
    }
}
