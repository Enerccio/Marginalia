package com.github.enerccio.marginalia.loc;

/**
 * Base of a bundle: {@link #loadMessages()} fills the texts with {@link #setValue}.
 */
public abstract class LocalizationBundleBase extends TextBundleBase<L> implements LocalizationBundle {

    protected LocalizationBundleBase() {
        super(L.class);
    }

}
