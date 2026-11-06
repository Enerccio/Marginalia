package com.github.enerccio.marginalia.extensions.namegenerator;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The categories other extensions register: the chain the generator trains on their words, the generators they give, who owns what, and what
 * happens when the owner goes away.
 */
class NameSourcesTest {

    /** A bundle that has a name and a state, nothing else. */
    private static final class Fake {
        int state = Bundle.ACTIVE;
        final Bundle bundle;

        Fake(String name) {
            bundle = (Bundle) Proxy.newProxyInstance(Bundle.class.getClassLoader(), new Class<?>[]{Bundle.class}, (proxy, method, args) ->
                    switch (method.getName()) {
                        case "getSymbolicName" -> name;
                        case "getState" -> state;
                        default -> throw new UnsupportedOperationException(method.getName());
                    });
        }
    }

    private static final NameTables SHIPPED = new NameTables(List.of(NameTables.train("Shipped - Male", List.of("alaric", "boris"), 2, null, null)));

    private final NameSources sources = new NameSources();
    private final NameCatalog catalog = new NameCatalog(SHIPPED, sources);
    private final Fake conlang = new Fake("conlang-translator");

    private void words(Fake owner, String title, Collection<String> words) {
        sources.registerWords(owner.bundle, title, () -> words, null, SHIPPED::has);
    }

    @Test
    void theGeneratorTrainsItsChainOnTheWordsAndMakesNamesLikeThem() {
        words(conlang, "Conlang - Vrakht", List.of("drak", "drok", "vrak", "krom"));

        List<String> names = catalog.generate("conlang - vrakht", 20, null, null, new Random(1));

        assertFalse(names.isEmpty());
        for (String name : names) {
            assertTrue(name.length() >= 4 && name.length() <= 4, name);
            assertTrue(name.matches("[DVK][a-z]+"), name);
            assertTrue(name.toLowerCase().chars().allMatch(c -> "drakovm".indexOf(c) >= 0), name);
        }
    }

    @Test
    void aRegisteredCategoryIsListedAndFilteredWithTheOthers() {
        words(conlang, "Conlang - Vrakht", List.of("drak"));

        assertEquals(List.of("Conlang - Vrakht", "Shipped - Male"), catalog.categories());
        assertEquals(List.of("Conlang - Vrakht"), catalog.categories("vrakht"));
        assertTrue(catalog.has("CONLANG - VRAKHT"));
        assertFalse(catalog.has("Conlang - Other"));
        // the shipped ones still work
        assertFalse(catalog.generate("Shipped - Male", 2, null, null, new Random(2)).isEmpty());
    }

    @Test
    void theWordsAreAskedForAgainAndTheChainFollowsThem() {
        List<String> words = new ArrayList<>(List.of("aaaa"));
        sources.registerWords(conlang.bundle, "Conlang - L", () -> words, null, SHIPPED::has);
        assertEquals(List.of("Aaaa"), catalog.generate("Conlang - L", 5, null, null, new Random(3)));

        words.clear();
        words.add("bbbb");

        assertEquals(List.of("Bbbb"), catalog.generate("Conlang - L", 5, null, null, new Random(3)));
    }

    @Test
    void theChainIsTrainedOnlyWhenTheWordsChanged() {
        AtomicInteger asked = new AtomicInteger();
        sources.registerWords(conlang.bundle, "Conlang - L", () -> {
            asked.incrementAndGet();
            return List.of("drak", "drok");
        }, null, SHIPPED::has);

        catalog.generate("Conlang - L", 3, null, null, new Random(4));
        catalog.generate("Conlang - L", 3, null, null, new Random(5));

        assertEquals(2, asked.get(), "the owner is asked each time, it is cheap");
        // the chain is the same object while the words are: a second call must not fail on a trained one
    }

    @Test
    void theOptionsSetTheOrderAndTheLengths() {
        sources.registerWords(conlang.bundle, "Conlang - L", () -> List.of("abcdefgh"),
                Map.of(NameGeneratorExtension.OPTION_ORDER, 3, NameGeneratorExtension.OPTION_MIN_LENGTH, 5,
                        NameGeneratorExtension.OPTION_MAX_LENGTH, 6), SHIPPED::has);

        // every name is cut off by the maximum: the word is 8 letters, the names can't reach it
        assertTrue(catalog.generate("Conlang - L", 3, null, null, new Random(6)).isEmpty());
        assertEquals(List.of("Abcdefgh"), catalog.generate("Conlang - L", 3, 8, 8, new Random(6)));
    }

