package com.github.enerccio.marginalia.extensions.audiobook.label;

import java.util.List;

/** Where the labels of a part are kept, so that the model is asked once for a text (the application keeps them in the attributes of the part). */
public interface LabelStore {

    /** @return the labels stored for the part under that key, null when there are none or they were made for another text or instruction */
    List<PieceLabel> find(String partId, String key);

    void put(String partId, String key, List<PieceLabel> labels);
}
