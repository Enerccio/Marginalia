package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.BookSettings;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;

/**
 * When the tidy pass runs by itself: at the end of a run that applies what the model found without asking (nobody is waiting for a
 * review, and the log has a revert), when the setting is on and the model has read enough parts since the last tidy. A run the user
 * reviews never starts a tidy: a review is not forced on the user in the middle of a flow.
 */
public final class TidyTrigger {

    private TidyTrigger() {
    }

    /**
     * @param guided whether the run is reviewed by the user
     */
    public static boolean due(KnowledgeState state, boolean guided) {
        BookSettings settings = state.getSettings();
        return !guided && settings.isExtractWithModel() && settings.isAutoTidy() && state.getPartsSinceTidy() >= settings.getTidyEveryParts();
    }
}
