package com.github.enerccio.marginalia.ui.workspace.parts;

import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceComponent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import org.apache.commons.io.FileUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.File;
import java.util.List;

@Configurable
public class AdminPart implements WorkspaceComponent {

    private static final Logger log = LoggerFactory.getLogger(AdminPart.class);

    private final Workspace workspace;

    @Autowired
    private OsgiService osgiService;

    @Autowired
    private Localization loc;

    private Grid<Bundle> extensionsGrid;

    public AdminPart(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public Component create() throws Exception {
        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        Tab extensionsTab = new Tab(loc.getValue(L.LABEL_EXTENSIONS));
        Component extensionsContent = createExtensionsTab();
        tabSheet.add(extensionsTab, extensionsContent);

        Div container = new Div(tabSheet);
        container.setSizeFull();
        return container;
    }

    private Component createExtensionsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(HorizontalLayout.Alignment.CENTER);

        InMemoryUploadHandler handler = new InMemoryUploadHandler((metadata, data) -> {
            try {
                File targetFile = new File(osgiService.getExtensionsPath(), metadata.fileName());
                FileUtils.writeByteArrayToFile(targetFile, data);

                osgiService.installPackage(targetFile);

                Notification.show(loc.getValue(L.MSG_EXTENSION_INSTALLED_SUCCESS));
                refreshGrid();
            } catch (Exception e) {
                log.error("Failed to load extension bundle", e);
                Notification.show(
                        String.format(loc.getValue(L.ERROR_EXTENSION_INSTALL_FAILED), e.getMessage()),
                        5000,
                        Notification.Position.MIDDLE
                );
            }
        });

        Upload upload = new Upload(handler);
        upload.setAcceptedFileExtensions(".jar");
        upload.setAcceptedMimeTypes("application/zip", "application/jar", "application/java-archive", "application/x-java-archive");
        upload.setUploadButton(new Button(loc.getValue(L.LABEL_UPLOAD_EXTENSION), VaadinIcon.UPLOAD.create()));
        upload.setDropLabel(new Div(new Button(loc.getValue(L.LABEL_UPLOAD_EXTENSION))));

        Button refreshButton = new Button(loc.getValue(L.LABEL_REFRESH_EXTENSIONS), VaadinIcon.REFRESH.create());
        refreshButton.addClickListener(e -> refreshGrid());

        toolbar.add(upload, refreshButton);

        extensionsGrid = new Grid<>();
        extensionsGrid.setSizeFull();

        extensionsGrid.addColumn(Bundle::getBundleId)
                .setHeader(loc.getValue(L.LABEL_BUNDLE_ID))
                .setFlexGrow(0)
                .setWidth("80px");

        extensionsGrid.addColumn(bundle -> {
                    String name = bundle.getHeaders().get("Bundle-Name");
                    return (name != null && !name.isBlank()) ? name : bundle.getSymbolicName();
                })
                .setHeader(loc.getValue(L.LABEL_BUNDLE_NAME))
                .setFlexGrow(1);

        extensionsGrid.addColumn(bundle -> bundle.getVersion().toString())
                .setHeader(loc.getValue(L.LABEL_BUNDLE_VERSION))
                .setFlexGrow(0)
                .setWidth("150px");

        extensionsGrid.addColumn(bundle -> formatBundleState(bundle.getState()))
                .setHeader(loc.getValue(L.LABEL_BUNDLE_STATE))
                .setFlexGrow(0)
                .setWidth("140px");

        extensionsGrid.addComponentColumn(bundle -> {
                    Button unloadButton = new Button(loc.getValue(L.LABEL_UNLOAD_EXTENSION), VaadinIcon.TRASH.create());
                    unloadButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
                    unloadButton.addClickListener(e -> unloadExtension(bundle));
                    return unloadButton;
                })
                .setHeader(loc.getValue(L.LABEL_ACTIONS))
                .setFlexGrow(0)
                .setWidth("130px");

        layout.add(toolbar, extensionsGrid);
        refreshGrid();

        return layout;
    }

    private void unloadExtension(Bundle bundle) {
        try {
            osgiService.uninstallPackage(bundle);
            Notification.show(loc.getValue(L.MSG_EXTENSION_UNINSTALLED_SUCCESS));
            refreshGrid();
        } catch (Exception e) {
            log.error("Failed to unload extension", e);
            Notification.show(
                    String.format(loc.getValue(L.ERROR_EXTENSION_UNINSTALL_FAILED), e.getMessage()),
                    5000,
                    Notification.Position.MIDDLE
            );
        }
    }

    private void refreshGrid() {
        if (extensionsGrid != null && osgiService != null) {
            List<Bundle> bundles = osgiService.getBundles();
            extensionsGrid.setItems(bundles);
        }
    }

    private String formatBundleState(int state) {
        return switch (state) {
            case Bundle.UNINSTALLED -> "UNINSTALLED";
            case Bundle.INSTALLED -> "INSTALLED";
            case Bundle.RESOLVED -> "RESOLVED";
            case Bundle.STARTING -> "STARTING";
            case Bundle.STOPPING -> "STOPPING";
            case Bundle.ACTIVE -> "ACTIVE";
            default -> "UNKNOWN (" + state + ")";
        };
    }

    @Override
    public void refresh() throws Exception {
        refreshGrid();
    }

    @Override
    public void onTabSwitched() throws Exception {
        refreshGrid();
    }

    @Override
    public void onTabClosed() throws Exception {

    }
}