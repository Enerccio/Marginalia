package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.domain.service.EffectiveHistoryLimit;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.textfield.IntegerField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * The most versions of a part the history keeps, as a book, a folder or the user sets it. An empty box sets no limit at this level.
 * The limit the levels above set shows as the placeholder, and under the box, where it limits more than the box itself, which level
 * it is: the strictest limit of all levels is the one that counts, so a larger number than the one above has no effect.
 */
@Configurable
public class HistoryLimitField extends IntegerField {

    @Autowired
    private Localization loc;

    private EffectiveHistoryLimit inherited;

    public HistoryLimitField(String label) {
        super(label);
        setWidthFull();
        setMin(0);
        setStepButtonsVisible(true);
        setClearButtonVisible(true);
        addValueChangeListener(_ -> refreshHelper());
    }

    /**
     * Puts the number into the box and works out what the levels above set.
     *
     * @param own       what this level sets, null when it sets no limit
     * @param inherited what the levels above it limit to, see {@code ManuscriptService#resolveInheritedHistoryLimit}
     */
    public void load(Integer own, EffectiveHistoryLimit inherited) {
        this.inherited = inherited;
        setPlaceholder(inherited == null || inherited.isUnlimited() ? "" : String.valueOf(inherited.value()));
        setValue(own);
        refreshHelper();
    }

    private void refreshHelper() {
        Integer own = getValue();
        boolean limitedAbove = inherited != null && !inherited.isUnlimited() && (own == null || own > inherited.value());
        if (limitedAbove) {
            setHelperText(origin(loc, inherited));
        } else if (inherited != null && inherited.isUnlimited() && own == null) {
            setHelperText(loc.getValue(L.MSG_HISTORY_LIMIT_NONE));
        } else {
            setHelperText(loc.getValue(L.HELP_HISTORY_MAX_VERSIONS));
        }
    }

    /**
     * Which level limits the history, as the text under the box.
     */
    public static String origin(Localization loc, EffectiveHistoryLimit limit) {
        return switch (limit.origin()) {
            case FOLDER -> String.format(loc.getValue(L.MSG_HISTORY_LIMIT_FOLDER), limit.value(), limit.folder().getName());
            case USER -> String.format(loc.getValue(L.MSG_HISTORY_LIMIT_USER), limit.value());
            case PERMISSION -> String.format(loc.getValue(L.MSG_HISTORY_LIMIT_PERMISSION), limit.value());
            default -> loc.getValue(L.MSG_HISTORY_LIMIT_NONE);
        };
    }
}
