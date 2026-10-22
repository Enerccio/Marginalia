package com.github.enerccio.marginalia.domain.agents.jsonmerge;

import com.github.enerccio.marginalia.domain.service.agent.jsonmerge.JsonMerge;
import com.github.enerccio.marginalia.domain.service.agent.jsonmerge.JsonMergeNode;
import com.github.enerccio.marginalia.domain.service.agent.jsonmerge.JsonMergeNode.Choice;
import com.github.enerccio.marginalia.domain.service.agent.jsonmerge.JsonMergeNode.Side;
import com.github.enerccio.marginalia.domain.service.agent.jsonmerge.JsonMergeNode.Status;
import com.github.enerccio.marginalia.domain.service.agent.jsonmerge.JsonText;
import com.github.enerccio.marginalia.domain.service.agent.jsonmerge.ListMatcher;
import com.github.enerccio.marginalia.domain.service.agent.jsonmerge.ListMatcher.Row;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The comparison of the three versions of JSON data behind the merge of attributes: the texts, the matching of the elements of
 * a list and the tree that puts the versions together.
 */
class JsonMergeTest {

    private static JsonElement json(String text) {
        return JsonParser.parseString(text);
    }

    private static JsonMergeNode node(String base, String agent, String book) {
        return JsonMergeNode.root(base == null ? null : json(base), agent == null ? null : json(agent), book == null ? null : json(book));
    }

    private static String result(JsonMergeNode node) {
        return JsonText.compact(node.result());
    }

    private static JsonMergeNode child(JsonMergeNode node, String title) {
        return node.children().stream().filter(c -> c.title().startsWith(title)).findFirst().orElseThrow();
    }

    // --- the texts

    @Test
    void compactTextHasSortedKeysAndKeepsNulls() {
        assertThat(JsonText.compact(json("{\"b\":1,\"a\":{\"z\":null,\"y\":\"<x>\"}}"))).isEqualTo("{\"a\":{\"y\":\"<x>\",\"z\":null},\"b\":1}");
        assertThat(JsonText.compact(null)).isEmpty();
    }

    @Test
    void prettyTextIsGsonsPrettyPrinting() {
        assertThat(JsonText.pretty(json("{\"a\":[1,2],\"b\":null}"))).isEqualTo("{\n  \"a\": [\n    1,\n    2\n  ],\n  \"b\": null\n}");
        assertThat(JsonText.pretty("{\"a\":1}")).isEqualTo("{\n  \"a\": 1\n}");
        assertThat(JsonText.pretty("not json")).isEqualTo("not json");
        assertThat(JsonText.pretty((JsonElement) null)).isEmpty();
    }

    @Test
    void parsingIsStrict() {
        assertThat(JsonText.parse("  ")).isNull();
        assertThat(JsonText.parse(null)).isNull();
        assertThat(JsonText.parse("\"text\"").getAsString()).isEqualTo("text");
        assertThat(JsonText.parse("12").getAsInt()).isEqualTo(12);
        assertThat(JsonText.parse("{\"a\": [1, 2]}").isJsonObject()).isTrue();
        for (String bad : List.of("hello", "{'a': 1}", "{\"a\": 1,}", "{\"a\": 1} x", "[1, 2", "{a: 1}")) {
            assertThatThrownBy(() -> JsonText.parse(bad)).as(bad).isInstanceOf(JsonText.InvalidJsonException.class);
            assertThat(JsonText.isValid(bad)).as(bad).isFalse();
        }
    }

    // --- the elements of a list

    private static List<JsonElement> list(String text) {
        List<JsonElement> elements = new ArrayList<>();
        json(text).getAsJsonArray().forEach(elements::add);
        return elements;
    }

    @Test
    void sameDataIsMatchedFirst() {
        assertThat(ListMatcher.match(list("[\"a\",\"b\",\"c\"]"), list("[\"c\",\"a\"]"))).containsExactly(1, -1, 0);
        // equal elements are used one by one
        assertThat(ListMatcher.match(list("[1,1,1]"), list("[1,1]"))).containsExactly(0, 1, -1);
    }

