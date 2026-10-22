package com.github.enerccio.marginalia.tool;

import com.github.enerccio.marginalia.domain.service.FunctionRegistry;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.CommandServiceImpl;
import com.github.enerccio.marginalia.domain.service.impl.FunctionRegistryImpl;
import com.github.enerccio.marginalia.domain.service.impl.ToolServiceImpl;
import com.github.enerccio.marginalia.domain.service.tool.*;
import com.github.enerccio.marginalia.domain.traits.*;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ToolServiceImpl}: what a {@link ToolSelection} turns on, the hints, the commands that go with a provider, and what
 * happens when a provider goes away or fails.
 */
class ToolServiceTest {

    public interface Names {
        @LLMFunction(name = "pick_names")
        void pick(FunctionContinuation<List<String>> done, String category);
    }

    public interface Places {
        @LLMFunction(name = "find_place")
        void find(FunctionContinuation<String> done, String query);
    }

    public interface AlsoNames {
        @LLMFunction(name = "pick_names")
        void pick(FunctionContinuation<List<String>> done, String category);
    }

    public static class PlaceCommands {
        @Command(commandName = "find-place", humanNameText = "Find place", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                signature = @CommandParameterSignature, returns = @CommandReturn(name = "place", type = String.class))
        public void find(CommandContext context) {
            context.setReturn("here");
        }
    }

    private final FunctionRegistry registry = new FunctionRegistryImpl();
    private final CommandServiceImpl commands = new CommandServiceImpl();
    private final ToolServiceImpl tools = new ToolServiceImpl();
    private static final ToolRequest REQUEST = new ToolRequest(null, "test");

    ToolServiceTest() {
        tools.setCommandService(commands);
    }

    @AfterEach
    void tearDown() {
        commands.destroy();
    }

    /** A provider with categories; it records what it was asked for. */
    private class NameProvider implements ToolProvider {

        final List<String> asked = new ArrayList<>();
        List<ToolItem> items = List.of(new ToolItem("a", "Alpha"), new ToolItem("b", "Beta"), new ToolItem("c", "Gamma"));

        @Override
        public List<ToolItem> items() {
            return items;
        }

        @Override
        public FunctionSet functions(Collection<String> checked, ToolRequest request) {
            asked.addAll(checked);
            return registry.bind(Names.class, (Names) (done, category) -> done.complete(List.of(category)));
        }

        @Override
        public String defaultHint() {
            return "Call pick_names.";
        }
    }

    private static class PlaceProvider implements ToolProvider {

        @Override
        public FunctionSet functions(Collection<String> checked, ToolRequest request) {
            return new FunctionRegistryImpl().bind(Places.class, (Places) (done, query) -> done.complete(query));
        }

        @Override
        public String defaultHint() {
            return "Call find_place.";
        }

        @Override
        public Object commands() {
            return new PlaceCommands();
        }
    }

    @Test
    void aProviderGetsOnlyTheCheckedItemsThatAreStillOffered() {
        NameProvider names = new NameProvider();
        tools.registerProvider("names", "names", "Name generator", names);
        ToolSelection selection = new ToolSelection().withItems("names/names", List.of("c", "gone", "a"));

        ToolSet set = tools.resolve(selection, REQUEST);

        assertThat(names.asked).containsExactly("c", "a");
        assertThat(set.functions().contains("pick_names")).isTrue();
        assertThat(set.hint()).isEqualTo("Call pick_names.");
    }

    @Test
    void nothingCheckedOrEverythingGoneGivesNoTools() {
        NameProvider names = new NameProvider();
        tools.registerProvider("names", "names", "Name generator", names);

        assertThat(tools.resolve(null, REQUEST).isEmpty()).isTrue();
        assertThat(tools.resolve(new ToolSelection(), REQUEST).isEmpty()).isTrue();
        assertThat(tools.resolve(new ToolSelection().withItems("names/names", List.of("gone")), REQUEST).isEmpty()).isTrue();
        assertThat(names.asked).isEmpty();
        assertThat(tools.resolve(new ToolSelection().withItems("other/key", List.of("a")), REQUEST).isEmpty()).isTrue();
    }

    @Test
    void aProviderWithoutItemsIsOnOrOff() {
        tools.registerProvider("places", "places", "Place provider", new PlaceProvider());
        ToolSelection selection = new ToolSelection();
        selection.entry("places/places").setEnabled(false);
        assertThat(tools.resolve(selection, REQUEST).isEmpty()).isTrue();

        selection.entry("places/places").setEnabled(true);
        assertThat(tools.resolve(selection, REQUEST).functions().contains("find_place")).isTrue();
    }

    @Test
    void theUsersHintReplacesTheDefaultOneAndOnlyProvidersThatGiveFunctionsHaveOne() {
        tools.registerProvider("names", "names", "Name generator", new NameProvider());
        tools.registerProvider("places", "places", "Place provider", new PlaceProvider());
        ToolSelection selection = new ToolSelection().withItems("names/names", List.of("a"));
        selection.entry("names/names").setHint("  Use my names.  ");
        selection.entry("places/places").setEnabled(true);

        ToolSet both = tools.resolve(selection, REQUEST);
        assertThat(both.functions().contains("pick_names")).isTrue();
        assertThat(both.functions().contains("find_place")).isTrue();
        assertThat(both.hint()).isEqualTo("Use my names.\n\nCall find_place.");

        selection.entry("places/places").setEnabled(false);
        assertThat(tools.resolve(selection, REQUEST).hint()).isEqualTo("Use my names.");
    }

