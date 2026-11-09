package com.github.enerccio.marginalia.extensions.translatorcommon.ui;

import com.github.enerccio.marginalia.extensions.translatorcommon.model.BookLanguage;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.LanguageRef;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;

import java.util.List;
import java.util.function.Supplier;

import static com.github.enerccio.marginalia.extensions.translatorcommon.ui.TranslatorCommonLocalization.*;

/**
 * The other languages of a book, besides the one it names first: a row for each with a button that takes it away, and a field to add one from the
 * library of the plugin. The speakers of each language are edited in a tab of their own by the plugin ({@link SpeakersEditor}); this panel only
 * decides which languages the book has.
 */
public class BookLanguagesPanel extends VerticalLayout {

    private final List<BookLanguage> extras;
    private final Supplier<List<LanguageRef>> library;
    private final String primaryId;
    private final Runnable onChange;

    /**
     * @param extras    the other languages of the book, edited in place
     * @param library   the languages the author can choose from (the library of the plugin)
     * @param primaryId the id of the first language of the book (it can not be added again), may be null
     * @param onChange  called after a language was added or taken away, when the plugin stores the list; the plugin builds its tabs again
     */
    public BookLanguagesPanel(List<BookLanguage> extras, Supplier<List<LanguageRef>> library, String primaryId, Runnable onChange) {
        this.extras = extras;
        this.library = library;
        this.primaryId = primaryId;
        this.onChange = onChange;
        setPadding(false);
        build();
    }

    private void build() {
        removeAll();
        Span hint = new Span(TC.localized(THE_BOOK_IS_TRANSLATED_INTO_THE_OTHER_LANGUAGES_TOO));
        hint.addClassName(CommonStyles.HINT);
        add(hint);
        List<LanguageRef> choices = library.get();
        for (BookLanguage extra : extras) {
            String name = choices.stream().filter(l -> l.id().equals(extra.getLanguageId())).findFirst().map(LanguageRef::name)
                    .orElse(TC.localized(A_LANGUAGE_THAT_IS_GONE));
            Button remove = new Button(TC.localized(REMOVE), event -> {
                extras.remove(extra);
                onChange.run();
                build();
            });
            remove.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            HorizontalLayout row = new HorizontalLayout(new Span(name), remove);
            row.setAlignItems(Alignment.CENTER);
            add(row);
        }
        List<LanguageRef> free = choices.stream().filter(l -> !l.id().equals(primaryId)
                && extras.stream().noneMatch(e -> l.id().equals(e.getLanguageId()))).toList();
        if (free.isEmpty()) {
            return;
        }
        Select<LanguageRef> select = new Select<>();
        select.setLabel(TC.localized(ANOTHER_LANGUAGE));
        select.setItems(free);
        select.setItemLabelGenerator(LanguageRef::name);
        Button add = new Button(TC.localized(ADD), event -> {
            if (select.getValue() != null) {
                extras.add(new BookLanguage(select.getValue().id()));
                onChange.run();
                build();
            }
        });
        HorizontalLayout adding = new HorizontalLayout(select, add);
        adding.setAlignItems(Alignment.END);
        add(adding);
    }
}
