package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.audiobook.audio.AudioKind;
import com.github.enerccio.marginalia.extensions.audiobook.export.AudiobookFiles;
import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionJob;
import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionJob.State;
import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionQueue;
import com.github.enerccio.marginalia.extensions.audiobook.queue.LabelTrace;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.ModelProtocolPicker;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * <i>Convert to audio</i>: <b>Load</b> reads the parts of the book and what each has (nothing, labels, audio); the user ticks parts and plans
 * what to do with them (delete the audio, remove the labels, generate, regenerate = all three), the plan is shown in a column of its own, and
 * <b>Start</b> carries it out: the audio and the labels are taken away at once, the parts to generate go into the queue of the user, and the same
 * list shows how each job goes (waiting, running with the pieces made so far, done, failed with the reason and a retry). The queue goes on when the
 * window is closed; a job that is over is shown again when the window is opened and the parts are loaded.
 */
public class ConvertDialog extends Dialog {

    /** What a plan does to a part; they are carried out in this order. */
    private enum Step {
        DELETE_AUDIO, REMOVE_LABELS, GENERATE
    }

    /** A part of the list: what it has, what is planned for it, and its job. */
    private static final class Row {
        private AudiobookService.PartState state;
        private final EnumSet<Step> plan = EnumSet.noneOf(Step.class);
        private ConversionJob.Snapshot job;

        private Row(AudiobookService.PartState state) {
            this.state = state;
        }
    }

    private final Manuscript manuscript;
    private final AudiobookService service;
    private final ConversionQueue queue;
    private final Consumer<ConversionJob.Snapshot> listener = snapshot -> UIUtils.accessComponent(this, () -> jobChanged(snapshot));

    private final Select<BackendConfig> backend = new Select<>();
    private final Button load = new Button(T.localized(LOAD), Solid.ARROW_ROTATE_RIGHT.create(), e -> load());
    private final Button editCast = new Button(T.localized(CAST), Solid.MASKS_THEATER.create());
    private final Button pronunciation = new Button(T.localized(PRONUNCIATION), Solid.LANGUAGE.create());
    private final Checkbox label = new Checkbox(T.localized(LABEL_WITH_A_LANGUAGE_MODEL));
    private final ModelProtocolPicker modelPicker = new ModelProtocolPicker();
    private final Button start = new Button(T.localized(START), Solid.PLAY.create(), e -> start());
    private final Grid<Row> grid = new Grid<>();
    private final List<Row> rows = new ArrayList<>();
    /** The parts queued with the labelling of the model in this window: when their job is done they have labels. */
    private final Set<String> queuedLabelled = new HashSet<>();
    private final Element audio = new Element("audio");
    private final Div wholeLink = new Div();
    /** The temporary file of the whole audiobook the link offers, deleted when another one replaces it or the window goes. */
    private Path wholeFile;
    private int parts;

    public ConvertDialog(Manuscript manuscript, AudiobookService service) {
        this.manuscript = manuscript;
        this.service = service;
        this.queue = service.queue();

        setHeaderTitle(T.localized(CONVERT_TO_AUDIO));
        setWidth("min(1000px, 95vw)");
        if (!UIUtils.isPhone()) {
            // not modal: the author works on beside it; the queue goes on when it is closed, its Stop is apart
            setModality(ModalityMode.MODELESS);
            setDraggable(true);
            setResizable(true);
        }
        setMaxHeight("92vh");

        backend.setLabel(T.localized(BACKEND));
        backend.setItems(service.backends());
        backend.setItemLabelGenerator(b -> StringUtils.defaultIfBlank(b.getName(), T.localized(NEW_BACKEND)));
        backend.setWidth("16em");
        backend.setValue(service.selectedBackend());

        Span hint = new Span();
        hint.addClassName(CssStyles.HINT_TEXT);
        try {
            parts = service.speakableParts(manuscript).size();
        } catch (Exception e) {
            AudiobookLog.get().warn("The parts of the book could not be counted: {}", e.getMessage(), e);
        }
        hint.setText(parts == 0 ? T.localized(THE_BOOK_HAS_NO_TEXT) : T.localized(LOAD_THE_PARTS_HINT).formatted(parts));
        load.setEnabled(parts > 0);
        start.addThemeName("primary");
        start.setEnabled(false);

        editCast.setThemeName("tertiary");
        pronunciation.setThemeName("tertiary");
        pronunciation.addClickListener(e -> new SpokenFormDialog(service, manuscript, () -> Notification.success(T.localized(THE_PRONUNCIATION_IS_STORED))).open());
        editCast.addClickListener(e -> new CastDialog(service, manuscript, () -> Notification.success(T.localized(THE_CAST_IS_STORED))).open());

        HorizontalLayout form = new HorizontalLayout(backend, load, editCast, pronunciation);
        form.setAlignItems(FlexComponent.Alignment.END);
        form.setWrap(true);

        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER);
        grid.addColumn(r -> r.state.title()).setHeader(T.localized(PART)).setFlexGrow(3);
        grid.addColumn(r -> statusText(r.state)).setHeader(T.localized(STATUS)).setAutoWidth(true);
        grid.addColumn(r -> planText(r.plan)).setHeader(T.localized(PLAN)).setAutoWidth(true);
        grid.addComponentColumn(this::stateCell).setHeader(T.localized(STATE)).setAutoWidth(true);
        grid.addColumn(r -> r.job == null ? "" : r.job.message()).setHeader(T.localized(MESSAGE)).setFlexGrow(2);
        // the buttons come and go with the jobs (play, download, Retry or Cancel): a width measured once, on the first rows, would cut them
        grid.addComponentColumn(this::actions).setWidth("13em").setFlexGrow(0);
        grid.setHeight("22em");

