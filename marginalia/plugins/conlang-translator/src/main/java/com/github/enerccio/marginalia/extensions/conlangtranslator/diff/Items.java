package com.github.enerccio.marginalia.extensions.conlangtranslator.diff;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * How the diff reads and writes the things of a language: for each {@link Target} its list, its identity, its text fields and its list fields.
 * Everything the diff knows about the model is here.
 */
final class Items {

    private static final Gson GSON = new Gson();

    /** Separates the parts of a sense inside the text of a list element. */
    static final char SEP = '\t';

    private Items() {
    }

    abstract static class Access<T> {

        final Target target;

        Access(Target target) {
            this.target = target;
        }

        abstract List<T> items(Language language);

        abstract String id(T item);

        abstract String label(T item);

        /** The text fields of the item, in the order they are shown. */
        abstract Map<String, String> text(T item);

        abstract void setText(T item, String field, String value);

        /** The list fields of the item: one text per element. */
        Map<String, List<String>> lists(T item) {
            return Map.of();
        }

        void setList(T item, String field, List<String> values) {
        }

        /** The identity of an element of a list field (a sense is the same sense by its meaning). */
        String elementKey(String field, String element) {
            return element;
        }

        /** Whether the order of the items is the author's and a change of it is a change. */
        boolean ordered() {
            return false;
        }

        /** Takes the item out of the language, cleaning what referred to it. */
        void remove(Language language, T item) {
            items(language).remove(item);
        }

        /** Puts the item into the language after the item with the id {@code anchor} (null: first), at the end when there is no such item. */
        void insert(Language language, T item, String anchor) {
            List<T> list = items(language);
            if (anchor == null) {
                list.addFirst(item);
                return;
            }
            for (int i = 0; i < list.size(); i++) {
                if (id(list.get(i)).equals(anchor)) {
                    list.add(i + 1, item);
                    return;
                }
            }
            list.add(item);
        }

        abstract Class<T> type();

        T copy(T item) {
            return GSON.fromJson(GSON.toJsonTree(item), type());
        }
    }

    static Access<?> of(Target target) {
        return switch (target) {
            case LANGUAGE -> LANGUAGE;
            case RULE -> RULES;
            case CATEGORY -> CATEGORIES;
            case REGISTER -> REGISTERS;
            case WORD -> WORDS;
            case NAME_CATEGORY -> NAME_CATEGORIES;
        };
    }

    private static String s(String value) {
        return value == null ? "" : value;
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String value, E fallback) {
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    // ---- the language itself

    static final Access<Language> LANGUAGE = new Access<>(Target.LANGUAGE) {
        @Override
        List<Language> items(Language language) {
            return List.of(language);
        }

        @Override
        String id(Language item) {
            return "language";
        }

        @Override
        String label(Language item) {
            return item.getName();
        }

        @Override
        Map<String, String> text(Language item) {
            Map<String, String> map = new LinkedHashMap<>();
            // the name is not here: it is what the library knows the language by, a comparison never renames
            map.put("description", item.getDescription());
            map.put("hyphenStructural", String.valueOf(item.isHyphenStructural()));
            return map;
        }

        @Override
        void setText(Language item, String field, String value) {
            switch (field) {
                case "description" -> item.setDescription(value);
                case "hyphenStructural" -> item.setHyphenStructural(Boolean.parseBoolean(value));
                default -> throw new IllegalArgumentException(field);
            }
        }

        @Override
        Class<Language> type() {
            return Language.class;
        }
    };

    // ---- the rules

    static final Access<RuleSection> RULES = new Access<>(Target.RULE) {
        @Override
        List<RuleSection> items(Language language) {
            return language.getRules();
        }

        @Override
        String id(RuleSection item) {
            return item.getId();
        }

        @Override
        String label(RuleSection item) {
            return item.getTitle();
        }

        @Override
        Map<String, String> text(RuleSection item) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("title", item.getTitle());
            map.put("audience", item.getAudience().name());
            map.put("text", item.getText());
            return map;
        }

        @Override
        void setText(RuleSection item, String field, String value) {
            switch (field) {
                case "title" -> item.setTitle(value);
                case "audience" -> item.setAudience(parse(Audience.class, value, Audience.MODEL));
                case "text" -> item.setText(value);
                default -> throw new IllegalArgumentException(field);
            }
        }

        @Override
        boolean ordered() {
            return true;
        }

        @Override
        Class<RuleSection> type() {
            return RuleSection.class;
        }
    };

    // ---- the categories

    static final Access<Category> CATEGORIES = new Access<>(Target.CATEGORY) {
        @Override
        List<Category> items(Language language) {
            return language.getCategories();
        }

        @Override
        String id(Category item) {
            return item.getId();
        }

        @Override
        String label(Category item) {
            return item.getName();
        }

        @Override
        Map<String, String> text(Category item) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("name", item.getName());
            map.put("note", item.getNote());
            map.put("alwaysSend", String.valueOf(item.isAlwaysSend()));
            return map;
        }

        @Override
        void setText(Category item, String field, String value) {
            switch (field) {
                case "name" -> item.setName(value);
                case "note" -> item.setNote(value);
                case "alwaysSend" -> item.setAlwaysSend(Boolean.parseBoolean(value));
                default -> throw new IllegalArgumentException(field);
            }
        }

        @Override
        boolean ordered() {
            return true;
        }

        @Override
        void remove(Language language, Category item) {
            language.deleteCategory(item.getId());
        }

