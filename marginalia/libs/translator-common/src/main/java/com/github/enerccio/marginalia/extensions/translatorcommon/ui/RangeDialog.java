package com.github.enerccio.marginalia.extensions.translatorcommon.ui;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.Mode;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.RangePart;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.Summary;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeRuns;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Supplier;

import static com.github.enerccio.marginalia.extensions.translatorcommon.ui.TranslatorCommonLocalization.*;

/**
 * The actions on a range of the parts of the story: translate the parts that are not translated, translate the translated ones again (the
 * rules or the lexicon changed), or put the English back. It stays open beside the story while the run goes on, one part after the other;
 * the story is drawn again when the run is over.
 */
public class RangeDialog extends Dialog {

    private final Manuscript book;
    private final Supplier<List<RangePart>> loader;
    private List<RangePart> all;
    private final RangeRuns runs;
    private final Runnable reload;

    private final Select<Mode> action = new Select<>();
    private final IntegerField from = new IntegerField(TC.localized(FROM_PART));
    private final IntegerField to = new IntegerField(TC.localized(TO_PART));
    private final Div count = new Div();
    private final ProgressBar progress = new ProgressBar();
    private final Div status = new Div();
    private final Button start = new Button(TC.localized(START));
    private final Button stop = new Button(TC.localized(STOP));
    private RangeRuns.Handle handle;
    private int planned;

    /**
     * @param loader the parts of the active branch that have text, in order, as the range sees them now: read when the dialog opens, before a
     *               run starts and when one is over, because a run changes which parts are translated
     * @param reload draws the story again, called when a run that changed something is over
     */
    public RangeDialog(Manuscript book, Supplier<List<RangePart>> loader, RangeRuns runs, Runnable reload) {
        this.book = book;
        this.loader = loader;
        this.all = loader.get();
        this.runs = runs;
        this.reload = reload;

        setHeaderTitle(TC.localized(TRANSLATE_PARTS));
        setWidth("520px");
        setMaxWidth("96vw");
        setModal(false);
        setDraggable(true);
        setCloseOnOutsideClick(false);

        action.setLabel(TC.localized(WHAT_DO_YOU_WANT));
        action.setItems(Mode.values());
        action.setItemLabelGenerator(RangeDialog::label);
        action.setValue(Mode.TRANSLATE_NEW);
        action.setWidthFull();

        int last = Math.max(1, all.size());
        for (IntegerField field : new IntegerField[]{from, to}) {
            field.setMin(1);
            field.setMax(last);
            field.setStepButtonsVisible(true);
            field.setWidthFull();
        }
        from.setValue(1);
        to.setValue(last);

        count.addClassName(CommonStyles.HINT);
        status.addClassName(CommonStyles.HINT);
        status.addClassName(CommonStyles.SOURCE_TEXT);
        progress.setVisible(false);
        progress.setWidthFull();
        stop.setEnabled(false);
        start.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        start.addClickListener(event -> begin());
        stop.addClickListener(event -> {
            if (handle != null) {
                handle.stop();
                status.setText(TC.localized(STOPPING_AFTER_THIS_PART));
                stop.setEnabled(false);
            }
        });

        action.addValueChangeListener(event -> refreshCount());
        from.addValueChangeListener(event -> refreshCount());
        to.addValueChangeListener(event -> refreshCount());

        VerticalLayout body = new VerticalLayout(action, from, to, count, progress, status);
        body.setPadding(false);
        add(body);
        getFooter().add(new Button(TC.localized(CLOSE), event -> close()), stop, start);
        refreshCount();
    }

    static String label(Mode mode) {
        return switch (mode) {
            case TRANSLATE_NEW -> TC.localized(TRANSLATE_THE_PARTS_THAT_ARE_NOT_TRANSLATED);
            case RETRANSLATE -> TC.localized(TRANSLATE_THE_TRANSLATED_PARTS_AGAIN);
            case REVERT -> TC.localized(PUT_THE_ENGLISH_BACK);
        };
    }

