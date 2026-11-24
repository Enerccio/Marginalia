package com.github.enerccio.marginalia.domain.templates;

import com.github.enerccio.marginalia.domain.templates.TemplateVariables.Scope;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Package level helpers behind the macros: moment.js format conversion, duration humanizing, number handling,
 * truthiness and variable storage.
 */
class TemplateHelpersTest {

    private static final ZonedDateTime TIME = ZonedDateTime.of(2026, 3, 5, 9, 7, 3, 45_000_000, ZoneOffset.ofHours(2));

    private static String format(String moment) {
        return TIME.format(DateTimeFormatter.ofPattern(MomentFormat.toJavaPattern(moment), Locale.ENGLISH));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "YYYY-MM-DD HH:mm:ss|2026-03-05 09:07:03",
            "YY/M/D H:m:s|26/3/5 9:7:3",
            "dddd, MMMM Do|Thursday, March 5",
            "ddd MMM|Thu Mar",
            "hh:mm A|09:07 AM",
            "h a|9 AM",
            "HH:mm:ss.SSS|09:07:03.045",
            "DDDD|064",
            "Z|+02:00",
            "ZZ|+0200",
            "[Today is] dddd|Today is Thursday",
            "[It's] YYYY|It's 2026",
            "YYYY [year]|2026 year",
    })
    void momentFormat(String moment, String expected) {
        assertThat(format(moment)).isEqualTo(expected);
    }

    @Test
    void momentFormatQuotesLettersThatAreNotTokens() {
        assertThat(MomentFormat.toJavaPattern("YYYY x")).isEqualTo("yyyy' x'");
        assertThat(MomentFormat.toJavaPattern("HH:mm")).isEqualTo("HH:mm");
    }

    @ParameterizedTest
    @CsvSource({
            "0, a few seconds",
            "44, a few seconds",
            "45, a minute",
            "89, a minute",
            "90, 2 minutes",
            "2640, 44 minutes",
            "2700, an hour",
            "5399, an hour",
            "5400, 2 hours",
            "75600, 21 hours",
            "79200, a day",
            "129600, 2 days",
            "2160000, 25 days",
            "2246400, a month",
            "3888000, 2 months",
            "27561600, 10 months",
            "27648000, a year",
            "47347200, 2 years",
            "315576000, 10 years",
    })
    void humanize(long seconds, String expected) {
        assertThat(TemplateData.humanize(Duration.ofSeconds(seconds), false)).isEqualTo(expected);
    }

    @Test
    void humanizeWithSuffix() {
        assertThat(TemplateData.humanize(Duration.ofHours(3), true)).isEqualTo("in 3 hours");
        assertThat(TemplateData.humanize(Duration.ofHours(-3), true)).isEqualTo("3 hours ago");
        assertThat(TemplateData.humanize(Duration.ZERO, true)).isEqualTo("in a few seconds");
    }

    @Test
    void numbers() {
        assertThat(TemplateData.parseNumber(" 12.50 ")).isEqualByComparingTo("12.5");
        assertThat(TemplateData.parseNumber("-3")).isEqualByComparingTo("-3");
        assertThat(TemplateData.parseNumber("1e2")).isEqualByComparingTo("100");
        assertThat(TemplateData.parseNumber("twelve")).isNull();
        assertThat(TemplateData.parseNumber("")).isNull();
        assertThat(TemplateData.parseNumber(null)).isNull();

        assertThat(TemplateData.formatNumber(new BigDecimal("12.500"))).isEqualTo("12.5");
        assertThat(TemplateData.formatNumber(new BigDecimal("1E+3"))).isEqualTo("1000");
        assertThat(TemplateData.formatNumber(new BigDecimal("0.000"))).isEqualTo("0");
        assertThat(TemplateData.formatNumber(new BigDecimal("-7"))).isEqualTo("-7");
    }

    @Test
    void truthiness() {
        assertThat(TemplateData.isTruthy(null)).isFalse();
        assertThat(TemplateData.isTruthy(false)).isFalse();
        assertThat(TemplateData.isTruthy(true)).isTrue();
        assertThat(TemplateData.isTruthy(List.of())).isFalse();
        assertThat(TemplateData.isTruthy(List.of(1))).isTrue();
        for (String falsy : List.of("", "  ", "false", "FALSE", "0", "off", "No", " no ")) {
            assertThat(TemplateData.isTruthy(falsy)).as(falsy).isFalse();
        }
        for (String truthy : List.of("1", "yes", "true", "anything", "0.0", "null")) {
            assertThat(TemplateData.isTruthy(truthy)).as(truthy).isTrue();
        }
    }

    @Test
    void variablesRoundTripThroughAttributes() {
        TemplateVariables variables = new TemplateVariables();
        variables.set(Scope.LOCAL, "hp", "17");
        variables.set(Scope.LOCAL, "note", "multi\nline \"quoted\"");
        variables.set(Scope.GLOBAL, "world", "Testland");

        JsonObject messageAttributes = new JsonObject();
        JsonObject manuscriptAttributes = new JsonObject();
        variables.storeTo(Scope.LOCAL, messageAttributes);
        variables.storeTo(Scope.GLOBAL, manuscriptAttributes);

        TemplateVariables loaded = new TemplateVariables();
        loaded.loadFrom(Scope.LOCAL, JsonParser.parseString(messageAttributes.toString()).getAsJsonObject());
        loaded.loadFrom(Scope.GLOBAL, JsonParser.parseString(manuscriptAttributes.toString()).getAsJsonObject());

        assertThat(loaded.getLocal()).containsExactly(
                java.util.Map.entry("hp", "17"), java.util.Map.entry("note", "multi\nline \"quoted\""));
        assertThat(loaded.getGlobal()).containsExactly(java.util.Map.entry("world", "Testland"));
        assertThat(messageAttributes.has(TemplateVariables.ATTRIBUTE_KEY)).isTrue();
    }

    @Test
    void variablesLoadToleratesForeignValues() {
        JsonObject attributes = JsonParser.parseString(
                "{\"templateVariables\": {\"n\": 5, \"b\": true, \"o\": {\"x\": 1}, \"nil\": null}}").getAsJsonObject();

        TemplateVariables variables = new TemplateVariables();
        variables.loadFrom(Scope.LOCAL, attributes);
        variables.loadFrom(Scope.LOCAL, new JsonObject());
        variables.loadFrom(Scope.LOCAL, JsonParser.parseString("{\"templateVariables\": \"broken\"}").getAsJsonObject());
        variables.loadFrom(Scope.LOCAL, null);

        assertThat(variables.getLocal()).containsOnlyKeys("n", "b", "o");
        assertThat(variables.get(Scope.LOCAL, "n")).isEqualTo("5");
        assertThat(variables.get(Scope.LOCAL, "b")).isEqualTo("true");
        assertThat(variables.get(Scope.LOCAL, "o")).isEqualTo("{\"x\":1}");
    }

    @Test
    void variablesIgnoreEmptyNamesAndNullValues() {
        TemplateVariables variables = new TemplateVariables();
        variables.set(Scope.LOCAL, "", "x");
        variables.set(Scope.LOCAL, null, "x");
        variables.set(Scope.LOCAL, "empty", null);

        assertThat(variables.getLocal()).containsOnlyKeys("empty");
        assertThat(variables.get(Scope.LOCAL, "empty")).isEmpty();
    }

    @Test
    void variablesCopyIsIndependent() {
        TemplateVariables variables = new TemplateVariables();
        variables.set(Scope.LOCAL, "a", "1");

        TemplateVariables copy = variables.copy();
        copy.set(Scope.LOCAL, "a", "2");
        copy.set(Scope.GLOBAL, "b", "3");

        assertThat(variables.get(Scope.LOCAL, "a")).isEqualTo("1");
        assertThat(variables.has(Scope.GLOBAL, "b")).isFalse();
    }

    @Test
    void contextForkCopiesEverything() {
        TemplateContext context = new TemplateContext();
        context.setPovCharacter("Alice");
        context.setStoryMessages(new java.util.ArrayList<>(List.of("one")));
        context.getVariables().set(Scope.LOCAL, "x", "1");

        TemplateContext fork = context.fork();
        fork.getStoryMessages().add("two");
        fork.getVariables().set(Scope.LOCAL, "x", "2");

        assertThat(fork.getPovCharacter()).isEqualTo("Alice");
        assertThat(context.getStoryMessages()).containsExactly("one");
        assertThat(context.getVariables().get(Scope.LOCAL, "x")).isEqualTo("1");
    }
}
