package com.github.enerccio.marginalia.extensions.reallangtranslator.translate;

import com.github.enerccio.marginalia.extensions.reallangtranslator.model.*;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.CharacterNote;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Speakers;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.SpeakersMode;

import java.util.List;

/**
 * The request that asks the model which direct speech of a part is spoken in the language and what it is in it. One request does a part and both
 * classifies and translates. There is no lexicon: the model knows the language, and the rules of the language (script, politeness, names) are the
 * only guard besides the checks of {@link AnswerChecker}.
 * <p>
 * The <b>system message</b> is the same for every part of a book (the instructions and the language), so a server that caches the prompt reads it
 * once; the <b>user message</b> has the part and what is known of the people in it.
 */
public final class TranslationPrompt {

    /** The instructions. {@code %1$s} is the language, {@code %2$s} the rule of who speaks it, {@code %3$s} the rule of the script, {@code %4$s} the line about the reading. */
    static final String INSTRUCTIONS = """
            You translate the direct speech of a story into %1$s. The story is written in English. You do not rewrite the story: you only report which direct speech is spoken in %1$s, and what it is in %1$s.

            For EVERY piece of direct speech in the PART (the words the characters say aloud, inside quotation marks) give one entry, in the order they appear:
            - "quote": the speech copied exactly from the PART, character for character, without the quotation marks. A quotation that has several sentences may be one entry or one entry for each sentence; the quotes of the entries never overlap. A quote is one unbroken piece of the PART: when narration or a dialogue tag (Hiroko said.) stands between two quotation marks, they are two entries, never one quote that skips the narration.
            - "speaker": who says it.
            - "language": "%1$s" when the speech is spoken in it, or "none" when it is spoken in English or in another language that is not %1$s.
            - "text": the speech in %1$s, %3$s Do not add quotation marks: the story has them. Empty when the language is "none".
            - "reading": %4$s
            - "understood": false when the people who hear it cannot understand it, otherwise true.
            - "register": the level of speech you chose (see THE LANGUAGE), empty for the neutral one.
            - "choice": one short sentence: why this language, and the main choices of the translation.

            After the entries give "names": the names of people and places and the special terms in the speeches you wrote in %1$s that the GLOSSARY below (if there is one) does not list, and that you had to choose a written form for. One entry for each: {"source": the word as the English writes it, "written": how you wrote it, in the base form the name has in a dictionary and not in the inflected form this sentence needed, "reading": how it is read (empty when not asked), "note": a few words, may be empty}. A name the GLOSSARY lists is written exactly as the GLOSSARY says and is not repeated here. The list is empty when there is nothing to report.

            Who speaks %1$s: %2$s The text of the PART overrides this: if it says that a speech is spoken in another language ("in English", "in the common tongue"), or that someone speaks %1$s, follow the text.

            How to translate: the speech is written the way a native author would write that character saying it, not word for word. Take the voice of the speaker (age, gender, manner) and the relationship to the listener from the CHARACTERS and the story. Keep what the PART says around the speech true to the translation (a word the narration repeats, a name). Keep the names as the language asks.

            Answer with one JSON object only, no other text, in this form:
            {"translations":[{"quote":"...","speaker":"...","language":"...","text":"...","reading":"","understood":true,"register":"","choice":"..."}],"names":[{"source":"...","written":"...","reading":"","note":""}]}
            When the PART has no direct speech, the lists are empty.""";

    /** Said in the user message when the PART holds speeches that are translated already (a run that only writes the missing ones). */
    static final String PARTLY_TRANSLATED = "The PART is partly translated already: a speech that is not written in English any more is finished. Give no entry for it; "
            + "give entries only for the direct speech that is still written in English.\n";

    private TranslationPrompt() {
    }

    /**
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

    /** A glossary up to this many entries goes to the model whole (the same text for every part, so a server that caches the prompt reads it once); a bigger one only with the entries the part has. */
    public static final int WHOLE_GLOSSARY = 100;

    public static Request build(RealLanguage language, Speakers speakers, String partText, Scene scene) {
        return build(language, List.of(), speakers, partText, scene);
    }

    /**
     * @param glossary the active entries of the merged glossary (see {@link Glossary}); may be empty
     */
    public static Request build(RealLanguage language, List<GlossaryEntry> glossary, Speakers speakers, String partText, Scene scene) {
        return build(language, glossary, speakers, partText, scene, false);
    }

