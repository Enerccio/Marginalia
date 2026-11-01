package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import com.github.enerccio.marginalia.extensions.imagegenerator.ImageGeneratorLog;

/** Short forms for the services, which don't import the extension class. */
final class ImageGeneratorLogs {

    private ImageGeneratorLogs() {
    }

    static void warn(String message, Object... args) {
        ImageGeneratorLog.get().warn(message, args);
    }

    static void info(String message, Object... args) {
        ImageGeneratorLog.get().info(message, args);
    }
}
