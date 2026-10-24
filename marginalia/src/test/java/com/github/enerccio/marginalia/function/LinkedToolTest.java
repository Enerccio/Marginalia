package com.github.enerccio.marginalia.function;

import com.github.enerccio.marginalia.domain.service.FunctionRegistry;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.service.function.FunctionDefinition;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.FunctionRegistryImpl;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link LLMFunction#linkedTool()}: a function is offered only while the functions linked to it are in the set too.
 */
class LinkedToolTest {

    public interface Pair {
        @LLMFunction(name = "list_things", linkedTool = "pick_things, count_things")
        void list(FunctionContinuation<String> done);

        @LLMFunction(name = "pick_things", linkedTool = {"list_things", "count_things"})
        void pick(FunctionContinuation<String> done);

        @LLMFunction(name = "count_things", linkedTool = "list_things,pick_things")
        void count(FunctionContinuation<String> done);

        @LLMFunction(name = "free_thing")
        void free(FunctionContinuation<String> done);
    }

    public interface Needy {
        @LLMFunction(name = "needy", linkedTool = "nowhere")
        void needy(FunctionContinuation<String> done);
    }

    public interface Half {
        @LLMFunction(name = "half", linkedTool = "other_half")
        void half(FunctionContinuation<String> done);
    }

    public interface OtherHalf {
        @LLMFunction(name = "other_half", linkedTool = "half")
        void other(FunctionContinuation<String> done);
    }

    private final FunctionRegistry registry = new FunctionRegistryImpl();

    private static List<String> names(FunctionSet set) {
        return set.getDefinitions().stream().map(FunctionDefinition::getName).sorted().toList();
    }

    @Test
    void definitionsKeepEveryLinkedTool() {
        FunctionSet set = registry.bind(Pair.class, new Pair() {
            public void list(FunctionContinuation<String> done) {}
            public void pick(FunctionContinuation<String> done) {}
            public void count(FunctionContinuation<String> done) {}
            public void free(FunctionContinuation<String> done) {}
        });
        assertThat(set.getDefinitions().stream().filter(d -> d.getName().equals("list_things")).findFirst().orElseThrow()
                .getLinkedTools()).containsExactly("pick_things", "count_things");
        assertThat(names(set)).containsExactly("count_things", "free_thing", "list_things", "pick_things");
    }

    @Test
    void aFunctionWithoutItsLinkedToolIsNotOffered() {
        FunctionSet set = registry.bind(Needy.class, done -> {});
        assertThat(set.isEmpty()).isTrue();
        assertThat(set.contains("needy")).isFalse();
    }

    @Test
    void theHalvesAppearWhenTheOtherIsAdded() {
        FunctionSet half = registry.bind(Half.class, done -> {});
        assertThat(half.isEmpty()).isTrue();
        FunctionSet both = half.and(registry.bind(OtherHalf.class, done -> {}));
        assertThat(names(both)).containsExactly("half", "other_half");
    }
}