    @Test
    void identityMatchesTheElementThatChanged() {
        int[] match = ListMatcher.match(list("[{\"id\":1,\"v\":\"a\"},{\"id\":2,\"v\":\"b\"}]"),
                list("[{\"id\":2,\"v\":\"completely different\"},{\"id\":1,\"v\":\"changed\"},{\"id\":3,\"v\":\"new\"}]"));
        assertThat(match).containsExactly(1, 0);
    }

    @Test
    void uuidIsTrustedBeforeName() {
        int[] match = ListMatcher.match(list("[{\"uuid\":\"u1\",\"name\":\"x\"},{\"uuid\":\"u2\",\"name\":\"y\"}]"),
                list("[{\"uuid\":\"u2\",\"name\":\"x\"},{\"uuid\":\"u1\",\"name\":\"y\"}]"));
        assertThat(match).containsExactly(1, 0);
    }

    @Test
    void elementsThatLookAlikeAreMatched() {
        int[] match = ListMatcher.match(
                list("[{\"role\":\"hero\",\"mood\":\"calm\",\"place\":\"the castle\"},{\"role\":\"villain\",\"mood\":\"angry\",\"place\":\"the swamp\"}]"),
                list("[{\"role\":\"villain\",\"mood\":\"furious\",\"place\":\"the swamp\"},{\"role\":\"hero\",\"mood\":\"tired\",\"place\":\"the castle\"}]"));
        assertThat(match).containsExactly(1, 0);
        // texts by their words
        assertThat(ListMatcher.match(list("[\"the dragon sleeps in the cave\"]"), list("[\"a goblin\",\"the dragon sleeps in a deep cave\"]")))
                .containsExactly(1);
    }

    @Test
    void whatIsLeftAndOfTheSameKindReplacedEachOther() {
        assertThat(ListMatcher.match(list("[1,2,3]"), list("[1,5,3]"))).containsExactly(0, 1, 2);
        // two versions that both added elements did not replace each other
        assertThat(ListMatcher.match(list("[\"n\",\"m\"]"), list("[\"n\",\"o\"]"), false)).containsExactly(0, -1);
        // not of the same kind, or not as many: added and removed
        assertThat(ListMatcher.match(list("[1]"), list("[\"x\"]"))).containsExactly(-1);
        assertThat(ListMatcher.match(list("[1,2]"), list("[1,5,6]"))[1]).isEqualTo(-1);
    }

    @Test
    void rowsFollowTheAgentAndKeepTheOthersNextToTheirNeighbours() {
        List<Row> rows = ListMatcher.align(list("[\"a\",\"b\",\"c\"]"), list("[\"a\",\"x\",\"c\"]"), list("[\"a\",\"b\",\"c\",\"d\"]"));
        // b became x for the agent (replaced), d is new in the book
        assertThat(rows).containsExactly(new Row(0, 0, 0), new Row(1, 1, 1), new Row(2, 2, 2), new Row(-1, -1, 3));
        rows = ListMatcher.align(list("[\"a\",\"b\"]"), list("[\"b\"]"), list("[\"a\",\"b\",\"n\"]"));
        assertThat(rows).containsExactly(new Row(0, -1, 0), new Row(1, 0, 1), new Row(-1, -1, 2));
        // what both added is one element
        rows = ListMatcher.align(list("[]"), list("[\"n\"]"), list("[\"n\"]"));
        assertThat(rows).containsExactly(new Row(-1, 0, 0));
    }

    // --- the tree

    @Test
    void changesInDifferentKeysAreBothTaken() {
        JsonMergeNode root = node("{\"a\":1,\"b\":2,\"c\":3}", "{\"a\":5,\"b\":2,\"c\":3}", "{\"a\":1,\"b\":9,\"c\":3}");
        assertThat(root.choice()).isEqualTo(Choice.MERGED);
        assertThat(result(root)).isEqualTo("{\"a\":5,\"b\":9,\"c\":3}");
        assertThat(root.conflict()).isFalse();
        assertThat(root.unresolved()).isZero();
        assertThat(child(root, "a").status()).isEqualTo(Status.AGENT);
        assertThat(child(root, "b").status()).isEqualTo(Status.BOOK);
        assertThat(child(root, "c").status()).isEqualTo(Status.UNCHANGED);
        assertThat(root.status()).isEqualTo(Status.BOTH);
    }

