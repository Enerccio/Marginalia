package com.github.enerccio.marginalia.extensions.translatorcommon.ui;

import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Span;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.TranslationWriter;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;

import static com.github.enerccio.marginalia.extensions.translatorcommon.ui.TranslatorCommonLocalization.*;

/**
 * The window that edits the English source of one translated speech: the author fixed the translation (or the model got the English wrong) and says
 * what the speech means, so that the models read it. It shows the translation as the story has it now, for reference.
 */
public class SourceEditDialog extends EditDialog {

    private final TextArea source = new TextArea(TC.localized(THE_ENGLISH_SOURCE));

    /**
     * @param currentText the translated text of the speech now, null when it is not known
     */
    public SourceEditDialog(AnchoredPassage passage, Span span, String currentText) {
        super(TC.localized(EDIT_THE_SOURCE), null);
        if (StringUtils.isNotEmpty(currentText)) {
            TextArea translation = new TextArea(TC.localized(THE_TRANSLATION));
            translation.setValue(currentText);
            translation.setReadOnly(true);
            translation.setWidthFull();
            body().add(translation);
        }
        source.setValue(span.getSource());
        source.setWidthFull();
        body().add(source);
        Div hint = new Div(TC.localized(THE_TRANSLATION_COUNTS_AS_UP_TO_DATE));
        hint.addClassName(CommonStyles.HINT);
        body().add(hint);
        String uuid = passage.getUuid();
        String kind = passage.getPassageKind();
        setValidator(() -> {
            if (StringUtils.isBlank(source.getValue())) {
                return TC.localized(THE_SOURCE_IS_EMPTY);
            }
            try {
                new TranslationWriter(kind).editSource(uuid, source.getValue());
            } catch (Exception e) {
                return e.getMessage();
            }
            Notification.success(TC.localized(THE_SOURCE_WAS_SAVED));
            return null;
        });
    }
}
