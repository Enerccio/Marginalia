package com.github.enerccio.marginalia.extensions.bookdescription.ui;

import com.github.enerccio.marginalia.extensions.bookdescription.model.BookDescriptionSettings;
import com.github.enerccio.marginalia.extensions.bookdescription.service.BookDescriptionService;
import com.github.enerccio.marginalia.extensions.bookdescription.service.Defaults;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.ui.widgets.TemplateHints;
import com.github.enerccio.marginalia.ui.widgets.TextAreaPopoverComponent;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import static com.github.enerccio.marginalia.extensions.bookdescription.BookDescriptionLocalization.*;

/**
 * The Book Description Generator section of the user settings: the user level of the two prompts, which books and folders inherit.
 */
@Configurable(preConstruction = true)
public class BookDescriptionSettingsForm extends VerticalLayout {

    @Autowired
    private Localization loc;

    private final BookDescriptionService service;

    private final TextAreaPopoverComponent descriptionPromptArea;
    private final TextAreaPopoverComponent notesPromptArea;

    private BookDescriptionSettings settings;

    public BookDescriptionSettingsForm(BookDescriptionService service) {
        this.service = service;

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        descriptionPromptArea = prompt(T.localized(BOOK_DESCRIPTION_PROMPT), T.localized(THE_PROMPT_THAT_WRITES_THE_NAME), Defaults.DESCRIPTION_PROMPT);
        notesPromptArea = prompt(T.localized(BOOK_NOTES_PROMPT), T.localized(THE_PROMPT_THAT_WRITES_NOTES), Defaults.NOTES_PROMPT);
        add(descriptionPromptArea, notesPromptArea);

        refresh();
    }

    private TextAreaPopoverComponent prompt(String label, String help, String defaultPrompt) {
        TextAreaPopoverComponent area = new TextAreaPopoverComponent(label);
        area.setWidthFull();
        area.setPlaceholder(defaultPrompt);
        area.setHelperText(help);
        ResizableTextArea.install(loc, area, "240px");
        area.setPopoverContent(TemplateHints.create(loc, area, area.getPopover(), null, defaultPrompt));
        return area;
    }

    public void refresh() {
        try {
            settings = service.getSettings();
            descriptionPromptArea.setValue(StringUtils.defaultString(settings.getDescriptionPrompt()));
            notesPromptArea.setValue(StringUtils.defaultString(settings.getNotesPrompt()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * The settings with the values of the form; what the form does not show stays as it was loaded.
     */
    public BookDescriptionSettings save() {
        if (settings == null) {
            settings = new BookDescriptionSettings();
        }
        // empty means the inherited or the built-in prompt is used
        settings.setDescriptionPrompt(StringUtils.defaultIfBlank(descriptionPromptArea.getValue(), null));
        settings.setNotesPrompt(StringUtils.defaultIfBlank(notesPromptArea.getValue(), null));
        return settings;
    }
}
