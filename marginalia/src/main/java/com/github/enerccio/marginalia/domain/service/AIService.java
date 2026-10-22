package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.repository.AIRepository;

/**
 * The AIs of the current user are their own and the ones shared by an administrator ({@link AI#isShared()}), see
 * {@link #findAllForUser()}. Only the owner can change or delete an AI, only an administrator can share one.
 */
public interface AIService extends ExtendableService<AI, AIRepository> {

    /**
     * Whether the current user can create a new AI: administrators always, others with the permission
     * {@link com.github.enerccio.marginalia.domain.security.permissions.PermissionType#CREATE_AI}.
     */
    boolean canCreate() throws Exception;

    /**
     * Whether the current user is an administrator, for the UI to show what only administrators can do (sharing an
     * AI, limits). Read from the database like {@code AdminGuard}: the session's copy of the user does not have the
     * flag filled in, so {@code currentUser.isAdmin()} is always false in the UI.
     */
    boolean isAdministrator();

    /**
     * Whether the AI is the current user's own, as opposed to one shared by somebody else. Only own AIs can be changed.
     */
    boolean isOwnedByCurrentUser(AI ai);

}
