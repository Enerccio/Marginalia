package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.agent.AgentFindings;
import com.github.enerccio.marginalia.domain.service.agent.RunTools;
import com.github.enerccio.marginalia.domain.service.agent.RunTools.Finding;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;

/**
 * The findings an agent reported with {@code report}, one row each: its number, its severity and its text. Shown in the popover of
 * the console and in the review of a run.
 */
@Extendable
final class AgentFindingsView extends VerticalLayout {

    private final Localization loc;

    AgentFindingsView(Localization loc) {
        this.loc = loc;
        setPadding(false);
        setSpacing(false);
        addClassName(CssStyles.AGENT_FINDINGS);
    }

    /**
     * Shows the findings instead of what was shown.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "findings", type = List.class)},
            locals = {@APILocals(name = "finding", type = RunTools.Finding.class),
                    @APILocals(name = "severity", type = Span.class),
                    @APILocals(name = "row", type = Div.class)})
    void setFindings(List<Finding> findings) {
        removeAll();
        for (Finding finding : findings) {
            Span severity = new Span(loc.getValue(severityLabel(finding.severity())));
            if (AgentFindings.WARNING.equals(finding.severity())) {
                severity.addClassName(CssStyles.AGENT_FINDING_WARNING);
            } else if (AgentFindings.PROBLEM.equals(finding.severity())) {
                severity.addClassName(CssStyles.AGENT_FINDING_PROBLEM);
            } else {
                severity.addClassName(CssStyles.HINT_TEXT);
            }
            Div row = new Div(new Span(finding.number() + "."), severity, new Span(finding.text()));
            row.addClassName(CssStyles.AGENT_FINDING);
            add(row);
        }
    }

    private static L severityLabel(String severity) {
        return switch (severity) {
            case AgentFindings.WARNING -> L.ENUM_AGENT_FINDING_WARNING;
            case AgentFindings.PROBLEM -> L.ENUM_AGENT_FINDING_PROBLEM;
            default -> L.LABEL_NOTE;
        };
    }
}
