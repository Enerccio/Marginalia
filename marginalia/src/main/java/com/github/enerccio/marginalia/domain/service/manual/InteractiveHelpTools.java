package com.github.enerccio.marginalia.domain.service.manual;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.domain.traits.LLMParam;

import java.util.List;

/**
 * {@link HelpTools} and the function to move through the interface for the user. Given to the model only when an
 * administrator allowed the assistant to interact; the moves are harmless ones (open a section, select a tab).
 */
public interface InteractiveHelpTools extends HelpTools {

    @LLMFunction(name = "interact_with_ui", description = "Moves through the Marginalia interface for the user: opens a "
            + "section or selects a tab, so that you can show them where something is. Only moves that the elements "
            + "list under \"Interactions\" in the result of get_ui_state are possible; nothing that changes, saves, "
            + "deletes or generates anything. Use it when the user asks you to show or take them somewhere, not "
            + "otherwise. Returns the state of the screen after the moves.")
    void interactWithUi(FunctionContinuation<String> done,
                        @LLMParam(description = "Path of the element that offers the interactions, as get_ui_state "
                                + "listed it (for example UI/Main/DesktopWorkspace)") String path,
                        @LLMParam(description = "The keys of the interactions to apply to it, in order (for example "
                                + "[\"open-section:Lorebooks\"]). Several when you know the sequence; the element's "
                                + "interactions are looked up again before each step") List<String> interactions);
}
