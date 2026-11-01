package com.github.enerccio.marginalia.domain.model;

/**
 * Where an image of a part is in the viewer and in the exports: the column is the side of the text, the row is the place in the
 * text (before it, in the middle of it, after it). Without wrapping the image is a block of the row, aligned to the column; with
 * wrapping and a left or right column the text flows around the image.
 */
public enum ImageAnchor {
    TOP_LEFT(Vertical.TOP, Horizontal.LEFT),
    TOP_CENTER(Vertical.TOP, Horizontal.CENTER),
    TOP_RIGHT(Vertical.TOP, Horizontal.RIGHT),
    CENTER_LEFT(Vertical.CENTER, Horizontal.LEFT),
    CENTER_CENTER(Vertical.CENTER, Horizontal.CENTER),
    CENTER_RIGHT(Vertical.CENTER, Horizontal.RIGHT),
    BOTTOM_LEFT(Vertical.BOTTOM, Horizontal.LEFT),
    BOTTOM_CENTER(Vertical.BOTTOM, Horizontal.CENTER),
    BOTTOM_RIGHT(Vertical.BOTTOM, Horizontal.RIGHT);

    public enum Vertical {TOP, CENTER, BOTTOM}

    public enum Horizontal {LEFT, CENTER, RIGHT}

    private final Vertical vertical;
    private final Horizontal horizontal;

    ImageAnchor(Vertical vertical, Horizontal horizontal) {
        this.vertical = vertical;
        this.horizontal = horizontal;
    }

    public Vertical vertical() {
        return vertical;
    }

    public Horizontal horizontal() {
        return horizontal;
    }

    public static ImageAnchor of(Vertical vertical, Horizontal horizontal) {
        for (ImageAnchor anchor : values()) {
            if (anchor.vertical == vertical && anchor.horizontal == horizontal) {
                return anchor;
            }
        }
        throw new IllegalArgumentException(vertical + " " + horizontal);
    }
}