    @Test
    void aGeneratorIsAProxyForTheOwner() {
        List<Map<String, Object>> asked = new ArrayList<>();
        sources.registerGenerator(conlang.bundle, "Conlang - Places", request -> {
            asked.add(request);
            return Arrays.asList(" Vrakhdor ", "Vrakhdor", null, "", "Kromgar", "Third");
        }, SHIPPED::has);

        List<String> names = catalog.generate("Conlang - Places", 2, 4, null, new Random(7));

        assertEquals(List.of("Vrakhdor", "Kromgar"), names);
        assertEquals(1, asked.size());
        assertEquals(2, asked.getFirst().get(NameGeneratorExtension.REQUEST_COUNT));
        assertEquals(4, asked.getFirst().get(NameGeneratorExtension.REQUEST_MIN_LENGTH));
        assertFalse(asked.getFirst().containsKey(NameGeneratorExtension.REQUEST_MAX_LENGTH));
        assertThrows(UnsupportedOperationException.class, () -> asked.getFirst().put("x", 1));
    }

    @Test
    void aFailingOwnerIsReportedAsAFailedCallAndNamesTheOwner() {
        sources.registerGenerator(conlang.bundle, "Conlang - Places", request -> {
            throw new IllegalArgumentException("no lexicon");
        }, SHIPPED::has);
        sources.registerWords(conlang.bundle, "Conlang - Empty", List::of, null, SHIPPED::has);

        IllegalStateException failed = assertThrows(IllegalStateException.class, () -> catalog.generate("Conlang - Places", 1, null, null, new Random(8)));
        assertTrue(failed.getMessage().contains("conlang-translator") && failed.getMessage().contains("no lexicon"), failed.getMessage());
        assertThrows(IllegalStateException.class, () -> catalog.generate("Conlang - Empty", 1, null, null, new Random(8)));
    }

    @Test
    void aTitleOfTheGeneratorOrOfAnotherOwnerIsRefusedAndOwnTitleIsReplaced() {
        Fake other = new Fake("another-plugin");
        words(conlang, "Conlang - L", List.of("aaaa"));

        assertThrows(IllegalArgumentException.class, () -> words(conlang, "shipped - male", List.of("aaaa")));
        assertThrows(IllegalArgumentException.class, () -> words(other, "CONLANG - L", List.of("bbbb")));
        assertThrows(IllegalArgumentException.class, () -> words(conlang, "  ", List.of("aaaa")));
        assertThrows(IllegalArgumentException.class, () -> sources.registerWords(null, "X", List::of, null, SHIPPED::has));
        assertThrows(IllegalArgumentException.class, () -> sources.registerWords(conlang.bundle, "X", null, null, SHIPPED::has));

        words(conlang, "conlang - l", List.of("bbbb"));
        assertEquals(List.of("Bbbb"), catalog.generate("Conlang - L", 3, null, null, new Random(9)));
    }

    @Test
    void unregisteringIsForTheOwnerOnly() {
        Fake other = new Fake("another-plugin");
        words(conlang, "Conlang - A", List.of("aaaa"));
        words(conlang, "Conlang - B", List.of("bbbb"));
        words(other, "Other - C", List.of("cccc"));

        assertFalse(sources.unregister(other.bundle, "Conlang - A"));
        assertTrue(sources.unregister(conlang.bundle, "conlang - a"));
        assertFalse(sources.unregister(conlang.bundle, "Conlang - A"));
        assertEquals(1, sources.unregisterAll(conlang.bundle));
        assertEquals(List.of("Other - C", "Shipped - Male"), catalog.categories());
    }

    @Test
    void theCategoriesOfABundleThatIsNotRunningAreGone() {
        words(conlang, "Conlang - A", List.of("aaaa"));
        assertTrue(catalog.has("Conlang - A"));

        conlang.state = Bundle.RESOLVED;

        assertFalse(catalog.has("Conlang - A"));
        assertEquals(List.of("Shipped - Male"), catalog.categories());
        assertThrows(IllegalArgumentException.class, () -> catalog.generate("Conlang - A", 1, null, null, new Random(10)));
        // the title is free for the next bundle of that name or another
        words(new Fake("another-plugin"), "Conlang - A", List.of("aaaa"));
        assertTrue(catalog.has("Conlang - A"));
    }

