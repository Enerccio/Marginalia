package com.github.enerccio.marginalia.templates;

import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateContext;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables.Scope;
import com.github.enerccio.marginalia.domain.templates.macros.Macros;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SillyTavern compatible macros rendered through the template service.
 */
class MacroRenderingTest extends TemplateTestBase {

    @Nested
    class Names {

        @Test
        void participants() throws Exception {
            assertThat(render("{{user}}|{{char}}|{{group}}|{{notChar}}")).isEqualTo("Alice|Alice|Alice, Bob, Carol|Bob, Carol");
        }

        @Test
        void aliasesAndCaseInsensitivity() throws Exception {
            assertThat(render("{{User}} {{CHAR}} {{charIfNotGroup}} {{groupNotMuted}}")).isEqualTo("Alice Alice Alice Alice, Bob, Carol");
        }

        @Test
        void legacyMarkers() throws Exception {
            assertThat(render("<USER> <BOT> <CHAR> <GROUP> <CHARIFNOTGROUP> <user>")).isEqualTo("Alice Alice Alice Alice, Bob, Carol Alice Alice");
            assertThat(render("<b>not a marker</b>")).isEqualTo("<b>not a marker</b>");
        }

        @Test
        void storyFields() throws Exception {
            assertThat(render("{{description}}|{{scenario}}|{{input}}"))
                    .isEqualTo("A smuggling story|A rainy harbour at dusk|Alice meets the smuggler");
        }

        @Test
        void groupFallsBackToPovWithoutPresentCharacters() throws Exception {
            context.setPresentCharacters(null);

            assertThat(render("[{{group}}][{{notChar}}]")).isEqualTo("[Alice][]");
        }

        @Test
        void notCharSplitsOnCommasAndNewlines() throws Exception {
            context.setPresentCharacters("Bob\n alice ,Carol,, Dave");

            assertThat(render("{{notChar}}")).isEqualTo("Bob, Carol, Dave");
        }

        @Test
        void emptyContext() throws Exception {
            LorebookTemplateData empty = new LorebookTemplateData();

            assertThat(render("[{{user}}][{{group}}][{{description}}][{{lastMessage}}][{{model}}][{{maxPrompt}}]", empty))
                    .isEqualTo("[][][][][][]");
        }
    }

    @Nested
    class History {

        @Test
        void lastMessageAndSummary() throws Exception {
            assertThat(render("{{lastMessage}}|{{lastCharMessage}}|{{lastMessageId}}|{{lastUserMessage}}|{{summary}}"))
                    .isEqualTo("Alice waited on the pier.|Alice waited on the pier.|1|Alice waits for the ship|Alice came to the harbour.");
        }

        @Test
        void firstMessageOfBranch() throws Exception {
            context.setStoryMessages(null);
            context.setLastInstructions(null);
            context.setLatestSummary(null);

            assertThat(render("[{{lastMessage}}][{{lastMessageId}}][{{lastUserMessage}}][{{summary}}]")).isEqualTo("[][][][]");
        }
    }

    @Nested
    class Time {

        @Test
        void currentTime() throws Exception {
            assertThat(render("{{time}}|{{date}}|{{weekday}}|{{isodate}}|{{isotime}}"))
                    .isEqualTo("2:30 PM|March 15, 2026|Sunday|2026-03-15|14:30");
        }

        @Test
        void timeWithUtcOffset() throws Exception {
            assertThat(render("{{time::UTC+2}}|{{time::UTC-5:30}}|{{time::utc+0}}|{{time::nonsense}}"))
                    .isEqualTo("4:30 PM|9:00 AM|2:30 PM|2:30 PM");
        }

        @Test
        void dateTimeFormatUsesMomentTokens() throws Exception {
            assertThat(render("{{datetimeformat::YYYY-MM-DD HH:mm:ss}}")).isEqualTo("2026-03-15 14:30:45");
            assertThat(render("{{datetimeformat::dddd [the] D. MMMM}}")).isEqualTo("Sunday the 15. March");
            assertThat(render("{{datetimeformat::ddd, MMM D YY h:mm A}}")).isEqualTo("Sun, Mar 15 26 2:30 PM");
            assertThat(render("{{datetimeformat::[It's] HH[h]}}")).isEqualTo("It's 14h");
        }

