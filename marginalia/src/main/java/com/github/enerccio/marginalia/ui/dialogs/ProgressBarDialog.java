package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
@Extendable
public class ProgressBarDialog extends ThreadAccessDialog {
    private static final long THRESHOLD = 5 * 100 * 1000 * 1000;

    @Autowired
    protected Localization loc;

    private Long total;
    private long current;
    private ProgressAction progressAction;
    protected Runnable afterAction;
    private ProgressBar progressBar;
    private Span status;
    private final boolean enableCancel;
    private volatile boolean cancelled;
    private L cancelLabel = L.LABEL_CANCEL;
    protected boolean manualCancel;
    protected VerticalLayout contentContainer;
    private long lastUpdateTime;

    public ProgressBarDialog(boolean enablePush, boolean enableCancel) {
        super("progressbarThread", enablePush);
        this.enableCancel = enableCancel;
    }

    public ProgressBarDialog(boolean enablePush) {
        this(enablePush, false);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "vl", type = VerticalLayout.class),
                    @APILocals(name = "cancel", type = Button.class)})
    public void create() {
        setWidth("450px");
        setHeight("160px");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.STRICT);

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();

        contentContainer = new VerticalLayout();
        contentContainer.setSizeFull();
        vl.add(contentContainer);

        progressBar = new ProgressBar();
        progressBar.setWidth("100%");
        contentContainer.add(progressBar);

        if (isDefined()) {
            progressBar.setIndeterminate(false);
            progressBar.setMin(0.0);
            progressBar.setMax(1.0);

            status = new Span("");
            contentContainer.add(status);
        } else {
            progressBar.setIndeterminate(true);
        }

        if (enableCancel) {
            setHeight("200px");
            Button cancel = new Button(loc.getValue(cancelLabel));
            cancel.addClickListener(event -> cancelled = true);
            vl.add(cancel);
        }

        add(vl);
    }

    /**
     * The text of the button that cancels (stops) the action, to set before {@link #create()}; cancel by default.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "cancelLabel", type = L.class)})
    public void setCancelLabel(L cancelLabel) {
        this.cancelLabel = cancelLabel;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = boolean.class))
    public boolean isCancelled() {
        return cancelled;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "progressAction", type = ProgressBarDialog.ProgressAction.class)})
    public void setAction(ProgressAction progressAction) {
        this.progressAction = progressAction;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "afterAction", type = Runnable.class)})
    public void setAfterAction(Runnable afterAction) {
        this.afterAction = afterAction;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "title", type = String.class)})
    public void setTitle(String title) {
        super.setHeaderTitle(title);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "total", type = Long.class)})
    public void setTotal(Long total) {
        this.total = total;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = boolean.class))
    protected boolean isDefined() {
        return total != null;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    public void updateProgress() {
        updateProgressMultiple(1);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "delta", type = long.class)})
    public void updateProgressMultiple(long delta) {
        if (isDefined()) {
            current += delta;
            double progress = ((1.0) / total) * current;

            long ctime = System.nanoTime();
            if (ctime - lastUpdateTime > THRESHOLD) {
                lastUpdateTime = ctime;
                vaadinLocked(() -> {
                    progressBar.setValue(Math.min(1.0, Math.max(progress, 0.0)));
                    status.setText(String.format("%s / %s", current, total));
                });
            }
        }
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void open() {
        super.open();
        run();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    @Override
    protected void runInThread() {
        lastUpdateTime = System.nanoTime();
        if (progressAction != null) {
            progressAction.run(this);
        }
        if (!manualCancel) {
            vaadinLockedSync(() -> {
                if (afterAction != null) {
                    afterAction.run();
                }
                close();
            });
        }
    }

    public interface ProgressAction {

        void run(ProgressBarDialog dialog);

    }

    public static class CancelProgressException extends RuntimeException {

    }
}
