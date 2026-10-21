package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentRunner;
import com.github.enerccio.marginalia.domain.service.agent.AgentDiagnostics;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InputStreamDownloadCallback;
import com.vaadin.flow.server.streams.InputStreamDownloadHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * What the run the console shows did, counted from its transcript (see {@link AgentDiagnostics}): the report as read text, to copy or
 * to download as Markdown or JSON. It is made when the tab is shown and when <b>Refresh</b> is pressed, not while the run works.
 */
@Configurable
public class AgentDiagnosticsView extends VerticalLayout implements LLMInteract {

    @Autowired
    private Localization loc;

    @Autowired
    private AgentRunner agentRunner;

    private final Supplier<AgentRun> runSupplier;

    private Markdown report;
    private Button copyButton;
    private Button downloadButton;
    private Button downloadJsonButton;
    private Span emptyLabel;
    // what is shown, null until the first refresh and for a run that does not exist
    private AgentDiagnostics.Report diagnostics;

    /**
     * @param runSupplier the run the console looks at now, null when there is none
     */
    public AgentDiagnosticsView(Supplier<AgentRun> runSupplier) {
        this.runSupplier = runSupplier;
    }

    public void create() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassNames(CssStyles.OVERFLOW_AUTO, CssStyles.GAP_S);

        Span help = new Span(loc.getValue(L.HELP_AGENT_DIAGNOSTICS));
        help.addClassName(CssStyles.HINT_TEXT);

        Button refreshButton = new Button(loc.getValue(L.LABEL_REFRESH), Solid.ARROWS_ROTATE.create(), event -> refresh());
        copyButton = new Button(loc.getValue(L.LABEL_COPY), Solid.COPY.create(), event -> copy());
        downloadButton = new Button(loc.getValue(L.LABEL_DOWNLOAD), Solid.DOWNLOAD.create());
        downloadJsonButton = new Button(loc.getValue(L.LABEL_DOWNLOAD_JSON), Solid.DOWNLOAD.create());
        HorizontalLayout header = new HorizontalLayout(refreshButton, copyButton,
                downloadAnchor(downloadButton, AgentDiagnostics.Report::toMarkdown, "agent-diagnostics.md", "text/markdown"),
                downloadAnchor(downloadJsonButton, AgentDiagnostics.Report::toJson, "agent-diagnostics.json", "application/json"));
        header.setWidthFull();
        header.setPadding(false);
        header.setWrap(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        emptyLabel = new Span(loc.getValue(L.MSG_AGENT_NO_RUN_YET));
        emptyLabel.addClassName(CssStyles.HINT_TEXT);

        report = new Markdown();
        report.setWidthFull();
        report.addClassName(CssStyles.AGENT_REPORT);

        add(help, header, emptyLabel, report);
        showEmpty();
    }

    /**
     * Counts the run again and shows the report.
     */
    public void refresh() {
        if (report == null) {
            return;
        }
        AgentRun run = runSupplier.get();
        if (run == null) {
            diagnostics = null;
            showEmpty();
            return;
        }
        try {
            diagnostics = agentRunner.diagnostics(run);
        } catch (IllegalArgumentException | IllegalStateException e) {
            diagnostics = null;
            showEmpty();
            Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
            return;
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }
        emptyLabel.setVisible(false);
        report.setContent(diagnostics.toMarkdown());
        copyButton.setEnabled(true);
        downloadButton.setEnabled(true);
        downloadJsonButton.setEnabled(true);
    }

    private void showEmpty() {
        emptyLabel.setVisible(true);
        report.setContent("");
        copyButton.setEnabled(false);
        downloadButton.setEnabled(false);
        downloadJsonButton.setEnabled(false);
    }

    private void copy() {
        if (diagnostics == null) {
            return;
        }
        JavascriptLibrary.copyToClipboard(UI.getCurrent().getPage(), diagnostics.toMarkdown());
        Notification.success(loc.getValue(L.MSG_COPIED_TO_CLIPBOARD));
    }

    private Anchor downloadAnchor(Button button, Function<AgentDiagnostics.Report, String> format, String fileName, String mimeType) {
        Anchor anchor = new Anchor(new InputStreamDownloadHandler((InputStreamDownloadCallback) downloadEvent -> {
            if (diagnostics == null) {
                throw new IOException("There are no diagnostics to download");
            }
            byte[] content = format.apply(diagnostics).getBytes(StandardCharsets.UTF_8);
            return new DownloadResponse(new ByteArrayInputStream(content), fileName, mimeType, content.length);
        }), "");
        anchor.getElement().setAttribute("download", true);
        anchor.add(button);
        return anchor;
    }

    // for the tests

    AgentDiagnostics.Report getDiagnostics() {
        return diagnostics;
    }

    Button getCopyButton() {
        return copyButton;
    }
}
