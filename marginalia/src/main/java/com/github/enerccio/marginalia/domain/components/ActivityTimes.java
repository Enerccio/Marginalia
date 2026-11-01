package com.github.enerccio.marginalia.domain.components;

import com.github.enerccio.marginalia.domain.collections.ActivityKind;

/**
 * The time of each kind of activity in milliseconds, each the union of its stretches ({@link TimeUnion}). The four numbers
 * are never added to each other: editing happens while the book is open and a generation while somebody is editing or
 * not, so they overlap.
 */
public record ActivityTimes(long open, long edit, long generate, long run) {

    public static final ActivityTimes ZERO = new ActivityTimes(0, 0, 0, 0);

    public long of(ActivityKind kind) {
        return switch (kind) {
            case OPEN -> open;
            case EDIT -> edit;
            case GENERATE -> generate;
            case RUN -> run;
        };
    }

    public boolean isZero() {
        return open == 0 && edit == 0 && generate == 0 && run == 0;
    }

}
