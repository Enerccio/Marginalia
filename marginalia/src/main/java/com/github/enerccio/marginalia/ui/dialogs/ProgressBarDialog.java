package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
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
    private boolean cancelled;
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

    public void create() {
        setWidth("450px");
        setHeight("150px");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

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
            Button cancel = new Button(loc.getValue(L.LABEL_CANCEL));
            cancel.addClickListener(event -> cancelled = true);
            vl.add(cancel);
        }

        add(vl);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setAction(ProgressAction progressAction) {
        this.progressAction = progressAction;
    }

    public void setAfterAction(Runnable afterAction) {
        this.afterAction = afterAction;
    }

    public void setTitle(String title) {
        super.setHeaderTitle(title);
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    protected boolean isDefined() {
        return total != null;
    }

    public void updateProgress() {
        updateProgressMultiple(1);
    }

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

    @Override
    public void open() {
        super.open();
        run();
    }

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
