package com.github.enerccio.marginalia.domain.service.manual;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.domain.traits.LLMParam;

/**
 * Everything the help assistant's model can call: the manual ({@link ManualTools}) and the state of the user's screen.
 * Read only.
 */
public interface HelpTools extends ManualTools {

    @LLMFunction(name = "get_ui_state", description = "Shows what the user currently sees in Marginalia: the section, tab "
            + "or dialog that is open and its state, as a tree of elements with logical paths. Call it when the question "
            + "is about what the user is looking at or doing right now. By default only what is on the screen is "
            + "expanded; the other tabs are just listed.")
    void getUiState(FunctionContinuation<String> done,
                    @LLMParam(description = "Path of the element to read (for example DesktopWorkspace/ManuscriptPart), "
                            + "taken from an earlier result; empty for the whole page", required = false) String path,
                    @LLMParam(description = "true also expands the tabs that are not open and the hidden parts, for "
                            + "something that is not on the screen; makes the answer longer. Default false",
                            required = false) boolean deep);

    @LLMFunction(name = "read_last_errors", description = "Reads what went wrong for the user lately, newest first: the "
            + "errors, the failed requests to the language model provider (wrong key, no such model, limit reached, "
            + "timeout...), the warnings and the validation messages that Marginalia showed them, with what is known "
            + "about each. Call it when the user says something failed, an error appeared, or they cannot save or "
            + "generate. A form that is open right now lists its invalid and empty required fields under \"Problems "
            + "with the form\" in get_ui_state.")
    void readLastErrors(FunctionContinuation<String> done,
                        @LLMParam(description = "How many of the last problems to read (default 3, at most 10)",
                                required = false) int count);
}
