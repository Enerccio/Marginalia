package com.github.enerccio.marginalia.extensions.conlangtranslator.ui;

import com.github.enerccio.marginalia.extensions.translatorcommon.ui.EditDialog;
import com.github.enerccio.marginalia.extensions.conlangtranslator.designer.Proposal;
import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.LanguageDiff;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.ui.widgets.Notification;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * Shows what the designer proposed and writes what the user picks into a language: the one place where a proposal becomes a change.
 */
final class Proposals {

    private Proposals() {
    }

    /**
     * @param target  the language the picked changes are written into (the copy the editor or the wizard works on)
     * @param changed run after something was written, for the window to show it
     */
    static void show(String title, Language target, Proposal proposal, Runnable changed) {
        StringBuilder note = new StringBuilder();
        if (!proposal.message().isBlank()) {
            note.append(proposal.message());
        }
        if (!proposal.dropped().isEmpty()) {
            note.append(note.isEmpty() ? "" : "\n\n").append(T.localized(LEFT_OUT_OF_THE_PROPOSAL)).append("\n- ").append(String.join("\n- ", proposal.dropped()));
        }
        if (proposal.set().isEmpty()) {
            Notification.warning(T.localized(THE_DESIGNER_PROPOSES_NOTHING));
            if (note.length() > 0) {
                new EditDialog(title, new com.vaadin.flow.component.html.Span(note.toString()), () -> null, null).open();
            }
            return;
        }
        new ChangeReviewDialog(title, proposal.set(), picked -> {
            LanguageDiff.Result result = LanguageDiff.apply(target, picked);
            Notification.success(T.localized(APPLIED_D_CHANGES).formatted(result.applied().size()));
            if (!result.stale().isEmpty()) {
                Notification.warning(T.localized(SKIPPED_D_CHANGED_MEANWHILE).formatted(result.stale().size()));
            }
            if (!result.unmet().isEmpty()) {
                Notification.warning(T.localized(SKIPPED_D_NEED_ANOTHER).formatted(result.unmet().size()));
            }
            changed.run();
        }).withNote(note.toString()).open();
    }
}
