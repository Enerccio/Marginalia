package com.github.enerccio.marginalia.domain.service.manual;

import com.github.enerccio.marginalia.domain.service.ManualService;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;

/**
 * {@link HelpTools}: the manual tools and the state of the screen read from a {@link UiStateSource}.
 */
public class HelpToolsImpl extends ManualToolsImpl implements HelpTools {

    protected final UiStateSource uiState;
    private final boolean interactive;

    /**
     * @param uiState where the screen is read, null when the request does not come from a page
     */
    public HelpToolsImpl(ManualService manual, UiStateSource uiState) {
        this(manual, ManualPage.ENGLISH, uiState, false);
    }

    /**
     * @param language code of the language of the user, the manual is read in it
     */
    public HelpToolsImpl(ManualService manual, String language, UiStateSource uiState) {
        this(manual, language, uiState, false);
    }

    /**
     * @param interactive whether the screen is read with what the elements offer to do (the model has {@link
     *                    InteractiveHelpTools} then)
     */
    protected HelpToolsImpl(ManualService manual, String language, UiStateSource uiState, boolean interactive) {
        super(manual, language);
        this.uiState = uiState;
        this.interactive = interactive;
    }

    @Override
    public void getUiState(FunctionContinuation<String> done, String path, boolean deep) {
        if (uiState == null) {
            done.fail("The screen of the user is not available.");
            return;
        }
        uiState.read(path == null ? "" : path, deep, interactive, done);
    }

    @Override
    public void readLastErrors(FunctionContinuation<String> done, int count) {
        if (uiState == null) {
            done.fail("The screen of the user is not available.");
            return;
        }
        uiState.readProblems(count, done);
    }
}
