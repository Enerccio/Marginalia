package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.extensions.httphooks.ui.HookListEditor;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
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

import java.util.List;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * The fields of one speech server ({@link BackendConfig}): name, model, address, keys, voice, defaults and the requests around a batch, with the
 * test of the connection. Shown for the backends of the user in {@link AudiobookSettingsForm} and for the shared ones of the administrator in
 * {@link SharedBackendsDialog}. {@link #show} fills the fields, {@link #commit} puts them back into the backend that is shown.
 */
public class BackendEditor extends VerticalLayout {

    private final AudiobookService service;
    private BackendConfig current;

    private final TextField name = new TextField(T.localized(NAME));
    private final TextField baseUrl = new TextField(T.localized(ADDRESS));
    private final PasswordField apiKey = new PasswordField(T.localized(API_KEY));
    private final Button removeKey = new Button(T.localized(REMOVE_THE_KEY), e -> removeKey());
    private final PasswordField hooksKey = new PasswordField(T.localized(KEY_OF_THE_REQUESTS));
    private final Button removeHooksKey = new Button(T.localized(REMOVE_THE_KEY), e -> removeHooksKey());
    private final Select<String> model = new Select<>();
    private final TextField gpu = new TextField(T.localized(GPU_NAME));
    private final Checkbox local = new Checkbox(T.localized(THE_SERVER_IS_LOCAL));
    private final VoiceChoices voiceChoices;
    private final VoiceField narratorVoice;
    private final IntegerField timeout = new IntegerField(T.localized(TIMEOUT_SECONDS));
    private final IntegerField restartEvery = new IntegerField(T.localized(RESTART_THE_SERVER_AFTER_PIECES));
    private final NumberField temperature = new NumberField(T.localized(TEMPERATURE));
    private final NumberField cfgWeight = new NumberField(T.localized(CFG_WEIGHT));
    private final NumberField speed = new NumberField(T.localized(SPEED));
    private final IntegerField chunkSize = new IntegerField(T.localized(CHUNK_SIZE));
    private final HookListEditor before = new HookListEditor(hookLabels(), AudiobookStyles.SECTION);
    private final HookListEditor after = new HookListEditor(hookLabels(), AudiobookStyles.SECTION);
    private final Button test = new Button(T.localized(TEST_CONNECTION), Solid.PLUG.create(), e -> testConnection());

    public BackendEditor(AudiobookService service) {
        this.service = service;
        this.voiceChoices = new VoiceChoices(service);
        this.narratorVoice = new VoiceField(T.localized(NARRATOR_VOICE), voiceChoices);

        setPadding(false);
        setSpacing(true);

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
        model.setItemLabelGenerator(BackendEditor::modelLabel);
        model.setWidth("22em");
        model.setHelperText(T.localized(THE_MODEL_THE_SERVER_IS_ASKED_TO_LOAD));
        model.addValueChangeListener(e -> showFieldsOf(e.getValue()));
        test.setTooltipText(T.localized(TEST_HELP));
        local.getElement().setProperty("title", T.localized(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK));
        narratorVoice.setWidthFull();
        narratorVoice.setHelperText(T.localized(NARRATOR_VOICE_HELP));
        timeout.setMin(0);
        timeout.setStepButtonsVisible(true);
        restartEvery.setMin(0);
        restartEvery.setStepButtonsVisible(true);
        restartEvery.setStep(10);
        restartEvery.setHelperText(T.localized(RESTART_THE_SERVER_HELP));
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
        HorizontalLayout requestOptions = new HorizontalLayout(timeout, restartEvery);
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

        gpu.setWidth("14em");
        gpu.setHelperText(T.localized(GPU_NAME_HELP));
        add(identity, baseUrl, gpu, credentials, narratorVoice, local, requestOptions, defaultsDetails, hookHint, hooksCredentials, beforeDetails, afterDetails, test);
        addClassName(AudiobookStyles.SECTION);
    }

    /** Higgs (ComfyUI) has none of the speech server's cfg weight, speed and chunk size, and its address is the one of ComfyUI. */
    private void showFieldsOf(String chosen) {
        boolean higgs = BackendConfig.MODEL_HIGGS.equals(chosen);
        cfgWeight.setVisible(!higgs);
        speed.setVisible(!higgs);
        chunkSize.setVisible(!higgs);
        baseUrl.setHelperText(T.localized(higgs ? COMFYUI_ADDRESS_HELP : BASE_ADDRESS_OF_THE_SPEECH_SERVER));
    }

    /**
     * Calls the listener when the user renames the open backend; the name is already in it. The boxes that list the backends refresh its label
     * with it (a label is made when the items are set).
     */
    public void onRename(Runnable listener) {
        name.addValueChangeListener(e -> {
            if (e.isFromClient() && current != null) {
                current.setName(name.getValue().strip());
                listener.run();
            }
        });
    }

    /** The first field, for the focus of a dialog. */
    public TextField nameField() {
        return name;
    }

    /** The address field, which sends a dialog with Enter as the name does. */
    public TextField addressField() {
        return baseUrl;
    }

    /** The backend that is shown, null when none. */
    public BackendConfig current() {
        return current;
    }

    private static String modelLabel(String model) {
        return switch (model) {
            case BackendConfig.MODEL_ORIGINAL -> T.localized(MODEL_ORIGINAL);
            case BackendConfig.MODEL_MULTILINGUAL -> T.localized(MODEL_MULTILINGUAL);
            case BackendConfig.MODEL_HIGGS -> T.localized(MODEL_HIGGS);
            default -> T.localized(MODEL_TURBO);
        };
    }

    private static HookListEditor.Labels hookLabels() {
        return new HookListEditor.Labels(T.localized(ADDRESS), T.localized(METHOD), T.localized(HEADERS_NAME_VALUE_ONE_PER_LINE), T.localized(BODY),
                T.localized(TIMEOUT_SECONDS), T.localized(WAIT_AFTERWARDS_SECONDS), T.localized(REQUEST), T.localized(ADD_A_REQUEST),
                T.localized(REMOVE_THE_REQUEST));
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
    public void show(BackendConfig config) {
        current = config;
        setVisible(config != null);
        if (config == null) {
            return;
        }
        name.setValue(config.getName());
        baseUrl.setValue(config.getBaseUrl());
        apiKey.clear();
        hooksKey.clear();
        model.setValue(config.getModel());
        narratorVoice.setValue(config.getNarratorVoice());
        gpu.setValue(config.getGpu());
        local.setValue(config.isLocal());
        timeout.setValue(config.getTimeoutSeconds());
        restartEvery.setValue(config.getRestartEveryPieces());
        temperature.setValue(config.getTemperature());
        cfgWeight.setValue(config.getCfgWeight());
        speed.setValue(config.getSpeed());
        chunkSize.setValue(config.getChunkSize());
        before.show(config.getBeforeHooks(), service::reveal);
        after.show(config.getAfterHooks(), service::reveal);
        updateKeyState();
    }

    /** Puts what the fields show into the backend that is open. */
    public void commit() {
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
        current.setGpu(gpu.getValue().strip());
        current.setLocal(local.getValue());
        current.setTimeoutSeconds(positive(timeout, BackendConfig.DEFAULT_TIMEOUT_SECONDS));
        current.setRestartEveryPieces(restartEvery.getValue() == null ? 0 : Math.max(0, restartEvery.getValue()));
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
                Notification.warning(error, Notification.DURATION_INFINITE);
            } else if (answer.modelLoaded()) {
                Notification.success(ran + T.localized(THE_SERVER_ANSWERS_THE_MODEL_IS_LOADED).formatted(StringUtils.defaultString(answer.model())));
            } else {
                Notification.success(ran + T.localized(THE_SERVER_ANSWERS_NO_MODEL));
            }
        }));
    }
}
