package com.github.enerccio.marginalia.extensions.conlangtranslator.designer;

/**
 * The stages of making a language, each a rules section that the model proposes and the author accepts before the next. The brief and the
 * seed lexicon are not here: the brief is the author's, the lexicon is made category by category.
 */
public enum DesignerStage {

    SOUNDS("Sounds", "the sound system: the consonants and vowels, the shape of a syllable, where the stress falls, what is forbidden, and the sounds that an English reader can read"),
    WRITING("Writing", "how each sound is written in the romanization the story uses, so that every word is spelled in one way only and an English reader can say it; capitals, hyphens and apostrophes included"),
    GRAMMAR("Grammar", "the grammar: the order of the words, how roles are marked (cases, particles, word order), number, tense and mood, questions and commands, negation, how adjectives and possession work, and politeness if the language has it"),
    WORD_FORMATION("Word formation", "how words are made: the roots, compounds, affixes, loanwords and, separately, how names of people and places are made; write the rules so that the reason of an invented word can name the rule it follows");

    private final String title;
    private final String about;

    DesignerStage(String title, String about) {
        this.title = title;
        this.about = about;
    }

    /** The title of the rules section of the stage. */
    public String title() {
        return title;
    }

    /** What the section has to cover, for the request. */
    public String about() {
        return about;
    }
}
