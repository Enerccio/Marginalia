package com.github.enerccio.marginalia.extensions.conlangtranslator.service;

import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

/**
 * The logger of the extension for the windows, which are created with {@code new} and have no way to ask for it; set when the extension loads
 * (the log goes to the extension log, see {@code OsgiService.createLogger}).
 */
public final class PluginLog {

    private static volatile Logger logger = NOPLogger.NOP_LOGGER;

    private PluginLog() {
    }

    public static void set(Logger log) {
        logger = log == null ? NOPLogger.NOP_LOGGER : log;
    }

    public static Logger get() {
        return logger;
    }
}
