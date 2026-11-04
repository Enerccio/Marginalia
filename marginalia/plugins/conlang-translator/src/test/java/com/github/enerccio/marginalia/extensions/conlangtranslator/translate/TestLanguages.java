package com.github.enerccio.marginalia.extensions.conlangtranslator.translate;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;

/**
 * Small languages for the tests: a rigid, military one in the way of Vrakht (mood particles, role particles, compounds) and the registers of
 * Aethel-Vael (one concept, three forms).
 */
final class TestLanguages {

    private TestLanguages() {
    }

    static LexiconEntry word(Language language, Category category, String form, String gloss) {
        LexiconEntry entry = LexiconEntry.ofAuthor(form, gloss);
        entry.setCategory(category == null ? null : category.getId());
        return language.addEntry(entry);
    }

    static Language vrakht() {
        Language language = new Language("Vrakht");
        language.setDescription("The speech of the empire.");
        language.getRules().add(new RuleSection("Grammar", "A speech starts with a mood particle: Da (statement), Frag (question), Bef (command)."));
        language.getRules().add(new RuleSection("History", "Made by soldiers.", Audience.DESIGNER));
        language.getRules().add(new RuleSection("Notes", "private notes", Audience.NONE));
        Category particles = new Category("Particles");
        particles.setAlwaysSend(true);
        Category war = new Category("Military & War");
        Category people = new Category("People & Social");
        language.getCategories().addAll(java.util.List.of(particles, war, people));
        word(language, particles, "Da", "statement mood");
        word(language, particles, "Frag", "question mood");
        word(language, particles, "Bef", "command mood");
        word(language, particles, "ge", "role: object");
        word(language, war, "drak", "war").getSenses().add(new Sense("fight", "verb"));
        word(language, war, "karn", "field");
        word(language, war, "vek", "enemy");
        word(language, war, "ja", "I");
        word(language, war, "se", "see");
        word(language, people, "blod", "blood").getSenses().add(new Sense("lineage"));
        word(language, people, "frou", "woman");
        word(language, people, "gild", "trade");
        return language;
    }

    /** Three forms of one concept; the suffix -is is the plural, Va- the future. */
    static Language aethel() {
        Language language = new Language("Aethel-Vael");
        Register formal = new Register("formal", "healers and nobles");
        Register soft = new Register("soft", "lovers");
        Register vulgar = new Register("vulgar", "soldiers");
        language.getRegisters().addAll(java.util.List.of(formal, soft, vulgar));
        Category body = new Category("Body");
        Category grammar = new Category("Grammar");
        language.getCategories().addAll(java.util.List.of(body, grammar));
        LexiconEntry mela = word(language, body, "mela", "breast");
        mela.setRegister(formal.getId());
        LexiconEntry melis = word(language, body, "melis", "breast");
        melis.setRegister(soft.getId());
        LexiconEntry lelya = word(language, body, "lelya", "breast");
        lelya.setRegister(vulgar.getId());
        mela.getVariants().add(melis.getId());
        melis.getVariants().add(lelya.getId());
        word(language, body, "silva", "tree");
        LexiconEntry plural = word(language, grammar, "-is", "plural");
        plural.setKind(EntryKind.AFFIX);
        LexiconEntry future = word(language, grammar, "Va-", "future");
        future.setKind(EntryKind.AFFIX);
        return language;
    }
}
