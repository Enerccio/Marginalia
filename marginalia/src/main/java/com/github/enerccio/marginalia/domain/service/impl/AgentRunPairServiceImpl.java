package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.AgentRunPairKind;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.AgentRunPair;
import com.github.enerccio.marginalia.domain.repository.AgentRunPairRepository;
import com.github.enerccio.marginalia.domain.service.AgentRunPairService;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import com.github.enerccio.marginalia.domain.service.agent.HandleTranslator;
import com.github.enerccio.marginalia.domain.service.agent.UuidPair;
import com.github.enerccio.marginalia.domain.service.command.CommandScope;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AgentRunPairServiceImpl implements AgentRunPairService {

    private static final Logger log = LoggerFactory.getLogger(AgentRunPairServiceImpl.class);

    private static final Pattern UUID = Pattern.compile("\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\b");
    private static final Pattern HANDLE = Pattern.compile(HandleTranslator.HANDLE_PATTERN);

    @Autowired
    @Lazy
    private AgentRunService agentRunService;

    // the proxy of this service: the translators call it from their own threads, the calls need their transactions
    @Autowired
    @Lazy
    private AgentRunPairService self;

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
            AgentRunPair row = new AgentRunPair(stored.getId(), pair.kind(), pair.copyUuid(), pair.originalUuid());
            row.setHandle(pair.handle());
            rows.add(row);
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
    public String handleFor(AgentRun run, AgentRunPairKind kind, String copyUuid) throws Exception {
        AgentRun stored = ownRun(run);
        AgentRunPair row = repository.findByCopy(stored.getId(), copyUuid);
        if (row != null && row.getHandle() != null) {
            return row.getHandle();
        }
        if (row == null) {
            if (kind == null || StringUtils.isBlank(copyUuid)) {
                throw new IllegalArgumentException("The item has no kind");
            }
            // made by the agent: it has no original yet
            row = new AgentRunPair(stored.getId(), kind, copyUuid, null);
        }
        char letter = letter(row.getKind());
        row.setHandle("h:" + letter + (repository.maxHandleNumber(stored.getId(), letter) + 1));
        repository.insertAll(List.of(row));
        return row.getHandle();
    }

    @Override
    @CommonTxReadOnly
    public Optional<String> findHandle(AgentRun run, String copyUuid) throws Exception {
        AgentRunPair row = repository.findByCopy(ownRun(run).getId(), copyUuid);
        return row == null ? Optional.empty() : Optional.ofNullable(row.getHandle());
    }

    @Override
    @CommonTxReadOnly
    public Optional<String> findCopyUuid(AgentRun run, String handle) throws Exception {
        AgentRunPair row = repository.findByHandle(ownRun(run).getId(), handle);
        return row == null ? Optional.empty() : Optional.of(row.getCopyUuid());
    }

    @Override
    public HandleTranslator translator(AgentRun run, CommandScope scope) throws Exception {
        return new RunHandles(ownRun(run), scope);
    }

    private static char letter(AgentRunPairKind kind) {
        return switch (kind) {
            case MESSAGE -> 'm';
            case SUMMARY -> 's';
            case LOREBOOK -> 'l';
            case ENTRY -> 'e';
        };
    }

    /**
     * The handles of one run. What was translated is remembered: the same uuid is asked about again and again in the
     * results of a run, and the handles never change.
     */
    private final class RunHandles implements HandleTranslator {

        private final AgentRun run;
        private final CommandScope scope;
        private final Map<String, String> handles = new ConcurrentHashMap<>();
        private final Map<String, String> uuids = new ConcurrentHashMap<>();

        RunHandles(AgentRun run, CommandScope scope) {
            this.run = run;
            this.scope = scope;
        }

        @Override
        public String toHandles(String text) {
            if (text == null || text.length() < 36) {
                return text;
            }
            Matcher matcher = UUID.matcher(text);
            StringBuilder result = new StringBuilder();
            while (matcher.find()) {
                String handle = handleOf(matcher.group().toLowerCase());
                matcher.appendReplacement(result, Matcher.quoteReplacement(handle == null ? matcher.group() : handle));
            }
            matcher.appendTail(result);
            return result.toString();
        }

        /** The handle of the uuid; null when it is not an item of the copy. */
        private String handleOf(String uuid) {
            String known = handles.get(uuid);
            if (known != null) {
                return known;
            }
            try {
                Optional<String> given = self.findHandle(run, uuid);
                if (given.isEmpty()) {
                    AgentRunPairKind kind = self.findByCopy(run, uuid).map(UuidPair::kind).orElseGet(() -> kindOf(uuid));
                    if (kind == null) {
                        return null;
                    }
                    given = Optional.of(self.handleFor(run, kind, uuid));
                }
                handles.put(uuid, given.get());
                uuids.put(given.get(), uuid);
                return given.get();
            } catch (Exception e) {
                log.warn("Agent run {}: the handle of {} could not be made: {}", run.getUuid(), uuid, e.getMessage());
                return null;
            }
        }

        /** What an item the copy has no pair for is: found among the parts and lorebooks of the book of the scope. */
        private AgentRunPairKind kindOf(String uuid) {
            if (scope == null) {
                return null;
            }
            try {
                scope.resolveMessage(uuid);
                return AgentRunPairKind.MESSAGE;
            } catch (Exception ignored) {
                // not a part
            }
            try {
                scope.resolveLorebook(uuid);
                return AgentRunPairKind.LOREBOOK;
            } catch (Exception ignored) {
                // not a lorebook
            }
            try {
                scope.resolveEntry(uuid);
                return AgentRunPairKind.ENTRY;
            } catch (Exception ignored) {
                // not an entry
            }
            return null;
        }

        @Override
        public Object fromHandles(Object value) {
            if (value instanceof String text) {
                return fromHandles(text);
            }
            if (value instanceof Map<?, ?> map) {
                Map<Object, Object> translated = new LinkedHashMap<>();
                map.forEach((key, entry) -> translated.put(key, fromHandles(entry)));
                return translated;
            }
            if (value instanceof List<?> list) {
                return list.stream().map(this::fromHandles).collect(Collectors.toCollection(ArrayList::new));
            }
            return value;
        }

        private String fromHandles(String text) {
            if (text.length() < 4 || !text.contains("h:")) {
                return text;
            }
            Matcher matcher = HANDLE.matcher(text);
            StringBuilder result = new StringBuilder();
            while (matcher.find()) {
                String uuid = uuidOf(matcher.group());
                matcher.appendReplacement(result, Matcher.quoteReplacement(uuid == null ? matcher.group() : uuid));
            }
            matcher.appendTail(result);
            return result.toString();
        }

        private String uuidOf(String handle) {
            String known = uuids.get(handle);
            if (known != null) {
                return known;
            }
            try {
                Optional<String> found = self.findCopyUuid(run, handle);
                found.ifPresent(uuid -> {
                    uuids.put(handle, uuid);
                    handles.put(uuid, handle);
                });
                return found.orElse(null);
            } catch (Exception e) {
                log.warn("Agent run {}: the item of {} could not be found: {}", run.getUuid(), handle, e.getMessage());
                return null;
            }
        }
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
        return new UuidPair(row.getKind(), row.getCopyUuid(), row.getOriginalUuid(), row.getHandle());
    }

    private AgentRun ownRun(AgentRun run) throws Exception {
        AgentRun stored = run == null ? null : agentRunService.findForUser(run.getUuid());
        if (stored == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        return stored;
    }
}