        @Override
        Class<Category> type() {
            return Category.class;
        }
    };

    // ---- the registers

    static final Access<Register> REGISTERS = new Access<>(Target.REGISTER) {
        @Override
        List<Register> items(Language language) {
            return language.getRegisters();
        }

        @Override
        String id(Register item) {
            return item.getId();
        }

        @Override
        String label(Register item) {
            return item.getName();
        }

        @Override
        Map<String, String> text(Register item) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("name", item.getName());
            map.put("note", item.getNote());
            return map;
        }

        @Override
        void setText(Register item, String field, String value) {
            switch (field) {
                case "name" -> item.setName(value);
                case "note" -> item.setNote(value);
                default -> throw new IllegalArgumentException(field);
            }
        }

        @Override
        boolean ordered() {
            return true;
        }

        @Override
        void remove(Language language, Register item) {
            language.deleteRegister(item.getId());
        }

        @Override
        Class<Register> type() {
            return Register.class;
        }
    };

    // ---- the words

    static final Access<LexiconEntry> WORDS = new Access<>(Target.WORD) {
        @Override
        List<LexiconEntry> items(Language language) {
            return language.getLexicon();
        }

        @Override
        String id(LexiconEntry item) {
            return item.getId();
        }

        @Override
        String label(LexiconEntry item) {
            String glosses = item.getSenses().stream().map(Sense::getGloss).filter(StringUtils::isNotBlank).collect(Collectors.joining("; "));
            return glosses.isEmpty() ? item.getWord() : item.getWord() + " (" + glosses + ")";
        }

        @Override
        Map<String, String> text(LexiconEntry item) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("word", item.getWord());
            map.put("script", item.getScript());
            map.put("category", s(item.getCategory()));
            map.put("kind", item.getKind().name());
            map.put("register", s(item.getRegister()));
            map.put("status", item.getStatus().name());
            map.put("rationale", item.getRationale());
            map.put("rule", item.getRule());
            map.put("notes", item.getNotes());
            return map;
        }

        @Override
        void setText(LexiconEntry item, String field, String value) {
            switch (field) {
                case "word" -> item.setWord(value);
                case "script" -> item.setScript(value);
                case "category" -> item.setCategory(StringUtils.trimToNull(value));
                case "kind" -> item.setKind(parse(EntryKind.class, value, EntryKind.ROOT));
                case "register" -> item.setRegister(StringUtils.trimToNull(value));
                case "status" -> item.setStatus(parse(EntryStatus.class, value, EntryStatus.NEW));
                case "rationale" -> item.setRationale(value);
                case "rule" -> item.setRule(value);
                case "notes" -> item.setNotes(value);
                default -> throw new IllegalArgumentException(field);
            }
        }

        @Override
        Map<String, List<String>> lists(LexiconEntry item) {
            Map<String, List<String>> map = new LinkedHashMap<>();
            map.put("senses", item.getSenses().stream().map(sense -> sense.getGloss() + SEP + sense.getPartOfSpeech() + SEP + sense.getNote()).toList());
            map.put("variants", new ArrayList<>(item.getVariants()));
            return map;
        }

        @Override
        void setList(LexiconEntry item, String field, List<String> values) {
            if ("senses".equals(field)) {
                List<Sense> senses = new ArrayList<>();
                for (String value : values) {
                    String[] parts = value.split(String.valueOf(SEP), -1);
                    Sense sense = new Sense(parts[0], parts.length > 1 ? parts[1] : "");
                    sense.setNote(parts.length > 2 ? parts[2] : "");
                    senses.add(sense);
                }
                item.setSenses(senses);
            } else if ("variants".equals(field)) {
                item.setVariants(new ArrayList<>(values));
            }
        }

        @Override
        String elementKey(String field, String element) {
            // a sense is the same sense by its meaning: another part of speech or note is a change of it
            if ("senses".equals(field)) {
                int cut = element.indexOf(SEP);
                return cut < 0 ? element : element.substring(0, cut);
            }
            return element;
        }

        @Override
        void remove(Language language, LexiconEntry item) {
            language.deleteEntry(item.getId());
        }

        @Override
        void insert(Language language, LexiconEntry item, String anchor) {
            language.addEntry(item);
        }

        @Override
        Class<LexiconEntry> type() {
            return LexiconEntry.class;
        }
    };

    // ---- the categories of names

    static final Access<NameCategory> NAME_CATEGORIES = new Access<>(Target.NAME_CATEGORY) {
        @Override
        List<NameCategory> items(Language language) {
            return language.getNameCategories();
        }

        @Override
        String id(NameCategory item) {
            return item.getId();
        }

        @Override
        String label(NameCategory item) {
            return item.getName();
        }

        @Override
        Map<String, String> text(NameCategory item) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("name", item.getName());
            return map;
        }

        @Override
        void setText(NameCategory item, String field, String value) {
            if ("name".equals(field)) {
                item.setName(value);
            } else {
                throw new IllegalArgumentException(field);
            }
        }

        /** The words chosen, by their ids: a list merged as a set, so what both sides chose stays chosen. An id of a word that is gone is ignored when the names are made. */
        @Override
        Map<String, List<String>> lists(NameCategory item) {
            Map<String, List<String>> map = new LinkedHashMap<>();
            map.put("entries", new ArrayList<>(item.getEntries()));
            return map;
        }

        @Override
        void setList(NameCategory item, String field, List<String> values) {
            if ("entries".equals(field)) {
                item.getEntries().clear();
                item.getEntries().addAll(values);
            }
        }

        @Override
        Class<NameCategory> type() {
            return NameCategory.class;
        }
    };
}
