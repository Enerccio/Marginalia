package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.extensions.audiobook.audio.AudioKind;
import com.github.enerccio.marginalia.extensions.audiobook.audio.Ffmpeg;
import com.github.enerccio.marginalia.extensions.audiobook.model.Actor;
import com.github.enerccio.marginalia.extensions.audiobook.model.AudiobookSettings;
import com.github.enerccio.marginalia.extensions.audiobook.model.AutoMode;
import com.github.enerccio.marginalia.extensions.audiobook.label.LabelPrompt;
import com.github.enerccio.marginalia.extensions.audiobook.label.LabelPromptData;
import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.ui.widgets.TemplateHints;
import com.github.enerccio.marginalia.ui.widgets.TextAreaPopoverComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * <i>Audiobook Settings</i>, a panel of <i>Settings → Extension Settings</i>: the speech servers (backends) the audiobook is made by, each with
 * the requests made around a batch. The backends are edited on a copy that {@link #save()} returns; the page stores it with the other
 * settings of the user.
 */
@Configurable(preConstruction = true)
public class AudiobookSettingsForm extends VerticalLayout {

    @Autowired
    private Localization loc;

    private final AudiobookService service;

    private List<BackendConfig> backends = new ArrayList<>();
    private BackendConfig current;
    private boolean binding;

    private final Select<BackendConfig> picker = new Select<>();
    private final Button addButton = new Button(Solid.PLUS.create(), e -> addBackend());
    private final Button deleteButton = new Button(Solid.TRASH.create(), e -> deleteBackend());

    private final Select<Actor> actorPicker = new Select<>();
    private final Button editActor = new Button(Solid.PEN.create(), e -> openActor(actorPicker.getValue()));
    private final Button newActor = new Button(Solid.PLUS.create(), e -> openActor(new Actor()));
    private final Button deleteActor = new Button(Solid.TRASH.create(), e -> confirmDeleteActor());

    private final Select<AutoMode> autoMode = new Select<>();
    private final TextAreaPopoverComponent labelPrompt = new TextAreaPopoverComponent(T.localized(LABELLING_PROMPT));

    private final BackendEditor editor;
    private final VerticalLayout ownSection = new VerticalLayout();
    private final Select<BackendConfig> inUse = new Select<>();
    private final Span ownDisabled = new Span(T.localized(OWN_SPEECH_SERVERS_ARE_OFF));

    public AudiobookSettingsForm(AudiobookService service) {
        this.service = service;
        this.editor = new BackendEditor(service);

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        Span help = new Span(T.localized(SPEECH_SERVERS_HELP));
        help.addClassName(CssStyles.HINT_TEXT);

        picker.setLabel(T.localized(BACKENDS));
        picker.setItemLabelGenerator(b -> StringUtils.defaultIfBlank(b.getName(), T.localized(NEW_BACKEND)));
        picker.setWidth("20em");
        picker.addValueChangeListener(e -> {
            if (!binding) {
                commit();
                show(e.getValue());
                refreshInUse();
            }
        });
        addButton.setThemeName("tertiary icon");
        addButton.setTooltipText(T.localized(ADD_A_BACKEND));
        deleteButton.setThemeName("tertiary icon");
        deleteButton.setTooltipText(T.localized(DELETE_THE_BACKEND));
        HorizontalLayout bar = new HorizontalLayout(picker, addButton, deleteButton);
        bar.setAlignItems(FlexComponent.Alignment.END);


        actorPicker.setLabel(T.localized(ACTORS));
        actorPicker.setItemLabelGenerator(ActorDialog::nameOf);
        actorPicker.setWidth("20em");
        actorPicker.addValueChangeListener(e -> updateActorButtons());
        for (Button button : List.of(editActor, newActor, deleteActor)) {
            button.setThemeName("tertiary icon");
        }
        editActor.setTooltipText(T.localized(EDIT_THE_ACTOR));
        newActor.setTooltipText(T.localized(NEW_ACTOR));
        deleteActor.setTooltipText(T.localized(DELETE_THE_ACTOR));
        HorizontalLayout actorBar = new HorizontalLayout(actorPicker, editActor, newActor, deleteActor);
        actorBar.setAlignItems(FlexComponent.Alignment.END);
        Span actorHelp = new Span(T.localized(ACTORS_HELP));
        actorHelp.addClassName(CssStyles.HINT_TEXT);
        VerticalLayout actorSection = new VerticalLayout(actorBar, actorHelp);
        actorSection.setPadding(false);
        actorSection.addClassName(AudiobookStyles.SECTION);

        autoMode.setLabel(T.localized(AUTOMATIC_CONVERSION_OF_NEW_PARTS));
        autoMode.setItems(AutoMode.values());
        autoMode.setItemLabelGenerator(m -> switch (m) {
            case NO -> T.localized(AUTO_NO);
            case PLAIN -> T.localized(AUTO_PLAIN);
            case LABELLED -> T.localized(AUTO_LABELLED);
        });
        autoMode.setWidth("34em");
        Span autoHelp = new Span(T.localized(A_GENERATED_PART_IS_MADE_INTO_AUDIO_WHEN_IT_IS_FINAL));
        autoHelp.addClassName(CssStyles.HINT_TEXT);
        VerticalLayout autoSection = new VerticalLayout(autoMode, autoHelp);
        autoSection.setPadding(false);
        autoSection.addClassName(AudiobookStyles.SECTION);

        labelPrompt.setWidthFull();
        labelPrompt.setPlaceholder(LabelPrompt.DEFAULT_TEMPLATE);
        labelPrompt.setHelperText(T.localized(THE_INSTRUCTION_THAT_TELLS_THE_MODEL_WHO_SPEAKS_EACH_PIECE));
        ResizableTextArea.install(loc, labelPrompt, "240px");
        labelPrompt.setPopoverContent(TemplateHints.create(loc, labelPrompt, labelPrompt.getPopover(), LabelPromptData.class, LabelPrompt.DEFAULT_TEMPLATE));
        VerticalLayout labelSection = new VerticalLayout(labelPrompt);
        labelSection.setPadding(false);
        labelSection.addClassName(AudiobookStyles.SECTION);

        inUse.setLabel(T.localized(BACKEND_IN_USE));
        inUse.setItemLabelGenerator(b -> StringUtils.defaultIfBlank(b.getName(), T.localized(NEW_BACKEND)) + (service.isShared(b) ? T.localized(SHARED_MARK) : ""));
        inUse.setWidth("24em");
        inUse.setHelperText(T.localized(BACKEND_IN_USE_HELP));

        ownDisabled.addClassName(CssStyles.HINT_TEXT);
        ownSection.setPadding(false);
        ownSection.setSpacing(true);
        ownSection.add(bar, editor);

        add(help, inUse, ownSection, ownDisabled, actorSection, autoSection, labelSection);
        if (service.isAdmin()) {
            add(sharedSection(), storageSection());
        }
        refresh();
    }

    /** For an administrator: the speech servers that users may use, and whether they may bring their own. */
    private VerticalLayout sharedSection() {
        Button open = new Button(T.localized(SHARED_SPEECH_SERVERS), Solid.SERVER.create(), e -> new SharedBackendsDialog(service, this::refresh).open());
        Span sharedHelp = new Span(T.localized(SHARED_SPEECH_SERVERS_HELP));
        sharedHelp.addClassName(CssStyles.HINT_TEXT);
        VerticalLayout section = new VerticalLayout(open, sharedHelp);
        section.setPadding(false);
        section.addClassName(AudiobookStyles.SECTION);
        return section;
    }

    /** For an administrator: how the audio is stored for everyone on the installation. */
    private VerticalLayout storageSection() {
        Select<AudioKind> storage = new Select<>();
        storage.setLabel(T.localized(STORAGE_OF_THE_AUDIO));
        storage.setItems(AudioKind.values());
        storage.setItemLabelGenerator(kind -> kind == AudioKind.WAV ? T.localized(STORAGE_WAV) : T.localized(STORAGE_MP3));
        storage.setWidth("34em");
        boolean ffmpeg = Ffmpeg.available();
        // a wav is encoded by ffmpeg for everything that is not a wav, so without it only mp3 can be chosen
        storage.setItemEnabledProvider(kind -> kind == AudioKind.MP3 || ffmpeg);
        storage.setValue(service.chosenStorage() == AudioKind.WAV && !ffmpeg ? AudioKind.MP3 : service.chosenStorage());
        storage.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null) {
                service.saveStorage(e.getValue());
                Notification.success(T.localized(THE_STORAGE_OF_THE_AUDIO_IS_SAVED));
            }
        });
        Span storageHelp = new Span(T.localized(STORAGE_OF_THE_AUDIO_HELP));
        storageHelp.addClassName(CssStyles.HINT_TEXT);
        Span ffmpegHint = new Span(T.localized(FFMPEG_IS_NOT_INSTALLED));
        ffmpegHint.addClassName(CssStyles.HINT_TEXT);
        ffmpegHint.setVisible(!ffmpeg);
        VerticalLayout section = new VerticalLayout(storage, storageHelp, ffmpegHint);
        section.setPadding(false);
        section.addClassName(AudiobookStyles.SECTION);
        return section;
    }

    /** Shows the stored settings. */
    public void refresh() {
        AudiobookSettings settings = service.getSettings();
        backends = new ArrayList<>(settings.getBackends());
        boolean own = service.usersMayAddOwn();
        ownSection.setVisible(own);
        ownDisabled.setVisible(!own);
        binding = true;
        try {
            picker.setItems(backends);
            BackendConfig first = settings.find(settings.getSelectedBackendId());
            current = first != null ? first : backends.stream().findFirst().orElse(null);
            picker.setValue(current);
        } finally {
            binding = false;
        }
        show(current);
        refreshInUse();
        showActors(settings, actorPicker.getValue() == null ? null : actorPicker.getValue().getId());
        autoMode.setValue(settings.getAutoMode());
        labelPrompt.setValue(StringUtils.defaultString(settings.getLabelPrompt()));
    }

    /** Fills the box of the backend in use: the shared ones the user may use and their own (those on the form, saved or not). */
    private void refreshInUse() {
        BackendConfig kept = inUse.getValue();
        String keptId = kept != null ? kept.getId() : service.getSettings().getSelectedBackendId();
        List<BackendConfig> items = new ArrayList<>(service.sharedSettings().availableTo(service.currentUserUuid()));
        if (service.usersMayAddOwn()) {
            items.addAll(backends);
        }
        inUse.setItems(items);
        BackendConfig value = items.stream().filter(b -> b.getId().equals(keptId)).findFirst().orElse(items.stream().findFirst().orElse(null));
        inUse.setValue(value);
    }

    /** Fills the box of the actors; the pick stays when the actor still exists. */
    private void showActors(AudiobookSettings settings, String picked) {
        List<Actor> actors = settings.getActors();
        actorPicker.setItems(actors);
        actorPicker.setValue(settings.findActor(picked) != null ? settings.findActor(picked) : actors.stream().findFirst().orElse(null));
        updateActorButtons();
    }

    private void updateActorButtons() {
        editActor.setEnabled(actorPicker.getValue() != null);
        deleteActor.setEnabled(actorPicker.getValue() != null);
    }

    private void openActor(Actor edited) {
        if (edited == null) {
            return;
        }
        new ActorDialog(service, edited, saved -> showActors(service.getSettings(), saved.getId())).open();
    }

    private void confirmDeleteActor() {
        Actor doomed = actorPicker.getValue();
        if (doomed == null) {
            return;
        }
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader(T.localized(DELETE_THE_ACTOR));
        confirm.setText(T.localized(THE_ACTOR_IS_DELETED).formatted(ActorDialog.nameOf(doomed)));
        confirm.setCancelable(true);
        confirm.setConfirmText(T.localized(DELETE));
        confirm.addConfirmListener(e -> {
            service.deleteActor(doomed.getId());
            showActors(service.getSettings(), null);
        });
        confirm.open();
    }

    /** The stored settings with what the form shows. */
    public AudiobookSettings save() {
        commit();
        AudiobookSettings stored = service.getSettings();
        stored.setBackends(new ArrayList<>(backends));
        // the actors are stored at once by their dialog, and stay in the stored settings
        stored.setAutoMode(autoMode.getValue());
        // empty means the built-in instruction is used
        stored.setLabelPrompt(labelPrompt.getValue());
        stored.setSelectedBackendId(inUse.getValue() == null ? null : inUse.getValue().getId());
        return stored;
    }

    // --- the backends ---

    private void addBackend() {
        commit();
        BackendConfig created = new BackendConfig();
        created.setName(T.localized(NEW_BACKEND));
        backends.add(created);
        binding = true;
        try {
            picker.setItems(backends);
            picker.setValue(created);
        } finally {
            binding = false;
        }
        show(created);
        refreshInUse();
    }

    private void deleteBackend() {
        if (current == null) {
            return;
        }
        backends.remove(current);
        current = null;
        binding = true;
        try {
            picker.setItems(backends);
            BackendConfig next = backends.isEmpty() ? null : backends.getFirst();
            picker.setValue(next);
            current = next;
        } finally {
            binding = false;
        }
        show(current);
        refreshInUse();
    }

    /** Shows the backend in the editor. */
    private void show(BackendConfig config) {
        current = config;
        deleteButton.setEnabled(config != null);
        editor.show(config);
    }

    private void commit() {
        editor.commit();
    }
}
