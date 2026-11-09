package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.ImageAnchor;
import com.github.enerccio.marginalia.domain.model.ImageAttachment;
import com.github.enerccio.marginalia.domain.model.PartLayout;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.markdown.Markdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * A part of the book as the reader sees it: the text with its images where the {@link ImageAttachment#effectiveAnchor() anchors} of the
 * images put them (see {@link PartLayout}). The text is cut at the boundaries of its blocks into several {@link Markdown} components
 * with the pictures between them, so the style of the text applies to every piece. The viewer and the preview of the story editor
 * build the part with this class, so they look the same.
 */
@Configurable(preConstruction = true)
public class PartView extends Div {
    private static final Logger log = LoggerFactory.getLogger(PartView.class);

    @Autowired
    private ResourceService resourceService;

    private final MessageImages renderer = new MessageImages(List.of());
    // the published book the part is read in, null for the author's own windows; the images of a book that is not the reader's are found through it
    private final String manuscriptUuid;

    public PartView(String markdown, List<ImageAttachment> images) {
        this(markdown, images, null);
    }

    /**
     * @param manuscriptUuid the book the part belongs to when it is shown to a reader, who may see the images of a published book
     */
    public PartView(String markdown, List<ImageAttachment> images, String manuscriptUuid) {
        this.manuscriptUuid = manuscriptUuid;
        addClassName(CssStyles.PART_VIEW);
        setContent(markdown, images);
    }

    public final void setContent(String markdown, List<ImageAttachment> images) {
        removeAll();
        for (PartLayout.Segment segment : PartLayout.layout(markdown, images)) {
            switch (segment) {
                case PartLayout.Text text -> add(markdown(text.markdown()));
                case PartLayout.Pictures pictures -> addPictures(pictures);
                case PartLayout.Surrounded band -> addBand(band);
            }
        }
        setVisible(getComponentCount() > 0);
    }

    private Markdown markdown(String text) {
        Markdown piece = new Markdown();
        piece.addClassName(CssStyles.CHAT_MESSAGE_MARKDOWN);
        piece.setContent(text);
        return piece;
    }

    /**
     * @param width width in percent of the box the picture is in, null for the size of the picture
     * @return the picture with its caption, null when the image is gone
     */
    private Div picture(ImageAttachment attachment, Integer width) {
        try {
            Resource resource = resourceService.findImageForReader(attachment.resource(), manuscriptUuid);
            if (resource == null) {
                return null;
            }
            Div picture = renderer.figure(resource, attachment.caption(), "100%");
            picture.addClassName(CssStyles.PART_PICTURE);
            if (width != null) {
                picture.addClassName(CssStyles.PART_PICTURE_SIZED);
                // the width asked for by the author
                picture.getStyle().setWidth(width + "%");
            }
            return picture;
        } catch (Exception e) {
            log.warn("Can't show image {}", attachment.resource(), e);
            return null;
        }
    }

    private void addPictures(PartLayout.Pictures pictures) {
        Div group = new Div();
        group.addClassName(CssStyles.PART_PICTURES);
        if (pictures.wrap()) {
            group.addClassName(CssStyles.PART_PICTURES_FLOAT);
        } else {
            group.addClassName(switch (pictures.anchor().horizontal()) {
                case LEFT -> CssStyles.PART_PICTURES_LEFT;
                case CENTER -> CssStyles.PART_PICTURES_CENTER;
                case RIGHT -> CssStyles.PART_PICTURES_RIGHT;
            });
        }
        for (ImageAttachment attachment : pictures.images()) {
            Div picture = picture(attachment, attachment.effectiveWidth());
            if (picture == null) {
                continue;
            }
            if (pictures.wrap()) {
                picture.addClassName(pictures.anchor().horizontal() == ImageAnchor.Horizontal.LEFT
                        ? CssStyles.PART_PICTURE_FLOAT_LEFT : CssStyles.PART_PICTURE_FLOAT_RIGHT);
            }
            group.add(picture);
        }
        if (group.getComponentCount() > 0) {
            add(group);
        }
    }

    /**
     * The text in two columns with the pictures between them; the column of the pictures is as wide as the widest of them.
     */
    private void addBand(PartLayout.Surrounded band) {
        int widest = band.images().stream().mapToInt(image -> image.effectiveWidth()).max().orElse(ImageAttachment.SURROUNDED_WIDTH);
        Div middle = new Div();
        middle.addClassName(CssStyles.PART_BAND_MIDDLE);
        for (ImageAttachment attachment : band.images()) {
            // each is a part of the column
            Div picture = picture(attachment, attachment.effectiveWidth() * 100 / widest);
            if (picture != null) {
                middle.add(picture);
            }
        }
        if (middle.getComponentCount() == 0) {
            // all the images are gone, the text stays as it was
            add(markdown(band.left()), markdown(band.right()));
            return;
        }
        Div left = new Div(markdown(band.left()));
        left.addClassName(CssStyles.PART_BAND_TEXT);
        Div right = new Div(markdown(band.right()));
        right.addClassName(CssStyles.PART_BAND_TEXT);
        Div grid = new Div(left, middle, right);
        grid.addClassName(CssStyles.PART_BAND);
        // the width of the middle column is the one the author asked for
        grid.getStyle().set("grid-template-columns", "1fr " + widest + "% 1fr");
        add(grid);
    }
}
