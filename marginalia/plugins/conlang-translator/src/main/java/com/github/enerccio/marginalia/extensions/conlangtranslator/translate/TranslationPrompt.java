package com.github.enerccio.marginalia.extensions.conlangtranslator.translate;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Audience;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Register;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.RuleSection;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.CharacterNote;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Speakers;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.SpeakersMode;

import java.util.List;

/**
 * The request that asks the model which direct speech of a part is spoken in the language and what it is in it. One request does a part and
 * both classifies and translates. The wording follows the one that was tried on a real model (see TODO.md); change it together with that test.
 * <p>
 * The <b>system message</b> is the same for every part of a book (the instructions, the rules, the registers and the lexicon), so a server that
 * caches the prompt reads it once; the <b>user message</b> has the part and what is known of the people in it.
 */
public final class TranslationPrompt {

    /** Said in the user message when the PART holds speeches that are translated already (a run that only writes the missing ones). */
    static final String PARTLY_TRANSLATED = "The PART is partly translated already: a speech that is not written in English any more is finished. Give no entry for it; "
            + "give entries only for the direct speech that is still written in English.\n";

    /** The instructions, before the language. {@code %s} is the name of the language. */
    static final String INSTRUCTIONS = """
            You translate the direct speech of a story into a constructed language. The story is written in English. You do not rewrite the story: you only report which direct speech is spoken in the constructed language, and what it is in that language.

            For EVERY piece of direct speech in the PART (the words the characters say aloud, inside quotation marks) give one entry, in the order they appear:
            - "quote": the speech copied exactly from the PART, character for character, without the quotation marks. A quotation that has several sentences may be one entry or one entry for each sentence; the quotes of the entries never overlap.
            - "speaker": who says it.
            - "language": "%1$s" when the speech is spoken in it, or "none" when it is spoken in ordinary English (the common language of the story) or in any other language than %1$s, real or invented (Japanese, Czech, another constructed language): speech that the PART gives to another language is "none" for this request, never %1$s.
            - "text": the speech in %1$s, written strictly by the rules of the language; empty when the language is "none".
            - "understood": false when the people who hear it cannot understand it, otherwise true.
            - "register": the register of the words you chose (see REGISTERS), empty for the neutral one.
            - "choice": one short sentence: why this language, and the main choices of the translation.

            Who speaks %1$s: %2$s The text of the PART overrides this: if it says that a speech is spoken in another language ("in the common tongue", "in English"), or that someone speaks %1$s, follow the text. A character who does not know %1$s may be spoken to in it; that speech is still in %1$s.

            How to translate: translate the meaning, not word for word, with the grammar of the language. Use the words of the lexicon exactly as written; a form may have several senses, pick the one the context needs. Choose the form of a word by the register that the speaker's voice and the situation ask for. A word that is already in %1$s, and every word of the lexicon, is kept as it is. Never put an English word into a translation, except the proper names of people and places, which stay as they are written in the PART. If you need a word that is not in the lexicon, invent one that follows the sound and word-formation rules of the language (a compound of existing roots is preferred), use it, and report it in "newWords". Do not stretch a word of the lexicon to a meaning it does not have: invent and report instead.

            Answer with one JSON object only, no other text, in this form:
            {"translations":[{"quote":"...","speaker":"...","language":"...","text":"...","understood":true,"register":"","choice":"..."}],"newWords":[{"word":"...","gloss":"...","category":"...","rationale":"...","register":"","kind":"","variantOf":""}]}
            For a new word "category" is an existing category of the lexicon or a new one, "rationale" says why this word (which roots or rule), "kind" is root, compound, affix, particle, name, number, phrase or other, and "variantOf" is the word it is another register of, if it is. When the PART has no direct speech, both lists are empty.""";

    private TranslationPrompt() {
    }

    /**
     * The request.
     *
     * @param system the system message
     * @param user   the user message
     */
    public record Request(String system, String user) {
    }

    /**
     * What is known of the part besides its text.
     *
     * @param pov      the point of view character, may be empty
     * @param present  the characters present, may be empty
     * @param earlier  the parts before it as the model reads them, oldest first, for who speaks to whom; may be empty
     */
    public record Scene(String pov, String present, List<String> earlier) {

        public static Scene none() {
            return new Scene("", "", List.of());
        }
    }

    /**
     * @param language the language the book uses
     * @param speakers who speaks it in this book
     * @param partText the part as the model reads it (the source)
     * @param scene    what is known of the part besides its text
     * @param maxLexiconChars the size above which the lexicon is cut down, see {@link LexiconText#select}
     */
    public static Request build(Language language, Speakers speakers, String partText, Scene scene, int maxLexiconChars) {
        return build(language, speakers, partText, scene, maxLexiconChars, false);
    }

