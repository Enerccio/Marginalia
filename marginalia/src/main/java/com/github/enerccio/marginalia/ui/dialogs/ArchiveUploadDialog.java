package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Asks for a zip archive and hands it to the callback; shared by the imports of {@code .md} files (see
 * {@link com.github.enerccio.marginalia.utils.MarkdownArchive}). The callback signals an archive it can't use with an
 * {@link IllegalArgumentException}, the user is warned and the dialog stays open.
 */
@Configurable
public class ArchiveUploadDialog extends Dialog {

    /**
     * What is done with the uploaded archive.
     */
    @FunctionalInterface
    public interface Handler {
        void accept(byte[] archive, String fileName) throws Exception;
    }

    @Autowired
    private Localization loc;

    private final String title;
    private final String message;
    private final Handler handler;

    private final UI ui = UI.getCurrent();

    private ArchiveUploadDialog(String title, String message, Handler handler) {
        this.title = title;
        this.message = message;
        this.handler = handler;
    }

    /**
     * Opens the dialog; the callback runs when an archive was uploaded and the dialog is closed when it did not fail.
     */
    public static void show(String title, String message, Handler handler) {
        ArchiveUploadDialog dialog = new ArchiveUploadDialog(title, message, handler);
        dialog.create();
        dialog.open();
    }

    private void create() {
        setHeaderTitle(title);
        setWidth("450px");

        InMemoryUploadHandler uploadHandler = new InMemoryUploadHandler((metadata, data) -> {
            // the dialogs the handler opens find the UI of the upload
            if (UI.getCurrent() == null) {
                UI.setCurrent(ui);
            }
            try {
                handler.accept(data, metadata.fileName());
                close();
            } catch (IllegalArgumentException e) {
                Notification.warning(loc.getValue(L.MSG_INVALID_ARCHIVE));
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });

        Upload upload = new Upload(uploadHandler);
        upload.setAcceptedFileTypes("application/zip", "application/x-zip-compressed", ".zip");
        VerticalLayout layout = new VerticalLayout(new Span(message), upload);
        layout.setPadding(true);
        add(layout);
        getFooter().add(new Button(loc.getValue(L.LABEL_CANCEL), event -> close()));
    }
}
