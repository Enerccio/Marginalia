package com.github.enerccio.marginalia.extensions.conlangtranslator.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.TranslationService.Status;
import com.github.enerccio.marginalia.extensions.conlangtranslator.store.ConlangStore;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.AnswerChecker.Word;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.BookLanguage;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.LanguageRef;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Speakers;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.UnderstandingSettings;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.ModelClient;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.ModelSelection;
import com.github.enerccio.marginalia.extensions.translatorcommon.text.UnderstandingBlock;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * What the author does with the language inside the book, outside the translation of a part: the speakers (they are kept in the book, so the
 * same language is spoken by other people in another story), the words of the language of the book, and the quick translation of a text.
 */
@Configurable
public class BookConlang {

    /** What a quick translation came to. {@code status} is TRANSLATED when there is a text; the other fields are empty otherwise. */
    public record Quick(Status status, String text, String choice, List<Word> words, List<Word> leftOut, List<String> unknownWords,
                        String model) {

        static Quick failed(Status status) {
            return new Quick(status, "", "", List.of(), List.of(), List.of(), "");
        }
    }

    /** One writer of the library at a time: a run of the translator adds words to the same languages. */
    private static final Object LIBRARY = LanguageAccess.LIBRARY;

    @Autowired
    private ManuscriptService manuscriptService;
    @Autowired
    private SettingService settingService;

    private final TranslationService translation;
    private final ModelClient modelClient = new ModelClient("conlang-translator");
    private final LanguageAccess languages = new LanguageAccess();

    public BookConlang(TranslationService translation) {
        this.translation = translation;
    }

    /**
     * The language the book names (an independent copy), null when it names none or the language is gone.
     */
    public Language languageOf(Manuscript book) throws Exception {
        return translation.languageOf(book, translation.settings(book));
    }

    /**
     * A language of the book (an independent copy): the one with this id, the first one for null; null when the book does not have it.
     *
     * @param languageId the language (its id)
     */
    public Language languageOf(Manuscript book, String languageId) throws Exception {
        if (languageId == null) {
            return languageOf(book);
        }
        return translation.languagesOf(book, translation.settings(book)).stream().map(TranslationService.BookLang::language)
                .filter(language -> language.getId().equals(languageId)).findFirst().orElse(null);
    }

    /**
     * The languages of the book that are in the library, the one it names first: for the menus and the choice of a language.
     */
    public List<LanguageRef> languages(Manuscript book) throws Exception {
        return translation.languagesOf(book, translation.settings(book)).stream()
                .map(item -> new LanguageRef(item.language().getId(), item.language().getName())).toList();
    }

    /**
     * The languages of the library of the user (as the book sees them), for adding one to the book.
     */
    public List<LanguageRef> libraryLanguages(Manuscript book) throws Exception {
        return languages.list(book).stream().map(language -> new LanguageRef(language.getId(), language.getName())).toList();
    }

