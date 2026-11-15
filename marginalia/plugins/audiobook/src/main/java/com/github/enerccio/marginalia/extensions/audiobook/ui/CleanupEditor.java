package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.extensions.audiobook.model.AudiobookSettings;
import com.github.enerccio.marginalia.extensions.audiobook.model.CleanupConfig;
import com.github.enerccio.marginalia.extensions.audiobook.model.CleanupPreset;
import com.github.enerccio.marginalia.extensions.audiobook.model.CleanupStep;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.GpuLockSelect;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.SettingsChanges;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;

import java.util.ArrayList;
import java.util.List;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * The cleanup of clips of the user (see {@link CleanupConfig}): the ComfyUI with the TTS Audio Suite, its own GPU lock and the presets, each a list of
 * steps (a separation model and which of its outputs is kept, the voice fixer, the ffmpeg filter). Saved with the settings page.
 */
public class CleanupEditor extends VerticalLayout {

    private final AudiobookService service;
    private CleanupConfig current;
    private final List<CleanupPreset> presets = new ArrayList<>();
    private List<String> models = new ArrayList<>(CleanupConfig.KNOWN_MODELS);

    private final TextField baseUrl = new TextField(T.localized(ADDRESS));
    private final PasswordField apiKey = new PasswordField(T.localized(API_KEY));
    private final Button removeKey = new Button(T.localized(REMOVE_THE_KEY), e -> removeKey());
    private final Checkbox local = new Checkbox(T.localized(THE_SERVER_IS_LOCAL));
    private final IntegerField timeout = new IntegerField(T.localized(TIMEOUT_SECONDS));
    private final GpuLockSelect gpuLock = new GpuLockSelect();
    private final Button test = new Button(T.localized(TEST_CONNECTION), Solid.PLUG.create(), e -> test());
    private final Select<CleanupPreset> preset = new Select<>();
    private final TextField presetName = new TextField(T.localized(NAME));
    private final VerticalLayout steps = new VerticalLayout();
    private boolean binding;

    public CleanupEditor(AudiobookService service) {
        this.service = service;
        setPadding(false);
        setSpacing(true);
        addClassName(AudiobookStyles.SECTION);

        Span help = new Span(T.localized(CLEANUP_OF_CLIPS_HELP));
        help.addClassName(CssStyles.HINT_TEXT);
        baseUrl.setWidthFull();
        apiKey.setWidth("20em");
        apiKey.setRevealButtonVisible(false);
        removeKey.setThemeName("tertiary small");
        local.getElement().setProperty("title", T.localized(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK));
        timeout.setMin(0);
        timeout.setStepButtonsVisible(true);
        gpuLock.setHelperText(T.localized(CLEANUP_GPU_LOCK_HELP));

        preset.setLabel(T.localized(CLEANUP_PRESET));
        preset.setItemLabelGenerator(CleanupPreset::getName);
        preset.setWidth("22em");
        preset.addValueChangeListener(e -> {
            if (!binding) {
                showPreset();
            }
        });
        presetName.setWidth("18em");
        presetName.addValueChangeListener(e -> {
            CleanupPreset shown = preset.getValue();
            if (!binding && shown != null && e.isFromClient()) {
                shown.setName(e.getValue().strip());
                SettingsChanges.changed(this);
            }
        });
        presetName.addBlurListener(e -> refreshPresets(preset.getValue()));
        Button add = new Button(Solid.PLUS.create(), e -> addPreset());
        add.setTooltipText(T.localized(NEW_PRESET));
        Button delete = new Button(Solid.TRASH.create(), e -> deletePreset());
        delete.setTooltipText(T.localized(DELETE_THE_PRESET));
        Button reset = new Button(T.localized(DEFAULT_PRESETS), Solid.ROTATE_LEFT.create(), e -> resetPresets());
        for (Button b : List.of(add, delete, reset)) {
            b.setThemeName("tertiary small");
        }
        Button addStep = new Button(T.localized(ADD_A_STEP), Solid.PLUS.create(), e -> addStep());
        addStep.setThemeName("tertiary small");
        steps.setPadding(false);
        steps.setSpacing(false);

        HorizontalLayout credentials = new HorizontalLayout(apiKey, removeKey, timeout);
        credentials.setWrap(true);
        credentials.setAlignItems(FlexComponent.Alignment.END);
        HorizontalLayout presetBar = new HorizontalLayout(preset, presetName, add, delete, reset);
        presetBar.setWrap(true);
        presetBar.setAlignItems(FlexComponent.Alignment.END);
        add(new H4(T.localized(CLEANUP_OF_CLIPS)), help, baseUrl, gpuLock, credentials, local, test, presetBar, steps, addStep);
    }

