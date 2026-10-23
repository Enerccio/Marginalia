package com.github.enerccio.marginalia.extensions.nameprovider;

import com.github.enerccio.marginalia.domain.service.FunctionRegistry;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.tool.ToolItem;
import com.github.enerccio.marginalia.domain.service.tool.ToolProvider;
import com.github.enerccio.marginalia.domain.service.tool.ToolRequest;

import java.util.*;

/**
 * The name provider as a tool suite: every list of real names is an item, and the functions the model gets know only the
 * lists that are checked. The same suite is offered as the commands {@link NameProviderCommands}.
 */
public class NameProviderToolProvider implements ToolProvider {

    /** Added to the prompt when the model gets the functions; the user can replace it where they choose the tools. */
    static final String HINT = """
[ Real name tools: you can call list_real_name_lists to see the lists of real names (forenames and surnames of countries), and pick_real_names with a list and a count to get names picked at random from it. Each name is a pair: the romanized name and the original one as it is written in its own language. Call a reasonable amount, minimum is 10. When you need a name for a person, place or thing, call pick_real_names instead of inventing one, and pick a name from the returned ones that fits. Write the picked name as it was returned (the romanized one, with the original next to it when that suits the setting); do not make up a different spelling. You may combine a forename with a surname of the same country. ]
""";

    private final NameLists lists;
    private final FunctionRegistry registry;

    public NameProviderToolProvider(NameLists lists, FunctionRegistry registry) {
        this.lists = lists;
        this.registry = registry;
    }

    @Override
    public List<ToolItem> items() {
        return lists.categories().stream().map(ToolItem::new).toList();
    }

    @Override
    public FunctionSet functions(Collection<String> items, ToolRequest request) {
        return registry.bind(NameProviderFunctions.class, new Restricted(new NameProviderFunctionsImpl(lists), items));
    }

    @Override
    public String defaultHint() {
        return HINT.strip();
    }

    @Override
    public String description() {
        return "Real names: the most common forenames and surnames of about 100 countries, with the original spelling.";
    }

    @Override
    public Object commands() {
        return new NameProviderCommands(lists);
    }

    /** The functions limited to the checked lists (compared without regard to case, like the lists do). */
    private static final class Restricted implements NameProviderFunctions {

        private final NameProviderFunctionsImpl delegate;
        private final List<String> categories;
        private final Set<String> allowed = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        Restricted(NameProviderFunctionsImpl delegate, Collection<String> categories) {
            this.delegate = delegate;
            this.categories = new ArrayList<>(categories);
            allowed.addAll(categories);
        }

        @Override
        public void listLists(FunctionContinuation<List<String>> done, String filter) {
            done.complete(NameLists.filter(categories, filter));
        }

        @Override
        public void pickNames(FunctionContinuation<List<NameEntry>> done, String category, int count) {
            if (category == null || !allowed.contains(category.strip())) {
                done.fail("Unknown category '" + category + "'. Categories: " + String.join(", ", categories));
                return;
            }
            delegate.pickNames(done, category, count);
        }
    }

}
