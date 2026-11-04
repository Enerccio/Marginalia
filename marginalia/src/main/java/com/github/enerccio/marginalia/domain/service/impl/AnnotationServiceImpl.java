package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.AnnotationRepository;
import com.github.enerccio.marginalia.domain.service.AnnotationService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.PassageService;
import com.github.enerccio.marginalia.domain.service.agent.UuidPair;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.google.gson.JsonArray;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AnnotationServiceImpl extends ExtendableServiceImpl<Annotation, AnnotationRepository> implements AnnotationService {

    private static final Logger log = LoggerFactory.getLogger(AnnotationServiceImpl.class);

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    @Lazy
    private ChatMessageService chatMessageService;

    @Autowired
    private PassageService passageService;

    @Override
    @CommonTx
    public Annotation create(Manuscript manuscript, ChatMessage part, Integer start, Integer end, String text, AnnotationSource source,
                             String type, String label) throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage stored = ownPart(book, part);
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("The note is empty");
        }
        Annotation annotation = new Annotation();
        annotation.setPart(stored);
        annotation.setSource(source == null ? AnnotationSource.AUTHOR : source);
        annotation.setText(text);
        annotation.setType(StringUtils.trimToNull(type));
        annotation.setLabel(StringUtils.trimToNull(label));
        setRange(annotation, textOf(stored), start, end);
        return save(annotation);
    }

    @Override
    @CommonTx
    public Annotation createFromQuote(Manuscript manuscript, ChatMessage part, String quote, String before, String after, String text,
                                      AnnotationSource source, String type, String label) throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage stored = ownPart(book, part);
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("The note is empty");
        }
        Annotation annotation = new Annotation();
        annotation.setPart(stored);
        annotation.setSource(source == null ? AnnotationSource.AUTHOR : source);
        annotation.setText(text);
        annotation.setType(StringUtils.trimToNull(type));
        annotation.setLabel(StringUtils.trimToNull(label));
        String body = textOf(stored);
        PassageServiceImpl.anchorByQuote(annotation, body, quote, before, after);
        return save(annotation);
    }

    @Override
    @CommonTxReadOnly
    public List<Annotation> listForPart(Manuscript manuscript, ChatMessage part) throws Exception {
        Manuscript book = ownBook(manuscript);
        return getRepository().findAllByPart(ownPart(book, part));
    }

    @Override
    @CommonTxReadOnly
    public List<Annotation> listForManuscript(Manuscript manuscript) throws Exception {
        return getRepository().findAllByManuscript(ownBook(manuscript));
    }

    @Override
    @CommonTx
    public Annotation update(Manuscript manuscript, Annotation annotation, String text, String type, Boolean sendToPrompt) throws Exception {
        Annotation stored = ownAnnotation(ownBook(manuscript), annotation);
        if (text != null) {
            if (text.isBlank()) {
                throw new IllegalArgumentException("The note is empty");
            }
            stored.setText(text);
        }
        if (type != null) {
            stored.setType(StringUtils.trimToNull(type));
        }
        if (sendToPrompt != null) {
            stored.setSendToPrompt(sendToPrompt);
        }
        return save(stored);
    }

    @Override
    @CommonTx
    public Annotation setStatus(Manuscript manuscript, Annotation annotation, AnnotationStatus status) throws Exception {
        Annotation stored = ownAnnotation(ownBook(manuscript), annotation);
        if (status == null) {
            throw new IllegalArgumentException("The status is missing");
        }
        if (status == AnnotationStatus.ORPHANED) {
            // only a change of the text orphans an annotation
            throw new IllegalArgumentException("An annotation is orphaned by the text, not by the author");
        }
        stored.setStatus(status);
        return save(stored);
    }

    @Override
    @CommonTx
    public Annotation reattach(Manuscript manuscript, Annotation annotation, Integer start, Integer end) throws Exception {
        Annotation stored = ownAnnotation(ownBook(manuscript), annotation);
        ChatMessage part = (ChatMessage) Hibernate.unproxy(chatMessageService.find(stored.getPart()));
        setRange(stored, textOf(part), start, end);
        stored.unorphan();
        return save(stored);
    }

    @Override
    @CommonTx
    public void remove(Manuscript manuscript, Annotation annotation) throws Exception {
        delete(ownAnnotation(ownBook(manuscript), annotation), false);
    }

    // ---- keeping the ranges in line with the text: the same for every kind of passage, see PassageService

    @Override
    @CommonTx
    public void shift(Manuscript manuscript, ChatMessage part, String oldText, String newText) throws Exception {
        passageService.shift(manuscript, part, oldText, newText);
    }

    @Override
    @CommonTx
    public void moveToPrevious(Manuscript manuscript, ChatMessage later, ChatMessage previous, int offset) throws Exception {
        passageService.moveToPrevious(manuscript, later, previous, offset);
    }

    @Override
    @CommonTx
    public void relocate(Manuscript manuscript, ChatMessage part) throws Exception {
        passageService.relocate(manuscript, part);
    }

    @Override
    @CommonTx
    public void copyForBranch(ChatMessage from, ChatMessage to) throws Exception {
        passageService.copyForBranch(from, to);
    }

    // ---- deletes

    @Override
    @CommonTx
    public void softDeleteForManuscript(Manuscript manuscript) throws Exception {
        passageService.softDeleteForManuscript(manuscript);
    }

    @Override
    @CommonTx
    public void softDeleteForParts(Collection<Long> partIds) throws Exception {
        passageService.softDeleteForParts(partIds);
    }

    @Override
    @CommonTx
    public void hardDeleteForParts(Collection<Long> partIds) throws Exception {
        passageService.hardDeleteForParts(partIds);
    }

    // ---- backups

    @Override
    @CommonTxReadOnly
    public Map<Long, JsonArray> marshalForManuscript(Manuscript manuscript) throws Exception {
        return passageService.marshalForManuscript(manuscript);
    }

    @Override
    @CommonTx
    public void restoreForPart(ChatMessage part, JsonArray annotations, Collection<UuidPair> pairs) throws Exception {
        passageService.restoreForPart(part, annotations, pairs);
    }

    // ---- helpers

    private static String textOf(ChatMessage part) {
        return PassageServiceImpl.textOf(part);
    }

    private static void setRange(Annotation annotation, String text, Integer start, Integer end) {
        PassageServiceImpl.setRange(annotation, text, start, end);
    }

    private static void anchor(Annotation annotation, String text, int start, int end) {
        PassageServiceImpl.anchor(annotation, text, start, end);
    }

    private Manuscript ownBook(Manuscript manuscript) throws Exception {
        Manuscript book = manuscript == null ? null : (Manuscript) Hibernate.unproxy(manuscriptService.findForUser(manuscript.getUuid()));
        if (book == null) {
            throw new IllegalArgumentException("The manuscript does not exist");
        }
        return book;
    }

    private ChatMessage ownPart(Manuscript book, ChatMessage part) throws Exception {
        // the part may be the lazy proxy of an association loaded earlier, its extended attributes are not read from the proxy
        ChatMessage stored = part == null ? null : (ChatMessage) Hibernate.unproxy(chatMessageService.find(part));
        if (stored == null || stored.isDeleted() || stored.getParentScript() == null || !stored.getParentScript().getId().equals(book.getId())) {
            throw new IllegalArgumentException("The part does not exist");
        }
        return stored;
    }

    private Annotation ownAnnotation(Manuscript book, Annotation annotation) throws Exception {
        Annotation stored = annotation == null ? null : findForUser(annotation.getUuid());
        if (stored == null || stored.getPart() == null || stored.getPart().isDeleted() || stored.getPart().getParentScript() == null
                || !stored.getPart().getParentScript().getId().equals(book.getId())) {
            throw new IllegalArgumentException("The annotation does not exist");
        }
        return stored;
    }

}