    /**
     * @param partText   the part as the model reads it: the source, or, for a run that writes only the missing speeches, the part as it is stored
     *                   with the translated speeches in it
     * @param translated whether {@code partText} holds speeches that are translated already; the model is told to leave them
     */
    public static Request build(RealLanguage language, List<GlossaryEntry> glossary, Speakers speakers, String partText, Scene scene, boolean translated) {
        List<GlossaryEntry> sent = glossary.size() <= WHOLE_GLOSSARY ? glossary : Glossary.relevant(glossary, partText);
        return new Request(system(language, speakers) + glossaryBlock(sent), user(language, speakers, partText, scene, translated));
    }

    static String system(RealLanguage language, Speakers speakers) {
        String tongue = language.getTongue();
        return String.format(INSTRUCTIONS, tongue, speakersRule(tongue, speakers), scriptRule(language.getScript()), readingRule(language))
                + "\n\n" + languageBlock(language);
    }

    /**
     * The glossary as the model reads it: how the names and the special words of the story are written in the language. Empty text for no entries.
     */
    public static String glossaryBlock(List<GlossaryEntry> entries) {
        if (entries == null || entries.stream().noneMatch(GlossaryEntry::isUsable)) {
            return "";
        }
        StringBuilder block = new StringBuilder("\n=== GLOSSARY ===\nThe names and special words of this story and how they are written in the language. Write each of them exactly like this "
                + "wherever it is in a speech (decline or inflect it only as the language requires, the form stays the same).\n");
        for (GlossaryEntry entry : entries) {
            if (!entry.isUsable()) {
                continue;
            }
            block.append("- ").append(entry.getSource()).append(": ");
            if (entry.getKind() == GlossaryKind.KEEP) {
                block.append("keep it as it is");
            } else {
                block.append(entry.getWritten());
                if (!entry.getReading().isEmpty()) {
                    block.append(" (read: ").append(entry.getReading()).append(')');
                }
            }
            if (!entry.getNote().isEmpty()) {
                block.append(" - ").append(entry.getNote());
            }
            block.append('\n');
        }
        return block.toString();
    }

    /**
     * What the model is told of the language: how the script, the politeness and the names are handled and anything else the user wants held to. The
     * same for every request of a book.
     */
    public static String languageBlock(RealLanguage language) {
        StringBuilder system = new StringBuilder("=== THE LANGUAGE: ").append(language.getTongue().toUpperCase()).append(" ===\n");
        if (!language.getPoliteness().isBlank()) {
            system.append("\n--- Politeness and speech level ---\n").append(language.getPoliteness().strip()).append('\n');
        }
        if (!language.getNames().isBlank()) {
            system.append("\n--- Names ---\n").append(language.getNames().strip()).append('\n');
        }
        system.append('\n').append(language.isHonorifics()
                ? "Write the honorific suffixes that go with a name (san, sama, kun, chan...) as the language does.\n"
                : "Do not write honorific suffixes after names: use the plain name and let the speech level carry the respect.\n");
        if (!language.getRules().isBlank()) {
            system.append("\n--- Other rules ---\n").append(language.getRules().strip()).append('\n');
        }
        return system.toString();
    }

    static String scriptRule(Script script) {
        return switch (script) {
            case KANA_KANJI -> "written the way a Japanese text is written: kana and kanji, with Japanese punctuation.";
            case KANA -> "written in hiragana and katakana only, no kanji, with Japanese punctuation.";
            case ROMAJI -> "written in Latin letters (Hepburn romaji), no kana and no kanji.";
            case NATIVE -> "written in the usual script of the language.";
        };
    }

    static String readingRule(RealLanguage language) {
        return language.isReading() && language.getScript() != Script.ROMAJI
                ? "how the text is read, in Latin letters (Hepburn romaji), the whole speech; empty when the language is \"none\"."
                : "always empty.";
    }

    private static String speakersRule(String tongue, Speakers speakers) {
        if (speakers.getMode() == SpeakersMode.ONLY) {
            return "only the characters the CHARACTERS list marks as speaking " + tongue + " speak it.";
        }
        return "a character speaks " + tongue + " unless the CHARACTERS list says they do not know it.";
    }

    static String user(RealLanguage language, Speakers speakers, String partText, Scene scene) {
        return user(language, speakers, partText, scene, false);
    }

    static String user(RealLanguage language, Speakers speakers, String partText, Scene scene, boolean translated) {
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
     * The characters of the notes that matter to the part: the ones called in the text, in the point of view or among the present ones. A book with
     * no notes has no list, and a character that is not in it is just a speaker of the default.
     */
    public static String characters(RealLanguage language, Speakers speakers, Scene scene, String partText) {
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
            text.append(": ").append(speaks ? "speaks " : "does NOT know ").append(language.getTongue());
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
