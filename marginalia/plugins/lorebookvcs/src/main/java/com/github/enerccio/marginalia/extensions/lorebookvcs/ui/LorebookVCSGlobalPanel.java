package com.github.enerccio.marginalia.extensions.lorebookvcs.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.extensions.lorebookvcs.model.LorebookVCSData;
import com.github.enerccio.marginalia.extensions.lorebookvcs.service.LorebookVCSService;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.InputStreamDownloadCallback;
import com.vaadin.flow.server.streams.InputStreamDownloadHandler;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class LorebookVCSGlobalPanel extends HorizontalLayout {

    private final LorebookVCSService vcsService;
    private final Lorebook lorebook;
    private final Runnable onDataImported;

    public LorebookVCSGlobalPanel(LorebookVCSService vcsService, Lorebook lorebook, Runnable onDataImported) {
        this.vcsService = vcsService;
        this.lorebook = lorebook;
        this.onDataImported = onDataImported;

        setWidthFull();
        setAlignItems(Alignment.CENTER);
        getStyle().set("padding", "6px 12px");
        getStyle().set("background", "var(--lumo-contrast-5pct)");
        getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        getStyle().set("margin-bottom", "8px");

        buildUI();
    }

    private void buildUI() {
        Span title = new Span("Lorebook Revisions");
        title.getStyle().set("font-weight", "bold");

        Anchor exportAnchor = new Anchor(new InputStreamDownloadHandler((InputStreamDownloadCallback) downloadEvent -> {
            try {
                String json = vcsService.exportVCSJson(lorebook);
                byte[] data = json.getBytes(StandardCharsets.UTF_8);
                String fileName = (lorebook.getName() != null ? lorebook.getName().replaceAll("[^a-zA-Z0-9.-]", "_") : "lorebook") + "_vcs_history.json";
                return new DownloadResponse(new ByteArrayInputStream(data), fileName, "application/json", data.length);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }), "");
        exportAnchor.getElement().setAttribute("download", true);

        Button exportBtn = new Button(Solid.FILE_EXPORT.create());
        exportBtn.setThemeName("tertiary icon small");
        exportBtn.setTooltipText("Export VCS History JSON");
        exportAnchor.add(exportBtn);

        Button importBtn = new Button(Solid.FILE_IMPORT.create());
        importBtn.setThemeName("tertiary icon small");
        importBtn.setTooltipText("Import VCS History JSON");

        ContextMenu importMenu = new ContextMenu();
        importMenu.setTarget(importBtn);
        importMenu.setOpenOnClick(true);

        importMenu.addItem("Import Marginalia VCS JSON", e -> openImportDialog(false));
        importMenu.addItem("Import SillyTavern VCS Extension JSON", e -> openImportDialog(true));

        HorizontalLayout rightGroup = new HorizontalLayout(importBtn, exportAnchor);
        rightGroup.setAlignItems(Alignment.CENTER);
        rightGroup.getStyle().set("margin-left", "auto");

        add(title, rightGroup);
    }

    private void openImportDialog(boolean isSillyTavernFormat) {
        if (lorebook == null) {
            Notification.warning("No active lorebook selected");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isSillyTavernFormat ? "Import SillyTavern VCS History" : "Import Lorebook VCS History");
        dialog.setWidth("450px");

        InMemoryUploadHandler handler = new InMemoryUploadHandler((metadata, data) -> {
            try {
                String json = new String(data, StandardCharsets.UTF_8);

                if (isSillyTavernFormat) {
                    LorebookVCSData parsedST = vcsService.parseSillyTavernVCSJson(json, lorebook);
                    vcsService.saveVCSData(lorebook, parsedST);
                    dialog.close();
                    Notification.show("Successfully imported SillyTavern VCS history");
                    if (onDataImported != null) onDataImported.run();
                } else {
                    LorebookVCSData parsedData = vcsService.parseVCSJson(json);
                    if (parsedData == null) {
                        Notification.error("Invalid VCS history format");
                        return;
                    }

                    String fileUuid = parsedData.getLorebookUuid();
                    String targetUuid = lorebook.getUuid();

                    if (StringUtils.isNotBlank(fileUuid) && !StringUtils.equals(fileUuid, targetUuid)) {
                        String msg = "Warning: The import file contains revision history designated for lorebook UUID '" + fileUuid
                                + "', but the active lorebook is '" + targetUuid + "'.\n\nProceeding will map these revisions onto the current lorebook. Are you sure?";

                        ConfirmDialog.show(msg, () -> {
                            try {
                                parsedData.setLorebookUuid(targetUuid);
                                vcsService.saveVCSData(lorebook, parsedData);
                                dialog.close();
                                Notification.show("Imported VCS history successfully");
                                if (onDataImported != null) onDataImported.run();
                            } catch (Exception ex) {
                                UIUtils.internalServerError(null, ex);
                            }
                        });
                    } else {
                        parsedData.setLorebookUuid(targetUuid);
                        vcsService.saveVCSData(lorebook, parsedData);
                        dialog.close();
                        Notification.show("Imported VCS history successfully");
                        if (onDataImported != null) onDataImported.run();
                    }
                }
            } catch (Exception e) {
                UIUtils.internalServerError(null, e);
            }
        });

        Upload upload = new Upload(handler);
        upload.setAcceptedMimeTypes("application/json");

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        VerticalLayout layout = new VerticalLayout(upload);
        layout.setPadding(true);

        dialog.add(layout);
        dialog.getFooter().add(cancelBtn);
        dialog.open();
    }
}