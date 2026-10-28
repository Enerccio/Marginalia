package com.github.enerccio.marginalia.manuscript;

import com.github.enerccio.marginalia.domain.components.PartMerge;
import com.github.enerccio.marginalia.domain.components.PartMerge.AttributeChange;
import com.github.enerccio.marginalia.domain.components.PartMerge.Join;
import com.github.enerccio.marginalia.domain.components.PartMerge.Kind;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The pure parts of merging a part into the part before it.
 */
class PartMergeTest {

    private static JsonObject json(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }

    @Test
    void sharedStartOfThePromptIsKeptOnce() {
        assertThat(PartMerge.combineText("Write the scene. Keep it short", "Write the scene. Add rain"))
                .isEqualTo("Write the scene. Keep it short\nAdd rain");
    }

    @Test
    void sharedStartIsNotCutInsideAWord() {
        assertThat(PartMerge.combineText("Writing a letter", "Write a poem")).isEqualTo("Writing a letter\nWrite a poem");
    }

    @Test
    void sameTextStaysOnce() {
        assertThat(PartMerge.combineText("Continue", "Continue")).isEqualTo("Continue");
    }

    @Test
    void textThatIsTheStartOfTheOtherGivesTheLongerOne() {
        assertThat(PartMerge.combineText("Continue", "Continue with the duel")).isEqualTo("Continue with the duel");
        assertThat(PartMerge.combineText("Continue with the duel", "Continue")).isEqualTo("Continue with the duel");
    }

    @Test
    void differentTextsAreOnSeparateLines() {
        assertThat(PartMerge.combineText("Alice", "Bob")).isEqualTo("Alice\nBob");
    }

    @Test
    void blankSideIsLeftOut() {
        assertThat(PartMerge.combineText(null, "Bob")).isEqualTo("Bob");
        assertThat(PartMerge.combineText("Alice", " ")).isEqualTo("Alice");
        assertThat(PartMerge.combineText("", null)).isNull();
    }

    @Test
    void joinIsSuggestedFromTheEndOfTheText() {
        assertThat(Join.suggest("He left.", "She stayed.")).isEqualTo(Join.PARAGRAPH);
        assertThat(Join.suggest("He left and", "she stayed.")).isEqualTo(Join.SPACE);
        assertThat(Join.suggest("He left and ", "she stayed.")).isEqualTo(Join.NONE);
        assertThat(Join.suggest("He left and", " she stayed.")).isEqualTo(Join.NONE);
    }

    @Test
    void textsAreJoined() {
        assertThat(PartMerge.join("a", "b", Join.PARAGRAPH)).isEqualTo("a\n\nb");
        assertThat(PartMerge.join("a", "b", Join.NONE)).isEqualTo("ab");
        assertThat(PartMerge.join("", "b", Join.PARAGRAPH)).isEqualTo("b");
        assertThat(PartMerge.join("a", null, Join.PARAGRAPH)).isEqualTo("a");
    }

    @Test
    void attributesAreComparedValueByValue() {
        JsonObject previous = json("{\"templateVariables\":{\"mood\":\"calm\",\"place\":\"inn\"},\"plugin\":{\"x\":1}}");
        JsonObject later = json("{\"templateVariables\":{\"mood\":\"angry\",\"place\":\"inn\",\"gold\":\"5\"}}");

        List<AttributeChange> changes = PartMerge.diffAttributes(previous, later);

        assertThat(changes).extracting(AttributeChange::name).containsExactly(
                "plugin › x", "templateVariables › gold", "templateVariables › mood", "templateVariables › place");
        assertThat(changes).extracting(AttributeChange::kind).containsExactly(
                Kind.ONLY_PREVIOUS, Kind.ONLY_LATER, Kind.DIFFERENT, Kind.SAME);
    }

    @Test
    void laterValuesWinUnlessTheAuthorChoosesOtherwise() {
        JsonObject previous = json("{\"templateVariables\":{\"mood\":\"calm\",\"place\":\"inn\"},\"plugin\":{\"x\":1}}");
        JsonObject later = json("{\"templateVariables\":{\"mood\":\"angry\",\"place\":\"inn\",\"gold\":\"5\"}}");

        assertThat(PartMerge.mergeAttributes(previous, later, Set.of(), Set.of())).isEqualTo(
                json("{\"plugin\":{\"x\":1},\"templateVariables\":{\"gold\":\"5\",\"mood\":\"angry\",\"place\":\"inn\"}}"));
        assertThat(PartMerge.mergeAttributes(previous, later, Set.of(List.of("templateVariables", "mood")), Set.of())).isEqualTo(
                json("{\"plugin\":{\"x\":1},\"templateVariables\":{\"gold\":\"5\",\"mood\":\"calm\",\"place\":\"inn\"}}"));
        assertThat(PartMerge.mergeAttributes(previous, later, Set.of(), Set.of(List.of("plugin", "x"), List.of("templateVariables", "gold")))).isEqualTo(
                json("{\"templateVariables\":{\"mood\":\"angry\",\"place\":\"inn\"}}"));
    }

    @Test
    void emptyAttributesGiveNothing() {
        assertThat(PartMerge.diffAttributes(new JsonObject(), new JsonObject())).isEmpty();
        assertThat(PartMerge.mergeAttributes(new JsonObject(), new JsonObject(), Set.of(), Set.of())).isEqualTo(new JsonObject());
    }
}
