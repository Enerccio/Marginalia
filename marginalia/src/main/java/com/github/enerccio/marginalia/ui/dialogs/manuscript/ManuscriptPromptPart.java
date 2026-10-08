package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.service.TemplateService;
import com.github.enerccio.marginalia.domain.templates.MasterTemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateData;
import com.github.enerccio.marginalia.domain.templates.UserPromptData;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.TemplateHints;
import com.github.enerccio.marginalia.ui.widgets.TextAreaPopoverComponent;
import com.github.enerccio.marginalia.ui.widgets.TextFieldPopOverComponent;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.firitin.layouts.VTabSheet;


@Configurable
@Extendable
public class ManuscriptPromptPart implements ManuscriptDialogPart {

    @Autowired
    private Localization loc;

    @Autowired
    private SettingService settingService;

    @Autowired
    private TemplateService templateService;

    private final ManuscriptDialog parent;

    private TextAreaPopoverComponent masterTemplateField;
    private TextFieldPopOverComponent povField;
    private TextFieldPopOverComponent tenseField;
    private TextAreaPopoverComponent styleField;
    private TextAreaPopoverComponent userPromptField;
    private TextAreaPopoverComponent summaryField;

    private boolean loading = false;

    public ManuscriptPromptPart(ManuscriptDialog parent) {
        this.parent = parent;
    }

    @Override
    public Component create(VTabSheet container) throws Exception {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        createFields();

        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.getStyle().set("padding-bottom", "16px");

        formLayout.add(masterTemplateField);
        formLayout.setColspan(masterTemplateField, 2);

        formLayout.add(povField, tenseField);

        formLayout.add(styleField);
        formLayout.setColspan(styleField, 2);

        formLayout.add(userPromptField);
        formLayout.setColspan(userPromptField, 2);

        formLayout.add(summaryField);
        formLayout.setColspan(summaryField, 2);

        mainLayout.add(formLayout);

        container.add(loc.getValue(L.LABEL_PROMPT_PART), mainLayout);
        return mainLayout;
    }

