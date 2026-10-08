package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.service.impl.SillyTavernEntryConverter.Filter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SillyTavern key logic compiled into Marginalia filters. Filters are checked by behaviour: they are matched exactly
 * like {@code ProcessLorebookStep} does (TEXT: case-insensitive substring, REGEX: case-insensitive, unicode case,
 * dotall, {@code find()}).
 */
class SillyTavernEntryConverterTest {

    private static JsonObject entry(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private static Filter filter(String json, List<String> primary, List<String> secondary) {
        return SillyTavernEntryConverter.filter(entry(json), primary, secondary);
    }

    /**
     * Same matching as ProcessLorebookStep.promptMatches.
     */
    private static boolean matches(Filter filter, String prompt) {
        if (filter == null) {
            return true;
        }
        if (filter.mode() == FilteringMode.TEXT) {
            return Strings.CI.contains(prompt, filter.filtering());
        }
        return Pattern.compile(filter.filtering(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL)
                .matcher(prompt).find();
    }

    // ---- primary keys ----------------------------------------------------------------------------------------------

    @Test
    void singlePlainKeyIsTextFilter() {
        Filter filter = filter("{}", List.of("dragon"), List.of());

        assertThat(filter).isEqualTo(new Filter("dragon", FilteringMode.TEXT));
        assertThat(matches(filter, "A DRAGONFLY")).isTrue();
        assertThat(matches(filter, "a wyrm")).isFalse();
    }

    @Test
    void severalPrimaryKeysMatchAny() {
        Filter filter = filter("{}", List.of("dragon", "wyrm", "drake"), List.of());

        assertThat(filter.mode()).isEqualTo(FilteringMode.REGEX);
        assertThat(matches(filter, "The Wyrm sleeps")).isTrue();
        assertThat(matches(filter, "a drake\nflies")).isTrue();
        assertThat(matches(filter, "a horse")).isFalse();
    }

    @Test
    void keysAreLiteralText() {
        Filter filter = filter("{}", List.of("a.b", "(x)", "c++", "\\E"), List.of());

        assertThat(matches(filter, "xa.bx")).isTrue();
        assertThat(matches(filter, "axb")).isFalse();
        assertThat(matches(filter, "call (x) now")).isTrue();
        assertThat(matches(filter, "I like c++")).isTrue();
        assertThat(matches(filter, "path \\E end")).isTrue();
    }

    @Test
    void noKeysOrConstantMeansAlwaysActive() {
        assertThat(filter("{}", List.of(), List.of("ignored"))).isNull();
        assertThat(filter("{\"constant\": true}", List.of("dragon"), List.of())).isNull();
    }

    // ---- case and whole words --------------------------------------------------------------------------------------

    @Test
    void caseSensitive() {
        Filter filter = filter("{\"caseSensitive\": true}", List.of("King"), List.of());

        assertThat(filter.mode()).isEqualTo(FilteringMode.REGEX);
        assertThat(matches(filter, "the King arrives")).isTrue();
        assertThat(matches(filter, "the king arrives")).isFalse();
    }

    @Test
    void wholeWords() {
        Filter filter = filter("{\"matchWholeWords\": true}", List.of("king", "žena"), List.of());

        assertThat(matches(filter, "the king, arrives")).isTrue();
        assertThat(matches(filter, "King")).isTrue();
        assertThat(matches(filter, "the kingdom falls")).isFalse();
        assertThat(matches(filter, "a viking")).isFalse();
        assertThat(matches(filter, "mladá žena")).isTrue();
        assertThat(matches(filter, "ženatý")).as("unicode word boundary").isFalse();
    }

    @Test
    void wholeWordsWithPunctuationKey() {
        Filter filter = filter("{\"matchWholeWords\": true}", List.of("c++"), List.of());

        assertThat(matches(filter, "I write c++ code")).isTrue();
        assertThat(matches(filter, "abc++")).isFalse();
    }

    // ---- regex keys ------------------------------------------------------------------------------------------------

    @Test
    void regexKeysKeepTheirPattern() {
        Filter filter = filter("{}", List.of("/dragons?\\b/i"), List.of());

        assertThat(filter.mode()).isEqualTo(FilteringMode.REGEX);
        assertThat(matches(filter, "Two DRAGONS")).isTrue();
        assertThat(matches(filter, "dragonfly")).isFalse();
    }

    @Test
    void regexKeyWithoutIFlagIsCaseSensitive() {
        Filter filter = filter("{}", List.of("/Dragon/"), List.of());

        assertThat(matches(filter, "a Dragon")).isTrue();
        assertThat(matches(filter, "a dragon")).isFalse();
    }

    @Test
    void regexAndPlainKeysCombine() {
        Filter filter = filter("{}", List.of("/kings?/i", "throne"), List.of());

        assertThat(matches(filter, "the throne room")).isTrue();
        assertThat(matches(filter, "Kings meet")).isTrue();
        assertThat(matches(filter, "a chair")).isFalse();
    }

    @Test
    void invalidRegexKeyIsMatchedLiterally() {
        Filter filter = filter("{}", List.of("/(unclosed/"), List.of());

        assertThat(matches(filter, "text /(unclosed/ text")).isTrue();
        assertThat(matches(filter, "unclosed")).isFalse();
    }

    // ---- secondary keys --------------------------------------------------------------------------------------------

    @Test
    void andAny() {
        Filter filter = filter("{\"selective\": true, \"selectiveLogic\": 0}", List.of("king"), List.of("crown", "throne"));

        assertThat(matches(filter, "the king and his crown")).isTrue();
        assertThat(matches(filter, "the throne\nof the king")).as("order and lines don't matter").isTrue();
        assertThat(matches(filter, "the king alone")).isFalse();
        assertThat(matches(filter, "a crown alone")).isFalse();
    }

    @Test
    void andAll() {
        Filter filter = filter("{\"selectiveLogic\": 3}", List.of("king"), List.of("crown", "throne"));

        assertThat(matches(filter, "king, crown and throne")).isTrue();
        assertThat(matches(filter, "king and crown")).isFalse();
    }

    @Test
    void notAny() {
        Filter filter = filter("{\"selectiveLogic\": 2}", List.of("king"), List.of("dead", "exiled"));

        assertThat(matches(filter, "the king rules")).isTrue();
        assertThat(matches(filter, "the king is dead")).isFalse();
        assertThat(matches(filter, "the exiled king")).isFalse();
    }

    @Test
    void notAll() {
        Filter filter = filter("{\"selectiveLogic\": 1}", List.of("king"), List.of("dead", "buried"));

        assertThat(matches(filter, "the king rules")).isTrue();
        assertThat(matches(filter, "the king is dead")).isTrue();
        assertThat(matches(filter, "the king is dead and buried")).isFalse();
    }

    @Test
    void secondaryKeysDefaultToAndAny() {
        Filter filter = filter("{}", List.of("king"), List.of("crown"));

        assertThat(matches(filter, "king crown")).isTrue();
        assertThat(matches(filter, "king")).isFalse();
    }

    @Test
    void secondaryKeysIgnoredWhenNotSelective() {
        Filter filter = filter("{\"selective\": false, \"selectiveLogic\": 3}", List.of("king"), List.of("crown"));

        assertThat(filter).isEqualTo(new Filter("king", FilteringMode.TEXT));
    }

    @Test
    void secondaryKeysRespectCaseAndWholeWords() {
        Filter filter = filter("{\"caseSensitive\": true, \"matchWholeWords\": true, \"selectiveLogic\": 0}",
                List.of("King"), List.of("Crown"));

        assertThat(matches(filter, "King with Crown")).isTrue();
        assertThat(matches(filter, "King with crown")).isFalse();
        assertThat(matches(filter, "King with Crowns")).isFalse();
    }

    // ---- other fields ----------------------------------------------------------------------------------------------

    @Test
    void positionMapsToInsertionMode() {
        assertThat(SillyTavernEntryConverter.insertionMode(entry("{}"))).isEqualTo(InsertionMode.IN_LORE_BLOCK);
        for (int position : new int[]{0, 1, 5, 6}) {
            assertThat(SillyTavernEntryConverter.insertionMode(entry("{\"position\": " + position + "}")))
                    .as("position " + position).isEqualTo(InsertionMode.IN_LORE_BLOCK);
        }
        for (int position : new int[]{2, 3, 4}) {
            assertThat(SillyTavernEntryConverter.insertionMode(entry("{\"position\": " + position + "}")))
                    .as("position " + position).isEqualTo(InsertionMode.BEFORE_USER_PROMPT);
        }
    }

    @Test
    void nameFallsBackToFirstKey() {
        assertThat(SillyTavernEntryConverter.name(entry("{\"comment\": \" Dragons \"}"), List.of("dragon"))).isEqualTo("Dragons");
        assertThat(SillyTavernEntryConverter.name(entry("{\"comment\": \"\"}"), List.of("dragon", "wyrm"))).isEqualTo("dragon");
        assertThat(SillyTavernEntryConverter.name(entry("{}"), List.of())).isEqualTo("Entry");
    }

    @Test
    void orderAndEnabled() {
        assertThat(SillyTavernEntryConverter.order(entry("{\"order\": 250}"))).isEqualTo(250);
        assertThat(SillyTavernEntryConverter.order(entry("{}"))).isEqualTo(100);
        assertThat(SillyTavernEntryConverter.order(entry("{\"order\": \"abc\"}"))).isEqualTo(100);
        assertThat(SillyTavernEntryConverter.enabled(entry("{\"disable\": true}"))).isFalse();
        assertThat(SillyTavernEntryConverter.enabled(entry("{}"))).isTrue();
    }

    @Test
    void unsupportedSettingsAreDescribed() {
        String notes = SillyTavernEntryConverter.unsupportedSettings(entry("""
                {"position": 4, "depth": 2, "useProbability": true, "probability": 40, "group": "weather",
                 "sticky": 3, "cooldown": 0, "delay": 1}
                """));

        assertThat(notes).startsWith("Imported from SillyTavern")
                .contains("depth 2").contains("probability 40%").contains("\"weather\"")
                .contains("sticky 3").contains("delay 1").doesNotContain("cooldown");
        assertThat(SillyTavernEntryConverter.unsupportedSettings(entry("{\"probability\": 100, \"position\": 1}"))).isNull();
        assertThat(SillyTavernEntryConverter.unsupportedSettings(entry("{\"useProbability\": false, \"probability\": 10}"))).isNull();
    }
}
