package com.github.enerccio.marginalia.ui.workspace.parts.admin;

import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.HelpAssistantService;
import com.github.enerccio.marginalia.domain.service.HelpAssistantService.Settings;
import com.github.enerccio.marginalia.domain.service.manual.HelpPrompts;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.Objects;

/**
 * Administrator's settings of the help assistant: whether it is on, which of the administrator's own models it uses
 * and, for a shared model, whether the users' limits apply to it.
 */
@Configurable
public class HelpAssistantPanel implements LLMInteract {

    @Autowired
    private Localization loc;

    @Autowired
    private HelpAssistantService helpService;

    @Autowired
    private AIService aiService;

    private Checkbox enabled;
    private ComboBox<AI> aiCombo;
    private Checkbox applyLimits;
    private Checkbox interactions;
    private TextArea adminPrompt;
    private TextArea userPrompt;

    public Component create() throws Exception {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        Span description = new Span(loc.getValue(L.HELP_ASSISTANT));
        description.addClassName(CssStyles.TEXT_SECONDARY);

        enabled = new Checkbox(loc.getValue(L.LABEL_ENABLED));

        aiCombo = new ComboBox<>(loc.getValue(L.LABEL_MODEL));
        aiCombo.setWidth("24em");
        aiCombo.setItemLabelGenerator(AI::getName);
        aiCombo.setClearButtonVisible(true);
        aiCombo.addValueChangeListener(e -> updateApplyLimits());

        // only a shared model has limits to apply
        applyLimits = new Checkbox(loc.getValue(L.LABEL_APPLY_LIMITS));
        applyLimits.setHelperText(loc.getValue(L.HELP_ASSISTANT_APPLY_LIMITS));

        interactions = new Checkbox(loc.getValue(L.LABEL_HELP_INTERACTIONS));
        interactions.setHelperText(loc.getValue(L.HELP_ASSISTANT_INTERACTIONS));

        adminPrompt = promptArea(L.LABEL_HELP_PROMPT_ADMIN);
        userPrompt = promptArea(L.LABEL_HELP_PROMPT_USER);

        Button save = new Button(loc.getValue(L.LABEL_SAVE), e -> save());
        save.setThemeName("primary");

        layout.add(description, enabled, aiCombo, applyLimits, interactions, promptRow(adminPrompt, true), promptRow(userPrompt, false), save);

        load();
        return layout;
    }

    private TextArea promptArea(L label) {
        TextArea area = new TextArea(loc.getValue(label));
        area.setHelperText(loc.getValue(L.HELP_ASSISTANT_PROMPT));
        area.setWidthFull();
        area.setMinRows(6);
        area.setMaxRows(14);
        return area;
    }

    private HorizontalLayout promptRow(TextArea area, boolean administrator) {
        Button reset = new Button(loc.getValue(L.LABEL_RESET_TO_DEFAULT),
                e -> area.setValue(HelpPrompts.defaultPrompt(administrator)));
        reset.setThemeName("tertiary");
        HorizontalLayout row = new HorizontalLayout(area, reset);
        row.setWidthFull();
        row.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.START);
        row.expand(area);
        return row;
    }

    private void load() throws Exception {
        // the assistant uses the administrator's own models, not the ones other administrators share
        List<AI> models = aiService.findAllForUser().stream().filter(aiService::isOwnedByCurrentUser).toList();
        aiCombo.setItems(models);

        Settings settings = helpService.getSettings();
        enabled.setValue(settings.enabled());
        aiCombo.setValue(models.stream().filter(ai -> Objects.equals(ai.getUuid(), settings.aiUuid())).findFirst().orElse(null));
        applyLimits.setValue(settings.applyLimits());
        interactions.setValue(settings.interactions());
        // what is in force: the stored prompt or the default
        adminPrompt.setValue(settings.adminPrompt() == null ? HelpPrompts.ADMIN : settings.adminPrompt());
        userPrompt.setValue(settings.userPrompt() == null ? HelpPrompts.USER : settings.userPrompt());
        updateApplyLimits();
    }

    private void updateApplyLimits() {
        AI ai = aiCombo.getValue();
        applyLimits.setEnabled(ai != null && ai.isShared());
    }

    private void save() {
        AI ai = aiCombo.getValue();
        if (enabled.getValue() && ai == null) {
            Notification.warning(loc.getValue(L.ERROR_AI_NOT_SET));
            return;
        }
        try {
            helpService.saveSettings(new Settings(enabled.getValue(), ai == null ? null : ai.getUuid(),
                    applyLimits.getValue(), adminPrompt.getValue(), userPrompt.getValue(), interactions.getValue()));
            Notification.success(loc.getValue(L.MSG_SETTINGS_SAVED));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
