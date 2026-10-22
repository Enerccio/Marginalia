package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.CheckpointService;
import com.github.enerccio.marginalia.domain.service.CheckpointService.CheckpointException;
import com.github.enerccio.marginalia.domain.service.CheckpointService.Made;
import com.github.enerccio.marginalia.domain.service.CheckpointService.Usage;
import com.github.enerccio.marginalia.domain.service.agent.CheckpointStore.Info;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Supplier;

/**
 * {@link CheckpointTools} of one run. Saving, listing and deleting are done at once; the restore only notes the request,
 * the runner does it when the round is over, so that every call of the round has its result first and the later calls of
 * the round do not work on a book that is being replaced.
 */
public final class CheckpointToolsImpl implements CheckpointTools {

    private final Supplier<AgentRun> run;
    private final CheckpointService service;

    private volatile String restore;

    /**
     * @param run the run as it is now: its working copy changes with a restore
     */
    public CheckpointToolsImpl(Supplier<AgentRun> run, CheckpointService service) {
        this.run = run;
        this.service = service;
    }

    /**
     * The checkpoint the model asked to go back to in this round, null when it did not; the runner takes it.
     */
    public String takeRestore() {
        String id = restore;
        restore = null;
        return id;
    }

    @Override
    public void checkpoint(FunctionContinuation<String> done, String label) {
        try {
            Made made = service.makeManual(run.get(), label);
            Info info = made.checkpoint();
            done.complete("Checkpoint " + info.id() + " saved (" + CheckpointText.bytes(info.bytes()) + ", " + info.workspaceFiles()
                    + " workspace file" + (info.workspaceFiles() == 1 ? "" : "s") + "). " + usageLine(service.usage(run.get())));
        } catch (CheckpointException e) {
            done.fail(e.getMessage());
        } catch (Exception e) {
            done.fail("The checkpoint could not be saved: " + e.getMessage());
        }
    }

    @Override
    public void list(FunctionContinuation<String> done) {
        try {
            List<Info> infos = service.list(run.get());
            done.complete(CheckpointText.list(infos) + (infos.isEmpty() ? "" : "\n\n") + usageLine(service.usage(run.get())));
        } catch (Exception e) {
            done.fail("The checkpoints could not be listed: " + e.getMessage());
        }
    }

    @Override
    public void restore(FunctionContinuation<String> done, String id) {
        try {
            String normal = CheckpointStore.normalizeId(id);
            List<Info> infos = service.list(run.get());
            Info info = normal == null ? null : infos.stream().filter(candidate -> candidate.id().equals(normal)).findFirst().orElse(null);
            if (info == null) {
                done.fail("There is no checkpoint " + StringUtils.abbreviate(StringUtils.defaultString(id), 40) + ". The checkpoints:\n"
                        + CheckpointText.list(infos));
                return;
            }
            this.restore = info.id();
            done.complete("The run goes back to checkpoint " + info.id() + " when this round ends: your working copy and your "
                    + "workspace are replaced by what was saved. Do not change anything else in this round, it would be lost.");
        } catch (Exception e) {
            done.fail("The restore could not be asked for: " + e.getMessage());
        }
    }

    @Override
    public void diff(FunctionContinuation<String> done, String id, String path) {
        try {
            done.complete(CheckpointText.difference(service.diff(run.get(), id, path)));
        } catch (CheckpointException | VirtualFileException e) {
            done.fail(e.getMessage());
        } catch (Exception e) {
            done.fail("The checkpoint could not be compared: " + e.getMessage());
        }
    }

    @Override
    public void delete(FunctionContinuation<String> done, String id) {
        try {
            Info info = service.delete(run.get(), id, true);
            done.complete("Checkpoint " + info.id() + " deleted (" + CheckpointText.bytes(info.bytes()) + " freed). "
                    + usageLine(service.usage(run.get())));
        } catch (CheckpointException e) {
            done.fail(e.getMessage());
        } catch (Exception e) {
            done.fail("The checkpoint could not be deleted: " + e.getMessage());
        }
    }

    private static String usageLine(Usage usage) {
        return "The checkpoints take " + CheckpointText.bytes(usage.usedBytes()) + " of " + CheckpointText.bytes(usage.quotaBytes())
                + " (" + CheckpointText.bytes(usage.reserveBytes()) + " of it is kept for the automatic ones, "
                + CheckpointText.bytes(usage.freeBytes()) + " are free for yours).";
    }
}