    /** Fills the fields with the stored setup (a new one when there is none). */
    public void show(CleanupConfig config) {
        current = config == null ? new CleanupConfig() : config;
        baseUrl.setValue(current.getBaseUrl());
        apiKey.clear();
        local.setValue(current.isLocal());
        timeout.setValue(current.getTimeoutSeconds());
        gpuLock.setLockUuid(current.getGpuLock());
        presets.clear();
        current.getPresets().forEach(p -> presets.add(p.copy()));
        refreshPresets(presets.isEmpty() ? null : presets.getFirst());
        updateKeyState();
    }

    /** The setup as the fields show it; null when no address is given (no cleanup). */
    public CleanupConfig commit() {
        if (current == null) {
            current = new CleanupConfig();
        }
        current.setBaseUrl(baseUrl.getValue().strip());
        if (!apiKey.isEmpty()) {
            current.setProtectedApiKey(service.protect(apiKey.getValue().strip()));
            apiKey.clear();
        }
        current.setLocal(local.getValue());
        current.setTimeoutSeconds(timeout.getValue() == null || timeout.getValue() <= 0 ? CleanupConfig.DEFAULT_TIMEOUT_SECONDS : timeout.getValue());
        if (gpuLock.isVisible()) {
            current.setGpuLock(gpuLock.getLockUuid());
        }
        List<CleanupPreset> kept = new ArrayList<>();
        for (CleanupPreset p : presets) {
            if (!p.getName().isBlank() && !p.getSteps().isEmpty()) {
                kept.add(p.copy());
            }
        }
        current.setPresets(kept);
        updateKeyState();
        return current.isConfigured() ? current : null;
    }

    // --- the presets ---

    private void refreshPresets(CleanupPreset select) {
        java.text.Collator collator = AudiobookSettings.collator();
        presets.sort((a, b) -> collator.compare(a.getName(), b.getName()));
        binding = true;
        try {
            preset.setItems(presets);
            preset.setValue(select);
        } finally {
            binding = false;
        }
        showPreset();
    }

    private void showPreset() {
        CleanupPreset shown = preset.getValue();
        binding = true;
        try {
            presetName.setValue(shown == null ? "" : shown.getName());
        } finally {
            binding = false;
        }
        presetName.setEnabled(shown != null);
        steps.removeAll();
        if (shown == null) {
            return;
        }
        List<CleanupStep> list = shown.getSteps();
        for (int i = 0; i < list.size(); i++) {
            steps.add(stepRow(shown, i));
        }
    }

