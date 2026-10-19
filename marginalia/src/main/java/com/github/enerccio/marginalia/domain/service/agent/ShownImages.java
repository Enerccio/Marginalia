package com.github.enerccio.marginalia.domain.service.agent;

import java.util.ArrayList;
import java.util.List;

/**
 * The images a model asked to see in one round ({@code view_image}), collected by the tool and taken by the runner,
 * which puts them in front of the model after the results of the round: a tool result is text only, so the picture
 * comes as a message of its own.
 */
public final class ShownImages {

    private final List<VirtualPath> paths = new ArrayList<>();

    public synchronized void add(VirtualPath path) {
        if (!paths.contains(path)) {
            paths.add(path);
        }
    }

    /**
     * The images asked for since the last call, in order; they are forgotten.
     */
    public synchronized List<VirtualPath> take() {
        List<VirtualPath> taken = List.copyOf(paths);
        paths.clear();
        return taken;
    }

}
