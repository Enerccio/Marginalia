package com.github.enerccio.marginalia.extensions.conlangtranslator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangStyles;
import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.ChangeSet;
import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.LanguageDiff;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.RunLanguage;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.BookConlang;
import com.github.enerccio.marginalia.extensions.conlangtranslator.text.ConlangKind;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.BookLanguage;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.LanguageRef;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Speakers;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.BookLanguagesPanel;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.SourceSearchReplaceDialog;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.SpeakersEditor;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.TabSheet;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * The tab of the story for the conlang of the book: the words of its language (the author looks them up and edits them while writing), who
 * speaks the language in this book, and the quick translation of a text. The language is the one the book names (its own, a folder's or the
 * user's); <i>Refresh</i> reads it again after it was changed in the window of the book. A book with more than one language has a field to choose
 * which of them the words, the speakers and the quick translation are about, and a tab to add or take away the other languages.
 */
public class ConlangBookView extends VerticalLayout {

    private final Manuscript book;
    private final BookConlang service;
    private final Span title = new Span();
    private final VerticalLayout body = new VerticalLayout();

    private final Select<LanguageRef> picker = new Select<>();
    private boolean picking;
    private String selectedId;
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
        picker.setItemLabelGenerator(LanguageRef::name);
        picker.setVisible(false);
        picker.addValueChangeListener(event -> {
            if (!picking && event.getValue() != null && !event.getValue().id().equals(selectedId)) {
                selectedId = event.getValue().id();
                refresh();
            }
        });
        HorizontalLayout header = new HorizontalLayout(title, picker, SourceSearchReplaceDialog.button(book, ConlangKind.ID), refresh);
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
            body.add(runChanges());
            List<LanguageRef> languages = service.languages(book);
            if (languages.isEmpty()) {
                picker.setVisible(false);
                title.setText(T.localized(CONLANG));
                Span none = new Span(T.localized(THE_BOOK_HAS_NO_LANGUAGE));
                none.addClassName(ConlangStyles.HINT);
                body.add(none);
                language = null;
                return;
            }
            if (languages.stream().noneMatch(item -> item.id().equals(selectedId))) {
                selectedId = languages.getFirst().id();
            }
            picking = true;
            picker.setItems(languages);
            picker.setValue(languages.stream().filter(item -> item.id().equals(selectedId)).findFirst().orElse(languages.getFirst()));
            picking = false;
            picker.setVisible(languages.size() > 1);
            language = service.languageOf(book, selectedId);
            title.setText(languages.size() > 1 ? "" : T.localized(LANGUAGE_S).formatted(language.getName()));
            knownWords.clear();
            language.getLexicon().forEach(entry -> knownWords.add(entry.getId()));

            TabSheet tabs = new TabSheet();
            tabs.setSizeFull();
            lexicon = new LexiconTab(language, this::saveWords);
            tabs.add(T.localized(WORDS), lexicon);
            tabs.add(T.localized(SPEAKERS), speakers());
            tabs.add(T.localized(LANGUAGES), otherLanguages());
            tabs.add(T.localized(QUICK_TRANSLATE), new QuickTranslatePanel(book, service, selectedId, this::reloadWords));
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

    /**
     * The languages an agent run changed that the book still holds: the run worked on a copy, the changes wait here until the user takes them into the
     * library (a three-way review against the language as it is now) or drops them.
     */
    private Component runChanges() throws Exception {
        VerticalLayout rows = new VerticalLayout();
        rows.setPadding(false);
        rows.setSpacing(false);
        for (Map.Entry<String, RunLanguage> entry : service.runChanges(book).entrySet()) {
            String id = entry.getKey();
            String name = entry.getValue().getOurs().getName();
            boolean made = entry.getValue().getBase() == null && service.runChangeSet(book, id) == null;
            Span text = new Span(T.localized(made ? AN_AGENT_RUN_MADE_THE_LANGUAGE_S : AN_AGENT_RUN_CHANGED_THE_LANGUAGE_S).formatted(name));
            text.addClassName(ConlangStyles.HINT);
            Button review = new Button(T.localized(REVIEW_THE_RUN), _ -> reviewRun(id, name));
            review.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            Button drop = new Button(T.localized(DROP), _ -> ConfirmDialog.show(T.localized(DROP_THE_CHANGES_OF_THE_RUN_TO_S).formatted(name), () -> {
                try {
                    service.discardRunChanges(book, id);
                    refresh();
                } catch (Exception e) {
                    Notification.error(String.format(T.localized(THE_LANGUAGES_CAN_NOT_BE_LOADED), e.getMessage()));
                }
            }));
            drop.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            HorizontalLayout row = new HorizontalLayout(text, review, drop);
            row.setAlignItems(Alignment.CENTER);
            row.setWidthFull();
            row.expand(text);
            rows.add(row);
        }
        return rows;
    }

    private void reviewRun(String id, String name) {
        try {
            ChangeSet set = service.runChangeSet(book, id);
            if (set == null) {
                ConfirmDialog.show(T.localized(ADD_THE_LANGUAGE_S_TO_YOUR_LIBRARY).formatted(name), () -> takeOver(id, null));
                return;
            }
            set.pickClean();
            new ChangeReviewDialog(T.localized(CHANGES_OF_THE_RUN_TO_S).formatted(name), set, picked -> takeOver(id, picked)).open();
        } catch (Exception e) {
            Notification.error(String.format(T.localized(THE_LANGUAGES_CAN_NOT_BE_LOADED), e.getMessage()));
        }
    }

    private void takeOver(String id, ChangeSet picked) {
        try {
            LanguageDiff.Result result = service.takeOverRunChanges(book, id, picked);
            Notification.success(T.localized(APPLIED_D_CHANGES).formatted(result.applied().size()));
            if (!result.stale().isEmpty()) {
                Notification.warning(T.localized(SKIPPED_D_CHANGED_MEANWHILE).formatted(result.stale().size()));
            }
            if (!result.unmet().isEmpty()) {
                Notification.warning(T.localized(SKIPPED_D_NEED_ANOTHER).formatted(result.unmet().size()));
            }
            refresh();
        } catch (Exception e) {
            Notification.error(String.format(T.localized(THE_LANGUAGES_CAN_NOT_BE_LOADED), e.getMessage()));
        }
    }

    private Component speakers() throws Exception {
        String languageId = selectedId;
        Speakers speakers = service.speakers(book, languageId);
        return new SpeakersEditor(speakers, () -> {
            try {
                service.saveSpeakers(book, languageId, speakers);
            } catch (Exception e) {
                Notification.error(String.format(T.localized(THE_LANGUAGES_CAN_NOT_BE_LOADED), e.getMessage()));
            }
        });
    }

    /** The languages of the book besides the one it names: add and remove; the tabs are built again after a change. */
    private Component otherLanguages() throws Exception {
        List<BookLanguage> others = service.otherLanguages(book);
        return new BookLanguagesPanel(others, () -> {
            try {
                return service.libraryLanguages(book);
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
            Language stored = service.languageOf(book, selectedId);
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
