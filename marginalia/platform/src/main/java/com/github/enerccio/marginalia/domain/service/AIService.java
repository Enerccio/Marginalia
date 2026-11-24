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

    /**
     * Makes a new AI of the user with the settings of the AI: the provider, the model, the API key (or the settings
     * of the extension), the reasoning, the limits of the tokens and the attributes of extensions. The clone is not
     * shared, has no limits of users and no usage. Only the owner can clone an AI (the API key is copied), and the
     * user needs the permission to create one.
     *
     * @param name the name of the clone, the name of the AI when blank
     * @return the new AI
     * @throws SecurityException        when the AI is not the user's own or the user can't create AIs
     * @throws IllegalArgumentException when the AI does not exist or is of a type that can't be cloned
     */
    AI cloneAI(AI source, String name) throws Exception;

}
