package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.domain.components.BookAuthorship;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.MessageRevisionService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.function.Supplier;

/**
 * How much of the text of the active branch a model wrote and how much the author wrote. It is worked out from the history of every part
 * (see {@code MessageRevisionService#authorship}), which reads all the versions of all the parts, so it is done when the button is
 * pressed, not whenever the tab is shown.
 */
@Configurable(preConstruction = true)
public class AuthorshipField extends TextField {

    @Autowired
    private Localization loc;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private MessageRevisionService messageRevisionService;

    private final Supplier<Manuscript> book;

    /**
     * @param book gives the book with the up to date active leaf
     */
    public AuthorshipField(Supplier<Manuscript> book) {
        this.book = book;
        setLabel(loc.getValue(L.LABEL_AUTHORSHIP));
        setHelperText(loc.getValue(L.HELP_AUTHORSHIP));
        setWidthFull();
        setReadOnly(true);
        Button calculate = new Button(loc.getValue(L.LABEL_CALCULATE), _ -> calculate());
        setSuffixComponent(calculate);
    }

    /**
     * Forgets the result, the branch may be another one.
     */
    public void reset() {
        setValue("");
    }

    private void calculate() {
        try {
            Manuscript manuscript = book.get();
            if (manuscript == null || manuscript.getActiveLeaf() == null) {
                setValue(String.format(loc.getValue(L.MSG_AUTHORSHIP_RATIO), 0, 0, 0));
                return;
            }
            BookAuthorship authorship = messageRevisionService.authorship(chatMessageService.getBranchFromLeaf(manuscript.getActiveLeaf()));
            long generated = Math.round(authorship.generatedShare() * 100);
            long written = authorship.totalWords() == 0 ? 0 : 100 - generated;
            setValue(String.format(loc.getValue(L.MSG_AUTHORSHIP_RATIO), written, generated, authorship.totalWords()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
