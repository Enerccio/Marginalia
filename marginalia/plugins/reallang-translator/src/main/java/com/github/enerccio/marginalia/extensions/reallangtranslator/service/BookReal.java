package com.github.enerccio.marginalia.extensions.reallangtranslator.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.BookSettings;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.RealLanguage;
import com.github.enerccio.marginalia.extensions.reallangtranslator.store.RealStore;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.BookLanguage;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.LanguageRef;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Speakers;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * What the author does with the language inside the book, outside the translation of a part: who speaks it (the speakers are kept in the book), and
 * the quick translation of a text.
 */
@Configurable
public class BookReal {

    @Autowired
    private ManuscriptService manuscriptService;

    private final TranslationService translation;

    public BookReal(TranslationService translation) {
        this.translation = translation;
    }

    /**
     * The language the book names, null when it names none or the language is gone.
     */
    public RealLanguage languageOf(Manuscript book) throws Exception {
        return translation.languageOf(translation.settings(book));
    }

    /**
     * The speakers of the book as they are stored now, never null.
     */
    public Speakers speakers(Manuscript book) throws Exception {
        return translation.settings(book).getSpeakers();
    }

    /**
     * Stores the speakers in the attributes of the book; the rest of the settings of the book is left as it is.
     */
    public void saveSpeakers(Manuscript book, Speakers speakers) throws Exception {
        manuscriptService.update(book, current -> {
            JsonObject attributes = current.getAttributes();
            if (attributes == null) {
                attributes = new JsonObject();
                current.setAttributes(attributes);
            }
            BookSettings settings = RealStore.readBook(attributes);
            settings.setSpeakers(speakers);
            RealStore.writeBook(attributes, settings);
        });
    }

    /**
     * The languages of the book that are in the library, the one it names first: for the menus and the choice of a language.
     */
    public List<LanguageRef> languages(Manuscript book) throws Exception {
        return translation.languagesOf(translation.settings(book)).stream()
                .map(item -> new LanguageRef(item.language().getId(), item.language().getName())).toList();
    }

    /**
     * The languages of the library of the user, for adding one to the book.
     */
    public List<LanguageRef> libraryLanguages() throws Exception {
        return translation.library().stream().map(language -> new LanguageRef(language.getId(), language.getName())).toList();
    }

    /**
     * The id of the language the book names first (its own, a folder's or the user's), null for none.
     */
    public String primaryLanguageId(Manuscript book) throws Exception {
        return translation.settings(book).getLanguageId();
    }

    /**
     * The speakers of a language of the book as they are stored now, never null.
     *
     * @param languageId the language (its id)
     * @throws IllegalArgumentException when the book does not have the language
     */
    public Speakers speakers(Manuscript book, String languageId) throws Exception {
        Speakers speakers = translation.settings(book).speakersOf(languageId);
        if (speakers == null) {
            throw new IllegalArgumentException("The book does not have that language");
        }
        return speakers;
    }

    /**
     * Stores the speakers of a language of the book; the rest of the settings of the book is left as it is.
     *
     * @throws IllegalArgumentException when the book does not have the language
     */
    public void saveSpeakers(Manuscript book, String languageId, Speakers speakers) throws Exception {
        String primary = translation.settings(book).getLanguageId();
        if (languageId == null || languageId.equals(primary)) {
            saveSpeakers(book, speakers);
            return;
        }
        manuscriptService.update(book, current -> {
            JsonObject attributes = ensure(current);
            BookSettings settings = RealStore.readBook(attributes);
            BookLanguage other = settings.getOtherLanguages().stream().filter(item -> languageId.equals(item.getLanguageId())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("The book does not have that language"));
            other.setSpeakers(speakers);
            RealStore.writeBook(attributes, settings);
        });
    }

    /**
     * The other languages of the book (besides the one it names), each with its speakers, as they are stored now.
     */
    public List<BookLanguage> otherLanguages(Manuscript book) throws Exception {
        return translation.settings(book).getOtherLanguages();
    }

    /**
     * Stores the other languages of the book; the rest of the settings of the book is left as it is.
     */
    public void saveOtherLanguages(Manuscript book, List<BookLanguage> others) throws Exception {
        manuscriptService.update(book, current -> {
            JsonObject attributes = ensure(current);
            BookSettings settings = RealStore.readBook(attributes);
            settings.setOtherLanguages(new java.util.ArrayList<>(others));
            RealStore.writeBook(attributes, settings);
        });
    }

    private static JsonObject ensure(Manuscript book) {
        JsonObject attributes = book.getAttributes();
        if (attributes == null) {
            attributes = new JsonObject();
            book.setAttributes(attributes);
        }
        return attributes;
    }

    /**
     * Translates a text into the language of the book. Nothing is written.
     */
    public TranslationService.Quick quickTranslate(Manuscript book, String text, String context) throws Exception {
        return translation.quickTranslate(book, text, context);
    }

    /**
     * Translates a text into one language of the book. Nothing is written.
     *
     * @param languageId the language (its id), null for the first one of the book
     */
    public TranslationService.Quick quickTranslate(Manuscript book, String text, String context, String languageId) throws Exception {
        return translation.quickTranslate(book, text, context, languageId);
    }
}
