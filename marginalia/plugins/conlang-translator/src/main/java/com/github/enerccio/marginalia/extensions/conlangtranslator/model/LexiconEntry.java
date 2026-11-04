package com.github.enerccio.marginalia.extensions.conlangtranslator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * A word of a language: its form (romanized, and the script form when there is one), its category, its senses, and why it is as it is.
 * The author owns the lexicon but the model writes most of it: a word the speech needs and the lexicon lacks is invented by the rules,
 * reported with its rationale, added at once and marked {@link EntryStatus#NEW} until the author looks at it.
 */
public class LexiconEntry {

    private String id = UUID.randomUUID().toString();
    /** The word, romanized as it is written in the story. */
    private String word = "";
    /** The word in the script of the language, when it has one. */
    private String script = "";
    /** The {@link Category} (id); null when it has none. */
    private String category;
    private EntryKind kind = EntryKind.ROOT;
    private List<Sense> senses = new ArrayList<>();
    /** Why the word is as it is: which rule or root, a sound chosen for the mood. The model writes it, the author may edit it, and it goes back into later requests. */
    private String rationale = "";
    /** The rule (title of a section) the word follows, when the model named one. */
    private String rule = "";
    private String notes = "";
    private Origin origin = Origin.author();
    private EntryStatus status = EntryStatus.NEW;
    /** The {@link Register} (id); null is the neutral one. */
    private String register;
    /** The entries (ids) that mean the same thing in another register: formal, soft, vulgar forms of one concept. */
    private List<String> variants = new ArrayList<>();

    public LexiconEntry() {
    }

    /**
     * A word of the author (accepted, not new).
     */
    public static LexiconEntry ofAuthor(String word, String gloss) {
        LexiconEntry entry = new LexiconEntry();
        entry.word = word;
        entry.getSenses().add(new Sense(gloss));
        entry.status = EntryStatus.ACCEPTED;
        return entry;
    }

    /**
     * A word the model invented and reported: new, with the rationale it gave.
     */
    public static LexiconEntry ofModel(String word, String gloss, String rationale, Origin origin) {
        LexiconEntry entry = new LexiconEntry();
        entry.word = word;
        entry.getSenses().add(new Sense(gloss));
        entry.rationale = rationale;
        entry.origin = origin;
        entry.status = EntryStatus.NEW;
        return entry;
    }

    /**
     * Whether {@code form} is this word, ignoring case and the spaces around it, and ignoring hyphens unless the language needs them
     * (see {@link Language#isHyphenStructural()}).
     */
    public boolean isForm(String form, boolean hyphenStructural) {
        String mine = normalize(word, hyphenStructural);
        return !mine.isEmpty() && mine.equals(normalize(form, hyphenStructural));
    }

    /**
     * The form compared by: lower case, no spaces around, and no hyphens unless they are structural in the language: a language whose
     * compounds are written with or without them (Vrakht) compares without, one where a hyphen joins a morpheme or marks a boundary keeps it.
     */
    public static String normalize(String form, boolean hyphenStructural) {
        String normalized = form == null ? "" : form.strip().toLowerCase(Locale.ROOT);
        return hyphenStructural ? normalized : normalized.replace("-", "");
    }

    /**
     * Whether the entry has a sense with this gloss (ignoring case and the spaces around it): the same meaning twice is a repeat.
     */
    public boolean hasGloss(String gloss) {
        String wanted = gloss == null ? "" : gloss.strip().toLowerCase(Locale.ROOT);
        return !wanted.isEmpty() && getSenses().stream().anyMatch(sense -> sense.getGloss().strip().toLowerCase(Locale.ROOT).equals(wanted));
    }

    /**
     * The author changed the word: a word of the model becomes the author's, and one the author did not look at is accepted. A locked
     * one stays locked.
     */
    public void editedByAuthor() {
        origin = Origin.author();
        if (status == EntryStatus.NEW) {
            status = EntryStatus.ACCEPTED;
        }
    }

    public boolean isLocked() {
        return getStatus() == EntryStatus.LOCKED;
    }

    public boolean isOffered() {
        return getStatus().isOffered();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWord() {
        return word == null ? "" : word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getScript() {
        return script == null ? "" : script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public EntryKind getKind() {
        return kind == null ? EntryKind.ROOT : kind;
    }

    public void setKind(EntryKind kind) {
        this.kind = kind;
    }

    public List<Sense> getSenses() {
        if (senses == null) {
            senses = new ArrayList<>();
        }
        return senses;
    }

    public void setSenses(List<Sense> senses) {
        this.senses = senses;
    }

    public String getRationale() {
        return rationale == null ? "" : rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public String getRule() {
        return rule == null ? "" : rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getNotes() {
        return notes == null ? "" : notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Origin getOrigin() {
        if (origin == null) {
            origin = Origin.author();
        }
        return origin;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    public EntryStatus getStatus() {
        return status == null ? EntryStatus.NEW : status;
    }

    public void setStatus(EntryStatus status) {
        this.status = status;
    }

    public String getRegister() {
        return register;
    }

    public void setRegister(String register) {
        this.register = register;
    }

    public List<String> getVariants() {
        if (variants == null) {
            variants = new ArrayList<>();
        }
        return variants;
    }

    public void setVariants(List<String> variants) {
        this.variants = variants;
    }
}
