package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.components.PromptTokenBreakdown;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.ChartJs;
import com.google.gson.Gson;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shows where the context of a generation went: the sections of the prompt against the context size, and the tokens
 * of each message sent to the model.
 */
@Configurable
@Extendable
public class TokenUsageDialog extends Dialog {

    private static final Gson gson = new Gson();

    private static final String COLOR_FREE = "#9aa0a6";
    private static final List<String> PALETTE = List.of(
            "#4e79a7", "#f28e2b", "#e15759", "#76b7b2", "#59a14f", "#edc948", "#b07aa1", "#ff9da7", "#9c755f");
    private static final Map<String, String> ROLE_COLORS = Map.of(
            "SYSTEM", "#4e79a7", "USER", "#f28e2b", "ASSISTANT", "#59a14f");

    @Autowired
    private Localization loc;

    private final PromptTokenBreakdown breakdown;

    /**
     * @param breakdown token counts of the prompt, null when they were not recorded (part generated before they were)
     */
    public TokenUsageDialog(PromptTokenBreakdown breakdown) {
        this.breakdown = breakdown;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "content", type = VerticalLayout.class),
                    @APILocals(name = "closeButton", type = Button.class),
                    @APILocals(name = "footerLayout", type = HorizontalLayout.class)})
    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_TOKEN_USAGE));
        setWidth("850px");
        setHeight("700px");
        setResizable(true);
        setDraggable(true);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        if (breakdown == null) {
            content.add(new Div(loc.getValue(L.MSG_TOKEN_USAGE_UNAVAILABLE)));
        } else {
            addSectionsChart(content);
            addMessagesChart(content);
        }
        add(content);

        Button closeButton = new Button(loc.getValue(L.LABEL_OK), event -> close());
        closeButton.setThemeName("primary");
        HorizontalLayout footerLayout = new HorizontalLayout(closeButton);
        footerLayout.setWidthFull();
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(footerLayout);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "content", type = VerticalLayout.class)},
            locals = {@APILocals(name = "labels", type = List.class),
                    @APILocals(name = "values", type = List.class),
                    @APILocals(name = "colors", type = List.class),
                    @APILocals(name = "dataset", type = Map.class),
                    @APILocals(name = "config", type = Map.class),
                    @APILocals(name = "chart", type = ChartJs.class),
                    @APILocals(name = "summary", type = Div.class),
                    @APILocals(name = "cut", type = Div.class)})
    private void addSectionsChart(VerticalLayout content) {
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        List<String> colors = new ArrayList<>();

        addSection(labels, values, colors, L.LABEL_JAILBREAK, breakdown.getJailbreakTokens());
        addSection(labels, values, colors, L.LABEL_TOKEN_TEMPLATE, breakdown.getTemplateTokens());
        addSection(labels, values, colors, L.LABEL_TOKEN_LORE, breakdown.getLoreTokens());
        addSection(labels, values, colors, L.LABEL_SUMMARIES, breakdown.getSummaryTokens());
        addSection(labels, values, colors, L.LABEL_TOKEN_STORY, breakdown.getHistoryTokens());
        addSection(labels, values, colors, L.LABEL_TOKEN_USER_LORE, breakdown.getUserLoreTokens());
        addSection(labels, values, colors, L.LABEL_TOKEN_USER_PROMPT, breakdown.getUserPromptTokens());
        addSection(labels, values, colors, L.LABEL_TOKEN_RESERVED, breakdown.getReservedTokens());

        long used = values.stream().mapToLong(Long::longValue).sum();
        if (breakdown.getContextTokens() > 0) {
            addSection(labels, values, colors, L.LABEL_TOKEN_RESPONSE, breakdown.getResponseTokens());
            long free = breakdown.getContextTokens() - used - breakdown.getResponseTokens();
            if (free > 0) {
                labels.add(loc.getValue(L.LABEL_TOKEN_FREE));
                values.add(free);
                colors.add(COLOR_FREE);
            }
        }

        Map<String, Object> dataset = new LinkedHashMap<>();
        dataset.put("data", values);
        dataset.put("backgroundColor", colors);

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("type", "doughnut");
        config.put("data", Map.of("labels", labels, "datasets", List.of(dataset)));
        config.put("options", Map.of("plugins", Map.of("legend", Map.of("position", "right"))));

        ChartJs chart = new ChartJs();
        chart.setChartConfig(gson.toJson(config));

        Div summary = new Div(loc.getValue(L.LABEL_TOKEN_CONTEXT) + ": " + breakdown.getContextTokens()
                + " | " + loc.getValue(L.LABEL_TOKENS) + ": " + used);
        summary.addClassName(CssStyles.TOKEN_USAGE_SUMMARY);
        content.add(summary, chart);
        if (breakdown.getCutMessages() > 0) {
            Div cut = new Div(String.format(loc.getValue(L.MSG_TOKEN_CUT), breakdown.getCutMessages(), breakdown.getCutTokens()));
            cut.addClassName(CssStyles.TOKEN_USAGE_SUMMARY);
            content.add(cut);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "labels", type = List.class),
                    @APIParameter(name = "values", type = List.class),
                    @APIParameter(name = "colors", type = List.class),
                    @APIParameter(name = "label", type = L.class),
                    @APIParameter(name = "tokens", type = long.class)})
    private void addSection(List<String> labels, List<Long> values, List<String> colors, L label, long tokens) {
        if (tokens <= 0) {
            return;
        }
        labels.add(loc.getValue(label));
        values.add(tokens);
        colors.add(PALETTE.get(colors.size() % PALETTE.size()));
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "content", type = VerticalLayout.class)},
            locals = {@APILocals(name = "messages", type = List.class),
                    @APILocals(name = "labels", type = List.class),
                    @APILocals(name = "values", type = List.class),
                    @APILocals(name = "colors", type = List.class),
                    @APILocals(name = "message", type = PromptTokenBreakdown.MessageTokens.class),
                    @APILocals(name = "dataset", type = Map.class),
                    @APILocals(name = "config", type = Map.class),
                    @APILocals(name = "chart", type = ChartJs.class),
                    @APILocals(name = "summary", type = Div.class)})
    private void addMessagesChart(VerticalLayout content) {
        List<PromptTokenBreakdown.MessageTokens> messages = breakdown.getMessages();
        if (messages.isEmpty()) {
            return;
        }
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        List<String> colors = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            PromptTokenBreakdown.MessageTokens message = messages.get(i);
            labels.add("#" + (i + 1) + " " + roleName(message.role()));
            values.add(message.tokens());
            colors.add(ROLE_COLORS.getOrDefault(String.valueOf(message.role()), COLOR_FREE));
        }

        Map<String, Object> dataset = new LinkedHashMap<>();
        dataset.put("label", loc.getValue(L.LABEL_TOKENS));
        dataset.put("data", values);
        dataset.put("backgroundColor", colors);

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("type", "bar");
        config.put("data", Map.of("labels", labels, "datasets", List.of(dataset)));
        config.put("options", Map.of("plugins", Map.of("legend", Map.of("display", false))));

        ChartJs chart = new ChartJs();
        chart.setChartConfig(gson.toJson(config));

        Div summary = new Div(loc.getValue(L.LABEL_TOKENS) + ": " + breakdown.getPayloadTokens());
        summary.addClassName(CssStyles.TOKEN_USAGE_SUMMARY);
        content.add(new H4(loc.getValue(L.LABEL_PAYLOAD_MESSAGES)), summary, chart);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "role", type = String.class)})
    private String roleName(String role) {
        if (role == null) {
            return "";
        }
        return switch (role) {
            case "SYSTEM" -> loc.getValue(L.LABEL_SYSTEM);
            case "USER" -> loc.getValue(L.LABEL_USER);
            case "ASSISTANT" -> loc.getValue(L.LABEL_ASSISTANT);
            default -> role;
        };
    }
}
