package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.github.enerccio.marginalia.extensions.audiobook.queue.LabelTrace;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;

import java.util.List;
import java.util.stream.IntStream;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * What the language model said while it labelled a part: for each request (a window of the pieces) its reasoning and its answer, as they arrive
 * while the labelling runs. The text is the one the job holds in memory ({@link LabelTrace}); it is gone when the finished jobs are cleared.
 */
public class LabelTraceDialog extends Dialog {

    private final LabelTrace trace;
    private final Select<Integer> windows = new Select<>();
    private final TextArea reasoning = new TextArea(T.localized(REASONING));
    private final TextArea output = new TextArea(T.localized(THE_ANSWER_OF_THE_MODEL));
    private final Runnable listener = () -> UIUtils.accessComponent(this, this::refresh);
    /** The window that is shown follows the last one until the user picks another. */
    private boolean followLatest = true;
    private int known;

    public LabelTraceDialog(String part, LabelTrace trace) {
        this.trace = trace;
        setHeaderTitle(T.localized(WHAT_THE_MODEL_SAID).formatted(part));
        setWidth("min(900px, 95vw)");
        setMaxHeight("92vh");

        windows.setLabel(T.localized(REQUEST));
        windows.setWidth("22em");
        windows.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                followLatest = e.getValue() != null && e.getValue() == known - 1;
            }
            show();
        });
        for (TextArea area : List.of(reasoning, output)) {
            area.setReadOnly(true);
            area.setWidthFull();
            area.setHeight("14em");
        }
        VerticalLayout content = new VerticalLayout(windows, reasoning, output);
        content.setPadding(false);
        add(content);
        getFooter().add(new Button(T.localized(CLOSE), e -> close()));
        refresh();
    }

    private void refresh() {
        List<LabelTrace.Window> all = trace.windows();
        if (all.size() != known) {
            known = all.size();
            Integer selected = windows.getValue();
            windows.setItems(IntStream.range(0, all.size()).boxed().toList());
            windows.setItemLabelGenerator(i -> (i + 1) + ": " + all.get(Math.min(i, all.size() - 1)).title());
            windows.setValue(followLatest || selected == null ? all.size() - 1 : selected);
        }
        show();
    }

    private void show() {
        List<LabelTrace.Window> all = trace.windows();
        Integer index = windows.getValue();
        if (index == null || index >= all.size()) {
            reasoning.clear();
            output.clear();
            return;
        }
        LabelTrace.Window window = all.get(index);
        reasoning.setValue(window.reasoning());
        output.setValue(window.output());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        trace.addListener(listener);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        trace.removeListener(listener);
        super.onDetach(detachEvent);
    }
}
