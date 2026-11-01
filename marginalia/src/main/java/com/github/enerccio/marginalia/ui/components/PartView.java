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

    public PartView(String markdown, List<ImageAttachment> images) {
        addClassName(CssStyles.PART_VIEW);
        setContent(markdown, images);
    }

    public final void setContent(String markdown, List<ImageAttachment> images) {
        removeAll();
        for (PartLayout.Segment segment : PartLayout.layout(markdown, images)) {
            switch (segment) {
                case PartLayout.Text text -> {
                    Markdown piece = new Markdown();
                    piece.addClassName(CssStyles.CHAT_MESSAGE_MARKDOWN);
                    piece.setContent(text.markdown());
                    add(piece);
                }
                case PartLayout.Pictures pictures -> addPictures(pictures);
            }
        }
        setVisible(getComponentCount() > 0);
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
            try {
                Resource resource = resourceService.findImage(attachment.resource());
                if (resource == null) {
                    continue;
                }
                Div picture = renderer.figure(resource, attachment.caption(), "100%");
                picture.addClassName(CssStyles.PART_PICTURE);
                Integer width = attachment.effectiveWidth();
                if (width != null) {
                    picture.addClassName(CssStyles.PART_PICTURE_SIZED);
                    // the width asked for by the author
                    picture.getStyle().setWidth(width + "%");
                }
                if (pictures.wrap()) {
                    picture.addClassName(pictures.anchor().horizontal() == ImageAnchor.Horizontal.LEFT
                            ? CssStyles.PART_PICTURE_FLOAT_LEFT : CssStyles.PART_PICTURE_FLOAT_RIGHT);
                }
                group.add(picture);
            } catch (Exception e) {
                log.warn("Can't show image {}", attachment.resource(), e);
            }
        }
        if (group.getComponentCount() > 0) {
            add(group);
        }
    }
}
