package com.github.enerccio.marginalia.extensions;

import com.github.enerccio.marginalia.domain.components.ModelText;
import com.github.enerccio.marginalia.domain.components.ModelText.Substitution;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PartTextRegistryTest {

    private static ChatMessage part(String text) {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return message;
    }

    /** Replaces every "Da" with "Yes". */
    private static class Replacer implements PartTextRegistry.Transformer {
        final List<Integer> prepared = new ArrayList<>();

        @Override
        public void prepare(Collection<ChatMessage> parts) {
            prepared.add(parts.size());
        }

        @Override
        public List<Substitution> substitutions(ChatMessage part) {
            List<Substitution> list = new ArrayList<>();
            int at = part.getResponse().indexOf("Da");
            while (at >= 0) {
                list.add(new Substitution(at, at + 2, "Yes"));
                at = part.getResponse().indexOf("Da", at + 2);
            }
            return list;
        }
    }

    @Test
    void withoutATransformerTheTextIsTheStoredOne() {
        PartTextRegistry registry = new PartTextRegistry();
        assertThat(registry.isActive()).isFalse();
        assertThat(registry.forModel(part("Da ek"))).isEqualTo("Da ek");
        assertThat(registry.forModel(part(null))).isEmpty();
        assertThat(registry.forModel((ChatMessage) null)).isEmpty();
        assertThat(registry.forModel(List.of(part("a"), part("b")))).containsExactly("a", "b");
    }

    @Test
    void aTransformerChangesWhatTheModelReads() {
        PartTextRegistry registry = new PartTextRegistry();
        registry.register("test", new Replacer());
        assertThat(registry.isActive()).isTrue();
        assertThat(registry.forModel(part("Da ek, Da du"))).isEqualTo("Yes ek, Yes du");
        ModelText text = registry.modelText(part("Da ek"));
        assertThat(text.stored()).isEqualTo("Da ek");
        assertThat(text.storedRange(0, 3)).containsExactly(0, 2);
    }

    @Test
    void theTransformerPreparesOnceForAllTheParts() {
        PartTextRegistry registry = new PartTextRegistry();
        Replacer replacer = new Replacer();
        registry.register("test", replacer);
        List<String> texts = registry.forModel(List.of(part("Da"), part("no"), part(""), part("Da Da")));
        assertThat(texts).containsExactly("Yes", "no", "", "Yes Yes");
        assertThat(replacer.prepared).containsExactly(4);
    }

    @Test
    void theFirstRegisteredWinsAnOverlapAndOwnersAreRemoved() {
        PartTextRegistry registry = new PartTextRegistry();
        registry.register("one", part -> List.of(new Substitution(0, 2, "ONE")));
        registry.register("two", part -> List.of(new Substitution(0, 2, "TWO"), new Substitution(3, 5, "tail")));
        assertThat(registry.forModel(part("ab cd"))).isEqualTo("ONE tail");
        registry.unregisterAll("one");
        assertThat(registry.forModel(part("ab cd"))).isEqualTo("TWO tail");
        registry.unregisterAll("two");
        assertThat(registry.isActive()).isFalse();
        assertThat(registry.forModel(part("ab cd"))).isEqualTo("ab cd");
    }

    @Test
    void aFailingTransformerDoesNotBreakTheRead() {
        PartTextRegistry registry = new PartTextRegistry();
        registry.register("broken", part -> {
            throw new IllegalStateException("boom");
        });
        registry.register("preparing", new PartTextRegistry.Transformer() {
            @Override
            public void prepare(Collection<ChatMessage> parts) {
                throw new IllegalStateException("cannot prepare");
            }

            @Override
            public List<Substitution> substitutions(ChatMessage part) {
                return List.of(new Substitution(0, 1, "never"));
            }
        });
        registry.register("fine", part -> List.of(new Substitution(0, 1, "X")));
        // the broken one is skipped, the one that could not prepare is skipped, the third works
        assertThat(registry.forModel(part("abc"))).isEqualTo("Xbc");
    }
}
