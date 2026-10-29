package com.github.enerccio.marginalia.domain.repository.impl;

import jakarta.persistence.EntityManager;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A folder shows the last change of what is in it, so a change of a book is a change of its folder and of every folder
 * above it (the same way a change of a part of a book is a change of the book).
 */
final class FolderModification {

    private FolderModification() {
    }

    /**
     * Sets {@code modification} of the folder and of its ancestors with one bulk update, which touches neither their
     * extended content nor their dirty state.
     *
     * @param folderId the folder the changed book is in, null for a book at the top level (nothing to do)
     */
    static void touch(EntityManager entityManager, Long folderId, Date now) {
        if (folderId == null) {
            return;
        }
        Set<Long> chain = new LinkedHashSet<>();
        Long current = folderId;
        // the set also ends a (never expected) cycle
        while (current != null && chain.add(current)) {
            List<Long> parent = entityManager.createQuery("SELECT f.parent.id FROM Folder f WHERE f.id = :id", Long.class)
                    .setParameter("id", current)
                    .getResultList();
            current = parent.isEmpty() ? null : parent.getFirst();
        }
        entityManager.createQuery("UPDATE Folder f SET f.modification = :now WHERE f.id IN :ids")
                .setParameter("now", now)
                .setParameter("ids", chain)
                .executeUpdate();
    }

    /**
     * The same for a book known by its id only, whose folder is read (a working copy of an agent is no content of its folder).
     */
    static void touchOfBook(EntityManager entityManager, Long bookId, Date now) {
        List<Long> folder = entityManager.createQuery("SELECT m.folder.id FROM Manuscript m WHERE m.id = :id AND m.folder IS NOT NULL AND m.agentic = false", Long.class)
                .setParameter("id", bookId)
                .getResultList();
        if (!folder.isEmpty()) {
            touch(entityManager, folder.getFirst(), now);
        }
    }
}
