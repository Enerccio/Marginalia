package com.github.enerccio.marginalia.extensions.reallangtranslator.service;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.BookSettings;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.Glossary;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.GlossaryEntry;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.RealLanguage;
import com.github.enerccio.marginalia.extensions.reallangtranslator.store.RealStore;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.BookLanguage;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.LanguageRef;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Speakers;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.UnderstandingSettings;
import com.github.enerccio.marginalia.extensions.translatorcommon.text.UnderstandingBlock;
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
    private final GlossaryService glossary;

    public BookReal(TranslationService translation, GlossaryService glossary) {
        this.translation = translation;
        this.glossary = glossary;
    }

    // ---- the glossary of names and special words (the three levels are merged by Glossary)

    /**
     * The glossary of the book in a language as the model gets it, with where every entry comes from; the entries that are off are in it, marked.
     *
     * @param languageId one of the languages of the book (its id), null for the first one
     * @throws IllegalArgumentException when the book does not have the language, or has none
     */
    public List<Glossary.Effective> glossary(Manuscript book, String languageId) throws Exception {
        return glossary.effective(book, glossaryLanguage(book, languageId));
    }

    /**
     * The folders above the book, the outer one first: where a book's entry can be moved to.
     */
    public List<Folder> glossaryFolders(Manuscript book) throws Exception {
        return glossary.folders(book);
    }

    /**
     * Adds an entry to a level of the glossary, or replaces the one with the same source word there.
     */
    public GlossaryEntry putGlossaryEntry(Manuscript book, String languageId, GlossaryService.Target target, GlossaryEntry entry) throws Exception {
        return glossary.put(book, glossaryLanguage(book, languageId), target, entry);
    }

    /**
     * Takes an entry out of a level.
     *
     * @return whether the level had it
     */
    public boolean removeGlossaryEntry(Manuscript book, String languageId, GlossaryService.Target target, String entryId) throws Exception {
        return glossary.remove(book, glossaryLanguage(book, languageId), target, entryId);
    }

    /**
     * The author looked at a proposed entry of the book.
     */
    public boolean acceptGlossaryEntry(Manuscript book, String languageId, String entryId) throws Exception {
        return glossary.accept(book, glossaryLanguage(book, languageId), entryId);
    }

    /**
     * Moves an entry of the book up to a folder or to the language.
     */
    public boolean moveGlossaryEntry(Manuscript book, String languageId, String entryId, GlossaryService.Target to) throws Exception {
        return glossary.moveFromBook(book, glossaryLanguage(book, languageId), entryId, to);
    }

    /**
     * Switches a word off, or on, for this book: an entry of the book with the same source word that overrides the one from above.
     */
    public void switchGlossaryEntry(Manuscript book, String languageId, GlossaryEntry inherited, boolean enabled) throws Exception {
        GlossaryEntry own = inherited.copy();
        own.setId(java.util.UUID.randomUUID().toString());
        own.setEnabled(enabled);
        own.setProposed(false);
        glossary.put(book, glossaryLanguage(book, languageId), GlossaryService.Target.book(), own);
    }

    private RealLanguage glossaryLanguage(Manuscript book, String languageId) throws Exception {
        List<TranslationService.BookLang> languages = translation.languagesOf(translation.settings(book));
        if (languages.isEmpty()) {
            throw new IllegalArgumentException("The book has no language");
        }
        if (languageId == null) {
            return languages.getFirst().language();
        }
        return languages.stream().map(TranslationService.BookLang::language).filter(language -> language.getId().equals(languageId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The book does not have that language"));
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
     * How the book puts the levels of understanding into the prompt, as they are stored now, never null.
     */
    public UnderstandingSettings understanding(Manuscript book) throws Exception {
        return translation.settings(book).getUnderstanding();
    }

    /**
     * Stores how the book puts the levels of understanding into the prompt; the rest of the settings of the book is left as it is.
     */
    public void saveUnderstanding(Manuscript book, UnderstandingSettings understanding) throws Exception {
        manuscriptService.update(book, current -> {
            JsonObject attributes = ensure(current);
            BookSettings settings = RealStore.readBook(attributes);
            settings.setUnderstanding(understanding);
            RealStore.writeBook(attributes, settings);
        });
    }

    /**
     * The languages of the book with their speakers, for the block that tells the models who follows which language.
     */
    public List<UnderstandingBlock.Entry> understandingEntries(Manuscript book) throws Exception {
        BookSettings settings = translation.settings(book);
        List<UnderstandingBlock.Entry> entries = new java.util.ArrayList<>();
        for (LanguageRef ref : languages(book)) {
            Speakers speakers = settings.speakersOf(ref.id());
            if (speakers != null) {
                entries.add(new UnderstandingBlock.Entry(ref.name(), speakers));
            }
        }
        return entries;
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
