package com.github.enerccio.marginalia.ui;

import com.github.enerccio.marginalia.domain.model.impl.Gpu;
import com.github.enerccio.marginalia.domain.model.impl.GpuLock;
import com.github.enerccio.marginalia.domain.service.GpuService;
import com.github.enerccio.marginalia.domain.service.gpu.GpuLockChoice;
import com.github.enerccio.marginalia.test.PlatformTestBase;
import com.github.enerccio.marginalia.ui.widgets.GpuLockSelect;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The GPU lock box of an extension's settings: attaching it (the settings page attaches a tab's content when the tab is selected) does not
 * change its value, which the page would count as an unsaved edit, but it still sees a lock added since it was made.
 */
class GpuLockSelectTest extends PlatformTestBase {

    @Autowired
    private GpuService gpuService;

    /** The UI of the test, held strongly (the current UI is a weak reference). */
    private UI ui;

    @BeforeEach
    void setUp() {
        ui = new UI();
        UI.setCurrent(ui);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
        ui = null;
    }

    private GpuLock lock(Gpu gpu) throws Exception {
        GpuLock lock = new GpuLock();
        lock.setName(uniqueName("lock"));
        lock.setGpu(gpu);
        lock.setMaxConcurrent(0);
        lock.setAcquireSteps(List.of());
        lock.setReleaseSteps(List.of());
        return gpuService.saveLock(lock);
    }

    @Test
    void attachingTheBoxDoesNotChangeTheChosenLock() throws Exception {
        loginAdmin();
        Gpu gpu = new Gpu();
        gpu.setName(uniqueName("gpu"));
        gpu = gpuService.saveGpu(gpu);
        GpuLock lock = lock(gpu);

        GpuLockSelect select = new GpuLockSelect();
        select.setLockUuid(lock.getUuid());
        AtomicInteger changes = new AtomicInteger();
        select.addValueChangeListener(e -> changes.incrementAndGet());

        ui.add(select);
        assertThat(changes).hasValue(0);
        assertThat(select.getLockUuid()).isEqualTo(lock.getUuid());

        ui.remove(select);
        GpuLock added = lock(gpu);
        ui.add(select);
        assertThat(select.getLockUuid()).isEqualTo(lock.getUuid());
        assertThat(select.getListDataView().getItems().map(GpuLockChoice::uuid)).contains(added.getUuid());
    }
}
