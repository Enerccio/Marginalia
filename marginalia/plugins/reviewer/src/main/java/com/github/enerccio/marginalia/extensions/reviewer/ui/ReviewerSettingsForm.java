package com.github.enerccio.marginalia.extensions.reviewer.ui;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.extensions.reviewer.model.ReviewerSettings;
import com.github.enerccio.marginalia.extensions.reviewer.service.ReviewerService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

import java.util.List;

public class ReviewerSettingsForm extends VerticalLayout {

    private final ReviewerService reviewerService;
    private final SettingService settingService;
    private final AIService aiService;
    private final ProtocolService protocolService;
    private final Localization loc;

    private ComboBox<AI> overrideModelCombo;
    private ComboBox<Protocol> overrideProtocolCombo;
    private TextArea reviewPromptPreArea;
    private TextArea reviewPromptArea;
    private Checkbox removeInstructionBox;
    private Checkbox removeUserBox;
    private Checkbox removeCharacterBox;
    private Checkbox removeWorldInfoBox;

    private ReviewerSettings settings;

    public ReviewerSettingsForm(
            ReviewerService reviewerService,
            SettingService settingService,
            AIService aiService,
            ProtocolService protocolService,
            Localization loc) {

        this.reviewerService = reviewerService;
        this.settingService = settingService;
        this.aiService = aiService;
        this.protocolService = protocolService;
        this.loc = loc;

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        buildForm();
        refresh();
    }

    private void buildForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();

        overrideModelCombo = new ComboBox<>("Reviewer Model (Default: Manuscript AI)");
        overrideModelCombo.setWidthFull();
        overrideModelCombo.setItemLabelGenerator(AI::getName);
        overrideModelCombo.setClearButtonVisible(true);

        overrideProtocolCombo = new ComboBox<>("Reviewer Protocol (Default: Manuscript Protocol)");
        overrideProtocolCombo.setWidthFull();
        overrideProtocolCombo.setItemLabelGenerator(Protocol::getName);
        overrideProtocolCombo.setClearButtonVisible(true);

        formLayout.add(overrideModelCombo, overrideProtocolCombo);

        reviewPromptPreArea = new TextArea("Review Pre-Prompt (System)");
        reviewPromptPreArea.setWidthFull();
        reviewPromptPreArea.setMinHeight("80px");

        reviewPromptArea = new TextArea("Review Post-Prompt (User)");
        reviewPromptArea.setWidthFull();
        reviewPromptArea.setMinHeight("120px");

        HorizontalLayout checkboxesLayout = new HorizontalLayout();
        checkboxesLayout.setWidthFull();
        checkboxesLayout.setSpacing(true);

        removeInstructionBox = new Checkbox("Remove instructions");
        removeUserBox = new Checkbox("Remove persona");
        removeCharacterBox = new Checkbox("Remove character");
        removeWorldInfoBox = new Checkbox("Remove world info");

        checkboxesLayout.add(removeInstructionBox, removeUserBox, removeCharacterBox, removeWorldInfoBox);

        Button saveBtn = new Button(loc.getValue(L.LABEL_SAVE), e -> save());
        saveBtn.setThemeName("primary");

        add(formLayout, reviewPromptPreArea, reviewPromptArea, checkboxesLayout, saveBtn);
    }

    public void refresh() {
        try {
            settings = reviewerService.getSettings(settingService);

            // Populate AI models and validate selection via find()
            List<AI> allModels = aiService.findAllForUser();
            overrideModelCombo.setItems(allModels);
            if (settings.getSelectedAiId() != null) {
                AI validAi = aiService.find(settings.getSelectedAiId());
                overrideModelCombo.setValue(validAi != null && !validAi.isDeleted() ? validAi : null);
            } else {
                overrideModelCombo.setValue(null);
            }

            // Populate Protocols and validate selection via find()
            List<Protocol> allProtocols = protocolService.findAllForUser();
            overrideProtocolCombo.setItems(allProtocols);
            if (settings.getSelectedProtocolId() != null) {
                Protocol validProtocol = protocolService.find(settings.getSelectedProtocolId());
                overrideProtocolCombo.setValue(validProtocol != null && !validProtocol.isDeleted() ? validProtocol : null);
            } else {
                overrideProtocolCombo.setValue(null);
            }

            reviewPromptPreArea.setValue(settings.getReviewPromptPre() != null ? settings.getReviewPromptPre() : "");
            reviewPromptArea.setValue(settings.getReviewPrompt() != null ? settings.getReviewPrompt() : "");
            removeInstructionBox.setValue(settings.isRemoveInstruction());
            removeUserBox.setValue(settings.isRemoveUser());
            removeCharacterBox.setValue(settings.isRemoveCharacter());
            removeWorldInfoBox.setValue(settings.isRemoveWorldInfo());

        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void save() {
        try {
            if (settings == null) {
                settings = new ReviewerSettings();
            }

            AI selectedAi = overrideModelCombo.getValue();
            settings.setSelectedAiId(selectedAi != null ? selectedAi.getId() : null);

            Protocol selectedProtocol = overrideProtocolCombo.getValue();
            settings.setSelectedProtocolId(selectedProtocol != null ? selectedProtocol.getId() : null);

            settings.setReviewPromptPre(reviewPromptPreArea.getValue());
            settings.setReviewPrompt(reviewPromptArea.getValue());
            settings.setRemoveInstruction(removeInstructionBox.getValue());
            settings.setRemoveUser(removeUserBox.getValue());
            settings.setRemoveCharacter(removeCharacterBox.getValue());
            settings.setRemoveWorldInfo(removeWorldInfoBox.getValue());

            reviewerService.saveSettings(settingService, settings);
            Notification.success(loc.getValue(L.MSG_SETTINGS_SAVED));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}