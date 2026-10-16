package com.github.enerccio.marginalia.extensions.nameprovider;

/**
 * A real name as the model gets it.
 *
 * @param romanized the name in Latin script
 * @param original  the name as it is written in its own language (the same as {@code romanized} for a name written in Latin script)
 */
public record NameEntry(String romanized, String original) {}
