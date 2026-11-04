package com.github.enerccio.marginalia.extensions.conlangtranslator.designer;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.LexiconText;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.TranslationPrompt.Request;

import java.util.List;

/**
 * The requests of the designer: each action of the editor and of the wizard is one request that carries the language as it is (the rules, the
 * categories, the registers and the lexicon), so what the model proposes agrees with what exists. The answer is always the JSON object of
 * {@link DesignerAnswer}; what an action asks for is in the user message.
 */
public final class DesignerPrompts {

    /** The size above which the lexicon is cut down in a request of the designer (characters). */
    public static final int MAX_LEXICON_CHARS = 40000;

    private static final String INSTRUCTIONS = """
            You are a designer of constructed languages. You work with an author who writes fiction in English and has characters who speak a
            language of their own; the story shows that language in the Latin alphabet. You propose, the author decides: never claim that anything
            is final. Follow the rules of the language as they are written below, never contradict them, and when you must add to them, say what
            you add. Keep every word pronounceable for an English reader and spelled in the way the rules say.

            Answer with ONE JSON object and nothing else, in this shape (leave out nothing; use empty arrays and empty strings for what the task
            does not ask):
            {"message": "what the author should read: why, in a few sentences",
             "sections": [{"title": "...", "text": "...", "audience": "translator|designer|nobody", "replaces": "title of the section this rewrites, or empty"}],
             "words": [{"word": "...", "gloss": "...", "category": "...", "rationale": "the rule or the roots, in one sentence", "register": "", "kind": "root|compound|affix|particle|name|number|phrase|other", "variantOf": ""}],
             "findings": [{"severity": "error|warning|note", "where": "...", "problem": "...", "fix": "..."}],
             "samples": [{"source": "...", "text": "...", "note": "..."}]}
            """;

    private DesignerPrompts() {
    }

    // ---- the actions

    /**
     * New words for a category (null: the words are not for a category, the model names them).
     *
     * @param count how many words, at least 1
     * @param note  what the author wants, may be empty
     * @param lore  the entries of the lorebook of the language to fit the world, may be empty
     */
    public static Request words(Language language, String category, int count, String note, String lore) {
        StringBuilder task = new StringBuilder("TASK: invent ").append(Math.max(1, count)).append(" new words");
        if (category != null && !category.isBlank()) {
            task.append(" for the category \"").append(category.strip()).append("\" (use exactly this name as their category)");
        }
        task.append(". Return them in \"words\" only. Every word needs a gloss and a rationale of one sentence: the roots and the rule it is made of, or, for a new root, the sounds you chose and why they suit the "
                + "meaning. \"New root\" alone is not a rationale. ")
                .append("Do not give a meaning that the lexicon has already; the same form for a different meaning is allowed but say so in the rationale. ")
                .append("Do not repeat a word that exists.");
        return new Request(system(language, lore), task + notePart(note));
    }

    /**
     * A new section of the rules for something that was missed (a numbering system, colors, politeness...), and the words it needs.
     */
    public static Request section(Language language, String note, String lore) {
        String task = "TASK: write a NEW section of the rules for the following, and the words that it needs. The section is a title and its text in \"sections\" "
                + "(replaces empty); the words are in \"words\". It must agree with the rules and the words that exist. Be concrete: give the forms, "
                + "not a description of what forms could be.";
        return new Request(system(language, lore), task + notePart(note));
    }

    /**
     * A rewrite of a section of the rules.
     */
    public static Request rewrite(Language language, RuleSection section, String note) {
        String task = "TASK: rewrite the section \"" + section.getTitle() + "\" of the rules as asked below. Return the whole new section in \"sections\" with "
                + "\"replaces\" set to \"" + section.getTitle() + "\". Keep what is right in it; change only what the note asks, and say in \"message\" what "
                + "you changed.\n\nTHE SECTION NOW:\n" + section.getText().strip();
        return new Request(system(language, ""), task + notePart(note));
    }

    /**
     * A review of the rules and the lexicon: findings only.
     */
    public static Request review(Language language) {
        String task = "TASK: review the language. Return your findings in \"findings\" and nothing else besides \"message\": contradictions between sections, "
                + "things the rules promise and do not define, words that break the sound system or the spelling, two words for one meaning, gaps "
                + "that a translator will hit. Name the section or the word in \"where\" and give a concrete \"fix\". If there is nothing wrong, say so in \"message\".";
        return new Request(system(language, ""), task);
    }

