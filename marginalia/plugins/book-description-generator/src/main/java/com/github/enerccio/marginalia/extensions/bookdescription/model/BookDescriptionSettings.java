package com.github.enerccio.marginalia.extensions.bookdescription.model;

/**
 * What is stored in the attributes of the user settings under {@link #KEY}: the user level of the two prompts (books and
 * folders inherit them, see {@code BookDescriptionInherited}; blank means the built-in text) and what the dialog opens with.
 */
public class BookDescriptionSettings {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.bookdescription.settings";

    /** The prompt for the name and the description, see {@code Defaults.DESCRIPTION_PROMPT}. */
    private String descriptionPrompt;
    /** The prompt for the notes of a part of a long story, see {@code Defaults.NOTES_PROMPT}. */
    private String notesPrompt;
    /** Whether the summaries of the story stand in for the messages they cover. */
    private boolean useSummaries = true;
    /** {@link OutputFormat} name. */
    private String format;

    public String getDescriptionPrompt() {
        return descriptionPrompt;
    }

    public void setDescriptionPrompt(String descriptionPrompt) {
        this.descriptionPrompt = descriptionPrompt;
    }

    public String getNotesPrompt() {
        return notesPrompt;
    }

    public void setNotesPrompt(String notesPrompt) {
        this.notesPrompt = notesPrompt;
    }

    public boolean isUseSummaries() {
        return useSummaries;
    }

    public void setUseSummaries(boolean useSummaries) {
        this.useSummaries = useSummaries;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    /** The format of the settings, {@link OutputFormat#AUTO} when none (or an unknown one) is saved. */
    public OutputFormat format() {
        try {
            return format == null ? OutputFormat.AUTO : OutputFormat.valueOf(format);
        } catch (IllegalArgumentException e) {
            return OutputFormat.AUTO;
        }
    }
}
