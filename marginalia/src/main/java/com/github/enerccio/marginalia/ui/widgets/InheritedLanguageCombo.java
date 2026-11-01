package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.service.EffectiveSetting;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.combobox.ComboBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * The language of a book or of a folder, where an empty box is not a language but "inherit": the book takes the language of
 * the nearest folder above it that sets one, else English. Like the fields of {@link PromptSettingsForm}, the empty box
 * shows what it inherits as its placeholder and says where that comes from under it.
 */
@Configurable
public class InheritedLanguageCombo extends ComboBox<Language> {

    @Autowired
    private Localization loc;

    @Autowired
    private ManuscriptService manuscriptService;

    private EffectiveSetting<Language> inherited;

    public InheritedLanguageCombo(String label) {
        super(label);
        setWidthFull();
        setItems(Language.values());
        setItemLabelGenerator(Language::getDisplayName);
        setAllowCustomValue(false);
        setClearButtonVisible(true);
        addValueChangeListener(_ -> refreshHelper());
    }

    /**
     * Puts the language into the box and works out what an empty box inherits.
     *
     * @param own           what the book or the folder sets itself, null when it does not
     * @param inheritedFrom the folder the book or folder is in (null: the top level)
     */
    public void load(Language own, Folder inheritedFrom) {
        try {
            inherited = manuscriptService.resolveInheritedLanguage(inheritedFrom);
            setPlaceholder(inherited.value().getDisplayName());
            setValue(own);
            refreshHelper();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void refreshHelper() {
        setHelperText(inherited == null || getValue() != null ? null : PromptSettingsForm.origin(loc, inherited));
    }
}
