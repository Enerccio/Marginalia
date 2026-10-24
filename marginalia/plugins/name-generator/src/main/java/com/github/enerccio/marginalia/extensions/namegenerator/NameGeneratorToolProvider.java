package com.github.enerccio.marginalia.extensions.namegenerator;

import com.github.enerccio.marginalia.domain.service.FunctionRegistry;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.tool.ToolItem;
import com.github.enerccio.marginalia.domain.service.tool.ToolProvider;
import com.github.enerccio.marginalia.domain.service.tool.ToolRequest;

import java.util.*;

/**
 * The name generator as a tool suite: every category of names is an item, and the functions the model gets know only the
 * categories that are checked. The same suite is offered as the commands {@link NameGeneratorCommands}.
 */
public class NameGeneratorToolProvider implements ToolProvider {

    /** Added to the prompt when the model gets the functions; the user can replace it where they choose the tools. */
    static final String HINT = """
[ Name tools: you can call list_name_categories to see the categories of names, and generate_names with a category and a count (and optionally min_length and max_length) to get names of it. Call a reasonable amount, minimum is 10. When you need a name for a person, place or thing, call generate_names instead of inventing one, and pick from the returned names. You may also modify or remix the returned names when none of them fits as it is: combine parts of two names, change an ending or a letter, adjust the spelling, or shorten or lengthen a name. Keep the result sounding like it belongs to the same category. ]
""";

    private final NameTables tables;
    private final FunctionRegistry registry;

    public NameGeneratorToolProvider(NameTables tables, FunctionRegistry registry) {
        this.tables = tables;
        this.registry = registry;
    }

    @Override
    public List<ToolItem> items() {
        return tables.categories().stream().map(ToolItem::new).toList();
    }

    @Override
    public FunctionSet functions(Collection<String> items, ToolRequest request) {
        return registry.bind(NameGeneratorFunctions.class, new Restricted(new NameGeneratorFunctionsImpl(tables), items));
    }

    @Override
    public String defaultHint() {
        return HINT.strip();
    }

    @Override
    public String description() {
        return "Names made up by Markov chains trained on lists of names: places, deities, companies, surnames of countries... Groups (like all Native American names) pick a category at random for every name.";
    }

    @Override
    public Object commands() {
        return new NameGeneratorCommands(tables);
    }

    /** The functions limited to the checked categories (compared without regard to case, like the tables do). */
    private static final class Restricted implements NameGeneratorFunctions {

        private final NameGeneratorFunctionsImpl delegate;
        private final List<String> categories;
        private final Set<String> allowed = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        Restricted(NameGeneratorFunctionsImpl delegate, Collection<String> categories) {
            this.delegate = delegate;
            this.categories = new ArrayList<>(categories);
            allowed.addAll(categories);
        }

        @Override
        public void listCategories(FunctionContinuation<List<String>> done, String filter) {
            done.complete(NameTables.filter(categories, filter));
        }

        @Override
        public void generateNames(FunctionContinuation<List<String>> done, String category, int count,
                                  Integer minLength, Integer maxLength) {
            if (category == null || !allowed.contains(category.strip())) {
                done.fail(NameTables.unknown("category", category, categories, "list_name_categories"));
                return;
            }
            delegate.generateNames(done, category, count, minLength, maxLength);
        }
    }

}
