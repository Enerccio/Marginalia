package com.github.enerccio.marginalia.extensions.audiobook.label;

import java.util.List;

/** Where the labels of a part are kept, so that the model is asked once for a text (the application keeps them in the attributes of the part). */
public interface LabelStore {

    /** @return the labels stored for the part under that key, null when there are none or they were made for another text or instruction */
    List<PieceLabel> find(String partId, String key);

    void put(String partId, String key, List<PieceLabel> labels);

    /** Like {@link #put(String, String, List)}, also remembering which text the labels are for (see {@link #findForText}). */
    default void put(String partId, String key, String text, List<PieceLabel> labels) {
        put(partId, key, labels);
    }

    /**
     * @return the labels stored for the part when they were made for exactly this text, whatever the instruction and the characters were; null when
     * there are none (or they were stored without the text). For a run without the model: it reads what the model made instead of flattening the part.
     */
    default List<PieceLabel> findForText(String partId, String text) {
        return null;
    }
}
