package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.AgentRunPair;
import com.github.enerccio.marginalia.domain.repository.AgentRunPairRepository;
import com.github.enerccio.marginalia.domain.service.AgentRunPairService;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import com.github.enerccio.marginalia.domain.service.agent.UuidPair;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.*;

public class AgentRunPairServiceImpl implements AgentRunPairService {

    private static final Logger log = LoggerFactory.getLogger(AgentRunPairServiceImpl.class);

    @Autowired
    @Lazy
    private AgentRunService agentRunService;

    private AgentRunPairRepository repository;

    public void setRepository(AgentRunPairRepository repository) {
        this.repository = repository;
    }

    @Override
    @CommonTx
    public void replace(AgentRun run, Collection<UuidPair> pairs) throws Exception {
        AgentRun stored = ownRun(run);
        Set<String> seen = new HashSet<>();
        List<AgentRunPair> rows = new ArrayList<>(pairs.size());
        for (UuidPair pair : pairs) {
            if (!seen.add(pair.copyUuid())) {
                throw new IllegalArgumentException("The item " + pair.copyUuid() + " of the copy is paired twice");
            }
            rows.add(new AgentRunPair(stored.getId(), pair.kind(), pair.copyUuid(), pair.originalUuid()));
        }
        repository.deleteByRun(stored.getId());
        repository.insertAll(rows);
        log.debug("Agent run {}: {} pairs of the working copy stored", stored.getUuid(), rows.size());
    }

    @Override
    @CommonTxReadOnly
    public List<UuidPair> getAll(AgentRun run) throws Exception {
        return repository.findAllByRun(ownRun(run).getId()).stream().map(AgentRunPairServiceImpl::toPair).toList();
    }

    @Override
    @CommonTxReadOnly
    public Optional<UuidPair> findByCopy(AgentRun run, String copyUuid) throws Exception {
        return Optional.ofNullable(repository.findByCopy(ownRun(run).getId(), copyUuid)).map(AgentRunPairServiceImpl::toPair);
    }

    @Override
    @CommonTxReadOnly
    public Optional<UuidPair> findByOriginal(AgentRun run, String originalUuid) throws Exception {
        return Optional.ofNullable(repository.findByOriginal(ownRun(run).getId(), originalUuid)).map(AgentRunPairServiceImpl::toPair);
    }

    @Override
    @CommonTxReadOnly
    public long count(AgentRun run) throws Exception {
        return repository.countByRun(ownRun(run).getId());
    }

    @Override
    @CommonTx
    public void deleteForRun(AgentRun run) throws Exception {
        if (run != null && run.getId() != null) {
            int deleted = repository.deleteByRun(run.getId());
            if (deleted > 0) {
                log.debug("Agent run {}: {} pairs of the working copy deleted", run.getUuid(), deleted);
            }
        }
    }

    private static UuidPair toPair(AgentRunPair row) {
        return new UuidPair(row.getKind(), row.getCopyUuid(), row.getOriginalUuid());
    }

    private AgentRun ownRun(AgentRun run) throws Exception {
        AgentRun stored = run == null ? null : agentRunService.findForUser(run.getUuid());
        if (stored == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        return stored;
    }
}
