package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.extensions.audiobook.model.Actor;
import com.github.enerccio.marginalia.extensions.audiobook.model.AudiobookSettings;
import com.github.enerccio.marginalia.extensions.audiobook.model.AutoMode;
import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.extensions.httphooks.ui.HookListEditor;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * <i>Audiobook Settings</i>, a panel of <i>Settings → Extension Settings</i>: the speech servers (backends) the audiobook is made by, each with
 * the requests made around a batch. The backends are edited on a copy that {@link #save()} returns; the page stores it with the other
 * settings of the user.
 */
public class AudiobookSettingsForm extends VerticalLayout {

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

    private final VerticalLayout editor = new VerticalLayout();
    private final TextField name = new TextField(T.localized(NAME));
    private final TextField baseUrl = new TextField(T.localized(ADDRESS));
    private final PasswordField apiKey = new PasswordField(T.localized(API_KEY));
    private final Button removeKey = new Button(T.localized(REMOVE_THE_KEY), e -> removeKey());
    private final PasswordField hooksKey = new PasswordField(T.localized(KEY_OF_THE_REQUESTS));
    private final Button removeHooksKey = new Button(T.localized(REMOVE_THE_KEY), e -> removeHooksKey());
    private final Select<String> model = new Select<>();
    private final Checkbox local = new Checkbox(T.localized(THE_SERVER_IS_LOCAL));
    private final VoiceChoices voiceChoices;
    private final VoiceField narratorVoice;
    private final IntegerField timeout = new IntegerField(T.localized(TIMEOUT_SECONDS));
    private final NumberField temperature = new NumberField(T.localized(TEMPERATURE));
    private final NumberField cfgWeight = new NumberField(T.localized(CFG_WEIGHT));
    private final NumberField speed = new NumberField(T.localized(SPEED));
    private final IntegerField chunkSize = new IntegerField(T.localized(CHUNK_SIZE));
    private final HookListEditor before = new HookListEditor(hookLabels(), AudiobookStyles.SECTION);
    private final HookListEditor after = new HookListEditor(hookLabels(), AudiobookStyles.SECTION);
    private final Button test = new Button(T.localized(TEST_CONNECTION), Solid.PLUG.create(), e -> testConnection());

    public AudiobookSettingsForm(AudiobookService service) {
        this.service = service;
        this.voiceChoices = new VoiceChoices(service);
        this.narratorVoice = new VoiceField(T.localized(NARRATOR_VOICE), voiceChoices);

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
            }
        });
        addButton.setThemeName("tertiary icon");
        addButton.setTooltipText(T.localized(ADD_A_BACKEND));
        deleteButton.setThemeName("tertiary icon");
        deleteButton.setTooltipText(T.localized(DELETE_THE_BACKEND));
        HorizontalLayout bar = new HorizontalLayout(picker, addButton, deleteButton);
        bar.setAlignItems(FlexComponent.Alignment.END);

        name.setWidth("14em");
        baseUrl.setWidthFull();
        baseUrl.setHelperText(T.localized(BASE_ADDRESS_OF_THE_SPEECH_SERVER));
        apiKey.setWidth("20em");
        apiKey.setRevealButtonVisible(false);
        removeKey.setThemeName("tertiary small");
        hooksKey.setWidth("20em");
        hooksKey.setRevealButtonVisible(false);
        hooksKey.setHelperText(T.localized(KEY_OF_THE_REQUESTS_HELP));
        removeHooksKey.setThemeName("tertiary small");
        model.setLabel(T.localized(SPEECH_MODEL));
        model.setItems(BackendConfig.MODELS);
        model.setItemLabelGenerator(AudiobookSettingsForm::modelLabel);
        model.setWidth("22em");
        model.setHelperText(T.localized(THE_MODEL_THE_SERVER_IS_ASKED_TO_LOAD));
        test.setTooltipText(T.localized(TEST_HELP));
        local.getElement().setProperty("title", T.localized(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK));
        narratorVoice.setWidthFull();
        narratorVoice.setHelperText(T.localized(NARRATOR_VOICE_HELP));
        timeout.setMin(0);
        timeout.setStepButtonsVisible(true);
        for (NumberField f : List.of(temperature, cfgWeight, speed)) {
            f.setMin(0);
            f.setStep(0.1);
            f.setStepButtonsVisible(true);
            f.setWidth("9em");
        }
        chunkSize.setMin(BackendConfig.MIN_CHUNK_SIZE);
        chunkSize.setMax(BackendConfig.MAX_CHUNK_SIZE);
        chunkSize.setStepButtonsVisible(true);

        HorizontalLayout identity = new HorizontalLayout(name, model);
        identity.setWrap(true);
        identity.setAlignItems(FlexComponent.Alignment.START);
        HorizontalLayout credentials = new HorizontalLayout(apiKey, removeKey);
        credentials.setWrap(true);
        credentials.setAlignItems(FlexComponent.Alignment.END);
        HorizontalLayout requestOptions = new HorizontalLayout(timeout);
        requestOptions.setWrap(true);
        HorizontalLayout defaults = new HorizontalLayout(temperature, cfgWeight, speed, chunkSize);
        defaults.setWrap(true);
        Details defaultsDetails = new Details(T.localized(DEFAULTS_OF_A_SEGMENT), defaults);

        Details beforeDetails = new Details(T.localized(BEFORE_A_BATCH), before);
        Details afterDetails = new Details(T.localized(AFTER_A_BATCH), after);
        Span hookHint = new Span(T.localized(REQUESTS_MADE_AROUND_A_BATCH));
        hookHint.addClassName(CssStyles.HINT_TEXT);
        HorizontalLayout hooksCredentials = new HorizontalLayout(hooksKey, removeHooksKey);
        hooksCredentials.setWrap(true);
        hooksCredentials.setAlignItems(FlexComponent.Alignment.START);

        editor.setPadding(false);
        editor.setSpacing(true);
        editor.add(identity, baseUrl, credentials, narratorVoice, local, requestOptions, defaultsDetails, hookHint, hooksCredentials, beforeDetails, afterDetails, test);
        editor.addClassName(AudiobookStyles.SECTION);

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

        add(help, bar, editor, actorSection, autoSection);
        refresh();
    }

    private static String modelLabel(String model) {
        return switch (model) {
            case BackendConfig.MODEL_ORIGINAL -> T.localized(MODEL_ORIGINAL);
            case BackendConfig.MODEL_MULTILINGUAL -> T.localized(MODEL_MULTILINGUAL);
            default -> T.localized(MODEL_TURBO);
        };
    }

    private static HookListEditor.Labels hookLabels() {
        return new HookListEditor.Labels(T.localized(ADDRESS), T.localized(METHOD), T.localized(HEADERS_NAME_VALUE_ONE_PER_LINE), T.localized(BODY),
                T.localized(TIMEOUT_SECONDS), T.localized(WAIT_AFTERWARDS_SECONDS), T.localized(REQUEST), T.localized(ADD_A_REQUEST),
                T.localized(REMOVE_THE_REQUEST));
    }

    /** Shows the stored settings. */
    public void refresh() {
        AudiobookSettings settings = service.getSettings();
        backends = new ArrayList<>(settings.getBackends());
        binding = true;
        try {
            picker.setItems(backends);
            current = settings.selected();
            picker.setValue(current);
        } finally {
            binding = false;
        }
        show(current);
        showActors(settings, actorPicker.getValue() == null ? null : actorPicker.getValue().getId());
        autoMode.setValue(settings.getAutoMode());
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
        if (stored.find(stored.getSelectedBackendId()) == null) {
            stored.setSelectedBackendId(current == null ? null : current.getId());
        }
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
    }

    private void removeHooksKey() {
        if (current != null) {
            current.setProtectedHooksKey(null);
            hooksKey.clear();
            updateKeyState();
        }
    }

    private void removeKey() {
        if (current != null) {
            current.setProtectedApiKey(null);
            apiKey.clear();
            updateKeyState();
        }
    }

    /** Fills the fields with the backend. */
    private void show(BackendConfig config) {
        current = config;
        editor.setVisible(config != null);
        deleteButton.setEnabled(config != null);
        if (config == null) {
            return;
        }
        binding = true;
        try {
            name.setValue(config.getName());
            baseUrl.setValue(config.getBaseUrl());
            apiKey.clear();
            hooksKey.clear();
            model.setValue(config.getModel());
            narratorVoice.setValue(config.getNarratorVoice());
            local.setValue(config.isLocal());
            timeout.setValue(config.getTimeoutSeconds());
            temperature.setValue(config.getTemperature());
            cfgWeight.setValue(config.getCfgWeight());
            speed.setValue(config.getSpeed());
            chunkSize.setValue(config.getChunkSize());
            before.show(config.getBeforeHooks(), service::reveal);
            after.show(config.getAfterHooks(), service::reveal);
        } finally {
            binding = false;
        }
        updateKeyState();
    }

    /** Puts what the fields show into the backend that is open. */
    private void commit() {
        if (current == null) {
            return;
        }
        current.setName(name.getValue().strip());
        current.setBaseUrl(baseUrl.getValue().strip());
        if (!apiKey.isEmpty()) {
            current.setProtectedApiKey(service.protect(apiKey.getValue().strip()));
            apiKey.clear();
        }
        if (!hooksKey.isEmpty()) {
            current.setProtectedHooksKey(service.protect(hooksKey.getValue().strip()));
            hooksKey.clear();
        }
        current.setModel(model.getValue() == null ? BackendConfig.MODEL_TURBO : model.getValue());
        current.setNarratorVoice(narratorVoice.getValue().strip());
        current.setLocal(local.getValue());
        current.setTimeoutSeconds(positive(timeout, BackendConfig.DEFAULT_TIMEOUT_SECONDS));
        current.setTemperature(positive(temperature, BackendConfig.DEFAULT_TEMPERATURE));
        current.setCfgWeight(positive(cfgWeight, BackendConfig.DEFAULT_CFG_WEIGHT));
        current.setSpeed(positive(speed, BackendConfig.DEFAULT_SPEED));
        current.setChunkSize(positive(chunkSize, BackendConfig.DEFAULT_CHUNK_SIZE));
        current.setBeforeHooks(before.read(service::protect));
        current.setAfterHooks(after.read(service::protect));
        updateKeyState();
    }

    private static int positive(IntegerField field, int fallback) {
        Integer value = field.getValue();
        return value == null || value <= 0 ? fallback : value;
    }

    private static double positive(NumberField field, double fallback) {
        Double value = field.getValue();
        return value == null || value <= 0 ? fallback : value;
    }

    private void updateKeyState() {
        boolean stored = current != null && current.getProtectedApiKey() != null;
        apiKey.setPlaceholder(stored ? T.localized(STORED_TYPE_TO_REPLACE) : "");
        removeKey.setVisible(stored);
        boolean hooksStored = current != null && current.getProtectedHooksKey() != null;
        hooksKey.setPlaceholder(hooksStored ? T.localized(STORED_TYPE_TO_REPLACE) : "");
        removeHooksKey.setVisible(hooksStored);
    }

    private void testConnection() {
        commit();
        BackendConfig config = current;
        if (config == null) {
            return;
        }
        UI ui = UI.getCurrent();
        test.setEnabled(false);
        boolean around = config.getBeforeHooks().stream().anyMatch(h -> h != null && !h.isEmpty())
                || config.getAfterHooks().stream().anyMatch(h -> h != null && !h.isEmpty());
        service.test(config, (answer, error) -> ui.access(() -> {
            test.setEnabled(true);
            String ran = around ? T.localized(THE_REQUESTS_AROUND_THE_TEST_RAN) : "";
            if (answer == null) {
                Notification.error(T.localized(CONNECTION_FAILED).formatted(error));
            } else if (error != null) {
                Notification.warning(error);
            } else if (answer.modelLoaded()) {
                Notification.success(ran + T.localized(THE_SERVER_ANSWERS_THE_MODEL_IS_LOADED).formatted(StringUtils.defaultString(answer.model())));
            } else {
                Notification.success(ran + T.localized(THE_SERVER_ANSWERS_NO_MODEL));
            }
        }));
    }
}
