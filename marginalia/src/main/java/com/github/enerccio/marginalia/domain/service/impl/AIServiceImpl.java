package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.settings.AppSettings;
import com.github.enerccio.marginalia.domain.repository.AIRepository;
import com.github.enerccio.marginalia.domain.security.AdminGuard;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

public class AIServiceImpl extends ExtendableServiceImpl<AI, AIRepository> implements AIService {

    @Autowired
    private AdminGuard adminGuard;

    @Autowired
    private SettingService settingService;

    @Override
    @CommonTxReadOnly
    public boolean isUserCreationAllowed() throws Exception {
        return settingService.getOrCreateApp(AppSettings.class).getUserAisAllowed();
    }

    @Override
    @CommonTx
    public void setUserCreationAllowed(boolean allowed) throws Exception {
        adminGuard.requireAdmin();
        AppSettings settings = settingService.getOrCreateApp(AppSettings.class);
        settings.setUserAisAllowed(allowed);
        settingService.save(settings);
    }

    @Override
    @CommonTxReadOnly
    public boolean canCreate() throws Exception {
        return adminGuard.isAdmin() || isUserCreationAllowed();
    }

    @Override
    @CommonTx
    public AI save(AI ai) throws Exception {
        checkCanWrite(ai);
        return super.save(ai);
    }

    @Override
    @CommonTx
    public AI saveWithoutEvent(AI ai) throws Exception {
        checkCanWrite(ai);
        return super.saveWithoutEvent(ai);
    }

    @Override
    @CommonTx
    public AI delete(AI ai, boolean hard) throws Exception {
        checkOwner(ai);
        return super.delete(ai, hard);
    }

    /**
     * Server side rules, the UI only hides what is not allowed: a new AI needs the permission to create, sharing
     * is for administrators, and other users' AIs (shared ones included) are read-only.
     */
    private void checkCanWrite(AI ai) throws Exception {
        boolean admin = adminGuard.isAdmin();
        if (ai.isShared() && !admin) {
            throw new SecurityException("Only administrators can share an AI");
        }
        if (ai.getId() == null) {
            if (!admin && !isUserCreationAllowed()) {
                throw new SecurityException("Creating AIs is disabled, use the shared ones");
            }
        } else {
            checkOwner(ai);
        }
    }

    @Override
    @CommonTxReadOnly
    public boolean isAdministrator() {
        try {
            return adminGuard.isAdmin();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read the current user", e);
        }
    }

    @Override
    public boolean isOwnedByCurrentUser(AI ai) {
        return ai.getOwner() == null || Objects.equals(ai.getOwner().getId(), currentUser.getId());
    }

    private void checkOwner(AI ai) {
        if (!isOwnedByCurrentUser(ai)) {
            throw new SecurityException("Only the owner can change an AI");
        }
    }
}
