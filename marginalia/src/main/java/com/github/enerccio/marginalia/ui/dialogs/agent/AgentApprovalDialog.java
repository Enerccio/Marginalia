package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.AgentApprovalService.PendingApproval;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Map;
import java.util.function.Consumer;

/**
 * The popup of the console that asks the user to allow a command the agent wants to run (the agent is in the
 * {@code ASK} mode for commands that change or remove data): the command and what it passes, and the buttons to
 * approve or deny. It can't be dismissed without a decision, the run waits for it.
 */
@Configurable
public class AgentApprovalDialog extends Dialog implements LLMInteract {

    /**
     * How much of one argument is shown.
     */
    static final int VALUE_LIMIT = 1000;

    @Autowired
    private Localization loc;

    private final String agentName;
    private final PendingApproval approval;
    private final Consumer<Boolean> onDecision;
    private Button approveButton;
    private Button denyButton;

    /**
     * @param onDecision called with true to approve, false to deny, after the dialog is closed
     */
    public AgentApprovalDialog(String agentName, PendingApproval approval, Consumer<Boolean> onDecision) {
        this.agentName = agentName;
        this.approval = approval;
        this.onDecision = onDecision;
    }

    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_AGENT_APPROVAL));
        setWidth("560px");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        Span question = new Span(String.format(loc.getValue(L.MSG_AGENT_APPROVAL_QUESTION), agentName, approval.command()));
        question.addClassName(CssStyles.WRAP_ANYWHERE);
        VerticalLayout content = new VerticalLayout(question);
        content.setPadding(false);
        if (!approval.arguments().isEmpty()) {
            Span title = new Span(loc.getValue(L.LABEL_ARGUMENTS));
            title.addClassNames(CssStyles.TEXT_SEMIBOLD);
            Div arguments = new Div();
            arguments.addClassNames(CssStyles.HELP_MESSAGE, CssStyles.TEXT_MONOSPACE, CssStyles.PRE_WRAP);
            arguments.setText(describe(approval.arguments()));
            content.add(title, arguments);
        }
        add(content);

        denyButton = new Button(loc.getValue(L.LABEL_DENY), event -> decide(false));
        denyButton.setThemeName("error");
        approveButton = new Button(loc.getValue(L.LABEL_APPROVE), event -> decide(true));
        approveButton.setThemeName("primary");
        HorizontalLayout footer = new HorizontalLayout(denyButton, approveButton);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(footer);
    }

    static String describe(Map<String, Object> arguments) {
        StringBuilder text = new StringBuilder();
        arguments.forEach((name, value) -> text.append(name).append(": ")
                .append(StringUtils.abbreviate(String.valueOf(value), VALUE_LIMIT)).append('\n'));
        return text.toString().stripTrailing();
    }

    public PendingApproval getApproval() {
        return approval;
    }

    private void decide(boolean approved) {
        close();
        onDecision.accept(approved);
    }

    // for the tests of the dialog

    Button getApproveButton() {
        return approveButton;
    }

    Button getDenyButton() {
        return denyButton;
    }
}
