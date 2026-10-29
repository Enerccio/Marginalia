package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.ModelProtocolPicker;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Sets the automatic annotation of the book (the desktop and the phone story tab, Tools menu): the model that annotates (none is the
 * model of the book), what to look for (as in {@link AnnotateDialog}), whether it is on, whether the notes the model found are added
 * without asking, and, only with that, whether the part is redrafted to address them and whether that redraft is applied without
 * asking. See {@link AutoAnnotate} for what the settings do.
 */
@Configurable(preConstruction = true)
public class AutoAnnotateDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private ManuscriptService manuscriptService;

    private final Manuscript manuscript;
    private final Runnable onSaved;

    private final ModelProtocolPicker model = new ModelProtocolPicker();
    private final TextArea instructions = new TextArea();
    private final Checkbox enabled = new Checkbox();
    private final Checkbox accept = new Checkbox();
    private final Checkbox redraft = new Checkbox();
    private final Checkbox acceptRedraft = new Checkbox();

    /**
     * @param manuscript the book, as it is stored now
     * @param onSaved    called after the settings were saved
     */
    public static void open(Manuscript manuscript, Runnable onSaved) {
        create(manuscript, onSaved).open();
    }

    /**
     * @return the dialog, not opened
     */
    static AutoAnnotateDialog create(Manuscript manuscript, Runnable onSaved) {
        return new AutoAnnotateDialog(manuscript, onSaved);
    }

    private AutoAnnotateDialog(Manuscript manuscript, Runnable onSaved) {
        this.manuscript = manuscript;
        this.onSaved = onSaved;

        setHeaderTitle(loc.getValue(L.LABEL_AUTO_ANNOTATE));
        setWidth("min(700px, 95vw)");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.STRICT);

        Span info = new Span(loc.getValue(L.MSG_AUTO_ANNOTATE_INFO));
        info.addClassName(CssStyles.HINT_TEXT);

        try {
            model.setSelection(AutoAnnotate.byUuid(aiService.findAllForUser(), manuscript.getAutoAnnotateModel()),
                    AutoAnnotate.byUuid(protocolService.findAllForUser(), manuscript.getAutoAnnotateProtocol()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        Span modelHint = new Span(loc.getValue(L.MSG_AUTO_ANNOTATE_MODEL_HINT));
        modelHint.addClassName(CssStyles.HINT_TEXT);

        instructions.setLabel(loc.getValue(L.LABEL_ANNOTATE_INSTRUCTIONS));
        instructions.setPlaceholder(loc.getValue(L.MSG_ANNOTATE_PLACEHOLDER));
        instructions.setValue(StringUtils.defaultString(manuscript.getAutoAnnotateInstructions()));
        instructions.setWidthFull();
        instructions.setMinRows(2);
        instructions.setMaxRows(6);

        enabled.setLabel(loc.getValue(L.LABEL_AUTO_ANNOTATE_ENABLE));
        enabled.setValue(manuscript.getAutoAnnotate());
        accept.setLabel(loc.getValue(L.LABEL_AUTO_ANNOTATE_ACCEPT));
        accept.setValue(manuscript.getAutoAnnotateAccept());
        redraft.setLabel(loc.getValue(L.LABEL_AUTO_ANNOTATE_REDRAFT));
        redraft.setValue(manuscript.getAutoAnnotateAccept() && manuscript.getAutoAnnotateRedraft());
        acceptRedraft.setLabel(loc.getValue(L.LABEL_AUTO_ANNOTATE_ACCEPT_REDRAFT));
        acceptRedraft.setValue(redraft.getValue() && manuscript.getAutoAnnotateAcceptRedraft());
        accept.addValueChangeListener(_ -> refreshDependent());
        redraft.addValueChangeListener(_ -> refreshDependent());
        refreshDependent();

        VerticalLayout content = new VerticalLayout(info, model, modelHint, instructions, enabled, accept, redraft, acceptRedraft);
        content.setPadding(false);
        content.setWidthFull();
        content.addClassName(CssStyles.GAP_S);
        add(content);

        Button save = new Button(loc.getValue(L.LABEL_SAVE), _ -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button close = new Button(loc.getValue(L.LABEL_CLOSE), _ -> close());
        getFooter().add(close, save);
    }

    /**
     * The redraft needs the notes to be added without asking, and its own automatic apply needs the redraft.
     */
    private void refreshDependent() {
        if (!accept.getValue()) {
            redraft.setValue(false);
        }
        redraft.setEnabled(accept.getValue());
        if (!redraft.getValue()) {
            acceptRedraft.setValue(false);
        }
        acceptRedraft.setEnabled(accept.getValue() && redraft.getValue());
    }

    private void save() {
        try {
            boolean redrafts = accept.getValue() && redraft.getValue();
            // the book is loaded again, the window has had it since it opened
            manuscriptService.update(manuscript, book -> {
                book.setAutoAnnotate(enabled.getValue());
                book.setAutoAnnotateInstructions(StringUtils.defaultIfBlank(instructions.getValue(), null));
                book.setAutoAnnotateModel(model.getAi() == null ? null : model.getAi().getUuid());
                book.setAutoAnnotateProtocol(model.getProtocol() == null ? null : model.getProtocol().getUuid());
                book.setAutoAnnotateAccept(accept.getValue());
                book.setAutoAnnotateRedraft(redrafts);
                book.setAutoAnnotateAcceptRedraft(redrafts && acceptRedraft.getValue());
            });
            Notification.success(loc.getValue(L.MSG_AUTO_ANNOTATE_SAVED));
            close();
            onSaved.run();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    // for the tests

    Checkbox enabledField() {
        return enabled;
    }

    Checkbox acceptField() {
        return accept;
    }

    Checkbox redraftField() {
        return redraft;
    }

    Checkbox acceptRedraftField() {
        return acceptRedraft;
    }

    TextArea instructionsField() {
        return instructions;
    }

    ModelProtocolPicker modelPicker() {
        return model;
    }

    void saveForTest() {
        save();
    }
}
