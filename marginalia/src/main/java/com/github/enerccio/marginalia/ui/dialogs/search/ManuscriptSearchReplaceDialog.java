package com.github.enerccio.marginalia.ui.dialogs.search;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService.Kind;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService.Options;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService.Plan;
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
 */
@Configurable(preConstruction = true)
public class ManuscriptSearchReplaceDialog extends SearchReplaceDialog {

    @Autowired
    private ManuscriptReplaceService replaceService;

    private final Manuscript manuscript;

    private Checkbox summariesBox;
    private Checkbox allBranchesBox;
    private Checkbox revalidateBox;

    public ManuscriptSearchReplaceDialog(Manuscript manuscript, boolean phone) {
        super(phone);
        this.manuscript = manuscript;
        create();
    }

    @Override
    protected String getTitle() {
        return loc.getValue(L.LABEL_SEARCH_AND_REPLACE);
    }

    @Override
    protected List<Component> createOptions() {
        summariesBox = new Checkbox(loc.getValue(L.LABEL_SEARCH_IN_SUMMARIES), true);
        allBranchesBox = new Checkbox(loc.getValue(L.LABEL_SEARCH_IN_ALL_BRANCHES), false);
        revalidateBox = new Checkbox(loc.getValue(L.LABEL_REVALIDATE_ALL_SUMMARIES), true);
        return List.of(summariesBox, allBranchesBox, revalidateBox);
    }

    @Override
    protected Searcher createSearcher(SearchRequest request) {
        Options options = new Options(request.search(), request.replacement(), request.regex(), request.caseSensitive(),
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
