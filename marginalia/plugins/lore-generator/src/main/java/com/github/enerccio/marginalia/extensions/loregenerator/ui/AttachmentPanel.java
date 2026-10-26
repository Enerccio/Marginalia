package com.github.enerccio.marginalia.extensions.loregenerator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.github.enerccio.marginalia.extensions.loregenerator.LoreGeneratorLocalization.T;

/** The files sent with the generation: the upload (also by pasting into the prompt) and the list of what was added. */
public class AttachmentPanel extends VerticalLayout {

    /**
     * Pasting files (a screenshot, a file copied in the file manager) into the prompt does not put anything in the text
     * area: the files go through the upload, like the ones chosen with its button. Pasted text is left alone.
     * {@code $0} is the text area, {@code $1} the upload.
     */
    private static final String PASTE_FILES_SCRIPT = """
            if ($0.__loreGeneratorPaste) {
              $0.removeEventListener('paste', $0.__loreGeneratorPaste, true);
            }
            $0.__loreGeneratorPaste = (event) => {
              const files = event.clipboardData ? Array.from(event.clipboardData.files) : [];
              if (files.length > 0) {
                event.preventDefault();
                $1.uploadFiles(files);
              }
            };
            $0.addEventListener('paste', $0.__loreGeneratorPaste, true);
            """;

    /** Sent with the generation, in the order they were added. */
    private final List<LLMAttachment> attachments = new ArrayList<>();
    private Upload upload;
    private final VerticalLayout list = new VerticalLayout();
    private boolean busy;

    /** @param prompt the text area whose pasted files are uploaded */
    public AttachmentPanel(TextArea prompt) {
        setPadding(false);
        getThemeList().add("spacing-xs");

        InMemoryUploadHandler uploadHandler = new InMemoryUploadHandler((metadata, bytes) -> {
            attachments.add(LLMAttachment.of(LLMAttachment.Kind.fromMimeType(metadata.contentType()),
                    metadata.contentType(), metadata.fileName(), bytes));
            upload.clearFileList();
            refresh();
        });
        upload = new Upload(uploadHandler);
        upload.setMaxFileSize(20 * 1024 * 1024);
        upload.setDropAllowed(true);
        // the script needs the elements of the page, which exist once the text area is attached
        prompt.addAttachListener(e ->
                prompt.getElement().executeJs(PASTE_FILES_SCRIPT, prompt.getElement(), upload.getElement()));

        list.setPadding(false);
        list.setSpacing(false);
        // many attachments scroll in their own box instead of pushing the prompt and the buttons away
        list.addClassName(CssStyles.OVERFLOW_AUTO);
        list.setMaxHeight("140px");
        list.setMinHeight("0");
        Span label = new Span(T.localized("Attachments (images and files sent with the generation)"));
        label.addClassName(CssStyles.HINT_TEXT);
        add(label, upload, list);
    }

    /** A copy of the attachments in the order they were added. */
    public List<LLMAttachment> getAttachments() {
        return new ArrayList<>(attachments);
    }

    /** @param busy an inference runs: no new files, none can be removed (they are in the request already) */
    public void setBusy(boolean busy) {
        this.busy = busy;
        upload.setVisible(!busy);
        refresh();
    }

    private void refresh() {
        list.removeAll();
        for (LLMAttachment attachment : attachments) {
            Span label = new Span(T.localized("%s (%s, %s)").formatted(StringUtils.defaultIfBlank(attachment.getName(), T.localized("attachment")),
                    StringUtils.defaultIfBlank(attachment.getMimeType(), T.localized("unknown type")),
                    attachment.getData() != null ? T.localized("%d kB").formatted(attachment.getData().length / 1024) : T.localized("link")));
            Button delete = new Button(Solid.TRASH.create(), e -> {
                attachments.remove(attachment);
                refresh();
            });
            delete.setThemeName("error tertiary");
            delete.setTooltipText(T.localized("Delete attachment"));
            delete.setEnabled(!busy);
            HorizontalLayout row = new HorizontalLayout(label, delete);
            row.setAlignItems(FlexComponent.Alignment.CENTER);
            list.add(row);
        }
    }
}