    /**
     * @param partText   the part as the model reads it: the source, or, for a run that writes only the missing speeches, the part as it is stored
     *                   with the translated speeches in it
     * @param translated whether {@code partText} holds speeches that are translated already; the model is told to leave them
     */
    public static Request build(Language language, Speakers speakers, String partText, Scene scene, int maxLexiconChars, boolean translated) {
        return new Request(system(language, speakers, partText, maxLexiconChars), user(language, speakers, partText, scene, translated));
    }

    static String system(Language language, Speakers speakers, String partText, int maxLexiconChars) {
        return String.format(INSTRUCTIONS, language.getName(), speakersRule(language, speakers)) + "\n\n" + languageBlock(language, partText, maxLexiconChars);
    }

    /**
     * What the model is told of the language: the description, the rules for the translator, the registers and the lexicon (cut down to what
     * {@code text} needs when it is too big). The same for every request of a book, so a server that caches the prompt reads it once.
     */
    public static String languageBlock(Language language, String text, int maxLexiconChars) {
        String name = language.getName();
        StringBuilder system = new StringBuilder();
        system.append("=== THE LANGUAGE: ").append(name.toUpperCase()).append(" ===\n");
        if (!language.getDescription().isBlank()) {
            system.append(language.getDescription().strip()).append("\n\n");
        }
        for (RuleSection section : language.getRules()) {
            if (section.getAudience() == Audience.MODEL && !section.getText().isBlank()) {
                system.append("\n--- ").append(section.getTitle().isBlank() ? "Rules" : section.getTitle()).append(" ---\n")
                        .append(section.getText().strip()).append('\n');
            }
        }
        if (language.isHyphenStructural()) {
            system.append("\nA hyphen is a part of a word of this language: keep it exactly.\n");
        }
        if (!language.getRegisters().isEmpty()) {
            system.append("\n=== REGISTERS ===\n");
            for (Register register : language.getRegisters()) {
                system.append(register.getName());
                if (!register.getNote().isBlank()) {
                    system.append(" - ").append(register.getNote().strip());
                }
                system.append('\n');
            }
        }
        system.append("\n=== LEXICON ===\n").append(LexiconText.select(language, text, maxLexiconChars)).append('\n');
        return system.toString();
    }

    private static String speakersRule(Language language, Speakers speakers) {
        String name = language.getName();
        if (speakers.getMode() == SpeakersMode.ONLY) {
            return "only the characters the CHARACTERS list marks as speaking " + name + " speak it.";
        }
        return "a character speaks " + name + " unless the CHARACTERS list says they do not know it.";
    }

    static String user(Language language, Speakers speakers, String partText, Scene scene) {
        return user(language, speakers, partText, scene, false);
    }

    static String user(Language language, Speakers speakers, String partText, Scene scene, boolean translated) {
        StringBuilder user = new StringBuilder();
        String characters = characters(language, speakers, scene, partText);
        if (!characters.isEmpty()) {
            user.append("CHARACTERS:\n").append(characters).append('\n');
        }
        if (scene != null && !scene.earlier().isEmpty()) {
            user.append("THE STORY BEFORE (for who speaks to whom; do not translate it):\n");
            scene.earlier().forEach(part -> user.append(part.strip()).append("\n\n"));
        }
        if (scene != null && !scene.pov().isBlank()) {
            user.append("Point of view: ").append(scene.pov().strip()).append('\n');
        }
        if (scene != null && !scene.present().isBlank()) {
            user.append("Characters present: ").append(scene.present().strip()).append('\n');
        }
        if (translated) {
            user.append('\n').append(PARTLY_TRANSLATED);
        }
        user.append("\nPART:\n").append(partText == null ? "" : partText.strip()).append('\n');
        return user.toString();
    }

    /**
     * The characters of the notes that matter to the part: the ones called in the text, in the point of view or among the present ones. A
     * book with no notes has no list, and a character that is not in it is just a speaker of the default.
     */
    public static String characters(Language language, Speakers speakers, Scene scene, String partText) {
        StringBuilder text = new StringBuilder();
        String haystack = ((partText == null ? "" : partText) + "\n" + (scene == null ? "" : scene.pov() + "\n" + scene.present())).toLowerCase();
        boolean only = speakers.getMode() == SpeakersMode.ONLY;
        for (CharacterNote note : speakers.getCharacters()) {
            boolean named = haystack.contains(note.getName().strip().toLowerCase())
                    || note.getAliases().stream().anyMatch(alias -> !alias.isBlank() && haystack.contains(alias.strip().toLowerCase()));
            if (!named || note.getName().isBlank()) {
                continue;
            }
            boolean speaks = only == note.isListed();
            text.append("- ").append(note.getName().strip());
            if (!note.getAliases().isEmpty()) {
                text.append(" (also ").append(String.join(", ", note.getAliases())).append(')');
            }
            text.append(": ").append(speaks ? "speaks " : "does NOT know ").append(language.getName());
            if (!note.getGender().isBlank()) {
                text.append("; ").append(note.getGender().strip());
            }
            if (!note.getVoice().isBlank()) {
                text.append("; voice: ").append(note.getVoice().strip());
            }
            text.append('\n');
        }
        return text.toString();
    }
}
