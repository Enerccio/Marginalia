package com.github.enerccio.marginalia.extensions.conlangtranslator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangStyles;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.EntryStatus;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.LexiconEntry;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * The words of the language of the book, for the author who looks one up while writing: search by the word or by its meaning, nothing to edit
 * (the language is edited in <i>Settings → Extension Settings</i>).
 */
public class LookupDialog extends Dialog {

    public LookupDialog(Language language) {
        setHeaderTitle(T.localized(WORDS_OF_S).formatted(language.getName()));
        setWidth("980px");
        setHeight("70vh");
        setMaxWidth("96vw");
        setMaxHeight("96vh");
        setResizable(true);
        setDraggable(true);

        List<LexiconEntry> offered = language.getLexicon().stream().filter(entry -> entry.getStatus() != EntryStatus.RETIRED).toList();
        ListDataProvider<LexiconEntry> data = new ListDataProvider<>(offered);

        TextField search = new TextField();
        search.setPlaceholder(T.localized(SEARCH_BY_WORD_OR_MEANING));
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setWidthFull();
        search.setValueChangeMode(ValueChangeMode.LAZY);
        Span count = new Span();
        count.addClassName(ConlangStyles.HINT);
        Runnable filter = () -> {
            String text = StringUtils.defaultString(search.getValue()).strip().toLowerCase(Locale.ROOT);
            data.setFilter(entry -> text.isEmpty() || entry.getWord().toLowerCase(Locale.ROOT).contains(text)
                    || entry.getSenses().stream().anyMatch(sense -> sense.getGloss().toLowerCase(Locale.ROOT).contains(text)));
            count.setText(T.localized(D_WORDS_D_NEW).formatted(offered.size(), language.newEntries().size()));
        };
        search.addValueChangeListener(_ -> filter.run());

        Grid<LexiconEntry> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        grid.addClassName(ConlangStyles.FILL);
        grid.setDataProvider(data);
        grid.addComponentColumn(entry -> {
            Span form = new Span(entry.getWord());
            form.addClassName(ConlangStyles.WORD);
            return form;
        }).setHeader(T.localized(WORD)).setSortable(true).setComparator((a, b) -> a.getWord().compareToIgnoreCase(b.getWord()))
                .setFlexGrow(0).setAutoWidth(true);
        grid.addColumn(EntryDialog::glosses).setHeader(T.localized(MEANING)).setFlexGrow(2);
        grid.addColumn(entry -> language.findCategory(entry.getCategory()) == null ? "" : language.findCategory(entry.getCategory()).getName())
                .setHeader(T.localized(CATEGORY)).setFlexGrow(1);
        grid.addColumn(entry -> language.findRegister(entry.getRegister()) == null ? "" : language.findRegister(entry.getRegister()).getName())
                .setHeader(T.localized(REGISTER)).setFlexGrow(0).setAutoWidth(true);
        grid.addColumn(LexiconEntry::getRationale).setHeader(T.localized(WHY_THE_WORD_IS_AS_IT_IS)).setFlexGrow(3);
        grid.addComponentColumn(entry -> {
            Span badge = new Span(T.localized(EntryDialog.statusLabel(entry.getStatus())));
            badge.addClassName(ConlangStyles.BADGE);
            badge.getElement().setAttribute("data-status", entry.getStatus().name());
            return badge;
        }).setHeader(T.localized(STATUS)).setFlexGrow(0).setWidth("7rem");
        grid.getColumns().forEach(column -> column.setResizable(true));
        filter.run();

        VerticalLayout content = new VerticalLayout(search, count, grid);
        content.setPadding(false);
        content.setSizeFull();
        content.setFlexGrow(1, grid);
        add(content);
        getFooter().add(new Button(T.localized(CLOSE), Solid.XMARK.create(), _ -> close()));
    }
}
