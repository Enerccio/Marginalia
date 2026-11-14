package com.github.enerccio.marginalia.extensions.imagegenerator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.ui.HookListEditor;
import com.github.enerccio.marginalia.extensions.imagegenerator.ImageGeneratorLog;
import com.github.enerccio.marginalia.extensions.imagegenerator.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.imagegenerator.model.BackendType;
import com.github.enerccio.marginalia.extensions.imagegenerator.model.ImageSettings;
import com.github.enerccio.marginalia.extensions.imagegenerator.model.PromptForm;
import com.github.enerccio.marginalia.extensions.imagegenerator.service.ImageBackend;
import com.github.enerccio.marginalia.extensions.imagegenerator.service.ImageGeneratorService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.ui.widgets.SettingsChanges;
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
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

import static com.github.enerccio.marginalia.extensions.imagegenerator.ImageGeneratorLocalization.*;

/**
 * <i>Image Generator Settings</i>, a panel of <i>Settings → Extension Settings</i>: the style and the negative prompt of the user
 * writer, and the backends the images are made by. The backends are edited on a copy that {@link #save()} returns; the page
 * stores it with the other settings of the user.
 */
@Configurable(preConstruction = true)
public class ImageSettingsForm extends VerticalLayout {

    @Autowired
    private Localization loc;

    private final ImageGeneratorService service;

    private ImageSettings settings = new ImageSettings();
    private List<BackendConfig> backends = new ArrayList<>();
    private BackendConfig current;
    private boolean binding;

    private final TextArea style = new TextArea(T.localized(STYLE));
    /** The user level of the negative prompt (a book or a folder can change it). */
    private final TextArea defaultNegative = new TextArea(T.localized(NEGATIVE_PROMPT));

    private final Select<BackendConfig> picker = new Select<>();
    private final Button addButton = new Button(Solid.PLUS.create(), e -> addBackend());
    private final Button deleteButton = new Button(Solid.TRASH.create(), e -> deleteBackend());

    private final VerticalLayout editor = new VerticalLayout();
    private final TextField name = new TextField(T.localized(NAME));
    private final Select<BackendType> type = new Select<>();
    private final TextField baseUrl = new TextField(T.localized(ADDRESS));
    private final PasswordField apiKey = new PasswordField(T.localized(API_KEY));
    private final Button removeKey = new Button(T.localized(REMOVE_THE_KEY), e -> removeKey());
    private final TextField model = new TextField(T.localized(MODEL));
    private final TextArea workflow = new TextArea(T.localized(WORKFLOW));
    private final Select<PromptForm> promptForm = new Select<>();
    private final TextArea instruction = new TextArea(T.localized(INSTRUCTION_FOR_THE_PROMPT_WRITER));
    private final TextField gpu = new TextField(T.localized(GPU_NAME));
    private final Checkbox local = new Checkbox(T.localized(THE_SERVER_IS_LOCAL));
    private final IntegerField width = new IntegerField(T.localized(WIDTH));
    private final IntegerField height = new IntegerField(T.localized(HEIGHT));
    private final IntegerField timeout = new IntegerField(T.localized(TIMEOUT_SECONDS));
    private final IntegerField idle = new IntegerField(T.localized(RELEASE_AFTER_IDLE_SECONDS));
    private final HookListEditor before;
    private final HookListEditor after;
    private final Button test = new Button(T.localized(TEST_CONNECTION), Solid.PLUG.create(), e -> testConnection());

