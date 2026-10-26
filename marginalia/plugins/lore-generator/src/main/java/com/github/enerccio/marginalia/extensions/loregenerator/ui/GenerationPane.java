package com.github.enerccio.marginalia.extensions.loregenerator.ui;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;

import static com.github.enerccio.marginalia.extensions.loregenerator.LoreGeneratorLocalization.T;

/**
 * A tab: the reasoning, the content and the button that runs or stops the inference.
 */
public class GenerationPane extends VerticalLayout {

    private final Markdown reasoningMarkdown = new Markdown();
    private final Details reasoningDetails = new Details(T.localized("Thinking Process"));
    private final Markdown contentMarkdown = new Markdown();
    private final Button runButton;
    private final String runLabel;
    private final StringBuilder reasoning = new StringBuilder();
    private final StringBuilder content = new StringBuilder();

    private boolean running;

    /**
     * @param run  starts the inference of this tab
     * @param stop stops the running one
     */
    public GenerationPane(String runLabel, Runnable run, Runnable stop) {
        this.runLabel = runLabel;
        setSizeFull();
        setPadding(false);

        addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_HIDDEN);

        // the reasoning has a box of its own, a long one does not push the content out of sight
        ScrollPanel reasoningScroll = new ScrollPanel(reasoningMarkdown);
        reasoningScroll.setHeight("25vh");
        reasoningDetails.add(reasoningScroll);
        reasoningDetails.setWidthFull();
        reasoningDetails.setVisible(false);
        contentMarkdown.setWidthFull();

        ScrollPanel scroll = new ScrollPanel(contentMarkdown);
        scroll.setSizeFull();
        scroll.addClassName(CssStyles.MIN_HEIGHT_0);

        runButton = new Button(runLabel, e -> {
            if (running) {
                stop.run();
            } else {
                run.run();
            }
        });
        runButton.setThemeName("primary");

        add(reasoningDetails, scroll, runButton);
        setFlexGrow(1, scroll);
        // at the right edge of the dialog, the same place wherever the splitter is
        setAlignSelf(FlexComponent.Alignment.END, runButton);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public String content() {
        return content.toString();
    }

    public boolean hasContent() {
        return StringUtils.isNotBlank(content);
    }

    public void clear() {
        reasoning.setLength(0);
        content.setLength(0);
        reasoningMarkdown.setContent("");
        contentMarkdown.setContent("");
        reasoningDetails.setVisible(false);
    }

    public void append(InferenceService.ChunkType type, String text) {
        if (type == InferenceService.ChunkType.REASONING) {
            reasoning.append(text);
            reasoningMarkdown.setContent(reasoning.toString());
            reasoningDetails.setVisible(true);
        } else {
            content.append(text);
            contentMarkdown.setContent(content.toString());
        }
    }

    /**
     * @param busy an inference runs (in this tab or in the other one): only the running tab can be used, to stop it
     */
    public void setBusy(boolean busy) {
        runButton.setText(running ? T.localized("Stop") : runLabel);
        runButton.setThemeName(running ? "error primary" : "primary");
        runButton.setEnabled(!busy || running);
    }
}
