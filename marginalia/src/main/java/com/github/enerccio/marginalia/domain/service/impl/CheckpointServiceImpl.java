package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.model.impl.AgentMessage;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.AgentRunPairService;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import com.github.enerccio.marginalia.domain.service.AgentService;
import com.github.enerccio.marginalia.domain.service.AgentVirtualFileSystemService;
import com.github.enerccio.marginalia.domain.service.CheckpointService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService.AgenticCopy;
import com.github.enerccio.marginalia.domain.service.agent.CheckpointStore;
import com.github.enerccio.marginalia.domain.service.agent.CheckpointStore.Info;
import com.github.enerccio.marginalia.domain.service.agent.CheckpointStore.Kind;
import com.github.enerccio.marginalia.domain.service.agent.CheckpointText;
import com.github.enerccio.marginalia.domain.service.agent.RunFolder;
import com.github.enerccio.marginalia.domain.service.agent.UuidPair;
import com.github.enerccio.marginalia.domain.service.agent.VirtualFileSystem;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CheckpointServiceImpl implements CheckpointService {

    private static final Logger log = LoggerFactory.getLogger(CheckpointServiceImpl.class);

    // a saved workspace is unpacked to the size it had, with some room for the folders
    private static final long UNPACK_SLACK_BYTES = 1024 * 1024;

    @Autowired
    @Lazy
    private AgentRunService agentRunService;

    @Autowired
    @Lazy
    private AgentService agentService;

    @Autowired
    @Lazy
    private AgentVirtualFileSystemService fileSystemService;

    @Autowired
    @Lazy
    private ManuscriptService manuscriptService;

    @Autowired
    @Lazy
    private AgentRunPairService pairService;

    @Autowired
    @Lazy
    private AgentMessageService messageService;

    // by run uuid: whoever makes, deletes or restores a checkpoint of a run takes its lock
    private final Map<String, Lock> locks = new ConcurrentHashMap<>();

    private Lock lockOf(AgentRun run) {
        return locks.computeIfAbsent(run.getUuid(), uuid -> new ReentrantLock());
    }

    private AgentRun own(AgentRun run) throws Exception {
        AgentRun stored = run == null ? null : agentRunService.findForUser(run.getUuid());
        if (stored == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        return stored;
    }

    private CheckpointStore storeOf(AgentRun run) throws Exception {
        RunFolder folder = fileSystemService.getFolder(run);
        return new CheckpointStore(folder.checkpoints());
    }

    private long quotaOf(AgentRun run) throws Exception {
        long granted = agentService.getGrant(run.getOwner()).maxCheckpointBytes();
        return granted > 0 ? granted : DEFAULT_QUOTA_BYTES;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // making
    // ---------------------------------------------------------------------------------------------------------------

    @Override
    public Info ensureStart(AgentRun run) throws Exception {
        AgentRun stored = own(run);
        Lock lock = lockOf(stored);
        lock.lock();
        try {
            for (Info info : storeOf(stored).list()) {
                if (info.kind() == Kind.START) {
                    return info;
                }
            }
            return make(stored, Kind.START, "start of the run", null, 0).checkpoint();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Made makeAutomatic(AgentRun run, String label) throws Exception {
        return makeAutomatic(run, label, null);
    }

    @Override
    public Made makeAutomatic(AgentRun run, String label, int forgetLast) throws Exception {
        return makeAutomatic(run, label, null, forgetLast);
    }

    private Made makeAutomatic(AgentRun run, String label, String protectedId) throws Exception {
        return makeAutomatic(run, label, protectedId, 0);
    }

    private Made makeAutomatic(AgentRun run, String label, String protectedId, int forgetLast) throws Exception {
        AgentRun stored = own(run);
        Lock lock = lockOf(stored);
        lock.lock();
        try {
            return make(stored, Kind.AUTOMATIC, label, protectedId, forgetLast);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Made makeManual(AgentRun run, String label) throws Exception {
        AgentRun stored = own(run);
        Lock lock = lockOf(stored);
        lock.lock();
        try {
            return make(stored, Kind.MANUAL, label, null, 0);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Writes a checkpoint and decides whether it fits. The caller holds the lock of the run.
     *
     * @param protectedId a checkpoint the ring must not let go of (the one being restored), may be null
     * @param forgetLast  how many of the last rows of the transcript a rewind to this checkpoint forgets besides the ones after it
     */
    private Made make(AgentRun run, Kind kind, String label, String protectedId, int forgetLast) throws Exception {
        Manuscript copy = run.getWorkingCopy();
        if (copy == null) {
            throw new CheckpointException("The run has no working copy, there is nothing to save");
        }
        CheckpointStore store = storeOf(run);
        List<Info> before = store.list();
        long quota = quotaOf(run);
        JsonObject snapshot = manuscriptService.snapshotAgenticCopy(copy);
        List<UuidPair> pairs = pairService.getAll(run);
        VirtualFileSystem files = fileSystemService.open(run);
        try (CheckpointStore.Pending pending = store.begin(kind, label, run.getRounds(), messageService.lastRow(run, forgetLast))) {
            pending.write(snapshot, pairs, files);
            long size = pending.size();
            long used = before.stream().mapToLong(Info::bytes).sum();
            List<Info> dropped = new ArrayList<>();
            switch (kind) {
                case START -> {
                    if (used + size > quota) {
                        throw new CheckpointFullException("The checkpoint of the start needs " + CheckpointText.bytes(size)
                                + ", more than the " + CheckpointText.bytes(quota) + " the checkpoints of a run may take");
                    }
                }
                case AUTOMATIC -> dropped.addAll(ringDrops(before, size, quota, protectedId));
                case MANUAL -> {
                    long reserve = reserve(before, size);
                    if (used + size + reserve > quota) {
                        throw new CheckpointFullException(fullMessage(size, quota, used, reserve, before));
                    }
                }
            }
            Info made = pending.commit();
            for (Info gone : dropped) {
                store.delete(gone.id());
            }
            log.info("Agent run {}: checkpoint {} ({}) made, {}, {} workspace file(s){}", run.getUuid(), made.id(),
                    kind.name().toLowerCase(), CheckpointText.bytes(made.bytes()), made.workspaceFiles(),
                    dropped.isEmpty() ? "" : ", " + dropped.size() + " older automatic one(s) dropped");
            return new Made(made, dropped);
        } catch (IOException e) {
            throw new CheckpointException("The checkpoint could not be saved: " + e.getMessage());
        }
    }

    /**
     * Which automatic checkpoints the new one pushes out: the oldest ones beyond the ring, and more of them when the quota
     * is still exceeded. The start and the ones made on purpose are never touched.
     *
     * @throws CheckpointFullException when the quota can't hold the new one anyway
     */
    private static List<Info> ringDrops(List<Info> before, long size, long quota, String protectedId) {
        List<Info> automatic = before.stream().filter(info -> info.kind() == Kind.AUTOMATIC && !info.id().equals(protectedId)).toList();
        int keep = CheckpointStore.RING - 1;
        List<Info> dropped = new ArrayList<>();
        long used = before.stream().mapToLong(Info::bytes).sum();
        long autoCount = before.stream().filter(info -> info.kind() == Kind.AUTOMATIC).count();
        int next = 0;
        while (next < automatic.size() && (autoCount - dropped.size() > keep || used + size > quota)) {
            Info oldest = automatic.get(next++);
            dropped.add(oldest);
            used -= oldest.bytes();
        }
        if (used + size > quota) {
            throw new CheckpointFullException("The automatic checkpoint needs " + CheckpointText.bytes(size)
                    + " and the checkpoints made on purpose take the rest of the " + CheckpointText.bytes(quota));
        }
        return dropped;
    }

    /**
     * What the runner may still need: room for the start checkpoint if it is missing and for the ring to fill up, at the
     * size of the newest checkpoint.
     */
    private static long reserve(List<Info> before, long size) {
        long estimate = before.isEmpty() ? size : before.getLast().bytes();
        long automatic = before.stream().filter(info -> info.kind() == Kind.AUTOMATIC).count();
        boolean start = before.stream().anyMatch(info -> info.kind() == Kind.START);
        return (CheckpointStore.RING - automatic + (start ? 0 : 1)) * estimate;
    }

    private static String fullMessage(long size, long quota, long used, long reserve, List<Info> before) {
        long free = Math.max(0, quota - used - reserve);
        StringBuilder text = new StringBuilder("The checkpoint needs ").append(CheckpointText.bytes(size)).append(", but only ")
                .append(CheckpointText.bytes(free)).append(" are free: the checkpoints of a run may take ")
                .append(CheckpointText.bytes(quota)).append(", they take ").append(CheckpointText.bytes(used))
                .append(" and ").append(CheckpointText.bytes(reserve)).append(" are kept for the automatic ones.");
        if (size + reserve > quota) {
            text.append(" It would not fit even with no other checkpoint, so deleting does not help; tell the author.");
        } else if (!before.isEmpty()) {
            text.append(" Delete the checkpoints you do not need with checkpoint_delete and try again. The checkpoints:\n")
                    .append(CheckpointText.list(before));
        }
        return text.toString();
    }

    // ---------------------------------------------------------------------------------------------------------------
    // reading and deleting
    // ---------------------------------------------------------------------------------------------------------------

    @Override
    public List<Info> list(AgentRun run) throws Exception {
        return storeOf(own(run)).list();
    }

    @Override
    public Usage usage(AgentRun run) throws Exception {
        AgentRun stored = own(run);
        List<Info> infos = storeOf(stored).list();
        long used = infos.stream().mapToLong(Info::bytes).sum();
        long estimate = infos.isEmpty() ? 0 : infos.getLast().bytes();
        return new Usage(used, quotaOf(stored), reserve(infos, estimate), infos.size());
    }

    @Override
    public Info delete(AgentRun run, String id, boolean byAgent) throws Exception {
        AgentRun stored = own(run);
        Lock lock = lockOf(stored);
        lock.lock();
        try {
            CheckpointStore store = storeOf(stored);
            Info info = store.find(id);
            if (info == null) {
                throw new CheckpointException("There is no checkpoint " + id + ". The checkpoints:\n" + CheckpointText.list(store.list()));
            }
            if (byAgent) {
                if (info.kind() == Kind.START) {
                    throw new CheckpointException("The checkpoint of the start of the run can't be deleted");
                }
                List<Info> all = store.list();
                if (all.getLast().id().equals(info.id())) {
                    throw new CheckpointException("The newest checkpoint can't be deleted, so that there is always a state to go back to");
                }
            }
            store.delete(info.id());
            log.info("Agent run {}: checkpoint {} deleted{}", stored.getUuid(), info.id(), byAgent ? " by the agent" : "");
            return info;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Path file(AgentRun run, String id) throws Exception {
        Path file = storeOf(own(run)).fileOf(id);
        if (file == null) {
            throw new CheckpointException("There is no checkpoint " + id);
        }
        return file;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // restoring
    // ---------------------------------------------------------------------------------------------------------------

    @Override
    @CommonTx
    public Restored restore(AgentRun run, String id) throws Exception {
        AgentRun stored = own(run);
        Lock lock = lockOf(stored);
        lock.lock();
        try {
            CheckpointStore store = storeOf(stored);
            Info info = store.find(id);
            if (info == null) {
                throw new CheckpointException("There is no checkpoint " + id + ". The checkpoints:\n" + CheckpointText.list(store.list()));
            }
            // what the run is now, in case the restore was a mistake; it never pushes out the checkpoint that is restored
            Info safety = null;
            try {
                safety = makeAutomatic(stored, "before restoring " + info.id(), info.id()).checkpoint();
            } catch (CheckpointFullException e) {
                log.info("Agent run {}: no room for a checkpoint before the restore: {}", stored.getUuid(), e.getMessage());
            }
            try (CheckpointStore.Snapshot snapshot = store.open(info.id())) {
                AgenticCopy fresh = manuscriptService.createAgenticCopyFromSnapshot(snapshot.manuscript());
                List<UuidPair> pairs = remap(fresh.pairs(), snapshot.pairs(), pairService.getAll(stored));
                AgentRun replaced = agentRunService.replaceWorkingCopy(stored, fresh.manuscript(), pairs);
                // the files last: when this fails the database work is rolled back and the workspace is as it was
                List<String> removed = fileSystemService.open(replaced).restoreWorkspace(snapshot.zip(), CheckpointStore.WORKSPACE,
                        info.workspaceBytes() + UNPACK_SLACK_BYTES);
                log.info("Agent run {}: restored to checkpoint {}, {} workspace file(s) removed", stored.getUuid(), info.id(), removed.size());
                return new Restored(info, safety, removed, replaced);
            }
        } catch (IOException e) {
            throw new CheckpointException("The checkpoint could not be restored: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // rewinding
    // ---------------------------------------------------------------------------------------------------------------

    /**
     * How many rows of the transcript stay when the run goes back to the checkpoint: the rows up to the one it was made at.
     *
     * @throws CheckpointException when the checkpoint has no place in the conversation, or it was rewound over
     */
    private int keepOf(AgentRun run, Info info) throws Exception {
        if (info.after() == null) {
            throw new CheckpointException("The checkpoint " + info.id() + " does not know where in the conversation it was made, so the "
                    + "conversation can't go back to it. Restore it instead.");
        }
        AgentMessage row = messageService.findForUser(info.after());
        if (row == null || row.getRun() == null || !row.getRun().getUuid().equals(run.getUuid()) || row.isRewound()) {
            throw new CheckpointException("The conversation after the checkpoint " + info.id() + " was undone by a rewind to an earlier one. "
                    + "Restore it instead.");
        }
        return row.getOrdinal() + 1;
    }

    @Override
    @CommonTxReadOnly
    public Set<String> rewindable(AgentRun run) throws Exception {
        AgentRun stored = own(run);
        Set<String> ids = new HashSet<>();
        for (Info info : storeOf(stored).list()) {
            try {
                keepOf(stored, info);
                ids.add(info.id());
            } catch (CheckpointException e) {
                // restore only
            }
        }
        return ids;
    }

    @Override
    @CommonTx
    public Rewound rewind(AgentRun run, String id) throws Exception {
        AgentRun stored = own(run);
        if (!stored.getStatus().isResumable()) {
            throw new CheckpointException("The run has to be paused or stopped to go back in its conversation, it is " + stored.getStatus());
        }
        Lock lock = lockOf(stored);
        lock.lock();
        try {
            Info info = storeOf(stored).find(id);
            if (info == null) {
                throw new CheckpointException("There is no checkpoint " + id);
            }
            int keep = keepOf(stored, info);
            // the state first: it saves the state before as a checkpoint with the conversation as it is; the rows are only marked
            // after it, in the same transaction, so a failure of either leaves the database as it was
            Restored restored = restore(stored, info.id());
            AgentRun back = restored.run();
            int forgotten = messageService.rewind(back, keep);
            List<LLMChatMessage> tell = new ArrayList<>();
            // a round that was cut in the middle: the calls that are left have no result, a model can't be sent that
            List<LLMChatMessage> kept = messageService.transcript(back);
            Set<String> answered = new HashSet<>();
            LLMChatMessage calls = null;
            for (LLMChatMessage message : kept) {
                if (message.getRole() == LLMRole.ASSISTANT) {
                    calls = message.hasToolCalls() ? message : null;
                    answered.clear();
                } else if (message.getRole() == LLMRole.TOOL) {
                    answered.add(message.getToolCallId());
                }
            }
            if (calls != null) {
                for (LLMToolCall call : calls.getToolCalls()) {
                    if (!answered.contains(call.getId())) {
                        tell.add(LLMChatMessage.ofToolResult(call.getId(), Defaults.AGENT_REWOUND_CALL));
                    }
                }
            }
            StringBuilder note = new StringBuilder(String.format(Defaults.AGENT_REWOUND, info.id(), info.round(), forgotten));
            if (!restored.removedFiles().isEmpty()) {
                note.append(" The files you wrote after it are gone: ")
                        .append(restored.removedFiles().stream().limit(20).collect(java.util.stream.Collectors.joining(", ")))
                        .append(restored.removedFiles().size() > 20 ? ", ..." : ".");
            }
            tell.add(LLMChatMessage.of(LLMRole.USER, note.toString()));
            messageService.append(back, tell);
            log.info("Agent run {}: rewound to checkpoint {}, {} message(s) forgotten", stored.getUuid(), info.id(), forgotten);
            return new Rewound(restored, forgotten);
        } finally {
            lock.unlock();
        }
    }

    /**
     * The pairs of the new copy. An item of it was an item of the old copy (the new pair names that uuid as its original,
     * see {@code createAgenticCopyFromSnapshot}); it gets the original and the handle the old item has now, else those it had
     * when the checkpoint was made. The handles of items that were made after the checkpoint stay in the table without an
     * item: a handle of the transcript then points at nothing and is never given to another item.
     */
    public static List<UuidPair> remap(List<UuidPair> fresh, List<UuidPair> saved, List<UuidPair> current) {
        Map<String, UuidPair> savedByCopy = new HashMap<>();
        saved.forEach(pair -> savedByCopy.put(pair.copyUuid(), pair));
        Map<String, UuidPair> currentByCopy = new HashMap<>();
        current.forEach(pair -> currentByCopy.put(pair.copyUuid(), pair));
        List<UuidPair> pairs = new ArrayList<>();
        Set<String> taken = new HashSet<>();
        for (UuidPair made : fresh) {
            String before = made.originalUuid();
            UuidPair source = currentByCopy.containsKey(before) ? currentByCopy.get(before) : savedByCopy.get(before);
            if (source == null) {
                continue;
            }
            taken.add(before);
            pairs.add(new UuidPair(made.kind(), made.copyUuid(), source.originalUuid(), source.handle()));
        }
        for (UuidPair pair : current) {
            if (!taken.contains(pair.copyUuid()) && pair.handle() != null) {
                pairs.add(pair);
            }
        }
        return pairs;
    }
}
