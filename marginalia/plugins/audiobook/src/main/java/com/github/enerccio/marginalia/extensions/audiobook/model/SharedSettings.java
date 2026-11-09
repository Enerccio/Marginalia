package com.github.enerccio.marginalia.extensions.audiobook.model;

import java.util.ArrayList;
import java.util.List;

/**
 * What the administrator decides for the installation, kept in the settings of the installation under {@code audiobook.shared}: the speech
 * servers everybody (or chosen users) may use, and whether users may bring their own.
 */
public class SharedSettings {

    private List<SharedBackend> backends = new ArrayList<>();
    /** Whether users may add speech servers of their own; the ones they added before are kept, and not used while this is off. */
    private boolean usersMayAddOwn = true;

    public List<SharedBackend> getBackends() {
        if (backends == null) {
            backends = new ArrayList<>();
        }
        return backends;
    }

    public void setBackends(List<SharedBackend> backends) {
        this.backends = backends;
    }

    public boolean isUsersMayAddOwn() {
        return usersMayAddOwn;
    }

    public void setUsersMayAddOwn(boolean usersMayAddOwn) {
        this.usersMayAddOwn = usersMayAddOwn;
    }

    /** The backends the user may use, in the order of the administrator. */
    public List<BackendConfig> availableTo(String userUuid) {
        List<BackendConfig> available = new ArrayList<>();
        for (SharedBackend shared : getBackends()) {
            if (shared.getConfig() != null && shared.allows(userUuid)) {
                available.add(shared.getConfig());
            }
        }
        return available;
    }

    /** The shared backend with the id (whoever may use it), null when there is none. */
    public SharedBackend find(String id) {
        if (id == null) {
            return null;
        }
        return getBackends().stream().filter(b -> b.getConfig() != null && id.equals(b.getConfig().getId())).findFirst().orElse(null);
    }
}
