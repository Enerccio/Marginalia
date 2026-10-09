package com.github.enerccio.marginalia.extensions.lorebookvcs.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.extensions.lorebookvcs.model.LorebookVCSData;
import com.github.enerccio.marginalia.extensions.lorebookvcs.service.LorebookVCSService;
import com.github.enerccio.marginalia.loc.Localization;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Configurable
public class LorebookVCSGlobalPanel extends HorizontalLayout {

    @Autowired
    private Localization loc;

    private final LorebookVCSService vcsService;
    private final Supplier<Lorebook> lorebookSupplier;
    private final Runnable onDataImported;

    /**
     * @param lorebookSupplier the lorebook selected in the view, read on every action
     */
    public LorebookVCSGlobalPanel(LorebookVCSService vcsService, Supplier<Lorebook> lorebookSupplier, Runnable onDataImported) {
        this.vcsService = vcsService;
        this.lorebookSupplier = lorebookSupplier;
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
                Lorebook lorebook = lorebookSupplier.get();
                String json = vcsService.exportVCSJson(lorebook);
                byte[] data = json.getBytes(StandardCharsets.UTF_8);
                String fileName = (lorebook != null && lorebook.getName() != null ? lorebook.getName().replaceAll("[^a-zA-Z0-9.-]", "_") : "lorebook") + "_vcs_history.json";
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
        Lorebook lorebook = lorebookSupplier.get();
        if (lorebook == null) {
            Notification.warning("No active lorebook selected");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle((isSillyTavernFormat ? "Import SillyTavern VCS History" : "Import Lorebook VCS History")
                + " into " + StringUtils.defaultString(lorebook.getName()));
        dialog.setWidth("450px");

        InMemoryUploadHandler handler = new InMemoryUploadHandler((metadata, data) -> {
            try {
                String json = new String(data, StandardCharsets.UTF_8);
                LorebookVCSData parsed;
                List<String> warnings = new ArrayList<>();

                if (isSillyTavernFormat) {
                    parsed = vcsService.parseSillyTavernVCSJson(json, lorebook);
                } else {
                    parsed = vcsService.parseVCSJson(json);
                    if (parsed == null) {
                        Notification.error("Invalid VCS history format");
                        return;
                    }

                    String fileUuid = parsed.getLorebookUuid();
                    if (StringUtils.isNotBlank(fileUuid) && !StringUtils.equals(fileUuid, lorebook.getUuid())) {
                        warnings.add("The import file contains revision history of another lorebook (UUID '" + fileUuid
                                + "'). Its revisions will be mapped onto the entries of '" + lorebook.getName()
                                + "' by entry name and order.");
                    }
                    int dropped = vcsService.remapEntries(parsed, lorebook);
                    if (dropped > 0) {
                        warnings.add(dropped + " entry histories match no entry of this lorebook and will be skipped.");
                    }
                }

                if (vcsService.hasHistory(lorebook)) {
                    warnings.add("The existing revision history of '" + lorebook.getName() + "' will be replaced.");
                }

                Runnable doImport = () -> {
                    try {
                        parsed.setLorebookUuid(lorebook.getUuid());
                        vcsService.saveVCSData(lorebook, parsed);
                        dialog.close();
                        Notification.show("Imported VCS history successfully");
                        if (onDataImported != null) onDataImported.run();
                    } catch (Exception ex) {
                        UIUtils.internalServerError(loc, ex);
                    }
                };

                if (warnings.isEmpty()) {
                    doImport.run();
                } else {
                    ConfirmDialog.show(String.join("\n\n", warnings) + "\n\nAre you sure?", doImport);
                }
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
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
