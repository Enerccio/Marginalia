package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ImageTools} over a {@link VirtualFileSystem}. The tool only checks the image and notes it in the
 * {@link ShownImages}; the runner reads it again when it makes the request, so what stays in the transcript is the path.
 * Like for the file tools, what goes wrong becomes a text for the model with no real path in it.
 */
public final class ImageToolsImpl implements ImageTools {

    private static final Logger log = LoggerFactory.getLogger(ImageToolsImpl.class);

    private final VirtualFileSystem fs;
    private final ShownImages shown;

    public ImageToolsImpl(VirtualFileSystem fs, ShownImages shown) {
        this.fs = fs;
        this.shown = shown;
    }

    @Override
    public void viewImage(FunctionContinuation<String> done, String path) {
        try {
            VirtualPath file = fs.parse(path);
            ImageFiles.Prepared image = fs.readImage(file);
            shown.add(file);
            String size = image.width() > 0 ? ", " + image.width() + " x " + image.height() + " pixels" : "";
            done.complete("Showing " + file + " (" + image.mimeType().substring("image/".length()) + size
                    + (image.changed() ? ", scaled down" : "") + ") in the next message.");
        } catch (VirtualFileException e) {
            if (e.getCause() != null) {
                log.warn("The tool view_image failed: {}", e.getMessage(), e.getCause());
            }
            done.fail(e.getMessage());
        } catch (RuntimeException e) {
            log.warn("The tool view_image failed", e);
            done.fail("The image could not be read");
        }
    }

}
