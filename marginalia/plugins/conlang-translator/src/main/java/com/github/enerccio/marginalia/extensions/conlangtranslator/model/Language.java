package com.github.enerccio.marginalia.extensions.conlangtranslator.model;

import com.google.gson.Gson;

import java.util.*;

/**
 * A constructed language: rules and lexicon, kept apart because they are written and edited differently. The rules are named sections the
 * author writes; the lexicon is owned by the author and written mostly by the model, in categories, with registers and variants.
 * Languages live in the library of the user ({@link com.github.enerccio.marginalia.extensions.conlangtranslator.store.ConlangStore}); a book
 * only chooses one.
 */
public class Language {

    /** The version of the document a language is exported as. */
    public static final int FORMAT = 1;

    private static final Gson GSON = new Gson();

    /** What the titles of the categories of names start with; the Name generator lists them together. */
    public static final String NAME_TITLE_PREFIX = "Conlang - ";

    private int format = FORMAT;
    private String id = UUID.randomUUID().toString();
    private String name = "";
    private String description = "";
    private List<RuleSection> rules = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private List<Register> registers = new ArrayList<>();
    private List<LexiconEntry> lexicon = new ArrayList<>();
    /** The categories of names this language gives the Name generator, each a choice of words of the lexicon. */
    private List<NameCategory> nameCategories = new ArrayList<>();
    /**
     * Whether a hyphen is a part of a word of the language (it marks a boundary or joins a morpheme and the form differs without it). Off
     * (the default): hyphens are ignored when forms are compared, so a compound written with or without them is the same word.
     */
    private boolean hyphenStructural;
    /** The example book the designer reads speeches from (uuid of a book of the user); null for none. Not exported. */
    private String exampleBook;
    /** The example lorebook the designer reads the world from (uuid of a lorebook of the user); null for none. Not exported. */
    private String exampleLorebook;

    public Language() {
    }

    public Language(String name) {
        this.name = name;
    }

    // ---- lookup

    public RuleSection findRule(String ruleId) {
        return getRules().stream().filter(rule -> rule.getId().equals(ruleId)).findFirst().orElse(null);
    }

    public Category findCategory(String categoryId) {
        return categoryId == null ? null : getCategories().stream().filter(category -> category.getId().equals(categoryId)).findFirst().orElse(null);
    }

    public Register findRegister(String registerId) {
        return registerId == null ? null : getRegisters().stream().filter(register -> register.getId().equals(registerId)).findFirst().orElse(null);
    }

    public NameCategory findNameCategory(String nameCategoryId) {
        return nameCategoryId == null ? null
                : getNameCategories().stream().filter(category -> category.getId().equals(nameCategoryId)).findFirst().orElse(null);
    }

    /** The title of the category of names in the Name generator's list: the language and the category, under the heading Conlang. */
    public String nameCategoryTitle(NameCategory category) {
        return NAME_TITLE_PREFIX + getName() + " - " + category.getName();
    }

    /**
     * The forms of the words of the category as they are now, for the Name generator: the entries the category names that still exist and are
     * not retired (an entry that was deleted or replaced is ignored, the choice is not repaired), each form once, in the order of the lexicon.
     */
    public List<String> nameWords(NameCategory category) {
        Set<String> chosen = new HashSet<>(category.getEntries());
        Set<String> forms = new LinkedHashSet<>();
        for (LexiconEntry entry : getLexicon()) {
            if (chosen.contains(entry.getId()) && entry.getStatus() != EntryStatus.RETIRED && !entry.getWord().isBlank()) {
                forms.add(entry.getWord().strip());
            }
        }
        return List.copyOf(forms);
    }

    public LexiconEntry findEntry(String entryId) {
        return getLexicon().stream().filter(entry -> entry.getId().equals(entryId)).findFirst().orElse(null);
    }

    /**
     * The entries that have the form (a form may have several entries: homophones are normal), in the order of the lexicon.
     */
    public List<LexiconEntry> findByForm(String form) {
        return getLexicon().stream().filter(entry -> entry.isForm(form, hyphenStructural)).toList();
    }

    /**
     * The entries of a category, in the order the author keeps them; null is the entries that have no (known) category.
     */
    public List<LexiconEntry> entriesOf(String categoryId) {
        if (categoryId == null || findCategory(categoryId) == null) {
            return getLexicon().stream().filter(entry -> entry.getCategory() == null || findCategory(entry.getCategory()) == null).toList();
        }
        return getLexicon().stream().filter(entry -> categoryId.equals(entry.getCategory())).toList();
    }

    /**
     * The words the author did not look at yet.
     */
    public List<LexiconEntry> newEntries() {
        return getLexicon().stream().filter(entry -> entry.getStatus() == EntryStatus.NEW).toList();
    }

    /**
     * The entries that mean the same thing as {@code entry} in another register: the entry itself and everything linked to it, directly or
     * through another entry (a link is not directional), in the order of the lexicon.
     */
    public List<LexiconEntry> variantGroup(LexiconEntry entry) {
        Map<String, LexiconEntry> byId = new LinkedHashMap<>();
        getLexicon().forEach(e -> byId.put(e.getId(), e));
        Set<String> group = new LinkedHashSet<>();
        List<String> open = new ArrayList<>(List.of(entry.getId()));
        while (!open.isEmpty()) {
            String current = open.removeLast();
            if (!group.add(current)) {
                continue;
            }
            LexiconEntry e = byId.get(current);
            if (e != null) {
                open.addAll(e.getVariants());
            }
            for (LexiconEntry other : getLexicon()) {
                if (other.getVariants().contains(current)) {
                    open.add(other.getId());
                }
            }
        }
        return getLexicon().stream().filter(e -> group.contains(e.getId())).toList();
    }