        @Test
        void idleDuration() throws Exception {
            assertThat(render("{{idleDuration}}|{{idle_duration}}")).isEqualTo("2 hours|2 hours");

            context.setLastMessageTime(null);
            assertThat(render("{{idleDuration}}")).isEqualTo("just now");
        }

        @Test
        void timeDiff() throws Exception {
            assertThat(render("{{timeDiff::2026-01-01 12:00::2026-01-01 10:00}}")).isEqualTo("in 2 hours");
            assertThat(render("{{timeDiff::2026-01-01::2026-01-08}}")).isEqualTo("7 days ago");
            assertThat(render("{{timeDiff::now::2026-03-15T14:30:30}}")).isEqualTo("in a few seconds");
            assertThat(render("{{timeDiff::2027-03-15T14:30:45Z::now}}")).isEqualTo("in a year");
            assertThat(render("[{{timeDiff::yesterday::now}}]")).isEqualTo("[]");
        }
    }

    @Nested
    class Variables {

        @Test
        void localVariables() throws Exception {
            assertThat(render("{{setvar::hp::10}}{{getvar::hp}}|{{addvar::hp::5}}{{getvar::hp}}|{{incvar::hp}}|{{decvar::hp}}|{{hasvar::hp}}|{{deletevar::hp}}{{hasvar::hp}}[{{getvar::hp}}]"))
                    .isEqualTo("10|15|16|15|true|false[]");
        }

        @Test
        void globalVariablesAreSeparateFromLocal() throws Exception {
            render("{{setglobalvar::world::Harbour}}{{setvar::world::local}}");

            assertThat(render("{{getglobalvar::world}}|{{getvar::world}}|{{$world}}|{{.world}}")).isEqualTo("Harbour|local|Harbour|local");
            assertThat(context.getVariables().get(Scope.GLOBAL, "world")).isEqualTo("Harbour");
            assertThat(context.getVariables().get(Scope.LOCAL, "world")).isEqualTo("local");

            render("{{flushglobalvar::world}}");
            assertThat(render("{{hasglobalvar::world}} {{hasvar::world}}")).isEqualTo("false true");
        }

        @Test
        void globalCounters() throws Exception {
            assertThat(render("{{incglobalvar::runs}}{{incglobalvar::runs}}{{decglobalvar::runs}}|{{addglobalvar::runs::0.5}}{{getglobalvar::runs}}"))
                    .isEqualTo("121|1.5");
        }

        @Test
        void addvarConcatenatesStrings() throws Exception {
            assertThat(render("{{setvar::list::apples}}{{addvar::list::, pears}}{{getvar::list}}")).isEqualTo("apples, pears");
            assertThat(render("{{addvar::fresh::7}}{{getvar::fresh}}")).isEqualTo("7");
        }

        @Test
        void numbersAreFormattedWithoutTrailingZeros() throws Exception {
            assertThat(render("{{setvar::n::1.50}}{{addvar::n::1.5}}{{getvar::n}}|{{setvar::m::1e3}}{{incvar::m}}"))
                    .isEqualTo("3|1001");
        }

        @Test
        void incOnNonNumberStartsFromZero() throws Exception {
            assertThat(render("{{setvar::x::abc}}{{incvar::x}}")).isEqualTo("1");
        }

        @Test
        void nestedMacroInName() throws Exception {
            assertThat(render("{{setvar::{{char}}_mood::nervous}}{{getvar::Alice_mood}}|{{getvar::{{user}}_mood}}"))
                    .isEqualTo("nervous|nervous");
        }

        @Test
        void extraSeparatorsGoToValue() throws Exception {
            assertThat(render("{{setvar::x::a::b::c}}{{getvar::x}}")).isEqualTo("a::b::c");
        }

