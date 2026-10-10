package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.domain.model.ImageAttachment;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.MessageImages;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Manages the images attached to a message: add, caption, remove. The images are not part of the message text, they are
 * shown with the message and in the exports, the model never sees them.
 */
@Configurable(preConstruction = true)
@Extendable
public class ImagesDialog extends Dialog {

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
        getFooter().add(new Button(loc.getValue(L.LABEL_CLOSE), _ -> close()));

        refresh(onChanged);
    }

    private void store(List<ImageAttachment> images, Consumer<ChatMessage> onChanged) throws Exception {
        message.setImages(images);
        message = chatMessageService.save(message);
        refresh(onChanged);
        onChanged.accept(message);
    }

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
                            current.set(index, new ImageAttachment(attachment.resource(), e.getValue()));
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

                VerticalLayout fields = new VerticalLayout(caption);
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