    private HorizontalLayout stepRow(CleanupPreset owner, int index) {
        CleanupStep step = owner.getSteps().get(index);
        Select<CleanupStep.Type> type = new Select<>();
        type.setItems(CleanupStep.Type.values());
        type.setItemLabelGenerator(t -> switch (t) {
            case SEPARATE -> T.localized(STEP_SEPARATE);
            case VOICE_FIXER -> T.localized(STEP_VOICE_FIXER);
            case FFMPEG -> T.localized(STEP_FFMPEG);
        });
        type.setValue(step.getType());
        type.setWidth("14em");

        ComboBox<String> model = new ComboBox<>();
        List<String> choices = new ArrayList<>(models);
        if (!step.getModel().isBlank() && !choices.contains(step.getModel())) {
            choices.add(step.getModel());
        }
        choices.sort(String.CASE_INSENSITIVE_ORDER);
        model.setItems(choices);
        model.setAllowCustomValue(true);
        model.addCustomValueSetListener(e -> model.setValue(e.getDetail()));
        model.setValue(step.getModel().isBlank() ? null : step.getModel());
        model.setWidth("24em");
        model.setPlaceholder(T.localized(SEPARATION_MODEL));
        model.addValueChangeListener(e -> {
            step.setModel(e.getValue());
            touched(e.isFromClient());
        });

        Select<CleanupStep.Keep> keep = new Select<>();
        keep.setItems(CleanupStep.Keep.values());
        keep.setItemLabelGenerator(k -> k == CleanupStep.Keep.REMAINING ? T.localized(KEEP_THE_REMAINING) : T.localized(KEEP_THE_EXTRACTED));
        keep.setValue(step.getKeep());
        keep.setWidth("14em");
        keep.setTooltipText(T.localized(KEEP_HELP));
        keep.addValueChangeListener(e -> {
            step.setKeep(e.getValue());
            touched(e.isFromClient());
        });

        IntegerField aggressiveness = new IntegerField();
        aggressiveness.setMin(0);
        aggressiveness.setMax(CleanupStep.MAX_AGGRESSIVENESS);
        aggressiveness.setStepButtonsVisible(true);
        aggressiveness.setValue(step.getAggressiveness());
        aggressiveness.setWidth("8em");
        aggressiveness.setTooltipText(T.localized(AGGRESSIVENESS));
        aggressiveness.addValueChangeListener(e -> {
            step.setAggressiveness(e.getValue());
            touched(e.isFromClient());
        });

        Select<Integer> mode = new Select<>();
        mode.setItems(0, 1, 2);
        mode.setItemLabelGenerator(m -> switch (m) {
            case 1 -> T.localized(FIXER_MODE_HIGH_FREQUENCIES);
            case 2 -> T.localized(FIXER_MODE_DEGRADED);
            default -> T.localized(FIXER_MODE_ORIGINAL);
        });
        mode.setValue(step.getMode());
        mode.setWidth("18em");
        mode.addValueChangeListener(e -> {
            step.setMode(e.getValue());
            touched(e.isFromClient());
        });

        Runnable visibility = () -> {
            CleanupStep.Type t = type.getValue();
            model.setVisible(t == CleanupStep.Type.SEPARATE);
            keep.setVisible(t == CleanupStep.Type.SEPARATE);
            aggressiveness.setVisible(t == CleanupStep.Type.SEPARATE);
            mode.setVisible(t == CleanupStep.Type.VOICE_FIXER);
        };
        visibility.run();
        type.addValueChangeListener(e -> {
            step.setType(e.getValue());
            visibility.run();
            touched(e.isFromClient());
        });

        Button up = new Button(Solid.ARROW_UP.create(), e -> move(owner, index, -1));
        up.setEnabled(index > 0);
        Button down = new Button(Solid.ARROW_DOWN.create(), e -> move(owner, index, 1));
        down.setEnabled(index < owner.getSteps().size() - 1);
        Button remove = new Button(Solid.XMARK.create(), e -> {
            owner.getSteps().remove(index);
            changed();
        });
        remove.setTooltipText(T.localized(REMOVE_THE_STEP));
        for (Button b : List.of(up, down, remove)) {
            b.setThemeName("tertiary small");
        }
        Span number = new Span((index + 1) + ".");
        HorizontalLayout row = new HorizontalLayout(number, type, model, keep, aggressiveness, mode, up, down, remove);
        row.setWrap(true);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        return row;
    }

    private void move(CleanupPreset owner, int index, int by) {
        List<CleanupStep> list = owner.getSteps();
        int to = index + by;
        if (to < 0 || to >= list.size()) {
            return;
        }
        list.add(to, list.remove(index));
        changed();
    }

    private void addStep() {
        CleanupPreset shown = preset.getValue();
        if (shown == null) {
            return;
        }
        shown.getSteps().add(CleanupStep.separate(CleanupConfig.MODEL_VOCALS, CleanupStep.Keep.EXTRACTED));
        changed();
    }

    private void addPreset() {
        CleanupPreset created = new CleanupPreset(T.localized(NEW_PRESET) + " " + (presets.size() + 1),
                List.of(CleanupStep.separate(CleanupConfig.MODEL_VOCALS, CleanupStep.Keep.EXTRACTED)));
        presets.add(created);
        refreshPresets(created);
        SettingsChanges.changed(this);
    }

    private void deletePreset() {
        CleanupPreset shown = preset.getValue();
        if (shown == null) {
            return;
        }
        presets.remove(shown);
        refreshPresets(presets.isEmpty() ? null : presets.getFirst());
        SettingsChanges.changed(this);
    }

    private void resetPresets() {
        presets.clear();
        CleanupConfig.defaultPresets().forEach(presets::add);
        refreshPresets(presets.getFirst());
        SettingsChanges.changed(this);
    }

    /** A field of a step row (made after the page started to track its fields) was changed by the user. */
    private void touched(boolean byUser) {
        if (byUser) {
            SettingsChanges.changed(this);
        }
    }

    private void changed() {
        showPreset();
        SettingsChanges.changed(this);
    }

    // --- the server ---

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
        CleanupConfig config = commit();
        if (config == null) {
            Notification.warning(T.localized(NO_CLEANUP));
            return;
        }
        UI ui = UI.getCurrent();
        test.setEnabled(false);
        service.testCleanup(config, (found, error) -> ui.access(() -> {
            test.setEnabled(true);
            if (error == null) {
                if (found != null && !found.isEmpty()) {
                    models = new ArrayList<>(found);
                    showPreset();
                }
                Notification.success(T.localized(THE_CLEANUP_WORKS).formatted(found == null ? 0 : found.size()));
            } else {
                Notification.error(T.localized(CONNECTION_FAILED).formatted(error));
            }
            UIPushGuard.push(ui);
        }));
    }
}