    /**
     * A free question of the author; the answer is in "message" and may come with sections or words to apply.
     *
     * @param history the earlier exchanges of the conversation, oldest first, each as {@code Author: ...} or {@code Designer: ...}
     */
    public static Request ask(Language language, String question, List<String> history, String lore) {
        StringBuilder user = new StringBuilder();
        if (history != null && !history.isEmpty()) {
            user.append("THE CONVERSATION SO FAR:\n");
            history.forEach(line -> user.append(line.strip()).append("\n"));
            user.append("\n");
        }
        user.append("TASK: answer the author in \"message\". Add \"sections\" or \"words\" only if the author asks for a change or your answer needs one.\n\nAUTHOR: ")
                .append(question == null ? "" : question.strip());
        return new Request(system(language, lore), user.toString());
    }

    /**
     * The proposal of one stage of the wizard: a rules section.
     *
     * @param brief    what the language is for, as the author wrote it
     * @param previous the proposal the author is refining (title and text), empty for the first one
     * @param note     what the author wants changed or added, may be empty
     */
    public static Request stage(Language language, DesignerStage stage, String brief, String previous, String note, String lore) {
        StringBuilder task = new StringBuilder("TASK: propose the section \"").append(stage.title()).append("\" of the rules: ").append(stage.about())
                .append(". Return exactly one section in \"sections\" with the title \"").append(stage.title())
                .append("\" and \"replaces\" empty; no words yet. The text is read by a translator in every request and must be enough to follow without "
                        + "guessing: give the forms themselves (a list or a table where it helps) and two or three examples, in about 200 to 500 words.");
        if (previous != null && !previous.isBlank()) {
            task.append("\n\nTHE PROPOSAL THE AUTHOR IS REFINING (keep what is right, change what the note asks):\n").append(previous.strip());
        }
        StringBuilder user = new StringBuilder();
        if (brief != null && !brief.isBlank()) {
            user.append("THE BRIEF (what the language is for):\n").append(brief.strip()).append("\n\n");
        }
        return new Request(system(language, lore), user + task.toString() + notePart(note));
    }

    /**
     * The sentences of the author translated into the language, for the author to read; words the sentences need that the lexicon lacks are new words.
     */
    public static Request samples(Language language, List<String> sentences) {
        StringBuilder user = new StringBuilder("TASK: translate each sentence into the language, using the lexicon and the rules; return them in \"samples\" in the order given, "
                + "with a note on the choices that were hard. A word the lexicon lacks is invented by the rules and listed in \"words\".\n\nSENTENCES:\n");
        int number = 1;
        for (String sentence : sentences) {
            user.append(number++).append(". ").append(sentence.strip()).append('\n');
        }
        return new Request(system(language, ""), user.toString());
    }

    // ---- the context

    private static String notePart(String note) {
        return note == null || note.isBlank() ? "" : "\n\nTHE AUTHOR'S NOTE:\n" + note.strip();
    }

    /** The instructions and the language as it is now. */
    static String system(Language language, String lore) {
        StringBuilder system = new StringBuilder(INSTRUCTIONS);
        system.append("\n=== THE LANGUAGE: ").append(language.getName().isBlank() ? "(not named yet)" : language.getName().toUpperCase()).append(" ===\n");
        if (!language.getDescription().isBlank()) {
            system.append(language.getDescription().strip()).append("\n");
        }
        if (language.getRules().stream().noneMatch(section -> section.getAudience() != Audience.NONE && !section.getText().isBlank())) {
            system.append("\n(The language has no rules yet.)\n");
        }
        for (RuleSection section : language.getRules()) {
            if (section.getAudience() != Audience.NONE && !section.getText().isBlank()) {
                system.append("\n--- ").append(section.getTitle().isBlank() ? "Rules" : section.getTitle()).append(" ---\n").append(section.getText().strip()).append('\n');
            }
        }
        if (language.isHyphenStructural()) {
            system.append("\nA hyphen is a part of a word of this language.\n");
        }
        if (!language.getRegisters().isEmpty()) {
            system.append("\n=== REGISTERS ===\n");
            for (Register register : language.getRegisters()) {
                system.append(register.getName()).append(register.getNote().isBlank() ? "" : " - " + register.getNote().strip()).append('\n');
            }
        }
        if (!language.getCategories().isEmpty()) {
            system.append("\n=== CATEGORIES ===\n");
            for (Category category : language.getCategories()) {
                system.append(category.getName()).append(category.getNote().isBlank() ? "" : " - " + category.getNote().strip()).append('\n');
            }
        }
        if (!language.getLexicon().isEmpty()) {
            system.append("\n=== LEXICON ===\n").append(LexiconText.select(language, null, MAX_LEXICON_CHARS)).append('\n');
        }
        if (lore != null && !lore.isBlank()) {
            system.append("\n=== THE WORLD (entries of the author's lorebook: the people, the places) ===\n").append(lore.strip()).append('\n');
        }
        return system.toString();
    }
}
