package com.github.enerccio.marginalia.extensions.reallangtranslator.model;

/**
 * What a book remembers of its word extraction: the prompt of the library it uses and the specification the author wrote for this book. Kept in
 * {@link BookSettings}; the range of parts is chosen for every run.
 */
public class ExtractionSettings {

    /** The id of the prompt (a built-in one or one of the user's library); null for none: the specification is then all the model is told. */
    private String promptId;
    /** Free text for this book: what to look for, what to leave out, how to write a kind of word. Goes after the prompt. */
    private String specification = "";

    public String getPromptId() {
        return promptId == null || promptId.isBlank() ? null : promptId;
    }

    public void setPromptId(String promptId) {
        this.promptId = promptId;
    }

    public String getSpecification() {
        return specification == null ? "" : specification.strip();
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }
}
