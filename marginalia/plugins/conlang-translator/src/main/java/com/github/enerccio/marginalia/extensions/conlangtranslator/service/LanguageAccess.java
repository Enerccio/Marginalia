package com.github.enerccio.marginalia.extensions.conlangtranslator.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.ChangeSet;
import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.LanguageDiff;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.RunLanguage;
import com.github.enerccio.marginalia.extensions.conlangtranslator.store.ConlangStore;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Where a language is read and written, for the user and for an agent run.
 * <p>
 * A user works on the <b>library</b> (the attributes of the user's settings). An agent run works on a hidden copy of the book (<code>agentic</code>)
 * and must not change the library behind the user's back, so the first change of a language in a run puts a copy of it, with the version it was
 * copied from, into the attributes of the run's book ({@link RunLanguage}); from then on the run reads and writes that copy. Nothing is copied at
 * the start of a run, and a run that never writes a language never has one. The copy travels with the book's own data (checkpoints, restore, the
 * review of the run, the delete of the copy), which is why the core needs no hook for it. When the run is applied the entry lands in the attributes of
 * the real book, and the user takes it over into the library ({@link #changes}, {@link #takeOver}) as a three-way change, so what the user changed
 * in the language meanwhile is not lost.
 */
@Configurable
public class LanguageAccess {

    /** One writer of the library at a time: a run of the translator adds words to the same languages. */
    public static final Object LIBRARY = new Object();

    @Autowired
    private SettingService settingService;
    @Autowired
    private ManuscriptService manuscriptService;

    /**
     * Whether the book is the working copy of an agent run.
     */
    public boolean inRun(Manuscript book) {
        return book != null && book.isAgentic();
    }

    // ---- reading

    /**
     * The languages the caller sees: the library, with the languages the run changed instead of the library's, and the ones it made at the end.
     * Independent copies.
     */
    public List<Language> list(Manuscript book) throws Exception {
        List<Language> languages = new ArrayList<>(ConlangStore.readLibrary(settingService.getOrCreate(UserSetting.class).getAttributes()));
        if (inRun(book)) {
            Map<String, RunLanguage> run = run(book);
            for (int i = 0; i < languages.size(); i++) {
                RunLanguage changed = run.get(languages.get(i).getId());
                if (changed != null) {
                    languages.set(i, ConlangStore.copy(changed.getOurs()));
                }
            }
            for (RunLanguage made : run.values()) {
                if (languages.stream().noneMatch(language -> language.getId().equals(made.getOurs().getId()))) {
                    languages.add(ConlangStore.copy(made.getOurs()));
                }
            }
        }
        return languages;
    }

    /**
     * The language with the id as the caller sees it (an independent copy), null when there is none.
     */
    public Language find(Manuscript book, String id) throws Exception {
        if (id == null) {
            return null;
        }
        if (inRun(book)) {
            RunLanguage changed = run(book).get(id);
            if (changed != null) {
                return ConlangStore.copy(changed.getOurs());
            }
        }
        return ConlangStore.find(settingService.getOrCreate(UserSetting.class).getAttributes(), id);
    }

    /**
     * The language a reference names: its id, or its name (the case does not matter).
     *
     * @throws IllegalArgumentException when no language, or more than one, is meant
     */
    public Language resolve(Manuscript book, String reference) throws Exception {
        List<Language> languages = list(book);
        if (StringUtils.isBlank(reference)) {
            throw new IllegalArgumentException(languages.isEmpty() ? "There is no language yet, conlang-create makes one"
                    : "Give the language: its name or id. Languages: " + names(languages));
        }
        String wanted = reference.strip();
        for (Language language : languages) {
            if (language.getId().equals(wanted)) {
                return language;
            }
        }
        List<Language> named = languages.stream().filter(language -> language.getName().strip().equalsIgnoreCase(wanted)).toList();
        if (named.size() == 1) {
            return named.getFirst();
        }
        if (named.size() > 1) {
            throw new IllegalArgumentException("More than one language is named " + wanted + ", give the id");
        }
        throw new IllegalArgumentException("There is no language " + wanted + ". Languages: " + (languages.isEmpty() ? "none" : names(languages)));
    }

    private static String names(List<Language> languages) {
        return String.join(", ", languages.stream().map(Language::getName).toList());
    }

    // ---- writing

    /**
     * Changes the language with the id: the library's in the library, the run's own copy in a run (made from the library's language at the first
     * change).
     *
     * @throws IllegalArgumentException when there is no such language
     */
    public void update(Manuscript book, String id, Consumer<Language> change) throws Exception {
        if (inRun(book)) {
            synchronized (runLock(book)) {
                Map<String, RunLanguage> run = run(book);
                RunLanguage entry = run.get(id);
                if (entry == null) {
                    Language library = ConlangStore.find(settingService.getOrCreate(UserSetting.class).getAttributes(), id);
                    if (library == null) {
                        throw new IllegalArgumentException("The language is not in the library");
                    }
                    entry = new RunLanguage(ConlangStore.copy(library), ConlangStore.copy(library), System.currentTimeMillis());
                    run.put(id, entry);
                }
                change.accept(entry.getOurs());
                storeRun(book, run);
            }
            return;
        }
        synchronized (LIBRARY) {
            UserSetting user = settingService.getOrCreate(UserSetting.class);
            Language stored = ConlangStore.find(user.getAttributes(), id);
            if (stored == null) {
                throw new IllegalArgumentException("The language is not in the library");
            }
            change.accept(stored);
            ConlangStore.save(user.getAttributes(), stored);
            settingService.save(user);
        }
    }

    /**
     * Adds a language that is new (a run keeps it with its own copy, with no base).
     */
    public void add(Manuscript book, Language language) throws Exception {
        if (inRun(book)) {
            synchronized (runLock(book)) {
                Map<String, RunLanguage> run = run(book);
                run.put(language.getId(), new RunLanguage(null, language, System.currentTimeMillis()));
                storeRun(book, run);
            }
            return;
        }
        synchronized (LIBRARY) {
            UserSetting user = settingService.getOrCreate(UserSetting.class);
            ConlangStore.save(user.getAttributes(), language);
            settingService.save(user);
        }
    }

    /**
     * Deletes the language from the library. A run does not delete the user's languages.
     *
     * @return whether the library had it
     */
    public boolean delete(Manuscript book, String id) throws Exception {
        if (inRun(book)) {
            throw new IllegalStateException("A language of the library is not deleted from an agent run; ask the user");
        }
        synchronized (LIBRARY) {
            UserSetting user = settingService.getOrCreate(UserSetting.class);
            boolean removed = ConlangStore.delete(user.getAttributes(), id);
            if (removed) {
                settingService.save(user);
            }
            return removed;
        }
    }

    // ---- what a run left

    /**
     * The languages a run changed, as the book carries them: the run's own copy while the run works, the real book after the run was applied.
     * Never null; independent copies.
     */
    public Map<String, RunLanguage> run(Manuscript book) throws Exception {
        Manuscript fresh = manuscriptService.find(book);
        return ConlangStore.readRun((fresh != null ? fresh : book).getAttributes());
    }

    private void storeRun(Manuscript book, Map<String, RunLanguage> run) throws Exception {
        manuscriptService.update(book, current -> {
            JsonObject attributes = current.getAttributes();
            if (attributes == null) {
                attributes = new JsonObject();
                current.setAttributes(attributes);
            }
            ConlangStore.writeRun(attributes, run);
        });
    }

    private Object runLock(Manuscript book) {
        return ("conlang-run-" + book.getUuid()).intern();
    }

    /**
     * What taking over the run's version of the language into the library would change: the three-way difference of the version the run copied, the
     * run's, and the library now. Null when the library does not have the language (it is added whole).
     */
    public ChangeSet changes(Manuscript book, String id) throws Exception {
        RunLanguage entry = run(book).get(id);
        if (entry == null) {
            throw new IllegalArgumentException("The book holds no changes of that language");
        }
        Language library = ConlangStore.find(settingService.getOrCreate(UserSetting.class).getAttributes(), id);
        if (library == null) {
            return null;
        }
        return entry.getBase() == null ? LanguageDiff.compare(library, entry.getOurs()) : LanguageDiff.diff(entry.getBase(), entry.getOurs(), library);
    }

    /**
     * Writes the picked changes into the library, or the whole language when the library lacks it, and drops the run's version from the book.
     *
     * @param picked the set {@link #changes} returned with the picks made; null for a language the library lacks
     * @return what was written
     */
    public LanguageDiff.Result takeOver(Manuscript book, String id, ChangeSet picked) throws Exception {
        if (inRun(book)) {
            throw new IllegalStateException("The changes of a run are taken over from the real book, not from the run");
        }
        RunLanguage entry = run(book).get(id);
        if (entry == null) {
            throw new IllegalArgumentException("The book holds no changes of that language");
        }
        LanguageDiff.Result result;
        synchronized (LIBRARY) {
            UserSetting user = settingService.getOrCreate(UserSetting.class);
            Language library = ConlangStore.find(user.getAttributes(), id);
            if (library == null) {
                ConlangStore.save(user.getAttributes(), entry.getOurs());
                result = new LanguageDiff.Result(List.of(), List.of(), List.of());
            } else {
                result = LanguageDiff.apply(library, picked);
                ConlangStore.save(user.getAttributes(), library);
            }
            settingService.save(user);
        }
        forget(book, id);
        return result;
    }

    /**
     * Drops the run's version of the language from the book without taking anything over.
     */
    public void forget(Manuscript book, String id) throws Exception {
        synchronized (runLock(book)) {
            Map<String, RunLanguage> run = new LinkedHashMap<>(run(book));
            if (run.remove(id) != null) {
                storeRun(book, run);
            }
        }
    }
}
