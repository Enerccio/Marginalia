package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.GpuStep;
import com.github.enerccio.marginalia.domain.service.GpuService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.context.request.RequestAttributes;

import java.util.Objects;

/**
 * A GPU step of the Admin page: the HTTP request (address, method, headers, body), how long it may take, how long to wait after it, and
 * a Test button that makes it now.
 */
@Configurable(preConstruction = true)
public class GpuStepDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private GpuService gpuService;

    private final TextField name = new TextField();
    private final TextField url = new TextField();
    private final ComboBox<String> method = new ComboBox<>();
    private final TextArea headers = new TextArea();
    private final TextArea body = new TextArea();
    private final IntegerField timeout = new IntegerField();
    private final IntegerField wait = new IntegerField();

    public GpuStepDialog(GpuStep step, Runnable onSave) {
        setHeaderTitle(loc.getValue(L.LABEL_GPU_STEPS));
        setWidth("min(720px, 95vw)");

        name.setLabel(loc.getValue(L.LABEL_NAME));
        name.setRequired(true);
        url.setLabel(loc.getValue(L.LABEL_HTTP_URL));
        url.setRequired(true);
        url.setPlaceholder("http://gpu-box:8080/unload");
        method.setLabel(loc.getValue(L.LABEL_HTTP_METHOD));
        method.setItems("POST", "GET", "PUT", "DELETE", "PATCH");
        method.setAllowCustomValue(false);
        headers.setLabel(loc.getValue(L.LABEL_HTTP_HEADERS));
        headers.setHelperText(loc.getValue(L.HELP_GPU_STEP_HEADERS));
        headers.setPlaceholder("Authorization: Bearer ...");
        body.setLabel(loc.getValue(L.LABEL_HTTP_BODY));
        body.setHelperText(loc.getValue(L.HELP_GPU_STEP_VARIABLES));
        body.setPlaceholder("{\"keep\": [\"{{model | strip:openai/}}\"]}");
        timeout.setLabel(loc.getValue(L.LABEL_REQUEST_TIMEOUT));
        timeout.setMin(0);
        timeout.setMax(GpuStep.MAX_TIMEOUT_SECONDS);
        timeout.setPlaceholder(String.valueOf(GpuStep.DEFAULT_TIMEOUT_SECONDS));
        wait.setLabel(loc.getValue(L.LABEL_WAIT_AFTER_SECONDS));
        wait.setMin(0);
        wait.setMax(GpuStep.MAX_WAIT_SECONDS);

        name.setValue(StringUtils.defaultString(step.getName()));
        url.setValue(StringUtils.defaultString(step.getUrl()));
        method.setValue(step.getMethod());
        headers.setValue(StringUtils.defaultString(step.getHeaders()));
        body.setValue(StringUtils.defaultString(step.getBody()));
        timeout.setValue(step.getTimeoutSeconds() > 0 ? step.getTimeoutSeconds() : null);
        wait.setValue(step.getWaitSeconds());

        FormLayout form = new FormLayout(name, method, url, headers, body, timeout, wait);
        form.setColspan(url, 2);
        form.setColspan(headers, 2);
        form.setColspan(body, 2);
        add(form);

        Button test = new Button(loc.getValue(L.LABEL_TEST_STEP), _ -> test());
        Button save = new Button(loc.getValue(L.LABEL_SAVE), _ -> {
            if (!valid()) {
                return;
            }
            try {
                write(step);
                gpuService.saveStep(step);
                close();
                onSave.run();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
        save.setThemeName("primary");
        getFooter().add(test, new Button(loc.getValue(L.LABEL_CANCEL), _ -> close()), save);
        DialogFocus.onOpen(this, name);
        DialogShortcuts.submitWith(save, name, url, headers, body, timeout, wait);
    }

    private boolean valid() {
        name.setInvalid(StringUtils.isBlank(name.getValue()));
        url.setInvalid(StringUtils.isBlank(url.getValue()));
        return !name.isInvalid() && !url.isInvalid();
    }

    private void write(GpuStep step) {
        step.setName(name.getValue().strip());
        step.setUrl(url.getValue().strip());
        step.setMethod(Objects.requireNonNullElse(method.getValue(), "POST"));
        step.setHeaders(StringUtils.isBlank(headers.getValue()) ? null : headers.getValue());
        step.setBody(StringUtils.isBlank(body.getValue()) ? null : body.getValue());
        step.setTimeoutSeconds(Objects.requireNonNullElse(timeout.getValue(), 0));
        step.setWaitSeconds(Objects.requireNonNullElse(wait.getValue(), 0));
    }

    /** Makes the request as it is in the fields now (not saved), on a thread of its own. */
    private void test() {
        if (StringUtils.isBlank(url.getValue())) {
            url.setInvalid(true);
            return;
        }
        GpuStep probe = new GpuStep();
        write(probe);
        if (StringUtils.isBlank(probe.getName())) {
            probe.setName("test");
        }
        UI ui = UI.getCurrent();
        // the service checks the administrator: the thread needs the request of the page
        RequestAttributes request = ThreadCopyRequestAttributes.create();
        Thread.ofVirtual().name("gpu-step-test").start(() -> {
            String error;
            try (ThreadCopyRequestAttributes.InRequestScope _ = new ThreadCopyRequestAttributes.InRequestScope(request)) {
                error = gpuService.testStep(probe);
            } catch (Exception e) {
                error = e.getMessage();
            }
            String result = error;
            ui.access(() -> {
                if (result == null) {
                    Notification.success(loc.getValue(L.MSG_GPU_STEP_OK));
                } else {
                    Notification.error(loc.getValue(L.ERROR_GPU_STEP_FAILED).formatted(result));
                }
                UIPushGuard.push(ui);
            });
        });
    }
}