        Button generate = planButton(GENERATE, Solid.PLAY, EnumSet.of(Step.GENERATE), false);
        Button regenerate = planButton(REGENERATE, Solid.ROTATE, EnumSet.allOf(Step.class), true);
        Button deleteAudio = planButton(DELETE_AUDIO, Solid.TRASH, EnumSet.of(Step.DELETE_AUDIO), false);
        Button removeLabels = planButton(REMOVE_LABELS, Solid.TAG, EnumSet.of(Step.REMOVE_LABELS), false);
        Button clearPlan = new Button(T.localized(CLEAR_THE_PLAN), Solid.XMARK.create(), e -> {
            selectedRows().forEach(r -> r.plan.clear());
            grid.getDataProvider().refreshAll();
            grid.recalculateColumnWidths();
        });
        clearPlan.setThemeName("tertiary");
        HorizontalLayout planning = new HorizontalLayout(generate, regenerate, deleteAudio, removeLabels, clearPlan, start);
        planning.setWrap(true);
        planning.setAlignItems(FlexComponent.Alignment.CENTER);

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

        audio.setAttribute("controls", true);
        audio.setAttribute("preload", "none");
        audio.getClassList().add(AudiobookStyles.CONVERT_AUDIO);
        Div player = new Div();
        player.getElement().appendChild(audio);

        VerticalLayout content = new VerticalLayout(hint, form, labelling, labelHint, grid, planning, player, wholeLink);
        content.setPadding(false);
        add(content);
        addDetachListener(e -> replaceWholeFile(null));

