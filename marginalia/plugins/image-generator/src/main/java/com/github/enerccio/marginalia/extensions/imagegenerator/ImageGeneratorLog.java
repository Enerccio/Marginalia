package com.github.enerccio.marginalia.extensions.imagegenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The logger of the extension: lines go to the extension log (Admin → Extensions → Log) once the extension is loaded;
 * before that, and after it is unloaded, only to the application log.
 */
public final class ImageGeneratorLog {

    private static final Logger FALLBACK = LoggerFactory.getLogger("image-generator");
    private static volatile Logger logger = FALLBACK;

    private ImageGeneratorLog() {
    }

    public static Logger get() {
        return logger;
    }

    static void install(Logger extensionLogger) {
        logger = extensionLogger;
    }

    static void reset() {
        logger = FALLBACK;
    }
}
