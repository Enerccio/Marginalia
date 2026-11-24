package com.github.enerccio.marginalia.domain.collections;

import com.github.enerccio.marginalia.loc.LocalizationKey;
import com.github.enerccio.marginalia.loc.M_L;

/**
 * What the shade of a day of the activity calendar says: the words the author wrote that day, or the time they spent editing.
 */
public enum ActivityMetric {

    WORDS(M_L.LABEL_WORDS_WRITTEN),
    TIME(M_L.LABEL_TIME_EDITING);

    private final LocalizationKey label;

    ActivityMetric(LocalizationKey label) {
        this.label = label;
    }

    public LocalizationKey getLabel() {
        return label;
    }

}
