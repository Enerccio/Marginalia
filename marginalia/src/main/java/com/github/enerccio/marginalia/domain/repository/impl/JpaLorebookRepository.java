package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.repository.LorebookRepository;

import com.github.enerccio.marginalia.domain.security.model.User;

import java.util.List;

public class JpaLorebookRepository extends JpaExtendableRepository<Lorebook> implements LorebookRepository {

    @Override
    protected Class<Lorebook> getEntityClass() {
        return Lorebook.class;
    }

    @Override
    public List<Lorebook> findAllByAgentic(boolean agentic, User user) throws Exception {
        List<Lorebook> books = getEntityManager().createQuery(
                        "SELECT l FROM Lorebook l WHERE l.deleted = false AND l.owner.id = :ownerId AND l.agentic = :agentic",
                        Lorebook.class)
                .setParameter("ownerId", user.getId())
                .setParameter("agentic", agentic)
                .getResultList();
        for (Lorebook book : books) {
            hydrate(book);
        }
        return books;
    }

    private static final String SHARED = "SELECT l FROM Lorebook l WHERE l.deleted = false AND l.shared = true AND l.agentic = false"
            + " AND l.owner.id <> :userId AND l.owner.deleted = false";

    @Override
    public List<Lorebook> findAllShared(User user) throws Exception {
        List<Lorebook> books = getEntityManager().createQuery(SHARED, Lorebook.class)
                .setParameter("userId", user.getId())
                .getResultList();
        for (Lorebook book : books) {
            hydrate(book);
        }
        return books;
    }

    @Override
    public Lorebook findShared(String uuid, User user) throws Exception {
        List<Lorebook> books = getEntityManager().createQuery(SHARED + " AND l.uuid = :uuid", Lorebook.class)
                .setParameter("userId", user.getId())
                .setParameter("uuid", uuid)
                .setMaxResults(1)
                .getResultList();
        return books.isEmpty() ? null : hydrate(books.getFirst());
    }
}
