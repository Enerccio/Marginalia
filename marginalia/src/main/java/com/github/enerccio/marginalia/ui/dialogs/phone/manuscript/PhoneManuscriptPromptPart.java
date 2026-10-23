package com.github.enerccio.marginalia.ui.dialogs.phone.manuscript;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.service.TemplateService;
import com.github.enerccio.marginalia.domain.templates.*;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialogPart;
import com.github.enerccio.marginalia.ui.widgets.*;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;


@Configurable
@Extendable
/**
 * The prompts tab of the {@link PhoneManuscriptDialog}: every field in one column.
 */
public class PhoneManuscriptPromptPart implements PhoneManuscriptDialogPart {

    @Autowired
    private Localization loc;

    @Autowired
    private SettingService settingService;

    @Autowired
    private TemplateService templateService;

    private final PhoneManuscriptDialog parent;

    private TextAreaPopoverComponent masterTemplateField;
    private TextFieldPopOverComponent povField;
    private TextFieldPopOverComponent tenseField;
    private TextAreaPopoverComponent styleField;
    private TextAreaPopoverComponent userPromptField;
    private TextAreaPopoverComponent summaryField;
    private TextAreaPopoverComponent metaSummaryField;
    private TextAreaPopoverComponent redraftField;

    private boolean loading = false;

