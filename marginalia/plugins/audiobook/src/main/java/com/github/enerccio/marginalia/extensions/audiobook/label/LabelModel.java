package com.github.enerccio.marginalia.extensions.audiobook.label;

/** Asks the language model once and waits for the answer (the application's implementation is {@code LlmLabelModel}; the tests use a fake). */
@FunctionalInterface
public interface LabelModel {

    /** @throws Exception when the model fails or its answer does not match the schema */
    LabelAnswer ask(String system, String user) throws Exception;
}
