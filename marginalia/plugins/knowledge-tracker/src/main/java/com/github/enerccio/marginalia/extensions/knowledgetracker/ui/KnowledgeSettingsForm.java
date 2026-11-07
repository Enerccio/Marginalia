package com.github.enerccio.marginalia.extensions.knowledgetracker.ui;

import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ExtractionPrompt;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.TidyPrompt;
import com.github.enerccio.marginalia.extensions.knowledgetracker.service.KnowledgeInherited;
import com.github.enerccio.marginalia.extensions.knowledgetracker.service.KnowledgeStateService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.ui.widgets.TemplateHints;
import com.github.enerccio.marginalia.ui.widgets.TextAreaPopoverComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import static com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerLocalization.*;

/**
 * The Knowledge Tracker section of the user settings: the user level of the two prompts, which books and folders inherit.
 */
@Configurable(preConstruction = true)
public class KnowledgeSettingsForm extends VerticalLayout {

    @Autowired
    private Localization loc;

    private final KnowledgeStateService service;

    private final TextAreaPopoverComponent extractionPromptArea;
    private final TextAreaPopoverComponent tidyPromptArea;

    public KnowledgeSettingsForm(KnowledgeStateService service) {
        this.service = service;

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        extractionPromptArea = prompt(T.localized(KNOWLEDGE_EXTRACTION_PROMPT), T.localized(THE_SYSTEM_MESSAGE_THAT_TELLS_THE_MODEL), ExtractionPrompt.SYSTEM);
        tidyPromptArea = prompt(T.localized(KNOWLEDGE_TIDY_PROMPT), T.localized(THE_SYSTEM_MESSAGE_OF_THE_TIDY_PASS), TidyPrompt.SYSTEM);
        add(extractionPromptArea, tidyPromptArea);

        refresh();
    }

    private TextAreaPopoverComponent prompt(String label, String help, String builtIn) {
        TextAreaPopoverComponent area = new TextAreaPopoverComponent(label);
        area.setWidthFull();
        area.setPlaceholder(builtIn);
        area.setHelperText(help);
        ResizableTextArea.install(loc, area, "240px");
        area.setPopoverContent(TemplateHints.create(loc, area, area.getPopover(), null, builtIn));
        return area;
    }

    public void refresh() {
        extractionPromptArea.setValue(StringUtils.defaultString(service.getUserPrompt(KnowledgeInherited.EXTRACTION_PROMPT)));
        tidyPromptArea.setValue(StringUtils.defaultString(service.getUserPrompt(KnowledgeInherited.TIDY_PROMPT)));
    }

    /** Puts the values of the form into the user settings; an empty one means the inherited or the built-in prompt is used. */
    public void save(UserSetting userSetting) {
        service.putUserPrompt(userSetting, KnowledgeInherited.EXTRACTION_PROMPT, extractionPromptArea.getValue());
        service.putUserPrompt(userSetting, KnowledgeInherited.TIDY_PROMPT, tidyPromptArea.getValue());
    }
}
