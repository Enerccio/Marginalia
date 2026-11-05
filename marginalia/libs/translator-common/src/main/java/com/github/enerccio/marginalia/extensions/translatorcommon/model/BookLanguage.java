package com.github.enerccio.marginalia.extensions.translatorcommon.model;

/**
 * A language a book is translated into besides the one it names first (the one of its inherited setting): the language (its id in the library of
 * the plugin) and who speaks it in this book. The plugin keeps a list of these in the settings of the book, so an epic with the tongues of the
 * elves, the dwarves and the orcs has one entry for each of the other two.
 */
public class BookLanguage {

    private String languageId;
    private Speakers speakers = new Speakers();

    public BookLanguage() {
    }

    /**
     * A language added to the book: nobody speaks it until the author lists the speakers (the first language of a book is spoken by everybody;
     * two languages that both claim everybody would both be asked for every speech).
     */
    public BookLanguage(String languageId) {
        this.languageId = languageId;
        this.speakers.setMode(SpeakersMode.ONLY);
    }

    public String getLanguageId() {
        return languageId == null || languageId.isBlank() ? null : languageId;
    }

    public void setLanguageId(String languageId) {
        this.languageId = languageId;
    }

    public Speakers getSpeakers() {
        if (speakers == null) {
            speakers = new Speakers();
        }
        return speakers;
    }

    public void setSpeakers(Speakers speakers) {
        this.speakers = speakers;
    }
}
