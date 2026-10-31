package com.github.enerccio.marginalia.extensions.lorebookvcs.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
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

import static com.github.enerccio.marginalia.extensions.lorebookvcs.LorebookVCSLocalization.*;

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
        addClassNames(CssStyles.BORDERED_PANEL, CssStyles.PADDING_COMPACT);

        buildUI();
    }

    private void buildUI() {
        Span title = new Span(T.localized(LOREBOOK_REVISIONS));
        title.addClassName(CssStyles.TEXT_BOLD);

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
        exportBtn.setTooltipText(T.localized(EXPORT_VCS_HISTORY_JSON));
        exportAnchor.add(exportBtn);

        Button importBtn = new Button(Solid.FILE_IMPORT.create());
        importBtn.setThemeName("tertiary icon small");
        importBtn.setTooltipText(T.localized(IMPORT_VCS_HISTORY_JSON));

        ContextMenu importMenu = new ContextMenu();
        importMenu.setTarget(importBtn);
        importMenu.setOpenOnClick(true);

        importMenu.addItem(T.localized(IMPORT_MARGINALIA_VCS_JSON), e -> openImportDialog(false));
        importMenu.addItem(T.localized(IMPORT_SILLYTAVERN_VCS_EXTENSION), e -> openImportDialog(true));

        HorizontalLayout rightGroup = new HorizontalLayout(importBtn, exportAnchor);
        rightGroup.setAlignItems(Alignment.CENTER);
        rightGroup.addClassName(CssStyles.MARGIN_LEFT_AUTO);

        add(title, rightGroup);
    }

    private void openImportDialog(boolean isSillyTavernFormat) {
        Lorebook lorebook = lorebookSupplier.get();
        if (lorebook == null) {
            Notification.warning(T.localized(NO_ACTIVE_LOREBOOK_SELECTED));
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle((isSillyTavernFormat
                ? T.localized(IMPORT_SILLYTAVERN_VCS_HISTORY)
                : T.localized(IMPORT_LOREBOOK_VCS_HISTORY)).formatted(StringUtils.defaultString(lorebook.getName())));
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
                        Notification.error(T.localized(INVALID_VCS_HISTORY_FORMAT));
                        return;
                    }

                    String fileUuid = parsed.getLorebookUuid();
                    if (StringUtils.isNotBlank(fileUuid) && !StringUtils.equals(fileUuid, lorebook.getUuid())) {
                        warnings.add(T.localized(THE_IMPORT_FILE_CONTAINS_REVISION)
                                .formatted(fileUuid, lorebook.getName()));
                    }
                    int dropped = vcsService.remapEntries(parsed, lorebook);
                    if (dropped > 0) {
                        warnings.add(T.localized(ENTRY_HISTORIES_MATCH_NO_ENTRY).formatted(dropped));
                    }
                }

                if (vcsService.hasHistory(lorebook)) {
                    warnings.add(T.localized(THE_EXISTING_REVISION_HISTORY).formatted(lorebook.getName()));
                }

                Runnable doImport = () -> {
                    try {
                        parsed.setLorebookUuid(lorebook.getUuid());
                        vcsService.saveVCSData(lorebook, parsed);
                        dialog.close();
                        Notification.show(T.localized(IMPORTED_VCS_HISTORY_SUCCESSFULLY));
                        if (onDataImported != null) onDataImported.run();
                    } catch (Exception ex) {
                        UIUtils.internalServerError(loc, ex);
                    }
                };

                if (warnings.isEmpty()) {
                    doImport.run();
                } else {
                    ConfirmDialog.show(String.join("\n\n", warnings) + "\n\n" + T.localized(ARE_YOU_SURE), doImport);
                }
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });

        Upload upload = new Upload(handler);
        upload.setAcceptedMimeTypes("application/json");

        Button cancelBtn = new Button(T.localized(CANCEL), e -> dialog.close());

        VerticalLayout layout = new VerticalLayout(upload);
        layout.setPadding(true);

        dialog.add(layout);
        dialog.getFooter().add(cancelBtn);
        dialog.open();
    }
}
