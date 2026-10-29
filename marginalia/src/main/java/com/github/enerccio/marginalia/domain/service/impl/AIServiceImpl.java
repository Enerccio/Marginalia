package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.model.impl.Usage;
import com.github.enerccio.marginalia.domain.repository.AIRepository;
import com.github.enerccio.marginalia.domain.security.AdminGuard;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.security.service.PermissionService;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.UsageService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

public class AIServiceImpl extends ExtendableServiceImpl<AI, AIRepository> implements AIService {

    @Autowired
    private AdminGuard adminGuard;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UsageService usageService;

    @Override
    @CommonTxReadOnly
    public boolean canCreate() throws Exception {
        return adminGuard.isAdmin() || permissionService.checkPermissions(currentUser, PermissionType.CREATE_AI);
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
    public AI saveWithoutModification(AI ai) throws Exception {
        checkCanWrite(ai);
        return super.saveWithoutModification(ai);
    }

    @Override
    @CommonTx
    public AI delete(AI ai, boolean hard) throws Exception {
        checkOwner(ai);
        if (!hard && ai.getId() != null) {
            for (Usage usage : usageService.findAllForAI(ai)) {
                usageService.delete(usage, false);
            }
        }
        return super.delete(ai, hard);
    }

    @Override
    @CommonTx
    public AI cloneAI(AI source, String name) throws Exception {
        AI original = source == null ? null : findForUser(source.getUuid());
        if (original == null) {
            throw new IllegalArgumentException("The AI does not exist");
        }
        // the API key is copied, so a shared AI of somebody else is not for cloning
        checkOwner(original);

        AI copy;
        if (original instanceof OpenAICompatible openAI) {
            OpenAICompatible target = new OpenAICompatible();
            target.setUri(openAI.getUri());
            target.setModel(openAI.getModel());
            target.setModelName(openAI.getModelName());
            target.setApiKey(openAI.getApiKey());
            target.setRequestTimeoutSeconds(openAI.getRequestTimeoutSeconds());
            target.setMaxRetries(openAI.getMaxRetries());
            target.setTokenizer(openAI.getTokenizer());
            if (openAI.getAdditionalParameters() != null) {
                target.setAdditionalParameters(openAI.getAdditionalParameters().deepCopy());
            }
            copy = target;
        } else if (original instanceof ExtensionAI extensionAI) {
            ExtensionAI target = new ExtensionAI();
            target.setExtensionId(extensionAI.getExtensionId());
            copy = target;
        } else {
            throw new IllegalArgumentException("The AI can't be cloned: " + original.getAiType());
        }
        copy.setAiType(original.getAiType());
        copy.setName(StringUtils.isBlank(name) ? original.getName() : name);
        copy.setJailbreak(original.getJailbreak());
        copy.setNeedsJailbreak(original.getNeedsJailbreak());
        copy.setEnabledReasoning(original.getEnabledReasoning());
        copy.setReasoningEffort(original.getReasoningEffort());
        copy.setMaxCompletionTokens(original.getMaxCompletionTokens());
        copy.setMaxContext(original.getMaxContext());
        copy.setMaxConcurrentRequests(original.getMaxConcurrentRequests());
        if (original.getAttributes() != null) {
            copy.setAttributes(original.getAttributes().deepCopy());
        }
        // not shared and without limits: that is for the administrator to set again
        return save(copy);
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
            if (!admin && !permissionService.checkPermissions(currentUser, PermissionType.CREATE_AI)) {
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
