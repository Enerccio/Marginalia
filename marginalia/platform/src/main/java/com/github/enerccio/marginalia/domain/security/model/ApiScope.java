package com.github.enerccio.marginalia.domain.security.model;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * What an {@link ApiKey} may do through the HTTP API; a key has any set of them. Every key may ask for the version and for itself
 * ({@code /version}, {@code /me}). The administration scopes ({@link #isAdministration()}) can be given only by an administrator and
 * work only while the owner of the key is one. Never rename a constant: the keys store them by name.
 */
public enum ApiScope {

    /**
     * The books of the user and the commands that only read ({@code readOnly}), the runs of the key's user.
     */
    READ(false),

    /**
     * Every command, also those that change or delete data; includes {@link #READ}.
     */
    WRITE(false),

    /**
     * The update of the installation: its status and log, and asking for an update ({@code /admin/update}).
     */
    UPDATE(true),

    /**
     * The extensions: list, install or replace, stop, start, remove, read their logs ({@code /admin/extensions}).
     */
    EXTENSIONS(true);

    private final boolean administration;

    ApiScope(boolean administration) {
        this.administration = administration;
    }

    public boolean isAdministration() {
        return administration;
    }

    /**
     * The scopes, with {@link #READ} added where {@link #WRITE} is.
     */
    public static Set<ApiScope> normalized(Collection<ApiScope> scopes) {
        EnumSet<ApiScope> set = scopes == null || scopes.isEmpty() ? EnumSet.noneOf(ApiScope.class) : EnumSet.copyOf(scopes);
        if (set.contains(WRITE)) {
            set.add(READ);
        }
        return set;
    }
}
