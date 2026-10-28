package com.github.enerccio.marginalia.extensions.stconverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The logger of the extension: lines go to the extension log (Admin → Extensions → Log) once the extension is loaded;
 * before that, and after it is unloaded, only to the application log (which gets the lines of the extension log too).
 */
public final class STConverterLog {

    private static final Logger FALLBACK = LoggerFactory.getLogger("sillytavern-converter");
    private static volatile Logger logger = FALLBACK;

    private STConverterLog() {
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
