package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.Gpu;
import com.github.enerccio.marginalia.domain.service.GpuService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/** A GPU of the Admin page: its name. */
@Configurable(preConstruction = true)
public class GpuDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private GpuService gpuService;

    public GpuDialog(Gpu gpu, Runnable onSave) {
        setHeaderTitle(loc.getValue(L.LABEL_GPU));
        setWidth("min(420px, 95vw)");
        TextField name = new TextField(loc.getValue(L.LABEL_NAME));
        name.setWidthFull();
        name.setRequired(true);
        name.setValue(StringUtils.defaultString(gpu.getName()));
        add(name);

        Button save = new Button(loc.getValue(L.LABEL_SAVE), _ -> {
            if (StringUtils.isBlank(name.getValue())) {
                name.setInvalid(true);
                return;
            }
            try {
                gpu.setName(name.getValue().strip());
                gpuService.saveGpu(gpu);
                close();
                onSave.run();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
        save.setThemeName("primary");
        getFooter().add(new Button(loc.getValue(L.LABEL_CANCEL), _ -> close()), save);
        DialogFocus.onOpen(this, name);
        DialogShortcuts.submitWith(save, name);
    }
}
