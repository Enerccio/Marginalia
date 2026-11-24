package com.github.enerccio.marginalia.domain.security.service;

import com.github.enerccio.marginalia.domain.security.model.ApiScope;
import com.github.enerccio.marginalia.domain.security.model.ApiKey;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.repository.ApiKeyRepository;
import com.github.enerccio.marginalia.domain.service.ExtendableService;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * The bearer keys of the HTTP API ({@code /api/v1}, see {@code ApiDispatcher}). A user may have keys when they are an administrator
 * or have the permission {@code API} ({@link #mayUseApi}); a key of a user who loses the permission stops working. The key is
 * {@value #TOKEN_START}{@code <prefix>_<secret>}: it is given to the user once, by {@link #create}, and only the prefix and a SHA-256
 * hash of the key are stored. {@link #findAllForUser()} lists the keys of the current user.
 */
public interface ApiKeyService extends ExtendableService<ApiKey, ApiKeyRepository> {

    String TOKEN_START = "mrg_";

    /**
     * How many keys a user can have at once.
     */
    int MAX_KEYS_PER_USER = 25;

    /**
     * A new key and the text of it, which is not stored anywhere: show it to the user once.
     */
    record CreatedKey(ApiKey key, String token) {
    }

    /**
     * Who called the API with a valid key.
     *
     * @param user   the owner of the key, loaded from the database
     * @param scopes what the key may do now: the scopes of the key, without the administration ones while the owner is not an
     *               administrator
     */
    record Principal(ApiKey key, User user, Set<ApiScope> scopes) {
        public boolean has(ApiScope scope) {
            return scopes.contains(scope);
        }
    }

    /**
     * Whether the user may have API keys: an administrator, or a user with the permission {@code API}.
     *
     * @param user may be the session's proxy of the user
     */
    boolean mayUseApi(User user) throws Exception;

    /**
     * Creates a key of the current user.
     *
     * @param scopes  what the key may do, any combination, at least one ({@link ApiScope#WRITE} brings {@link ApiScope#READ})
     * @param expires when the key stops working, null for never
     * @throws SecurityException        when the user may not use the API, or asks for an administration scope and is not an
     *                                  administrator
     * @throws IllegalArgumentException when no scope is given, the name is blank or too long, the expiry is in the past, or the user
     *                                  has {@link #MAX_KEYS_PER_USER} keys already
     */
    CreatedKey create(String name, Collection<ApiScope> scopes, Date expires) throws Exception;

    /**
     * Changes the name, the scopes and the expiry of a key of the current user; the text of the key stays the same.
     *
     * @param scopes  as for {@link #create}; an administration scope the key does not have yet needs an administrator
     * @param expires when the key stops working, null for never; a time in the past only when it is the key's own expiry (an
     *                expired key keeps it while its name or scopes change)
     * @return the saved key
     * @throws SecurityException        when the user may not use the API, or adds an administration scope and is not an administrator
     * @throws IllegalArgumentException when the key is not the user's, no scope is given, the name is blank or too long, or the expiry
     *                                  is a new one in the past
     */
    ApiKey update(String uuid, String name, Collection<ApiScope> scopes, Date expires) throws Exception;

    /**
     * Revokes (deletes) a key of the current user; an administrator may revoke the key of anyone.
     *
     * @return whether a key was revoked
     */
    boolean revoke(String uuid) throws Exception;

    /**
     * The caller of a key, null when the key is not valid: unknown, revoked, expired, its owner deleted or no longer allowed to use the
     * API. Needs no current user. Marks the key used (at most once a minute).
     *
     * @param token the text of the key, as sent in the {@code Authorization} header
     */
    Principal authenticate(String token) throws Exception;

}
