package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.BranchProcessor;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.Part;

import java.util.Collection;
import java.util.List;

/**
 * What it takes to commit a run to the state: move the cursor over its parts, then apply the changes.
 * <p>
 * The order matters. Moving the cursor first brings the state in line with the branch ({@link BranchProcessor#align}), which
 * takes back everything learned from a part that is not yet read; changes applied before that would be taken back with it.
 */
public final class RunCommit {

    private RunCommit() {
    }

    public static ChangeApplier.Result commit(KnowledgeState state, List<Part> branch, List<Part> run, ChangeSet changes,
                                              Collection<String> separators, long now) {
        return commit(state, branch, run, changes, separators, now, false);
    }

    /**
     * @param branch the active branch
     * @param run    the parts of the run: they must be the next ones in line
     * @param logged whether the applied changes go into the change log (an automatic run)
     * @return {@code null} when they are not the next ones in line (the story changed since the proposal), else what was applied
     */
    public static ChangeApplier.Result commit(KnowledgeState state, List<Part> branch, List<Part> run, ChangeSet changes,
                                              Collection<String> separators, long now, boolean logged) {
        List<Part> pending = BranchProcessor.pending(state, branch, branch.size(), run.size());
        if (!sameParts(pending, run)) {
            return null;
        }
        int end = indexOf(branch, run.getLast().uuid()) + 1;
        BranchProcessor.process(state, branch, end, separators, now, run.size());
        return ChangeApplier.apply(state, changes, logged, now);
    }

    private static boolean sameParts(List<Part> a, List<Part> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).uuid().equals(b.get(i).uuid())) {
                return false;
            }
        }
        return true;
    }

    private static int indexOf(List<Part> branch, String uuid) {
        for (int i = 0; i < branch.size(); i++) {
            if (branch.get(i).uuid().equals(uuid)) {
                return i;
            }
        }
        return -1;
    }
}
