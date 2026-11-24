package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.domain.service.GpuService;
import com.github.enerccio.marginalia.domain.service.gpu.GpuLockChoice;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.combobox.ComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.Objects;

/**
 * The GPU lock an AI or a backend of an extension takes for its jobs (see {@code GpuService}); empty for none. Holds the uuid of the lock,
 * which is what the AI or the backend stores. Hidden when the administrator set up no lock (and none is chosen), so installations without
 * a shared GPU see nothing of it. Extensions use it as it is ({@code new GpuLockSelect()}, {@link #setLockUuid}, {@link #getLockUuid}).
 */
@Configurable(preConstruction = true)
public class GpuLockSelect extends ComboBox<GpuLockChoice> {

    private static final Logger log = LoggerFactory.getLogger(GpuLockSelect.class);

    @Autowired
    private Localization loc;

    @Autowired
    private GpuService gpuService;

    private List<GpuLockChoice> choices = List.of();

    public GpuLockSelect() {
        if (loc != null) {
            // none outside the application (the tests of an extension)
            setLabel(loc.getValue(L.LABEL_GPU_LOCK));
            setPlaceholder(loc.getValue(L.LABEL_NO_GPU_LOCK));
        }
        setClearButtonVisible(true);
        setAllowCustomValue(false);
        setItemLabelGenerator(GpuLockChoice::label);
        setWidthFull();
        reload();
        // a form built once per page (the settings of an extension) sees the locks the administrator added since; only when they did change:
        // setItems resets the value, and the value changes it fires would count as an edit of the settings (the settings page attaches a
        // tab's content only when the tab is selected)
        addAttachListener(e -> {
            if (readChoices().equals(choices)) {
                return;
            }
            String chosen = getLockUuid();
            reload();
            select(chosen);
        });
    }

    private List<GpuLockChoice> readChoices() {
        try {
            return gpuService == null ? List.of() : gpuService.lockChoices();
        } catch (Exception e) {
            log.warn("The GPU locks could not be read: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /** Reads the locks again (they may have been added or deleted since the box was made); the box is shown when there is one. */
    public void reload() {
        choices = readChoices();
        setItems(choices);
        setVisible(!choices.isEmpty());
    }

    /** The uuid of the chosen lock, null for none. */
    public String getLockUuid() {
        GpuLockChoice value = getValue();
        return value == null ? null : value.uuid();
    }

    /** Reads the locks again and chooses the lock of the uuid; a uuid of a lock that is gone (or null) chooses none. */
    public void setLockUuid(String uuid) {
        reload();
        select(uuid);
    }

    private void select(String uuid) {
        setValue(uuid == null ? null : choices.stream().filter(c -> Objects.equals(c.uuid(), uuid)).findFirst().orElse(null));
    }
}
