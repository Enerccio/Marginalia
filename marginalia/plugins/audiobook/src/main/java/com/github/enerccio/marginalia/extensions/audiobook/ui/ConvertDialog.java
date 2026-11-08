package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.audiobook.model.AudiobookSettings;
import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionJob;
import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionJob.State;
import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionQueue;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.ui.widgets.ModelProtocolPicker;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * <i>Convert to audio</i>: the user picks a backend and a range of parts, the parts go into the queue of the user, and the list of jobs shows
 * how each goes (waiting, running with the pieces made so far, done with a download, failed with the reason and a retry). The queue goes on
 * when the window is closed; a job that is over is shown again when the window is opened.
 */
public class ConvertDialog extends Dialog {

    private final Manuscript manuscript;
    private final AudiobookService service;
    private final ConversionQueue queue;
    private final Consumer<ConversionJob.Snapshot> listener = snapshot -> UIUtils.accessComponent(this, this::refresh);

    private final Select<BackendConfig> backend = new Select<>();
    private final IntegerField from = new IntegerField(T.localized(FIRST_PART));
    private final IntegerField to = new IntegerField(T.localized(LAST_PART));
    private final Button editCast = new Button(T.localized(CAST), Solid.MASKS_THEATER.create());
    private final Button pronunciation = new Button(T.localized(PRONUNCIATION), Solid.LANGUAGE.create());
    private final Checkbox label = new Checkbox(T.localized(LABEL_WITH_A_LANGUAGE_MODEL));
    private final ModelProtocolPicker modelPicker = new ModelProtocolPicker();
    private final Button start = new Button(T.localized(START), Solid.PLAY.create(), e -> start());
    private final Grid<ConversionJob.Snapshot> grid = new Grid<>();
    private int parts;

    public ConvertDialog(Manuscript manuscript, AudiobookService service) {
        this.manuscript = manuscript;
        this.service = service;
        this.queue = service.queue();

        setHeaderTitle(T.localized(CONVERT_TO_AUDIO));
        setWidth("min(900px, 95vw)");
        setMaxHeight("92vh");

        AudiobookSettings settings = service.getSettings();
        backend.setLabel(T.localized(BACKEND));
        backend.setItems(settings.getBackends());
        backend.setItemLabelGenerator(b -> StringUtils.defaultIfBlank(b.getName(), T.localized(NEW_BACKEND)));
        backend.setWidth("16em");
        backend.setValue(settings.selected());

        Span hint = new Span();
        hint.addClassName(CssStyles.HINT_TEXT);
        try {
            parts = service.speakableParts(manuscript).size();
        } catch (Exception e) {
            AudiobookLog.get().warn("The parts of the book could not be counted: {}", e.getMessage(), e);
        }
        hint.setText(parts == 0 ? T.localized(THE_BOOK_HAS_NO_TEXT) : T.localized(THE_BOOK_HAS_PARTS_WITH_TEXT).formatted(parts));
        for (IntegerField f : new IntegerField[]{from, to}) {
            f.setMin(1);
            f.setMax(Math.max(parts, 1));
            f.setStepButtonsVisible(true);
            f.setWidth("9em");
        }
        from.setValue(1);
        to.setValue(Math.max(parts, 1));
        start.addThemeName("primary");
        start.setEnabled(parts > 0 && !settings.getBackends().isEmpty());

        editCast.setThemeName("tertiary");
        pronunciation.setThemeName("tertiary");
        pronunciation.addClickListener(e -> new SpokenFormDialog(service, manuscript, () -> Notification.success(T.localized(THE_PRONUNCIATION_IS_STORED))).open());
        editCast.addClickListener(e -> new CastDialog(service, manuscript, () -> Notification.success(T.localized(THE_CAST_IS_STORED))).open());

        HorizontalLayout form = new HorizontalLayout(backend, from, to, start, editCast, pronunciation);
        form.setAlignItems(FlexComponent.Alignment.END);
        form.setWrap(true);

        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER);
        grid.addColumn(ConversionJob.Snapshot::title).setHeader(T.localized(PART)).setFlexGrow(3);
        grid.addColumn(s -> stateText(s.state())).setHeader(T.localized(STATE)).setAutoWidth(true);
        grid.addColumn(s -> s.total() == 0 ? "" : s.done() + " / " + s.total()).setHeader(T.localized(PROGRESS)).setAutoWidth(true);
        grid.addColumn(ConversionJob.Snapshot::message).setHeader(T.localized(MESSAGE)).setFlexGrow(2);
        grid.addComponentColumn(this::actions).setAutoWidth(true);
        grid.setHeight("18em");

