package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.run.RunScope;
import com.github.enerccio.marginalia.domain.service.run.Runs;

/**
 * The books and parts of Marginalia for {@link Runs}: a book is the container of the holds, a part is in its book.
 * Set as the scope of the runs when the Spring context starts (the bean's init method).
 */
public class BookRunScope implements RunScope {

    /** Makes this the scope of the runs. */
    public void install() {
        Runs.setScope(this);
    }

    @Override
    public Long containerOf(BaseEntity entity) {
        if (entity instanceof Manuscript) {
            return entity.getId();
        }
        if (entity instanceof ChatMessage part && part.getParentScript() != null) {
            return RunScope.idOf(part.getParentScript());
        }
        return null;
    }

    @Override
    public boolean isContainer(Class<?> type) {
        return type == Manuscript.class;
    }

    @Override
    public boolean isPart(Class<?> type) {
        return type == ChatMessage.class;
    }

    @Override
    public String nameOf(OwnedEntity container) {
        return container instanceof Manuscript book ? book.getName() : null;
    }
}
