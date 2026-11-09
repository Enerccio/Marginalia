package com.github.enerccio.marginalia.extensions.imagegenerator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.imagegenerator.ImageGeneratorExtension;
import com.github.enerccio.marginalia.extensions.imagegenerator.ImageGeneratorLog;
import com.github.enerccio.marginalia.extensions.imagegenerator.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.imagegenerator.model.ImageSettings;
import com.github.enerccio.marginalia.extensions.imagegenerator.service.ImageFormats;
import com.github.enerccio.marginalia.extensions.imagegenerator.service.ImageGeneratorService;
import com.github.enerccio.marginalia.extensions.imagegenerator.service.ImageRequest;
import com.github.enerccio.marginalia.extensions.imagegenerator.service.ImageSession;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.DesktopNotifier;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.github.enerccio.marginalia.ui.widgets.ModelProtocolPicker;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.ByteArrayInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.imagegenerator.ImageGeneratorLocalization.*;

/**
 * Makes an image for one part of the story: the model writes the prompt from the part (the user can change it), the chosen backend
 * makes the picture, the user rerolls until it fits and accepts it - it becomes an image of the part. The backend is held between
 * the images (see {@link ImageSession}) and released when the window is done with it.
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
    private final IntegerField width = new IntegerField(T.localized(WIDTH));
    private final IntegerField height = new IntegerField(T.localized(HEIGHT));
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

    private ImageSession session;
    private String sessionBackendId;
    private CancellationToken promptToken;
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

        settings = service.getSettings();

        modelPicker.setPlaceholder(T.localized(MODEL_FOR_THE_PROMPT));
        initModelPicker();

        backendSelect.setLabel(T.localized(BACKEND));
        backendSelect.setItems(settings.getBackends());
        backendSelect.setItemLabelGenerator(b -> StringUtils.defaultIfBlank(b.getName(), T.localized(NEW_BACKEND)));
        backendSelect.setWidth("16em");
        BackendConfig selected = settings.find(settings.getSelectedBackendId());
        if (selected == null && !settings.getBackends().isEmpty()) {
            selected = settings.getBackends().getFirst();
        }
        backendSelect.setValue(selected);
        backendSelect.addValueChangeListener(e -> backendChanged(e.getValue()));

        HorizontalLayout top = new HorizontalLayout(modelPicker, writePrompt, backendSelect);
        top.setWidthFull();
        top.setWrap(true);
        top.setAlignItems(FlexComponent.Alignment.END);

        sendLore.setValue(settings.isSendLore());
        sendLore.getElement().setProperty("title", T.localized(THE_ENTRIES_THE_PART_ACTIVATES));
        sendLore.addValueChangeListener(e -> rememberSendLore(e.getValue()));
        details.setWidthFull();
        details.setMinHeight("4em");
        details.setMaxHeight("10em");
        details.setHelperText(T.localized(WHAT_THE_PICTURE_MUST_SHOW));

        prompt.setWidthFull();
        prompt.setMinHeight("8em");
        prompt.setMaxHeight("16em");

        negative.setWidthFull();
        negative.setMinHeight("4em");
        width.setMin(64);
        width.setStepButtonsVisible(true);
        height.setMin(64);
        height.setStepButtonsVisible(true);
        Details more = new Details(T.localized(SIZE_AND_NEGATIVE_PROMPT), new VerticalLayout(new HorizontalLayout(width, height), negative));
        more.setWidthFull();

        status.addClassName(ImageStyles.STATUS);
        preview.addClassName(ImageStyles.PREVIEW);
        preview.setWidthFull();
        accept.setThemeName("primary");
        accept.setVisible(false);
        discard.setVisible(false);
        generate.setThemeName("primary");

        if (settings.getBackends().isEmpty()) {
            Span hint = new Span(T.localized(NO_BACKEND_YET));
            hint.addClassName(CssStyles.HINT_TEXT);
            preview.add(hint);
        }

        VerticalLayout content = new VerticalLayout(top, sendLore, details, prompt, more, status, preview);
        content.setPadding(false);
        content.setSpacing(true);
        add(content);

        getFooter().add(new Button(T.localized(CLOSE), e -> close()), discard, accept, generate);
        DialogShortcuts.submitWith(writePrompt, details);
        DialogShortcuts.submitWith(generate, prompt, negative, width, height);

        applyBackendDefaults(selected);
        updateButtons();
        addDetachListener(this::detached);

        // the part is the whole prompt when nothing else is there: write it right away
        if (message != null && StringUtils.isNotBlank(message.getResponse())) {
            addOpenedChangeListener(e -> {
                if (e.isOpened() && prompt.isEmpty() && !writing) {
                    writePrompt();
                }
            });
        }
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
        // the new backend is a new session; the one that was held is let go
        releaseSession();
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
        if (backend == null) {
            return;
        }
        width.setValue(backend.getWidth());
        height.setValue(backend.getHeight());
        negative.setValue(backend.getNegativePrompt());
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
                    sendLore.getValue(), details.getValue());
            if (request.skippedLore() > 0) {
                Notification.warning(T.localized(LORE_ENTRIES_DID_NOT_FIT).formatted(request.skippedLore()));
            }
            if (sendLore.getValue()) {
                status.setText(T.localized(WAITING_FOR_THE_MODEL) + " " + T.localized(LORE_ENTRIES_SENT).formatted(request.loreEntries()));
            }
            promptToken = service.streamPrompt(ai, protocol, request.payload(), new ImageGeneratorService.PromptListener() {
                @Override
                public void onProgress(String text) {
                    ui.access(() -> {
                        if (writing) {
                            prompt.setValue(text.strip());
                            UIPushGuard.push(ui);
                        }
                    });
                }

                @Override
                public void onDone(boolean cancelled, Throwable error) {
                    ui.access(() -> {
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
                        }
                        updateButtons();
                        UIPushGuard.push(ui);
                    });
                }
            });
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
        String text = service.withStyle(settings, prompt.getValue());
        if (StringUtils.isBlank(text)) {
            Notification.warning(T.localized(THE_PROMPT_IS_EMPTY));
            return;
        }
        ImageRequest request = new ImageRequest(text, negative.getValue(), ImageGeneratorService.newSeed(),
                width.getValue() == null ? backend.getWidth() : width.getValue(),
                height.getValue() == null ? backend.getHeight() : height.getValue());
        UI ui = UI.getCurrent();
        ImageSession active = session(backend, ui);
        AtomicBoolean cancel = new AtomicBoolean();
        imageCancel = cancel;
        making = true;
        status.setText(T.localized(GENERATING_THE_IMAGE));
        updateButtons();
        worker.submit(() -> {
            byte[] bytes = null;
            String error = null;
            try {
                bytes = active.generate(request, cancel::get);
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
            ui.access(() -> {
                making = false;
                status.setText("");
                if (message != null) {
                    Notification.error(T.localized(GENERATING_FAILED).formatted(message));
                } else if (result != null) {
                    showImage(result);
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

    private ImageSession session(BackendConfig backend, UI ui) {
        if (session == null || !backend.getId().equals(sessionBackendId)) {
            releaseSession();
            sessionBackendId = backend.getId();
            session = service.createSession(backend, error -> ui.access(() -> {
                Notification.success(T.localized(THE_BACKEND_WAS_RELEASED));
                if (error != null) {
                    Notification.warning(T.localized(THE_AFTER_HOOK_FAILED).formatted(error));
                }
                UIPushGuard.push(ui);
            }));
        }
        return session;
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
    }

    private void discard() {
        image = null;
        preview.removeAll();
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
            close();
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
    }

    private void releaseSession() {
        ImageSession old = session;
        session = null;
        sessionBackendId = null;
        if (old != null) {
            // the after hook is a request; the thread of the window may be gone
            Thread.ofVirtual().name("image-generator-release").start(old::end);
        }
    }

    private void detached(DetachEvent event) {
        abortPrompt();
        if (imageCancel != null) {
            imageCancel.set(true);
        }
        releaseSession();
        worker.shutdown();
    }
}
