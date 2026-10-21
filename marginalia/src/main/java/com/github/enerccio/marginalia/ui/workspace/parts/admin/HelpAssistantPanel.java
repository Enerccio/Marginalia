package com.github.enerccio.marginalia.ui.workspace.parts.admin;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.HelpAssistantService;
import com.github.enerccio.marginalia.domain.service.HelpAssistantService.Settings;
import com.github.enerccio.marginalia.domain.service.manual.HelpPrompts;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
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
@Extendable
public class HelpAssistantPanel implements LLMInteract {

    @Autowired
    private Localization loc;

    @Autowired
    private HelpAssistantService helpService;

    @Autowired
    private AIService aiService;

    private ComboBox<AI> aiCombo;
    private Checkbox adminData;
    private TextArea adminPrompt;
    private TextArea userPrompt;

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "layout", type = VerticalLayout.class),
                    @APILocals(name = "description", type = Span.class),
                    @APILocals(name = "save", type = Button.class)})
    public Component create() throws Exception {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        Span description = new Span(loc.getValue(L.HELP_ASSISTANT));
        description.addClassName(CssStyles.TEXT_SECONDARY);

        aiCombo = new ComboBox<>(loc.getValue(L.LABEL_MODEL));
        aiCombo.setWidth("24em");
        aiCombo.setItemLabelGenerator(AI::getName);
        aiCombo.setClearButtonVisible(true);

        adminData = new Checkbox(loc.getValue(L.LABEL_HELP_ADMIN_DATA));
        adminData.setHelperText(loc.getValue(L.HELP_ASSISTANT_ADMIN_DATA));

        adminPrompt = promptArea(L.LABEL_HELP_PROMPT_ADMIN);
        userPrompt = promptArea(L.LABEL_HELP_PROMPT_USER);

        Button save = new Button(loc.getValue(L.LABEL_SAVE), e -> save());
        save.setThemeName("primary");

        layout.add(description, aiCombo, adminData, promptRow(adminPrompt, true), promptRow(userPrompt, false), save);

        load();
        return layout;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = TextArea.class),
            parameters = {@APIParameter(name = "label", type = L.class)},
            locals = {@APILocals(name = "area", type = TextArea.class)})
    private TextArea promptArea(L label) {
        TextArea area = new TextArea(loc.getValue(label));
        area.setHelperText(loc.getValue(L.HELP_ASSISTANT_PROMPT));
        area.setWidthFull();
        area.setMinRows(6);
        area.setMaxRows(14);
        return area;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = HorizontalLayout.class),
            parameters = {@APIParameter(name = "area", type = TextArea.class),
                    @APIParameter(name = "administrator", type = boolean.class)},
            locals = {@APILocals(name = "reset", type = Button.class),
                    @APILocals(name = "row", type = HorizontalLayout.class)})
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

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "models", type = List.class),
                    @APILocals(name = "settings", type = HelpAssistantService.Settings.class)})
    private void load() throws Exception {
        // the assistant uses the administrator's own models, not the ones other administrators share
        List<AI> models = aiService.findAllForUser().stream().filter(aiService::isOwnedByCurrentUser).toList();
        aiCombo.setItems(models);

        Settings settings = helpService.getSettings();
        aiCombo.setValue(models.stream().filter(ai -> Objects.equals(ai.getUuid(), settings.aiUuid())).findFirst().orElse(null));
        adminData.setValue(settings.adminData());
        // what is in force: the stored prompt or the default
        adminPrompt.setValue(settings.adminPrompt() == null ? HelpPrompts.ADMIN : settings.adminPrompt());
        userPrompt.setValue(settings.userPrompt() == null ? HelpPrompts.USER : settings.userPrompt());
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "ai", type = AI.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void save() {
        AI ai = aiCombo.getValue();
        try {
            helpService.saveSettings(new Settings(ai == null ? null : ai.getUuid(),
                    adminPrompt.getValue(), userPrompt.getValue(), adminData.getValue()));
            Notification.success(loc.getValue(L.MSG_SETTINGS_SAVED));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
