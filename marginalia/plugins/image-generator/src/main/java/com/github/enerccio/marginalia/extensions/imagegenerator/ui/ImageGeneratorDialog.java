package com.github.enerccio.marginalia.extensions.imagegenerator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.imagegenerator.ImageGeneratorExtension;
import com.github.enerccio.marginalia.extensions.imagegenerator.ImageGeneratorLog;
import com.github.enerccio.marginalia.extensions.imagegenerator.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.imagegenerator.model.ImageSettings;
import com.github.enerccio.marginalia.extensions.imagegenerator.service.*;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.*;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.context.request.RequestAttributes;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.imagegenerator.ImageGeneratorLocalization.*;

/**
 * Makes an image for one part of the story: the model writes the prompt from the part (the user can change it), the chosen backend
 * makes the picture, the user rerolls until it fits and accepts it - it becomes an image of the part. The GPU lock of the backend is held for
 * each image only (see {@link ImageJob}): a window that is closed, folded or left open holds no GPU.
 */
@Configurable(preConstruction = true)
public class ImageGeneratorDialog extends Dialog {

    @Autowired
    private Localization loc;

    private final ImageGeneratorService service;
    private final Manuscript manuscript;
    private final Consumer<ChatMessage> onChanged;
    private ChatMessage message;
    private ImageSettings settings;

