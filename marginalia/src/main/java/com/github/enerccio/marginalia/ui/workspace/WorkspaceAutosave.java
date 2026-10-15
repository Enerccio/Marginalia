package com.github.enerccio.marginalia.ui.workspace;

import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.ui.Restorable;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Keeps the {@link Restorable} state of a workspace in the {@link UserSetting} of the user, so the next login opens the
 * workspace where it was left. Shared by the desktop and the phone workspace.
 * <p>
 * Nothing is saved until {@link #restore()} has run: the workspace builds its tabs (and so fires its own change
 * events) before the old state is read, and saving those would overwrite it. Neither the saving nor the restoring
 * reports an error to the user, the autosave is a convenience and must never get in the way.
 */
@Configurable
public class WorkspaceAutosave {
    private static final Logger log = LoggerFactory.getLogger(WorkspaceAutosave.class);

    @Autowired
    private SettingService settingService;

    private final Restorable root;
    private boolean active;
    private boolean restoring;
    private String lastSaved;

    public WorkspaceAutosave(Restorable root) {
        this.root = root;
    }

    /**
     * Stores the current state, unless it is the one stored already, the old state is being restored or it was not
     * restored yet.
     */
    public void save() {
        if (!active || restoring) {
            return;
        }
        try {
            JsonObject state = root.storeState();
            String serialized = state.toString();
            if (serialized.equals(lastSaved)) {
                return;
            }
            UserSetting setting = settingService.getOrCreate(UserSetting.class);
            setting.setWorkspaceState(state);
            settingService.save(setting);
            lastSaved = serialized;
        } catch (Exception e) {
            log.warn("Could not save the state of the workspace", e);
        }
    }

    /**
     * Restores the stored state and starts saving. The changes the restoring makes to the UI are not saved.
     */
    public void restore() {
        restoring = true;
        try {
            JsonObject state = settingService.getOrCreate(UserSetting.class).getWorkspaceState();
            if (state != null && state.size() > 0) {
                root.restoreState(state);
                lastSaved = state.toString();
            }
        } catch (Exception e) {
            log.warn("Could not restore the state of the workspace", e);
        } finally {
            restoring = false;
            active = true;
        }
    }

}
