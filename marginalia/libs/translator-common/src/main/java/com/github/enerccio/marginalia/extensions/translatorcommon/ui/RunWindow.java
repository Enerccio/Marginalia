package com.github.enerccio.marginalia.extensions.translatorcommon.ui;

import com.github.enerccio.marginalia.extensions.translatorcommon.service.RunProgress;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import static com.github.enerccio.marginalia.extensions.translatorcommon.ui.TranslatorCommonLocalization.*;

/**
 * The window of a run that asks a model: the thinking of the model and its answer as they arrive, and a Stop button. It blocks the page (a model
 * server with one slot would make anything else wait anyway) and opens only when a request really goes to the model, so a part with nothing to
 * ask shows nothing.
 * <p>
 * Create it on the thread of the user's request, hand {@link #progress()} to the run ({@code PartRuns.start}) and call {@link #finish()} when the
 * run is over. What arrives is shown at most a few times a second, so a fast model does not flood the page.
 */
public final class RunWindow implements RunProgress.Listener {

    private static final long MINIMUM_MILLIS_BETWEEN_UPDATES = 150;

    private final UI ui;
    private final String title;
    private final RunProgress progress = new RunProgress(this);

    private final StringBuilder reasoning = new StringBuilder();
    private final StringBuilder answer = new StringBuilder();
    private long lastUpdate;
    private boolean finished;

    // the components are created and touched on the thread of the page only
    private Dialog dialog;
    private Span status;
    private Span reasoningHeading;
    private Div reasoningBox;
    private Span answerHeading;
    private Div answerBox;
    private Button stop;

    /**
     * @param title the title of the window ("Translating the part")
     */
    public RunWindow(String title) {
        this.ui = UI.getCurrent();
        this.title = title;
    }

    /**
     * What the run reports to and what stops it.
     */
    public RunProgress progress() {
        return progress;
    }

    /**
     * The run is over (done, failed or stopped): the window closes. Safe to call when it never opened.
     */
    public void finish() {
        synchronized (this) {
            finished = true;
        }
        onPage(() -> {
            if (dialog != null) {
                dialog.close();
                dialog = null;
            }
        });
    }

    @Override
    public void requestStarted() {
        synchronized (this) {
            reasoning.setLength(0);
            answer.setLength(0);
            lastUpdate = 0;
        }
        onPage(() -> {
            boolean over;
            synchronized (this) {
                over = finished;
            }
            if (over) {
                return;
            }
            if (dialog == null) {
                build();
                dialog.open();
            }
            showText();
            status.setText(TC.localized(WAITING_FOR_THE_MODEL));
        });
    }

    @Override
    public void chunk(boolean isReasoning, String text) {
        boolean show;
        synchronized (this) {
            (isReasoning ? reasoning : answer).append(text);
            long now = System.currentTimeMillis();
            show = now - lastUpdate >= MINIMUM_MILLIS_BETWEEN_UPDATES;
            if (show) {
                lastUpdate = now;
            }
        }
        if (show) {
            onPage(() -> {
                if (dialog != null) {
                    showText();
                    if (!progress.isStopped()) {
                        status.setText(TC.localized(isReasoning ? THE_MODEL_IS_THINKING : THE_MODEL_IS_ANSWERING));
                    }
                }
            });
        }
    }

    private void build() {
        dialog = new Dialog();
        dialog.setHeaderTitle(title);
        dialog.setWidth("min(900px, 95vw)");
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setModality(ModalityMode.STRICT);

        status = new Span();
        status.addClassName(CommonStyles.HINT);
        reasoningHeading = new Span(TC.localized(REASONING));
        reasoningHeading.addClassName(CommonStyles.RUN_HEADING);
        reasoningBox = new Div();
        reasoningBox.addClassName(CommonStyles.RUN_BOX);
        answerHeading = new Span(TC.localized(ANSWER));
        answerHeading.addClassName(CommonStyles.RUN_HEADING);
        answerBox = new Div();
        answerBox.addClassName(CommonStyles.RUN_BOX);

        VerticalLayout content = new VerticalLayout(status, reasoningHeading, reasoningBox, answerHeading, answerBox);
        content.setPadding(false);
        content.setWidthFull();
        dialog.add(content);

        stop = new Button(TC.localized(STOP), event -> {
            stop.setEnabled(false);
            status.setText(TC.localized(STOPPING_THE_REQUEST));
            progress.stop();
        });
        stop.addThemeVariants(ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(stop);
    }

    /**
     * Puts what has arrived into the boxes; a box with nothing in it is not shown.
     */
    private void showText() {
        String thinking;
        String text;
        synchronized (this) {
            thinking = reasoning.toString();
            text = answer.toString();
        }
        reasoningHeading.setVisible(!thinking.isEmpty());
        reasoningBox.setVisible(!thinking.isEmpty());
        reasoningBox.setText(thinking);
        answerHeading.setVisible(!text.isEmpty());
        answerBox.setVisible(!text.isEmpty());
        answerBox.setText(text);
        // the newest text is at the bottom of its box
        reasoningBox.getElement().executeJs("this.scrollTop = this.scrollHeight");
        answerBox.getElement().executeJs("this.scrollTop = this.scrollHeight");
    }

    /**
     * Runs the work on the thread of the page and pushes it to the browser; a page that is gone shows nothing.
     */
    private void onPage(Runnable work) {
        if (ui == null) {
            return;
        }
        try {
            ui.access(() -> {
                work.run();
                UIPushGuard.push(ui);
            });
        } catch (UIDetachedException e) {
            // the page is gone, nobody sees the window
        }
    }
}
