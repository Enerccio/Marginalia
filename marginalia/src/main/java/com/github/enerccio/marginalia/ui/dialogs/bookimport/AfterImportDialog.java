package com.github.enerccio.marginalia.ui.dialogs.bookimport;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.agent.AgentConsoleDialog;
import com.github.enerccio.marginalia.ui.dialogs.manuscript.CreateSummariesDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * What to do with a book after parts were imported into it: write the summaries of the new story ({@link CreateSummariesDialog}) and
 * let an agent take the lore notes ({@link LoreNotesAgent}). Both are also in the settings menu of the story; this only puts them in
 * front of the author when they are most wanted. The summaries come first in the text: the agent locks the book while it works.
 */
@Configurable
public class AfterImportDialog extends Dialog {

    @Autowired
    private Localization loc;

    private final Manuscript manuscript;
    private final Runnable onChanged;

    /**
     * @param onChanged runs when the story may have changed (summaries written), to show it again
     */
    private AfterImportDialog(Manuscript manuscript, Runnable onChanged) {
        this.manuscript = manuscript;
        this.onChanged = onChanged;
    }

    /**
     * Asks what to do after the import into the book.
     */
    public static void show(Manuscript manuscript, Runnable onChanged) {
        AfterImportDialog dialog = new AfterImportDialog(manuscript, onChanged);
        dialog.build();
        dialog.open();
    }

    private void build() {
        setHeaderTitle(loc.getValue(L.LABEL_IMPORT_FINISHED));
        setWidth("520px");
        setMaxWidth("100vw");

        Span help = new Span(loc.getValue(L.HELP_AFTER_IMPORT));
        help.addClassName(CssStyles.HINT_TEXT);

        Button summaries = new Button(loc.getValue(L.LABEL_CREATE_SUMMARIES) + "...",
                _ -> new CreateSummariesDialog(manuscript, onChanged).show());
        summaries.setWidthFull();
        // the window goes when the agent is started, not when the question is asked: the author may say no
        Button notes = new Button(loc.getValue(L.LABEL_TAKE_LORE_NOTES) + "...", _ -> startLoreNotes(manuscript, this::close));
        notes.setWidthFull();

        VerticalLayout layout = new VerticalLayout(help, summaries, notes);
        layout.setPadding(false);
        add(layout);

        Button notNow = new Button(loc.getValue(L.LABEL_NOT_NOW), _ -> close());
        notNow.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(notNow);
        UIUtils.fitPhone(this, true);
    }

    /**
     * Asks and starts the agent that takes the lore notes of the book, then opens its console. Says why when it can't be started.
     * The action of the settings menu of the story.
     */
    public static void startLoreNotes(Manuscript manuscript) {
        startLoreNotes(manuscript, () -> {
        });
    }

    /**
     * @param onStarted runs when the author agreed and the agent was started
     */
    private static void startLoreNotes(Manuscript manuscript, Runnable onStarted) {
        new Helper().ask(manuscript, onStarted);
    }

    @Configurable
    private static final class Helper {
        @Autowired
        private Localization loc;

        private final LoreNotesAgent agent = new LoreNotesAgent();

        void ask(Manuscript manuscript, Runnable onStarted) {
            try {
                LoreNotesAgent.Problem problem = agent.check(manuscript);
                if (problem != null) {
                    Notification.warning(switch (problem) {
                        case NOT_ALLOWED -> loc.getValue(L.MSG_LORE_NOTES_NOT_ALLOWED);
                        case NO_MODEL -> loc.getValue(L.MSG_LORE_NOTES_NO_MODEL);
                        case HAS_AGENT -> String.format(loc.getValue(L.MSG_LORE_NOTES_HAS_AGENT), agentName(manuscript));
                    });
                    return;
                }
                ConfirmDialog.show(loc.getValue(L.MSG_LORE_NOTES_CONFIRM), () -> start(manuscript, onStarted));
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }

        private String agentName(Manuscript manuscript) throws Exception {
            return agent.existingAgentName(manuscript);
        }

        private void start(Manuscript manuscript, Runnable onStarted) {
            try {
                Agent started = agent.start(manuscript);
                Notification.success(loc.getValue(L.MSG_LORE_NOTES_STARTED));
                AgentConsoleDialog console = new AgentConsoleDialog(started);
                console.create();
                console.open();
                onStarted.run();
            } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
                Notification.error(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }
    }
}
