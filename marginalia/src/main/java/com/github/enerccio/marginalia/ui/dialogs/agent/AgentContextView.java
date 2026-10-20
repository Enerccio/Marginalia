package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentRunner;
import com.github.enerccio.marginalia.domain.service.agent.AgentContext;
import com.github.enerccio.marginalia.domain.service.agent.AgentContext.Kind;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.widgets.ChartJs;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.Gson;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InputStreamDownloadCallback;
import com.vaadin.flow.server.streams.InputStreamDownloadHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

/**
 * The context of the run the console shows, taken apart: what the model reads with its next request as charts (the kinds of
 * parts against the cap, every part in the order it is sent, the size the model reported at each answer), a table of the parts
 * and the exact text of the one picked, and the whole of it as a Markdown file to download or copy, for debugging an agent. It is
 * made when the tab is shown and when <b>Refresh</b> is pressed, not while the run works: building it counts the tokens of
 * every part.
 */
@Configurable
public class AgentContextView extends VerticalLayout implements LLMInteract {

    private static final Gson GSON = new Gson();

    private static final String COLOR_FREE = "#9aa0a6";
    private static final Map<Kind, String> COLORS = Map.of(
            Kind.SYSTEM_PROMPT, "#4e79a7", Kind.INSTRUCTIONS, "#76b7b2", Kind.NOTE, "#b07aa1", Kind.TOOL, "#edc948",
            Kind.SUMMARY, "#9c755f", Kind.USER, "#f28e2b", Kind.ASSISTANT, "#59a14f", Kind.RESULT, "#e15759");

    @Autowired
    private Localization loc;

    @Autowired
    private AgentRunner agentRunner;

    private final Supplier<AgentRun> runSupplier;

    private Span sizeLabel;
    private VerticalLayout charts;
    private Grid<AgentContext.Part> grid;
    private TextArea viewer;
    private Button copyButton;
    private Button downloadButton;
    // what is shown, null until the first refresh and for a run that does not exist
    private AgentContext context;
    // the place of each part in the context, for the table
    private final Map<AgentContext.Part, Integer> numbers = new IdentityHashMap<>();
    private Span emptyLabel;

    /**
     * @param runSupplier the run the console looks at now, null when there is none
     */
    public AgentContextView(Supplier<AgentRun> runSupplier) {
        this.runSupplier = runSupplier;
    }

    public void create() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassNames(CssStyles.OVERFLOW_AUTO, CssStyles.GAP_S);

        Span help = new Span(loc.getValue(L.HELP_AGENT_CONTEXT));
        help.addClassName(CssStyles.HINT_TEXT);

        sizeLabel = new Span();
        sizeLabel.addClassName(CssStyles.TEXT_SEMIBOLD);
        Button refreshButton = new Button(loc.getValue(L.LABEL_REFRESH), Solid.ARROWS_ROTATE.create(), event -> refresh());
        copyButton = new Button(loc.getValue(L.LABEL_COPY), Solid.COPY.create(), event -> copy());
        downloadButton = new Button(loc.getValue(L.LABEL_DOWNLOAD), Solid.DOWNLOAD.create());
        Anchor download = downloadAnchor(downloadButton);
        HorizontalLayout header = new HorizontalLayout(sizeLabel, refreshButton, copyButton, download);
        header.setWidthFull();
        header.setPadding(false);
        header.setWrap(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        emptyLabel = new Span(loc.getValue(L.MSG_AGENT_NO_RUN_YET));
        emptyLabel.addClassName(CssStyles.HINT_TEXT);

        charts = new VerticalLayout();
        charts.setPadding(false);
        charts.setSpacing(false);
        charts.setWidthFull();

        grid = new Grid<>();
        grid.setWidthFull();
        grid.setHeight("320px");
        grid.addColumn(part -> numbers.get(part)).setHeader("#").setFlexGrow(0).setWidth("70px");
        grid.addColumn(part -> kindName(part.kind())).setHeader(loc.getValue(L.LABEL_TYPE)).setFlexGrow(1);
        grid.addColumn(AgentContext.Part::title).setHeader(loc.getValue(L.LABEL_NAME)).setFlexGrow(3);
        grid.addColumn(AgentContext.Part::tokens).setHeader(loc.getValue(L.LABEL_TOKENS)).setFlexGrow(1);
        grid.addColumn(this::share).setHeader(loc.getValue(L.LABEL_AGENT_CONTEXT_SHARE)).setFlexGrow(1);
        grid.addSelectionListener(event -> show(event.getFirstSelectedItem().orElse(null)));

        viewer = new TextArea();
        viewer.setReadOnly(true);
        viewer.setWidthFull();
        viewer.setMinHeight("320px");
        viewer.setMaxHeight("600px");
        viewer.addClassName(CssStyles.TEXT_MONOSPACE_LUMO);
        viewer.setPlaceholder(loc.getValue(L.MSG_AGENT_CONTEXT_PICK));

        add(help, header, emptyLabel, charts, new H4(loc.getValue(L.LABEL_AGENT_CONTEXT_PARTS)), grid, viewer);
        showEmpty();
    }