    /** The parts the choice works on. */
    private List<RangePart> selected() {
        int first = from.getValue() == null ? 1 : from.getValue();
        int last = to.getValue() == null ? all.size() : to.getValue();
        return RangeLoop.select(action.getValue(), all, first, last);
    }

    private void refreshCount() {
        long inRange = all.stream().filter(part -> from.getValue() != null && to.getValue() != null
                && part.number() >= from.getValue() && part.number() <= to.getValue()).count();
        count.setText(String.format(TC.localized(PARTS_IN_THE_RANGE_D_TO_DO_D), inRange, selected().size()));
    }

    /** Reads the parts again; what the dialog had stays when that fails. */
    private void reloadParts() {
        try {
            all = loader.get();
        } catch (RuntimeException e) {
            Notification.error(String.format(TC.localized(THE_TRANSLATION_FAILED), e.getMessage()));
        }
    }

    private void begin() {
        reloadParts();
        List<RangePart> parts = selected();
        if (parts.isEmpty()) {
            Notification.error(TC.localized(NOTHING_TO_DO_IN_THE_RANGE));
            return;
        }
        if (action.getValue() == Mode.REVERT) {
            ConfirmDialog.show(String.format(TC.localized(PUT_THE_ENGLISH_BACK_INTO_D_PARTS), parts.size()), () -> run(parts));
        } else {
            run(parts);
        }
    }

    private void run(List<RangePart> parts) {
        planned = parts.size();
        Mode mode = action.getValue();
        RangeRuns.Handle started = runs.start(book, mode, parts, this::showProgress, this::finished);
        if (started == null) {
            Notification.error(TC.localized(A_RANGE_IS_BEING_TRANSLATED_ALREADY));
            return;
        }
        handle = started;
        setRunning(true);
        showProgress(0);
    }

    private void setRunning(boolean running) {
        start.setEnabled(!running);
        stop.setEnabled(running);
        for (var field : new com.vaadin.flow.component.Component[]{action, from, to}) {
            ((com.vaadin.flow.component.HasEnabled) field).setEnabled(!running);
        }
        progress.setVisible(running);
    }

    private void showProgress(int done) {
        progress.setMax(Math.max(1, planned));
        progress.setValue(done);
        status.setText(String.format(TC.localized(PART_D_OF_D), Math.min(done + 1, planned), planned));
    }

    private void finished(Summary summary, Throwable failure) {
        handle = null;
        setRunning(false);
        if (failure != null) {
            status.setText(String.format(TC.localized(THE_TRANSLATION_FAILED), failure.getMessage()));
            Notification.error(status.getText());
            return;
        }
        status.setText(describe(summary));
        if (summary.aborted() || summary.failed() > 0) {
            Notification.error(status.getText());
        } else {
            Notification.success(status.getText());
        }
        if (summary.changed() > 0) {
            reload.run();
        }
        reloadParts();
        refreshCount();
    }

    /**
     * What the run came to, in words.
     */
    static String describe(Summary summary) {
        StringBuilder text = new StringBuilder(String.format(TC.localized(D_PARTS_CHANGED_D_SPEECHES), summary.changed(), summary.speeches()));
        if (summary.newWords() > 0) {
            text.append(' ').append(String.format(TC.localized(D_NEW_WORDS), summary.newWords()));
        }
        if (summary.kept() > 0) {
            text.append(' ').append(String.format(TC.localized(D_EDITED_SPEECHES_STAY), summary.kept()));
        }
        if (summary.failed() > 0) {
            text.append(' ').append(String.format(TC.localized(D_PARTS_FAILED_S), summary.failed(), summary.firstFailure()));
        }
        if (summary.aborted()) {
            text.append(' ').append(StringUtils.defaultString(summary.firstFailure()));
        }
        if (summary.stopped()) {
            text.append(' ').append(String.format(TC.localized(STOPPED_AFTER_D_OF_D_PARTS), summary.processed(), summary.planned()));
        }
        return text.toString();
    }
}
