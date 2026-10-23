package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.components.BranchTokens;
import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.ChartJs;
import com.google.gson.Gson;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shows, before a part is generated, what the story of the active branch takes of the prompt: the parts as they are
 * and with the summaries standing in for the older ones, against the budget of the prompt (the context of the model of
 * the book minus the room for the response).
 */
@Configurable
public class BranchTokensDialog extends Dialog {

    private static final Gson gson = new Gson();

    private static final String COLOR_COVERED = "#9aa0a6";
    private static final String COLOR_SUMMARIES = "#f28e2b";
    private static final String COLOR_RECENT = "#4e79a7";

    @Autowired
    private Localization loc;

    @Autowired
    private ChatMessageService chatMessageService;

    private final Manuscript manuscript;

    public BranchTokensDialog(Manuscript manuscript) {
        this.manuscript = manuscript;
    }

    /**
     * A line of the table: {@code parts} is negative where it does not apply.
     */
    private record Row(String label, int parts, long tokens) {
    }

    public void create() throws Exception {
        setHeaderTitle(loc.getValue(L.LABEL_BRANCH_TOKENS));
        setWidth("750px");
        setResizable(true);
        setDraggable(true);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        BranchTokens tokens = chatMessageService.getBranchTokens(manuscript.getActiveLeaf());
        long budget = TokenLimits.promptTokens(manuscript.getAi(), manuscript.getProtocol());
        if (tokens.parts() == 0) {
            content.add(new Div(loc.getValue(L.MSG_BRANCH_TOKENS_EMPTY)));
        } else {
            content.add(chart(tokens), table(tokens, budget));
            content.add(note(budget > 0
                    ? String.format(loc.getValue(L.MSG_BRANCH_TOKENS_BUDGET), format(budget),
                    format(TokenLimits.contextTokens(manuscript.getAi(), manuscript.getProtocol())),
                    format(TokenLimits.responseTokens(manuscript.getAi(), manuscript.getProtocol())))
                    : loc.getValue(L.MSG_BRANCH_TOKENS_NO_BUDGET)));
            if (tokens.staleSummaries() > 0) {
                content.add(note(String.format(loc.getValue(L.MSG_BRANCH_TOKENS_STALE), tokens.staleSummaries())));
            }
            content.add(note(loc.getValue(L.MSG_BRANCH_TOKENS_HELP)));
        }
        add(content);

        Button closeButton = new Button(loc.getValue(L.LABEL_CLOSE), event -> close());
        closeButton.setThemeName("primary");
        getFooter().add(closeButton);
    }

    private ChartJs chart(BranchTokens tokens) {
        List<String> labels = List.of(loc.getValue(L.LABEL_TOKENS_WITHOUT_SUMMARIES), loc.getValue(L.LABEL_TOKENS_WITH_SUMMARIES));
        List<Object> datasets = List.of(
                dataset(loc.getValue(L.LABEL_TOKENS_COVERED_PARTS), COLOR_COVERED, tokens.summarizedTokens(), 0),
                dataset(loc.getValue(L.LABEL_SUMMARIES), COLOR_SUMMARIES, 0, tokens.summaryTokens()),
                dataset(loc.getValue(L.LABEL_TOKENS_RECENT_PARTS), COLOR_RECENT, tokens.unsummarizedTokens(), tokens.unsummarizedTokens()));

        Map<String, Object> axis = Map.of("stacked", true);
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("type", "bar");
        config.put("data", Map.of("labels", labels, "datasets", datasets));
        config.put("options", Map.of("indexAxis", "y", "scales", Map.of("x", axis, "y", axis),
                "plugins", Map.of("legend", Map.of("position", "bottom"))));

        ChartJs chart = new ChartJs();
        chart.setHeight("220px");
        chart.setChartConfig(gson.toJson(config));
        return chart;
    }

    private static Map<String, Object> dataset(String label, String color, long withoutSummaries, long withSummaries) {
        Map<String, Object> dataset = new LinkedHashMap<>();
        dataset.put("label", label);
        dataset.put("data", List.of(withoutSummaries, withSummaries));
        dataset.put("backgroundColor", color);
        return dataset;
    }

    private Grid<Row> table(BranchTokens tokens, long budget) {
        Grid<Row> grid = new Grid<>(Row.class, false);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER);
        grid.setAllRowsVisible(true);
        grid.addColumn(Row::label).setHeader("").setFlexGrow(3);
        grid.addColumn(row -> row.parts() < 0 ? "" : String.valueOf(row.parts())).setHeader(loc.getValue(L.LABEL_PARTS))
                .setTextAlign(ColumnTextAlign.END);
        grid.addColumn(row -> format(row.tokens())).setHeader(loc.getValue(L.LABEL_TOKENS))
                .setTextAlign(ColumnTextAlign.END);
        grid.addColumn(row -> budget > 0 ? Math.round(100.0 * row.tokens() / budget) + " %" : "")
                .setHeader(loc.getValue(L.LABEL_TOKEN_BUDGET_SHARE))
                .setTextAlign(ColumnTextAlign.END);
        grid.setItems(
                new Row(loc.getValue(L.LABEL_TOKENS_WITHOUT_SUMMARIES), tokens.parts(), tokens.partTokens()),
                new Row(loc.getValue(L.LABEL_SUMMARIES), tokens.summaries(), tokens.summaryTokens()),
                new Row(loc.getValue(L.LABEL_TOKENS_RECENT_PARTS), tokens.unsummarizedParts(), tokens.unsummarizedTokens()),
                new Row(loc.getValue(L.LABEL_TOKENS_WITH_SUMMARIES), -1, tokens.withSummariesTokens()));
        return grid;
    }

    private Div note(String text) {
        Div note = new Div(text);
        note.addClassName(CssStyles.TOKEN_USAGE_SUMMARY);
        return note;
    }

    private static String format(long value) {
        return String.format("%,d", value);
    }
}
