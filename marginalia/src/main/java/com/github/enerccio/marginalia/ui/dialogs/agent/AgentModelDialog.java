package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.domain.model.AgentModelProfile;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.ModelProtocolPicker;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * One model profile of an agent: the role and the description the agent reads to choose it for a sub-agent, the AI and the
 * protocol, whether it can use commands and tools at all, the commands it may use and its context limit. The main profile (the
 * model of the agent itself, picked in the dialog of the agent) is edited here only for the commands of a sub-agent that runs on
 * the same model. The profile is changed only when the dialog is confirmed.
 */
@Configurable(preConstruction = true)
public class AgentModelDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    private final AgentModelProfile profile;
    private final boolean main;
    private final Consumer<AgentModelProfile> onOk;

    private TextField roleField;
    private ModelProtocolPicker modelPicker;
    private TextArea descriptionField;
    private Checkbox toolsCheckbox;
    private IntegerField contextLimitField;
    private Checkbox limitCommandsCheckbox;
    private AgentCommandsEditor commandsEditor;

    /**
     * @param profile the profile to edit (not changed), a blank one for a new model
     * @param main    whether it is the main profile: only its commands are edited
     * @param onOk    gets the edited copy
     */
    public AgentModelDialog(AgentModelProfile profile, boolean main, Consumer<AgentModelProfile> onOk) {
        this.profile = profile.copy();
        this.main = main;
        this.onOk = onOk;
    }

    public void create() {
        setHeaderTitle(loc.getValue(main ? L.LABEL_AGENT_MAIN_MODEL : L.LABEL_AGENT_MODELS));
        setWidth("900px");
        setMaxWidth("96vw");
        UIUtils.fitPhone(this, true);
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        roleField = new TextField(loc.getValue(L.LABEL_ROLE));
        roleField.setRequiredIndicatorVisible(true);
        roleField.setWidthFull();
        modelPicker = new ModelProtocolPicker();
        descriptionField = new TextArea(loc.getValue(L.LABEL_DESCRIPTION));
        descriptionField.setWidthFull();
        descriptionField.setMinHeight("5em");
        toolsCheckbox = new Checkbox(loc.getValue(L.LABEL_AGENT_MODEL_TOOLS), true);
        toolsCheckbox.setHelperText(loc.getValue(L.HELP_AGENT_MODEL_TOOLS));
        contextLimitField = new IntegerField(loc.getValue(L.LABEL_CONTEXT_LIMIT));
        contextLimitField.setMin(1);
        contextLimitField.setWidthFull();
        limitCommandsCheckbox = new Checkbox(loc.getValue(L.LABEL_AGENT_LIMIT_COMMANDS));
        limitCommandsCheckbox.setHelperText(loc.getValue(L.HELP_AGENT_MODEL_COMMANDS));
        commandsEditor = new AgentCommandsEditor();
        commandsEditor.setWidthFull();
        limitCommandsCheckbox.addValueChangeListener(event -> commandsEditor.setVisible(event.getValue()));
        toolsCheckbox.addValueChangeListener(event -> {
            limitCommandsCheckbox.setVisible(event.getValue());
            commandsEditor.setVisible(event.getValue() && limitCommandsCheckbox.getValue());
        });

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        form.setWidthFull();
        if (!main) {
            form.add(roleField, modelPicker, descriptionField, contextLimitField, toolsCheckbox);
        }
        form.add(limitCommandsCheckbox, commandsEditor);
        add(form);

        Button cancelButton = new Button(loc.getValue(L.LABEL_CANCEL), event -> close());
        Button okButton = new Button(loc.getValue(L.LABEL_OK), event -> save());
        okButton.setThemeName("primary");
        HorizontalLayout footer = new HorizontalLayout(cancelButton, okButton);
        footer.setWidthFull();
        footer.setWrap(true);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(footer);

        load();
    }

    private void load() {
        try {
            roleField.setValue(StringUtils.defaultString(profile.getRole()));
            descriptionField.setValue(StringUtils.defaultString(profile.getDescription()));
            contextLimitField.setValue(profile.getContextCap());
            toolsCheckbox.setValue(profile.isTools());
            AI ai = profile.getAiUuid() == null ? null : aiService.findAllForUser().stream()
                    .filter(candidate -> Objects.equals(candidate.getUuid(), profile.getAiUuid())).findFirst().orElse(null);
            Protocol protocol = profile.getProtocolUuid() == null ? null : protocolService.findForUser(profile.getProtocolUuid());
            modelPicker.setSelection(ai, protocol);
            limitCommandsCheckbox.setValue(profile.getCommands() != null);
            commandsEditor.setAllowed(profile.getCommands() == null ? java.util.List.of() : profile.getCommands());
            commandsEditor.setVisible(profile.getCommands() != null && (main || profile.isTools()));
            limitCommandsCheckbox.setVisible(main || profile.isTools());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void save() {
        if (!main && (StringUtils.isBlank(roleField.getValue()) || modelPicker.getAi() == null || modelPicker.getProtocol() == null
                || contextLimitField.isInvalid())) {
            Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
            return;
        }
        if (!main) {
            profile.setRole(roleField.getValue().trim());
            profile.setAiUuid(modelPicker.getAi().getUuid());
            profile.setProtocolUuid(modelPicker.getProtocol().getUuid());
            profile.setDescription(StringUtils.isBlank(descriptionField.getValue()) ? null : descriptionField.getValue().strip());
            profile.setContextCap(contextLimitField.getValue());
            profile.setTools(toolsCheckbox.getValue());
        }
        profile.setCommands(limitCommandsCheckbox.getValue() && (main || toolsCheckbox.getValue()) ? new ArrayList<>(commandsEditor.getAllowed()) : null);
        close();
        onOk.accept(profile);
    }

    // for the tests
    TextField getRoleField() {
        return roleField;
    }

    ModelProtocolPicker getModelPicker() {
        return modelPicker;
    }

    Checkbox getToolsCheckbox() {
        return toolsCheckbox;
    }

    Checkbox getLimitCommandsCheckbox() {
        return limitCommandsCheckbox;
    }

    AgentCommandsEditor getCommandsEditor() {
        return commandsEditor;
    }

    TextArea getDescriptionField() {
        return descriptionField;
    }

    void confirm() {
        save();
    }
}
