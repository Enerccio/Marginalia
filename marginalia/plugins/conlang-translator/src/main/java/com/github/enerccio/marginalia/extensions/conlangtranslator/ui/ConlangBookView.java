package com.github.enerccio.marginalia.extensions.conlangtranslator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangStyles;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Speakers;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.BookConlang;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;

import java.util.HashSet;
import java.util.Set;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * The tab of the story for the conlang of the book: the words of its language (the author looks them up and edits them while writing), who
 * speaks the language in this book, and the quick translation of a text. The language is the one the book names (its own, a folder's or the
 * user's); <i>Refresh</i> reads it again after it was changed in the window of the book.
 */
public class ConlangBookView extends VerticalLayout {

    private final Manuscript book;
    private final BookConlang service;
    private final Span title = new Span();
    private final VerticalLayout body = new VerticalLayout();

    private Language language;
    private LexiconTab lexicon;
    private final Set<String> knownWords = new HashSet<>();

    public ConlangBookView(Manuscript book, BookConlang service) {
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
    public void refresh() {
        body.removeAll();
        lexicon = null;
        try {
            language = service.languageOf(book);
            if (language == null) {
                title.setText(T.localized(CONLANG));
                Span none = new Span(T.localized(THE_BOOK_HAS_NO_LANGUAGE));
                none.addClassName(ConlangStyles.HINT);
                body.add(none);
                return;
            }
            title.setText(T.localized(LANGUAGE_S).formatted(language.getName()));
            knownWords.clear();
            language.getLexicon().forEach(entry -> knownWords.add(entry.getId()));

            TabSheet tabs = new TabSheet();
            tabs.setSizeFull();
            lexicon = new LexiconTab(language, this::saveWords);
            tabs.add(T.localized(WORDS), lexicon);
            tabs.add(T.localized(SPEAKERS), speakers());
            tabs.add(T.localized(QUICK_TRANSLATE), new QuickTranslatePanel(book, service, this::reloadWords));
            tabs.addSelectedChangeListener(_ -> {
                if (lexicon != null) {
                    lexicon.refresh();
                }
            });
            body.add(tabs);
        } catch (Exception e) {
            Notification.error(String.format(T.localized(THE_LANGUAGES_CAN_NOT_BE_LOADED), e.getMessage()));
        }
    }

    private Component speakers() throws Exception {
        Speakers speakers = service.speakers(book);
        return new SpeakersEditor(speakers, () -> {
            try {
                service.saveSpeakers(book, speakers);
            } catch (Exception e) {
                Notification.error(String.format(T.localized(THE_LANGUAGES_CAN_NOT_BE_LOADED), e.getMessage()));
            }
        });
    }

    private void saveWords() {
        try {
            service.saveWords(language, knownWords);
            lexicon.refresh();
        } catch (Exception e) {
            Notification.error(String.format(T.localized(THE_LANGUAGES_CAN_NOT_BE_LOADED), e.getMessage()));
        }
    }

    /** Words were added to the library from outside the list (the quick translation): the list shows the stored words, the tab stays. */
    private void reloadWords() {
        try {
            Language stored = service.languageOf(book);
            if (stored == null || language == null || lexicon == null) {
                return;
            }
            language.getLexicon().clear();
            language.getLexicon().addAll(stored.getLexicon());
            language.getCategories().clear();
            language.getCategories().addAll(stored.getCategories());
            knownWords.clear();
            language.getLexicon().forEach(entry -> knownWords.add(entry.getId()));
            lexicon.refresh();
        } catch (Exception e) {
            Notification.error(String.format(T.localized(THE_LANGUAGES_CAN_NOT_BE_LOADED), e.getMessage()));
        }
    }
}
