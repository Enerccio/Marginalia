package com.github.enerccio.marginalia.domain.service.manual;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.domain.traits.LLMParam;

/**
 * The functions the help assistant's model calls to read the manual, see {@link ManualToolsImpl}. Read only.
 */
public interface ManualTools {

    @LLMFunction(name = "search_manual", description = "Searches the Marginalia user manual and returns the best matching "
            + "sections (page path, heading and text). Use it for every question about Marginalia before answering.")
    void searchManual(FunctionContinuation<String> done,
                      @LLMParam(description = "What to look for: a few keywords, English works best") String query);

    @LLMFunction(name = "read_manual_page", description = "Reads a whole page of the Marginalia user manual by its path, "
            + "e.g. books/story-editor.md. Paths are in the page index and in the results of search_manual.")
    void readManualPage(FunctionContinuation<String> done,
                        @LLMParam(description = "Path of the page, e.g. administration/users.md") String path);
}
