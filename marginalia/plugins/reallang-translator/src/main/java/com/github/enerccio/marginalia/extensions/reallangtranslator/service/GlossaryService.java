package com.github.enerccio.marginalia.extensions.reallangtranslator.service;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.BookSettings;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.Glossary;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.Glossary.Effective;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.Glossary.Layer;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.Glossary.Level;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.GlossaryEntry;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.RealLanguage;
import com.github.enerccio.marginalia.extensions.reallangtranslator.store.RealStore;
import com.github.enerccio.marginalia.extensions.reallangtranslator.translate.TranslationAnswer;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

/**
 * The glossary of names and special words at its three levels: the language (in the library of the user), the folders of the book (their attributes) and
 * the book (its settings). {@link Glossary} merges them; this class reads and writes each level and takes the names the model proposed into the book.
 * <p>
 * The entries a level holds are its own: the book dialog shows the ones that come from above and changes them where they live. The glossary of a
 * folder and of a book is kept for each language of the book (a name is written another way in every language); the language's own is in the language.
 */
@Configurable
public class GlossaryService {

    /**
     * Where an entry lives.
     *
     * @param level      the level
     * @param folderUuid the folder, for {@link Level#FOLDER}; null otherwise
     */
    public record Target(Level level, String folderUuid) {

        /** Where the entry the merge found lives. */
        public static Target of(Effective effective) {
            return new Target(effective.level(), effective.id());
        }

        public static Target language() {
            return new Target(Level.LANGUAGE, null);
        }

        public static Target book() {
            return new Target(Level.BOOK, null);
        }

        public static Target folder(String uuid) {
            return new Target(Level.FOLDER, uuid);
        }
    }

    /** Writers of the glossary, one at a time (a run proposes names while the author edits). */
    private static final Object WRITE = new Object();

    @Autowired
    private ManuscriptService manuscriptService;
    @Autowired
    private FolderService folderService;

    private final LanguageLibrary library;

    public GlossaryService(LanguageLibrary library) {
        this.library = library;
    }

    /**
     * The levels of the glossary of a book in a language, the farthest first: the language, the outer folder to the inner one, the book.
     */
    public List<Layer> layers(Manuscript book, RealLanguage language) throws Exception {
        List<Layer> layers = new ArrayList<>();
        layers.add(new Layer(Level.LANGUAGE, null, language.getName(), copy(language.getGlossary())));
        Manuscript current = manuscriptService.find(book);
        Manuscript source = current != null ? current : book;
        for (Folder folder : folders(source)) {
            layers.add(new Layer(Level.FOLDER, folder.getUuid(), folder.getName(), copy(RealStore.readFolderGlossary(folder.getAttributes(), language.getId()))));
        }
        layers.add(new Layer(Level.BOOK, null, source.getName(), copy(RealStore.readBook(source.getAttributes()).getGlossary(language.getId()))));
        return layers;
    }

    /**
     * The folders above the book, the outer one first.
     */
    public List<Folder> folders(Manuscript book) throws Exception {
        if (book.getFolder() == null) {
            return List.of();
        }
        List<Folder> chain = new ArrayList<>(folderService.findChain(book.getFolder()));
        Collections.reverse(chain);
        return chain;
    }

    /**
     * The merged glossary of the book in a language, with where every entry comes from; entries that are off are in it, marked not active.
     */
    public List<Effective> effective(Manuscript book, RealLanguage language) throws Exception {
        return Glossary.merge(layers(book, language));
    }

    /**
     * The entries the model uses for the book in a language.
     */
    public List<GlossaryEntry> active(Manuscript book, RealLanguage language) throws Exception {
        return Glossary.active(effective(book, language));
    }

    /**
     * The entries of one level as they are stored now.
     */
    public List<GlossaryEntry> entries(Manuscript book, RealLanguage language, Target target) throws Exception {
        return switch (target.level()) {
            case LANGUAGE -> copy(libraryLanguage(language).getGlossary());
            case FOLDER -> copy(RealStore.readFolderGlossary(folderOf(book, target.folderUuid()).getAttributes(), language.getId()));
            case BOOK -> copy(RealStore.readBook(manuscriptService.find(book).getAttributes()).getGlossary(language.getId()));
        };
    }

    /**
     * Adds an entry to a level, or replaces the one that has the same source word (or id) there.
     *
     * @return the entry as stored (a new entry keeps its id)
     * @throws IllegalArgumentException when the entry has no source, or no written form and is not a kept word
     */
    public GlossaryEntry put(Manuscript book, RealLanguage language, Target target, GlossaryEntry entry) throws Exception {
        if (!entry.isUsable()) {
            throw new IllegalArgumentException("The entry needs the word as the story writes it and how it is written (or to be kept as it is)");
        }
        synchronized (WRITE) {
            List<GlossaryEntry> entries = Glossary.put(entries(book, language, target), entry);
            write(book, language, target, entries);
        }
        return entry;
    }

    /**
     * Takes an entry out of a level.
     *
     * @return whether the level had it
     */
    public boolean remove(Manuscript book, RealLanguage language, Target target, String entryId) throws Exception {
        synchronized (WRITE) {
            List<GlossaryEntry> entries = entries(book, language, target);
            if (!entries.removeIf(entry -> entry.getId().equals(entryId))) {
                return false;
            }
            write(book, language, target, entries);
            return true;
        }
    }

