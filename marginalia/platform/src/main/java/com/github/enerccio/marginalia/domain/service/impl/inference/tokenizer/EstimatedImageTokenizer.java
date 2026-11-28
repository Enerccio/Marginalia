package com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.service.ImageTokenizer;
import com.github.enerccio.marginalia.domain.service.agent.ImageFiles;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;

/**
 * The estimate of an image by its pixels, for every model: one token per {@value #PIXELS_PER_TOKEN} pixels (Anthropic's
 * published rule; Qwen VL takes about one per 784, OpenAI and Gemini take fewer, Gemma a fixed 256), of the image as it is
 * sent, that is scaled down to {@link ImageFiles#MAX_SIDE} like {@link ImageFiles#prepare} does. Rather too much than too
 * little: an image that does not fit fails the request, a story part too many only shortens the story so far.
 * <p>
 * An image whose size can't be read from its header (a link, a file the application does not know) counts as
 * {@value #UNKNOWN_TOKENS} tokens, about a full size image of most providers.
 */
public class EstimatedImageTokenizer implements ImageTokenizer {

    public static final String NAME = "ESTIMATE";
    /** Pixels per token. */
    public static final int PIXELS_PER_TOKEN = 750;
    /** The tokens of an image of unknown size. */
    public static final long UNKNOWN_TOKENS = 1600;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public long countImageTokens(AI ai, LLMAttachment image) {
        return estimate(image);
    }

    /**
     * The estimate of the attachment, 0 for one that is not an image.
     */
    public static long estimate(LLMAttachment image) {
        if (image == null || kind(image) != LLMAttachment.Kind.IMAGE) {
            return 0;
        }
        int[] size = image.getData() == null ? null : ImageFiles.dimensions(image.getData());
        return size == null ? UNKNOWN_TOKENS : estimate(size[0], size[1]);
    }

    /**
     * The estimate of an image of the size, scaled down as it would be sent; at least 1.
     */
    public static long estimate(int width, int height) {
        if (width < 1 || height < 1) {
            return UNKNOWN_TOKENS;
        }
        double scale = Math.min(1, (double) ImageFiles.MAX_SIDE / Math.max(width, height));
        long w = Math.max(1, Math.round(width * scale));
        long h = Math.max(1, Math.round(height * scale));
        return Math.max(1, (w * h + PIXELS_PER_TOKEN - 1) / PIXELS_PER_TOKEN);
    }

    static LLMAttachment.Kind kind(LLMAttachment attachment) {
        return attachment.getKind() != null ? attachment.getKind() : LLMAttachment.Kind.fromMimeType(attachment.getMimeType());
    }
}
