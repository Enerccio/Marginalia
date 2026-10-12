package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;

/**
 * What a story tab needs from the dialog that holds it. Implemented by the desktop and the phone book dialog, so the
 * helpers of this package serve both story tabs.
 */
public interface StoryHost {

    Manuscript refreshManuscript() throws Exception;

    Manuscript save() throws Exception;

    void freeze();

    void unfreeze();

}
