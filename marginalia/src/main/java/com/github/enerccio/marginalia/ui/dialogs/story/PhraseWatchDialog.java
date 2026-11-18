package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.PhraseWatchService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.M_L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.WatchedPhrasesEditor;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * The phrase watch list of the book (the desktop and the phone story tab, Tools menu): whether the parts generated in it are scanned for
 * the phrases (a step after the automatic annotation, see {@link AutoAnnotateSteps}), the book's own entries and what it inherits from
 * the user settings and its folders, and a scan of the parts of the active branch that are already written.
 */
@Configurable(preConstruction = true)
public class PhraseWatchDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private PhraseWatchService phraseWatchService;

    private final Manuscript manuscript;
    private final Runnable onSaved;
    private final Checkbox enabled = new Checkbox();
    // the parts of the active branch the scan covers, both empty for all of it
    private final IntegerField fromPart = new IntegerField();
    private final IntegerField toPart = new IntegerField();
    private final WatchedPhrasesEditor phrases;

    /**
     * @param manuscript the book
     * @param onSaved    called after the settings were saved or the story was scanned
     */
    public static void open(Manuscript manuscript, Runnable onSaved) {
        PhraseWatchDialog dialog = create(manuscript, onSaved);
        if (dialog != null) {
            dialog.open();
        }
    }

    /**
     * @return the dialog, not opened; null when the book is gone (the author was told)
     */
    static PhraseWatchDialog create(Manuscript manuscript, Runnable onSaved) {
        PhraseWatchDialog dialog = new PhraseWatchDialog(manuscript, onSaved);
        return dialog.load() ? dialog : null;
    }

    private PhraseWatchDialog(Manuscript manuscript, Runnable onSaved) {
        this.manuscript = manuscript;
        this.onSaved = onSaved;
        this.phrases = new WatchedPhrasesEditor(loc);

        setHeaderTitle(loc.getValue(M_L.LABEL_PHRASE_WATCH));
        setWidth("min(900px, 95vw)");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.STRICT);

        Span info = new Span(loc.getValue(M_L.MSG_PHRASE_WATCH_INFO));
        info.addClassName(CssStyles.HINT_TEXT);
        enabled.setLabel(loc.getValue(M_L.LABEL_PHRASE_WATCH_ENABLE));

        for (IntegerField number : List.of(fromPart, toPart)) {
            number.setMin(1);
            number.setStepButtonsVisible(true);
        }
        fromPart.setLabel(loc.getValue(M_L.LABEL_ANNOTATE_FROM_PART));
        toPart.setLabel(loc.getValue(M_L.LABEL_ANNOTATE_TO_PART));
        Span rangeHint = new Span(loc.getValue(M_L.MSG_PHRASE_WATCH_RANGE_HINT));
        rangeHint.addClassName(CssStyles.HINT_TEXT);
        HorizontalLayout range = new HorizontalLayout(fromPart, toPart, rangeHint);
        range.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        range.setWrap(true);

        VerticalLayout content = new VerticalLayout(info, enabled, phrases, range);
        content.setPadding(false);
        content.setWidthFull();
        content.addClassName(CssStyles.GAP_S);
        add(content);

        Button scan = new Button(loc.getValue(M_L.LABEL_PHRASE_WATCH_SCAN), _ -> scan());
        Button save = new Button(loc.getValue(L.LABEL_SAVE), _ -> {
            if (save()) {
                Notification.success(loc.getValue(M_L.MSG_PHRASE_WATCH_SAVED));
                close();
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button close = new Button(loc.getValue(L.LABEL_CLOSE), _ -> close());
        getFooter().add(close, scan, save);
        // Enter in a number of the range scans it
        DialogShortcuts.submitWith(scan, fromPart, toPart);
    }

    private boolean load() {
        try {
            Manuscript stored = manuscriptService.find(manuscript);
            if (stored == null || stored.isDeleted()) {
                Notification.warning(loc.getValue(M_L.MSG_MANUSCRIPT_NOT_FOUND));
                return false;
            }
            enabled.setValue(stored.getPhraseWatch());
            phrases.setTemplates(phraseWatchService.getTemplates());
            phrases.setValue(stored.getWatchedPhrases());
            phrases.setInherited(phraseWatchService.resolveInherited(stored.getFolder()));
            return true;
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return false;
        }
    }

    /**
     * @return false when the list can't be saved (the author was told)
     */
    private boolean save() {
        if (!phrases.validate()) {
            return false;
        }
        try {
            phraseWatchService.setForBook(manuscript, phrases.getValue(), enabled.getValue());
            onSaved.run();
            return true;
        } catch (IllegalArgumentException e) {
            Notification.warning(String.format(loc.getValue(M_L.ERROR_PHRASE_INVALID), e.getMessage()));
            return false;
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return false;
        }
    }

    /**
     * Saves the list and annotates its hits in the parts of the active branch of the range, all of them when it is empty.
     */
    private void scan() {
        if (!save()) {
            return;
        }
        try {
            int made = phraseWatchService.scanRange(manuscript, fromPart.getValue(), toPart.getValue());
            Notification.success(String.format(loc.getValue(M_L.MSG_PHRASE_WATCH_SCANNED), made));
            close();
            onSaved.run();
        } catch (IllegalArgumentException e) {
            // a number outside of the branch
            Notification.warning(e.getMessage());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    // for the tests

    IntegerField fromPartField() {
        return fromPart;
    }

    IntegerField toPartField() {
        return toPart;
    }

    Checkbox enabledField() {
        return enabled;
    }

    WatchedPhrasesEditor phrasesEditor() {
        return phrases;
    }

    boolean saveForTest() {
        return save();
    }

    void scanForTest() {
        scan();
    }
}
