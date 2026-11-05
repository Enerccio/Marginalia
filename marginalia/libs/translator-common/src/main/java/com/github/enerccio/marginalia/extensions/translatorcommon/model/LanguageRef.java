package com.github.enerccio.marginalia.extensions.translatorcommon.model;

/**
 * A language of a book as the screens show it: its id in the library of the plugin and the name the author gave it.
 *
 * @param id   the id in the library
 * @param name what the screens call it
 */
public record LanguageRef(String id, String name) {
}
