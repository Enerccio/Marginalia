package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.audiobook.audio.AudioKind;
import com.github.enerccio.marginalia.extensions.audiobook.label.Delivery;
import com.github.enerccio.marginalia.extensions.audiobook.label.ScriptFromLabels;
import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionJob;
import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionQueue;
import com.github.enerccio.marginalia.extensions.audiobook.script.ScriptEntry;
import com.github.enerccio.marginalia.extensions.audiobook.script.VoiceRef;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.extensions.audiobook.studio.StudioScript;
import com.github.enerccio.marginalia.extensions.audiobook.studio.StudioScript.Status;
import com.github.enerccio.marginalia.extensions.audiobook.studio.StudioSegment;
import com.github.enerccio.marginalia.ui.widgets.ModelProtocolPicker;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * The audiobook studio: the script of one part, segment by segment. The annotation (the language model, or the plain narration) makes a draft; here
 * the segments are read and changed (the words that are spoken, the voice, the delivery, every number the speech server gets, the pause), heard one
 * by one with other takes, and the part is made from the script as it is. A segment that was changed is locked: a new annotation leaves it alone.
 * The part has a status (draft, reviewed, final); a final part is left alone by the batch.
 */
public class StudioDialog extends Dialog {

    private final Manuscript manuscript;
    private final AudiobookService service;
    private final ConversionQueue queue;
    private final VoiceChoices voiceChoices;
    private final Consumer<ConversionJob.Snapshot> listener = snapshot -> UIUtils.accessComponent(this, () -> onJob(snapshot));

    private final List<ChatMessage> parts = new ArrayList<>();
    private final Select<ChatMessage> partBox = new Select<>();
    private final Select<Status> status = new Select<>();
    private final Checkbox label = new Checkbox(T.localized(LABEL_WITH_A_LANGUAGE_MODEL));
    private final ModelProtocolPicker modelPicker = new ModelProtocolPicker();
    private final Button annotate = new Button(T.localized(ANNOTATE), Solid.WAND_MAGIC_SPARKLES.create(), e -> annotate());
    private final Button pronunciation = new Button(T.localized(PRONUNCIATION), Solid.LANGUAGE.create());
    private final Span bookWords = new Span();
    private final Button make = new Button(T.localized(MAKE_THE_PART), Solid.PLAY.create(), e -> makePart());
    private final Button hearPart = new Button(T.localized(HEAR_THE_PART), Solid.HEADPHONES.create(), e -> hearPart());
    private final Span message = new Span();
    /** Shown while a job of the part waits or runs: the annotation takes a while and must not be started twice. */
    private final ProgressBar working = new ProgressBar();
    private final Button stop = new Button(T.localized(STOP), Solid.STOP.create(), e -> stopJobs());
    /** The buttons that start work on the selected segment. */
    private final List<Button> segmentButtons = new ArrayList<>();
    private final Grid<Integer> grid = new Grid<>();

    private final TextField speaker = new TextField(T.localized(SPEAKER));
    private final TextArea text = new TextArea(T.localized(SPOKEN_TEXT));
    private final VoiceField voice;
    private final Select<String> model = new Select<>();
    private final TextField language = new TextField(T.localized(LANGUAGE));
    private final Select<Delivery> delivery = new Select<>();
    private final NumberField intensity = new NumberField(T.localized(INTENSITY));
    private final NumberField exaggeration = new NumberField(T.localized(EXAGGERATION));
    private final NumberField cfgWeight = new NumberField(T.localized(CFG_WEIGHT));
    private final NumberField temperature = new NumberField(T.localized(TEMPERATURE));
    private final NumberField speed = new NumberField(T.localized(SPEED));
    private final IntegerField seed = new IntegerField(T.localized(SEED));
    private final NumberField pause = new NumberField(T.localized(PAUSE_AFTER_SECONDS));
    private final Select<Integer> takes = new Select<>();
    private final TextArea request = new TextArea(T.localized(THE_REQUEST_TO_THE_SERVER));
    private final Span locked = new Span();
    private final Div player = new Div();
    private final Element audio = new Element("audio");