    @Test
    void theSameKeyChangedByBothIsAConflictTheAgentWinsAndTheUserDecides() {
        JsonMergeNode root = node("{\"a\":1,\"b\":2}", "{\"a\":5,\"b\":2}", "{\"a\":7,\"b\":3}");
        assertThat(result(root)).isEqualTo("{\"a\":5,\"b\":3}");
        assertThat(root.conflict()).isTrue();
        assertThat(root.status()).isEqualTo(Status.CONFLICT);
        assertThat(root.unresolved()).isEqualTo(1);

        child(root, "a").choose(Choice.BOOK);
        assertThat(result(root)).isEqualTo("{\"a\":7,\"b\":3}");
        assertThat(root.unresolved()).isZero();
        // the conflict is still there to be seen
        assertThat(child(root, "a").conflict()).isTrue();
        child(root, "a").choose(Choice.BASE);
        assertThat(result(root)).isEqualTo("{\"a\":1,\"b\":3}");
    }

    @Test
    void bothChangingToTheSameDataIsNoConflict() {
        JsonMergeNode root = node("{\"a\":1}", "{\"a\":2}", "{\"a\":2}");
        assertThat(root.conflict()).isFalse();
        assertThat(child(root, "a").status()).isEqualTo(Status.SAME);
        assertThat(result(root)).isEqualTo("{\"a\":2}");
    }

    @Test
    void deletingWhatTheOtherChangedIsAConflict() {
        JsonMergeNode root = node("{\"a\":1,\"b\":2}", "{\"b\":2}", "{\"a\":8,\"b\":2}");
        assertThat(root.conflict()).isTrue();
        assertThat(result(root)).isEqualTo("{\"b\":2}");
        child(root, "a").choose(Choice.BOOK);
        assertThat(result(root)).isEqualTo("{\"a\":8,\"b\":2}");
    }

    @Test
    void keysOnlyOneSideAddedAreKept() {
        JsonMergeNode root = node("{}", "{\"x\":1}", "{\"y\":2}");
        assertThat(result(root)).isEqualTo("{\"x\":1,\"y\":2}");
        assertThat(root.conflict()).isFalse();
        // taking the key away again
        child(root, "x").choose(Choice.BOOK);
        assertThat(result(root)).isEqualTo("{\"y\":2}");
    }

    @Test
    void deeperPlacesAreMergedToo() {
        JsonMergeNode root = node("{\"o\":{\"p\":{\"q\":1,\"r\":1}}}", "{\"o\":{\"p\":{\"q\":2,\"r\":1}}}", "{\"o\":{\"p\":{\"q\":1,\"r\":3}}}");
        assertThat(result(root)).isEqualTo("{\"o\":{\"p\":{\"q\":2,\"r\":3}}}");
        assertThat(root.children()).hasSize(1);
        assertThat(child(root, "o").choice()).isEqualTo(Choice.MERGED);
    }

    @Test
    void childrenAreMadeWhenTheyAreAskedFor() {
        JsonMergeNode root = node("{\"o\":{\"p\":1}}", "{\"o\":{\"p\":2}}", "{\"o\":{\"p\":1}}");
        assertThat(root.isLoaded()).isFalse();
        assertThat(root.hasChildren()).isTrue();
        assertThat(root.isLoaded()).isFalse();
        JsonMergeNode o = child(root, "o");
        assertThat(o.isLoaded()).isFalse();
        // the agent alone changed it: the node knows it without looking inside
        assertThat(o.status()).isEqualTo(Status.AGENT);
        assertThat(result(root)).isEqualTo("{\"o\":{\"p\":2}}");
    }

