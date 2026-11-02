package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.SummaryService.ImportEntry;
import com.github.enerccio.marginalia.domain.service.SummaryService.ImportStatus;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.InputStreamDownloadCallback;
import com.vaadin.flow.server.streams.InputStreamDownloadHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Brings the summaries of an exported file ({@link SummaryService#exportSummaries}) back into the active branch of the
 * book: the file is read when it is uploaded, the grid says what would happen to each summary and nothing is written
 * until the user presses Import. {@link #addMenuItems} puts both the export and this dialog into the menu bar of the
 * summaries overview (the desktop and the phone one).
 */
@Configurable
public class SummariesImportDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private SummaryService summaryService;

    private final Manuscript manuscript;
    private final Runnable onImported;
    private final UI ui = UI.getCurrent();

    private String fileText;
    private List<ImportEntry> entries = List.of();

    private Checkbox byPosition;
    private Span status;
    private Grid<ImportEntry> grid;
    private Button importButton;

    private SummariesImportDialog(Manuscript manuscript, Runnable onImported) {
        this.manuscript = manuscript;
        this.onImported = onImported;
    }

    /**
     * Adds <i>Export Summaries</i> (a download of the file) and <i>Import Summaries</i> to the bar.
     *
     * @param onImported runs after summaries were changed, the overview loads them again
     */
    public static void addMenuItems(MenuBar bar, Manuscript manuscript, Localization loc, SummaryService summaryService,
                                    Runnable onImported) {
        Anchor export = new Anchor(new InputStreamDownloadHandler((InputStreamDownloadCallback) event -> {
            try {
                byte[] data = summaryService.exportSummaries(manuscript).getBytes(StandardCharsets.UTF_8);
                String name = StringUtils.defaultIfBlank(manuscript.getName(), "book").replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
                return new DownloadResponse(new ByteArrayInputStream(data), name + "-summaries.md", "text/markdown", data.length);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }), loc.getValue(L.LABEL_EXPORT_SUMMARIES));
        export.getElement().setAttribute("download", true);
        bar.addItem(export);
        bar.addItem(loc.getValue(L.LABEL_IMPORT_SUMMARIES), _ -> {
            SummariesImportDialog dialog = new SummariesImportDialog(manuscript, onImported);
            dialog.create();
            dialog.open();
        });
    }

    private void create() {
        setHeaderTitle(loc.getValue(L.LABEL_IMPORT_SUMMARIES));
        setWidth("900px");
        setMaxWidth("100vw");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.STRICT);

        Upload upload = new Upload(new InMemoryUploadHandler((metadata, data) -> ui.access(() -> {
            fileText = new String(data, StandardCharsets.UTF_8);
            if (fileText.startsWith("﻿")) {
                fileText = fileText.substring(1);
            }
            analyze();
        })));
        upload.setAcceptedFileTypes(".md", ".txt", "text/markdown", "text/plain");
        upload.setMaxFiles(1);

        byPosition = new Checkbox(loc.getValue(L.LABEL_MATCH_BY_POSITION));
        byPosition.addValueChangeListener(_ -> analyze());
        Span help = new Span(loc.getValue(L.HELP_MATCH_BY_POSITION));
        help.addClassName(CssStyles.TEXT_SECONDARY);

        status = new Span();
        grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("220px");
        grid.addColumn(ImportEntry::order).setHeader(loc.getValue(L.LABEL_ORDER)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(entry -> statusText(entry.status())).setHeader(loc.getValue(L.LABEL_STATUS)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(entry -> StringUtils.abbreviate(StringUtils.normalizeSpace(entry.text()), 300))
                .setHeader(loc.getValue(L.LABEL_CONTENT)).setFlexGrow(1);

        VerticalLayout layout = new VerticalLayout(new Span(loc.getValue(L.MSG_IMPORT_SUMMARIES)), upload, byPosition, help, status, grid);
        layout.setPadding(false);
        add(layout);

        importButton = new Button(loc.getValue(L.LABEL_IMPORT), _ -> doImport());
        importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new Button(loc.getValue(L.LABEL_CANCEL), _ -> close()), importButton);
        refreshState();
        UIUtils.fitPhone(this, true);
    }

    private String statusText(ImportStatus importStatus) {
        return switch (importStatus) {
            case CHANGED -> loc.getValue(L.LABEL_SUMMARY_WILL_CHANGE);
            case CHANGED_BY_POSITION -> loc.getValue(L.LABEL_SUMMARY_WILL_CHANGE_BY_POSITION);
            case UNCHANGED -> loc.getValue(L.ENUM_JSON_STATUS_UNCHANGED);
            case NOT_FOUND -> loc.getValue(L.LABEL_SUMMARY_PART_NOT_FOUND);
            case EMPTY -> loc.getValue(L.LABEL_SUMMARY_EMPTY_TEXT);
        };
    }

    private void analyze() {
        if (fileText == null) {
            return;
        }
        try {
            entries = summaryService.analyzeImport(manuscript, fileText, byPosition.getValue());
        } catch (IllegalArgumentException e) {
            fileText = null;
            entries = List.of();
            Notification.warning(loc.getValue(L.MSG_INVALID_SUMMARIES_FILE));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }
        refreshState();
    }

    private void refreshState() {
        grid.setItems(entries);
        long changes = entries.stream().filter(entry -> entry.status().isChange()).count();
        status.setText(entries.isEmpty() ? "" : String.format(loc.getValue(L.MSG_SUMMARIES_IMPORT_READY), changes, entries.size()));
        importButton.setEnabled(changes > 0);
    }

    private void doImport() {
        try {
            int changed = summaryService.applyImport(manuscript, entries);
            Notification.success(String.format(loc.getValue(L.MSG_SUMMARIES_IMPORTED), changed));
            close();
            onImported.run();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
