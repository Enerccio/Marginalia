package com.github.enerccio.marginalia.ui.workspace.parts.admin;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.service.AgentService;
import com.github.enerccio.marginalia.domain.service.AgentWebhookService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.ui.widgets.TemplateHints;
import com.github.enerccio.marginalia.ui.widgets.TextAreaPopoverComponent;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Administrator's settings of agents for the whole application: the system prompt of the agents that have none of their
 * own and whose users have no default prompt in their settings. Blank is the built-in prompt, which is the placeholder
 * and what the info popover fills in. (What the users may do with agents is the permission {@code AGENTS}.)
 */
@Configurable
public class AgentSettingsPanel implements LLMInteract {

    @Autowired
    private Localization loc;

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentWebhookService webhookService;

    private Checkbox webhooksAllowed;

    private TextAreaPopoverComponent promptField;
    private TextAreaPopoverComponent compactionField;

    public Component create() throws Exception {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        Span description = new Span(loc.getValue(L.HELP_AGENT_DEFAULT_PROMPT_APP));
        description.addClassName(CssStyles.TEXT_SECONDARY);

        promptField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_AGENT_DEFAULT_PROMPT));
        promptField.setWidthFull();
        promptField.setPlaceholder(Defaults.DEFAULT_AGENT_PROMPT);
        promptField.setPopoverContent(TemplateHints.create(loc, promptField, promptField.getPopover(), null,
                Defaults.DEFAULT_AGENT_PROMPT));
        ResizableTextArea.install(loc, promptField, "240px");

        Span compactionDescription = new Span(loc.getValue(L.HELP_AGENT_DEFAULT_COMPACTION_PROMPT_APP));
        compactionDescription.addClassName(CssStyles.TEXT_SECONDARY);

        compactionField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_AGENT_DEFAULT_COMPACTION_PROMPT));
        compactionField.setWidthFull();
        compactionField.setPlaceholder(Defaults.DEFAULT_AGENT_COMPACTION_PROMPT);
        compactionField.setPopoverContent(TemplateHints.create(loc, compactionField, compactionField.getPopover(), null,
                Defaults.DEFAULT_AGENT_COMPACTION_PROMPT));
        ResizableTextArea.install(loc, compactionField, "240px");

        webhooksAllowed = new Checkbox(loc.getValue(L.LABEL_AGENT_WEBHOOKS_ALLOWED));
        webhooksAllowed.setHelperText(loc.getValue(L.HELP_AGENT_WEBHOOKS_ALLOWED));

        Button save = new Button(loc.getValue(L.LABEL_SAVE), e -> save());
        save.setThemeName("primary");

        layout.add(description, promptField, compactionDescription, compactionField, webhooksAllowed, save);
        load();
        return layout;
    }

    private void load() throws Exception {
        promptField.setValue(agentService.getAppSystemPrompt());
        compactionField.setValue(agentService.getAppCompactionPrompt());
        webhooksAllowed.setValue(webhookService.isAllowed());
    }

    private void save() {
        try {
            agentService.setAppSystemPrompt(promptField.getValue());
            agentService.setAppCompactionPrompt(compactionField.getValue());
            webhookService.setAllowed(webhooksAllowed.getValue());
            Notification.success(loc.getValue(L.MSG_SETTINGS_SAVED));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
