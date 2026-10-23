package com.github.enerccio.marginalia.domain.collections;

/**
 * Who wrote an {@link com.github.enerccio.marginalia.domain.model.impl.Annotation}.
 */
public enum AnnotationSource {

    AUTHOR,
    /**
     * A model asked to annotate a part (the annotate dialog).
     */
    AI,
    AGENT,
    /**
     * An extension (the checkers), named in the label of the annotation.
     */
    PLUGIN

}
