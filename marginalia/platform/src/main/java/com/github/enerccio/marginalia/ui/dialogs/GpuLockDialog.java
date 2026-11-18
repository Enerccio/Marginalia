package com.github.enerccio.marginalia.ui.dialogs;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Gpu;
import com.github.enerccio.marginalia.domain.model.impl.GpuLock;
import com.github.enerccio.marginalia.domain.model.impl.GpuStep;
import com.github.enerccio.marginalia.domain.service.GpuService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A GPU lock of the Admin page: its name, its GPU, how many of its jobs run at once, and the two ordered lists of steps (the steps are
 * shared: the list takes them from the steps of the installation).
 */
@Configurable(preConstruction = true)
public class GpuLockDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private GpuService gpuService;

    public GpuLockDialog(GpuLock lock, List<Gpu> gpus, List<GpuStep> steps, Runnable onSave) {
        setHeaderTitle(loc.getValue(L.LABEL_GPU_LOCK));
        setWidth("min(720px, 95vw)");

        TextField name = new TextField(loc.getValue(L.LABEL_NAME));
        name.setRequired(true);
        name.setValue(StringUtils.defaultString(lock.getName()));

        ComboBox<Gpu> gpu = new ComboBox<>(loc.getValue(L.LABEL_GPU));
        gpu.setRequired(true);
        gpu.setItems(gpus.stream().sorted(java.util.Comparator.comparing(Gpu::getName, String.CASE_INSENSITIVE_ORDER)).toList());
        gpu.setItemLabelGenerator(Gpu::getName);
        gpu.setValue(lock.getGpu() == null ? (gpus.size() == 1 ? gpus.getFirst() : null)
                : gpus.stream().filter(g -> Objects.equals(g.getId(), lock.getGpu().getId())).findFirst().orElse(null));

        IntegerField maxConcurrent = new IntegerField(loc.getValue(L.LABEL_MAX_CONCURRENT_JOBS));
        maxConcurrent.setMin(0);
        maxConcurrent.setStepButtonsVisible(true);
        maxConcurrent.setHelperText(loc.getValue(L.HELP_GPU_LOCK_MAX_CONCURRENT));
        maxConcurrent.setValue(lock.getMaxConcurrent());

        StepList acquire = new StepList(loc.getValue(L.LABEL_ACQUIRE_STEPS), loc.getValue(L.HELP_GPU_ACQUIRE_STEPS), steps, lock.getAcquireSteps());
        StepList release = new StepList(loc.getValue(L.LABEL_RELEASE_STEPS), loc.getValue(L.HELP_GPU_RELEASE_STEPS), steps, lock.getReleaseSteps());

        FormLayout form = new FormLayout(name, gpu, maxConcurrent);
        add(form, acquire, release);

        Button save = new Button(loc.getValue(L.LABEL_SAVE), _ -> {
            name.setInvalid(StringUtils.isBlank(name.getValue()));
            gpu.setInvalid(gpu.getValue() == null);
            if (name.isInvalid() || gpu.isInvalid()) {
                return;
            }
            try {
                lock.setName(name.getValue().strip());
                lock.setGpu(gpu.getValue());
                lock.setMaxConcurrent(Objects.requireNonNullElse(maxConcurrent.getValue(), 0));
                lock.setAcquireSteps(new ArrayList<>(acquire.chosen));
                lock.setReleaseSteps(new ArrayList<>(release.chosen));
                gpuService.saveLock(lock);
                close();
                onSave.run();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
        save.setThemeName("primary");
        getFooter().add(new Button(loc.getValue(L.LABEL_CANCEL), _ -> close()), save);
        DialogFocus.onOpen(this, name);
        DialogShortcuts.submitWith(save, name, maxConcurrent);
    }

    /** An ordered list of steps: add one from the steps of the installation, move it, remove it. */
    private final class StepList extends VerticalLayout {

        private final List<GpuStep> chosen = new ArrayList<>();
        private final VerticalLayout rows = new VerticalLayout();

        StepList(String label, String help, List<GpuStep> all, List<GpuStep> current) {
            setPadding(false);
            setSpacing(false);
            Span title = new Span(label);
            Span helper = new Span(help);
            helper.addClassName(CssStyles.TEXT_SECONDARY);
            // the steps as the installation has them now (the lock may hold copies read earlier)
            for (GpuStep step : current) {
                all.stream().filter(s -> Objects.equals(s.getId(), step.getId())).findFirst().ifPresent(chosen::add);
            }
            rows.setPadding(false);
            rows.setSpacing(false);

            ComboBox<GpuStep> pick = new ComboBox<>();
            // by name; the lists of the lock keep their order: it is the order the steps run in
            pick.setItems(all.stream().sorted(java.util.Comparator.comparing(GpuStep::getName, String.CASE_INSENSITIVE_ORDER)).toList());
            pick.setItemLabelGenerator(GpuStep::getName);
            pick.setPlaceholder(loc.getValue(L.LABEL_ADD_GPU_STEP));
            pick.addValueChangeListener(event -> {
                if (event.getValue() != null) {
                    chosen.add(event.getValue());
                    pick.clear();
                    refresh();
                }
            });
            add(title, helper, rows, pick);
            refresh();
        }

        private void refresh() {
            rows.removeAll();
            for (int i = 0; i < chosen.size(); i++) {
                int index = i;
                Span name = new Span((i + 1) + ". " + chosen.get(i).getName());
                Button up = new Button(Solid.ARROW_UP.create(), _ -> move(index, -1));
                up.setTooltipText(loc.getValue(L.LABEL_MOVE_UP));
                up.setEnabled(i > 0);
                Button down = new Button(Solid.ARROW_DOWN.create(), _ -> move(index, 1));
                down.setTooltipText(loc.getValue(L.LABEL_MOVE_DOWN));
                down.setEnabled(i < chosen.size() - 1);
                Button remove = new Button(Solid.XMARK.create(), _ -> {
                    chosen.remove(index);
                    refresh();
                });
                remove.setTooltipText(loc.getValue(L.LABEL_REMOVE));
                for (Button button : List.of(up, down, remove)) {
                    button.setThemeName("tertiary icon small");
                }
                HorizontalLayout row = new HorizontalLayout(name, up, down, remove);
                row.setAlignItems(FlexComponent.Alignment.CENTER);
                rows.add(row);
            }
        }

        private void move(int index, int by) {
            int other = index + by;
            if (other >= 0 && other < chosen.size()) {
                Collections.swap(chosen, index, other);
                refresh();
            }
        }
    }
}
