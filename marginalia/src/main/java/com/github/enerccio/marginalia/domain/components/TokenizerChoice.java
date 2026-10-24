package com.github.enerccio.marginalia.domain.components;

/**
 * A way to count the tokens of an OpenAI compatible AI that the user can choose: the {@code id} is what the AI stores, the
 * {@code label} is shown. Built-in choices are named after their strategy, those of plugins start with {@code ext:}.
 */
public record TokenizerChoice(String id, String label) {

    public static final String EXTENSION_PREFIX = "ext:";

    public boolean isExtension() {
        return id.startsWith(EXTENSION_PREFIX);
    }
}
