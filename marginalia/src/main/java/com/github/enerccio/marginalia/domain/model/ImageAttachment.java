package com.github.enerccio.marginalia.domain.model;

/**
 * Image attached to a message. The image is not part of the message text (so it never reaches the model and does not
 * change summaries), it is shown with the message and embedded in the exports.
 *
 * @param resource uuid of the {@link com.github.enerccio.marginalia.domain.model.impl.Resource} holding the image
 * @param caption  text shown with the image, may be empty
 */
public record ImageAttachment(String resource, String caption) {

}
