package com.github.enerccio.marginalia.extensions.reallangtranslator.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.*;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.TranslationService.BookLang;
import com.github.enerccio.marginalia.extensions.reallangtranslator.translate.TermExtraction;
import com.github.enerccio.marginalia.extensions.reallangtranslator.translate.TranslationPrompt;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.ModelClient;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.ModelSelection;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

/**
 * The word extraction: asks the model for the names and special words of a part (with the form each has in the language) and takes what it finds into
 * the glossary of the book as proposals. The instructions are the prompt of the library the book chose and the specification the author wrote
 * ({@link TermExtraction}); the part is read as the model reads it, and nothing in the story is changed.
 */
@Configurable
public class ExtractionService {

    /** What came of an extraction. */
    public enum Status {
        /** The model answered; the terms it found were taken into the glossary (those that were new). */
        DONE,
        /** The book has no language, or the one asked for is not one of its languages. */
        NO_LANGUAGE,
        /** There is no model for the request. */
        NO_MODEL,
        /** The book has neither a prompt nor a specification: the model would not know what to look for. */
        NO_PROMPT,
        /** The part has no text. */
        EMPTY,
        /** The model did not answer in the expected form; nothing was changed. */
        BAD_ANSWER
    }

    /**
     * @param status what came of it
     * @param found  how many terms the model listed
     * @param added  how many of them were new and were added to the glossary of the book as proposals
     * @param note   what the user should know, may be empty
     */
    public record Outcome(Status status, int found, int added, String note) {

        static Outcome of(Status status, String note) {
            return new Outcome(status, 0, 0, note);
        }
    }

    @Autowired
    private ManuscriptService manuscriptService;
    @Autowired
    private ChatMessageService chatMessageService;
    @Autowired
    private PartTextRegistry partTexts;

    private final ModelClient modelClient = new ModelClient("reallang-translator");
    private final TranslationService translation;
    private final GlossaryService glossary;
    private final LanguageLibrary library;

    public ExtractionService(TranslationService translation, GlossaryService glossary, LanguageLibrary library) {
        this.translation = translation;
        this.glossary = glossary;
        this.library = library;
    }

    /**
     * Extracts the terms of the part into the glossary of the book in a language, with the prompt and the specification the book has.
     *
     * @param languageId one of the languages of the book (its id), null for the first one
     * @throws IllegalArgumentException when the book does not have the language
     */
    public Outcome extract(Manuscript manuscript, ChatMessage part, String languageId) throws Exception {
        Manuscript book = manuscriptService.find(manuscript);
        ChatMessage current = chatMessageService.find(part);
        if (book == null || current == null) {
            throw new IllegalArgumentException("The part does not exist");
        }
        BookSettings settings = translation.resolve(book);
        List<BookLang> languages = translation.languagesOf(settings);
        if (languages.isEmpty()) {
            return Outcome.of(Status.NO_LANGUAGE, settings.getLanguageId() == null ? "The book has no language." : "The language of the book is not in your library.");
        }
        RealLanguage language = languageId == null ? languages.getFirst().language() : languages.stream().map(BookLang::language)
                .filter(item -> item.getId().equals(languageId)).findFirst().orElseThrow(() -> new IllegalArgumentException("The book does not have that language"));
        ExtractionSettings extraction = settings.getExtraction();
        ExtractionPrompt prompt = library.findPrompt(extraction.getPromptId());
        if (prompt == null && extraction.getSpecification().isEmpty()) {
            return Outcome.of(Status.NO_PROMPT, "The book has no extraction prompt and no specification.");
        }
        List<String> texts = partTexts.forModel(List.of(current));
        String text = texts.isEmpty() ? "" : texts.getFirst();
        if (StringUtils.isBlank(text)) {
            return Outcome.of(Status.EMPTY, "The part has no text.");
        }
        ModelSelection model = translation.resolveModel(book, settings);
        if (model == null) {
            return Outcome.of(Status.NO_MODEL, "There is no model for the extraction: the book has none and neither have your defaults.");
        }
        List<GlossaryEntry> known = glossary.active(book, language);
        TranslationPrompt.Request request = TermExtraction.build(language, prompt == null ? "" : prompt.getText(), extraction.getSpecification(), known, text);
        TermExtraction.Answer answer = TermExtraction.parse(modelClient.ask(model, book, request.system(), request.user()));
        if (answer == null) {
            return Outcome.of(Status.BAD_ANSWER, "The model did not answer in the expected form.");
        }
        List<GlossaryEntry> entries = new ArrayList<>();
        answer.terms().forEach(term -> entries.add(term.entry()));
        int added = glossary.proposeEntries(book, language, entries, text).size();
        return new Outcome(Status.DONE, entries.size(), added, "");
    }
}
