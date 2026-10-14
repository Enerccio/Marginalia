package com.github.enerccio.marginalia.ipc;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.IPCService;
import com.github.enerccio.marginalia.domain.service.impl.IPCServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link IPCService}: the providers of data about books and story parts that extensions offer to each other.
 */
class IPCServiceTest {

    private final IPCService ipc = new IPCServiceImpl();

    @Test
    void providersAreListedInTheOrderOfRegistration() {
        ipc.registerManuscriptProvider("b.one", "z", "Zeta", (m, b, id) -> "z");
        ipc.registerManuscriptProvider("b.two", "a", "Alpha", (m, b, id) -> "a");
        ipc.registerManuscriptProvider("b.one", "a", "Alpha of one", (m, b, id) -> "a1");

        Map<String, Map<String, String>> providers = ipc.getManuscriptProviders();
        assertThat(providers.keySet()).containsExactly("b.one", "b.two");
        assertThat(providers.get("b.one").entrySet()).extracting(Map.Entry::getKey, Map.Entry::getValue)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("z", "Zeta"),
                        org.assertj.core.groups.Tuple.tuple("a", "Alpha of one"));
    }

    @Test
    void manuscriptAndChatMessageProvidersAreSeparate() {
        ipc.registerManuscriptProvider("b", "k", "Book", (m, b, id) -> "book");
        ipc.registerChatMessageProvider("b", "k", "Part", (m, b, id) -> "part");

        assertThat(ipc.getManuscriptProviders().get("b")).containsEntry("k", "Book");
        assertThat(ipc.getChatMessageProviders().get("b")).containsEntry("k", "Part");
    }

    @Test
    void registeringAgainReplacesTheProviderAndKeepsItsPlace() throws Exception {
        ipc.registerManuscriptProvider("b", "first", "First", (m, bundle, id) -> "old");
        ipc.registerManuscriptProvider("b", "second", "Second", (m, bundle, id) -> "second");
        ipc.registerManuscriptProvider("b", "first", "First again", (m, bundle, id) -> "new");

        assertThat(ipc.getManuscriptProviders().get("b").keySet()).containsExactly("first", "second");
        assertThat(ipc.getManuscriptProviders().get("b")).containsEntry("first", "First again");
        assertThat(ipc.getManuscriptData("b", "first", new Manuscript(), null, "caller")).isEqualTo("new");
    }

    @Test
    void providerGetsWhatTheCallerPassed() throws Exception {
        Manuscript manuscript = new Manuscript();
        ChatMessage message = new ChatMessage();
        List<Object> seen = new java.util.ArrayList<>();
        ipc.registerManuscriptProvider("b", "m", "M", (m, bundle, id) -> {
            seen.add(m);
            seen.add(id);
            return "book data";
        });
        ipc.registerChatMessageProvider("b", "c", "C", (m, bundle, id) -> {
            seen.add(m);
            seen.add(id);
            return "part data";
        });

        assertThat(ipc.getManuscriptData("b", "m", manuscript, null, "who")).isEqualTo("book data");
        assertThat(ipc.getChatMessageData("b", "c", message, null, "who2")).isEqualTo("part data");
        assertThat(seen).containsExactly(manuscript, "who", message, "who2");
    }

    @Test
    void unknownProviderGivesNull() throws Exception {
        assertThat(ipc.getManuscriptData("nobody", "k", new Manuscript(), null, "c")).isNull();
        ipc.registerManuscriptProvider("b", "k", "K", (m, bundle, id) -> "x");
        assertThat(ipc.getManuscriptData("b", "other", new Manuscript(), null, "c")).isNull();
        assertThat(ipc.getChatMessageData("b", "k", new ChatMessage(), null, "c")).isNull();
    }

    @Test
    void whatTheProviderThrowsReachesTheCaller() {
        ipc.registerManuscriptProvider("b", "k", "K", (m, bundle, id) -> {
            throw new IllegalStateException("broken");
        });
        assertThatThrownBy(() -> ipc.getManuscriptData("b", "k", new Manuscript(), null, "c"))
                .isInstanceOf(IllegalStateException.class).hasMessage("broken");
    }

    @Test
    void unregisteringRemovesProviders() throws Exception {
        ipc.registerManuscriptProvider("b", "one", "One", (m, bundle, id) -> "1");
        ipc.registerManuscriptProvider("b", "two", "Two", (m, bundle, id) -> "2");
        ipc.registerChatMessageProvider("b", "msg", "Msg", (m, bundle, id) -> "m");
        ipc.registerManuscriptProvider("other", "k", "K", (m, bundle, id) -> "k");

        ipc.unregisterManuscriptProvider("b", "one");
        assertThat(ipc.getManuscriptProviders().get("b").keySet()).containsExactly("two");

        ipc.unregisterAll("b");
        assertThat(ipc.getManuscriptProviders().keySet()).containsExactly("other");
        assertThat(ipc.getChatMessageProviders()).isEmpty();
        assertThat(ipc.getManuscriptData("b", "two", new Manuscript(), null, "c")).isNull();
    }

    @Test
    void listingIsASnapshot() {
        ipc.registerManuscriptProvider("b", "k", "K", (m, bundle, id) -> "x");
        Map<String, Map<String, String>> before = ipc.getManuscriptProviders();
        ipc.registerManuscriptProvider("b", "later", "Later", (m, bundle, id) -> "y");
        ipc.registerManuscriptProvider("c", "k", "K", (m, bundle, id) -> "z");

        assertThat(before.keySet()).containsExactly("b");
        assertThat(before.get("b").keySet()).containsExactly("k");
        assertThatThrownBy(() -> before.put("x", Map.of())).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> before.get("b").put("x", "y")).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void registrationNeedsIdentifiersAndCallback() {
        assertThatThrownBy(() -> ipc.registerManuscriptProvider(" ", "k", "K", (m, b, id) -> "x"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ipc.registerManuscriptProvider("b", null, "K", (m, b, id) -> "x"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ipc.registerChatMessageProvider("b", "k", "K", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nameDefaultsToTheKey() {
        ipc.registerManuscriptProvider("b", "key", null, (m, bundle, id) -> "x");
        assertThat(ipc.getManuscriptProviders().get("b")).containsEntry("key", "key");
    }
}
