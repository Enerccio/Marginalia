package com.github.enerccio.marginalia.extensions.loregenerator.ui;

import com.github.enerccio.marginalia.extensions.loregenerator.model.LoreGeneratorData;
import com.github.enerccio.marginalia.extensions.loregenerator.service.Defaults;
import com.github.enerccio.marginalia.extensions.loregenerator.service.LoreGeneratorService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * The Lore Generator section of the user settings.
 */
@Configurable(preConstruction = true)
public class LoreGeneratorSettingsForm extends VerticalLayout {

    @Autowired
    private Localization loc;

    private final LoreGeneratorService loreGeneratorService;

    private TextArea compactionPromptArea;

    private LoreGeneratorData data;

    public LoreGeneratorSettingsForm(LoreGeneratorService loreGeneratorService) {
        this.loreGeneratorService = loreGeneratorService;

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        compactionPromptArea = new TextArea("Compaction prompt");
        compactionPromptArea.setWidthFull();
        compactionPromptArea.setPlaceholder(Defaults.COMPACTION_PROMPT);
        ResizableTextArea.install(loc, compactionPromptArea, "160px");
        add(compactionPromptArea);

        refresh();
    }

    public void refresh() {
        try {
            data = loreGeneratorService.getData();
            compactionPromptArea.setValue(StringUtils.defaultString(data.getCompactionPrompt()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * The data with the values of the form; what the form does not show stays as it was loaded.
     */
    public LoreGeneratorData save() {
        if (data == null) {
            data = new LoreGeneratorData();
        }
        // empty means the default prompt is used
        data.setCompactionPrompt(StringUtils.defaultIfBlank(compactionPromptArea.getValue(), null));
        return data;
    }
}