    /**
     * Replaces all the entries of a level (the dialog saves its list).
     */
    public void replace(Manuscript book, RealLanguage language, Target target, List<GlossaryEntry> entries) throws Exception {
        synchronized (WRITE) {
            write(book, language, target, new ArrayList<>(entries));
        }
    }

    /**
     * Moves an entry of the book to a higher level (a name that is common to the books of a series, or to the language), so the book does not hold it
     * any more. The same source word at the higher level is replaced.
     *
     * @return whether the book had the entry
     */
    public boolean moveFromBook(Manuscript book, RealLanguage language, String entryId, Target to) throws Exception {
        if (to.level() == Level.BOOK) {
            throw new IllegalArgumentException("The entry is in the book already");
        }
        synchronized (WRITE) {
            List<GlossaryEntry> own = entries(book, language, Target.book());
            GlossaryEntry entry = own.stream().filter(item -> item.getId().equals(entryId)).findFirst().orElse(null);
            if (entry == null) {
                return false;
            }
            entry.setProposed(false);
            write(book, language, to, Glossary.put(entries(book, language, to), entry));
            own.remove(entry);
            write(book, language, Target.book(), own);
            return true;
        }
    }

    /**
     * Accepts a proposed entry of the book: the author looked at it.
     *
     * @return whether the book had it
     */
    public boolean accept(Manuscript book, RealLanguage language, String entryId) throws Exception {
        synchronized (WRITE) {
            List<GlossaryEntry> own = entries(book, language, Target.book());
            GlossaryEntry entry = own.stream().filter(item -> item.getId().equals(entryId)).findFirst().orElse(null);
            if (entry == null) {
                return false;
            }
            entry.setProposed(false);
            write(book, language, Target.book(), own);
            return true;
        }
    }

    /**
     * Takes the names the model chose a written form for into the book as proposed entries, unless the glossary has the word (at any level, active or
     * off) or the part does not have it. The model uses a proposal from the next request on.
     *
     * @param partText the part as the model read it
     * @return the entries that were added
     */
    public List<GlossaryEntry> propose(Manuscript book, RealLanguage language, List<TranslationAnswer.Name> names, String partText) throws Exception {
        if (names == null || names.isEmpty()) {
            return List.of();
        }
        List<GlossaryEntry> candidates = new ArrayList<>();
        for (TranslationAnswer.Name name : names) {
            candidates.add(new GlossaryEntry(name.source().strip(), com.github.enerccio.marginalia.extensions.reallangtranslator.model.GlossaryKind.WRITTEN,
                    name.written().strip(), name.reading() == null ? "" : name.reading().strip(), name.note() == null ? "" : name.note().strip()));
        }
        return proposeEntries(book, language, candidates, partText);
    }

    /**
     * Takes entries into the book as proposals, unless the glossary has the word (at any level, active or off), the text does not have it, or an
     * earlier candidate of the same call is the word. What the word extraction finds goes in this way.
     *
     * @param candidates the entries to propose; they are marked as proposed
     * @param partText   the text the entries must be in (the part as the model read it)
     * @return the entries that were added
     */
    public List<GlossaryEntry> proposeEntries(Manuscript book, RealLanguage language, List<GlossaryEntry> candidates, String partText) throws Exception {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        synchronized (WRITE) {
            Set<String> known = new HashSet<>();
            effective(book, language).forEach(item -> known.add(item.entry().key()));
            List<GlossaryEntry> own = entries(book, language, Target.book());
            List<GlossaryEntry> added = new ArrayList<>();
            for (GlossaryEntry candidate : candidates) {
                if (!candidate.isUsable() || known.contains(candidate.key()) || !Glossary.contains(partText, candidate.getSource())) {
                    continue;
                }
                GlossaryEntry entry = candidate.copy();
                entry.setProposed(true);
                known.add(entry.key());
                own.add(entry);
                added.add(entry);
            }
            if (!added.isEmpty()) {
                write(book, language, Target.book(), own);
            }
            return added;
        }
    }

    private void write(Manuscript book, RealLanguage language, Target target, List<GlossaryEntry> entries) throws Exception {
        switch (target.level()) {
            case LANGUAGE -> {
                RealLanguage stored = libraryLanguage(language);
                stored.setGlossary(entries);
                library.store(stored);
            }
            case FOLDER -> {
                Folder folder = folderOf(book, target.folderUuid());
                JsonObject attributes = folder.getAttributes();
                if (attributes == null) {
                    attributes = new JsonObject();
                    folder.setAttributes(attributes);
                }
                RealStore.writeFolderGlossary(attributes, language.getId(), entries);
                folderService.save(folder);
            }
            case BOOK -> manuscriptService.update(book, current -> {
                JsonObject attributes = current.getAttributes();
                if (attributes == null) {
                    attributes = new JsonObject();
                    current.setAttributes(attributes);
                }
                BookSettings settings = RealStore.readBook(attributes);
                settings.setGlossary(language.getId(), entries);
                RealStore.writeBook(attributes, settings);
            });
        }
    }

    private RealLanguage libraryLanguage(RealLanguage language) throws Exception {
        return library.languages().stream().filter(item -> item.getId().equals(language.getId())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The language is not in your library"));
    }

    private Folder folderOf(Manuscript book, String uuid) throws Exception {
        return folders(manuscriptService.find(book)).stream().filter(folder -> folder.getUuid().equals(uuid)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The book is not in that folder"));
    }

    private static List<GlossaryEntry> copy(List<GlossaryEntry> entries) {
        return new ArrayList<>(entries.stream().map(GlossaryEntry::copy).toList());
    }
}
