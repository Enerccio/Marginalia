package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.extensions.betareaders.service.PersonaWriter;

import java.util.List;
import java.util.function.Consumer;

/**
 * What lets the model write a persona for a window that does not know the services: starts the request, which tells {@code done}
 * how it ended (on any thread).
 */
@FunctionalInterface
public interface PersonaGenerator {

    /**
     * @param brief the wish of the user, a few words
     * @param taken the names the persona must not take
     * @return what stops it
     */
    CancellationToken start(String brief, List<String> taken, Consumer<PersonaWriter.Outcome> done) throws Exception;
}
