package com.github.enerccio.marginalia.extensions.reallangtranslator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.reallangtranslator.ReallangKind;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.BookReal;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.BookLanguage;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.LanguageRef;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Speakers;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.UnderstandingSettings;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.BookLanguagesPanel;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.CommonStyles;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.SourceSearchReplaceDialog;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.SpeakersEditor;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;

import java.util.List;

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
        HorizontalLayout header = new HorizontalLayout(title, SourceSearchReplaceDialog.button(book, ReallangKind.ID), refresh);
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
     * Reads the languages and the speakers of the book again and builds the tabs: the speakers of each language, the other languages of the book and
     * the quick translation.
     */
    public final void refresh() {
        body.removeAll();
        try {
            List<LanguageRef> languages = service.languages(book);
            if (languages.isEmpty()) {
                title.setText(T.localized(REAL_LANGUAGE));
                Span none = new Span(T.localized(THE_BOOK_HAS_NO_LANGUAGE));
                none.addClassName(CommonStyles.HINT);
                body.add(none);
                return;
            }
            title.setText(languages.size() == 1 ? T.localized(LANGUAGE_S).formatted(languages.getFirst().name())
                    : T.localized(LANGUAGES_S).formatted(String.join(", ", languages.stream().map(LanguageRef::name).toList())));
            TabSheet tabs = new TabSheet();
            tabs.setSizeFull();
            for (LanguageRef language : languages) {
                tabs.add(languages.size() == 1 ? T.localized(SPEAKERS) : T.localized(SPEAKERS_S).formatted(language.name()), speakers(language.id()));
            }
            for (LanguageRef language : languages) {
                tabs.add(languages.size() == 1 ? T.localized(GLOSSARY) : T.localized(GLOSSARY_S).formatted(language.name()), new GlossaryPanel(book, service, language.id()));
            }
            tabs.add(T.localized(LANGUAGES), otherLanguages());
            tabs.add(T.localized(QUICK_TRANSLATE), new QuickTranslatePanel(book, service, languages));
            body.add(tabs);
        } catch (Exception e) {
            Notification.error(String.format(T.localized(THE_LANGUAGES_CAN_NOT_BE_LOADED), e.getMessage()));
        }
    }

    private SpeakersEditor speakers(String languageId) throws Exception {
        Speakers speakers = service.speakers(book, languageId);
        UnderstandingSettings understanding = service.understanding(book);
        return new SpeakersEditor(speakers, understanding, () -> {
            try {
                service.saveSpeakers(book, languageId, speakers);
                service.saveUnderstanding(book, understanding);
            } catch (Exception e) {
                Notification.error(String.format(T.localized(THE_LANGUAGES_CAN_NOT_BE_LOADED), e.getMessage()));
            }
        });
    }

    /** The languages of the book besides the one it names: add and remove; the tabs are built again after a change. */
    private BookLanguagesPanel otherLanguages() throws Exception {
        List<BookLanguage> others = service.otherLanguages(book);
        return new BookLanguagesPanel(others, () -> {
            try {
                return service.libraryLanguages();
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }, service.primaryLanguageId(book), () -> {
            try {
                service.saveOtherLanguages(book, others);
                refresh();
            } catch (Exception e) {
                Notification.error(String.format(T.localized(THE_LANGUAGES_CAN_NOT_BE_LOADED), e.getMessage()));
            }
        });
    }
}
