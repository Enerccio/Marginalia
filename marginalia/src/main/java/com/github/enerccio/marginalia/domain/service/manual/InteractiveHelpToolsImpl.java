package com.github.enerccio.marginalia.domain.service.manual;

import com.github.enerccio.marginalia.domain.service.ManualService;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;

import java.util.List;

/**
 * {@link InteractiveHelpTools}: the help tools and the moves through the screen of a {@link UiStateSource}.
 */
public final class InteractiveHelpToolsImpl extends HelpToolsImpl implements InteractiveHelpTools {

    /**
     * @param uiState where the screen is read and moved through, null when the request does not come from a page
     */
    public InteractiveHelpToolsImpl(ManualService manual, UiStateSource uiState) {
        this(manual, ManualPage.ENGLISH, uiState);
    }

    /**
     * @param language code of the language of the user, the manual is read in it
     */
    public InteractiveHelpToolsImpl(ManualService manual, String language, UiStateSource uiState) {
        super(manual, language, uiState, true);
    }

    @Override
    public void interactWithUi(FunctionContinuation<String> done, String path, List<String> interactions) {
        if (uiState == null) {
            done.fail("The screen of the user is not available.");
            return;
        }
        if (interactions == null || interactions.isEmpty()) {
            done.fail("Name at least one interaction to apply.");
            return;
        }
        uiState.interact(path == null ? "" : path, interactions, done);
    }
}
