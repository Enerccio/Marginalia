package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.CssStyles;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * A row of a table of the agent console on a phone: the columns do not fit side by side there, so a row says what they say
 * one line under the other: a title with the state at its right, small lines of detail, and the buttons of the row.
 */
final class AgentCard {

    private AgentCard() {
    }

    /**
     * @param title   the first line, what the row is
     * @param state   the short text at the right of the title, may be empty
     * @param details small lines under it; the empty ones are left out
     * @param actions the buttons of the row, null for none
     */
    static VerticalLayout of(String title, String state, List<String> details, Component actions) {
        Span name = new Span(title);
        name.addClassNames(CssStyles.TEXT_SEMIBOLD, CssStyles.WRAP_ANYWHERE);
        HorizontalLayout head = new HorizontalLayout(name);
        head.setWidthFull();
        head.setWrap(true);
        head.setAlignItems(FlexComponent.Alignment.BASELINE);
        head.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        if (StringUtils.isNotBlank(state)) {
            Span stateLabel = new Span(state);
            stateLabel.addClassName(CssStyles.HINT_TEXT);
            head.add(stateLabel);
        }
        VerticalLayout card = new VerticalLayout(head);
        card.setPadding(false);
        card.setSpacing(false);
        card.addClassNames(CssStyles.AGENT_CARD, CssStyles.GAP_S);
        for (String detail : details) {
            if (StringUtils.isNotBlank(detail)) {
                Span line = new Span(detail);
                line.addClassNames(CssStyles.HINT_TEXT, CssStyles.WRAP_ANYWHERE);
                card.add(line);
            }
        }
        if (actions != null) {
            card.add(actions);
        }
        return card;
    }
}