    /**
     * The id of the language the book names first (its own, a folder's or the user's), null for none.
     */
    public String primaryLanguageId(Manuscript book) throws Exception {
        return translation.settings(book).getLanguageId();
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
            BookSettings settings = ConlangStore.readBook(attributes);
            settings.setOtherLanguages(new java.util.ArrayList<>(others));
            ConlangStore.writeBook(attributes, settings);
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

    // ---- the speakers

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
            BookSettings settings = ConlangStore.readBook(attributes);
            BookLanguage other = settings.getOtherLanguages().stream().filter(item -> languageId.equals(item.getLanguageId())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("The book does not have that language"));
            other.setSpeakers(speakers);
            ConlangStore.writeBook(attributes, settings);
        });
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
            BookSettings settings = ConlangStore.readBook(attributes);
            settings.setSpeakers(speakers);
            ConlangStore.writeBook(attributes, settings);
        });
    }

    // ---- the understanding of the languages

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
            BookSettings settings = ConlangStore.readBook(attributes);
            settings.setUnderstanding(understanding);
            ConlangStore.writeBook(attributes, settings);
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

    // ---- the words

    /**
     * Stores the words the author changed in the library. The language was loaded earlier and a run may have added words since, so the
     * words the screen does not know ({@code known} are the ids it loaded) are taken from the library, and only the lexicon is written: the
     * rest of the language is as it is stored now. {@code shown} and {@code known} are brought up to date.
     */
    public void saveWords(Language shown, Set<String> known) throws Exception {
        synchronized (LIBRARY) {
            UserSetting user = settingService.getOrCreate(UserSetting.class);
            Language stored = ConlangStore.find(user.getAttributes(), shown.getId());
            if (stored == null) {
                throw new IllegalStateException("The language is not in your library any more");
            }
            Set<String> shownIds = new HashSet<>();
            shown.getLexicon().forEach(entry -> shownIds.add(entry.getId()));
            for (LexiconEntry entry : stored.getLexicon()) {
                if (!known.contains(entry.getId()) && !shownIds.contains(entry.getId())) {
                    shown.getLexicon().add(entry);
                }
            }
            stored.getLexicon().clear();
            stored.getLexicon().addAll(shown.getLexicon());
            ConlangStore.save(user.getAttributes(), stored);
            settingService.save(user);
            shown.getCategories().clear();
            shown.getCategories().addAll(stored.getCategories());
            known.clear();
            shown.getLexicon().forEach(entry -> known.add(entry.getId()));
        }
    }

    // ---- what an agent run left

    /**
     * The languages an agent run changed that the book still holds, by the id of the language: the run's version and the version it started from.
     * The user takes them over into the library (or drops them); never null.
     */
    public java.util.Map<String, RunLanguage> runChanges(Manuscript book) throws Exception {
        return languages.run(book);
    }

    /**
     * What taking the run's version of the language over would change in the library, null when the library does not have the language (it is
     * added whole).
     */
    public com.github.enerccio.marginalia.extensions.conlangtranslator.diff.ChangeSet runChangeSet(Manuscript book, String languageId) throws Exception {
        return languages.changes(book, languageId);
    }

    /**
     * Writes the picked changes into the library and drops the run's version from the book.
     */
    public com.github.enerccio.marginalia.extensions.conlangtranslator.diff.LanguageDiff.Result takeOverRunChanges(
            Manuscript book, String languageId, com.github.enerccio.marginalia.extensions.conlangtranslator.diff.ChangeSet picked) throws Exception {
        return languages.takeOver(book, languageId, picked);
    }

    /**
     * Drops the run's version of the language from the book; the library is not touched.
     */
    public void discardRunChanges(Manuscript book, String languageId) throws Exception {
        languages.forget(book, languageId);
    }

    // ---- the quick translation

    /**
     * Translates a text into the language of the book. Nothing is written: the words it needed come back to be imported or not.
     *
     * @param context who says it, to whom, in what situation; may be empty
     */
    public Quick quickTranslate(Manuscript manuscript, String text, String context) throws Exception {
        return quickTranslate(manuscript, text, context, null);
    }

    /**
     * Translates a text into one language of the book. Nothing is written.
     *
     * @param languageId the language (its id), null for the first one of the book
     * @throws IllegalArgumentException when the book does not have the language
     */
    public Quick quickTranslate(Manuscript manuscript, String text, String context, String languageId) throws Exception {
        Manuscript book = manuscriptService.find(manuscript);
        if (book == null) {
            throw new IllegalArgumentException("The book does not exist");
        }
        if (StringUtils.isBlank(text)) {
            return Quick.failed(Status.EMPTY);
        }
        BookSettings settings = translation.resolve(book);
        List<TranslationService.BookLang> found = translation.languagesOf(book, settings);
        if (found.isEmpty()) {
            return Quick.failed(Status.NO_LANGUAGE);
        }
        TranslationService.BookLang chosen = translation.pick(found, languageId);
        Language language = chosen.language();
        ModelSelection model = translation.resolveModel(book, settings);
        if (model == null) {
            return Quick.failed(Status.NO_MODEL);
        }
        TranslationPrompt.Request request = QuickTranslation.build(language, chosen.speakers(), text, context, settings.getMaxLexiconChars());
        QuickTranslation.Answer answer = QuickTranslation.parse(modelClient.ask(model, book, request.system(), request.user()));
        if (answer == null) {
            return Quick.failed(Status.BAD_ANSWER);
        }
        AnswerChecker.Checked checked = AnswerChecker.checkNewWords(answer.newWords(), language);
        List<String> reported = checked.words().stream().map(word -> word.word().word()).toList();
        List<String> unknown = new LexiconCheck(language, reported, text).unknown(answer.text());
        return new Quick(Status.TRANSLATED, answer.text(), answer.choice(), checked.words(), checked.leftOut(), unknown, model.ai().getName());
    }

    /**
     * Adds the words of a quick translation to the lexicon of the language of the book, new (the author looks at them in the list of words).
     * A word the lexicon has meanwhile with the same meaning is left out.
     *
     * @return how many words were added
     */
    public int importWords(Manuscript book, List<Word> words, String modelName) throws Exception {
        return importWords(book, words, modelName, null);
    }

    /**
     * Adds the words of a quick translation to the lexicon of one language of the book, see {@link #importWords(Manuscript, List, String)}.
     *
     * @param languageId the language (its id), null for the first one of the book
     */
    public int importWords(Manuscript book, List<Word> words, String modelName, String languageId) throws Exception {
        Language current = languageOf(book, languageId);
        if (current == null || words.isEmpty()) {
            return 0;
        }
        int[] added = {0};
        languages.update(book, current.getId(), stored -> {
            List<Word> fresh = AnswerChecker.checkNewWords(words.stream().map(Word::word).toList(), stored).words();
            if (!fresh.isEmpty()) {
                NewWords.addTo(stored, fresh, Origin.model(null, modelName, System.currentTimeMillis()));
                added[0] = fresh.size();
            }
        });
        return added[0];
    }
}
