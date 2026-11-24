package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.service.CleanupService.EntityKey;

import java.util.List;
import java.util.Set;

/**
 * What the application adds to the trash ({@link TrashService}): which kinds of objects may be deleted for good and what goes with
 * them, and the work around a restore or a delete that only the application knows (the parts of a book hang in a tree, the size of a
 * book is kept). The trash calls it from its own transaction; without one, nothing can be deleted for good.
 */
public interface TrashPolicy {

    /** No kind can be deleted for good, nothing to do. */
    TrashPolicy NONE = new TrashPolicy() {
    };

    /**
     * The kinds of objects that can be deleted for good from the trash, see {@link TrashService#getPermanentlyDeletableTypes()}.
     */
    default List<Class<?>> permanentlyDeletable() {
        return List.of();
    }

    /**
     * The kinds that go with a deleted object without being a reason to refuse the delete (its summaries, its annotations, its
     * tag links); anything else that belongs to it blocks the delete.
     */
    default Set<Class<?>> ownedOnDelete() {
        return Set.of();
    }

    /**
     * The trash brings these back or takes them away with bulk updates that no service hears.
     */
    default void changed(Set<EntityKey> selection) {
    }

    /**
     * Right before these are deleted for good, in the same transaction (a rollback undoes it when the delete is refused): what
     * must not point at them any more is moved.
     */
    default void beforePermanentDelete(Set<EntityKey> selection) {
    }
}
