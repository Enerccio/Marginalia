package com.github.enerccio.marginalia.extensions.conlangtranslator.designer;

import com.github.enerccio.marginalia.extensions.conlangtranslator.designer.DesignerAnswer.Section;
import com.github.enerccio.marginalia.extensions.conlangtranslator.designer.DesignerAnswer.Word;
import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.LanguageDiff;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.store.ConlangStore;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.AnswerChecker;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.NewWords;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.TranslationAnswer.NewWord;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Turns the answer of the designer into a proposal: the language with the answer in it, and the difference between it and the language as a change
 * set that is reviewed and applied like any other ({@link LanguageDiff}). A proposal is never a change: nothing is written to the language.
 * <p>
 * What is kept: a section is new, or rewrites the one of its title; a word needs a form and a meaning, and a word that the language has (the same
 * form and a shared meaning) is left out. New words are what the model made: origin <i>model</i>, status <i>new</i>.
 */
public final class ProposalBuilder {

    private ProposalBuilder() {
    }

    /**
     * @param current the language the proposal is for, which is not changed
     * @param origin  who made the words: the model and the time
     */
    public static Proposal build(Language current, DesignerAnswer answer, Origin origin) {
        Language incoming = ConlangStore.copy(current);
        List<String> dropped = new ArrayList<>();
        for (Section section : answer.sections()) {
            section(incoming, section, dropped);
        }
        List<AnswerChecker.Word> words = new ArrayList<>();
        for (Word word : answer.words()) {
            if (word.word() == null || word.word().isBlank() || word.gloss() == null || word.gloss().isBlank()) {
                dropped.add(describe(word) + ": no form or no meaning");
            } else if (hasWord(incoming, word, words)) {
                dropped.add(describe(word) + ": the language has this word with this meaning already");
            } else {
                words.add(new AnswerChecker.Word(new NewWord(word.word().strip(), word.gloss().strip(), word.category(), word.rationale(), word.register(),
                        word.kind(), word.variantOf()), List.of()));
            }
        }
        NewWords.addTo(incoming, words, origin);
        var set = LanguageDiff.compare(current, incoming);
        set.pickAdditions();
        return new Proposal(set, incoming, answer.message(), dropped);
    }

    private static void section(Language incoming, Section proposed, List<String> dropped) {
        if (proposed.title() == null || proposed.title().isBlank() || proposed.text() == null || proposed.text().isBlank()) {
            dropped.add("a section with no title or no text");
            return;
        }
        Audience audience = audience(proposed.audience());
        String target = proposed.replaces() != null && !proposed.replaces().isBlank() ? proposed.replaces() : null;
        RuleSection existing = target == null ? null : incoming.getRules().stream()
                .filter(section -> section.getTitle().strip().equalsIgnoreCase(target.strip())).findFirst().orElse(null);
        if (existing == null && target == null) {
            // a "new" section that has the title of an existing one is a rewrite of it
            existing = incoming.getRules().stream().filter(section -> section.getTitle().strip().equalsIgnoreCase(proposed.title().strip())).findFirst().orElse(null);
        }
        if (existing != null) {
            existing.setText(proposed.text().strip());
            if (proposed.audience() != null && !proposed.audience().isBlank()) {
                existing.setAudience(audience);
            }
        } else {
            incoming.getRules().add(new RuleSection(proposed.title().strip(), proposed.text().strip(), audience));
        }
    }

    private static Audience audience(String name) {
        if (name == null) {
            return Audience.MODEL;
        }
        return switch (name.strip().toLowerCase(Locale.ROOT)) {
            case "designer" -> Audience.DESIGNER;
            case "nobody", "none" -> Audience.NONE;
            default -> Audience.MODEL;
        };
    }

    /** The same form with a shared meaning, in the language or among the words already accepted from the answer. */
    private static boolean hasWord(Language language, Word word, List<AnswerChecker.Word> accepted) {
        boolean hyphen = language.isHyphenStructural();
        for (LexiconEntry entry : language.getLexicon()) {
            if (entry.isForm(word.word(), hyphen) && entry.hasGloss(word.gloss())) {
                return true;
            }
        }
        return accepted.stream().anyMatch(other -> LexiconEntry.normalize(other.word().word(), hyphen).equals(LexiconEntry.normalize(word.word(), hyphen))
                && other.word().gloss().strip().equalsIgnoreCase(word.gloss().strip()));
    }

    private static String describe(Word word) {
        return (word.word() == null ? "?" : word.word()) + (word.gloss() == null ? "" : " (" + word.gloss() + ")");
    }
}