    /**
     * Makes the context of the run again and shows it.
     */
    public void refresh() {
        if (sizeLabel == null) {
            return;
        }
        AgentRun run = runSupplier.get();
        if (run == null) {
            context = null;
            showEmpty();
            return;
        }
        try {
            context = agentRunner.context(run);
        } catch (IllegalArgumentException | IllegalStateException e) {
            context = null;
            showEmpty();
            Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
            return;
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }
        showContext();
    }

    private void showEmpty() {
        emptyLabel.setVisible(true);
        sizeLabel.setText("");
        charts.removeAll();
        grid.setItems(List.of());
        viewer.clear();
        copyButton.setEnabled(false);
        downloadButton.setEnabled(false);
    }

    private void showContext() {
        emptyLabel.setVisible(false);
        long estimated = context.estimatedTokens();
        StringBuilder size = new StringBuilder(context.contextCap() > 0
                ? String.format(loc.getValue(L.MSG_AGENT_CONTEXT_SIZE), estimated, context.contextCap())
                : String.format(loc.getValue(L.MSG_AGENT_CONTEXT_SIZE_NO_CAP), estimated));
        if (context.reportedTokens() > 0) {
            size.append(". ").append(String.format(loc.getValue(L.MSG_AGENT_CONTEXT_REPORTED), context.reportedTokens()));
        }
        sizeLabel.setText(size.toString());
        copyButton.setEnabled(true);
        downloadButton.setEnabled(true);

        charts.removeAll();
        charts.add(kindsChart(), partsChart());
        if (!context.history().isEmpty()) {
            charts.add(new H4(loc.getValue(L.LABEL_AGENT_CONTEXT_HISTORY)), historyChart());
        }
        numbers.clear();
        context.parts().forEach(part -> numbers.put(part, numbers.size() + 1));
        grid.setItems(context.parts());
        viewer.clear();
    }

    private void show(AgentContext.Part part) {
        viewer.setValue(part == null ? "" : part.text());
    }

    private String share(AgentContext.Part part) {
        long total = context == null ? 0 : context.estimatedTokens();
        return total == 0 ? "" : String.format("%.1f %%", part.tokens() * 100.0 / total);
    }

    private String kindName(Kind kind) {
        return switch (kind) {
            case SYSTEM_PROMPT -> loc.getValue(L.LABEL_AGENT_CONTEXT_PROMPT);
            case INSTRUCTIONS -> loc.getValue(L.LABEL_INSTRUCTIONS);
            case NOTE -> loc.getValue(L.LABEL_AGENT_CONTEXT_NOTES);
            case TOOL -> loc.getValue(L.LABEL_AGENT_CONTEXT_TOOLS);
            case SUMMARY -> loc.getValue(L.LABEL_SUMMARY);
            case USER -> loc.getValue(L.LABEL_USER);
            case ASSISTANT -> loc.getValue(L.LABEL_ASSISTANT);
            case RESULT -> loc.getValue(L.LABEL_AGENT_CONTEXT_RESULTS);
        };
    }

