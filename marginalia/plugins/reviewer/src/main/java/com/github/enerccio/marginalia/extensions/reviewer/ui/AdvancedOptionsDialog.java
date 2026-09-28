package com.github.enerccio.marginalia.extensions.reviewer.ui;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.reviewer.model.AdvancedOptions;
import com.github.enerccio.marginalia.extensions.reviewer.model.ReviewerSettings;
import com.github.enerccio.marginalia.extensions.reviewer.service.ReviewerService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;

import java.util.function.Consumer;

public class AdvancedOptionsDialog extends Dialog {

    private final AdvancedOptions options;
    private final Consumer<AdvancedOptions> onConfirm;

    private TextArea promptArea;
    private Checkbox usePromptInfo;
    private Checkbox lorebookBox;
    private IntegerField tokenLimitField;
    private ComboBox<String> lorebookTriggerCombo;

    public AdvancedOptionsDialog(ReviewerService reviewerService, Manuscript manuscript, ReviewerSettings settings, AdvancedOptions existingOptions, Consumer<AdvancedOptions> onConfirm) {
        this.options = existingOptions != null ? existingOptions : new AdvancedOptions();
        this.onConfirm = onConfirm;

        if (existingOptions == null) {
            options.setPrompt(settings.getReviewPrompt());
        }

        setHeaderTitle("Advanced Review Options");
        setWidth("600px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        promptArea = new TextArea("Prompt Override");
        promptArea.setWidthFull();
        promptArea.setMinHeight("120px");
        promptArea.setValue(options.getPrompt() != null ? options.getPrompt() : "");

        usePromptInfo = new Checkbox("Use standard prompt info", options.isUsePromptInfo());
        usePromptInfo.addValueChangeListener(e -> toggleFields(!e.getValue()));

        HorizontalLayout optionsRow = new HorizontalLayout();
        optionsRow.setWidthFull();
        optionsRow.setAlignItems(Alignment.BASELINE);

        lorebookBox = new Checkbox("Include Lorebook", options.isLorebook());

        tokenLimitField = new IntegerField("Token Limit");
        tokenLimitField.setValue(reviewerService.getTokenLimit(settings, manuscript));

        lorebookTriggerCombo = new ComboBox<>("Lorebook Trigger");
        lorebookTriggerCombo.setItems("review", "normal");
        lorebookTriggerCombo.setValue(options.getLorebookTrigger());

        optionsRow.add(lorebookBox, tokenLimitField, lorebookTriggerCombo);

        layout.add(promptArea, usePromptInfo, optionsRow);
        add(layout);

        toggleFields(!usePromptInfo.getValue());

        Button generateBtn = new Button("Generate", e -> {
            saveState();
            close();
            onConfirm.accept(options);
        });
        generateBtn.setThemeName("primary");

        Button cancelBtn = new Button("Cancel", e -> close());

        getFooter().add(cancelBtn, generateBtn);
    }

    private void toggleFields(boolean enabled) {
        lorebookBox.setEnabled(enabled);
        tokenLimitField.setEnabled(enabled);
        lorebookTriggerCombo.setEnabled(enabled);
    }

    private void saveState() {
        options.setPrompt(promptArea.getValue());
        options.setUsePromptInfo(usePromptInfo.getValue());
        options.setLorebook(lorebookBox.getValue());
        options.setTokenLimit(tokenLimitField.getValue() != null ? tokenLimitField.getValue() : 4096);
        options.setLorebookTrigger(lorebookTriggerCombo.getValue());
    }
}