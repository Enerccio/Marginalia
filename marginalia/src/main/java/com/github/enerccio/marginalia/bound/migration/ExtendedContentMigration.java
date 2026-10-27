package com.github.enerccio.marginalia.bound.migration;

import com.github.enerccio.marginalia.bound.Migration;
import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.MessagePrompt;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import jakarta.persistence.metamodel.EntityType;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.util.*;

/**
 * App version 5 → 6: the extended content of every entity is stored gzipped from now on ({@code ExtendableEntity}), the
 * rows that were saved before are compressed here; and the built prompt and the lore of a part ({@code builtPrompt}, {@code backgroundLore}) are in their
 * own table ({@link MessagePrompt}, V20 adds it), not in the extended content of the part. Every part that has them
 * there (deleted ones too, they can be restored) gets a row and the keys are removed from its extended content. The
 * prompt of a deleted part is deleted with it. A part that already has a prompt row is left as it is (its keys are only
 * removed), so the migration can be repeated after a failure. Finally the database is vacuumed, as it holds the space
 * of the removed text until it is. Both steps leave what is done as it is, so the migration can be repeated after a failure.
 */
public class ExtendedContentMigration implements Migration {
    private static final Logger log = LoggerFactory.getLogger(ExtendedContentMigration.class);

    // a part weighs hundreds of kilobytes with its prompt
    private static final int BATCH = 25;
    private static final int WRITE_ATTEMPTS = 3;

    private final Gson gson = new Gson();

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Override
    public int migrate(int cversion, MigrationType migrationType) throws Exception {
        if (migrationType != MigrationType.APP || cversion != 5) {
            return cversion;
        }

        int moved = move();
        log.info("Moved the prompt of {} messages to their own table", moved);
        int compressed = 0;
        for (EntityType<?> type : entityManagerFactory.getMetamodel().getEntities()) {
            if (ExtendableEntity.class.isAssignableFrom(type.getJavaType())) {
                int done = compress(type.getName());
                log.info("Compressed the extended content of {} {} rows", done, type.getName());
                compressed += done;
            }
        }
        if (moved > 0 || compressed > 0) {
            vacuum();
        }
        return 6;
    }

    /**
     * A part whose prompt is to be moved: what the write needs, read before it (the part is not loaded again).
     */
    private record Move(Long message, Long owner, boolean deleted, Date modification, String prompt, String lore,
                        byte[] content, boolean hasRow) {}

