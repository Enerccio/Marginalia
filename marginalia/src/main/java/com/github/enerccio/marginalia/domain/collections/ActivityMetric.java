package com.github.enerccio.marginalia.domain.collections;

import com.github.enerccio.marginalia.loc.L;

/**
 * What the shade of a day of the activity calendar says: the words the author wrote that day, or the time they spent editing.
 */
public enum ActivityMetric {

    WORDS(L.LABEL_WORDS_WRITTEN),
    TIME(L.LABEL_TIME_EDITING);

    private final L label;

    ActivityMetric(L label) {
        this.label = label;
    }

    public L getLabel() {
        return label;
    }

}