        @Test
        void argumentSyntaxes() throws Exception {
            assertThat(render("{{setvar greeting::Hello, {{user}}!}}{{getvar greeting}}|{{getvar:greeting}}|{{getvar  ::  greeting  }}"))
                    .isEqualTo("Hello, Alice!|Hello, Alice!|Hello, Alice!");
        }

        @Test
        void scopedSetvarDedentsAndTrims() throws Exception {
            String template = "{{ setvar backstory }}\n    Line one.\n      Indented.\n    Line two.\n{{ /setvar }}{{getvar::backstory}}";

            assertThat(render(template)).isEqualTo("Line one.\n  Indented.\nLine two.");
        }

        @Test
        void scopedSetvarWithHashKeepsWhitespace() throws Exception {
            assertThat(render("[{{# setvar raw }}\n  kept  \n{{ /setvar }}{{getvar::raw}}]")).isEqualTo("[\n  kept  \n]");
        }

        @Test
        void scopedContentIsEvaluated() throws Exception {
            assertThat(render("{{setvar who}}{{user}} and {{reverse::boB}}{{/setvar}}{{getvar::who}}")).isEqualTo("Alice and Bob");
        }

        @Test
        void unsetVariableIsEmpty() throws Exception {
            assertThat(render("[{{getvar::nothing}}][{{.nothing}}][{{$nothing}}]")).isEqualTo("[][][]");
        }
    }

    @Nested
    class Shorthands {

        @Test
        void arithmetic() throws Exception {
            assertThat(render("{{.hp = 20}}{{.hp}}|{{.hp -= 5}}{{.hp}}|{{.hp += 2}}{{.hp}}|{{.hp++}}|{{.hp--}}"))
                    .isEqualTo("20|15|17|18|17");
        }

        @Test
        void incrementReturnsNewValue() throws Exception {
            assertThat(render("{{.turn++}} {{.turn++}} {{$runs++}}")).isEqualTo("1 2 1");
        }

        @Test
        void fallbacks() throws Exception {
            render("{{.empty = }}{{.zero = 0}}");

            assertThat(render("{{.nobody || Guest}}|{{.nobody ?? Guest}}|{{.empty || Guest}}|[{{.empty ?? Guest}}]|{{.zero || none}}|{{.zero ?? none}}"))
                    .isEqualTo("Guest|Guest|Guest|[]|none|0");
        }

        @Test
        void assignIfMissing() throws Exception {
            assertThat(render("{{.title ||= Wanderer}}|{{.title ??= Ignored}}|{{.title ||= Ignored}}|{{.title}}"))
                    .isEqualTo("Wanderer|Wanderer|Wanderer|Wanderer");
            assertThat(render("{{.blank = }}{{.blank ??= kept empty}}|{{.blank ||= filled}}")).isEqualTo("|filled");
        }

        @Test
        void comparisons() throws Exception {
            render("{{.hp = 17}}{{.name = Alice}}");

            assertThat(render("{{.hp == 17}} {{.hp != 17}} {{.hp > 10}} {{.hp >= 18}} {{.hp < 20}} {{.hp <= 16}}"))
                    .isEqualTo("true false true false true false");
            assertThat(render("{{.name == {{user}}}} {{.name > 3}} {{.missing < 1}}")).isEqualTo("true false false");
        }

        @Test
        void valueMayContainMacros() throws Exception {
            assertThat(render("{{.log = start}}{{.log += {{noop}} -> {{user}}}}{{.log}}")).isEqualTo("start -> Alice");
        }

        @Test
        void minusWithNonNumberIsIgnored() throws Exception {
            assertThat(render("{{.hp = 5}}{{.hp -= lots}}{{.hp}}")).isEqualTo("5");
        }

        @Test
        void hashKeepsShorthandWorking() throws Exception {
            assertThat(render("{{#.x = 1}}{{.x}}")).isEqualTo("1");
        }

