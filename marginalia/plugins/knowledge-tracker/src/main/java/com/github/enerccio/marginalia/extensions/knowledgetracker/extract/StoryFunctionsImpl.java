package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;

/** The functions of one request, answered from what its {@link PartReader} may show. */
public final class StoryFunctionsImpl implements StoryFunctions {

    private final PartReader reader;

    public StoryFunctionsImpl(PartReader reader) {
        this.reader = reader;
    }

    @Override
    public void readPart(FunctionContinuation<String> done, int part) {
        done.complete(reader.read(part));
    }
}
