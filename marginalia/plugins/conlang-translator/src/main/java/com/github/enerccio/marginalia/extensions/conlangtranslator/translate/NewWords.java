package com.github.enerccio.marginalia.extensions.conlangtranslator.translate;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.AnswerChecker.Word;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.TranslationAnswer.NewWord;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Puts the words the model reported into the lexicon at once, new, so that the next request says the same thing the same way. The category
 * is found by its name (a name nobody has makes a category), so is the register; the word it is another register of becomes a variant of
 * it. The words come from {@link AnswerChecker}, which has left out what the lexicon has already.
 */
public final class NewWords {

    private NewWords() {
    }

    /**
     * @param origin who made them: the model, the part and the time
     * @return the entries that were added, in the order of the answer
     */
    public static List<LexiconEntry> addTo(Language language, List<Word> words, Origin origin) {
        List<LexiconEntry> added = new ArrayList<>();
        for (Word reported : words) {
            NewWord word = reported.word();
            LexiconEntry entry = LexiconEntry.ofModel(word.word().strip(), word.gloss().strip(), text(word.rationale()), origin);
            entry.setCategory(categoryId(language, word.category()));
            entry.setKind(kind(word.kind()));
            Register register = register(language, word.register());
            entry.setRegister(register == null ? null : register.getId());
            language.addEntry(entry);
            LexiconEntry variantOf = word.variantOf() == null || word.variantOf().isBlank() ? null
                    : language.findByForm(word.variantOf()).stream().filter(other -> other != entry).findFirst().orElse(null);
            if (variantOf != null) {
                entry.getVariants().add(variantOf.getId());
            }
            added.add(entry);
        }
        return added;
    }

    private static String text(String text) {
        return text == null ? "" : text.strip();
    }

    private static String categoryId(Language language, String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String wanted = name.strip();
        return language.getCategories().stream().filter(category -> category.getName().strip().equalsIgnoreCase(wanted)).findFirst()
                .orElseGet(() -> {
                    Category category = new Category(wanted);
                    language.getCategories().add(category);
                    return category;
                }).getId();
    }

    private static Register register(Language language, String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return language.getRegisters().stream().filter(register -> register.getName().strip().equalsIgnoreCase(name.strip())).findFirst().orElse(null);
    }

    private static EntryKind kind(String name) {
        if (name == null || name.isBlank()) {
            return EntryKind.ROOT;
        }
        try {
            return EntryKind.valueOf(name.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return EntryKind.OTHER;
        }
    }
}