        @Test
        void invalidShorthandIsLeftToHandlebars() throws Exception {
            // "{{.}}" is the handlebars current-context reference, not a variable
            assertThat(render("{{#povCharacter}}{{.}}{{/povCharacter}}")).isEqualTo("Alice");
        }
    }

    @Nested
    class Conditionals {

        @Test
        void macroConditions() throws Exception {
            assertThat(render("{{if user}}yes{{else}}no{{/if}}")).isEqualTo("yes");
            context.setPovCharacter(null);
            assertThat(render("{{if user}}yes{{else}}no{{/if}}")).isEqualTo("no");
        }

        @Test
        void negation() throws Exception {
            assertThat(render("{{if !user}}no pov{{else}}pov{{/if}}|{{if !personality}}ignored is empty{{/if}}|{{if !!user}}double{{/if}}"))
                    .isEqualTo("pov|ignored is empty|double");
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "off", "false", "no", "OFF", "False"})
        void falsyLiterals(String value) throws Exception {
            assertThat(render("{{if " + value + "}}WRONG{{else}}falsy{{/if}}")).isEqualTo("falsy");
        }

        @Test
        void plainTextIsTruthy() throws Exception {
            assertThat(render("{{if anything}}truthy{{/if}}|{{IF yes}}upper{{/IF}}")).isEqualTo("truthy|upper");
        }

        @Test
        void propertyConditions() throws Exception {
            assertThat(render("{{if sceneSetting}}scene{{/if}}|{{if manuscriptName}}name{{/if}}")).isEqualTo("scene|name");
            data.setSceneSetting(null);
            context.setSceneSetting(null);
            assertThat(render("[{{if sceneSetting}}scene{{/if}}]")).isEqualTo("[]");
        }

        @Test
        void variableConditions() throws Exception {
            render("{{.mood = calm}}{{.hp = 17}}");

            assertThat(render("{{if .mood}}mood {{.mood}}{{/if}}|{{if !$nothing}}no global{{/if}}|{{if .missing}}WRONG{{else}}missing{{/if}}"))
                    .isEqualTo("mood calm|no global|missing");
            assertThat(render("{{if {{.hp > 10}} }}above{{else}}WRONG{{/if}}|{{if .hp > 20}}WRONG{{else}}below{{/if}}"))
                    .isEqualTo("above|below");
        }

        @Test
        void nestedBlocksAreDedented() throws Exception {
            String template = """
                    {{ if scenario }}
                        scene:
                        {{ if {{.mood == calm}} }}
                            calm
                        {{ else }}
                            restless
                        {{ /if }}
                    {{ else }}
                        no scene
                    {{ /if }}""";

            assertThat(render(template)).isEqualTo("scene:\nrestless");
            assertThat(render("{{.mood = calm}}" + template)).isEqualTo("scene:\ncalm");
        }

        @Test
        void hashKeepsWhitespace() throws Exception {
            assertThat(render("[{{#if user}}\n  kept\n{{/if}}]")).isEqualTo("[\n  kept\n]");
        }

        @Test
        void inlineForm() throws Exception {
            assertThat(render("{{if user::short form}}{{if !user::WRONG}}|{{if user::a::b}}")).isEqualTo("short form|a::b");
        }

        @Test
        void inlineFormInsideArgument() throws Exception {
            assertThat(render("{{setvar::x::{{if user::has}}{{if !user::not}}}}{{getvar::x}}")).isEqualTo("has");
        }

        @Test
        void handlebarsIfStillWorks() throws Exception {
            assertThat(render("{{#if povCharacter}}OK{{else}}no POV{{/if}}")).isEqualTo("OK");
        }

        @Test
        void unterminatedIfIsLiteral() throws Exception {
            assertThat(render("{{if user}}no close")).isEqualTo("{{if user}}no close");
            assertThat(render("{{if}}x")).isEqualTo("{{if}}x");
        }

