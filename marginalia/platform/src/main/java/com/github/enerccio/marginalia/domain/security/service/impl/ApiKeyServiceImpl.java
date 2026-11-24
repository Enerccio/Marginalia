package com.github.enerccio.marginalia.domain.security.service.impl;

import com.github.enerccio.marginalia.domain.security.AdminGuard;
import com.github.enerccio.marginalia.domain.security.model.ApiScope;
import com.github.enerccio.marginalia.domain.security.model.ApiKey;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.security.repository.ApiKeyRepository;
import com.github.enerccio.marginalia.domain.security.service.ApiKeyService;
import com.github.enerccio.marginalia.domain.security.service.PermissionService;
import com.github.enerccio.marginalia.domain.service.impl.ExtendableServiceImpl;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HexFormat;
import java.util.Set;

public class ApiKeyServiceImpl extends ExtendableServiceImpl<ApiKey, ApiKeyRepository> implements ApiKeyService {

    static final int MAX_NAME_LENGTH = 100;
    private static final int PREFIX_LENGTH = 12;
    private static final int SECRET_BYTES = 32;
    private static final long LAST_USED_RESOLUTION_MS = 60_000L;
    private static final String PREFIX_ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";

    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private AdminGuard adminGuard;

    @Autowired
    @Lazy
    private PermissionService permissionService;

    @Override
    @CommonTxReadOnly
    public boolean mayUseApi(User user) throws Exception {
        User loaded = user == null || user.getId() == null ? null : userService.find(user.getId());
        if (loaded == null || loaded.isDeleted()) {
            return false;
        }
        return loaded.isAdmin() || permissionService.checkPermissions(loaded, PermissionType.API);
    }

    @Override
    @CommonTx
    public CreatedKey create(String name, Collection<ApiScope> scopes, Date expires) throws Exception {
        if (!mayUseApi(currentUser)) {
            throw new SecurityException("The user may not use the API");
        }
        Set<ApiScope> given = ApiScope.normalized(scopes);
        if (given.isEmpty()) {
            throw new IllegalArgumentException("The key needs at least one scope");
        }
        if (given.stream().anyMatch(ApiScope::isAdministration)) {
            adminGuard.requireAdmin();
        }
        String trimmed = StringUtils.trimToEmpty(name);
        if (trimmed.isEmpty() || trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("The name of the key must have 1 to " + MAX_NAME_LENGTH + " characters");
        }
        if (expires != null && expires.getTime() <= System.currentTimeMillis()) {
            throw new IllegalArgumentException("The key would be expired already");
        }
        if (findAllForUser().size() >= MAX_KEYS_PER_USER) {
            throw new IllegalArgumentException("A user can have at most " + MAX_KEYS_PER_USER + " keys");
        }

        String prefix = randomPrefix();
        while (getRepository().findByPrefix(prefix) != null) {
            prefix = randomPrefix();
        }
        byte[] secret = new byte[SECRET_BYTES];
        RANDOM.nextBytes(secret);
        String token = TOKEN_START + prefix + "_" + Base64.getUrlEncoder().withoutPadding().encodeToString(secret);

        ApiKey key = new ApiKey();
        key.setName(trimmed);
        key.setKeyPrefix(prefix);
        key.setSecretHash(hash(token));
        key.setScopes(given);
        key.setExpires(expires);
        return new CreatedKey(save(key), token);
    }

    @Override
    @CommonTx
    public boolean revoke(String uuid) throws Exception {
        ApiKey key = findForUser(uuid);
        if (key == null && adminGuard.isAdmin()) {
            Long id = StringUtils.isBlank(uuid) ? null : find(uuid);
            key = id == null ? null : find(id);
        }
        if (key == null || key.isDeleted()) {
            return false;
        }
        delete(key, false);
        return true;
    }

    @Override
    @CommonTx
    public Principal authenticate(String token) throws Exception {
        String prefix = prefixOf(token);
        if (prefix == null) {
            return null;
        }
        ApiKey key = getRepository().findByPrefix(prefix);
        if (key == null || key.isDeleted()
                || !MessageDigest.isEqual(hash(token).getBytes(StandardCharsets.US_ASCII), key.getSecretHash().getBytes(StandardCharsets.US_ASCII))) {
            return null;
        }
        long now = System.currentTimeMillis();
        if (key.isExpired(now) || key.getOwner() == null) {
            return null;
        }
        User owner = userService.find(key.getOwner().getId());
        if (owner == null || owner.isDeleted() || !(owner.isAdmin() || permissionService.checkPermissions(owner, PermissionType.API))) {
            return null;
        }
        Set<ApiScope> scopes = key.getScopes();
        if (!owner.isAdmin()) {
            scopes.removeIf(ApiScope::isAdministration);
        }
        if (key.getLastUsed() == null || now - key.getLastUsed().getTime() >= LAST_USED_RESOLUTION_MS) {
            key.setLastUsed(new Date(now));
            key = saveWithoutModification(key);
        }
        return new Principal(key, owner, Set.copyOf(scopes));
    }

    /**
     * The prefix of a key of the form {@code mrg_<prefix>_<secret>}, null when the text does not have that form.
     */
    static String prefixOf(String token) {
        if (token == null || !token.startsWith(TOKEN_START) || token.length() > 200) {
            return null;
        }
        String rest = token.substring(TOKEN_START.length());
        int separator = rest.indexOf('_');
        if (separator != PREFIX_LENGTH || rest.length() == separator + 1) {
            return null;
        }
        String prefix = rest.substring(0, separator);
        for (int i = 0; i < prefix.length(); i++) {
            if (PREFIX_ALPHABET.indexOf(prefix.charAt(i)) < 0) {
                return null;
            }
        }
        return prefix;
    }

    private static String randomPrefix() {
        StringBuilder sb = new StringBuilder(PREFIX_LENGTH);
        for (int i = 0; i < PREFIX_LENGTH; i++) {
            sb.append(PREFIX_ALPHABET.charAt(RANDOM.nextInt(PREFIX_ALPHABET.length())));
        }
        return sb.toString();
    }

    /**
     * SHA-256 of the whole key as lowercase hex. The key has 256 random bits, so a slow password hash would add nothing.
     */
    static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
