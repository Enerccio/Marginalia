package com.github.enerccio.marginalia.ui.dialogs.search;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService.Kind;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService.Options;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService.Plan;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * {@link SearchReplaceDialog} for the text of a book: the parts of the story and, if the user wants, their summaries,
 * of the active branch or of all the branches, and
 * optionally revalidates all the summaries of the book afterwards. For the desktop and, with {@code phone} set, for the phone.
 * With {@code summariesOnly} (the one of the summaries overview) it looks into the summaries only and leaves the story
 * alone.
 */
@Configurable(preConstruction = true)
@Extendable
public class ManuscriptSearchReplaceDialog extends SearchReplaceDialog {

    @Autowired
    private ManuscriptReplaceService replaceService;

    private final Manuscript manuscript;
    private final boolean summariesOnly;

    private Checkbox summariesBox;
    private Checkbox allBranchesBox;
    private Checkbox revalidateBox;

    public ManuscriptSearchReplaceDialog(Manuscript manuscript, boolean phone) {
        this(manuscript, phone, false);
    }

    public ManuscriptSearchReplaceDialog(Manuscript manuscript, boolean phone, boolean summariesOnly) {
        super(phone);
        this.manuscript = manuscript;
        this.summariesOnly = summariesOnly;
        create();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = String.class))
    @Override
    protected String getTitle() {
        return summariesOnly ? loc.getValue(L.LABEL_SUMMARIES) + ": " + loc.getValue(L.LABEL_SEARCH_AND_REPLACE)
                : loc.getValue(L.LABEL_SEARCH_AND_REPLACE);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = List.class))
    @Override
    protected List<Component> createOptions() {
        summariesBox = new Checkbox(loc.getValue(L.LABEL_SEARCH_IN_SUMMARIES), true);
        allBranchesBox = new Checkbox(loc.getValue(L.LABEL_SEARCH_IN_ALL_BRANCHES), false);
        revalidateBox = new Checkbox(loc.getValue(L.LABEL_REVALIDATE_ALL_SUMMARIES), true);
        // there is nothing else to look into in the summaries overview
        return summariesOnly ? List.of(allBranchesBox, revalidateBox) : List.of(summariesBox, allBranchesBox, revalidateBox);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = SearchReplaceDialog.Searcher.class),
            parameters = {@APIParameter(name = "request", type = SearchReplaceDialog.SearchRequest.class)},
            locals = {@APILocals(name = "options", type = ManuscriptReplaceService.Options.class)})
    @Override
    protected Searcher createSearcher(SearchRequest request) {
        Options options = summariesOnly
                ? Options.summariesOnly(request.search(), request.replacement(), request.regex(), request.caseSensitive(),
                allBranchesBox.getValue(), revalidateBox.getValue())
                : new Options(request.search(), request.replacement(), request.regex(), request.caseSensitive(),
                summariesBox.getValue(), allBranchesBox.getValue(), revalidateBox.getValue());
        return progress -> {
            Plan plan = replaceService.plan(manuscript, options, progress);
            List<SearchReplaceItem> items = plan.items().stream()
                    .map(item -> new SearchReplaceItem(
                            loc.getValue(item.kind() == Kind.MESSAGE ? L.LABEL_STORY_PART : L.LABEL_SUMMARY),
                            String.valueOf(item.id()), item.hits()))
                    .toList();
            String warning = plan.outdatedSummaries() == 0 ? null
                    : String.format(loc.getValue(L.MSG_REPLACE_OUTDATED_SUMMARIES), plan.outdatedSummaries());
            return new SearchResult(items, warning, replacing -> {
                ManuscriptReplaceService.Result result = replaceService.apply(manuscript, plan, replacing);
                String message = String.format(loc.getValue(L.MSG_REPLACE_DONE), result.hits(), result.changed());
                return result.skipped() == 0 ? message
                        : message + "\n" + String.format(loc.getValue(L.MSG_REPLACE_SKIPPED), result.skipped());
            });
        };
    }

}