    private void createFields() {
        masterTemplateField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_MASTER_TEMPLATE));
        masterTemplateField.setWidthFull();
        masterTemplateField.setMinHeight("180px");
        masterTemplateField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
        });

        userPromptField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_USER_PROMPT));
        userPromptField.setWidthFull();
        userPromptField.setMinHeight("180px");
        userPromptField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
        });

        summaryField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_SUMMARY_PROMPT));
        summaryField.setWidthFull();
        summaryField.setMinHeight("180px");
        summaryField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
        });

        styleField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_STYLE));
        styleField.setWidthFull();
        styleField.setMinHeight("120px");
        styleField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
        });

        povField = new TextFieldPopOverComponent(loc.getValue(L.LABEL_POV));
        povField.setWidthFull();
        povField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
        });

        tenseField = new TextFieldPopOverComponent(loc.getValue(L.LABEL_TENSE));
        tenseField.setWidthFull();
        tenseField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
        });
    }

    private Component createTemplateHintPopoverContent(HasValue<?, String> field, Popover popover, Class<? extends TemplateData> clazz, String defaultValue) {
        return TemplateHints.create(loc, field, popover, clazz, defaultValue);
    }

    private Manuscript refreshModel() throws Exception {
        return parent.refreshManuscript();
    }

    private void autosave() {
        if (loading) {
            return;
        }

        String masterTemplate = masterTemplateField.getValue();
        if (StringUtils.isNotBlank(masterTemplate)) {
            try {
                TemplateService.ValidationResult result = templateService.isValidTemplate(masterTemplate, "masterTemplate");
                if (!result.isValid()) {
                    Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + result.errorMessage());
                    return;
                }
            } catch (Exception e) {
                Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + e.getMessage());
                return;
            }
        }

        String userPrompt = userPromptField.getValue();
        if (StringUtils.isNotBlank(userPrompt)) {
            try {
                TemplateService.ValidationResult result = templateService.isValidTemplate(userPrompt, "userPrompt");
                if (!result.isValid()) {
                    Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + result.errorMessage());
                    return;
                }
            } catch (Exception e) {
                Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + e.getMessage());
                return;
            }
        }

        String summaryPrompt = summaryField.getValue();
        if (StringUtils.isNotBlank(summaryPrompt)) {
            try {
                TemplateService.ValidationResult result = templateService.isValidTemplate(summaryPrompt, "summaryPrompt");
                if (!result.isValid()) {
                    Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + result.errorMessage());
                    return;
                }
            } catch (Exception e) {
                Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + e.getMessage());
                return;
            }
        }

        try {
            Manuscript manuscript = refreshModel();
            if (manuscript == null) {
                return;
            }

            manuscript.setTemplate(masterTemplate);
            manuscript.setPov(povField.getValue());
            manuscript.setTense(tenseField.getValue());
            manuscript.setStyle(styleField.getValue());
            manuscript.setUserPrompt(userPrompt);
            manuscript.setSummaryPrompt(summaryPrompt);

            parent.save();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @Override
    public void setFrozen(boolean frozen) {
        // ignored, since tab will be unselectable
    }

    @Override
    public void load(Manuscript manuscript) {
        if (manuscript == null) {
            return;
        }
        loading = true;
        try {
            UserSetting userSetting = settingService.getOrCreate(UserSetting.class);

            String defaultMasterTemplate = StringUtils.isNotBlank(userSetting.getMasterTemplate())
                    ? userSetting.getMasterTemplate() : Defaults.DEFAULT_MASTER_TEMPLATE;
            String defaultPov = StringUtils.isNotBlank(userSetting.getDefaultPov())
                    ? userSetting.getDefaultPov() : Defaults.DEFAULT_POV;
            String defaultTense = StringUtils.isNotBlank(userSetting.getDefaultTense())
                    ? userSetting.getDefaultTense() : Defaults.DEFAULT_TENSE;
            String defaultStyle = StringUtils.isNotBlank(userSetting.getDefaultStyle())
                    ? userSetting.getDefaultStyle() : Defaults.DEFAULT_STYLE;
            String defaultUserPrompt = StringUtils.isNotBlank(userSetting.getDefaultUserPrompt())
                    ? userSetting.getDefaultUserPrompt() : Defaults.DEFAULT_USER_PROMPT;

            masterTemplateField.setPlaceholder(defaultMasterTemplate);
            povField.setPlaceholder(defaultPov);
            tenseField.setPlaceholder(defaultTense);
            styleField.setPlaceholder(defaultStyle);
            userPromptField.setPlaceholder(defaultUserPrompt);

            masterTemplateField.setValue(StringUtils.defaultString(manuscript.getTemplate()));
            povField.setValue(StringUtils.defaultString(manuscript.getPov()));
            tenseField.setValue(StringUtils.defaultString(manuscript.getTense()));
            styleField.setValue(StringUtils.defaultString(manuscript.getStyle()));
            userPromptField.setValue(StringUtils.defaultString(manuscript.getUserPrompt()));

            masterTemplateField.setPopoverContent(createTemplateHintPopoverContent(
                    masterTemplateField, masterTemplateField.getPopover(), MasterTemplateData.class, defaultMasterTemplate));
            userPromptField.setPopoverContent(createTemplateHintPopoverContent(
                    userPromptField, userPromptField.getPopover(), UserPromptData.class, defaultUserPrompt));
            styleField.setPopoverContent(createTemplateHintPopoverContent(
                    styleField, styleField.getPopover(), null, defaultStyle));
            povField.setPopoverContent(createTemplateHintPopoverContent(
                    povField, povField.getPopover(), null, defaultPov));
            tenseField.setPopoverContent(createTemplateHintPopoverContent(
                    tenseField, tenseField.getPopover(), null, defaultTense));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        } finally {
            loading = false;
        }
    }

    @Override
    public void onTabLeave() throws Exception {

    }

    @Override
    public void onTabEnter() throws Exception {

    }
}