        modelPicker.setPlaceholder(T.localized(THE_MODEL_THAT_LABELS));
        modelPicker.setEnabled(false);
        try {
            modelPicker.setSelection(service.bookAi(manuscript), service.bookProtocol(manuscript));
        } catch (Exception e) {
            AudiobookLog.get().warn("The model of the book could not be read: {}", e.getMessage(), e);
        }
        label.addValueChangeListener(e -> modelPicker.setEnabled(e.getValue()));
        Span labelHint = new Span(T.localized(THE_PARTS_ARE_LABELLED_FIRST));
        labelHint.addClassName(CssStyles.HINT_TEXT);
        HorizontalLayout labelling = new HorizontalLayout(label, modelPicker);
        labelling.setAlignItems(FlexComponent.Alignment.CENTER);
        labelling.setWrap(true);

        VerticalLayout content = new VerticalLayout(hint, form, labelling, labelHint, grid);
        content.setPadding(false);
        add(content);

        Button stop = new Button(T.localized(STOP_ALL), Solid.STOP.create(), e -> queue.cancelAll());
        Button clear = new Button(T.localized(CLEAR_FINISHED), Solid.BROOM.create(), e -> {
            queue.clearFinished();
            refresh();
        });
        Button close = new Button(T.localized(CLOSE), e -> close());
        getFooter().add(stop, clear, close);
        refresh();
    }

    private void start() {
        BackendConfig chosen = backend.getValue();
        if (chosen == null) {
            Notification.error(T.localized(NO_BACKEND_YET));
            return;
        }
        var narratorActor = service.getSettings().findActor(service.castOf(manuscript).getNarratorActorId());
        boolean castNarrates = narratorActor != null && !narratorActor.getVoice().isBlank();
        if (chosen.getNarratorVoice().isBlank() && !castNarrates) {
            Notification.error(T.localized(THERE_IS_NO_NARRATOR_VOICE));
            return;
        }
        int first = from.getValue() == null ? 1 : from.getValue();
        int last = to.getValue() == null ? parts : to.getValue();
        if (first < 1 || last < first || last > parts) {
            Notification.error(T.localized(THE_RANGE_IS_NOT_IN_THE_BOOK).formatted(parts));
            return;
        }
        try {
            var jobs = service.convert(manuscript, chosen, first, last,
                    label.getValue() ? new AudiobookService.LabelOptions(modelPicker.getAi(), modelPicker.getProtocol()) : null);
            Notification.success(T.localized(THE_PARTS_ARE_IN_THE_QUEUE).formatted(jobs.size()));
            UI ui = UI.getCurrent();
            String book = manuscript.getName();
            BatchNotifier.whenOver(queue, jobs, done -> BatchNotifier.tell(ui, false, book, done));
            if (jobs.size() < last - first + 1) {
                Notification.success(T.localized(FINAL_PARTS_ARE_LEFT_ALONE).formatted(last - first + 1 - jobs.size()));
            }
        } catch (IllegalArgumentException e) {
            Notification.error(T.localized(THE_CONVERSION_COULD_NOT_BE_STARTED).formatted(e.getMessage()));
        } catch (Exception e) {
            AudiobookLog.get().warn("The conversion could not be started: {}", e.getMessage(), e);
            Notification.error(T.localized(THE_CONVERSION_COULD_NOT_BE_STARTED).formatted(e.getMessage()));
        }
        refresh();
    }

    /** What can be done with a job: take its audio, try it again, or stop it. */
    private HorizontalLayout actions(ConversionJob.Snapshot job) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(false);
        if (job.state() == State.DONE) {
            StreamResource resource = new StreamResource("part.mp3", () -> {
                byte[] audio = service.partAudio(job.partId());
                return new ByteArrayInputStream(audio == null ? new byte[0] : audio);
            });
            resource.setContentType("audio/mpeg");
            Anchor download = new Anchor(resource, "");
            download.getElement().setAttribute("download", true);
            download.add(new Button(T.localized(DOWNLOAD), Solid.DOWNLOAD.create()));
            row.add(download);
        } else if (job.state() == State.FAILED || job.state() == State.CANCELLED) {
            Button retry = new Button(T.localized(RETRY), Solid.ROTATE_RIGHT.create(), e -> queue.retry(job.id()));
            retry.setThemeName("tertiary small");
            row.add(retry);
        } else {
            Button cancel = new Button(T.localized(CANCEL), Solid.XMARK.create(), e -> queue.cancel(job.id()));
            cancel.setThemeName("tertiary small");
            row.add(cancel);
        }
        return row;
    }

    private static String stateText(State state) {
        return switch (state) {
            case WAITING -> T.localized(STATE_WAITING);
            case RUNNING -> T.localized(STATE_RUNNING);
            case DONE -> T.localized(STATE_DONE);
            case FAILED -> T.localized(STATE_FAILED);
            case CANCELLED -> T.localized(STATE_CANCELLED);
        };
    }

    private void refresh() {
        grid.setItems(queue.jobs());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        queue.addListener(listener);
        refresh();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        queue.removeListener(listener);
        super.onDetach(detachEvent);
    }
}
