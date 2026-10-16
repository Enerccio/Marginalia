package com.github.enerccio.marginalia.extensions.arcoutline.ui;

import com.github.enerccio.marginalia.extensions.arcoutline.model.ArcOutlineSettings;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService;
import com.github.enerccio.marginalia.extensions.arcoutline.service.Defaults;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.ui.widgets.TemplateHints;
import com.github.enerccio.marginalia.ui.widgets.TextAreaPopoverComponent;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * The Arc Outline section of the user settings.
 */
@Configurable(preConstruction = true)
public class ArcOutlineSettingsForm extends VerticalLayout {

    @Autowired
    private Localization loc;

    private final ArcOutlineService service;

    private final TextAreaPopoverComponent generationPromptArea;
    private final TextAreaPopoverComponent chapterGenerationPromptArea;
    private final TextAreaPopoverComponent importPromptArea;
    private final TextAreaPopoverComponent nameToolsPromptArea;
    private final TextAreaPopoverComponent nameProviderPromptArea;

    private ArcOutlineSettings settings;

    public ArcOutlineSettingsForm(ArcOutlineService service) {
        this.service = service;

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        generationPromptArea = new TextAreaPopoverComponent("Arc generation prompt");
        generationPromptArea.setWidthFull();
        generationPromptArea.setPlaceholder(Defaults.GENERATION_PROMPT);
        generationPromptArea.setHelperText("Template. Variables: {{backgroundLore}}, {{existingArcs}}, {{specification}}. Empty: the default prompt.");
        ResizableTextArea.install(loc, generationPromptArea, "240px");
        generationPromptArea.setPopoverContent(TemplateHints.create(loc, generationPromptArea,
                generationPromptArea.getPopover(), null, Defaults.GENERATION_PROMPT));
        add(generationPromptArea);

        chapterGenerationPromptArea = new TextAreaPopoverComponent("Chapter generation prompt");
        chapterGenerationPromptArea.setWidthFull();
        chapterGenerationPromptArea.setPlaceholder(Defaults.CHAPTER_GENERATION_PROMPT);
        chapterGenerationPromptArea.setHelperText("Template. Variables: {{backgroundLore}}, {{outline}}, {{numberOfChapters}}, {{generalSpecification}}, {{chapterSpecification}}. Empty: the default prompt.");
        ResizableTextArea.install(loc, chapterGenerationPromptArea, "240px");
        chapterGenerationPromptArea.setPopoverContent(TemplateHints.create(loc, chapterGenerationPromptArea,
                chapterGenerationPromptArea.getPopover(), null, Defaults.CHAPTER_GENERATION_PROMPT));
        add(chapterGenerationPromptArea);

        importPromptArea = new TextAreaPopoverComponent("Story import prompt");
        importPromptArea.setWidthFull();
        importPromptArea.setPlaceholder(Defaults.IMPORT_PROMPT);
        importPromptArea.setHelperText("Template. Variables: {{backgroundLore}}, {{existingArcs}}, {{story}}, {{numberOfChapters}}. Empty: the default prompt.");
        ResizableTextArea.install(loc, importPromptArea, "240px");
        importPromptArea.setPopoverContent(TemplateHints.create(loc, importPromptArea,
                importPromptArea.getPopover(), null, Defaults.IMPORT_PROMPT));
        add(importPromptArea);

        nameToolsPromptArea = new TextAreaPopoverComponent("Name generator hint (added to the arc generation prompt when categories of the Name Generator are selected)");
        nameToolsPromptArea.setWidthFull();
        nameToolsPromptArea.setPlaceholder(Defaults.NAME_TOOLS_PROMPT);
        ResizableTextArea.install(loc, nameToolsPromptArea, "100px");
        nameToolsPromptArea.setPopoverContent(TemplateHints.create(loc, nameToolsPromptArea,
                nameToolsPromptArea.getPopover(), null, Defaults.NAME_TOOLS_PROMPT));
        add(nameToolsPromptArea);

        nameProviderPromptArea = new TextAreaPopoverComponent("Name provider hint (added to the arc generation prompt when categories of the Name Provider are selected)");
        nameProviderPromptArea.setWidthFull();
        nameProviderPromptArea.setPlaceholder(Defaults.NAME_PROVIDER_PROMPT);
        ResizableTextArea.install(loc, nameProviderPromptArea, "100px");
        nameProviderPromptArea.setPopoverContent(TemplateHints.create(loc, nameProviderPromptArea,
                nameProviderPromptArea.getPopover(), null, Defaults.NAME_PROVIDER_PROMPT));
        add(nameProviderPromptArea);

        refresh();
    }

    public void refresh() {
        try {
            settings = service.getSettings();
            generationPromptArea.setValue(StringUtils.defaultString(settings.getGenerationPrompt()));
            chapterGenerationPromptArea.setValue(StringUtils.defaultString(settings.getChapterGenerationPrompt()));
            importPromptArea.setValue(StringUtils.defaultString(settings.getImportPrompt()));
            nameToolsPromptArea.setValue(StringUtils.defaultString(settings.getNameToolsPrompt()));
            nameProviderPromptArea.setValue(StringUtils.defaultString(settings.getNameProviderPrompt()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * The settings with the values of the form; what the form does not show stays as it was loaded.
     */
    public ArcOutlineSettings save() {
        if (settings == null) {
            settings = new ArcOutlineSettings();
        }
        // empty means the default prompt is used
        settings.setGenerationPrompt(StringUtils.defaultIfBlank(generationPromptArea.getValue(), null));
        settings.setChapterGenerationPrompt(StringUtils.defaultIfBlank(chapterGenerationPromptArea.getValue(), null));
        settings.setImportPrompt(StringUtils.defaultIfBlank(importPromptArea.getValue(), null));
        settings.setNameToolsPrompt(StringUtils.defaultIfBlank(nameToolsPromptArea.getValue(), null));
        settings.setNameProviderPrompt(StringUtils.defaultIfBlank(nameProviderPromptArea.getValue(), null));
        return settings;
    }
}
