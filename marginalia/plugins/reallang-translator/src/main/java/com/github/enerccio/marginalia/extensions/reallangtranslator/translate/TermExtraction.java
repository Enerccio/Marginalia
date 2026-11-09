package com.github.enerccio.marginalia.extensions.reallangtranslator.translate;

import com.github.enerccio.marginalia.extensions.reallangtranslator.model.Glossary;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.GlossaryEntry;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.GlossaryKind;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.RealLanguage;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.ModelJson;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The word extraction: the model reads a part of the story and lists its proper nouns and coined terms with the form each has in the language. The
 * instructions are the author's (a prompt of the library, the specification of the book); the plugin adds the language, the glossary the book has already
 * and the output format, so a prompt is only about the rules and works whatever the author wrote about the format.
 */
public final class TermExtraction {

    private static final Gson GSON = new Gson();

    private TermExtraction() {
    }

    /**
     * One term as the model answered for it.
     *
     * @param source   the term as the story writes it
     * @param category the kind of term the model put it in, a few words
     * @param written  its form in the language
     * @param reading  how it is read, empty when it is read as it is written
     * @param keep     whether it stays as the story writes it
     * @param note     a few words for the translator, may be empty
     */
    public record Term(String source, String category, String written, String reading, Boolean keep, String note) {

        /** The glossary entry for the term (a proposal); null when it has no source or neither a form nor to be kept. */
        public GlossaryEntry entry() {
            String word = StringUtils.defaultString(source).strip();
            boolean kept = Boolean.TRUE.equals(keep);
            if (word.isEmpty() || (!kept && StringUtils.isBlank(written))) {
                return null;
            }
            String why = StringUtils.defaultString(category).strip();
            String detail = StringUtils.defaultString(note).strip();
            String joined = why.isEmpty() ? detail : detail.isEmpty() ? why : why + ": " + detail;
            GlossaryEntry entry = kept ? new GlossaryEntry(word, GlossaryKind.KEEP, "", "", joined)
                    : new GlossaryEntry(word, GlossaryKind.WRITTEN, written.strip(), StringUtils.defaultString(reading).strip(), joined);
            entry.setProposed(true);
            return entry;
        }
    }

    /**
     * What the model answered.
     *
     * @param terms the terms, never null
     */
    public record Answer(List<Term> terms) {
    }

    /**
     * The JSON schema of the answer, for a service that takes a structured output.
     */
    public static final String SCHEMA = """
            {"type":"object","additionalProperties":false,"required":["terms"],"properties":{
            "terms":{"type":"array","items":{"type":"object","additionalProperties":false,
            "required":["source","category","written","reading","keep","note"],"properties":{
            "source":{"type":"string"},"category":{"type":"string"},"written":{"type":"string"},"reading":{"type":"string"},
            "keep":{"type":"boolean"},"note":{"type":"string"}}}}}}
            """;

    static final String OUTPUT = """
            === OUTPUT FORMAT ===
            Whatever format the instructions above ask for, answer with one JSON object only, no other text and no code fence, in this form:
            {"terms":[{"source":"...","category":"...","written":"...","reading":"","keep":false,"note":""}]}
            - "source": the term as the MANUSCRIPT writes it, in its base form (singular, without a possessive or a suffix), the way the glossary would list it.
            - "category": the kind of term you put it in, a few words.
            - "written": its form in %1$s, as the instructions say to write that kind of term. Empty only when "keep" is true.
            - "reading": how it is read, when the instructions give a reading for that kind of term or the form is not read the way it is written; otherwise empty.
            - "keep": true when the term stays as the MANUSCRIPT writes it; then "written" and "reading" are empty.
            - "note": a few words for the translator, empty when there is nothing to add.
            List every term once, and only terms the MANUSCRIPT has. Leave out the terms the glossary above has already. When there is nothing to list answer {"terms":[]}.""";

    /**
     * The request for one part (or a few).
     *
     * @param language      the language the glossary is for
     * @param prompt        the text of the prompt of the library, may be empty
     * @param specification what the author wrote for this book, may be empty
     * @param known         the active entries of the glossary the book has; only the ones the text has are told to the model
     * @param text          the part as the model reads it
     */
    public static TranslationPrompt.Request build(RealLanguage language, String prompt, String specification, List<GlossaryEntry> known, String text) {
        StringBuilder system = new StringBuilder("You build the glossary of a story that is being translated into ").append(language.getTongue())
                .append(". You read the MANUSCRIPT and list the proper nouns and coined terms of it that need one fixed form in ").append(language.getTongue())
                .append(", so that the whole book writes each of them the same way.\n\n");
        if (StringUtils.isNotBlank(prompt)) {
            system.append("=== INSTRUCTIONS ===\n").append(prompt.strip()).append("\n\n");
        }
        if (StringUtils.isNotBlank(specification)) {
            system.append("=== SPECIFICATION OF THIS BOOK ===\n").append(specification.strip()).append("\n\n");
        }
        system.append(TranslationPrompt.languageBlock(language)).append('\n');
        List<GlossaryEntry> seen = Glossary.relevant(known == null ? List.of() : known, text);
        if (seen.stream().anyMatch(GlossaryEntry::isUsable)) {
            system.append("=== THE GLOSSARY OF THE BOOK (already decided: do not list these again, and keep your forms consistent with them) ===\n");
            for (GlossaryEntry entry : seen) {
                if (!entry.isUsable()) {
                    continue;
                }
                system.append("- ").append(entry.getSource()).append(": ")
                        .append(entry.getKind() == GlossaryKind.KEEP ? "kept as it is" : entry.getWritten())
                        .append(entry.getReading().isEmpty() ? "" : " (" + entry.getReading() + ")").append('\n');
            }
            system.append('\n');
        }
        system.append(String.format(OUTPUT, language.getTongue()));
        return new TranslationPrompt.Request(system.toString(), "MANUSCRIPT:\n" + (text == null ? "" : text.strip()) + '\n');
    }

    /**
     * @return the answer; null when no object with the list of terms could be read in the text
     */
    public static Answer parse(String raw) {
        String json = ModelJson.object(raw);
        if (json == null) {
            return null;
        }
        try {
            Answer answer = GSON.fromJson(json, Answer.class);
            if (answer == null || answer.terms() == null) {
                return null;
            }
            List<Term> terms = new ArrayList<>();
            answer.terms().stream().filter(term -> term != null && term.entry() != null).forEach(terms::add);
            return new Answer(terms);
        } catch (JsonParseException e) {
            return null;
        }
    }
}