    @Test
    void twoProvidersWithAFunctionOfTheSameNameFailWithBothNames() {
        tools.registerProvider("a", "names", "First names", new NameProvider());
        tools.registerProvider("b", "names", "Second names", new ToolProvider() {
            @Override
            public FunctionSet functions(Collection<String> checked, ToolRequest request) {
                return registry.bind(AlsoNames.class, (AlsoNames) (done, category) -> done.complete(List.of()));
            }
        });
        ToolSelection selection = new ToolSelection().withItems("a/names", List.of("a"));
        selection.entry("b/names").setEnabled(true);

        assertThatThrownBy(() -> tools.resolve(selection, REQUEST)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("First names").hasMessageContaining("Second names").hasMessageContaining("pick_names");
    }

    @Test
    void aProviderThatFailsIsLeftOut() {
        tools.registerProvider("bad", "bad", "Broken", new ToolProvider() {
            @Override
            public FunctionSet functions(Collection<String> checked, ToolRequest request) {
                throw new IllegalStateException("broken");
            }
        });
        tools.registerProvider("places", "places", "Place provider", new PlaceProvider());
        ToolSelection selection = new ToolSelection();
        selection.entry("bad/bad").setEnabled(true);
        selection.entry("places/places").setEnabled(true);

        ToolSet set = tools.resolve(selection, REQUEST);

        assertThat(set.functions().contains("find_place")).isTrue();
        assertThat(set.hint()).isEqualTo("Call find_place.");
    }

    @Test
    void theCommandsOfAProviderAreRegisteredAndGoWithIt() {
        tools.registerProvider("places", "places", "Place provider", new PlaceProvider());
        assertThat(commands.getCommands("find-place")).hasSize(1);
        assertThat(tools.getProviders()).singleElement().satisfies(info -> {
            assertThat(info.commands()).isTrue();
            assertThat(info.name()).isEqualTo("Place provider");
            assertThat(info.selectable()).isFalse();
        });

        tools.unregisterAll("places");

        assertThat(commands.getCommands("find-place")).isEmpty();
        assertThat(tools.getProviders()).isEmpty();
    }

    @Test
    void registeringAKeyAgainReplacesTheProviderAndItsCommandsAndKeepsThePlace() {
        tools.registerProvider("places", "first", "First", new PlaceProvider());
        tools.registerProvider("places", "second", "Second", new NameProvider());
        tools.registerProvider("places", "first", "First again", new PlaceProvider());

        assertThat(tools.getProviders()).extracting(ToolProviderInfo::name).containsExactly("First again", "Second");
        assertThat(commands.getCommands("find-place")).hasSize(1);

        tools.unregisterProvider("places", "first");
        assertThat(commands.getCommands("find-place")).isEmpty();
        assertThat(tools.getProviders()).extracting(ToolProviderInfo::ref).containsExactly("places/second");
    }

    @Test
    void theListOfProvidersHasTheItemsAsTheyAreNow() {
        NameProvider names = new NameProvider();
        tools.registerProvider("names", "names", "Name generator", names);
        assertThat(tools.getProviders().getFirst().items()).extracting(ToolItem::id).containsExactly("a", "b", "c");

        names.items = List.of(new ToolItem("z"));

        assertThat(tools.getProviders().getFirst().items()).extracting(ToolItem::label).containsExactly("z");
    }

    @Test
    void theSelectionCountsWhatIsOnAmongTheProvidersThatExist() {
        tools.registerProvider("names", "names", "Name generator", new NameProvider());
        tools.registerProvider("places", "places", "Place provider", new PlaceProvider());
        ToolSelection selection = new ToolSelection().withItems("names/names", List.of("a", "gone", "b"));
        selection.entry("places/places").setEnabled(true);
        selection.entry("removed/removed").setEnabled(true);

        assertThat(selection.count(tools.getProviders())).isEqualTo(3);
        assertThat(selection.isEmpty()).isFalse();
        assertThat(new ToolSelection().isEmpty()).isTrue();
    }

    @Test
    void theSelectionIsPlainJsonAndCompactDropsWhatChoosesNothing() {
        ToolSelection selection = new ToolSelection().withItems("names/names", List.of("a"));
        selection.entry("empty/none");
        selection.entry("places/places").setHint("Mine");
        selection.compact();

        Gson gson = new Gson();
        ToolSelection back = gson.fromJson(gson.toJson(selection), ToolSelection.class);

        assertThat(back.getProviders()).containsOnlyKeys("names/names", "places/places");
        assertThat(back.get("names/names").getItems()).containsExactly("a");
        assertThat(back.get("places/places").getHint()).isEqualTo("Mine");
        assertThat(back.copy().get("names/names").getItems()).containsExactly("a");
    }
}
