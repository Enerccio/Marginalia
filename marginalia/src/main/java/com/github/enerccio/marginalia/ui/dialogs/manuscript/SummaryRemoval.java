package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.domain.collections.SummaryType;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.utils.UIUtils;

import java.util.function.Consumer;

/**
 * Asks the user and removes the summary of a part: a summary is deleted, a meta summary can also be unwound to the
 * summary it replaced.
 */
public final class SummaryRemoval {

    private SummaryRemoval() {
    }

    /**
     * @param done gets the updated part after the summary was removed
     */
    public static void confirmAndRemove(Localization loc, SummaryService summaryService, ChatMessage message, Consumer<ChatMessage> done) {
        try {
            Summary summary = message != null && message.getSummary() != null ? summaryService.find(message.getSummary()) : null;
            if (summary == null) {
                return;
            }
            if (summary.getSummaryType() == SummaryType.META_SUMMARY) {
                ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE_META_SUMMARY), loc.getValue(L.LABEL_UNWIND), loc.getValue(L.LABEL_DELETE),
                        () -> remove(loc, summaryService, message, true, done), () -> remove(loc, summaryService, message, false, done), true);
            } else {
                ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> remove(loc, summaryService, message, false, done));
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private static void remove(Localization loc, SummaryService summaryService, ChatMessage message, boolean unwind, Consumer<ChatMessage> done) {
        try {
            done.accept(summaryService.removeSummary(message, unwind));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