    @Test
    void trainingSkipsWordsTheChainCannotHold() {
        NameTables.Category category = NameTables.train("T", Arrays.asList("Drak", " ", null, "a^b", "c$d", "KROM"), 2, null, null);

        assertEquals(2, category.size());
        assertEquals(4, category.minLength());
        assertEquals(4, category.maxLength());
        assertThrows(IllegalArgumentException.class, () -> NameTables.train("T", List.of("^", " "), 2, null, null));
    }

    @Test
    void aPreviewMakesNamesFromWordsAndRegistersNothing() {
        List<String> names = NameSources.preview(List.of("drak", "drok", "vrak"), null, 10, new Random(11));

        assertFalse(names.isEmpty());
        assertTrue(names.stream().allMatch(n -> n.matches("[DV][a-z]+")), names.toString());
        assertEquals(List.of("Shipped - Male"), catalog.categories());
        assertThrows(IllegalArgumentException.class, () -> NameSources.preview(List.of(" ", "^"), null, 10, new Random(11)));
        assertThrows(IllegalArgumentException.class, () -> NameSources.preview(null, null, 10, new Random(11)));
    }

    @Test
    void aProviderAnswersForTheCallerWhoAsks() {
        String[] caller = {"anna"};
        Map<String, List<String>> perUser = Map.of(
                "anna", List.of("drak", "drok", "vrak"),
                "boris", List.of("aaaa"));
        sources.registerProvider(conlang.bundle, "names", () -> {
            if (caller[0] == null) {
                throw new IllegalStateException("no session");
            }
            return Map.of("Conlang - " + caller[0] + " - Places", perUser.get(caller[0]));
        });

        assertEquals(List.of("Conlang - anna - Places", "Shipped - Male"), catalog.categories());
        assertTrue(catalog.has("conlang - ANNA - places"));
        assertFalse(catalog.has("Conlang - boris - Places"));
        assertTrue(catalog.generate("Conlang - anna - Places", 5, null, null, new Random(12)).stream().allMatch(n -> n.matches("[DV][a-z]+")));

        caller[0] = "boris";
        assertEquals(List.of("Conlang - boris - Places", "Shipped - Male"), catalog.categories());
        assertEquals(List.of("Aaaa"), catalog.generate("Conlang - boris - Places", 5, null, null, new Random(12)));
        assertThrows(IllegalArgumentException.class, () -> catalog.generate("Conlang - anna - Places", 1, null, null, new Random(12)));

        // a thread without a session has nothing from this provider, the shipped categories stay
        caller[0] = null;
        assertEquals(List.of("Shipped - Male"), catalog.categories());
    }

    @Test
    void aProviderCategoryWithoutWordsIsNotListedAndTheShippedOnesWin() {
        sources.registerProvider(conlang.bundle, "names", () -> Map.of("Conlang - Empty", List.of(), "shipped - male", List.of("zzzz")));

        assertEquals(List.of("Shipped - Male"), catalog.categories());
        assertFalse(catalog.generate("Shipped - Male", 2, null, null, new Random(13)).contains("Zzzz"));
    }

    @Test
    void aProviderFollowsItsWordsAndGoesWithItsOwner() {
        List<String> words = new ArrayList<>(List.of("aaaa"));
        sources.registerProvider(conlang.bundle, "names", () -> Map.of("Conlang - L", words));
        assertEquals(List.of("Aaaa"), catalog.generate("Conlang - L", 3, null, null, new Random(14)));

        words.clear();
        words.add("bbbb");
        assertEquals(List.of("Bbbb"), catalog.generate("Conlang - L", 3, null, null, new Random(14)));

        // the same key replaces, another key is another provider, and unregistering is the owner's
        sources.registerProvider(conlang.bundle, "names", () -> Map.of("Conlang - M", List.of("cccc")));
        assertEquals(List.of("Conlang - M", "Shipped - Male"), catalog.categories());
        assertFalse(sources.unregisterProvider(new Fake("another-plugin").bundle, "names"));
        assertTrue(sources.unregisterProvider(conlang.bundle, "names"));
        assertEquals(List.of("Shipped - Male"), catalog.categories());

        sources.registerProvider(conlang.bundle, "names", () -> Map.of("Conlang - M", List.of("cccc")));
        assertEquals(1, sources.unregisterAll(conlang.bundle));
        conlang.state = Bundle.RESOLVED;
        sources.registerProvider(conlang.bundle, "names", () -> Map.of("Conlang - N", List.of("dddd")));
        assertEquals(List.of("Shipped - Male"), catalog.categories(), "a bundle that is not running has no provider");
    }
}
