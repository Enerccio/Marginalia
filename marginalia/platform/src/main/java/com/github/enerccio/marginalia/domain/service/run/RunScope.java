package com.github.enerccio.marginalia.domain.service.run;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import org.hibernate.proxy.HibernateProxy;

/**
 * What {@link Runs} needs to know of the objects of the application, which it can't name: which object is the
 * container its holds are grouped by (a book in Marginalia), the container another object is in, and which objects are
 * the parts (what a generation writes and the cards show as busy). The application sets it once with
 * {@link Runs#setScope}; until then nothing is a part and nothing has a container.
 */
public interface RunScope {

    /** Knows no objects. */
    RunScope NONE = new RunScope() {
        @Override
        public Long containerOf(BaseEntity entity) {
            return null;
        }

        @Override
        public boolean isContainer(Class<?> type) {
            return false;
        }

        @Override
        public boolean isPart(Class<?> type) {
            return false;
        }

        @Override
        public String nameOf(OwnedEntity container) {
            return null;
        }
    };

    /**
     * The id of the container the entity is, or is in, null when it has none or it is not known. The entity may be a
     * detached proxy: use {@link #idOf} and do not initialize it.
     */
    Long containerOf(BaseEntity entity);

    /** Whether objects of the type are containers (a book): their runs are checked by {@link Runs.Operation}, not by holds. */
    boolean isContainer(Class<?> type);

    /** Whether objects of the type are parts. */
    boolean isPart(Class<?> type);

    /** The title of a container for the panel of the runs, null when it has none. Never called with a proxy. */
    String nameOf(OwnedEntity container);

    /** The id of an entity without initializing a proxy (it may be detached). */
    static Long idOf(Object entity) {
        if (entity instanceof HibernateProxy proxy) {
            Object id = proxy.getHibernateLazyInitializer().getIdentifier();
            return id instanceof Long value ? value : null;
        }
        return entity instanceof BaseEntity base ? base.getId() : null;
    }
}