        Button stop = new Button(T.localized(STOP_ALL), Solid.STOP.create(), e -> queue.cancelAll());
        Button clear = new Button(T.localized(CLEAR_FINISHED), Solid.BROOM.create(), e -> {
            queue.clearFinished();
            attachJobs();
            grid.getDataProvider().refreshAll();
            grid.recalculateColumnWidths();
        });
        Button whole = new Button(T.localized(DOWNLOAD_THE_AUDIOBOOK), Solid.FILE_AUDIO.create(), e -> downloadWhole());
        whole.setTooltipText(T.localized(DOWNLOAD_THE_AUDIOBOOK_HELP));
        Button close = new Button(T.localized(CLOSE), e -> close());
        getFooter().add(whole, stop, clear, close);
    }

    /** A button that adds steps to the plan of the selected parts (or, for {@code replace}, makes them the plan). */
    private Button planButton(String text, Solid icon, EnumSet<Step> steps, boolean replace) {
        Button button = new Button(T.localized(text), icon.create(), e -> plan(steps, replace));
        button.setThemeName("tertiary");
        return button;
    }

    private List<Row> selectedRows() {
        return new ArrayList<>(grid.getSelectedItems());
    }

    private void plan(EnumSet<Step> steps, boolean replace) {
        List<Row> selected = selectedRows();
        if (selected.isEmpty()) {
            Notification.error(T.localized(SELECT_THE_PARTS_FIRST));
            return;
        }
        int left = 0;
        for (Row row : selected) {
            if (row.state.finalStatus()) {
                left++;
                continue;
            }
            if (replace) {
                row.plan.clear();
            }
            row.plan.addAll(steps);
        }
        if (left > 0) {
            Notification.success(T.localized(FINAL_PARTS_ARE_LEFT_ALONE).formatted(left));
        }
        grid.getDataProvider().refreshAll();
        grid.recalculateColumnWidths();
    }

    /** Reads the parts of the book and what each has; the plans are dropped. */
    private void load() {
        try {
            rows.clear();
            for (AudiobookService.PartState state : service.partStates(manuscript)) {
                rows.add(new Row(state));
            }
            attachJobs();
            grid.setItems(rows);
            grid.recalculateColumnWidths();
            start.setEnabled(!rows.isEmpty());
        } catch (Exception e) {
            AudiobookLog.get().warn("The parts of the book could not be loaded: {}", e.getMessage(), e);
            Notification.error(T.localized(THE_CONVERSION_COULD_NOT_BE_STARTED).formatted(e.getMessage()));
        }
    }

    /** Gives each part the latest job of the queue that is about it. */
    private void attachJobs() {
        for (Row row : rows) {
            row.job = null;
        }
        for (ConversionJob.Snapshot job : queue.jobs()) {
            for (Row row : rows) {
                if (row.state.uuid().equals(job.partId())) {
                    row.job = job;
                }
            }
        }
    }

    /** A job moved on: the row shows it; a part that is done has its audio (and the labels, when the model labelled it). */
    private void jobChanged(ConversionJob.Snapshot job) {
        for (Row row : rows) {
            if (!row.state.uuid().equals(job.partId())) {
                continue;
            }
            row.job = job;
            if (job.state() == State.DONE && job.mode() == ConversionJob.Mode.PART) {
                row.state = new AudiobookService.PartState(row.state.number(), row.state.uuid(), row.state.title(),
                        row.state.labelled() || queuedLabelled.contains(row.state.uuid()), AudiobookService.StoredAudio.CURRENT, row.state.finalStatus());
            }
            grid.getDataProvider().refreshItem(row);
        }
    }

    /** Carries out the plan: asks first when it takes something away. */
    private void start() {
        List<Row> planned = rows.stream().filter(r -> !r.plan.isEmpty()).toList();
        if (planned.isEmpty()) {
            Notification.error(T.localized(NOTHING_IS_PLANNED));
            return;
        }
        boolean generating = planned.stream().anyMatch(r -> r.plan.contains(Step.GENERATE));
        BackendConfig chosen = backend.getValue();
        if (generating) {
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
        }
        long audioGoes = planned.stream().filter(r -> r.plan.contains(Step.DELETE_AUDIO)).count();
        long labelsGo = planned.stream().filter(r -> r.plan.contains(Step.REMOVE_LABELS)).count();
        if (audioGoes + labelsGo == 0) {
            apply(planned, chosen);
            return;
        }
        ConfirmDialog.show(T.localized(APPLY_THE_PLAN_QUESTION).formatted(audioGoes, labelsGo), () -> apply(planned, chosen));
    }

    private void apply(List<Row> planned, BackendConfig chosen) {
        int audioDeleted = 0;
        int labelsRemoved = 0;
        int busy = 0;
        List<Row> generate = new ArrayList<>();
        for (Row row : planned) {
            try {
                if (service.busy(row.state.uuid())) {
                    busy++;
                    continue;
                }
                if (row.plan.contains(Step.DELETE_AUDIO)) {
                    service.deleteAudio(row.state.uuid());
                    audioDeleted++;
                }
                if (row.plan.contains(Step.REMOVE_LABELS)) {
                    service.removeLabels(row.state.uuid());
                    labelsRemoved++;
                }
                if (row.plan.contains(Step.GENERATE)) {
                    generate.add(row);
                }
                AudiobookService.PartState now = service.partState(row.state.number(), row.state.uuid());
                if (now != null) {
                    row.state = now;
                }
                row.plan.clear();
            } catch (Exception e) {
                AudiobookLog.get().warn("The plan could not be carried out for {}: {}", row.state.title(), e.getMessage(), e);
                Notification.error(T.localized(THE_CONVERSION_COULD_NOT_BE_STARTED).formatted(e.getMessage()));
            }
        }
        if (audioDeleted + labelsRemoved > 0) {
            Notification.success(T.localized(THE_PLAN_WAS_CARRIED_OUT).formatted(audioDeleted, labelsRemoved));
        }
        if (busy > 0) {
            Notification.error(T.localized(PARTS_IN_THE_QUEUE_ARE_LEFT_OUT).formatted(busy));
        }
        if (!generate.isEmpty()) {
            generate(generate, chosen);
        }
        attachJobs();
        grid.getDataProvider().refreshAll();
        grid.recalculateColumnWidths();
    }

    private void generate(List<Row> generate, BackendConfig chosen) {
        try {
            AudiobookService.LabelOptions labelling = label.getValue() ? new AudiobookService.LabelOptions(modelPicker.getAi(), modelPicker.getProtocol()) : null;
            List<Integer> numbers = generate.stream().map(r -> r.state.number()).sorted().toList();
            var jobs = service.convertParts(manuscript, chosen, numbers, labelling, ConversionJob.Mode.PART);
            if (labelling != null) {
                generate.forEach(r -> queuedLabelled.add(r.state.uuid()));
            }
            generate.forEach(r -> r.plan.clear());
            Notification.success(T.localized(THE_PARTS_ARE_IN_THE_QUEUE).formatted(jobs.size()));
            UI ui = UI.getCurrent();
            String book = manuscript.getName();
            BatchNotifier.whenOver(queue, jobs, done -> BatchNotifier.tell(ui, false, book, done));
            if (jobs.size() < numbers.size()) {
                Notification.success(T.localized(FINAL_PARTS_ARE_LEFT_ALONE).formatted(numbers.size() - jobs.size()));
            }
        } catch (IllegalArgumentException e) {
            Notification.error(T.localized(THE_CONVERSION_COULD_NOT_BE_STARTED).formatted(e.getMessage()));
        } catch (Exception e) {
            AudiobookLog.get().warn("The conversion could not be started: {}", e.getMessage(), e);
            Notification.error(T.localized(THE_CONVERSION_COULD_NOT_BE_STARTED).formatted(e.getMessage()));
        }
    }

    /** Joins the parts that have up to date audio into one mp3 with a chapter for each, and offers it for download (a link in the window). */
    private void downloadWhole() {
        UI ui = UI.getCurrent();
        Notification.success(T.localized(THE_AUDIOBOOK_IS_BEING_JOINED));
        service.wholeAudiobookInBackground(manuscript, (whole, error) -> ui.access(() -> {
            if (!isAttached()) {
                // the window was closed while the book was joined
                delete(whole == null ? null : whole.audio());
                return;
            }
            if (error != null) {
                Notification.error(T.localized(THE_CONVERSION_COULD_NOT_BE_STARTED).formatted(error));
                return;
            }
            if (whole.audio() == null) {
                Notification.error(T.localized(NO_PART_HAS_UP_TO_DATE_AUDIO).formatted(String.join(", ", whole.left())));
                return;
            }
            if (!whole.left().isEmpty()) {
                Notification.warning(T.localized(PARTS_LEFT_OUT_OF_THE_AUDIOBOOK).formatted(String.join(", ", whole.left())));
            }
            Path file = whole.audio();
            replaceWholeFile(file);
            // served from the temporary file, the audiobook is never in memory
            StreamResource resource = new StreamResource(AudiobookFiles.fileSafe(manuscript.getName()) + ".mp3", () -> {
                try {
                    return Files.newInputStream(file);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            resource.setContentType("audio/mpeg");
            long size;
            try {
                size = Files.size(file);
            } catch (IOException e) {
                size = 0;
            }
            Anchor link = new Anchor(resource, T.localized(DOWNLOAD_THE_AUDIOBOOK_FILE).formatted(size / (1024 * 1024) + 1));
            link.getElement().setAttribute("download", true);
            wholeLink.removeAll();
            wholeLink.add(link);
        }));
    }

    /** Keeps the file of the new whole audiobook (null for none) and deletes the one before it. */
    private void replaceWholeFile(Path file) {
        if (wholeFile != null && !wholeFile.equals(file)) {
            delete(wholeFile);
        }
        wholeFile = file;
    }

    private static void delete(Path file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException | RuntimeException e) {
                // a temporary file that stays is not worth an error
            }
        }
    }

    /** The state of the job of a part; when the model labelled the part it is a link to what the model said. */
    private Component stateCell(Row row) {
        if (row.job == null) {
            return new Span();
        }
        String text = stateText(row.job.state()) + (row.job.total() == 0 ? "" : " " + row.job.done() + " / " + row.job.total());
        LabelTrace trace = queue.trace(row.job.id());
        if (trace == null || trace.isEmpty()) {
            return new Span(text);
        }
        Button link = new Button(text, e -> new LabelTraceDialog(row.state.title(), trace).open());
        link.setThemeName("tertiary small");
        link.setTooltipText(T.localized(CLICK_TO_SEE_WHAT_THE_MODEL_SAID));
        return link;
    }

    /** What can be done with a part: play and download its audio, try its job again, or stop it. */
    private HorizontalLayout actions(Row row) {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(false);
        String uuid = row.state.uuid();
        if (row.state.audio() == AudiobookService.StoredAudio.CURRENT) {
            Button play = new Button(Solid.PLAY.create(), e -> play(uuid));
            play.setThemeName("tertiary small");
            play.setTooltipText(T.localized(PLAY));
            buttons.add(play);
            // the bytes are read when the browser asks (a list of many parts must not load all the audio to be shown)
            StreamResource resource = new StreamResource("part-" + row.state.number(), () -> {
                byte[] audioBytes = service.partAudio(uuid);
                return new ByteArrayInputStream(audioBytes == null ? new byte[0] : audioBytes);
            });
            resource.setContentTypeResolver((res, context) -> {
                byte[] audioBytes = service.partAudio(uuid);
                return audioBytes == null ? AudioKind.MP3.mime() : AudioKind.of(audioBytes).mime();
            });
            Anchor download = new Anchor(resource, "");
            download.getElement().setAttribute("download", true);
            Button save = new Button(Solid.DOWNLOAD.create());
            save.setThemeName("tertiary small");
            save.setTooltipText(T.localized(DOWNLOAD));
            download.add(save);
            buttons.add(download);
        }
        ConversionJob.Snapshot job = row.job;
        if (job != null && (job.state() == State.FAILED || job.state() == State.CANCELLED)) {
            Button retry = new Button(T.localized(RETRY), Solid.ROTATE_RIGHT.create(), e -> queue.retry(job.id()));
            retry.setThemeName("tertiary small");
            buttons.add(retry);
        } else if (job != null && !job.finished()) {
            Button cancel = new Button(T.localized(CANCEL), Solid.XMARK.create(), e -> queue.cancel(job.id()));
            cancel.setThemeName("tertiary small");
            buttons.add(cancel);
        }
        return buttons;
    }

    /** Plays the joined audio of a part in the player under the list. */
    private void play(String partUuid) {
        byte[] stored = service.partAudio(partUuid);
        if (stored == null) {
            return;
        }
        AudioKind kind = AudioKind.of(stored);
        StreamResource resource = new StreamResource("part." + kind.extension(), () -> new ByteArrayInputStream(stored));
        resource.setContentType(kind.mime());
        audio.setAttribute("src", resource);
        audio.setAttribute("autoplay", true);
        audio.callJsFunction("load");
    }

    private static String statusText(AudiobookService.PartState state) {
        String text = switch (state.audio()) {
            case CURRENT -> T.localized(PART_GENERATED);
            case OUTDATED -> T.localized(PART_GENERATED_TEXT_CHANGED);
            case NONE -> state.labelled() ? T.localized(PART_LABELLED) : T.localized(PART_NOT_GENERATED);
        };
        return state.finalStatus() ? text + " (" + T.localized(PART_FINAL) + ")" : text;
    }

    private static String planText(EnumSet<Step> plan) {
        if (plan.equals(EnumSet.allOf(Step.class))) {
            return T.localized(REGENERATE);
        }
        List<String> names = new ArrayList<>();
        for (Step step : plan) {
            names.add(T.localized(switch (step) {
                case DELETE_AUDIO -> DELETE_AUDIO;
                case REMOVE_LABELS -> REMOVE_LABELS;
                case GENERATE -> GENERATE;
            }));
        }
        return String.join(", ", names);
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

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        queue.addListener(listener);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        queue.removeListener(listener);
        super.onDetach(detachEvent);
    }
}