    /**
     * The repeats: entries with the same form that share a meaning (the same gloss twice, the same word listed in two places). The same
     * form with different meanings is a homophone and is normal, never listed. Each group has two entries or more, in lexicon order.
     */
    public List<List<LexiconEntry>> repeats() {
        Map<String, List<LexiconEntry>> byForm = new LinkedHashMap<>();
        for (LexiconEntry entry : getLexicon()) {
            String form = LexiconEntry.normalize(entry.getWord(), hyphenStructural);
            if (!form.isEmpty()) {
                byForm.computeIfAbsent(form, f -> new ArrayList<>()).add(entry);
            }
        }
        List<List<LexiconEntry>> repeats = new ArrayList<>();
        for (List<LexiconEntry> sameForm : byForm.values()) {
            if (sameForm.size() < 2) {
                continue;
            }
            List<LexiconEntry> repeated = new ArrayList<>();
            for (LexiconEntry entry : sameForm) {
                boolean sharesMeaning = sameForm.stream().anyMatch(other -> other != entry
                        && entry.getSenses().stream().anyMatch(sense -> other.hasGloss(sense.getGloss())));
                if (sharesMeaning) {
                    repeated.add(entry);
                }
            }
            if (repeated.size() > 1) {
                repeats.add(repeated);
            }
        }
        return repeats;
    }

    // ---- change

    /**
     * Adds a word at the end of the lexicon (so a new word changes a request only from its place on). The same form with another meaning is
     * a homophone, added without a warning.
     */
    public LexiconEntry addEntry(LexiconEntry entry) {
        getLexicon().add(entry);
        return entry;
    }

    /**
     * Removes the word and the links that other words had to it.
     *
     * @return whether the language had it
     */
    public boolean deleteEntry(String entryId) {
        boolean removed = getLexicon().removeIf(entry -> entry.getId().equals(entryId));
        if (removed) {
            getLexicon().forEach(entry -> entry.getVariants().remove(entryId));
        }
        return removed;
    }

    /**
     * Removes the category; its words stay, without a category.
     */
    public boolean deleteCategory(String categoryId) {
        boolean removed = getCategories().removeIf(category -> category.getId().equals(categoryId));
        if (removed) {
            getLexicon().stream().filter(entry -> categoryId.equals(entry.getCategory())).forEach(entry -> entry.setCategory(null));
        }
        return removed;
    }

    /**
     * Removes the register; the words in it are neutral again.
     */
    public boolean deleteRegister(String registerId) {
        boolean removed = getRegisters().removeIf(register -> register.getId().equals(registerId));
        if (removed) {
            getLexicon().stream().filter(entry -> registerId.equals(entry.getRegister())).forEach(entry -> entry.setRegister(null));
        }
        return removed;
    }

    // ---- copies

    /**
     * An independent copy under another name, with its own id: the rules, categories, registers, lexicon and the links to the
     * example book and lorebook (a copy inside the user's own library keeps them). The ids inside stay, they only mean something inside
     * the language.
     */
    public Language duplicate(String newName) {
        Language copy = GSON.fromJson(GSON.toJsonTree(this), Language.class);
        copy.id = UUID.randomUUID().toString();
        copy.name = newName;
        return copy;
    }

    /**
     * The language as it is shared with another user or installation: the rules, categories, registers and lexicon, without the
     * links to the example book and lorebook (their uuids mean nothing elsewhere).
     */
    public Language forExport() {
        Language copy = GSON.fromJson(GSON.toJsonTree(this), Language.class);
        copy.format = FORMAT;
        copy.exampleBook = null;
        copy.exampleLorebook = null;
        return copy;
    }

    /**
     * What an imported document becomes in the library: its own id (so it never replaces a language), no links to the example book and
     * lorebook. A document of a newer format is refused.
     *
     * @throws IllegalArgumentException when the document is of a newer format
     */
    public Language forImport() {
        if (format > FORMAT) {
            throw new IllegalArgumentException("The language is of a newer format (" + format + ")");
        }
        Language copy = forExport();
        copy.id = UUID.randomUUID().toString();
        return copy;
    }

    // ---- accessors

    public int getFormat() {
        return format;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<RuleSection> getRules() {
        if (rules == null) {
            rules = new ArrayList<>();
        }
        return rules;
    }

    public List<Category> getCategories() {
        if (categories == null) {
            categories = new ArrayList<>();
        }
        return categories;
    }

    public List<Register> getRegisters() {
        if (registers == null) {
            registers = new ArrayList<>();
        }
        return registers;
    }

    public List<LexiconEntry> getLexicon() {
        if (lexicon == null) {
            lexicon = new ArrayList<>();
        }
        return lexicon;
    }

    public List<NameCategory> getNameCategories() {
        if (nameCategories == null) {
            nameCategories = new ArrayList<>();
        }
        return nameCategories;
    }

    public boolean isHyphenStructural() {
        return hyphenStructural;
    }

    public void setHyphenStructural(boolean hyphenStructural) {
        this.hyphenStructural = hyphenStructural;
    }

    public String getExampleBook() {
        return exampleBook;
    }

    public void setExampleBook(String exampleBook) {
        this.exampleBook = exampleBook;
    }

    public String getExampleLorebook() {
        return exampleLorebook;
    }

    public void setExampleLorebook(String exampleLorebook) {
        this.exampleLorebook = exampleLorebook;
    }
}
