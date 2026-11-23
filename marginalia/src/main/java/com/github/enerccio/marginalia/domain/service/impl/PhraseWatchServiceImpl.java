package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.components.PhraseMatcher;
import com.github.enerccio.marginalia.domain.model.AnnotationTemplate;
import com.github.enerccio.marginalia.domain.model.WatchedPhrase;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.AnnotationService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.EffectiveSetting;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.PhraseWatchService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.traits.NoTx;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.loc.M_L;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class PhraseWatchServiceImpl implements PhraseWatchService {

    /** The longest label (the entry of the list) an annotation is given. */
    private static final int LABEL_LENGTH = 80;

    @Autowired
    private Localization loc;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private FolderService folderService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private AnnotationService annotationService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private User currentUser;

    @Override
    @NoTx
    public List<Effective> resolve(Manuscript book) throws Exception {
        Manuscript stored = manuscriptService.find(book);
        if (stored == null) {
            return List.of();
        }
        Map<String, Effective> entries = collect(stored.getFolder());
        for (WatchedPhrase phrase : stored.getWatchedPhrases()) {
            put(entries, new Effective(phrase.copy(), EffectiveSetting.Origin.BOOK, null));
        }
        return enabled(entries);
    }

    @Override
    @NoTx
    public List<Effective> resolveInherited(Folder folder) throws Exception {
        return enabled(collect(folder));
    }

    /** The user settings, then the folders from the top down to the one given; a nearer entry of the same phrase replaces the other. */
    private Map<String, Effective> collect(Folder folder) throws Exception {
        Map<String, Effective> entries = new LinkedHashMap<>();
        UserSetting settings = userSettings();
        if (settings != null) {
            for (WatchedPhrase phrase : settings.getWatchedPhrases()) {
                put(entries, new Effective(phrase.copy(), EffectiveSetting.Origin.USER, null));
            }
        }
        for (Folder above : folderService.findChain(folder).reversed()) {
            for (WatchedPhrase phrase : above.getWatchedPhrases()) {
                put(entries, new Effective(phrase.copy(), EffectiveSetting.Origin.FOLDER, above));
            }
        }
        return entries;
    }

    private static void put(Map<String, Effective> entries, Effective entry) {
        if (entry.phrase() == null || StringUtils.isBlank(entry.phrase().getPhrase())) {
            return;
        }
        String key = entry.phrase().key();
        // a nearer level goes where the phrase was first set: the list keeps the order the author knows
        entries.remove(key);
        entries.put(key, entry);
    }

    private static List<Effective> enabled(Map<String, Effective> entries) {
        return entries.values().stream().filter(entry -> !entry.phrase().isDisabled()).toList();
    }

    @Override
    @NoTx
    public List<AnnotationTemplate> getTemplates() throws Exception {
        UserSetting settings = userSettings();
        if (settings == null) {
            return List.of();
        }
        return settings.getAnnotationTemplates().stream().map(AnnotationTemplate::copy).toList();
    }

    @Override
    @NoTx
    public Manuscript setForBook(Manuscript book, List<WatchedPhrase> phrases, boolean scanGenerated) throws Exception {
        List<WatchedPhrase> cleaned = clean(phrases);
        return manuscriptService.update(book, stored -> {
            stored.setWatchedPhrases(cleaned);
            stored.setPhraseWatch(scanGenerated ? Boolean.TRUE : null);
        });
    }

    @Override
    @NoTx
    public Folder setForFolder(Folder folder, List<WatchedPhrase> phrases) throws Exception {
        List<WatchedPhrase> cleaned = clean(phrases);
        // the folder may be the lazy proxy of an association loaded earlier: its id is known without a session, the rest is not
        Folder byId = folder == null || folder.getId() == null ? null : folderService.find(folder.getId());
        Folder stored = byId == null ? null : folderService.findForUser(byId.getUuid());
        if (stored == null) {
            return null;
        }
        stored.setWatchedPhrases(cleaned);
        return folderService.save(stored);
    }

    /**
     * Copies of the entries with their phrase trimmed, the blank ones left out.
     *
     * @throws IllegalArgumentException naming the first entry that can't be used
     */
    static List<WatchedPhrase> clean(List<WatchedPhrase> phrases) {
        List<WatchedPhrase> cleaned = new ArrayList<>();
        if (phrases == null) {
            return cleaned;
        }
        for (WatchedPhrase phrase : phrases) {
            if (phrase == null || StringUtils.isBlank(phrase.getPhrase())) {
                continue;
            }
            WatchedPhrase copy = phrase.copy();
            copy.setPhrase(phrase.getPhrase().strip());
            copy.setTemplate(StringUtils.trimToNull(phrase.getTemplate()));
            String problem = PhraseMatcher.validate(copy);
            if (problem != null) {
                throw new IllegalArgumentException(copy.getPhrase() + ": " + problem);
            }
            cleaned.add(copy);
        }
        return cleaned;
    }

    @Override
    @NoTx
    public List<Annotation> scan(Manuscript book, ChatMessage part) throws Exception {
        ChatMessage stored = part == null ? null : chatMessageService.find(part);
        if (stored == null || stored.isDeleted() || StringUtils.isBlank(stored.getResponse())) {
            return List.of();
        }
        List<Effective> entries = resolve(book);
        if (entries.isEmpty()) {
            return List.of();
        }
        return scan(book, stored, entries.stream().map(Effective::phrase).toList(), templates());
    }

    @Override
    @NoTx
    public int scanBranch(Manuscript book) throws Exception {
        return scanRange(book, null, null);
    }

    @Override
    @NoTx
    public int scanRange(Manuscript book, Integer from, Integer to) throws Exception {
        Manuscript stored = manuscriptService.find(book);
        if (stored == null || stored.getActiveLeaf() == null) {
            return 0;
        }
        List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(stored.getActiveLeaf());
        for (Integer number : new Integer[]{from, to}) {
            if (number != null && (number < 1 || number > branch.size())) {
                throw new IllegalArgumentException("No part " + number + " on the active branch (1 to " + branch.size() + ")");
            }
        }
        int first = from == null ? 0 : from - 1;
        int last = to == null ? branch.size() - 1 : to - 1;
        return scanParts(stored, branch.subList(Math.min(first, last), Math.max(first, last) + 1));
    }

    @Override
    @NoTx
    public int scanBetween(Manuscript book, ChatMessage from, ChatMessage to) throws Exception {
        Manuscript stored = manuscriptService.find(book);
        if (stored == null || stored.getActiveLeaf() == null) {
            return 0;
        }
        List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(stored.getActiveLeaf());
        return scanRange(stored, numberOf(branch, from), numberOf(branch, to));
    }

    private static Integer numberOf(List<ChatMessage> branch, ChatMessage part) {
        if (part == null) {
            return null;
        }
        for (int i = 0; i < branch.size(); i++) {
            if (branch.get(i).getId().equals(part.getId())) {
                return i + 1;
            }
        }
        throw new IllegalArgumentException("The part is not on the active branch");
    }

    private int scanParts(Manuscript stored, List<ChatMessage> parts) throws Exception {
        List<WatchedPhrase> phrases = resolve(stored).stream().map(Effective::phrase).toList();
        if (phrases.isEmpty()) {
            return 0;
        }
        Map<String, AnnotationTemplate> templates = templates();
        int made = 0;
        for (ChatMessage part : parts) {
            if (part.isDeleted() || StringUtils.isBlank(part.getResponse())) {
                continue;
            }
            made += scan(stored, part, phrases, templates).size();
        }
        return made;
    }

    private List<Annotation> scan(Manuscript book, ChatMessage part, List<WatchedPhrase> phrases, Map<String, AnnotationTemplate> templates)
            throws Exception {
        String text = part.getResponse();
        List<PhraseMatcher.Hit> hits = PhraseMatcher.find(text, phrases);
        if (hits.isEmpty()) {
            return List.of();
        }
        // what the list annotated before: the same phrase at the same place is not annotated again, unless its note was resolved (the
        // phrase is still there: a redraft wrote it back, the note moved with the text onto it)
        Set<String> known = new HashSet<>();
        for (Annotation annotation : annotationService.listForPart(book, part)) {
            if (annotation.getSource() == AnnotationSource.PHRASE && annotation.hasRange()
                    && annotation.getStatus() != AnnotationStatus.RESOLVED) {
                known.add(annotation.getLabel() + "|" + annotation.getStart() + "|" + annotation.getEnd());
            }
        }
        String fallbackNote = loc.getValue(M_L.MSG_PHRASE_WATCH_NOTE);
        List<Annotation> made = new ArrayList<>();
        for (PhraseMatcher.Hit hit : hits) {
            int start = hit.start();
            int end = hit.end();
            // the passage leaves the white space at its edges out, as the annotation does
            while (start < end && Character.isWhitespace(text.charAt(start))) {
                start++;
            }
            while (end > start && Character.isWhitespace(text.charAt(end - 1))) {
                end--;
            }
            if (start >= end) {
                continue;
            }
            String label = StringUtils.abbreviate(hit.phrase().getPhrase(), LABEL_LENGTH);
            if (!known.add(label + "|" + start + "|" + end)) {
                continue;
            }
            AnnotationTemplate template = hit.phrase().getTemplate() == null ? null : templates.get(hit.phrase().getTemplate());
            String match = text.substring(start, end);
            String note = AnnotationTemplate.render(template != null && StringUtils.isNotBlank(template.getText()) ? template.getText()
                    : fallbackNote, match, hit.phrase().getPhrase());
            if (StringUtils.isBlank(note)) {
                note = AnnotationTemplate.render(fallbackNote, match, hit.phrase().getPhrase());
            }
            Annotation annotation = annotationService.create(book, part, start, end, note, AnnotationSource.PHRASE,
                    template == null ? null : template.getType(), label);
            if (template != null && template.isSendToPrompt()) {
                annotation = annotationService.update(book, annotation, null, null, true);
            }
            made.add(annotation);
        }
        return made;
    }

    private Map<String, AnnotationTemplate> templates() throws Exception {
        Map<String, AnnotationTemplate> templates = new HashMap<>();
        for (AnnotationTemplate template : getTemplates()) {
            if (template.getUuid() != null) {
                templates.put(template.getUuid(), template);
            }
        }
        return templates;
    }

    /** Read only: a user without settings has set nothing (see {@code ManuscriptServiceImpl.resolveInherited}). */
    private UserSetting userSettings() throws Exception {
        return settingService.find(UserSetting.class, currentUser);
    }
}