    /**
     * Walks the table by id, a batch at a time, so memory stays flat and a failure keeps the finished parts. A batch is
     * read in one session and each part of it written in another: the id generator of a new row updates its sequence table on a second
     * connection, and SQLite refuses the write of a transaction that already has a read snapshot ({@code SQLITE_BUSY_SNAPSHOT},
     * "database is locked"), as it does for {@code PermissionsMigration}.
     */
    private int move() throws Exception {
        int moved = 0;
        long lastId = 0;
        while (true) {
            List<Move> moves = new ArrayList<>();
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
                    Set<Long> withRow = new HashSet<>(em
                            .createQuery("select p.message.id from MessagePrompt p where p.message.id in :ids", Long.class)
                            .setParameter("ids", batch.stream().map(ChatMessage::getId).toList())
                            .getResultList());
                    for (ChatMessage message : batch) {
                        Move move = read(message, withRow.contains(message.getId()));
                        if (move != null) {
                            moves.add(move);
                        }
                    }
                }
            }
            for (Move move : moves) {
                write(move);
            }
            moved += moves.size();
            if (read < BATCH) {
                return moved;
            }
        }
    }

    private Move read(ChatMessage message, boolean hasRow) {
        byte[] content = message.getExtendedContent();
        if (content == null || content.length < 2) {
            return null;
        }
        JsonObject root;
        try {
            root = gson.fromJson(new String(content, StandardCharsets.UTF_8), JsonObject.class);
        } catch (Exception e) {
            log.warn("The extended content of the message {} is not JSON, left alone", message.getId());
            return null;
        }
        if (root == null || !root.has(MessagePrompt.PROMPT_KEY) && !root.has(MessagePrompt.LORE_KEY)) {
            return null;
        }
        String prompt = take(root, MessagePrompt.PROMPT_KEY);
        String lore = take(root, MessagePrompt.LORE_KEY);
        return new Move(message.getId(), message.getOwner() == null ? null : message.getOwner().getId(), message.isDeleted(),
                message.getModification(), prompt, lore, ExtendableEntity.compress(gson.toJson(root).getBytes(StandardCharsets.UTF_8)), hasRow);
    }

    /**
     * One transaction for one part, so a failure leaves the parts that are done as they are. The row is persisted first (that
     * is where its id is generated, before the transaction has touched the database), the content is written by a bulk
     * update that loads nothing.
     */
    private void write(Move move) throws Exception {
        // the transaction is small and a failed one wrote nothing, so a lock another connection held for a moment is waited out
        for (int attempt = 1; ; attempt++) {
            try {
                writeOnce(move);
                return;
            } catch (PessimisticLockException | LockTimeoutException e) {
                if (attempt == WRITE_ATTEMPTS) {
                    throw e;
                }
                log.warn("The database is locked, moving the prompt of the message {} again ({}/{})", move.message(), attempt, WRITE_ATTEMPTS);
                Thread.sleep(200L * attempt);
            }
        }
    }

    private void writeOnce(Move move) throws Exception {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            try {
                boolean worth = move.prompt() != null && !move.prompt().isEmpty() || move.lore() != null && !move.lore().isEmpty();
                if (!move.hasRow() && worth) {
                    MessagePrompt row = new MessagePrompt();
                    row.setUuid(UUID.randomUUID().toString());
                    row.setCreation(new Date());
                    row.setModification(move.modification());
                    row.setOwner(move.owner() == null ? null : em.getReference(User.class, move.owner()));
                    row.setDeleted(move.deleted());
                    row.setMessage(em.getReference(ChatMessage.class, move.message()));
                    row.setBuiltPrompt(move.prompt());
                    row.setBackgroundLore(move.lore());
                    em.persist(row);
                }
                em.createQuery("update ChatMessage m set m.extendedContent = :content where m.id = :id")
                        .setParameter("content", move.content())
                        .setParameter("id", move.message())
                        .executeUpdate();
                em.getTransaction().commit();
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
    }

    /**
     * Compresses the extended content of the rows of the entity that have it plain, deleted ones too. Walks the table by id,
     * one transaction per batch.
     */
    private int compress(String entityName) throws Exception {
        int compressed = 0;
        long lastId = 0;
        while (true) {
            try (EntityManager em = entityManagerFactory.createEntityManager()) {
                em.getTransaction().begin();
                try {
                    List<? extends ExtendableEntity> batch = em
                            .createQuery("select e from " + entityName + " e where e.id > :last order by e.id", ExtendableEntity.class)
                            .setParameter("last", lastId)
                            .setMaxResults(BATCH)
                            .getResultList();
                    for (ExtendableEntity entity : batch) {
                        lastId = entity.getId();
                        if (entity.needsCompression()) {
                            entity.setExtendedContent(entity.getExtendedContent());
                            compressed++;
                        }
                    }
                    em.getTransaction().commit();
                    if (batch.size() < BATCH) {
                        return compressed;
                    }
                } catch (Exception e) {
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    throw e;
                }
            }
        }
    }

    private static String take(JsonObject object, String key) {
        JsonElement value = object.remove(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : null;
    }

    /**
     * The file of the database does not shrink by itself. A failure only leaves the space unused.
     */
    private void vacuum() {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            em.unwrap(Session.class).doWork(connection -> {
                boolean autoCommit = connection.getAutoCommit();
                connection.setAutoCommit(true);
                try (Statement statement = connection.createStatement()) {
                    statement.execute("VACUUM");
                } finally {
                    connection.setAutoCommit(autoCommit);
                }
            });
        } catch (Exception e) {
            log.warn("The database could not be vacuumed, its file keeps the space of the moved prompts", e);
        }
    }
}
