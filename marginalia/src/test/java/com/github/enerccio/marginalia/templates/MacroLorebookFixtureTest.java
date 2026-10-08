package com.github.enerccio.marginalia.templates;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Renders the "Macro Test" SillyTavern lorebook (src/test/resources/macro-test.json) the way a generation does: enabled
 * entries in order, all sharing one template data object. Each entry documents its expected output inline; this test
 * pins those expectations.
 */
class MacroLorebookFixtureTest extends TemplateTestBase {

    private final Map<String, String> payloads = new LinkedHashMap<>();
    private final Map<String, Boolean> disabled = new LinkedHashMap<>();
    private final Map<String, String> rendered = new LinkedHashMap<>();

    @BeforeEach
    void loadAndRender() throws Exception {
        JsonObject lorebook;
        try (var reader = new InputStreamReader(getClass().getResourceAsStream("/macro-test.json"), StandardCharsets.UTF_8)) {
            lorebook = JsonParser.parseReader(reader).getAsJsonObject();
        }
        List<JsonObject> entries = new ArrayList<>();
        lorebook.getAsJsonObject("entries").entrySet().forEach(e -> entries.add(e.getValue().getAsJsonObject()));
        entries.sort((a, b) -> Integer.compare(a.get("order").getAsInt(), b.get("order").getAsInt()));

        for (JsonObject entry : entries) {
            String name = entry.get("comment").getAsString();
            payloads.put(name, entry.get("content").getAsString());
            disabled.put(name, entry.get("disable").getAsBoolean());
        }

        for (Map.Entry<String, String> entry : payloads.entrySet()) {
            if (disabled.get(entry.getKey())) {
                continue;
            }
            try {
                rendered.put(entry.getKey(), render(entry.getValue()));
            } catch (Exception e) {
                // same fallback as ProcessLorebookStep: a broken entry is used as it is
                rendered.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private String entry(String prefix) {
        return rendered.entrySet().stream()
                .filter(e -> e.getKey().startsWith("MacroTest " + prefix))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No rendered entry " + prefix));
    }

    @Test
    void fixtureIsComplete() {
        assertThat(payloads).hasSize(14);
        assertThat(disabled.values()).containsOnlyOnce(true);
    }

    @Test
    void noEntryRendersWrongBranches() {
        rendered.forEach((name, output) -> {
            if (!name.contains("broken")) {
                // entries 10 and 11 print escaped / unknown macros literally on purpose
                String unescaped = output.replace("{{notAMacro}}", "").replace("{{fooBar::baz}}", "");
                assertThat(unescaped).as(name).doesNotContain("WRONG").doesNotContain("{{");
            }
        });
        assertThat(rendered.values()).noneMatch(v -> v.contains("[12 WRONG"));
    }

    @Test
    void setup() {
        assertThat(entry("01")).isEqualTo("[01 setup] branch turn #1 | manuscript run #1  (regenerate: turn stays, run grows; new part: both grow)");
    }

    @Test
    void names() {
        assertThat(entry("02")).isEqualTo("""
                [02 names]
                user=Alice | char=Alice | legacy=Alice/Alice | group=Alice, Bob, Carol | notChar=Bob, Carol
                description=A smuggling story
                scenario=A rainy harbour at dusk
                input=Alice meets the smuggler
                handlebars sections: POV is Alice | manuscript=Harbour Lights""");
    }

    @Test
    void history() {
        assertThat(entry("03")).isEqualTo("""
                [03 history]
                previous part exists, lastMessageId=1, its instructions: "Alice waits for the ship"
                summary: present (Alice came to the harbour.)""");
    }

    @Test
    void time() {
        assertThat(entry("04")).isEqualTo("""
                [04 time]
                time=2:30 PM | UTC+0=2:30 PM | date=March 15, 2026 | weekday=Sunday | iso=2026-03-15 14:30
                custom=2026-03-15 14:30:45 | [Sunday the 15. March]
                idle since last part=2 hours
                timeDiff=in 2 hours (expect "in 2 hours") | 7 days ago (expect "7 days ago")""");
    }

    @Test
    void variables() {
        assertThat(entry("05")).isEqualTo("""
                [05 variables]
                from entry 01: hero=Alice counter=5 mood=calm world=Testland
                backstory="Line one of backstory.
                Line two of backstory." (two lines, no indentation)
                counter: addvar 10 -> 15 (15), incvar -> 16 (16), decvar -> 15 (15)
                string addvar: apples, pears (apples, pears)
                nested name: nervous (nervous)
                hasvar/deletevar: true -> false (true -> false)
                local vs global: [] [Testland] ([] [Testland])
                space syntax: Hello, Alice!""");
    }

    @Test
    void shorthands() {
        assertThat(entry("06")).isEqualTo("""
                [06 shorthands]
                hp=20 | -= 5: 15 (15) | += 2: 17 (17) | ++: 18 (18) | --: 17 (17)
                fallbacks: Guest (Guest) | Guest (Guest) | Guest (Guest) | [] ([])
                assign-if: Wanderer (Wanderer) | Wanderer (Wanderer)
                compare: true false true false true false (true false true false true false)
                append with space: start -> next (start -> next)
                global: Testland (Testland)""");
    }

    @Test
    void conditionals() {
        assertThat(entry("07")).isEqualTo("""
                [07 conditionals]
                has POV character
                ignored macro is empty -> falsy -> negated OK
                mood is calm | global "nothing" not set OK
                hp above 10 OK
                0 falsy OK / off falsy OK / plain text truthy OK
                scene block (indentation removed):
                nested if: mood is calm OK
                inline: short form OK
                handlebars if still works: OK""");
    }

    @Test
    void random() {
        String output = entry("08");
        assertThat(output).matches("(?s)\\[08 random]\\n"
                + "random \\(re-rolls every generation\\): (red|green|blue) \\| legacy (north|south|east|west) \\| scoped (one|two|three)\\n"
                + "pick \\(same every generation of this manuscript\\): (sword|axe|bow|staff)\\n"
                + "dice: 1d20=\\d+ \\| 2d6\\+3=\\d+ \\(5-15\\) \\| d100=\\d+ \\| 6=\\d \\(1-6\\)");
    }

    @Test
    void runtime() {
        assertThat(entry("09")).isEqualTo("[09 runtime] model=mock-model | maxContextTokens=8192 | maxResponseTokens=1024 | maxPrompt=7168 | type=normal (normal / regenerate / swipe)");
    }

    @Test
    void utility() {
        assertThat(entry("10")).isEqualTo("[10 utility]\n"
                + "a\nb\n\nc (a and b on separate lines, empty line before c)\n"
                + "x     y (5 spaces) | ailanigraM (ailanigraM) | esrever depocs (esrever depocs)\n"
                + "noop:[] comment:[] escaped:[{{notAMacro}}] (shows the braces literally)\n"
                + "preserve whitespace:[\n  kept  \n] (newline, two spaces, kept, two spaces, newline)\n"
                + "before trimafter trim (expect \"before trimafter trim\" on one line)");
    }

    @Test
    void sillyTavernOnly() {
        assertThat(entry("11")).isEqualTo("""
                [11 sillytavern-only] persona=[] charPrompt=[] outlet=[] instruct=[] (all empty) isMobile=false hasExtension=false (false false)
                unknown macro with args stays literal: {{fooBar::baz}}
                unknown variable reports: Error (Error)""");
    }

    @Test
    void disabledEntryIsSkipped() {
        assertThat(rendered.keySet()).noneMatch(name -> name.contains("DISABLED"));
    }

    @Test
    void sharedContextAcrossEntries() {
        assertThat(entry("13")).isEqualTo("[13 shared context] disabled entry ran: no OK | hp from 06: 17 (17) | title from 06: Wanderer (Wanderer) | turn: 1");
        assertThat(context.getVariables().getLocal()).containsEntry("loreMarker", "set by lorebook entry 13");
    }

    @Test
    void brokenEntryIsUsedRaw() throws Exception {
        String raw = payloads.entrySet().stream().filter(e -> e.getKey().contains("broken")).findFirst().orElseThrow().getValue();

        assertThat(isValid(raw)).isFalse();
        assertThat(entry("14")).isEqualTo(raw);
    }

    @Test
    void renderingAgainInSameContextContinuesCounters() throws Exception {
        assertThat(render(payloads.get("MacroTest 01 - setup"))).contains("branch turn #2 | manuscript run #2");
    }
}
