package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.CleanupService.EntityKey;
import com.github.enerccio.marginalia.domain.service.ManuscriptSizeService;
import com.github.enerccio.marginalia.domain.service.TrashPolicy;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.*;

/**
 * The trash of the application: parts and lorebook entries can be deleted for good (with their summaries, annotations, prompts,
 * revisions and tag links), the children of a deleted part move to its parent, and the size of the books is noted again.
 */
public class BookTrashPolicy implements TrashPolicy {

    private static final Logger log = LoggerFactory.getLogger(BookTrashPolicy.class);
    private static final int CHUNK_SIZE = 500;
    private static final List<Class<?>> PERMANENTLY_DELETABLE = List.of(ChatMessage.class, LorebookEntry.class);
    // what goes with a permanently deleted object
    private static final Set<Class<?>> OWNED_ON_DELETE = Set.of(Summary.class, TagRelation.class, AnchoredPassage.class, MessagePrompt.class,
            MessageRevision.class);

    @Autowired
    @Lazy
    private ManuscriptSizeService sizeService;

    private EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Class<?>> permanentlyDeletable() {
        return PERMANENTLY_DELETABLE;
    }

    @Override
    public Set<Class<?>> ownedOnDelete() {
        return OWNED_ON_DELETE;
    }

    /**
     * The books whose parts (or the books themselves) the trash brings back or takes away: their size may have changed.
     */
    @Override
    public void changed(Set<EntityKey> selection) {
        try {
            Set<Long> books = new LinkedHashSet<>();
            List<Long> parts = new ArrayList<>();
            for (EntityKey key : selection) {
                if (key.type() == ChatMessage.class) {
                    parts.add(key.id());
                } else if (key.type() == Manuscript.class) {
                    books.add(key.id());
                }
            }
            for (List<Long> chunk : chunks(parts)) {
                books.addAll(entityManager.createQuery("SELECT DISTINCT m.parentScript.id FROM ChatMessage m WHERE m.id IN :ids", Long.class)
                        .setParameter("ids", chunk)
                        .getResultList());
            }
            books.forEach(sizeService::touch);
        } catch (RuntimeException e) {
            log.warn("Failed to note the books whose size the trash changed", e);
        }
    }

    /**
     * Moves the children of the messages to the nearest ancestor that is not among the messages (deleted children
     * too, a restored one finds its parent) and the active leaf of the books on them to it, like the delete of a
     * message in the editor does.
     */
    @Override
    public void beforePermanentDelete(Set<EntityKey> selection) {
        List<Long> ids = selection.stream().filter(key -> key.type() == ChatMessage.class).map(EntityKey::id).toList();
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, Long> parentOf = new HashMap<>();
        for (List<Long> chunk : chunks(ids)) {
            for (Object[] row : entityManager.createQuery("SELECT m.id, m.parent.id FROM ChatMessage m WHERE m.id IN :ids", Object[].class)
                    .setParameter("ids", chunk).getResultList()) {
                parentOf.put(((Number) row[0]).longValue(), row[1] != null ? ((Number) row[1]).longValue() : null);
            }
        }
        Set<Long> removed = new HashSet<>(ids);
        for (Long id : ids) {
            Long target = parentOf.get(id);
            while (target != null && removed.contains(target)) {
                target = parentOf.get(target);
            }
            ChatMessage newParent = target != null ? entityManager.getReference(ChatMessage.class, target) : null;
            entityManager.createQuery("UPDATE ChatMessage m SET m.parent = :parent WHERE m.parent.id = :id")
                    .setParameter("parent", newParent)
                    .setParameter("id", id)
                    .executeUpdate();
            entityManager.createQuery("UPDATE Manuscript b SET b.activeLeaf = :parent WHERE b.activeLeaf.id = :id")
                    .setParameter("parent", newParent)
                    .setParameter("id", id)
                    .executeUpdate();
        }
        entityManager.flush();
        entityManager.clear();
    }

    private static List<List<Long>> chunks(List<Long> ids) {
        List<List<Long>> chunks = new ArrayList<>();
        for (int i = 0; i < ids.size(); i += CHUNK_SIZE) {
            chunks.add(ids.subList(i, Math.min(ids.size(), i + CHUNK_SIZE)));
        }
        return chunks;
    }
}
