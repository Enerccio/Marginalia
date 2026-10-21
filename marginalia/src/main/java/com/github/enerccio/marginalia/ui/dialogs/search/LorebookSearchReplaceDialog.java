package com.github.enerccio.marginalia.ui.dialogs.search;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.service.LorebookReplaceService;
import com.github.enerccio.marginalia.domain.service.LorebookReplaceService.Options;
import com.github.enerccio.marginalia.domain.service.LorebookReplaceService.Plan;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * {@link SearchReplaceDialog} for the entries of a lorebook: their name, text and comment. For the desktop and, with
 * {@code phone} set, for the phone. The table shows the name of an entry where the book's one shows the id, an entry is
 * known by its name.
 */
@Configurable(preConstruction = true)
@Extendable
public class LorebookSearchReplaceDialog extends SearchReplaceDialog {

    @Autowired
    private LorebookReplaceService replaceService;

    private final Lorebook lorebook;

    public LorebookSearchReplaceDialog(Lorebook lorebook, boolean phone) {
        super(phone);
        this.lorebook = lorebook;
        create();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = String.class))
    @Override
    protected String getTitle() {
        return loc.getValue(L.LABEL_SEARCH_AND_REPLACE);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = String.class))
    @Override
    protected String getIdColumnHeader() {
        return loc.getValue(L.LABEL_NAME);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = SearchReplaceDialog.Searcher.class),
            parameters = {@APIParameter(name = "request", type = SearchReplaceDialog.SearchRequest.class)},
            locals = {@APILocals(name = "options", type = LorebookReplaceService.Options.class)})
    @Override
    protected Searcher createSearcher(SearchRequest request) {
        Options options = new Options(request.search(), request.replacement(), request.regex(), request.caseSensitive());
        return progress -> {
            Plan plan = replaceService.plan(lorebook, options, progress);
            List<SearchReplaceItem> items = plan.items().stream()
                    .map(item -> new SearchReplaceItem(loc.getValue(L.ENUM_DOMAIN_LOREBOOK_ENTRY),
                            StringUtils.defaultIfBlank(item.name(), String.valueOf(item.id())), item.hits()))
                    .toList();
            return new SearchResult(items, null, replacing -> {
                LorebookReplaceService.Result result = replaceService.apply(lorebook, plan, replacing);
                String message = String.format(loc.getValue(L.MSG_REPLACE_DONE), result.hits(), result.changed());
                return result.skipped() == 0 ? message
                        : message + "\n" + String.format(loc.getValue(L.MSG_REPLACE_SKIPPED), result.skipped());
            });
        };
    }

}