    private ChatMessage part;
    private StudioScript script;
    private int selected = -1;
    private boolean filling;
    /** What to play when the job that makes it is done: {@code segment:<key>} or {@code part}. */
    private String playWhenDone;

    public StudioDialog(Manuscript manuscript, AudiobookService service) {
        this.manuscript = manuscript;
        this.service = service;
        this.queue = service.queue();
        this.voiceChoices = new VoiceChoices(service);
        this.voice = new VoiceField(T.localized(VOICE), voiceChoices);

        setHeaderTitle(T.localized(AUDIOBOOK_STUDIO));
        setWidth("min(1500px, 98vw)");
        setHeight("94vh");
        // no padding of the dialog and no footer: the close button is in the header, the rest of the height is the work
        addThemeVariants(com.vaadin.flow.component.dialog.DialogVariant.LUMO_NO_PADDING);

        try {
            parts.addAll(service.speakableParts(manuscript));
        } catch (Exception e) {
            AudiobookLog.get().warn("The parts of the book could not be read: {}", e.getMessage(), e);
        }
        partBox.setLabel(T.localized(PART));
        partBox.setItems(parts);
        partBox.setItemLabelGenerator(p -> parts.indexOf(p) + 1 + ": " + shorten(p.getResponse(), 60));
        partBox.setWidth("26em");
        partBox.addValueChangeListener(e -> loadPart(e.getValue()));

        status.setLabel(T.localized(STATUS));
        status.setItems(Status.values());
        status.setItemLabelGenerator(StudioDialog::statusText);
        status.setWidth("11em");
        status.addValueChangeListener(e -> {
            if (!filling && script != null && e.getValue() != null) {
                script = script.withStatus(e.getValue());
                service.saveStudio(part.getUuid(), script);
                updateButtons();
            }
        });

        modelPicker.setPlaceholder(T.localized(THE_MODEL_THAT_LABELS));
        try {
            modelPicker.setSelection(service.bookAi(manuscript), service.bookProtocol(manuscript));
        } catch (Exception e) {
            AudiobookLog.get().warn("The model of the book could not be read: {}", e.getMessage(), e);
        }
        label.addValueChangeListener(e -> modelPicker.setEnabled(e.getValue()));
        label.setValue(true);
        message.addClassName(CssStyles.HINT_TEXT);

        pronunciation.setThemeName("tertiary");
        pronunciation.addClickListener(e -> new SpokenFormDialog(service, manuscript, () -> Notification.success(T.localized(THE_PRONUNCIATION_IS_STORED))).open());
        // two rows: the part and what is done with it, then the model that labels (model and protocol side by side)
        partBox.setWidth("24em");
        HorizontalLayout partRow = new HorizontalLayout(partBox, status, annotate, stop, pronunciation);
        partRow.setAlignItems(FlexComponent.Alignment.END);
        partRow.setWrap(true);
        HorizontalLayout labelRow = new HorizontalLayout(label, modelPicker);
        labelRow.setAlignItems(FlexComponent.Alignment.END);
        labelRow.setWrap(true);
        VerticalLayout top = new VerticalLayout(partRow, labelRow);
        top.setPadding(false);
        top.setSpacing(false);

        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER);
        grid.addColumn(i -> i + 1).setHeader("#").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(i -> segment(i).entry().speaker()).setHeader(T.localized(SPEAKER)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(i -> segment(i).entry().text()).setHeader(T.localized(SPOKEN_TEXT)).setFlexGrow(1);
        grid.addColumn(i -> segment(i).delivery().toLowerCase(Locale.ROOT)).setHeader(T.localized(DELIVERY)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(i -> segment(i).locked() ? "\u2713" : "").setHeader(T.localized(LOCKED)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(i -> segment(i).takes().size() > 1 ? String.valueOf(segment(i).takes().size()) : "").setHeader(T.localized(TAKES)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(i -> service.segmentAudio(part.getUuid(), segment(i).entry().key()) != null ? "✓" : "").setHeader(T.localized(MADE))
                .setAutoWidth(true).setFlexGrow(0);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addSelectionListener(e -> select(e.getFirstSelectedItem().orElse(-1)));
        grid.setWidth("55%");
        grid.setHeight("100%");

        VerticalLayout editor = buildEditor();
        HorizontalLayout body = new HorizontalLayout(grid, editor);
        body.setSizeFull();
        body.setFlexGrow(1, editor);

        audio.setAttribute("controls", true);
        audio.getStyle().set("width", "100%");
        player.getElement().appendChild(audio);
        player.setWidthFull();
        HorizontalLayout partBar = new HorizontalLayout(make, hearPart);
        partBar.setAlignItems(FlexComponent.Alignment.CENTER);

        working.setIndeterminate(true);
        working.setVisible(false);
        VerticalLayout content = new VerticalLayout(top, body, partBar, message, working, player);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.addClassName(AudiobookStyles.STUDIO);
        content.setFlexGrow(1, body);
        add(content);

        Button close = new Button(T.localized(CLOSE), e -> close());
        close.setThemeName("tertiary");
        getHeader().add(close);
        if (!parts.isEmpty()) {
            partBox.setValue(parts.getFirst());
        } else {
            message.setText(T.localized(THE_BOOK_HAS_NO_TEXT));
        }
        updateButtons();
    }

    // --- the editor ---

    private VerticalLayout buildEditor() {
        model.setLabel(T.localized(SPEECH_MODEL));
        model.setItems(BackendConfig.MODELS);
        delivery.setLabel(T.localized(DELIVERY));
        delivery.setItems(Delivery.values());
        delivery.setItemLabelGenerator(Delivery::label);
        for (NumberField f : List.of(intensity, exaggeration, cfgWeight, temperature, speed, pause)) {
            f.setStep(0.05);
            f.setStepButtonsVisible(true);
            f.setMin(0);
        }
        intensity.setMax(1);
        seed.setMin(0);
        seed.setStepButtonsVisible(true);
        text.setWidthFull();
        text.setMinHeight("7em");
        speaker.setWidth("12em");
        language.setWidth("6em");
                takes.setLabel(T.localized(TAKE));
        takes.setItemLabelGenerator(s -> T.localized(SEED_X).formatted(s) + (hasAudio(s) ? " ✓" : ""));
        takes.setWidth("14em");
        request.setReadOnly(true);
        request.setWidthFull();
        request.setMinHeight("7em");
        request.setMaxHeight("12em");
        locked.addClassName(CssStyles.HINT_TEXT);
        bookWords.addClassName(CssStyles.HINT_TEXT);

        FormLayout numbers = new FormLayout(model, language, delivery, intensity, exaggeration, cfgWeight, temperature, speed, seed, pause);
        numbers.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2), new FormLayout.ResponsiveStep("30em", 5));

        Button apply = new Button(T.localized(APPLY), Solid.CHECK.create(), e -> apply());
        apply.addThemeName("primary");
        Button hear = new Button(T.localized(HEAR), Solid.VOLUME_HIGH.create(), e -> hear(null));
        Button another = new Button(T.localized(ANOTHER_TAKE), Solid.DICE.create(), e -> anotherTake());
        Button lower = new Button(T.localized(LOWER_THE_INTENSITY), Solid.ARROW_DOWN.create(), e -> lowerIntensity());
        Button longer = new Button(T.localized(MORE_PAUSE), Solid.PAUSE.create(), e -> morePause());
        Button unlock = new Button(T.localized(UNLOCK), Solid.UNLOCK.create(), e -> unlock());
        Button hearTake = new Button(T.localized(CHOOSE_AND_HEAR_THE_TAKE), Solid.CHECK_DOUBLE.create(), e -> chooseTake());
        for (Button b : List.of(hear, another, lower, longer, unlock, hearTake)) {
            b.setThemeName("tertiary small");
        }
        segmentButtons.addAll(List.of(hear, another));
        HorizontalLayout main = new HorizontalLayout(apply, hear, another, lower, longer);
        main.setWrap(true);
        HorizontalLayout takeRow = new HorizontalLayout(takes, hearTake, unlock);
        takeRow.setAlignItems(FlexComponent.Alignment.END);

        voice.setWidth("auto");
        HorizontalLayout who = new HorizontalLayout(speaker, voice, locked);
        who.setAlignItems(FlexComponent.Alignment.END);
        who.setWrap(true);
        VerticalLayout editor = new VerticalLayout(who, bookWords, text, numbers, main, takeRow, request);
        editor.setPadding(false);
        editor.setSpacing(false);
        editor.addThemeName("spacing-xs");
        editor.setHeight("100%");
        editor.getStyle().set("overflow-y", "auto");
        return editor;
    }

    private StudioSegment segment(int index) {
        return script.segments().get(index);
    }

    private boolean hasAudio(int takeSeed) {
        return selected >= 0 && service.segmentAudio(part.getUuid(), segment(selected).entryOfTake(takeSeed).key()) != null;
    }

    private void select(int index) {
        selected = script != null && index >= 0 && index < script.segments().size() ? index : -1;
        fill();
    }

    /** Shows the selected segment in the fields. */
    private void fill() {
        filling = true;
        try {
            boolean any = selected >= 0;
            for (com.vaadin.flow.component.HasEnabled component : List.<com.vaadin.flow.component.HasEnabled>of(speaker, text, voice, model, language, delivery, intensity,
                    exaggeration, cfgWeight, temperature, speed, seed, pause, takes)) {
                component.setEnabled(any);
            }
            if (!any) {
                speaker.clear();
                text.clear();
                voice.clear();
                request.clear();
                locked.setText("");
                bookWords.setText("");
                return;
            }
            StudioSegment segment = segment(selected);
            ScriptEntry entry = segment.entry();
            speaker.setValue(entry.speaker());
            // the words of the book, when what is said is spelled differently (the pronunciation rules)
            bookWords.setText(segment.source().equals(entry.text()) ? "" : T.localized(IN_THE_BOOK).formatted(segment.source()));
            text.setValue(entry.text());
            voice.setValue(entry.voice().toString());
            model.setValue(entry.model());
            language.setValue(entry.language() == null ? "" : entry.language());
            delivery.setValue(deliveryOf(segment));
            intensity.setValue(segment.intensity());
            exaggeration.setValue(entry.exaggeration());
            cfgWeight.setValue(entry.cfgWeight());
            temperature.setValue(entry.temperature());
            speed.setValue(entry.speed());
            seed.setValue(entry.seed());
            pause.setValue(entry.pauseAfter());
            takes.setItems(segment.takes());
            takes.setValue(entry.seed());
            locked.setText(segment.locked() ? T.localized(CHANGED_BY_HAND_KEPT_BY_A_NEW_ANNOTATION) : T.localized(AS_THE_ANNOTATION_MADE_IT));
            request.setValue(service.requestOf(entry, service::serverNameOfClip));
        } finally {
            filling = false;
        }
    }

    private static Delivery deliveryOf(StudioSegment segment) {
        try {
            return Delivery.valueOf(segment.delivery());
        } catch (IllegalArgumentException e) {
            return Delivery.NEUTRAL;
        }
    }

    /** The entry the fields describe. */
    private ScriptEntry readEntry(ScriptEntry old) {
        String languageText = language.getValue().strip();
        VoiceRef chosen;
        try {
            chosen = voice.getValue().isBlank() ? old.voice() : VoiceRef.parse(voice.getValue());
        } catch (IllegalArgumentException e) {
            chosen = old.voice();
        }
        return new ScriptEntry(speaker.getValue().strip().isEmpty() ? old.speaker() : speaker.getValue().strip(),
                text.getValue().isBlank() ? old.text() : text.getValue().strip(), chosen, model.getValue() == null ? old.model() : model.getValue(),
                languageText.isEmpty() ? null : languageText, number(exaggeration, old.exaggeration()), number(cfgWeight, old.cfgWeight()),
                number(temperature, old.temperature()), number(speed, old.speed()), seed.getValue() == null ? old.seed() : seed.getValue(), old.chunkSize(),
                old.format(), number(pause, old.pauseAfter()));
    }

    private static double number(NumberField field, double fallback) {
        return field.getValue() == null ? fallback : field.getValue();
    }

    /** Keeps what the fields say: the segment is changed (locked) when anything differs. */
    private void apply() {
        if (selected < 0) {
            return;
        }
        StudioSegment old = segment(selected);
        ScriptEntry entry = readEntry(old.entry());
        double newIntensity = number(intensity, old.intensity());
        Delivery newDelivery = delivery.getValue() == null ? deliveryOf(old) : delivery.getValue();
        boolean turbo = BackendConfig.MODEL_TURBO.equals(entry.model());
        // the intensity of a model that has an exaggeration moves it, unless it was set by hand in the same go
        if (newIntensity != old.intensity() && entry.exaggeration() == old.entry().exaggeration() && !turbo) {
            entry = withExaggeration(entry, ScriptFromLabels.exaggeration(newIntensity));
        }
        // another delivery asks the cast for the voice of that mood, unless the voice was set by hand in the same go
        if (!newDelivery.name().equals(old.delivery()) && entry.voice().equals(old.entry().voice())) {
            entry = withVoice(entry, service.voiceFor(manuscript, service.getSettings().selected(), entry.speaker(), newDelivery.name(), entry.language()));
        }
        StudioSegment changed = old;
        if (!entry.equals(old.entry())) {
            changed = changed.edited(entry);
        }
        if (!newDelivery.name().equals(old.delivery()) || newIntensity != old.intensity()) {
            changed = changed.withDelivery(newDelivery.name(), newIntensity);
        }
        if (changed != old) {
            store(changed);
        }
    }

    private static ScriptEntry withExaggeration(ScriptEntry e, double value) {
        return new ScriptEntry(e.speaker(), e.text(), e.voice(), e.model(), e.language(), value, e.cfgWeight(), e.temperature(), e.speed(), e.seed(), e.chunkSize(),
                e.format(), e.pauseAfter());
    }

    private static ScriptEntry withVoice(ScriptEntry e, VoiceRef voice) {
        return new ScriptEntry(e.speaker(), e.text(), voice, e.model(), e.language(), e.exaggeration(), e.cfgWeight(), e.temperature(), e.speed(), e.seed(),
                e.chunkSize(), e.format(), e.pauseAfter());
    }

    private static ScriptEntry withPause(ScriptEntry e, double pause) {
        return new ScriptEntry(e.speaker(), e.text(), e.voice(), e.model(), e.language(), e.exaggeration(), e.cfgWeight(), e.temperature(), e.speed(), e.seed(),
                e.chunkSize(), e.format(), pause);
    }

    /** Puts the changed segment into the script, stores the script and shows it. */
    private void store(StudioSegment changed) {
        script = script.withSegment(selected, changed);
        service.saveStudio(part.getUuid(), script);
        int keep = selected;
        refreshGrid();
        grid.select(keep);
        select(keep);
    }

    private void anotherTake() {
        if (selected < 0 || busy()) {
            return;
        }
        store(segment(selected).anotherTake(ThreadLocalRandom.current().nextInt(1, 1_000_000)));
        hear(null);
    }

    private void lowerIntensity() {
        if (selected < 0) {
            return;
        }
        StudioSegment old = segment(selected);
        double lower = Math.max(0, Math.round((old.intensity() - 0.15) * 100) / 100.0);
        ScriptEntry entry = BackendConfig.MODEL_TURBO.equals(old.entry().model()) ? old.entry()
                : withExaggeration(old.entry(), ScriptFromLabels.exaggeration(lower));
        // on Turbo the intensity changes nothing in the sound: a new seed makes the difference audible
        StudioSegment changed = old.edited(entry).withDelivery(old.delivery(), lower);
        store(BackendConfig.MODEL_TURBO.equals(old.entry().model()) ? changed.anotherTake(ThreadLocalRandom.current().nextInt(1, 1_000_000)) : changed);
        hear(null);
    }

    private void morePause() {
        if (selected < 0) {
            return;
        }
        StudioSegment old = segment(selected);
        store(old.edited(withPause(old.entry(), Math.round((old.entry().pauseAfter() + 0.3) * 100) / 100.0)));
    }

    private void unlock() {
        if (selected >= 0) {
            store(segment(selected).withLocked(false));
        }
    }

    /** The take picked in the box becomes the current one of the segment, and is heard. */
    private void chooseTake() {
        if (selected < 0 || takes.getValue() == null) {
            return;
        }
        StudioSegment old = segment(selected);
        if (takes.getValue() != old.entry().seed()) {
            store(old.edited(old.entry().withSeed(takes.getValue())));
        }
        hear(null);
    }

    // --- the work ---

    private void loadPart(ChatMessage chosen) {
        part = chosen;
        selected = -1;
        script = chosen == null ? null : service.studio(chosen.getUuid());
        filling = true;
        try {
            status.setValue(script == null ? Status.DRAFT : script.status());
        } finally {
            filling = false;
        }
        refreshGrid();
        fill();
        if (chosen != null) {
            boolean stale = script != null && com.github.enerccio.marginalia.extensions.audiobook.script.PartScript.stale(script.sourceHash(), chosen.getResponse());
            message.setText(script == null ? T.localized(THIS_PART_HAS_NO_SCRIPT_YET) : stale ? T.localized(THE_TEXT_CHANGED_ANNOTATE_AGAIN) : "");
        }
        updateButtons();
    }

    private void refreshGrid() {
        grid.setItems(script == null ? List.of() : java.util.stream.IntStream.range(0, script.segments().size()).boxed().toList());
    }

    private void updateButtons() {
        boolean haveScript = script != null && !script.segments().isEmpty();
        boolean isFinal = script != null && script.status() == Status.FINAL;
        boolean busy = busy();
        annotate.setEnabled(part != null && !isFinal && !busy);
        make.setEnabled(haveScript && !busy);
        hearPart.setEnabled(part != null);
        status.setEnabled(script != null);
        segmentButtons.forEach(b -> b.setEnabled(!busy));
        working.setVisible(busy);
        stop.setVisible(busy);
    }

    /** Stops what runs for the selected part (the annotation asks the language model, the making asks the speech server): both end at once. */
    private void stopJobs() {
        if (part == null) {
            return;
        }
        queue.jobs().stream().filter(j -> j.partId().equals(part.getUuid()) && !j.finished()).forEach(j -> queue.cancel(j.id()));
        playWhenDone = null;
    }

    /** Whether a job of the selected part waits or runs (the annotation, the making of the part, the hearing of a segment). */
    private boolean busy() {
        return part != null && queue.jobs().stream().anyMatch(j -> j.partId().equals(part.getUuid()) && !j.finished());
    }

    private BackendConfig backend() {
        BackendConfig backend = service.getSettings().selected();
        if (backend == null) {
            Notification.error(T.localized(NO_BACKEND_YET));
        }
        return backend;
    }

    private void annotate() {
        BackendConfig backend = backend();
        if (backend == null || part == null || busy()) {
            return;
        }
        try {
            int number = parts.indexOf(part) + 1;
            var options = label.getValue() ? new AudiobookService.LabelOptions(modelPicker.getAi(), modelPicker.getProtocol()) : null;
            var jobs = service.convert(manuscript, backend, number, number, options, ConversionJob.Mode.SCRIPT_ONLY);
            message.setText(jobs.isEmpty() ? T.localized(A_FINAL_PART_IS_NOT_ANNOTATED) : T.localized(ANNOTATING));
            updateButtons();
        } catch (Exception e) {
            AudiobookLog.get().warn("The annotation could not be started: {}", e.getMessage(), e);
            Notification.error(T.localized(THE_CONVERSION_COULD_NOT_BE_STARTED).formatted(e.getMessage()));
        }
    }

    private void makePart() {
        if (busy()) {
            return;
        }
        try {
            service.render(part.getUuid(), "Part " + (parts.indexOf(part) + 1));
            playWhenDone = "part";
            message.setText(T.localized(MAKING_THE_PART));
            updateButtons();
        } catch (IllegalArgumentException e) {
            Notification.error(e.getMessage());
        } catch (Exception e) {
            AudiobookLog.get().warn("The part could not be made: {}", e.getMessage(), e);
            Notification.error(T.localized(THE_CONVERSION_COULD_NOT_BE_STARTED).formatted(e.getMessage()));
        }
    }

    private void hearPart() {
        byte[] audioBytes = part == null ? null : service.partAudio(part.getUuid());
        if (audioBytes == null) {
            Notification.error(T.localized(THE_PART_HAS_NO_AUDIO_YET));
            return;
        }
        play(audioBytes);
    }

    /** Plays the selected segment (or the take with the given seed): at once when its audio is there, else after the job that makes it. */
    private void hear(Integer takeSeed) {
        if (selected < 0 || busy()) {
            return;
        }
        ScriptEntry entry = takeSeed == null ? segment(selected).entry() : segment(selected).entryOfTake(takeSeed);
        byte[] ready = service.segmentAudio(part.getUuid(), entry.key());
        if (ready != null) {
            play(ready);
            return;
        }
        service.hear(part.getUuid(), "Part " + (parts.indexOf(part) + 1) + ", segment " + (selected + 1), List.of(entry), script.sourceHash());
        playWhenDone = "segment:" + entry.key();
        message.setText(T.localized(MAKING_THE_SEGMENT));
        updateButtons();
    }

    private void play(byte[] bytes) {
        AudioKind kind = AudioKind.of(bytes);
        StreamResource resource = new StreamResource("studio-" + System.nanoTime() + "." + kind.extension(), () -> new ByteArrayInputStream(bytes));
        resource.setContentType(kind.mime());
        audio.setAttribute("src", resource);
        audio.callJsFunction("play");
    }

    private void onJob(ConversionJob.Snapshot job) {
        if (part == null || !job.partId().equals(part.getUuid())) {
            return;
        }
        if (!job.finished()) {
            // a job of this part (also one started from the dialog of the conversion): the buttons wait for it
            updateButtons();
            return;
        }
        if (job.state() == ConversionJob.State.FAILED) {
            message.setText(T.localized(THE_JOB_FAILED).formatted(job.message()));
            playWhenDone = null;
            updateButtons();
            return;
        }
        if (job.state() == ConversionJob.State.CANCELLED) {
            message.setText("");
            playWhenDone = null;
            updateButtons();
            return;
        }
        int keep = selected;
        loadPart(part);
        message.setText(job.message());
        if (keep >= 0 && script != null && keep < script.segments().size()) {
            grid.select(keep);
        }
        String wanted = playWhenDone;
        playWhenDone = null;
        if (wanted == null) {
            return;
        }
        if (wanted.equals("part")) {
            hearPart();
        } else if (wanted.startsWith("segment:")) {
            String key = wanted.substring("segment:".length());
            byte[] bytes = service.segmentAudio(part.getUuid(), key);
            if (bytes != null) {
                play(bytes);
            }
        }
    }

    private static String statusText(Status status) {
        return switch (status) {
            case DRAFT -> T.localized(STATUS_DRAFT);
            case REVIEWED -> T.localized(STATUS_REVIEWED);
            case FINAL -> T.localized(STATUS_FINAL);
        };
    }

    private static String shorten(String text, int max) {
        String flat = text == null ? "" : text.strip().replaceAll("\\s+", " ");
        return flat.length() > max ? flat.substring(0, max) + "..." : flat;
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
