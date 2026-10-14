package com.github.enerccio.marginalia.extensions.reviewer.ui;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.reviewer.model.ReviewerProfile;
import com.github.enerccio.marginalia.extensions.reviewer.model.ReviewerSettings;
import com.github.enerccio.marginalia.extensions.reviewer.service.ReviewerService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.combobox.ComboBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * Chooses the reviewer profile of a book, it takes priority over the one selected in the settings. Saves the choice
 * with the book as soon as it is made.
 */
@Configurable(preConstruction = true)
public class ManuscriptProfileSelect extends ComboBox<ReviewerProfile> {

    @Autowired
    private Localization loc;

    private final ReviewerService reviewerService;
    private Manuscript manuscript;

    public ManuscriptProfileSelect(ReviewerService reviewerService) {
        super("Reviewer Profile (Default: selected in settings)");
        this.reviewerService = reviewerService;

        setWidthFull();
        setAllowCustomValue(false);
        setClearButtonVisible(true);
        setItemLabelGenerator(ReviewerProfile::name);
        addValueChangeListener(e -> {
            if (e.isFromClient() && manuscript != null) {
                try {
                    manuscript = reviewerService.saveManuscriptProfile(manuscript,
                            e.getValue() != null ? e.getValue().uuid() : null);
                } catch (Exception ex) {
                    UIUtils.internalServerError(loc, ex);
                }
            }
        });
    }

    public void load(Manuscript manuscript) {
        this.manuscript = manuscript;
        try {
            ReviewerSettings settings = reviewerService.getSettings();
            List<ReviewerProfile> profiles = reviewerService.getProfiles(settings);
            setItems(profiles);
            String uuid = reviewerService.getManuscriptProfile(manuscript);
            setValue(profiles.stream().filter(p -> p.uuid().equals(uuid)).findFirst().orElse(null));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
