package com.github.enerccio.marginalia.extensions.audiobook.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** A named list of {@link CleanupStep}s run one after the other on a clip. */
public class CleanupPreset {

    private String name;
    private List<CleanupStep> steps = new ArrayList<>();

    public CleanupPreset() {
    }

    public CleanupPreset(String name, List<CleanupStep> steps) {
        this.name = name;
        this.steps = new ArrayList<>(steps);
    }

    public CleanupPreset copy() {
        return new CleanupPreset(name, getSteps().stream().map(CleanupStep::copy).toList());
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CleanupStep> getSteps() {
        if (steps == null) {
            steps = new ArrayList<>();
        }
        return steps;
    }

    public void setSteps(List<CleanupStep> steps) {
        this.steps = steps;
    }

    /** The steps in a line, kept with a version as how it was made. */
    public String describe() {
        return getSteps().stream().map(CleanupStep::describe).collect(Collectors.joining(" → "));
    }
}
