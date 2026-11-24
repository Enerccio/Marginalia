package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.repository.ManuscriptRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.SharedResources;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * The files of the parts of a book the reader may view (their own, or a published one), whoever owns the book. Images
 * only when the book shows its images to readers.
 */
public class BookSharedResources implements SharedResources {

    private EntityManager entityManager;

    @Autowired
    private ManuscriptRepository manuscriptRepository;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Resource findForReader(String resourceUuid, String manuscriptUuid, User reader, boolean image) throws Exception {
        Resource resource = findOfViewableBook(resourceUuid, manuscriptUuid, reader);
        if (resource == null || resource.getMimeType() == null || resource.getMimeType().startsWith("image/") != image) {
            return null;
        }
        if (image) {
            Manuscript book = manuscriptRepository.findViewable(manuscriptUuid, reader);
            if (book == null || !book.isShowImagesToReaders()) {
                return null;
            }
        }
        return resource;
    }

    /**
     * The not deleted resource with the uuid that belongs to the owner of the book and is linked to a not deleted part
     * of the book, null when the book can't be viewed by the reader or the resource is none of these.
     */
    private Resource findOfViewableBook(String uuid, String manuscriptUuid, User reader) {
        List<Resource> resources = entityManager
                .createQuery("SELECT r FROM Resource r, Manuscript m WHERE r.uuid = :uuid AND r.deleted = false AND m.uuid = :book AND m.deleted = false"
                        + " AND (m.owner.id = :reader OR m.published = true) AND r.owner.id = m.owner.id AND r.clazz = :clazz"
                        + " AND r.objectId IN (SELECT c.id FROM ChatMessage c WHERE c.parentScript.id = m.id AND c.deleted = false)", Resource.class)
                .setParameter("uuid", uuid)
                .setParameter("book", manuscriptUuid)
                .setParameter("reader", reader.getId())
                .setParameter("clazz", ChatMessage.class.getName())
                .setMaxResults(1)
                .getResultList();
        return resources.isEmpty() ? null : resources.getFirst();
    }
}
