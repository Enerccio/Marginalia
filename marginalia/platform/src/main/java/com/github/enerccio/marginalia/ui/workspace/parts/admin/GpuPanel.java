package com.github.enerccio.marginalia.ui.workspace.parts.admin;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Gpu;
import com.github.enerccio.marginalia.domain.model.impl.GpuLock;
import com.github.enerccio.marginalia.domain.model.impl.GpuStep;
import com.github.enerccio.marginalia.domain.service.GpuService;
import com.github.enerccio.marginalia.domain.service.gpu.GpuStatus;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.dialogs.*;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.context.request.RequestAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The GPUs of the installation on the Admin page (see {@link GpuService}): the GPUs with what they are loaded with and an Unload button,
 * the locks, and the steps the locks run.
 */
@Configurable
public class GpuPanel implements LLMInteract {

    @Autowired
    private Localization loc;

    @Autowired
    private GpuService gpuService;

    private final Grid<Gpu> gpuGrid = new Grid<>(Gpu.class, false);
    private final Grid<GpuLock> lockGrid = new Grid<>(GpuLock.class, false);
    private final Grid<GpuStep> stepGrid = new Grid<>(GpuStep.class, false);
    private Map<String, GpuStatus> statuses = Map.of();

    public Component create() throws Exception {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        Span description = new Span(loc.getValue(L.HELP_GPUS));
        description.addClassName(CssStyles.TEXT_SECONDARY);

        Button refresh = new Button(loc.getValue(L.LABEL_REFRESH), Solid.ARROWS_ROTATE.create(), _ -> refresh());

        // --- GPUs ---
        gpuGrid.addColumn(Gpu::getName).setHeader(loc.getValue(L.LABEL_NAME)).setFlexGrow(2);
        gpuGrid.addColumn(gpu -> {
            GpuStatus status = statuses.get(gpu.getUuid());
            return status == null || status.loadedLock() == null ? loc.getValue(L.LABEL_NOTHING_LOADED) : status.loadedLock();
        }).setHeader(loc.getValue(L.LABEL_LOADED_LOCK)).setFlexGrow(2);
        gpuGrid.addColumn(gpu -> {
            GpuStatus status = statuses.get(gpu.getUuid());
            return status == null ? "0 / 0" : status.active() + " / " + status.waiting();
        }).setHeader(loc.getValue(L.LABEL_GPU_JOBS)).setFlexGrow(1);
        gpuGrid.addComponentColumn(gpu -> {
            Button unload = new Button(loc.getValue(L.LABEL_UNLOAD_NOW), _ -> unload(gpu));
            unload.setThemeName("small");
            Button edit = new Button(Solid.PEN.create(), _ -> new GpuDialog(gpu, this::refresh).open());
            edit.setThemeName("tertiary icon small");
            edit.setTooltipText(loc.getValue(L.LABEL_EDIT));
            Button delete = new Button(Solid.TRASH.create(), _ -> ConfirmDialog.show(loc.getValue(L.MSG_DELETE_GPU).formatted(gpu.getName()), () -> {
                try {
                    gpuService.deleteGpu(gpu);
                    refresh();
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            }));
            delete.setThemeName("tertiary icon small");
            delete.setTooltipText(loc.getValue(L.LABEL_DELETE));
            return new HorizontalLayout(unload, edit, delete);
        }).setAutoWidth(true).setFlexGrow(0);
        gpuGrid.setAllRowsVisible(true);
        Button addGpu = new Button(loc.getValue(L.LABEL_ADD_GPU), Solid.PLUS_CIRCLE.create(), _ -> new GpuDialog(new Gpu(), this::refresh).open());

        // --- locks ---
        lockGrid.addColumn(GpuLock::getName).setHeader(loc.getValue(L.LABEL_NAME)).setFlexGrow(2);
        lockGrid.addColumn(lock -> lock.getGpu() == null ? "" : lock.getGpu().getName()).setHeader(loc.getValue(L.LABEL_GPU)).setFlexGrow(1);
        lockGrid.addColumn(lock -> lock.getMaxConcurrent() == 0 ? "∞" : String.valueOf(lock.getMaxConcurrent()))
                .setHeader(loc.getValue(L.LABEL_MAX_CONCURRENT_JOBS)).setFlexGrow(1);
        lockGrid.addColumn(lock -> names(lock.getAlwaysSteps())).setHeader(loc.getValue(L.LABEL_ALWAYS_STEPS)).setFlexGrow(2);
        lockGrid.addColumn(lock -> names(lock.getAcquireSteps())).setHeader(loc.getValue(L.LABEL_ACQUIRE_STEPS)).setFlexGrow(2);
        lockGrid.addColumn(lock -> names(lock.getReleaseSteps())).setHeader(loc.getValue(L.LABEL_RELEASE_STEPS)).setFlexGrow(2);
        lockGrid.addComponentColumn(lock -> {
            Button edit = new Button(Solid.PEN.create(), _ -> editLock(lock));
            edit.setThemeName("tertiary icon small");
            edit.setTooltipText(loc.getValue(L.LABEL_EDIT));
            Button delete = new Button(Solid.TRASH.create(), _ -> ConfirmDialog.show(loc.getValue(L.MSG_DELETE_GPU_LOCK).formatted(lock.getName()), () -> {
                try {
                    gpuService.deleteLock(lock);
                    refresh();
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            }));
            delete.setThemeName("tertiary icon small");
            delete.setTooltipText(loc.getValue(L.LABEL_DELETE));
            return new HorizontalLayout(edit, delete);
        }).setAutoWidth(true).setFlexGrow(0);
        lockGrid.setAllRowsVisible(true);
        Button addLock = new Button(loc.getValue(L.LABEL_ADD_GPU_LOCK), Solid.PLUS_CIRCLE.create(), _ -> editLock(new GpuLock()));

        // --- steps ---
        stepGrid.addColumn(GpuStep::getName).setHeader(loc.getValue(L.LABEL_NAME)).setFlexGrow(1);
        stepGrid.addColumn(GpuStep::getMethod).setHeader(loc.getValue(L.LABEL_HTTP_METHOD)).setAutoWidth(true).setFlexGrow(0);
        stepGrid.addColumn(GpuStep::getUrl).setHeader(loc.getValue(L.LABEL_HTTP_URL)).setFlexGrow(3);
        stepGrid.addComponentColumn(step -> {
            Button edit = new Button(Solid.PEN.create(), _ -> new GpuStepDialog(step, this::refresh).open());
            edit.setThemeName("tertiary icon small");
            edit.setTooltipText(loc.getValue(L.LABEL_EDIT));
            Button delete = new Button(Solid.TRASH.create(), _ -> ConfirmDialog.show(loc.getValue(L.MSG_DELETE_GPU_STEP).formatted(step.getName()), () -> {
                try {
                    gpuService.deleteStep(step);
                    refresh();
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            }));
            delete.setThemeName("tertiary icon small");
            delete.setTooltipText(loc.getValue(L.LABEL_DELETE));
            return new HorizontalLayout(edit, delete);
        }).setAutoWidth(true).setFlexGrow(0);
        stepGrid.setAllRowsVisible(true);
        Button addStep = new Button(loc.getValue(L.LABEL_ADD_GPU_STEP), Solid.PLUS_CIRCLE.create(), _ -> new GpuStepDialog(new GpuStep(), this::refresh).open());

        FlexLayout top = new FlexLayout(refresh);
        top.addClassNames(CssStyles.FLEX_WRAP, CssStyles.GAP_S);
        layout.add(description, top,
                new H4(loc.getValue(L.LABEL_GPUS)), addGpu, gpuGrid,
                new H4(loc.getValue(L.LABEL_GPU_LOCKS)), addLock, lockGrid,
                new H4(loc.getValue(L.LABEL_GPU_STEPS)), addStep, stepGrid);
        refresh();
        return layout;
    }

    private static String names(List<GpuStep> steps) {
        return steps == null ? "" : steps.stream().map(GpuStep::getName).collect(Collectors.joining(", "));
    }

    private void editLock(GpuLock lock) {
        try {
            new GpuLockDialog(lock, gpuService.findGpus(), gpuService.findSteps(), this::refresh).open();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /** Reads the GPUs, the locks, the steps and what the GPUs do now. */
    public void refresh() {
        try {
            Map<String, GpuStatus> read = new HashMap<>();
            for (GpuStatus status : gpuService.statuses()) {
                read.put(status.gpuUuid(), status);
            }
            statuses = read;
            gpuGrid.setItems(gpuService.findGpus());
            lockGrid.setItems(gpuService.findLocks());
            stepGrid.setItems(gpuService.findSteps());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void unload(Gpu gpu) {
        UI ui = UI.getCurrent();
        // the refresh at the end reads the GPUs as the administrator: the thread of the steps needs the request of the page
        RequestAttributes request = ThreadCopyRequestAttributes.create();
        try {
            gpuService.unload(gpu).whenComplete((error, failure) -> {
                try {
                    ui.access(() -> {
                        try (ThreadCopyRequestAttributes.InRequestScope _ = new ThreadCopyRequestAttributes.InRequestScope(request)) {
                        String message = failure != null ? failure.getMessage() : error;
                        if (message == null) {
                            Notification.success(loc.getValue(L.MSG_GPU_UNLOADED));
                        } else {
                            Notification.error(loc.getValue(L.ERROR_GPU_UNLOAD_FAILED).formatted(message));
                        }
                        refresh();
                        }
                        UIPushGuard.push(ui);
                    });
                } catch (UIDetachedException e) {
                    // the page is gone
                }
            });
            refresh();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @Override
    public String getLLMState() {
        return "The GPUs tab: the GPUs shared by the models and the image and speech servers (with the lock each is loaded with and an"
                + " Unload now button), the GPU locks (how a GPU is loaded and freed) and the steps (HTTP requests) the locks run.";
    }
}
