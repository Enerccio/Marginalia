package com.github.enerccio.marginalia.extensions.reviewer.ui;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.extensions.reviewer.model.AdvancedOptions;
import com.github.enerccio.marginalia.extensions.reviewer.model.ReviewerSetting;
import com.github.enerccio.marginalia.extensions.reviewer.service.ReviewerService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.function.Consumer;

@Configurable
public class AdvancedOptionsDialog extends Dialog {
    private static final Gson gson = new GsonBuilder().create();

    @Autowired
    private Localization loc;

    private final AdvancedOptions options;
    private final ReviewerSetting setting;
    private final Manuscript manuscript;
    private final ReviewerService reviewerService;
    private final InferenceService inferenceService;
    private final ChatMessage targetMessage;

    private TextArea promptArea;
    private Checkbox usePromptInfo;
    private Checkbox lorebookBox;
    private IntegerField tokenLimitField;
    private Span counter;

    public AdvancedOptionsDialog(ReviewerService reviewerService, InferenceService inferenceService, Manuscript manuscript,
                                 ReviewerSetting setting, ChatMessage targetMessage, AdvancedOptions existingOptions, Consumer<AdvancedOptions> onConfirm) throws Exception {
        this.reviewerService = reviewerService;
        this.setting = setting;
        this.manuscript = manuscript;
        this.inferenceService = inferenceService;
        this.targetMessage = targetMessage;
        this.options = existingOptions != null ? existingOptions : new AdvancedOptions();

        if (existingOptions == null) {
            options.setPrompt(setting.getReviewPrompt());
        }

        setHeaderTitle("Advanced Review Options");
        setWidth("900px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        promptArea = new TextArea("Prompt Override");
        promptArea.setWidthFull();
        ResizableTextArea.install(loc, promptArea, "160px");
        promptArea.setValue(options.getPrompt() != null ? options.getPrompt() : "");
        promptArea.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                refreshCount();
            }
        });

        usePromptInfo = new Checkbox("Use standard prompt info", options.isUsePromptInfo());
        usePromptInfo.addValueChangeListener(e -> toggleFields(!e.getValue()));
        usePromptInfo.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                refreshCount();
            }
        });

        counter = new Span("");
        counter.setWidthFull();

        HorizontalLayout optionsRow = new HorizontalLayout();
        optionsRow.setWidthFull();
        optionsRow.setAlignItems(Alignment.BASELINE);

        lorebookBox = new Checkbox("Include Lorebook", options.isLorebook());
        lorebookBox.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                refreshCount();
            }
        });

        tokenLimitField = new IntegerField("Token Limit");
        tokenLimitField.setValue(reviewerService.getTokenLimit(setting, manuscript));
        tokenLimitField.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                refreshCount();
            }
        });

        optionsRow.add(lorebookBox, tokenLimitField);

        layout.add(promptArea, usePromptInfo, counter, optionsRow);
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

    private void refreshCount() {
        counter.setText("");
        if (!usePromptInfo.getValue()) {
            try {
                saveState();
                List<LLMChatMessage> prompt = reviewerService.buildChatCompletePrompts(inferenceService, targetMessage, setting, options);
                String text = gson.toJson(prompt);
                counter.setText("Total tokens: " + inferenceService.countTokensApprox(text));
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }
    }

    private void toggleFields(boolean enabled) {
        lorebookBox.setEnabled(enabled);
        tokenLimitField.setEnabled(enabled);
    }

    private void saveState() {
        try {
            options.setPrompt(promptArea.getValue());
            options.setUsePromptInfo(usePromptInfo.getValue());
            options.setLorebook(lorebookBox.getValue());
            options.setTokenLimit(tokenLimitField.getValue() != null ? tokenLimitField.getValue() :
                    reviewerService.getTokenLimit(setting, manuscript));

        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}