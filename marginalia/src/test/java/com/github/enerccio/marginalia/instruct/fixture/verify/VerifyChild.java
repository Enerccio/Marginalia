package com.github.enerccio.marginalia.instruct.fixture.verify;

import com.github.enerccio.marginalia.domain.traits.Extendable;

/**
 * Inherits {@code render} from {@link VerifyTarget}: decorators for it are registered for the superclass.
 */
@Extendable
public class VerifyChild extends VerifyTarget {

    public void extra() {
    }
}
