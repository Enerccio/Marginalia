package com.github.enerccio.marginalia.extensions.conlangtranslator.ui;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangStyles;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.ReaderMode;
import com.github.enerccio.marginalia.extensions.conlangtranslator.text.ReaderText;
import com.github.enerccio.marginalia.ui.components.PartView;
import com.vaadin.flow.component.html.Div;

import java.util.List;

/**
 * Puts the translated speech of a part into the page of a published book the way the book asks for ({@link ReaderMode}). In the click mode the
 * browser gets the conlang with a mark on each speech; the click carries the id of the passage and the English is found on the server, so in the
 * other modes there is nothing in the page to look at.
 */
public final class ReaderView {

    private static final String CLICK_EVENT = "conlang-click";

    private ReaderView() {
    }

    /**
     * Changes the part the viewer built from the stored text.
     *
     * @param view     the part as the viewer made it
     * @param part     the part
     * @param passages the translated passages of the part
     */
    public static void apply(PartView view, ChatMessage part, ReaderMode mode, List<AnchoredPassage> passages) {
        if (passages.isEmpty() || mode == ReaderMode.CONLANG) {
            return;
        }
        view.setContent(ReaderText.render(mode, part.getResponse(), passages), part.getImages());
        if (mode != ReaderMode.CLICK) {
            return;
        }
        // the popover is placed against this part, over the mark that was clicked
        view.addClassName(ConlangStyles.READER_PART);
        Div anchor = new Div();
        anchor.addClassName(CssStyles.ANNOTATION_ANCHOR);
        view.add(anchor);
        view.getElement().executeJs(CLICK_SCRIPT, view.getElement(), anchor.getElement(), ReaderText.MARK_ATTRIBUTE, CLICK_EVENT);
        view.getElement().addEventListener(CLICK_EVENT, event -> {
            String uuid = event.getEventData().get("event.detail.id").asString();
            passages.stream().filter(passage -> passage.getUuid().equals(uuid)).findFirst()
                    // the reader is not the author: no warning that the text was edited
                    .ifPresent(passage -> SourcePopover.open(passage, anchor, null));
        }).addEventData("event.detail.id");
    }

    /**
     * Makes the speech of a part clickable: moves the anchor onto the mark that was clicked and tells the server which passage it is. Not when
     * the reader selected text (they are copying, not asking).
     */
    private static final String CLICK_SCRIPT = """
            const el = $0;
            const anchor = $1;
            const attribute = $2;
            const event = $3;
            if (el._conlangClick) {
              return;
            }
            el._conlangClick = true;
            el.addEventListener('click', e => {
              const selection = window.getSelection();
              if (selection && !selection.isCollapsed && String(selection).length > 0) {
                return;
              }
              const mark = e.target.closest ? e.target.closest('mark[' + attribute + ']') : null;
              if (!mark || !el.contains(mark)) {
                return;
              }
              const hostRect = el.getBoundingClientRect();
              const rect = mark.getBoundingClientRect();
              anchor.style.left = (rect.left - hostRect.left) + 'px';
              anchor.style.top = (rect.top - hostRect.top) + 'px';
              anchor.style.width = rect.width + 'px';
              anchor.style.height = rect.height + 'px';
              el.dispatchEvent(new CustomEvent(event, { detail: { id: mark.getAttribute(attribute) } }));
            });
            """;
}
