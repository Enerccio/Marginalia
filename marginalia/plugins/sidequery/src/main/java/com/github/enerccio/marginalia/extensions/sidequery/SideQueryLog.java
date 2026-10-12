package com.github.enerccio.marginalia.extensions.sidequery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The logger of the extension: lines go to the extension log (Admin → Extensions → Log) once the extension is loaded;
 * before that, and after it is unloaded, only to the application log (which gets the lines of the extension log too). Classes of the extension take it when they are
 * created.
 */
public final class SideQueryLog {

    private static final Logger FALLBACK = LoggerFactory.getLogger("sidequery");
    private static volatile Logger logger = FALLBACK;

    private SideQueryLog() {
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