    public PhoneManuscriptPromptPart(PhoneManuscriptDialog parent) {
        this.parent = parent;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "mainLayout", type = VerticalLayout.class)})
    @Override
    public Component create() throws Exception {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);
        mainLayout.addClassName(CssStyles.OVERFLOW_AUTO);

        createFields();

        mainLayout.add(masterTemplateField, povField, tenseField, styleField, userPromptField, summaryField, metaSummaryField, redraftField);
        return mainLayout;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    private void createFields() {
        masterTemplateField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_MASTER_TEMPLATE));
        masterTemplateField.setWidthFull();
        ResizableTextArea.install(loc, masterTemplateField, "160px");
        masterTemplateField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
        });

        userPromptField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_USER_PROMPT));
        userPromptField.setWidthFull();
        ResizableTextArea.install(loc, userPromptField, "160px");
        userPromptField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
        });

        summaryField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_SUMMARY_PROMPT));
        summaryField.setWidthFull();
        ResizableTextArea.install(loc, summaryField, "160px");
        summaryField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
        });

        metaSummaryField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_META_SUMMARY_PROMPT));
        metaSummaryField.setWidthFull();
        ResizableTextArea.install(loc, metaSummaryField, "160px");
        metaSummaryField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
        });

        redraftField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_REDRAFT_PROMPT));
        redraftField.setWidthFull();
        ResizableTextArea.install(loc, redraftField, "160px");
        redraftField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
        });

        styleField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_STYLE));
        styleField.setWidthFull();
        ResizableTextArea.install(loc, styleField, "160px");
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

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "field", type = HasValue.class),
                    @APIParameter(name = "popover", type = Popover.class),
                    @APIParameter(name = "clazz", type = Class.class),
                    @APIParameter(name = "defaultValue", type = String.class)})
    private Component createTemplateHintPopoverContent(HasValue<?, String> field, Popover popover, Class<? extends TemplateData> clazz, String defaultValue) {
        return TemplateHints.create(loc, field, popover, clazz, defaultValue);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Manuscript.class))
    private Manuscript refreshModel() throws Exception {
        return parent.refreshManuscript();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "masterTemplate", type = String.class),
                    @APILocals(name = "userPrompt", type = String.class),
                    @APILocals(name = "summaryPrompt", type = String.class),
                    @APILocals(name = "metaSummaryPrompt", type = String.class),
                    @APILocals(name = "redraftPrompt", type = String.class),
                    @APILocals(name = "manuscript", type = Manuscript.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void autosave() {
        if (loading) {
            return;
        }

        if (!validateTemplate(masterTemplateField, "masterTemplate", MasterTemplateData.class)
                || !validateTemplate(userPromptField, "userPrompt", UserPromptData.class)
                || !validateTemplate(summaryField, "summaryPrompt", SummaryTemplateData.class)
                || !validateTemplate(metaSummaryField, "metaSummaryPrompt", MetaSummaryTemplateData.class)
                || !validateTemplate(redraftField, "redraftPrompt", RedraftTemplateData.class)) {
            return;
        }

        String masterTemplate = masterTemplateField.getValue();
        String userPrompt = userPromptField.getValue();
        String summaryPrompt = summaryField.getValue();
        String metaSummaryPrompt = metaSummaryField.getValue();
        String redraftPrompt = redraftField.getValue();

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
            manuscript.setMetaSummaryPrompt(metaSummaryPrompt);
            manuscript.setRedraftPrompt(redraftPrompt);

            parent.save();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * @return false if the template is invalid and must not be saved; unknown names are only shown as a warning
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = boolean.class),
            parameters = {@APIParameter(name = "field", type = TextAreaPopoverComponent.class),
                    @APIParameter(name = "templateName", type = String.class),
                    @APIParameter(name = "dataClass", type = Class.class)},
            locals = {@APILocals(name = "template", type = String.class),
                    @APILocals(name = "result", type = TemplateService.ValidationResult.class),
                    @APILocals(name = "e", type = Exception.class)})
    private boolean validateTemplate(TextAreaPopoverComponent field, String templateName, Class<? extends TemplateData> dataClass) {
        String template = field.getValue();
        if (StringUtils.isBlank(template)) {
            TemplateHints.showWarnings(loc, field, null);
            return true;
        }
        try {
            TemplateService.ValidationResult result = templateService.isValidTemplate(template, templateName, dataClass);
            if (!result.isValid()) {
                Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + result.errorMessage());
                return false;
            }
            TemplateHints.showWarnings(loc, field, result);
            return true;
        } catch (Exception e) {
            Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + e.getMessage());
            return false;
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "frozen", type = boolean.class)})
    @Override
    public void setFrozen(boolean frozen) {
        // ignored, since tab will be unselectable
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class)},
            locals = {@APILocals(name = "userSetting", type = UserSetting.class),
                    @APILocals(name = "defaultMasterTemplate", type = String.class),
                    @APILocals(name = "defaultPov", type = String.class),
                    @APILocals(name = "defaultTense", type = String.class),
                    @APILocals(name = "defaultStyle", type = String.class),
                    @APILocals(name = "defaultUserPrompt", type = String.class),
                    @APILocals(name = "defaultSummaryPrompt", type = String.class),
                    @APILocals(name = "defaultMetaSummaryPrompt", type = String.class),
                    @APILocals(name = "defaultRedraftPrompt", type = String.class),
                    @APILocals(name = "e", type = Exception.class)})
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
            String defaultSummaryPrompt = StringUtils.isNotBlank(userSetting.getDefaultSummaryPrompt())
                    ? userSetting.getDefaultSummaryPrompt() : Defaults.DEFAULT_SUMMARY_PROMPT;
            String defaultMetaSummaryPrompt = StringUtils.isNotBlank(userSetting.getDefaultMetaSummaryPrompt())
                    ? userSetting.getDefaultMetaSummaryPrompt() : Defaults.DEFAULT_META_SUMMARY;
            String defaultRedraftPrompt = StringUtils.isNotBlank(userSetting.getDefaultRedraftPrompt())
                    ? userSetting.getDefaultRedraftPrompt() : Defaults.DEFAULT_REDRAFT_PROMPT;

            masterTemplateField.setPlaceholder(defaultMasterTemplate);
            povField.setPlaceholder(defaultPov);
            tenseField.setPlaceholder(defaultTense);
            styleField.setPlaceholder(defaultStyle);
            userPromptField.setPlaceholder(defaultUserPrompt);
            summaryField.setPlaceholder(defaultSummaryPrompt);
            metaSummaryField.setPlaceholder(defaultMetaSummaryPrompt);
            redraftField.setPlaceholder(defaultRedraftPrompt);

            masterTemplateField.setValue(StringUtils.defaultString(manuscript.getTemplate()));
            povField.setValue(StringUtils.defaultString(manuscript.getPov()));
            tenseField.setValue(StringUtils.defaultString(manuscript.getTense()));
            styleField.setValue(StringUtils.defaultString(manuscript.getStyle()));
            userPromptField.setValue(StringUtils.defaultString(manuscript.getUserPrompt()));
            summaryField.setValue(StringUtils.defaultString(manuscript.getSummaryPrompt()));
            metaSummaryField.setValue(StringUtils.defaultString(manuscript.getMetaSummaryPrompt()));
            redraftField.setValue(StringUtils.defaultString(manuscript.getRedraftPrompt()));

            validateTemplate(masterTemplateField, "masterTemplate", MasterTemplateData.class);
            validateTemplate(userPromptField, "userPrompt", UserPromptData.class);
            validateTemplate(summaryField, "summaryPrompt", SummaryTemplateData.class);
            validateTemplate(metaSummaryField, "metaSummaryPrompt", MetaSummaryTemplateData.class);
            validateTemplate(redraftField, "redraftPrompt", RedraftTemplateData.class);

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
            summaryField.setPopoverContent(createTemplateHintPopoverContent(
                    summaryField, summaryField.getPopover(), SummaryTemplateData.class, defaultSummaryPrompt));
            metaSummaryField.setPopoverContent(createTemplateHintPopoverContent(
                    metaSummaryField, metaSummaryField.getPopover(), MetaSummaryTemplateData.class, defaultMetaSummaryPrompt));
            redraftField.setPopoverContent(createTemplateHintPopoverContent(
                    redraftField, redraftField.getPopover(), RedraftTemplateData.class, defaultRedraftPrompt));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        } finally {
            loading = false;
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void onTabLeave() throws Exception {

    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void onTabEnter() throws Exception {

    }
}
