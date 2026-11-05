package com.github.enerccio.marginalia.extensions.reallangtranslator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.RealLanguage;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.BookReal;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Speakers;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.CommonStyles;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.SpeakersEditor;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;

import static com.github.enerccio.marginalia.extensions.reallangtranslator.ReallangTranslatorLocalization.*;

/**
 * The tab of the story for the real language of the book: who speaks the language in this book and the quick translation of a text. The language is
 * the one the book names (its own, a folder's or the user's); <i>Refresh</i> reads it again after it was changed in the window of the book.
 */
public class ReallangBookView extends VerticalLayout {

    private final Manuscript book;
    private final BookReal service;
    private final Span title = new Span();
    private final VerticalLayout body = new VerticalLayout();

    public ReallangBookView(Manuscript book, BookReal service) {
        this.book = book;
        this.service = service;
        setPadding(false);
        setSpacing(false);
        setSizeFull();
        setMinWidth("0");

        Button refresh = new Button(Solid.ROTATE.create(), _ -> refresh());
        refresh.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refresh.setTooltipText(T.localized(REFRESH));
        refresh.setAriaLabel(T.localized(REFRESH));
        HorizontalLayout header = new HorizontalLayout(title, refresh);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.expand(title);
        body.setPadding(false);
        body.setSizeFull();
        body.setMinWidth("0");
        add(header, body);
        setFlexGrow(1, body);
        refresh();
    }

    /**
     * Reads the language and the speakers of the book again and builds the tabs.
     */
    public final void refresh() {
        body.removeAll();
        try {
            RealLanguage language = service.languageOf(book);
            if (language == null) {
                title.setText(T.localized(REAL_LANGUAGE));
                Span none = new Span(T.localized(THE_BOOK_HAS_NO_LANGUAGE));
                none.addClassName(CommonStyles.HINT);
                body.add(none);
                return;
            }
            title.setText(T.localized(LANGUAGE_S).formatted(language.getName()));
            TabSheet tabs = new TabSheet();
            tabs.setSizeFull();
            tabs.add(T.localized(SPEAKERS), speakers());
            tabs.add(T.localized(QUICK_TRANSLATE), new QuickTranslatePanel(book, service));
            body.add(tabs);
        } catch (Exception e) {
            Notification.error(String.format(T.localized(THE_LANGUAGES_CAN_NOT_BE_LOADED), e.getMessage()));
        }
    }

    private SpeakersEditor speakers() throws Exception {
        Speakers speakers = service.speakers(book);
        return new SpeakersEditor(speakers, () -> {
            try {
                service.saveSpeakers(book, speakers);
            } catch (Exception e) {
                Notification.error(String.format(T.localized(THE_LANGUAGES_CAN_NOT_BE_LOADED), e.getMessage()));
            }
        });
    }
}