    private final ModelProtocolPicker modelPicker = new ModelProtocolPicker();
    private final Select<BackendConfig> backendSelect = new Select<>();
    private final Button writePrompt = new Button(T.localized(WRITE_THE_PROMPT), Solid.PEN_FANCY.create(), e -> writePrompt());
    private final Checkbox sendLore = new Checkbox(T.localized(SEND_LOREBOOK_ENTRIES));
    private final TextArea details = new TextArea(T.localized(DETAILS));
    private final TextArea prompt = new TextArea(T.localized(PROMPT));
    private final TextArea negative = new TextArea(T.localized(NEGATIVE_PROMPT));
    private final TextArea style = new TextArea(T.localized(STYLE));
    /** The negative prompt of the book (book, folders, user). */
    private String bookNegative = "";
    /** What is stored in the part, so an unchanged window is not saved again. */
    private PartImageValues partValues;
    /**
     * The request context of the window, taken when it opens: the callbacks of the worker threads (the model, the backend, the list of
     * LoRAs) run without one, and the services need it for the session's user.
     */
    private final RequestAttributes requestAttributes = ThreadCopyRequestAttributes.create();
    private final IntegerField width = new IntegerField(T.localized(WIDTH));
    private final IntegerField height = new IntegerField(T.localized(HEIGHT));
    private final ComboBox<String> loraBox = new ComboBox<>(T.localized(LORA));
    private final NumberField loraStrength = new NumberField(T.localized(LORA_STRENGTH));
    private List<String> loraItems = new ArrayList<>();
    private final Button generate = new Button(T.localized(GENERATE), Solid.IMAGE.create(), e -> generate());
    private final Button accept = new Button(T.localized(ACCEPT), Solid.CHECK.create(), e -> accept());
    private final Button discard = new Button(T.localized(DISCARD), Solid.TRASH.create(), e -> discard());
    private final Span status = new Span();
    private final Div preview = new Div();

    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "image-generator-dialog");
        t.setDaemon(true);
        return t;
    });

    private CancellationToken promptToken;
    // not modal: closing it while the model or the backend works folds it into the corner, the work goes on
    private final FloatingWindow floating;
    private AtomicBoolean imageCancel;
    private boolean writing;
    private boolean making;
    private byte[] image;
    private String imageExtension;
    private int imageCounter;

    public ImageGeneratorDialog(ChatMessage message, Manuscript manuscript, ImageGeneratorService service,
                                Consumer<ChatMessage> onChanged) {
        this.message = message;
        this.manuscript = manuscript;
        this.service = service;
        this.onChanged = onChanged;

        setHeaderTitle(T.localized(GENERATE_IMAGE));
        setWidth("min(900px, 95vw)");
        setMaxHeight("92vh");
        // Esc would fold a running window by accident
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        // closed for good (also from the corner, or with the book), it stops what runs and lets the backend go
        floating = new FloatingWindow(this, ImageGeneratorExtension.NOTIFY_IMAGE,
                () -> T.localized(GENERATE_IMAGE) + " \u00b7 " + manuscript.getName(), this::dispose).book(manuscript);

        settings = service.getSettings();

        modelPicker.setPlaceholder(T.localized(MODEL_FOR_THE_PROMPT));
        initModelPicker();

        backendSelect.setLabel(T.localized(BACKEND));
        backendSelect.setItems(BackendConfig.byName(settings.getBackends()));
        backendSelect.setItemLabelGenerator(b -> StringUtils.defaultIfBlank(b.getName(), T.localized(NEW_BACKEND)));
        backendSelect.setWidth("12em");
        BackendConfig selected = settings.find(settings.getSelectedBackendId());
        if (selected == null && !settings.getBackends().isEmpty()) {
            selected = settings.getBackends().getFirst();
        }
        backendSelect.setValue(selected);
        backendSelect.addValueChangeListener(e -> backendChanged(e.getValue()));

        HorizontalLayout top = new HorizontalLayout(modelPicker, writePrompt);
        top.setWidthFull();
        top.setWrap(true);
        top.setAlignItems(FlexComponent.Alignment.END);

        sendLore.setValue(settings.isSendLore());
        sendLore.getElement().setProperty("title", T.localized(THE_ENTRIES_THE_PART_ACTIVATES));
        sendLore.addValueChangeListener(e -> rememberSendLore(e.getValue()));
        details.setWidthFull();
        // with the check box under it as tall as the style and the negative prompt next to it; the explanation is the placeholder
        // (a helper line would make the window taller)
        ResizableTextArea.install(loc, details, "116px");
        details.setPlaceholder(T.localized(WHAT_THE_PICTURE_MUST_SHOW));
        details.getElement().setProperty("title", T.localized(WHAT_THE_PICTURE_MUST_SHOW));
        style.setWidthFull();
        ResizableTextArea.install(loc, style, "56px");
        // a helper line would make the window taller
        style.getElement().setProperty("title", T.localized(STYLE_FOR_THIS_WINDOW));

        prompt.setWidthFull();
        ResizableTextArea.install(loc, prompt, "160px");

        negative.setWidthFull();
        ResizableTextArea.install(loc, negative, "56px");
        width.setMin(64);
        width.setStepButtonsVisible(true);
        width.setWidth("8em");
        height.setMin(64);
        height.setStepButtonsVisible(true);
        height.setWidth("8em");
        initLora();
        // what makes the image, in one row that wraps on a narrow screen
        HorizontalLayout imageRow = new HorizontalLayout(backendSelect, loraBox, loraStrength, width, height);
        imageRow.setWidthFull();
        imageRow.setWrap(true);
        imageRow.setAlignItems(FlexComponent.Alignment.END);
        // the details on one side, the two texts of the book on the other (one under the other on a narrow screen)
        VerticalLayout detailsColumn = compact(new VerticalLayout(details, sendLore));
        VerticalLayout bookTexts = compact(new VerticalLayout(style, negative));
        Div split = new Div(detailsColumn, bookTexts);
        split.addClassName(ImageStyles.SPLIT);
        style.setValue(service.resolveStyle(manuscript));
        bookNegative = StringUtils.defaultString(service.resolveNegative(manuscript)).strip();

        status.addClassName(ImageStyles.STATUS);
        preview.addClassName(ImageStyles.PREVIEW);
        preview.setWidthFull();
        accept.setThemeName("primary");
        accept.setVisible(false);
        discard.setVisible(false);
        generate.setThemeName("primary");

        // the frame of the image takes room only when it shows something
        preview.setVisible(false);
        if (settings.getBackends().isEmpty()) {
            Span hint = new Span(T.localized(NO_BACKEND_YET));
            hint.addClassName(CssStyles.HINT_TEXT);
            preview.add(hint);
            preview.setVisible(true);
        }

        add(compact(new VerticalLayout(top, split, prompt, imageRow, status, preview)));

        getFooter().add(new Button(T.localized(CLOSE), e -> close()), discard, accept, generate);
        DialogShortcuts.submitWith(writePrompt, details);
        DialogShortcuts.submitWith(generate, prompt, negative, style, width, height, loraStrength);

        applyBackendDefaults(selected);
        restorePartValues();
        updateButtons();
        addDetachListener(this::detached);

        // the prompt is written when asked: the details and the lorebook choice are filled in first
        DialogFocus.onOpen(this, details);
    }

    // --- model and backend ---

    private void initModelPicker() {
        try {
            modelPicker.setSelection(service.resolveAI(settings, manuscript), service.resolveProtocol(settings, manuscript));
            modelPicker.addSelectionListener(() -> {
                try {
                    ImageSettings changed = service.getSettings();
                    changed.setSelectedAiId(modelPicker.getAi() == null ? null : modelPicker.getAi().getId());
                    changed.setSelectedProtocolId(modelPicker.getProtocol() == null ? null : modelPicker.getProtocol().getId());
                    service.saveSettings(changed);
                    settings = changed;
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            });
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void backendChanged(BackendConfig backend) {
        applyBackendDefaults(backend);
        if (backend != null) {
            try {
                ImageSettings changed = service.getSettings();
                changed.setSelectedBackendId(backend.getId());
                service.saveSettings(changed);
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }
        updateButtons();
    }

    private void applyBackendDefaults(BackendConfig backend) {
        setLoraVisible(backend != null && backend.usesLora());
        if (backend == null) {
            return;
        }
        width.setValue(backend.getWidth());
        height.setValue(backend.getHeight());
        negative.setValue(bookNegative);
        if (backend.usesLora()) {
            // what the server listed last: it may not run before the first image (its before hooks start it)
            setLoraItems(backend.getKnownLoras(), backend.getLora());
            loraStrength.setValue(backend.getLoraStrength());
            loadLoras(backend);
        }
    }

    /** {@code ui.access} in the request context of the window, for a callback of another thread that uses the services. */
    private void inRequest(UI ui, Command command) {
        ui.access(() -> {
            try (InRequestScope scope = new InRequestScope(requestAttributes)) {
                command.execute();
            }
        });
    }

    // --- what the window used for the part ---

    /** The window opens with what was used for the part last time; a part without it keeps the book's and the backend's values. */
    private void restorePartValues() {
        partValues = service.partValues(message);
        if (partValues == null) {
            return;
        }
        if (partValues.details() != null) {
            details.setValue(partValues.details());
        }
        if (partValues.style() != null) {
            style.setValue(partValues.style());
        }
        if (partValues.negative() != null) {
            negative.setValue(partValues.negative());
        }
        if (partValues.prompt() != null) {
            prompt.setValue(partValues.prompt());
        }
        if (partValues.width() != null && partValues.width() > 0) {
            width.setValue(partValues.width());
        }
        if (partValues.height() != null && partValues.height() > 0) {
            height.setValue(partValues.height());
        }
    }

    /** Keeps what the window shows with the part (after the prompt was written and when an image is made), if it changed. */
    private void rememberPartValues() {
        if (message == null) {
            return;
        }
        PartImageValues values = new PartImageValues(details.getValue(), style.getValue(), negative.getValue(), prompt.getValue(),
                width.getValue(), height.getValue());
        if (values.equals(partValues)) {
            return;
        }
        try {
            message = service.rememberPartValues(message, values);
            partValues = values;
            // the card keeps its own copy of the part: a stale one would drop the values with its next save
            onChanged.accept(message);
        } catch (Exception e) {
            // an agent working on the book, a part deleted meanwhile: the image is still made, only not remembered
            ImageGeneratorLog.get().warn("Remembering the values of the window failed: {}", e.getMessage());
        }
    }

    // --- the LoRA ---

    /** A layout without padding and with the small gap between its rows. */
    private static VerticalLayout compact(VerticalLayout layout) {
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getThemeList().add("spacing-xs");
        return layout;
    }

    private void setLoraVisible(boolean visible) {
        loraBox.setVisible(visible);
        loraStrength.setVisible(visible);
    }

    private void initLora() {
        loraBox.setAllowCustomValue(true);
        loraBox.setClearButtonVisible(true);
        loraBox.setPlaceholder(T.localized(NO_LORA));
        loraBox.setWidth("18em");
        // a file the list misses (a subfolder, a server that does not list)
        loraBox.addCustomValueSetListener(e -> {
            String typed = e.getDetail().strip();
            if (!typed.isEmpty()) {
                setLoraItems(loraItems, typed);
            }
        });
        loraStrength.setStep(0.05);
        loraStrength.setMin(-5);
        loraStrength.setMax(5);
        loraStrength.setStepButtonsVisible(true);
        loraStrength.setWidth("8em");
        setLoraVisible(false);
    }

    private void setLoraItems(List<String> names, String selected) {
        List<String> items = new ArrayList<>(names);
        if (selected != null && !items.contains(selected)) {
            items.add(selected);
        }
        items.sort(String.CASE_INSENSITIVE_ORDER);
        loraItems = items;
        loraBox.setItems(items);
        loraBox.setValue(selected);
    }

    /** Asks the server for its LoRAs; the stored choice stays when it can't answer. */
    private void loadLoras(BackendConfig backend) {
        UI ui = UI.getCurrent();
        if (ui == null) {
            return;
        }
        ImageBackend server = service.createBackend(backend);
        Thread.ofVirtual().name("image-generator-loras").start(() -> {
            List<String> names;
            String error = null;
            try {
                names = server.loras();
            } catch (BackendException e) {
                names = null;
                error = e.getMessage();
            } catch (RuntimeException e) {
                ImageGeneratorLog.get().warn("Reading the LoRAs failed: {}", e.getMessage(), e);
                names = null;
                error = String.valueOf(e.getMessage());
            }
            List<String> found = names;
            String message = error;
            inRequest(ui, () -> {
                if (backendSelect.getValue() != backend) {
                    return;
                }
                if (found != null) {
                    setLoraItems(found, loraBox.getValue());
                    rememberKnownLoras(backend, found);
                } else if (backend.getKnownLoras().isEmpty()) {
                    // with a list from before nothing is missing: the server is probably just not started yet
                    Notification.warning(T.localized(THE_LORAS_COULD_NOT_BE_READ).formatted(message));
                }
                UIPushGuard.push(ui);
            });
        });
    }

    /** Keeps the list the server gave with the backend, for a window that opens while the server is not running. */
    private void rememberKnownLoras(BackendConfig backend, List<String> names) {
        if (backend.getKnownLoras().equals(names)) {
            return;
        }
        backend.setKnownLoras(names);
        try {
            ImageSettings changed = service.getSettings();
            BackendConfig stored = changed.find(backend.getId());
            if (stored != null) {
                stored.setKnownLoras(names);
                service.saveSettings(changed);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /** Keeps the LoRA of the window with the backend, for the next window. */
    private void rememberLora(BackendConfig backend, String lora, double strength) {
        if (StringUtils.equals(backend.getLora(), lora) && backend.getLoraStrength() == strength) {
            return;
        }
        backend.setLora(lora);
        backend.setLoraStrength(strength);
        try {
            ImageSettings changed = service.getSettings();
            BackendConfig stored = changed.find(backend.getId());
            if (stored != null) {
                stored.setLora(lora);
                stored.setLoraStrength(strength);
                service.saveSettings(changed);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    // --- the prompt ---

    private void writePrompt() {
        if (writing) {
            abortPrompt();
            return;
        }
        AI ai = modelPicker.getAi();
        Protocol protocol = modelPicker.getProtocol();
        if (ai == null || protocol == null || message == null) {
            return;
        }
        UI ui = UI.getCurrent();
        writing = true;
        prompt.clear();
        status.setText(T.localized(WAITING_FOR_THE_MODEL));
        updateButtons();
        try {
            ImageGeneratorService.PromptRequest request = service.buildPromptRequest(settings, manuscript, message, ai, protocol,
                    sendLore.getValue(), details.getValue(), style.getValue(), service.instructionFor(backendSelect.getValue()));
            if (request.skippedLore() > 0) {
                Notification.warning(T.localized(LORE_ENTRIES_DID_NOT_FIT).formatted(request.skippedLore()));
            }
            if (sendLore.getValue()) {
                status.setText(T.localized(WAITING_FOR_THE_MODEL) + " " + T.localized(LORE_ENTRIES_SENT).formatted(request.loreEntries()));
            }
            Runs.Binding binding = floating.bind();
            try (binding) {
            promptToken = service.streamPrompt(ai, protocol, request.payload(), new ImageGeneratorService.PromptListener() {
                @Override
                public void onProgress(String text) {
                    inRequest(ui, () -> {
                        if (writing) {
                            prompt.setValue(text.strip());
                            UIPushGuard.push(ui);
                        }
                    });
                }

                @Override
                public void onDone(boolean cancelled, Throwable error) {
                    inRequest(ui, () -> {
                        writing = false;
                        status.setText("");
                        if (error != null) {
                            ImageGeneratorLog.get().warn("Writing the prompt failed: {}", error.getMessage(), error);
                            Notification.error(T.localized(THE_MODEL_FAILED).formatted(error.getMessage()));
                        }
                        if (error != null) {
                            new DesktopNotifier(ui).failed(ImageGeneratorExtension.NOTIFY_PROMPT, false, manuscript.getName());
                        } else if (!cancelled) {
                            new DesktopNotifier(ui).finished(ImageGeneratorExtension.NOTIFY_PROMPT, false, manuscript.getName());
                            rememberPartValues();
                        }
                        updateButtons();
                        UIPushGuard.push(ui);
                    });
                }
            });
            }
        } catch (Exception e) {
            writing = false;
            status.setText("");
            ImageGeneratorLog.get().warn("Writing the prompt failed: {}", e.getMessage(), e);
            UIUtils.inferenceError(loc, e);
            updateButtons();
        }
    }

    private void rememberSendLore(boolean value) {
        try {
            ImageSettings changed = service.getSettings();
            changed.setSendLore(value);
            service.saveSettings(changed);
            settings = changed;
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void abortPrompt() {
        if (promptToken != null) {
            promptToken.cancel();
        }
    }

    // --- the image ---

    private void generate() {
        BackendConfig backend = backendSelect.getValue();
        if (backend == null || making) {
            if (making && imageCancel != null) {
                imageCancel.set(true);
            }
            return;
        }
        String text = ImageGeneratorService.withStyle(style.getValue(), prompt.getValue());
        if (StringUtils.isBlank(text)) {
            Notification.warning(T.localized(THE_PROMPT_IS_EMPTY));
            return;
        }
        String lora = backend.usesLora() ? StringUtils.trimToNull(loraBox.getValue()) : null;
        double strength = loraStrength.getValue() == null ? backend.getLoraStrength() : loraStrength.getValue();
        if (backend.usesLora()) {
            rememberLora(backend, lora, strength);
        }
        ImageRequest request = new ImageRequest(text, negative.getValue(), ImageGeneratorService.newSeed(),
                width.getValue() == null ? backend.getWidth() : width.getValue(),
                height.getValue() == null ? backend.getHeight() : height.getValue(), lora, strength);
        rememberPartValues();
        UI ui = UI.getCurrent();
        AtomicBoolean cancel = new AtomicBoolean();
        imageCancel = cancel;
        making = true;
        status.setText(T.localized(GENERATING_THE_IMAGE));
        updateButtons();
        worker.submit(() -> {
            byte[] bytes = null;
            String error = null;
            try {
                bytes = service.generate(backend, request, cancel::get);
                if (ImageFormats.extension(bytes) == null) {
                    bytes = null;
                    error = T.localized(THE_IMAGE_IS_NOT_VALID);
                }
            } catch (BackendException e) {
                error = cancel.get() ? null : e.getMessage();
                if (error != null) {
                    ImageGeneratorLog.get().warn("Generating the image failed: {}", e.getMessage());
                }
            } catch (RuntimeException e) {
                ImageGeneratorLog.get().warn("Generating the image failed: {}", e.getMessage(), e);
                error = String.valueOf(e.getMessage());
            }
            byte[] result = bytes;
            String message = error;
            inRequest(ui, () -> {
                making = false;
                status.setText("");
                if (message != null) {
                    Notification.error(T.localized(GENERATING_FAILED).formatted(message));
                } else if (result != null) {
                    showImage(result);
                    if (backend.usesLora() && backendSelect.getValue() == backend) {
                        // the server runs now (the before hooks started it): its list is current
                        loadLoras(backend);
                    }
                }
                if (message != null) {
                    new DesktopNotifier(ui).failed(ImageGeneratorExtension.NOTIFY_IMAGE, false, manuscript.getName());
                } else if (result != null) {
                    new DesktopNotifier(ui).finished(ImageGeneratorExtension.NOTIFY_IMAGE, false, manuscript.getName());
                }
                updateButtons();
                UIPushGuard.push(ui);
            });
        });
    }

    private void showImage(byte[] bytes) {
        image = bytes;
        imageExtension = ImageFormats.extension(bytes);
        String filename = "generated-" + (++imageCounter) + "." + imageExtension;
        StreamResource resource = new StreamResource(filename, () -> new ByteArrayInputStream(bytes));
        resource.setContentType(ImageFormats.mimeType(imageExtension));
        resource.setCacheTime(0);
        preview.removeAll();
        preview.add(new Image(resource, ""));
        preview.setVisible(true);
    }

    private void discard() {
        image = null;
        preview.removeAll();
        preview.setVisible(false);
        updateButtons();
    }

    private void accept() {
        if (image == null) {
            return;
        }
        try {
            message = service.attach(message, image, "generated-" + System.currentTimeMillis() + "." + imageExtension);
            Notification.success(T.localized(THE_IMAGE_WAS_ADDED));
            if (onChanged != null) {
                onChanged.accept(message);
            }
            floating.closeForGood();
        } catch (IllegalArgumentException e) {
            Notification.error(T.localized(THE_IMAGE_IS_NOT_VALID));
        } catch (Exception e) {
            ImageGeneratorLog.get().warn("Attaching the image failed: {}", e.getMessage(), e);
            UIUtils.internalServerError(loc, e);
        }
    }

    // --- the state ---

    private void updateButtons() {
        boolean hasBackend = backendSelect.getValue() != null;
        writePrompt.setText(writing ? T.localized(ABORT) : T.localized(WRITE_THE_PROMPT));
        writePrompt.setEnabled(!making && message != null);
        generate.setText(making ? T.localized(ABORT) : image == null ? T.localized(GENERATE) : T.localized(REROLL));
        generate.setEnabled(hasBackend && !writing);
        accept.setVisible(image != null);
        accept.setEnabled(!making);
        discard.setVisible(image != null);
        discard.setEnabled(!making);
        backendSelect.setEnabled(!making);
        floating.setRunning(writing || making);
    }

    private void detached(DetachEvent event) {
        // a window folded into the corner is detached too, and goes on
        if (!floating.isMinimised()) {
            dispose();
        }
    }

    /** Stops what runs and lets the backend go: the window is closed for good, or its page is gone. */
    private void dispose() {
        abortPrompt();
        if (imageCancel != null) {
            imageCancel.set(true);
        }
        worker.shutdown();
    }
}