    @Test
    void choosingInsideAWholeKeepsTheRestOfTheWhole() {
        JsonMergeNode root = node("{\"o\":{\"p\":1,\"q\":1}}", "{\"o\":{\"p\":2,\"q\":2}}", "{\"o\":{\"p\":1,\"q\":1}}");
        // the agent changed everything in o, o is the agent's as a whole
        JsonMergeNode o = child(root, "o");
        assertThat(o.choice()).isEqualTo(Choice.AGENT);
        child(o, "p").choose(Choice.BOOK);
        assertThat(o.choice()).isEqualTo(Choice.MERGED);
        assertThat(result(root)).isEqualTo("{\"o\":{\"p\":1,\"q\":2}}");
    }

    @Test
    void aWholeCanBeTakenFromAVersion() {
        JsonMergeNode root = node("{\"o\":{\"p\":1}}", "{\"o\":{\"p\":2}}", "{\"o\":{\"p\":3}}");
        assertThat(root.conflict()).isTrue();
        child(root, "o").choose(Choice.BOOK);
        assertThat(result(root)).isEqualTo("{\"o\":{\"p\":3}}");
        assertThat(root.unresolved()).isZero();
        root.choose(Choice.BASE);
        assertThat(result(root)).isEqualTo("{\"o\":{\"p\":1}}");
        assertThatThrownBy(() -> child(child(root, "o"), "p").choose(Choice.MERGED)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void elementsOfAListAreMergedByWhatTheyAre() {
        JsonMergeNode root = node(
                "{\"l\":[{\"id\":1,\"v\":\"a\"},{\"id\":2,\"v\":\"b\"}]}",
                "{\"l\":[{\"id\":1,\"v\":\"A\"},{\"id\":2,\"v\":\"b\"},{\"id\":3,\"v\":\"new\"}]}",
                "{\"l\":[{\"id\":2,\"v\":\"B\"},{\"id\":1,\"v\":\"a\"}]}");
        assertThat(root.conflict()).isFalse();
        assertThat(result(root)).isEqualTo("{\"l\":[{\"id\":1,\"v\":\"A\"},{\"id\":2,\"v\":\"B\"},{\"id\":3,\"v\":\"new\"}]}");
        JsonMergeNode list = child(root, "l");
        assertThat(list.children()).hasSize(3);
        assertThat(list.children().get(0).title()).startsWith("[0] ").isNotEmpty();
    }

    @Test
    void anElementBothRemovedOrOneRemovedAndTheOtherLeftIsGone() {
        JsonMergeNode root = node("[\"a\",\"b\",\"c\"]", "[\"a\",\"c\"]", "[\"a\",\"b\"]");
        assertThat(result(root)).isEqualTo("[\"a\"]");
        assertThat(root.conflict()).isFalse();
    }

    @Test
    void anElementBothAddedIsOne() {
        JsonMergeNode root = node("[]", "[\"n\",\"m\"]", "[\"n\",\"o\"]");
        // the element only the book has goes after the one before it in the book
        assertThat(result(root)).isEqualTo("[\"n\",\"o\",\"m\"]");
    }

    @Test
    void anElementCanBeCopiedFromTheBookOrLeftOut() {
        JsonMergeNode root = node("[\"a\",\"b\"]", "[\"a\"]", "[\"a\",\"b\",\"n\"]");
        // the agent dropped b, the book did not touch it: gone; n is the book's addition: stays
        assertThat(result(root)).isEqualTo("[\"a\",\"n\"]");
        JsonMergeNode b = root.children().stream().filter(c -> c.get(Side.BASE) != null && c.get(Side.BASE).getAsString().equals("b")).findFirst().orElseThrow();
        b.choose(Choice.BOOK);
        assertThat(result(root)).isEqualTo("[\"a\",\"b\",\"n\"]");
        JsonMergeNode n = root.children().stream().filter(c -> c.get(Side.BOOK) != null && c.get(Side.BOOK).getAsString().equals("n")).findFirst().orElseThrow();
        n.choose(Choice.AGENT);
        assertThat(result(root)).isEqualTo("[\"a\",\"b\"]");
    }

    @Test
    void aPlaceCanBeLeftOut() {
        JsonMergeNode root = node("{\"a\":1,\"b\":2}", "{\"a\":1,\"b\":2}", "{\"a\":1,\"b\":2}");
        child(root, "a").choose(Choice.NONE);
        assertThat(result(root)).isEqualTo("{\"b\":2}");
        JsonMergeNode list = node("[1,2,3]", "[1,2,3]", "[1,2,3]");
        list.children().get(1).choose(Choice.NONE);
        assertThat(result(list)).isEqualTo("[1,3]");
    }

    @Test
    void aPlaceTakenAsAWholeRulesOverWhatIsInsideIt() {
        JsonMergeNode root = node("{\"o\":{\"p\":1,\"q\":1}}", "{\"o\":{\"p\":2,\"q\":1}}", "{\"o\":{\"p\":1,\"q\":3}}");
        JsonMergeNode o = child(root, "o");
        JsonMergeNode p = child(o, "p");
        assertThat(JsonText.compact(p.effectiveResult())).isEqualTo("2");
        assertThat(p.isOverruled()).isFalse();
        // p was taken from the agent, but o is the book's now; the choice inside is not lost, only not used
        o.choose(Choice.BOOK);
        assertThat(o.choice()).isEqualTo(Choice.BOOK);
        assertThat(JsonText.compact(o.effectiveResult())).isEqualTo("{\"p\":1,\"q\":3}");
        root.choose(Choice.BASE);
        assertThat(p.isOverruled()).isTrue();
        assertThat(p.effectiveChoice()).isEqualTo(Choice.BASE);
        assertThat(JsonText.compact(p.effectiveResult())).isEqualTo("1");
        assertThat(JsonText.compact(o.effectiveResult())).isEqualTo("{\"p\":1,\"q\":1}");
    }

    @Test
    void conflictsAreCounted() {
        JsonMergeNode root = node("{\"a\":1,\"b\":1,\"c\":{\"d\":1}}", "{\"a\":2,\"b\":2,\"c\":{\"d\":2}}", "{\"a\":3,\"b\":1,\"c\":{\"d\":3}}");
        assertThat(root.conflicts()).isEqualTo(2);
        assertThat(root.unresolved()).isEqualTo(2);
        child(child(root, "c"), "d").choose(Choice.AGENT);
        assertThat(root.conflicts()).isEqualTo(2);
        assertThat(root.unresolved()).isEqualTo(1);
    }

    @Test
    void theWholeDocumentCanStartOver() {
        JsonMergeNode root = node("{\"a\":1}", "{\"a\":2}", "{\"a\":3}");
        child(root, "a").choose(Choice.BOOK);
        root.setAgentText("{\"a\":9}");
        child(root, "a").choose(Choice.NONE);
        root.reset();
        assertThat(root.agentText()).isEqualTo("{\n  \"a\": 2\n}");
        assertThat(result(root)).isEqualTo("{\"a\":2}");
        assertThat(root.unresolved()).isEqualTo(1);
        assertThatThrownBy(() -> child(root, "a").reset()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nothingMeansTheDataIsNotThere() {
        JsonMergeNode root = node(null, "{\"a\":1}", null);
        assertThat(result(root)).isEqualTo("{\"a\":1}");
        root = node("{\"a\":1}", null, "{\"a\":1}");
        assertThat(result(root)).isEmpty();
        root = node("{\"a\":1}", null, "{\"a\":2}");
        assertThat(root.conflict()).isTrue();
        assertThat(result(root)).isEmpty();
        root.choose(Choice.BOOK);
        assertThat(result(root)).isEqualTo("{\"a\":2}");
    }

    @Test
    void valuesOfAnotherKindAreAConflict() {
        JsonMergeNode root = node("{\"a\":[1]}", "{\"a\":{\"x\":1}}", "{\"a\":[1,2]}");
        assertThat(root.conflict()).isTrue();
        assertThat(child(root, "a").hasChildren()).isFalse();
        assertThat(result(root)).isEqualTo("{\"a\":{\"x\":1}}");
    }

    @Test
    void theDataIsCopiedNotShared() {
        JsonElement agent = json("{\"a\":{\"b\":1}}");
        JsonMergeNode root = JsonMergeNode.root(json("{\"a\":{\"b\":0}}"), agent, json("{\"a\":{\"b\":0}}"));
        child(root, "a").setAgentText("{\"b\":2}");
        assertThat(agent).isEqualTo(json("{\"a\":{\"b\":1}}"));
        JsonElement out = root.result();
        out.getAsJsonObject().get("a").getAsJsonObject().addProperty("b", 99);
        assertThat(result(root)).isEqualTo("{\"a\":{\"b\":2}}");
    }

    // --- the agent's data as text

    @Test
    void theAgentsDataIsEditedAsText() {
        JsonMergeNode root = node("{\"a\":{\"x\":1,\"y\":1}}", "{\"a\":{\"x\":2,\"y\":1}}", "{\"a\":{\"x\":3,\"y\":1}}");
        JsonMergeNode a = child(root, "a");
        assertThat(a.conflict()).isTrue();
        assertThat(a.agentText()).isEqualTo("{\n  \"x\": 2,\n  \"y\": 1\n}");

        a.setAgentText("{\n  \"x\": 3,\n  \"y\": 4\n}");
        // x is what the book has now, y is the agent's
        assertThat(a.conflict()).isFalse();
        assertThat(root.conflict()).isFalse();
        assertThat(result(root)).isEqualTo("{\"a\":{\"x\":3,\"y\":4}}");
        assertThat(child(a, "y").status()).isEqualTo(Status.AGENT);
    }

    @Test
    void textThatIsNotJsonChangesNothing() {
        JsonMergeNode root = node("{\"a\":1}", "{\"a\":2}", "{\"a\":1}");
        JsonMergeNode a = child(root, "a");
        assertThatThrownBy(() -> a.setAgentText("{\"a\": ")).isInstanceOf(JsonText.InvalidJsonException.class);
        assertThatThrownBy(() -> a.setAgentText("two")).isInstanceOf(JsonText.InvalidJsonException.class);
        assertThat(a.agentText()).isEqualTo("2");
        assertThat(result(root)).isEqualTo("{\"a\":2}");
    }

    @Test
    void emptyTextRemovesTheAgentsData() {
        JsonMergeNode root = node("{\"a\":1,\"b\":2}", "{\"a\":1,\"b\":3}", "{\"a\":1,\"b\":2}");
        child(root, "b").setAgentText("  ");
        assertThat(child(root, "b").get(Side.AGENT)).isNull();
        assertThat(result(root)).isEqualTo("{\"a\":1}");
        // and back
        child(root, "b").setAgentText("5");
        assertThat(result(root)).isEqualTo("{\"a\":1,\"b\":5}");
    }

    @Test
    void editingTheTextOfThePlaceAboveRecomputesThePlacesInside() {
        JsonMergeNode root = node("{\"a\":{\"x\":1}}", "{\"a\":{\"x\":1}}", "{\"a\":{\"x\":2}}");
        assertThat(result(root)).isEqualTo("{\"a\":{\"x\":2}}");
        root.setAgentText("{\"a\":{\"x\":1,\"z\":[1,2]}}");
        assertThat(result(root)).isEqualTo("{\"a\":{\"x\":2,\"z\":[1,2]}}");
        assertThat(root.agentText()).contains("\"z\": [");
    }

    @Test
    void anEditOfThePlaceInsideIsNotLostByThePlaceAbove() {
        // the agent did not change a at all, so a is the book's as a whole; the edit makes the agent's data different
        JsonMergeNode root = node("{\"a\":{\"x\":1,\"y\":1}}", "{\"a\":{\"x\":1,\"y\":1}}", "{\"a\":{\"x\":1,\"y\":2}}");
        assertThat(child(root, "a").choice()).isEqualTo(Choice.BOOK);
        child(child(root, "a"), "x").setAgentText("7");
        assertThat(result(root)).isEqualTo("{\"a\":{\"x\":7,\"y\":2}}");
        assertThat(root.agentText()).contains("\"x\": 7");
    }

    @Test
    void theAgentsDataCanBeAddedWhereItHadNone() {
        JsonMergeNode root = node("{\"a\":{\"x\":1}}", "{}", "{\"a\":{\"x\":1}}");
        assertThat(result(root)).isEqualTo("{}");
        JsonMergeNode a = child(root, "a");
        child(a, "x").setAgentText("5");
        assertThat(root.agentText()).isEqualTo("{\n  \"a\": {\n    \"x\": 5\n  }\n}");
        assertThat(result(root)).isEqualTo("{\"a\":{\"x\":5}}");
    }

    @Test
    void elementsOfAListCanBeEditedAndRemovedAsText() {
        JsonMergeNode root = node("[1,2,3]", "[1,2,3]", "[1,2,3]");
        List<JsonMergeNode> rows = root.children();
        rows.get(1).setAgentText("");
        assertThat(root.agentText()).isEqualTo("[\n  1,\n  3\n]");
        rows.get(2).setAgentText("9");
        assertThat(root.agentText()).isEqualTo("[\n  1,\n  9\n]");
        assertThat(result(root)).isEqualTo("[1,9]");
    }

    // --- correcting the guess

    @Test
    void elementsCanBeSplitAndMatchedAgain() {
        JsonMergeNode root = node("[{\"name\":\"a\",\"v\":1}]", "[{\"name\":\"a\",\"v\":2}]", "[{\"name\":\"a\",\"v\":1}]");
        JsonMergeNode row = root.children().getFirst();
        assertThat(row.canSplit()).isTrue();
        assertThat(result(root)).isEqualTo("[{\"name\":\"a\",\"v\":2}]");

        List<JsonMergeNode> parts = row.split();
        assertThat(parts).hasSize(3);
        assertThat(root.children()).hasSize(3);
        assertThat(parts.get(0).get(Side.AGENT)).isNotNull();
        assertThat(parts.get(0).get(Side.BOOK)).isNull();
        // as separate elements the agent's is new, the book's stays and the base's is gone
        assertThat(result(root)).isEqualTo("[{\"name\":\"a\",\"v\":2},{\"name\":\"a\",\"v\":1}]");

        assertThat(parts.get(0).matchCandidates()).hasSize(2);
        JsonMergeNode joined = parts.get(0).match(parts.get(1));
        assertThat(joined.get(Side.AGENT)).isNotNull();
        assertThat(joined.get(Side.BOOK)).isNotNull();
        assertThat(joined.get(Side.BASE)).isNull();
        assertThat(root.children()).hasSize(2);
        // an element does not match itself, or one that has an element of the same version
        assertThatThrownBy(() -> joined.match(joined)).isInstanceOf(IllegalArgumentException.class);
    }

    // --- the texts of the merge

    @Test
    void textsAreMerged() {
        assertThat(JsonMerge.merge("{\"a\":1,\"b\":2}", "{\"a\":5,\"b\":2}", "{\"a\":1,\"b\":9}")).isEqualTo("{\"a\":5,\"b\":9}");
        assertThat(JsonMerge.conflicts("{\"a\":1}", "{\"a\":5}", "{\"a\":6}")).isTrue();
        assertThat(JsonMerge.conflicts("{\"a\":1,\"b\":1}", "{\"a\":5,\"b\":1}", "{\"a\":1,\"b\":6}")).isFalse();
        // nothing at all
        assertThat(JsonMerge.merge("", "{\"a\":1}", "")).isEqualTo("{\"a\":1}");
        assertThat(JsonMerge.merge("{\"a\":1}", "", "{\"a\":1}")).isEmpty();
    }

    @Test
    void textThatIsNotJsonIsOneValue() {
        assertThat(JsonMerge.merge("one", "two", "one")).isEqualTo("two");
        assertThat(JsonMerge.conflicts("one", "two", "three")).isTrue();
        assertThat(JsonMerge.conflicts("one", "two", "one")).isFalse();
    }
}
