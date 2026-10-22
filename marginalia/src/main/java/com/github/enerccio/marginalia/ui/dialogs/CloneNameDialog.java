package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;

/**
 * Asks for the name of the clone of a lorebook, protocol or AI, with the name of the original and "(Copy)" filled in.
 */
public final class CloneNameDialog {

    private CloneNameDialog() {
    }

    /**
     * @param title      what the dialog says, e.g. "Clone protocol"
     * @param sourceName the name of the original
     * @param onClone    called with the name the user confirmed (never blank)
     */
    public static void show(Localization loc, String title, String sourceName, TextInputDialog.MessageAction onClone) {
        new TextInputDialog.Builder(title, onClone)
                .showCancel(true)
                .messageRequired()
                .inputMessageLabel(loc.getValue(L.LABEL_NAME))
                .initialValue(sourceName + " (" + loc.getValue(L.LABEL_COPY) + ")")
                .build()
                .open();
    }
}
