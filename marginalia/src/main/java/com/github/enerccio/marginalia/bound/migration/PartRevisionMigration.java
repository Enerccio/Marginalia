package com.github.enerccio.marginalia.bound.migration;

import com.github.enerccio.marginalia.bound.Migration;
import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.MessageRevision;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * App version 6 → 7: the versions of the text of a part ({@link MessageRevision}, V22 adds the table) of the parts that were
 * written before the history was kept. A part written by a model ({@code modelUsed}) starts with a {@code GENERATE} version
 * of the text as the model wrote it ({@code originalResponse}, with the reasoning and the model and protocol), and has a
 * {@code HUMAN} version of the text it has now when that is another one; any other part (typed, imported) starts with a
 * {@code HUMAN} version of its text. Deleted parts too (they can be restored), their versions are deleted with them. A part
 * without text, or one that has a version already, gets none, so the migration can be repeated after a failure. The part
 * itself is not touched ({@code modification} stays).
 * <p>
 * {@code OriginalResponseMigration} counted all the text that existed then as generated, so a part from before it that was
 * edited has the same {@code originalResponse} and {@code response}: it gets the one {@code GENERATE} version.
 */
public class PartRevisionMigration implements Migration {
    private static final Logger log = LoggerFactory.getLogger(PartRevisionMigration.class);

    private static final int BATCH = 50;
    private static final int WRITE_ATTEMPTS = 3;

    private final ExtendableEntityListener listener = new ExtendableEntityListener();

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private ChatMessageService chatMessageService;

    @Override
    public int migrate(int cversion, MigrationType migrationType) throws Exception {
        if (migrationType != MigrationType.APP || cversion != 6) {
            return cversion;
        }
        log.info("Made {} versions of the text of the existing parts", make());
        return 7;
    }

    /**
     * A version to be written: everything it needs, read before (the part is not loaded again).
     */
    private record Draft(Long message, Long owner, boolean deleted, Date creation, RevisionSource source, String detail,
                         String text, String reasoning, int words, long tokens) {}

    /**
     * Walks the table by id, a batch at a time, so memory stays flat and a failure keeps the finished parts. A batch is read in
     * one session and the versions of each part of it written in another: the id generator of a new row updates its sequence
     * table on a second connection, and SQLite refuses the write of a transaction that already has a read snapshot
     * ({@code SQLITE_BUSY_SNAPSHOT}), see {@code ExtendedContentMigration}.
     */
    private int make() throws Exception {
        int made = 0;
        long lastId = 0;
        while (true) {
            List<List<Draft>> parts = new ArrayList<>();
            int read;
            try (EntityManager em = entityManagerFactory.createEntityManager()) {
                List<ChatMessage> batch = em
                        .createQuery("select m from ChatMessage m where m.id > :last order by m.id", ChatMessage.class)
                        .setParameter("last", lastId)
                        .setMaxResults(BATCH)
                        .getResultList();
                read = batch.size();
                if (!batch.isEmpty()) {
                    lastId = batch.getLast().getId();
                    Set<Long> withVersions = new HashSet<>(em
                            .createQuery("select distinct r.message.id from MessageRevision r where r.message.id in :ids", Long.class)
                            .setParameter("ids", batch.stream().map(ChatMessage::getId).toList())
                            .getResultList());
                    for (ChatMessage message : batch) {
                        if (!withVersions.contains(message.getId())) {
                            List<Draft> drafts = drafts(message);
                            if (!drafts.isEmpty()) {
                                parts.add(drafts);
                            }
                        }
                    }
                }
            }
            for (List<Draft> drafts : parts) {
                write(drafts);
                made += drafts.size();
            }
            if (read < BATCH) {
                return made;
            }
        }
    }

    private List<Draft> drafts(ChatMessage message) {
        String response = message.getResponse();
        if (StringUtils.isBlank(response)) {
            return List.of();
        }
        Long owner = message.getOwner() == null ? null : message.getOwner().getId();
        Date created = message.getCreation() == null ? new Date() : message.getCreation();
        List<Draft> drafts = new ArrayList<>();
        String generated = StringUtils.isNotBlank(message.getModelUsed()) ? StringUtils.defaultIfBlank(message.getOriginalResponse(), response) : null;
        if (generated != null) {
            boolean same = generated.equals(response);
            drafts.add(new Draft(message.getId(), owner, message.isDeleted(), created, RevisionSource.GENERATE, detail(message), generated,
                    StringUtils.defaultIfEmpty(message.getResponseReasoning(), null),
                    same ? message.getWordCount() : chatMessageService.countWords(generated), same ? message.getTokenCount() : 0));
            if (same) {
                return drafts;
            }
        }
        // after the generated one, when the part was changed
        Date changed = message.getModification() != null && message.getModification().after(created)
                ? message.getModification() : new Date(created.getTime() + 1);
        drafts.add(new Draft(message.getId(), owner, message.isDeleted(), generated == null ? created : changed, RevisionSource.HUMAN, null,
                response, null, message.getWordCount(), message.getTokenCount()));
        return drafts;
    }

    private static String detail(ChatMessage message) {
        String model = StringUtils.trimToNull(message.getModelUsed());
        String protocol = StringUtils.trimToNull(message.getProtocolUsed());
        return protocol == null ? model : model + " · " + protocol;
    }

    /**
     * One transaction for the versions of one part, so a failure leaves the parts that are done as they are. The rows are
     * persisted before the transaction has touched the database.
     */
    private void write(List<Draft> drafts) throws Exception {
        // the transaction is small and a failed one wrote nothing, so a lock another connection held for a moment is waited out
        for (int attempt = 1; ; attempt++) {
            try {
                writeOnce(drafts);
                return;
            } catch (PessimisticLockException | LockTimeoutException e) {
                if (attempt == WRITE_ATTEMPTS) {
                    throw e;
                }
                log.warn("The database is locked, making the versions of the message {} again ({}/{})", drafts.getFirst().message(), attempt, WRITE_ATTEMPTS);
                Thread.sleep(200L * attempt);
            }
        }
    }

    private void writeOnce(List<Draft> drafts) throws Exception {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            try {
                for (Draft draft : drafts) {
                    MessageRevision revision = new MessageRevision();
                    revision.setUuid(UUID.randomUUID().toString());
                    revision.setCreation(draft.creation());
                    revision.setModification(draft.creation());
                    revision.setOwner(draft.owner() == null ? null : em.getReference(User.class, draft.owner()));
                    revision.setDeleted(draft.deleted());
                    revision.setMessage(em.getReference(ChatMessage.class, draft.message()));
                    revision.setSource(draft.source());
                    revision.setSourceDetail(draft.detail());
                    revision.setWordCount(draft.words());
                    revision.setTokenCount(draft.tokens());
                    revision.setResponse(draft.text());
                    revision.setResponseReasoning(draft.reasoning());
                    listener.serialize(revision);
                    em.persist(revision);
                }
                em.getTransaction().commit();
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
    }
}
