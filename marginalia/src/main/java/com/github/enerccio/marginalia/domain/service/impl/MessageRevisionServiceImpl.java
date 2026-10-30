package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.components.RevisionData;
import com.github.enerccio.marginalia.domain.components.RevisionRow;
import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessageRevision;
import com.github.enerccio.marginalia.domain.repository.MessageRevisionRepository;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.MessageRevisionService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MessageRevisionServiceImpl extends ExtendableServiceImpl<MessageRevision, MessageRevisionRepository> implements MessageRevisionService {

    // the part service deletes and restores the versions with the parts
    @Autowired
    @Lazy
    private ChatMessageService chatMessageService;

    @Override
    @CommonTx
    public MessageRevision record(ChatMessage part, RevisionSource source, String detail, boolean agent) throws Exception {
        if (part == null || part.getId() == null) {
            throw new IllegalArgumentException("The part is not saved");
        }
        return record(part, RevisionData.of(part, source, detail, agent));
    }

    @Override
    @CommonTx
    public MessageRevision record(ChatMessage part, RevisionData data) throws Exception {
        if (part == null || part.getId() == null) {
            throw new IllegalArgumentException("The part is not saved");
        }
        String text = data.text();
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        MessageRevision latest = getRepository().findLatest(part);
        if (latest != null && text.equals(latest.getResponse())) {
            return null;
        }
        RevisionSource kind = data.source() == null ? RevisionSource.HUMAN : data.source();
        MessageRevision revision = new MessageRevision();
        revision.setMessage(part);
        // the owner of the part, whoever records it (a worker, an agent merge)
        revision.setOwner(part.getOwner());
        revision.setSource(kind);
        revision.setAgent(data.agent());
        revision.setSourceDetail(StringUtils.abbreviate(StringUtils.trimToNull(data.detail()), 255));
        revision.setLabel(StringUtils.abbreviate(StringUtils.trimToNull(data.label()), 255));
        revision.setWordCount(data.wordCount() != null ? data.wordCount() : chatMessageService.countWords(text));
        revision.setTokenCount(data.tokenCount() != null ? data.tokenCount() : text.equals(part.getResponse()) ? part.getTokenCount() : 0L);
        revision.setResponse(text);
        revision.setResponseReasoning(StringUtils.defaultIfEmpty(data.reasoning(), null));
        return save(revision);
    }

    @Override
    @CommonTxReadOnly
    public MessageRevision findLatest(ChatMessage part) throws Exception {
        MessageRevision latest = getRepository().findLatest(part);
        return latest != null && ownedByCurrentUser(latest) ? latest : null;
    }

    @Override
    @CommonTxReadOnly
    public List<RevisionRow> list(ChatMessage part, boolean includeDeleted) throws Exception {
        if (part == null || !ownedByCurrentUser(part)) {
            return List.of();
        }
        return getRepository().findRows(part, includeDeleted);
    }

    @Override
    @CommonTxReadOnly
    public Map<Long, Long> countFor(Collection<ChatMessage> parts) throws Exception {
        List<Long> ids = parts.stream().filter(part -> part != null && ownedByCurrentUser(part)).map(ChatMessage::getId)
                .filter(Objects::nonNull).toList();
        return getRepository().countByPartIds(ids);
    }

    @Override
    @CommonTx
    public void softDeleteForManuscript(Manuscript manuscript) throws Exception {
        getRepository().softDeleteByManuscript(manuscript);
    }

    @Override
    @CommonTx
    public void softDeleteForParts(Collection<Long> partIds) throws Exception {
        getRepository().softDeleteByPartIds(partIds);
    }

    @Override
    @CommonTx
    public void hardDeleteForParts(Collection<Long> partIds) throws Exception {
        getRepository().hardDeleteByPartIds(partIds);
    }

    private boolean ownedByCurrentUser(OwnedEntity entity) {
        return entity.getOwner() != null && Objects.equals(entity.getOwner().getId(), currentUser.getId());
    }
}
