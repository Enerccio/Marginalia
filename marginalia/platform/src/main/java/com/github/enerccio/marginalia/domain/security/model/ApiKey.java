package com.github.enerccio.marginalia.domain.security.model;

import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A bearer key of the HTTP API ({@code Authorization: Bearer mrg_<prefix>_<secret>}). The {@link #getOwner() owner} is the user the
 * key acts as. The key itself is shown once when it is created; only the {@link #getKeyPrefix() prefix} (to find it) and a SHA-256
 * hash of the whole key are stored. A revoked key is deleted.
 */
@Entity
@Table(name = "api_keys")
public class ApiKey extends ExtendableEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 32)
    private String keyPrefix;

    @Column(nullable = false, length = 64)
    private String secretHash;

    /**
     * The {@link ApiScope}s by name, separated by commas.
     */
    @Transient
    @ExtendedAttribute
    private String scopes;

    /**
     * When the key stops working, null for never.
     */
    @Transient
    @ExtendedAttribute
    private Date expires;

    /**
     * When the key was last used, written at most once a minute.
     */
    @Transient
    @ExtendedAttribute
    private Date lastUsed;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getSecretHash() {
        return secretHash;
    }

    public void setSecretHash(String secretHash) {
        this.secretHash = secretHash;
    }

    /**
     * What the key was given (names that are no scope any more are left out).
     */
    public Set<ApiScope> getScopes() {
        EnumSet<ApiScope> set = EnumSet.noneOf(ApiScope.class);
        if (scopes != null) {
            for (String name : scopes.split(",")) {
                try {
                    set.add(ApiScope.valueOf(name.trim()));
                } catch (IllegalArgumentException e) {
                    // a scope of another version
                }
            }
        }
        return set;
    }

    public void setScopes(Collection<ApiScope> scopes) {
        this.scopes = ApiScope.normalized(scopes).stream().map(Enum::name).collect(Collectors.joining(","));
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public Date getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }

    public boolean isExpired(long now) {
        return expires != null && expires.getTime() <= now;
    }
}