        @Test
        void conditionalSideEffectsOnlyInTakenBranch() throws Exception {
            render("{{if user}}{{.taken = yes}}{{else}}{{.skipped = yes}}{{/if}}");

            assertThat(render("{{.taken}}|[{{.skipped}}]")).isEqualTo("yes|[]");
        }
    }

    @Nested
    class Randomness {

        @Test
        void randomChoosesFromArguments() throws Exception {
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                seen.add(render("{{random::red::green::blue}}"));
            }
            assertThat(seen).containsExactlyInAnyOrder("red", "green", "blue");
        }

        @Test
        void randomLegacyAndScopedForms() throws Exception {
            for (int i = 0; i < 20; i++) {
                assertThat(render("{{random:north,south}}")).isIn("north", "south");
                assertThat(render("{{random}}one, two{{/random}}")).isIn("one", "two");
                assertThat(render("{{random::solo}}")).isEqualTo("solo");
            }
        }

        @Test
        void pickIsStablePerSeedAndPosition() throws Exception {
            String template = "{{pick::sword::axe::bow::staff}}";
            String first = render(template);

            for (int i = 0; i < 10; i++) {
                assertThat(render(template)).isEqualTo(first);
            }

            Set<String> bySeed = new HashSet<>();
            for (int i = 0; i < 40; i++) {
                context.setPickSeed("manuscript-" + i);
                bySeed.add(render(template));
            }
            assertThat(bySeed).hasSizeGreaterThan(1).isSubsetOf("sword", "axe", "bow", "staff");
        }

        @Test
        void pickPositionsAreIndependent() throws Exception {
            Set<String> pairs = new HashSet<>();
            for (int i = 0; i < 40; i++) {
                context.setPickSeed("seed-" + i);
                String[] picked = render("{{pick::a::b::c::d}}|{{pick::a::b::c::d}}").split("\\|");
                pairs.add(String.valueOf(picked[0].equals(picked[1])));
            }
            assertThat(pairs).contains("false");
        }

        @Test
        void diceRolls() throws Exception {
            for (int i = 0; i < 200; i++) {
                assertThat(Integer.parseInt(render("{{roll::1d20}}"))).isBetween(1, 20);
                assertThat(Integer.parseInt(render("{{roll::2d6+3}}"))).isBetween(5, 15);
                assertThat(Integer.parseInt(render("{{roll::3d4-10}}"))).isBetween(-7, 2);
                assertThat(Integer.parseInt(render("{{roll d100}}"))).isBetween(1, 100);
                assertThat(Integer.parseInt(render("{{roll:6}}"))).isBetween(1, 6);
            }
        }

        @Test
        void invalidDiceRenderEmpty() throws Exception {
            assertThat(render("[{{roll::abc}}][{{roll::0d6}}][{{roll::1001d6}}][{{roll::1d0}}]")).isEqualTo("[][][][]");
        }

        @Test
        void seededRandomIsReproducible() throws Exception {
            String template = "{{random::a::b::c::d::e}}{{roll::1d1000}}";
            context.setRandom(new Random(7));
            String first = render(template);
            context.setRandom(new Random(7));

            assertThat(render(template)).isEqualTo(first);
        }
    }

    @Nested
    class Runtime {

        @Test
        void tokensAndModel() throws Exception {
            assertThat(render("{{model}}|{{maxContextTokens}}|{{maxContext}}|{{maxResponseTokens}}|{{maxResponse}}|{{maxPrompt}}|{{lastGenerationType}}"))
                    .isEqualTo("mock-model|8192|8192|1024|1024|7168|normal");
        }

        @Test
        void maxPromptWithoutResponseLimit() throws Exception {
            context.setMaxResponseTokens(null);

            assertThat(render("{{maxPrompt}}|[{{maxResponseTokens}}]")).isEqualTo("8192|[]");
        }
    }

    @Nested
    class Utility {

        @Test
        void whitespace() throws Exception {
            assertThat(render("a{{newline}}b{{newline::2}}c")).isEqualTo("a\nb\n\nc");
            assertThat(render("x{{space::5}}y{{space}}z{{space::x}}!")).isEqualTo("x     y z !");
            assertThat(render("[{{newline::0}}][{{space::-3}}]")).isEqualTo("[][]");
        }

        @Test
        void reverse() throws Exception {
            assertThat(render("{{reverse::Marginalia}}")).isEqualTo("ailanigraM");
            assertThat(render("{{ reverse }}\n    scoped reverse\n{{ /reverse }}")).isEqualTo("esrever depocs");
            assertThat(render("{{reverse::{{user}}}}")).isEqualTo("ecilA");
        }

        @Test
        void noopAndComments() throws Exception {
            assertThat(render("[{{noop}}][{{// invisible {{user}} }}]")).isEqualTo("[][]");
            assertThat(render("a{{//}}\nhidden {{setvar::x::1}}\n{{///}}b[{{getvar::x}}]")).isEqualTo("ab[]");
        }

        @Test
        void escapedBraces() throws Exception {
            assertThat(render("\\{\\{notAMacro\\}\\}")).isEqualTo("{{notAMacro}}");
            assertThat(render("\\{\\{user\\}\\}")).isEqualTo("{{user}}");
        }

        @Test
        void trimRemovesSurroundingNewlines() throws Exception {
            assertThat(render("before trim\n\n{{trim}}\n\nafter trim")).isEqualTo("before trimafter trim");
            assertThat(render("{{setvar::a::1}}\n{{setvar::b::2}}\n{{trim}}\nresult {{getvar::a}}{{getvar::b}}"))
                    .isEqualTo("result 12");
        }
    }

    @Nested
    class SillyTavernCompatibility {

        @Test
        void sillyTavernOnlyMacrosRenderEmpty() throws Exception {
            assertThat(render("[{{persona}}][{{charPrompt}}][{{outlet::x}}][{{instructUserPrefix}}][{{mesExamples}}][{{authorsNote}}]"))
                    .isEqualTo("[][][][][][]");
            assertThat(render("{{isMobile}} {{hasExtension::foo}}")).isEqualTo("false false");
        }

        @Test
        void everyRegisteredMacroRenders() throws Exception {
            for (Macros.MacroDefinition definition : Macros.all()) {
                if (Macros.IF.equals(definition.name())) {
                    continue;
                }
                StringBuilder call = new StringBuilder("{{").append(definition.name());
                for (int i = 0; i < Math.max(definition.minArgs(), 0); i++) {
                    call.append("::1");
                }
                call.append("}}");
                String output = render("[" + call + "]");
                assertThat(output).as(call.toString()).startsWith("[").endsWith("]").doesNotContain("{{").doesNotContain("Error");
            }
        }

        @Test
        void unknownMacroWithArgumentsStaysLiteral() throws Exception {
            assertThat(render("{{fooBar::baz}} {{fooBar:baz}}")).isEqualTo("{{fooBar::baz}} {{fooBar:baz}}");
        }

        @Test
        void unknownMacroWithoutArgumentsIsReported() throws Exception {
            assertThat(render("{{fooBar}}")).isEqualTo("Error");
        }

        @Test
        void macroWithMissingArgumentsDoesNotFail() throws Exception {
            assertThat(render("[{{getvar}}][{{setvar::onlyName}}]")).doesNotContain("Error");
        }
    }

    @Test
    void macrosWorkInEveryTemplateDataType() throws Exception {
        TemplateContext shared = newContext();
        for (var values : List.of(new com.github.enerccio.marginalia.domain.templates.MasterTemplateData(),
                new com.github.enerccio.marginalia.domain.templates.UserPromptData(),
                new com.github.enerccio.marginalia.domain.templates.SummaryTemplateData(),
                new LorebookTemplateData())) {
            values.setTemplateContext(shared);
            assertThat(render("{{user}} {{.n++}} {{datetimeformat::YYYY}}", values)).as(values.getClass().getSimpleName())
                    .startsWith("Alice ").endsWith(" 2026");
        }
        assertThat(shared.getVariables().get(Scope.LOCAL, "n")).isEqualTo("4");
    }
}
