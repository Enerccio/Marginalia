package com.github.enerccio.marginalia.extensions.translatorcommon.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.search.replace.TextSearch;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.SourceReplace;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.ui.dialogs.search.SearchReplaceDialog;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;

import java.util.List;

import static com.github.enerccio.marginalia.extensions.translatorcommon.ui.TranslatorCommonLocalization.*;

/**
 * The application's search and replace window over the English sources of the translated speeches of the active branch of a book: the table lists
 * the speeches that have a match (part and speech number), which open to the matches in the source. Only the sources change; the translated text
 * is the text of the story and is replaced from the book's own search window.
 */
public class SourceSearchReplaceDialog extends SearchReplaceDialog {

    private final Manuscript book;
    private final SourceReplace replace;

    /**
     * @param kindId the id of the passage kind of the plugin
     * @param phone  the layout of the phone
     */
    public SourceSearchReplaceDialog(Manuscript book, String kindId, boolean phone) {
        super(phone);
        this.book = book;
        this.replace = new SourceReplace(kindId);
        create();
    }

    /**
     * The button of the plugin's tab of the story that opens the window.
     *
     * @param kindId the id of the passage kind of the plugin
     */
    public static Button button(Manuscript book, String kindId) {
        Button button = new Button(Solid.EXCHANGE_ALT.create(), _ -> new SourceSearchReplaceDialog(book, kindId, UIUtils.isPhone()).open());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        button.setTooltipText(TC.localized(SEARCH_AND_REPLACE_IN_THE_SOURCES));
        button.setAriaLabel(TC.localized(SEARCH_AND_REPLACE_IN_THE_SOURCES));
        return button;
    }

    @Override
    protected String getTitle() {
        return TC.localized(SEARCH_AND_REPLACE_IN_THE_SOURCES);
    }

    @Override
    protected String getIdColumnHeader() {
        return TC.localized(SPEECH);
    }

    @Override
    protected Searcher createSearcher(SearchRequest request) {
        // made here and not in the search so that a wrong regular expression is told to the user at once
        TextSearch search = TextSearch.of(request.search(), request.replacement(), request.regex(), request.caseSensitive());
        return progress -> {
            SourceReplace.Plan plan = replace.plan(book, search, progress);
            List<SearchReplaceItem> items = plan.items().stream()
                    .map(item -> new SearchReplaceItem(TC.localized(TRANSLATED_SPEECH),
                            TC.localized(PART_D_SPEECH_D).formatted(item.part(), item.number()), item.hits()))
                    .toList();
            return new SearchResult(items, null, replacing -> {
                SourceReplace.Result result = replace.apply(plan, replacing);
                String message = String.format(loc.getValue(L.MSG_REPLACE_DONE), result.hits(), result.changed());
                return result.skipped() == 0 ? message
                        : message + "\n" + String.format(loc.getValue(L.MSG_REPLACE_SKIPPED), result.skipped());
            });
        };
    }
}