    public ImageSettingsForm(ImageGeneratorService service) {
        this.service = service;
        // built here, not with the fields: the localization is injected before the body runs
        this.before = new HookListEditor(hookLabels(), ImageStyles.SECTION, loc);
        this.after = new HookListEditor(hookLabels(), ImageStyles.SECTION, loc);

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        style.setWidthFull();
        ResizableTextArea.install(loc, style, "80px");
        style.setHelperText(T.localized(PUT_IN_FRONT_OF_EVERY_PROMPT));
        defaultNegative.setWidthFull();
        defaultNegative.setHelperText(T.localized(NEGATIVE_PROMPT_HELP));
        ResizableTextArea.install(loc, defaultNegative, "80px");
        Span inherited = new Span(T.localized(A_BOOK_OR_A_FOLDER_CAN_CHANGE_THESE));
        inherited.addClassName(CssStyles.HINT_TEXT);
        promptForm.setLabel(T.localized(PROMPT_FORM));
        promptForm.setItems(PromptForm.values());
        promptForm.setItemLabelGenerator(f -> switch (f) {
            case TAGS -> T.localized(TAGS_FORM);
            case SENTENCES -> T.localized(SENTENCES_FORM);
            case CUSTOM -> T.localized(FREE_TEXT);
        });
        promptForm.setHelperText(T.localized(PROMPT_FORM_HELP));
        promptForm.setWidth("24em");
        promptForm.addValueChangeListener(e -> {
            // free text starts from the instruction that was in use, to be changed
            if (!binding && e.getValue() == PromptForm.CUSTOM && instruction.isEmpty() && e.getOldValue() != null) {
                instruction.setValue(e.getOldValue().instruction());
            }
            instruction.setVisible(e.getValue() == PromptForm.CUSTOM);
        });
        instruction.setWidthFull();
        ResizableTextArea.install(loc, instruction, "160px");
        instruction.setVisible(false);

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

        type.setLabel(T.localized(TYPE));
        type.setItems(BackendType.values());
        type.setItemLabelGenerator(t -> t == BackendType.COMFYUI ? T.localized(COMFYUI) : T.localized(OPENAI_IMAGES_API));
        type.addValueChangeListener(e -> {
            if (!binding) {
                commit();
                updateVisibility();
            }
        });
        name.setWidth("14em");
        baseUrl.setWidthFull();
        baseUrl.setHelperText(T.localized(BASE_ADDRESS_OF_THE_SERVER));
        apiKey.setWidth("20em");
        apiKey.setRevealButtonVisible(false);
        removeKey.setThemeName("tertiary small");
        model.setWidth("16em");
        workflow.setWidthFull();
        // a workflow is hundreds of lines of JSON: it scrolls inside the field unless expanded
        ResizableTextArea.install(loc, workflow, "240px");
        workflow.setHelperText(T.localized(API_FORMAT_WORKFLOW));
        local.getElement().setProperty("title", T.localized(ALLOWS_ADDRESSES_OF_THE_LOCAL_NETWORK));
        for (IntegerField f : List.of(width, height, timeout, idle)) {
            f.setMin(0);
            f.setStepButtonsVisible(true);
        }
        idle.setHelperText(T.localized(THE_REQUEST_AFTER_THE_IMAGES_RUNS));

        HorizontalLayout identity = new HorizontalLayout(name, type);
        identity.setWrap(true);
        HorizontalLayout credentials = new HorizontalLayout(apiKey, removeKey, model);
        credentials.setWrap(true);
        credentials.setAlignItems(FlexComponent.Alignment.END);
        HorizontalLayout numbers = new HorizontalLayout(width, height, timeout, idle);
        numbers.setWrap(true);

        Details beforeDetails = new Details(T.localized(BEFORE_THE_FIRST_IMAGE), before);
        Details afterDetails = new Details(T.localized(AFTER_THE_LAST_IMAGE), after);
        Span hookHint = new Span(T.localized(A_REQUEST_MADE_AROUND_THE_IMAGES));
        hookHint.addClassName(CssStyles.HINT_TEXT);

        editor.setPadding(false);
        editor.setSpacing(true);
        gpu.setWidth("14em");
        gpu.setHelperText(T.localized(GPU_NAME_HELP));
        editor.add(identity, baseUrl, gpu, credentials, workflow, promptForm, instruction, local, numbers, hookHint, beforeDetails,
                afterDetails, test);
        editor.addClassName(ImageStyles.SECTION);

        add(inherited, style, defaultNegative, bar, editor);
        refresh();
    }

    /** Shows the stored settings. */
    public void refresh() {
        settings = service.getSettings();
        backends = new ArrayList<>(settings.getBackends());
        style.setValue(settings.getStylePrefix());
        defaultNegative.setValue(settings.getNegativePrompt().isBlank() ? legacyNegative(settings) : settings.getNegativePrompt());
        binding = true;
        try {
            picker.setItems(backends);
            current = settings.find(settings.getSelectedBackendId());
            if (current == null && !backends.isEmpty()) {
                current = backends.getFirst();
            }
            picker.setValue(current);
        } finally {
            binding = false;
        }
        show(current);
    }

    /**
     * The negative prompt the backends had before it became a setting of the user and the book: the one of the selected backend (else the
     * first that has one) is taken over the first time the panel is shown, and stored with the next save.
     */
    private static String legacyNegative(ImageSettings settings) {
        BackendConfig selected = settings.find(settings.getSelectedBackendId());
        if (selected != null && !selected.getNegativePrompt().isBlank()) {
            return selected.getNegativePrompt();
        }
        return settings.getBackends().stream().map(BackendConfig::getNegativePrompt).filter(n -> !n.isBlank()).findFirst().orElse("");
    }

