package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.components.BookPlacement;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.ManuscriptRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.search.LikePatterns;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptSearchCriteria;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptSearchCriteria.TagMatch;
import com.github.enerccio.marginalia.domain.service.search.Sorter;
import jakarta.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class JpaManuscriptRepository extends JpaExtendableRepository<Manuscript> implements ManuscriptRepository {

    @Override
    protected Class<Manuscript> getEntityClass() {
        return Manuscript.class;
    }

    /**
     * A change of a book (saved, deleted) is a change of the folders it is in, see {@link FolderModification}. Not for
     * {@code saveWithoutModification}, nor for opening and moving a book, which leave the book's own modification alone too.
     */
    @Override
    protected void changed(Manuscript book) throws Exception {
        // the working copy of an agent is no content of the folder
        if (book.getFolder() != null && !book.isAgentic()) {
            FolderModification.touch(entityManager, book.getFolder().getId(), new Date());
        }
    }

    @Override
    public Manuscript findViewable(String uuid, User user) throws Exception {
        List<Manuscript> results = getEntityManager().createQuery(
                        "SELECT m FROM Manuscript m WHERE m.uuid = :uuid AND m.deleted = false AND (m.owner.id = :ownerId OR m.published = true)",
                        Manuscript.class)
                .setParameter("uuid", uuid)
                .setParameter("ownerId", user.getId())
                .setMaxResults(1)
                .getResultList();
        return results.isEmpty() ? null : hydrate(results.getFirst());
    }

    @Override
    public void markOpened(Long id, User user) throws Exception {
        // bulk update, so opening a book doesn't touch modification or extended content
        getEntityManager().createQuery("UPDATE Manuscript m SET m.lastOpened = :now WHERE m.id = :id AND m.owner.id = :ownerId")
                .setParameter("now", new Date())
                .setParameter("id", id)
                .setParameter("ownerId", user.getId())
                .executeUpdate();
    }

    @Override
    public List<Manuscript> findRegularByFolders(Collection<Long> folderIds, User user) throws Exception {
        List<Manuscript> books = new ArrayList<>();
        for (List<Long> chunk : chunks(folderIds)) {
            books.addAll(getEntityManager().createQuery(
                            "SELECT m FROM Manuscript m WHERE m.deleted = false AND m.agentic = false AND m.owner.id = :ownerId "
                                    + "AND m.folder.id IN :ids ORDER BY m.id", Manuscript.class)
                    .setParameter("ownerId", user.getId())
                    .setParameter("ids", chunk)
                    .getResultList());
        }
        for (Manuscript book : books) {
            hydrate(book);
        }
        return books;
    }

    @Override
    public Map<Long, Long> countRegularByFolders(User user) throws Exception {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : getEntityManager().createQuery(
                        "SELECT m.folder.id, COUNT(m) FROM Manuscript m WHERE m.deleted = false AND m.agentic = false "
                                + "AND m.owner.id = :ownerId AND m.folder IS NOT NULL GROUP BY m.folder.id", Object[].class)
                .setParameter("ownerId", user.getId())
                .getResultList()) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    @Override
    public Set<String> findExistingRegularUuids(Collection<String> uuids, User user) throws Exception {
        Set<String> existing = new HashSet<>();
        for (List<String> chunk : chunks(uuids)) {
            existing.addAll(getEntityManager().createQuery(
                            "SELECT m.uuid FROM Manuscript m WHERE m.deleted = false AND m.agentic = false AND m.owner.id = :ownerId "
                                    + "AND m.uuid IN :uuids", String.class)
                    .setParameter("ownerId", user.getId())
                    .setParameter("uuids", chunk)
                    .getResultList());
        }
        return existing;
    }

    @Override
    public List<BookPlacement> findPlacements(User user) throws Exception {
        List<BookPlacement> placements = new ArrayList<>();
        for (Object[] row : getEntityManager().createQuery(
                        "SELECT m.uuid, m.name, f.id FROM Manuscript m LEFT JOIN m.folder f WHERE m.deleted = false "
                                + "AND m.agentic = false AND m.owner.id = :ownerId", Object[].class)
                .setParameter("ownerId", user.getId())
                .getResultList()) {
            placements.add(new BookPlacement((String) row[0], (String) row[1], (Long) row[2]));
        }
        return placements;
    }

    @Override
    public int moveToFolder(Collection<Long> bookIds, Folder folder, User user) throws Exception {
        int moved = 0;
        for (List<Long> chunk : chunks(bookIds)) {
            moved += getEntityManager().createQuery(
                            "UPDATE Manuscript m SET m.folder = :folder WHERE m.id IN :ids AND m.owner.id = :ownerId")
                    .setParameter("folder", folder)
                    .setParameter("ids", chunk)
                    .setParameter("ownerId", user.getId())
                    .executeUpdate();
        }
        return moved;
    }

    @Override
    public int moveFolderContent(Collection<Long> fromFolderIds, Folder to, User user) throws Exception {
        int moved = 0;
        for (List<Long> chunk : chunks(fromFolderIds)) {
            moved += getEntityManager().createQuery(
                            "UPDATE Manuscript m SET m.folder = :folder WHERE m.folder.id IN :ids AND m.owner.id = :ownerId")
                    .setParameter("folder", to)
                    .setParameter("ids", chunk)
                    .setParameter("ownerId", user.getId())
                    .executeUpdate();
        }
        return moved;
    }

    @Override
    public List<Manuscript> findAllByAgentic(boolean agentic, User user) throws Exception {
        List<Manuscript> books = getEntityManager().createQuery(
                        "SELECT m FROM Manuscript m WHERE m.deleted = false AND m.owner.id = :ownerId AND m.agentic = :agentic",
                        Manuscript.class)
                .setParameter("ownerId", user.getId())
                .setParameter("agentic", agentic)
                .getResultList();
        for (Manuscript book : books) {
            hydrate(book);
        }
        return books;
    }

    @Override
    public List<Long> searchManuscripts(ManuscriptFilterValues filterValues, List<Sorter> sorters, User user) throws Exception {
        return searchManuscripts(filterValues, sorters, user, null);
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    @Override
    public List<Long> searchManuscripts(ManuscriptFilterValues filterValues, List<Sorter> sorters, User user, Boolean agentic) throws Exception {
        if (filterValues == null) {
            String agenticFilter = agentic == null ? "" : " AND m.agentic = :agentic";
            TypedQuery<Long> all = getEntityManager().createQuery("SELECT m.id FROM Manuscript m WHERE m.deleted = false AND m.owner.id = :ownerId"
                    + agenticFilter + " " + Sorter.toOrderBy("m", sorters), Long.class)
                    .setParameter("ownerId", user.getId());
            if (agentic != null) {
                all.setParameter("agentic", agentic);
            }
            return all.getResultList();
        } else {
            String base = "SELECT DISTINCT (m.id) FROM Manuscript m LEFT JOIN TagRelation tr ON tr.objectId = m.id LEFT JOIN Tag t ON tr.tag = t WHERE ";
            Map<String, Object> parameters = new HashMap<>();

            List<String> ands = new ArrayList<>();

            ands.add("m.deleted = false");
            ands.add("m.owner.id = :ownerId");
            parameters.put("ownerId", user.getId());
            if (agentic != null) {
                ands.add("m.agentic = :agentic");
                parameters.put("agentic", agentic);
            }

            FolderScope.add(ands, parameters, filterValues, "m.folder", "m.uuid");
            if (filterValues.getName() != null) {
                String nameFilter = filterValues.getName();
                nameFilter = LikePatterns.fromWildcards(nameFilter);
                ands.add("m.name LIKE :name ESCAPE '\\'");
                parameters.put("name", nameFilter + "%");
            }
            if (filterValues.getTags() != null && !filterValues.getTags().isEmpty()) {
                ands.add("tr.clazz = :clazz");
                parameters.put("clazz", Manuscript.class.getName());

                List<String> ors = new ArrayList<>();
                for (int i=0; i<filterValues.getTags().size(); i++) {
                    String tagKey = "tag" + i;
                    ors.add("t.value = :" + tagKey);
                    parameters.put(tagKey, filterValues.getTags().get(i));
                }
                ands.add("(" + String.join(" OR ", ors) +")");
            }

            String query = base + String.join(" AND ", ands);

            if (filterValues.getTags() != null && !filterValues.getTags().isEmpty()) {
                query += " GROUP BY m.id HAVING COUNT(DISTINCT t.value) = :tagCount";
                parameters.put("tagCount", filterValues.getTags().size());
            }

            query += Sorter.toOrderBy("m", sorters);

            TypedQuery<Long> q = getEntityManager().createQuery(query, Long.class);

            for (String key : parameters.keySet()) {
                q.setParameter(key, parameters.get(key));
            }

            return q.getResultList();
        }
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    @Override
    public List<Long> searchManuscripts(ManuscriptSearchCriteria criteria, List<Sorter> sorters, User user) throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        List<String> ands = new ArrayList<>();
        ands.add("m.deleted = false");
        ands.add("m.owner.id = :ownerId");
        ands.add("m.agentic = false");
        parameters.put("ownerId", user.getId());

        if (StringUtils.isNotBlank(criteria.getName())) {
            ands.add("m.name LIKE :name ESCAPE '\\'");
            parameters.put("name", "%" + LikePatterns.fromWildcards(criteria.getName().strip()) + "%");
        }
        range(ands, parameters, "m.creation", "created", criteria.getCreatedFrom(), criteria.getCreatedTo());
        range(ands, parameters, "m.modification", "modified", criteria.getModifiedFrom(), criteria.getModifiedTo());
        if (criteria.getMinParts() != null) {
            ands.add(PARTS + " >= :minParts");
            parameters.put("minParts", (long) criteria.getMinParts());
        }
        if (criteria.getMaxParts() != null) {
            ands.add(PARTS + " <= :maxParts");
            parameters.put("maxParts", (long) criteria.getMaxParts());
        }

        List<String> included = tagChecks(criteria.getIncludeTags(), "in", parameters);
        if (!included.isEmpty()) {
            ands.add(criteria.getIncludeMatch() == TagMatch.ALL
                    ? String.join(" AND ", included)
                    : "(" + String.join(" OR ", included) + ")");
        }
        List<String> excluded = tagChecks(criteria.getExcludeTags(), "ex", parameters);
        if (!excluded.isEmpty()) {
            ands.add(criteria.getExcludeMatch() == TagMatch.ALL
                    ? "NOT (" + String.join(" AND ", excluded) + ")"
                    : "NOT (" + String.join(" OR ", excluded) + ")");
        }
        if (!included.isEmpty() || !excluded.isEmpty()) {
            parameters.put("clazz", Manuscript.class.getName());
        }

        String query = "SELECT m.id FROM Manuscript m WHERE " + String.join(" AND ", ands) + " " + Sorter.toOrderBy("m", sorters);
        TypedQuery<Long> q = getEntityManager().createQuery(query, Long.class);
        parameters.forEach(q::setParameter);
        return q.getResultList();
    }

    private static final String PARTS = "(SELECT COUNT(c) FROM ChatMessage c WHERE c.parentScript = m AND c.deleted = false)";

    private static void range(List<String> ands, Map<String, Object> parameters, String field, String name, Date from, Date to) {
        if (from != null) {
            ands.add(field + " >= :" + name + "From");
            parameters.put(name + "From", from);
        }
        if (to != null) {
            ands.add(field + " < :" + name + "To");
            parameters.put(name + "To", to);
        }
    }

    /**
     * One "the book carries the tag" check per tag, the values are the parameters {@code prefix + number}.
     */
    private static List<String> tagChecks(List<String> tags, String prefix, Map<String, Object> parameters) {
        List<String> checks = new ArrayList<>();
        for (int i = 0; i < tags.size(); i++) {
            String name = prefix + i;
            checks.add("EXISTS (SELECT 1 FROM TagRelation tr WHERE tr.objectId = m.id AND tr.clazz = :clazz AND tr.negative = false"
                    + " AND tr.deleted = false AND tr.tag.deleted = false AND tr.tag.value = :" + name + ")");
            parameters.put(name, tags.get(i));
        }
        return checks;
    }
}
