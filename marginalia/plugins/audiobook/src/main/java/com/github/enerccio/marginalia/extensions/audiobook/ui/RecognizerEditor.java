package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.extensions.audiobook.model.RecognizerConfig;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.GpuLockSelect;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.SettingsChanges;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * The speech recognition of the user (see {@link RecognizerConfig}): ComfyUI with the Granite ASR of the TTS Audio Suite, or an OpenAI-compatible
 * transcription server. It checks the pieces of a batch and recognises the text of a voice clip. Saved with the settings page.
 */
public class RecognizerEditor extends VerticalLayout {

    private final AudiobookService service;
    private RecognizerConfig current;

    private final Select<RecognizerConfig.Type> type = new Select<>();
    private final TextField baseUrl = new TextField(T.localized(ADDRESS));
    private final PasswordField apiKey = new PasswordField(T.localized(API_KEY));
    private final Button removeKey = new Button(T.localized(REMOVE_THE_KEY), e -> removeKey());
    private final TextField model = new TextField(T.localized(RECOGNITION_MODEL));
    private final Checkbox local = new Checkbox(T.localized(THE_SERVER_IS_LOCAL));
    private final IntegerField timeout = new IntegerField(T.localized(TIMEOUT_SECONDS));
    private final GpuLockSelect gpuLock = new GpuLockSelect();
    private final Button test = new Button(T.localized(TEST_CONNECTION), Solid.PLUG.create(), e -> test());

    public RecognizerEditor(AudiobookService service) {
        this.service = service;
        setPadding(false);
        setSpacing(true);
        addClassName(AudiobookStyles.SECTION);

        Span help = new Span(T.localized(SPEECH_RECOGNITION_HELP));
        help.addClassName(CssStyles.HINT_TEXT);
        type.setLabel(T.localized(SPEECH_RECOGNITION));
        type.setItems(RecognizerConfig.Type.values());
        type.setItemLabelGenerator(t -> t == RecognizerConfig.Type.OPENAI ? T.localized(RECOGNIZER_OPENAI) : T.localized(RECOGNIZER_COMFYUI));
        type.setWidth("22em");
        type.addValueChangeListener(e -> model.setPlaceholder(e.getValue() == RecognizerConfig.Type.OPENAI
                ? RecognizerConfig.DEFAULT_OPENAI_MODEL : RecognizerConfig.DEFAULT_GRANITE_MODEL));
        baseUrl.setWidthFull();
        apiKey.setWidth("20em");
        apiKey.setRevealButtonVisible(false);
        removeKey.setThemeName("tertiary small");
        model.setWidth("20em");
        local.getElement().setProperty("title", T.localized(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK));
        timeout.setMin(0);
        timeout.setStepButtonsVisible(true);
        gpuLock.setHelperText(T.localized(GPU_LOCK_HELP));

        HorizontalLayout credentials = new HorizontalLayout(apiKey, removeKey);
        credentials.setWrap(true);
        credentials.setAlignItems(FlexComponent.Alignment.END);
        HorizontalLayout options = new HorizontalLayout(model, timeout);
        options.setWrap(true);
        add(new H4(T.localized(SPEECH_RECOGNITION)), help, type, baseUrl, gpuLock, credentials, options, local, test);
    }

    /** Fills the fields with the stored setup (a new one when there is none). */
    public void show(RecognizerConfig config) {
        current = config == null ? new RecognizerConfig() : config;
        type.setValue(current.getType());
        baseUrl.setValue(current.getBaseUrl());
        apiKey.clear();
        model.setValue(StringUtils.defaultString(current.getStoredModel()));
        local.setValue(current.isLocal());
        timeout.setValue(current.getTimeoutSeconds());
        gpuLock.setLockUuid(current.getGpuLock());
        updateKeyState();
    }

    /** The setup as the fields show it; null when no address is given (no speech recognition). */
    public RecognizerConfig commit() {
        if (current == null) {
            current = new RecognizerConfig();
        }
        current.setType(type.getValue() == null ? RecognizerConfig.Type.COMFYUI_GRANITE : type.getValue());
        current.setBaseUrl(baseUrl.getValue().strip());
        if (!apiKey.isEmpty()) {
            current.setProtectedApiKey(service.protect(apiKey.getValue().strip()));
            apiKey.clear();
        }
        current.setModel(StringUtils.trimToNull(model.getValue()));
        current.setLocal(local.getValue());
        current.setTimeoutSeconds(timeout.getValue() == null || timeout.getValue() <= 0 ? RecognizerConfig.DEFAULT_TIMEOUT_SECONDS : timeout.getValue());
        if (gpuLock.isVisible()) {
            current.setGpuLock(gpuLock.getLockUuid());
        }
        updateKeyState();
        return current.isConfigured() ? current : null;
    }

    private void removeKey() {
        if (current != null) {
            current.setProtectedApiKey(null);
            apiKey.clear();
            updateKeyState();
            SettingsChanges.changed(this);
        }
    }

    private void updateKeyState() {
        boolean stored = current != null && current.getProtectedApiKey() != null;
        apiKey.setPlaceholder(stored ? T.localized(STORED_TYPE_TO_REPLACE) : "");
        removeKey.setVisible(stored);
    }

    private void test() {
        RecognizerConfig config = commit();
        if (config == null) {
            Notification.warning(T.localized(NO_SPEECH_RECOGNITION));
            return;
        }
        UI ui = UI.getCurrent();
        test.setEnabled(false);
        service.testRecognizer(config, error -> ui.access(() -> {
            test.setEnabled(true);
            if (error == null) {
                Notification.success(T.localized(THE_SPEECH_RECOGNITION_WORKS));
            } else {
                Notification.error(T.localized(CONNECTION_FAILED).formatted(error));
            }
            UIPushGuard.push(ui);
        }));
    }
}
