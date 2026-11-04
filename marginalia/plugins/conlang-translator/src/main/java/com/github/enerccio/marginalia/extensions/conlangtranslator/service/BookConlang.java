package com.github.enerccio.marginalia.extensions.conlangtranslator.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.TranslationService.ModelSelection;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.TranslationService.Status;
import com.github.enerccio.marginalia.extensions.conlangtranslator.store.ConlangStore;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.AnswerChecker.Word;
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
    private static final Object LIBRARY = new Object();

    @Autowired
    private ManuscriptService manuscriptService;
    @Autowired
    private SettingService settingService;

    private final TranslationService translation;
    private final ModelClient modelClient = new ModelClient();

    public BookConlang(TranslationService translation) {
        this.translation = translation;
    }

    /**
     * The language the book names (an independent copy), null when it names none or the language is gone.
     */
    public Language languageOf(Manuscript book) throws Exception {
        return translation.languageOf(translation.settings(book));
    }

    // ---- the speakers

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

    // ---- the quick translation

    /**
     * Translates a text into the language of the book. Nothing is written: the words it needed come back to be imported or not.
     *
     * @param context who says it, to whom, in what situation; may be empty
     */
    public Quick quickTranslate(Manuscript manuscript, String text, String context) throws Exception {
        Manuscript book = manuscriptService.find(manuscript);
        if (book == null) {
            throw new IllegalArgumentException("The book does not exist");
        }
        if (StringUtils.isBlank(text)) {
            return Quick.failed(Status.EMPTY);
        }
        BookSettings settings = translation.resolve(book);
        Language language = translation.languageOf(settings);
        if (language == null) {
            return Quick.failed(Status.NO_LANGUAGE);
        }
        ModelSelection model = translation.resolveModel(book, settings);
        if (model == null) {
            return Quick.failed(Status.NO_MODEL);
        }
        TranslationPrompt.Request request = QuickTranslation.build(language, settings.getSpeakers(), text, context, settings.getMaxLexiconChars());
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
        Language current = languageOf(book);
        if (current == null || words.isEmpty()) {
            return 0;
        }
        synchronized (LIBRARY) {
            UserSetting user = settingService.getOrCreate(UserSetting.class);
            Language stored = ConlangStore.find(user.getAttributes(), current.getId());
            if (stored == null) {
                return 0;
            }
            List<Word> fresh = AnswerChecker.checkNewWords(words.stream().map(Word::word).toList(), stored).words();
            if (fresh.isEmpty()) {
                return 0;
            }
            NewWords.addTo(stored, fresh, Origin.model(null, modelName, System.currentTimeMillis()));
            ConlangStore.save(user.getAttributes(), stored);
            settingService.save(user);
            return fresh.size();
        }
    }
}