    /** The stored settings with what the form shows. */
    public ImageSettings save() {
        commit();
        ImageSettings stored = service.getSettings();
        stored.setStylePrefix(style.getValue());
        stored.setNegativePrompt(defaultNegative.getValue());
        stored.setBackends(new ArrayList<>(backends));
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

    private void removeKey() {
        if (current != null) {
            current.setProtectedApiKey(null);
            apiKey.clear();
            updateKeyState();
            // the field was empty already: no value change tells the settings page
            SettingsChanges.changed(this);
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
            type.setValue(config.getType());
            baseUrl.setValue(config.getBaseUrl());
            apiKey.clear();
            model.setValue(StringUtils.defaultString(config.getModel()));
            // a backend from before the choice: the instruction the user had then becomes its free text
            String legacy = settings.getStoredPromptInstruction();
            if (config.hasPromptForm() || legacy == null) {
                promptForm.setValue(config.getPromptForm());
                instruction.setValue(config.getPromptInstruction());
            } else {
                promptForm.setValue(PromptForm.CUSTOM);
                instruction.setValue(legacy);
            }
            workflow.setValue(StringUtils.defaultString(config.getWorkflow()));
            gpu.setValue(config.getGpu());
            local.setValue(config.isLocal());
            width.setValue(config.getWidth());
            height.setValue(config.getHeight());
            timeout.setValue(config.getTimeoutSeconds());
            idle.setValue(config.getIdleSeconds());
            before.show(config.getBeforeHooks(), service::reveal);
            after.show(config.getAfterHooks(), service::reveal);
        } finally {
            binding = false;
        }
        updateKeyState();
        updateVisibility();
    }

    /** Puts what the fields show into the backend that is open. */
    private void commit() {
        if (current == null) {
            return;
        }
        current.setName(name.getValue().strip());
        current.setType(type.getValue() == null ? BackendType.COMFYUI : type.getValue());
        current.setBaseUrl(baseUrl.getValue().strip());
        if (!apiKey.isEmpty()) {
            current.setProtectedApiKey(service.protect(apiKey.getValue().strip()));
            apiKey.clear();
        }
        current.setModel(StringUtils.defaultIfBlank(model.getValue().strip(), null));
        current.setPromptForm(promptForm.getValue() == null ? PromptForm.TAGS : promptForm.getValue());
        current.setPromptInstruction(StringUtils.defaultIfBlank(instruction.getValue(), null));
        current.setWorkflow(StringUtils.defaultIfBlank(workflow.getValue(), null));
        current.setGpu(gpu.getValue().strip());
        current.setLocal(local.getValue());
        current.setWidth(valueOr(width, BackendConfig.DEFAULT_WIDTH));
        current.setHeight(valueOr(height, BackendConfig.DEFAULT_HEIGHT));
        current.setTimeoutSeconds(valueOr(timeout, BackendConfig.DEFAULT_TIMEOUT_SECONDS));
        current.setIdleSeconds(idle.getValue() == null ? 0 : Math.max(0, idle.getValue()));
        current.setBeforeHooks(before.read(service::protect));
        current.setAfterHooks(after.read(service::protect));
        updateKeyState();
    }

    private static HookListEditor.Labels hookLabels() {
        return new HookListEditor.Labels(T.localized(ADDRESS), T.localized(METHOD), T.localized(HEADERS_NAME_VALUE_ONE_PER_LINE), T.localized(BODY),
                T.localized(TIMEOUT_SECONDS), T.localized(WAIT_AFTERWARDS_SECONDS), T.localized(REQUEST), T.localized(ADD_A_REQUEST),
                T.localized(REMOVE_THE_REQUEST));
    }

    private static int valueOr(IntegerField field, int fallback) {
        Integer value = field.getValue();
        return value == null || value <= 0 ? fallback : value;
    }

    private void updateKeyState() {
        boolean stored = current != null && current.getProtectedApiKey() != null;
        apiKey.setPlaceholder(stored ? T.localized(STORED_TYPE_TO_REPLACE) : "");
        removeKey.setVisible(stored);
    }

    private void updateVisibility() {
        boolean comfy = type.getValue() != BackendType.OPENAI;
        workflow.setVisible(comfy);
        model.setVisible(!comfy);
    }

    private void testConnection() {
        commit();
        BackendConfig config = current;
        if (config == null) {
            return;
        }
        UI ui = UI.getCurrent();
        ImageBackend backend = service.createBackend(config);
        test.setEnabled(false);
        Thread.ofVirtual().name("image-generator-test").start(() -> {
            String error = null;
            try {
                backend.test();
            } catch (BackendException e) {
                error = e.getMessage();
            } catch (RuntimeException e) {
                ImageGeneratorLog.get().warn("Testing the connection failed: {}", e.getMessage(), e);
                error = String.valueOf(e.getMessage());
            }
            String message = error;
            ui.access(() -> {
                test.setEnabled(true);
                if (message == null) {
                    Notification.success(T.localized(CONNECTED));
                } else {
                    Notification.error(T.localized(CONNECTION_FAILED).formatted(message));
                }
            });
        });
    }
}
