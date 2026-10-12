package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Usage;
import com.github.enerccio.marginalia.domain.repository.UsageRepository;
import com.github.enerccio.marginalia.domain.security.AdminGuard;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.UsageService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.github.enerccio.marginalia.domain.traits.NoTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class UsageServiceImpl extends OwnedServiceImpl<Usage, UsageRepository> implements UsageService {

    private static final Logger log = LoggerFactory.getLogger(UsageServiceImpl.class);

    /**
     * The first use of an AI creates the usage; the lock is held until that transaction committed, so two parallel
     * first uses don't both create it.
     */
    private final Object createLock = new Object();

    @Autowired
    @Lazy
    private UsageService self;

    @Autowired
    private AIService aiService;

    @Autowired
    private AdminGuard adminGuard;

    @Override
    @NoTx
    public void record(AI ai, long prefillTokens, long generatedTokens) {
        if (ai == null || ai.getId() == null) {
            return;
        }
        try {
            synchronized (createLock) {
                self.add(ai, Math.max(0, prefillTokens), Math.max(0, generatedTokens));
            }
        } catch (Exception e) {
            log.warn("Failed to count the usage of AI {}", ai.getId(), e);
        }
    }

    @Override
    @CommonTx
    public void add(AI ai, long prefillTokens, long generatedTokens) throws Exception {
        if (getRepository().add(ai, currentUser, prefillTokens, generatedTokens) > 0) {
            return;
        }
        Usage usage = new Usage();
        usage.setAi(aiService.find(ai.getId()));
        usage.setPrefillTokens(prefillTokens);
        usage.setGeneratedTokens(generatedTokens);
        usage.setRequests(1);
        usage.setLastUsed(new Date());
        save(usage);
    }

    @Override
    @CommonTxReadOnly
    public Usage findForUser(AI ai) throws Exception {
        if (ai == null || ai.getId() == null) {
            return null;
        }
        return getRepository().findByAI(ai, currentUser);
    }

    @Override
    @CommonTxReadOnly
    public List<Usage> findAllForAI(AI ai) throws Exception {
        AI stored = ai == null ? null : aiService.find(ai.getId());
        if (stored == null) {
            return List.of();
        }
        boolean owner = stored.getOwner() != null && Objects.equals(stored.getOwner().getId(), currentUser.getId());
        if (!owner && !adminGuard.isAdmin()) {
            throw new SecurityException("Only the owner of the AI or an administrator can see who uses it");
        }
        return getRepository().findAllByAI(stored);
    }
}
