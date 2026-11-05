package com.github.enerccio.marginalia.extensions.reallangtranslator.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.BookSettings;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.RealLanguage;
import com.github.enerccio.marginalia.extensions.reallangtranslator.store.RealStore;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Speakers;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

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
     * Translates a text into the language of the book. Nothing is written.
     */
    public TranslationService.Quick quickTranslate(Manuscript book, String text, String context) throws Exception {
        return translation.quickTranslate(book, text, context);
    }
}
