package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.ImageAttachment;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * Images attached to a message, shown under its text. The images are read from the store when the browser asks for
 * them, not when the message is rendered.
 */
@Configurable(preConstruction = true)
@Extendable
public class MessageImages extends Div {
    private static final Logger log = LoggerFactory.getLogger(MessageImages.class);

    @Autowired
    private ResourceService resourceService;

    public MessageImages(List<ImageAttachment> images) {
        setWidthFull();
        addClassName(CssStyles.MESSAGE_IMAGES);
        setImages(images);
    }

    public final void setImages(List<ImageAttachment> images) {
        removeAll();
        for (ImageAttachment attachment : images) {
            try {
                Resource resource = resourceService.findImage(attachment.resource());
                if (resource != null) {
                    add(figure(resource, attachment.caption(), "320px"));
                }
            } catch (Exception e) {
                log.warn("Can't show image {}", attachment.resource(), e);
            }
        }
        setVisible(getComponentCount() > 0);
    }

    /**
     * @return the image with its caption, the image is limited to the given width and keeps its proportions
     */
    public Div figure(Resource resource, String caption, String maxWidth) {
        StreamResource stream = new StreamResource(
                StringUtils.defaultIfBlank(resource.getOriginalName(), resource.getUuid()),
                () -> {
                    try {
                        return new ByteArrayInputStream(resourceService.getResourceData(resource));
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                });
        stream.setContentType(resource.getMimeType());
        stream.setCacheTime(0);

        Image image = new Image(stream, StringUtils.defaultString(caption));
        image.setMaxWidth(maxWidth);
        image.addClassName(CssStyles.MESSAGE_IMAGE);

        Div figure = new Div(image);
        figure.addClassName(CssStyles.MAX_WIDTH_FULL);
        if (StringUtils.isNotBlank(caption)) {
            Span text = new Span(caption);
            text.addClassNames(CssStyles.DISPLAY_BLOCK, CssStyles.TEXT_S, CssStyles.TEXT_SECONDARY);
            figure.add(text);
        }
        return figure;
    }
}
