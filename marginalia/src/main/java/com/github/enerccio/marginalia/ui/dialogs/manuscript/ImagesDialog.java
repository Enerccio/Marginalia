package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.ImageAnchor;
import com.github.enerccio.marginalia.domain.model.ImageAttachment;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.components.MessageImages;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Manages the images attached to a message: add, caption, remove, and where each one is in the viewer and in the exports (the place
 * in the text, whether the text flows around it, its width). The images are not part of the message text, the model never sees them.
 * The story editor ignores the place and lists the images after the text; the preview shows what the viewer makes of them.
 */
@Configurable(preConstruction = true)
@Extendable
public class ImagesDialog extends Dialog implements LLMInteract {

    @Autowired
    private Localization loc;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ChatMessageService chatMessageService;

    private final VerticalLayout list = new VerticalLayout();
    private final MessageImages renderer = new MessageImages(List.of());
    private ChatMessage message;

    public ImagesDialog(ChatMessage message, Consumer<ChatMessage> onChanged) {
        this(message, onChanged, null);
    }

    /**
     * @param preview shows the part as the viewer does, null for a dialog without the button
     */
    public ImagesDialog(ChatMessage message, Consumer<ChatMessage> onChanged, Runnable preview) {
        this.message = message;

        setHeaderTitle(loc.getValue(L.LABEL_IMAGES));
        setWidth("600px");
        setMaxHeight("80vh");

        list.setPadding(false);
        list.setSpacing(true);

        InMemoryUploadHandler handler = new InMemoryUploadHandler((metadata, data) -> {
            try {
                Resource resource = resourceService.uploadImage(metadata.fileName(), data, ChatMessage.class, this.message.getId());
                List<ImageAttachment> images = this.message.getImages();
                images.add(new ImageAttachment(resource.getUuid(), ""));
                store(images, onChanged);
            } catch (IllegalArgumentException e) {
                Notification.error(loc.getValue(L.ERROR_INVALID_IMAGE));
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
        Upload upload = new Upload(handler);
        upload.setAcceptedMimeTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setMaxFileSize(ResourceService.MAX_IMAGE_BYTES);
        upload.setUploadButton(new Button(loc.getValue(L.LABEL_ADD_IMAGE), VaadinIcon.PICTURE.create()));
        upload.setDropAllowed(true);
        upload.addFileRejectedListener(_ -> Notification.error(loc.getValue(L.ERROR_INVALID_IMAGE)));

        VerticalLayout content = new VerticalLayout(upload, list);
        content.setPadding(false);
        add(content);
        if (preview != null) {
            Button show = new Button(loc.getValue(L.LABEL_PREVIEW_AS_VIEWER), VaadinIcon.EYE.create(), _ -> preview.run());
            getFooter().add(show);
        }
        getFooter().add(new Button(loc.getValue(L.LABEL_CLOSE), _ -> close()));

        refresh(onChanged);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "images", type = List.class),
                    @APIParameter(name = "onChanged", type = Consumer.class)})
    private void store(List<ImageAttachment> images, Consumer<ChatMessage> onChanged) throws Exception {
        message.setImages(images);
        message = chatMessageService.save(message);
        refresh(onChanged);
        onChanged.accept(message);
    }

    /**
     * Saves the image at the index as the function changes it.
     *
     * @param redraw true to build the controls again (the change affects what the other controls offer)
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "index", type = int.class),
                    @APIParameter(name = "change", type = UnaryOperator.class),
                    @APIParameter(name = "redraw", type = boolean.class),
                    @APIParameter(name = "onChanged", type = Consumer.class)},
            locals = {@APILocals(name = "current", type = List.class),
                    @APILocals(name = "ex", type = Exception.class)})
    private void change(int index, UnaryOperator<ImageAttachment> change, boolean redraw, Consumer<ChatMessage> onChanged) {
        try {
            List<ImageAttachment> current = message.getImages();
            current.set(index, change.apply(current.get(index)));
            message.setImages(current);
            message = chatMessageService.save(message);
            if (redraw) {
                refresh(onChanged);
            }
            onChanged.accept(message);
        } catch (Exception ex) {
            UIUtils.internalServerError(loc, ex);
        }
    }

    /**
     * The place of the image (a 3x3 picker: the column is the side of the text, the row the place in it), whether the text flows
     * around it, and its width.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = Div.class),
            parameters = {@APIParameter(name = "attachment", type = ImageAttachment.class),
                    @APIParameter(name = "index", type = int.class),
                    @APIParameter(name = "onChanged", type = Consumer.class)},
            locals = {@APILocals(name = "anchor", type = ImageAnchor.class),
                    @APILocals(name = "picker", type = Div.class),
                    @APILocals(name = "candidate", type = ImageAnchor.class),
                    @APILocals(name = "button", type = Button.class),
                    @APILocals(name = "name", type = String.class),
                    @APILocals(name = "wrap", type = Checkbox.class),
                    @APILocals(name = "automatic", type = Checkbox.class),
                    @APILocals(name = "width", type = IntegerField.class),
                    @APILocals(name = "shown", type = Integer.class),
                    @APILocals(name = "options", type = VerticalLayout.class),
                    @APILocals(name = "row", type = HorizontalLayout.class),
                    @APILocals(name = "hint", type = Span.class)})
    private Div layoutControls(ImageAttachment attachment, int index, Consumer<ChatMessage> onChanged) {
        ImageAnchor anchor = attachment.effectiveAnchor();

        Div picker = new Div();
        picker.addClassName(CssStyles.IMAGE_ANCHOR_PICKER);
        for (ImageAnchor candidate : ImageAnchor.values()) {
            boolean selected = candidate == anchor;
            Button button = new Button((selected ? VaadinIcon.CIRCLE : VaadinIcon.CIRCLE_THIN).create(),
                    _ -> change(index, current -> current.withLayout(candidate, current.wrap(), current.width()), true, onChanged));
            button.setThemeName(selected ? "primary icon" : "tertiary icon");
            String name = loc.getValue(L.valueOf("ENUM_IMAGE_ANCHOR_" + candidate.name()));
            button.setAriaLabel(name);
            UIUtils.addTooltip(button, name);
            picker.add(button);
        }

        Checkbox wrap = new Checkbox(loc.getValue(L.LABEL_IMAGE_WRAP), attachment.wrap());
        wrap.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                change(index, current -> current.withLayout(current.anchor(), e.getValue(), current.width()), true, onChanged);
            }
        });

        Checkbox automatic = new Checkbox(loc.getValue(L.LABEL_AUTOMATIC), attachment.width() == null);
        IntegerField width = new IntegerField(loc.getValue(L.LABEL_IMAGE_WIDTH));
        width.setMin(ImageAttachment.MIN_WIDTH);
        width.setMax(attachment.maxWidth());
        width.setStepButtonsVisible(true);
        width.setStep(5);
        Integer shown = attachment.effectiveWidth();
        width.setValue(shown == null ? Math.min(50, width.getMax()) : shown);
        width.setEnabled(attachment.width() != null);
        automatic.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                Integer value = e.getValue() ? null : Math.min(50, width.getMax());
                change(index, current -> current.withLayout(current.anchor(), current.wrap(), value), true, onChanged);
            }
        });
        width.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null && !width.isInvalid()) {
                change(index, current -> current.withLayout(current.anchor(), current.wrap(), e.getValue()), false, onChanged);
            }
        });

        VerticalLayout options = new VerticalLayout(wrap, new HorizontalLayout(automatic, width));
        options.setPadding(false);
        options.setSpacing(false);
        ((HorizontalLayout) options.getComponentAt(1)).setAlignItems(FlexComponent.Alignment.END);
        HorizontalLayout row = new HorizontalLayout(picker, options);
        row.setAlignItems(FlexComponent.Alignment.START);
        Span hint = new Span(loc.getValue(L.HELP_IMAGE_LAYOUT));
        hint.addClassNames(CssStyles.TEXT_S, CssStyles.TEXT_SECONDARY);
        return new Div(row, hint);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "onChanged", type = Consumer.class)},
            locals = {@APILocals(name = "images", type = List.class),
                    @APILocals(name = "attachment", type = ImageAttachment.class),
                    @APILocals(name = "resource", type = Resource.class),
                    @APILocals(name = "caption", type = TextField.class),
                    @APILocals(name = "remove", type = Button.class),
                    @APILocals(name = "fields", type = VerticalLayout.class),
                    @APILocals(name = "row", type = HorizontalLayout.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void refresh(Consumer<ChatMessage> onChanged) {
        list.removeAll();
        List<ImageAttachment> images = message.getImages();
        for (int i = 0; i < images.size(); i++) {
            int index = i;
            ImageAttachment attachment = images.get(i);
            try {
                Resource resource = resourceService.findImage(attachment.resource());
                if (resource == null) {
                    continue;
                }

                TextField caption = new TextField(loc.getValue(L.LABEL_IMAGE_CAPTION));
                caption.setWidthFull();
                caption.setValue(attachment.caption() == null ? "" : attachment.caption());
                caption.addValueChangeListener(e -> {
                    if (e.isFromClient()) {
                        try {
                            List<ImageAttachment> current = message.getImages();
                            current.set(index, current.get(index).withCaption(e.getValue()));
                            message.setImages(current);
                            message = chatMessageService.save(message);
                            onChanged.accept(message);
                        } catch (Exception ex) {
                            UIUtils.internalServerError(loc, ex);
                        }
                    }
                });

                Button remove = new Button(VaadinIcon.TRASH.create(), _ -> {
                    try {
                        List<ImageAttachment> current = new ArrayList<>(message.getImages());
                        current.remove(index);
                        // the picture is no longer used by the part (the resource stays, it can be deleted in Resources)
                        if (message.getId().equals(resource.getObjectId()) && ChatMessage.class.getName().equals(resource.getClazz())) {
                            resourceService.unlink(resource);
                        }
                        store(current, onChanged);
                    } catch (Exception ex) {
                        UIUtils.internalServerError(loc, ex);
                    }
                });
                remove.setThemeName("tertiary icon error");
                remove.setAriaLabel(loc.getValue(L.LABEL_REMOVE_IMAGE));
                UIUtils.addTooltip(remove, loc.getValue(L.LABEL_REMOVE_IMAGE));

                VerticalLayout fields = new VerticalLayout(caption, layoutControls(attachment, index, onChanged));
                fields.setPadding(false);
                HorizontalLayout row = new HorizontalLayout(renderer.figure(resource, "", "160px"), fields, remove);
                row.setWidthFull();
                row.setAlignItems(FlexComponent.Alignment.CENTER);
                row.setFlexGrow(1, fields);
                list.add(row);
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }
    }
}
