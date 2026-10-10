package com.github.enerccio.marginalia.ui.dialogs;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ExporterService;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import com.github.enerccio.marginalia.domain.service.ExporterService.Exporter;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InputStreamDownloadCallback;
import com.vaadin.flow.server.streams.InputStreamDownloadHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Exports the active branch of a book: asks for the format and the book details, collects the messages of the branch,
 * exports them and offers the result for download.
 */
@Configurable(preConstruction = true)
public class ExportDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private ExporterService exporterService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private User user;

    private final Manuscript manuscript;

    private Select<Exporter> format;
    private TextField fileName;
    private TextField bookTitle;
    private TextField author;
    private IntegerField from;
    private IntegerField to;
    private Checkbox includeHeaders;

    /**
     * @param manuscript the book with the up to date active leaf, which is the end of the exported branch
     */
    public ExportDialog(Manuscript manuscript) {
        this.manuscript = manuscript;

        setHeaderTitle(loc.getValue(L.LABEL_EXPORT_STORY));
        setWidth("500px");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.STRICT);

        format = new Select<>();
        format.setLabel(loc.getValue(L.LABEL_EXPORT_FORMAT));
        List<Exporter> exporters = exporterService.getExporters();
        format.setItems(exporters);
        format.setItemLabelGenerator(e -> e.getName() + " (." + e.getFileExtension() + ")");
        if (!exporters.isEmpty()) {
            format.setValue(exporters.getFirst());
        }

        fileName = new TextField(loc.getValue(L.LABEL_FILE_NAME), StringUtils.defaultString(manuscript.getName()), "");
        bookTitle = new TextField(loc.getValue(L.LABEL_BOOK_TITLE), StringUtils.defaultString(manuscript.getName()), "");
        author = new TextField(loc.getValue(L.LABEL_AUTHOR), StringUtils.defaultIfBlank(user.getFullName(), user.getLogin()), "");

        from = new IntegerField(loc.getValue(L.LABEL_FROM_MESSAGE));
        from.setMin(1);
        from.setValue(1);
        from.setStepButtonsVisible(true);
        to = new IntegerField(loc.getValue(L.LABEL_TO_MESSAGE));
        to.setMin(1);
        to.setStepButtonsVisible(true);

        includeHeaders = new Checkbox(loc.getValue(L.LABEL_INCLUDE_TITLE_PAGE), true);

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        form.add(format, fileName, bookTitle, author, from, to, includeHeaders);
        form.getChildren().forEach(c -> c.getElement().getStyle().set("width", "100%"));
        add(form);

        Button export = new Button(loc.getValue(L.LABEL_EXPORT_BACKUP), Solid.FILE_EXPORT.create(), event -> start());
        export.setThemeName("primary");
        export.setEnabled(!exporters.isEmpty());
        getFooter().add(new Button(loc.getValue(L.LABEL_CANCEL), event -> close()), export);
    }

    private void start() {
        Exporter exporter = format.getValue();
        boolean valid = true;
        for (TextField field : new TextField[]{fileName, bookTitle}) {
            field.setInvalid(StringUtils.isBlank(field.getValue()));
            valid &= !field.isInvalid();
        }
        if (from.isInvalid() || to.isInvalid()) {
            valid = false;
        } else if (from.getValue() != null && to.getValue() != null && to.getValue() < from.getValue()) {
            to.setErrorMessage(loc.getValue(L.ERROR_EXPORT_RANGE_INVALID));
            to.setInvalid(true);
            valid = false;
        } else {
            to.setInvalid(false);
        }
        if (!valid || exporter == null) {
            return;
        }

        ExportOptions options = new ExportOptions(bookTitle.getValue().trim(), StringUtils.trimToEmpty(author.getValue()),
                includeHeaders.getValue(), manuscript.getLanguage().getCode());
        String file = fileNameFor(fileName.getValue(), exporter);
        int first = from.getValue() == null ? 1 : from.getValue();
        Integer last = to.getValue();
        close();
        collect(exporter, options, file, first, last);
    }

    /**
     * Messages are collected on the active branch as it is now, in a dialog of its own since long books take a while.
     */
    private void collect(Exporter exporter, ExportOptions options, String file, int first, Integer last) {
        AtomicReference<List<ChatMessage>> collected = new AtomicReference<>(List.of());

        ProgressBarDialog collecting = new ProgressBarDialog(true);
        collecting.setTitle(loc.getValue(L.MSG_EXPORT_COLLECTING));
        collecting.create();
        collecting.setAction(dialog -> {
            try {
                collected.set(chatMessageService.getBranchFromLeaf(manuscript.getActiveLeaf()).stream()
                        .filter(m -> StringUtils.isNotBlank(m.getResponse()))
                        .toList());
            } catch (Exception e) {
                failed(dialog, e);
            }
        });
        collecting.setAfterAction(() -> {
            List<ChatMessage> all = collected.get();
            int toIndex = last == null ? all.size() : Math.min(last, all.size());
            if (first > toIndex) {
                Notification.warning(loc.getValue(L.MSG_EXPORT_NOTHING));
                return;
            }
            export(exporter, options, file, all.subList(first - 1, toIndex));
        });
        collecting.open();
    }

    private void export(Exporter exporter, ExportOptions options, String file, List<ChatMessage> messages) {
        AtomicReference<byte[]> exported = new AtomicReference<>();

        ProgressBarDialog exporting = new ProgressBarDialog(true);
        exporting.setTitle(loc.getValue(L.MSG_EXPORTING));
        exporting.setTotal((long) messages.size());
        exporting.create();
        exporting.setAction(dialog -> {
            try {
                exported.set(exporter.export(options, messages, dialog));
            } catch (Exception e) {
                failed(dialog, e);
            }
        });
        exporting.setAfterAction(() -> showResult(exporter, file, exported.get()));
        exporting.open();
    }

    /**
     * Called on the worker thread of the progress dialog, which then must not run its after action.
     */
    private void failed(ProgressBarDialog dialog, Exception e) {
        dialog.manualCancel = true;
        dialog.vaadinLocked(() -> {
            dialog.close();
            UIUtils.internalServerError(loc, e);
        });
    }

    private void showResult(Exporter exporter, String file, byte[] data) {
        Dialog result = new Dialog();
        result.setHeaderTitle(loc.getValue(L.LABEL_EXPORT_STORY));
        result.setCloseOnEsc(true);
        result.setCloseOnOutsideClick(false);
        result.setModality(ModalityMode.STRICT);

        Anchor download = new Anchor(new InputStreamDownloadHandler((InputStreamDownloadCallback) event ->
                new DownloadResponse(new ByteArrayInputStream(data), file, exporter.getMimeType(), data.length)), "");
        download.getElement().setAttribute("download", true);
        Button downloadButton = new Button(loc.getValue(L.LABEL_DOWNLOAD) + " " + file, Solid.DOWNLOAD.create());
        downloadButton.setThemeName("primary");
        download.add(downloadButton);

        VerticalLayout content = new VerticalLayout(new Span(String.format(loc.getValue(L.MSG_EXPORT_READY), file)), download);
        content.setPadding(false);
        result.add(content);
        result.getFooter().add(new Button(loc.getValue(L.LABEL_CLOSE), event -> result.close()));
        result.open();
    }

    /**
     * @return name of the file with the extension of the exporter, characters that file systems don't allow replaced
     */
    static String fileNameFor(String name, Exporter exporter) {
        String extension = "." + exporter.getFileExtension();
        String base = name.trim();
        if (base.toLowerCase().endsWith(extension)) {
            base = base.substring(0, base.length() - extension.length());
        }
        base = base.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
        return (base.isEmpty() ? "export" : base) + extension;
    }
}