    /**
     * The kinds of parts against the cap: what the context is made of and how much room is left.
     */
    private ChartJs kindsChart() {
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        List<String> colors = new ArrayList<>();
        for (Kind kind : Kind.values()) {
            long tokens = context.tokens(kind);
            if (tokens > 0) {
                labels.add(kindName(kind));
                values.add(tokens);
                colors.add(COLORS.get(kind));
            }
        }
        long free = context.contextCap() - context.estimatedTokens();
        if (context.contextCap() > 0 && free > 0) {
            labels.add(loc.getValue(L.LABEL_TOKEN_FREE));
            values.add(free);
            colors.add(COLOR_FREE);
        }
        Map<String, Object> dataset = new LinkedHashMap<>();
        dataset.put("data", values);
        dataset.put("backgroundColor", colors);
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("type", "doughnut");
        config.put("data", Map.of("labels", labels, "datasets", List.of(dataset)));
        config.put("options", Map.of("plugins", Map.of("legend", Map.of("position", "right"))));
        return chart(config);
    }

    /**
     * Every part in the order it is sent, a bar of its tokens in the colour of its kind.
     */
    private ChartJs partsChart() {
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        List<String> colors = new ArrayList<>();
        int number = 1;
        for (AgentContext.Part part : context.parts()) {
            labels.add(number++ + " " + part.title());
            values.add(part.tokens());
            colors.add(COLORS.get(part.kind()));
        }
        Map<String, Object> dataset = new LinkedHashMap<>();
        dataset.put("label", loc.getValue(L.LABEL_TOKENS));
        dataset.put("data", values);
        dataset.put("backgroundColor", colors);
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("type", "bar");
        config.put("data", Map.of("labels", labels, "datasets", List.of(dataset)));
        config.put("options", Map.of("plugins", Map.of("legend", Map.of("display", false))));
        return chart(config);
    }

    /**
     * The size of the context the model reported at each of its answers, with the cap.
     */
    private ChartJs historyChart() {
        List<Integer> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        for (AgentContext.Point point : context.history()) {
            labels.add(point.round());
            values.add(point.tokens());
        }
        List<Map<String, Object>> datasets = new ArrayList<>();
        Map<String, Object> reported = new LinkedHashMap<>();
        reported.put("label", loc.getValue(L.LABEL_TOKENS));
        reported.put("data", values);
        reported.put("borderColor", COLORS.get(Kind.ASSISTANT));
        reported.put("tension", 0.2);
        datasets.add(reported);
        if (context.contextCap() > 0) {
            Map<String, Object> cap = new LinkedHashMap<>();
            cap.put("label", loc.getValue(L.LABEL_TOKEN_CONTEXT));
            cap.put("data", labels.stream().map(label -> context.contextCap()).toList());
            cap.put("borderColor", COLORS.get(Kind.RESULT));
            cap.put("borderDash", List.of(6, 4));
            cap.put("pointRadius", 0);
            datasets.add(cap);
        }
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("type", "line");
        config.put("data", Map.of("labels", labels, "datasets", datasets));
        return chart(config);
    }

    private ChartJs chart(Map<String, Object> config) {
        ChartJs chart = new ChartJs();
        chart.setChartConfig(GSON.toJson(config));
        return chart;
    }

    private void copy() {
        if (context == null) {
            return;
        }
        JavascriptLibrary.copyToClipboard(UI.getCurrent().getPage(), context.toMarkdown());
        Notification.success(loc.getValue(L.MSG_COPIED_TO_CLIPBOARD));
    }

    private Anchor downloadAnchor(Button button) {
        Anchor anchor = new Anchor(new InputStreamDownloadHandler((InputStreamDownloadCallback) downloadEvent -> {
            if (context == null) {
                throw new IOException("There is no context to download");
            }
            byte[] content = context.toMarkdown().getBytes(StandardCharsets.UTF_8);
            return new DownloadResponse(new ByteArrayInputStream(content), "agent-context.md", "text/markdown", content.length);
        }), "");
        anchor.getElement().setAttribute("download", true);
        anchor.add(button);
        return anchor;
    }

    // for the tests

    AgentContext getContext() {
        return context;
    }

    Grid<AgentContext.Part> getGrid() {
        return grid;
    }
}
