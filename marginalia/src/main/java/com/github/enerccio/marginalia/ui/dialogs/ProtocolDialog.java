package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.model.impl.ChatCompletionProtocol;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class ProtocolDialog extends Dialog {

    @Autowired
    protected Localization loc;

    @Autowired
    private ProtocolService protocolService;

    private Protocol protocol;
    private Runnable onSave;

    private TextField nameField;
    private ComboBox<ProtocolType> typeCombo;
    private IntegerField maxTokensField;
    private IntegerField replyTokensField;

    private Checkbox temperatureEnabledCheckbox;
    private NumberField temperatureField;

    private Checkbox topPEnabledCheckbox;
    private NumberField topPField;

    private Checkbox frequencyPenaltyEnabledCheckbox;
    private NumberField frequencyPenaltyField;

    private Checkbox presencePenaltyEnabledCheckbox;
    private NumberField presencePenaltyField;

    public ProtocolDialog() {
        this(null);
    }

    public ProtocolDialog(Protocol protocol) {
        this.protocol = protocol;
    }

    public void create() {
        boolean isEdit = protocol != null && protocol.getId() != null;
        setHeaderTitle(isEdit ? loc.getValue(L.LABEL_EDIT_PROTOCOL) : loc.getValue(L.LABEL_NEW_PROTOCOL));
        setWidth("600px");
        setHeight("680px");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        createFields();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);

        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.add(nameField, typeCombo);
        formLayout.add(maxTokensField, replyTokensField);
        formLayout.add(temperatureEnabledCheckbox, temperatureField);
        formLayout.add(topPEnabledCheckbox, topPField);
        formLayout.add(frequencyPenaltyEnabledCheckbox, frequencyPenaltyField);
        formLayout.add(presencePenaltyEnabledCheckbox, presencePenaltyField);

        mainLayout.add(formLayout);
        add(mainLayout);

        if (isEdit) {
            populateFields();
        } else {
            typeCombo.setValue(ProtocolType.CHAT_COMPLETION);
            temperatureEnabledCheckbox.setValue(false);
            temperatureField.setEnabled(false);
            topPEnabledCheckbox.setValue(false);
            topPField.setEnabled(false);
            frequencyPenaltyEnabledCheckbox.setValue(false);
            frequencyPenaltyField.setEnabled(false);
            presencePenaltyEnabledCheckbox.setValue(false);
            presencePenaltyField.setEnabled(false);
        }

        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        Button cancelButton = new Button(loc.getValue(L.LABEL_CANCEL), event -> close());
        Button saveButton = new Button(loc.getValue(L.LABEL_OK), event -> save());
        saveButton.setThemeName("primary");

        footerLayout.add(cancelButton, saveButton);
        getFooter().add(footerLayout);
    }

    private void createFields() {
        nameField = new TextField(loc.getValue(L.LABEL_NAME));
        nameField.setRequired(true);
        nameField.setWidthFull();

        typeCombo = new ComboBox<>(loc.getValue(L.LABEL_TYPE));
        typeCombo.setItems(ProtocolType.values());
        typeCombo.setItemLabelGenerator(type -> loc.getValue(loc.getProtocolType(type)));
        typeCombo.setRequired(true);
        typeCombo.setWidthFull();

        maxTokensField = new IntegerField(loc.getValue(L.LABEL_MAX_TOKENS));
        maxTokensField.setWidthFull();

        replyTokensField = new IntegerField(loc.getValue(L.LABEL_REPLY_TOKENS));
        replyTokensField.setWidthFull();

        temperatureEnabledCheckbox = new Checkbox(loc.getValue(L.LABEL_ENABLE_TEMPERATURE));
        temperatureField = new NumberField(loc.getValue(L.LABEL_TEMPERATURE));
        temperatureField.setWidthFull();
        temperatureField.setEnabled(false);
        temperatureEnabledCheckbox.addValueChangeListener(event -> temperatureField.setEnabled(Boolean.TRUE.equals(event.getValue())));

        topPEnabledCheckbox = new Checkbox(loc.getValue(L.LABEL_ENABLE_TOP_P));
        topPField = new NumberField(loc.getValue(L.LABEL_TOP_P));
        topPField.setWidthFull();
        topPField.setEnabled(false);
        topPEnabledCheckbox.addValueChangeListener(event -> topPField.setEnabled(Boolean.TRUE.equals(event.getValue())));

        frequencyPenaltyEnabledCheckbox = new Checkbox(loc.getValue(L.LABEL_ENABLE_FREQUENCY_PENALTY));
        frequencyPenaltyField = new NumberField(loc.getValue(L.LABEL_FREQUENCY_PENALTY));
        frequencyPenaltyField.setWidthFull();
        frequencyPenaltyField.setEnabled(false);
        frequencyPenaltyEnabledCheckbox.addValueChangeListener(event -> frequencyPenaltyField.setEnabled(Boolean.TRUE.equals(event.getValue())));

        presencePenaltyEnabledCheckbox = new Checkbox(loc.getValue(L.LABEL_ENABLE_PRESENCE_PENALTY));
        presencePenaltyField = new NumberField(loc.getValue(L.LABEL_PRESENCE_PENALTY));
        presencePenaltyField.setWidthFull();
        presencePenaltyField.setEnabled(false);
        presencePenaltyEnabledCheckbox.addValueChangeListener(event -> presencePenaltyField.setEnabled(Boolean.TRUE.equals(event.getValue())));
    }

    private void populateFields() {
        if (protocol == null) {
            return;
        }

        nameField.setValue(StringUtils.defaultString(protocol.getName()));
        typeCombo.setValue(protocol.getProtocolType());
        maxTokensField.setValue(protocol.getMaxTokens());
        replyTokensField.setValue(protocol.getReplyTokens());

        temperatureEnabledCheckbox.setValue(Boolean.TRUE.equals(protocol.getTemperatureEnabled()));
        temperatureField.setValue(protocol.getTemperature());
        temperatureField.setEnabled(Boolean.TRUE.equals(protocol.getTemperatureEnabled()));

        topPEnabledCheckbox.setValue(Boolean.TRUE.equals(protocol.getTopPEnabled()));
        topPField.setValue(protocol.getTopP());
        topPField.setEnabled(Boolean.TRUE.equals(protocol.getTopPEnabled()));

        frequencyPenaltyEnabledCheckbox.setValue(Boolean.TRUE.equals(protocol.getFrequencyPenaltyEnabled()));
        frequencyPenaltyField.setValue(protocol.getFrequencyPenalty());
        frequencyPenaltyField.setEnabled(Boolean.TRUE.equals(protocol.getFrequencyPenaltyEnabled()));

        presencePenaltyEnabledCheckbox.setValue(Boolean.TRUE.equals(protocol.getPresencePenaltyEnabled()));
        presencePenaltyField.setValue(protocol.getPresencePenalty());
        presencePenaltyField.setEnabled(Boolean.TRUE.equals(protocol.getPresencePenaltyEnabled()));
    }

    private void save() {
        String name = nameField.getValue();
        ProtocolType type = typeCombo.getValue();

        if (StringUtils.isBlank(name) || type == null) {
            Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
            return;
        }

        try {
            if (protocol == null) {
                switch (type) {
                    case CHAT_COMPLETION -> protocol = new ChatCompletionProtocol();
                    default -> throw new IllegalArgumentException("Unsupported protocol type: " + type);
                }
            }

            protocol.setName(name.trim());
            protocol.setProtocolType(type);
            protocol.setMaxTokens(maxTokensField.getValue() != null ? maxTokensField.getValue() : 0);
            protocol.setReplyTokens(replyTokensField.getValue() != null ? replyTokensField.getValue() : 0);

            protocol.setTemperatureEnabled(temperatureEnabledCheckbox.getValue());
            protocol.setTemperature(temperatureField.getValue());

            protocol.setTopPEnabled(topPEnabledCheckbox.getValue());
            protocol.setTopP(topPField.getValue());

            protocol.setFrequencyPenaltyEnabled(frequencyPenaltyEnabledCheckbox.getValue());
            protocol.setFrequencyPenalty(frequencyPenaltyField.getValue());

            protocol.setPresencePenaltyEnabled(presencePenaltyEnabledCheckbox.getValue());
            protocol.setPresencePenalty(presencePenaltyField.getValue());

            protocolService.save(protocol);
            close();

            if (onSave != null) {
                onSave.run();
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    public Runnable getOnSave() {
        return onSave;
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }
}