package com.github.enerccio.marginalia.extensions.audiobook.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A speech server of the administrator that users may use: its settings ({@link BackendConfig}, the keys protected as in the user's own) and who
 * may use it. A user never gets to see the address, the keys or the requests of it, only its name and model.
 */
public class SharedBackend {

    private BackendConfig config = new BackendConfig();
    /** True: every user of the installation may use it; false: only the users named in {@link #getUserUuids()}. */
    private boolean allUsers = true;
    private Set<String> userUuids = new LinkedHashSet<>();

    public BackendConfig getConfig() {
        return config;
    }

    public void setConfig(BackendConfig config) {
        this.config = config;
    }

    public boolean isAllUsers() {
        return allUsers;
    }

    public void setAllUsers(boolean allUsers) {
        this.allUsers = allUsers;
    }

    /** The uuids of the users who may use it when {@link #isAllUsers()} is false (a copy). */
    public Set<String> getUserUuids() {
        return userUuids == null ? new LinkedHashSet<>() : new LinkedHashSet<>(userUuids);
    }

    public void setUserUuids(Set<String> userUuids) {
        this.userUuids = userUuids == null ? new LinkedHashSet<>() : new LinkedHashSet<>(userUuids);
    }

    /** Whether the user may use the backend; nobody when the user is not known and the backend is not for everyone. */
    public boolean allows(String userUuid) {
        return allUsers || (userUuid != null && userUuids != null && userUuids.contains(userUuid));
    }
}
